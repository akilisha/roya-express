package com.akilisha.oss.roya.plugins.ai.nodes.triggers;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.concurrent.CompletableFuture;

/**
 * Application event trigger node - triggers on internal application events.
 * 
 * TODO: Integrate with application event bus/event system
 * TODO: Support event filtering by type, source, etc.
 * TODO: Support event subscription/registration
 * TODO: Handle event payload passing
 * TODO: Support async event processing
 * TODO: Integrate with Roya's event system if one exists
 * 
 * Example usage (when implemented):
 * <pre>
 * ai.workflow("event-handler")
 *     .trigger("app-event", AppEventTrigger.create("user.created"))
 *     .llm("process", builder -> builder.systemPrompt("..."))
 *     .edge("app-event", "process")
 *     .build();
 * </pre>
 */
public class AppEventTrigger implements WorkflowNode {
    
    private final String eventType;
    private final String eventSource;
    
    private AppEventTrigger(String eventType, String eventSource) {
        this.eventType = eventType;
        this.eventSource = eventSource;
    }
    
    /**
     * Create an application event trigger.
     * 
     * @param eventType Event type name (e.g., "user.created", "order.placed")
     * @return AppEventTrigger instance
     */
    public static AppEventTrigger create(String eventType) {
        return new AppEventTrigger(eventType, null);
    }
    
    /**
     * Create an application event trigger with source filter.
     */
    public static AppEventTrigger create(String eventType, String eventSource) {
        return new AppEventTrigger(eventType, eventSource);
    }
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        // When event fired, pass event data
        return CompletableFuture.completedFuture(
            NodeOutput.success(input.data())
        );
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public String getEventSource() {
        return eventSource;
    }
}

