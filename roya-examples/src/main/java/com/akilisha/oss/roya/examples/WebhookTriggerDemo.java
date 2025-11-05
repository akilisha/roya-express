package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.WebhookTrigger;
import com.akilisha.oss.roya.plugins.ai.webhooks.WebhookRegistry;
import com.akilisha.oss.roya.workflow.core.Workflow;

import java.util.Map;

/**
 * Demonstration of WebhookTrigger functionality.
 * <p>
 * This example shows how to:
 * 1. Create a workflow with a WebhookTrigger
 * 2. Register webhook routes with the application
 * 3. Test webhook endpoints via HTTP requests
 * <p>
 * To test:
 * 1. Start the server: java WebhookTriggerDemo
 * 2. Send POST request: curl -X POST <a href="http://localhost:3000/api/webhook">...</a> -H "Content-Type: application/json" -d '{"message":"Hello"}'
 */
public class WebhookTriggerDemo {

    public static void main(String[] args) {
        System.out.println("🚀 WebhookTrigger Demo");
        System.out.println("=======================\n");

        // Create Roya application
        Roya app = Roya.create();

        // Install AI plugin
        AIPlugin aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());

        try {
            aiPlugin.start();
        } catch (Exception e) {
            System.err.println("Failed to start AI plugin: " + e.getMessage());
        }

        // Get AI service
        AI ai = app.services().get(AI.class);

        // Build workflow with webhook trigger
        System.out.println("📋 Building workflow with WebhookTrigger...");
        Workflow workflow = ai.workflow("webhook-demo")
                .trigger("webhook", WebhookTrigger.builder()
                        .path("/api/webhook")
                        .method("POST")
                        .build())
                .llm("process", builder -> builder
                        .systemPrompt("You are a helpful assistant. Respond to user messages concisely.")
                        .inputKey("message")
                        .outputKey("response"))
                .edge("webhook", "process")
                .build();

        System.out.println("✅ Workflow built: " + workflow.getNodeIds());

        // Register webhook routes
        System.out.println("\n🔗 Registering webhook routes...");
        WebhookRegistry.getInstance().registerRoutes(app);

        // Add a simple test endpoint
        app.get("/", (req, res, next) -> {
            res.json(Map.of(
                    "message", "WebhookTrigger Demo",
                    "endpoints", Map.of(
                            "POST /api/webhook", "Trigger workflow with webhook",
                            "GET /health", "Health check"
                    ),
                    "test", "curl -X POST http://localhost:3000/api/webhook -H \"Content-Type: application/json\" -d '{\"message\":\"Hello, AI!\"}'"
            ));
        });

        app.get("/health", (req, res, next) -> {
            res.json(Map.of("status", "healthy"));
        });

        // Start server
        System.out.println("\n🌐 Starting server on port 3000...");
        System.out.println("📍 Webhook endpoint: POST http://localhost:3000/api/webhook");
        System.out.println("📍 Test endpoint: GET http://localhost:3000/");
        System.out.println("\n💡 Try it:");
        System.out.println("   curl -X POST http://localhost:3000/api/webhook \\");
        System.out.println("        -H \"Content-Type: application/json\" \\");
        System.out.println("        -d '{\"message\":\"Hello, AI!\"}'");
        System.out.println("\n⏳ Waiting for requests...\n");

        app.listen(3000);
    }
}

