package com.akilisha.oss.roya.api;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Path parameters extracted from route patterns.
 *
 * Express: req.params
 * Example: /users/:id → req.params().get("id")
 */
public interface Params {
    /**
     * Get a path parameter value.
     *
     * @param name Parameter name
     * @return Parameter value or empty
     */
    Optional<String> get(String name);

    /**
     * Get a path parameter as integer.
     *
     * @param name Parameter name
     * @return Parameter value as int
     * @throws NumberFormatException if not a valid integer
     */
    default int getInt(String name) {
        return Integer.parseInt(get(name).orElseThrow());
    }

    /**
     * Get a path parameter as long.
     *
     * @param name Parameter name
     * @return Parameter value as long
     * @throws NumberFormatException if not a valid long
     */
    default long getLong(String name) {
        return Long.parseLong(get(name).orElseThrow());
    }

    /**
     * Get a path parameter as UUID.
     *
     * @param name Parameter name
     * @return Parameter value as UUID
     * @throws IllegalArgumentException if not a valid UUID
     */
    default UUID getUUID(String name) {
        return UUID.fromString(get(name).orElseThrow());
    }

    /**
     * Get all parameters as a map.
     *
     * @return All parameters
     */
    Map<String, String> all();

    /**
     * Check if a parameter exists.
     *
     * @param name Parameter name
     * @return true if present
     */
    default boolean has(String name) {
        return get(name).isPresent();
    }
}
