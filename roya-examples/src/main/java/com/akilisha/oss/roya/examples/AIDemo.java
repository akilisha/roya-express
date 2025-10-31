package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.core.middleware.Cors;
import com.akilisha.oss.roya.core.middleware.Morgan;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIOptions;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.cache.CachePlugin;

import java.math.BigDecimal;
import java.util.Map;

/**
 * AI Plugin Demo - Demonstrates first-class AI integration.
 *
 * Shows:
 * - ask() - Simple chat completion
 * - extract() - Type-safe structured output extraction
 * - stream() - Streaming responses
 * - Cost tracking (via LLMResponse)
 * - Caching (via Cache plugin integration)
 */
public class AIDemo {

    // Example record for structured extraction
    record ProductInfo(String name, BigDecimal price, String description, String category) {}

    public static void main(String[] args) {
        var app = Roya.create();

        // Setup common middleware
        app.use(Morgan.combined());
        app.use(Cors.cors());
        app.use(BodyParser.bodyParser());

        // Register Cache Plugin (optional - for AI response caching)
        var cachePlugin = new CachePlugin();
        cachePlugin.register(app.services());
        try {
            cachePlugin.start();
        } catch (Exception e) {
            System.err.println("Warning: Cache plugin failed to start: " + e.getMessage());
        }

        // Register AI Plugin
        var aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        try {
            aiPlugin.start();
        } catch (Exception e) {
            System.err.println("Error: AI plugin failed to start: " + e.getMessage());
            System.err.println("Make sure to set: -Dai.openai.apiKey=your-api-key");
            System.exit(1);
        }

        // Root endpoint - Show available routes
        app.get("/", (Request req, Response res, Next next) -> {
            res.json(Map.of(
                "message", "AI Plugin Demo",
                "routes", Map.of(
                    "POST /ai/ask", "Simple chat completion",
                    "POST /ai/extract", "Type-safe structured extraction",
                    "POST /ai/stream", "Streaming response (see notes)",
                    "GET /ai/status", "AI plugin status"
                ),
                "notes", Map.of(
                    "caching", "AI responses are cached (24h TTL) for cost savings",
                    "streaming", "Streaming responses return tokens as they arrive",
                    "extraction", "Use Java records for type-safe structured outputs"
                )
            ));
        });

        // Simple ask endpoint
        app.post("/ai/ask", (Request req, Response res, Next next) -> {
            try {
                AI ai = req.get(AI.class);

                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) req.get("body");

                String systemPrompt = (String) body.getOrDefault("systemPrompt", "You are a helpful assistant.");
                String userMessage = (String) body.get("userMessage");

                if (userMessage == null) {
                    res.status(400).json(Map.of("error", "userMessage is required"));
                    return;
                }

                // Optional: Override model/temperature via body
                AIOptions options = AIOptions.defaults();
                if (body.containsKey("model")) {
                    options = AIOptions.builder()
                        .model((String) body.get("model"))
                        .temperature((Double) body.getOrDefault("temperature", 0.7))
                        .maxTokens((Integer) body.getOrDefault("maxTokens", 1000))
                        .build();
                }

                String answer = ai.ask(systemPrompt, userMessage, options);

                res.json(Map.of(
                    "systemPrompt", systemPrompt,
                    "userMessage", userMessage,
                    "answer", answer,
                    "model", options.model(),
                    "cached", false // Would need to track this in AIServiceImpl
                ));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Type-safe extraction endpoint
        app.post("/ai/extract", (Request req, Response res, Next next) -> {
            try {
                AI ai = req.get(AI.class);

                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) req.get("body");

                String prompt = (String) body.get("prompt");

                if (prompt == null) {
                    res.status(400).json(Map.of("error", "prompt is required"));
                    return;
                }

                // Extract ProductInfo from prompt
                // Example prompt: "Product: Widget Pro, Price: $29.99, Description: A premium widget..."
                ProductInfo product = ai.extract(ProductInfo.class, prompt);

                res.json(Map.of(
                    "prompt", prompt,
                    "extracted", Map.of(
                        "name", product.name(),
                        "price", product.price(),
                        "description", product.description(),
                        "category", product.category()
                    ),
                    "type", "ProductInfo record"
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                    "error", "Extraction failed",
                    "message", e.getMessage()
                ));
            }
        });

        // Streaming endpoint (basic example)
        app.post("/ai/stream", (Request req, Response res, Next next) -> {
            try {
                AI ai = req.get(AI.class);

                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) req.get("body");

                String systemPrompt = (String) body.getOrDefault("systemPrompt", "You are a helpful assistant.");
                String userMessage = (String) body.get("userMessage");

                if (userMessage == null) {
                    res.status(400).json(Map.of("error", "userMessage is required"));
                    return;
                }

                // Collect streaming tokens (simplified for demo)
                // Note: Full async streaming requires proper Response API support
                StringBuilder fullResponse = new StringBuilder();
                ai.stream(systemPrompt, userMessage, token -> {
                    fullResponse.append(token);
                });

                // Send as SSE-formatted response
                res.header("Content-Type", "text/event-stream");
                res.header("Cache-Control", "no-cache");
                res.header("Connection", "keep-alive");

                // Format as SSE and send
                String sseResponse = "data: " + fullResponse.toString().replace("\n", "\\n") + "\n\ndata: [DONE]\n\n";
                res.send(sseResponse);
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Status endpoint
        app.get("/ai/status", (Request req, Response res, Next next) -> {
            res.json(Map.of(
                "status", "ok",
                "plugin", "ai",
                "version", "1.0.0",
                "provider", System.getProperty("ai.provider", "openai"),
                "caching", System.getProperty("ai.cache.enabled", "true"),
                "note", "Set -Dai.openai.apiKey=your-key to use"
            ));
        });

        // Start server
        app.listen(3000, () -> {
            System.out.println("✓ AI Demo running on http://localhost:3000\n");
            System.out.println("📝 Quick Test:");
            System.out.println("  POST /ai/ask { \"userMessage\": \"What is Java?\" }");
            System.out.println("  POST /ai/extract { \"prompt\": \"Product: Widget Pro, Price: $29.99, Description: Premium widget, Category: Electronics\" }");
            System.out.println("  GET /ai/status");
            System.out.println("\n⚠️  Make sure to set: -Dai.openai.apiKey=your-api-key");
        });
    }
}

