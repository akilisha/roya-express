package com.akilisha.oss.roya.workflow.edges;

/**
 * Defines how errors should be handled when a node fails
 */
public enum ErrorStrategy {
    /**
     * Propagate the error - fail the entire workflow
     */
    PROPAGATE,

    /**
     * Skip the failed node and continue workflow execution
     */
    SKIP_AND_CONTINUE,

    /**
     * Execute a fallback node specified in edge metadata
     */
    USE_FALLBACK,

    /**
     * Execute compensation logic to undo previous operations
     */
    COMPENSATE
}
