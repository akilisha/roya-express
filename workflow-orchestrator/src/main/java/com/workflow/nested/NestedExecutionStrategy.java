package com.akilisha.oss.roya.workflow.nested;

/**
 * Strategy for executing nested child workflows
 */
public enum NestedExecutionStrategy {
    /**
     * Wait for all children to complete successfully.
     * Fail immediately if any child fails.
     */
    WAIT_FOR_ALL,
    
    /**
     * Wait for all children to complete (success or failure).
     * Continue even if some children fail.
     */
    WAIT_FOR_ALL_BEST_EFFORT,
    
    /**
     * Return as soon as the first child completes successfully.
     * Fail if all children fail.
     */
    FIRST_SUCCESS,
    
    /**
     * Wait for all children and select the best result based on aggregator logic.
     */
    BEST_OF_ALL
}
