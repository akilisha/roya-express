package com.akilisha.oss.roya.plugins.vector;

import com.akilisha.oss.roya.api.plugin.RoyaPlugin;
import com.akilisha.oss.roya.api.plugin.Services;
import com.akilisha.oss.roya.plugins.vector.embeddings.EmbeddingService;
import com.akilisha.oss.roya.plugins.vector.embeddings.OpenAIEmbeddingService;

/**
 * Vector store plugin - semantic search and document indexing.
 * 
 * Provides vector storage and search capabilities for RAG.
 */
public class VectorStorePlugin implements RoyaPlugin {
    
    @Override
    public String id() {
        return "vector";
    }
    
    @Override
    public String version() {
        return "1.0.0";
    }
    
    @Override
    public String description() {
        return "Vector store for semantic search and RAG (embedded backend for dev)";
    }
    
    @Override
    public void register(Services services) {
        services.singleton(VectorStore.class, () -> {
            // Configuration from system properties or environment variables
            String provider = System.getProperty("vector.embedding.provider",
                System.getenv().getOrDefault("VECTOR_EMBEDDING_PROVIDER", "openai"));
            String apiKey = System.getProperty("vector.openai.apiKey");
            if (apiKey == null || apiKey.isBlank()) {
                apiKey = System.getenv("VECTOR_OPENAI_API_KEY");
            }
            if (apiKey == null || apiKey.isBlank()) {
                apiKey = System.getenv("OPENAI_API_KEY"); // Reuse AI plugin key if set
            }
            String model = System.getProperty("vector.openai.model",
                System.getenv().getOrDefault("VECTOR_OPENAI_MODEL", "text-embedding-3-small"));
            
            EmbeddingService embeddingService;
            
            switch (provider.toLowerCase()) {
                case "openai" -> {
                    if (apiKey == null || apiKey.isBlank()) {
                        throw new IllegalArgumentException(
                            "vector.openai.apiKey is required when using OpenAI embedding provider"
                        );
                    }
                    embeddingService = new OpenAIEmbeddingService(apiKey, model);
                }
                // Future providers: local, Cohere, etc.
                default -> throw new IllegalArgumentException(
                    "Unknown embedding provider: " + provider + ". Supported: openai"
                );
            }
            
            return new VectorStoreServiceImpl(embeddingService);
        });
    }
    
    @Override
    public void start() throws Exception {
        String apiKey = System.getProperty("vector.openai.apiKey");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("VECTOR_OPENAI_API_KEY");
        }
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("OPENAI_API_KEY");
        }
        
        String backendType = System.getProperty("vector.backend.embedded",
            System.getenv().getOrDefault("VECTOR_BACKEND_EMBEDDED", "false"));
        boolean useEmbedded = Boolean.parseBoolean(backendType);
        String qdrantUrl = System.getProperty("qdrant.url",
            System.getenv().getOrDefault("QDRANT_URL", "http://localhost:6333"));
        
        System.out.println("✓ VectorStorePlugin: Vector store service initialized");
        System.out.println("  - Embedding Provider: " + System.getProperty("vector.embedding.provider",
            System.getenv().getOrDefault("VECTOR_EMBEDDING_PROVIDER", "openai")));
        System.out.println("  - Model: " + System.getProperty("vector.openai.model",
            System.getenv().getOrDefault("VECTOR_OPENAI_MODEL", "text-embedding-3-small")));
        System.out.println("  - Backend: " + (useEmbedded ? "Embedded (in-memory)" : "Qdrant (" + qdrantUrl + ")"));
        System.out.println("  - API Key: " + (apiKey != null && !apiKey.isBlank()
            ? "✓ Set (" + apiKey.substring(0, Math.min(8, apiKey.length())) + "...)"
            : "✗ Not set"));
        if (!useEmbedded) {
            System.out.println("  - Note: Start Qdrant with: docker-compose up -d qdrant");
        }
        System.out.println("  - Usage: VectorStore vs = req.get(VectorStore.class);");
    }
    
    @Override
    public void stop() throws Exception {
        // No cleanup needed for embedded backend
    }
}

