package com.akilisha.oss.roya.plugins.ai.polling;

import com.akilisha.oss.roya.plugins.ai.nodes.triggers.PollingTrigger;
import com.akilisha.oss.roya.plugins.ai.execution.WorkflowExecutorFactory;
import com.akilisha.oss.roya.plugins.ai.workflow.WorkflowRegistry;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Predicate;

/**
 * Registry for polling-triggered workflows.
 * 
 * Manages ScheduledExecutorService and registers polling tasks for PollingTrigger nodes,
 * enabling periodic polling of external endpoints and triggering workflows when conditions are met.
 */
public class PollingRegistry {
    
    private static final PollingRegistry INSTANCE = new PollingRegistry();
    
    /**
     * Polling registration information.
     */
    public record PollingRegistration(
        String pollId,
        String url,
        String method,
        Duration interval,
        Duration timeout,
        Predicate<HttpResponse<String>> condition,
        Map<String, String> headers,
        String requestBody,
        String workflowName,
        String triggerNodeId,
        Workflow workflow,
        PollingTrigger trigger
    ) {}
    
    private final Map<String, PollingRegistration> registrations = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private HttpClient httpClient;
    private boolean initialized = false;
    
    private PollingRegistry() {
        // Singleton
    }
    
    /**
     * Get the singleton instance.
     */
    public static PollingRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize the scheduler and HTTP client.
     * Should be called once during application startup.
     */
    public synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        scheduler = Executors.newScheduledThreadPool(10, r -> {
            Thread t = new Thread(r, "PollingRegistry-worker");
            t.setDaemon(true);
            return t;
        });
        
        httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        
        initialized = true;
        System.out.println("✓ PollingRegistry initialized - Scheduler started");
    }
    
    /**
     * Shutdown the scheduler.
     * Should be called during application shutdown.
     */
    public synchronized void shutdown() {
        if (scheduler != null) {
            // Cancel all scheduled tasks
            scheduledTasks.values().forEach(task -> task.cancel(false));
            scheduledTasks.clear();
            
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            System.out.println("✓ PollingRegistry shut down - Scheduler stopped");
        }
    }
    
    /**
     * Register a polling-triggered workflow.
     * 
     * @param registration Polling registration information
     */
    public void register(PollingRegistration registration) {
        if (!initialized) {
            initialize();
        }
        
        String pollId = registration.pollId();
        registrations.put(pollId, registration);
        
        // Schedule polling task
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
            () -> performPoll(registration),
            0, // Initial delay
            registration.interval().toMillis(),
            TimeUnit.MILLISECONDS
        );
        
        scheduledTasks.put(pollId, future);
        
        System.out.println("✓ Registered polling: " + pollId + " (" + registration.url() + 
            ", interval: " + registration.interval() + 
            ") → workflow: " + registration.workflowName());
    }
    
    /**
     * Unregister a polling task.
     */
    public void unregister(String pollId) {
        ScheduledFuture<?> future = scheduledTasks.remove(pollId);
        if (future != null) {
            future.cancel(false);
        }
        
        PollingRegistration registration = registrations.remove(pollId);
        if (registration != null) {
            System.out.println("✓ Unregistered polling: " + pollId);
        }
    }
    
    /**
     * Get registration for a poll ID.
     */
    public PollingRegistration get(String pollId) {
        return registrations.get(pollId);
    }
    
    /**
     * Perform a single poll operation.
     */
    private void performPoll(PollingRegistration registration) {
        scheduler.submit(() -> {
            try {
                // Build HTTP request
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(registration.url()))
                    .timeout(registration.timeout());
                
                // Add headers
                if (registration.headers() != null) {
                    registration.headers().forEach(requestBuilder::header);
                }
                
                // Set method and body
                String method = registration.method().toUpperCase();
                if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
                    String body = registration.requestBody() != null ? registration.requestBody() : "";
                    requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(body));
                } else {
                    requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
                }
                
                HttpRequest request = requestBuilder.build();
                
                // Execute HTTP request
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                // Check condition
                Predicate<HttpResponse<String>> condition = registration.condition();
                if (condition != null && !condition.test(response)) {
                    // Condition not met - continue polling
                    return;
                }
                
                // Condition met - execute workflow
                executeWorkflow(registration, response);
                
            } catch (Exception e) {
                System.err.println("✗ Polling error: " + registration.url() + " - " + e.getMessage());
                // Continue polling on error
            }
        });
    }
    
    /**
     * Execute workflow for polling result.
     */
    private void executeWorkflow(PollingRegistration registration, HttpResponse<String> response) {
        scheduler.submit(() -> {
            try {
                // Create workflow input with polling data
                Map<String, Object> pollData = new HashMap<>();
                pollData.put("url", registration.url());
                pollData.put("statusCode", response.statusCode());
                pollData.put("body", response.body());
                pollData.put("headers", response.headers().map());
                
                // Add polling metadata
                pollData.put("_polling", Map.of(
                    "pollId", registration.pollId(),
                    "method", registration.method(),
                    "timestamp", System.currentTimeMillis()
                ));
                
                // Execute workflow from trigger node
                WorkflowExecutor executor = WorkflowExecutorFactory.create(registration.workflow());
                WorkflowResult result = executor.executeFrom(
                    registration.triggerNodeId(),
                    pollData
                ).join();
                
                if (result.isSuccess()) {
                    System.out.println("✓ Polling workflow executed successfully: " + registration.workflowName() + 
                        " (trigger: " + registration.triggerNodeId() + ", url: " + registration.url() + ")");
                } else {
                    String error = result.finalOutput().error().orElse("Unknown error");
                    System.err.println("✗ Polling workflow execution failed: " + registration.workflowName() + 
                        " (trigger: " + registration.triggerNodeId() + ", url: " + registration.url() + ") - " + error);
                }
            } catch (Exception e) {
                System.err.println("✗ Polling workflow execution error: " + registration.workflowName() + 
                    " (trigger: " + registration.triggerNodeId() + ", url: " + registration.url() + ") - " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}

