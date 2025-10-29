package com.akilisha.oss.roya.plugins.auth.oauth;

/**
 * OAuth authorization URL with state for CSRF protection.
 */
public record OAuthAuthorizationUrl(String url, String state) {
}

