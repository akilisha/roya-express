package com.akilisha.oss.roya.workflow.core;

/**
 * Status of a node execution
 */
public enum NodeStatus {
    /**
     * Node executed successfully
     */
    SUCCESS,
    
    /**
     * Node execution failed
     */
    FAILURE,
    
    /**
     * Node execution was skipped
     */
    SKIP
}
