package com.akilisha.oss.roya.examples.langchain4j.tutorial08_ai_services;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;
import java.util.Map;

/**
 * Tutorial 08: AI Services (Declarative Interfaces)
 *
 * <p>Recreates LangChain4j's AI Services tutorial.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>Defining AI Service interfaces with annotations</li>
 *   <li>Automatic implementation by LangChain4j</li>
 *   <li>System and user message annotations</li>
 *   <li>Variable injection with @V</li>
 *   <li>Structured extraction via return types</li>
 *   <li>Streaming responses</li>
 * </ul>
 *
 * <p>LangChain4j Approach:
 * <pre>
 * interface Assistant {
 *     @SystemMessage("You are a helpful assistant")
 *     String chat(@UserMessage String userMessage);
 * }
 *
 * Assistant assistant = AiServices.builder(Assistant.class)
 *     .chatModel(chatModel)
 *     .build();
 *
 * String response = assistant.chat("Hello");
 * </pre>
 *
 * <p>Roya Approach:
 * <pre>
 * AI ai = req.get(AI.class);
 *
 * interface Assistant {
 *     @SystemMessage("You are a helpful assistant")
 *     String chat(@UserMessage String userMessage);
 * }
 *
 * Assistant assistant = ai.aiService(Assistant.class);
 * String response = assistant.chat("Hello");
 * </pre>
 *
 * <p><b>Key Points:</b>
 * <ul>
 *   <li>AI Services provide type-safe, declarative AI interfaces</li>
 *   <li>LangChain4j automatically implements the interface</li>
 *   <li>Annotations (@SystemMessage, @UserMessage) control prompt structure</li>
 *   <li>@V annotation injects variables into prompts</li>
 *   <li>Return types define extraction behavior (String, records, etc.)</li>
 *   <li>Works seamlessly with Roya's AI plugin</li>
 * </ul>
 *
 * <p>HTTP Endpoints:
 * <ul>
 *   <li>{@code POST /chat} - Simple chat using AI Service</li>
 *   <li>{@code POST /translate} - Translation with variable injection</li>
 *   <li>{@code POST /extract} - Structured extraction via AI Service</li>
 *   <li>{@code POST /sentiment} - Sentiment analysis with structured output</li>
 *   <li>{@code GET /} - Root endpoint with instructions</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * // Simple chat
 * curl -X POST http://localhost:3008/chat \
 *   -H "Content-Type: application/json" \
 *   -d '{"message": "Hello, tell me a joke"}'
 *
 * // Translation with variable injection
 * curl -X POST http://localhost:3008/translate \
 *   -H "Content-Type: application/json" \
 *   -d '{"text": "Hello", "targetLanguage": "Spanish"}'
 *
 * // Structured extraction
 * curl -X POST http://localhost:3008/extract \
 *   -H "Content-Type: application/json" \
 *   -d '{"text": "John Doe, age 30, email john@example.com"}'
 *
 * // Sentiment analysis
 * curl -X POST http://localhost:3008/sentiment \
 *   -H "Content-Type: application/json" \
 *   -d '{"text": "I love this framework!"}'
 * </pre>
 */
public class Tutorial08AIServices {

    // AI Service Interface: Simple Chat Assistant
    interface ChatAssistant {
        @SystemMessage("You are a helpful, friendly assistant. Be concise and clear.")
        String chat(@UserMessage String userMessage);
    }

    // AI Service Interface: Translator with Variable Injection
    interface Translator {
        @SystemMessage("You are a professional translator. Translate accurately and naturally.")
        String translate(
            @UserMessage("Translate the following {{text}} to {{targetLanguage}}:") 
            @V("text") String text,
            @V("targetLanguage") String targetLanguage
        );
    }

    // AI Service Interface: Person Information Extractor
    interface PersonExtractor {
        @SystemMessage("Extract person information from text and return structured data.")
        PersonInfo extractPerson(@UserMessage String text);
        
        record PersonInfo(String name, int age, String email, String company) {}
    }

    // AI Service Interface: Sentiment Analyzer
    interface SentimentAnalyzer {
        @SystemMessage("You are a sentiment analyzer. Classify text sentiment as Positive, Negative, or Neutral.")
        Sentiment analyzeSentiment(@UserMessage String text);
        
        record Sentiment(String sentiment, double confidence, String explanation) {}
    }

    // AI Service Interface: Recipe Generator
    interface RecipeGenerator {
        @SystemMessage("You are a professional chef. Generate detailed recipes.")
        Recipe generateRecipe(
            @UserMessage("Generate a recipe for {{dish}} that serves {{servings}} people.") 
            @V("dish") String dish,
            @V("servings") int servings
        );
        
        record Recipe(String dish, int servings, List<String> ingredients, List<String> instructions, int prepTimeMinutes, int cookTimeMinutes) {}
    }

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

        // Simple Chat using AI Service
        app.post("/chat", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, String> body = req.body(Map.class);
            String message = body != null && body.containsKey("message")
                ? body.get("message")
                : "Hello";

            // Create AI Service instance - LangChain4j automatically implements the interface
            ChatAssistant assistant = ai.aiService(ChatAssistant.class);
            String response = assistant.chat(message);

            res.json(Map.of(
                "message", message,
                "response", response,
                "note", "AI Service automatically handles prompt construction and LLM calls"
            ));
        });

        // Translation with Variable Injection
        app.post("/translate", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = req.body(Map.class);
            String text = body != null && body.containsKey("text")
                ? (String) body.get("text")
                : "Hello";
            String targetLanguage = body != null && body.containsKey("targetLanguage")
                ? (String) body.get("targetLanguage")
                : "Spanish";

            // Create Translator service - @V annotations inject variables into prompt
            Translator translator = ai.aiService(Translator.class);
            String translation = translator.translate(text, targetLanguage);

            res.json(Map.of(
                "original", text,
                "targetLanguage", targetLanguage,
                "translation", translation,
                "note", "@V annotations inject variables into the prompt template"
            ));
        });

        // Structured Extraction via AI Service
        app.post("/extract", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, String> body = req.body(Map.class);
            String text = body != null && body.containsKey("text")
                ? body.get("text")
                : "John Doe, age 30, email john@example.com, works at Acme Corp";

            // Create PersonExtractor service - return type defines extraction structure
            PersonExtractor extractor = ai.aiService(PersonExtractor.class);
            PersonExtractor.PersonInfo person = extractor.extractPerson(text);

            res.json(Map.of(
                "input", text,
                "extracted", Map.of(
                    "name", person.name(),
                    "age", person.age(),
                    "email", person.email(),
                    "company", person.company()
                ),
                "note", "Return type (record) automatically structures the extraction"
            ));
        });

        // Sentiment Analysis with Structured Output
        app.post("/sentiment", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, String> body = req.body(Map.class);
            String text = body != null && body.containsKey("text")
                ? body.get("text")
                : "I love this framework!";

            // Create SentimentAnalyzer service - returns structured sentiment data
            SentimentAnalyzer analyzer = ai.aiService(SentimentAnalyzer.class);
            SentimentAnalyzer.Sentiment sentiment = analyzer.analyzeSentiment(text);

            res.json(Map.of(
                "text", text,
                "sentiment", sentiment.sentiment(),
                "confidence", sentiment.confidence(),
                "explanation", sentiment.explanation(),
                "note", "AI Service returns structured data (record) automatically"
            ));
        });

        // Recipe Generation with Multiple Variables
        app.post("/recipe", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> body = req.body(Map.class);
            String dish = body != null && body.containsKey("dish")
                ? (String) body.get("dish")
                : "chocolate chip cookies";
            int servings = body != null && body.containsKey("servings")
                ? ((Number) body.get("servings")).intValue()
                : 12;

            // Create RecipeGenerator service - multiple @V variables
            RecipeGenerator generator = ai.aiService(RecipeGenerator.class);
            RecipeGenerator.Recipe recipe = generator.generateRecipe(dish, servings);

            res.json(Map.of(
                "recipe", Map.of(
                    "dish", recipe.dish(),
                    "servings", recipe.servings(),
                    "ingredients", recipe.ingredients(),
                    "instructions", recipe.instructions(),
                    "prepTimeMinutes", recipe.prepTimeMinutes(),
                    "cookTimeMinutes", recipe.cookTimeMinutes()
                ),
                "note", "AI Service handles complex structured extraction with multiple variables"
            ));
        });

        // Root endpoint with instructions
        app.get("/", (req, res, next) -> {
            res.json(Map.of(
                "tutorial", "Tutorial 08: AI Services (Declarative Interfaces)",
                "description", "Demonstrates LangChain4j's AI Services pattern - declarative interfaces with automatic implementation",
                "endpoints", Map.of(
                    "POST /chat", "Simple chat using AI Service",
                    "POST /translate", "Translation with variable injection (@V)",
                    "POST /extract", "Structured extraction via return type",
                    "POST /sentiment", "Sentiment analysis with structured output",
                    "POST /recipe", "Recipe generation with multiple variables"
                ),
                "keyFeatures", List.of(
                    "@SystemMessage - Defines system prompt",
                    "@UserMessage - Defines user message template",
                    "@V - Injects variables into prompt templates",
                    "Return types - Automatically structures extraction (records)",
                    "ai.aiService(ServiceClass.class) - Creates service instance"
                ),
                "examples", List.of(
                    "curl -X POST http://localhost:3008/chat -H 'Content-Type: application/json' -d '{\"message\":\"Hello\"}'",
                    "curl -X POST http://localhost:3008/translate -H 'Content-Type: application/json' -d '{\"text\":\"Hello\",\"targetLanguage\":\"Spanish\"}'",
                    "curl -X POST http://localhost:3008/extract -H 'Content-Type: application/json' -d '{\"text\":\"John Doe, age 30, email john@example.com\"}'",
                    "curl -X POST http://localhost:3008/sentiment -H 'Content-Type: application/json' -d '{\"text\":\"I love this!\"}'",
                    "curl -X POST http://localhost:3008/recipe -H 'Content-Type: application/json' -d '{\"dish\":\"chocolate cake\",\"servings\":8}'"
                )
            ));
        });

        int port = 3008;
        System.out.println("🚀 Tutorial 08: AI Services (Declarative Interfaces)");
        System.out.println("📡 Server running on http://localhost:" + port);
        System.out.println("📚 Endpoints:");
        System.out.println("   POST /chat - Simple chat using AI Service");
        System.out.println("   POST /translate - Translation with @V variable injection");
        System.out.println("   POST /extract - Structured extraction via return type");
        System.out.println("   POST /sentiment - Sentiment analysis with structured output");
        System.out.println("   POST /recipe - Recipe generation with multiple variables");
        System.out.println("   GET  / - API documentation");
        System.out.println();
        System.out.println("💡 Key Concept: Define interfaces with annotations, LangChain4j implements them automatically!");

        app.listen(port);
    }
}

