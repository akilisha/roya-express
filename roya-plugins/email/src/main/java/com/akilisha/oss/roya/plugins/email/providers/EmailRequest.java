package com.akilisha.oss.roya.plugins.email.providers;

import java.util.List;
import java.util.Map;

/**
 * Email request to be sent by a provider.
 */
public record EmailRequest(
    String fromEmail,
    String fromName,
    List<String> to,
    List<String> cc,
    List<String> bcc,
    String subject,
    String htmlBody,
    String textBody,
    List<String> tags,
    Map<String, String> metadata
) {
    // Convenience constructors
    public static EmailRequest simple(String to, String subject, String body) {
        return new EmailRequest(
            null, null,
            List.of(to),
            List.of(), List.of(),
            subject, body, null,
            List.of(), Map.of()
        );
    }
}

