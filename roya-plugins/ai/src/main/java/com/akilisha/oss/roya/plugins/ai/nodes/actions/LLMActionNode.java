package com.akilisha.oss.roya.plugins.ai.nodes.actions;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIOptions;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * LLM action node - wraps AI.llm() operations in a workflow node.
 *
 * Supports:
 * - System prompts
 * - Custom AI options (temperature, model, etc.)
 * - Configurable input/output keys
 *
 * Example:
 * <pre>
 * LLMActionNode node = LLMActionNode.builder(ai)
 *     .systemPrompt("You are a helpful assistant")
 *     .inputKey("message")
 *     .outputKey("response")
 *     .options(AIOptions.builder().temperature(0.7).build())
 *     .build();
 * </pre>
 */
public class LLMActionNode implements WorkflowNode {

    private final AI ai;
    private final String systemPrompt;
    private final AIOptions options;
    private final String inputKey;
    private final String outputKey;

    // Constructor for simple cases
    public LLMActionNode(AI ai, String systemPrompt, String inputKey, String outputKey) {
        this(ai, systemPrompt, AIOptions.defaults(), inputKey, outputKey);
    }

    // Full constructor
    public LLMActionNode(AI ai, String systemPrompt, AIOptions options,
                        String inputKey, String outputKey) {
        this.ai = ai;
        this.systemPrompt = systemPrompt;
        this.options = options != null ? options : AIOptions.defaults();
        this.inputKey = inputKey;
        this.outputKey = outputKey;
    }

    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get input from context
                String userMessage = input.getString(inputKey);
                if (userMessage == null) {
                    return NodeOutput.failure("Input key '" + inputKey + "' not found or null");
                }

                // Call LLM
                String response = ai.llm().ask(systemPrompt, userMessage, options);

                // Return output - also merge into global context for downstream nodes
                input.context().set(outputKey, response);

                return NodeOutput.success(Map.of(outputKey, response));

            } catch (Exception e) {
                return NodeOutput.failure("LLM call failed: " + e.getMessage());
            }
        });
    }

    /**
     * Create a builder for fluent configuration.
     */
    public static Builder builder(AI ai) {
        return new Builder(ai);
    }

    /**
     * Builder for LLMActionNode.
     */
    public static class Builder {
        private final AI ai;
        private String systemPrompt;
        private AIOptions options = AIOptions.defaults();
        private String inputKey = "message";
        private String outputKey = "response";

        Builder(AI ai) {
            this.ai = ai;
        }

        public Builder systemPrompt(String prompt) {
            this.systemPrompt = prompt;
            return this;
        }

        public Builder options(AIOptions opts) {
            this.options = opts;
            return this;
        }

        public Builder inputKey(String key) {
            this.inputKey = key;
            return this;
        }

        public Builder outputKey(String key) {
            this.outputKey = key;
            return this;
        }

        public LLMActionNode build() {
            if (systemPrompt == null || systemPrompt.isBlank()) {
                systemPrompt = "You are a helpful assistant";
            }
            return new LLMActionNode(ai, systemPrompt, options, inputKey, outputKey);
        }
    }
}



