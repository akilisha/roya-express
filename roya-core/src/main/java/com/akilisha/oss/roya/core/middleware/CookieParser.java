package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.*;
import java.util.Optional;

/**
 * Cookie parser middleware - Express-compatible.
 *
 * Express: app.use(cookieParser())
 * Roya:    app.use(cookieParser())
 *
 * Parses cookies from request headers and makes them available via req.cookies().
 * Note: Cookie parsing is already implemented in CookiesImpl, this middleware
 * just ensures cookies are parsed.
 *
 * Example:
 * <pre>
 * app.use(cookieParser());
 * 
 * app.get("/profile", (req, res) -> {
 *     Optional<String> sessionId = req.cookies().get("sessionId");
 *     // ...
 * });
 * </pre>
 */
public final class CookieParser {

    /**
     * Create cookie parser middleware.
     *
     * Cookies are already parsed by the Request implementation.
     * This middleware is a no-op for now, provided for Express compatibility.
     *
     * @return Middleware handler
     */
    public static Handler cookieParser() {
        return (req, res, next) -> {
            // Cookies are already parsed in RequestImpl
            // This middleware is provided for Express.js API compatibility
            next.handle(req, res);
        };
    }

    /**
     * Create cookie parser with custom secret (for signed cookies).
     *
     * @param secret Secret for signing cookies
     * @return Middleware handler
     */
    public static Handler cookieParser(String secret) {
        // Signed cookie parsing not yet implemented
        return (req, res, next) -> {
            // For now, unsigned only
            next.handle(req, res);
        };
    }
}

