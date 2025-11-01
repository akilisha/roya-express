package com.akilisha.oss.roya.api;

import java.io.IOException;

/**
 * SSE emitter for sending events to the client.
 *
 * Implements standard Server-Sent Events format:
 * - "data: <content>\n\n"
 * - "event: <name>\ndata: <content>\n\n"
 *
 * Flushes after each emit to ensure real-time delivery.
 */
public interface SSEEmitter extends AutoCloseable {

    /**
     * Emit an SSE event with data.
     *
     * Standard format: "data: <data>\n\n"
     *
     * @param data Event data
     * @return this (for chaining)
     * @throws IOException if write fails
     */
    SSEEmitter emit(String data) throws IOException;

    /**
     * Emit an SSE event with name and data.
     *
     * Standard format: "event: <name>\ndata: <data>\n\n"
     *
     * @param name Event name
     * @param data Event data
     * @return this (for chaining)
     * @throws IOException if write fails
     */
    SSEEmitter emit(String name, String data) throws IOException;

    /**
     * Close the SSE connection.
     */
    @Override
    void close() throws IOException;
}
