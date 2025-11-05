package com.akilisha.oss.roya.plugins.ai.langchain;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatMemory provider for managing ChatMemory instances per conversation ID.
 * 
 * <p>This provider exposes LangChain4j's ChatMemory primitives directly,
 * allowing framework users to access full ChatMemory capabilities including
 * messages, metadata, and memory management operations.
 * 
 * <p><b>Design Philosophy:</b>
 * <ul>
 *   <li><b>Auto-generate IDs</b>: If no conversation ID is provided, one is auto-generated</li>
 *   <li><b>No conflicts</b>: UUID-based IDs ensure uniqueness</li>
 *   <li><b>Flexible usage</b>: Works with explicit IDs, session IDs, or auto-generated IDs</li>
 *   <li><b>Framework-friendly</b>: Integrates naturally with session/user context</li>
 * </ul>
 * 
 * <p>Usage examples:
 * <pre>
 * // Option 1: Auto-generate conversation ID
 * ChatMemoryProvider provider = ChatMemoryProvider.getInstance();
 * ChatMemory memory = provider.getOrCreate(); // Returns new ChatMemory with auto-generated ID
 * String conversationId = memory.id(); // Get the generated ID
 * 
 * // Option 2: Use explicit conversation ID
 * ChatMemory memory = provider.getOrCreate("conversation-123");
 * 
 * // Option 3: Use session ID (from Session middleware)
 * String sessionId = Session.getSession(req).get("sessionId").toString();
 * ChatMemory memory = provider.getOrCreate("session:" + sessionId);
 * 
 * // Option 4: Use user ID (for multi-user apps)
 * String userId = req.params().get("userId").orElse("anonymous");
 * ChatMemory memory = provider.getOrCreate("user:" + userId);
 * </pre>
 */
public class ChatMemoryProvider {
    private static final ChatMemoryProvider INSTANCE = new ChatMemoryProvider();
    private final Map<String, ChatMemory> memories = new ConcurrentHashMap<>();
    private static final int DEFAULT_MAX_MESSAGES = 10;

    private ChatMemoryProvider() {}

    public static ChatMemoryProvider getInstance() {
        return INSTANCE;
    }

    /**
     * Get or create a ChatMemory instance with an auto-generated conversation ID.
     * 
     * <p>This is the recommended method for simple use cases where you don't need
     * to persist conversations across requests. The ID is a UUID, ensuring uniqueness.
     * 
     * @return ChatMemory instance (LangChain4j primitive) with auto-generated ID
     */
    public ChatMemory getOrCreate() {
        String conversationId = generateConversationId();
        return getOrCreate(conversationId);
    }

    /**
     * Get or create a ChatMemory instance for the given conversation ID.
     * 
     * <p>If the conversation ID is null or empty, a new UUID-based ID is generated.
     * This ensures you always get a valid ChatMemory instance without worrying about
     * ID conflicts or management.
     * 
     * @param conversationId Unique conversation identifier (null/empty = auto-generate)
     * @return ChatMemory instance (LangChain4j primitive)
     */
    public ChatMemory getOrCreate(String conversationId) {
        // Auto-generate ID if not provided
        String id = (conversationId == null || conversationId.isEmpty()) 
            ? generateConversationId() 
            : conversationId;
        
        return memories.computeIfAbsent(id, memoryId -> 
            MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(DEFAULT_MAX_MESSAGES)
                .build()
        );
    }

    /**
     * Get an existing ChatMemory instance, or null if not found.
     * 
     * @param conversationId Conversation identifier
     * @return ChatMemory instance or null
     */
    public ChatMemory get(String conversationId) {
        if (conversationId == null || conversationId.isEmpty()) {
            return null;
        }
        return memories.get(conversationId);
    }

    /**
     * Clear conversation memory for the given conversation ID.
     * 
     * @param conversationId Conversation identifier
     */
    public void clear(String conversationId) {
        if (conversationId != null && !conversationId.isEmpty()) {
            memories.remove(conversationId);
        }
    }

    /**
     * Clear all conversation memories.
     */
    public void clearAll() {
        memories.clear();
    }

    /**
     * Generate a unique conversation ID using UUID.
     * 
     * @return UUID-based conversation ID
     */
    private String generateConversationId() {
        return "conv-" + UUID.randomUUID().toString().replace("-", "");
    }
}

