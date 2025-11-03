package com.akilisha.oss.roya.workflow.core;

/**
 * Categorizes nodes by their role in the workflow
 */
public enum NodeType {
    /**
     * Initiates workflow execution (e.g., cron, webhook, event listener)
     */
    TRIGGER,

    /**
     * Performs side effects (e.g., HTTP call, database operation, file I/O)
     */
    ACTION,

    /**
     * Branching logic based on conditions
     */
    CONDITIONAL,

    /**
     * Data transformation and routing decisions
     */
    LOGIC,

    /**
     * User-defined custom functionality
     */
    CUSTOM
}
