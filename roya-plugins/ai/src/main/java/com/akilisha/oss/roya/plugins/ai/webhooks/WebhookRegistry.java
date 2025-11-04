package com.akilisha.oss.roya.plugins.ai.webhooks;

import com.akilisha.oss.roya.api.*;
import com.akilisha.oss.roya.api.plugin.Application;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.WebhookTrigger;
import com.akilisha.oss.roya.plugins.ai.execution.WorkflowExecutorFactory;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for webhook-triggered workflows.
 * 
 * Maps webhook paths to workflows, enabling automatic HTTP route registration
 * when WebhookTrigger nodes are used in workflows.
 */
public class WebhookRegistry {
    
    private static final WebhookRegistry INSTANCE = new WebhookRegistry();
    
    /**
     * Webhook registration information.
     */
    public record WebhookRegistration(
        String path,
        String httpMethod,
        String workflowName,
        String triggerNodeId,
        Workflow workflow,
        WebhookTrigger trigger
    ) {}
    
    private final Map<String, WebhookRegistration> registrations = new ConcurrentHashMap<>();
    
    private WebhookRegistry() {
        // Singleton
    }
    
    /**
     * Get the singleton instance.
     */
    public static WebhookRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register a webhook-triggered workflow.
     * 
     * @param registration Webhook registration information
     */
    public void register(WebhookRegistration registration) {
        String key = registration.path() + ":" + registration.httpMethod();
        registrations.put(key, registration);
    }
    
    /**
     * Get webhook registration for a path and method.
     */
    public WebhookRegistration get(String path, String method) {
        String key = path + ":" + method;
        return registrations.get(key);
    }
    
    /**
     * Create HTTP handler for a webhook path.
     * This handler extracts webhook data and triggers workflow execution.
     */
    public static Handler createHandler(WebhookRegistration registration) {
        return (Request req, Response res, Next next) -> {
            try {
                // Extract webhook data from request
                Map<String, Object> webhookData = extractWebhookData(req);
                
                // Add webhook metadata
                webhookData.put("_webhook", Map.of(
                    "path", req.path(),
                    "method", req.method(),
                    "headers", req.headers().all(),
                    "query", req.query().all()
                ));
                
                // Execute workflow from trigger node
                WorkflowExecutor executor = WorkflowExecutorFactory.create(registration.workflow());
                WorkflowResult result = executor.executeFrom(
                    registration.triggerNodeId(),
                    webhookData
                ).join();
                
                // Return workflow result
                if (result.isSuccess()) {
                    res.status(200).json(Map.of(
                        "success", true,
                        "result", result.finalOutput().data()
                    ));
                } else {
                    String errorMessage = result.finalOutput().error()
                        .orElse("Unknown error");
                    res.status(500).json(Map.of(
                        "success", false,
                        "error", errorMessage
                    ));
                }
            } catch (Exception e) {
                res.status(500).json(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
            }
        };
    }
    
    /**
     * Extract webhook data from HTTP request.
     */
    private static Map<String, Object> extractWebhookData(Request req) {
        Map<String, Object> data = new java.util.HashMap<>();
        
        // Body (if JSON)
        if (req.headers().contentType().map(ct -> ct.contains("application/json")).orElse(false)) {
            Object body = req.body();
            
            // If body is already parsed (by middleware), use it
            if (body instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> bodyMap = (Map<String, Object>) body;
                data.putAll(bodyMap);
            } else if (body == null) {
                // Body not parsed yet - parse manually
                String bodyText = req.bodyText();
                if (bodyText != null && !bodyText.isEmpty()) {
                    try {
                        // Try to get ObjectMapper from request context, or create default
                        com.fasterxml.jackson.databind.ObjectMapper objectMapper;
                        try {
                            objectMapper = req.get(com.fasterxml.jackson.databind.ObjectMapper.class);
                        } catch (Exception e) {
                            objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        }
                        
                        Object parsed = objectMapper.readValue(bodyText, Object.class);
                        if (parsed instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> bodyMap = (Map<String, Object>) parsed;
                            data.putAll(bodyMap);
                        } else {
                            data.put("body", parsed);
                        }
                    } catch (Exception e) {
                        // JSON parsing failed - store as text
                        data.put("body", bodyText);
                    }
                }
            } else {
                // Body is some other type - store as-is
                data.put("body", body);
            }
        } else {
            // Form data or text
            String bodyText = req.bodyText();
            if (bodyText != null && !bodyText.isEmpty()) {
                data.put("body", bodyText);
            }
        }
        
        // Query parameters
        req.query().all().forEach((key, value) -> {
            data.put("query_" + key, value);
        });
        
        return data;
    }
    
    /**
     * Register all webhook routes with the application.
     * This should be called after workflows are built.
     */
    public void registerRoutes(Application app) {
        for (WebhookRegistration registration : registrations.values()) {
            registerRoute(app, registration);
        }
    }
    
    /**
     * Register a single webhook route with the application.
     * This allows dynamic route registration after the server has started.
     */
    public void registerRoute(Application app, WebhookRegistration registration) {
        String method = registration.httpMethod().toUpperCase();
        String path = registration.path();
        
        // Register route with appropriate HTTP method
        Handler handler = createHandler(registration);
        app.route(method, path, handler);
        
        System.out.println("✅ Registered webhook: " + method + " " + path + " → workflow: " + registration.workflowName());
    }
    
    /**
     * Register a single webhook route with the application by path and method.
     * This allows dynamic route registration after the server has started.
     */
    public void registerRoute(Application app, String path, String method) {
        WebhookRegistration registration = get(path, method);
        if (registration != null) {
            registerRoute(app, registration);
        } else {
            System.out.println("⚠️  Webhook not found for registration: " + method + " " + path);
        }
    }
}

