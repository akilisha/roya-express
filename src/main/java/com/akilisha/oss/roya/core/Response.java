package com.akilisha.oss.roya.core;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

/**
 * HTTP Response - Express-compatible API.
 *
 * Represents an outgoing HTTP response with fluent API for building responses.
 * Designed to match Express.js response API as closely as possible.
 *
 * Express methods mapped to Roya:
 * - res.status(code)           → res.status(code)
 * - res.send(text)             → res.send(text)
 * - res.json(obj)              → res.json(obj)
 * - res.sendFile(path)         → res.sendFile(path)
 * - res.redirect(url)          → res.redirect(url)
 * - res.set(name, value)       → res.header(name, value)
 * - res.cookie(name, value)    → res.cookie(name, value)
 */
public interface Response {

    // ========== Status ==========

    /**
     * Set the HTTP status code.
     *
     * Express: res.status(code)
     *
     * @param code HTTP status code (200, 404, 500, etc.)
     * @return this (for chaining)
     */
    Response status(int code);

    /**
     * Get the current status code.
     *
     * @return HTTP status code
     */
    int getStatus();

    // ========== Headers ==========

    /**
     * Set a response header.
     *
     * Express: res.set(name, value) or res.header(name, value)
     *
     * @param name Header name
     * @param value Header value
     * @return this (for chaining)
     */
    Response header(String name, String value);

    /**
     * Set multiple headers at once.
     *
     * Express: res.set(headers)
     *
     * @param headers Map of header names to values
     * @return this (for chaining)
     */
    Response headers(Map<String, String> headers);

    /**
     * Get a response header value.
     *
     * Express: res.get(name)
     *
     * @param name Header name
     * @return Header value or null
     */
    String getHeader(String name);

    /**
     * Remove a response header.
     *
     * @param name Header name
     * @return this (for chaining)
     */
    Response removeHeader(String name);

    // ========== Content Type ==========

    /**
     * Set the Content-Type header.
     *
     * Express: res.type(type)
     *
     * @param contentType MIME type
     * @return this (for chaining)
     */
    Response type(String contentType);

    /**
     * Set Content-Type to application/json.
     *
     * Convenience for: res.type("application/json")
     */
    default Response json() {
        return type("application/json");
    }

    /**
     * Set Content-Type to text/html.
     *
     * Convenience for: res.type("text/html")
     */
    default Response html() {
        return type("text/html");
    }

    /**
     * Set Content-Type to text/plain.
     *
     * Convenience for: res.type("text/plain")
     */
    default Response text() {
        return type("text/plain");
    }

    // ========== Body (Text) ==========

    /**
     * Send a text response.
     *
     * Express: res.send(text)
     *
     * @param text Response body
     */
    void send(String text);

    /**
     * Send a text response with status code.
     *
     * @param status HTTP status code
     * @param text Response body
     */
    default void send(int status, String text) {
        status(status).send(text);
    }

    // ========== Body (JSON) ==========

    /**
     * Send a JSON response.
     *
     * Automatically serializes the object to JSON and sets Content-Type.
     *
     * Express: res.json(obj)
     *
     * @param data Object to serialize (record, Map, List, etc.)
     */
    void json(Object data);

    /**
     * Send a JSON response with status code.
     *
     * @param status HTTP status code
     * @param data Object to serialize
     */
    default void json(int status, Object data) {
        status(status).json(data);
    }

    // ========== Body (HTML) ==========

    /**
     * Send an HTML response.
     *
     * Sets Content-Type to text/html and sends the HTML string.
     *
     * @param html HTML content
     */
    default void sendHtml(String html) {
        type("text/html").send(html);
    }

    // ========== Files ==========

    /**
     * Send a file.
     *
     * Express: res.sendFile(path)
     *
     * @param path File path
     */
    void sendFile(Path path);

    /**
     * Send a file with options.
     *
     * @param path File path
     * @param options File send options (caching, etc.)
     */
    void sendFile(Path path, FileSendOptions options);

    /**
     * Trigger a file download.
     *
     * Express: res.download(path, filename)
     *
     * Sets Content-Disposition header to trigger browser download.
     *
     * @param path File path
     * @param filename Download filename (what the user sees)
     */
    void download(Path path, String filename);

    /**
     * Trigger a file download with default filename.
     *
     * Uses the actual filename from the path.
     *
     * @param path File path
     */
    default void download(Path path) {
        download(path, path.getFileName().toString());
    }

    // ========== Redirects ==========

    /**
     * Redirect to a URL (302 by default).
     *
     * Express: res.redirect(url)
     *
     * @param url Target URL
     */
    void redirect(String url);

    /**
     * Redirect to a URL with custom status code.
     *
     * Express: res.redirect(status, url)
     *
     * @param status HTTP status code (301, 302, 307, 308)
     * @param url Target URL
     */
    void redirect(int status, String url);

    /**
     * Redirect back to the referrer.
     *
     * Express: res.redirect('back')
     *
     * Falls back to "/" if no referrer.
     */
    void redirectBack();

    // ========== Cookies ==========

    /**
     * Set a cookie.
     *
     * Express: res.cookie(name, value)
     *
     * @param name Cookie name
     * @param value Cookie value
     * @return this (for chaining)
     */
    Response cookie(String name, String value);

    /**
     * Set a cookie with options.
     *
     * Express: res.cookie(name, value, options)
     *
     * @param cookie Cookie object with options
     * @return this (for chaining)
     */
    Response cookie(Cookie cookie);

    /**
     * Clear a cookie.
     *
     * Express: res.clearCookie(name)
     *
     * @param name Cookie name
     * @return this (for chaining)
     */
    Response clearCookie(String name);

    // ========== Streaming ==========

    /**
     * Get the raw output stream for custom response writing.
     *
     * Use this for streaming large files, SSE, WebSocket upgrades, etc.
     *
     * @return Output stream
     */
    OutputStream stream();

    /**
     * Stream JSON responses (for large arrays or server-sent events).
     *
     * Roya extension for AI streaming, large datasets, etc.
     *
     * @param streamer Consumer that writes to the stream
     */
    void streamJson(Consumer<JsonStream> streamer);

    // ========== Template Rendering ==========

    /**
     * Render a template.
     *
     * Express: res.render(view, data)
     *
     * Requires template engine plugin (JTE, etc.)
     *
     * @param template Template name
     * @param data Data to pass to template
     */
    void render(String template, Object data);

    /**
     * Render a template without data.
     *
     * @param template Template name
     */
    default void render(String template) {
        render(template, null);
    }

    // ========== Convenience Methods (Common Status Codes) ==========

    /**
     * Send 200 OK with JSON data.
     *
     * @param data Response data
     */
    default void ok(Object data) {
        status(200).json(data);
    }

    /**
     * Send 201 Created with JSON data.
     *
     * @param data Created resource
     */
    default void created(Object data) {
        status(201).json(data);
    }

    /**
     * Send 202 Accepted with JSON data.
     *
     * @param data Response data
     */
    default void accepted(Object data) {
        status(202).json(data);
    }

    /**
     * Send 204 No Content.
     */
    default void noContent() {
        status(204).send("");
    }

    /**
     * Send 400 Bad Request with error message.
     *
     * @param message Error message
     */
    default void badRequest(String message) {
        status(400).json(Map.of("error", message));
    }

    /**
     * Send 401 Unauthorized with error message.
     *
     * @param message Error message
     */
    default void unauthorized(String message) {
        status(401).json(Map.of("error", message));
    }

    /**
     * Send 403 Forbidden with error message.
     *
     * @param message Error message
     */
    default void forbidden(String message) {
        status(403).json(Map.of("error", message));
    }

    /**
     * Send 404 Not Found with error message.
     *
     * @param message Error message
     */
    default void notFound(String message) {
        status(404).json(Map.of("error", message));
    }

    /**
     * Send 500 Internal Server Error with error message.
     *
     * @param message Error message
     */
    default void internalError(String message) {
        status(500).json(Map.of("error", message));
    }

    // ========== Response State ==========

    /**
     * Check if headers have been sent.
     *
     * Express: res.headersSent
     *
     * @return true if headers sent, false otherwise
     */
    boolean isHeadersSent();

    /**
     * Check if the response has been finished.
     *
     * @return true if response complete, false otherwise
     */
    boolean isFinished();
}
