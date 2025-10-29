package com.akilisha.oss.roya.plugins.email;

import com.akilisha.oss.roya.plugins.email.providers.EmailFeature;
import com.akilisha.oss.roya.plugins.email.providers.EmailProvider;
import com.akilisha.oss.roya.plugins.email.providers.EmailRequest;
import com.akilisha.oss.roya.plugins.email.template.HandlebarsTemplateEngine;
import com.akilisha.oss.roya.plugins.email.template.TemplateEngine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Email service implementation.
 *
 * Wraps email providers and provides unified API.
 */
public class EmailServiceImpl implements Email {

    private final EmailProvider provider;
    private final TemplateEngine templateEngine;
    private final String defaultFromEmail;
    private final String defaultFromName;

    public EmailServiceImpl(
        EmailProvider provider,
        TemplateEngine templateEngine,
        String defaultFromEmail,
        String defaultFromName
    ) {
        this.provider = provider;
        this.templateEngine = templateEngine;
        this.defaultFromEmail = defaultFromEmail;
        this.defaultFromName = defaultFromName;
    }

    @Override
    public CompletableFuture<EmailResult> send(String to, String subject, String body) {
        EmailRequest request = new EmailRequest(
            defaultFromEmail, defaultFromName,
            List.of(to),
            List.of(), List.of(),
            subject, body, null,
            List.of(), Map.of()
        );
        return provider.send(request);
    }

    @Override
    public CompletableFuture<EmailResult> send(String to, String subject, String htmlBody, String textBody) {
        EmailRequest request = new EmailRequest(
            defaultFromEmail, defaultFromName,
            List.of(to),
            List.of(), List.of(),
            subject, htmlBody, textBody,
            List.of(), Map.of()
        );
        return provider.send(request);
    }

    @Override
    public CompletableFuture<EmailResult> sendTemplate(
        String to, String subject, String templateName, Map<String, Object> data
    ) {
        try {
            String htmlBody = templateEngine.render(templateName, data);
            return send(to, subject, htmlBody);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                EmailResult.failure(provider.name(), "Template rendering failed: " + e.getMessage())
            );
        }
    }

    @Override
    public CompletableFuture<EmailResult> send(EmailMessage message) {
        EmailRequest request = new EmailRequest(
            message.fromEmail() != null ? message.fromEmail() : defaultFromEmail,
            message.fromName() != null ? message.fromName() : defaultFromName,
            message.to(),
            message.cc(),
            message.bcc(),
            message.subject(),
            message.htmlBody(),
            message.textBody(),
            message.tags(),
            message.metadata()
        );
        return provider.send(request);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T provider(Class<T> providerType) {
        // Return provider instance if it matches
        if (providerType.isInstance(provider)) {
            return providerType.cast(provider);
        }
        // Special cases for provider-specific SDK access
        if (provider instanceof com.akilisha.oss.roya.plugins.email.providers.SendGridProvider) {
            if (providerType == com.sendgrid.SendGrid.class) {
                return providerType.cast(((com.akilisha.oss.roya.plugins.email.providers.SendGridProvider) provider).getSendGrid());
            }
        }
        // TODO: Uncomment when MailerSend SDK is available
        // if (provider instanceof MailerSendProvider) {
        //     if (providerType == com.mailersend.sdk.MailerSend.class) {
        //         return providerType.cast(((MailerSendProvider) provider).getMailerSend());
        //     }
        // }
        return null;
    }
}

