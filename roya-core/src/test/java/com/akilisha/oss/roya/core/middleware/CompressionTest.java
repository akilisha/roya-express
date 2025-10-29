package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for Compression middleware.
 */
@DisplayName("Compression Middleware")
class CompressionTest {

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
    @DisplayName("should set gzip encoding when client accepts")
    void shouldSetGzipWhenClientAccepts() throws Exception {
        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.headers().get("Accept-Encoding")).thenReturn(java.util.Optional.of("gzip, deflate"));
        
        Handler middleware = Compression.compression();
        middleware.handle(mockRequest, mockResponse, mockNext);
        
        verify(mockResponse).header("Content-Encoding", "gzip");
        verify(mockNext).handle(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("should not set encoding when client doesn't accept gzip")
    void shouldNotSetEncodingWhenNoGzip() throws Exception {
        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.headers().get("Accept-Encoding")).thenReturn(java.util.Optional.of("deflate"));
        
        Handler middleware = Compression.compression();
        middleware.handle(mockRequest, mockResponse, mockNext);
        
        verify(mockResponse, never()).header(eq("Content-Encoding"), anyString());
        verify(mockNext).handle(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("should respect custom filter")
    void shouldRespectCustomFilter() throws Exception {
        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.path()).thenReturn("/api/users");
        when(mockRequest.headers().get("Accept-Encoding")).thenReturn(java.util.Optional.of("gzip"));
        
        Handler middleware = Compression.compression(req -> req.path().startsWith("/static"));
        middleware.handle(mockRequest, mockResponse, mockNext);
        
        verify(mockResponse, never()).header(eq("Content-Encoding"), anyString());
        verify(mockNext).handle(mockRequest, mockResponse);
    }
}

