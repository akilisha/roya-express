package com.akilisha.oss.roya.plugins.vector.backends;

import com.akilisha.oss.roya.plugins.vector.Document;
import com.akilisha.oss.roya.plugins.vector.DocumentMatch;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Qdrant backend using REST API.
 *
 * Minimal implementation: create collection, upsert, search.
 */
public class QdrantVectorBackend {
    private final String baseUrl; // e.g., http://localhost:6333
    private final String apiKey;  // optional
    private final OkHttpClient http;
    private final ObjectMapper mapper;

    public QdrantVectorBackend(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
        this.http = new OkHttpClient();
        this.mapper = new ObjectMapper();
    }

    private Request.Builder requestBuilder(String url) {
        Request.Builder b = new Request.Builder().url(url);
        if (apiKey != null && !apiKey.isBlank()) {
            b.addHeader("api-key", apiKey);
        }
        b.addHeader("Content-Type", "application/json");
        return b;
    }

    public void ensureCollection(String collection, int vectorSize) {
        try {
            // Create collection (idempotent)
            Map<String, Object> vectors = Map.of(
                "size", vectorSize,
                "distance", "Cosine"
            );
            Map<String, Object> payload = Map.of("vectors", vectors);
            String bodyJson = mapper.writeValueAsString(payload);

            Request req = requestBuilder(baseUrl + "/collections/" + collection)
                .put(RequestBody.create(bodyJson, MediaType.parse("application/json")))
                .build();
            try (Response res = http.newCall(req).execute()) {
                // 200/201/409 acceptable (already exists)
                if (!res.isSuccessful() && res.code() != 409) {
                    throw new RuntimeException("Qdrant create collection failed: " + res.code());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Qdrant error: " + e.getMessage(), e);
        }
    }

    public void upsert(String collection, List<Document> documents) {
        try {
            // Qdrant upsert points format
            List<Map<String, Object>> points = new ArrayList<>();
            for (Document doc : documents) {
                if (doc.embedding().isEmpty()) continue; // skip without embedding
                float[] emb = doc.embedding().get();
                List<Double> vector = new ArrayList<>(emb.length);
                for (float v : emb) vector.add((double) v);
                Map<String, Object> point = new HashMap<>();
                point.put("id", doc.id());
                point.put("vector", vector);
                Map<String, Object> payload = new HashMap<>();
                payload.put("content", doc.content());
                payload.put("metadata", doc.metadata());
                point.put("payload", payload);
                points.add(point);
            }
            Map<String, Object> body = Map.of("points", points);
            String bodyJson = mapper.writeValueAsString(body);

            Request req = requestBuilder(baseUrl + "/collections/" + collection + "/points")
                .post(RequestBody.create(bodyJson, MediaType.parse("application/json")))
                .build();
            try (Response res = http.newCall(req).execute()) {
                if (!res.isSuccessful()) {
                    throw new RuntimeException("Qdrant upsert failed: " + res.code());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Qdrant error: " + e.getMessage(), e);
        }
    }

    public List<DocumentMatch> search(String collection, float[] queryEmbedding, int topK, double minScore) {
        try {
            List<Double> vector = new ArrayList<>(queryEmbedding.length);
            for (float v : queryEmbedding) vector.add((double) v);
            Map<String, Object> body = new HashMap<>();
            body.put("vector", vector);
            body.put("limit", topK);
            String bodyJson = mapper.writeValueAsString(body);

            Request req = requestBuilder(baseUrl + "/collections/" + collection + "/points/search")
                .post(RequestBody.create(bodyJson, MediaType.parse("application/json")))
                .build();
            try (Response res = http.newCall(req).execute()) {
                if (!res.isSuccessful()) {
                    throw new RuntimeException("Qdrant search failed: " + res.code());
                }
                String json = res.body().string();
                Map<?, ?> parsed = mapper.readValue(json, Map.class);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> results = (List<Map<String, Object>>) parsed.get("result");
                if (results == null) return List.of();
                return results.stream()
                    .map(r -> {
                        // Qdrant ID can be string or number
                        Object idObj = r.get("id");
                        String id = idObj != null ? idObj.toString() : "unknown";
                        double score = ((Number) r.getOrDefault("score", 0.0)).doubleValue();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> payload = (Map<String, Object>) r.getOrDefault("payload", Map.of());
                        String content = (String) payload.getOrDefault("content", "");
                        @SuppressWarnings("unchecked")
                        Map<String, Object> metadata = (Map<String, Object>) payload.getOrDefault("metadata", Map.of());
                        Document doc = Document.of(id, content, metadata);
                        return new DocumentMatch(doc, score);
                    })
                    .filter(m -> m.score() >= minScore)
                    .collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new RuntimeException("Qdrant error: " + e.getMessage(), e);
        }
    }
}
