package com.akilisha.oss.roya.plugins.ai.langchain;

import com.akilisha.oss.roya.plugins.ai.*;
import com.akilisha.oss.roya.plugins.ai.builder.AIWorkflowBuilder;
import com.akilisha.oss.roya.plugins.ai.langchain.services.EmbeddingService;
import com.akilisha.oss.roya.plugins.ai.langchain.services.LLMService;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.Result;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;

import java.net.ConnectException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * LangChain adapter implementation of Roya's AI interface.
 *
 * Bridges LangChain4j models to Roya's unified API.
 * Currently implements LLM, Embeddings. Vectors/RAG/Agents/NLP/Vision/Audio coming soon.
 */
public class LangChainAdapter implements AI {

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;
    private final EmbeddingModel embeddingModel;

    // AI Services instances (created lazily)
    private LLMService llmService;
    private EmbeddingService embeddingService;
    
    // Qdrant connection (lazy initialization with graceful failure)
    private QdrantConnectionState qdrantState;

    public LangChainAdapter(ChatModel chatModel, StreamingChatModel streamingChatModel, EmbeddingModel embeddingModel) {
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
        this.embeddingModel = embeddingModel;
    }
    
    /**
     * Get or create Qdrant connection with graceful failure handling.
     */
    private QdrantConnectionState getQdrantConnection() {
        if (qdrantState == null) {
            qdrantState = new QdrantConnectionState();
            String qdrantUrl = System.getenv("QDRANT_URL");
            if (qdrantUrl == null || qdrantUrl.isBlank()) {
                qdrantUrl = "http://localhost:6333"; // Default from docker-compose
            }
            qdrantState.url = qdrantUrl;
            
            try {
                // Parse URL to extract host and port
                // Note: QdrantEmbeddingStore uses gRPC (port 6334), not REST API (port 6333)
                URI uri = URI.create(qdrantUrl);
                String host = uri.getHost() != null ? uri.getHost() : "localhost";
                
                // If port is specified in URL, check if it's REST (6333) or gRPC (6334)
                // If REST port is specified, use gRPC port instead
                int port = uri.getPort() > 0 ? uri.getPort() : 6334; // Default gRPC port
                if (port == 6333) {
                    // REST API port specified, use gRPC port instead
                    port = 6334;
                }
                
                // Test connectivity using the official Qdrant Java client
                // This properly tests gRPC connectivity on port 6334
                try {
                    io.qdrant.client.QdrantClient testClient = new io.qdrant.client.QdrantClient(
                        io.qdrant.client.QdrantGrpcClient.newBuilder(host, port, false).build()
                    );
                    
                    // Try to list collections (lightweight operation that tests connectivity)
                    testClient.listCollectionsAsync().get();
                    testClient.close();
                } catch (Exception e) {
                    // Check if it's a connection error
                    Throwable cause = e.getCause();
                    if (e instanceof java.net.ConnectException || 
                        e instanceof java.net.SocketTimeoutException ||
                        (cause instanceof java.net.ConnectException) ||
                        (cause instanceof java.net.SocketTimeoutException) ||
                        e.getMessage().contains("Connection refused") ||
                        e.getMessage().contains("Connection timed out")) {
                        ConnectException connEx = new ConnectException("Cannot connect to Qdrant gRPC at " + host + ":" + port);
                        connEx.initCause(e);
                        throw connEx;
                    }
                    // Other errors (like gRPC status errors) might mean Qdrant is reachable but had an issue
                    // We'll continue and let LangChain4j's QdrantEmbeddingStore handle it
                }
                
                // Store connection info - stores will be created per collection
                qdrantState.host = host;
                qdrantState.port = port;
                qdrantState.connected = true;
            } catch (Exception e) {
                qdrantState.connected = false;
                
                // Check for specific exception types
                if (e instanceof ClassNotFoundException) {
                    qdrantState.error = "Qdrant support not available: langchain4j-qdrant module not found. " +
                        "Please ensure the dependency is added to your build file.";
                } else if (e instanceof ConnectException || 
                          e instanceof java.net.SocketTimeoutException ||
                          (e.getCause() instanceof ConnectException) ||
                          (e.getCause() instanceof java.net.SocketTimeoutException)) {
                    qdrantState.error = "Cannot connect to Qdrant at " + qdrantState.url + ": " + e.getMessage() + 
                        ". Please ensure Qdrant is running (docker compose up -d qdrant) and accessible.";
                } else {
                    qdrantState.error = "Failed to initialize Qdrant: " + e.getMessage() + 
                        ". Please check QDRANT_URL environment variable (default: http://localhost:6333). " +
                        "Note: Qdrant uses gRPC on port 6334 by default, but REST API is on 6333.";
                }
            }
        }
        return qdrantState;
    }
    
    /**
     * Helper class to track Qdrant connection state.
     */
    private static class QdrantConnectionState {
        Map<String, EmbeddingStore<TextSegment>> embeddingStores = new java.util.concurrent.ConcurrentHashMap<>();
        String host;
        int port;
        boolean connected = false;
        String url;
        String error;
        io.qdrant.client.QdrantClient qdrantClient; // For collection management
        
        /**
         * Get or create an embedding store for a specific collection.
         * Automatically creates the collection if it doesn't exist.
         */
        EmbeddingStore<TextSegment> getOrCreateStore(String collectionName, EmbeddingModel embeddingModel) {
            return embeddingStores.computeIfAbsent(collectionName, name -> {
                // Ensure collection exists before creating the store
                ensureCollectionExists(name, embeddingModel);
                
                return QdrantEmbeddingStore.builder()
                    .host(host)
                    .port(port)
                    .collectionName(name)
                    .build();
            });
        }
        
        /**
         * Ensure a Qdrant collection exists, creating it if necessary.
         */
        private void ensureCollectionExists(String collectionName, EmbeddingModel embeddingModel) {
            if (qdrantClient == null) {
                // Create Qdrant client for collection management
                qdrantClient = new io.qdrant.client.QdrantClient(
                    io.qdrant.client.QdrantGrpcClient.newBuilder(host, port, false).build()
                );
            }
            
            try {
                // Check if collection exists by trying to get its info
                qdrantClient.getCollectionInfoAsync(collectionName).get();
                // If we get here, collection exists
                return;
            } catch (java.util.concurrent.ExecutionException e) {
                // If collection doesn't exist, we'll get an exception and create it
                Throwable cause = e.getCause();
                if (cause instanceof io.grpc.StatusRuntimeException) {
                    io.grpc.StatusRuntimeException grpcEx = (io.grpc.StatusRuntimeException) cause;
                    if (grpcEx.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                        // Collection doesn't exist, create it
                        createCollection(collectionName, embeddingModel);
                        return;
                    }
                }
                // Other errors - rethrow
                throw new RuntimeException("Failed to check collection existence: " + e.getMessage(), e);
            } catch (Exception e) {
                // Check if it's a NOT_FOUND error
                if (e.getMessage() != null && e.getMessage().contains("doesn't exist")) {
                    createCollection(collectionName, embeddingModel);
                    return;
                }
                throw new RuntimeException("Failed to check collection existence: " + e.getMessage(), e);
            }
        }
        
        /**
         * Create a Qdrant collection with the appropriate vector configuration.
         */
        private void createCollection(String collectionName, EmbeddingModel embeddingModel) {
            if (embeddingModel == null) {
                throw new IllegalStateException("EmbeddingModel not configured - cannot determine vector dimension for collection creation");
            }
            
            try {
                // Get embedding dimension from the model
                int dimension = embeddingModel.dimension();
                
                // Create collection configuration using gRPC API
                io.qdrant.client.grpc.Collections.VectorParams vectorParams = io.qdrant.client.grpc.Collections.VectorParams.newBuilder()
                    .setSize(dimension)
                    .setDistance(io.qdrant.client.grpc.Collections.Distance.Cosine)
                    .build();
                
                io.qdrant.client.grpc.Collections.VectorsConfig vectorsConfig = io.qdrant.client.grpc.Collections.VectorsConfig.newBuilder()
                    .setParams(vectorParams)
                    .build();
                
                io.qdrant.client.grpc.Collections.CreateCollection createCollection = io.qdrant.client.grpc.Collections.CreateCollection.newBuilder()
                    .setCollectionName(collectionName)
                    .setVectorsConfig(vectorsConfig)
                    .build();
                
                // Create the collection
                qdrantClient.createCollectionAsync(createCollection).get();
                
                System.out.println("✅ Created Qdrant collection: " + collectionName + " (dimension: " + dimension + ")");
            } catch (Exception e) {
                throw new RuntimeException("Failed to create Qdrant collection '" + collectionName + "': " + e.getMessage(), e);
            }
        }
        
        /**
         * Close the Qdrant client when done.
         */
        void close() {
            if (qdrantClient != null) {
                try {
                    qdrantClient.close();
                } catch (Exception e) {
                    // Ignore errors on close
                }
            }
        }
    }

    /**
     * Get or create the LLM AI Service instance.
     * For streaming operations, ensure StreamingChatModel is configured.
     */
    private LLMService getLLMService() {
        if (llmService == null && chatModel != null) {
            // Use builder to support both ChatModel and StreamingChatModel
            var builder = AiServices.builder(LLMService.class)
                .chatModel(chatModel);
            
            // Add StreamingChatModel if available (for streaming operations)
            if (streamingChatModel != null) {
                builder.streamingChatModel(streamingChatModel);
            }
            
            llmService = builder.build();
        }
        return llmService;
    }

    /**
     * Get or create the Embedding Service instance.
     * Note: Embeddings don't use AI Services pattern, but we provide a wrapper for consistency.
     */
    private EmbeddingService getEmbeddingService() {
        if (embeddingService == null && embeddingModel != null) {
            embeddingService = new EmbeddingService(){
                @Override
                public float[] embed(String text) {
                    try {
                        Response<Embedding> response = embeddingModel.embed(text);
                        Embedding embedding = response.content();
                        return embedding.vector();
                    } catch (Exception e) {
                        throw new RuntimeException("Embedding failed: " + e.getMessage(), e);
                    }
                }

                @Override
                public List<float[]> embed(List<String> texts) {
                    try {
                        // Convert List<String> to List<TextSegment> for batch embedding
                        List<TextSegment> segments = texts.stream()
                                .map(TextSegment::from)
                                .collect(Collectors.toList());

                        // Perform batch embedding
                        Response<List<Embedding>> response = embeddingModel.embedAll(segments);
                        List<Embedding> embeddings = response.content();

                        // Convert List<Embedding> to List<float[]>
                        return embeddings.stream()
                                .map(Embedding::vector)
                                .collect(Collectors.toList());
                    } catch (Exception e) {
                        throw new RuntimeException("Batch embedding failed: " + e.getMessage(), e);
                    }
                }
            };
        }
        return embeddingService;
    }

    @Override
    public LLM llm() {
        return new LLM() {
            @Override
            public String ask(String systemPrompt, String userMessage) {
                return LangChainAdapter.this.ask(systemPrompt, userMessage);
            }

            @Override
            public String ask(String systemPrompt, String userMessage, AIOptions options) {
                return LangChainAdapter.this.ask(systemPrompt, userMessage, options);
            }

            @Override
            public <T> T extract(Class<T> type, String prompt) {
                return LangChainAdapter.this.extract(type, prompt);
            }

            @Override
            public <T> T extract(Class<T> type, String prompt, AIOptions options) {
                return LangChainAdapter.this.extract(type, prompt, options);
            }

            @Override
            public void stream(String systemPrompt, String userMessage, Consumer<String> onToken) {
                LangChainAdapter.this.stream(systemPrompt, userMessage, onToken);
            }

            @Override
            public void stream(String systemPrompt, String userMessage, AIOptions options, Consumer<String> onToken) {
                LangChainAdapter.this.stream(systemPrompt, userMessage, options, onToken);
            }
        };
    }

    @Override
    public Embeddings embeddings() {
        return new Embeddings() {
            @Override
            public float[] embed(String text) {
                try {
                    EmbeddingService service = getEmbeddingService();
                    if (service == null) {
                        throw new AIException("EmbeddingModel not configured");
                    }
                    return service.embed(text);
                } catch (Exception e) {
                    throw new AIException("LangChain embedding failed: " + e.getMessage(), e);
                }
            }

            @Override
            public List<float[]> embed(List<String> texts) {
                try {
                    EmbeddingService service = getEmbeddingService();
                    if (service == null) {
                        throw new AIException("EmbeddingModel not configured");
                    }
                    return service.embed(texts);
                } catch (Exception e) {
                    throw new AIException("LangChain batch embedding failed: " + e.getMessage(), e);
                }
            }
        };
    }

    @Override
    public Vectors vectors() {
        return new Vectors() {
            @Override
            public void indexPath(String collection, java.nio.file.Path directory, ChunkingOptions options) {
                var state = getQdrantConnection();
                if (!state.connected) {
                    throw new AIException("Qdrant connection failed: " + state.error);
                }
                
                if (embeddingModel == null) {
                    throw new AIException("EmbeddingModel not configured - required for vector indexing");
                }
                
                try {
                    // Get or create embedding store for this collection
                    EmbeddingStore<TextSegment> embeddingStore = state.getOrCreateStore(collection, embeddingModel);
                    
                    // Load documents from directory
                    List<dev.langchain4j.data.document.Document> documents = FileSystemDocumentLoader.loadDocuments(directory);
                    
                    // Split documents into chunks
                    DocumentSplitter splitter = DocumentSplitters.recursive(
                        options.size(),
                        options.overlap()
                    );
                    
                    List<TextSegment> segments = splitter.splitAll(documents);
                    
                    // Embed and store segments
                    List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
                    embeddingStore.addAll(embeddings, segments);
                    
                } catch (Exception e) {
                    throw new AIException("Failed to index directory: " + e.getMessage(), e);
                }
            }

            @Override
            public void index(String collection, java.util.List<VectorDoc> documents) {
                var state = getQdrantConnection();
                if (!state.connected) {
                    throw new AIException("Qdrant connection failed: " + state.error);
                }
                
                if (embeddingModel == null) {
                    throw new AIException("EmbeddingModel not configured - required for vector indexing");
                }
                
                try {
                    // Get or create embedding store for this collection
                    EmbeddingStore<TextSegment> embeddingStore = state.getOrCreateStore(collection, embeddingModel);
                    
                    // Convert VectorDoc to TextSegment
                    List<TextSegment> segments = documents.stream()
                        .map(doc -> {
                            TextSegment segment = TextSegment.from(doc.content());
                            // Add metadata
                            if (doc.metadata() != null && !doc.metadata().isEmpty()) {
                                doc.metadata().forEach((key, value) -> 
                                    segment.metadata().put(key, value.toString())
                                );
                            }
                            // Store document ID as metadata
                            if (doc.id() != null) {
                                segment.metadata().put("document_id", doc.id());
                            }
                            return segment;
                        })
                        .collect(Collectors.toList());
                    
                    // Embed segments
                    List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
                    
                    // Store in Qdrant
                    embeddingStore.addAll(embeddings, segments);
                    
                } catch (Exception e) {
                    throw new AIException("Failed to index documents: " + e.getMessage(), e);
                }
            }
        };
    }

    @Override
    public RAGApi ragApi() {
        return new RAGApi() {
            @Override
            public RAGResponse ask(String question) {
                return ask(question, RAGOptions.builder().build());
            }

            @Override
            public RAGResponse ask(String question, RAGOptions options) {
                var state = getQdrantConnection();
                if (!state.connected) {
                    throw new AIException("Qdrant connection failed: " + state.error);
                }
                
                if (embeddingModel == null) {
                    throw new AIException("EmbeddingModel not configured - required for RAG");
                }
                
                if (chatModel == null) {
                    throw new AIException("ChatModel not configured - required for RAG");
                }
                
                try {
                    // Get or create embedding store for the specified collection
                    String collectionName = options.collection() != null ? options.collection() : "default";
                    EmbeddingStore<TextSegment> embeddingStore = state.getOrCreateStore(collectionName, embeddingModel);
                    
                    // Embed the question to search for similar documents
                    Embedding questionEmbedding = embeddingModel.embed(question).content();
                    
                    // Search for similar embeddings using EmbeddingSearchRequest
                    EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                        .queryEmbedding(questionEmbedding)
                        .maxResults(options.topK())
                        .build();
                    
                    // Note: minScore filtering happens after retrieval if the API doesn't support it
                    List<EmbeddingMatch<TextSegment>> allMatches = embeddingStore.search(searchRequest).matches();
                    
                    // Filter by minScore if provided
                    List<EmbeddingMatch<TextSegment>> matches = options.minScore() != null ?
                        allMatches.stream()
                            .filter(match -> match.score() >= options.minScore().floatValue())
                            .collect(Collectors.toList()) :
                        allMatches;
                    
                    // Convert to Document format
                    // Based on example: match.embedded() returns TextSegment, match.score() exists
                    List<com.akilisha.oss.roya.plugins.ai.Document> sources = matches.stream()
                        .map(match -> {
                            TextSegment segment = match.embedded();
                            
                            // Extract ID - try to get from metadata or generate one
                            String id = "";
                            try {
                                // Check if there's an id method
                                java.lang.reflect.Method idMethod = match.getClass().getMethod("id");
                                Object idObj = idMethod.invoke(match);
                                id = idObj != null ? idObj.toString() : "";
                            } catch (Exception e) {
                                // No id method - generate one
                                id = java.util.UUID.randomUUID().toString();
                            }
                            
                            // Extract metadata
                            Map<String, Object> metadata = new java.util.HashMap<>();
                            try {
                                java.lang.reflect.Method metadataMethod = segment.getClass().getMethod("metadata");
                                Object metadataObj = metadataMethod.invoke(segment);
                                if (metadataObj instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, String> metaMap = (Map<String, String>) metadataObj;
                                    metaMap.forEach((key, value) -> metadata.put(key, value));
                                }
                            } catch (Exception e) {
                                // Metadata not available or different API
                            }
                            
                            return new com.akilisha.oss.roya.plugins.ai.Document(
                                segment.text(),
                                id,
                                metadata
                            );
                        })
                        .collect(Collectors.toList());
                    
                    // Build context from retrieved documents
                    String context = matches.stream()
                        .map(match -> match.embedded().text())
                        .collect(Collectors.joining("\n\n"));
                    
                    // Generate answer using LLM with context
                    String systemPrompt = "Answer the question based only on the provided context. " +
                        "If the context doesn't contain enough information, say so. " +
                        "Use the context to provide a comprehensive answer.";
                    
                    String prompt = String.format(
                        "Context:\n%s\n\nQuestion: %s\n\nAnswer:",
                        context.isEmpty() ? "(No relevant context found)" : context,
                        question
                    );
                    
                    String answer = LangChainAdapter.this.ask(systemPrompt, prompt, options.aiOptions());
                    
                    return new RAGResponse(answer, sources);
                    
                } catch (Exception e) {
                    throw new AIException("RAG query failed: " + e.getMessage(), e);
                }
            }
        };
    }

    @Override
    public Agents agents() {
        // Agents require tools and a ChatModel
        // LangChain4j has built-in agent support via AiServices with tools
        return new Agents() {
            @Override
            public Agent create(java.util.function.Consumer<AgentBuilder> config) {
                if (chatModel == null) {
                    throw new IllegalStateException("Agent requires a ChatModel - LangChainAdapter has no ChatModel configured");
                }
                
                // Create agent builder
                var builder = new AgentBuilderImpl();
                config.accept(builder);
                
                if (builder.tools == null || builder.tools.isEmpty()) {
                    throw new IllegalStateException("Agent requires at least one tool - configure via .tools()");
                }
                
                // Use the adapter's chatModel or the one provided by builder
                ChatModel modelToUse = builder.chatModel != null ? 
                    (ChatModel) builder.chatModel : chatModel;
                
                // Create AI Service interface for agent
                interface AgentService {
                    @dev.langchain4j.service.SystemMessage("{{systemPrompt}}")
                    @dev.langchain4j.service.UserMessage("{{userMessage}}")
                    String run(String systemPrompt, String userMessage);
                }
                
                // Build AI Service with tools using the specified model
                // Use reflection to configure tools to avoid type dependency issues
                var serviceBuilder = AiServices.builder(AgentService.class)
                    .chatModel(modelToUse);
                
                // Configure tools via reflection
                try {
                    var toolsMethod = serviceBuilder.getClass().getMethod("tools", java.util.List.class);
                    toolsMethod.invoke(serviceBuilder, builder.tools);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to configure agent tools: " + e.getMessage(), e);
                }
                
                AgentService agentService = serviceBuilder.build();
                
                // Return agent implementation
                return input -> {
                    String response = agentService.run(
                        builder.systemPrompt != null ? builder.systemPrompt : "You are a helpful assistant",
                        input
                    );
                    return new AgentResult(response, List.of());
                };
            }
        };
    }
    
    /**
     * Agent builder implementation.
     */
    private static class AgentBuilderImpl implements AgentBuilder {
        Object chatModel;
        java.util.List<Object> tools;
        String systemPrompt;
        
        @Override
        public AgentBuilder model(Object chatLanguageModel) {
            this.chatModel = chatLanguageModel;
            return this;
        }
        
        @Override
        public AgentBuilder tools(java.util.List<Object> tools) {
            this.tools = tools;
            return this;
        }
        
        @Override
        public AgentBuilder systemPrompt(String prompt) {
            this.systemPrompt = prompt;
            return this;
        }
    }

    // Convenience methods - delegate to llm()

    @Override
    public String ask(String systemPrompt, String userMessage) {
        return ask(systemPrompt, userMessage, AIOptions.defaults());
    }

    @Override
    public String ask(String systemPrompt, String userMessage, AIOptions options) {
        try {
            // Use AI Services for automatic message handling
            LLMService service = getLLMService();
            if (service == null) {
                throw new AIException("ChatModel not configured");
            }
            return service.ask(systemPrompt != null ? systemPrompt : "", userMessage);
        } catch (Exception e) {
            throw new AIException("LangChain chat completion failed: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> T extract(Class<T> type, String prompt) {
        return extract(type, prompt, AIOptions.defaults());
    }

    @Override
    public <T> T extract(Class<T> type, String prompt, AIOptions options) {
        try {
            // Use AI Services for automatic structured extraction
            // AI Services handles JSON parsing automatically!
            LLMService service = getLLMService();
            if (service == null) {
                throw new AIException("ChatModel not configured");
            }
            String systemPrompt = "Extract information from the following text into JSON format matching the specified schema. Respond with JSON only.";
            return service.extract(systemPrompt, prompt, type);
        } catch (Exception e) {
            throw new AIException("LangChain extraction failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void stream(String systemPrompt, String userMessage, Consumer<String> onToken) {
        stream(systemPrompt, userMessage, AIOptions.defaults(), onToken);
    }

    @Override
    public void stream(String systemPrompt, String userMessage, AIOptions options, Consumer<String> onToken) {
        try {
            // Use AI Services for streaming (requires StreamingChatModel)
            if (streamingChatModel == null) {
                throw new AIException("Streaming not supported - no StreamingChatModel configured");
            }
            
            LLMService service = getLLMService();
            if (service == null) {
                throw new AIException("ChatModel not configured");
            }
            
            // Use TokenStream from AI Services
            TokenStream tokenStream = service.stream(
                systemPrompt != null ? systemPrompt : "",
                userMessage
            );
            
            // Configure TokenStream callbacks
            tokenStream
                .onPartialResponse(onToken::accept)
                .onError(error -> {
                    throw new AIException("LangChain streaming error: " + error.getMessage(), error);
                })
                .start();
        } catch (Exception e) {
            throw new AIException("LangChain streaming failed: " + e.getMessage(), e);
        }
    }

    @Override
    public RAGResponse rag(String question) {
        return rag(question, RAGOptions.builder().build());
    }

    @Override
    public RAGResponse rag(String question, RAGOptions options) {
        // Delegate to ragApi() for consistency
        return ragApi().ask(question, options);
    }

    @Override
    public <T> T provider(Class<T> providerType) {
        // Return the underlying models if requested
        if (providerType.isInstance(chatModel)) {
            return providerType.cast(chatModel);
        }
        if (providerType.isInstance(streamingChatModel)) {
            return providerType.cast(streamingChatModel);
        }
        if (providerType.isInstance(embeddingModel)) {
            return providerType.cast(embeddingModel);
        }
        return null;
    }

    @Override
    public AIResponse<String> askWithMetadata(String systemPrompt, String userMessage, AIOptions options) {
        try {
            LLMService service = getLLMService();
            if (service == null) {
                throw new AIException("ChatModel not configured");
            }
            
            // Use Result wrapper to get metadata
            Result<String> result = service.askWithMetadata(
                systemPrompt != null ? systemPrompt : "",
                userMessage
            );
            
            // Extract token usage and other metadata
            TokenUsage tokenUsage = result.tokenUsage();
            int promptTokens = tokenUsage != null ? tokenUsage.inputTokenCount() : 0;
            int completionTokens = tokenUsage != null ? tokenUsage.outputTokenCount() : 0;
            int totalTokens = tokenUsage != null ? tokenUsage.totalTokenCount() : 0;
            
            // Calculate cost (simplified - should use actual model pricing)
            double cost = calculateCost(options.model(), promptTokens, completionTokens);
            
            return new AIResponse<>(
                result.content(),
                options.model(),
                promptTokens,
                completionTokens,
                totalTokens,
                cost,
                false // TODO: Check if result was cached
            );
        } catch (Exception e) {
            throw new AIException("LangChain chat completion with metadata failed: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> AIResponse<T> extractWithMetadata(Class<T> type, String prompt, AIOptions options) {
        try {
            LLMService service = getLLMService();
            if (service == null) {
                throw new AIException("ChatModel not configured");
            }
            
            String systemPrompt = "Extract information from the following text into JSON format matching the specified schema. Respond with JSON only.";
            
            // Use Result wrapper to get metadata
            Result<T> result = service.extractWithMetadata(systemPrompt, prompt, type);
            
            // Extract token usage and other metadata
            TokenUsage tokenUsage = result.tokenUsage();
            int promptTokens = tokenUsage != null ? tokenUsage.inputTokenCount() : 0;
            int completionTokens = tokenUsage != null ? tokenUsage.outputTokenCount() : 0;
            int totalTokens = tokenUsage != null ? tokenUsage.totalTokenCount() : 0;
            
            // Calculate cost (simplified - should use actual model pricing)
            double cost = calculateCost(options.model(), promptTokens, completionTokens);
            
            return new AIResponse<>(
                result.content(),
                options.model(),
                promptTokens,
                completionTokens,
                totalTokens,
                cost,
                false // TODO: Check if result was cached
            );
        } catch (Exception e) {
            throw new AIException("LangChain extraction with metadata failed: " + e.getMessage(), e);
        }
    }

    @Override
    public AIWorkflowBuilder workflow(String name) {
        throw new UnsupportedOperationException(
            "Workflow API not available in LangChainAdapter. Use UnifiedAIService for workflows."
        );
    }

    @Override
    public LangGraphService langGraph() {
        return null; // Not available in LangChainAdapter
    }

    @Override
    public GoogleADKService googleADK() {
        return null; // Not available in LangChainAdapter
    }

    @Override
    public <T> T aiService(Class<T> serviceClass) {
        // Use LangChain4j's AiServices to create the service
        // This provides access to ChatModel, StreamingChatModel, EmbeddingModel, etc.

        if (chatModel == null) {
            throw new IllegalStateException("ChatModel not configured - cannot create AI Service");
        }

        // For known service interfaces, return our implementations
        if (serviceClass == LLMService.class) {
            @SuppressWarnings("unchecked")
            T service = (T) getLLMService();
            return service;
        }

        if (serviceClass == EmbeddingService.class) {
            @SuppressWarnings("unchecked")
            T service = (T) getEmbeddingService();
            return service;
        }

        // For any other service interface, use AiServices.create() directly
        // This allows developers to define their own AI Service interfaces
        var builder = AiServices.builder(serviceClass);

        if (chatModel != null) {
            builder.chatModel(chatModel);
        }

        if (streamingChatModel != null) {
            builder.streamingChatModel(streamingChatModel);
        }

        // Note: EmbeddingModel is not configured via builder for AI Services
        // AI Services use EmbeddingModel for RAG (ContentRetriever), not directly

        return builder.build();
    }

    /**
     * Create an AI Service instance with optional RAG, tools, and memory support.
     *
     * This method supports advanced configuration via AiServiceBuilder:
     * <pre>
     * // With RAG
     * ContentRetriever retriever = ...;
     * var service = langChainAdapter.aiService(MyService.class, builder -> builder
     *     .contentRetriever(retriever)
     * );
     *
     * // With tools
     * List&lt;ToolSpecification&gt; tools = ...;
     * var service = langChainAdapter.aiService(MyService.class, builder -> builder
     *     .tools(tools)
     * );
     *
     * // With memory
     * ChatMemory memory = ...;
     * var service = langChainAdapter.aiService(MyService.class, builder -> builder
     *     .chatMemory(memory)
     * );
     * </pre>
     */
    public <T> T aiService(Class<T> serviceClass, java.util.function.Consumer<? super Object> config) {
        if (chatModel == null) {
            throw new IllegalStateException("ChatModel not configured - cannot create AI Service");
        }

        var builder = AiServices.builder(serviceClass);

        if (chatModel != null) {
            builder.chatModel(chatModel);
        }

        if (streamingChatModel != null) {
            builder.streamingChatModel(streamingChatModel);
        }

        // Apply custom configuration (RAG, tools, memory, etc.)
        // The builder type is the concrete implementation from LangChain4j
        config.accept(builder);

        return builder.build();
    }
}



