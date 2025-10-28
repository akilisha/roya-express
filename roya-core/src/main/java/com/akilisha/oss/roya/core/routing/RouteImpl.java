package com.akilisha.oss.roya.core.routing;

import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.routing.PathMatcher;
import com.akilisha.oss.roya.api.routing.Route;
import com.akilisha.oss.roya.api.routing.RouteMatch;

import java.util.Map;

/**
 * Implementation of Route.
 */
public class RouteImpl implements Route {

    private final String method;
    private final String path;
    private final Handler handler;
    private final PathMatcher pathMatcher;

    public RouteImpl(String method, String path, Handler handler, PathMatcher pathMatcher) {
        this.method = method;
        this.path = path;
        this.handler = handler;
        this.pathMatcher = pathMatcher;
    }

    @Override
    public String method() {
        return method;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public Handler handler() {
        return handler;
    }

    @Override
    public RouteMatch match(String requestMethod, String requestPath) {
        // Check HTTP method first (null means ANY method)
        if (method != null && !method.equalsIgnoreCase(requestMethod)) {
            return null;
        }

        // Check path match
        if (!pathMatcher.matches(requestPath)) {
            return null;
        }

        // Extract parameters
        Map<String, String> params = pathMatcher.extractParams(requestPath);

        return new RouteMatch(this, params);
    }
}
