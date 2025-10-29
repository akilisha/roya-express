package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for Json middleware - JSON body parsing.
 */
@DisplayName("Json Middleware")
class JsonTest {

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
    @DisplayName("should parse JSON body")
    void shouldParseJsonBody() throws Exception {
        String jsonBody = "{\"name\":\"John\",\"age\":30}";
        
        when(mockRequest.method()).thenReturn("POST");
        when(mockRequest.headers().get("Content-Type")).thenReturn(java.util.Optional.of("application/json"));
        when(mockRequest.bodyText()).thenReturn(jsonBody);
        when(mockRequest.get("body")).thenReturn(null); // Not already parsed
        
        Handler middleware = Json.json();
        middleware.handle(mockRequest, mockResponse, mockNext);
        
        verify(mockRequest).set("body", any());
        verify(mockNext).handle(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("should skip non-JSON content types")
    void shouldSkipNonJsonContentTypes() throws Exception {
        when(mockRequest.method()).thenReturn("POST");
        when(mockRequest.headers().get("Content-Type")).thenReturn(java.util.Optional.of("text/plain"));
        
        Handler middleware = Json.json();
        middleware.handle(mockRequest, mockResponse, mockNext);
        
        verify(mockRequest, never()).set(eq("body"), any());
        verify(mockNext).handle(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("should skip methods without body")
    void shouldSkipMethodsWithoutBody() throws Exception {
        when(mockRequest.method()).thenReturn("GET");
        
        Handler middleware = Json.json();
        middleware.handle(mockRequest, mockResponse, mockNext);
        
        verify(mockRequest, never()).set(eq("body"), any());
        verify(mockNext).handle(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("should not re-parse if already parsed")
    void shouldNotReParseIfAlreadyParsed() throws Exception {
        when(mockRequest.method()).thenReturn("POST");
        when(mockRequest.headers().get("Content-Type")).thenReturn(java.util.Optional.of("application/json"));
        when(mockRequest.get("body")).thenReturn(Map.of()); // Already has body
        
        Handler middleware = Json.json();
        middleware.handle(mockRequest, mockResponse, mockNext);
        
        verify(mockRequest, never()).bodyText();
        verify(mockNext).handle(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("should handle empty JSON body")
    void shouldHandleEmptyJsonBody() throws Exception {
        when(mockRequest.method()).thenReturn("POST");
        when(mockRequest.headers().get("Content-Type")).thenReturn(java.util.Optional.of("application/json"));
        when(mockRequest.bodyText()).thenReturn("");
        when(mockRequest.get("body")).thenReturn(null);
        
        Handler middleware = Json.json();
        middleware.handle(mockRequest, mockResponse, mockNext);
        
        verify(mockRequest).set("body", Map.of());
        verify(mockNext).handle(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("should send 400 on invalid JSON")
    void shouldSend400OnInvalidJson() throws Exception {
        String invalidJson = "{invalid json}";
        
        when(mockRequest.method()).thenReturn("POST");
        when(mockRequest.headers().get("Content-Type")).thenReturn(java.util.Optional.of("application/json"));
        when(mockRequest.bodyText()).thenReturn(invalidJson);
        when(mockRequest.get("body")).thenReturn(null);
        
        Handler middleware = Json.json();
        middleware.handle(mockRequest, mockResponse, mockNext);
        
        verify(mockResponse).status(400);
        verify(mockResponse).json(any());
        verify(mockNext, never()).handle(mockRequest, mockResponse);
    }
}

