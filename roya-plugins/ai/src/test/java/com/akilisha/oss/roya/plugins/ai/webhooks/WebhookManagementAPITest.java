package com.akilisha.oss.roya.plugins.ai.webhooks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WebhookManagementAPI.
 * 
 * Note: Most functionality is tested via integration tests.
 * These unit tests verify helper methods and edge cases.
 */
class WebhookManagementAPITest {

    @Test
    void testGenerateId() {
        String id = WebhookPersistenceService.generateId("/api/webhook", "POST");
        assertEquals("POST:/api/webhook", id);
        
        id = WebhookPersistenceService.generateId("/api/test", "GET");
        assertEquals("GET:/api/test", id);
        
        // Method should be uppercase
        id = WebhookPersistenceService.generateId("/api/test", "post");
        assertEquals("POST:/api/test", id);
    }

    @Test
    void testWebhookConfigFromRegistration() {
        WebhookRegistry.WebhookRegistration registration = new WebhookRegistry.WebhookRegistration(
            "/api/webhook",
            "POST",
            "test-workflow",
            "webhook",
            null,
            null
        );
        
        WebhookPersistenceService.WebhookConfig config = WebhookPersistenceService.WebhookConfig.fromRegistration(
            registration,
            "test-secret",
            WebhookSignatureVerifier.Algorithm.SHA256,
            "X-Hub-Signature-256"
        );
        
        assertEquals("POST:/api/webhook", config.id());
        assertEquals("/api/webhook", config.path());
        assertEquals("POST", config.method());
        assertEquals("test-workflow", config.workflowName());
        assertEquals("test-secret", config.secret());
        assertEquals(WebhookSignatureVerifier.Algorithm.SHA256, config.algorithm());
    }
}

