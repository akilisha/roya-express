package com.akilisha.oss.roya.plugins.auth.oauth;

import java.util.List;

/**
 * OAuth provider configuration.
 */
public record OAuthConfig(
    String clientId,
    String clientSecret,
    List<String> scopes,
    String authorizeUrl,
    String tokenUrl,
    String userInfoUrl
) {
    public OAuthConfig(String clientId, String clientSecret, List<String> scopes) {
        this(clientId, clientSecret, scopes, null, null, null);
    }
}

