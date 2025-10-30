package com.akilisha.oss.roya.plugins.vector.embeddings;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for generating text embeddings.
 * 
 * Converts text into vector embeddings for semantic search.
 */
public interface EmbeddingService {
    /**
     * Generate embedding for a single text.
     * 
     * @param text Input text to embed
     * @return Future that completes with the embedding vector
     */
    CompletableFuture<float[]> embed(String text);
    
    /**
     * Generate embeddings in batch (more efficient than individual calls).
     * 
     * @param texts List of texts to embed
     * @return Future that completes with list of embeddings (same order as input)
     */
    CompletableFuture<List<float[]>> embed(List<String> texts);
    
    /**
     * Get embedding dimensions (e.g., 1536 for OpenAI text-embedding-3-small).
     * 
     * @return Number of dimensions in embedding vectors
     */
    int dimensions();
    
    /**
     * Get provider name (e.g., "openai", "local").
     * 
     * @return Provider identifier
     */
    String provider();
    
    /**
     * Get model name (e.g., "text-embedding-3-small").
     * 
     * @return Model identifier
     */
    String model();
}

