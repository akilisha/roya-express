package com.akilisha.oss.roya.plugins.ai.nodes.triggers;

import com.akilisha.oss.roya.plugins.ai.webhooks.WebhookRegistry;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for WebhookTrigger.
 * 
 * Tests webhook registration, HTTP route creation, and workflow execution.
 */
class WebhookTriggerTest {

    @BeforeEach
    void setUp() {
        // Clear any existing webhook registrations between tests
        // Note: In a real scenario, you'd want a reset method on WebhookRegistry
    }

    @Test
    void testWebhookTriggerCreation() {
        // Test basic creation
        WebhookTrigger trigger1 = WebhookTrigger.create();
        assertNotNull(trigger1);
        assertEquals("/webhook", trigger1.getPath());
        assertEquals("POST", trigger1.getHttpMethod());

        // Test with path
        WebhookTrigger trigger2 = WebhookTrigger.create("/api/webhook");
        assertNotNull(trigger2);
        assertEquals("/api/webhook", trigger2.getPath());
        assertEquals("POST", trigger2.getHttpMethod());

        // Test with builder
        WebhookTrigger trigger3 = WebhookTrigger.builder()
            .path("/custom/webhook")
            .method("GET")
            .secret("test-secret")
            .build();
        assertNotNull(trigger3);
        assertEquals("/custom/webhook", trigger3.getPath());
        assertEquals("GET", trigger3.getHttpMethod());
        assertEquals("test-secret", trigger3.getSecret());
    }

    @Test
    void testWebhookTriggerWorkflowExecution() {
        // Create a simple workflow with webhook trigger
        WebhookTrigger trigger = WebhookTrigger.builder()
            .path("/test/webhook")
            .method("POST")
            .build();

        // Create a simple workflow manually
        Workflow workflow = Workflow.create()
            .trigger("webhook", trigger)
            .build();

        // Set workflow metadata and register
        trigger.setWorkflowMetadata("test-webhook", "webhook");
        trigger.register(workflow);

        // Execute workflow manually (simulating webhook call)
        WorkflowExecutor executor = new WorkflowExecutor(workflow);
        Map<String, Object> webhookData = Map.of(
            "message", "Hello from webhook",
            "user", "test-user"
        );

        CompletableFuture<WorkflowResult> future = executor.executeFrom("webhook", webhookData);
        WorkflowResult result = future.join();

        // Verify workflow executed successfully
        assertTrue(result.isSuccess());
        assertNotNull(result.finalOutput());
        
        // Verify webhook data was passed through
        Map<String, Object> outputData = result.finalOutput().data();
        assertTrue(outputData.containsKey("message"));
        assertEquals("Hello from webhook", outputData.get("message"));
    }

    @Test
    void testWebhookRegistration() {
        // Build workflow with webhook trigger
        WebhookTrigger trigger = WebhookTrigger.builder()
            .path("/api/test")
            .method("POST")
            .build();

        Workflow workflow = Workflow.create()
            .trigger("webhook", trigger)
            .build();

        // Set metadata and register
        trigger.setWorkflowMetadata("registration-test", "webhook");
        trigger.register(workflow);

        // Verify webhook was registered
        WebhookRegistry.WebhookRegistration registration = WebhookRegistry.getInstance()
            .get("/api/test", "POST");
        assertNotNull(registration);
        assertEquals("/api/test", registration.path());
        assertEquals("POST", registration.httpMethod());
        assertEquals("registration-test", registration.workflowName());
        assertEquals("webhook", registration.triggerNodeId());
    }

    @Test
    void testMultipleWebhookTriggers() {
        // Create workflow with webhook trigger
        WebhookTrigger trigger1 = WebhookTrigger.builder()
            .path("/api/webhook1")
            .method("POST")
            .build();

        Workflow workflow1 = Workflow.create()
            .trigger("webhook1", trigger1)
            .build();

        trigger1.setWorkflowMetadata("webhook1", "webhook1");
        trigger1.register(workflow1);

        // Create second workflow
        WebhookTrigger trigger2 = WebhookTrigger.builder()
            .path("/api/webhook2")
            .method("GET")
            .build();

        Workflow workflow2 = Workflow.create()
            .trigger("webhook2", trigger2)
            .build();

        trigger2.setWorkflowMetadata("webhook2", "webhook2");
        trigger2.register(workflow2);

        // Verify both are registered
        WebhookRegistry.WebhookRegistration reg1 = WebhookRegistry.getInstance()
            .get("/api/webhook1", "POST");
        assertNotNull(reg1);
        assertEquals("webhook1", reg1.workflowName());

        WebhookRegistry.WebhookRegistration reg2 = WebhookRegistry.getInstance()
            .get("/api/webhook2", "GET");
        assertNotNull(reg2);
        assertEquals("webhook2", reg2.workflowName());
    }

    @Test
    void testWebhookTriggerWithCustomNode() {
        // Create a custom node that processes webhook data
        WorkflowNode customNode = new WorkflowNode() {
            @Override
            public CompletableFuture<NodeOutput> execute(NodeInput input) {
                Map<String, Object> data = input.data();
                // Transform the data
                Map<String, Object> output = Map.of(
                    "processed", true,
                    "originalMessage", data.getOrDefault("message", ""),
                    "timestamp", System.currentTimeMillis()
                );
                return CompletableFuture.completedFuture(NodeOutput.success(output));
            }
        };

        // Create workflow
        WebhookTrigger trigger = WebhookTrigger.builder()
            .path("/api/custom")
            .method("POST")
            .build();

        Workflow workflow = Workflow.create()
            .trigger("webhook", trigger)
            .action("process", customNode)
            .edge("webhook", "process")
            .build();

        trigger.setWorkflowMetadata("custom-test", "webhook");
        trigger.register(workflow);

        // Execute workflow
        WorkflowExecutor executor = new WorkflowExecutor(workflow);
        Map<String, Object> webhookData = Map.of(
            "message", "Hello, custom processor!"
        );

        CompletableFuture<WorkflowResult> future = executor.executeFrom("webhook", webhookData);
        WorkflowResult result = future.join();

        // Verify workflow executed and processed data
        assertTrue(result.isSuccess());
        Map<String, Object> output = result.finalOutput().data();
        assertTrue(output.containsKey("processed"));
        assertEquals(true, output.get("processed"));
        assertEquals("Hello, custom processor!", output.get("originalMessage"));
    }
}

