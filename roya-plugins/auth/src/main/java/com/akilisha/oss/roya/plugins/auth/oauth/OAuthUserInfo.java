package com.akilisha.oss.roya.plugins.auth.oauth;

import java.util.Map;

/**
 * User information from OAuth provider.
 */
public record OAuthUserInfo(
    String providerId,
    String email,
    String name,
    String picture,
    Map<String, Object> metadata
) {
    public OAuthUserInfo(String providerId, String email, String name, String picture, Map<String, Object> metadata) {
        this.providerId = providerId;
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.metadata = metadata != null ? metadata : Map.of();
    }
}

