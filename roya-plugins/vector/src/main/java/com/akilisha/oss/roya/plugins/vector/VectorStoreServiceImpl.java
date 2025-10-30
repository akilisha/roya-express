package com.akilisha.oss.roya.plugins.vector;

import com.akilisha.oss.roya.plugins.vector.backends.EmbeddedVectorBackend;
import com.akilisha.oss.roya.plugins.vector.backends.QdrantVectorBackend;
import com.akilisha.oss.roya.plugins.vector.embeddings.EmbeddingService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Vector store service implementation - orchestrates embedding generation and vector search.
 */
public class VectorStoreServiceImpl implements VectorStore {
    
    private final EmbeddingService embeddingService;
    private final EmbeddedVectorBackend embedded;
    private final QdrantVectorBackend qdrant; // optional
    
    public VectorStoreServiceImpl(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
        this.embedded = new EmbeddedVectorBackend();
        // Default to local Qdrant (docker-compose) if not explicitly disabled
        String qdrantUrl = System.getProperty("qdrant.url", 
            System.getenv().getOrDefault("QDRANT_URL", "http://localhost:6333"));
        String qdrantKey = System.getProperty("qdrant.apiKey", 
            System.getenv().getOrDefault("QDRANT_API_KEY", ""));
        boolean useEmbedded = Boolean.parseBoolean(
            System.getProperty("vector.backend.embedded",
                System.getenv().getOrDefault("VECTOR_BACKEND_EMBEDDED", "false")));
        // Use Qdrant unless explicitly forced to use embedded
        this.qdrant = useEmbedded ? null : new QdrantVectorBackend(qdrantUrl, qdrantKey);
    }
    
    @Override
    public CompletableFuture<Void> index(String collection, List<Document> documents) {
        return CompletableFuture.supplyAsync(() -> {
            // Generate embeddings for documents that don't have them
            List<String> textsToEmbed = documents.stream()
                .filter(doc -> doc.embedding().isEmpty())
                .map(Document::content)
                .collect(Collectors.toList());
            
            List<Document> documentsToIndex;
            
            if (textsToEmbed.isEmpty()) {
                // All documents already have embeddings
                documentsToIndex = documents;
            } else {
                // Generate embeddings
                List<float[]> embeddings = embeddingService.embed(textsToEmbed).join();
                
                // Merge documents with embeddings
                // Need to track which embedding index to use
                int[] embeddingIndex = {0}; // Array to allow modification in lambda
                documentsToIndex = documents.stream()
                    .map(doc -> {
                        if (doc.embedding().isPresent()) {
                            return doc; // Already has embedding
                        } else {
                            // Use generated embedding
                            float[] embedding = embeddings.get(embeddingIndex[0]++);
                            return Document.withEmbedding(
                                doc.id(),
                                doc.content(),
                                doc.metadata(),
                                embedding
                            );
                        }
                    })
                    .collect(Collectors.toList());
            }
            
            // Index in backend(s)
            if (qdrant != null) {
                try {
                    qdrant.ensureCollection(collection, embeddingService.dimensions());
                    qdrant.upsert(collection, documentsToIndex);
                } catch (Exception e) {
                    System.err.println("Warning: Qdrant indexing failed, using embedded backend: " + e.getMessage());
                    embedded.index(collection, documentsToIndex);
                }
            } else {
                // Use embedded backend
                embedded.index(collection, documentsToIndex);
            }
            return null;
        });
    }
    
    @Override
    public CompletableFuture<Void> index(String collection, Path directory) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!Files.isDirectory(directory)) {
                    throw new IllegalArgumentException("Not a directory: " + directory);
                }

                // Defaults: ~800 char chunks with 200 overlap (roughly ~200 tokens/50 overlap)
                var chunker = new com.akilisha.oss.roya.plugins.vector.chunking.FixedSizeChunker(800, 200);

                var docs = Files.walk(directory)
                    .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".txt"))
                    .flatMap(p -> {
                        try {
                            String content = Files.readString(p);
                            List<String> chunks = chunker.chunk(content);
                            // Create Document per chunk with metadata
                            return java.util.stream.IntStream.range(0, chunks.size())
                                .mapToObj(i -> {
                                    String id = p.toString() + "#" + i;
                                    Map<String, Object> metadata = Map.of(
                                        "source", p.toString(),
                                        "chunkIndex", i
                                    );
                                    return Document.of(id, chunks.get(i), metadata);
                                });
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to read file: " + p + ", " + e.getMessage(), e);
                        }
                    })
                    .collect(Collectors.toList());

                // Index all chunk documents (embeddings generated automatically)
                index(collection, docs).join();
                return null;
            } catch (Exception e) {
                throw new RuntimeException("Failed to index directory: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public List<DocumentMatch> search(String collection, String query, int topK) {
        // Generate embedding for query
        float[] queryEmbedding = embeddingService.embed(query).join();
        
        // Prefer Qdrant if configured, else embedded
        if (qdrant != null) {
            try {
                return qdrant.search(collection, queryEmbedding, topK, 0.0);
            } catch (Exception e) {
                // Fallback to embedded if Qdrant fails
                System.err.println("Warning: Qdrant search failed, using embedded backend: " + e.getMessage());
                return embedded.search(collection, queryEmbedding, topK);
            }
        }
        return embedded.search(collection, queryEmbedding, topK);
    }
    
    @Override
    public List<DocumentMatch> search(String collection, String query, int topK, double minScore) {
        // Generate embedding for query
        float[] queryEmbedding = embeddingService.embed(query).join();
        
        // Prefer Qdrant if configured, else embedded
        if (qdrant != null) {
            try {
                return qdrant.search(collection, queryEmbedding, topK, minScore);
            } catch (Exception e) {
                // Fallback to embedded if Qdrant fails
                System.err.println("Warning: Qdrant search failed, using embedded backend: " + e.getMessage());
                return embedded.search(collection, queryEmbedding, topK, minScore);
            }
        }
        return embedded.search(collection, queryEmbedding, topK, minScore);
    }
    
    @Override
    public void deleteCollection(String collection) {
        // Only affects embedded backend
        embedded.deleteCollection(collection);
    }
    
    @Override
    public CollectionStats getStats(String collection) {
        long documentCount = embedded.getDocumentCount(collection);
        return new CollectionStats(
            collection,
            documentCount,
            embeddingService.dimensions(),
            documentCount * embeddingService.dimensions() * 4L // Approximate: 4 bytes per float
        );
    }
    
    @Override
    public <T> T provider(Class<T> providerType) {
        // Check Qdrant backend first (if available)
        if (qdrant != null && providerType.isInstance(qdrant)) {
            return providerType.cast(qdrant);
        }
        // Check embedded backend
        if (providerType.isInstance(embedded)) {
            return providerType.cast(embedded);
        }
        // Check embedding service
        if (providerType.isInstance(embeddingService)) {
            return providerType.cast(embeddingService);
        }
        return null;
    }
}

