package com.akilisha.oss.roya.plugins.vector;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Vector store interface for semantic search and document indexing.
 * 
 * Provides high-level API for indexing documents and searching by semantic similarity.
 */
public interface VectorStore {
    /**
     * Index documents (with automatic chunking and embedding).
     * 
     * Documents are automatically embedded using the configured EmbeddingService.
     * 
     * @param collection Collection name (namespace for documents)
     * @param documents Documents to index
     * @return Future that completes when indexing is done
     */
    CompletableFuture<Void> index(String collection, List<Document> documents);
    
    /**
     * Index files from directory.
     * 
     * Files are read, chunked, and indexed automatically.
     * 
     * @param collection Collection name
     * @param directory Directory containing files to index
     * @return Future that completes when indexing is done
     */
    CompletableFuture<Void> index(String collection, Path directory);
    
    /**
     * Search for similar documents by text query.
     * 
     * Query is automatically embedded and compared against stored vectors.
     * 
     * @param collection Collection to search
     * @param query Text query
     * @param topK Number of results to return
     * @return List of matching documents with similarity scores (sorted by score, highest first)
     */
    List<DocumentMatch> search(String collection, String query, int topK);
    
    /**
     * Search with minimum score threshold.
     * 
     * Only returns documents with similarity score >= minScore.
     * 
     * @param collection Collection to search
     * @param query Text query
     * @param topK Number of results to return
     * @param minScore Minimum similarity score (0.0 - 1.0)
     * @return List of matching documents meeting the threshold
     */
    List<DocumentMatch> search(String collection, String query, int topK, double minScore);
    
    /**
     * Delete a collection and all its documents.
     * 
     * @param collection Collection name
     */
    void deleteCollection(String collection);
    
    /**
     * Get collection statistics.
     * 
     * @param collection Collection name
     * @return Collection stats (document count, dimensions, size)
     */
    CollectionStats getStats(String collection);
    
    /**
     * Get provider-specific client access (for advanced use cases).
     * 
     * @param providerType Provider type class (e.g., QdrantClient.class)
     * @return Provider instance or null if not available
     */
    <T> T provider(Class<T> providerType);
}

