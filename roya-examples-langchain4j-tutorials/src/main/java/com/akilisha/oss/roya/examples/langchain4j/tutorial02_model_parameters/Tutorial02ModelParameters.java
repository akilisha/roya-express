package com.akilisha.oss.roya.examples.langchain4j.tutorial02_model_parameters;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.ai.AIOptions;

import java.util.Map;

/**
 * Tutorial 02: Model Parameters
 *
 * <p>This tutorial recreates LangChain4j's {@code _01_ModelParameters.java} tutorial
 * using Roya's unified AI interface.
 *
 * <h3>LangChain4j Approach:</h3>
 * <pre>
 * ChatModel model = OpenAiChatModel.builder()
 *     .apiKey(ApiKeys.OPENAI_API_KEY)
 *     .modelName(GPT_4_O_MINI)
 *     .temperature(0.3)
 *     .timeout(ofSeconds(60))
 *     .logRequests(true)
 *     .logResponses(true)
 *     .build();
 *
 * String response = model.chat("Explain in three lines how to make a beautiful painting");
 * </pre>
 *
 * <h3>Roya Approach:</h3>
 * <pre>
 * AI ai = req.get(AI.class);
 * String response = ai.llm().ask(
 *     "You are helpful",
 *     "Explain in three lines how to make a beautiful painting",
 *     AIOptions.builder()
 *         .model("gpt-4o-mini")
 *         .temperature(0.3)
 *         .timeout(Duration.ofSeconds(60))
 *         .build()
 * );
 * </pre>
 *
 * <h3>Roya Advantages:</h3>
 * <ul>
 *   <li><b>Unified API</b>: Same options API across all providers</li>
 *   <li><b>Per-request configuration</b>: Options can be set per request, not just at model creation</li>
 *   <li><b>Type-safe</b>: All parameters are strongly typed</li>
 *   <li><b>Provider-agnostic</b>: Switch providers without changing code</li>
 * </ul>
 *
 * <h3>Run:</h3>
 * <pre>
 * // Set environment variable
 * export OPENAI_API_KEY=your-api-key
 *
 * // Run via Gradle task
 * ./gradlew :roya-examples-langchain4j-tutorials:runTutorial02
 *
 * // Or via HTTP endpoints
 * curl <a href="http://localhost:3002/temperature/low">...</a>
 * curl <a href="http://localhost:3002/temperature/high">...</a>
 * curl <a href="http://localhost:3002/compare">...</a>
 * </pre>
 */
public class Tutorial02ModelParameters {

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

        // Low temperature (more deterministic, focused responses)
        app.get("/temperature/low", (req, res, next) -> {
            AI ai = req.get(AI.class);
            String prompt = "Explain in three lines how to make a beautiful painting";

            String response = ai.llm().ask(
                    "You are a helpful assistant",
                    prompt,
                    AIOptions.builder()
                            .model("gpt-4o-mini")
                            .temperature(0.3)  // Low = more deterministic
                            .build()
            );

            res.json(Map.of(
                    "temperature", 0.3,
                    "prompt", prompt,
                    "response", response,
                    "note", "Low temperature produces more focused, deterministic responses"
            ));
        });

        // High temperature (more creative, diverse responses)
        app.get("/temperature/high", (req, res, next) -> {
            AI ai = req.get(AI.class);
            String prompt = "Explain in three lines how to make a beautiful painting";

            String response = ai.llm().ask(
                    "You are a helpful assistant",
                    prompt,
                    AIOptions.builder()
                            .model("gpt-4o-mini")
                            .temperature(0.9)  // High = more creative
                            .build()
            );

            res.json(Map.of(
                    "temperature", 0.9,
                    "prompt", prompt,
                    "response", response,
                    "note", "High temperature produces more creative, diverse responses"
            ));
        });

        // All parameters example (matching LangChain4j tutorial)
        app.get("/full", (req, res, next) -> {
            AI ai = req.get(AI.class);
            String prompt = "Explain in three lines how to make a beautiful painting";

            String response = ai.llm().ask(
                    "You are a helpful assistant",
                    prompt,
                    AIOptions.builder()
                            .model("gpt-4o-mini")
                            .temperature(0.3)
                            // Note: Timeout and logging are handled at the adapter level in Roya
                            // Timeout can be configured via system properties or adapter-level settings
                            // Logging can be enabled via system properties (e.g., ai.logging.enabled=true)
                            .build()
            );

            res.json(Map.of(
                    "model", "gpt-4o-mini",
                    "temperature", 0.3,
                    "prompt", prompt,
                    "response", response,
                    "note", "This matches the LangChain4j tutorial configuration. " +
                            "Timeout and logging are configured at adapter level in Roya."
            ));
        });

        // Compare different temperature settings
        app.get("/compare", (req, res, next) -> {
            AI ai = req.get(AI.class);
            String prompt = "Write a haiku about programming";

            // Low temperature
            String lowTemp = ai.llm().ask(
                    "You are a helpful assistant",
                    prompt,
                    AIOptions.builder().temperature(0.1).build()
            );

            // Medium temperature
            String mediumTemp = ai.llm().ask(
                    "You are a helpful assistant",
                    prompt,
                    AIOptions.builder().temperature(0.5).build()
            );

            // High temperature
            String highTemp = ai.llm().ask(
                    "You are a helpful assistant",
                    prompt,
                    AIOptions.builder().temperature(0.9).build()
            );

            res.json(Map.of(
                    "prompt", prompt,
                    "temperature_0.1", lowTemp,
                    "temperature_0.5", mediumTemp,
                    "temperature_0.9", highTemp,
                    "note", "Compare how temperature affects creativity and diversity"
            ));
        });

        // Print startup message
        System.out.println("""
                ╔══════════════════════════════════════════════════════════╗
                ║  Tutorial 02: Model Parameters (Roya)                     ║
                ║                                                          ║
                ║  Endpoints:                                             ║
                ║    GET /temperature/low   - Low temp (0.3)              ║
                ║    GET /temperature/high   - High temp (0.9)            ║
                ║    GET /full              - Full config (matches LC4j)  ║
                ║    GET /compare           - Compare temps side-by-side   ║
                ║                                                          ║
                ║  Make sure OPENAI_API_KEY is set!                       ║
                ╚══════════════════════════════════════════════════════════╝
                """);

        app.listen(3002);
    }
}

