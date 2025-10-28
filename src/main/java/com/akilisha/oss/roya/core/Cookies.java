package com.akilisha.oss.roya.core;

import java.util.Optional;

/**
 * HTTP cookies (request).
 *
 * Express: req.cookies (with cookie-parser middleware)
 */
public interface Cookies {

    /**
     * Get a cookie value.
     *
     * @param name Cookie name
     * @return Cookie value or empty
     */
    Optional<String> get(String name);

    /**
     * Check if a cookie exists.
     *
     * @param name Cookie name
     * @return true if present
     */
    default boolean has(String name) {
        return get(name).isPresent();
    }
}
