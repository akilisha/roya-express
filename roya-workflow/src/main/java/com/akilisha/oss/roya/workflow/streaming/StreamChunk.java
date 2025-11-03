package com.akilisha.oss.roya.workflow.streaming;

import java.util.Map;

/**
 * A chunk of streamed data from a streaming node
 */
public record StreamChunk(
    String content,
    ChunkType type,
    Map<String, Object> metadata
) {

    /**
     * Create a text chunk
     */
    public static StreamChunk text(String content) {
        return new StreamChunk(content, ChunkType.TEXT, Map.of());
    }

    /**
     * Create a function call chunk
     */
    public static StreamChunk functionCall(String content, Map<String, Object> metadata) {
        return new StreamChunk(content, ChunkType.FUNCTION_CALL, metadata);
    }

    /**
     * Create a tool use chunk
     */
    public static StreamChunk toolUse(String content, Map<String, Object> metadata) {
        return new StreamChunk(content, ChunkType.TOOL_USE, metadata);
    }

    /**
     * Create a completion chunk
     */
    public static StreamChunk completion() {
        return new StreamChunk("", ChunkType.COMPLETION, Map.of());
    }
}
