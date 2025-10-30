package com.akilisha.oss.roya.plugins.vector;

import java.util.Map;
import java.util.Optional;

/**
 * Document model for vector storage.
 * 
 * Represents a document with optional embedding for vector search.
 */
public record Document(
    String id,                        // Unique document ID
    String content,                   // Document content/text
    Map<String, Object> metadata,     // Additional metadata (source, title, timestamp, etc.)
    Optional<float[]> embedding       // Optional embedding vector (can be lazy-loaded)
) {
    /**
     * Create document without embedding.
     */
    public static Document of(String id, String content) {
        return new Document(id, content, Map.of(), Optional.empty());
    }
    
    /**
     * Create document with metadata.
     */
    public static Document of(String id, String content, Map<String, Object> metadata) {
        return new Document(id, content, metadata, Optional.empty());
    }
    
    /**
     * Create document with embedding.
     */
    public static Document withEmbedding(String id, String content, Map<String, Object> metadata, float[] embedding) {
        return new Document(id, content, metadata, Optional.of(embedding));
    }
}

