package com.akilisha.oss.roya.plugins.auth;

import java.util.Map;
import java.util.Optional;

/**
 * Authentication service - GoTrue-inspired Postgres-backed auth.
 *
 * Provides email/password authentication, JWT tokens, sessions, and user management.
 * Supports both JWT token-based and session-based authentication.
 *
 * Example usage:
 * <pre>
 * Auth auth = req.get(Auth.class);
 * 
 * // Register user
 * User user = auth.register("alice@example.com", "password123");
 * 
 * // Login
 * AuthResult result = auth.login("alice@example.com", "password123");
 * String token = result.token();
 * 
 * // Verify token
 * Optional&lt;User&gt; user = auth.verifyToken(token);
 * 
 * // Protected route
 * app.get("/profile", auth.required(), (req, res) -&gt; {
 *     User user = (User) req.get("user");
 *     res.json(user);
 * });
 * </pre>
 */
public interface Auth {

    /**
     * Register a new user with email and password.
     *
     * @param email User email
     * @param password Plain text password (will be hashed)
     * @return Created user
     * @throws AuthException if email already exists
     */
    User register(String email, String password);

    /**
     * Register a new user with email, password, and additional user data.
     *
     * @param email User email
     * @param password Plain text password
     * @param userData Additional metadata (stored as JSONB)
     * @return Created user
     * @throws AuthException if email already exists
     */
    User register(String email, String password, Map<String, Object> userData);

    /**
     * Authenticate user with email and password.
     * Returns JWT token and refresh token.
     *
     * @param email User email
     * @param password Plain text password
     * @return Auth result with tokens
     * @throws AuthException if credentials invalid
     */
    AuthResult login(String email, String password);

    /**
     * Authenticate using an existing JWT token.
     * Useful for token refresh flows.
     *
     * @param token JWT token
     * @return Auth result with new tokens
     * @throws AuthException if token invalid or expired
     */
    AuthResult loginWithToken(String token);

    /**
     * Verify a JWT token and return the associated user.
     *
     * @param token JWT token
     * @return User if token valid, empty if invalid/expired
     */
    Optional<User> verifyToken(String token);

    /**
     * Refresh an access token using a refresh token.
     *
     * @param refreshToken Refresh token
     * @return New auth result with tokens
     * @throws AuthException if refresh token invalid
     */
    AuthResult refreshToken(String refreshToken);

    /**
     * Logout user by invalidating tokens/session.
     *
     * @param token JWT token or session identifier
     */
    void logout(String token);

    /**
     * Get user by ID.
     *
     * @param userId User ID
     * @return User if found
     */
    Optional<User> getUser(String userId);

    /**
     * Update user data.
     *
     * @param userId User ID
     * @param updates Map of fields to update
     * @return Updated user
     * @throws AuthException if user not found
     */
    User updateUser(String userId, Map<String, Object> updates);

    /**
     * Delete user account.
     *
     * @param userId User ID
     */
    void deleteUser(String userId);

    /**
     * Change user password.
     *
     * @param userId User ID
     * @param oldPassword Current password
     * @param newPassword New password
     * @throws AuthException if old password incorrect
     */
    void changePassword(String userId, String oldPassword, String newPassword);

    /**
     * Request password reset (sends reset token).
     *
     * @param email User email
     * @return Reset token (in production, send via email)
     */
    String requestPasswordReset(String email);

    /**
     * Reset password using reset token.
     *
     * @param resetToken Reset token from requestPasswordReset()
     * @param newPassword New password
     * @throws AuthException if reset token invalid or expired
     */
    void resetPassword(String resetToken, String newPassword);

    /**
     * Get middleware for protecting routes (requires authentication).
     *
     * Sets req.get("user") with authenticated User if valid token/session found.
     * Returns 401 if not authenticated.
     *
     * @return Handler for route protection
     */
    com.akilisha.oss.roya.api.Handler required();

    /**
     * Get optional middleware (sets user if authenticated, doesn't fail if not).
     *
     * Useful for routes that work both authenticated and unauthenticated.
     *
     * @return Handler for optional authentication
     */
    com.akilisha.oss.roya.api.Handler optional();

    /**
     * Get OAuth service for OAuth provider authentication.
     *
     * @return OAuth service instance
     */
    com.akilisha.oss.roya.plugins.auth.oauth.OAuth oauth();
}

