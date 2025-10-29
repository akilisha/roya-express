package com.akilisha.oss.roya.core.routing;

import com.akilisha.oss.roya.api.*;
import com.akilisha.oss.roya.api.routing.PathMatcher;
import com.akilisha.oss.roya.api.routing.Route;
import com.akilisha.oss.roya.api.routing.RouteMatch;
import com.akilisha.oss.roya.core.ParamsImpl;
import java.util.ArrayList;
import java.util.List;

/**
 * Router implementation.
 *
 * Maintains a list of routes and matches incoming requests against them.
 * Routes are matched in registration order (Express behavior).
 */
public class RouterImpl implements Router {

    private final List<Route> routes = new ArrayList<>();

    /**
     * Create a new router.
     */
    public static Router create() {
        return new RouterImpl();
    }

    @Override
    public Router use(Handler handler) {
        // Middleware with no path - matches all requests
        routes.add(createRoute(null, null, handler));
        return this;
    }

    @Override
    public Router use(String path, Handler handler) {
        // Middleware with path prefix - match if path starts with prefix
        routes.add(createRoute(null, path, handler));
        return this;
    }
    
    /**
     * Mount a router at a specific path (nested routing).
     * 
     * Express: app.use('/api', router)
     * 
     * @param mountPath Path to mount this router at
     * @param router Router to mount
     * @return this
     */
    public Router use(String mountPath, Router router) {
        // Create a wrapper handler that strips mount path before routing
        Handler wrapper = (req, res, next) -> {
            String originalPath = req.path();
            
            // Strip mount path from request path for nested router
            if (originalPath.startsWith(mountPath)) {
                String remainingPath = originalPath.substring(mountPath.length());
                if (remainingPath.isEmpty()) {
                    remainingPath = "/";
                }
                
                // Create a path-adjusted request that exposes only the remaining path
                Request adjustedRequest = new PathAdjustedRequest(req, remainingPath);
                
                // Delegate to nested router with adjusted path
                router.handle(adjustedRequest, res, next);
            } else {
                // Path doesn't match mount point, skip this router
                next.handle(req, res);
            }
        };
        
        routes.add(createRoute(null, mountPath, wrapper));
        return this;
    }

    @Override
    public Router get(String path, Handler... handlers) {
        for (Handler handler : handlers) {
            routes.add(createRoute("GET", path, handler));
        }
        return this;
    }

    @Override
    public Router post(String path, Handler... handlers) {
        for (Handler handler : handlers) {
            routes.add(createRoute("POST", path, handler));
        }
        return this;
    }

    @Override
    public Router put(String path, Handler... handlers) {
        for (Handler handler : handlers) {
            routes.add(createRoute("PUT", path, handler));
        }
        return this;
    }

    @Override
    public Router delete(String path, Handler... handlers) {
        for (Handler handler : handlers) {
            routes.add(createRoute("DELETE", path, handler));
        }
        return this;
    }

    @Override
    public Router patch(String path, Handler... handlers) {
        for (Handler handler : handlers) {
            routes.add(createRoute("PATCH", path, handler));
        }
        return this;
    }

    @Override
    public Router all(String path, Handler... handlers) {
        for (Handler handler : handlers) {
            routes.add(createRoute(null, path, handler));
        }
        return this;
    }

    /**
     * Handle incoming request by matching against routes.
     *
     * This is the Handler interface implementation - makes routers composable.
     */
    @Override
    public void handle(Request req, Response res, Next next) throws Exception {
        String method = req.method();
        String path = req.path();

        // Track whether any route matched
        boolean routeMatched = false;

        // Try to match routes in order
        for (Route route : routes) {
            RouteMatch match = route.match(method, path);

            if (match != null) {
                routeMatched = true;
                
                // Found a match! Set path parameters
                if (!match.params().isEmpty()) {
                    req.setParams(match.params());
                }

                // Execute the route's handler
                route.handler().handle(req, res, next);

                // If handler sent response, stop here
                // Otherwise continue to next route in the loop
                if (res.isFinished()) {
                    return;
                }
            }
        }

        // Only call outer next() if NO routes matched (Express.js behavior)
        // This allows outer middleware to handle 404s
        if (!routeMatched) {
            next.handle(req, res);
        }
    }

    /**
     * Create a route with appropriate path matcher.
     */
    private Route createRoute(String method, String path, Handler handler) {
        PathMatcher matcher;

        if (path == null) {
            // No path = match everything
            matcher = new PathMatcher() {
                @Override
                public boolean matches(String p) {
                    return true;
                }

                @Override
                public java.util.Map<String, String> extractParams(String p) {
                    return java.util.Map.of();
                }
            };
        } else if (method == null) {
            // Middleware (method is null) = prefix matching
            // Example: "/api" matches "/api", "/api/users", etc.
            matcher = new PrefixPathMatcher(path);
        } else if (isStaticPath(path)) {
            // Optimization: use fast string matcher for static paths
            matcher = new StaticPathMatcher(path);
        } else {
            // Use Express path matcher for dynamic paths
            matcher = new ExpressPathMatcher(path);
        }

        return new RouteImpl(method, path, handler, matcher);
    }

    /**
     * Check if path is static (no parameters, wildcards, etc.)
     */
    private boolean isStaticPath(String path) {
        return (
            !path.contains(":") &&
            !path.contains("*") &&
            !path.contains("?") &&
            !path.contains("+") &&
            !path.contains("(")
        );
    }
}
