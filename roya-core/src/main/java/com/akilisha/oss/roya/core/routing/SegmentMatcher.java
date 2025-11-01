package com.akilisha.oss.roya.core.routing;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interface for matching path segments.
 * Different implementations handle static strings, regex patterns, and parameters.
 */
interface SegmentMatcher {
    /**
     * Match a segment against the pattern.
     * 
     * @param segment The path segment to match
     * @param params Map to collect extracted parameter values
     * @param paramName Name of parameter if this is a param matcher (null otherwise)
     * @return true if segment matches
     */
    boolean matches(String segment, Map<String, String> params, String paramName);
    
    /**
     * Get the segment pattern for debugging.
     */
    String pattern();
}

/**
 * Static segment matcher - exact string match.
 */
class StaticSegmentMatcher implements SegmentMatcher {
    private final String pattern;
    
    StaticSegmentMatcher(String pattern) {
        this.pattern = pattern;
    }
    
    @Override
    public boolean matches(String segment, Map<String, String> params, String paramName) {
        return pattern.equals(segment);
    }
    
    @Override
    public String pattern() {
        return pattern;
    }
}

/**
 * Regex segment matcher - matches using regex pattern.
 * Handles parameters, quantifiers, character classes, etc.
 */
class RegexSegmentMatcher implements SegmentMatcher {
    private final Pattern compiledPattern;
    private final String patternStr;
    private final String paramName;
    
    RegexSegmentMatcher(String patternStr, String paramName) {
        this.patternStr = patternStr;
        this.compiledPattern = Pattern.compile("^" + patternStr + "$");
        this.paramName = paramName;
    }
    
    @Override
    public boolean matches(String segment, Map<String, String> params, String paramName) {
        Matcher matcher = compiledPattern.matcher(segment);
        if (matcher.matches()) {
            // Extract captured groups
            if (this.paramName != null && matcher.groupCount() > 0) {
                String value = matcher.group(1); // First capture group
                if (value != null) {
                    params.put(this.paramName, value);
                }
            } else if (paramName != null && matcher.groupCount() > 0) {
                // Param name passed from outside
                String value = matcher.group(1);
                if (value != null) {
                    params.put(paramName, value);
                }
            }
            return true;
        }
        return false;
    }
    
    @Override
    public String pattern() {
        return patternStr;
    }
}

/**
 * Factory for creating segment matchers.
 */
class SegmentMatcherFactory {
    static SegmentMatcher create(String segmentPattern, String paramName) {
        // Check if it's a parameter pattern (starts with capture group)
        if (segmentPattern.startsWith("(.*") || segmentPattern.startsWith("(.*?")) {
            // Parameter pattern
            return new RegexSegmentMatcher(segmentPattern, paramName);
        } else if (segmentPattern.startsWith("(") && segmentPattern.endsWith(")")) {
            // Regex pattern wrapped in group - extract inner pattern
            String inner = segmentPattern.substring(1, segmentPattern.length() - 1);
            // Check if inner is actually a regex or just a wrapped static string
            if (inner.contains("[") || inner.contains("^") || inner.contains("$") || 
                inner.contains("\\") || inner.contains("+") || inner.contains("*") || 
                inner.contains("?") || inner.contains("|")) {
                // Real regex pattern
                return new RegexSegmentMatcher(inner, paramName);
            } else {
                // Was wrapped but is actually static - unwrap
                return new StaticSegmentMatcher(inner);
            }
        } else {
            // Static segment - exact match
            return new StaticSegmentMatcher(segmentPattern);
        }
    }
}

