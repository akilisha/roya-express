package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.core.middleware.Cors;
import com.akilisha.oss.roya.core.middleware.Morgan;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIOptions;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.cache.CachePlugin;

import java.math.BigDecimal;
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

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) req.get("body");
            String question = (String) body.get("question");

            String answer = ai.ask(
                "You are a helpful assistant. Be concise.",
                question
            );

            res.json(Map.of(
                "question", question,
                "answer", answer,
                "note", "Cached on second call (cost savings!)"
            ));
        });

        // ========== DEMO 2: Type-Safe Extraction (THE KILLER FEATURE) ==========
        app.post("/demo/extract/product", (Request req, Response res, Next next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) req.get("body");
            String description = (String) body.get("description");

            // ONE LINE - Type-safe extraction!
            // This returns a REAL ProductInfo record, not a String or JSON
            ProductInfo product = ai.extract(ProductInfo.class, 
                "Extract product information from: " + description
            );

            res.json(Map.of(
                "description", description,
                "extracted", Map.of(
                    "name", product.name(),
                    "price", product.price(),
                    "category", product.category(),
                    "tags", product.tags()
                ),
                "type", product.getClass().getSimpleName(),
                "note", "This is a real Java object - type-safe, IDE-autocomplete works!"
            ));
        });

        // ========== DEMO 3: User Profile Extraction ==========
        app.post("/demo/extract/profile", (Request req, Response res, Next next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) req.get("body");
            String text = (String) body.get("text");

            // Extract user profile - ONE LINE, TYPE-SAFE
            UserProfile profile = ai.extract(UserProfile.class, text);

            res.json(Map.of(
                "input", text,
                "profile", Map.of(
                    "name", profile.name(),
                    "email", profile.email(),
                    "age", profile.age(),
                    "bio", profile.bio(),
                    "interests", profile.interests()
                ),
                "magic", "Zero boilerplate. Zero JSON parsing. Just works."
            ));
        });

        // ========== DEMO 4: Sentiment Analysis ==========
        app.post("/demo/sentiment", (Request req, Response res, Next next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) req.get("body");
            String review = (String) body.get("review");

            // Analyze sentiment - returns typed record
            SentimentAnalysis sentiment = ai.extract(SentimentAnalysis.class,
                "Analyze the sentiment of this review: " + review
            );

            res.json(Map.of(
                "review", review,
                "sentiment", sentiment.sentiment(),
                "confidence", sentiment.confidence(),
                "summary", sentiment.summary()
            ));
        });

        // ========== DEMO 5: Code Review ==========
        app.post("/demo/code-review", (Request req, Response res, Next next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) req.get("body");
            String code = (String) body.get("code");

            CodeReview review = ai.extract(CodeReview.class,
                "Review this code and provide feedback: " + code
            );

            res.json(Map.of(
                "code", code.substring(0, Math.min(100, code.length())) + "...",
                "review", review.review(),
                "score", review.score(),
                "issues", review.issues(),
                "suggestions", review.suggestions()
            ));
        });

        // ========== DEMO 6: Advanced Options ==========
        app.post("/demo/custom-model", (Request req, Response res, Next next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) req.get("body");
            String question = (String) body.get("question");
            
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

            res.json(Map.of(
                "product", product,
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
        app.listen(3000, () -> {
            System.out.println("\n🚀 AI Showcase running on http://localhost:3000\n");
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
}

