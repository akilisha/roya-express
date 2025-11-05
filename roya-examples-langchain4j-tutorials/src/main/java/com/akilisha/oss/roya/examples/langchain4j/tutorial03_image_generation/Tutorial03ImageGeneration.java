package com.akilisha.oss.roya.examples.langchain4j.tutorial03_image_generation;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.ai.AIOptions;

import java.util.Map;

/**
 * Tutorial 03: Image Generation
 *
 * <p>This tutorial recreates LangChain4j's {@code _02_OpenAiImageModelExamples.java} tutorial
 * using Roya's unified Vision API.
 *
 * <h3>LangChain4j Approach:</h3>
 * <pre>
 * ImageModel model = OpenAiImageModel.builder()
 *     .apiKey(ApiKeys.OPENAI_API_KEY)
 *     .modelName(DALL_E_3)
 *     .build();
 *
 * Response&lt;Image&gt; response = model.generate(
 *     "Swiss software developers with cheese fondue, a parrot and a cup of coffee"
 * );
 * System.out.println(response.content().url());
 * </pre>
 *
 * <h3>Roya Approach:</h3>
 * <pre>
 * AI ai = req.get(AI.class);
 * String imageUrl = ai.vision().generateImage(
 *     "Swiss software developers with cheese fondue, a parrot and a cup of coffee"
 * );
 * </pre>
 *
 * <h3>Roya Advantages:</h3>
 * <ul>
 *   <li><b>Unified API</b>: Same Vision API for all multimodal operations</li>
 *   <li><b>Simple interface</b>: No need to build ImageModel instances</li>
 *   <li><b>Provider-agnostic</b>: Switch providers without code changes</li>
 *   <li><b>Consistent with other vision ops</b>: generateImage() alongside analyzeImage()</li>
 * </ul>
 *
 * <h3>Run:</h3>
 * <pre>
 * // Set environment variable
 * export OPENAI_API_KEY=your-api-key
 *
 * // Run via Gradle task
 * ./gradlew :roya-examples-langchain4j-tutorials:runTutorial03
 *
 * // Or via HTTP endpoints
 * curl <a href="http://localhost:3003/generate">...</a>
 * curl <a href="http://localhost:3003/generate?prompt=Your%20prompt%20here">...</a>
 * curl <a href="http://localhost:3003/generate-dalle2?prompt=Your%20prompt%20here">...</a>
 * </pre>
 */
public class Tutorial03ImageGeneration {

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

        // Generate image endpoint (DALL-E 3, default)
        app.get("/generate", (req, res, next) -> {
            String prompt = req.query().get("prompt").orElse(
                    "Swiss software developers with cheese fondue, a parrot and a cup of coffee"
            );

            try {
                AI ai = req.get(AI.class);
                String imageUrl = ai.vision().generateImage(prompt);

                res.json(Map.of(
                        "prompt", prompt,
                        "imageUrl", imageUrl,
                        "model", "dall-e-3",
                        "note", "Generated using DALL-E 3 (default)"
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                        "error", "Failed to generate image",
                        "message", e.getMessage(),
                        "note", "Make sure OPENAI_API_KEY is set and DALL-E models are available"
                ));
            }
        });

        // Generate image with DALL-E 2 (supports multiple images)
        app.get("/generate-dalle2", (req, res, next) -> {
            String prompt = req.query().get("prompt").orElse(
                    "A futuristic cityscape at sunset"
            );

            try {
                AI ai = req.get(AI.class);
                // Use DALL-E 2 via model option
                String imageUrl = ai.vision().generateImage(
                        prompt,
                        AIOptions.builder()
                                .model("dall-e-2")
                                .additionalOptions(Map.of("n", 1)) // Number of images
                                .build()
                );

                res.json(Map.of(
                        "prompt", prompt,
                        "imageUrl", imageUrl,
                        "model", "dall-e-2",
                        "note", "Generated using DALL-E 2"
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                        "error", "Failed to generate image",
                        "message", e.getMessage()
                ));
            }
        });

        // Generate image with custom options
        app.get("/generate-advanced", (req, res, next) -> {
            String prompt = req.query().get("prompt").orElse(
                    "A beautiful landscape painting"
            );

            try {
                AI ai = req.get(AI.class);
                String imageUrl = ai.vision().generateImage(
                        prompt,
                        AIOptions.builder()
                                .model("dall-e-3")
                                .additionalOptions(Map.of(
                                        "size", "1024x1024",    // Image size
                                        "quality", "standard"   // Quality: standard or hd
                                ))
                                .build()
                );

                res.json(Map.of(
                        "prompt", prompt,
                        "imageUrl", imageUrl,
                        "model", "dall-e-3",
                        "size", "1024x1024",
                        "quality", "standard",
                        "note", "Generated with custom size and quality options"
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                        "error", "Failed to generate image",
                        "message", e.getMessage()
                ));
            }
        });

        // Print startup message
        System.out.println("""
                ╔══════════════════════════════════════════════════════════╗
                ║  Tutorial 03: Image Generation (Roya)                  ║
                ║                                                          ║
                ║  Endpoints:                                             ║
                ║    GET /generate           - DALL-E 3 (default)       ║
                ║    GET /generate-dalle2     - DALL-E 2                 ║
                ║    GET /generate-advanced   - Custom options           ║
                ║                                                          ║
                ║  Query params:                                          ║
                ║    ?prompt=Your prompt here                             ║
                ║                                                          ║
                ║  Make sure OPENAI_API_KEY is set!                       ║
                ╚══════════════════════════════════════════════════════════╝
                """);

        app.listen(3003);
    }
}
