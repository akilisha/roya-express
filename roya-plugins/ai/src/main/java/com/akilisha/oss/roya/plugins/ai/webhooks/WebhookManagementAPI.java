package com.akilisha.oss.roya.plugins.ai.webhooks;

import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.akilisha.oss.roya.plugins.ai.workflow.WorkflowRegistry;
import com.akilisha.oss.roya.workflow.core.Workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Webhook Management API endpoints.
 * 
 * Provides REST API for managing webhook configurations:
 * - GET    /api/webhooks       - List all webhooks
 * - GET    /api/webhooks/:id   - Get a specific webhook
 * - POST   /api/webhooks       - Create/register a new webhook
 * - DELETE /api/webhooks/:id   - Delete a webhook
 * - PATCH  /api/webhooks/:id   - Update a webhook (enable/disable, update secret, etc.)
 */
public class WebhookManagementAPI {
    
    private final WebhookPersistenceService persistenceService;
    
    public WebhookManagementAPI(WebhookPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }
    
    /**
     * Register all webhook management routes with the application.
     */
    public void registerRoutes(com.akilisha.oss.roya.api.plugin.Application app) {
        // List all webhooks
        app.route("GET", "/api/webhooks", listWebhooks());
        
        // Get specific webhook
        app.route("GET", "/api/webhooks/:id", getWebhook());
        
        // Create new webhook
        app.route("POST", "/api/webhooks", createWebhook());
        
        // Update webhook
        app.route("PATCH", "/api/webhooks/:id", updateWebhook());
        
        // Delete webhook
        app.route("DELETE", "/api/webhooks/:id", deleteWebhook());
    }
    
    /**
     * GET /api/webhooks - List all webhooks.
     */
    private Handler listWebhooks() {
        return (Request req, Response res, Next next) -> {
            try {
                List<WebhookPersistenceService.WebhookConfig> configs = 
                    persistenceService.getStore().loadAll();
                
                List<Map<String, Object>> webhooks = new ArrayList<>();
                for (WebhookPersistenceService.WebhookConfig config : configs) {
                    webhooks.add(toJson(config));
                }
                
                res.status(200).json(Map.of(
                    "success", true,
                    "webhooks", webhooks,
                    "count", webhooks.size()
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
            }
        };
    }
    
    /**
     * GET /api/webhooks/:id - Get a specific webhook.
     */
    private Handler getWebhook() {
        return (Request req, Response res, Next next) -> {
            try {
                String id = req.params().get("id").orElse(null);
                if (id == null || id.isEmpty()) {
                    res.status(400).json(Map.of(
                        "success", false,
                        "error", "Webhook ID is required"
                    ));
                    return;
                }
                
                WebhookPersistenceService.WebhookConfig config = 
                    persistenceService.getStore().load(id);
                
                if (config == null) {
                    res.status(404).json(Map.of(
                        "success", false,
                        "error", "Webhook not found: " + id
                    ));
                    return;
                }
                
                res.status(200).json(Map.of(
                    "success", true,
                    "webhook", toJson(config)
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
            }
        };
    }
    
    /**
     * POST /api/webhooks - Create/register a new webhook.
     */
    private Handler createWebhook() {
        return (Request req, Response res, Next next) -> {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = req.body(Map.class);
                
                if (body == null) {
                    res.status(400).json(Map.of(
                        "success", false,
                        "error", "Request body is required"
                    ));
                    return;
                }
                
                // Validate required fields
                String path = (String) body.get("path");
                String method = (String) body.get("method");
                String workflowName = (String) body.get("workflowName");
                String triggerNodeId = (String) body.get("triggerNodeId");
                
                if (path == null || method == null || workflowName == null || triggerNodeId == null) {
                    res.status(400).json(Map.of(
                        "success", false,
                        "error", "Missing required fields: path, method, workflowName, triggerNodeId"
                    ));
                    return;
                }
                
                // Get workflow from registry
                WorkflowRegistry registry = WorkflowRegistry.getInstance();
                Workflow workflow = registry.get(workflowName);
                
                if (workflow == null) {
                    res.status(400).json(Map.of(
                        "success", false,
                        "error", "Workflow not found: " + workflowName
                    ));
                    return;
                }
                
                // Extract optional fields
                String secret = (String) body.get("secret");
                String algorithmStr = (String) body.get("algorithm");
                String headerName = (String) body.get("headerName");
                Boolean enabled = body.get("enabled") != null ? 
                    Boolean.parseBoolean(body.get("enabled").toString()) : true;
                
                @SuppressWarnings("unchecked")
                Map<String, Object> metadata = (Map<String, Object>) body.get("metadata");
                
                WebhookSignatureVerifier.Algorithm algorithm = algorithmStr != null ?
                    WebhookSignatureVerifier.Algorithm.fromString(algorithmStr) : 
                    WebhookSignatureVerifier.Algorithm.SHA256;
                
                // Create webhook config
                WebhookPersistenceService.WebhookConfig config = new WebhookPersistenceService.WebhookConfig(
                    WebhookPersistenceService.generateId(path, method),
                    path,
                    method.toUpperCase(),
                    workflowName,
                    triggerNodeId,
                    secret,
                    algorithm,
                    headerName != null ? headerName : 
                        WebhookSignatureVerifier.HeaderName.X_HUB_SIGNATURE_256.getHeaderName(),
                    enabled,
                    metadata != null ? metadata : Map.of()
                );
                
                // Register webhook
                persistenceService.registerWebhook(config, workflow);
                
                res.status(201).json(Map.of(
                    "success", true,
                    "webhook", toJson(config),
                    "message", "Webhook registered successfully"
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
            }
        };
    }
    
    /**
     * PATCH /api/webhooks/:id - Update a webhook.
     */
    private Handler updateWebhook() {
        return (Request req, Response res, Next next) -> {
            try {
                String id = req.params().get("id").orElse(null);
                if (id == null || id.isEmpty()) {
                    res.status(400).json(Map.of(
                        "success", false,
                        "error", "Webhook ID is required"
                    ));
                    return;
                }
                
                WebhookPersistenceService.WebhookConfig existing = 
                    persistenceService.getStore().load(id);
                
                if (existing == null) {
                    res.status(404).json(Map.of(
                        "success", false,
                        "error", "Webhook not found: " + id
                    ));
                    return;
                }
                
                @SuppressWarnings("unchecked")
                Map<String, Object> body = req.body(Map.class);
                
                if (body == null || body.isEmpty()) {
                    res.status(400).json(Map.of(
                        "success", false,
                        "error", "Request body is required"
                    ));
                    return;
                }
                
                // Build updated config (only update provided fields)
                String secret = body.containsKey("secret") ? 
                    (String) body.get("secret") : existing.secret();
                String algorithmStr = body.containsKey("algorithm") ? 
                    (String) body.get("algorithm") : 
                    (existing.algorithm() != null ? existing.algorithm().getName() : null);
                String headerName = body.containsKey("headerName") ? 
                    (String) body.get("headerName") : existing.headerName();
                Boolean enabled = body.containsKey("enabled") ? 
                    Boolean.parseBoolean(body.get("enabled").toString()) : existing.enabled();
                
                @SuppressWarnings("unchecked")
                Map<String, Object> metadata = body.containsKey("metadata") ? 
                    (Map<String, Object>) body.get("metadata") : existing.metadata();
                
                WebhookSignatureVerifier.Algorithm algorithm = algorithmStr != null ?
                    WebhookSignatureVerifier.Algorithm.fromString(algorithmStr) : 
                    existing.algorithm();
                
                WebhookPersistenceService.WebhookConfig updated = new WebhookPersistenceService.WebhookConfig(
                    existing.id(),
                    existing.path(),
                    existing.method(),
                    existing.workflowName(),
                    existing.triggerNodeId(),
                    secret,
                    algorithm,
                    headerName,
                    enabled,
                    metadata
                );
                
                // Update in store
                persistenceService.getStore().save(updated);
                
                // Re-register if enabled
                if (updated.enabled()) {
                    WorkflowRegistry registry = WorkflowRegistry.getInstance();
                    Workflow workflow = registry.get(updated.workflowName());
                    if (workflow != null) {
                        persistenceService.registerWebhook(updated, workflow);
                    }
                }
                
                res.status(200).json(Map.of(
                    "success", true,
                    "webhook", toJson(updated),
                    "message", "Webhook updated successfully"
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
            }
        };
    }
    
    /**
     * DELETE /api/webhooks/:id - Delete a webhook.
     */
    private Handler deleteWebhook() {
        return (Request req, Response res, Next next) -> {
            try {
                String id = req.params().get("id").orElse(null);
                if (id == null || id.isEmpty()) {
                    res.status(400).json(Map.of(
                        "success", false,
                        "error", "Webhook ID is required"
                    ));
                    return;
                }
                
                WebhookPersistenceService.WebhookConfig config = 
                    persistenceService.getStore().load(id);
                
                if (config == null) {
                    res.status(404).json(Map.of(
                        "success", false,
                        "error", "Webhook not found: " + id
                    ));
                    return;
                }
                
                persistenceService.unregisterWebhook(id);
                
                res.status(200).json(Map.of(
                    "success", true,
                    "message", "Webhook deleted successfully"
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
            }
        };
    }
    
    /**
     * Convert WebhookConfig to JSON-safe map.
     */
    private Map<String, Object> toJson(WebhookPersistenceService.WebhookConfig config) {
        Map<String, Object> json = new HashMap<>();
        json.put("id", config.id());
        json.put("path", config.path());
        json.put("method", config.method());
        json.put("workflowName", config.workflowName());
        json.put("triggerNodeId", config.triggerNodeId());
        json.put("enabled", config.enabled());
        json.put("algorithm", config.algorithm() != null ? config.algorithm().getName() : null);
        json.put("headerName", config.headerName());
        json.put("metadata", config.metadata());
        // Don't expose secret in JSON response (security)
        json.put("hasSecret", config.secret() != null && !config.secret().isEmpty());
        return json;
    }
}

