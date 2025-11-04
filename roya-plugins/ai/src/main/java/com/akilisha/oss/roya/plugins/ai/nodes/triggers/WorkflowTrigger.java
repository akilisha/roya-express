package com.akilisha.oss.roya.plugins.ai.nodes.triggers;

import com.akilisha.oss.roya.plugins.ai.chaining.WorkflowChainRegistry;
import com.akilisha.oss.roya.plugins.ai.workflow.WorkflowRegistry;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.concurrent.CompletableFuture;

/**
 * Workflow trigger node - triggers when another workflow completes.
 * 
 * <p><b>⚠️ DEPRECATED: MOVING TO WORKFLOW LIBRARY</b></p>
 * <p>This functionality is being moved to the core workflow library as a first-class feature.
 * This implementation is kept temporarily for reference but should not be used.</p>
 * 
 * This allows chaining workflows together, where one workflow can trigger another
 * via event subscription. Parent workflow completion event triggers child workflow.
 * 
 * Characteristics:
 * - Event-driven (pub/sub pattern)
 * - Separate workflows (loosely coupled)
 * - Asynchronous (parent completes, child starts later)
 * 
 * Example usage:
 * <pre>
 * Workflow parent = ai.workflow("parent-workflow")
 *     .trigger("cron", CronJobTrigger.create("0 9 * * *"))
 *     .llm("process", builder -> builder.outputKey("result"))
 *     .build();
 * 
 * Workflow child = ai.workflow("child-workflow")
 *     .trigger("parent-complete", WorkflowTrigger.onSuccess(parent))
 *     .llm("followup", builder -> builder
 *         .inputKey("result")  // Receives parent's result
 *         .systemPrompt("Process: {{result}}")
 *     )
 *     .edge("parent-complete", "followup")
 *     .build();
 * </pre>
 * 
 * Note: For nested execution within a single workflow, use ForkNode instead.
 */
public class WorkflowTrigger implements WorkflowNode {
    
    private final Workflow triggerWorkflow;
    private final String triggerWorkflowName;
    private final boolean onSuccess;
    private final boolean onFailure;
    private String workflowName; // Set during workflow build (child workflow name)
    private String triggerNodeId; // Set during workflow build
    
    private WorkflowTrigger(Workflow triggerWorkflow, String triggerWorkflowName, 
                           boolean onSuccess, boolean onFailure) {
        this.triggerWorkflow = triggerWorkflow;
        this.triggerWorkflowName = triggerWorkflowName;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }
    
    /**
     * Create a workflow trigger that fires when the workflow completes (success or failure).
     */
    public static WorkflowTrigger create(Workflow workflow) {
        // Look up workflow name, or use "unknown" if not registered yet
        String workflowName = WorkflowRegistry.getInstance()
            .getWorkflowName(workflow)
            .orElse("unknown");
        return new WorkflowTrigger(workflow, workflowName, true, true);
    }
    
    /**
     * Create a workflow trigger that fires only on success.
     */
    public static WorkflowTrigger onSuccess(Workflow workflow) {
        String workflowName = WorkflowRegistry.getInstance()
            .getWorkflowName(workflow)
            .orElse("unknown");
        return new WorkflowTrigger(workflow, workflowName, true, false);
    }
    
    /**
     * Create a workflow trigger that fires only on failure.
     */
    public static WorkflowTrigger onFailure(Workflow workflow) {
        String workflowName = WorkflowRegistry.getInstance()
            .getWorkflowName(workflow)
            .orElse("unknown");
        return new WorkflowTrigger(workflow, workflowName, false, true);
    }
    
    /**
     * Create a workflow trigger with explicit workflow name.
     * Use this when the workflow is not yet registered.
     */
    public static WorkflowTrigger create(Workflow workflow, String workflowName) {
        return new WorkflowTrigger(workflow, workflowName, true, true);
    }
    
    /**
     * Create a workflow trigger that fires only on success, with explicit workflow name.
     */
    public static WorkflowTrigger onSuccess(Workflow workflow, String workflowName) {
        return new WorkflowTrigger(workflow, workflowName, true, false);
    }
    
    /**
     * Create a workflow trigger that fires only on failure, with explicit workflow name.
     */
    public static WorkflowTrigger onFailure(Workflow workflow, String workflowName) {
        return new WorkflowTrigger(workflow, workflowName, false, true);
    }
    
    /**
     * Set workflow metadata (called during workflow build).
     */
    public void setWorkflowMetadata(String workflowName, String triggerNodeId) {
        this.workflowName = workflowName;
        this.triggerNodeId = triggerNodeId;
    }
    
    /**
     * Register this workflow chain with the registry (called during workflow build).
     */
    public void register(Workflow workflow) {
        if (workflowName == null || triggerNodeId == null) {
            throw new IllegalStateException("WorkflowTrigger must be set with workflow metadata before registration");
        }
        
        // Update workflow name if it was "unknown" and workflow is now registered
        String parentWorkflowName = triggerWorkflowName;
        if ("unknown".equals(parentWorkflowName)) {
            parentWorkflowName = WorkflowRegistry.getInstance()
                .getWorkflowName(triggerWorkflow)
                .orElse(parentWorkflowName);
        }
        
        // Generate unique chain ID
        String chainId = generateChainId(workflowName, triggerNodeId);
        
        WorkflowChainRegistry.ChainRegistration registration = 
            new WorkflowChainRegistry.ChainRegistration(
                chainId,
                triggerWorkflow,
                parentWorkflowName,
                workflow,
                workflowName,
                triggerNodeId,
                this
            );
        
        WorkflowChainRegistry.getInstance().register(registration);
        
        // Attach listener to parent workflow executor (if not already attached)
        // This will be done automatically when parent workflow is executed
    }
    
    /**
     * Generate a unique chain ID.
     */
    private String generateChainId(String childWorkflowName, String triggerNodeId) {
        return triggerWorkflowName + "→" + childWorkflowName + ":" + triggerNodeId;
    }
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        // When triggered by parent workflow completion, pass through parent's result data
        // Parent's data is already in input.data() (added by WorkflowChainRegistry)
        return CompletableFuture.completedFuture(
            NodeOutput.success(input.data())
        );
    }
    
    public Workflow getTriggerWorkflow() {
        return triggerWorkflow;
    }
    
    public String getTriggerWorkflowName() {
        return triggerWorkflowName;
    }
    
    public boolean isOnSuccess() {
        return onSuccess;
    }
    
    public boolean isOnFailure() {
        return onFailure;
    }
}

