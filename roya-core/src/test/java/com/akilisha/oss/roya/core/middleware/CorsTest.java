package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for Cors middleware - CORS headers.
 */
@DisplayName("Cors Middleware")
class CorsTest {

    private Request mockRequest;
    private Response mockResponse;
    private Next mockNext;

    @BeforeEach
    void setUp() {
        mockRequest = mock(Request.class);
        mockResponse = mock(Response.class);
        mockNext = mock(Next.class);
    }

    @Test
    @DisplayName("should set CORS headers")
    void shouldSetCorsHeaders() throws Exception {
        when(mockRequest.method()).thenReturn("POST");
        when(mockRequest.headers().get("Origin")).thenReturn(java.util.Optional.of("https://example.com"));
        
        Handler middleware = Cors.cors();
        middleware.handle(mockRequest, mockResponse, mockNext);
        
        verify(mockResponse).header("Access-Control-Allow-Origin", "https://example.com");
        verify(mockResponse).header(eq("Access-Control-Allow-Methods"), anyString());
        verify(mockResponse).header(eq("Access-Control-Allow-Headers"), anyString());
        verify(mockNext).handle(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("should handle preflight OPTIONS request")
    void shouldHandlePreflightOptions() throws Exception {
        when(mockRequest.method()).thenReturn("OPTIONS");
        
        Handler middleware = Cors.cors();
        middleware.handle(mockRequest, mockResponse, mockNext);
        
        verify(mockResponse).status(204);
        verify(mockNext, never()).handle(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("should allow custom CORS options")
    void shouldAllowCustomCorsOptions() throws Exception {
        when(mockRequest.method()).thenReturn("POST");
        when(mockRequest.headers().get("Origin")).thenReturn(java.util.Optional.of("https://mydomain.com"));
        
        Cors.CorsOptions options = Cors.CorsOptions.builder()
            .origin("https://mydomain.com")
            .credentials(true)
            .build();
        
        Handler middleware = Cors.cors(options);
        middleware.handle(mockRequest, mockResponse, mockNext);
        
        verify(mockResponse).header("Access-Control-Allow-Origin", "https://mydomain.com");
        verify(mockResponse).header("Access-Control-Allow-Credentials", "true");
    }
}

