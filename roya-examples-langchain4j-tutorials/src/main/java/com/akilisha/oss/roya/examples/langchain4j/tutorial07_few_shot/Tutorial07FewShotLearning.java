package com.akilisha.oss.roya.examples.langchain4j.tutorial07_few_shot;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIOptions;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;

import java.util.List;
import java.util.Map;

/**
 * Tutorial 07: Few-Shot Learning
 *
 * <p>Recreates LangChain4j's few-shot learning tutorial.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>Providing examples to guide LLM behavior</li>
 *   <li>Structured output formatting via examples</li>
 *   <li>Task-specific instruction following</li>
 *   <li>Pattern matching and consistency</li>
 * </ul>
 *
 * <p>LangChain4j Approach:
 * <pre>
 * String examples = """
 *     Example 1:
 *     Input: "I love this product!"
 *     Output: Positive
 *
 *     Example 2:
 *     Input: "This is terrible."
 *     Output: Negative
 *     """;
 *
 * String systemPrompt = "You are a sentiment analyzer. " + examples;
 * String response = model.generate(systemPrompt, userInput);
 * </pre>
 *
 * <p>Roya Approach:
 * <pre>
 * AI ai = req.get(AI.class);
 *
 * String examples = buildExamples();
 * String systemPrompt = "You are a sentiment analyzer.\n\n" + examples;
 *
 * String response = ai.llm().ask(
 *     systemPrompt,
 *     userInput,
 *     AIOptions.defaults()
 * );
 * </pre>
 *
 * <p><b>Key Points:</b>
 * <ul>
 *   <li>Few-shot learning improves accuracy by providing examples</li>
 *   <li>Examples demonstrate the desired output format</li>
 *   <li>Works with both structured extraction and free-form responses</li>
 *   <li>Few-shot examples are included in the system prompt</li>
 * </ul>
 *
 * <p>HTTP Endpoints:
 * <ul>
 *   <li>{@code POST /sentiment} - Analyze sentiment using few-shot examples</li>
 *   <li>{@code POST /translate} - Translate text using few-shot examples</li>
 *   <li>{@code POST /classify} - Classify text using few-shot examples</li>
 *   <li>{@code GET /} - Root endpoint with instructions</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * // Sentiment analysis with few-shot examples
 * curl -X POST <a href="http://localhost:3007/sentiment">...</a> \
 *   -H "Content-Type: application/json" \
 *   -d '{"text": "I absolutely love this framework!"}'
 *
 * // Translation with few-shot examples
 * curl -X POST <a href="http://localhost:3007/translate">...</a> \
 *   -H "Content-Type: application/json" \
 *   -d '{"text": "Hello, how are you?", "targetLanguage": "Spanish"}'
 *
 * // Classification with few-shot examples
 * curl -X POST <a href="http://localhost:3007/classify">...</a> \
 *   -H "Content-Type: application/json" \
 *   -d '{"text": "I need help with my order"}'
 * </pre>
 */
public class Tutorial07FewShotLearning {

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

        // Sentiment Analysis with Few-Shot Examples
        app.post("/sentiment", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, String> body = req.body(Map.class);
            String text = body != null && body.containsKey("text")
                ? body.get("text")
                : "This is great!";

            // Few-shot examples for sentiment analysis
            String examples = """
                Example 1:
                Input: "I love this product!"
                Output: Positive

                Example 2:
                Input: "This is terrible."
                Output: Negative

                Example 3:
                Input: "It's okay, nothing special."
                Output: Neutral
                """;

            String systemPrompt = "You are a sentiment analyzer. Classify the sentiment of the input text as Positive, Negative, or Neutral.\n\n" + examples;

            String sentiment = ai.llm().ask(
                systemPrompt,
                "Input: \"" + text + "\"\nOutput:",
                AIOptions.builder().model("gpt-4o-mini").build()
            );

            res.json(Map.of(
                "text", text,
                "sentiment", sentiment.trim(),
                "note", "Few-shot learning with 3 examples guides the model's classification"
            ));
        });

        // Translation with Few-Shot Examples
        app.post("/translate", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, String> body = req.body(Map.class);
            String text = body != null && body.containsKey("text")
                ? body.get("text")
                : "Hello";
            String targetLanguage = body != null && body.containsKey("targetLanguage")
                ? body.get("targetLanguage")
                : "Spanish";

            // Few-shot examples for translation
            String examples = """
                Example 1:
                English: "Hello, how are you?"
                Spanish: "Hola, ¿cómo estás?"

                Example 2:
                English: "Good morning"
                Spanish: "Buenos días"

                Example 3:
                English: "Thank you very much"
                Spanish: "Muchas gracias"
                """;

            String systemPrompt = "You are a translator. Translate the English text to " + targetLanguage + ".\n\n" + examples;

            String translation = ai.llm().ask(
                systemPrompt,
                "English: \"" + text + "\"\n" + targetLanguage + ":",
                AIOptions.builder().model("gpt-4o-mini").build()
            );

            res.json(Map.of(
                "original", text,
                "targetLanguage", targetLanguage,
                "translation", translation.trim(),
                "note", "Few-shot examples demonstrate the translation pattern"
            ));
        });

        // Classification with Few-Shot Examples
        app.post("/classify", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, String> body = req.body(Map.class);
            String text = body != null && body.containsKey("text")
                ? body.get("text")
                : "I need help";

            // Few-shot examples for customer support classification
            String examples = """
                Example 1:
                Text: "I can't login to my account"
                Category: Technical Support

                Example 2:
                Text: "I want to cancel my subscription"
                Category: Billing

                Example 3:
                Text: "Where is my order?"
                Category: Shipping

                Example 4:
                Text: "How do I use this feature?"
                Category: General Inquiry
                """;

            String systemPrompt = "You are a customer support classifier. Categorize the customer message into one of: Technical Support, Billing, Shipping, or General Inquiry.\n\n" + examples;

            String category = ai.llm().ask(
                systemPrompt,
                "Text: \"" + text + "\"\nCategory:",
                AIOptions.builder().model("gpt-4o-mini").build()
            );

            res.json(Map.of(
                "text", text,
                "category", category.trim(),
                "note", "Few-shot learning with 4 examples teaches the classification pattern"
            ));
        });

        // Structured Extraction with Few-Shot Examples
        app.post("/extract", (req, res, next) -> {
            AI ai = req.get(AI.class);

            @SuppressWarnings("unchecked")
            Map<String, String> body = req.body(Map.class);
            String text = body != null && body.containsKey("text")
                ? body.get("text")
                : "John Doe, age 30, email john@example.com, works at Acme Corp";

            // Record for structured extraction
            record PersonInfo(String name, int age, String email, String company) {}

            // Few-shot examples for structured extraction
            String examples = """
                Example 1:
                Text: "Alice Smith, 25 years old, alice@email.com, works at Tech Inc"
                Output: {"name": "Alice Smith", "age": 25, "email": "alice@email.com", "company": "Tech Inc"}

                Example 2:
                Text: "Bob Johnson, age 35, bob@company.com, employed by StartupXYZ"
                Output: {"name": "Bob Johnson", "age": 35, "email": "bob@company.com", "company": "StartupXYZ"}
                """;

            String systemPrompt = "You are a data extractor. Extract person information from text and return JSON.\n\n" + examples + "\n\nAlways return valid JSON matching the example format.";

            // Use extraction with few-shot examples in system prompt
            PersonInfo person = ai.llm().extract(
                PersonInfo.class,
                systemPrompt + "\n\nText: " + text,
                AIOptions.builder().model("gpt-4o-mini").build()
            );

            res.json(Map.of(
                "input", text,
                "extracted", Map.of(
                    "name", person.name(),
                    "age", person.age(),
                    "email", person.email(),
                    "company", person.company()
                ),
                "note", "Few-shot examples guide structured extraction format"
            ));
        });

        // Root endpoint with instructions
        app.get("/", (req, res, next) -> {
            res.json(Map.of(
                "tutorial", "Tutorial 07: Few-Shot Learning",
                "description", "Demonstrates providing examples to guide LLM behavior",
                "endpoints", Map.of(
                    "POST /sentiment", "Analyze sentiment with few-shot examples",
                    "POST /translate", "Translate text with few-shot examples",
                    "POST /classify", "Classify text with few-shot examples",
                    "POST /extract", "Structured extraction with few-shot examples"
                ),
                "examples", List.of(
                    "curl -X POST http://localhost:3007/sentiment -H 'Content-Type: application/json' -d '{\"text\":\"I love this!\"}'",
                    "curl -X POST http://localhost:3007/translate -H 'Content-Type: application/json' -d '{\"text\":\"Hello\",\"targetLanguage\":\"Spanish\"}'",
                    "curl -X POST http://localhost:3007/classify -H 'Content-Type: application/json' -d '{\"text\":\"I need help\"}'",
                    "curl -X POST http://localhost:3007/extract -H 'Content-Type: application/json' -d '{\"text\":\"John Doe, age 30, email john@example.com, works at Acme\"}'"
                )
            ));
        });

        int port = 3007;
        System.out.println("🚀 Tutorial 07: Few-Shot Learning");
        System.out.println("📡 Server running on http://localhost:" + port);
        System.out.println("📚 Endpoints:");
        System.out.println("   POST /sentiment - Sentiment analysis with examples");
        System.out.println("   POST /translate - Translation with examples");
        System.out.println("   POST /classify - Classification with examples");
        System.out.println("   POST /extract - Structured extraction with examples");
        System.out.println("   GET  / - API documentation");
        System.out.println();

        app.listen(port);
    }
}

