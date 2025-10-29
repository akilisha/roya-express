package com.akilisha.oss.roya.plugins.auth.oauth;

import com.akilisha.oss.roya.plugins.auth.AuthResult;

import java.util.List;

/**
 * OAuth service - handles OAuth provider registration and callbacks.
 */
public interface OAuth {

    /**
     * Register an OAuth provider.
     *
     * @param provider OAuth provider implementation
     */
    void registerProvider(OAuthProvider provider);

    /**
     * Get list of registered provider names.
     *
     * @return List of provider names (e.g., ["google", "github"])
     */
    List<String> getProviders();

    /**
     * Get authorization URL for a provider.
     *
     * @param provider Provider name
     * @param redirectUri Callback URL
     * @return Authorization URL with state parameter
     * @throws OAuthException if provider not registered
     */
    OAuthAuthorizationUrl getAuthorizationUrl(String provider, String redirectUri) throws OAuthException;

    /**
     * Handle OAuth callback and create/update user.
     *
     * @param provider Provider name
     * @param code Authorization code
     * @param state State token (for CSRF protection)
     * @param redirectUri Original redirect URI
     * @return Authentication result with JWT token
     * @throws OAuthException if callback fails
     */
    AuthResult handleCallback(String provider, String code, String state, String redirectUri) throws OAuthException;
}

