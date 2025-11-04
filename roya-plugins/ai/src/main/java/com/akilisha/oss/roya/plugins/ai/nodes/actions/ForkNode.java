package com.akilisha.oss.roya.plugins.ai.nodes.actions;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.chaining.WorkflowChainRegistry;
import com.akilisha.oss.roya.plugins.ai.workflow.WorkflowRegistry;
import com.akilisha.oss.roya.plugins.ai.execution.WorkflowExecutorFactory;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Fork node - executes a child workflow within parent workflow context.
 * 
 * This enables nested workflow execution where:
 * - Parent workflow spawns child workflow
 * - Parent waits for child completion
 * - Child's result flows back to parent
 * - Parent can continue execution after child completes
 * 
 * Supports parallel execution when multiple fork nodes are connected with parallel edges.
 * 
 * Example:
 * <pre>
 * Workflow child = ai.workflow("child")
 *     .trigger("start", ManualTrigger.create())
 *     .llm("process", builder -> builder.outputKey("result"))
 *     .build();
 * 
 * Workflow parent = ai.workflow("parent")
 *     .trigger("start", ManualTrigger.create())
 *     .fork("child", child, builder -> builder
 *         .inputKey("data")  // Pass parent's data to child
 *         .outputKey("childResult")  // Get child's result back
 *     )
 *     .llm("finalize", builder -> builder.inputKey("childResult"))
 *     .edge("start", "child")
 *     .edge("child", "finalize")
 *     .build();
 * </pre>
 */
public class ForkNode implements WorkflowNode {
    
    private final Workflow childWorkflow;
    private final String inputKey;  // Key from parent context to pass to child
    private final String outputKey;  // Key in parent context to store child result
    private final String childTriggerNodeId;  // Which trigger node to start child from
    
    private ForkNode(Workflow childWorkflow, String inputKey, String outputKey, String childTriggerNodeId) {
        this.childWorkflow = childWorkflow;
        this.inputKey = inputKey;
        this.outputKey = outputKey;
        this.childTriggerNodeId = childTriggerNodeId;
    }
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Prepare child workflow input data
                Map<String, Object> childInputData = new HashMap<>();
                
                if (inputKey != null && !inputKey.isEmpty()) {
                    // Extract specific key from parent context
                    Object value = input.data().get(inputKey);
                    if (value != null) {
                        childInputData.put(inputKey, value);
                    } else {
                        // If inputKey not found, pass entire parent context
                        childInputData.putAll(input.data());
                    }
                } else {
                    // No inputKey specified - pass entire parent context
                    childInputData.putAll(input.data());
                }
                
                // Execute child workflow
                WorkflowExecutor executor = WorkflowExecutorFactory.create(childWorkflow);
                
                // Start child workflow from its trigger node
                // If childTriggerNodeId not specified, use first trigger node
                String triggerNodeId = childTriggerNodeId != null ? 
                    childTriggerNodeId : 
                    childWorkflow.getTriggerNodes().stream().findFirst().orElseThrow(
                        () -> new IllegalStateException("Child workflow has no trigger nodes")
                    );
                
                WorkflowResult childResult = executor.executeFrom(triggerNodeId, childInputData).join();
                
                // Extract child result
                Map<String, Object> childOutputData = childResult.finalOutput().data();
                
                // Prepare output for parent workflow
                Map<String, Object> parentOutput = new HashMap<>();
                parentOutput.putAll(input.data());  // Preserve parent context
                
                if (outputKey != null && !outputKey.isEmpty()) {
                    // Store child result under specific key
                    parentOutput.put(outputKey, childOutputData);
                } else {
                    // Merge child result into parent context
                    parentOutput.putAll(childOutputData);
                }
                
                // Add metadata about child execution
                parentOutput.put("_fork", Map.of(
                    "childWorkflow", childWorkflow.toString(),  // Workflow doesn't have getName() yet
                    "childSuccess", childResult.isSuccess(),
                    "childExecutionId", childResult.getExecutionId(),
                    "childNodesExecuted", childResult.getTrace().size()
                ));
                
                if (childResult.isSuccess()) {
                    return NodeOutput.success(parentOutput);
                } else {
                    // Child workflow failed - propagate failure or continue?
                    // For now, propagate failure
                    String error = childResult.finalOutput().error()
                        .orElse("Child workflow execution failed");
                    return NodeOutput.failure("Child workflow failed: " + error);
                }
                
            } catch (Exception e) {
                return NodeOutput.failure("Fork execution failed: " + e.getMessage());
            }
        });
    }
    
    public Workflow getChildWorkflow() {
        return childWorkflow;
    }
    
    public String getInputKey() {
        return inputKey;
    }
    
    public String getOutputKey() {
        return outputKey;
    }
    
    public String getChildTriggerNodeId() {
        return childTriggerNodeId;
    }
    
    /**
     * Builder for ForkNode configuration.
     */
    public static class Builder {
        private final Workflow childWorkflow;
        private String inputKey;
        private String outputKey;
        private String childTriggerNodeId;
        
        public Builder(Workflow childWorkflow) {
            this.childWorkflow = childWorkflow;
        }
        
        /**
         * Specify which key from parent context to pass to child.
         * If not specified, entire parent context is passed.
         */
        public Builder inputKey(String inputKey) {
            this.inputKey = inputKey;
            return this;
        }
        
        /**
         * Specify which key in parent context to store child result.
         * If not specified, child result is merged into parent context.
         */
        public Builder outputKey(String outputKey) {
            this.outputKey = outputKey;
            return this;
        }
        
        /**
         * Specify which trigger node to start child workflow from.
         * If not specified, first trigger node is used.
         */
        public Builder childTriggerNodeId(String childTriggerNodeId) {
            this.childTriggerNodeId = childTriggerNodeId;
            return this;
        }
        
        /**
         * Build the ForkNode instance.
         */
        public ForkNode build() {
            return new ForkNode(childWorkflow, inputKey, outputKey, childTriggerNodeId);
        }
    }
    
    /**
     * Create a builder for ForkNode.
     */
    public static Builder builder(Workflow childWorkflow) {
        return new Builder(childWorkflow);
    }
}

