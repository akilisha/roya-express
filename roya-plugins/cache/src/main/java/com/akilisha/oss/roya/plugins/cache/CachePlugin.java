package com.akilisha.oss.roya.plugins.cache;

import com.akilisha.oss.roya.api.plugin.RoyaPlugin;
import com.akilisha.oss.roya.api.plugin.Services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Cache plugin - registers FFM-based cache service.
 *
 * Provides high-performance caching using FFM memory-mapped files.
 * No external dependencies - pure Java solution.
 */
public class CachePlugin implements RoyaPlugin {

    @Override
    public String id() {
        return "cache";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "FFM-based cache with memory-mapped files (Kafka-style)";
    }

    @Override
    public void register(Services services) {
        services.singleton(Cache.class, () -> {
            try {
                // Configuration from environment variables
                String cacheDir = System.getProperty("cache.dir", "./cache");
                long maxSizeBytes = Long.parseLong(
                    System.getProperty("cache.maxSize", "1073741824")); // 1GB default
                EvictionStrategy strategy = EvictionStrategy.parse(
                    System.getProperty("cache.eviction", "LRU"));
                Duration defaultTtl = Duration.ofSeconds(
                    Long.parseLong(System.getProperty("cache.ttl", "86400"))); // 24 hours

                EvictionConfig config = new EvictionConfig(
                    maxSizeBytes,
                    strategy,
                    defaultTtl
                );

                Path cachePath = Paths.get(cacheDir);
                Files.createDirectories(cachePath);

                return new CacheServiceImpl(cachePath, config);
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize cache", e);
            }
        });
    }

    @Override
    public void start() throws Exception {
        System.out.println("✓ CachePlugin: FFM-based cache initialized");
        System.out.println("  - Cache directory: " + System.getProperty("cache.dir", "./cache"));
        System.out.println("  - Max size: " + System.getProperty("cache.maxSize", "1073741824") + " bytes");
        System.out.println("  - Eviction: " + System.getProperty("cache.eviction", "LRU"));
    }

    @Override
    public void stop() throws Exception {
        // No cleanup needed - arena will be cleaned up automatically
    }
}

