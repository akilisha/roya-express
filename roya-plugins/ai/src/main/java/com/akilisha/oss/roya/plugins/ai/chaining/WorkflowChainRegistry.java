package com.akilisha.oss.roya.plugins.ai.chaining;

import com.akilisha.oss.roya.plugins.ai.nodes.triggers.WorkflowTrigger;
import com.akilisha.oss.roya.plugins.ai.workflow.WorkflowRegistry;
import com.akilisha.oss.roya.plugins.ai.execution.WorkflowExecutorFactory;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import com.akilisha.oss.roya.workflow.visitor.WorkflowVisitor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry for workflow chaining via WorkflowTrigger.
 * 
 * Manages event subscriptions where child workflows are triggered when parent workflows complete.
 * Uses WorkflowVisitor pattern to listen for workflow completion events.
 */
public class WorkflowChainRegistry {
    
    private static final WorkflowChainRegistry INSTANCE = new WorkflowChainRegistry();
    
    /**
     * Chain registration - child workflow triggered by parent completion.
     */
    public record ChainRegistration(
        String chainId,
        Workflow parentWorkflow,
        String parentWorkflowName,
        Workflow childWorkflow,
        String childWorkflowName,
        String childTriggerNodeId,
        WorkflowTrigger trigger
    ) {}
    
    private final Map<String, ChainRegistration> registrations = new ConcurrentHashMap<>();
    private final Map<String, WorkflowCompletionListener> listeners = new ConcurrentHashMap<>();
    private boolean initialized = false;
    
    private WorkflowChainRegistry() {
        // Singleton
    }
    
    /**
     * Get the singleton instance.
     */
    public static WorkflowChainRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize the registry.
     * Should be called once during application startup.
     */
    public synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        initialized = true;
        System.out.println("✓ WorkflowChainRegistry initialized");
    }
    
    /**
     * Register a workflow chain (child workflow triggered by parent completion).
     * 
     * @param registration Chain registration information
     */
    public void register(ChainRegistration registration) {
        if (!initialized) {
            initialize();
        }
        
        String chainId = registration.chainId();
        registrations.put(chainId, registration);
        
        // Create or get listener for parent workflow
        String parentWorkflowName = registration.parentWorkflowName();
        WorkflowCompletionListener listener = listeners.computeIfAbsent(
            parentWorkflowName,
            k -> new WorkflowCompletionListener(parentWorkflowName)
        );
        
        // Register this chain with the listener
        listener.addChain(registration);
        
        System.out.println("✓ Registered workflow chain: " + chainId + 
            " (parent: " + parentWorkflowName + " → child: " + registration.childWorkflowName() + ")");
    }
    
    /**
     * Unregister a workflow chain.
     */
    public void unregister(String chainId) {
        ChainRegistration registration = registrations.remove(chainId);
        if (registration != null) {
            String parentWorkflowName = registration.parentWorkflowName();
            WorkflowCompletionListener listener = listeners.get(parentWorkflowName);
            if (listener != null) {
                listener.removeChain(chainId);
                if (listener.isEmpty()) {
                    listeners.remove(parentWorkflowName);
                }
            }
            System.out.println("✓ Unregistered workflow chain: " + chainId);
        }
    }
    
    /**
     * Get listener for a parent workflow (for attaching to executor).
     */
    public WorkflowCompletionListener getListener(String parentWorkflowName) {
        return listeners.get(parentWorkflowName);
    }
    
    /**
     * Workflow completion listener that triggers child workflows.
     */
    public static class WorkflowCompletionListener implements WorkflowVisitor {
        private final String parentWorkflowName;
        private final Map<String, ChainRegistration> chains = new ConcurrentHashMap<>();
        
        public WorkflowCompletionListener(String parentWorkflowName) {
            this.parentWorkflowName = parentWorkflowName;
        }
        
        public void addChain(ChainRegistration registration) {
            chains.put(registration.chainId(), registration);
        }
        
        public void removeChain(String chainId) {
            chains.remove(chainId);
        }
        
        public boolean isEmpty() {
            return chains.isEmpty();
        }
        
        @Override
        public void onWorkflowComplete(String workflowId, WorkflowResult result) {
            // workflowId is the execution ID, not workflow name
            // We need to match workflows by checking if they're registered for this parent
            // Since we're listening for a specific parent workflow, we can check the workflow instance
            
            System.out.println("🔍 WorkflowCompletionListener: Workflow completed - " + 
                "executionId: " + workflowId + ", parentWorkflowName: " + parentWorkflowName);
            
            // Check if any chain matches this workflow
            boolean isOurParent = chains.values().stream()
                .anyMatch(chain -> {
                    // Check if the completed workflow matches our parent workflow
                    String parentName = chain.parentWorkflowName();
                    return parentName != null && parentName.equals(parentWorkflowName);
                });
            
            if (!isOurParent) {
                System.out.println("   ⏭️  Not our parent workflow, skipping");
                return;
            }
            
            System.out.println("✅ Parent workflow completed! Triggering child workflows...");
            
            // Trigger all registered child workflows
            chains.values().forEach(chain -> {
                try {
                    // Check if trigger condition matches
                    WorkflowTrigger trigger = chain.trigger();
                    boolean shouldTrigger = false;
                    
                    if (result.isSuccess() && trigger.isOnSuccess()) {
                        shouldTrigger = true;
                    } else if (result.isFailure() && trigger.isOnFailure()) {
                        shouldTrigger = true;
                    }
                    
                    if (!shouldTrigger) {
                        System.out.println("   ⏭️  Trigger condition not met for chain: " + chain.chainId());
                        return;
                    }
                    
                    System.out.println("✅ Triggering child workflow: " + chain.childWorkflowName());
                    
                    // Execute child workflow with parent's result data
                    WorkflowExecutor executor = WorkflowExecutorFactory.create(chain.childWorkflow());
                    
                    // Pass parent's final output data to child
                    Map<String, Object> childInputData = result.finalOutput().data();
                    
                    // Add metadata about parent execution
                    childInputData.put("_parent", Map.of(
                        "parentWorkflow", chain.parentWorkflowName(),
                        "parentExecutionId", result.getExecutionId(),
                        "parentSuccess", result.isSuccess()
                    ));
                    
                    CompletableFuture<WorkflowResult> childFuture = executor.executeFrom(
                        chain.childTriggerNodeId(),
                        childInputData
                    );
                    
                    childFuture.whenComplete((childResult, ex) -> {
                        if (ex != null) {
                            System.err.println("✗ Child workflow execution error: " + 
                                chain.childWorkflowName() + " - " + ex.getMessage());
                        } else if (childResult.isSuccess()) {
                            System.out.println("✓ Child workflow executed successfully: " + 
                                chain.childWorkflowName() + " (triggered by: " + parentWorkflowName + ")");
                        } else {
                            System.err.println("✗ Child workflow execution failed: " + 
                                chain.childWorkflowName() + " (triggered by: " + parentWorkflowName + ")");
                        }
                    });
                    
                } catch (Exception e) {
                    System.err.println("✗ Error triggering child workflow: " + 
                        chain.childWorkflowName() + " - " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }
}

