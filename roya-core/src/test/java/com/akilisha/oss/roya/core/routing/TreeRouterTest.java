package com.akilisha.oss.roya.core.routing;

import com.akilisha.oss.roya.api.Handler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for tree-based routing infrastructure.
 * 
 * Tests the core routing algorithm directly without HTTP layer integration.
 * 
 * Examples tested:
 * - GET     /
 * - GET     /hello
 * - POST    /api/user
 * - GET     /api/user/:uid
 * - PUT     /api/user/:uid
 * - DELETE  /api/user/:uid
 * - POST    /api/user/:uid/address
 * - GET     /api/user/:uid/address/:aid
 * - PUT     /api/user/:uid/address/:aid
 * - DELETE  /api/user/:uid/address/:aid
 */
@DisplayName("Tree Router Infrastructure")
class TreeRouterTest {

    private RouteTree routeTree;

    @BeforeEach
    void setUp() {
        routeTree = new RouteTree();
    }

    // ========== PathSegmentParser Tests ==========

    @Test
    @DisplayName("should parse root path")
    void testParseRoot() {
        List<String> segments = PathSegmentParser.parse("/");
        assertEquals(1, segments.size());
        assertEquals("", segments.get(0));
    }

    @Test
    @DisplayName("should parse simple static path")
    void testParseSimpleStatic() {
        List<String> segments = PathSegmentParser.parse("/hello");
        assertEquals(1, segments.size());
        assertEquals("hello", segments.get(0));
    }

    @Test
    @DisplayName("should parse parameter at end")
    void testParseParamAtEnd() {
        List<String> segments = PathSegmentParser.parse("/users/:id");
        assertEquals(2, segments.size());
        assertEquals("users", segments.get(0));
        assertEquals("(.*)", segments.get(1)); // Greedy at end
    }

    @Test
    @DisplayName("should parse non-greedy parameter in middle")
    void testParseNonGreedyParamInMiddle() {
        List<String> segments = PathSegmentParser.parse("/api/user/:uid/address");
        assertEquals(4, segments.size());
        assertEquals("api", segments.get(0));
        assertEquals("user", segments.get(1));
        assertEquals("(.*?)", segments.get(2)); // Non-greedy in middle
        assertEquals("address", segments.get(3));
    }

    @Test
    @DisplayName("should parse multiple parameters")
    void testParseMultipleParams() {
        List<String> segments = PathSegmentParser.parse("/api/user/:uid/address/:aid");
        assertEquals(5, segments.size());
        assertEquals("api", segments.get(0));
        assertEquals("user", segments.get(1));
        assertEquals("(.*?)", segments.get(2)); // Non-greedy :uid
        assertEquals("address", segments.get(3));
        assertEquals("(.*)", segments.get(4)); // Greedy :aid at end
    }

    @Test
    @DisplayName("should extract parameter names")
    void testExtractParamNames() {
        List<String> names = PathSegmentParser.extractParamNames("/api/user/:uid/address/:aid");
        assertEquals(2, names.size());
        assertEquals("uid", names.get(0));
        assertEquals("aid", names.get(1));
    }

    // ========== RouteTree Matching Tests ==========

    @Test
    @DisplayName("should match root path")
    void testMatchRootPath() {
        Handler handler = mockHandler();
        
        routeTree.addRoute("GET", "/", handler);
        RouteTree.MatchResult result = routeTree.match("GET", "/");
        
        assertNotNull(result);
        assertEquals(1, result.getHandlers().size());
        assertEquals(handler, result.getHandlers().get(0));
        assertTrue(result.getParams().isEmpty());
    }

    @Test
    @DisplayName("should match simple static route")
    void testMatchSimpleStatic() {
        Handler handler = mockHandler();
        
        routeTree.addRoute("GET", "/hello", handler);
        RouteTree.MatchResult result = routeTree.match("GET", "/hello");
        
        assertNotNull(result);
        assertEquals(1, result.getHandlers().size());
        assertEquals(handler, result.getHandlers().get(0));
    }

    @Test
    @DisplayName("should not match non-existent route")
    void testNoMatch() {
        Handler handler = mockHandler();
        
        routeTree.addRoute("GET", "/hello", handler);
        RouteTree.MatchResult result = routeTree.match("GET", "/goodbye");
        
        assertNull(result);
    }

    @Test
    @DisplayName("should extract single path parameter")
    void testMatchSingleParam() {
        Handler handler = mockHandler();
        
        routeTree.addRoute("GET", "/users/:id", handler);
        RouteTree.MatchResult result = routeTree.match("GET", "/users/123");
        
        assertNotNull(result);
        assertEquals(1, result.getHandlers().size());
        Map<String, String> params = result.getParams();
        assertEquals("123", params.get("id"));
    }

    @Test
    @DisplayName("should extract greedy parameter at end")
    void testMatchGreedyParamAtEnd() {
        Handler handler = mockHandler();
        
        routeTree.addRoute("GET", "/users/:uid", handler);
        RouteTree.MatchResult result = routeTree.match("GET", "/users/alice-smith-123");
        
        assertNotNull(result);
        Map<String, String> params = result.getParams();
        assertEquals("alice-smith-123", params.get("uid"));
    }

    @Test
    @DisplayName("should extract multiple path parameters")
    void testMatchMultipleParams() {
        Handler handler = mockHandler();
        
        routeTree.addRoute("GET", "/api/user/:uid/address/:aid", handler);
        RouteTree.MatchResult result = routeTree.match("GET", "/api/user/alice/address/home");
        
        assertNotNull(result);
        Map<String, String> params = result.getParams();
        assertEquals("alice", params.get("uid"));
        assertEquals("home", params.get("aid"));
    }

    @Test
    @DisplayName("should capture non-greedy parameter in middle")
    void testMatchNonGreedyParamInMiddle() {
        Handler handler = mockHandler();
        
        routeTree.addRoute("GET", "/api/user/:uid/address/:aid", handler);
        RouteTree.MatchResult result = routeTree.match("GET", "/api/user/some-id-123/address/work");
        
        assertNotNull(result);
        Map<String, String> params = result.getParams();
        assertEquals("some-id-123", params.get("uid")); // Non-greedy stops at /
        assertEquals("work", params.get("aid"));
    }

    @Test
    @DisplayName("should not match when method doesn't match")
    void testMethodMismatch() {
        Handler handler = mockHandler();
        
        routeTree.addRoute("POST", "/users/:id", handler);
        RouteTree.MatchResult result = routeTree.match("GET", "/users/123");
        
        assertNull(result);
    }

    @Test
    @DisplayName("should match method-specific routes")
    void testMethodSpecific() {
        Handler getHandler = mockHandler();
        Handler postHandler = mockHandler();
        Handler putHandler = mockHandler();
        Handler delHandler = mockHandler();
        
        routeTree.addRoute("GET", "/api/user/:uid", getHandler);
        routeTree.addRoute("POST", "/api/user/:uid", postHandler);
        routeTree.addRoute("PUT", "/api/user/:uid", putHandler);
        routeTree.addRoute("DELETE", "/api/user/:uid", delHandler);
        
        assertNotNull(routeTree.match("GET", "/api/user/123"));
        assertNotNull(routeTree.match("POST", "/api/user/123"));
        assertNotNull(routeTree.match("PUT", "/api/user/123"));
        assertNotNull(routeTree.match("DELETE", "/api/user/123"));
        
        assertNull(routeTree.match("PATCH", "/api/user/123"));
    }

    @Test
    @DisplayName("should prioritize static routes over parameter routes")
    void testStaticPriorityOverParams() {
        Handler staticHandler = mockHandler();
        Handler paramHandler = mockHandler();
        
        routeTree.addRoute("GET", "/users/specific", staticHandler);
        routeTree.addRoute("GET", "/users/:id", paramHandler);
        
        // Static path should match static route
        RouteTree.MatchResult result1 = routeTree.match("GET", "/users/specific");
        assertNotNull(result1);
        assertEquals(staticHandler, result1.getHandlers().get(0));
        assertTrue(result1.getParams().isEmpty());
        
        // Param path should match param route
        RouteTree.MatchResult result2 = routeTree.match("GET", "/users/123");
        assertNotNull(result2);
        assertEquals(paramHandler, result2.getHandlers().get(0));
        assertEquals("123", result2.getParams().get("id"));
    }

    @Test
    @DisplayName("should handle deep nested routes")
    void testDeepNestedRoutes() {
        Handler handler = mockHandler();
        
        routeTree.addRoute("GET", "/a/b/c/d/e/f", handler);
        RouteTree.MatchResult result = routeTree.match("GET", "/a/b/c/d/e/f");
        
        assertNotNull(result);
        assertEquals(handler, result.getHandlers().get(0));
    }

    @Test
    @DisplayName("should match routes with different segment counts")
    void testDifferentSegmentCounts() {
        Handler handler1 = mockHandler();
        Handler handler2 = mockHandler();
        
        routeTree.addRoute("GET", "/users/:id", handler1);
        routeTree.addRoute("GET", "/users/:userId/posts/:postId", handler2);
        
        // Shorter route
        RouteTree.MatchResult result1 = routeTree.match("GET", "/users/123");
        assertNotNull(result1);
        assertEquals(handler1, result1.getHandlers().get(0));
        assertEquals("123", result1.getParams().get("id"));
        
        // Longer route
        RouteTree.MatchResult result2 = routeTree.match("GET", "/users/123/posts/456");
        assertNotNull(result2);
        assertEquals(handler2, result2.getHandlers().get(0));
        assertEquals("123", result2.getParams().get("userId"));
        assertEquals("456", result2.getParams().get("postId"));
    }

    // Helper method
    private Handler mockHandler() {
        return (req, res, next) -> {};
    }
}

