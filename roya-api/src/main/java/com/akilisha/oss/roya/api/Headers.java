package com.akilisha.oss.roya.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * HTTP request headers.
 *
 * Express: req.headers or req.get(name)
 */
public interface Headers {
    /**
     * Get a header value (case-insensitive).
     *
     * @param name Header name
     * @return Header value or empty
     */
    Optional<String> get(String name);

    /**
     * Get all values for a header (for multi-value headers).
     *
     * @param name Header name
     * @return List of values (empty if not present)
     */
    List<String> getAll(String name);

    /**
     * Get all headers as a map (first value for each header).
     *
     * @return All headers
     */
    Map<String, String> all();

    /**
     * Check if a header exists.
     *
     * @param name Header name
     * @return true if present
     */
    default boolean has(String name) {
        return get(name).isPresent();
    }

    /**
     * Get Content-Length header.
     *
     * @return Content-Length or empty
     */
    Optional<Long> contentLength();

    /**
     * Get Accept-Language headers.
     *
     * @return List of accepted languages
     */
    List<String> acceptLanguages();

    // ========== Common Headers (Convenience) ==========

    /**
     * Get Content-Type header.
     *
     * @return Content-Type or empty
     */
    default Optional<String> contentType() {
        return get("Content-Type");
    }

    /**
     * Get Authorization header.
     *
     * @return Authorization or empty
     */
    default Optional<String> authorization() {
        return get("Authorization");
    }

    /**
     * Get Bearer token from Authorization header.
     *
     * Extracts token from "Bearer <token>" format.
     *
     * @return Token or empty
     */
    default Optional<String> bearer() {
        return authorization()
            .filter(auth -> auth.startsWith("Bearer "))
            .map(auth -> auth.substring(7));
    }

    /**
     * Get User-Agent header.
     *
     * @return User-Agent or empty
     */
    default Optional<String> userAgent() {
        return get("User-Agent");
    }

    /**
     * Get Referer header.
     *
     * @return Referer or empty
     */
    default Optional<String> referer() {
        return get("Referer");
    }

    /**
     * Get Accept header.
     *
     * @return Accept or empty
     */
    default Optional<String> accept() {
        return get("Accept");
    }

    /**
     * Get Host header.
     *
     * @return Host or empty
     */
    default Optional<String> host() {
        return get("Host");
    }
}
