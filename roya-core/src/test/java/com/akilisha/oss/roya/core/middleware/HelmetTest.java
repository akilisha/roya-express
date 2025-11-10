package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for Helmet middleware - security headers.
 */
@DisplayName("Helmet Middleware")
class HelmetTest {

    private Request mockRequest;
    private Response mockResponse;
    private Next mockNext;

    @BeforeEach
    void setUp() {
        mockRequest = mock(Request.class);
        mockResponse = mock(Response.class);
        mockNext = mock(Next.class);
        when(mockResponse.header(anyString(), anyString())).thenReturn(mockResponse);
    }

    @Test
    @DisplayName("should set security headers")
    void shouldSetSecurityHeaders() throws Exception {
        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.secure()).thenReturn(false);

        Handler middleware = Helmet.helmet();
        middleware.handle(mockRequest, mockResponse, mockNext);

        verify(mockResponse).header("X-Content-Type-Options", "nosniff");
        verify(mockResponse).header(eq("X-Frame-Options"), anyString());
        verify(mockResponse).header("X-XSS-Protection", "0");
        verify(mockNext).handle(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("should set HSTS header for HTTPS")
    void shouldSetHstsForHttps() throws Exception {
        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.secure()).thenReturn(true);

        Handler middleware = Helmet.helmet();
        middleware.handle(mockRequest, mockResponse, mockNext);

        verify(mockResponse).header(eq("Strict-Transport-Security"), anyString());
    }

    @Test
    @DisplayName("should not set HSTS for HTTP")
    void shouldNotSetHstsForHttp() throws Exception {
        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.secure()).thenReturn(false);

        Handler middleware = Helmet.helmet();
        middleware.handle(mockRequest, mockResponse, mockNext);

        verify(mockResponse, never()).header(eq("Strict-Transport-Security"), anyString());
    }

    @Test
    @DisplayName("should allow custom helmet options")
    void shouldAllowCustomHelmetOptions() throws Exception {
        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.secure()).thenReturn(false);

        Helmet.HelmetOptions options = Helmet.HelmetOptions.builder()
            .frameOptions("DENY")
            .build();

        Handler middleware = Helmet.helmet(options);
        middleware.handle(mockRequest, mockResponse, mockNext);

        verify(mockResponse).header("X-Frame-Options", "DENY");
    }
}

