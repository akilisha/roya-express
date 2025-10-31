package com.akilisha.oss.roya.plugins.email.providers;

import com.akilisha.oss.roya.plugins.email.EmailResult;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SMTP email provider (universal fallback).
 *
 * Uses Jakarta Mail for SMTP-compatible providers.
 */
public class SmtpProvider implements EmailProvider {

    private final Session session;
    private final String fromEmail;
    private final ExecutorService executorService;

    public SmtpProvider(String host, int port, String username, String password, String fromEmail) {
        this.fromEmail = fromEmail;

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", host);

        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        // Use virtual threads for async email sending
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public String name() {
        return "smtp";
    }

    @Override
    public CompletableFuture<EmailResult> send(EmailRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MimeMessage message = new MimeMessage(session);

                // From
                String from = request.fromEmail() != null ? request.fromEmail() : fromEmail;
                String fromName = request.fromName();
                if (fromName != null && !fromName.isBlank()) {
                    message.setFrom(new InternetAddress(from, fromName));
                } else {
                    message.setFrom(new InternetAddress(from));
                }

                // To
                for (String to : request.to()) {
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                }

                // CC
                for (String cc : request.cc()) {
                    message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
                }

                // BCC
                for (String bcc : request.bcc()) {
                    message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc));
                }

                // Subject
                message.setSubject(request.subject());

                // Body
                if (request.htmlBody() != null && request.textBody() != null) {
                    // Multipart with both HTML and text
                    var multipart = new jakarta.mail.internet.MimeMultipart("alternative");

                    var textPart = new jakarta.mail.internet.MimeBodyPart();
                    textPart.setText(request.textBody(), "utf-8");
                    multipart.addBodyPart(textPart);

                    var htmlPart = new jakarta.mail.internet.MimeBodyPart();
                    htmlPart.setContent(request.htmlBody(), "text/html; charset=utf-8");
                    multipart.addBodyPart(htmlPart);

                    message.setContent(multipart);
                } else if (request.htmlBody() != null) {
                    message.setContent(request.htmlBody(), "text/html; charset=utf-8");
                } else {
                    message.setText(request.textBody(), "utf-8");
                }

                // Send
                Transport.send(message);

                // SMTP doesn't provide message ID, generate one
                String messageId = "smtp-" + System.currentTimeMillis();
                return EmailResult.success(messageId, "smtp");
            } catch (Exception e) {
                return EmailResult.failure("smtp", e.getMessage());
            }
        }, executorService);
    }

    @Override
    public boolean supportsFeature(EmailFeature feature) {
        // SMTP doesn't support advanced features
        return false;
    }
}

