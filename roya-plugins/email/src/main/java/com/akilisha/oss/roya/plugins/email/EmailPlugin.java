package com.akilisha.oss.roya.plugins.email;

import com.akilisha.oss.roya.api.plugin.*;
import com.akilisha.oss.roya.plugins.email.providers.MailerSendProvider;
import com.akilisha.oss.roya.plugins.email.providers.SendGridProvider;
import com.akilisha.oss.roya.plugins.email.providers.SmtpProvider;
import com.akilisha.oss.roya.plugins.email.template.HandlebarsTemplateEngine;
import com.akilisha.oss.roya.plugins.email.template.TemplateEngine;

/**
 * Email plugin - provider-agnostic email service.
 *
 * Supports multiple email providers (SendGrid, MailerSend, Brevo, SMTP).
 * Thin wrappers around provider SDKs.
 */
public class EmailPlugin implements RoyaPlugin {

    @Override
    public String id() {
        return "email";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "Provider-agnostic email service with thin SDK wrappers";
    }

    @Override
    public void register(Services services) {
        services.singleton(Email.class, () -> {
            // Configuration from environment variables
            String provider = System.getProperty("email.provider", "smtp");
            String defaultFromEmail = System.getProperty("email.from", "noreply@example.com");
            String defaultFromName = System.getProperty("email.fromName", "");
            String templatesDir = System.getProperty("email.templates", "./email-templates");

            com.akilisha.oss.roya.plugins.email.providers.EmailProvider emailProvider;

            switch (provider.toLowerCase()) {
                case "sendgrid" -> {
                    String apiKey = System.getProperty("email.sendgrid.apiKey");
                    if (apiKey == null || apiKey.isBlank()) {
                        throw new IllegalArgumentException("email.sendgrid.apiKey is required when using SendGrid provider");
                    }
                    emailProvider = new SendGridProvider(apiKey);
                }
                case "mailersend" -> {
                    String apiKey = System.getProperty("email.mailersend.apiKey");
                    if (apiKey == null || apiKey.isBlank()) {
                        throw new IllegalArgumentException("email.mailersend.apiKey is required when using MailerSend provider");
                    }
                    emailProvider = new MailerSendProvider(apiKey);
                }
                case "smtp" -> {
                    String smtpHost = System.getProperty("email.smtp.host", "localhost");
                    int smtpPort = Integer.parseInt(System.getProperty("email.smtp.port", "587"));
                    String smtpUsername = System.getProperty("email.smtp.username", "");
                    String smtpPassword = System.getProperty("email.smtp.password", "");
                    emailProvider = new SmtpProvider(smtpHost, smtpPort, smtpUsername, smtpPassword, defaultFromEmail);
                }
                default -> {
                    // Default to SMTP
                    String smtpHost = System.getProperty("email.smtp.host", "localhost");
                    int smtpPort = Integer.parseInt(System.getProperty("email.smtp.port", "587"));
                    String smtpUsername = System.getProperty("email.smtp.username", "");
                    String smtpPassword = System.getProperty("email.smtp.password", "");
                    emailProvider = new SmtpProvider(smtpHost, smtpPort, smtpUsername, smtpPassword, defaultFromEmail);
                }
                // Add more providers here (MailerSend, Brevo, Resend, Postmark)
            }

            TemplateEngine templateEngine = new HandlebarsTemplateEngine(templatesDir);

            return new EmailServiceImpl(emailProvider, templateEngine, defaultFromEmail, defaultFromName);
        });
    }

    @Override
    public void start() throws Exception {
        System.out.println("✓ EmailPlugin: Email service initialized");
        System.out.println("  - Provider: " + System.getProperty("email.provider", "smtp"));
        System.out.println("  - From: " + System.getProperty("email.from", "noreply@example.com"));
        System.out.println("  - Templates: " + System.getProperty("email.templates", "./email-templates"));
    }

    @Override
    public void stop() throws Exception {
        // No cleanup needed
    }
}

