package com.akilisha.oss.roya.plugins.auth.oauth;

/**
 * OAuth-related exception.
 */
public class OAuthException extends Exception {
    
    public OAuthException(String message) {
        super(message);
    }

    public OAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}

