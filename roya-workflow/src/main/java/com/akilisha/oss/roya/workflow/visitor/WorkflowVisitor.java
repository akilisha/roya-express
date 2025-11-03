package com.akilisha.oss.roya.workflow.visitor;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.edges.Edge;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;

import java.time.Duration;
import java.util.Map;

/**
 * Visitor pattern for workflow observability.
 * Implementations can log, collect metrics, trace execution, etc.
 */
public interface WorkflowVisitor {

    /**
     * Called when workflow execution starts
     */
    default void onWorkflowStart(String workflowId, Map<String, Object> initialData) {}

    /**
     * Called when workflow execution completes
     */
    default void onWorkflowComplete(String workflowId, WorkflowResult result) {}

    /**
     * Called when a node starts execution
     */
    default void onNodeStart(String nodeId, NodeInput input) {}

    /**
     * Called when a node completes successfully
     */
    default void onNodeComplete(String nodeId, NodeOutput output, Duration executionTime) {}

    /**
     * Called when a node execution fails
     */
    default void onNodeError(String nodeId, Throwable error) {}

    /**
     * Called when an edge is traversed
     */
    default void onEdgeTraversal(String fromNodeId, String toNodeId, Edge edge) {}

    /**
     * Called when a node execution is being retried
     */
    default void onRetry(String nodeId, int attemptNumber, Duration delay) {}
}
