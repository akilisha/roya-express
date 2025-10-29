package com.akilisha.oss.roya.plugins.email;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Unified email service interface.
 *
 * Provides a consistent API regardless of email provider.
 * Supports both simple text/html emails and template-based emails.
 *
 * Example usage:
 * <pre>
 * Email email = req.get(Email.class);
 *
 * // Simple email
 * email.send("user@example.com", "Hello", "Welcome to Roya!")
 *     .thenAccept(result -> {
 *         if (result.success()) {
 *             System.out.println("Email sent: " + result.messageId());
 *         }
 *     });
 *
 * // Template email
 * email.sendTemplate("user@example.com", "Welcome", "welcome-template", Map.of(
 *     "name", "John",
 *     "activationLink", "https://app.com/activate"
 * ));
 * </pre>
 */
public interface Email {
    /**
     * Send a simple email.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body (text or HTML)
     * @return CompletableFuture with email result
     */
    CompletableFuture<EmailResult> send(String to, String subject, String body);

    /**
     * Send an email with HTML content.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlBody HTML email body
     * @param textBody Optional plain text body (fallback)
     * @return CompletableFuture with email result
     */
    CompletableFuture<EmailResult> send(String to, String subject, String htmlBody, String textBody);

    /**
     * Send an email using a template.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param templateName Template name (without extension)
     * @param data Template data (key-value pairs for template variables)
     * @return CompletableFuture with email result
     */
    CompletableFuture<EmailResult> sendTemplate(
        String to, String subject, String templateName, Map<String, Object> data
    );

    /**
     * Send an email with full control over message properties.
     *
     * @param message Email message builder
     * @return CompletableFuture with email result
     */
    CompletableFuture<EmailResult> send(EmailMessage message);

    /**
     * Get provider-specific API access (when needed for advanced features).
     *
     * @param providerType Provider type class (e.g., SendGrid.class)
     * @return Provider instance or null if not available
     */
    <T> T provider(Class<T> providerType);
}

