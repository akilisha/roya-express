package com.akilisha.oss.roya.plugins.ai.langchain.services;

import java.util.List;

/**
 * Embedding service interface.
 * 
 * Note: Embeddings don't use AI Services pattern (they're not chat-based),
 * but we provide this interface for consistency. Implementation will
 * use EmbeddingModel directly.
 */
public interface EmbeddingService {
    
    /**
     * Embed a single text into a vector.
     * 
     * @param text Text to embed
     * @return Float array representing the embedding vector
     */
    float[] embed(String text);
    
    /**
     * Embed multiple texts into vectors (batch operation).
     * 
     * @param texts List of texts to embed
     * @return List of float arrays (one per text)
     */
    List<float[]> embed(List<String> texts);
}

