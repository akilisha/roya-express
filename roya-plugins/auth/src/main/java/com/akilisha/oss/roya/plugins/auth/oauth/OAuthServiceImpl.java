package com.akilisha.oss.roya.plugins.auth.oauth;

import com.akilisha.oss.roya.plugins.auth.*;
import com.akilisha.oss.roya.plugins.auth.AuthServiceImpl;
import com.akilisha.oss.roya.plugins.database.Database;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.jooq.impl.DSL.*;

/**
 * OAuth service implementation.
 */
public class OAuthServiceImpl implements OAuth {

    private final Map<String, OAuthProvider> providers = new ConcurrentHashMap<>();
    private final Auth auth;
    private final Database database;
    private final Map<String, String> stateStore = new ConcurrentHashMap<>(); // In production, use Redis or similar
    private final SecureRandom random = new SecureRandom();

    public OAuthServiceImpl(Auth auth, Database database) {
        this.auth = auth;
        this.database = database;
    }

    @Override
    public void registerProvider(OAuthProvider provider) {
        providers.put(provider.getName(), provider);
    }

    @Override
    public List<String> getProviders() {
        return new ArrayList<>(providers.keySet());
    }

    @Override
    public OAuthAuthorizationUrl getAuthorizationUrl(String provider, String redirectUri) throws OAuthException {
        OAuthProvider oauthProvider = providers.get(provider);
        if (oauthProvider == null) {
            throw new OAuthException("OAuth provider not registered: " + provider);
        }

        // Generate state for CSRF protection
        String state = generateState();
        stateStore.put(state, redirectUri);

        String authUrl = oauthProvider.getAuthorizationUrl(state, redirectUri);
        return new OAuthAuthorizationUrl(authUrl, state);
    }

    @Override
    public AuthResult handleCallback(String provider, String code, String state, String redirectUri) throws OAuthException {
        OAuthProvider oauthProvider = providers.get(provider);
        if (oauthProvider == null) {
            throw new OAuthException("OAuth provider not registered: " + provider);
        }

        // Verify state (CSRF protection)
        String storedRedirectUri = stateStore.remove(state);
        if (storedRedirectUri == null || !storedRedirectUri.equals(redirectUri)) {
            throw new OAuthException("Invalid or expired state token");
        }

        // Exchange code for token
        OAuthToken token = oauthProvider.exchangeCode(code, redirectUri);

        // Get user info from provider
        OAuthUserInfo userInfo = oauthProvider.getUserInfo(token);

        // Find or create user
        User user = findOrCreateOAuthUser(provider, userInfo, token);

        // Generate JWT token
        String jwtToken = generateJWTToken(user);
        String refreshToken = generateRefreshToken();

        // Store refresh token
        storeRefreshToken(user.id(), refreshToken);

        return new AuthResult(jwtToken, refreshToken, user);
    }

    private User findOrCreateOAuthUser(String provider, OAuthUserInfo userInfo, OAuthToken token) throws OAuthException {
        DSLContext dsl = database.dsl();

        // Check if user exists with this provider + provider_id
        Result<Record> existing = dsl.selectFrom(table("auth_users"))
            .where(field("provider").eq(provider))
            .and(field("provider_id").eq(userInfo.providerId()))
            .fetch();

        if (!existing.isEmpty()) {
            // Update user info
            Record userRecord = existing.get(0);
            String userId = userRecord.get("id", java.util.UUID.class).toString();

            Map<String, Object> providerMetadata = new HashMap<>();
            providerMetadata.put("accessToken", token.accessToken());
            if (token.refreshToken() != null) {
                providerMetadata.put("refreshToken", token.refreshToken());
            }
            providerMetadata.putAll(userInfo.metadata());

            dsl.update(table("auth_users"))
                .set(field("email"), userInfo.email())
                .set(field("provider_metadata"), toJson(providerMetadata))
                .set(field("updated_at"), currentTimestamp())
                .where(field("id").eq(java.util.UUID.fromString(userId)))
                .execute();

            return auth.getUser(userId).orElseThrow(() -> new OAuthException("Failed to update OAuth user"));
        }

        // Check if email already exists (link OAuth account to existing email account)
        if (userInfo.email() != null && !userInfo.email().isEmpty()) {
            Result<Record> emailUsers = dsl.selectFrom(table("auth_users"))
                .where(field("email").eq(userInfo.email()))
                .and(field("provider").eq("email"))
                .fetch();

            if (!emailUsers.isEmpty()) {
                // Link OAuth to existing email account (update provider)
                Record userRecord = emailUsers.get(0);
                String userId = userRecord.get("id", java.util.UUID.class).toString();

                Map<String, Object> providerMetadata = new HashMap<>();
                providerMetadata.put("accessToken", token.accessToken());
                if (token.refreshToken() != null) {
                    providerMetadata.put("refreshToken", token.refreshToken());
                }
                providerMetadata.putAll(userInfo.metadata());

                dsl.update(table("auth_users"))
                    .set(field("provider"), provider)
                    .set(field("provider_id"), userInfo.providerId())
                    .set(field("provider_metadata"), toJson(providerMetadata))
                    .set(field("updated_at"), currentTimestamp())
                    .where(field("id").eq(java.util.UUID.fromString(userId)))
                    .execute();

                return auth.getUser(userId).orElseThrow(() -> new OAuthException("Failed to link OAuth account"));
            }
        }

        // Create new user
        java.util.UUID userId = java.util.UUID.randomUUID();

        Map<String, Object> userData = new HashMap<>();
        if (userInfo.name() != null) {
            userData.put("name", userInfo.name());
        }
        if (userInfo.picture() != null) {
            userData.put("picture", userInfo.picture());
        }

        Map<String, Object> providerMetadata = new HashMap<>();
        providerMetadata.put("accessToken", token.accessToken());
        if (token.refreshToken() != null) {
            providerMetadata.put("refreshToken", token.refreshToken());
        }
        providerMetadata.putAll(userInfo.metadata());

        dsl.insertInto(table("auth_users"))
            .set(field("id"), userId)
            .set(field("email"), userInfo.email() != null ? userInfo.email() : provider + ":" + userInfo.providerId())
            .set(field("password_hash"), (String) null) // OAuth users don't have passwords
            .set(field("provider"), provider)
            .set(field("provider_id"), userInfo.providerId())
            .set(field("user_data"), toJson(userData))
            .set(field("provider_metadata"), toJson(providerMetadata))
            .execute();

        return auth.getUser(userId.toString())
            .orElseThrow(() -> new OAuthException("Failed to create OAuth user"));
    }

    private String generateState() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateJWTToken(User user) {
        // Access AuthServiceImpl's generateToken method (package-private)
        // Since OAuthServiceImpl is in same package, we can access it
        return ((AuthServiceImpl) auth).generateToken(user);
    }

    private String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    private void storeRefreshToken(String userId, String refreshToken) {
        DSLContext dsl = database.dsl();
        String tokenHash = hashToken(refreshToken);
        java.time.Instant expiresAt = java.time.Instant.now().plus(7, java.time.temporal.ChronoUnit.DAYS);

        dsl.insertInto(table("auth_refresh_tokens"))
            .set(field("user_id"), java.util.UUID.fromString(userId))
            .set(field("token_hash"), tokenHash)
            .set(field("expires_at"), java.sql.Timestamp.from(expiresAt))
            .execute();
    }

    private String hashToken(String token) {
        return Base64.getEncoder().encodeToString(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String toJson(Map<String, Object> data) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{}";
        }
    }
}

