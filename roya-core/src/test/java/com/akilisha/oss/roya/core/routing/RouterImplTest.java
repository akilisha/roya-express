package com.akilisha.oss.roya.core.routing;

import com.akilisha.oss.roya.api.*;
import com.akilisha.oss.roya.core.ParamsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

/**
 * Tests for RouterImpl - testing the routing engine.
 */
@DisplayName("RouterImpl")
class RouterImplTest {

    private Router router;
    private Request mockRequest;
    private Response mockResponse;
    private Next mockNext;

    @BeforeEach
    void setUp() {
        router = RouterImpl.create();

        // Create mocks
        mockRequest = mock(Request.class);
        mockResponse = mock(Response.class);
        mockNext = mock(Next.class);

        // Set up default behavior
        when(mockRequest.path()).thenReturn("/");
        when(mockRequest.method()).thenReturn("GET");
        when(mockResponse.isFinished()).thenReturn(false);

        // Mock params() to return real implementation
        ParamsImpl mockParams = new ParamsImpl();
        when(mockRequest.params()).thenReturn(mockParams);

        // Allow setParams to be called (do nothing for mocks)
        // Use anyMap() for cleaner type handling
        doNothing().when(mockRequest).setParams(anyMap());
    }

    // ========== Basic Route Registration ==========

    @Test
    @DisplayName("should register GET routes")
    void shouldRegisterGetRoutes() {
        Handler handler = mock(Handler.class);

        router.get("/", handler);

        verifyNoInteractions(handler);
    }

    @Test
    @DisplayName("should register POST routes")
    void shouldRegisterPostRoutes() {
        Handler handler = mock(Handler.class);

        router.post("/users", handler);

        verifyNoInteractions(handler);
    }

    @Test
    @DisplayName("should register multiple HTTP methods")
    void shouldRegisterMultipleMethods() {
        Handler handler1 = mock(Handler.class);
        Handler handler2 = mock(Handler.class);

        router.get("/users", handler1);
        router.post("/users", handler2);

        verifyNoInteractions(handler1, handler2);
    }

    // ========== Route Matching ==========

    @Test
    @DisplayName("should match routes by HTTP method")
    void shouldMatchRoutesByMethod() throws Exception {
        Handler getHandler = (req, res, next) -> res.send("GET");
        Handler postHandler = (req, res, next) -> res.send("POST");

        router.get("/users", getHandler);
        router.post("/users", postHandler);

        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.path()).thenReturn("/users");

        router.handle(mockRequest, mockResponse, mockNext);

        verify(mockNext, never()).handle(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("should continue to next when no route matches")
    void shouldContinueWhenNoMatch() throws Exception {
        Handler handler = (req, res, next) -> res.send("Hello");

        router.get("/users", handler);

        when(mockRequest.path()).thenReturn("/posts");

        router.handle(mockRequest, mockResponse, mockNext);

        verify(mockNext).handle(mockRequest, mockResponse);
    }

    // ========== Parameterized Routes ==========

    @Test
    @DisplayName("should extract path parameters")
    void shouldExtractPathParameters() throws Exception {
        Handler handler = (req, res, next) -> {
            var params = req.params();
            res.json(java.util.Map.of("id", params.get("id").orElse("")));
        };

        router.get("/users/:id", handler);

        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.path()).thenReturn("/users/123");
        when(mockRequest.params()).thenReturn(new ParamsImpl());

        router.handle(mockRequest, mockResponse, mockNext);

        verify(mockNext, never()).handle(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("should match routes with multiple parameters")
    void shouldMatchMultipleParams() throws Exception {
        Handler handler = mock(Handler.class);

        router.get("/users/:userId/posts/:postId", handler);

        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.path()).thenReturn("/users/123/posts/456");

        router.handle(mockRequest, mockResponse, mockNext);

        verify(handler, times(1)).handle(any(Request.class), any(Response.class), any(Next.class));
        verify(mockNext, never()).handle(mockRequest, mockResponse);
    }

    // ========== Order Precedence ==========

    @Test
    @DisplayName("should match routes in registration order")
    void shouldMatchInRegistrationOrder() throws Exception {
        Handler handler1 = mock(Handler.class);
        Handler handler2 = mock(Handler.class);

        router.get("/users", handler1);
        router.get("/users", handler2);

        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.path()).thenReturn("/users");

        router.handle(mockRequest, mockResponse, mockNext);

        // First handler should be called
        verify(handler1).handle(any(Request.class), any(Response.class), any(Next.class));
        // Second handler should not be called unless the first delegates via next()
        verify(handler2, never()).handle(any(Request.class), any(Response.class), any(Next.class));
    }

    // ========== Middleware ==========

    @Test
    @DisplayName("should register middleware")
    void shouldRegisterMiddleware() throws Exception {
        Handler middleware = mock(Handler.class);

        router.use(middleware);

        when(mockRequest.path()).thenReturn("/any-path");

        router.handle(mockRequest, mockResponse, mockNext);

        verify(middleware).handle(any(Request.class), any(Response.class), any(Next.class));
    }

    @Test
    @DisplayName("should chain middleware")
    void shouldChainMiddleware() throws Exception {
        Handler middleware1 = mock(Handler.class);
        Handler middleware2 = mock(Handler.class);

        router.use(middleware1);
        router.use(middleware2);

        when(mockRequest.path()).thenReturn("/");

        router.handle(mockRequest, mockResponse, mockNext);

        verify(middleware1).handle(any(Request.class), any(Response.class), any(Next.class));
        verify(middleware2).handle(any(Request.class), any(Response.class), any(Next.class));
    }

    // ========== Multiple Handlers per Route ==========

    @Test
    @DisplayName("should execute multiple handlers per route")
    void shouldExecuteMultipleHandlers() throws Exception {
        Handler handler1 = mock(Handler.class);
        Handler handler2 = mock(Handler.class);

        doAnswer(invocation -> {
            Next next = invocation.getArgument(2);
            next.handle(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(handler1).handle(any(Request.class), any(Response.class), any(Next.class));

        router.get("/users", handler1, handler2);

        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.path()).thenReturn("/users");

        router.handle(mockRequest, mockResponse, mockNext);

        // Both handlers should be called
        verify(handler1).handle(any(Request.class), any(Response.class), any(Next.class));
        verify(handler2).handle(any(Request.class), any(Response.class), any(Next.class));
    }

    // ========== Path-Mounted Middleware ==========

    @Test
    @DisplayName("should mount middleware to specific path")
    void shouldMountMiddlewareToPath() throws Exception {
        Handler middleware = mock(Handler.class);

        router.use("/api", middleware);

        when(mockRequest.path()).thenReturn("/api/users");

        router.handle(mockRequest, mockResponse, mockNext);

        verify(middleware).handle(any(Request.class), any(Response.class), any(Next.class));
    }

    @Test
    @DisplayName("should not invoke path-mounted middleware for different paths")
    void shouldNotInvokePathMountedMiddleware() throws Exception {
        Handler middleware = mock(Handler.class);

        router.use("/api", middleware);

        when(mockRequest.path()).thenReturn("/users");

        router.handle(mockRequest, mockResponse, mockNext);

        verify(middleware, never()).handle(any(), any(), any());
    }

    // ========== ALL Method ==========

    @Test
    @DisplayName("should handle all HTTP methods with all()")
    void shouldHandleAllMethods() throws Exception {
        Handler handler = mock(Handler.class);

        router.all("/users", handler);

        String[] methods = {"GET", "POST", "PUT", "DELETE"};

        for (String method : methods) {
            when(mockRequest.method()).thenReturn(method);
            when(mockRequest.path()).thenReturn("/users");

            router.handle(mockRequest, mockResponse, mockNext);

            verify(handler, times(1)).handle(any(Request.class), any(Response.class), any(Next.class));

            // Reset mock for next iteration to avoid accumulation
            reset(handler);
        }
    }
}

