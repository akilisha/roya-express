package com.akilisha.oss.roya.plugins.ai.nodes.triggers;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.concurrent.CompletableFuture;

/**
 * Manual trigger node - used when workflow is started programmatically.
 * 
 * This trigger simply passes through the input data, making it suitable
 * for workflows that are executed manually (e.g., via executor.executeFrom()).
 * 
 * Example:
 * <pre>
 * ai.workflow("my-workflow")
 *     .trigger("manual", ManualTrigger.create())
 *     .llm("process", builder -> builder.systemPrompt("..."))
 *     .edge("manual", "process")
 *     .build();
 * </pre>
 */
public class ManualTrigger implements WorkflowNode {
    
    private ManualTrigger() {
        // Singleton pattern - use create()
    }
    
    /**
     * Create a manual trigger instance.
     */
    public static ManualTrigger create() {
        return new ManualTrigger();
    }
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        // Simply pass through the input data
        return CompletableFuture.completedFuture(
            NodeOutput.success(input.data())
        );
    }
}

