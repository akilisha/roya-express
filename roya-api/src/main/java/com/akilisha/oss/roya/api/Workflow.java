package com.akilisha.oss.roya.api;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * A compiled workflow ready for execution.
 *
 * Workflows maintain state across nodes and support:
 * - Conditional execution
 * - Parallel execution
 * - Loop/iteration
 * - Plugin integration
 */
public interface Workflow {
    /**
     * Execute the workflow synchronously.
     *
     * @param input Initial workflow input
     * @return Final workflow result with state
     */
    WorkflowResult run(WorkflowInput input);

    /**
     * Execute the workflow asynchronously.
     *
     * @param input Initial workflow input
     * @return CompletableFuture of workflow result
     */
    CompletableFuture<WorkflowResult> runAsync(WorkflowInput input);

    /**
     * Get workflow name.
     */
    String name();

    /**
     * Visualize the workflow graph (generates diagram).
     */
    void visualize();

    /**
     * Get workflow metadata.
     */
    Map<String, Object> metadata();
}

