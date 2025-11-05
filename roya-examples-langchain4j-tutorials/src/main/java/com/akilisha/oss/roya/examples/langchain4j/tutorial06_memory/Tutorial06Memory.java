package com.akilisha.oss.roya.examples.langchain4j.tutorial06_memory;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIOptions;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tutorial 06: Memory/Conversation
 *
 * <p>Recreates LangChain4j's {@code _05_Memory.java} tutorial.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>Conversation history management using LangChain4j's ChatMemory</li>
 *   <li>Context-aware responses</li>
 *   <li>Multi-turn conversations</li>
 *   <li>Memory persistence across requests</li>
 * </ul>
 *
 * <p>LangChain4j Approach:
 * <pre>
 * ChatMemory memory = MessageWindowChatMemory.builder()
 *     .id("conversation-123")
 *     .maxMessages(10)
 *     .build();
 * ChatModel model = ...;
 *
 * // First turn
 * memory.add(UserMessage.from("Hello, my name is Klaus"));
 * Response&lt;AiMessage&gt; response = model.generate(memory.messages());
 * memory.add(response.content());
 *
 * // Second turn (model remembers previous messages)
 * memory.add(UserMessage.from("What is my name?"));
 * Response&lt;AiMessage&gt; response2 = model.generate(memory.messages());
 * memory.add(response2.content());
 * </pre>
 *
 * <p>Roya Approach:
 * <pre>
 * AI ai = req.get(AI.class);
 * ChatMemoryProvider provider = ChatMemoryProvider.getInstance();
 * ChatMemory memory = provider.getOrCreate("conversation-123");
 *
 * // First turn - ChatMemory manages context automatically
 * String response1 = ai.llm().ask(memory, "You are a helpful assistant.", "Hello, my name is Klaus");
 *
 * // Second turn - uses same ChatMemory, context is maintained
 * String response2 = ai.llm().ask(memory, "You are a helpful assistant.", "What is my name?");
 * </pre>
 *
 * <p><b>Key Points:</b>
 * <ul>
 *   <li>Uses LangChain4j's ChatMemory primitives directly</li>
 *   <li>Memory persists across multiple requests via conversation ID</li>
 *   <li>LangChain4j handles message management automatically</li>
 *   <li>Memory can be cleared or reset per conversation</li>
 *   <li>Works with both ask() and stream() methods</li>
 * </ul>
 *
 * <p>HTTP Endpoints:
 * <ul>
 *   <li>{@code POST /chat} - Send a message in a conversation</li>
 *   <li>{@code GET /chat/:conversationId} - Get conversation history (as ChatMessage list)</li>
 *   <li>{@code DELETE /chat/:conversationId} - Clear conversation memory</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * // Start a conversation
 * curl -X POST <a href="http://localhost:3006/chat">...</a> \
 *   -H "Content-Type: application/json" \
 *   -d '{"conversationId": "conv-1", "message": "Hello, my name is Klaus"}'
 *
 * curl -X POST "http://localhost:3006/chat?conversationId=1" \
 *   --header 'Content-Type: application/x-www-form-urlencoded'
 *   -d "conversationId%3D1%26message%3Ddood%2C%20where%27s%20my%20car%3F"
 *
 * // Continue the conversation
 * curl -X POST <a href="http://localhost:3006/chat">...</a> \
 *   -H "Content-Type: application/json" \
 *   -d '{"conversationId": "conv-1", "message": "What is my name?"}'
 *
 * // Get conversation history (returns LangChain4j ChatMessage objects)
 * curl <a href="http://localhost:3006/chat/conv-1">...</a>
 * curl http://localhost:3006/chat/1
 *
 * // Clear conversation
 * curl -X DELETE <a href="http://localhost:3006/chat/conv-1">...</a>
 * </pre>
 */
public class Tutorial06Memory {

    public static void main(String[] args) {
        var app = Roya.create();

        // Register body parser middleware for form-urlencoded and JSON
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

        // Chat endpoint - Send a message in a conversation using LangChain4j's ChatMemory
        app.post("/chat", (req, res, next) -> {
            AI ai = req.get(AI.class);

            // Check query parameters first, then body
            String conversationId = req.query().get("conversationId")
                .orElseGet(() -> {
                    @SuppressWarnings("unchecked")
                    Map<String, String> body = req.body(Map.class);
                    return body != null && body.containsKey("conversationId")
                        ? body.get("conversationId")
                        : "default-" + System.currentTimeMillis();
                });

            @SuppressWarnings("unchecked")
            Map<String, String> body = req.body(Map.class);
            String message = body != null && body.containsKey("message")
                ? body.get("message")
                : "Hello";

            // Use LangChain4j's ChatMemory via overloaded ask() method
            var memoryProvider = com.akilisha.oss.roya.plugins.ai.langchain.ChatMemoryProvider.getInstance();
            dev.langchain4j.memory.ChatMemory memory = memoryProvider.getOrCreate(conversationId);

            String response = ai.llm().ask(
                memory,
                "You are a helpful assistant.",
                message,
                AIOptions.builder().model("gpt-4o-mini").build()
            );

            // Get conversation history using LangChain4j's ChatMemory
            // We'll expose this via a separate endpoint
            res.json(Map.of(
                "conversationId", conversationId,
                "message", message,
                "response", response,
                "note", "Conversation memory is maintained by LangChain4j's ChatMemory"
            ));
        });

        // Get conversation history - returns LangChain4j ChatMessage objects
        app.get("/chat/:conversationId", (req, res, next) -> {
            String conversationId = req.params().get("conversationId").orElse("unknown");

            // Get ChatMemory instance and return its messages
            // Access ChatMemoryProvider to get ChatMemory - this exposes LangChain4j primitives
            var memoryProvider = com.akilisha.oss.roya.plugins.ai.langchain.ChatMemoryProvider.getInstance();
            ChatMemory chatMemory = memoryProvider.get(conversationId);

            if (chatMemory == null) {
                res.json(Map.of(
                    "conversationId", conversationId,
                    "messages", List.of(),
                    "messageCount", 0,
                    "note", "Conversation not found"
                ));
                return;
            }

            // Return LangChain4j ChatMessage objects directly from ChatMemory
            List<ChatMessage> messages = chatMemory.messages();
            res.json(Map.of(
                "conversationId", conversationId,
                "messages", messages.stream()
                    .map(msg -> {
                        // Extract text from ChatMessage - different types have different methods
                        String text = switch (msg) {
                            case dev.langchain4j.data.message.SystemMessage systemMessage -> systemMessage.text();
                            case dev.langchain4j.data.message.UserMessage userMessage -> userMessage.singleText();
                            case dev.langchain4j.data.message.AiMessage aiMessage -> aiMessage.text();
                            default -> msg.toString(); // Fallback
                        };
                        return Map.of(
                            "type", msg.getClass().getSimpleName(),
                            "text", text
                        );
                    })
                    .collect(Collectors.toList()),
                "messageCount", messages.size(),
                "note", "Messages are LangChain4j ChatMessage objects from ChatMemory"
            ));
        });

        // Clear conversation memory
        app.delete("/chat/:conversationId", (req, res, next) -> {
            String conversationId = req.params().get("conversationId").orElse("unknown");

            // Clear memory using ChatMemoryProvider
            var memoryProvider = com.akilisha.oss.roya.plugins.ai.langchain.ChatMemoryProvider.getInstance();
            memoryProvider.clear(conversationId);

            res.json(Map.of(
                "conversationId", conversationId,
                "status", "cleared",
                "note", "ChatMemory cleared for this conversation"
            ));
        });

        // Example: Multi-turn conversation demo
        app.get("/demo", (req, res, next) -> {
            AI ai = req.get(AI.class);
            String conversationId = "demo-conversation";

            // Get ChatMemory instance
            var memoryProvider = com.akilisha.oss.roya.plugins.ai.langchain.ChatMemoryProvider.getInstance();
            dev.langchain4j.memory.ChatMemory memory = memoryProvider.getOrCreate(conversationId);

            // Clear any existing messages
            memory.clear();

            List<String> responses = new ArrayList<>();

            // Turn 1: Introduce yourself (uses ChatMemory)
            String response1 = ai.llm().ask(
                memory,
                "You are a helpful assistant.",
                "Hello, my name is Klaus",
                AIOptions.builder().model("gpt-4o-mini").build()
            );
            responses.add("Turn 1 - User: Hello, my name is Klaus");
            responses.add("Turn 1 - Assistant: " + response1);

            // Turn 2: Ask about name (ChatMemory maintains context automatically)
            String response2 = ai.llm().ask(
                memory,
                "You are a helpful assistant.",
                "What is my name?",
                AIOptions.builder().model("gpt-4o-mini").build()
            );
            responses.add("Turn 2 - User: What is my name?");
            responses.add("Turn 2 - Assistant: " + response2);

            res.json(Map.of(
                "conversationId", conversationId,
                "demonstration", "Multi-turn conversation using LangChain4j's ChatMemory",
                "responses", responses,
                "note", "The assistant remembers the name from the first turn via ChatMemory"
            ));
        });

        // Root endpoint with instructions
        app.get("/", (req, res, next) -> {
            res.json(Map.of(
                "tutorial", "Tutorial 06: Memory/Conversation",
                "implementation", "Uses LangChain4j's ChatMemory primitives directly",
                "endpoints", Map.of(
                    "POST /chat", "Send a message in a conversation. Body: {\"conversationId\": \"...\", \"message\": \"...\"}",
                    "GET /chat/:conversationId", "Get conversation history (returns LangChain4j ChatMessage objects)",
                    "DELETE /chat/:conversationId", "Clear conversation memory",
                    "GET /demo", "Run a multi-turn conversation demo"
                ),
                "examples", Map.of(
                    "start_conversation", "POST /chat with body: {\"conversationId\": \"conv-1\", \"message\": \"Hello, my name is Klaus\"}",
                    "continue_conversation", "POST /chat with body: {\"conversationId\": \"conv-1\", \"message\": \"What is my name?\"}",
                    "get_history", "GET /chat/conv-1",
                    "clear_conversation", "DELETE /chat/conv-1",
                    "demo", "GET /demo"
                ),
                "note", "This tutorial uses LangChain4j's ChatMemory API directly - no simulation, real framework implementation."
            ));
        });

        System.out.println("==========================================");
        System.out.println("Tutorial 06: Memory/Conversation");
        System.out.println("==========================================");
        System.out.println("Server starting on http://localhost:3006");
        System.out.println();
        System.out.println("Endpoints:");
        System.out.println("  POST /chat");
        System.out.println("  GET /chat/:conversationId");
        System.out.println("  DELETE /chat/:conversationId");
        System.out.println("  GET /demo");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  curl -X POST http://localhost:3006/chat \\");
        System.out.println("    -H 'Content-Type: application/json' \\");
        System.out.println("    -d '{\"conversationId\": \"conv-1\", \"message\": \"Hello, my name is Klaus\"}'");
        System.out.println("  curl -X POST http://localhost:3006/chat \\");
        System.out.println("    -H 'Content-Type: application/json' \\");
        System.out.println("    -d '{\"conversationId\": \"conv-1\", \"message\": \"What is my name?\"}'");
        System.out.println("  curl http://localhost:3006/chat/conv-1");
        System.out.println("==========================================");

        app.listen(3006);
    }
}

