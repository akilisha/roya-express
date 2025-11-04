package com.akilisha.oss.roya.workflow.streaming;

/**
 * Type of streamed chunk
 */
public enum ChunkType {
    /**
     * Regular text content
     */
    TEXT,
    
    /**
     * AI model requesting a function call
     */
    FUNCTION_CALL,
    
    /**
     * AI model using a tool
     */
    TOOL_USE,
    
    /**
     * Stream completion marker
     */
    COMPLETION
}
