package com.akilisha.oss.roya.workflow.hitm;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Provider for human approval/input in workflows.
 * Implementations can use webhooks, polling, UI callbacks, etc.
 */
public interface ApprovalProvider {

    /**
     * Request approval for a decision
     *
     * @param requestId Unique identifier for this approval request
     * @param prompt The question or decision to present to the human
     * @param context Additional context data for the human
     * @return Future that completes when human responds (true = approved, false = rejected)
     */
    CompletableFuture<Boolean> requestApproval(String requestId, String prompt, Map<String, Object> context);

    /**
     * Request input from a human
     *
     * @param requestId Unique identifier for this input request
     * @param prompt The question to ask the human
     * @param context Additional context data
     * @return Future that completes with the human's input
     */
    CompletableFuture<String> requestInput(String requestId, String prompt, Map<String, Object> context);

    /**
     * Request a choice from predefined options
     *
     * @param requestId Unique identifier for this choice request
     * @param prompt The question to ask
     * @param options Available options
     * @param context Additional context data
     * @return Future that completes with the selected option index
     */
    CompletableFuture<Integer> requestChoice(
        String requestId,
        String prompt,
        String[] options,
        Map<String, Object> context
    );

    /**
     * Cancel a pending request
     */
    void cancelRequest(String requestId);
}
