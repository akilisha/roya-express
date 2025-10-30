package com.akilisha.oss.roya.api;

import java.util.Optional;

/**
 * Secrets access facade.
 *
 * Intended to be backed by Helidon Vault (HCV) in production. For development,
 * an implementation may read from Helidon Config.
 */
public interface Secrets {

    /**
     * Get a secret value from a logical path and key.
     * Example: path="secret/data/email/sendgrid", key="apiKey".
     */
    Optional<String> getOptional(String path, String key);

    default String get(String path, String key) {
        return getOptional(path, key).orElseThrow(() -> new IllegalStateException("Secret not found: " + path + "#" + key));
    }
}


