package com.akilisha.oss.roya.plugins.ai;

import com.akilisha.oss.roya.plugins.ai.providers.LLMProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.service.OpenAiService;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * AI service implementation - orchestration layer.
 *
 * Handles:
 * - Caching (via Cache plugin)
 * - Token counting and cost tracking
 * - Provider abstraction
 * - Structured output extraction (JSON mode + Jackson)
 */
public class AIServiceImpl implements AI {

    private final LLMProvider provider;
    private final com.akilisha.oss.roya.plugins.cache.Cache cache;  // Optional - may be null
    private final ObjectMapper objectMapper;
    private final boolean cachingEnabled;
    private static final OkHttpClient QDRANT_HTTP = new OkHttpClient.Builder()
        .callTimeout(java.time.Duration.ofSeconds(10))
        .connectTimeout(java.time.Duration.ofSeconds(5))
        .readTimeout(java.time.Duration.ofSeconds(10))
        .writeTimeout(java.time.Duration.ofSeconds(10))
        .build();

    public AIServiceImpl(LLMProvider provider, com.akilisha.oss.roya.plugins.cache.Cache cache) {
        this.provider = provider;
        this.cache = cache;
        this.cachingEnabled = cache != null;
        this.objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public LLM llm() {
        return new LLM() {
            @Override
            public String ask(String systemPrompt, String userMessage) {
                return AIServiceImpl.this.ask(systemPrompt, userMessage);
            }

            @Override
            public String ask(String systemPrompt, String userMessage, AIOptions options) {
                return AIServiceImpl.this.ask(systemPrompt, userMessage, options);
            }

            @Override
            public <T> T extract(Class<T> type, String prompt) {
                return AIServiceImpl.this.extract(type, prompt);
            }

            @Override
            public <T> T extract(Class<T> type, String prompt, AIOptions options) {
                return AIServiceImpl.this.extract(type, prompt, options);
            }

            @Override
            public void stream(String systemPrompt, String userMessage, Consumer<String> onToken) {
                AIServiceImpl.this.stream(systemPrompt, userMessage, onToken);
            }

            @Override
            public void stream(String systemPrompt, String userMessage, AIOptions options, Consumer<String> onToken) {
                AIServiceImpl.this.stream(systemPrompt, userMessage, options, onToken);
            }
        };
    }

    @Override
    public Embeddings embeddings() {
        return new Embeddings() {
            @Override
            public float[] embed(String text) {
                String openAiKey = System.getProperty("ai.openai.apiKey",
                    Optional.ofNullable(System.getenv("AI_OPENAI_API_KEY")).orElse(System.getenv("OPENAI_API_KEY")));
                if (openAiKey == null || openAiKey.isBlank()) {
                    throw new AIException("OPENAI_API_KEY (or ai.openai.apiKey) is required for embeddings");
                }
                return embedWithOpenAI(openAiKey, text);
            }

            @Override
            public java.util.List<float[]> embed(java.util.List<String> texts) {
                String openAiKey = System.getProperty("ai.openai.apiKey",
                    Optional.ofNullable(System.getenv("AI_OPENAI_API_KEY")).orElse(System.getenv("OPENAI_API_KEY")));
                if (openAiKey == null || openAiKey.isBlank()) {
                    throw new AIException("OPENAI_API_KEY (or ai.openai.apiKey) is required for embeddings");
                }
                java.util.List<float[]> out = new java.util.ArrayList<>(texts.size());
                for (String t : texts) out.add(embedWithOpenAI(openAiKey, t));
                return out;
            }
        };
    }

    @Override
    public Vectors vectors() {
        return new Vectors() {
            @Override
            public void indexPath(String collection, java.nio.file.Path directory, ChunkingOptions options) {
                try {
                    java.util.List<VectorDoc> docs = new java.util.ArrayList<>();
                    java.nio.file.Files.walk(directory)
                        .filter(p -> java.nio.file.Files.isRegularFile(p))
                        .filter(p -> {
                            String s = p.toString().toLowerCase();
                            return s.endsWith(".md") || s.endsWith(".markdown") || s.endsWith(".txt");
                        })
                        .forEach(p -> {
                            try {
                                String content = java.nio.file.Files.readString(p);
                                for (Chunk ch : chunk(content, options)) {
                                    docs.add(new VectorDoc(p.toString() + "#" + ch.index, ch.text, java.util.Map.of("source", p.toString(), "chunkIndex", ch.index)));
                                }
                            } catch (Exception e) { throw new RuntimeException(e); }
                        });
                    index(collection, docs);
                } catch (Exception e) {
                    throw new RuntimeException("Indexing path failed: " + e.getMessage(), e);
                }
            }

            @Override
            public void index(String collection, java.util.List<VectorDoc> documents) {
                try {
                    String qdrantUrl = System.getProperty("qdrant.url",
                        System.getenv().getOrDefault("QDRANT_URL", "http://localhost:6333"));
                    String qdrantApiKey = System.getProperty("qdrant.apiKey",
                        System.getenv().getOrDefault("QDRANT_API_KEY", ""));
                    String openAiKey = System.getProperty("ai.openai.apiKey",
                        Optional.ofNullable(System.getenv("AI_OPENAI_API_KEY")).orElse(System.getenv("OPENAI_API_KEY")));
                    if (openAiKey == null || openAiKey.isBlank()) {
                        throw new AIException("OPENAI_API_KEY (or ai.openai.apiKey) is required for indexing");
                    }
                    // Ensure collection exists (cosine distance)
                    if (!documents.isEmpty()) {
                        float[] emb0 = embedWithOpenAI(openAiKey, documents.get(0).content());
                        java.util.Map<String, Object> vectors = new java.util.HashMap<>();
                        vectors.put("size", emb0.length);
                        vectors.put("distance", "Cosine");
                        java.util.Map<String, Object> payload = new java.util.HashMap<>();
                        payload.put("vectors", vectors);
                        String body = new ObjectMapper().writeValueAsString(payload);
                        OkHttpClient http = new OkHttpClient();
                        Request.Builder rb = new Request.Builder()
                            .url(qdrantUrl.replaceAll("/$", "") + "/collections/" + collection)
                            .put(RequestBody.create(body, MediaType.parse("application/json")))
                            .addHeader("Content-Type", "application/json");
                        if (qdrantApiKey != null && !qdrantApiKey.isBlank()) rb.addHeader("api-key", qdrantApiKey);
                        try (Response res = http.newCall(rb.build()).execute()) {
                            if (!res.isSuccessful() && res.code() != 409) {
                                throw new RuntimeException("Qdrant create collection failed: " + res.code());
                            }
                        }
                    }
                    // Upsert points (batch embeddings for throughput)
                    java.util.List<java.util.Map<String, Object>> points = new java.util.ArrayList<>();
                    final int batchSize = 64;
                    for (int start = 0; start < documents.size(); start += batchSize) {
                        int end = Math.min(start + batchSize, documents.size());
                        var batch = documents.subList(start, end);
                        var texts = batch.stream().map(VectorDoc::content).toList();
                        var vectors = embedBatchWithOpenAI(openAiKey, texts);
                        for (int i = 0; i < batch.size(); i++) {
                            VectorDoc d = batch.get(i);
                            float[] vec = vectors.get(i);
                            java.util.List<Double> vector = new java.util.ArrayList<>(vec.length);
                            for (float v : vec) vector.add((double) v);
                            java.util.Map<String, Object> payload = new java.util.HashMap<>();
                            payload.put("content", d.content());
                            payload.put("metadata", d.metadata());
                            java.util.Map<String, Object> point = new java.util.HashMap<>();
                            point.put("id", d.id());
                            point.put("vector", vector);
                            point.put("payload", payload);
                            points.add(point);
                        }
                    }
                    java.util.Map<String, Object> body = new java.util.HashMap<>();
                    body.put("points", points);
                    String json = new ObjectMapper().writeValueAsString(body);
                    OkHttpClient http = new OkHttpClient();
                    Request.Builder rb = new Request.Builder()
                        .url(qdrantUrl.replaceAll("/$", "") + "/collections/" + collection + "/points")
                        .post(RequestBody.create(json, MediaType.parse("application/json")))
                        .addHeader("Content-Type", "application/json");
                    if (qdrantApiKey != null && !qdrantApiKey.isBlank()) rb.addHeader("api-key", qdrantApiKey);
                    try (Response res = http.newCall(rb.build()).execute()) {
                        if (!res.isSuccessful()) throw new RuntimeException("Qdrant upsert failed: " + res.code());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Indexing failed: " + e.getMessage(), e);
                }
            }

            record Chunk(int index, String text) {}
            private java.util.List<Chunk> chunk(String text, ChunkingOptions opt) {
                java.util.List<Chunk> out = new java.util.ArrayList<>();
                if (text == null || text.isBlank()) return out;
                int size = opt.size();
                int overlap = Math.max(0, Math.min(opt.overlap(), size - 1));
                int start = 0; int idx = 0; int len = text.length();
                while (start < len) {
                    int end = Math.min(start + size, len);
                    out.add(new Chunk(idx++, text.substring(start, end)));
                    if (end == len) break;
                    start = end - overlap;
                    if (start < 0) start = 0;
                }
                return out;
            }
        };
    }

    @Override
    public RAGApi ragApi() {
        return new RAGApi() {
            @Override
            public RAGResponse ask(String question) { return AIServiceImpl.this.rag(question); }
            @Override
            public RAGResponse ask(String question, RAGOptions options) { return AIServiceImpl.this.rag(question, options); }
        };
    }

    @Override
    public Agents agents() {
        return config -> {
            // Minimal agent wrapper; extend with tools later
            return input -> new AgentResult(
                AIServiceImpl.this.ask("You are a helpful assistant", input, AIOptions.defaults()),
                java.util.List.of()
            );
        };
    }

    @Override
    public String ask(String systemPrompt, String userMessage) {
        return ask(systemPrompt, userMessage, AIOptions.defaults());
    }

    @Override
    public String ask(String systemPrompt, String userMessage, AIOptions options) {
        // Check cache first (exact match for MVP)
        String cacheKey = buildCacheKey(systemPrompt, userMessage, options);
        if (cachingEnabled) {
            Optional<String> cached = cache.get(cacheKey, String.class);
            if (cached.isPresent()) {
                return cached.get();
            }
        }

        // Call provider
        com.akilisha.oss.roya.plugins.ai.LLMResponse response = provider.complete(systemPrompt, userMessage, options);

        // Cache the response (default 24 hour TTL)
        if (cachingEnabled) {
            cache.set(cacheKey, response.text(), Duration.ofHours(24));
        }

        // TODO: Track costs via Metrics plugin
        // TODO: Log token usage

        return response.text();
    }

    @Override
    public <T> T extract(Class<T> type, String prompt) {
        // Use optimized settings for extraction (lower temperature, deterministic)
        return extract(type, prompt, AIOptions.forExtraction());
    }

    @Override
    public <T> T extract(Class<T> type, String prompt, AIOptions options) {
        if (!type.isRecord()) {
            throw new AIException("extract() requires a record type, got: " + type.getSimpleName());
        }

        // Build structured extraction prompt
        String systemPrompt = String.format(
            "Extract information from the following text and return it as JSON matching this structure: %s",
            type.getSimpleName()
        );

        // Check cache
        String cacheKey = buildExtractCacheKey(type, prompt, options);
        if (cachingEnabled) {
            Optional<T> cached = cache.get(cacheKey, type);
            if (cached.isPresent()) {
                return cached.get();
            }
        }

        // Call provider with JSON mode
        com.akilisha.oss.roya.plugins.ai.LLMResponse response = provider.completeJson(systemPrompt, prompt, options);
        String jsonText = response.text();

        // Clean JSON (remove markdown code blocks if present)
        jsonText = cleanJson(jsonText);

        try {
            // Deserialize to record type
            T result = objectMapper.readValue(jsonText, type);

            // Cache the result
            if (cachingEnabled) {
                cache.set(cacheKey, result, Duration.ofHours(24));
            }

            return result;
        } catch (Exception e) {
            // Log helpful debug info (first 500 chars of model output)
            String preview = jsonText == null ? "<null>" : jsonText.substring(0, Math.min(500, jsonText.length()));
            System.err.println("[AI.extract] Deserialization failed for type " + type.getSimpleName());
            System.err.println("[AI.extract] Model JSON preview: " + preview);
            e.printStackTrace();
            throw new AIException("Failed to extract " + type.getSimpleName() + " from response: " + e.getMessage(), e);
        }
    }

    @Override
    public void stream(String systemPrompt, String userMessage, Consumer<String> onToken) {
        stream(systemPrompt, userMessage, AIOptions.defaults(), onToken);
    }

    @Override
    public void stream(String systemPrompt, String userMessage, AIOptions options, Consumer<String> onToken) {
        // Streaming doesn't cache (can't cache partial responses)
        provider.stream(systemPrompt, userMessage, options, onToken);
    }

    @Override
    public RAGResponse rag(String question) {
        return rag(question, RAGOptions.defaults());
    }

    @Override
    public RAGResponse rag(String question, RAGOptions options) {
        // Qdrant-only retrieval (no embedded)
        try {
            String qdrantUrl = System.getProperty("qdrant.url",
                System.getenv().getOrDefault("QDRANT_URL", "http://localhost:6333"));
            String qdrantApiKey = System.getProperty("qdrant.apiKey",
                System.getenv().getOrDefault("QDRANT_API_KEY", ""));
            String openAiKey = System.getProperty("ai.openai.apiKey",
                Optional.ofNullable(System.getenv("AI_OPENAI_API_KEY"))
                    .orElse(System.getenv("OPENAI_API_KEY")));

            if (openAiKey == null || openAiKey.isBlank()) {
                throw new AIException("OPENAI_API_KEY (or ai.openai.apiKey) is required for RAG embeddings");
            }

            String collection = System.getProperty("rag.collection",
                System.getenv().getOrDefault("RAG_COLLECTION", "docs"));
            int topK = options.topK();
            double minScore = options.minScore() != null ? options.minScore() : 0.0;

            // Embed question via OpenAI
            float[] queryVector = embedWithOpenAI(openAiKey, question);

            // Search Qdrant
            java.util.List<com.akilisha.oss.roya.plugins.ai.Document> sources = new java.util.ArrayList<>();
            StringBuilder contextBuilder = new StringBuilder();
            for (var hit : qdrantSearch(qdrantUrl, qdrantApiKey, collection, queryVector, topK)) {
                double score = ((Number) hit.getOrDefault("score", 0.0)).doubleValue();
                if (score < minScore) continue;
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> payload = (java.util.Map<String, Object>) hit.getOrDefault("payload", java.util.Map.of());
                String text = (String) payload.getOrDefault("content", "");
                contextBuilder.append(text).append("\n\n");
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> md = (java.util.Map<String, Object>) payload.getOrDefault("metadata", java.util.Map.of());
                String id = String.valueOf(hit.getOrDefault("id", ""));
                sources.add(new com.akilisha.oss.roya.plugins.ai.Document(text, id, md));
            }

            String context = contextBuilder.toString().trim();
            if (!context.isBlank()) {
                String systemPrompt = "You are a helpful assistant. Answer the question based ONLY on the provided context. " +
                        "If the context doesn't contain enough information to answer, say so.";
                String userPrompt = String.format("Context:\n%s\n\nQuestion: %s\n\nAnswer based on the context above:",
                    context, question);
                String ragAnswer = ask(systemPrompt, userPrompt, options.aiOptions());
                return new RAGResponse(ragAnswer, sources);
            }
        } catch (Exception e) {
            System.err.println("[AI.rag] Qdrant retrieval failed: " + e.getMessage());
        }

        // Fallback: simple answer without retrieval
        String answer = ask(
            "You are a helpful assistant. Answer the question as best as you can.",
            question,
            options.aiOptions()
        );
        return new RAGResponse(answer, java.util.List.of());
    }

    private float[] embedWithOpenAI(String apiKey, String text) {
        OpenAiService svc = new OpenAiService(apiKey);
        String model = System.getProperty("vector.openai.model",
            System.getenv().getOrDefault("VECTOR_OPENAI_MODEL", "text-embedding-3-small"));
        EmbeddingRequest req = EmbeddingRequest.builder()
            .model(model)
            .input(java.util.List.of(text))
            .build();
        var res = svc.createEmbeddings(req);
        java.util.List<Double> vals = res.getData().get(0).getEmbedding();
        float[] vec = new float[vals.size()];
        for (int i = 0; i < vals.size(); i++) vec[i] = vals.get(i).floatValue();
        return vec;
    }

    private java.util.List<java.util.Map<String, Object>> qdrantSearch(String baseUrl, String apiKey, String collection, float[] vector, int topK) throws Exception {
        OkHttpClient http = QDRANT_HTTP;
        java.util.List<Double> vec = new java.util.ArrayList<>(vector.length);
        for (float v : vector) vec.add((double) v);
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("vector", vec);
        body.put("limit", topK);
        String json = new ObjectMapper().writeValueAsString(body);
        Request.Builder rb = new Request.Builder()
            .url(baseUrl.replaceAll("/$", "") + "/collections/" + collection + "/points/search")
            .post(RequestBody.create(json, MediaType.parse("application/json")))
            .addHeader("Content-Type", "application/json");
        if (apiKey != null && !apiKey.isBlank()) rb.addHeader("api-key", apiKey);
        int attempts = 0;
        Exception last = null;
        while (attempts < 3) {
            attempts++;
            try (Response res = http.newCall(rb.build()).execute()) {
                if (!res.isSuccessful()) throw new RuntimeException("Qdrant search failed: " + res.code());
                String out = res.body().string();
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> parsed = new ObjectMapper().readValue(out, java.util.Map.class);
                @SuppressWarnings("unchecked")
                java.util.List<java.util.Map<String, Object>> result = (java.util.List<java.util.Map<String, Object>>) parsed.getOrDefault("result", java.util.List.of());
                return result;
            } catch (Exception e) {
                last = e;
                try { Thread.sleep(250L * attempts); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
        throw new RuntimeException("Qdrant search failed after retries: " + (last != null ? last.getMessage() : "unknown"), last);
    }

    private java.util.List<float[]> embedBatchWithOpenAI(String apiKey, java.util.List<String> texts) {
        OpenAiService svc = new OpenAiService(apiKey);
        String model = System.getProperty("vector.openai.model",
            System.getenv().getOrDefault("VECTOR_OPENAI_MODEL", "text-embedding-3-small"));
        EmbeddingRequest req = EmbeddingRequest.builder()
            .model(model)
            .input(texts)
            .build();
        var res = svc.createEmbeddings(req);
        java.util.List<float[]> out = new java.util.ArrayList<>(res.getData().size());
        for (var d : res.getData()) {
            java.util.List<Double> vals = d.getEmbedding();
            float[] vec = new float[vals.size()];
            for (int i = 0; i < vals.size(); i++) vec[i] = vals.get(i).floatValue();
            out.add(vec);
        }
        return out;
    }

    @Override
    public <T> T provider(Class<T> providerType) {
        if (providerType.isInstance(provider)) {
            return providerType.cast(provider);
        }
        // Special case for OpenAI
        if (provider instanceof com.akilisha.oss.roya.plugins.ai.providers.OpenAIClient) {
            if (providerType == com.theokanning.openai.service.OpenAiService.class) {
                return providerType.cast(((com.akilisha.oss.roya.plugins.ai.providers.OpenAIClient) provider).getOpenAiService());
            }
        }
        return null;
    }

    @Override
    public AIResponse<String> askWithMetadata(String systemPrompt, String userMessage, AIOptions options) {
        // Check cache first
        String cacheKey = buildCacheKey(systemPrompt, userMessage, options);
        if (cachingEnabled) {
            Optional<String> cached = cache.get(cacheKey, String.class);
            if (cached.isPresent()) {
                // Return cached response with metadata (cost = 0 since cached)
                // Note: We don't have token info for cached responses, so estimate based on response length
                int estimatedTokens = cached.get().length() / 4; // Rough estimate: ~4 chars per token
                return new AIResponse<>(
                    cached.get(),
                    options.model(),
                    estimatedTokens,  // Estimated (we don't know the actual)
                    estimatedTokens,  // Estimated
                    estimatedTokens * 2,  // Estimated total
                    0.0,  // Cached = FREE
                    true  // This was cached!
                );
            }
        }

        // Call provider
        LLMResponse response = provider.complete(systemPrompt, userMessage, options);

        // Cache the response
        if (cachingEnabled) {
            cache.set(cacheKey, response.text(), Duration.ofHours(24));
        }

        // Build metadata response
        double cost = response.calculateCost();
        return new AIResponse<>(
            response.text(),
            response.model(),
            response.promptTokens(),
            response.completionTokens(),
            response.totalTokens(),
            cost,
            false  // Not cached (fresh API call)
        );
    }

    @Override
    public <T> AIResponse<T> extractWithMetadata(Class<T> type, String prompt, AIOptions options) {
        if (!type.isRecord()) {
            throw new AIException("extract() requires a record type, got: " + type.getSimpleName());
        }

        // Build structured extraction prompt
        String systemPrompt = String.format(
            "Extract information from the following text and return it as JSON matching this structure: %s",
            type.getSimpleName()
        );

        // Check cache
        String cacheKey = buildExtractCacheKey(type, prompt, options);
        if (cachingEnabled) {
            Optional<T> cached = cache.get(cacheKey, type);
            if (cached.isPresent()) {
                // Return cached result with metadata (cost = 0)
                int estimatedTokens = prompt.length() / 4; // Rough estimate
                return new AIResponse<>(
                    cached.get(),
                    options.model(),
                    estimatedTokens,
                    estimatedTokens,
                    estimatedTokens * 2,
                    0.0,  // Cached = FREE
                    true  // This was cached!
                );
            }
        }

        // Call provider with JSON mode
        LLMResponse response = provider.completeJson(systemPrompt, prompt, options);
        String jsonText = response.text();

        // Clean JSON
        jsonText = cleanJson(jsonText);

        try {
            // Deserialize to record type
            T result = objectMapper.readValue(jsonText, type);

            // Cache the result
            if (cachingEnabled) {
                cache.set(cacheKey, result, Duration.ofHours(24));
            }

            // Build metadata response
            double cost = response.calculateCost();
            return new AIResponse<>(
                result,
                response.model(),
                response.promptTokens(),
                response.completionTokens(),
                response.totalTokens(),
                cost,
                false  // Not cached (fresh API call)
            );
        } catch (Exception e) {
            // Log helpful debug info
            String preview = jsonText == null ? "<null>" : jsonText.substring(0, Math.min(500, jsonText.length()));
            System.err.println("[AI.extract] Deserialization failed for type " + type.getSimpleName());
            System.err.println("[AI.extract] Model JSON preview: " + preview);
            e.printStackTrace();
            throw new AIException("Failed to extract " + type.getSimpleName() + " from response: " + e.getMessage(), e);
        }
    }

    /**
     * Build cache key for ask() calls.
     */
    private String buildCacheKey(String systemPrompt, String userMessage, AIOptions options) {
        // Include model in key (different models may give different results)
        return "ai:ask:" + options.model() + ":" + 
               Integer.toHexString((systemPrompt + userMessage).hashCode());
    }

    /**
     * Build cache key for extract() calls.
     */
    private String buildExtractCacheKey(Class<?> type, String prompt, AIOptions options) {
        return "ai:extract:" + type.getName() + ":" + options.model() + ":" +
               Integer.toHexString(prompt.hashCode());
    }

    /**
     * Clean JSON text (remove markdown code blocks, etc.).
     */
    private String cleanJson(String jsonText) {
        String cleaned = jsonText.trim();
        // Remove markdown code blocks if present
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }
}

