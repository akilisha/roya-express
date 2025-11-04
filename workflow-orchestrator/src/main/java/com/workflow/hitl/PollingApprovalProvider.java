package com.akilisha.oss.roya.workflow.hitl;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Simple polling-based approval provider.
 * Polls a response store until a human provides input or timeout occurs.
 */
public class PollingApprovalProvider implements ApprovalProvider {
    
    private final Map<String, PendingRequest> pendingRequests;
    private final Duration pollInterval;
    private final Duration timeout;
    private final ScheduledExecutorService scheduler;
    
    public PollingApprovalProvider() {
        this(Duration.ofSeconds(2), Duration.ofMinutes(10));
    }
    
    public PollingApprovalProvider(Duration pollInterval, Duration timeout) {
        this.pendingRequests = new ConcurrentHashMap<>();
        this.pollInterval = pollInterval;
        this.timeout = timeout;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    
    @Override
    public CompletableFuture<Boolean> requestApproval(
        String requestId,
        String prompt,
        Map<String, Object> context
    ) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        PendingRequest request = new PendingRequest(requestId, prompt, context, Instant.now());
        pendingRequests.put(requestId, request);
        
        // Start polling
        scheduler.scheduleAtFixedRate(
            () -> checkForResponse(requestId, future),
            0,
            pollInterval.toMillis(),
            TimeUnit.MILLISECONDS
        );
        
        // Timeout
        scheduler.schedule(
            () -> {
                if (!future.isDone()) {
                    future.completeExceptionally(
                        new ApprovalTimeoutException("Approval request timed out: " + requestId)
                    );
                    pendingRequests.remove(requestId);
                }
            },
            timeout.toMillis(),
            TimeUnit.MILLISECONDS
        );
        
        return future;
    }
    
    @Override
    public CompletableFuture<String> requestInput(
        String requestId,
        String prompt,
        Map<String, Object> context
    ) {
        CompletableFuture<String> future = new CompletableFuture<>();
        PendingRequest request = new PendingRequest(requestId, prompt, context, Instant.now());
        pendingRequests.put(requestId, request);
        
        // Similar polling pattern
        scheduler.scheduleAtFixedRate(
            () -> {
                PendingRequest pending = pendingRequests.get(requestId);
                if (pending != null && pending.hasTextResponse()) {
                    future.complete(pending.getTextResponse());
                    pendingRequests.remove(requestId);
                }
            },
            0,
            pollInterval.toMillis(),
            TimeUnit.MILLISECONDS
        );
        
        // Timeout
        scheduler.schedule(
            () -> {
                if (!future.isDone()) {
                    future.completeExceptionally(
                        new ApprovalTimeoutException("Input request timed out: " + requestId)
                    );
                    pendingRequests.remove(requestId);
                }
            },
            timeout.toMillis(),
            TimeUnit.MILLISECONDS
        );
        
        return future;
    }
    
    @Override
    public CompletableFuture<Integer> requestChoice(
        String requestId,
        String prompt,
        String[] options,
        Map<String, Object> context
    ) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        PendingRequest request = new PendingRequest(requestId, prompt, context, Instant.now());
        request.setOptions(options);
        pendingRequests.put(requestId, request);
        
        // Similar polling pattern
        scheduler.scheduleAtFixedRate(
            () -> {
                PendingRequest pending = pendingRequests.get(requestId);
                if (pending != null && pending.hasChoiceResponse()) {
                    future.complete(pending.getChoiceResponse());
                    pendingRequests.remove(requestId);
                }
            },
            0,
            pollInterval.toMillis(),
            TimeUnit.MILLISECONDS
        );
        
        // Timeout
        scheduler.schedule(
            () -> {
                if (!future.isDone()) {
                    future.completeExceptionally(
                        new ApprovalTimeoutException("Choice request timed out: " + requestId)
                    );
                    pendingRequests.remove(requestId);
                }
            },
            timeout.toMillis(),
            TimeUnit.MILLISECONDS
        );
        
        return future;
    }
    
    @Override
    public void cancelRequest(String requestId) {
        pendingRequests.remove(requestId);
    }
    
    private void checkForResponse(String requestId, CompletableFuture<Boolean> future) {
        PendingRequest request = pendingRequests.get(requestId);
        if (request != null && request.hasResponse()) {
            future.complete(request.isApproved());
            pendingRequests.remove(requestId);
        }
    }
    
    /**
     * Submit a response to a pending request (called by human/external system)
     */
    public void submitApproval(String requestId, boolean approved) {
        PendingRequest request = pendingRequests.get(requestId);
        if (request != null) {
            request.setApproved(approved);
        }
    }
    
    /**
     * Submit text input to a pending request
     */
    public void submitInput(String requestId, String input) {
        PendingRequest request = pendingRequests.get(requestId);
        if (request != null) {
            request.setTextResponse(input);
        }
    }
    
    /**
     * Submit choice to a pending request
     */
    public void submitChoice(String requestId, int choiceIndex) {
        PendingRequest request = pendingRequests.get(requestId);
        if (request != null) {
            request.setChoiceResponse(choiceIndex);
        }
    }
    
    /**
     * Get all pending requests (for UI display)
     */
    public Map<String, PendingRequest> getPendingRequests() {
        return Map.copyOf(pendingRequests);
    }
    
    /**
     * Shutdown the provider
     */
    public void shutdown() {
        scheduler.shutdown();
    }
    
    /**
     * Represents a pending approval/input request
     */
    public static class PendingRequest {
        private final String requestId;
        private final String prompt;
        private final Map<String, Object> context;
        private final Instant createdAt;
        private Boolean approved;
        private String textResponse;
        private Integer choiceResponse;
        private String[] options;
        
        public PendingRequest(String requestId, String prompt, Map<String, Object> context, Instant createdAt) {
            this.requestId = requestId;
            this.prompt = prompt;
            this.context = context;
            this.createdAt = createdAt;
        }
        
        public boolean hasResponse() {
            return approved != null;
        }
        
        public boolean hasTextResponse() {
            return textResponse != null;
        }
        
        public boolean hasChoiceResponse() {
            return choiceResponse != null;
        }
        
        public boolean isApproved() {
            return approved != null && approved;
        }
        
        public void setApproved(boolean approved) {
            this.approved = approved;
        }
        
        public String getTextResponse() {
            return textResponse;
        }
        
        public void setTextResponse(String textResponse) {
            this.textResponse = textResponse;
        }
        
        public Integer getChoiceResponse() {
            return choiceResponse;
        }
        
        public void setChoiceResponse(Integer choiceResponse) {
            this.choiceResponse = choiceResponse;
        }
        
        public String[] getOptions() {
            return options;
        }
        
        public void setOptions(String[] options) {
            this.options = options;
        }
        
        public String getRequestId() {
            return requestId;
        }
        
        public String getPrompt() {
            return prompt;
        }
        
        public Map<String, Object> getContext() {
            return context;
        }
        
        public Instant getCreatedAt() {
            return createdAt;
        }
        
        public Duration getAge() {
            return Duration.between(createdAt, Instant.now());
        }
    }
    
    /**
     * Exception thrown when approval/input times out
     */
    public static class ApprovalTimeoutException extends RuntimeException {
        public ApprovalTimeoutException(String message) {
            super(message);
        }
    }
}
