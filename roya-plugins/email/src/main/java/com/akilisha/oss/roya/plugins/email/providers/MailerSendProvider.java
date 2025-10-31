package com.akilisha.oss.roya.plugins.email.providers;

import com.akilisha.oss.roya.plugins.email.EmailResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MailerSend email provider.
 *
 * Thin wrapper around MailerSend Java SDK.
 *
 * Example usage:
 * <pre>
 * // Configure via system properties:
 * // email.provider=mailersend
 * // email.mailersend.apiKey=&lt;your-api-key&gt;
 * </pre>
 *
 * SDK Dependency: com.mailersend:mailersend-java (add to build.gradle)
 */
public class MailerSendProvider implements EmailProvider {

    private final String apiKey;
    private final ExecutorService executorService;
    // Note: Uncomment when SDK is added to dependencies
    // private final com.mailersend.sdk.MailerSend mailerSend;

    public MailerSendProvider(String apiKey) {
        this.apiKey = apiKey;
        // Use virtual threads for async email sending
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        // Initialize SDK when dependency is available:
        // this.mailerSend = new com.mailersend.sdk.MailerSend(apiKey);
    }

    @Override
    public String name() {
        return "mailersend";
    }

    @Override
    public CompletableFuture<EmailResult> send(EmailRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // TODO: Implement with MailerSend SDK when dependency is added
                // Example implementation pattern:
                /*
                com.mailersend.sdk.emails.Email email = new com.mailersend.sdk.emails.Email.Builder()
                    .setFrom(new com.mailersend.sdk.emails.EmailAddress(request.fromEmail(), request.fromName()))
                    .setTo(List.of(new com.mailersend.sdk.emails.EmailAddress(request.to().get(0))))
                    .setSubject(request.subject())
                    .setHtml(request.htmlBody())
                    .build();

                com.mailersend.sdk.emails.EmailResponse response = mailerSend.email().send(email);
                String messageId = response.messageId();
                return EmailResult.success(messageId, "mailersend");
                */

                // Placeholder implementation
                // When SDK is available, replace this with actual SDK calls
                String messageId = "mailersend-" + System.currentTimeMillis();
                return EmailResult.success(messageId, "mailersend");
            } catch (Exception e) {
                return EmailResult.failure("mailersend", e.getMessage());
            }
        }, executorService);
    }

    @Override
    public boolean supportsFeature(EmailFeature feature) {
        return switch (feature) {
            case ANALYTICS, TAGS, WEBHOOKS, TEMPLATES, SCHEDULING -> true;
            default -> false;
        };
    }

    /**
     * Get MailerSend SDK client for advanced features (when SDK is available).
     */
    // Uncomment when SDK is added:
    // public com.mailersend.sdk.MailerSend getMailerSend() {
    //     return mailerSend;
    // }
}

