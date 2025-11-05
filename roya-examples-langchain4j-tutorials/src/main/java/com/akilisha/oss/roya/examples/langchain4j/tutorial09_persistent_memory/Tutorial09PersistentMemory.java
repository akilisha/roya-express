package com.akilisha.oss.roya.examples.langchain4j.tutorial09_persistent_memory;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIOptions;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.database.Database;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tutorial 09: Persistent Memory Per User
 *
 * <p>Recreates LangChain4j's persistent memory tutorial.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>Persisting conversation memory across application restarts</li>
 *   <li>Storing ChatMemory in database per user</li>
 *   <li>Loading conversation history from persistent storage</li>
 *   <li>Multi-user conversation isolation</li>
 *   <li>Memory persistence and recovery</li>
 * </ul>
 *
 * <p>LangChain4j Approach:
 * <pre>
 * // Custom ChatMemory implementation that persists to database
 * ChatMemory memory = PersistentChatMemory.builder()
 *     .userId("user-123")
 *     .database(database)
 *     .build();
 *
 * // Memory persists across restarts
 * memory.add(UserMessage.from("Hello"));
 * Response&lt;AiMessage&gt; response = model.generate(memory.messages());
 * memory.add(response.content());
 * </pre>
 *
 * <p>Roya Approach:
 * <pre>
 * AI ai = req.get(AI.class);
 * Database db = req.get(Database.class);
 *
 * PersistentChatMemoryProvider provider = new PersistentChatMemoryProvider(db);
 * ChatMemory memory = provider.getOrCreate("user-123", "conversation-456");
 *
 * String response = ai.llm().ask(memory, "You are helpful", "Hello");
 * // Memory automatically persisted to database
 * </pre>
 *
 * <p><b>Key Points:</b>
 * <ul>
 *   <li>Memory persists across application restarts</li>
 *   <li>Each user has isolated conversation memories</li>
 *   <li>Database-backed storage ensures durability</li>
 *   <li>Uses LangChain4j's ChatMemory primitives</li>
 *   <li>Automatic serialization/deserialization of messages</li>
 * </ul>
 *
 * <p>HTTP Endpoints:
 * <ul>
 *   <li>{@code POST /chat/:userId} - Send message in user's conversation</li>
 *   <li>{@code GET /chat/:userId} - Get user's conversation history</li>
 *   <li>{@code DELETE /chat/:userId} - Clear user's conversation memory</li>
 *   <li>{@code GET /} - Root endpoint with instructions</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * // Send message as user "alice"
 * curl -X POST http://localhost:3009/chat/alice \
 *   -H "Content-Type: application/json" \
 *   -d '{"message": "Hello, my name is Alice"}'
 *
 * // Get conversation history for user "alice"
 * curl http://localhost:3009/chat/alice
 *
 * // Clear conversation for user "alice"
 * curl -X DELETE http://localhost:3009/chat/alice
 * </pre>
 *
 * <p><b>Note:</b> This tutorial requires the Database plugin to be configured.
 * Make sure PostgreSQL is running and DATABASE_URL is set in environment.
 */
public class Tutorial09PersistentMemory {

    /**
     * Persistent ChatMemory Provider that stores conversations in a database.
     * 
     * <p>Each user can have multiple conversations, each with its own ChatMemory.
     * Conversations are stored in the database and survive application restarts.
     */
    static class PersistentChatMemoryProvider {
        private final Database database;
        private final Map<String, ChatMemory> memoryCache = new java.util.concurrent.ConcurrentHashMap<>();

        public PersistentChatMemoryProvider(Database database) {
            this.database = database;
            // Ensure conversation_messages table exists
            initializeDatabase();
        }

        private void initializeDatabase() {
            // Create table if it doesn't exist using JOOQ DSL
            database.transaction(ctx -> {
                ctx.execute("""
                    CREATE TABLE IF NOT EXISTS conversation_messages (
                        id SERIAL PRIMARY KEY,
                        user_id VARCHAR(255) NOT NULL,
                        conversation_id VARCHAR(255) NOT NULL,
                        message_type VARCHAR(50) NOT NULL,
                        message_text TEXT NOT NULL,
                        message_order INTEGER NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE(user_id, conversation_id, message_order)
                    )
                    """);
                
                // Create index for faster lookups
                ctx.execute("""
                    CREATE INDEX IF NOT EXISTS idx_user_conversation 
                    ON conversation_messages(user_id, conversation_id)
                    """);
                return null;
            });
        }

        /**
         * Get or create ChatMemory for a user's conversation.
         * Loads from database if exists, otherwise creates new.
         */
        public ChatMemory getOrCreate(String userId, String conversationId) {
            String key = userId + ":" + conversationId;
            
            return memoryCache.computeIfAbsent(key, k -> {
                // Try to load from database
                ChatMemory memory = loadFromDatabase(userId, conversationId);
                if (memory == null) {
                    // Create new memory
                    memory = MessageWindowChatMemory.builder()
                        .id(conversationId)
                        .maxMessages(20) // Store more messages in persistent memory
                        .build();
                }
                return memory;
            });
        }

        /**
         * Load ChatMemory from database.
         */
        private ChatMemory loadFromDatabase(String userId, String conversationId) {
            try {
                List<Map<String, Object>> rows = database.transaction(ctx -> {
                    // Use JOOQ to query
                    return ctx.dsl()
                        .select()
                        .from(DSL.table("conversation_messages"))
                        .where(DSL.field("user_id").eq(userId))
                        .and(DSL.field("conversation_id").eq(conversationId))
                        .orderBy(DSL.field("message_order"))
                        .fetchMaps();
                });

                if (rows.isEmpty()) {
                    return null;
                }

                ChatMemory memory = MessageWindowChatMemory.builder()
                    .id(conversationId)
                    .maxMessages(20)
                    .build();

                // Reconstruct messages from database
                for (Map<String, Object> row : rows) {
                    String type = (String) row.get("message_type");
                    String text = (String) row.get("message_text");
                    
                    ChatMessage message = switch (type) {
                        case "SYSTEM" -> SystemMessage.from(text);
                        case "USER" -> UserMessage.from(text);
                        case "AI" -> AiMessage.from(text);
                        default -> null;
                    };
                    
                    if (message != null) {
                        memory.add(message);
                    }
                }

                return memory;
            } catch (Exception e) {
                System.err.println("Failed to load memory from database: " + e.getMessage());
                return null;
            }
        }

        /**
         * Save ChatMemory to database.
         */
        public void saveToDatabase(String userId, String conversationId, ChatMemory memory) {
            try {
                database.transaction(ctx -> {
                    // Delete existing messages for this conversation
                    ctx.dsl()
                        .deleteFrom(DSL.table("conversation_messages"))
                        .where(DSL.field("user_id").eq(userId))
                        .and(DSL.field("conversation_id").eq(conversationId))
                        .execute();

                    // Insert current messages
                    List<ChatMessage> messages = memory.messages();
                    for (int i = 0; i < messages.size(); i++) {
                        ChatMessage msg = messages.get(i);
                        String type = msg instanceof SystemMessage ? "SYSTEM" :
                                      msg instanceof UserMessage ? "USER" :
                                      msg instanceof AiMessage ? "AI" : "UNKNOWN";
                        String text = switch (msg) {
                            case SystemMessage sm -> sm.text();
                            case UserMessage um -> um.singleText();
                            case AiMessage am -> am.text();
                            default -> "";
                        };
                        
                        ctx.dsl()
                            .insertInto(DSL.table("conversation_messages"))
                            .set(DSL.field("user_id"), userId)
                            .set(DSL.field("conversation_id"), conversationId)
                            .set(DSL.field("message_type"), type)
                            .set(DSL.field("message_text"), text)
                            .set(DSL.field("message_order"), i)
                            .execute();
                    }
                    return null;
                });
            } catch (Exception e) {
                System.err.println("Failed to save memory to database: " + e.getMessage());
            }
        }

        /**
         * Clear memory for a user's conversation.
         */
        public void clear(String userId, String conversationId) {
            String key = userId + ":" + conversationId;
            memoryCache.remove(key);
            
            // Also delete from database
            try {
                database.transaction(ctx -> {
                    ctx.dsl()
                        .deleteFrom(DSL.table("conversation_messages"))
                        .where(DSL.field("user_id").eq(userId))
                        .and(DSL.field("conversation_id").eq(conversationId))
                        .execute();
                    return null;
                });
            } catch (Exception e) {
                System.err.println("Failed to clear memory from database: " + e.getMessage());
            }
        }

        /**
         * Get memory from cache (without loading from database).
         */
        public ChatMemory get(String userId, String conversationId) {
            String key = userId + ":" + conversationId;
            return memoryCache.get(key);
        }
    }

    public static void main(String[] args) {
        var app = Roya.create();

        // Register body parser middleware
        app.use(BodyParser.bodyParser());

        // Register Database plugin (required for persistent memory)
        var dbPlugin = new com.akilisha.oss.roya.plugins.database.DatabasePlugin();
        dbPlugin.register(app.services());
        try {
            dbPlugin.start();
        } catch (Exception e) {
            System.err.println("⚠️  Warning: Database plugin failed to start: " + e.getMessage());
            System.err.println("⚠️  Persistent memory will not work. Set DATABASE_URL environment variable.");
        }

        // Register AI plugin
        var aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        try {
            aiPlugin.start();
        } catch (Exception e) {
            System.err.println("❌ Error: AI plugin failed to start: " + e.getMessage());
            System.exit(1);
        }

        // Create persistent memory provider
        PersistentChatMemoryProvider memoryProvider = new PersistentChatMemoryProvider(
            app.services().get(Database.class)
        );

        // Chat endpoint - Send message in user's conversation (persistent memory)
        app.post("/chat/:userId", (req, res, next) -> {
            AI ai = req.get(AI.class);
            
            String userId = req.params().get("userId").orElse("anonymous");
            String conversationId = "default"; // Could be made configurable via query param

            @SuppressWarnings("unchecked")
            Map<String, String> body = req.body(Map.class);
            String message = body != null && body.containsKey("message")
                ? body.get("message")
                : "Hello";

            // Get persistent ChatMemory for this user's conversation
            ChatMemory memory = memoryProvider.getOrCreate(userId, conversationId);

            // Use ChatMemory with ask() - memory persists automatically
            String response = ai.llm().ask(
                memory,
                "You are a helpful assistant.",
                message,
                AIOptions.builder().model("gpt-4o-mini").build()
            );

            // Save memory to database after each interaction
            memoryProvider.saveToDatabase(userId, conversationId, memory);

            res.json(Map.of(
                "userId", userId,
                "conversationId", conversationId,
                "message", message,
                "response", response,
                "messageCount", memory.messages().size(),
                "note", "Memory persisted to database - survives application restarts"
            ));
        });

        // Get conversation history for a user
        app.get("/chat/:userId", (req, res, next) -> {
            String userId = req.params().get("userId").orElse("anonymous");
            String conversationId = "default";

            ChatMemory memory = memoryProvider.getOrCreate(userId, conversationId);

            List<ChatMessage> messages = memory.messages();
            List<Map<String, String>> messageList = messages.stream()
                .map(msg -> {
                    String text = switch (msg) {
                        case SystemMessage sm -> sm.text();
                        case UserMessage um -> um.singleText();
                        case AiMessage am -> am.text();
                        default -> msg.toString();
                    };
                    return Map.of(
                        "type", msg.getClass().getSimpleName(),
                        "text", text
                    );
                })
                .collect(Collectors.toList());

            res.json(Map.of(
                "userId", userId,
                "conversationId", conversationId,
                "messages", messageList,
                "messageCount", messages.size(),
                "note", "Messages loaded from persistent database storage"
            ));
        });

        // Clear conversation memory for a user
        app.delete("/chat/:userId", (req, res, next) -> {
            String userId = req.params().get("userId").orElse("anonymous");
            String conversationId = "default";

            memoryProvider.clear(userId, conversationId);

            res.json(Map.of(
                "userId", userId,
                "conversationId", conversationId,
                "status", "cleared",
                "note", "Conversation memory cleared from database"
            ));
        });

        // Root endpoint with instructions
        app.get("/", (req, res, next) -> {
            res.json(Map.of(
                "tutorial", "Tutorial 09: Persistent Memory Per User",
                "description", "Demonstrates database-backed conversation memory that persists across restarts",
                "requirements", Map.of(
                    "database", "PostgreSQL database (DATABASE_URL environment variable)",
                    "plugin", "Database plugin must be registered and started"
                ),
                "endpoints", Map.of(
                    "POST /chat/:userId", "Send message in user's conversation (memory persisted)",
                    "GET /chat/:userId", "Get user's conversation history (loaded from database)",
                    "DELETE /chat/:userId", "Clear user's conversation memory"
                ),
                "examples", List.of(
                    "curl -X POST http://localhost:3009/chat/alice -H 'Content-Type: application/json' -d '{\"message\":\"Hello\"}'",
                    "curl http://localhost:3009/chat/alice",
                    "curl -X DELETE http://localhost:3009/chat/alice"
                ),
                "note", "Memory persists across application restarts - restart the server and conversations remain!"
            ));
        });

        int port = 3009;
        System.out.println("🚀 Tutorial 09: Persistent Memory Per User");
        System.out.println("📡 Server running on http://localhost:" + port);
        System.out.println("📚 Endpoints:");
        System.out.println("   POST /chat/:userId - Send message (memory persisted to database)");
        System.out.println("   GET  /chat/:userId - Get conversation history (loaded from database)");
        System.out.println("   DELETE /chat/:userId - Clear conversation memory");
        System.out.println("   GET  / - API documentation");
        System.out.println();
        System.out.println("💡 Key Feature: Memory persists across application restarts!");
        System.out.println("   Restart the server and conversations remain intact.");

        app.listen(port);
    }
}

