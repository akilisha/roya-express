package com.akilisha.oss.roya.plugins.ai.workflow;

import com.akilisha.oss.roya.workflow.core.Workflow;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for workflows by name.
 * 
 * Maps workflow names to workflow instances, enabling webhook persistence
 * to reference workflows by name and load them on application startup.
 * 
 * Usage:
 * <pre>
 * // Register workflow
 * Workflow workflow = ai.workflow("my-workflow")...build();
 * WorkflowRegistry.getInstance().register("my-workflow", workflow);
 * 
 * // Retrieve workflow
 * Workflow workflow = WorkflowRegistry.getInstance().get("my-workflow");
 * 
 * // Get workflow name from instance
 * Optional<String> name = WorkflowRegistry.getInstance().getWorkflowName(workflow);
 * </pre>
 */
public class WorkflowRegistry {
    
    private static final WorkflowRegistry INSTANCE = new WorkflowRegistry();
    
    private final Map<String, Workflow> workflows = new ConcurrentHashMap<>();
    private final Map<Workflow, String> workflowToName = new ConcurrentHashMap<>();
    
    private WorkflowRegistry() {
        // Singleton
    }
    
    /**
     * Get the singleton instance.
     */
    public static WorkflowRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register a workflow by name.
     * 
     * @param name Workflow name
     * @param workflow Workflow instance
     */
    public void register(String name, Workflow workflow) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Workflow name cannot be null or empty");
        }
        if (workflow == null) {
            throw new IllegalArgumentException("Workflow cannot be null");
        }
        workflows.put(name, workflow);
        workflowToName.put(workflow, name);
    }
    
    /**
     * Get a workflow by name.
     * 
     * @param name Workflow name
     * @return Workflow instance or null if not found
     */
    public Workflow get(String name) {
        return workflows.get(name);
    }
    
    /**
     * Get workflow name from workflow instance.
     * 
     * @param workflow Workflow instance
     * @return Optional workflow name
     */
    public Optional<String> getWorkflowName(Workflow workflow) {
        return Optional.ofNullable(workflowToName.get(workflow));
    }
    
    /**
     * Check if a workflow is registered.
     * 
     * @param name Workflow name
     * @return true if registered
     */
    public boolean exists(String name) {
        return workflows.containsKey(name);
    }
    
    /**
     * Unregister a workflow.
     * 
     * @param name Workflow name
     * @return The workflow that was removed, or null if not found
     */
    public Workflow unregister(String name) {
        Workflow workflow = workflows.remove(name);
        if (workflow != null) {
            workflowToName.remove(workflow);
        }
        return workflow;
    }
    
    /**
     * Get all registered workflow names.
     * 
     * @return Set of workflow names
     */
    public java.util.Set<String> getAllNames() {
        return java.util.Set.copyOf(workflows.keySet());
    }
    
    /**
     * Get the number of registered workflows.
     * 
     * @return Number of workflows
     */
    public int size() {
        return workflows.size();
    }
    
    /**
     * Clear all registered workflows.
     * Useful for testing.
     */
    public void clear() {
        workflows.clear();
        workflowToName.clear();
    }
}

