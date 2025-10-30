package com.akilisha.oss.roya.plugins.vector.chunking;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits text into fixed-size chunks with optional overlap.
 */
public class FixedSizeChunker implements ChunkingStrategy {
    private final int chunkSize;
    private final int overlap;

    public FixedSizeChunker(int chunkSize, int overlap) {
        if (chunkSize <= 0) throw new IllegalArgumentException("chunkSize must be > 0");
        if (overlap < 0) throw new IllegalArgumentException("overlap must be >= 0");
        if (overlap >= chunkSize) throw new IllegalArgumentException("overlap must be < chunkSize");
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    @Override
    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }
        int start = 0;
        int len = text.length();
        while (start < len) {
            int end = Math.min(start + chunkSize, len);
            chunks.add(text.substring(start, end));
            if (end == len) break;
            start = end - overlap;
            if (start < 0) start = 0;
        }
        return chunks;
    }

    public int chunkSize() { return chunkSize; }
    public int overlap() { return overlap; }
}
