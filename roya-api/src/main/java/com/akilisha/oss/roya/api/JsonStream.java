package com.akilisha.oss.roya.api;

/**
 * Stream for writing JSON responses incrementally.
 *
 * Roya extension for streaming AI responses, large datasets, etc.
 */
public interface JsonStream {
    /**
     * Send a JSON chunk.
     *
     * @param data Object to serialize and send
     */
    void send(Object data);

    /**
     * Close the stream.
     */
    void close();
}
