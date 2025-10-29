package com.akilisha.oss.roya.core.routing;

import com.akilisha.oss.roya.api.routing.PathMatcher;

import java.util.Map;

/**
 * Matcher for path prefixes (used for middleware).
 *
 * For paths like "/api", "/admin", etc., this matches any path that starts with the prefix.
 * Example: prefix "/api" matches "/api", "/api/users", "/api/users/123", etc.
 */
public class PrefixPathMatcher implements PathMatcher {

    private final String prefix;

    public PrefixPathMatcher(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean matches(String path) {
        return path.startsWith(prefix);
    }

    @Override
    public Map<String, String> extractParams(String path) {
        return Map.of();
    }
}

