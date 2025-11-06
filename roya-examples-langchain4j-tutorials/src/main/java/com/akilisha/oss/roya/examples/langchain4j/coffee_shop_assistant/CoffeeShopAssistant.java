package com.akilisha.oss.roya.examples.langchain4j.coffee_shop_assistant;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.ai.RAGOptions;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coffee Shop Assistant - Roya version of Helidon's coffee shop assistant example.
 *
 * <p>This demonstrates:
 * <ul>
 *   <li>AI Service with chat memory (10 message window)</li>
 *   <li>RAG integration for menu items</li>
 *   <li>REST API for chat interactions</li>
 *   <li>Conversation memory management</li>
 * </ul>
 *
 * <p>Helidon Original:
 * <pre>
 * @Ai.Service
 * @Ai.ChatMemoryWindow(10)
 * public interface ChatAiService {
 *     @SystemMessage("You are Frank - a server in a coffee shop...")
 *     String chat(String question);
 * }
 * </pre>
 *
 * <p>Roya Implementation:
 * <pre>
 * interface ChatAiService {
 *     @SystemMessage("You are Frank - a server in a coffee shop...")
 *     String chat(String question);
 * }
 *
 * // Create service with memory window
 * ChatMemory memory = MessageWindowChatMemory.builder()
 *     .maxMessages(10)
 *     .build();
 * ChatAiService service = ai.aiService(ChatAiService.class, builder -> {
 *     builder.chatMemory(memory);
 * });
 * </pre>
 *
 * <p>HTTP Endpoints:
 * <ul>
 *   <li>{@code POST /chat} - Chat with the coffee shop assistant</li>
 *   <li>{@code POST /chat/:conversationId} - Continue conversation</li>
 *   <li>{@code GET /menu} - Get menu items (from RAG)</li>
 *   <li>{@code POST /index-menu} - Index menu items into RAG</li>
 *   <li>{@code GET /} - API documentation</li>
 * </ul>
 */
public class CoffeeShopAssistant {

    public static void main(String[] args) {
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

        // Chat memory manager (manages conversations per conversation ID)
        ChatMemoryManager memoryManager = new ChatMemoryManager();

        // Index menu items endpoint (initializes RAG with menu data)
        app.post("/index-menu", (req, res, next) -> {
            AI ai = req.get(AI.class);

            // Sample menu items (in a real app, this would come from a database or file)
            List<String> menuItems = List.of(
                    "Espresso - $2.50 - Strong, concentrated coffee served in a small cup",
                    "Cappuccino - $3.50 - Espresso with steamed milk and foam",
                    "Latte - $3.75 - Espresso with steamed milk and a small amount of foam",
                    "Americano - $2.75 - Espresso with hot water",
                    "Mocha - $4.00 - Espresso with chocolate and steamed milk",
                    "Macchiato - $3.25 - Espresso with a dollop of foam",
                    "Cold Brew - $3.50 - Cold-brewed coffee served over ice",
                    "Frappuccino - $4.50 - Blended coffee drink with ice and flavors",
                    "Green Tea - $2.25 - Traditional green tea",
                    "Black Tea - $2.25 - Classic black tea",
                    "Chai Latte - $3.75 - Spiced tea with steamed milk",
                    "Croissant - $3.00 - Buttery, flaky pastry",
                    "Bagel - $2.50 - Fresh bagel with cream cheese",
                    "Muffin - $2.75 - Fresh baked muffin",
                    "Sandwich - $6.50 - Fresh sandwich with your choice of filling"
            );

            try {
                // Index menu items into RAG collection
                String collection = "menu-items";
                ai.vectors().createCollection(collection);

                List<AI.VectorDoc> documents = menuItems.stream()
                        .map(item -> new AI.VectorDoc(
                                "menu-" + menuItems.indexOf(item),
                                item,
                                Map.of("type", "menu-item", "category", "food")
                        ))
                        .toList();

                ai.vectors().index(collection, documents);

                res.json(Map.of(
                        "status", "indexed",
                        "collection", collection,
                        "itemCount", menuItems.size(),
                        "note", "Menu items have been indexed into RAG. The assistant can now answer questions about the menu."
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                        "error", "Failed to index menu items",
                        "message", e.getMessage(),
                        "note", "Make sure Qdrant is running"
                ));
            }
        });

        // Chat endpoint (creates new conversation)
        app.post("/chat", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = req.body(Map.class);
            String question = body != null && body.containsKey("question")
                    ? (String) body.get("question")
                    : "Hello, what's on the menu?";

            // Generate conversation ID
            String conversationId = "conv-" + System.currentTimeMillis();

            // Get or create chat memory for this conversation
            ChatMemory memory = memoryManager.getOrCreate(conversationId);

            // Create AI Service with memory window (matching Helidon's @Ai.ChatMemoryWindow(10))
            ChatAiService service = ai.aiService(ChatAiService.class, builder -> {
                builder.chatMemory(memory);
            });

            String response = service.chat(question);

            res.json(Map.of(
                    "conversationId", conversationId,
                    "question", question,
                    "response", response,
                    "note", "Chat memory maintains context across messages (10 message window)"
            ));
        });

        // Chat endpoint with conversation ID (continues existing conversation)
        app.post("/chat/:conversationId", (req, res, next) -> {
            AI ai = req.get(AI.class);

            String conversationId = req.params().get("conversationId")
                    .orElseThrow(() -> new IllegalArgumentException("conversationId parameter is required"));

            @SuppressWarnings("unchecked")
            Map<String, Object> body = req.body(Map.class);
            String question = body != null && body.containsKey("question")
                    ? (String) body.get("question")
                    : "What else do you have?";

            // Get or create chat memory for this conversation
            ChatMemory memory = memoryManager.getOrCreate(conversationId);

            // Create AI Service with memory window
            ChatAiService service = ai.aiService(ChatAiService.class, builder -> {
                builder.chatMemory(memory);
            });

            String response = service.chat(question);

            res.json(Map.of(
                    "conversationId", conversationId,
                    "question", question,
                    "response", response,
                    "messageCount", memory.messages().size(),
                    "note", "Continuing conversation - memory maintains context"
            ));
        });

        // Get menu items endpoint (from RAG)
        app.get("/menu", (req, res, next) -> {
            AI ai = req.get(AI.class);

            try {
                // Query RAG for menu items
                String query = "What items are on the menu?";
                RAGOptions options = RAGOptions.builder()
                        .collection("menu-items")
                        .topK(20)
                        .build();

                var response = ai.ragApi().ask(query, options);

                res.json(Map.of(
                        "menu", response.sources().stream()
                                .map(doc -> doc.content())
                                .toList(),
                        "query", query,
                        "note", "Menu items retrieved from RAG (vector store)"
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                        "error", "Failed to retrieve menu",
                        "message", e.getMessage(),
                        "note", "Make sure menu items are indexed (POST /index-menu)"
                ));
            }
        });

        // Root endpoint with instructions
        app.get("/", (req, res, next) -> {
            res.json(Map.of(
                    "application", "Coffee Shop Assistant",
                    "description", "AI-powered coffee shop assistant with chat memory and RAG",
                    "features", List.of(
                            "Chat memory (10 message window) - maintains context across messages",
                            "RAG integration - menu items indexed for semantic search",
                            "Conversation management - continue conversations with conversation ID",
                            "Natural language interactions - ask about menu, place orders"
                    ),
                    "endpoints", Map.of(
                            "POST /index-menu", "Index menu items into RAG (run once)",
                            "POST /chat", "Start a new chat conversation",
                            "POST /chat/:conversationId", "Continue an existing conversation",
                            "GET /menu", "Get menu items (from RAG)"
                    ),
                    "examples", List.of(
                            "curl -X POST http://localhost:3013/index-menu",
                            "curl -X POST http://localhost:3013/chat -H 'Content-Type: application/json' -d '{\"question\":\"What coffee do you have?\"}'",
                            "curl -X POST http://localhost:3013/chat/conv-123 -H 'Content-Type: application/json' -d '{\"question\":\"I'll have a latte\"}'"
                    ),
                    "note", "First run POST /index-menu to initialize menu items, then start chatting!"
            ));
        });

        int port = 3013;
        System.out.println("☕ Coffee Shop Assistant");
        System.out.println("📡 Server running on http://localhost:" + port);
        System.out.println("📚 Endpoints:");
        System.out.println("   POST /index-menu - Index menu items into RAG (run once)");
        System.out.println("   POST /chat - Start a new chat conversation");
        System.out.println("   POST /chat/:conversationId - Continue an existing conversation");
        System.out.println("   GET  /menu - Get menu items");
        System.out.println("   GET  / - API documentation");
        System.out.println();
        System.out.println("💡 Features:");
        System.out.println("   - Chat memory (10 message window) - maintains context");
        System.out.println("   - RAG integration - menu items indexed for semantic search");
        System.out.println("   - Natural language - ask about menu, place orders");
        System.out.println();
        System.out.println("📋 First Steps:");
        System.out.println("   1. Run: POST /index-menu (to initialize menu items)");
        System.out.println("   2. Run: POST /chat (to start chatting)");

        app.listen(port);
    }

    /**
     * AI Service interface for the coffee shop assistant.
     * Equivalent to Helidon's ChatAiService with @Ai.Service and @Ai.ChatMemoryWindow(10).
     */
    interface ChatAiService {
        /**
         * Responds to a given question in a human-friendly manner.
         *
         * @param question the customer's question or request
         * @return a response in natural language, adhering to the role of a coffee shop server
         */
        @SystemMessage("""
                You are Frank - a server in a coffee shop.
                You must not answer any questions not related to the menu or making orders.
                Use the saveOrder callback function to save the order.
                """)
        String chat(@UserMessage String question);
    }

    /**
     * Chat memory manager - maintains separate conversations per conversation ID.
     * Uses MessageWindowChatMemory with max 10 messages (matching Helidon's @Ai.ChatMemoryWindow(10)).
     */
    private static class ChatMemoryManager {
        private static final int MEMORY_WINDOW = 10; // Match Helidon's @Ai.ChatMemoryWindow(10)
        private final Map<String, ChatMemory> memories = new ConcurrentHashMap<>();

        public ChatMemory getOrCreate(String conversationId) {
            return memories.computeIfAbsent(conversationId, id ->
                    MessageWindowChatMemory.builder()
                            .id(id)
                            .maxMessages(MEMORY_WINDOW)
                            .build()
            );
        }

        public ChatMemory get(String conversationId) {
            return memories.get(conversationId);
        }

        public void clear(String conversationId) {
            memories.remove(conversationId);
        }
    }
}

