package com.akilisha.oss.roya.workflow.edges;

import com.akilisha.oss.roya.workflow.core.ExecutionContext;
import com.akilisha.oss.roya.workflow.retry.RetryPolicy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Represents an edge in the workflow graph.
 * Contains routing logic, execution mode, retry policy, and metadata.
 */
public class Edge {
    
    private final String targetNodeId;
    private final ExecutionMode mode;
    private final Predicate<ExecutionContext> condition;
    private final Map<String, Object> metadata;
    private final RetryPolicy retryPolicy;
    private final ErrorStrategy errorStrategy;
    
    private Edge(String targetNodeId,
                 ExecutionMode mode,
                 Predicate<ExecutionContext> condition,
                 Map<String, Object> metadata,
                 RetryPolicy retryPolicy,
                 ErrorStrategy errorStrategy) {
        this.targetNodeId = targetNodeId;
        this.mode = mode;
        this.condition = condition;
        this.metadata = metadata;
        this.retryPolicy = retryPolicy;
        this.errorStrategy = errorStrategy;
    }
    
    // ===== Factory Methods =====
    
    /**
     * Create a sequential edge (default)
     */
    public static Edge sequential() {
        return new Edge(null, ExecutionMode.SEQUENTIAL, ctx -> true, Map.of(),
            RetryPolicy.noRetry(), ErrorStrategy.PROPAGATE);
    }
    
    /**
     * Create a parallel execution edge
     */
    public static Edge parallel() {
        return new Edge(null, ExecutionMode.PARALLEL, ctx -> true, Map.of(),
            RetryPolicy.noRetry(), ErrorStrategy.PROPAGATE);
    }
    
    /**
     * Create an async edge (fire and forget)
     */
    public static Edge async() {
        return new Edge(null, ExecutionMode.ASYNC, ctx -> true, Map.of(),
            RetryPolicy.noRetry(), ErrorStrategy.PROPAGATE);
    }
    
    /**
     * Create a conditional edge
     */
    public static Edge when(Predicate<ExecutionContext> condition) {
        return new Edge(null, ExecutionMode.SEQUENTIAL, condition, Map.of(),
            RetryPolicy.noRetry(), ErrorStrategy.PROPAGATE);
    }
    
    // ===== Fluent Configuration =====
    
    /**
     * Add metadata to the edge
     */
    public Edge withMetadata(String key, Object value) {
        Map<String, Object> newMetadata = new HashMap<>(this.metadata);
        newMetadata.put(key, value);
        return new Edge(targetNodeId, mode, condition, newMetadata, retryPolicy, errorStrategy);
    }
    
    /**
     * Set execution mode
     */
    public Edge withMode(ExecutionMode mode) {
        return new Edge(targetNodeId, mode, condition, metadata, retryPolicy, errorStrategy);
    }
    
    /**
     * Add retry policy
     */
    public Edge withRetry(RetryPolicy retryPolicy) {
        return new Edge(targetNodeId, mode, condition, metadata, retryPolicy, errorStrategy);
    }
    
    /**
     * Set error handling strategy
     */
    public Edge onError(ErrorStrategy errorStrategy) {
        return new Edge(targetNodeId, mode, condition, metadata, retryPolicy, errorStrategy);
    }
    
    /**
     * Set target node (internal use)
     */
    public Edge withTarget(String targetNodeId) {
        return new Edge(targetNodeId, mode, condition, metadata, retryPolicy, errorStrategy);
    }
    
    // ===== Getters =====
    
    public String getTargetNodeId() {
        return targetNodeId;
    }
    
    public ExecutionMode getMode() {
        return mode;
    }
    
    public Map<String, Object> getMetadata() {
        return Map.copyOf(metadata);
    }
    
    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }
    
    public ErrorStrategy getErrorStrategy() {
        return errorStrategy;
    }
    
    /**
     * Check if this edge should be traversed given the current context
     */
    public boolean shouldTraverse(ExecutionContext context) {
        return condition.test(context);
    }
    
    @Override
    public String toString() {
        return String.format("Edge[to=%s, mode=%s]", targetNodeId, mode);
    }
}
