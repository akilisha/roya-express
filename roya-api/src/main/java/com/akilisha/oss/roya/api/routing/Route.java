package com.akilisha.oss.roya.api.routing;

import com.akilisha.oss.roya.api.Handler;

/**
 * A route represents a path pattern + HTTP method + handler.
 *
 * Express routes combine:
 * - HTTP method (GET, POST, ALL, etc.)
 * - Path pattern ("/users/:id")
 * - Handler(s) to execute
 *
 * Routes are registered with app/router and matched against incoming requests.
 */
public interface Route {

    /**
     * Get the HTTP method this route handles.
     *
     * @return HTTP method (GET, POST, etc.) or null for all methods
     */
    String method();

    /**
     * Get the path pattern for this route.
     *
     * Examples:
     * - "/users" - exact match
     * - "/users/:id" - parameterized
     * - "/users/:id(\\d+)" - with regex constraint
     * - Pattern.compile(...) - full regex
     *
     * @return Path pattern string or regex
     */
    String path();

    /**
     * Get the handler for this route.
     *
     * @return Handler to execute when route matches
     */
    Handler handler();

    /**
     * Attempt to match this route against a request path.
     *
     * @param method HTTP method (GET, POST, etc.)
     * @param path Request path
     * @return RouteMatch if matched, or null if no match
     */
    RouteMatch match(String method, String path);
}
