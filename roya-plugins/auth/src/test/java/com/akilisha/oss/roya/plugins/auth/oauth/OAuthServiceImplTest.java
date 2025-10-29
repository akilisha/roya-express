package com.akilisha.oss.roya.plugins.auth.oauth;

import com.akilisha.oss.roya.plugins.auth.*;
import com.akilisha.oss.roya.plugins.database.Database;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OAuthServiceImpl.
 */
@DisplayName("OAuth Service Implementation Tests")
class OAuthServiceImplTest {

    @Mock
    private Auth auth;
    
    @Mock
    private Database database;

    private OAuthServiceImpl oauthService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        oauthService = new OAuthServiceImpl(auth, database);
    }

    @Test
    @DisplayName("Should register OAuth providers")
    void testRegisterProvider() {
        OAuthConfig config = new OAuthConfig("id", "secret", Arrays.asList("scope"));
        GoogleOAuthProvider provider = new GoogleOAuthProvider(config);
        
        oauthService.registerProvider(provider);
        
        List<String> providers = oauthService.getProviders();
        assertEquals(1, providers.size());
        assertTrue(providers.contains("google"));
    }

    @Test
    @DisplayName("Should return empty list when no providers registered")
    void testGetProvidersEmpty() {
        List<String> providers = oauthService.getProviders();
        assertTrue(providers.isEmpty());
    }

    @Test
    @DisplayName("Should return list of registered provider names")
    void testGetProvidersMultiple() {
        OAuthConfig googleConfig = new OAuthConfig("g-id", "g-secret", Arrays.asList("scope"));
        OAuthConfig githubConfig = new OAuthConfig("gh-id", "gh-secret", Arrays.asList("scope"));
        
        oauthService.registerProvider(new GoogleOAuthProvider(googleConfig));
        oauthService.registerProvider(new GitHubOAuthProvider(githubConfig));
        
        List<String> providers = oauthService.getProviders();
        assertEquals(2, providers.size());
        assertTrue(providers.contains("google"));
        assertTrue(providers.contains("github"));
    }

    @Test
    @DisplayName("Should throw exception when getting auth URL for unregistered provider")
    void testGetAuthorizationUrlUnregisteredProvider() {
        OAuthException exception = assertThrows(OAuthException.class, () -> {
            oauthService.getAuthorizationUrl("unknown", "http://localhost/callback");
        });
        
        assertTrue(exception.getMessage().contains("not registered"));
    }

    @Test
    @DisplayName("Should generate authorization URL for registered provider")
    void testGetAuthorizationUrl() throws OAuthException {
        OAuthConfig config = new OAuthConfig("id", "secret", Arrays.asList("scope"));
        GoogleOAuthProvider provider = new GoogleOAuthProvider(config);
        oauthService.registerProvider(provider);
        
        OAuthAuthorizationUrl authUrl = oauthService.getAuthorizationUrl("google", "http://localhost/callback");
        
        assertNotNull(authUrl);
        assertNotNull(authUrl.url());
        assertNotNull(authUrl.state());
        assertFalse(authUrl.state().isEmpty());
        // URL should contain Google OAuth endpoint
        assertTrue(authUrl.url().contains("accounts.google.com") || authUrl.url().contains("oauth"));
    }

    @Test
    @DisplayName("Should generate unique state tokens")
    void testStateGeneration() throws OAuthException {
        OAuthConfig config = new OAuthConfig("id", "secret", Arrays.asList("scope"));
        GoogleOAuthProvider provider = new GoogleOAuthProvider(config);
        oauthService.registerProvider(provider);
        
        OAuthAuthorizationUrl url1 = oauthService.getAuthorizationUrl("google", "http://localhost/callback");
        OAuthAuthorizationUrl url2 = oauthService.getAuthorizationUrl("google", "http://localhost/callback");
        
        // States should be different (unique)
        assertNotEquals(url1.state(), url2.state());
    }
}

