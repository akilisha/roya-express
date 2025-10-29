package com.akilisha.oss.roya.plugins.cache;

import java.util.List;

/**
 * Cache eviction strategy.
 *
 * Determines which entries to evict when cache reaches capacity.
 */
public enum EvictionStrategy {
    /**
     * Least Recently Used - evict entries that haven't been accessed recently.
     * Best for: Temporal locality (recent data likely accessed again)
     */
    LRU,

    /**
     * Least Frequently Used - evict entries with lowest access count.
     * Best for: Popular content caching (videos, images)
     */
    LFU,

    /**
     * Time-To-Live - evict expired entries only.
     * Best for: Time-sensitive data (sessions, temporary data)
     */
    TTL,

    /**
     * Size-based - evict largest entries first.
     * Best for: Memory-constrained environments
     */
    SIZE,

    /**
     * Adaptive - combines LRU + LFU + SIZE scores.
     * Best for: General-purpose caching
     */
    ADAPTIVE;

    /**
     * Parse eviction strategy from string.
     *
     * @param name Strategy name (case-insensitive)
     * @return Eviction strategy
     * @throws IllegalArgumentException if name doesn't match any strategy
     */
    public static EvictionStrategy parse(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown eviction strategy: " + name + 
                ". Must be one of: " + List.of(values()));
        }
    }
}

