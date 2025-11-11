package com.akilisha.oss.roya.workflow.core;

import java.util.Map;
import java.util.Optional;

/**
 * Output from a workflow node execution.
 * Contains the result data, execution status, and optional error message.
 */
public record NodeOutput(
    Map<String, Object> data,
    NodeStatus status,
    Optional<String> error
) {
    
    /**
     * Create a successful node output
     */
    public static NodeOutput success(Map<String, Object> data) {
        return new NodeOutput(new java.util.HashMap<>(data), NodeStatus.SUCCESS, Optional.empty());
    }
    
    /**
     * Create a successful output with single key-value pair
     */
    public static NodeOutput success(String key, Object value) {
        return success(Map.of(key, value));
    }
    
    /**
     * Create a successful output with no data
     */
    public static NodeOutput success() {
        return success(Map.of());
    }
    
    /**
     * Create a failure output
     */
    public static NodeOutput failure(String error) {
        return new NodeOutput(Map.of(), NodeStatus.FAILURE, Optional.of(error));
    }
    
    /**
     * Create a failure output with exception
     */
    public static NodeOutput failure(Throwable throwable) {
        return failure(throwable.getMessage() != null ? 
            throwable.getMessage() : throwable.getClass().getSimpleName());
    }
    
    /**
     * Create a skip output
     */
    public static NodeOutput skip(String reason) {
        return new NodeOutput(Map.of(), NodeStatus.SKIP, Optional.of(reason));
    }
    
    /**
     * Check if execution was successful
     */
    public boolean isSuccess() {
        return status == NodeStatus.SUCCESS;
    }
    
    /**
     * Check if execution failed
     */
    public boolean isFailure() {
        return status == NodeStatus.FAILURE;
    }
    
    /**
     * Check if execution was skipped
     */
    public boolean isSkip() {
        return status == NodeStatus.SKIP;
    }
}
