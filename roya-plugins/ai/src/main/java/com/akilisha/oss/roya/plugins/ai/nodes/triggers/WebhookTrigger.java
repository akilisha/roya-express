package com.akilisha.oss.roya.plugins.ai.nodes.triggers;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.concurrent.CompletableFuture;

/**
 * Webhook trigger node - receives HTTP webhook requests.
 * 
 * In a real implementation, this would be registered with the framework's HTTP router
 * to receive POST/GET requests at a specific endpoint. For now, this is a placeholder
 * that can be triggered manually with webhook data.
 * 
 * TODO: Integrate with Roya's HTTP routing to auto-register webhook endpoints
 * TODO: Support webhook signature verification
 * TODO: Support different HTTP methods (GET, POST, PUT, etc.)
 * TODO: Support webhook path configuration
 * 
 * Example usage (when integrated):
 * <pre>
 * ai.workflow("webhook-handler")
 *     .trigger("webhook", WebhookTrigger.create("/api/webhook"))
 *     .llm("process", builder -> builder.systemPrompt("..."))
 *     .edge("webhook", "process")
 *     .build();
 * </pre>
 */
public class WebhookTrigger implements WorkflowNode {
    
    private final String path;
    
    private WebhookTrigger(String path) {
        this.path = path;
    }
    
    /**
     * Create a webhook trigger for a specific path.
     * 
     * @param path The webhook endpoint path (e.g., "/api/webhook")
     * @return WebhookTrigger instance
     */
    public static WebhookTrigger create(String path) {
        return new WebhookTrigger(path);
    }
    
    /**
     * Create a webhook trigger with default path.
     */
    public static WebhookTrigger create() {
        return new WebhookTrigger("/webhook");
    }
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        // Pass through input data (webhook payload)
        // In real implementation, this would be called by HTTP handler
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
}

