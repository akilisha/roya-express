package com.akilisha.oss.roya.plugins.cache;

import com.akilisha.oss.roya.plugins.cache.serialization.JsonSerializer;
import com.akilisha.oss.roya.plugins.cache.serialization.Serializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * FFM-based cache backend using memory-mapped files.
 *
 * Architecture:
 * - Uses FFM MemorySegment (wraps MappedByteBuffer for now; can support >2GB with multiple segments)
 * - Kafka-inspired: Append-only write log with offset index
 * - Zero-copy reads via FFM direct memory access
 * - Configurable eviction strategies
 *
 * Note: Currently uses MemorySegment.ofBuffer(MappedByteBuffer) which has 2GB limit per segment.
 * For files >2GB, we'd need multiple segments. This implementation handles up to 2GB per segment.
 */
public class FFMCacheBackend {

    // Metadata size per entry (timestamp + ttl + size)
    // Must be aligned for 8-byte access (JAVA_LONG)
    private static final int METADATA_SIZE = alignTo8(8 + 8 + 4); // 64-bit timestamp + 64-bit TTL + 32-bit size (24 bytes, aligned to 32)
    private static final long INDEX_SLOT_SIZE = 16; // hash (8 bytes) + offset (8 bytes) - already 16-byte aligned

    /**
     * Align size to 8-byte boundary (required for FFM long access).
     */
    private static int alignTo8(int size) {
        return ((size + 7) / 8) * 8;
    }

    private final Path cacheDir;
    private final EvictionConfig config;
    private final Serializer serializer;

    // FFM memory segments (mapped files)
    private final Arena arena; // Arena for managing FFM memory
    private final MemorySegment cacheFile; // Append-only cache data file
    private final MemorySegment indexFile;  // Hash index (key hash -> offset)

    // In-memory structures for eviction tracking
    private final Map<Long, EntryMetadata> entryMetadata = new ConcurrentHashMap<>();
    private final Map<String, Long> keyToHash = new ConcurrentHashMap<>();
    private final Set<String> allKeys = ConcurrentHashMap.newKeySet(); // For keys() pattern matching

    // Statistics
    private long currentSize = 0;
    private long writeOffset = METADATA_SIZE; // Skip header
    private long cacheHits = 0;
    private long cacheMisses = 0;

    // Thread safety
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Legacy constructor - creates a JsonSerializer with default ObjectMapper.
     * WARNING: This should only be used as a fallback.
     * Prefer passing ObjectMapper from services for consistency.
     */
    public FFMCacheBackend(Path cacheDir, EvictionConfig config) throws IOException {
        this(cacheDir, config, new JsonSerializer());
    }

    public FFMCacheBackend(Path cacheDir, EvictionConfig config, ObjectMapper objectMapper) throws IOException {
        this(cacheDir, config, new JsonSerializer(objectMapper));
    }

    private FFMCacheBackend(Path cacheDir, EvictionConfig config, Serializer serializer) throws IOException {
        this.cacheDir = cacheDir;
        this.config = config;
        this.serializer = serializer;

        // Create cache directory if it doesn't exist
        Files.createDirectories(cacheDir);

        // Create/Open cache files
        Path cacheFilePath = cacheDir.resolve("cache.data");
        Path indexFilePath = cacheDir.resolve("index.data");

        // Create files if they don't exist
        if (!Files.exists(cacheFilePath)) {
            Files.createFile(cacheFilePath);
        }
        if (!Files.exists(indexFilePath)) {
            Files.createFile(indexFilePath);
        }

        // Memory-map files using FFM
        // IMPORTANT: Currently using MemorySegment.ofBuffer(MappedByteBuffer)
        // - This wraps MappedByteBuffer, which has 2GB limit per segment
        // - True FFM MemorySegment (direct mapping) doesn't have this limit
        // - For MVP, 2GB per segment is sufficient (can extend to multi-segment later)
        // - To support >2GB: Use multiple segments or wait for FFM direct file mapping API
        this.arena = Arena.ofShared(); // Shared arena for long-lived segments

        try (FileChannel cacheChannel = FileChannel.open(cacheFilePath,
                StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            // FileChannel.map() requires size <= Integer.MAX_VALUE
            // Cap at Integer.MAX_VALUE - 1 to be safe
            long maxAllowedSize = Integer.MAX_VALUE - 1L; // ~2GB - 1 byte

            // Calculate desired file size (max of existing size, config, or 1MB minimum)
            long cacheFileSize = Math.max(
                    Math.max(cacheChannel.size(), config.maxSizeBytes()),
                    1024 * 1024 // At least 1MB
            );

            // Enforce hard limit for FileChannel.map()
            cacheFileSize = Math.min(cacheFileSize, maxAllowedSize);

            // Extend file if needed (but don't exceed maxAllowedSize)
            if (cacheChannel.size() < cacheFileSize && cacheFileSize <= maxAllowedSize) {
                cacheChannel.position(cacheFileSize - 1);
                cacheChannel.write(java.nio.ByteBuffer.allocate(1));
            }

            // Map file to memory segment via MappedByteBuffer
            // Convert to int for map() call (we've ensured it's <= Integer.MAX_VALUE)
            int fileSizeInt = (int) Math.min(cacheFileSize, Integer.MAX_VALUE);
            java.nio.MappedByteBuffer buffer = cacheChannel.map(
                    FileChannel.MapMode.READ_WRITE, 0, fileSizeInt);
            this.cacheFile = MemorySegment.ofBuffer(buffer);
        }

        // Index file: hash table with fixed slots
        long maxAllowedSize = Integer.MAX_VALUE - 1L; // ~2GB - 1 byte
        long indexSize = calculateIndexSize(config.maxSizeBytes());
        // Ensure minimum size, cap at Integer.MAX_VALUE
        indexSize = Math.max(indexSize, INDEX_SLOT_SIZE * 100); // At least 100 slots
        indexSize = Math.min(indexSize, maxAllowedSize);
        try (FileChannel indexChannel = FileChannel.open(indexFilePath,
                StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            // Use existing size if larger (don't shrink)
            if (indexChannel.size() > 0 && indexChannel.size() > indexSize) {
                indexSize = indexChannel.size();
                indexSize = Math.min(indexSize, maxAllowedSize); // Still cap
            }
            if (indexChannel.size() < indexSize && indexSize <= maxAllowedSize) {
                indexChannel.position(indexSize - 1);
                indexChannel.write(java.nio.ByteBuffer.allocate(1));
            }
            // Convert to int for map() call
            int indexSizeInt = (int) Math.min(indexSize, Integer.MAX_VALUE);
            java.nio.MappedByteBuffer buffer = indexChannel.map(
                    FileChannel.MapMode.READ_WRITE, 0, indexSizeInt);
            this.indexFile = MemorySegment.ofBuffer(buffer);
        }

        // Initialize write offset from file header (offset 0 is aligned)
        long savedOffset = cacheFile.get(ValueLayout.JAVA_LONG, 0);
        if (savedOffset > 0) {
            writeOffset = alignTo8((int) savedOffset);
        } else {
            writeOffset = METADATA_SIZE; // Start after header
        }
    }

    /**
     * Calculate index size based on max cache size.
     *
     * Use load factor ~0.75, so index size should be ~1.33x max entries.
     */
    private long calculateIndexSize(long maxCacheBytes) {
        // Estimate: average entry size ~1KB, so max entries = maxCacheBytes / 1024
        long maxEntries = maxCacheBytes / 1024;
        long indexSlots = (long) (maxEntries * 1.33); // Load factor 0.75
        return indexSlots * INDEX_SLOT_SIZE;
    }

    /**
     * Hash key to long value.
     */
    private long hashKey(String key) {
        return key.hashCode();
    }

    /**
     * Find index slot for a hash.
     */
    private long findIndexSlot(long hash) {
        long indexSize = indexFile.byteSize() / INDEX_SLOT_SIZE;
        long slot = Math.abs(hash) % indexSize;

        // Linear probing for collisions
        long startSlot = slot;
        while (true) {
            // INDEX_SLOT_SIZE is 16 (already aligned), so slot * 16 is always aligned
            long hashOffset = slot * INDEX_SLOT_SIZE;
            long storedHash = indexFile.get(ValueLayout.JAVA_LONG, hashOffset);

            // Empty slot or matching hash
            if (storedHash == 0 || storedHash == hash) {
                return slot;
            }

            // Next slot (wrap around)
            slot = (slot + 1) % indexSize;
            if (slot == startSlot) {
                throw new IllegalStateException("Index full - cannot find slot");
            }
        }
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        lock.readLock().lock();
        try {
            long keyHash = hashKey(key);
            long slot = findIndexSlot(keyHash);
            long slotOffset = slot * INDEX_SLOT_SIZE;

            // Check if hash matches
            long storedHash = indexFile.get(ValueLayout.JAVA_LONG, slotOffset);
            if (storedHash != keyHash) {
                cacheMisses++;
                return Optional.empty();
            }

            // Get data offset
            long dataOffset = indexFile.get(ValueLayout.JAVA_LONG, slotOffset + 8);
            if (dataOffset == 0) {
                cacheMisses++;
                return Optional.empty();
            }

            // Read metadata (ensure aligned access)
            // All offsets must be 8-byte aligned for JAVA_LONG
            long alignedDataOffset = alignTo8((int) dataOffset);
            long timestamp = cacheFile.get(ValueLayout.JAVA_LONG, alignedDataOffset);
            long ttlNanos = cacheFile.get(ValueLayout.JAVA_LONG, alignedDataOffset + 8);
            int dataSize = (int) cacheFile.get(ValueLayout.JAVA_LONG, alignedDataOffset + 16); // Read as long, cast to int

            // Check TTL
            Instant createdAt = Instant.ofEpochMilli(timestamp);
            Instant expiresAt = createdAt.plusNanos(ttlNanos);
            if (Instant.now().isAfter(expiresAt)) {
                // Expired - remove it
                delete(key);
                cacheMisses++;
                return Optional.empty();
            }

            // Read data using FFM (after aligned metadata)
            long dataReadOffset = alignedDataOffset + METADATA_SIZE;
            MemorySegment dataSegment = MemorySegment.ofArray(new byte[dataSize]);
            MemorySegment.copy(
                    cacheFile, dataReadOffset,
                    dataSegment, 0,
                    dataSize
            );
            byte[] data = dataSegment.toArray(ValueLayout.JAVA_BYTE);

            // Deserialize
            if (type == Boolean.class) {
                System.out.println("DEBUG get key=" + key + " dataSize=" + dataSize + " offset=" + dataOffset);
            }
            T value = serializer.deserialize(data, type);

            // Update eviction metadata (for LRU/LFU)
            EntryMetadata metadata = entryMetadata.get(keyHash);
            if (metadata != null) {
                EntryMetadata updated = metadata.recordAccess();
                entryMetadata.put(keyHash, updated);
            }

            cacheHits++;
            return Optional.of(value);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void set(String key, Object value, Duration ttl) {
        lock.writeLock().lock();
        try {
            if (needsEviction()) {
                evict();
            }

            byte[] data = serializer.serialize(value);
            int dataSize = data.length;
            int entrySize = METADATA_SIZE + dataSize;

            long dataOffset;
            while (true) {
                long alignedOffset = alignTo8((int) writeOffset);
                if (alignedOffset != writeOffset) {
                    writeOffset = alignedOffset;
                }

                if (writeOffset + entrySize <= cacheFile.byteSize()) {
                    dataOffset = writeOffset;
                    break;
                }

                evict();

                if (writeOffset + entrySize > cacheFile.byteSize()) {
                    writeOffset = METADATA_SIZE; // wrap to start

                    if (entrySize > cacheFile.byteSize() - METADATA_SIZE) {
                        throw new IllegalStateException("Cache entry too large for cache file");
                    }
                    continue;
                }
            }

            long timestamp = System.currentTimeMillis();
            long ttlNanos = ttl.toNanos();

            cacheFile.set(ValueLayout.JAVA_LONG, dataOffset, timestamp);
            cacheFile.set(ValueLayout.JAVA_LONG, dataOffset + 8, ttlNanos);
            cacheFile.set(ValueLayout.JAVA_LONG, dataOffset + 16, (long) dataSize);

            long dataWriteOffset = dataOffset + METADATA_SIZE;
            MemorySegment dataSegment = MemorySegment.ofArray(data);
            MemorySegment.copy(
                    dataSegment, 0,
                    cacheFile, dataWriteOffset,
                    dataSize
            );

            long keyHash = hashKey(key);
            long slot = findIndexSlot(keyHash);
            long slotOffset = slot * INDEX_SLOT_SIZE;

            indexFile.set(ValueLayout.JAVA_LONG, slotOffset, keyHash);
            indexFile.set(ValueLayout.JAVA_LONG, slotOffset + 8, dataOffset);

            entryMetadata.put(keyHash, new EntryMetadata(key, timestamp, dataSize));
            keyToHash.put(key, keyHash);
            allKeys.add(key);

            writeOffset = dataOffset + entrySize;
            cacheFile.set(ValueLayout.JAVA_LONG, 0, writeOffset);

            currentSize++;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void delete(String key) {
        lock.writeLock().lock();
        try {
            Long keyHash = keyToHash.remove(key);
            if (keyHash == null) {
                return;
            }

            long slot = findIndexSlot(keyHash);
            long slotOffset = slot * INDEX_SLOT_SIZE;

            // Clear index slot
            indexFile.set(ValueLayout.JAVA_LONG, slotOffset, 0L);
            indexFile.set(ValueLayout.JAVA_LONG, slotOffset + 8, 0L);

            entryMetadata.remove(keyHash);
            allKeys.remove(key);
            currentSize--;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            // Clear index
            for (long i = 0; i < indexFile.byteSize(); i += INDEX_SLOT_SIZE) {
                indexFile.set(ValueLayout.JAVA_LONG, i, 0L);
                indexFile.set(ValueLayout.JAVA_LONG, i + 8, 0L);
            }

            entryMetadata.clear();
            keyToHash.clear();
            allKeys.clear();
            writeOffset = METADATA_SIZE;
            cacheFile.set(ValueLayout.JAVA_LONG, 0, writeOffset);
            currentSize = 0;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Get all keys matching a pattern (supports prefix matching with "*").
     * Package-private for CacheServiceImpl access.
     */
    Set<String> keys(String pattern) {
        lock.readLock().lock();
        try {
            if (pattern.endsWith("*")) {
                String prefix = pattern.substring(0, pattern.length() - 1);
                return allKeys.stream()
                        .filter(key -> key.startsWith(prefix))
                        .collect(Collectors.toSet());
            } else {
                return allKeys.contains(pattern) ? Set.of(pattern) : Set.of();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private boolean needsEviction() {
        // Simple check: evict if we're at 90% capacity
        return currentSize >= (config.maxSizeBytes() / 1024) * 0.9;
    }

    private void evict() {
        // Implement eviction based on strategy
        int toEvict = Math.max(1, (int) (currentSize * 0.1)); // Evict 10%

        switch (config.strategy()) {
            case TTL -> evictExpired();
            case LRU -> evictLRU(toEvict);
            case LFU -> evictLFU(toEvict);
            case SIZE -> evictBySize(toEvict);
            case ADAPTIVE -> evictAdaptive(toEvict);
        }
    }

    private void evictExpired() {
        // Evict expired entries based on TTL
        List<String> toRemove = new ArrayList<>();
        Instant now = Instant.now();

        for (Map.Entry<Long, EntryMetadata> entry : entryMetadata.entrySet()) {
            EntryMetadata metadata = entry.getValue();
            // Check if expired (simplified - would need to check actual TTL from cache)
            // For now, evict entries older than default TTL
            long age = System.currentTimeMillis() - metadata.createdAt();
            if (age > config.defaultTtl().toMillis()) {
                toRemove.add(metadata.key());
            }
        }

        // Remove expired entries (limit to avoid removing everything)
        toRemove.stream().limit(100).forEach(this::delete);
    }

    private void evictLRU(int count) {
        // Evict least recently used entries (oldest access time)
        List<Map.Entry<Long, EntryMetadata>> sorted = entryMetadata.entrySet().stream()
                .sorted(Comparator.comparingLong(e -> e.getValue().lastAccessTime()))
                .limit(count)
                .toList();

        for (var entry : sorted) {
            delete(entry.getValue().key());
        }
    }

    private void evictLFU(int count) {
        // Evict least frequently used entries (lowest access count)
        List<Map.Entry<Long, EntryMetadata>> sorted = entryMetadata.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getValue().accessCount()))
                .limit(count)
                .toList();

        for (var entry : sorted) {
            delete(entry.getValue().key());
        }
    }

    private void evictBySize(int count) {
        // Evict largest entries first
        List<Map.Entry<Long, EntryMetadata>> sorted = entryMetadata.entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<Long, EntryMetadata> e) -> e.getValue().size()).reversed())
                .limit(count)
                .toList();

        for (var entry : sorted) {
            delete(entry.getValue().key());
        }
    }

    private void evictAdaptive(int count) {
        // Adaptive: Combine LRU + LFU + SIZE scores
        // Score = (LRU_weight * access_age) + (LFU_weight * access_count_inverse) + (SIZE_weight * size)

        List<Map.Entry<Long, EntryMetadata>> sorted = entryMetadata.entrySet().stream()
                .sorted((e1, e2) -> {
                    EntryMetadata m1 = e1.getValue();
                    EntryMetadata m2 = e2.getValue();

                    // Calculate adaptive score (lower = more likely to evict)
                    long age1 = System.currentTimeMillis() - m1.lastAccessTime();
                    long age2 = System.currentTimeMillis() - m2.lastAccessTime();

                    double score1 = (age1 / 1000.0) + (1000.0 / m1.accessCount()) + (m1.size() / 100.0);
                    double score2 = (age2 / 1000.0) + (1000.0 / m2.accessCount()) + (m2.size() / 100.0);

                    return Double.compare(score1, score2);
                })
                .limit(count)
                .toList();

        for (var entry : sorted) {
            delete(entry.getValue().key());
        }
    }

    public CacheStats getStats() {
        lock.readLock().lock();
        try {
            long total = cacheHits + cacheMisses;
            double hitRatio = total > 0 ? (double) cacheHits / total * 100.0 : 0.0;

            return new CacheStats(
                    currentSize,
                    config.maxSizeBytes() / 1024, // Convert to approximate entries
                    cacheHits,
                    cacheMisses,
                    hitRatio
            );
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Metadata for cache entries (for eviction tracking).
     */
    private record EntryMetadata(
            String key,
            long createdAt,
            int size,
            long lastAccessTime,
            int accessCount
    ) {
        EntryMetadata(String key, long createdAt, int size) {
            this(key, createdAt, size, System.currentTimeMillis(), 1);
        }

        EntryMetadata recordAccess() {
            return new EntryMetadata(
                    key, createdAt, size,
                    System.currentTimeMillis(),
                    accessCount + 1
            );
        }
    }
}
