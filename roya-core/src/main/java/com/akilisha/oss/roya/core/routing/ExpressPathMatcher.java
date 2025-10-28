package com.akilisha.oss.roya.core.routing;

import com.akilisha.oss.roya.api.routing.PathMatcher;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Express-compatible path matcher.
 *
 * Supports all Express path patterns:
 * - Static: "/users" → exact match
 * - Parameters: "/users/:id" → captures id
 * - Optional: "/ab?cd" → matches "acd" or "abcd"
 * - Wildcard: "/files/*" → matches anything after /files/
 * - Regex constraints: "/users/:id(\\d+)" → id must be digits
 * - Named groups: "/users/(?<id>\\d+)" → named capture
 *
 * Implementation converts Express syntax to Java regex patterns.
 */
public class ExpressPathMatcher implements PathMatcher {

    private final Pattern pattern;
    private final List<String> paramNames;

    /**
     * Create matcher from Express-style path pattern.
     *
     * @param path Express path pattern (e.g., "/users/:id")
     */
    public ExpressPathMatcher(String path) {
        this.paramNames = new ArrayList<>();
        this.pattern = compilePattern(path);
    }

    @Override
    public boolean matches(String path) {
        return pattern.matcher(path).matches();
    }

    @Override
    public Map<String, String> extractParams(String path) {
        Matcher matcher = pattern.matcher(path);

        if (!matcher.matches()) {
            return Map.of();
        }

        Map<String, String> params = new HashMap<>();

        // Extract named parameters
        for (String paramName : paramNames) {
            try {
                String value = matcher.group(paramName);
                if (value != null) {
                    params.put(paramName, value);
                }
            } catch (IllegalArgumentException e) {
                // Named group doesn't exist, skip
            }
        }

        return params;
    }

    /**
     * Convert Express path pattern to Java regex Pattern.
     *
     * Express syntax:
     * - /users/:id → /users/([^/]+)  (capture any segment)
     * - /users/:id(\\d+) → /users/(\\d+)  (capture with constraint)
     * - /ab?cd → /ab?cd  (optional character)
     * - /ab+cd → /ab+cd  (one or more)
     * - /ab*cd → /ab*cd  (zero or more)
     * - /files/* → /files/.*  (wildcard segment)
     *
     * @param path Express path pattern
     * @return Compiled regex pattern
     */
    private Pattern compilePattern(String path) {
        StringBuilder regex = new StringBuilder("^");

        int i = 0;
        while (i < path.length()) {
            char c = path.charAt(i);

            if (c == ':') {
                // Parameter: :id or :id(regex)
                i++; // skip ':'

                // Extract parameter name
                StringBuilder paramName = new StringBuilder();
                while (i < path.length() && isParamChar(path.charAt(i))) {
                    paramName.append(path.charAt(i));
                    i++;
                }

                String name = paramName.toString();
                paramNames.add(name);

                // Check for regex constraint: :id(\\d+)
                if (i < path.length() && path.charAt(i) == '(') {
                    // Find matching closing paren
                    int start = i;
                    int depth = 1;
                    i++; // skip opening paren

                    while (i < path.length() && depth > 0) {
                        if (path.charAt(i) == '(') depth++;
                        else if (path.charAt(i) == ')') depth--;
                        i++;
                    }

                    String constraint = path.substring(start + 1, i - 1);
                    regex.append("(?<").append(name).append(">").append(constraint).append(")");
                } else {
                    // Default: capture any segment (non-slash)
                    regex.append("(?<").append(name).append(">[^/]+)");
                }
            } else if (c == '*') {
                // Wildcard: match anything
                regex.append(".*");
                i++;
            } else if (c == '?' || c == '+') {
                // Optional or one-or-more (apply to previous char)
                regex.append(c);
                i++;
            } else if (isRegexSpecial(c)) {
                // Escape regex special characters
                regex.append("\\").append(c);
                i++;
            } else {
                // Regular character
                regex.append(c);
                i++;
            }
        }

        regex.append("$");

        return Pattern.compile(regex.toString());
    }

    /**
     * Check if character is valid in parameter name.
     */
    private boolean isParamChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    /**
     * Check if character is regex special and needs escaping.
     */
    private boolean isRegexSpecial(char c) {
        return ".^$|[]{}()".indexOf(c) >= 0;
    }
}
