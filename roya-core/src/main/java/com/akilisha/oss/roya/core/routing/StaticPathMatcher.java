package com.akilisha.oss.roya.core.routing;

import com.akilisha.oss.roya.api.routing.PathMatcher;

import java.util.Map;

/**
 * Optimized matcher for static paths (no parameters).
 *
 * For paths like "/users", "/api/status", etc.
 * Much faster than regex since it's just string equality.
 */
public class StaticPathMatcher implements PathMatcher {

    private final String exactPath;

    public StaticPathMatcher(String path) {
        this.exactPath = path;
    }

    @Override
    public boolean matches(String path) {
        return exactPath.equals(path);
    }

    @Override
    public Map<String, String> extractParams(String path) {
        return Map.of();
    }
}
