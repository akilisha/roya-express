package com.akilisha.oss.roya.plugins.ai.nodes;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Embedding node for text-to-vector conversion.
 *
 * Supports both single and batch embedding operations.
 *
 * Example:
 * <pre>
 * EmbeddingNode node = EmbeddingNode.builder(ai)
 *     .inputKey("text")
 *     .outputKey("embedding")
 *     .batch(false)
 *     .build();
 * </pre>
 */
public class EmbeddingNode implements WorkflowNode {

    private final AI ai;
    private final String inputKey;
    private final String outputKey;
    private final boolean batch;

    // Constructor for single embedding
    public EmbeddingNode(AI ai, String inputKey, String outputKey) {
        this(ai, inputKey, outputKey, false);
    }

    // Full constructor
    public EmbeddingNode(AI ai, String inputKey, String outputKey, boolean batch) {
        this.ai = ai;
        this.inputKey = inputKey;
        this.outputKey = outputKey;
        this.batch = batch;
    }

    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (batch) {
                    // Batch embedding
                    @SuppressWarnings("unchecked")
                    List<String> texts = (List<String>) input.get(inputKey, List.class);
                    if (texts == null) {
                        return NodeOutput.failure("Input key '" + inputKey + "' not found or null (expected List<String>)");
                    }

                    List<float[]> embeddings = ai.embeddings().embed(texts);

                    // Store in context
                    input.context().set(outputKey, embeddings);

                    return NodeOutput.success(Map.of(outputKey, embeddings));
                } else {
                    // Single embedding
                    String text = input.getString(inputKey);
                    if (text == null) {
                        return NodeOutput.failure("Input key '" + inputKey + "' not found or null (expected String)");
                    }

                    float[] embedding = ai.embeddings().embed(text);

                    // Store in context
                    input.context().set(outputKey, embedding);

                    return NodeOutput.success(Map.of(outputKey, embedding));
                }
            } catch (Exception e) {
                return NodeOutput.failure("Embedding failed: " + e.getMessage());
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
     * Builder for EmbeddingNode.
     */
    public static class Builder {
        private final AI ai;
        private String inputKey = "text";
        private String outputKey = "embedding";
        private boolean batch = false;

        Builder(AI ai) {
            this.ai = ai;
        }

        public Builder inputKey(String key) {
            this.inputKey = key;
            return this;
        }

        public Builder outputKey(String key) {
            this.outputKey = key;
            return this;
        }

        public Builder batch(boolean isBatch) {
            this.batch = isBatch;
            return this;
        }

        public EmbeddingNode build() {
            return new EmbeddingNode(ai, inputKey, outputKey, batch);
        }
    }
}



