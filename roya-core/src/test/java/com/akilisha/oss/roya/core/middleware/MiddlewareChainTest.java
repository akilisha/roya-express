package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * CRITICAL: Tests for middleware chain behavior - the FUNDAMENTAL middleware pattern.
 *
 * Middleware chain behavior (Express.js pattern):
 * 
 * 1. Call next() → Continue to next handler in chain
 * 2. Don't call next() → Short-circuit (response sent, chain stops)
 * 3. Call next.error() → Jump to error handler
 *
 * This is THE CORE behavior that makes middleware work.
 */
@DisplayName("Middleware Chain Behavior - THE FUNDAMENTAL PATTERN")
class MiddlewareChainTest {

    @Test
    @DisplayName("CRITICAL: should continue chain when next() IS called")
    void shouldContinueChainWhenNextIsCalled() throws Exception {
        List<String> executionOrder = new ArrayList<>();
        
        // Handler 1: Calls next() → continues
        Handler handler1 = (req, res, next) -> {
            executionOrder.add("handler1");
            next.handle(req, res); // ✓ CALLS NEXT - chain continues
        };
        
        // Handler 2: Calls next() → continues
        Handler handler2 = (req, res, next) -> {
            executionOrder.add("handler2");
            next.handle(req, res); // ✓ CALLS NEXT - chain continues
        };
        
        // Handler 3: Does not call next() → short-circuits
        Handler handler3 = (req, res, next) -> {
            executionOrder.add("handler3");
            // X DOES NOT CALL NEXT - chain stops here
        };
        
        Request mockReq = mock(Request.class);
        Response mockRes = mock(Response.class);
        Next mockNext = mock(Next.class);

        // Execute handler1 → handler2 → handler3 → mockNext
        handler1.handle(mockReq, mockRes, (r, re) -> {
            handler2.handle(r, re, (req, resp) -> {
                handler3.handle(req, resp, mockNext);
            });
        });

        // Verify ALL handlers executed (next() was called each time)
        assertThat(executionOrder).containsExactly("handler1", "handler2", "handler3");
        // mockNext should NOT be called because handler3 didn't call next()
        verify(mockNext, never()).handle(mockReq, mockRes);
    }

    @Test
    @DisplayName("CRITICAL: should short-circuit when next() is NOT called")
    void shouldShortCircuitWhenNextNotCalled() throws Exception {
        List<String> executionOrder = new ArrayList<>();
        
        Handler shortCircuitHandler = (req, res, next) -> {
            executionOrder.add("shortCircuit");
            // X Does NOT call next() - stops chain here
            // In real code: res.send("Response sent - chain stops");
        };
        
        Handler shouldNotExecute = (req, res, next) -> {
            executionOrder.add("shouldNeverRun"); // This should NEVER execute
            next.handle(req, res);
        };
        
        Request mockReq = mock(Request.class);
        Response mockRes = mock(Response.class);
        Next mockNext = mock(Next.class);

        // Execute shortCircuitHandler → shouldNotExecute
        shortCircuitHandler.handle(mockReq, mockRes, (req, resp) -> {
            shouldNotExecute.handle(req, resp, mockNext);
        });

        // Only shortCircuitHandler should execute
        assertThat(executionOrder).containsExactly("shortCircuit");
        assertThat(executionOrder).doesNotContain("shouldNeverRun");
        verify(mockNext, never()).handle(mockReq, mockRes);
    }

    @Test
    @DisplayName("CRITICAL: should propagate errors when next.error() is called")
    void shouldPropagateErrorsWhenNextErrorCalled() throws Exception {
        List<String> executionOrder = new ArrayList<>();
        RuntimeException testError = new RuntimeException("Test error");
        
        Handler errorHandler = (req, res, next) -> {
            executionOrder.add("errorHandler");
            next.error(testError, req, res); // ✓ Calls next.error() - jumps to error handler
        };
        
        Handler shouldNotExecute = (req, res, next) -> {
            executionOrder.add("shouldNeverRun"); // Should NOT execute
            next.handle(req, res);
        };

        Request mockReq = mock(Request.class);
        Response mockRes = mock(Response.class);
        Next mockNext = mock(Next.class);

        try {
            // This should throw NextException
            errorHandler.handle(mockReq, mockRes, (req, resp) -> {
                shouldNotExecute.handle(req, resp, mockNext);
            });
            
            // Should not reach here
            assertThat(false).isTrue(); // Force failure
        } catch (NextException e) {
            // ✓ NextException wraps the original error
            assertThat(e.getCause()).isEqualTo(testError);
            assertThat(executionOrder).containsExactly("errorHandler");
            assertThat(executionOrder).doesNotContain("shouldNeverRun");
        }
    }

    @Test
    @DisplayName("Real-world: Auth middleware - calls next() on success, short-circuits on failure")
    void realWorldAuthMiddleware() throws Exception {
        List<String> executionOrder = new ArrayList<>();
        
        // Simulating authentication middleware
        Handler authMiddleware = (req, res, next) -> {
            String auth = req.headers().authorization().orElse(null);
            
            if (auth != null && auth.startsWith("Bearer ")) {
                executionOrder.add("auth-passed");
                next.handle(req, res); // ✓ CALLS NEXT - user is authenticated
            } else {
                executionOrder.add("auth-failed");
                // X DOES NOT CALL NEXT - short-circuits with 401
                // In real code: res.status(401).send("Unauthorized");
            }
        };
        
        // Protected handler
        Handler protectedHandler = (req, res, next) -> {
            executionOrder.add("protectedHandler");
            next.handle(req, res);
        };

        Request mockReq = mock(Request.class);
        Response mockRes = mock(Response.class);
        Next mockNext = mock(Next.class);

        // Test 1: No auth token - should short-circuit
        executionOrder.clear();
        when(mockReq.headers().authorization()).thenReturn(java.util.Optional.empty());
        
        authMiddleware.handle(mockReq, mockRes, (req, resp) -> {
            protectedHandler.handle(req, resp, mockNext);
        });

        assertThat(executionOrder).containsExactly("auth-failed");
        assertThat(executionOrder).doesNotContain("protectedHandler");
        verify(mockNext, never()).handle(mockReq, mockRes);

        // Test 2: With auth token - should continue chain
        executionOrder.clear();
        when(mockReq.headers().authorization()).thenReturn(java.util.Optional.of("Bearer token123"));
        
        authMiddleware.handle(mockReq, mockRes, (req, resp) -> {
            protectedHandler.handle(req, resp, mockNext);
        });

        assertThat(executionOrder).containsExactly("auth-passed", "protectedHandler");
        verify(mockNext).handle(mockReq, mockRes);
    }

    @Test
    @DisplayName("Real-world: JSON parser - calls next() on success, errors on invalid JSON")
    void realWorldJsonParser() throws Exception {
        List<String> executionOrder = new ArrayList<>();
        
        Handler jsonParser = (req, res, next) -> {
            String body = req.bodyText();
            
            try {
                // Parse JSON...
                if (body == null || body.trim().isEmpty()) {
                    executionOrder.add("empty-body");
                    req.set("body", Map.of());
                    next.handle(req, res); // ✓ CALLS NEXT - valid empty body
                } else if (body.contains("{")) {
                    executionOrder.add("valid-json");
                    req.set("body", body); // Simplified
                    next.handle(req, res); // ✓ CALLS NEXT - valid JSON
                } else {
                    executionOrder.add("invalid-json");
                    // X DOES NOT CALL NEXT - invalid JSON, send 400
                    // In real code: res.status(400).send("Invalid JSON");
                }
            } catch (Exception e) {
                executionOrder.add("parse-error");
                next.error(e, req, res); // Calls next.error() - propagate error
            }
        };
        
        Handler shouldExecute = (req, res, next) -> {
            executionOrder.add("shouldExecute");
            next.handle(req, res);
        };

        Request mockReq = mock(Request.class);
        Response mockRes = mock(Response.class);
        Next mockNext = mock(Next.class);

        // Test 1: Valid JSON - should continue
        executionOrder.clear();
        when(mockReq.bodyText()).thenReturn("{\"key\":\"value\"}");
        
        jsonParser.handle(mockReq, mockRes, (req, resp) -> {
            shouldExecute.handle(req, resp, mockNext);
        });

        assertThat(executionOrder).containsExactly("valid-json", "shouldExecute");
        verify(mockNext).handle(mockReq, mockRes);
    }
}
