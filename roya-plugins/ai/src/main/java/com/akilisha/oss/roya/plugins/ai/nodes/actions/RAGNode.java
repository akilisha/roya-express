package com.akilisha.oss.roya.plugins.ai.nodes.actions;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.RAGOptions;
import com.akilisha.oss.roya.plugins.ai.RAGResponse;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * RAG (Retrieval-Augmented Generation) node for workflows.
 * 
 * Performs RAG queries using Qdrant for retrieval and LLM for generation.
 * 
 * Example:
 * <pre>
 * RAGNode node = RAGNode.builder(ai)
 *     .inputKey("question")
 *     .outputKey("answer")
 *     .options(RAGOptions.builder().topK(5).build())
 *     .build();
 * </pre>
 */
public class RAGNode implements WorkflowNode {
    
    private final AI ai;
    private final String inputKey;
    private final String outputKey;
    private final RAGOptions options;

    public RAGNode(AI ai, String inputKey, String outputKey, RAGOptions options) {
        this.ai = ai;
        this.inputKey = inputKey;
        this.outputKey = outputKey;
        this.options = options != null ? options : RAGOptions.builder().build();
    }

    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String question = input.getString(inputKey);
                if (question == null) {
                    return NodeOutput.failure("Input key '" + inputKey + "' not found or null");
                }

                RAGResponse response = ai.ragApi().ask(question, options);

                // Store both answer and sources in context
                input.context().set(outputKey, response.answer());
                input.context().set(outputKey + ".sources", response.sources());

                return NodeOutput.success(Map.of(
                    outputKey, response.answer(),
                    outputKey + ".sources", response.sources()
                ));
            } catch (Exception e) {
                return NodeOutput.failure("RAG query failed: " + e.getMessage());
            }
        });
    }

    public static Builder builder(AI ai) {
        return new Builder(ai);
    }

    public static class Builder {
        private final AI ai;
        private String inputKey = "question";
        private String outputKey = "answer";
        private RAGOptions options;

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

        public Builder options(RAGOptions opts) {
            this.options = opts;
            return this;
        }

        public RAGNode build() {
            return new RAGNode(ai, inputKey, outputKey, options);
        }
    }
}

