package com.akilisha.oss.roya.api.routing;

import java.util.Map;

/**
 * Strategy interface for matching paths.
 *
 * Different implementations support different Express path patterns:
 * - ExpressPathMatcher: Express syntax (/users/:id, /ab?cd, etc.)
 * - RegexPathMatcher: Full Java regex
 * - StaticPathMatcher: Exact string match (optimization)
 *
 * This allows us to use the most efficient matcher for each route.
 */
public interface PathMatcher {

    /**
     * Test if this matcher matches the given path.
     *
     * @param path Request path to test
     * @return true if matches, false otherwise
     */
    boolean matches(String path);

    /**
     * Extract path parameters from the given path.
     *
     * For example:
     * Pattern: /users/:id
     * Path:    /users/123
     * Returns: { "id": "123" }
     *
     * @param path Request path
     * @return Extracted parameters (empty map if none)
     */
    Map<String, String> extractParams(String path);
}
