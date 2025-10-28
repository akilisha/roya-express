package com.akilisha.oss.roya.core;

import java.util.List;
import java.util.Optional;

/**
 * Query string parameters.
 *
 * Express: req.query
 * Example: ?page=1&limit=10 → req.query().get("page")
 */
public interface Query {

    /**
     * Get a query parameter value.
     *
     * @param name Parameter name
     * @return Parameter value or empty
     */
    Optional<String> get(String name);

    /**
     * Get all values for a query parameter (for array params).
     *
     * Example: ?tag=java&tag=web → req.query().getAll("tag") = ["java", "web"]
     *
     * @param name Parameter name
     * @return List of values (empty if not present)
     */
    List<String> getAll(String name);

    /**
     * Get a query parameter as integer.
     *
     * @param name Parameter name
     * @param defaultValue Default if not present or invalid
     * @return Parameter value as int
     */
    default int getInt(String name, int defaultValue) {
        try {
            return get(name).map(Integer::parseInt).orElse(defaultValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get a query parameter as boolean.
     *
     * @param name Parameter name
     * @param defaultValue Default if not present
     * @return Parameter value as boolean
     */
    default boolean getBoolean(String name, boolean defaultValue) {
        return get(name).map(Boolean::parseBoolean).orElse(defaultValue);
    }

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
