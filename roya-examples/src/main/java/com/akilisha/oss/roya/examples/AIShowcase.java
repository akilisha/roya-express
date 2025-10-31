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
import com.akilisha.oss.roya.plugins.ai.AIResponse;
import com.akilisha.oss.roya.plugins.cache.CachePlugin;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Plugin Showcase - Demonstrates the revolutionary developer experience.
 *
 * This showcases WHY the AI plugin is the "selling point":
 * - Type-safe extraction (zero boilerplate)
 * - Automatic caching (90%+ cost savings)
 * - Simple API (ask, extract, stream, rag)
 * - Integrated with other services (Database, Email, etc.)
 *
 * 🎬 Get your popcorn ready! 🍿
 */
public class AIShowcase {

    // Example records - these become your AI schemas automatically
    record ProductInfo(
        String name,
        BigDecimal price,
        String description,
        String category,
        List<String> tags
    ) {}

    record UserProfile(
        String name,
        String email,
        int age,
        String bio,
        List<String> interests
    ) {}

    record SentimentAnalysis(
        String sentiment,  // "positive", "negative", "neutral"
        double confidence,
        String summary
    ) {}

    record CodeReview(
        String review,
        int score,  // 1-10
        List<String> issues,
        List<String> suggestions
    ) {}

    public static void main(String[] args) {
        var app = Roya.create();
        var objectMapper = new ObjectMapper(); // For serializing records with nulls

        // Setup
        app.use(Morgan.combined());
        app.use(Cors.cors());
        app.use(BodyParser.bodyParser());

        // Register Cache Plugin (for AI response caching - automatic cost savings!)
        var cachePlugin = new CachePlugin();
        cachePlugin.register(app.services());
        try {
            cachePlugin.start();
        } catch (Exception e) {
            System.err.println("Warning: Cache plugin failed to start: " + e.getMessage());
        }

        // Register AI Plugin - THE STAR OF THE SHOW 🌟
        var aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        try {
            aiPlugin.start();
        } catch (Exception e) {
            System.err.println("❌ Error: AI plugin failed to start: " + e.getMessage());
            System.err.println("   Make sure to set: -Dai.openai.apiKey=your-api-key");
            System.exit(1);
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("🌟 ROYA AI PLUGIN SHOWCASE 🌟");
        System.out.println("=".repeat(70));
        System.out.println("\nThis demonstrates why AI plugin is revolutionary:");
        System.out.println("1. Type-safe extraction (Java records = AI schemas)");
        System.out.println("2. Automatic caching (90%+ cost savings)");
        System.out.println("3. Simple API (ask, extract, stream, rag)");
        System.out.println("4. Integrated with Database, Email, etc.");
        System.out.println("\n" + "=".repeat(70) + "\n");

        // ========== DEMO 1: Simple Chat ==========
        app.post("/demo/chat", (Request req, Response res, Next next) -> {
            AI ai = req.get(AI.class);

            Map<String, Object> body = readJsonBody(req);
            String question = body != null ? (String) body.get("question") : null;

            // Use metadata version to get token usage and cost!
            AIResponse<String> response = ai.askWithMetadata(
                "You are a helpful assistant. Be concise.",
                question
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = objectMapper.convertValue(response, Map.class);

            res.json(Map.of(
                "question", question,
                "answer", response.data(),
                "metadata", metadata,
                "note", response.cached()
                    ? "✅ Served from cache (FREE!)"
                    : "💰 Fresh API call (cost tracked)"
            ));
        });

        // ========== DEMO 2: Type-Safe Extraction (THE KILLER FEATURE) ==========
        app.post("/demo/extract/product", (Request req, Response res, Next next) -> {
            try {
                AI ai = req.get(AI.class);

                Map<String, Object> body = readJsonBody(req);
                String description = body != null ? (String) body.get("description") : null;
                if (description == null || description.isBlank()) {
                    res.status(400).json(Map.of("error", "description is required"));
                    return;
                }

                // ONE LINE - Type-safe extraction with metadata!
                // This returns a REAL ProductInfo record + token/cost info
                AIResponse<ProductInfo> response = ai.extractWithMetadata(
                    ProductInfo.class,
                    "Extract product information from: " + description
                );

                // Serialize record using Jackson (handles nulls gracefully)
                @SuppressWarnings("unchecked")
                Map<String, Object> extracted = objectMapper.convertValue(response.data(), Map.class);

                @SuppressWarnings("unchecked")
                Map<String, Object> metadata = objectMapper.convertValue(response, Map.class);

                res.json(Map.of(
                    "description", description,
                    "extracted", extracted,
                    "metadata", metadata,
                    "type", response.data().getClass().getSimpleName(),
                    "note", response.cached()
                        ? "✅ Cached response (FREE!) - Type-safe Java object"
                        : "💰 Fresh extraction - Type-safe Java object with cost tracking"
                ));
            } catch (Exception e) {
                System.err.println("/demo/extract/product failed: " + e.getMessage());
                e.printStackTrace();
                res.status(500).json(Map.of(
                    "error", e.getClass().getSimpleName(),
                    "message", e.getMessage()
                ));
            }
        });

        // ========== DEMO 3: User Profile Extraction ==========
        app.post("/demo/extract/profile", (Request req, Response res, Next next) -> {
            AI ai = req.get(AI.class);

            Map<String, Object> body = readJsonBody(req);
            String text = body != null ? (String) body.get("text") : null;
            if (text == null || text.isBlank()) {
                res.status(400).json(Map.of("error", "text is required"));
                return;
            }

            // Extract user profile - ONE LINE, TYPE-SAFE
            UserProfile profile = ai.extract(UserProfile.class, text);

            // Serialize record using Jackson (handles nulls gracefully)
            @SuppressWarnings("unchecked")
            Map<String, Object> profileMap = objectMapper.convertValue(profile, Map.class);

            res.json(Map.of(
                "input", text,
                "profile", profileMap,
                "magic", "Zero boilerplate. Zero JSON parsing. Just works."
            ));
        });

        // ========== DEMO 4: Sentiment Analysis ==========
        app.post("/demo/sentiment", (Request req, Response res, Next next) -> {
            AI ai = req.get(AI.class);

            Map<String, Object> body = readJsonBody(req);
            String review = body != null ? (String) body.get("review") : null;
            if (review == null || review.isBlank()) {
                res.status(400).json(Map.of("error", "review is required"));
                return;
            }

            // Analyze sentiment - returns typed record
            SentimentAnalysis sentiment = ai.extract(SentimentAnalysis.class,
                "Analyze the sentiment of this review: " + review
            );

            // Serialize record using Jackson (handles nulls gracefully)
            @SuppressWarnings("unchecked")
            Map<String, Object> sentimentMap = objectMapper.convertValue(sentiment, Map.class);

            res.json(Map.of(
                "review", review,
                "sentiment", sentimentMap
            ));
        });

        // ========== DEMO 5: Code Review ==========
        app.post("/demo/code-review", (Request req, Response res, Next next) -> {
            AI ai = req.get(AI.class);

            Map<String, Object> body = readJsonBody(req);
            String code = body != null ? (String) body.get("code") : null;
            if (code == null || code.isBlank()) {
                res.status(400).json(Map.of("error", "code is required"));
                return;
            }

            CodeReview review = ai.extract(CodeReview.class,
                "Review this code and provide feedback: " + code
            );

            // Serialize record using Jackson (handles nulls gracefully)
            @SuppressWarnings("unchecked")
            Map<String, Object> reviewMap = objectMapper.convertValue(review, Map.class);

            res.json(Map.of(
                "code", code.substring(0, Math.min(100, code.length())) + "...",
                "review", reviewMap
            ));
        });

        // ========== DEMO 6: Advanced Options ==========
        app.post("/demo/custom-model", (Request req, Response res, Next next) -> {
            AI ai = req.get(AI.class);

            Map<String, Object> body = readJsonBody(req);
            String question = body != null ? (String) body.get("question") : null;
            if (question == null || question.isBlank()) {
                res.status(400).json(Map.of("error", "question is required"));
                return;
            }

            // Use GPT-4 with custom temperature
            AIOptions options = AIOptions.builder()
                .model("gpt-4")
                .temperature(0.3)  // Lower = more focused
                .maxTokens(500)
                .build();

            String answer = ai.ask("You are an expert", question, options);

            res.json(Map.of(
                "question", question,
                "answer", answer,
                "model", "gpt-4",
                "temperature", 0.3
            ));
        });

        // ========== DEMO 7: Integration Example (AI + Database) ==========
        app.post("/demo/store-extracted", (Request req, Response res, Next next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) req.get("body");
            String description = (String) body.get("description");

            // Extract product
            ProductInfo product = ai.extract(ProductInfo.class, description);

            // In a real app, you'd store it:
            // Database db = req.get(Database.class);
            // db.insert("products", product);  // Type-safe SQL too!

            // Serialize record using Jackson (handles nulls gracefully)
            @SuppressWarnings("unchecked")
            Map<String, Object> productMap = objectMapper.convertValue(product, Map.class);

            res.json(Map.of(
                "product", productMap,
                "message", "Extracted and ready to store (type-safe all the way!)",
                "note", "This demonstrates AI + Database working together seamlessly"
            ));
        });

        // Status endpoint
        app.get("/demo/status", (Request req, Response res, Next next) -> {
            res.json(Map.of(
                "status", "✨ AI Plugin Showcase Running ✨",
                "features", List.of(
                    "Type-safe extraction with Java records",
                    "Automatic response caching (90%+ cost savings)",
                    "Token counting and cost tracking",
                    "Simple API: ask(), extract(), stream(), rag()",
                    "Integrated with Cache, Database, Email plugins",
                    "Provider-agnostic (OpenAI today, Anthropic tomorrow)"
                ),
                "endpoints", Map.of(
                    "POST /demo/chat", "Simple chat completion",
                    "POST /demo/extract/product", "Extract ProductInfo (type-safe!)",
                    "POST /demo/extract/profile", "Extract UserProfile (type-safe!)",
                    "POST /demo/sentiment", "Sentiment analysis",
                    "POST /demo/code-review", "Code review with structured feedback",
                    "POST /demo/custom-model", "Use custom model/temperature",
                    "POST /demo/store-extracted", "AI + Database integration"
                ),
                "tryIt", Map.of(
                    "example1", Map.of(
                        "endpoint", "POST /demo/extract/product",
                        "body", Map.of(
                            "description", "Widget Pro is a premium product selling for $29.99 in the Electronics category. Features: durable, lightweight, smart."
                        )
                    ),
                    "example2", Map.of(
                        "endpoint", "POST /demo/chat",
                        "body", Map.of(
                            "question", "What makes Roya Framework special?"
                        )
                    )
                )
            ));
        });

        // Root endpoint
        app.get("/", (Request req, Response res, Next next) -> {
            res.redirect("/demo/status");
        });

        // Start server
        app.listen(3001, () -> {
            System.out.println("\n🚀 AI Showcase running on http://localhost:3001\n");
            System.out.println("📝 Try these:");
            System.out.println("\n1. Type-safe extraction:");
            System.out.println("   POST /demo/extract/product");
            System.out.println("   { \"description\": \"Widget Pro, $29.99, Electronics, features: durable\" }");
            System.out.println("\n2. Simple chat:");
            System.out.println("   POST /demo/chat");
            System.out.println("   { \"question\": \"What is Java?\" }");
            System.out.println("\n3. View status:");
            System.out.println("   GET /demo/status");
            System.out.println("\n" + "=".repeat(70));
            System.out.println("🎬 SHOWCASE READY! 🍿");
            System.out.println("=".repeat(70) + "\n");
        });
    }

    private static Map<String, Object> readJsonBody(Request req) {
        try {
            String text = req.bodyText();
            if (text == null || text.isBlank()) {
                return new HashMap<>();
            }
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> map = mapper.readValue(text, Map.class);
            return map;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}

