package com.akilisha.oss.roya.plugins.ai.nodes.actions;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIOptions;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Type-safe extraction node for structured data extraction.
 *
 * Extracts structured data from text using Java records/classes as schemas.
 *
 * Example:
 * <pre>
 * record ReceiptDetails(String vendor, Double total, String date) {}
 *
 * ExtractNode&lt;ReceiptDetails&gt; node = ExtractNode.builder(ai, ReceiptDetails.class)
 *     .systemPrompt("Extract receipt information")
 *     .inputKey("receiptText")
 *     .outputKey("details")
 *     .build();
 * </pre>
 */
public class ExtractNode<T> implements WorkflowNode {

    private final AI ai;
    private final Class<T> extractType;
    private final String systemPrompt;
    private final AIOptions options;
    private final String inputKey;
    private final String outputKey;

    // Constructor for simple cases
    public ExtractNode(AI ai, Class<T> extractType, String inputKey, String outputKey) {
        this(ai, extractType, null, AIOptions.defaults(), inputKey, outputKey);
    }

    // Full constructor
    public ExtractNode(AI ai, Class<T> extractType, String systemPrompt,
                      AIOptions options, String inputKey, String outputKey) {
        this.ai = ai;
        this.extractType = extractType;
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
                String prompt = input.getString(inputKey);
                if (prompt == null) {
                    return NodeOutput.failure("Input key '" + inputKey + "' not found or null");
                }

                // Extract structured data
                T extracted = ai.llm().extract(extractType, prompt, options);

                // Return output - also merge into global context
                input.context().set(outputKey, extracted);

                return NodeOutput.success(Map.of(outputKey, extracted));

            } catch (Exception e) {
                return NodeOutput.failure("Extraction failed: " + e.getMessage());
            }
        });
    }

    /**
     * Create a builder for fluent configuration.
     */
    public static <T> Builder<T> builder(AI ai, Class<T> extractType) {
        return new Builder<>(ai, extractType);
    }

    /**
     * Builder for ExtractNode.
     */
    public static class Builder<T> {
        private final AI ai;
        private final Class<T> extractType;
        private String systemPrompt;
        private AIOptions options = AIOptions.defaults();
        private String inputKey = "prompt";
        private String outputKey = "extracted";

        Builder(AI ai, Class<T> extractType) {
            this.ai = ai;
            this.extractType = extractType;
        }

        public Builder<T> systemPrompt(String prompt) {
            this.systemPrompt = prompt;
            return this;
        }

        public Builder<T> options(AIOptions opts) {
            this.options = opts;
            return this;
        }

        public Builder<T> inputKey(String key) {
            this.inputKey = key;
            return this;
        }

        public Builder<T> outputKey(String key) {
            this.outputKey = key;
            return this;
        }

        public ExtractNode<T> build() {
            String prompt = systemPrompt != null ? systemPrompt :
                "Extract information from the text into structured format matching " + extractType.getSimpleName();
            return new ExtractNode<>(ai, extractType, prompt, options, inputKey, outputKey);
        }
    }
}



