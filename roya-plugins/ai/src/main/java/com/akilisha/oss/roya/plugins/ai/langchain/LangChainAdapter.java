package com.akilisha.oss.roya.plugins.ai.langchain;

import com.akilisha.oss.roya.plugins.ai.*;
import com.akilisha.oss.roya.plugins.ai.builder.AIWorkflowBuilder;
import com.akilisha.oss.roya.plugins.ai.langchain.services.EmbeddingService;
import com.akilisha.oss.roya.plugins.ai.langchain.services.LLMService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
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

    // ChatMemory provider (manages ChatMemory instances per conversation ID)
    private final ChatMemoryProvider memoryProvider = ChatMemoryProvider.getInstance();

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
         * Made public for use in createCollection.
         */
        void ensureCollectionExists(String collectionName, EmbeddingModel embeddingModel) {
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

        /**
         * Delete a collection.
         */
        void deleteCollection(String collectionName) {
            if (qdrantClient == null) {
                qdrantClient = new io.qdrant.client.QdrantClient(
                    io.qdrant.client.QdrantGrpcClient.newBuilder(host, port, false).build()
                );
            }

            try {
                qdrantClient.deleteCollectionAsync(collectionName).get();
                // Remove from cache
                embeddingStores.remove(collectionName);
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete collection '" + collectionName + "': " + e.getMessage(), e);
            }
        }

        /**
         * List all collections.
         */
        java.util.List<String> listCollections() {
            if (qdrantClient == null) {
                qdrantClient = new io.qdrant.client.QdrantClient(
                    io.qdrant.client.QdrantGrpcClient.newBuilder(host, port, false).build()
                );
            }

            try {
                java.util.List<String> collections = qdrantClient.listCollectionsAsync().get();
                return collections;
            } catch (Exception e) {
                throw new RuntimeException("Failed to list collections: " + e.getMessage(), e);
            }
        }

        /**
         * Get collection statistics.
         */
        com.akilisha.oss.roya.plugins.ai.rag.CollectionStats getCollectionStats(String collectionName) {
            if (qdrantClient == null) {
                qdrantClient = new io.qdrant.client.QdrantClient(
                    io.qdrant.client.QdrantGrpcClient.newBuilder(host, port, false).build()
                );
            }

            try {
                io.qdrant.client.grpc.Collections.CollectionInfo info =
                    qdrantClient.getCollectionInfoAsync(collectionName).get();

                long vectorCount = info.getPointsCount();
                int vectorSize = (int) info.getConfig().getParams().getVectorsConfig().getParams().getSize();
                String status = info.getStatus().name();

                java.util.Map<String, Object> metadata = new java.util.HashMap<>();
                metadata.put("points_count", vectorCount);
                metadata.put("vector_size", vectorSize);
                metadata.put("status", status);

                return new com.akilisha.oss.roya.plugins.ai.rag.CollectionStats(
                    collectionName,
                    vectorCount,
                    vectorSize,
                    status,
                    metadata
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to get collection stats for '" + collectionName + "': " + e.getMessage(), e);
            }
        }

        /**
         * Check if a collection exists.
         */
        boolean collectionExists(String collectionName) {
            if (qdrantClient == null) {
                qdrantClient = new io.qdrant.client.QdrantClient(
                    io.qdrant.client.QdrantGrpcClient.newBuilder(host, port, false).build()
                );
            }

            try {
                qdrantClient.getCollectionInfoAsync(collectionName).get();
                return true;
            } catch (java.util.concurrent.ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof io.grpc.StatusRuntimeException) {
                    io.grpc.StatusRuntimeException grpcEx = (io.grpc.StatusRuntimeException) cause;
                    if (grpcEx.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                        return false;
                    }
                }
                // Other errors - assume doesn't exist
                return false;
            } catch (Exception e) {
                return false;
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
            public String ask(dev.langchain4j.memory.ChatMemory memory, String systemPrompt, String userMessage) {
                return LangChainAdapter.this.ask(memory, systemPrompt, userMessage, AIOptions.defaults());
            }

            @Override
            public String ask(dev.langchain4j.memory.ChatMemory memory, String systemPrompt, String userMessage, AIOptions options) {
                return LangChainAdapter.this.ask(memory, systemPrompt, userMessage, options);
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

            @Override
            public void stream(dev.langchain4j.memory.ChatMemory memory, String systemPrompt, String userMessage, Consumer<String> onToken) {
                LangChainAdapter.this.stream(memory, systemPrompt, userMessage, AIOptions.defaults(), onToken);
            }

            @Override
            public void stream(dev.langchain4j.memory.ChatMemory memory, String systemPrompt, String userMessage, AIOptions options, Consumer<String> onToken) {
                LangChainAdapter.this.stream(memory, systemPrompt, userMessage, options, onToken);
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
                long startTime = System.currentTimeMillis();
                boolean success = false;

                try {
                    var state = getQdrantConnection();
                    if (!state.connected) {
                        throw new AIException("Qdrant connection failed: " + state.error);
                    }

                    if (embeddingModel == null) {
                        throw new AIException("EmbeddingModel not configured - required for vector indexing");
                    }

                    // Get or create embedding store for this collection
                    EmbeddingStore<TextSegment> embeddingStore = state.getOrCreateStore(collection, embeddingModel);

                    // Load documents from directory
                    List<dev.langchain4j.data.document.Document> documents = FileSystemDocumentLoader.loadDocuments(directory);

                    // Filter by file extensions (.md, .markdown, .txt)
                    List<dev.langchain4j.data.document.Document> filteredDocs = documents.stream()
                        .filter(doc -> {
                            // Try to get source from metadata
                            String fileName = null;
                            try {
                                java.lang.reflect.Method metadataMethod = doc.getClass().getMethod("metadata");
                                Object metadataObj = metadataMethod.invoke(doc);
                                if (metadataObj instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, String> metaMap = (Map<String, String>) metadataObj;
                                    fileName = metaMap.get("source");
                                }
                            } catch (Exception e) {
                                // Metadata access failed, include document
                            }

                            if (fileName == null) return true; // Include if no source metadata

                            String lowerName = fileName.toLowerCase();
                            return lowerName.endsWith(".md") ||
                                   lowerName.endsWith(".markdown") ||
                                   lowerName.endsWith(".txt");
                        })
                        .collect(Collectors.toList());

                    // Split documents into chunks
                    DocumentSplitter splitter = DocumentSplitters.recursive(
                        options.size(),
                        options.overlap()
                    );

                    List<TextSegment> segments = splitter.splitAll(filteredDocs);

                    // Embed and store segments
                    List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
                    embeddingStore.addAll(embeddings, segments);

                    success = true;
                } catch (Exception e) {
                    throw new AIException("Failed to index directory: " + e.getMessage(), e);
                } finally {
                    // Record metrics
                    long durationMs = System.currentTimeMillis() - startTime;
                    com.akilisha.oss.roya.plugins.ai.rag.RAGMetrics.recordVectorIndexOperation(durationMs, success);
                }
            }

            @Override
            public void index(String collection, java.util.List<VectorDoc> documents) {
                long startTime = System.currentTimeMillis();
                boolean success = false;

                try {
                    var state = getQdrantConnection();
                    if (!state.connected) {
                        throw new AIException("Qdrant connection failed: " + state.error);
                    }

                    if (embeddingModel == null) {
                        throw new AIException("EmbeddingModel not configured - required for vector indexing");
                    }

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

                    success = true;
                } catch (Exception e) {
                    throw new AIException("Failed to index documents: " + e.getMessage(), e);
                } finally {
                    // Record metrics
                    long durationMs = System.currentTimeMillis() - startTime;
                    com.akilisha.oss.roya.plugins.ai.rag.RAGMetrics.recordVectorIndexOperation(durationMs, success);
                }
            }

            @Override
            public void createCollection(String collection) {
                try {
                    var state = getQdrantConnection();
                    if (!state.connected) {
                        throw new AIException("Qdrant connection failed: " + state.error);
                    }

                    if (embeddingModel == null) {
                        throw new AIException("EmbeddingModel not configured - required for collection creation");
                    }

                    state.ensureCollectionExists(collection, embeddingModel);
                    // Also ensure store is created in cache
                    state.getOrCreateStore(collection, embeddingModel);

                    // Record metrics
                    com.akilisha.oss.roya.plugins.ai.rag.RAGMetrics.recordCollectionCreate();
                } catch (Exception e) {
                    throw new AIException("Failed to create collection: " + e.getMessage(), e);
                }
            }

            @Override
            public void deleteCollection(String collection) {
                try {
                    var state = getQdrantConnection();
                    if (!state.connected) {
                        throw new AIException("Qdrant connection failed: " + state.error);
                    }

                    state.deleteCollection(collection);

                    // Record metrics
                    com.akilisha.oss.roya.plugins.ai.rag.RAGMetrics.recordCollectionDelete();
                } catch (Exception e) {
                    throw new AIException("Failed to delete collection: " + e.getMessage(), e);
                }
            }

            @Override
            public java.util.List<String> listCollections() {
                var state = getQdrantConnection();
                if (!state.connected) {
                    throw new AIException("Qdrant connection failed: " + state.error);
                }

                try {
                    return state.listCollections();
                } catch (Exception e) {
                    throw new AIException("Failed to list collections: " + e.getMessage(), e);
                }
            }

            @Override
            public com.akilisha.oss.roya.plugins.ai.rag.CollectionStats getCollectionStats(String collection) {
                var state = getQdrantConnection();
                if (!state.connected) {
                    throw new AIException("Qdrant connection failed: " + state.error);
                }

                try {
                    return state.getCollectionStats(collection);
                } catch (Exception e) {
                    throw new AIException("Failed to get collection stats: " + e.getMessage(), e);
                }
            }

            @Override
            public boolean collectionExists(String collection) {
                var state = getQdrantConnection();
                if (!state.connected) {
                    return false;
                }

                try {
                    return state.collectionExists(collection);
                } catch (Exception e) {
                    return false;
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
                long startTime = System.currentTimeMillis();
                boolean success = false;

                try {
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

                    // Rerank if requested
                    if (options.rerank() && !matches.isEmpty()) {
                        matches = rerankMatches(matches, question, options.aiOptions());
                    }

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

                    success = true;
                    return new RAGResponse(answer, sources);

                } catch (Exception e) {
                    throw new AIException("RAG query failed: " + e.getMessage(), e);
                } finally {
                    // Record metrics
                    long durationMs = System.currentTimeMillis() - startTime;
                    com.akilisha.oss.roya.plugins.ai.rag.RAGMetrics.recordRAGQuery(durationMs, success);
                }
            }
        };
    }

    /**
     * Rerank matches using LLM-based relevance scoring.
     * Uses the LLM to score each match's relevance to the question.
     */
    private List<EmbeddingMatch<TextSegment>> rerankMatches(
            List<EmbeddingMatch<TextSegment>> matches,
            String question,
            AIOptions aiOptions) {
        if (chatModel == null) {
            // If no LLM available, return original matches
            return matches;
        }

        try {
            // Score each match
            List<java.util.Map.Entry<EmbeddingMatch<TextSegment>, Double>> scoredMatches = new java.util.ArrayList<>();

            for (EmbeddingMatch<TextSegment> match : matches) {
                String docText = match.embedded().text();
                String prompt = String.format(
                    "Rate the relevance of the following document to the question on a scale of 0.0 to 1.0.\n" +
                    "Question: %s\n\n" +
                    "Document:\n%s\n\n" +
                    "Return only a single number between 0.0 and 1.0.",
                    question,
                    docText
                );

                String response = ask("You are a relevance scorer. Return only a number.", prompt, aiOptions);

                try {
                    double score = Double.parseDouble(response.trim());
                    scoredMatches.add(new java.util.AbstractMap.SimpleEntry<>(match, score));
                } catch (NumberFormatException e) {
                    // If parsing fails, use original similarity score
                    scoredMatches.add(new java.util.AbstractMap.SimpleEntry<>(match, (double) match.score()));
                }
            }

            // Sort by rerank score (descending)
            scoredMatches.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

            // Return reranked matches
            return scoredMatches.stream()
                .map(java.util.Map.Entry::getKey)
                .collect(Collectors.toList());

        } catch (Exception e) {
            // If reranking fails, return original matches
            return matches;
        }
    }

    @Override
    public Vision vision() {
        return new Vision() {
            @Override
            public String generateImage(String prompt) {
                return generateImage(prompt, AIOptions.defaults());
            }

            @Override
            public String generateImage(String prompt, AIOptions options) {
                try {
                    // Get API key from environment or system properties
                    String apiKey = System.getenv("OPENAI_API_KEY");
                    if (apiKey == null || apiKey.isBlank()) {
                        apiKey = System.getProperty("ai.openai.apiKey");
                    }
                    if (apiKey == null || apiKey.isBlank()) {
                        apiKey = System.getProperty("OPENAI_API_KEY");
                    }
                    if (apiKey == null || apiKey.isBlank()) {
                        throw new AIException("OpenAI API key not configured - required for image generation. " +
                                "Set OPENAI_API_KEY environment variable.");
                    }

                    // Use reflection to access LangChain4j's ImageModel
                    Class<?> openAiImageModelClass = Class.forName("dev.langchain4j.model.openai.OpenAiImageModel");
                    Class<?> imageModelNameEnum = Class.forName("dev.langchain4j.model.openai.OpenAiImageModelName");

                    // Determine model (default to DALL_E_3, fallback to DALL_E_2)
                    String modelName = options.model();
                    Object modelEnumValue;
                    if (modelName != null && modelName.contains("dall-e-2")) {
                        java.lang.reflect.Field dallE2Field = imageModelNameEnum.getField("DALL_E_2");
                        modelEnumValue = dallE2Field.get(null);
                    } else {
                        // Default to DALL-E 3
                        java.lang.reflect.Field dallE3Field = imageModelNameEnum.getField("DALL_E_3");
                        modelEnumValue = dallE3Field.get(null);
                    }

                    // Build ImageModel
                    java.lang.reflect.Method builderMethod = openAiImageModelClass.getMethod("builder");
                    Object builder = builderMethod.invoke(null);

                    java.lang.reflect.Method apiKeyMethod = builder.getClass().getMethod("apiKey", String.class);
                    apiKeyMethod.invoke(builder, apiKey);

                    java.lang.reflect.Method modelNameMethod = builder.getClass().getMethod("modelName", imageModelNameEnum);
                    modelNameMethod.invoke(builder, modelEnumValue);

                    // Set additional options if present
                    if (options.additionalOptions() != null) {
                        // Handle size, quality, etc. via additionalOptions
                        Object size = options.additionalOptions().get("size");
                        if (size != null) {
                            try {
                                java.lang.reflect.Method sizeMethod = builder.getClass().getMethod("size", String.class);
                                sizeMethod.invoke(builder, size.toString());
                            } catch (NoSuchMethodException e) {
                                // Size not supported, ignore
                            }
                        }

                        Object quality = options.additionalOptions().get("quality");
                        if (quality != null) {
                            try {
                                java.lang.reflect.Method qualityMethod = builder.getClass().getMethod("quality", String.class);
                                qualityMethod.invoke(builder, quality.toString());
                            } catch (NoSuchMethodException e) {
                                // Quality not supported, ignore
                            }
                        }

                        Object n = options.additionalOptions().get("n");
                        if (n != null && modelEnumValue.toString().contains("DALL_E_2")) {
                            // Only DALL-E 2 supports multiple images
                            try {
                                java.lang.reflect.Method nMethod = builder.getClass().getMethod("n", int.class);
                                nMethod.invoke(builder, Integer.parseInt(n.toString()));
                            } catch (NoSuchMethodException e) {
                                // N not supported, ignore
                            }
                        }
                    }

                    java.lang.reflect.Method buildMethod = builder.getClass().getMethod("build");
                    Object imageModel = buildMethod.invoke(builder);

                    // Generate image
                    java.lang.reflect.Method generateMethod = imageModel.getClass().getMethod("generate", String.class);
                    Object response = generateMethod.invoke(imageModel, prompt);

                    // Extract URL from response
                    java.lang.reflect.Method contentMethod = response.getClass().getMethod("content");
                    Object image = contentMethod.invoke(response);

                    // Handle both single Image and List<Image>
                    Object imageUrl;
                    if (image instanceof java.util.List<?> imageList && !imageList.isEmpty()) {
                        // Multiple images (DALL-E 2)
                        Object firstImage = imageList.get(0);
                        java.lang.reflect.Method urlMethod = firstImage.getClass().getMethod("url");
                        imageUrl = urlMethod.invoke(firstImage);
                    } else {
                        // Single image (DALL-E 3)
                        java.lang.reflect.Method urlMethod = image.getClass().getMethod("url");
                        imageUrl = urlMethod.invoke(image);
                    }

                    // Convert URI to String if needed
                    if (imageUrl instanceof java.net.URI) {
                        return ((java.net.URI) imageUrl).toString();
                    } else {
                        return imageUrl.toString();
                    }

                } catch (ClassNotFoundException e) {
                    throw new AIException("Image generation requires langchain4j-open-ai dependency", e);
                } catch (Exception e) {
                    throw new AIException("Image generation failed: " + e.getMessage(), e);
                }
            }

            @Override
            public String analyzeImage(String imageUrl, String prompt) {
                return analyzeImage(imageUrl, prompt, AIOptions.defaults());
            }

            @Override
            public String analyzeImage(String imageUrl, String prompt, AIOptions options) {
                if (chatModel == null) {
                    throw new AIException("ChatModel not configured - required for vision operations");
                }

                try {
                    // Create ImageContent using LangChain4j's Content interface
                    // Use reflection to avoid direct dependency
                    Object imageContent = createImageContent(imageUrl);

                    // Create UserMessage with image content using reflection
                    // LangChain4j uses UserMessage.from(text, imageContent) for multimodal
                    Class<?> userMessageClass = Class.forName("dev.langchain4j.data.message.UserMessage");
                    java.lang.reflect.Method fromMethod = userMessageClass.getMethod("from", Object.class, Object.class);

                    // Create system message
                    Class<?> systemMessageClass = Class.forName("dev.langchain4j.data.message.SystemMessage");
                    java.lang.reflect.Method fromSysMethod = systemMessageClass.getMethod("from", String.class);
                    Object systemMessage = fromSysMethod.invoke(null, "You are a vision model that analyzes images. Describe what you see accurately and in detail.");

                    // Create user message with text and image
                    Object userMessage = fromMethod.invoke(null, prompt, imageContent);

                    // Create chat message list
                    java.util.List<Object> messages = new java.util.ArrayList<>();
                    messages.add(systemMessage);
                    messages.add(userMessage);

                    // Call ChatModel.generate() with messages
                    java.lang.reflect.Method generateMethod = chatModel.getClass().getMethod("generate", java.util.List.class);
                    Object response = generateMethod.invoke(chatModel, messages);

                    // Extract text from response
                    java.lang.reflect.Method contentMethod = response.getClass().getMethod("content");
                    Object content = contentMethod.invoke(response);
                    java.lang.reflect.Method textMethod = content.getClass().getMethod("text");
                    return (String) textMethod.invoke(content);

                } catch (Exception e) {
                    throw new AIException("Image analysis failed: " + e.getMessage(), e);
                }
            }

            @Override
            public String transcribeAudio(String audioUrl) {
                return transcribeAudio(audioUrl, AIOptions.defaults());
            }

            @Override
            public String transcribeAudio(String audioUrl, AIOptions options) {
                if (chatModel == null) {
                    throw new AIException("ChatModel not configured - required for audio transcription");
                }

                try {
                    // Create AudioContent using LangChain4j's Content interface
                    // Use reflection to avoid direct dependency
                    Object audioContent = createAudioContent(audioUrl);

                    // Create TextContent for the prompt
                    Class<?> textContentClass = Class.forName("dev.langchain4j.data.message.TextContent");
                    java.lang.reflect.Method fromTextMethod = textContentClass.getMethod("from", String.class);
                    Object textContent = fromTextMethod.invoke(null, "Write a transcription of this audio file");

                    // Create UserMessage with audio and text content using reflection
                    // LangChain4j uses UserMessage.from(audioContent, textContent) for multimodal
                    Class<?> userMessageClass = Class.forName("dev.langchain4j.data.message.UserMessage");
                    java.lang.reflect.Method fromMethod = userMessageClass.getMethod("from", Object.class, Object.class);
                    Object userMessage = fromMethod.invoke(null, audioContent, textContent);

                    // Create chat message list
                    java.util.List<Object> messages = new java.util.ArrayList<>();
                    messages.add(userMessage);

                    // Call ChatModel.generate() with messages
                    java.lang.reflect.Method generateMethod = chatModel.getClass().getMethod("generate", java.util.List.class);
                    Object response = generateMethod.invoke(chatModel, messages);

                    // Extract text from response
                    java.lang.reflect.Method contentMethod = response.getClass().getMethod("content");
                    Object content = contentMethod.invoke(response);
                    java.lang.reflect.Method textMethod = content.getClass().getMethod("text");
                    return (String) textMethod.invoke(content);

                } catch (ClassNotFoundException e) {
                    throw new AIException("Audio transcription requires langchain4j-google-ai-gemini for Gemini models or provider-specific audio support", e);
                } catch (Exception e) {
                    throw new AIException("Audio transcription failed: " + e.getMessage(), e);
                }
            }

            @Override
            public String describeVideo(String videoUrl, String prompt) {
                return describeVideo(videoUrl, prompt, AIOptions.defaults());
            }

            @Override
            public String describeVideo(String videoUrl, String prompt, AIOptions options) {
                if (chatModel == null) {
                    throw new AIException("ChatModel not configured - required for video description");
                }

                try {
                    // Create VideoContent using LangChain4j's Content interface
                    // Use reflection to avoid direct dependency
                    Object videoContent = createVideoContent(videoUrl);

                    // Create TextContent for the prompt
                    Class<?> textContentClass = Class.forName("dev.langchain4j.data.message.TextContent");
                    java.lang.reflect.Method fromTextMethod = textContentClass.getMethod("from", String.class);
                    Object textContent = fromTextMethod.invoke(null, prompt != null && !prompt.isEmpty() ? prompt : "Describe this video");

                    // Create UserMessage with video and text content using reflection
                    // LangChain4j uses UserMessage.from(videoContent, textContent) for multimodal
                    Class<?> userMessageClass = Class.forName("dev.langchain4j.data.message.UserMessage");
                    java.lang.reflect.Method fromMethod = userMessageClass.getMethod("from", Object.class, Object.class);
                    Object userMessage = fromMethod.invoke(null, videoContent, textContent);

                    // Create chat message list
                    java.util.List<Object> messages = new java.util.ArrayList<>();
                    messages.add(userMessage);

                    // Call ChatModel.generate() with messages
                    java.lang.reflect.Method generateMethod = chatModel.getClass().getMethod("generate", java.util.List.class);
                    Object response = generateMethod.invoke(chatModel, messages);

                    // Extract text from response
                    java.lang.reflect.Method contentMethod = response.getClass().getMethod("content");
                    Object content = contentMethod.invoke(response);
                    java.lang.reflect.Method textMethod = content.getClass().getMethod("text");
                    return (String) textMethod.invoke(content);

                } catch (ClassNotFoundException e) {
                    throw new AIException("Video description requires langchain4j-google-ai-gemini for Gemini models or provider-specific video support", e);
                } catch (Exception e) {
                    throw new AIException("Video description failed: " + e.getMessage(), e);
                }
            }

            @Override
            public String processPdf(String pdfUrl, String prompt) {
                return processPdf(pdfUrl, prompt, AIOptions.defaults());
            }

            @Override
            public String processPdf(String pdfUrl, String prompt, AIOptions options) {
                if (chatModel == null) {
                    throw new AIException("ChatModel not configured - required for PDF processing");
                }

                try {
                    // Create PdfFileContent using LangChain4j's Content interface
                    // Use reflection to avoid direct dependency
                    Object pdfContent = createPdfFileContent(pdfUrl);

                    // Create TextContent for the prompt
                    Class<?> textContentClass = Class.forName("dev.langchain4j.data.message.TextContent");
                    java.lang.reflect.Method fromTextMethod = textContentClass.getMethod("from", String.class);
                    Object textContent = fromTextMethod.invoke(null, prompt != null && !prompt.isEmpty() ? prompt : "Give a summary of this document");

                    // Create UserMessage with PDF and text content using reflection
                    // LangChain4j uses UserMessage.from(pdfContent, textContent) for multimodal
                    Class<?> userMessageClass = Class.forName("dev.langchain4j.data.message.UserMessage");
                    java.lang.reflect.Method fromMethod = userMessageClass.getMethod("from", Object.class, Object.class);
                    Object userMessage = fromMethod.invoke(null, pdfContent, textContent);

                    // Create chat message list
                    java.util.List<Object> messages = new java.util.ArrayList<>();
                    messages.add(userMessage);

                    // Call ChatModel.generate() with messages
                    java.lang.reflect.Method generateMethod = chatModel.getClass().getMethod("generate", java.util.List.class);
                    Object response = generateMethod.invoke(chatModel, messages);

                    // Extract text from response
                    java.lang.reflect.Method contentMethod = response.getClass().getMethod("content");
                    Object content = contentMethod.invoke(response);
                    java.lang.reflect.Method textMethod = content.getClass().getMethod("text");
                    return (String) textMethod.invoke(content);

                } catch (ClassNotFoundException e) {
                    throw new AIException("PDF processing requires langchain4j-google-ai-gemini for Gemini models or provider-specific PDF support", e);
                } catch (Exception e) {
                    throw new AIException("PDF processing failed: " + e.getMessage(), e);
                }
            }
        };
    }

    /**
     * Create ImageContent from URL, base64, or file path using LangChain4j.
     * Uses reflection to avoid direct dependency on LangChain4j Content types.
     */
    private Object createImageContent(String imageUrl) {
        try {
            // Try to use LangChain4j's ImageContent class
            Class<?> imageContentClass = Class.forName("dev.langchain4j.data.message.ImageContent");

            // Check if it's a URL (http:// or https://)
            if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                // Use fromUrl method
                java.lang.reflect.Method fromUrlMethod = imageContentClass.getMethod("fromUrl", String.class);
                return fromUrlMethod.invoke(null, imageUrl);
            }

            // Check if it's a base64 data URI (data:image/...)
            if (imageUrl.startsWith("data:image/")) {
                // Extract base64 part
                String base64Data = imageUrl.substring(imageUrl.indexOf(",") + 1);
                // Use fromBase64 method if available
                try {
                    java.lang.reflect.Method fromBase64Method = imageContentClass.getMethod("fromBase64", String.class);
                    return fromBase64Method.invoke(null, base64Data);
                } catch (NoSuchMethodException e) {
                    // Fallback: try fromDataUri
                    java.lang.reflect.Method fromDataUriMethod = imageContentClass.getMethod("fromDataUri", String.class);
                    return fromDataUriMethod.invoke(null, imageUrl);
                }
            }

            // Assume it's a file path
            java.nio.file.Path filePath = java.nio.file.Paths.get(imageUrl);
            java.lang.reflect.Method fromFileMethod = imageContentClass.getMethod("from", java.nio.file.Path.class);
            return fromFileMethod.invoke(null, filePath);

        } catch (Exception e) {
            throw new AIException("Failed to create ImageContent: " + e.getMessage(), e);
        }
    }

    /**
     * Create AudioContent from URL, Google Cloud Storage URL, file path, or URI using LangChain4j.
     * Uses reflection to avoid direct dependency on LangChain4j Content types.
     *
     * <p>Supports:
     * <ul>
     *   <li>HTTP/HTTPS URLs: "https://example.com/audio.mp3"</li>
     *   <li>Google Cloud Storage URLs: "gs://bucket/audio.mp3"</li>
     *   <li>Local file paths: "/path/to/audio.mp3"</li>
     *   <li>URIs: Paths.get("audio.mp3").toUri()</li>
     * </ul>
     */
    private Object createAudioContent(String audioUrl) {
        try {
            // Try to use LangChain4j's AudioContent class
            Class<?> audioContentClass = Class.forName("dev.langchain4j.data.message.AudioContent");

            // Check if it's a URL (http://, https://, or gs://)
            if (audioUrl.startsWith("http://") || audioUrl.startsWith("https://") || audioUrl.startsWith("gs://")) {
                // Use from method with String (URL)
                java.lang.reflect.Method fromMethod = audioContentClass.getMethod("from", String.class);
                return fromMethod.invoke(null, audioUrl);
            }

            // Assume it's a file path
            java.nio.file.Path filePath = java.nio.file.Paths.get(audioUrl);
            // Try from(Path) first
            try {
                java.lang.reflect.Method fromPathMethod = audioContentClass.getMethod("from", java.nio.file.Path.class);
                return fromPathMethod.invoke(null, filePath);
            } catch (NoSuchMethodException e) {
                // Fallback: try from(URI)
                java.lang.reflect.Method fromUriMethod = audioContentClass.getMethod("from", java.net.URI.class);
                return fromUriMethod.invoke(null, filePath.toUri());
            }

        } catch (Exception e) {
            throw new AIException("Failed to create AudioContent: " + e.getMessage(), e);
        }
    }

    /**
     * Create VideoContent from URL, Google Cloud Storage URL, file path, or URI using LangChain4j.
     * Uses reflection to avoid direct dependency on LangChain4j Content types.
     *
     * <p>Supports:
     * <ul>
     *   <li>HTTP/HTTPS URLs: "https://example.com/video.mp4"</li>
     *   <li>Google Cloud Storage URLs: "gs://bucket/video.mp4"</li>
     *   <li>Local file paths: "/path/to/video.mp4"</li>
     *   <li>URIs: Paths.get("video.mp4").toUri()</li>
     * </ul>
     */
    private Object createVideoContent(String videoUrl) {
        try {
            // Try to use LangChain4j's VideoContent class
            Class<?> videoContentClass = Class.forName("dev.langchain4j.data.message.VideoContent");

            // Check if it's a URL (http://, https://, or gs://)
            if (videoUrl.startsWith("http://") || videoUrl.startsWith("https://") || videoUrl.startsWith("gs://")) {
                // Use from method with String (URL)
                java.lang.reflect.Method fromMethod = videoContentClass.getMethod("from", String.class);
                return fromMethod.invoke(null, videoUrl);
            }

            // Assume it's a file path
            java.nio.file.Path filePath = java.nio.file.Paths.get(videoUrl);
            // Try from(Path) first
            try {
                java.lang.reflect.Method fromPathMethod = videoContentClass.getMethod("from", java.nio.file.Path.class);
                return fromPathMethod.invoke(null, filePath);
            } catch (NoSuchMethodException e) {
                // Fallback: try from(URI)
                java.lang.reflect.Method fromUriMethod = videoContentClass.getMethod("from", java.net.URI.class);
                return fromUriMethod.invoke(null, filePath.toUri());
            }

        } catch (Exception e) {
            throw new AIException("Failed to create VideoContent: " + e.getMessage(), e);
        }
    }

    /**
     * Create PdfFileContent from URL, Google Cloud Storage URL, file path, or URI using LangChain4j.
     * Uses reflection to avoid direct dependency on LangChain4j Content types.
     *
     * <p>Supports:
     * <ul>
     *   <li>HTTP/HTTPS URLs: "https://example.com/document.pdf"</li>
     *   <li>Google Cloud Storage URLs: "gs://bucket/document.pdf"</li>
     *   <li>Local file paths: "/path/to/document.pdf"</li>
     *   <li>URIs: Paths.get("document.pdf").toUri()</li>
     * </ul>
     */
    private Object createPdfFileContent(String pdfUrl) {
        try {
            // Try to use LangChain4j's PdfFileContent class
            Class<?> pdfContentClass = Class.forName("dev.langchain4j.data.message.PdfFileContent");

            // Check if it's a URL (http://, https://, or gs://)
            if (pdfUrl.startsWith("http://") || pdfUrl.startsWith("https://") || pdfUrl.startsWith("gs://")) {
                // Use from method with String (URL)
                java.lang.reflect.Method fromMethod = pdfContentClass.getMethod("from", String.class);
                return fromMethod.invoke(null, pdfUrl);
            }

            // Assume it's a file path
            java.nio.file.Path filePath = java.nio.file.Paths.get(pdfUrl);
            // Try from(Path) first
            try {
                java.lang.reflect.Method fromPathMethod = pdfContentClass.getMethod("from", java.nio.file.Path.class);
                return fromPathMethod.invoke(null, filePath);
            } catch (NoSuchMethodException e) {
                // Fallback: try from(URI)
                java.lang.reflect.Method fromUriMethod = pdfContentClass.getMethod("from", java.net.URI.class);
                return fromUriMethod.invoke(null, filePath.toUri());
            }

        } catch (Exception e) {
            throw new AIException("Failed to create PdfFileContent: " + e.getMessage(), e);
        }
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
            // Use StreamingChatModel directly (framework approach - use underlying model)
            if (streamingChatModel == null) {
                throw new AIException("Streaming not supported - no StreamingChatModel configured");
            }

            // Build messages
            List<ChatMessage> messages = new java.util.ArrayList<>();
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                messages.add(SystemMessage.from(systemPrompt));
            }
            messages.add(UserMessage.from(userMessage));

            // Use CompletableFuture to wait for streaming completion
            java.util.concurrent.CompletableFuture<Void> future = new java.util.concurrent.CompletableFuture<>();

            // Stream using StreamingChatResponseHandler
            // Note: StreamingChatModel.generate() returns void and handles streaming via callbacks
            streamingChatModel.chat(
                messages,
                new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String partialResponse) {
                        onToken.accept(partialResponse);
                    }

                    @Override
                    public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse completeResponse) {
                        future.complete(null);
                    }

                    @Override
                    public void onError(Throwable error) {
                        future.completeExceptionally(error);
                    }
                }
            );

            // Wait for streaming to complete (blocking call)
            future.join();
        } catch (java.util.concurrent.CompletionException e) {
            // Unwrap CompletionException to get the actual error
            Throwable cause = e.getCause();
            if (cause instanceof AIException) {
                throw (AIException) cause;
            }
            throw new AIException("LangChain streaming failed: " + cause.getMessage(), cause);
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

    /**
     * Ask with ChatMemory using LangChain4j's ChatMemory API.
     *
     * <p>Pattern: {@code chatModel.generate(chatMemory.messages())}
     *
     * @param memory ChatMemory instance (repository of chat messages)
     * @param systemPrompt System prompt (role/context)
     * @param userMessage User message
     * @param options AI options
     * @return AI response
     */
    private String ask(dev.langchain4j.memory.ChatMemory memory, String systemPrompt, String userMessage, AIOptions options) {
        if (chatModel == null) {
            throw new AIException("ChatModel not configured - required for memory operations");
        }

        try {
            // Add system message if not already present (only add once per conversation)
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                boolean hasSystemMessage = memory.messages().stream()
                    .anyMatch(msg -> msg instanceof SystemMessage);
                if (!hasSystemMessage) {
                    memory.add(SystemMessage.from(systemPrompt));
                }
            }

            // Add user message to memory
            memory.add(UserMessage.from(userMessage));

            // Use AI Services with ChatMemory - LangChain4j pattern
            // Create a temporary AI Service configured with this ChatMemory
            var builder = AiServices.builder(LLMService.class)
                .chatModel(chatModel)
                .chatMemory(memory);

            LLMService service = builder.build();

            // Use the service - it will automatically use memory.messages()
            String response = service.ask(systemPrompt != null ? systemPrompt : "", userMessage);

            // Memory is automatically updated by LangChain4j's AI Services

            return response;
        } catch (Exception e) {
            throw new AIException("LangChain memory operation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Stream with ChatMemory using LangChain4j's ChatMemory API.
     *
     * <p>Pattern: {@code streamingChatModel.chat(chatMemory.messages(), handler)}
     *
     * @param memory ChatMemory instance (repository of chat messages)
     * @param systemPrompt System prompt (role/context)
     * @param userMessage User message
     * @param options AI options
     * @param onToken Callback for each token
     */
    private void stream(dev.langchain4j.memory.ChatMemory memory, String systemPrompt, String userMessage, AIOptions options, Consumer<String> onToken) {
        if (streamingChatModel == null) {
            throw new AIException("Streaming not supported - no StreamingChatModel configured");
        }

        try {
            // Add system message if not already present
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                boolean hasSystemMessage = memory.messages().stream()
                    .anyMatch(msg -> msg instanceof SystemMessage);
                if (!hasSystemMessage) {
                    memory.add(SystemMessage.from(systemPrompt));
                }
            }

            // Add user message to memory
            memory.add(UserMessage.from(userMessage));

            // Use CompletableFuture to wait for streaming completion
            java.util.concurrent.CompletableFuture<Void> future = new java.util.concurrent.CompletableFuture<>();

            // Stream using StreamingChatResponseHandler with memory.messages()
            streamingChatModel.chat(
                memory.messages(),
                new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String partialResponse) {
                        onToken.accept(partialResponse);
                    }

                    @Override
                    public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse completeResponse) {
                        // Add AI response to memory
                        memory.add(completeResponse.aiMessage());
                        future.complete(null);
                    }

                    @Override
                    public void onError(Throwable error) {
                        future.completeExceptionally(error);
                    }
                }
            );

            // Wait for streaming to complete (blocking call)
            future.join();
        } catch (java.util.concurrent.CompletionException e) {
            // Unwrap CompletionException to get the actual error
            Throwable cause = e.getCause();
            if (cause instanceof AIException) {
                throw (AIException) cause;
            }
            throw new AIException("LangChain streaming with memory failed: " + cause.getMessage(), cause);
        } catch (Exception e) {
            throw new AIException("LangChain streaming with memory failed: " + e.getMessage(), e);
        }
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
            if (chatModel == null) {
                throw new AIException("ChatModel not configured");
            }

            String systemPrompt = "Extract information from the following text into JSON format matching the specified schema. Respond with JSON only.";

            // Create a custom AI Service interface for extraction with metadata
            // We can't use LLMService.extractWithMetadata because Result<T> with generic T
            // is not allowed in LangChain4j AI Services. So we create a custom interface here.
            interface ExtractWithMetadataService {
                @dev.langchain4j.service.SystemMessage("{{systemPrompt}}\n\nExtract information from the following text into the specified format. Respond with JSON only.")
                @dev.langchain4j.service.UserMessage("{{prompt}}")
                <T> dev.langchain4j.service.Result<T> extractWithMetadata(
                    @dev.langchain4j.service.V("systemPrompt") String systemPrompt,
                    @dev.langchain4j.service.V("prompt") String prompt,
                    Class<T> extractType
                );
            }

            // Create AI Service instance with the custom interface
            ExtractWithMetadataService service = AiServices.builder(ExtractWithMetadataService.class)
                .chatModel(chatModel)
                .build();

            // Call extraction with metadata
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

        builder.chatModel(chatModel);

        if (streamingChatModel != null) {
            builder.streamingChatModel(streamingChatModel);
        }

        // Auto-attach memory for convenience
        if (memoryProvider != null) {
            builder.chatMemory(memoryProvider.getOrCreate());
        }

        // Note: EmbeddingModel is not configured via builder for AI Services
        // AI Services use EmbeddingModel for RAG (ContentRetriever), not directly

        return builder.build();
    }

    /**
     * Create an AI Service instance with optional RAG, tools, and memory support.
     *
     * This method supports advanced configuration via AiServices builder with full type safety:
     * <pre>
     * // With RAG
     * ContentRetriever retriever = ...;
     * var service = langChainAdapter.aiService(MyService.class, builder -> builder
     *     .contentRetriever(retriever)
     * );
     *
     * // With tools (type-safe, no reflection needed!)
     * CalculatorTools tools = new CalculatorTools();
     * var service = langChainAdapter.aiService(MyService.class, builder -> builder
     *     .tools(tools)
     * );
     *
     * // With custom memory (overrides auto-generated memory)
     * ChatMemory memory = ChatMemoryProvider.getInstance().getOrCreate("user-123");
     * var service = langChainAdapter.aiService(MyService.class, builder -> builder
     *     .chatMemory(memory)
     * );
     *
     * // With multiple features
     * var service = langChainAdapter.aiService(MyService.class, builder -> builder
     *     .tools(tools)
     *     .contentRetriever(retriever)
     *     .chatMemory(memory)
     * );
     * </pre>
     *
     * <p><b>Automatic Memory:</b>
     * By default, a ChatMemory instance is automatically created using {@link ChatMemoryProvider#getOrCreate()}.
     * This provides conversation memory out of the box. If you configure your own memory via the builder,
     * it will override the auto-generated memory.
     *
     * <p><b>Type Safety:</b>
     * The builder parameter is fully typed as {@code Consumer<AiServices<T>>}, providing full IDE
     * autocomplete and compile-time type checking. No reflection needed!
     *
     * @param serviceClass AI Service interface class
     * @param config Configuration consumer that receives the AiServices builder
     * @return AI Service instance (proxy)
     */
    public <T> T aiService(Class<T> serviceClass, java.util.function.Consumer<AiServices<T>> config) {
        if (chatModel == null) {
            throw new IllegalStateException("ChatModel not configured - cannot create AI Service");
        }

        var builder = AiServices.builder(serviceClass);

        builder.chatModel(chatModel);

        if (streamingChatModel != null) {
            builder.streamingChatModel(streamingChatModel);
        }

        // Auto-attach memory for convenience (can be overridden by user's config)
        if (memoryProvider != null) {
            builder.chatMemory(memoryProvider.getOrCreate());
        }

        // Apply custom configuration (RAG, tools, memory, etc.)
        // User's config runs after auto-memory, so they can override if needed
        config.accept(builder);

        return builder.build();
    }

    /**
     * Calculate cost based on model, prompt tokens, and completion tokens.
     * Uses standard pricing per 1K tokens.
     *
     * TODO: Make this configurable and support all models/pricing tiers.
     */
    private double calculateCost(String model, int promptTokens, int completionTokens) {
        if (model == null || model.isEmpty()) {
            return 0.0;
        }

        // Standard pricing per 1K tokens (as of 2024)
        // These are approximate values - should be configurable
        double promptCostPer1K = 0.0;
        double completionCostPer1K = 0.0;

        String modelLower = model.toLowerCase();

        // OpenAI pricing (per 1K tokens)
        if (modelLower.contains("gpt-4") || modelLower.contains("gpt4")) {
            if (modelLower.contains("turbo") || modelLower.contains("0125")) {
                promptCostPer1K = 0.01;      // $0.01 per 1K input tokens
                completionCostPer1K = 0.03;   // $0.03 per 1K output tokens
            } else {
                promptCostPer1K = 0.03;      // $0.03 per 1K input tokens
                completionCostPer1K = 0.06;  // $0.06 per 1K output tokens
            }
        } else if (modelLower.contains("gpt-3.5") || modelLower.contains("gpt35")) {
            promptCostPer1K = 0.0015;        // $0.0015 per 1K input tokens
            completionCostPer1K = 0.002;     // $0.002 per 1K output tokens
        } else if (modelLower.contains("claude")) {
            // Anthropic Claude pricing
            promptCostPer1K = 0.008;          // $0.008 per 1K input tokens
            completionCostPer1K = 0.024;     // $0.024 per 1K output tokens
        } else if (modelLower.contains("mistral")) {
            // Mistral pricing
            promptCostPer1K = 0.0002;         // $0.0002 per 1K input tokens
            completionCostPer1K = 0.0006;    // $0.0006 per 1K output tokens
        } else {
            // Default: assume free/open-source model
            promptCostPer1K = 0.0;
            completionCostPer1K = 0.0;
        }

        // Calculate total cost
        double promptCost = (promptTokens / 1000.0) * promptCostPer1K;
        double completionCost = (completionTokens / 1000.0) * completionCostPer1K;

        return promptCost + completionCost;
    }
}



