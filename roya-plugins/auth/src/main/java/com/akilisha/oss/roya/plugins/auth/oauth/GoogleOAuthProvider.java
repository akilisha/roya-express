package com.akilisha.oss.roya.plugins.auth.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Google OAuth 2.0 provider implementation.
 */
public class GoogleOAuthProvider implements OAuthProvider {

    private final OAuthConfig config;
    private final OAuth20Service oauthService;
    private final ObjectMapper objectMapper;

    public GoogleOAuthProvider(OAuthConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        
        // Build OAuth service with Google API
        this.oauthService = new ServiceBuilder(config.clientId())
            .apiSecret(config.clientSecret())
            .defaultScope(String.join(" ", config.scopes()))
            .build(GoogleApi20.instance());
    }

    @Override
    public String getName() {
        return "google";
    }

    @Override
    public String getAuthorizationUrl(String state, String redirectUri) {
        return oauthService.getAuthorizationUrl(state);
    }

    @Override
    public OAuthToken exchangeCode(String code, String redirectUri) throws OAuthException {
        try {
            OAuth2AccessToken accessToken = oauthService.getAccessToken(code);
            
            // Extract expiry if available
            Instant expiresAt = null;
            if (accessToken.getExpiresIn() != null) {
                expiresAt = Instant.now().plusSeconds(accessToken.getExpiresIn());
            }

            Map<String, Object> metadata = new HashMap<>();
            if (accessToken.getRefreshToken() != null) {
                metadata.put("refreshToken", accessToken.getRefreshToken());
            }
            metadata.put("scope", accessToken.getScope());
            metadata.put("tokenType", accessToken.getTokenType());

            return new OAuthToken(
                accessToken.getAccessToken(),
                accessToken.getRefreshToken(),
                expiresAt,
                accessToken.getTokenType(),
                metadata
            );
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new OAuthException("Failed to exchange authorization code", e);
        }
    }

    @Override
    public OAuthUserInfo getUserInfo(OAuthToken token) throws OAuthException {
        try {
            OAuthRequest request = new OAuthRequest(Verb.GET, "https://www.googleapis.com/oauth2/v2/userinfo");
            oauthService.signRequest(new OAuth2AccessToken(token.accessToken()), request);
            
            Response response = oauthService.execute(request);
            if (!response.isSuccessful()) {
                throw new OAuthException("Failed to fetch user info: " + response.getCode() + " " + response.getBody());
            }

            JsonNode userData = objectMapper.readTree(response.getBody());
            
            String id = userData.get("id").asText();
            String email = userData.has("email") ? userData.get("email").asText() : null;
            String name = userData.has("name") ? userData.get("name").asText() : null;
            String picture = userData.has("picture") ? userData.get("picture").asText() : null;

            // Extract all metadata
            Map<String, Object> metadata = new HashMap<>();
            userData.fields().forEachRemaining(entry -> {
                if (!java.util.List.of("id", "email", "name", "picture").contains(entry.getKey())) {
                    metadata.put(entry.getKey(), entry.getValue().asText());
                }
            });

            return new OAuthUserInfo(id, email, name, picture, metadata);
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new OAuthException("Failed to fetch user info from Google", e);
        }
    }
}

