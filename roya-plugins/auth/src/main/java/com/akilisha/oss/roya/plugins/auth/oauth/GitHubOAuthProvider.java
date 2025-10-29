package com.akilisha.oss.roya.plugins.auth.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * GitHub OAuth 2.0 provider implementation.
 */
public class GitHubOAuthProvider implements OAuthProvider {

    private final OAuthConfig config;
    private final OAuth20Service oauthService;
    private final ObjectMapper objectMapper;

    public GitHubOAuthProvider(OAuthConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        
        // Build OAuth service with GitHub API
        this.oauthService = new ServiceBuilder(config.clientId())
            .apiSecret(config.clientSecret())
            .defaultScope(String.join(" ", config.scopes()))
            .build(GitHubApi.instance());
    }

    @Override
    public String getName() {
        return "github";
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
            // GitHub user endpoint
            OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.github.com/user");
            oauthService.signRequest(new OAuth2AccessToken(token.accessToken()), request);
            
            Response response = oauthService.execute(request);
            if (!response.isSuccessful()) {
                throw new OAuthException("Failed to fetch user info: " + response.getCode() + " " + response.getBody());
            }

            JsonNode userData = objectMapper.readTree(response.getBody());
            
            // GitHub returns id as integer, convert to string
            String id = String.valueOf(userData.get("id").asLong());
            String email = userData.has("email") ? userData.get("email").asText() : null;
            
            // Try to get email from private endpoint if not in public profile
            if (email == null || email.isEmpty()) {
                try {
                    OAuthRequest emailRequest = new OAuthRequest(Verb.GET, "https://api.github.com/user/emails");
                    oauthService.signRequest(new OAuth2AccessToken(token.accessToken()), emailRequest);
                    Response emailResponse = oauthService.execute(emailRequest);
                    if (emailResponse.isSuccessful()) {
                        JsonNode emails = objectMapper.readTree(emailResponse.getBody());
                        if (emails.isArray() && emails.size() > 0) {
                            JsonNode primaryEmail = emails.get(0);
                            email = primaryEmail.get("email").asText();
                        }
                    }
                } catch (Exception e) {
                    // Fallback: use login as identifier
                    email = userData.has("login") ? userData.get("login").asText() + "@github.local" : null;
                }
            }
            
            String name = userData.has("name") ? userData.get("name").asText() : 
                         userData.has("login") ? userData.get("login").asText() : null;
            String picture = userData.has("avatar_url") ? userData.get("avatar_url").asText() : null;

            // Extract all metadata
            Map<String, Object> metadata = new HashMap<>();
            userData.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                if (!List.of("id", "email", "name", "login", "avatar_url").contains(key)) {
                    JsonNode value = entry.getValue();
                    if (value.isTextual()) {
                        metadata.put(key, value.asText());
                    } else if (value.isNumber()) {
                        metadata.put(key, value.asLong());
                    } else if (value.isBoolean()) {
                        metadata.put(key, value.asBoolean());
                    }
                }
            });

            return new OAuthUserInfo(id, email, name, picture, metadata);
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new OAuthException("Failed to fetch user info from GitHub", e);
        }
    }
}

