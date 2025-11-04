package com.akilisha.oss.roya.workflow.hitl;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Workflow node that pauses execution and waits for human approval or input.
 * Essential for AI agent workflows that require human oversight.
 */
public class HumanApprovalNode implements WorkflowNode {
    
    private final ApprovalProvider provider;
    private final String prompt;
    private final ApprovalType type;
    private final String[] options; // For choice type
    
    /**
     * Create an approval node (yes/no decision)
     */
    public HumanApprovalNode(ApprovalProvider provider, String prompt) {
        this(provider, prompt, ApprovalType.BOOLEAN, null);
    }
    
    /**
     * Create an input node (free text input)
     */
    public static HumanApprovalNode forInput(ApprovalProvider provider, String prompt) {
        return new HumanApprovalNode(provider, prompt, ApprovalType.TEXT, null);
    }
    
    /**
     * Create a choice node (select from options)
     */
    public static HumanApprovalNode forChoice(ApprovalProvider provider, String prompt, String... options) {
        return new HumanApprovalNode(provider, prompt, ApprovalType.CHOICE, options);
    }
    
    private HumanApprovalNode(ApprovalProvider provider, String prompt, ApprovalType type, String[] options) {
        this.provider = provider;
        this.prompt = prompt;
        this.type = type;
        this.options = options;
    }
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        // Generate unique request ID
        String requestId = UUID.randomUUID().toString();
        
        // Get context for human to see
        Map<String, Object> context = input.context().snapshot();
        
        // Request based on type
        return switch (type) {
            case BOOLEAN -> requestApproval(requestId, context);
            case TEXT -> requestInput(requestId, context);
            case CHOICE -> requestChoice(requestId, context);
        };
    }
    
    private CompletableFuture<NodeOutput> requestApproval(String requestId, Map<String, Object> context) {
        return provider.requestApproval(requestId, prompt, context)
            .thenApply(approved -> {
                if (approved) {
                    return NodeOutput.success(Map.of(
                        "approved", true,
                        "requestId", requestId
                    ));
                } else {
                    return NodeOutput.failure("Human rejected the request");
                }
            })
            .exceptionally(ex -> 
                NodeOutput.failure("Approval request failed: " + ex.getMessage())
            );
    }
    
    private CompletableFuture<NodeOutput> requestInput(String requestId, Map<String, Object> context) {
        return provider.requestInput(requestId, prompt, context)
            .thenApply(input -> 
                NodeOutput.success(Map.of(
                    "humanInput", input,
                    "requestId", requestId
                ))
            )
            .exceptionally(ex -> 
                NodeOutput.failure("Input request failed: " + ex.getMessage())
            );
    }
    
    private CompletableFuture<NodeOutput> requestChoice(String requestId, Map<String, Object> context) {
        return provider.requestChoice(requestId, prompt, options, context)
            .thenApply(choiceIndex -> 
                NodeOutput.success(Map.of(
                    "choiceIndex", choiceIndex,
                    "choiceValue", options[choiceIndex],
                    "requestId", requestId
                ))
            )
            .exceptionally(ex -> 
                NodeOutput.failure("Choice request failed: " + ex.getMessage())
            );
    }
    
    /**
     * Type of human interaction
     */
    public enum ApprovalType {
        BOOLEAN,  // Yes/No approval
        TEXT,     // Free text input
        CHOICE    // Select from options
    }
    
    public ApprovalProvider getProvider() {
        return provider;
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public ApprovalType getType() {
        return type;
    }
    
    public String[] getOptions() {
        return options;
    }
}
