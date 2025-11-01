package com.akilisha.oss.roya.core.routing;

import com.akilisha.oss.roya.api.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tree-based route storage and matching.
 *
 * Structure: Method → Segments → Handler
 *
 * Uses depth-first search (DFS) to match routes segment by segment.
 * Parameter nodes are tried after static nodes for better performance.
 */
class RouteTree {
    private final Map<String, RouteNode> methodRoots = new HashMap<>();

    /**
     * Add a route to the tree.
     *
     * @param method HTTP method (GET, POST, etc.) or null for all methods
     * @param path Express path pattern (e.g., "/api/user/:id")
     * @param handler Route handler
     */
    void addRoute(String method, String path, Handler handler) {
        List<String> segments = PathSegmentParser.parse(path);
        List<String> paramNames = PathSegmentParser.extractParamNames(path);

        RouteNode root = getOrCreateMethodRoot(method);
        RouteNode current = root;

        // Build tree path segment by segment
        int paramIndex = 0;
        for (int i = 0; i < segments.size(); i++) {
            String segmentPattern = segments.get(i);
            String paramName = null;

            // Check if this segment corresponds to a parameter
            if (segmentPattern.startsWith("(.*") || segmentPattern.startsWith("(.*?")) {
                if (paramIndex < paramNames.size()) {
                    paramName = paramNames.get(paramIndex++);
                }
            }

            // Get or create child node
            current = current.getOrCreateChild(segmentPattern, paramName);
        }

        // Add handler to terminal node
        current.addHandler(handler);
    }

    /**
     * Match a request against the route tree.
     *
     * @param method HTTP method
     * @param path Request path
     * @return MatchResult containing handlers and extracted parameters, or null if no match
     */
    MatchResult match(String method, String path) {
        RouteNode root = methodRoots.get(method);
        if (root == null) {
            // Try "ALL" method as fallback
            root = methodRoots.get(null);
            if (root == null) {
                return null;
            }
        }

        // Split path into segments
        List<String> pathSegments = splitPath(path);

        // DFS search
        Map<String, String> params = new HashMap<>();
        List<Handler> handlers = new ArrayList<>();

        if (dfsMatch(root, pathSegments, 0, params, handlers)) {
            return new MatchResult(handlers, params);
        }

        return null;
    }

    /**
     * Depth-first search to match path segments against tree.
     *
     * @param node Current tree node
     * @param segments Remaining path segments to match
     * @param segmentIndex Current segment index
     * @param params Map to collect extracted parameters
     * @param handlers List to collect matched handlers
     * @return true if match found
     */
    private boolean dfsMatch(RouteNode node, List<String> segments, int segmentIndex,
                           Map<String, String> params, List<Handler> handlers) {
        // Check if we've consumed all segments
        if (segmentIndex >= segments.size()) {
            // Check if current node is terminal (has handlers)
            if (node.isTerminal()) {
                handlers.addAll(node.getHandlers());
                return true;
            }
            return false;
        }

        String segment = segments.get(segmentIndex);

        // Try matching this segment against current node's children
        // Priority: static nodes first, then parameter nodes
        List<RouteNode> children = node.getChildren();
        List<RouteNode> staticNodes = new ArrayList<>();
        List<RouteNode> paramNodes = new ArrayList<>();

        for (RouteNode child : children) {
            if (child.isParameter()) {
                paramNodes.add(child);
            } else {
                staticNodes.add(child);
            }
        }

        // Try static nodes first (more specific)
        for (RouteNode child : staticNodes) {
            Map<String, String> childParams = new HashMap<>(params);
            if (child.matches(segment, childParams)) {
                // Match found, recurse deeper
                if (dfsMatch(child, segments, segmentIndex + 1, childParams, handlers)) {
                    params.putAll(childParams);
                    return true;
                }
            }
        }

        // Try parameter nodes (less specific, but more flexible)
        for (RouteNode child : paramNodes) {
            Map<String, String> childParams = new HashMap<>(params);
            if (child.matches(segment, childParams)) {
                // Special case: greedy param at end - consume all remaining segments NOW
                if (child.getSegmentPattern().equals("(.*)") && child.isTerminal()) {
                    // Consume all remaining segments into this parameter
                    StringBuilder value = new StringBuilder();
                    value.append(segment); // Start with current segment
                    for (int i = segmentIndex + 1; i < segments.size(); i++) {
                        value.append("/");
                        value.append(segments.get(i));
                    }
                    // Get param name from node
                    if (child.getParamName() != null) {
                        childParams.put(child.getParamName(), value.toString());
                    }
                    handlers.addAll(child.getHandlers());
                    params.putAll(childParams);
                    return true;
                }
                // Match found, recurse deeper
                if (dfsMatch(child, segments, segmentIndex + 1, childParams, handlers)) {
                    params.putAll(childParams);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Split path into segments (excluding leading/trailing slashes).
     * Handles special case for parameters with trailing slashes in patterns.
     */
    private List<String> splitPath(String path) {
        List<String> segments = new ArrayList<>();
        if (path == null || path.isEmpty() || path.equals("/")) {
            segments.add("");
            return segments;
        }

        String normalized = path.startsWith("/") ? path.substring(1) : path;
        String[] parts = normalized.split("/", -1);

        for (String part : parts) {
            if (!part.isEmpty()) {
                segments.add(part);
            }
        }

        return segments;
    }

    /**
     * Get or create root node for HTTP method.
     */
    private RouteNode getOrCreateMethodRoot(String method) {
        return methodRoots.computeIfAbsent(method, k -> new RouteNode("", null));
    }

    /**
     * Match result containing handlers and extracted parameters.
     */
    static class MatchResult {
        private final List<Handler> handlers;
        private final Map<String, String> params;

        MatchResult(List<Handler> handlers, Map<String, String> params) {
            this.handlers = handlers;
            this.params = params;
        }

        List<Handler> getHandlers() {
            return handlers;
        }

        Map<String, String> getParams() {
            return params;
        }
    }
}

