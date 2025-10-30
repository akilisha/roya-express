package com.akilisha.oss.roya.plugins.vector.backends;

import com.akilisha.oss.roya.plugins.vector.Document;
import com.akilisha.oss.roya.plugins.vector.DocumentMatch;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory vector store backend (perfect for dev/testing).
 * 
 * Uses cosine similarity for vector search. All data is stored in memory
 * (volatile - lost on restart).
 */
public class EmbeddedVectorBackend {
    
    // Collection name -> (document ID -> document with embedding)
    private final Map<String, Map<String, Document>> collections = new ConcurrentHashMap<>();
    
    /**
     * Index documents in a collection.
     * 
     * @param collection Collection name
     * @param documents Documents with embeddings
     */
    public void index(String collection, List<Document> documents) {
        Map<String, Document> collectionDocs = collections.computeIfAbsent(collection, k -> new ConcurrentHashMap<>());
        
        for (Document doc : documents) {
            if (doc.embedding().isEmpty()) {
                throw new IllegalArgumentException("Document " + doc.id() + " must have embedding to index");
            }
            collectionDocs.put(doc.id(), doc);
        }
    }
    
    /**
     * Search for similar documents using cosine similarity.
     * 
     * @param collection Collection name
     * @param queryEmbedding Query embedding vector
     * @param topK Number of results
     * @return List of document matches sorted by similarity (highest first)
     */
    public List<DocumentMatch> search(String collection, float[] queryEmbedding, int topK) {
        Map<String, Document> collectionDocs = collections.get(collection);
        if (collectionDocs == null || collectionDocs.isEmpty()) {
            return List.of();
        }
        
        // Calculate cosine similarity for all documents
        List<DocumentMatch> matches = collectionDocs.values().stream()
            .filter(doc -> doc.embedding().isPresent())
            .map(doc -> {
                float[] docEmbedding = doc.embedding().get();
                double score = cosineSimilarity(queryEmbedding, docEmbedding);
                return new DocumentMatch(doc, score);
            })
            .sorted((a, b) -> Double.compare(b.score(), a.score())) // Descending
            .limit(topK)
            .collect(Collectors.toList());
        
        return matches;
    }
    
    /**
     * Search with minimum score threshold.
     */
    public List<DocumentMatch> search(String collection, float[] queryEmbedding, int topK, double minScore) {
        return search(collection, queryEmbedding, topK).stream()
            .filter(match -> match.score() >= minScore)
            .collect(Collectors.toList());
    }
    
    /**
     * Delete a collection.
     */
    public void deleteCollection(String collection) {
        collections.remove(collection);
    }
    
    /**
     * Get collection stats.
     */
    public long getDocumentCount(String collection) {
        Map<String, Document> collectionDocs = collections.get(collection);
        return collectionDocs != null ? collectionDocs.size() : 0;
    }
    
    /**
     * Check if collection exists.
     */
    public boolean hasCollection(String collection) {
        return collections.containsKey(collection);
    }
    
    /**
     * Calculate cosine similarity between two vectors.
     * 
     * @param a First vector
     * @param b Second vector
     * @return Cosine similarity (0.0 - 1.0, higher = more similar)
     */
    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vectors must have same length");
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        
        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        if (denominator == 0.0) {
            return 0.0;
        }
        
        return dotProduct / denominator;
    }
}

