package com.akilisha.oss.roya.plugins.ai;

/**
 * RAG options for retrieval and generation.
 */
public record RAGOptions(
    String collection,     // Qdrant collection name to search
    int topK,              // Number of documents to retrieve
    boolean rerank,        // Whether to rerank results
    Double minScore,       // Minimum similarity score
    AIOptions aiOptions    // AI generation options
) {
    public static RAGOptions defaults() {
        return new RAGOptions(
            "default",         // Default collection name
            5,                 // Default topK
            false,             // Default no reranking
            0.7,              // Default min score
            AIOptions.defaults()
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String collection = "default";
        private int topK = 5;
        private boolean rerank = false;
        private Double minScore = 0.7;
        private AIOptions aiOptions = AIOptions.defaults();

        public Builder collection(String collection) {
            this.collection = collection;
            return this;
        }

        public Builder topK(int topK) {
            this.topK = topK;
            return this;
        }

        public Builder rerank(boolean rerank) {
            this.rerank = rerank;
            return this;
        }

        public Builder minScore(Double minScore) {
            this.minScore = minScore;
            return this;
        }

        public Builder aiOptions(AIOptions aiOptions) {
            this.aiOptions = aiOptions;
            return this;
        }

        public RAGOptions build() {
            return new RAGOptions(collection, topK, rerank, minScore, aiOptions);
        }
    }
}



