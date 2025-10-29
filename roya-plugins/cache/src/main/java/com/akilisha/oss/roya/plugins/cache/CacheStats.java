package com.akilisha.oss.roya.plugins.cache;

/**
 * Cache statistics.
 *
 * Provides information about cache usage and performance.
 */
public record CacheStats(
    long size,           // Current number of entries
    long maxSize,        // Maximum capacity
    long hits,           // Cache hits
    long misses,         // Cache misses
    double hitRatio      // Hit ratio percentage (hits / (hits + misses))
) {
    // Compact constructor with validation
    public CacheStats {
        // Allow negative initial values (will be 0 initially)
        if (size < 0) size = 0;
        if (maxSize < 0) maxSize = 0;
        if (hits < 0) hits = 0;
        if (misses < 0) misses = 0;
        if (hitRatio < 0) hitRatio = 0;
        if (hitRatio > 100) hitRatio = 100;
    }
}

