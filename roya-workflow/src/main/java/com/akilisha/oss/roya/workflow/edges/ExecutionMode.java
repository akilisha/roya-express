package com.akilisha.oss.roya.workflow.edges;

/**
 * Defines how a node should be executed relative to others
 */
public enum ExecutionMode {
    /**
     * Execute sequentially - wait for previous node to complete
     */
    SEQUENTIAL,

    /**
     * Execute in parallel - fork execution
     */
    PARALLEL,

    /**
     * Execute asynchronously - fire and forget
     */
    ASYNC
}
