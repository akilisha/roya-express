package com.akilisha.oss.roya.plugins.auth;

import java.time.Instant;
import java.util.Map;

/**
 * User model - represents an authenticated user.
 *
 * Immutable record representing a user in the auth system.
 */
public record User(
    String id,
    String email,
    Instant createdAt,
    Instant updatedAt,
    Map<String, Object> userData
) {
    /**
     * Create a new user.
     */
    public User(String id, String email, Instant createdAt, Instant updatedAt, Map<String, Object> userData) {
        this.id = id;
        this.email = email;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
        this.userData = userData != null ? userData : Map.of();
    }
}

