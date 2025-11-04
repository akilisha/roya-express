package com.akilisha.oss.roya.plugins.ai.nodes.triggers;

import com.akilisha.oss.roya.plugins.ai.webhooks.WebhookRegistry;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.concurrent.CompletableFuture;

/**
 * Webhook trigger node - receives HTTP webhook requests and triggers workflow execution.
 * 
 * Automatically registers HTTP routes when used in workflows, enabling external systems
 * to trigger AI workflows via HTTP requests.
 * 
 * Features:
 * - Auto-registers HTTP routes (POST, GET, PUT, etc.)
 * - Extracts webhook payload (JSON, form-data, text)
 * - Passes webhook data to workflow context
 * - Supports signature verification (optional)
 * 
 * Example usage:
 * <pre>
 * Workflow workflow = ai.workflow("webhook-handler")
 *     .trigger("webhook", WebhookTrigger.builder()
 *         .path("/api/webhook")
 *         .method("POST")
 *         .build())
 *     .llm("process", builder -> builder.systemPrompt("..."))
 *     .edge("webhook", "process")
 *     .build();
 * 
 * // Webhook automatically registered: POST /api/webhook
 * // External systems can POST to this endpoint to trigger workflow
 * </pre>
 */
public class WebhookTrigger implements WorkflowNode {
    
    private final String path;
    private final String httpMethod;
    private final String secret; // For signature verification
    private String workflowName; // Set during workflow build
    private String triggerNodeId; // Set during workflow build
    
    private WebhookTrigger(String path, String httpMethod, String secret) {
        this.path = path;
        this.httpMethod = httpMethod != null ? httpMethod : "POST";
        this.secret = secret;
    }
    
    /**
     * Create a webhook trigger builder.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Create a webhook trigger for a specific path (defaults to POST).
     */
    public static WebhookTrigger create(String path) {
        return new Builder().path(path).build();
    }
    
    /**
     * Create a webhook trigger with default path (/webhook).
     */
    public static WebhookTrigger create() {
        return new Builder().build();
    }
    
    /**
     * Set workflow metadata (called during workflow build).
     */
    public void setWorkflowMetadata(String workflowName, String triggerNodeId) {
        this.workflowName = workflowName;
        this.triggerNodeId = triggerNodeId;
    }
    
    /**
     * Register this webhook with the registry (called during workflow build).
     */
    public void register(com.akilisha.oss.roya.workflow.core.Workflow workflow) {
        if (workflowName == null || triggerNodeId == null) {
            throw new IllegalStateException("WebhookTrigger must be set with workflow metadata before registration");
        }
        
        WebhookRegistry.WebhookRegistration registration = new WebhookRegistry.WebhookRegistration(
            path,
            httpMethod,
            workflowName,
            triggerNodeId,
            workflow,
            this
        );
        
        WebhookRegistry.getInstance().register(registration);
    }
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        // Pass through webhook data to workflow
        // The HTTP handler extracts the data and passes it here
        return CompletableFuture.completedFuture(
            NodeOutput.success(input.data())
        );
    }
    
    /**
     * Get the webhook path.
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Get the HTTP method.
     */
    public String getHttpMethod() {
        return httpMethod;
    }
    
    /**
     * Get the secret for signature verification (if set).
     */
    public String getSecret() {
        return secret;
    }
    
    /**
     * Builder for WebhookTrigger.
     */
    public static class Builder {
        private String path = "/webhook";
        private String httpMethod = "POST";
        private String secret;
        
        /**
         * Set the webhook endpoint path.
         */
        public Builder path(String path) {
            this.path = path;
            return this;
        }
        
        /**
         * Set the HTTP method (POST, GET, PUT, etc.).
         */
        public Builder method(String method) {
            this.httpMethod = method;
            return this;
        }
        
        /**
         * Set the webhook secret for signature verification.
         */
        public Builder secret(String secret) {
            this.secret = secret;
            return this;
        }
        
        /**
         * Build the WebhookTrigger instance.
         */
        public WebhookTrigger build() {
            return new WebhookTrigger(path, httpMethod, secret);
        }
    }
}

