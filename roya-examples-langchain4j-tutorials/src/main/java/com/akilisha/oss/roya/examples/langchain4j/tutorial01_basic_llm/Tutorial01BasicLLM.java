package com.akilisha.oss.roya.examples.langchain4j.tutorial01_basic_llm;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;

import java.util.Map;

/**
 * Tutorial 01: Basic LLM Usage
 *
 * <p>This tutorial recreates LangChain4j's {@code _00_HelloWorld.java} tutorial
 * using Roya's workflow-first approach.
 *
 * <h3>LangChain4j Approach:</h3>
 * <pre>
 * ChatModel model = OpenAiChatModel.builder()
 *     .apiKey(ApiKeys.OPENAI_API_KEY)
 *     .modelName(GPT_4_O_MINI)
 *     .build();
 *
 * String answer = model.chat("Say Hello World");
 * System.out.println(answer);
 * </pre>
 *
 * <h3>Roya Approach:</h3>
 * <pre>
 * AI ai = req.get(AI.class);
 * String answer = ai.llm().ask("You are helpful", "Say Hello World");
 * </pre>
 *
 * <h3>Roya Advantages:</h3>
 * <ul>
 *   <li><b>Type-safe API</b>: Unified AI interface, no low-level model building</li>
 *   <li><b>Automatic caching</b>: Responses cached automatically (90%+ cost reduction)</li>
 *   <li><b>Cost tracking</b>: Token usage and costs tracked automatically</li>
 *   <li><b>Provider-agnostic</b>: Switch providers without code changes</li>
 *   <li><b>Express-compatible</b>: Works seamlessly in HTTP handlers</li>
 * </ul>
 *
 * <h3>Run:</h3>
 * <pre>
 * // Set environment variable
 * export OPENAI_API_KEY=your-api-key
 *
 * // Run via Gradle task
 * ./gradlew :roya-examples-langchain4j-tutorials:runTutorial01
 *
 * // Or via HTTP endpoint (after starting the server)
 * curl <a href="http://localhost:3001/hello">...</a>
 * </pre>
 */
public class Tutorial01BasicLLM {

    public static void main(String[] args) {
        var app = Roya.create();

        // Register AI plugin (reads OPENAI_API_KEY from environment)
        var aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        try {
            aiPlugin.start();
        } catch (Exception e) {
            System.err.println("❌ Error: AI plugin failed to start: " + e.getMessage());
            System.exit(1);
        }

        // Simple route demonstrating basic LLM usage
        app.get("/hello", (req, res, next) -> {
            AI ai = req.get(AI.class);

            // One-line LLM call - same as LangChain4j's model.chat()
            String answer = ai.llm().ask(
                    "You are a helpful assistant",  // System prompt
                    "Say Hello World"                 // User message
            );

            res.json(Map.of(
                    "question", "Say Hello World",
                    "answer", answer,
                    "note", "This uses Roya's unified AI API with automatic caching and cost tracking"
            ));
        });

        // Comparison endpoint showing metadata
        app.get("/compare", (req, res, next) -> {
            AI ai = req.get(AI.class);

            // Roya approach with options
            String royaAnswer = ai.llm().ask(
                    "You are helpful",
                    "Say Hello World",
                    com.akilisha.oss.roya.plugins.ai.AIOptions.builder()
                            .model("gpt-4o-mini")
                            .build()
            );

            res.json(Map.of(
                    "roya_answer", royaAnswer,
                    "note", "Token usage and cost tracking are available via LLMResponse metadata",
                    "see", "ai.llm().ask() returns LLMResponse with cached(), tokens(), cost() methods"
            ));
        });

        // Print startup message
        System.out.println("""
                ╔══════════════════════════════════════════════════════════╗
                ║  Tutorial 01: Basic LLM Usage (Roya)                      ║
                ║                                                          ║
                ║  Endpoints:                                             ║
                ║    GET /hello    - Basic LLM call                       ║
                ║    GET /compare   - Show comparison                      ║
                ║                                                          ║
                ║  Make sure OPENAI_API_KEY is set!                       ║
                ╚══════════════════════════════════════════════════════════╝
                """);

        app.listen(3001);
    }
}

