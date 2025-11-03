package com.akilisha.oss.roya.plugins.ai.nodes.triggers;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.concurrent.CompletableFuture;

/**
 * Workflow trigger node - triggers when another workflow completes.
 * 
 * This allows chaining workflows together, where one workflow can trigger another.
 * 
 * TODO: Implement workflow execution tracking
 * TODO: Support workflow completion event subscription
 * TODO: Support workflow result filtering (only trigger on success/failure)
 * TODO: Support workflow context/data passing between workflows
 * TODO: Handle workflow dependencies and orchestration
 * 
 * Example usage (when implemented):
 * <pre>
 * Workflow parentWorkflow = ...;
 * 
 * ai.workflow("child-workflow")
 *     .trigger("parent-complete", WorkflowTrigger.create(parentWorkflow))
 *     .llm("process", builder -> builder.systemPrompt("..."))
 *     .edge("parent-complete", "process")
 *     .build();
 * </pre>
 */
public class WorkflowTrigger implements WorkflowNode {
    
    private final Workflow triggerWorkflow;
    private final boolean onSuccess;
    private final boolean onFailure;
    
    private WorkflowTrigger(Workflow triggerWorkflow, boolean onSuccess, boolean onFailure) {
        this.triggerWorkflow = triggerWorkflow;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }
    
    /**
     * Create a workflow trigger that fires when the workflow completes (success or failure).
     */
    public static WorkflowTrigger create(Workflow workflow) {
        return new WorkflowTrigger(workflow, true, true);
    }
    
    /**
     * Create a workflow trigger that fires only on success.
     */
    public static WorkflowTrigger onSuccess(Workflow workflow) {
        return new WorkflowTrigger(workflow, true, false);
    }
    
    /**
     * Create a workflow trigger that fires only on failure.
     */
    public static WorkflowTrigger onFailure(Workflow workflow) {
        return new WorkflowTrigger(workflow, false, true);
    }
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        // When workflow completes, pass workflow result data
        return CompletableFuture.completedFuture(
            NodeOutput.success(input.data())
        );
    }
    
    public Workflow getTriggerWorkflow() {
        return triggerWorkflow;
    }
    
    public boolean isOnSuccess() {
        return onSuccess;
    }
    
    public boolean isOnFailure() {
        return onFailure;
    }
}

