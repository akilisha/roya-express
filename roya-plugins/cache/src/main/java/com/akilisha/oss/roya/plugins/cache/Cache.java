package com.akilisha.oss.roya.plugins.cache;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Cache service interface.
 *
 * Provides fast, efficient caching using FFM-based memory-mapped files.
 *
 * Example usage:
 * <pre>
 * Cache cache = req.get(Cache.class);
 *
 * // Get from cache
 * Optional&lt;User&gt; cached = cache.get("user:123", User.class);
 * if (cached.isPresent()) {
 *     return cached.get();
 * }
 *
 * // Cache miss - fetch from DB
 * User user = database.getUser("123");
 *
 * // Store in cache (1 hour TTL)
 * cache.set("user:123", user, Duration.ofHours(1));
 * </pre>
 */
public interface Cache {
    /**
     * Get a value from cache.
     *
     * @param key Cache key
     * @param type Value type
     * @return Cached value or empty if not found/expired
     */
    <T> Optional<T> get(String key, Class<T> type);

    /**
     * Store a value in cache with default TTL.
     *
     * @param key Cache key
     * @param value Value to cache
     */
    void set(String key, Object value);

    /**
     * Store a value in cache with specific TTL.
     *
     * @param key Cache key
     * @param value Value to cache
     * @param ttl Time-to-live duration
     */
    void set(String key, Object value, Duration ttl);

    /**
     * Delete a value from cache.
     *
     * @param key Cache key
     */
    void delete(String key);

    /**
     * Clear all cached values.
     *
     * Use with caution - clears entire cache.
     */
    void clear();

    /**
     * Get multiple values from cache.
     *
     * @param keys List of cache keys
     * @param type Value type
     * @return Map of key to value (only includes found values)
     */
    <T> Map<String, T> getMulti(List<String> keys, Class<T> type);

    /**
     * Store multiple values in cache.
     *
     * @param entries Map of key to value
     */
    void setMulti(Map<String, Object> entries);

    /**
     * Store multiple values in cache with TTL.
     *
     * @param entries Map of key to value
     * @param ttl Time-to-live duration for all entries
     */
    void setMulti(Map<String, Object> entries, Duration ttl);

    /**
     * Get all keys matching a pattern.
     *
     * Note: Pattern matching may not be supported by all backends.
     *
     * @param pattern Pattern to match (e.g., "user:*")
     * @return Set of matching keys
     */
    Set<String> keys(String pattern);

    /**
     * Increment a numeric value atomically.
     *
     * @param key Cache key
     * @return New value after increment
     */
    Long increment(String key);

    /**
     * Increment a numeric value by specified amount.
     *
     * @param key Cache key
     * @param amount Amount to increment by
     * @return New value after increment
     */
    Long incrementBy(String key, long amount);

    /**
     * Get cache statistics.
     *
     * @return Cache statistics
     */
    CacheStats getStats();
}

