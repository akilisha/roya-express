package com.akilisha.oss.roya.workflow.execution;

import com.akilisha.oss.roya.workflow.core.ExecutionContext;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.NodeStatus;

import java.util.List;

/**
 * Result of a workflow execution
 */
public record WorkflowResult(
    NodeOutput finalOutput,
    ExecutionContext context
) {
    
    /**
     * Check if workflow completed successfully
     */
    public boolean isSuccess() {
        return finalOutput.status() == NodeStatus.SUCCESS;
    }
    
    /**
     * Check if workflow failed
     */
    public boolean isFailure() {
        return finalOutput.status() == NodeStatus.FAILURE;
    }
    
    /**
     * Get execution trace
     */
    public List<ExecutionEvent> getTrace() {
        return context.getTrace();
    }
    
    /**
     * Get workflow execution ID
     */
    public String getExecutionId() {
        return context.getExecutionId();
    }
    
    @Override
    public String toString() {
        return String.format("WorkflowResult[success=%s, nodes=%d, elapsed=%s]",
            isSuccess(), context.getExecutedNodes().size(), context.getElapsedTime());
    }
}
