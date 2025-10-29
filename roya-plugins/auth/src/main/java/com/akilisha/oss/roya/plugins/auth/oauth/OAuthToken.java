package com.akilisha.oss.roya.plugins.auth.oauth;

import java.time.Instant;
import java.util.Map;

/**
 * OAuth token information.
 */
public record OAuthToken(
    String accessToken,
    String refreshToken,
    Instant expiresAt,
    String tokenType,
    Map<String, Object> metadata
) {
    public OAuthToken(String accessToken, String refreshToken, Instant expiresAt, String tokenType, Map<String, Object> metadata) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt != null ? expiresAt : Instant.now().plusSeconds(3600);
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.metadata = metadata != null ? metadata : Map.of();
    }

    public OAuthToken(String accessToken) {
        this(accessToken, null, null, null, null);
    }
}

