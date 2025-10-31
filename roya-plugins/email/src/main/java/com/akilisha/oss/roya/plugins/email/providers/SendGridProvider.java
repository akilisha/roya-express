package com.akilisha.oss.roya.plugins.email.providers;

import com.akilisha.oss.roya.plugins.email.EmailResult;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SendGrid email provider.
 *
 * Thin wrapper around SendGrid Java SDK.
 */
public class SendGridProvider implements EmailProvider {

    private final SendGrid sendGrid;
    private final ExecutorService executorService;

    public SendGridProvider(String apiKey) {
        this.sendGrid = new SendGrid(apiKey);
        // Use virtual threads for async email sending
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public String name() {
        return "sendgrid";
    }

    @Override
    public CompletableFuture<EmailResult> send(EmailRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Mail mail = new Mail();

                // From
                String fromEmail = request.fromEmail();
                String fromName = request.fromName();
                if (fromName != null && !fromName.isBlank()) {
                    mail.setFrom(new Email(fromEmail, fromName));
                } else {
                    mail.setFrom(new Email(fromEmail));
                }

                // To
                Personalization personalization = new Personalization();
                for (String to : request.to()) {
                    personalization.addTo(new Email(to));
                }
                for (String cc : request.cc()) {
                    personalization.addCc(new Email(cc));
                }
                for (String bcc : request.bcc()) {
                    personalization.addBcc(new Email(bcc));
                }
                mail.addPersonalization(personalization);

                // Subject
                mail.setSubject(request.subject());

                // Body
                if (request.htmlBody() != null) {
                    Content htmlContent = new Content("text/html", request.htmlBody());
                    mail.addContent(htmlContent);
                }
                if (request.textBody() != null) {
                    Content textContent = new Content("text/plain", request.textBody());
                    mail.addContent(textContent);
                }

                // Tags
                for (String tag : request.tags()) {
                    mail.addCategory(tag);
                }

                // Send
                Request sgRequest = new Request();
                sgRequest.setMethod(Method.POST);
                sgRequest.setEndpoint("mail/send");
                sgRequest.setBody(mail.build());

                Response response = sendGrid.api(sgRequest);

                if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                    // Extract message ID from response headers or generate one
                    String messageId = response.getHeaders().getOrDefault("X-Message-Id", "sg-" + System.currentTimeMillis());
                    return EmailResult.success(messageId, "sendgrid");
                } else {
                    return EmailResult.failure("sendgrid", "Status: " + response.getStatusCode() + ", Body: " + response.getBody());
                }
            } catch (Exception e) {
                return EmailResult.failure("sendgrid", e.getMessage());
            }
        }, executorService);
    }

    @Override
    public boolean supportsFeature(EmailFeature feature) {
        return switch (feature) {
            case ANALYTICS, TAGS, WEBHOOKS, TEMPLATES -> true;
            default -> false;
        };
    }

    /**
     * Get SendGrid client for advanced features.
     */
    public SendGrid getSendGrid() {
        return sendGrid;
    }
}

