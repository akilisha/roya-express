package com.akilisha.oss.roya.plugins.auth.oauth;

/**
 * OAuth provider interface - abstracts OAuth 2.0 flow.
 *
 * Each provider (Google, GitHub, etc.) implements this interface.
 */
public interface OAuthProvider {

    /**
     * Provider name (e.g., "google", "github").
     */
    String getName();

    /**
     * Get authorization URL for redirecting user to provider.
     *
     * @param state CSRF state token
     * @param redirectUri Callback URL
     * @return Authorization URL
     */
    String getAuthorizationUrl(String state, String redirectUri);

    /**
     * Exchange authorization code for access token.
     *
     * @param code Authorization code from callback
     * @param redirectUri Original redirect URI (must match)
     * @return OAuth token (access_token, refresh_token, etc.)
     */
    OAuthToken exchangeCode(String code, String redirectUri) throws OAuthException;

    /**
     * Fetch user profile information from provider.
     *
     * @param token OAuth access token
     * @return User information from provider
     */
    OAuthUserInfo getUserInfo(OAuthToken token) throws OAuthException;
}

