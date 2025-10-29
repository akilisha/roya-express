package com.akilisha.oss.roya.plugins.email;

import java.util.Optional;

/**
 * Result of an email sending operation.
 */
public record EmailResult(
    String messageId,    // Provider's message ID
    String provider,     // Provider name (e.g., "sendgrid", "smtp")
    boolean success,     // Whether email was sent successfully
    Optional<String> error  // Error message if failed
) {
    public EmailResult {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("provider must not be null or blank");
        }
    }

    public static EmailResult success(String messageId, String provider) {
        return new EmailResult(messageId, provider, true, Optional.empty());
    }

    public static EmailResult failure(String provider, String error) {
        return new EmailResult(null, provider, false, Optional.of(error));
    }
}

