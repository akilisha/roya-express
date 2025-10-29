package com.akilisha.oss.roya.plugins.email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Email message builder for advanced email composition.
 *
 * Example:
 * <pre>
 * EmailMessage message = EmailMessage.builder()
 *     .from("noreply@example.com", "Example App")
 *     .to("user@example.com")
 *     .cc("admin@example.com")
 *     .subject("Welcome")
 *     .htmlBody("&lt;h1&gt;Welcome!&lt;/h1&gt;")
 *     .textBody("Welcome!")
 *     .tag("welcome-email")
 *     .build();
 *
 * email.send(message);
 * </pre>
 */
public record EmailMessage(
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
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String fromEmail;
        private String fromName;
        private final List<String> to = new ArrayList<>();
        private final List<String> cc = new ArrayList<>();
        private final List<String> bcc = new ArrayList<>();
        private String subject;
        private String htmlBody;
        private String textBody;
        private final List<String> tags = new ArrayList<>();
        private final Map<String, String> metadata = new HashMap<>();

        public Builder from(String email) {
            this.fromEmail = email;
            return this;
        }

        public Builder from(String email, String name) {
            this.fromEmail = email;
            this.fromName = name;
            return this;
        }

        public Builder to(String email) {
            this.to.add(email);
            return this;
        }

        public Builder to(List<String> emails) {
            this.to.addAll(emails);
            return this;
        }

        public Builder cc(String email) {
            this.cc.add(email);
            return this;
        }

        public Builder cc(List<String> emails) {
            this.cc.addAll(emails);
            return this;
        }

        public Builder bcc(String email) {
            this.bcc.add(email);
            return this;
        }

        public Builder bcc(List<String> emails) {
            this.bcc.addAll(emails);
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder htmlBody(String htmlBody) {
            this.htmlBody = htmlBody;
            return this;
        }

        public Builder textBody(String textBody) {
            this.textBody = textBody;
            return this;
        }

        public Builder body(String body) {
            // Assume HTML if contains HTML tags, otherwise plain text
            if (body.contains("<") && body.contains(">")) {
                this.htmlBody = body;
            } else {
                this.textBody = body;
            }
            return this;
        }

        public Builder tag(String tag) {
            this.tags.add(tag);
            return this;
        }

        public Builder metadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }

        public EmailMessage build() {
            if (fromEmail == null || fromEmail.isBlank()) {
                throw new IllegalStateException("fromEmail is required");
            }
            if (to.isEmpty()) {
                throw new IllegalStateException("At least one 'to' address is required");
            }
            if (subject == null || subject.isBlank()) {
                throw new IllegalStateException("subject is required");
            }
            if (htmlBody == null && textBody == null) {
                throw new IllegalStateException("At least one body (htmlBody or textBody) is required");
            }
            return new EmailMessage(
                fromEmail, fromName, List.copyOf(to), List.copyOf(cc), List.copyOf(bcc),
                subject, htmlBody, textBody, List.copyOf(tags), Map.copyOf(metadata)
            );
        }
    }
}

