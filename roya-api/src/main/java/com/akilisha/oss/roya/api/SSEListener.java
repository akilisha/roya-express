package com.akilisha.oss.roya.api;

/**
 * Server-Sent Events listener interface.
 *
 * SSE listeners receive write-only access via an emitter.
 * The framework handles headers and connection management.
 *
 * Example:
 * <pre>{@code
 * app.sse("/events", emitter -> {
 *     emitter.emit("data: hello\n\n");
 *     emitter.emit("data: world\n\n");
 * });
 * }</pre>
 */
@FunctionalInterface
public interface SSEListener {
    /**
     * Handle an SSE connection.
     *
     * @param emitter SSE emitter for writing events to the client
     * @throws Exception Any exception will close the SSE stream
     */
    void handle(SSEEmitter emitter) throws Exception;
}
