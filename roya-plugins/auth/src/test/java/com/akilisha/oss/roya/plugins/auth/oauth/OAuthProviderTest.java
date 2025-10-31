package com.akilisha.oss.roya.plugins.auth.oauth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OAuth provider implementations.
 */
@DisplayName("OAuth Provider Tests")
class OAuthProviderTest {

    @Test
    @DisplayName("OAuthConfig should create with required fields")
    void testOAuthConfig() {
        OAuthConfig config = new OAuthConfig(
            "client-id",
            "client-secret",
            Arrays.asList("scope1", "scope2")
        );

        assertEquals("client-id", config.clientId());
        assertEquals("client-secret", config.clientSecret());
        assertEquals(2, config.scopes().size());
        assertTrue(config.scopes().contains("scope1"));
        assertTrue(config.scopes().contains("scope2"));
    }

    @Test
    @DisplayName("GoogleOAuthProvider should have correct name")
    void testGoogleProviderName() {
        OAuthConfig config = new OAuthConfig("id", "secret", Arrays.asList("scope"));
        GoogleOAuthProvider provider = new GoogleOAuthProvider(config);
        assertEquals("google", provider.getName());
    }

    @Test
    @DisplayName("GitHubOAuthProvider should have correct name")
    void testGitHubProviderName() {
        OAuthConfig config = new OAuthConfig("id", "secret", Arrays.asList("scope"));
        GitHubOAuthProvider provider = new GitHubOAuthProvider(config);
        assertEquals("github", provider.getName());
    }

    @Test
    @DisplayName("OAuthToken should store token information")
    void testOAuthToken() {
        OAuthToken token = new OAuthToken(
            "access-token",
            "refresh-token",
            java.time.Instant.now().plusSeconds(3600),
            "Bearer",
            java.util.Map.of("key", "value")
        );

        assertEquals("access-token", token.accessToken());
        assertEquals("refresh-token", token.refreshToken());
        assertEquals("Bearer", token.tokenType());
        assertNotNull(token.expiresAt());
        assertEquals("value", token.metadata().get("key"));
    }

    @Test
    @DisplayName("OAuthUserInfo should store user information")
    void testOAuthUserInfo() {
        OAuthUserInfo userInfo = new OAuthUserInfo(
            "provider-id",
            "user@example.com",
            "John Doe",
            "https://example.com/picture.jpg",
            java.util.Map.of("locale", "en")
        );

        assertEquals("provider-id", userInfo.providerId());
        assertEquals("user@example.com", userInfo.email());
        assertEquals("John Doe", userInfo.name());
        assertEquals("https://example.com/picture.jpg", userInfo.picture());
        assertEquals("en", userInfo.metadata().get("locale"));
    }
}

