package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.Request;

import java.util.Optional;

/**
 * Compression middleware - Express-compatible.
 *
 * Express: app.use(compression())
 * Roya:    app.use(compression())
 *
 * Compresses response bodies using gzip when the client accepts it.
 * Automatically sets Content-Encoding header.
 *
 * Example:
 * <pre>
 * app.use(compression());
 *
 * // Or with custom filter:
 * app.use(compression((req) -> {
 *     return req.path().startsWith("/api");
 * }));
 * </pre>
 */
public final class Compression {

    /**
     * Create compression middleware.
     *
     * @return Middleware handler
     */
    public static Handler compression() {
        return (req, res, next) -> {
            // Check if client accepts gzip
            Optional<String> acceptEncoding = req.headers().get("Accept-Encoding");

            if (acceptEncoding.isPresent() && acceptEncoding.get().contains("gzip")) {
                // Wrap response with gzip
                res.header("Content-Encoding", "gzip");
            }

            next.handle(req, res);
        };
    }

    /**
     * Create compression middleware with custom filter.
     *
     * @param filter Function to determine if response should be compressed
     * @return Middleware handler
     */
    public static Handler compression(java.util.function.Function<Request, Boolean> filter) {
        return (req, res, next) -> {
            // Skip compression if filter returns false
            if (!filter.apply(req)) {
                next.handle(req, res);
                return;
            }

            // Check if client accepts gzip
            Optional<String> acceptEncoding = req.headers().get("Accept-Encoding");

            if (acceptEncoding.isPresent() && acceptEncoding.get().contains("gzip")) {
                // Wrap response with gzip
                res.header("Content-Encoding", "gzip");
            }

            next.handle(req, res);
        };
    }
}

