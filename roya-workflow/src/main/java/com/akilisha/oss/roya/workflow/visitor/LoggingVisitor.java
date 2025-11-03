package com.akilisha.oss.roya.workflow.visitor;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.edges.Edge;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;

import java.time.Duration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Visitor that logs workflow execution events.
 * Uses Java's built-in logging (can be adapted to SLF4J, Log4j, etc.)
 */
public class LoggingVisitor implements WorkflowVisitor {

    private static final Logger logger = Logger.getLogger(LoggingVisitor.class.getName());
    private final Level level;

    public LoggingVisitor() {
        this(Level.INFO);
    }

    public LoggingVisitor(Level level) {
        this.level = level;
    }

    @Override
    public void onWorkflowStart(String workflowId, Map<String, Object> initialData) {
        logger.log(level, String.format("Workflow [%s] started with %d inputs",
            workflowId, initialData.size()));
    }

    @Override
    public void onWorkflowComplete(String workflowId, WorkflowResult result) {
        logger.log(level, String.format("Workflow [%s] completed. Success: %s, Nodes executed: %d",
            workflowId, result.isSuccess(), result.getTrace().size()));
    }

    @Override
    public void onNodeStart(String nodeId, NodeInput input) {
        logger.log(level, String.format("Node [%s] started", nodeId));
    }

    @Override
    public void onNodeComplete(String nodeId, NodeOutput output, Duration executionTime) {
        logger.log(level, String.format("Node [%s] completed in %dms with status: %s",
            nodeId, executionTime.toMillis(), output.status()));
    }

    @Override
    public void onNodeError(String nodeId, Throwable error) {
        logger.log(Level.SEVERE, String.format("Node [%s] failed: %s",
            nodeId, error.getMessage()), error);
    }

    @Override
    public void onEdgeTraversal(String fromNodeId, String toNodeId, Edge edge) {
        logger.log(Level.FINE, String.format("Edge traversal: %s -> %s (mode: %s)",
            fromNodeId, toNodeId, edge.getMode()));
    }

    @Override
    public void onRetry(String nodeId, int attemptNumber, Duration delay) {
        logger.log(Level.WARNING, String.format("Retrying node [%s]: attempt %d after %dms delay",
            nodeId, attemptNumber, delay.toMillis()));
    }
}
