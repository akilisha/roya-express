package com.akilisha.oss.roya.plugins.auth;

/**
 * Authentication result - contains tokens after successful login.
 *
 * Provides both access token (short-lived) and refresh token (long-lived).
 */
public record AuthResult(
    String token,
    String refreshToken,
    User user
) {
}

