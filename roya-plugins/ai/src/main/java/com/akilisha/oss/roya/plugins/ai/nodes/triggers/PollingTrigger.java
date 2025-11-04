package com.akilisha.oss.roya.plugins.ai.nodes.triggers;

import com.akilisha.oss.roya.plugins.ai.polling.PollingRegistry;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * Polling trigger node - periodically polls an endpoint and triggers workflow when condition is met.
 * 
 * Uses ScheduledExecutorService to periodically poll HTTP endpoints and trigger workflows
 * when a condition is satisfied (e.g., status code 200, response body contains specific content).
 * 
 * Features:
 * - Configurable polling interval
 * - HTTP method support (GET, POST, PUT, etc.)
 * - Custom headers and request body
 * - Conditional triggering (predicate-based)
 * - Timeout configuration
 * 
 * Example usage:
 * <pre>
 * Workflow workflow = ai.workflow("poll-api")
 *     .trigger("poll", PollingTrigger.builder()
 *         .url("http://api.example.com/status")
 *         .interval(Duration.ofSeconds(5))
 *         .condition(response -> response.statusCode() == 200)
 *         .build())
 *     .llm("process", builder -> builder.systemPrompt("..."))
 *     .edge("poll", "process")
 *     .build();
 * </pre>
 */
public class PollingTrigger implements WorkflowNode {
    
    private final String url;
    private final String method;
    private final Duration interval;
    private final Duration timeout;
    private final Predicate<HttpResponse<String>> condition;
    private final Map<String, String> headers;
    private final String requestBody;
    private String workflowName; // Set during workflow build
    private String triggerNodeId; // Set during workflow build
    
    private PollingTrigger(String url, String method, Duration interval, Duration timeout,
                          Predicate<HttpResponse<String>> condition, Map<String, String> headers,
                          String requestBody) {
        this.url = url;
        this.method = method != null ? method : "GET";
        this.interval = interval != null ? interval : Duration.ofSeconds(5);
        this.timeout = timeout != null ? timeout : Duration.ofSeconds(10);
        this.condition = condition != null ? condition : response -> response.statusCode() == 200;
        this.headers = headers != null ? headers : new HashMap<>();
        this.requestBody = requestBody;
    }
    
    /**
     * Create a polling trigger with URL and interval.
     * 
     * @param url URL to poll
     * @param interval Polling interval
     * @return PollingTrigger instance
     */
    public static PollingTrigger create(String url, Duration interval) {
        return new PollingTrigger(url, "GET", interval, null, null, null, null);
    }
    
    /**
     * Create a polling trigger with condition.
     */
    public static PollingTrigger create(String url, Duration interval, Predicate<HttpResponse<String>> condition) {
        return new PollingTrigger(url, "GET", interval, null, condition, null, null);
    }
    
    /**
     * Set workflow metadata (called during workflow build).
     */
    public void setWorkflowMetadata(String workflowName, String triggerNodeId) {
        this.workflowName = workflowName;
        this.triggerNodeId = triggerNodeId;
    }
    
    /**
     * Register this polling task with the registry (called during workflow build).
     */
    public void register(com.akilisha.oss.roya.workflow.core.Workflow workflow) {
        if (workflowName == null || triggerNodeId == null) {
            throw new IllegalStateException("PollingTrigger must be set with workflow metadata before registration");
        }
        
        // Generate unique poll ID from workflow name and trigger node ID
        String pollId = generatePollId(workflowName, triggerNodeId);
        
        PollingRegistry.PollingRegistration registration = new PollingRegistry.PollingRegistration(
            pollId,
            url,
            method,
            interval,
            timeout,
            condition,
            headers,
            requestBody,
            workflowName,
            triggerNodeId,
            workflow,
            this
        );
        
        PollingRegistry.getInstance().register(registration);
    }
    
    /**
     * Generate a unique poll ID from workflow name and trigger node ID.
     */
    private String generatePollId(String workflowName, String triggerNodeId) {
        return workflowName + ":" + triggerNodeId;
    }
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        // When triggered by polling, pass through polling data
        // Polling data is already added by PollingRegistry
        return CompletableFuture.completedFuture(
            NodeOutput.success(input.data())
        );
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getMethod() {
        return method;
    }
    
    public Duration getInterval() {
        return interval;
    }
    
    public Duration getTimeout() {
        return timeout;
    }
    
    public Predicate<HttpResponse<String>> getCondition() {
        return condition;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public String getRequestBody() {
        return requestBody;
    }
    
    /**
     * Builder for PollingTrigger.
     */
    public static class Builder {
        private String url;
        private String method = "GET";
        private Duration interval = Duration.ofSeconds(5);
        private Duration timeout = Duration.ofSeconds(10);
        private Predicate<HttpResponse<String>> condition = response -> response.statusCode() == 200;
        private Map<String, String> headers = new HashMap<>();
        private String requestBody;
        
        /**
         * Set the URL to poll.
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }
        
        /**
         * Set the HTTP method.
         */
        public Builder method(String method) {
            this.method = method;
            return this;
        }
        
        /**
         * Set the polling interval.
         */
        public Builder interval(Duration interval) {
            this.interval = interval;
            return this;
        }
        
        /**
         * Set the request timeout.
         */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }
        
        /**
         * Set the condition predicate.
         */
        public Builder condition(Predicate<HttpResponse<String>> condition) {
            this.condition = condition;
            return this;
        }
        
        /**
         * Add a header.
         */
        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }
        
        /**
         * Set request body (for POST/PUT/PATCH).
         */
        public Builder requestBody(String body) {
            this.requestBody = body;
            return this;
        }
        
        /**
         * Build the PollingTrigger instance.
         */
        public PollingTrigger build() {
            if (url == null || url.isEmpty()) {
                throw new IllegalArgumentException("URL is required");
            }
            return new PollingTrigger(url, method, interval, timeout, condition, headers, requestBody);
        }
    }
    
    /**
     * Create a builder for PollingTrigger.
     */
    public static Builder builder() {
        return new Builder();
    }
}

