package com.akilisha.oss.roya.api;

import java.util.function.Consumer;

/**
 * Builder for vector database indexing operations.
 *
 * Allows indexing documents with embeddings and metadata.
 */
public interface IndexBuilder {
    /**
     * Add a document to index.
     *
     * @param id Document ID
     * @param content Document content (text)
     * @param embedding Vector embedding (will be auto-generated if null)
     * @param metadata Document metadata
     */
    IndexBuilder document(String id, String content, float[] embedding, Consumer<MetadataBuilder> metadata);

    /**
     * Add a document with auto-embedded content.
     */
    IndexBuilder document(String id, String content, Consumer<MetadataBuilder> metadata);

    /**
     * Set upsert mode (update if exists, insert otherwise).
     */
    IndexBuilder upsert();

    /**
     * Set batch size for indexing.
     */
    IndexBuilder batchSize(int size);
}

/**
 * Builder for document metadata.
 */
interface MetadataBuilder {
    /**
     * Add metadata field.
     */
    MetadataBuilder put(String key, Object value);

    /**
     * Add multiple metadata fields.
     */
    MetadataBuilder putAll(java.util.Map<String, Object> metadata);
}

