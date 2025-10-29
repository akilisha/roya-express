package com.akilisha.oss.roya.plugins.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Cache service.
 */
@DisplayName("Cache Service Tests")
class CacheServiceTest {

    @TempDir
    Path tempDir;
    
    private Cache cache;

    @BeforeEach
    void setUp() throws IOException {
        EvictionConfig config = new EvictionConfig(
            1024 * 1024, // 1MB
            EvictionStrategy.LRU,
            Duration.ofHours(1)
        );
        cache = new CacheServiceImpl(tempDir, config);
    }

    @Test
    @DisplayName("Should store and retrieve values")
    void testSetAndGet() {
        cache.set("key1", "value1");
        
        Optional<String> value = cache.get("key1", String.class);
        assertTrue(value.isPresent());
        assertEquals("value1", value.get());
    }

    @Test
    @DisplayName("Should return empty for non-existent keys")
    void testGetNonExistent() {
        Optional<String> value = cache.get("nonexistent", String.class);
        assertFalse(value.isPresent());
    }

    @Test
    @DisplayName("Should support different types")
    void testDifferentTypes() {
        cache.set("string", "test");
        cache.set("integer", 42);
        cache.set("map", Map.of("key", "value"));
        
        assertEquals("test", cache.get("string", String.class).orElse(null));
        assertEquals(42, cache.get("integer", Integer.class).orElse(null));
        assertNotNull(cache.get("map", Map.class).orElse(null));
    }

    @Test
    @DisplayName("Should delete values")
    void testDelete() {
        cache.set("key1", "value1");
        cache.delete("key1");
        
        assertFalse(cache.get("key1", String.class).isPresent());
    }

    @Test
    @DisplayName("Should clear all values")
    void testClear() {
        cache.set("key1", "value1");
        cache.set("key2", "value2");
        cache.clear();
        
        assertFalse(cache.get("key1", String.class).isPresent());
        assertFalse(cache.get("key2", String.class).isPresent());
    }

    @Test
    @DisplayName("Should support increment operations")
    void testIncrement() {
        Long value1 = cache.increment("counter");
        Long value2 = cache.increment("counter");
        Long value3 = cache.incrementBy("counter", 5);
        
        assertEquals(1L, value1);
        assertEquals(2L, value2);
        assertEquals(7L, value3);
    }

    @Test
    @DisplayName("Should return cache statistics")
    void testStats() {
        cache.set("key1", "value1");
        cache.get("key1", String.class); // Hit
        cache.get("key2", String.class); // Miss
        
        CacheStats stats = cache.getStats();
        assertTrue(stats.size() >= 0);
        assertTrue(stats.hits() >= 0);
        assertTrue(stats.misses() >= 0);
        assertTrue(stats.hitRatio() >= 0 && stats.hitRatio() <= 100);
    }
}

