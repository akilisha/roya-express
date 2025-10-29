package com.akilisha.oss.roya.plugins.email.providers;

import com.akilisha.oss.roya.plugins.email.EmailResult;

import java.util.concurrent.CompletableFuture;

/**
 * Email provider abstraction.
 *
 * Thin wrapper around provider-specific SDKs.
 * Each provider implementation delegates to its SDK for actual email sending.
 */
public interface EmailProvider {
    /**
     * Provider name (e.g., "sendgrid", "mailersend", "smtp").
     */
    String name();

    /**
     * Send an email using this provider.
     *
     * @param request Email request with all details
     * @return CompletableFuture with email result
     */
    CompletableFuture<EmailResult> send(EmailRequest request);

    /**
     * Check if provider supports a specific feature.
     *
     * @param feature Feature to check
     * @return true if supported, false otherwise
     */
    boolean supportsFeature(EmailFeature feature);
}

