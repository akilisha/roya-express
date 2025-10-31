package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.core.middleware.Cors;
import com.akilisha.oss.roya.core.middleware.Morgan;
import com.akilisha.oss.roya.plugins.cache.Cache;
import com.akilisha.oss.roya.plugins.cache.CacheStats;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Cache Demo - FFM-based caching demonstration.
 *
 * Shows:
 * - FFM memory-mapped file cache
 * - Cache CRUD operations
 * - TTL support
 * - Cache statistics
 * - Increment operations
 *
 * Features:
 * - Zero external dependencies (no Redis)
 * - Kafka-inspired append-only architecture
 * - Configurable eviction strategies
 */
public class CacheDemo {

    public static void main(String[] args) {
        var app = Roya.create();

        // Register Cache plugin
        var services = app.services();
        var cachePlugin = new com.akilisha.oss.roya.plugins.cache.CachePlugin();
        cachePlugin.register(services);

        // Middleware
        app.use(Morgan.combined());
        app.use(Cors.cors());
        app.use(BodyParser.bodyParser());

        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  💾 Roya Cache Demo - FFM-Based Caching                  ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║                                                          ║");
        System.out.println("║  Features:                                               ║");
        System.out.println("║  - FFM memory-mapped files (no 2GB MappedByteBuffer!)  ║");
        System.out.println("║  - Kafka-inspired append-only architecture              ║");
        System.out.println("║  - Zero external dependencies                            ║");
        System.out.println("║  - Configurable eviction (LRU, LFU, TTL, SIZE, ADAPTIVE)║");
        System.out.println("║                                                          ║");
        System.out.println("║  Endpoints:                                              ║");
        System.out.println("║  - GET  /cache/:key          Get cached value           ║");
        System.out.println("║  - POST /cache/:key           Set cached value           ║");
        System.out.println("║  - DELETE /cache/:key         Delete cached value        ║");
        System.out.println("║  - GET  /cache/stats         Cache statistics           ║");
        System.out.println("║  - POST /cache/increment/:key Increment numeric value   ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        // Get value from cache
        app.get("/cache/:key", (req, res, next) -> {
            try {
                Cache cache = req.get(Cache.class);
                String key = req.params().get("key").orElse("");

                // Try to get - we need to know the type
                // For demo, assume it's a Map or String
                Optional<Map> value = cache.get(key, Map.class);
                if (value.isEmpty()) {
                    Optional<String> strValue = cache.get(key, String.class);
                    if (strValue.isPresent()) {
                        res.json(Map.of(
                            "key", key,
                            "value", strValue.get(),
                            "type", "string"
                        ));
                    } else {
                        res.status(404).json(Map.of(
                            "error", "Not found",
                            "key", key
                        ));
                    }
                } else {
                    res.json(Map.of(
                        "key", key,
                        "value", value.get(),
                        "type", "map"
                    ));
                }
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Set value in cache
        app.post("/cache/:key", (req, res, next) -> {
            try {
                Cache cache = req.get(Cache.class);
                String key = req.params().get("key").orElse("");
                Map<String, Object> body = req.body(Map.class);
                Object value = body.get("value");
                Long ttlSeconds = body.containsKey("ttl") ? Long.parseLong(body.get("ttl").toString()) : null;

                if (ttlSeconds != null) {
                    cache.set(key, value, Duration.ofSeconds(ttlSeconds));
                } else {
                    cache.set(key, value);
                }

                res.json(Map.of(
                    "message", "Value cached",
                    "key", key,
                    "ttl", ttlSeconds != null ? ttlSeconds + " seconds" : "default"
                ));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Delete value from cache
        app.delete("/cache/:key", (req, res, next) -> {
            try {
                Cache cache = req.get(Cache.class);
                String key = req.params().get("key").orElse("");

                cache.delete(key);

                res.json(Map.of(
                    "message", "Value deleted",
                    "key", key
                ));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Cache statistics
        app.get("/cache/stats", (req, res, next) -> {
            try {
                Cache cache = req.get(Cache.class);
                CacheStats stats = cache.getStats();

                res.json(Map.of(
                    "size", stats.size(),
                    "maxSize", stats.maxSize(),
                    "hits", stats.hits(),
                    "misses", stats.misses(),
                    "hitRatio", String.format("%.2f%%", stats.hitRatio())
                ));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Increment operation
        app.post("/cache/increment/:key", (req, res, next) -> {
            try {
                Cache cache = req.get(Cache.class);
                String key = req.params().get("key").orElse("");
                Map<String, Object> body = req.body(Map.class);
                Long amount = body.containsKey("amount") ? Long.parseLong(body.get("amount").toString()) : 1L;

                Long newValue = cache.incrementBy(key, amount);

                res.json(Map.of(
                    "key", key,
                    "value", newValue,
                    "message", "Incremented by " + amount
                ));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Example: Cache-aside pattern
        app.get("/users/:id/cached", (req, res, next) -> {
            try {
                Cache cache = req.get(Cache.class);
                String userId = req.params().get("id").orElse("");
                String cacheKey = "user:" + userId;

                // Try cache first
                Optional<Map> cached = cache.get(cacheKey, Map.class);
                if (cached.isPresent()) {
                    res.json(Map.of(
                        "user", cached.get(),
                        "source", "cache",
                        "message", "Served from cache"
                    ));
                    return;
                }

                // Cache miss - simulate DB fetch
                Map<String, Object> user = new HashMap<>();
                user.put("id", userId);
                user.put("name", "User " + userId);
                user.put("email", "user" + userId + "@example.com");

                // Store in cache (1 hour TTL)
                cache.set(cacheKey, user, Duration.ofHours(1));

                res.json(Map.of(
                    "user", user,
                    "source", "database",
                    "message", "Fetched from database and cached"
                ));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        app.listen(3000, () -> {
            System.out.println("✓ Cache Demo running on http://localhost:3000\n");
            System.out.println("📝 Quick Test:");
            System.out.println("  1. POST /cache/user:123 {\"value\": {\"name\": \"John\"}, \"ttl\": 3600}");
            System.out.println("  2. GET  /cache/user:123");
            System.out.println("  3. POST /cache/increment/counter {\"amount\": 5}");
            System.out.println("  4. GET  /cache/stats\n");
        });
    }
}

