package com.akilisha.oss.roya.api;

/**
 * Server-Sent Events handler interface.
 *
 * SSE handlers have write-only access via an auto-closeable sink.
 * Attempting to use regular Response methods will throw exceptions.
 *
 * Example:
 * <pre>{@code
 * app.sse("/events", (req, sink) -> {
 *     sink.emit(SseEvent.create("hello"))
 *         .emit(SseEvent.create("world"));
 * });
 * }</pre>
 */
@FunctionalInterface
public interface SseHandler {
    /**
     * Handle an SSE request.
     *
     * @param req The request object
     * @param sink The SSE sink for emitting events (auto-closeable)
     * @throws Exception Any exception propagates to error handlers
     */
    void handle(Request req, AutoCloseable sink) throws Exception;
}
