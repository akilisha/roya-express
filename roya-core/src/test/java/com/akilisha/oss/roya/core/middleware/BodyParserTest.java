package com.akilisha.oss.roya.core.middleware;

import com.akilisha.oss.roya.api.Handler;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for BodyParser middleware - form-urlencoded body parsing.
 */
@DisplayName("BodyParser Middleware - Form URL Encoded")
class BodyParserTest {

    private Request mockRequest;
    private Response mockResponse;
    private Next mockNext;
    private com.akilisha.oss.roya.api.Headers mockHeaders;

    @BeforeEach
    void setUp() {
        mockRequest = mock(Request.class);
        mockResponse = mock(Response.class);
        mockNext = mock(Next.class);
        mockHeaders = mock(com.akilisha.oss.roya.api.Headers.class);
        
        when(mockRequest.headers()).thenReturn(mockHeaders);
        when(mockRequest.get(ObjectMapper.class)).thenReturn(new ObjectMapper());
    }

    @Test
    @DisplayName("should parse basic form-urlencoded body")
    void shouldParseBasicFormUrlEncoded() throws Exception {
        String bodyText = "conversationId=1&message=hello";

        when(mockRequest.method()).thenReturn("POST");
        when(mockHeaders.contentType()).thenReturn(Optional.of("application/x-www-form-urlencoded"));
        when(mockRequest.bodyText()).thenReturn(bodyText);
        when(mockRequest.get("body")).thenReturn(null); // Not already parsed

        Handler middleware = BodyParser.bodyParser();
        middleware.handle(mockRequest, mockResponse, mockNext);

        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(mockRequest).set(eq("body"), captor.capture());
        @SuppressWarnings("unchecked")
        Map<String, String> parsed = (Map<String, String>) captor.getValue();
        assertEquals("1", parsed.get("conversationId"));
        assertEquals("hello", parsed.get("message"));
        verify(mockNext).handle(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("should decode URL-encoded values")
    void shouldDecodeUrlEncodedValues() throws Exception {
        String bodyText = "message=dood%2C%20where%27s%20my%20car%3F";

        when(mockRequest.get("body")).thenReturn(null);
        when(mockRequest.method()).thenReturn("POST");
        when(mockHeaders.contentType()).thenReturn(Optional.of("application/x-www-form-urlencoded"));
        when(mockRequest.bodyText()).thenReturn(bodyText);

        Handler middleware = BodyParser.bodyParser();
        middleware.handle(mockRequest, mockResponse, mockNext);

        // Verify the body was set with decoded values
        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(mockRequest).set(eq("body"), captor.capture());
        
        @SuppressWarnings("unchecked")
        Map<String, String> parsed = (Map<String, String>) captor.getValue();
        assertEquals("dood, where's my car?", parsed.get("message"));
    }

    @Test
    @DisplayName("should handle empty values")
    void shouldHandleEmptyValues() throws Exception {
        String bodyText = "key1=value1&key2=&key3=value3";

        when(mockRequest.get("body")).thenReturn(null);
        when(mockRequest.method()).thenReturn("POST");
        when(mockHeaders.contentType()).thenReturn(Optional.of("application/x-www-form-urlencoded"));
        when(mockRequest.bodyText()).thenReturn(bodyText);

        Handler middleware = BodyParser.bodyParser();
        middleware.handle(mockRequest, mockResponse, mockNext);

        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(mockRequest).set(eq("body"), captor.capture());
        
        @SuppressWarnings("unchecked")
        Map<String, String> parsed = (Map<String, String>) captor.getValue();
        assertEquals("value1", parsed.get("key1"));
        assertEquals("", parsed.get("key2")); // Empty value should be preserved
        assertEquals("value3", parsed.get("key3"));
    }

    @Test
    @DisplayName("should handle equals sign in value")
    void shouldHandleEqualsInValue() throws Exception {
        String bodyText = "equation=x%3Dy%2Bz"; // x=y+z

        when(mockRequest.get("body")).thenReturn(null);
        when(mockRequest.method()).thenReturn("POST");
        when(mockHeaders.contentType()).thenReturn(Optional.of("application/x-www-form-urlencoded"));
        when(mockRequest.bodyText()).thenReturn(bodyText);

        Handler middleware = BodyParser.bodyParser();
        middleware.handle(mockRequest, mockResponse, mockNext);

        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(mockRequest).set(eq("body"), captor.capture());
        
        @SuppressWarnings("unchecked")
        Map<String, String> parsed = (Map<String, String>) captor.getValue();
        assertEquals("x=y+z", parsed.get("equation"));
    }

    @Test
    @DisplayName("should skip pairs without equals sign")
    void shouldSkipPairsWithoutEquals() throws Exception {
        String bodyText = "key1=value1&key2"; // key2 has no equals

        when(mockRequest.get("body")).thenReturn(null);
        when(mockRequest.method()).thenReturn("POST");
        when(mockHeaders.contentType()).thenReturn(Optional.of("application/x-www-form-urlencoded"));
        when(mockRequest.bodyText()).thenReturn(bodyText);

        Handler middleware = BodyParser.bodyParser();
        middleware.handle(mockRequest, mockResponse, mockNext);

        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(mockRequest).set(eq("body"), captor.capture());
        
        @SuppressWarnings("unchecked")
        Map<String, String> parsed = (Map<String, String>) captor.getValue();
        assertEquals("value1", parsed.get("key1"));
        assertEquals("", parsed.get("key2")); // No equals treated as empty value
        assertEquals(2, parsed.size());
    }

    @Test
    @DisplayName("should handle empty body")
    void shouldHandleEmptyBody() throws Exception {
        String bodyText = "";

        when(mockRequest.get("body")).thenReturn(null);
        when(mockRequest.method()).thenReturn("POST");
        when(mockHeaders.contentType()).thenReturn(Optional.of("application/x-www-form-urlencoded"));
        when(mockRequest.bodyText()).thenReturn(bodyText);

        Handler middleware = BodyParser.bodyParser();
        middleware.handle(mockRequest, mockResponse, mockNext);

        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(mockRequest).set(eq("body"), captor.capture());
        
        @SuppressWarnings("unchecked")
        Map<String, String> parsed = (Map<String, String>) captor.getValue();
        assertTrue(parsed.isEmpty(), "Empty body should result in empty map");
    }

    @Test
    @DisplayName("should handle null body")
    void shouldHandleNullBody() throws Exception {
        when(mockRequest.get("body")).thenReturn(null);
        when(mockRequest.method()).thenReturn("POST");
        when(mockHeaders.contentType()).thenReturn(Optional.of("application/x-www-form-urlencoded"));
        when(mockRequest.bodyText()).thenReturn(null);

        Handler middleware = BodyParser.bodyParser();
        middleware.handle(mockRequest, mockResponse, mockNext);

        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(mockRequest).set(eq("body"), captor.capture());
        
        @SuppressWarnings("unchecked")
        Map<String, String> parsed = (Map<String, String>) captor.getValue();
        assertTrue(parsed.isEmpty(), "Null body should result in empty map");
    }

    @Test
    @DisplayName("should handle multiple key-value pairs")
    void shouldHandleMultiplePairs() throws Exception {
        String bodyText = "a=1&b=2&c=3&d=4";

        when(mockRequest.get("body")).thenReturn(null);
        when(mockRequest.method()).thenReturn("POST");
        when(mockHeaders.contentType()).thenReturn(Optional.of("application/x-www-form-urlencoded"));
        when(mockRequest.bodyText()).thenReturn(bodyText);

        Handler middleware = BodyParser.bodyParser();
        middleware.handle(mockRequest, mockResponse, mockNext);

        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(mockRequest).set(eq("body"), captor.capture());
        
        @SuppressWarnings("unchecked")
        Map<String, String> parsed = (Map<String, String>) captor.getValue();
        assertEquals("1", parsed.get("a"));
        assertEquals("2", parsed.get("b"));
        assertEquals("3", parsed.get("c"));
        assertEquals("4", parsed.get("d"));
        assertEquals(4, parsed.size());
    }

    @Test
    @DisplayName("should handle spaces in values")
    void shouldHandleSpacesInValues() throws Exception {
        String bodyText = "name=John%20Doe&city=New%20York";

        when(mockRequest.get("body")).thenReturn(null);
        when(mockRequest.method()).thenReturn("POST");
        when(mockHeaders.contentType()).thenReturn(Optional.of("application/x-www-form-urlencoded"));
        when(mockRequest.bodyText()).thenReturn(bodyText);

        Handler middleware = BodyParser.bodyParser();
        middleware.handle(mockRequest, mockResponse, mockNext);

        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(mockRequest).set(eq("body"), captor.capture());
        
        @SuppressWarnings("unchecked")
        Map<String, String> parsed = (Map<String, String>) captor.getValue();
        assertEquals("John Doe", parsed.get("name"));
        assertEquals("New York", parsed.get("city"));
    }

    @Test
    @DisplayName("should handle plus signs in values")
    void shouldHandlePlusSigns() throws Exception {
        String bodyText = "email=user%2Btest%40example.com"; // user+test@example.com

        when(mockRequest.get("body")).thenReturn(null);
        when(mockRequest.method()).thenReturn("POST");
        when(mockHeaders.contentType()).thenReturn(Optional.of("application/x-www-form-urlencoded"));
        when(mockRequest.bodyText()).thenReturn(bodyText);

        Handler middleware = BodyParser.bodyParser();
        middleware.handle(mockRequest, mockResponse, mockNext);

        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(mockRequest).set(eq("body"), captor.capture());
        
        @SuppressWarnings("unchecked")
        Map<String, String> parsed = (Map<String, String>) captor.getValue();
        assertEquals("user+test@example.com", parsed.get("email"));
    }

    @Test
    @DisplayName("should skip invalid URL-encoded pairs gracefully")
    void shouldSkipInvalidPairs() throws Exception {
        String bodyText = "valid=value&invalid%"; // Invalid encoding

        when(mockRequest.get("body")).thenReturn(null);
        when(mockRequest.method()).thenReturn("POST");
        when(mockHeaders.contentType()).thenReturn(Optional.of("application/x-www-form-urlencoded"));
        when(mockRequest.bodyText()).thenReturn(bodyText);

        Handler middleware = BodyParser.bodyParser();
        middleware.handle(mockRequest, mockResponse, mockNext);

        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(mockRequest).set(eq("body"), captor.capture());
        
        @SuppressWarnings("unchecked")
        Map<String, String> parsed = (Map<String, String>) captor.getValue();
        assertEquals("value", parsed.get("valid"));
        // Invalid pair should be skipped (no exception thrown)
    }

    @Test
    @DisplayName("should skip GET requests")
    void shouldSkipGetRequests() throws Exception {
        when(mockRequest.get("body")).thenReturn(null);
        when(mockRequest.method()).thenReturn("GET");
        when(mockHeaders.contentType()).thenReturn(Optional.of("application/x-www-form-urlencoded"));
        when(mockRequest.bodyText()).thenReturn("key=value");

        Handler middleware = BodyParser.bodyParser();
        middleware.handle(mockRequest, mockResponse, mockNext);

        verify(mockRequest, never()).set(eq("body"), any());
        verify(mockNext).handle(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("should skip if body already parsed")
    void shouldSkipIfAlreadyParsed() throws Exception {
        Map<String, String> existingBody = Map.of("key", "value");

        when(mockRequest.get("body")).thenReturn(existingBody);
        when(mockRequest.method()).thenReturn("POST");
        when(mockHeaders.contentType()).thenReturn(Optional.of("application/x-www-form-urlencoded"));

        Handler middleware = BodyParser.bodyParser();
        middleware.handle(mockRequest, mockResponse, mockNext);

        verify(mockRequest, never()).set(eq("body"), any());
        verify(mockNext).handle(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("should handle case-insensitive Content-Type")
    void shouldHandleCaseInsensitiveContentType() throws Exception {
        String bodyText = "key=value";

        when(mockRequest.get("body")).thenReturn(null);
        when(mockRequest.method()).thenReturn("POST");
        when(mockHeaders.contentType()).thenReturn(Optional.of("APPLICATION/X-WWW-FORM-URLENCODED"));
        when(mockRequest.bodyText()).thenReturn(bodyText);

        Handler middleware = BodyParser.bodyParser();
        middleware.handle(mockRequest, mockResponse, mockNext);

        verify(mockRequest).set(eq("body"), any(Map.class));
        verify(mockNext).handle(mockRequest, mockResponse);
    }

    @Test
    @DisplayName("should handle Content-Type with charset")
    void shouldHandleContentTypeWithCharset() throws Exception {
        String bodyText = "key=value";

        when(mockRequest.get("body")).thenReturn(null);
        when(mockRequest.method()).thenReturn("POST");
        when(mockHeaders.contentType()).thenReturn(Optional.of("application/x-www-form-urlencoded; charset=UTF-8"));
        when(mockRequest.bodyText()).thenReturn(bodyText);

        Handler middleware = BodyParser.bodyParser();
        middleware.handle(mockRequest, mockResponse, mockNext);

        verify(mockRequest).set(eq("body"), any(Map.class));
        verify(mockNext).handle(mockRequest, mockResponse);
    }
}
