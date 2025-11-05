package com.akilisha.oss.roya.examples.langchain4j.tutorial05_streaming;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIOptions;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tutorial 05: Streaming
 *
 * <p>Recreates LangChain4j's {@code _04_Streaming.java} tutorial.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>Streaming AI responses token-by-token</li>
 *   <li>Token counting estimation</li>
 *   <li>HTTP streaming with Server-Sent Events (SSE)</li>
 * </ul>
 *
 * <p>LangChain4j Approach:
 * <pre>
 * OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()...build();
 * model.chat(prompt, new StreamingChatResponseHandler() {
 *     void onPartialResponse(String partialResponse) { ... }
 *     void onCompleteResponse(ChatResponse completeResponse) { ... }
 *     void onError(Throwable error) { ... }
 * });
 * </pre>
 *
 * <p>Roya Approach:
 * <pre>
 * AI ai = req.get(AI.class);
 * ai.stream("You are helpful", "Tell me a story", token -> {
 *     // Handle each token as it arrives
 *     res.write(token);
 * });
 * </pre>
 *
 * <p><b>Key Points:</b>
 * <ul>
 *   <li>Roya's streaming uses a simple {@code Consumer<String>} callback</li>
 *   <li>Tokens are delivered as they arrive from the LLM</li>
 *   <li>HTTP streaming can be implemented with Server-Sent Events (SSE)</li>
 * </ul>
 *
 * <p>HTTP Endpoints:
 * <ul>
 *   <li>{@code GET /stream?prompt=Write a short funny poem about developers and null-pointers, 10 lines maximum}</li>
 *   <li>{@code GET /stream-sse?prompt=Tell me a story} - Server-Sent Events streaming</li>
 *   <li>{@code POST /stream-async} - Async streaming with CompletableFuture</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * // Simple streaming (prints tokens to console)
 * curl "<a href="http://localhost:3005/stream?prompt=Write">...</a> a short poem"
 *
 * // Server-Sent Events streaming (browser-compatible)
 * curl "<a href="http://localhost:3005/stream-sse?prompt=Tell">...</a> me a story"
 *
 * // Async streaming
 * curl -X POST <a href="http://localhost:3005/stream-async">...</a> \
 *   -H "Content-Type: application/json" \
 *   -d '{"prompt": "Write a poem"}'
 * </pre>
 */
public class Tutorial05Streaming {

    public static void main(String[] args) {
        var app = Roya.create();

        // Register AI plugin
        var aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        try {
            aiPlugin.start();
        } catch (Exception e) {
            System.err.println("❌ Error: AI plugin failed to start: " + e.getMessage());
            System.exit(1);
        }

        // Simple Streaming Example (Console Output)
        app.get("/stream", (req, res, next) -> {
            AI ai = req.get(AI.class);

            String prompt = req.query().get("prompt")
                .orElse("Write a short funny poem about developers and null-pointers, 10 lines maximum");

            // Count characters and estimate tokens
            int charCount = prompt.length();
            // Note: Token counting requires provider-specific estimators
            // For now, we'll use a simple approximation: ~4 characters per token
            int estimatedTokens = charCount / 4;

            // Stream the response token by token
            StringBuilder fullResponse = new StringBuilder();
            AtomicInteger tokenCount = new AtomicInteger(0);

            ai.stream(
                "You are a helpful assistant.",
                prompt,
                AIOptions.builder().model("gpt-4o-mini").build(),
                token -> {
                    fullResponse.append(token);
                    tokenCount.incrementAndGet();
                    System.out.print(token); // Print each token as it arrives
                }
            );

            res.json(Map.of(
                "prompt", prompt,
                "char_count", charCount,
                "estimated_tokens", estimatedTokens,
                "actual_tokens", tokenCount.get(),
                "full_response", fullResponse.toString(),
                "note", "Check console output to see tokens streaming in real-time"
            ));
        });

        // Server-Sent Events (SSE) Streaming Example
        app.get("/stream-sse", (req, res, next) -> {
            AI ai = req.get(AI.class);

            String prompt = req.query().get("prompt")
                .orElse("Tell me a short story about a developer");

            // Set SSE headers
            res.header("Content-Type", "text/event-stream");
            res.header("Cache-Control", "no-cache");
            res.header("Connection", "keep-alive");

            // Get output stream for SSE
            var outputStream = res.stream();

            try {
                // Stream tokens as SSE events
                ai.stream(
                    "You are a creative storyteller.",
                    prompt,
                    AIOptions.builder().model("gpt-4o-mini").build(),
                    token -> {
                        try {
                            // Format as SSE: "data: <token>\n\n"
                            String escapedToken = token.replace("\n", "\\n");
                            outputStream.write(("data: " + escapedToken + "\n\n").getBytes());
                            outputStream.flush();
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to write SSE event", e);
                        }
                    }
                );

                // Send completion event
                outputStream.write("data: [DONE]\n\n".getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                try {
                    outputStream.write(("data: Error: " + e.getMessage() + "\n\n").getBytes());
                    outputStream.close();
                } catch (Exception ex) {
                    // Ignore
                }
            }
        });

        // Async Streaming Example (using CompletableFuture)
        app.post("/stream-async", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, String> body = req.body(Map.class);
            String prompt = body != null && body.containsKey("prompt")
                ? body.get("prompt")
                : "Write a short poem";

            CompletableFuture<String> futureResponse = new CompletableFuture<>();
            StringBuilder responseBuilder = new StringBuilder();

            // Start streaming asynchronously
            ai.stream(
                "You are a helpful assistant.",
                prompt,
                AIOptions.builder().model("gpt-4o-mini").build(),
                    responseBuilder::append
            );

            // Wait for completion (in real implementation, you'd use proper async handling)
            // For this demo, we'll collect the response
            String fullResponse = responseBuilder.toString();

            res.json(Map.of(
                "prompt", prompt,
                "response", fullResponse,
                "note", "This demonstrates async streaming. In production, use proper async HTTP handling."
            ));
        });

        // Root endpoint with instructions
        app.get("/", (req, res, next) -> {
            res.json(Map.of(
                "tutorial", "Tutorial 05: Streaming",
                "endpoints", Map.of(
                    "/stream", "Simple streaming (prints to console). Query param: prompt",
                    "/stream-sse", "Server-Sent Events streaming. Query param: prompt",
                    "/stream-async", "Async streaming example. POST with JSON body: {\"prompt\": \"...\"}"
                ),
                "examples", Map.of(
                    "simple", "/stream?prompt=Write a short funny poem about developers and null-pointers, 10 lines maximum",
                    "sse", "/stream-sse?prompt=Tell me a story",
                    "async", "POST /stream-async with body: {\"prompt\": \"Write a poem\"}"
                ),
                "note", "Streaming requires StreamingChatModel to be configured. Check console output for token-by-token streaming."
            ));
        });

        System.out.println("==========================================");
        System.out.println("Tutorial 05: Streaming");
        System.out.println("==========================================");
        System.out.println("Server starting on http://localhost:3005");
        System.out.println();
        System.out.println("Endpoints:");
        System.out.println("  GET /stream?prompt=...");
        System.out.println("  GET /stream-sse?prompt=...");
        System.out.println("  POST /stream-async");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  curl \"http://localhost:3005/stream?prompt=Write a short poem\"");
        System.out.println("  curl \"http://localhost:3005/stream-sse?prompt=Tell me a story\"");
        System.out.println("==========================================");

        app.listen(3005);
    }
}

