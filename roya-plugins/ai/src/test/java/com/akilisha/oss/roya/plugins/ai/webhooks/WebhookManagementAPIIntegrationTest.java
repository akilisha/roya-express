package com.akilisha.oss.roya.plugins.ai.webhooks;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.WebhookTrigger;
import com.akilisha.oss.roya.plugins.ai.workflow.WorkflowRegistry;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Webhook Management API.
 * 
 * Tests the full stack including HTTP server, persistence, and workflow execution.
 * 
 * Test Plan:
 * 1. Start Roya server with AIPlugin
 * 2. Test GET /api/webhooks (list all)
 * 3. Test POST /api/webhooks (create)
 * 4. Test GET /api/webhooks/:id (get specific)
 * 5. Test PATCH /api/webhooks/:id (update)
 * 6. Test DELETE /api/webhooks/:id (delete)
 * 7. Test webhook execution (POST to registered webhook)
 */
class WebhookManagementAPIIntegrationTest {

    private Roya app;
    private AIPlugin aiPlugin;
    private int serverPort;
    private HttpClient httpClient;
    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() throws Exception {
        // Create Roya app
        app = Roya.create();
        
        // Install AI plugin
        aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        aiPlugin.setup(app);
        
        // Start server
        serverPort = 8080;
        app.listen(serverPort, () -> {
            System.out.println("✓ Test server started on port " + serverPort);
        });
        
        // Give server time to start
        Thread.sleep(500);
        
        // Create HTTP client
        httpClient = HttpClient.newHttpClient();
        
        // Clear registry
        WorkflowRegistry.getInstance().clear();
        // Note: WebhookRegistry doesn't have a clear() method, but registrations are per-application
        // so each test starts fresh
    }

    @AfterEach
    void tearDown() throws Exception {
            if (app != null) {
                try {
                    app.close(); // Use close() instead of stop()
                } catch (Exception e) {
                    // Ignore
                }
            }
        WorkflowRegistry.getInstance().clear();
    }

    @Test
    void testListWebhooks_Empty() throws Exception {
        // GET /api/webhooks
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/webhooks"))
            .GET()
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        
        // Verify response contains empty array
        String body = response.body();
        assertTrue(body.contains("\"success\":true"));
        assertTrue(body.contains("\"webhooks\":[]") || body.contains("\"count\":0"));
    }

    @Test
    void testCreateWebhook_ValidRequest() throws Exception {
        // Create a test workflow
        Workflow testWorkflow = createTestWorkflow("test-workflow");
        WorkflowRegistry.getInstance().register("test-workflow", testWorkflow);
        
        // POST /api/webhooks
        String requestBody = """
            {
                "path": "/api/test-webhook",
                "method": "POST",
                "workflowName": "test-workflow",
                "triggerNodeId": "webhook",
                "secret": "test-secret",
                "algorithm": "sha256",
                "enabled": true
            }
            """;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/webhooks"))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .header("Content-Type", "application/json")
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(201, response.statusCode());
        
        String body = response.body();
        assertTrue(body.contains("\"success\":true"));
        assertTrue(body.contains("\"webhook\""));
        assertTrue(body.contains("/api/test-webhook"));
        
        // Verify webhook was created by listing
        HttpRequest listRequest = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/webhooks"))
            .GET()
            .build();
        
        HttpResponse<String> listResponse = httpClient.send(listRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, listResponse.statusCode());
        assertTrue(listResponse.body().contains("/api/test-webhook"));
    }

    @Test
    void testCreateWebhook_MissingRequiredFields() throws Exception {
        // POST /api/webhooks with missing fields
        String requestBody = """
            {
                "path": "/api/test"
            }
            """;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/webhooks"))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .header("Content-Type", "application/json")
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("error"));
    }

    @Test
    void testCreateWebhook_InvalidWorkflow() throws Exception {
        // POST /api/webhooks with non-existent workflow
        String requestBody = """
            {
                "path": "/api/test",
                "method": "POST",
                "workflowName": "non-existent-workflow",
                "triggerNodeId": "webhook"
            }
            """;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/webhooks"))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .header("Content-Type", "application/json")
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Workflow not found"));
    }

    @Test
    void testGetWebhook_ValidId() throws Exception {
        // First create a webhook
        Workflow testWorkflow = createTestWorkflow("test-workflow");
        WorkflowRegistry.getInstance().register("test-workflow", testWorkflow);
        
        String requestBody = """
            {
                "path": "/api/test-webhook",
                "method": "POST",
                "workflowName": "test-workflow",
                "triggerNodeId": "webhook"
            }
            """;
        
        HttpRequest createRequest = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/webhooks"))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .header("Content-Type", "application/json")
            .build();
        
        httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());
        
        // Now get it
        HttpRequest getRequest = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/webhooks/POST:/api/test-webhook"))
            .GET()
            .build();
        
        HttpResponse<String> response = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"success\":true"));
        assertTrue(response.body().contains("/api/test-webhook"));
    }

    @Test
    void testGetWebhook_NotFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/webhooks/INVALID"))
            .GET()
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("not found"));
    }

    @Test
    void testUpdateWebhook() throws Exception {
        // Create webhook
        Workflow testWorkflow = createTestWorkflow("test-workflow");
        WorkflowRegistry.getInstance().register("test-workflow", testWorkflow);
        
        String createBody = """
            {
                "path": "/api/test-webhook",
                "method": "POST",
                "workflowName": "test-workflow",
                "triggerNodeId": "webhook",
                "enabled": true
            }
            """;
        
        HttpRequest createRequest = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/webhooks"))
            .POST(HttpRequest.BodyPublishers.ofString(createBody))
            .header("Content-Type", "application/json")
            .build();
        
        httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());
        
        // Update webhook
        String updateBody = """
            {
                "enabled": false
            }
            """;
        
        HttpRequest updateRequest = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/webhooks/POST:/api/test-webhook"))
            .method("PATCH", HttpRequest.BodyPublishers.ofString(updateBody))
            .header("Content-Type", "application/json")
            .build();
        
        HttpResponse<String> response = httpClient.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"success\":true"));
        assertTrue(response.body().contains("\"enabled\":false"));
    }

    @Test
    void testDeleteWebhook() throws Exception {
        // Create webhook
        Workflow testWorkflow = createTestWorkflow("test-workflow");
        WorkflowRegistry.getInstance().register("test-workflow", testWorkflow);
        
        String createBody = """
            {
                "path": "/api/test-webhook",
                "method": "POST",
                "workflowName": "test-workflow",
                "triggerNodeId": "webhook"
            }
            """;
        
        HttpRequest createRequest = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/webhooks"))
            .POST(HttpRequest.BodyPublishers.ofString(createBody))
            .header("Content-Type", "application/json")
            .build();
        
        httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());
        
        // Delete webhook
        HttpRequest deleteRequest = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/webhooks/POST:/api/test-webhook"))
            .DELETE()
            .build();
        
        HttpResponse<String> response = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"success\":true"));
        
        // Verify deleted
        HttpRequest getRequest = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/webhooks/POST:/api/test-webhook"))
            .GET()
            .build();
        
        HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse.statusCode());
    }

    @Test
    void testWebhookExecution() throws Exception {
        // Create webhook with workflow
        Workflow testWorkflow = createTestWorkflow("test-workflow");
        WorkflowRegistry.getInstance().register("test-workflow", testWorkflow);
        
        String createBody = """
            {
                "path": "/api/test-webhook",
                "method": "POST",
                "workflowName": "test-workflow",
                "triggerNodeId": "webhook"
            }
            """;
        
        HttpRequest createRequest = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/webhooks"))
            .POST(HttpRequest.BodyPublishers.ofString(createBody))
            .header("Content-Type", "application/json")
            .build();
        
        httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());
        
        // Execute webhook
        String webhookBody = """
            {
                "message": "Hello from webhook test",
                "userId": "123"
            }
            """;
        
        HttpRequest webhookRequest = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/test-webhook"))
            .POST(HttpRequest.BodyPublishers.ofString(webhookBody))
            .header("Content-Type", "application/json")
            .build();
        
        HttpResponse<String> response = httpClient.send(webhookRequest, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"success\":true"));
        assertTrue(response.body().contains("\"result\""));
    }

    /**
     * Create a simple test workflow.
     */
    private Workflow createTestWorkflow(String name) {
        WorkflowNode processorNode = new WorkflowNode() {
            @Override
            public CompletableFuture<NodeOutput> execute(NodeInput input) {
                Map<String, Object> data = input.data();
                Map<String, Object> output = Map.of(
                    "processed", true,
                    "message", data.getOrDefault("message", ""),
                    "timestamp", System.currentTimeMillis()
                );
                return CompletableFuture.completedFuture(NodeOutput.success(output));
            }
        };
        
        return Workflow.create()
            .trigger("webhook", WebhookTrigger.builder()
                .path("/api/test-webhook")
                .method("POST")
                .build())
            .action("process", processorNode)
            .edge("webhook", "process")
            .build();
    }
}
