package com.akilisha.oss.roya.docuRoya.routes;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.akilisha.oss.roya.core.middleware.RateLimit;
import com.akilisha.oss.roya.plugins.cache.Cache;
import com.akilisha.oss.roya.plugins.metrics.Metrics;
import com.akilisha.oss.roya.plugins.objectstorage.ObjectStorage;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Testing routes for comprehensive feature verification.
 *
 * These endpoints are specifically designed to test:
 * - Cache plugin (explicit operations)
 * - Object Storage plugin (listing, deletion, presigned URLs)
 * - Metrics plugin (custom metrics)
 * - Rate Limiting (stress testing endpoint)
 * - Middleware integration
 */
public class TestingRouter {
    private final Roya app;

    public TestingRouter(Roya app) {
        this.app = app;
    }

    public void register() {
        // ========== CACHE TESTING ==========

        // Cache Set/Get Test - Store and retrieve arbitrary data
        app.post("/api/test/cache/set", (Request req, Response res, Next next) -> {
            Map<String, Object> body = req.body(Map.class);
            String key = (String) body.get("key");
            Object value = body.get("value");
            int ttl = (Integer) body.getOrDefault("ttl", 3600); // Default 1 hour

            var cache = req.get(Cache.class);
            cache.set(key, value, Duration.ofSeconds(ttl));

            res.status(201).json(Map.of(
                    "message", "Cache entry stored",
                    "key", key,
                    "ttl", ttl
            ));
        });

        app.get("/api/test/cache/get/:key", (Request req, Response res, Next next) -> {
            String key = req.params().get("key").orElse("");

            var cache = req.get(Cache.class);
            Optional<Object> value = cache.get(key, Object.class);

            if (value.isEmpty()) {
                res.status(404).json(Map.of(
                        "error", "Cache miss",
                        "key", key
                ));
                return;
            }

            res.json(Map.of(
                    "status", "Cache hit",
                    "key", key,
                    "value", value.get()
            ));
        });

        // Cache Stats - Get cache statistics
        app.get("/api/test/cache/stats", (Request req, Response res, Next next) -> {
            // This endpoint demonstrates manual cache inspection
            // In production, use metrics endpoint for cache stats
            res.json(Map.of(
                    "message", "Check /metrics for cache statistics",
                    "hint", "Look for cache.* metrics in Prometheus output"
            ));
        });

        // Cache Invalidation Test - Clear specific cache entry
        app.delete("/api/test/cache/invalidate/:key", (Request req, Response res, Next next) -> {
            String key = req.params().get("key").orElse("");

            var cache = req.get(Cache.class);
            cache.delete(key);
            res.json(Map.of(
                    "message", "Cache entry invalidated",
                    "key", key
            ));
        });

        // ========== OBJECT STORAGE TESTING ==========

        // List all files
        app.get("/api/test/storage/list", (Request req, Response res, Next next) -> {
            String prefix = req.query().get("prefix").orElse("");
            String bucket = req.query().get("bucket").orElse("ducuroya");

            var storage = req.get(ObjectStorage.class);
            var objects = storage.list(bucket, prefix);

            res.json(Map.of(
                    "bucket", bucket,
                    "prefix", prefix,
                    "count", objects.size(),
                    "objects", objects
            ));
        });

        // Delete file
        app.delete("/api/test/storage/delete/:key", (Request req, Response res, Next next) -> {
            String key = req.params().get("key").orElse("");
            String bucket = req.query().get("bucket").orElse("ducuroya");

            var storage = req.get(ObjectStorage.class);
            boolean deleted = storage.delete(bucket, key);

            if (!deleted) {
                res.status(404).json(Map.of(
                        "error", "Object not found",
                        "bucket", bucket,
                        "key", key
                ));
                return;
            }

            res.json(Map.of(
                    "message", "Object deleted",
                    "bucket", bucket,
                    "key", key
            ));
        });

        // Presigned PUT URL (for direct client uploads)
        app.post("/api/test/storage/presigned-put", (Request req, Response res, Next next) -> {
            Map<String, Object> body = req.body(Map.class);
            String key = (String) body.get("key");
            String contentType = (String) body.getOrDefault("contentType", "application/octet-stream");
            int ttl = (Integer) body.getOrDefault("ttl", 3600);
            String bucket = (String) body.getOrDefault("bucket", "ducuroya");

            var storage = req.get(ObjectStorage.class);

            try {
                var url = storage.presignedPut(bucket, key, Duration.ofSeconds(ttl), contentType);
                res.json(Map.of(
                        "url", url.toString(),
                        "bucket", bucket,
                        "key", key,
                        "contentType", contentType,
                        "ttl", ttl
                ));
            } catch (UnsupportedOperationException e) {
                res.status(501).json(Map.of(
                        "error", "Presigned PUT not supported",
                        "message", e.getMessage()
                ));
            }
        });

        // Download object (with metadata)
        app.get("/api/test/storage/get/:key", (Request req, Response res, Next next) -> {
            String key = req.params().get("key").orElse("");
            String bucket = req.query().get("bucket").orElse("ducuroya");

            var storage = req.get(ObjectStorage.class);
            var objData = storage.get(bucket, key);

            if (objData.isEmpty()) {
                res.status(404).json(Map.of(
                        "error", "Object not found",
                        "bucket", bucket,
                        "key", key
                ));
                return;
            }

            res.json(Map.of(
                    "bucket", bucket,
                    "key", key,
                    "contentType", objData.get().contentType(),
                    "size", objData.get().size(),
                    "metadata", objData.get().metadata()
            ));
        });

        // ========== METRICS TESTING ==========

        // Increment custom counter
        app.post("/api/test/metrics/counter", (Request req, Response res, Next next) -> {
            Map<String, Object> body = req.body(Map.class);
            String name = (String) body.get("name");
            long increment = ((Number) body.getOrDefault("increment", 1)).longValue();

            var metrics = req.get(Metrics.class);
            var counter = metrics.counter(name); // Metrics.counter() doesn't support tags in current version
            counter.increment(increment);

            res.json(Map.of(
                    "message", "Counter incremented",
                    "name", name,
                    "increment", increment
            ));
        });

        // Record gauge value
        app.post("/api/test/metrics/gauge", (Request req, Response res, Next next) -> {
            Map<String, Object> body = req.body(Map.class);
            String name = (String) body.get("name");
            double value = ((Number) body.get("value")).doubleValue();

            var metrics = req.get(Metrics.class);
            // Gauge requires a Supplier<Number>, so we'll create a simple one
            var gauge = metrics.gauge(name, () -> value);

            res.json(Map.of(
                    "message", "Gauge registered",
                    "name", name,
                    "value", value
            ));
        });

        // Record timer duration
        app.post("/api/test/metrics/timer", (Request req, Response res, Next next) -> {
            Map<String, Object> body = req.body(Map.class);
            String name = (String) body.get("name");
            long durationMs = ((Number) body.get("durationMs")).longValue();

            var metrics = req.get(Metrics.class);
            var timer = metrics.timer(name); // Metrics.timer() doesn't support tags in current version
            timer.record(Duration.ofMillis(durationMs));

            res.json(Map.of(
                    "message", "Timer recorded",
                    "name", name,
                    "durationMs", durationMs
            ));
        });

        // ========== RATE LIMITING TESTING ==========

        // Endpoint with aggressive rate limiting (for stress testing)
        app.get("/api/test/rate-limit/aggressive",
                RateLimit.builder()
                        .max(10)  // Only 10 requests
                        .window(Duration.ofMinutes(1))  // Per minute
                        .build(),
                (Request req, Response res, Next next) -> {
                    res.json(Map.of(
                            "message", "Success! Rate limit not exceeded",
                            "hint", "This endpoint allows 10 requests per minute. Make 11 requests to see rate limiting."
                    ));
                });

        // Endpoint with moderate rate limiting (for normal usage testing)
        app.get("/api/test/rate-limit/moderate",
                RateLimit.builder()
                        .max(50)
                        .window(Duration.ofMinutes(1))
                        .build(),
                (Request req, Response res, Next next) -> {
                    res.json(Map.of(
                            "message", "Success! Rate limit not exceeded",
                            "hint", "This endpoint allows 50 requests per minute."
                    ));
                });

        // Endpoint with per-IP rate limiting (demonstrates per-client limits)
        app.get("/api/test/rate-limit/per-ip",
                RateLimit.builder()
                        .max(20)
                        .window(Duration.ofMinutes(1))
                        .build(), // Default is already per-IP via req.ip()
                (Request req, Response res, Next next) -> {
                    String clientIp = req.ip();
                    res.json(Map.of(
                            "message", "Success! Rate limit not exceeded",
                            "clientIp", clientIp,
                            "hint", "Each IP gets 20 requests per minute independently."
                    ));
                });

        // ========== MIDDLEWARE TESTING ==========

        // Test CORS headers
        app.get("/api/test/cors", (Request req, Response res, Next next) -> {
            res.json(Map.of(
                    "message", "CORS headers should be present in response",
                    "hint", "Check response headers for Access-Control-Allow-Origin, etc."
            ));
        });

        // Test request/response logging (Morgan)
        app.get("/api/test/logging", (Request req, Response res, Next next) -> {
            res.json(Map.of(
                    "message", "Check console for structured JSON logs",
                    "hint", "Morgan middleware should log this request with trace ID"
            ));
        });

        // ========== GENERAL TESTING ==========

        // Echo test - return request details
        app.post("/api/test/echo", (Request req, Response res, Next next) -> {
            res.json(Map.of(
                    "method", req.method(),
                    "path", req.path(),
                    "headers", req.headers().all(),
                    "query", req.query().all(),
                    "body", req.body()
            ));
        });

        // Slow endpoint (for timeout testing)
        app.get("/api/test/slow", (Request req, Response res, Next next) -> {
            int delayMs = Integer.parseInt(req.query().get("delay").orElse("1000"));
            try {
                Thread.sleep(delayMs);
                res.json(Map.of(
                        "message", "Slow response completed",
                        "delayMs", delayMs
                ));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                res.status(500).json(Map.of("error", "Interrupted"));
            }
        });

        // Error endpoint (for error handling testing)
        app.get("/api/test/error", (Request req, Response res, Next next) -> {
            throw new RuntimeException("Intentional test error - this should be caught by error handler");
        });
    }
}
