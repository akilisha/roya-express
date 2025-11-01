package com.akilisha.oss.roya.core.routing;

import com.akilisha.oss.roya.api.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Node in the route tree.
 * Each node represents a path segment and contains:
 * - Segment matcher (static/regex/param)
 * - Child nodes (for deeper segments)
 * - Handler (if this is a terminal node)
 */
class RouteNode {
    private final SegmentMatcher matcher;
    private final String segmentPattern;
    private final String paramName; // For parameter segments
    private final Map<String, RouteNode> children = new HashMap<>(); // key = segment pattern
    private final List<Handler> handlers = new ArrayList<>();
    private boolean isTerminal = false;
    
    RouteNode(String segmentPattern, String paramName) {
        this.segmentPattern = segmentPattern;
        this.paramName = paramName;
        this.matcher = SegmentMatcherFactory.create(segmentPattern, paramName);
    }
    
    /**
     * Get or create a child node for the given segment pattern.
     */
    RouteNode getOrCreateChild(String segmentPattern, String paramName) {
        return children.computeIfAbsent(segmentPattern, k -> new RouteNode(segmentPattern, paramName));
    }
    
    /**
     * Get all child nodes (for DFS traversal).
     */
    List<RouteNode> getChildren() {
        return new ArrayList<>(children.values());
    }
    
    /**
     * Add handler to this node (makes it terminal).
     */
    void addHandler(Handler handler) {
        handlers.add(handler);
        isTerminal = true;
    }
    
    /**
     * Get handlers for this node.
     */
    List<Handler> getHandlers() {
        return new ArrayList<>(handlers);
    }
    
    /**
     * Check if this is a terminal node (has handlers).
     */
    boolean isTerminal() {
        return isTerminal;
    }
    
    /**
     * Match a segment against this node's pattern.
     * 
     * @param segment The path segment to match
     * @param params Map to collect extracted parameters
     * @return true if segment matches this node's pattern
     */
    boolean matches(String segment, Map<String, String> params) {
        return matcher.matches(segment, params, paramName);
    }
    
    /**
     * Get segment pattern (for debugging/logging).
     */
    String getSegmentPattern() {
        return segmentPattern;
    }
    
    /**
     * Get parameter name (if this is a parameter node).
     */
    String getParamName() {
        return paramName;
    }
    
    /**
     * Check if this node matches parameter patterns (for traversal order).
     * Parameter nodes should be tried after static nodes.
     */
    boolean isParameter() {
        return paramName != null || 
               segmentPattern.startsWith("(.*") || 
               segmentPattern.startsWith("(.*?");
    }
}

