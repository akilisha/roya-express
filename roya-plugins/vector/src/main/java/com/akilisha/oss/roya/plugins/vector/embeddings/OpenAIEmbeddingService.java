package com.akilisha.oss.roya.plugins.vector.embeddings;

import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.service.OpenAiService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * OpenAI embedding service implementation.
 * 
 * Uses OpenAI's text-embedding models for generating embeddings.
 */
public class OpenAIEmbeddingService implements EmbeddingService {
    
    private final OpenAiService openAiService;
    private final String model;
    private final int dimensions;
    
    /**
     * Create OpenAI embedding service.
     * 
     * @param apiKey OpenAI API key
     * @param model Model name (default: "text-embedding-3-small", 1536 dims)
     */
    public OpenAIEmbeddingService(String apiKey, String model) {
        this.openAiService = new OpenAiService(apiKey);
        this.model = model != null ? model : "text-embedding-3-small";
        // OpenAI text-embedding-3-small has 1536 dimensions
        // text-embedding-3-large has 3072 dimensions
        this.dimensions = this.model.contains("large") ? 3072 : 1536;
    }
    
    /**
     * Create with default model (text-embedding-3-small).
     */
    public OpenAIEmbeddingService(String apiKey) {
        this(apiKey, null);
    }
    
    @Override
    public CompletableFuture<float[]> embed(String text) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                EmbeddingRequest request = EmbeddingRequest.builder()
                    .model(model)
                    .input(List.of(text))
                    .build();
                
                EmbeddingResult result = openAiService.createEmbeddings(request);
                
                if (result.getData().isEmpty()) {
                    throw new RuntimeException("OpenAI returned no embeddings");
                }
                
                // Convert List<Double> to float[]
                List<Double> embedding = result.getData().get(0).getEmbedding();
                float[] embeddingArray = new float[embedding.size()];
                for (int i = 0; i < embedding.size(); i++) {
                    embeddingArray[i] = embedding.get(i).floatValue();
                }
                
                return embeddingArray;
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate embedding: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<float[]>> embed(List<String> texts) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                EmbeddingRequest request = EmbeddingRequest.builder()
                    .model(model)
                    .input(texts)
                    .build();
                
                EmbeddingResult result = openAiService.createEmbeddings(request);
                
                // Convert each embedding from List<Double> to float[]
                return result.getData().stream()
                    .map(data -> {
                        List<Double> embedding = data.getEmbedding();
                        float[] embeddingArray = new float[embedding.size()];
                        for (int i = 0; i < embedding.size(); i++) {
                            embeddingArray[i] = embedding.get(i).floatValue();
                        }
                        return embeddingArray;
                    })
                    .collect(Collectors.toList());
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate embeddings: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public int dimensions() {
        return dimensions;
    }
    
    @Override
    public String provider() {
        return "openai";
    }
    
    @Override
    public String model() {
        return model;
    }
}

