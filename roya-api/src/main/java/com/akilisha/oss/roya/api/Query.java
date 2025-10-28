package com.akilisha.oss.roya.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
     * Get a query parameter as integer (throws if missing).
     *
     * @param name Parameter name
     * @return Parameter value as int
     */
    int getInt(String name);

    /**
     * Get a query parameter as long (throws if missing).
     *
     * @param name Parameter name
     * @return Parameter value as long
     */
    long getLong(String name);

    /**
     * Get a query parameter as boolean (throws if missing).
     *
     * @param name Parameter name
     * @return Parameter value as boolean
     */
    boolean getBoolean(String name);

    /**
     * Get a query parameter as UUID (throws if missing).
     *
     * @param name Parameter name
     * @return Parameter value as UUID
     */
    UUID getUUID(String name);

    /**
     * Get all parameters as a map (first value for each param).
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
