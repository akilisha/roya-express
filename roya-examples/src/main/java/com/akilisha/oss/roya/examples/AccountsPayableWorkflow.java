package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIOptions;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.LLMActionNode;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;
import com.akilisha.oss.roya.workflow.edges.Edge;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import com.akilisha.oss.roya.workflow.visitor.LoggingVisitor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Accounts Payable Automation Workflow
 *
 * This workflow automates the process of handling supplier invoice payment inquiries:
 * 1. Gmail trigger - new message arrived
 * 2. Read email's subject and content - analyze intent and sentiment
 * 3. Check for attachments
 * 3.1. If present: Download, OCR, use as RAG context
 * 4. Check PowerBI report for payment status
 * 5. Customize response based on status (unscheduled, scheduled, paid, not found)
 * 6. Format and send email response
 *
 * Run:
 * ./gradlew :roya-examples:run --args="AccountsPayableWorkflow"
 */
public class AccountsPayableWorkflow {

    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("Accounts Payable Automation Workflow");
        System.out.println("=".repeat(70));

        var app = Roya.create();

        // Register AI Plugin
        var aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        try {
            aiPlugin.start();
        } catch (Exception e) {
            System.err.println("❌ Error: AI plugin failed to start: " + e.getMessage());
            System.err.println("   Make sure to set: -Dai.openai.apiKey=your-api-key");
            System.exit(1);
        }

        AI ai = app.services().get(AI.class);

        // Build the workflow
        Workflow workflow = buildAccountsPayableWorkflow(ai);

        // Execute workflow
        WorkflowExecutor executor = new WorkflowExecutor(workflow)
            .addVisitor(new LoggingVisitor());

        // Simulate a new email from a supplier
        Map<String, Object> emailData = Map.of(
            "emailId", "msg-12345",
            "from", "supplier@example.com",
            "to", "accounts@vendor.com",
            "subject", "Payment Status Inquiry - Invoice #INV-2024-001",
            "body", "Hello, I'm inquiring about the payment status of invoice #INV-2024-001 dated 2024-01-15. " +
                    "The amount is $5,000.00. Could you please confirm if this has been scheduled for payment?",
            "hasAttachments", true,
            "attachmentCount", 1,
            "attachments", List.of(Map.of(
                "id", "att-001",
                "filename", "invoice-INV-2024-001.pdf",
                "mimeType", "application/pdf",
                "size", 245678
            ))
        );

        System.out.println("\n📧 Simulating new email from supplier...");
        System.out.println("   From: " + emailData.get("from"));
        System.out.println("   Subject: " + emailData.get("subject"));
        System.out.println("   Has Attachments: " + emailData.get("hasAttachments"));

        // Start from trigger node
        WorkflowResult result = executor.executeFrom(
            "gmailTrigger",
            emailData
        ).join();

        // Display results
        displayResults(result);

        executor.shutdown();
    }

    /**
     * Build the complete accounts payable automation workflow.
     */
    private static Workflow buildAccountsPayableWorkflow(AI ai) {
        return ai.workflow("accounts-payable-automation")
            // ===== TRIGGERS =====
            // 1. Gmail trigger - new message arrived
            .trigger("gmailTrigger", new GmailTriggerNode())

            // ===== ANALYSIS =====
            // 2. Read email's subject and content - analyze intent and sentiment
            .workflowBuilder()
            .logic("prepareEmailContent", new PrepareEmailContentNode())
            .action("analyzeEmail", LLMActionNode.builder(ai)
                .systemPrompt("You are an accounts payable assistant. Analyze the email to determine:\n" +
                    "1. Intent: Is this an inquiry about invoice payment status?\n" +
                    "2. Sentiment: Is the tone neutral, urgent, or frustrated?\n" +
                    "3. Invoice details: Extract invoice number, amount, date if mentioned\n" +
                    "4. Action needed: What specific information is being requested?\n\n" +
                    "Respond in JSON format with: intent, sentiment, invoiceNumber, invoiceAmount, invoiceDate, actionNeeded")
                .inputKey("emailContent")
                .outputKey("emailAnalysis")
                .options(AIOptions.builder()
                    .model(System.getProperty("ai.model", "gpt-3.5-turbo"))  // Use OpenAI by default, or override via -Dai.model
                    .build())
                .build())

            // ===== ATTACHMENT HANDLING =====
            // 3. Check for attachments
            .logic("checkAttachments", new AttachmentCheckNode())

            // 3.1. If present: Download and OCR
            .action("downloadAttachments", new AttachmentDownloadNode())
            .action("ocrAttachments", new OCRNode())

            // 3.1.3. Use extracted text as RAG for handling inquiry
            // Note: In production, you would first index invoice documents to the "invoices" collection
            // For this demo, we'll use the OCR text directly as context instead of RAG
            .logic("prepareRAGContext", new PrepareRAGContextNode())

            // ===== PAYMENT STATUS CHECK =====
            // 4. Check PowerBI report for payment status
            .action("checkPowerBI", new PowerBINode())

            // ===== ROUTING LOGIC =====
            // 5. Route based on payment status
            .logic("routeByStatus", new PaymentStatusRouterNode())

            // ===== RESPONSE PREPARATION =====
            // 5.1. If unscheduled - schedule and prepare response
            .action("schedulePayment", new SchedulePaymentNode())
            .action("prepareUnscheduledResponse", com.akilisha.oss.roya.plugins.ai.nodes.actions.LLMActionNode.builder(ai)
                .systemPrompt("You are an accounts payable clerk. Draft a professional email response informing the supplier that:\n" +
                    "1. Their invoice has been received and reviewed\n" +
                    "2. The payment has been scheduled for [payment date]\n" +
                    "3. Expected payment date and method\n" +
                    "4. Any additional information needed\n\n" +
                    "Be courteous and professional. Include invoice number and amount for reference.")
                .inputKey("paymentScheduleInfo")
                .outputKey("responseText")
                .build())

            // 5.2. If scheduled - prepare response info
            .action("prepareScheduledResponse", com.akilisha.oss.roya.plugins.ai.nodes.actions.LLMActionNode.builder(ai)
                .systemPrompt("You are an accounts payable clerk. Draft a professional email response informing the supplier that:\n" +
                    "1. Their invoice payment is already scheduled\n" +
                    "2. The scheduled payment date\n" +
                    "3. Payment method and reference number\n" +
                    "4. Confirmation details\n\n" +
                    "Be courteous and professional. Include invoice number and amount for reference.")
                .inputKey("scheduledPaymentInfo")
                .outputKey("responseText")
                .build())

            // 5.3. If paid - prepare response info
            .action("preparePaidResponse", com.akilisha.oss.roya.plugins.ai.nodes.actions.LLMActionNode.builder(ai)
                .systemPrompt("You are an accounts payable clerk. Draft a professional email response informing the supplier that:\n" +
                    "1. Their invoice has been paid\n" +
                    "2. Payment date and amount\n" +
                    "3. Payment method and transaction reference\n" +
                    "4. Confirmation details\n\n" +
                    "Be courteous and professional. Include invoice number and amount for reference.")
                .inputKey("paidPaymentInfo")
                .outputKey("responseText")
                .build())

            // 5.4. If not found - escalate to accounts clerk
            .action("escalateToClerk", new EscalationNode())

            // ===== EMAIL SENDING =====
            // 6. Format and send email response
            .action("formatAndSendEmail", new EmailResponseNode())

            // ===== EDGES =====
            // Gmail trigger -> Prepare email content
            .edge("gmailTrigger", "prepareEmailContent")

            // Prepare email content -> Analyze email
            .edge("prepareEmailContent", "analyzeEmail", Edge.sequential()
                .withRetry(com.akilisha.oss.roya.workflow.retry.RetryPolicy.exponentialBackoff(3, java.time.Duration.ofSeconds(1)))
            )

            // Analyze -> Check attachments
            .edge("analyzeEmail", "checkAttachments")

            // Check attachments -> Download (if has attachments)
            .edge("checkAttachments", "downloadAttachments",
                Edge.when(ctx -> Boolean.TRUE.equals(ctx.get("hasAttachments")))
            )

            // Download -> OCR
            .edge("downloadAttachments", "ocrAttachments")

            // OCR -> Prepare RAG context (if OCR text available)
            .edge("ocrAttachments", "prepareRAGContext",
                Edge.when(ctx -> ctx.get("ocrText") != null && !ctx.get("ocrText").toString().isEmpty())
            )

            // Check attachments -> PowerBI (if no attachments, or after RAG context)
            .edge("checkAttachments", "checkPowerBI",
                Edge.when(ctx -> !Boolean.TRUE.equals(ctx.get("hasAttachments")))
            )
            .edge("prepareRAGContext", "checkPowerBI")

            // PowerBI -> Route by status
            .edge("checkPowerBI", "routeByStatus")

            // Route -> Schedule payment (if unscheduled)
            .edge("routeByStatus", "schedulePayment",
                Edge.when(ctx -> "unscheduled".equals(ctx.get("paymentStatus")))
            )

            // Route -> Prepare scheduled response
            .edge("routeByStatus", "prepareScheduledResponse",
                Edge.when(ctx -> "scheduled".equals(ctx.get("paymentStatus")))
            )

            // Route -> Prepare paid response
            .edge("routeByStatus", "preparePaidResponse",
                Edge.when(ctx -> "paid".equals(ctx.get("paymentStatus")))
            )

            // Route -> Escalate (if not found)
            .edge("routeByStatus", "escalateToClerk",
                Edge.when(ctx -> "not_found".equals(ctx.get("paymentStatus")))
            )

            // Schedule -> Prepare unscheduled response
            .edge("schedulePayment", "prepareUnscheduledResponse")

            // All response preparations -> Format and send
            .edge("prepareUnscheduledResponse", "formatAndSendEmail")
            .edge("prepareScheduledResponse", "formatAndSendEmail")
            .edge("preparePaidResponse", "formatAndSendEmail")
            .edge("escalateToClerk", "formatAndSendEmail")

            .build();
    }

    /**
     * Display workflow execution results.
     */
    private static void displayResults(WorkflowResult result) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("Workflow Execution Result");
        System.out.println("=".repeat(70));

        if (result.isSuccess()) {
            System.out.println("✓ Workflow completed successfully!");
            System.out.println("\nFinal Context:");
            result.finalOutput().data().forEach((key, value) -> {
                System.out.println("  " + key + ": " + value);
            });
        } else {
            System.out.println("✗ Workflow failed!");
            result.finalOutput().error().ifPresent(error ->
                System.out.println("Error: " + error)
            );
        }

        System.out.println("\nExecution Trace:");
        result.getTrace().forEach(event ->
            System.out.println("  " + event)
        );
    }

    // ===== CUSTOM NODE IMPLEMENTATIONS =====

    /**
     * Prepare email content node - combines subject and body for analysis.
     */
    static class PrepareEmailContentNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.supplyAsync(() -> {
                String subject = (String) input.data().get("subject");
                String body = (String) input.data().get("body");
                String emailContent = String.format("Subject: %s\n\nBody: %s", subject, body);
                Map<String, Object> result = new HashMap<>(input.data());
                result.put("emailContent", emailContent);
                return NodeOutput.success(result);
            });
        }
    }

    /**
     * Prepare RAG context node - combines email and OCR text for context.
     */
    static class PrepareRAGContextNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.supplyAsync(() -> {
                String ocrText = (String) input.data().get("ocrText");
                String emailContent = (String) input.data().get("emailContent");
                String ragContext = String.format(
                    "Email Content:\n%s\n\nExtracted from Attachments:\n%s",
                    emailContent != null ? emailContent : "",
                    ocrText != null ? ocrText : ""
                );
                Map<String, Object> result = new HashMap<>(input.data());
                result.put("ragContext", ragContext);
                return NodeOutput.success(result);
            });
        }
    }

    /**
     * Gmail trigger node - simulates monitoring for new emails.
     * In production, this would integrate with Gmail API via webhook or polling.
     */
    static class GmailTriggerNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.supplyAsync(() -> {
                // In production: Poll Gmail API or listen for webhooks
                // For demo: Input already contains email data
                Map<String, Object> emailData = new HashMap<>(input.data());

                System.out.println("\n📬 Gmail Trigger: New email received");
                System.out.println("   Email ID: " + emailData.get("emailId"));

                return NodeOutput.success(emailData);
            });
        }
    }

    /**
     * Attachment check node - determines if email has attachments.
     */
    static class AttachmentCheckNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.supplyAsync(() -> {
                Boolean hasAttachments = Boolean.TRUE.equals(input.data().get("hasAttachments"));
                Integer attachmentCount = (Integer) input.data().getOrDefault("attachmentCount", 0);

                System.out.println("\n📎 Attachment Check:");
                System.out.println("   Has Attachments: " + hasAttachments);
                System.out.println("   Count: " + attachmentCount);

                Map<String, Object> result = new HashMap<>(input.data());
                result.put("hasAttachments", hasAttachments);
                result.put("attachmentCount", attachmentCount);

                return NodeOutput.success(result);
            });
        }
    }

    /**
     * Attachment download node - downloads email attachments.
     * In production, this would download from Gmail API.
     */
    static class AttachmentDownloadNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.supplyAsync(() -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> attachments =
                    (List<Map<String, Object>>) input.data().get("attachments");

                System.out.println("\n⬇️ Downloading Attachments:");
                if (attachments != null) {
                    for (Map<String, Object> attachment : attachments) {
                        System.out.println("   ✓ Downloaded: " + attachment.get("filename") +
                            " (" + attachment.get("size") + " bytes)");
                    }
                }

                Map<String, Object> result = new HashMap<>(input.data());
                result.put("downloadedAttachments", attachments);
                return NodeOutput.success(result);
            });
        }
    }

    /**
     * OCR node - extracts text from PDF/image attachments using OCR.
     * In production, this would use Tesseract, AWS Textract, or similar.
     */
    static class OCRNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.supplyAsync(() -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> attachments =
                    (List<Map<String, Object>>) input.data().get("downloadedAttachments");

                System.out.println("\n🔍 OCR Processing:");
                StringBuilder ocrText = new StringBuilder();

                if (attachments != null) {
                    for (Map<String, Object> attachment : attachments) {
                        String filename = (String) attachment.get("filename");
                        System.out.println("   Processing: " + filename);

                        // Simulate OCR extraction
                        // In production: Use Tesseract, AWS Textract, Google Vision API, etc.
                        String extractedText = simulateOCR(filename);
                        ocrText.append("=== ").append(filename).append(" ===\n");
                        ocrText.append(extractedText).append("\n\n");
                    }
                }

                System.out.println("   ✓ Extracted " + ocrText.length() + " characters");

                Map<String, Object> result = new HashMap<>(input.data());
                result.put("ocrText", ocrText.toString());
                return NodeOutput.success(result);
            });
        }

        private String simulateOCR(String filename) {
            // Simulate OCR extraction from invoice PDF
            return """
                INVOICE
                Invoice Number: INV-2024-001
                Date: January 15, 2024
                Due Date: February 15, 2024
                
                Bill To:
                Vendor Company
                123 Business St
                City, State 12345
                
                Description: Professional Services
                Amount: $5,000.00
                
                Payment Terms: Net 30
                """;
        }
    }

    /**
     * PowerBI node - checks payment status in PowerBI report.
     * In production, this would query PowerBI REST API or database.
     */
    static class PowerBINode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.supplyAsync(() -> {
                // Extract invoice number from email analysis or OCR
                String invoiceNumber = extractInvoiceNumber(input);

                System.out.println("\n📊 Checking PowerBI Report:");
                System.out.println("   Invoice Number: " + invoiceNumber);

                // Simulate PowerBI query
                // In production: Query PowerBI REST API or underlying database
                PaymentStatus status = queryPowerBI(invoiceNumber);

                System.out.println("   Status: " + status.status);
                if (status.paymentDate != null) {
                    System.out.println("   Payment Date: " + status.paymentDate);
                }
                if (status.scheduledDate != null) {
                    System.out.println("   Scheduled Date: " + status.scheduledDate);
                }

                Map<String, Object> result = new HashMap<>(input.data());
                result.put("paymentStatus", status.status);
                result.put("invoiceNumber", invoiceNumber);
                result.put("paymentDate", Objects.requireNonNullElse(status.paymentDate, "##-##-####"));
                result.put("scheduledDate", Objects.requireNonNullElse(status.scheduledDate, "##-##-####"));
                result.put("paymentMethod", Objects.requireNonNullElse(status.paymentMethod, "unspecified"));
                result.put("transactionReference", Objects.requireNonNullElse(status.transactionReference, "not completed"));

                return NodeOutput.success(result);
            });
        }

        private String extractInvoiceNumber(NodeInput input) {
            // Try to extract from email analysis first
            Object analysis = input.data().get("emailAnalysis");
            if (analysis != null && analysis.toString().contains("INV-2024-001")) {
                return "INV-2024-001";
            }

            // Try OCR text
            String ocrText = (String) input.data().get("ocrText");
            if (ocrText != null) {
                Pattern pattern = Pattern.compile("INV-\\d{4}-\\d{3}");
                java.util.regex.Matcher matcher = pattern.matcher(ocrText);
                if (matcher.find()) {
                    return matcher.group();
                }
            }

            // Fallback: extract from subject
            String subject = (String) input.data().get("subject");
            if (subject != null) {
                Pattern pattern = Pattern.compile("INV-\\d{4}-\\d{3}");
                java.util.regex.Matcher matcher = pattern.matcher(subject);
                if (matcher.find()) {
                    return matcher.group();
                }
            }

            return "UNKNOWN";
        }

        private PaymentStatus queryPowerBI(String invoiceNumber) {
            // Simulate PowerBI query
            // In production: Make REST API call to PowerBI or query database
            return switch (invoiceNumber) {
                case "INV-2024-001" -> new PaymentStatus("unscheduled", null, null, null, null);
                case "INV-2024-002" -> new PaymentStatus("scheduled", null, "2024-02-20", "ACH", "ACH-2024-002");
                case "INV-2024-003" -> new PaymentStatus("paid", "2024-01-30", null, "Wire Transfer", "WT-2024-003");
                default -> new PaymentStatus("not_found", null, null, null, null);
            };
        }

        record PaymentStatus(
            String status,
            String paymentDate,
            String scheduledDate,
            String paymentMethod,
            String transactionReference
        ) {}
    }

    /**
     * Payment status router node - routes workflow based on payment status.
     */
    static class PaymentStatusRouterNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.supplyAsync(() -> {
                String status = (String) input.data().get("paymentStatus");
                System.out.println("\n🔀 Routing by Payment Status: " + status);

                Map<String, Object> result = new HashMap<>(input.data());
                result.put("routingStatus", status);
                return NodeOutput.success(result);
            });
        }
    }

    /**
     * Schedule payment node - schedules payment for unscheduled invoices.
     */
    static class SchedulePaymentNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.supplyAsync(() -> {
                String invoiceNumber = (String) input.data().get("invoiceNumber");

                System.out.println("\n📅 Scheduling Payment:");
                System.out.println("   Invoice: " + invoiceNumber);

                // Simulate payment scheduling
                // In production: Update ERP system, create payment batch, etc.
                String scheduledDate = "2024-02-20";
                String paymentMethod = "ACH";
                String batchReference = "BATCH-2024-02-20-001";

                System.out.println("   ✓ Scheduled for: " + scheduledDate);
                System.out.println("   Method: " + paymentMethod);
                System.out.println("   Batch: " + batchReference);

                Map<String, Object> result = new HashMap<>(input.data());
                result.put("scheduledDate", scheduledDate);
                result.put("paymentMethod", paymentMethod);
                result.put("batchReference", batchReference);
                result.put("paymentScheduleInfo", String.format(
                    "Invoice %s has been scheduled for payment on %s via %s. Batch reference: %s",
                    invoiceNumber, scheduledDate, paymentMethod, batchReference
                ));

                return NodeOutput.success(result);
            });
        }
    }

    /**
     * Escalation node - escalates to accounts clerk for manual review.
     */
    static class EscalationNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.supplyAsync(() -> {
                String invoiceNumber = (String) input.data().get("invoiceNumber");

                System.out.println("\n⚠️ Escalating to Accounts Clerk:");
                System.out.println("   Invoice: " + invoiceNumber);
                System.out.println("   Reason: Invoice not found in system");

                // Simulate escalation
                // In production: Create ticket, send notification, etc.
                String escalationId = "ESC-" + System.currentTimeMillis();
                String responseText = String.format(
                    "Thank you for your inquiry regarding invoice %s. " +
                    "We are unable to locate this invoice in our system at this time. " +
                    "Our accounts payable team has been notified and will investigate. " +
                    "We will follow up with you within 2 business days. " +
                    "Escalation ID: %s",
                    invoiceNumber, escalationId
                );

                Map<String, Object> result = new HashMap<>(input.data());
                result.put("escalationId", escalationId);
                result.put("responseText", responseText);
                result.put("requiresManualReview", true);

                return NodeOutput.success(result);
            });
        }
    }

    /**
     * Email response node - formats and sends email response.
     */
    static class EmailResponseNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.supplyAsync(() -> {
                String to = (String) input.data().get("from"); // Reply to sender
                String subject = "Re: " + input.data().get("subject");
                String responseText = (String) input.data().get("responseText");

                // If escalation, use escalation response
                if (responseText == null) {
                    responseText = (String) input.data().get("responseText");
                }

                System.out.println("\n📧 Formatting and Sending Email Response:");
                System.out.println("   To: " + to);
                System.out.println("   Subject: " + subject);
                System.out.println("   Response Length: " + (responseText != null ? responseText.length() : 0) + " characters");

                // In production: Use Email plugin to send
                // Email email = services.get(Email.class);
                // email.send(to, subject, responseText);

                System.out.println("   ✓ Email sent successfully");

                Map<String, Object> result = new HashMap<>(input.data());
                result.put("emailSent", true);
                result.put("responseEmail", Map.of(
                    "to", to,
                    "subject", subject,
                    "body", responseText != null ? responseText : ""
                ));

                return NodeOutput.success(result);
            });
        }
    }
}
