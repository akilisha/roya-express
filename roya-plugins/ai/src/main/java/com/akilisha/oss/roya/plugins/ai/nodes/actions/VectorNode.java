package com.akilisha.oss.roya.plugins.ai.nodes.actions;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Vector indexing node for workflows.
 * 
 * Indexes documents into Qdrant vector store.
 * Supports both individual documents and directory indexing.
 * 
 * Example:
 * <pre>
 * VectorNode node = VectorNode.builder(ai)
 *     .inputKey("documents")
 *     .collection("my-collection")
 *     .build();
 * </pre>
 */
public class VectorNode implements WorkflowNode {
    
    private final AI ai;
    private final String inputKey;
    private final String collection;
    private final Path directory;
    private final AI.ChunkingOptions chunkingOptions;

    // For indexing documents from context
    public VectorNode(AI ai, String inputKey, String collection) {
        this.ai = ai;
        this.inputKey = inputKey;
        this.collection = collection;
        this.directory = null;
        this.chunkingOptions = null;
    }

    // For indexing directory
    public VectorNode(AI ai, String collection, Path directory, AI.ChunkingOptions chunkingOptions) {
        this.ai = ai;
        this.inputKey = null;
        this.collection = collection;
        this.directory = directory;
        this.chunkingOptions = chunkingOptions;
    }

    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (directory != null) {
                    // Index directory
                    ai.vectors().indexPath(collection, directory, chunkingOptions);
                    return NodeOutput.success(Map.of("collection", collection, "indexed", "directory"));
                } else {
                    // Index documents from context
                    @SuppressWarnings("unchecked")
                    List<AI.VectorDoc> documents = input.get(inputKey, List.class);
                    if (documents == null) {
                        return NodeOutput.failure("Input key '" + inputKey + "' not found or null");
                    }
                    
                    ai.vectors().index(collection, documents);
                    return NodeOutput.success(Map.of(
                        "collection", collection,
                        "indexed", documents.size()
                    ));
                }
            } catch (Exception e) {
                return NodeOutput.failure("Vector indexing failed: " + e.getMessage());
            }
        });
    }

    public static Builder builder(AI ai) {
        return new Builder(ai);
    }

    public static class Builder {
        private final AI ai;
        private String inputKey;
        private String collection;
        private Path directory;
        private AI.ChunkingOptions chunkingOptions;

        Builder(AI ai) {
            this.ai = ai;
        }

        public Builder inputKey(String key) {
            this.inputKey = key;
            return this;
        }

        public Builder collection(String collection) {
            this.collection = collection;
            return this;
        }

        public Builder directory(Path dir, AI.ChunkingOptions options) {
            this.directory = dir;
            this.chunkingOptions = options;
            return this;
        }

        public VectorNode build() {
            if (directory != null) {
                if (collection == null) {
                    throw new IllegalStateException("Collection name is required for directory indexing");
                }
                return new VectorNode(ai, collection, directory, chunkingOptions);
            } else {
                if (inputKey == null || collection == null) {
                    throw new IllegalStateException("Input key and collection are required for document indexing");
                }
                return new VectorNode(ai, inputKey, collection);
            }
        }
    }
}

