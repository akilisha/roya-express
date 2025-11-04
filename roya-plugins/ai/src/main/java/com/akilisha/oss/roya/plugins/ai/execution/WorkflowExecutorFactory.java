package com.akilisha.oss.roya.plugins.ai.execution;

import com.akilisha.oss.roya.plugins.ai.chaining.WorkflowChainRegistry;
import com.akilisha.oss.roya.plugins.ai.workflow.WorkflowRegistry;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;

import java.util.Optional;

/**
 * Helper for creating WorkflowExecutor instances with automatic listener attachment.
 * 
 * Automatically attaches WorkflowCompletionListener for WorkflowTrigger chaining
 * when workflows are executed.
 */
public class WorkflowExecutorFactory {
    
    /**
     * Create a WorkflowExecutor with automatic listener attachment.
     * 
     * If the workflow has registered chain listeners (for WorkflowTrigger),
     * they are automatically attached.
     * 
     * @param workflow Workflow to execute
     * @return WorkflowExecutor with listeners attached
     */
    public static WorkflowExecutor create(Workflow workflow) {
        WorkflowExecutor executor = new WorkflowExecutor(workflow);
        
        // Attach chain listener if registered (for WorkflowTrigger chaining)
        Optional<String> workflowName = WorkflowRegistry.getInstance()
            .getWorkflowName(workflow);
        if (workflowName.isPresent()) {
            WorkflowChainRegistry.WorkflowCompletionListener listener = 
                WorkflowChainRegistry.getInstance().getListener(workflowName.get());
            if (listener != null) {
                executor.addVisitor(listener);
            }
        }
        
        return executor;
    }
}

