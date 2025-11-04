package com.akilisha.oss.roya.workflow.continuation;

import com.akilisha.oss.roya.workflow.core.ExecutionContext;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * A node that executes another workflow as a continuation.
 * The parent workflow's context is passed as input to the child workflow,
 * and the child's results can be merged back into the parent context.
 * 
 * This enables sequential workflow composition where workflows are chained together.
 */
public class ContinuationNode implements WorkflowNode {
    
    private final Workflow childWorkflow;
    private final String startNodeId;
    private final boolean mergeContext;
    private final String namespace;
    
    /**
     * Create a continuation node with context merging enabled
     */
    public ContinuationNode(Workflow childWorkflow, String startNodeId) {
        this(childWorkflow, startNodeId, true, null);
    }
    
    /**
     * Create a continuation node with optional context merging
     */
    public ContinuationNode(Workflow childWorkflow, String startNodeId, boolean mergeContext) {
        this(childWorkflow, startNodeId, mergeContext, null);
    }
    
    /**
     * Create a continuation node with namespace for context isolation
     * 
     * @param childWorkflow The workflow to execute
     * @param startNodeId The starting node ID in the child workflow
     * @param mergeContext Whether to merge child context back to parent
     * @param namespace Optional namespace prefix for child context keys
     */
    public ContinuationNode(Workflow childWorkflow, String startNodeId, boolean mergeContext, String namespace) {
        this.childWorkflow = childWorkflow;
        this.startNodeId = startNodeId;
        this.mergeContext = mergeContext;
        this.namespace = namespace;
    }
    
    /**
     * Create a continuation node with namespace (context merging enabled)
     */
    public static ContinuationNode withNamespace(Workflow childWorkflow, String startNodeId, String namespace) {
        return new ContinuationNode(childWorkflow, startNodeId, true, namespace);
    }
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        // Create executor for child workflow
        WorkflowExecutor childExecutor = new WorkflowExecutor(childWorkflow);
        
        // Pass parent context as input to child
        Map<String, Object> childInput = input.context().snapshot();
        
        return childExecutor.executeFrom(startNodeId, childInput)
            .thenApply(childResult -> {
                if (childResult.isFailure()) {
                    return NodeOutput.failure(
                        "Continuation workflow failed: " + 
                        childResult.finalOutput().error().orElse("Unknown error")
                    );
                }
                
                // Optionally merge child context back into parent
                if (mergeContext) {
                    mergeChildContext(input.context(), childResult.context());
                }
                
                // Return child's final output
                return childResult.finalOutput();
            })
            .exceptionally(ex -> 
                NodeOutput.failure("Continuation workflow error: " + ex.getMessage())
            );
    }
    
    /**
     * Merge child workflow context back into parent context
     */
    private void mergeChildContext(ExecutionContext parentContext, ExecutionContext childContext) {
        Map<String, Object> childData = childContext.snapshot();
        
        if (namespace != null && !namespace.isEmpty()) {
            // Merge with namespace prefix to avoid collisions
            childData.forEach((key, value) -> 
                parentContext.set(namespace + "." + key, value)
            );
        } else {
            // Direct merge (may overwrite parent keys)
            childData.forEach(parentContext::set);
        }
    }
    
    /**
     * Get the child workflow being executed
     */
    public Workflow getChildWorkflow() {
        return childWorkflow;
    }
    
    /**
     * Get the start node ID
     */
    public String getStartNodeId() {
        return startNodeId;
    }
    
    /**
     * Check if context merging is enabled
     */
    public boolean isMergeContext() {
        return mergeContext;
    }
    
    /**
     * Get the namespace (if any)
     */
    public String getNamespace() {
        return namespace;
    }
}
