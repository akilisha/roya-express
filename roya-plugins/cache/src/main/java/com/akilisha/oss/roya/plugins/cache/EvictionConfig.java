package com.akilisha.oss.roya.plugins.cache;

import java.time.Duration;

/**
 * Configuration for cache eviction.
 */
public record EvictionConfig(
    long maxSizeBytes,        // Maximum cache size in bytes
    EvictionStrategy strategy, // Eviction strategy
    Duration defaultTtl        // Default TTL for entries
) {
    public EvictionConfig {
        if (maxSizeBytes <= 0) {
            throw new IllegalArgumentException("maxSizeBytes must be positive");
        }
        if (defaultTtl.isNegative() || defaultTtl.isZero()) {
            throw new IllegalArgumentException("defaultTtl must be positive");
        }
    }
}

