# Roya Routing Architecture

## Overview

Roya uses a **tree-based routing algorithm** that organizes routes hierarchically by HTTP method and path segments. This design provides **O(depth) lookup time** instead of O(n) linear search, and naturally handles Express.js-style route patterns like `:param`, `*`, and regex constraints.

## Core Concepts

### 1. Path Segmentation

Routes are broken down into **segments** (the parts between `/`):

```
/api/users/:id/posts/:pid
 ↓    ↓     ↓   ↓      ↓
seg1 seg2 param seg3 param
```

### 2. Tree Structure

The router builds a tree where:
- **Root nodes** are organized by **HTTP method** (GET, POST, PUT, DELETE, etc.)
- **Child nodes** represent **path segments**
- **Leaf nodes** (terminal nodes) hold the **handlers** for that route

```
RouteTree
├── GET
│   ├── api
│   │   ├── users
│   │   │   ├── :id (param) → [Handler1]
│   │   │   └── :id/posts/:pid (param → param) → [Handler2]
│   │   └── articles → [Handler3]
│   └── health → [Handler4]
└── POST
    └── api
        ├── users → [Handler5]
        └── articles → [Handler6]
```

### 3. Segment Matchers

Each segment can be matched by:
- **StaticMatcher**: Exact string match (`"users"`, `"api"`)
- **RegexMatcher**: Pattern match (`(.*)`, `(.*?)`, `[0-9]+`)

Parameter segments (`:id`) are converted to regex:
- **Last segment**: `(.*)` - **greedy** (captures everything to end)
- **Middle segment**: `(.*?)` - **non-greedy** (stops at next `/`)

## Route Registration

When you register a route:

```java
router.post("/api/users/:uid/address/:aid", handler);
```

1. **Parse the path** into segments: `["api", "users", "(.*?)", "address", "(.*)"]`
2. **Extract parameter names**: `["uid", "aid"]`
3. **Traverse/build the tree**:
   - Start at method root (POST)
   - For each segment: get or create child node
   - Store param names on parameter nodes
4. **Attach handler** to terminal node

## Route Matching

When a request arrives:

```
GET /api/users/123/address/home
```

1. **Split request path** into segments: `["api", "users", "123", "address", "home"]`
2. **Start DFS** from method root (GET) with `segmentIndex = 0`
3. **At each node**, prioritize matching order:
   - **Static nodes first** (more specific)
   - **Parameter nodes second** (more flexible)
4. **When a match is found**:
   - Capture param values in a map
   - Continue to next segment (recursively)
5. **When all segments consumed** and node is terminal → **SUCCESS**

### Special Case: Greedy Parameters

A greedy param at the end (`(.*)`) **consumes all remaining segments**:

```
Route:   /articles/:slug
Request: /articles/2024/my-year-in-review

Result: slug = "2024/my-year-in-review"
```

Greedy params are detected when:
- Segment pattern is `(.*)` (terminal segment)
- Node is terminal (has handlers)
- We enter this node during DFS

The router immediately consumes all remaining segments into the parameter.

## Code Architecture

### PathSegmentParser

Converts Express-style paths to segment arrays:

```java
PathSegmentParser.parse("/api/users/:uid/address/:aid")
// Returns: ["api", "user", "(.*?)", "address", "(.*)"]
//          ["api", "user", "(.*?)", "address", "(.*)"]
```

**Key logic:**
- `:param` at end → `(.*)` (greedy)
- `:param` in middle → `(.*?)` (non-greedy)
- Static segments → as-is
- Regex patterns → wrapped in `()`

### SegmentMatcher

Interface for matching segments:

```java
interface SegmentMatcher {
    boolean matches(String segment, Map<String, String> params, String paramName);
}
```

**Implementations:**
- `StaticSegmentMatcher`: Uses `equals()`
- `RegexSegmentMatcher`: Uses `Pattern.matcher()`, captures groups

### RouteNode

Represents a tree node:

```java
class RouteNode {
    SegmentMatcher matcher;      // How to match this segment
    String segmentPattern;        // Original pattern
    String paramName;             // Param name (if parameter node)
    List<RouteNode> children;     // Child nodes
    List<Handler> handlers;       // Terminal handlers
}
```

**Key methods:**
- `getOrCreateChild(pattern, paramName)`: Builds tree
- `matches(segment, params)`: Checks if segment matches
- `isParameter()`: Identifies param nodes for priority ordering

### RouteTree

The main tree structure:

```java
class RouteTree {
    Map<String, RouteNode> methodRoots;  // GET, POST, etc.
    
    void addRoute(String method, String path, Handler handler);
    MatchResult match(String method, String path);
}
```

**MatchResult:**
```java
class MatchResult {
    List<Handler> handlers;      // Matched handlers
    Map<String, String> params;  // Extracted parameters
}
```

### DFS Algorithm

```java
private boolean dfsMatch(RouteNode node, List<String> segments, int segmentIndex,
                        Map<String, String> params, List<Handler> handlers) {
    // Base case: consumed all segments
    if (segmentIndex >= segments.size()) {
        if (node.isTerminal()) {
            handlers.addAll(node.getHandlers());
            return true;  // Match found
        }
        return false;  // Dead end
    }
    
    String segment = segments.get(segmentIndex);
    List<RouteNode> children = node.getChildren();
    
    // Separate static and param nodes
    List<RouteNode> staticNodes = ...;
    List<RouteNode> paramNodes = ...;
    
    // Try static first (priority)
    for (RouteNode child : staticNodes) {
        if (child.matches(segment, params)) {
            if (dfsMatch(child, segments, segmentIndex + 1, params, handlers)) {
                return true;
            }
        }
    }
    
    // Try params second
    for (RouteNode child : paramNodes) {
        // Special case: greedy param at end
        if (child.getSegmentPattern().equals("(.*)") && child.isTerminal()) {
            // Consume all remaining segments
            String value = segment;
            for (int i = segmentIndex + 1; i < segments.size(); i++) {
                value += "/" + segments.get(i);
            }
            params.put(child.getParamName(), value);
            handlers.addAll(child.getHandlers());
            return true;
        }
        
        if (child.matches(segment, params)) {
            if (dfsMatch(child, segments, segmentIndex + 1, params, handlers)) {
                return true;
            }
        }
    }
    
    return false;  // No match
}
```

## Examples

### Example 1: Static Route

```java
router.get("/hello", handler);
```

**Tree:**
```
GET → hello [handler]
```

**Match:** `GET /hello` ✅

### Example 2: Single Parameter

```java
router.get("/users/:id", handler);
```

**Tree:**
```
GET → users → (.*) [handler]
            param: id
```

**Match:** `GET /users/123` ✅ → `params = {id: "123"}`

### Example 3: Multiple Parameters

```java
router.get("/api/user/:uid/address/:aid", handler);
```

**Tree:**
```
GET → api → user → (.*?) → address → (.*) [handler]
                    uid                    aid
```

**Match:** `GET /api/user/alice/address/home` ✅
- `uid = "alice"`
- `aid = "home"`

### Example 4: Greedy Parameter

```java
router.get("/articles/:slug", handler);
```

**Tree:**
```
GET → articles → (.*) [handler]
                 slug (greedy)
```

**Match:** `GET /articles/2024/my-year-in-review` ✅
- `slug = "2024/my-year-in-review"` (consumes all remaining segments)

### Example 5: Static Priority

```java
router.get("/users/admin", handler1);
router.get("/users/:id", handler2);
```

**Tree:**
```
GET → users → admin [handler1]
           → (.*) [handler2]
                  id
```

**Priority:**
- `GET /users/admin` → matches **static** `admin` → handler1 ✅
- `GET /users/123` → matches **param** `(.*)` → handler2 ✅

### Example 6: Method-Specific Routes

```java
router.get("/api/user/:uid", getHandler);
router.post("/api/user/:uid", postHandler);
router.put("/api/user/:uid", putHandler);
```

**Tree:**
```
GET  → api → user → (.*) [getHandler]
POST → api → user → (.*) [postHandler]
PUT  → api → user → (.*) [putHandler]
```

**Matching:**
- `GET /api/user/123` → getHandler ✅
- `POST /api/user/123` → postHandler ✅
- `PUT /api/user/123` → putHandler ✅
- `DELETE /api/user/123` → 404 ❌

## Performance Characteristics

### Lookup Time

- **Old (linear)**: O(n) where n = number of routes
- **New (tree)**: O(d) where d = depth of route

For a typical API with 100 routes of depth 3:
- Linear: **100 comparisons** (worst case)
- Tree: **3 comparisons** (always)

### Memory

- Each route adds **O(depth)** nodes
- Typical overhead: ~50 bytes per node
- 1000 routes of depth 3: ~150KB

### Trade-offs

**Advantages:**
- Fast lookup, even with thousands of routes
- Natural handling of nested routes
- Priority ordering (static > params) built-in
- Extensible to regex constraints

**Disadvantages:**
- More complex than linear search
- Tree traversal overhead for very simple apps
- Requires path parsing upfront

## Testing

See `roya-core/src/test/java/com/akilisha/oss/roya/core/routing/TreeRouterTest.java` for comprehensive tests covering:
- Static routes
- Single/multiple parameters
- Greedy parameters
- Route priority
- Method-specific routes
- Deep nesting

## Future Enhancements

Potential improvements:
- Regex constraints: `/:id(\\d+)` → `[0-9]+`
- Wildcards: `/*` → catch-all routes
- Optional params: `/:id?` → optional matching
- Route groups: better organization for related routes
- Middleware at tree nodes: per-segment middleware

## References

- Express.js Routing: https://expressjs.com/en/guide/routing.html
- Trie/Patricia Tree: Classic tree-based routing algorithm
- Radix Tree: Space-optimized tree structure
