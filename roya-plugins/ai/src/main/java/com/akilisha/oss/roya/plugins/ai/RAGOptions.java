package com.akilisha.oss.roya.plugins.ai;

/**
 * RAG options for retrieval and generation.
 * 
 * <p>⚠️ NOTE: Configuration precedence implementation is a placeholder.
 * Proper config system should use a dedicated configuration library/pattern.
 * See backlog: Config defaults and precedence for collection/topK/minScore
 */
public record RAGOptions(
    String collection,     // Qdrant collection name to search
    int topK,              // Number of documents to retrieve
    boolean rerank,        // Whether to rerank results
    Double minScore,       // Minimum similarity score
    AIOptions aiOptions    // AI generation options
) {
    public static RAGOptions defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String collection;
        private Integer topK;
        private Boolean rerank;
        private Double minScore;
        private AIOptions aiOptions;

        public Builder() {
            // Initialize with defaults from environment/system properties
            this.collection = getConfig("collection", "RAG_COLLECTION", "rag.collection", "default");
            this.topK = getIntConfig("topK", "RAG_TOP_K", "rag.topK", 5);
            this.rerank = getBooleanConfig("rerank", "RAG_RERANK", "rag.rerank", false);
            this.minScore = getDoubleConfig("minScore", "RAG_MIN_SCORE", "rag.minScore", 0.7);
            this.aiOptions = AIOptions.defaults();
        }
        
        /**
         * Get configuration value with precedence: env var > system property > default.
         */
        private String getConfig(String fieldName, String envVar, String sysProp, String defaultValue) {
            // Check environment variable first
            String envValue = System.getenv(envVar);
            if (envValue != null && !envValue.isBlank()) {
                return envValue;
            }
            
            // Check system property
            String sysValue = System.getProperty(sysProp);
            if (sysValue != null && !sysValue.isBlank()) {
                return sysValue;
            }
            
            return defaultValue;
        }
        
        /**
         * Get integer configuration value.
         */
        private int getIntConfig(String fieldName, String envVar, String sysProp, int defaultValue) {
            String envValue = System.getenv(envVar);
            if (envValue != null && !envValue.isBlank()) {
                try {
                    return Integer.parseInt(envValue);
                } catch (NumberFormatException e) {
                    // Invalid value, use default
                }
            }
            
            String sysValue = System.getProperty(sysProp);
            if (sysValue != null && !sysValue.isBlank()) {
                try {
                    return Integer.parseInt(sysValue);
                } catch (NumberFormatException e) {
                    // Invalid value, use default
                }
            }
            
            return defaultValue;
        }
        
        /**
         * Get double configuration value.
         */
        private double getDoubleConfig(String fieldName, String envVar, String sysProp, double defaultValue) {
            String envValue = System.getenv(envVar);
            if (envValue != null && !envValue.isBlank()) {
                try {
                    return Double.parseDouble(envValue);
                } catch (NumberFormatException e) {
                    // Invalid value, use default
                }
            }
            
            String sysValue = System.getProperty(sysProp);
            if (sysValue != null && !sysValue.isBlank()) {
                try {
                    return Double.parseDouble(sysValue);
                } catch (NumberFormatException e) {
                    // Invalid value, use default
                }
            }
            
            return defaultValue;
        }
        
        /**
         * Get boolean configuration value.
         */
        private boolean getBooleanConfig(String fieldName, String envVar, String sysProp, boolean defaultValue) {
            String envValue = System.getenv(envVar);
            if (envValue != null && !envValue.isBlank()) {
                return Boolean.parseBoolean(envValue);
            }
            
            String sysValue = System.getProperty(sysProp);
            if (sysValue != null && !sysValue.isBlank()) {
                return Boolean.parseBoolean(sysValue);
            }
            
            return defaultValue;
        }

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
            return new RAGOptions(
                collection != null ? collection : "default",
                topK != null ? topK : 5,
                rerank != null ? rerank : false,
                minScore != null ? minScore : 0.7,
                aiOptions != null ? aiOptions : AIOptions.defaults()
            );
        }
    }
}



