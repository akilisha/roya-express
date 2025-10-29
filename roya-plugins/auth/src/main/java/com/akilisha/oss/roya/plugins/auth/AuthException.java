package com.akilisha.oss.roya.plugins.auth;

/**
 * Authentication-related exception.
 *
 * Thrown for various auth failures (invalid credentials, expired tokens, etc.)
 */
public class AuthException extends RuntimeException {
    
    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}

