package com.akilisha.oss.roya.api.routing;

import java.util.Map;

/**
 * Result of matching a route against a request.
 *
 * Contains:
 * - The matched route
 * - Extracted path parameters
 *
 * Express example:
 * Route: /users/:id
 * Path:  /users/123
 * Match: { id: "123" }
 */
public record RouteMatch(
    Route route,
    Map<String, String> params
) {
    /**
     * Create a match with no parameters.
     */
    public RouteMatch(Route route) {
        this(route, Map.of());
    }
}
