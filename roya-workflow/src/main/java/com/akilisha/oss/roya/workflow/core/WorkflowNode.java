package com.akilisha.oss.roya.workflow.core;

import java.util.concurrent.CompletableFuture;

/**
 * Core abstraction for a workflow node.
 * Each node represents a unit of work in the workflow graph.
 */
@FunctionalInterface
public interface WorkflowNode {

    /**
     * Execute this node with the given input.
     *
     * @param input The input data and execution context
     * @return A future that completes with the node's output
     */
    CompletableFuture<NodeOutput> execute(NodeInput input);
}
