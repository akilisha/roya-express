package com.akilisha.oss.roya.plugins.ai.rag;

/**
 * Collection statistics for a Qdrant collection.
 */
public record CollectionStats(
    String collection,
    long vectorCount,
    int vectorSize,
    String status,
    java.util.Map<String, Object> metadata
) {
    public static CollectionStats empty(String collection) {
        return new CollectionStats(collection, 0, 0, "unknown", java.util.Map.of());
    }
}

