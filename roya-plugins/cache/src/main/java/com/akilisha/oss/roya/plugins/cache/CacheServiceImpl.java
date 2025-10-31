package com.akilisha.oss.roya.plugins.cache;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache service implementation.
 *
 * Wraps FFMCacheBackend and provides the Cache interface.
 */
public class CacheServiceImpl implements Cache {

    private final FFMCacheBackend backend;
    private final Duration defaultTtl;
    private final Map<String, Long> numericCache = new ConcurrentHashMap<>(); // For increment operations

    public CacheServiceImpl(Path cacheDir, EvictionConfig config) throws IOException {
        this.backend = new FFMCacheBackend(cacheDir, config);
        this.defaultTtl = config.defaultTtl();
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        return backend.get(key, type);
    }

    @Override
    public void set(String key, Object value) {
        set(key, value, defaultTtl);
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        backend.set(key, value, ttl);
    }

    @Override
    public void delete(String key) {
        backend.delete(key);
        numericCache.remove(key);
    }

    @Override
    public void clear() {
        backend.clear();
        numericCache.clear();
    }

    @Override
    public <T> Map<String, T> getMulti(List<String> keys, Class<T> type) {
        Map<String, T> result = new HashMap<>();
        for (String key : keys) {
            Optional<T> value = get(key, type);
            value.ifPresent(v -> result.put(key, v));
        }
        return result;
    }

    @Override
    public void setMulti(Map<String, Object> entries) {
        setMulti(entries, defaultTtl);
    }

    @Override
    public void setMulti(Map<String, Object> entries, Duration ttl) {
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            set(entry.getKey(), entry.getValue(), ttl);
        }
    }

    @Override
    public Set<String> keys(String pattern) {
        // Backend's keys() method is package-private, accessible from same package
        return backend.keys(pattern);
    }

    @Override
    public Long increment(String key) {
        return incrementBy(key, 1);
    }

    @Override
    public Long incrementBy(String key, long amount) {
        return numericCache.compute(key, (k, v) -> {
            // Try to get current value from cache
            Optional<Long> cached = get(key, Long.class);
            long current = cached.orElse(v != null ? v : 0L);
            long newValue = current + amount;

            // Store in backend cache (no TTL for numeric values)
            backend.set(key, newValue, defaultTtl);

            return newValue;
        });
    }

    @Override
    public CacheStats getStats() {
        return backend.getStats();
    }
}

