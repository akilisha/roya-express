package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.Mockito.*;

/**
 * Tests for CookieParser middleware.
 */
@DisplayName("CookieParser Middleware")
class CookieParserTest {

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
    @DisplayName("should call next()")
    void shouldCallNext() throws Exception {
        Handler middleware = CookieParser.cookieParser();
        middleware.handle(mockRequest, mockResponse, mockNext);
        
        verify(mockNext).handle(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("should work with secret")
    void shouldWorkWithSecret() throws Exception {
        Handler middleware = CookieParser.cookieParser("my-secret");
        middleware.handle(mockRequest, mockResponse, mockNext);
        
        verify(mockNext).handle(mockRequest, mockResponse);
    }
}

