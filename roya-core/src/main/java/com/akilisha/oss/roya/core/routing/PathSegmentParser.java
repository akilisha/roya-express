package com.akilisha.oss.roya.core.routing;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses Express-style path patterns into segments for tree-based routing.
 * 
 * Examples:
 * - "/api/user/:uid" → ["api", "user", "(.*)"]
 * - "/api/user/:uid/address/:aid" → ["api", "user", "(.*?)/", "address", "(.*)"]
 * - "/ab?cd" → ["(ab?cd)"]
 * - "/\\/[0-9]+-[[\\w]]*\\/" → ["([0-9]+-[[\\w]]*)?/"]
 */
public class PathSegmentParser {
    
    /**
     * Parse path into segments with appropriate regex patterns.
     * 
     * @param path Express path pattern (e.g., "/api/user/:id")
     * @return List of segment patterns (each is a regex or static string)
     */
    public static List<String> parse(String path) {
        List<String> segments = new ArrayList<>();
        
        if (path == null || path.isEmpty() || path.equals("/")) {
            segments.add("");
            return segments;
        }
        
        // Remove leading slash, split by remaining slashes
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        String[] parts = normalized.split("/", -1); // Keep empty trailing parts
        
        for (int i = 0; i < parts.length; i++) {
            String segment = parts[i];
            boolean isLast = (i == parts.length - 1);
            
            if (segment.isEmpty()) {
                // Empty segment (e.g., trailing slash or double slash)
                continue;
            }
            
            if (segment.startsWith(":")) {
                // Parameter: :uid
                String paramName = segment.substring(1);
                // Remove regex constraints if present: :id(\\d+)
                int constraintStart = paramName.indexOf('(');
                if (constraintStart > 0) {
                    paramName = paramName.substring(0, constraintStart);
                }
                
                if (isLast) {
                    // Last segment: greedy match to end
                    segments.add("(.*)");
                } else {
                    // Middle segment: non-greedy match until next /
                    // The "/" is consumed by path splitting, so just match non-greedy
                    segments.add("(.*?)");
                }
            } else if (isRegexPattern(segment)) {
                // Already a regex pattern (e.g., "[0-9]+", "ab?cd")
                // Preserve as-is, wrap in group if needed
                segments.add("(" + segment + ")");
            } else {
                // Static segment - store as-is (no escaping needed for exact match)
                segments.add(segment);
            }
        }
        
        return segments;
    }
    
    /**
     * Check if a segment is a regex pattern.
     * Regex patterns contain regex special characters that aren't Express syntax.
     */
    private static boolean isRegexPattern(String segment) {
        // Simple heuristic: if it contains regex quantifiers or character classes
        // that aren't part of Express syntax, treat as regex
        return segment.contains("[") && segment.contains("]") ||
               segment.contains("^") || segment.contains("$") ||
               segment.matches(".*[\\\\].*"); // Contains escaped characters
    }
    
    /**
     * Extract parameter names from path pattern.
     * Used for building param map during matching.
     * 
     * @param path Express path pattern
     * @return List of parameter names in order
     */
    public static List<String> extractParamNames(String path) {
        List<String> paramNames = new ArrayList<>();
        
        if (path == null || path.isEmpty()) {
            return paramNames;
        }
        
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        String[] parts = normalized.split("/");
        
        for (String part : parts) {
            if (part.startsWith(":")) {
                String paramName = part.substring(1);
                // Remove any regex constraints: :id(\\d+)
                int constraintStart = paramName.indexOf('(');
                if (constraintStart > 0) {
                    paramName = paramName.substring(0, constraintStart);
                }
                paramNames.add(paramName);
            }
        }
        
        return paramNames;
    }
}

