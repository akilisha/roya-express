package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.WebhookTrigger;
import com.akilisha.oss.roya.plugins.ai.workflow.WorkflowRegistry;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Manual test script for Webhook Management API.
 *
 * This script:
 * 1. Starts a Roya server with AIPlugin
 * 2. Creates a test workflow
 * 3. Tests all webhook management endpoints
 * 4. Tests webhook execution
 *
 * Usage:
 *   java -cp ... WebhookManagementAPIManualTest
 *
 * Then use curl or Postman to test:
 *   curl http://localhost:8080/api/webhooks
 *   curl -X POST http://localhost:8080/api/webhooks -H "Content-Type: application/json" -d '{...}'
 */
public class WebhookManagementAPIManualTest {

    public static void main(String[] args) throws Exception {
        System.out.println("🚀 Starting Webhook Management API Test Server...");

        // Create Roya app
        Roya app = Roya.create();

        // Install AI plugin
        AIPlugin aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        aiPlugin.setup(app);

        // Get AI service
        AI ai = app.services().get(AI.class);

        // Create test workflow
        System.out.println("📋 Creating test workflow...");
        Workflow testWorkflow = ai.workflow("test-webhook-workflow")
            .trigger("webhook", WebhookTrigger.builder()
                .path("/api/test-webhook")
                .method("POST")
                .build())
            .llm("process", builder -> builder
                .systemPrompt("You are a helpful assistant. Summarize the webhook data.")
                .inputKey("message")
                .outputKey("summary")
            )
            .edge("webhook", "process")
            .build();

        System.out.println("✅ Test workflow created: test-webhook-workflow");
        System.out.println("✅ Webhook registered: POST /api/test-webhook");
        
        // Register webhook routes AFTER building the workflow
        // (The workflow build registers the webhook in WebhookRegistry, but routes need to be registered separately)
        com.akilisha.oss.roya.plugins.ai.webhooks.WebhookRegistry.getInstance().registerRoutes(app);
        System.out.println("✅ Webhook routes registered with HTTP server");

        // Start server
        int port = 8080;
        app.listen(port, () -> {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("✅ Webhook Management API Test Server Running!");
            System.out.println("=".repeat(60));
            System.out.println("\n📍 Server URL: http://localhost:" + port);
            System.out.println("\n📋 Test Endpoints:");
            System.out.println("  GET    http://localhost:" + port + "/api/webhooks");
            System.out.println("  POST   http://localhost:" + port + "/api/webhooks");
            System.out.println("  GET    http://localhost:" + port + "/api/webhooks/POST:/api/test-webhook");
            System.out.println("  PATCH  http://localhost:" + port + "/api/webhooks/POST:/api/test-webhook");
            System.out.println("  DELETE http://localhost:" + port + "/api/webhooks/POST:/api/test-webhook");
            System.out.println("\n🔗 Test Webhook Execution:");
            System.out.println("  POST   http://localhost:" + port + "/api/test-webhook");
            System.out.println("\n📝 Example curl command to trigger the workflow:");
            System.out.println("  curl -X POST http://localhost:" + port + "/api/test-webhook \\");
            System.out.println("    -H \"Content-Type: application/json\" \\");
            System.out.println("    -d '{\"message\": \"Hello from webhook!\"}'");
            System.out.println("\n📝 Example POST /api/webhooks (to create new webhook via API):");
            System.out.println("  {");
            System.out.println("    \"path\": \"/api/my-webhook\",");
            System.out.println("    \"method\": \"POST\",");
            System.out.println("    \"workflowName\": \"test-webhook-workflow\",");
            System.out.println("    \"triggerNodeId\": \"webhook\",");
            System.out.println("    \"secret\": \"my-secret\",");
            System.out.println("    \"algorithm\": \"sha256\"");
            System.out.println("  }");
            System.out.println("\n" + "=".repeat(60));
            System.out.println("Press Ctrl+C to stop the server");
            System.out.println("=".repeat(60) + "\n");
        });

        // Keep server running
        Thread.currentThread().join();
    }
}

