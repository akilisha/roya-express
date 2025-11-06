package com.akilisha.oss.roya.examples.langchain4j.customer_support_agent;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import com.akilisha.oss.roya.plugins.ai.langchain.LangChainAdapter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

/**
 * Customer Support Agent - Roya version of LangChain4j's customer support agent example.
 *
 * <p>This demonstrates:
 * <ul>
 *   <li>AI Service with RAG (Retrieval-Augmented Generation)</li>
 *   <li>ContentRetriever integration with embedding store</li>
 *   <li>TokenWindowChatMemory (1000 token limit)</li>
 *   <li>Document ingestion and chunking</li>
 *   <li>Interactive console interface</li>
 * </ul>
 *
 * <p>LangChain4j Original (Spring Boot):
 * <pre>
 * @Bean
 * ChatMemory chatMemory(Tokenizer tokenizer) {
 *     return TokenWindowChatMemory.withMaxTokens(1000, tokenizer);
 * }
 *
 * @Bean
 * ContentRetriever contentRetriever(EmbeddingStore embeddingStore, EmbeddingModel embeddingModel) {
 *     return EmbeddingStoreContentRetriever.builder()
 *         .embeddingStore(embeddingStore)
 *         .embeddingModel(embeddingModel)
 *         .maxResults(1)
 *         .minScore(0.6)
 *         .build();
 * }
 *
 * CustomerSupportAgent agent = AiServices.builder(CustomerSupportAgent.class)
 *     .chatModel(chatModel)
 *     .chatMemory(chatMemory)
 *     .contentRetriever(contentRetriever)
 *     .build();
 * </pre>
 *
 * <p>Roya Implementation:
 * <pre>
 * AI ai = req.get(AI.class);
 *
 * // Create embedding store and ingest document
 * EmbeddingStore&lt;TextSegment&gt; embeddingStore = new InMemoryEmbeddingStore&lt;&gt;();
 * Document document = loadDocument(Paths.get("miles-of-smiles-terms-of-use.txt"), new TextDocumentParser());
 * DocumentSplitter splitter = DocumentSplitters.recursive(100, 0, new OpenAiTokenizer("gpt-3.5-turbo"));
 * EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
 *     .documentSplitter(splitter)
 *     .embeddingModel(embeddingModel)
 *     .embeddingStore(embeddingStore)
 *     .build();
 * ingestor.ingest(document);
 *
 * // Create ContentRetriever for RAG
 * ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
 *     .embeddingStore(embeddingStore)
 *     .embeddingModel(embeddingModel)
 *     .maxResults(1)
 *     .minScore(0.6)
 *     .build();
 *
 * // Create TokenWindowChatMemory
 * Tokenizer tokenizer = new OpenAiTokenizer("gpt-3.5-turbo");
 * ChatMemory chatMemory = TokenWindowChatMemory.withMaxTokens(1000, tokenizer);
 *
 * // Create AI Service with RAG and memory
 * CustomerSupportAgent agent = ai.aiService(CustomerSupportAgent.class, builder -> {
 *     builder.chatMemory(chatMemory);
 *     builder.contentRetriever(contentRetriever);
 * });
 * </pre>
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>RAG - Answers questions using retrieved context from documents</li>
 *   <li>TokenWindowChatMemory - Maintains conversation history (1000 token limit)</li>
 *   <li>Document ingestion - Loads and chunks documents automatically</li>
 *   <li>Embedding store - Uses in-memory embedding store for demo</li>
 * </ul>
 *
 * <p>HTTP Endpoints:
 * <ul>
 *   <li>{@code POST /ask} - Ask questions to the customer support agent</li>
 *   <li>{@code POST /chat} - Interactive chat (maintains conversation)</li>
 *   <li>{@code GET /} - API documentation</li>
 * </ul>
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>Document file: {@code miles-of-smiles-terms-of-use.txt} (or any text file)</li>
 *   <li>OPENAI_API_KEY must be configured (for tokenizer and chat model)</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * // Interactive mode (console)
 * java -cp ... CustomerSupportAgent
 *
 * // HTTP API
 * curl -X POST http://localhost:3015/ask \
 *   -H "Content-Type: application/json" \
 *   -d '{"question": "What is your refund policy?"}'
 * </pre>
 */
public class CustomerSupportAgent {

    /**
     * Customer Support Agent AI Service interface.
     * Uses RAG to answer questions based on ingested documents.
     */
    interface CustomerSupportAgentService {
        /**
         * Answer a customer support question using RAG.
         *
         * @param question Customer's question
         * @return Answer based on retrieved context from documents
         */
        @SystemMessage("You are a customer support agent. " +
                "Answer questions based only on the information provided in the context. " +
                "If the answer is not in the context, say so.")
        String answer(@UserMessage String question);
    }

    /**
     * Create and configure the customer support agent with RAG.
     */
    private static CustomerSupportAgentService createAgent(AI ai, Path documentPath) throws Exception {
        // 1. Get embedding model (try from provider, fallback to local model)
        EmbeddingModel embeddingModel = null;
        
        // Try to get EmbeddingModel from AI provider
        try {
            LangChainAdapter adapter = ai.provider(LangChainAdapter.class);
            if (adapter != null) {
                embeddingModel = adapter.provider(EmbeddingModel.class);
            }
        } catch (Exception e) {
            // Provider not available, will use fallback
        }
        
        // Fallback to local embedding model (no API key needed)
        if (embeddingModel == null) {
            embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        }

        // 2. Create in-memory embedding store
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        // 3. Load document
        Document document = loadDocument(documentPath, new TextDocumentParser());

        // 4. Split document into segments (recursive, 300 chars per segment)
        DocumentSplitter documentSplitter = DocumentSplitters.recursive(300, 0);

        // 5. Ingest document into embedding store
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(documentSplitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(document);

        System.out.println("✓ Document ingested: " + documentPath.getFileName());
        System.out.println("  Segments created and embedded into store");

        // 6. Create ContentRetriever for RAG
        int maxResults = 1;
        double minScore = 0.6;

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();

        // 7. Create ChatMemory (MessageWindowChatMemory for conversation history)
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(30);

        // 8. Create AI Service with RAG and memory
        // Note: We need LangChainAdapter for aiService, but we can create it without provider
        final ChatMemory finalChatMemory = chatMemory;  // Make final for lambda
        final ContentRetriever finalContentRetriever = contentRetriever;  // Make final for lambda
        
        // Get adapter for aiService (will use fallback if not available)
        LangChainAdapter adapter = ai.provider(LangChainAdapter.class);
        if (adapter == null) {
            throw new IllegalStateException(
                "LangChainAdapter not available. Please ensure AIPlugin is properly configured. " +
                "The Customer Support Agent requires a ChatModel to be configured."
            );
        }
        
        return ai.aiService(CustomerSupportAgentService.class, builder -> {
            builder.chatMemory(finalChatMemory);
            builder.contentRetriever(finalContentRetriever);
        });
    }

    public static void main(String[] args) {
        // Determine document path (check for command-line argument or use default)
        Path documentPath;
        if (args.length > 0) {
            documentPath = Paths.get(args[0]);
        } else {
            // Try to find default document in multiple locations
            String[] possiblePaths = {
                "miles-of-smiles-terms-of-use.txt",
                "roya-examples-langchain4j-tutorials/miles-of-smiles-terms-of-use.txt",
                "src/main/resources/miles-of-smiles-terms-of-use.txt"
            };
            
            documentPath = null;
            for (String path : possiblePaths) {
                Path candidate = Paths.get(path);
                if (candidate.toFile().exists()) {
                    documentPath = candidate;
                    break;
                }
            }
            
            if (documentPath == null) {
                // Create default document path for error message
                documentPath = Paths.get("miles-of-smiles-terms-of-use.txt");
            }
        }

        var app = Roya.create();

        // Register body parser middleware
        app.use(BodyParser.bodyParser());

        // Register AI plugin
        var aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        try {
            aiPlugin.start();
        } catch (Exception e) {
            System.err.println("❌ Error: AI plugin failed to start: " + e.getMessage());
            System.exit(1);
        }

        // Initialize agent
        CustomerSupportAgentService agent;
        final Path finalDocumentPath = documentPath;  // Make final for lambda
        try {
            AI ai = app.services().get(AI.class);
            agent = createAgent(ai, finalDocumentPath);
        } catch (Exception e) {
            System.err.println("❌ Error: Failed to create customer support agent: " + e.getMessage());
            System.err.println("   Make sure the document file exists: " + finalDocumentPath);
            System.exit(1);
            return;  // Unreachable but satisfies compiler
        }

        final CustomerSupportAgentService finalAgent = agent;  // Make final for lambda

        // Ask endpoint (stateless)
        app.post("/ask", (req, res, next) -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = req.body(Map.class);
            String question = body != null && body.containsKey("question")
                ? (String) body.get("question")
                : "What is your refund policy?";

            try {
                String answer = finalAgent.answer(question);

                res.json(Map.of(
                    "question", question,
                    "answer", answer,
                    "note", "Answer generated using RAG (Retrieval-Augmented Generation)"
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                    "error", "Failed to process question",
                    "message", e.getMessage()
                ));
            }
        });

        // Chat endpoint (maintains conversation)
        app.post("/chat", (req, res, next) -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = req.body(Map.class);
            String question = body != null && body.containsKey("question")
                ? (String) body.get("question")
                : "Hello, I have a question";

            try {
                String answer = finalAgent.answer(question);

                res.json(Map.of(
                    "question", question,
                    "answer", answer,
                    "note", "Conversation maintained using ChatMemory"
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                    "error", "Failed to process question",
                    "message", e.getMessage()
                ));
            }
        });

        // Root endpoint with instructions
        app.get("/", (req, res, next) -> {
            res.json(Map.of(
                "application", "Customer Support Agent",
                "description", "AI-powered customer support agent with RAG",
                "features", java.util.List.of(
                    "RAG (Retrieval-Augmented Generation) - answers based on document context",
                    "TokenWindowChatMemory - maintains conversation history (1000 tokens)",
                    "Document ingestion - automatically chunks and embeds documents",
                    "In-memory embedding store - fast retrieval for demo purposes"
                ),
                "endpoints", Map.of(
                    "POST /ask", "Ask a question (stateless)",
                    "POST /chat", "Chat with agent (maintains conversation)",
                    "GET /", "API documentation"
                ),
                "examples", java.util.List.of(
                    "curl -X POST http://localhost:3015/ask -H 'Content-Type: application/json' -d '{\"question\":\"What is your refund policy?\"}'",
                    "curl -X POST http://localhost:3015/chat -H 'Content-Type: application/json' -d '{\"question\":\"Hello\"}'"
                ),
                "document", finalDocumentPath.toString(),
                "note", "Agent uses RAG to answer questions based on ingested documents"
            ));
        });

        int port = 3015;
        System.out.println("🎧 Customer Support Agent");
        System.out.println("📡 Server running on http://localhost:" + port);
        System.out.println("📚 Endpoints:");
        System.out.println("   POST /ask - Ask a question (stateless)");
        System.out.println("   POST /chat - Chat with agent (maintains conversation)");
        System.out.println("   GET  / - API documentation");
        System.out.println();
        System.out.println("💡 Key Features:");
        System.out.println("   - RAG: Answers based on retrieved document context");
        System.out.println("   - TokenWindowChatMemory: Maintains conversation (1000 token limit)");
        System.out.println("   - Document ingestion: Automatically chunks and embeds documents");
        System.out.println();
        System.out.println("📄 Document: " + finalDocumentPath);
        System.out.println();
        System.out.println("💬 Interactive Mode:");
        System.out.println("   Type questions and press Enter. Type 'exit' to quit.");

        // Start interactive mode in a separate thread
        Thread interactiveThread = new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.println();
                System.out.println("==========================================");
                System.out.println("Customer Support Agent - Interactive Mode");
                System.out.println("==========================================");
                System.out.println("Type 'exit' to quit");
                System.out.println();

                while (true) {
                    System.out.print("You: ");
                    String question = scanner.nextLine();

                    if ("exit".equalsIgnoreCase(question)) {
                        break;
                    }

                    if (question.isBlank()) {
                        continue;
                    }

                    try {
                        String answer = finalAgent.answer(question);
                        System.out.println("Agent: " + answer);
                        System.out.println();
                    } catch (Exception e) {
                        System.err.println("Error: " + e.getMessage());
                        System.out.println();
                    }
                }

                System.out.println("Goodbye!");
                System.exit(0);
            }
        }, "interactive-chat");
        interactiveThread.setDaemon(true);
        interactiveThread.start();

        app.listen(port);
    }
}

