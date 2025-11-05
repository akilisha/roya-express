package com.akilisha.oss.roya.examples.langchain4j.tutorial04_prompt_templates;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIOptions;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.ai.Prompts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tutorial 04: Prompt Templates
 *
 * <p>Recreates LangChain4j's {@code _03_PromptTemplate.java} tutorial.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>Simple prompt templates with variable substitution</li>
 *   <li>Structured prompts using records</li>
 * </ul>
 *
 * <p>LangChain4j Approach:
 * <pre>
 * PromptTemplate promptTemplate = PromptTemplate.from("Create a recipe for a {{dishType}}...");
 * Prompt prompt = promptTemplate.apply(variables);
 * </pre>
 *
 * <p>Roya Approach (uses LangChain4j primitives directly):
 * <pre>
 * PromptTemplate promptTemplate = Prompts.template("Create a recipe for a {{dishType}}...");
 * Prompt prompt = promptTemplate.apply(variables);
 * </pre>
 *
 * <p><b>Key Points:</b>
 * <ul>
 *   <li>Roya exposes LangChain4j's {@code Prompt} and {@code PromptTemplate} as first-class types</li>
 *   <li>Use {@code Prompts.template()} to create templates</li>
 *   <li>Use {@code Prompts.from()} to create prompts from strings</li>
 *   <li>AI methods accept {@code Prompt} objects directly (type-safe!)</li>
 * </ul>
 *
 * <p>HTTP Endpoints:
 * <ul>
 *   <li>{@code GET /simple?dishType=oven dish&ingredients=potato,tomato,feta,olive oil}</li>
 *   <li>{@code GET /structured?dish=salad&ingredients=cucumber,tomato,feta,onion,olives}</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * // Simple template
 * curl "<a href="http://localhost:3004/simple?dishType=oven">...</a> dish&ingredients=potato,tomato,feta,olive oil"
 *
 * // Structured template
 * curl "<a href="http://localhost:3004/structured?dish=salad&ingredients=cucumber,tomato,feta,onion,olives">...</a>"
 * </pre>
 */
public class Tutorial04PromptTemplates {


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

        // Simple Prompt Template Example
        app.get("/simple", (req, res, next) -> {
            AI ai = req.get(AI.class);

            // Get query parameters
            String dishType = req.query().get("dishType").orElse("oven dish");
            String ingredientsStr = req.query().get("ingredients").orElse("potato, tomato, feta, olive oil");

            // Create prompt template with {{variable}} placeholders
            var template = Prompts.template(
                "Create a recipe for a {{dishType}} with the following ingredients: {{ingredients}}"
            );

            // Variables map
            Map<String, Object> variables = new HashMap<>();
            variables.put("dishType", dishType);
            variables.put("ingredients", ingredientsStr);

            // Apply template to create a Prompt
            var prompt = template.apply(variables);

            // Generate response using Prompt objects (type-safe!)
            String response = ai.ask(
                Prompts.from("You are a helpful cooking assistant."),
                prompt,
                AIOptions.builder().model("gpt-4o-mini").build()
            );

            res.json(Map.of(
                "template", template.template(),
                "variables", variables,
                "prompt", prompt.text(),
                "response", response
            ));
        });

        // Structured Prompt Template Example
        app.get("/structured", (req, res, next) -> {
            AI ai = req.get(AI.class);

            // Get query parameters
            String dish = req.query().get("dish").orElse("salad");
            String ingredientsStr = req.query().get("ingredients").orElse("cucumber, tomato, feta, onion, olives");

            // Parse ingredients list
            List<String> ingredients = List.of(ingredientsStr.split(",\\s*"));

            // Create structured prompt template using PromptTemplate
            var structuredTemplate = Prompts.template("""
                Create a recipe of a {{dish}} that can be prepared using only {{ingredients}}.
                Structure your answer in the following way:
                Recipe name: ...
                Description: ...
                Preparation time: ...
                Required ingredients:
                - ...
                - ...
                Instructions:
                - ...
                - ...
                """);

            // Apply variables (ingredients list converted to comma-separated string)
            String ingredientsList = String.join(", ", ingredients);
            Map<String, Object> variables = Map.of("dish", dish, "ingredients", ingredientsList);
            var prompt = structuredTemplate.apply(variables);

            // Generate response using Prompt objects
            String recipe = ai.ask(
                Prompts.from("You are a helpful cooking assistant."),
                prompt,
                AIOptions.builder().model("gpt-4o-mini").build()
            );

            res.json(Map.of(
                "dish", dish,
                "ingredients", ingredients,
                "prompt", prompt.text(),
                "recipe", recipe
            ));
        });

        // Root endpoint with instructions
        app.get("/", (req, res, next) -> {
            res.json(Map.of(
                "tutorial", "Tutorial 04: Prompt Templates",
                "endpoints", Map.of(
                    "/simple", "Simple template with {{variable}} syntax. Query params: dishType, ingredients",
                    "/structured", "Structured prompt using record. Query params: dish, ingredients"
                ),
                "examples", Map.of(
                    "simple", "/simple?dishType=oven dish&ingredients=potato,tomato,feta,olive oil",
                    "structured", "/structured?dish=salad&ingredients=cucumber,tomato,feta,onion,olives"
                )
            ));
        });

        System.out.println("==========================================");
        System.out.println("Tutorial 04: Prompt Templates");
        System.out.println("==========================================");
        System.out.println("Server starting on http://localhost:3004");
        System.out.println();
        System.out.println("Endpoints:");
        System.out.println("  GET /simple?dishType=...&ingredients=...");
        System.out.println("  GET /structured?dish=...&ingredients=...");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  curl \"http://localhost:3004/simple?dishType=oven dish&ingredients=potato,tomato,feta,olive oil\"");
        System.out.println("  curl \"http://localhost:3004/structured?dish=salad&ingredients=cucumber,tomato,feta,onion,olives\"");
        System.out.println("==========================================");

        app.listen(3004);
    }
}

