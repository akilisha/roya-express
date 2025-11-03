package com.akilisha.oss.roya.plugins.ai.nodes.triggers;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Custom trigger node - allows developers to implement their own trigger logic.
 * 
 * This is a flexible trigger that accepts a custom function to determine when to trigger.
 * Use this when none of the built-in triggers fit your needs.
 * 
 * Example usage:
 * <pre>
 * ai.workflow("custom-trigger")
 *     .trigger("custom", CustomTrigger.create(
 *         () -> {
 *             // Custom logic to determine if workflow should trigger
 *             return checkDatabase() || checkExternalAPI();
 *         }
 *     ))
 *     .llm("process", builder -> builder.systemPrompt("..."))
 *     .edge("custom", "process")
 *     .build();
 * </pre>
 */
public class CustomTrigger implements WorkflowNode {
    
    private final Supplier<Boolean> triggerCondition;
    private final Runnable onTrigger;
    
    private CustomTrigger(Supplier<Boolean> triggerCondition, Runnable onTrigger) {
        this.triggerCondition = triggerCondition;
        this.onTrigger = onTrigger;
    }
    
    /**
     * Create a custom trigger with a condition function.
     * 
     * @param triggerCondition Function that returns true when workflow should trigger
     * @return CustomTrigger instance
     */
    public static CustomTrigger create(Supplier<Boolean> triggerCondition) {
        return new CustomTrigger(triggerCondition, null);
    }
    
    /**
     * Create a custom trigger with condition and callback.
     * 
     * @param triggerCondition Function that returns true when workflow should trigger
     * @param onTrigger Optional callback to execute when triggered
     * @return CustomTrigger instance
     */
    public static CustomTrigger create(Supplier<Boolean> triggerCondition, Runnable onTrigger) {
        return new CustomTrigger(triggerCondition, onTrigger);
    }
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        // Check condition and trigger if true
        if (triggerCondition != null && triggerCondition.get()) {
            if (onTrigger != null) {
                onTrigger.run();
            }
            return CompletableFuture.completedFuture(
                NodeOutput.success(input.data())
            );
        }
        
        // Condition not met, return empty output (workflow won't proceed)
        return CompletableFuture.completedFuture(
            NodeOutput.success()
        );
    }
    
    public Supplier<Boolean> getTriggerCondition() {
        return triggerCondition;
    }
}

