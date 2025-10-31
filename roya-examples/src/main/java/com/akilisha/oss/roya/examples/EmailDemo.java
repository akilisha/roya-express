package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.core.middleware.Cors;
import com.akilisha.oss.roya.core.middleware.Morgan;
import com.akilisha.oss.roya.plugins.email.Email;
import com.akilisha.oss.roya.plugins.email.EmailMessage;

import java.util.Map;

/**
 * Email Demo - Provider-agnostic email sending.
 *
 * Shows:
 * - Simple email sending
 * - Template-based emails
 * - EmailMessage builder
 * - Async email operations
 *
 * Configuration (via system properties):
 * - email.provider=smtp|sendgrid
 * - email.sendgrid.apiKey=<api-key> (if using SendGrid)
 * - email.smtp.host, email.smtp.port, email.smtp.username, email.smtp.password (if using SMTP)
 * - email.from=<from-email>
 * - email.templates=<templates-directory>
 */
public class EmailDemo {

    public static void main(String[] args) {
        var app = Roya.create();

        // Register Email plugin
        var services = app.services();
        var emailPlugin = new com.akilisha.oss.roya.plugins.email.EmailPlugin();
        emailPlugin.register(services);

        // Middleware
        app.use(Morgan.combined());
        app.use(Cors.cors());
        app.use(BodyParser.bodyParser());

        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  📧 Roya Email Demo - Provider-Agnostic Email Service   ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║                                                          ║");
        System.out.println("║  Features:                                               ║");
        System.out.println("║  - Provider-agnostic (SendGrid, SMTP, etc.)           ║");
        System.out.println("║  - Thin wrappers around provider SDKs                    ║");
        System.out.println("║  - Handlebars template engine                           ║");
        System.out.println("║  - Async email sending (CompletableFuture)              ║");
        System.out.println("║                                                          ║");
        System.out.println("║  Endpoints:                                              ║");
        System.out.println("║  - POST /email/send          Send simple email           ║");
        System.out.println("║  - POST /email/template      Send template email         ║");
        System.out.println("║  - POST /email/advanced      Send with full control      ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        // Simple email endpoint
        app.post("/email/send", (req, res, next) -> {
            try {
                Email email = req.get(Email.class);
                Map<String, Object> body = req.body(Map.class);

                String to = (String) body.get("to");
                String subject = (String) body.get("subject");
                String message = (String) body.get("message");

                email.send(to, subject, message)
                    .thenAccept(result -> {
                        if (result.success()) {
                            System.out.println("✓ Email sent: " + result.messageId());
                        } else {
                            System.err.println("✗ Email failed: " + result.error().orElse("Unknown error"));
                        }
                    })
                    .exceptionally(e -> {
                        System.err.println("✗ Email exception: " + e.getMessage());
                        return null;
                    });

                res.json(Map.of(
                    "status", "sending",
                    "message", "Email queued for sending"
                ));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Template email endpoint
        app.post("/email/template", (req, res, next) -> {
            try {
                Email email = req.get(Email.class);
                Map<String, Object> body = req.body(Map.class);

                String to = (String) body.get("to");
                String subject = (String) body.get("subject");
                String templateName = (String) body.get("template");
                @SuppressWarnings("unchecked")
                Map<String, Object> templateData = (Map<String, Object>) body.get("data");

                email.sendTemplate(to, subject, templateName, templateData)
                    .thenAccept(result -> {
                        if (result.success()) {
                            System.out.println("✓ Template email sent: " + result.messageId());
                        } else {
                            System.err.println("✗ Template email failed: " + result.error().orElse("Unknown error"));
                        }
                    });

                res.json(Map.of(
                    "status", "sending",
                    "message", "Template email queued"
                ));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Advanced email endpoint (EmailMessage builder)
        app.post("/email/advanced", (req, res, next) -> {
            try {
                Email email = req.get(Email.class);
                Map<String, Object> body = req.body(Map.class);

                EmailMessage message = EmailMessage.builder()
                    .from((String) body.getOrDefault("from", "noreply@example.com"))
                    .to((String) body.get("to"))
                    .subject((String) body.get("subject"))
                    .htmlBody((String) body.get("htmlBody"))
                    .textBody((String) body.get("textBody"))
                    .tag((String) body.getOrDefault("tag", "api-send"))
                    .build();

                email.send(message)
                    .thenAccept(result -> {
                        if (result.success()) {
                            System.out.println("✓ Advanced email sent: " + result.messageId() + " via " + result.provider());
                        } else {
                            System.err.println("✗ Advanced email failed: " + result.error().orElse("Unknown error"));
                        }
                    });

                res.json(Map.of(
                    "status", "sending",
                    "message", "Advanced email queued"
                ));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        // Email info endpoint
        app.get("/email/info", (req, res, next) -> {
            try {
                Email email = req.get(Email.class);
                String providerName = email.provider(com.akilisha.oss.roya.plugins.email.providers.EmailProvider.class).name();

                res.json(Map.of(
                    "provider", providerName,
                    "templates", System.getProperty("email.templates", "./email-templates"),
                    "from", System.getProperty("email.from", "noreply@example.com"),
                    "note", "Check system properties for full configuration"
                ));
            } catch (Exception e) {
                next.error(e, req, res);
            }
        });

        try {
            emailPlugin.start();
        } catch (Exception e) {
            System.err.println("Warning: Failed to start EmailPlugin: " + e.getMessage());
        }
        app.listen(3000, () -> {
            System.out.println("✓ Email Demo running on http://localhost:3000\n");
            System.out.println("📝 Quick Test:");
            System.out.println("  POST /email/send {");
            System.out.println("    \"to\": \"user@example.com\",");
            System.out.println("    \"subject\": \"Hello\",");
            System.out.println("    \"message\": \"Welcome to Roya!\"");
            System.out.println("  }\n");
            System.out.println("⚠️  Configure email provider via system properties:");
            System.out.println("  - email.provider=smtp|sendgrid");
            System.out.println("  - email.sendgrid.apiKey=<key> (for SendGrid)");
            System.out.println("  - email.smtp.host, email.smtp.port, etc. (for SMTP)\n");
        });
    }
}

