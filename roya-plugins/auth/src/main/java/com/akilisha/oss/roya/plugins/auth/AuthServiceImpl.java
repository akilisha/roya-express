package com.akilisha.oss.roya.plugins.auth;

import com.akilisha.oss.roya.plugins.database.Database;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.mindrot.jbcrypt.BCrypt;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.jooq.impl.DSL.*;

/**
 * Auth service implementation - Postgres-backed authentication.
 *
 * GoTrue-inspired implementation using Database plugin for persistence.
 * Supports both JWT tokens and session-based authentication.
 */
public class AuthServiceImpl implements Auth {

    private final Database database;
    private final SecretKey jwtSecret;
    private final ObjectMapper objectMapper;
    private final long accessTokenExpirySeconds;
    private final long refreshTokenExpirySeconds;
    private com.akilisha.oss.roya.plugins.auth.oauth.OAuth oauthService;

    public AuthServiceImpl(Database database, String jwtSecret) {
        this(database, jwtSecret, 3600, 604800); // 1 hour access, 7 days refresh
    }

    public AuthServiceImpl(Database database, String jwtSecret, 
                          long accessTokenExpirySeconds, long refreshTokenExpirySeconds) {
        this.database = database;
        // Use HMAC-SHA for JWT signing
        this.jwtSecret = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.objectMapper = new ObjectMapper();
        this.accessTokenExpirySeconds = accessTokenExpirySeconds;
        this.refreshTokenExpirySeconds = refreshTokenExpirySeconds;
    }

    @Override
    public User register(String email, String password) {
        return register(email, password, Map.of());
    }

    @Override
    public User register(String email, String password, Map<String, Object> userData) {
        // Check if email already exists with email provider
        DSLContext dsl = database.dsl();
        
        Result<Record> existing = dsl.selectFrom(table("auth_users"))
            .where(field("email").eq(email))
            .and(field("provider").eq("email"))
            .fetch();
        
        if (!existing.isEmpty()) {
            throw new AuthException("User with email " + email + " already exists");
        }

        // Hash password
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());

        // Generate UUID for user
        UUID userId = UUID.randomUUID();
        
        // Serialize user_data to JSON
        String userDataJson = toJson(userData);

        // Insert user (email provider, user_data as JSONB)
        dsl.insertInto(table("auth_users"))
            .set(field("id"), userId)
            .set(field("email"), email)
            .set(field("password_hash"), passwordHash)
            .set(field("provider"), "email")
            .set(field("user_data"), userDataJson)
            .execute();

        // Fetch created user
        return getUserById(userId.toString())
            .orElseThrow(() -> new AuthException("Failed to create user"));
    }

    @Override
    public AuthResult login(String email, String password) {
        DSLContext dsl = database.dsl();
        
        // Find user by email
        Result<Record> users = dsl.selectFrom(table("auth_users"))
            .where(field("email").eq(email))
            .fetch();
        
        if (users.isEmpty()) {
            throw new AuthException("Invalid email or password");
        }

        Record userRecord = users.get(0);
        String passwordHash = userRecord.get("password_hash", String.class);
        
        // Verify password
        if (!BCrypt.checkpw(password, passwordHash)) {
            throw new AuthException("Invalid email or password");
        }

        // Get user
        String userId = userRecord.get("id", UUID.class).toString();
        User user = getUserById(userId)
            .orElseThrow(() -> new AuthException("User not found"));

        // Generate tokens
        String token = generateToken(user);
        String refreshToken = generateRefreshToken(user);

        // Store refresh token
        storeRefreshToken(userId, refreshToken);

        return new AuthResult(token, refreshToken, user);
    }

    @Override
    public AuthResult loginWithToken(String token) {
        Optional<User> user = verifyToken(token);
        if (user.isEmpty()) {
            throw new AuthException("Invalid or expired token");
        }

        // Generate new tokens
        String newToken = generateToken(user.get());
        String refreshToken = generateRefreshToken(user.get());
        storeRefreshToken(user.get().id(), refreshToken);

        return new AuthResult(newToken, refreshToken, user.get());
    }

    @Override
    public Optional<User> verifyToken(String token) {
        try {
            // Parse JWT token
            Claims claims = Jwts.parser()
                .verifyWith(jwtSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String userId = claims.getSubject();
            return getUserById(userId);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public AuthResult refreshToken(String refreshToken) {
        DSLContext dsl = database.dsl();
        
        String tokenHash = hashToken(refreshToken);
        
        // Find refresh token
        Result<Record> tokens = dsl.selectFrom(table("auth_refresh_tokens"))
            .where(field("token_hash").eq(tokenHash))
            .and(field("expires_at").greaterThan(currentTimestamp()))
            .fetch();
        
        if (tokens.isEmpty()) {
            throw new AuthException("Invalid or expired refresh token");
        }

        Record tokenRecord = tokens.get(0);
        String userId = tokenRecord.get("user_id", UUID.class).toString();
        
        // Delete old refresh token
        dsl.deleteFrom(table("auth_refresh_tokens"))
            .where(field("token_hash").eq(tokenHash))
            .execute();

        // Get user
        User user = getUserById(userId)
            .orElseThrow(() -> new AuthException("User not found"));

        // Generate new tokens
        String newToken = generateToken(user);
        String newRefreshToken = generateRefreshToken(user);
        storeRefreshToken(userId, newRefreshToken);

        return new AuthResult(newToken, newRefreshToken, user);
    }

    @Override
    public void logout(String token) {
        // Delete refresh token if found
        String tokenHash = hashToken(token);
        DSLContext dsl = database.dsl();
        dsl.deleteFrom(table("auth_refresh_tokens"))
            .where(field("token_hash").eq(tokenHash))
            .execute();
        
        // TODO: For session-based logout, also delete from auth_sessions
    }

    @Override
    public Optional<User> getUser(String userId) {
        return getUserById(userId);
    }

    @Override
    public User updateUser(String userId, Map<String, Object> updates) {
        DSLContext dsl = database.dsl();
        
        // Get existing user
        User existing = getUserById(userId)
            .orElseThrow(() -> new AuthException("User not found"));

        // Merge user_data
        Map<String, Object> newUserData = new HashMap<>(existing.userData());
        newUserData.putAll(updates);

        // Update user (user_data as JSONB)
        dsl.update(table("auth_users"))
            .set(field("user_data"), toJson(newUserData))
            .set(field("updated_at"), currentTimestamp())
            .where(field("id").eq(UUID.fromString(userId)))
            .execute();

        return getUserById(userId)
            .orElseThrow(() -> new AuthException("Failed to update user"));
    }

    @Override
    public void deleteUser(String userId) {
        DSLContext dsl = database.dsl();
        int deleted = dsl.deleteFrom(table("auth_users"))
            .where(field("id").eq(UUID.fromString(userId)))
            .execute();
        
        if (deleted == 0) {
            throw new AuthException("User not found");
        }
    }

    @Override
    public void changePassword(String userId, String oldPassword, String newPassword) {
        DSLContext dsl = database.dsl();
        
        // Get user and verify old password
        Result<Record> users = dsl.selectFrom(table("auth_users"))
            .where(field("id").eq(UUID.fromString(userId)))
            .fetch();
        
        if (users.isEmpty()) {
            throw new AuthException("User not found");
        }

        Record userRecord = users.get(0);
        String passwordHash = userRecord.get("password_hash", String.class);
        
        if (!BCrypt.checkpw(oldPassword, passwordHash)) {
            throw new AuthException("Incorrect old password");
        }

        // Update password
        String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        dsl.update(table("auth_users"))
            .set(field("password_hash"), newPasswordHash)
            .set(field("updated_at"), currentTimestamp())
            .where(field("id").eq(UUID.fromString(userId)))
            .execute();

        // Invalidate all refresh tokens for security
        dsl.deleteFrom(table("auth_refresh_tokens"))
            .where(field("user_id").eq(UUID.fromString(userId)))
            .execute();
    }

    @Override
    public String requestPasswordReset(String email) {
        DSLContext dsl = database.dsl();
        
        // Find user
        Result<Record> users = dsl.selectFrom(table("auth_users"))
            .where(field("email").eq(email))
            .fetch();
        
        if (users.isEmpty()) {
            // Don't reveal if user exists (security best practice)
            return UUID.randomUUID().toString(); // Return dummy token
        }

        Record userRecord = users.get(0);
        String userId = userRecord.get("id", UUID.class).toString();

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(resetToken);
        
        // Store reset token (expires in 1 hour)
        Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS);
        
        dsl.insertInto(table("auth_password_resets"))
            .set(field("user_id"), UUID.fromString(userId))
            .set(field("token_hash"), tokenHash)
            .set(field("expires_at"), Timestamp.from(expiresAt))
            .execute();

        // In production, send email with resetToken
        // For now, return token (should be sent via email)
        return resetToken;
    }

    @Override
    public void resetPassword(String resetToken, String newPassword) {
        DSLContext dsl = database.dsl();
        
        String tokenHash = hashToken(resetToken);
        
        // Find valid reset token
        Result<Record> resets = dsl.selectFrom(table("auth_password_resets"))
            .where(field("token_hash").eq(tokenHash))
            .and(field("expires_at").greaterThan(currentTimestamp()))
            .and(field("used").eq(false))
            .fetch();
        
        if (resets.isEmpty()) {
            throw new AuthException("Invalid or expired reset token");
        }

        Record resetRecord = resets.get(0);
        String userId = resetRecord.get("user_id", UUID.class).toString();
        boolean used = resetRecord.get("used", Boolean.class);
        
        if (used) {
            throw new AuthException("Reset token already used");
        }

        // Update password
        String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        dsl.update(table("auth_users"))
            .set(field("password_hash"), newPasswordHash)
            .set(field("updated_at"), currentTimestamp())
            .where(field("id").eq(UUID.fromString(userId)))
            .execute();

        // Mark token as used
        dsl.update(table("auth_password_resets"))
            .set(field("used"), true)
            .where(field("token_hash").eq(tokenHash))
            .execute();

        // Invalidate all refresh tokens
        dsl.deleteFrom(table("auth_refresh_tokens"))
            .where(field("user_id").eq(UUID.fromString(userId)))
            .execute();
    }

    @Override
    public com.akilisha.oss.roya.api.Handler required() {
        return (req, res, next) -> {
            // Try to get token from Authorization header
            String authHeader = req.headers().get("Authorization").orElse("");
            String token = null;
            
            if (authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }

            // If no token in header, try session
            if (token == null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> session = (Map<String, Object>) req.get("session");
                if (session != null) {
                    Object userId = session.get("userId");
                    if (userId != null) {
                        // Session-based auth - get user from session
                        Optional<User> user = getUser(userId.toString());
                        if (user.isPresent()) {
                            req.set("user", user.get());
                            next.handle(req, res);
                            return;
                        }
                    }
                }
            }

            // Try JWT token
            if (token != null) {
                Optional<User> user = verifyToken(token);
                if (user.isPresent()) {
                    req.set("user", user.get());
                    next.handle(req, res);
                    return;
                }
            }

            // Not authenticated
            res.status(401).json(Map.of("error", "Unauthorized"));
        };
    }

    @Override
    public com.akilisha.oss.roya.api.Handler optional() {
        return (req, res, next) -> {
            // Similar to required(), but doesn't fail if not authenticated
            String authHeader = req.headers().get("Authorization").orElse("");
            String token = null;
            
            if (authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }

            // Try session first
            if (token == null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> session = (Map<String, Object>) req.get("session");
                if (session != null) {
                    Object userId = session.get("userId");
                    if (userId != null) {
                        Optional<User> user = getUser(userId.toString());
                        user.ifPresent(u -> req.set("user", u));
                    }
                }
            }

            // Try JWT token
            if (token != null) {
                verifyToken(token).ifPresent(user -> req.set("user", user));
            }

            next.handle(req, res);
        };
    }

    @Override
    public com.akilisha.oss.roya.plugins.auth.oauth.OAuth oauth() {
        if (oauthService == null) {
            oauthService = new com.akilisha.oss.roya.plugins.auth.oauth.OAuthServiceImpl(this, database);
        }
        return oauthService;
    }

    // Helper methods

    private Optional<User> getUserById(String userId) {
        DSLContext dsl = database.dsl();
        
        Result<Record> users = dsl.selectFrom(table("auth_users"))
            .where(field("id").eq(UUID.fromString(userId)))
            .fetch();
        
        if (users.isEmpty()) {
            return Optional.empty();
        }

        Record userRecord = users.get(0);
        return Optional.of(recordToUser(userRecord));
    }

    private User recordToUser(Record record) {
        String id = record.get("id", UUID.class).toString();
        String email = record.get("email", String.class);
        Instant createdAt = record.get("created_at", java.sql.Timestamp.class).toInstant();
        Instant updatedAt = record.get("updated_at", java.sql.Timestamp.class).toInstant();
        
        // Parse user_data JSONB
        Object userDataObj = record.get("user_data");
        Map<String, Object> userData = Map.of();
        if (userDataObj != null) {
            try {
                if (userDataObj instanceof String) {
                    userData = objectMapper.readValue((String) userDataObj, Map.class);
                } else if (userDataObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) userDataObj;
                    userData = map;
                }
            } catch (Exception e) {
                // Fallback to empty map
            }
        }

        // Include provider metadata in userData if present
        Object providerMetadataObj = record.get("provider_metadata");
        if (providerMetadataObj != null) {
            try {
                Map<String, Object> providerMetadata;
                if (providerMetadataObj instanceof String) {
                    providerMetadata = objectMapper.readValue((String) providerMetadataObj, Map.class);
                } else if (providerMetadataObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) providerMetadataObj;
                    providerMetadata = map;
                } else {
                    providerMetadata = Map.of();
                }
                
                // Merge provider metadata into userData
                Map<String, Object> merged = new HashMap<>(userData);
                merged.put("_provider", record.get("provider", String.class));
                merged.put("_providerId", record.get("provider_id", String.class));
                merged.put("_providerMetadata", providerMetadata);
                userData = merged;
            } catch (Exception e) {
                // Ignore provider metadata parse errors
            }
        }

        return new User(id, email, createdAt, updatedAt, userData);
    }

    // Public for OAuth service access
    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(accessTokenExpirySeconds, ChronoUnit.SECONDS);

        return Jwts.builder()
            .subject(user.id())
            .claim("email", user.email())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(jwtSecret)
            .compact();
    }

    private String generateRefreshToken(User user) {
        return UUID.randomUUID().toString();
    }

    private void storeRefreshToken(String userId, String refreshToken) {
        DSLContext dsl = database.dsl();
        String tokenHash = hashToken(refreshToken);
        Instant expiresAt = Instant.now().plus(refreshTokenExpirySeconds, ChronoUnit.SECONDS);
        
        dsl.insertInto(table("auth_refresh_tokens"))
            .set(field("user_id"), UUID.fromString(userId))
            .set(field("token_hash"), tokenHash)
            .set(field("expires_at"), Timestamp.from(expiresAt))
            .execute();
    }

    private String hashToken(String token) {
        // Simple hash for token storage (in production, use proper hashing)
        return Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{}";
        }
    }
}

