package com.akilisha.oss.roya.plugins.vector;

/**
 * Collection statistics.
 */
public record CollectionStats(
    String collectionName,
    long documentCount,        // Number of documents in collection
    int embeddingDimensions,  // Dimension of embeddings
    long totalSize            // Total size in bytes (approximate)
) {
}

