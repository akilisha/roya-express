package com.akilisha.oss.roya.plugins.vector.chunking;

import java.util.List;

/**
 * Strategy for splitting text into chunks for indexing.
 */
public interface ChunkingStrategy {
    /**
     * Split text into chunks.
     *
     * @param text The input text
     * @return List of chunks in order
     */
    List<String> chunk(String text);
}
