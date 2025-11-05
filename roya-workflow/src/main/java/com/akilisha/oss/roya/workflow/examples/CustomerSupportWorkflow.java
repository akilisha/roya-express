package com.akilisha.oss.roya.workflow.examples;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;
import com.akilisha.oss.roya.workflow.edges.Edge;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import com.akilisha.oss.roya.workflow.nodes.LLMNode;
import com.akilisha.oss.roya.workflow.nodes.TransformNode;
import com.akilisha.oss.roya.workflow.retry.RetryPolicy;
import com.akilisha.oss.roya.workflow.visitor.LoggingVisitor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Example: AI customer support agent workflow
 * <p>
 * Flow:
 * 1. User submits a support request (trigger)
 * 2. LLM classifies the intent
 * 3. Extract intent from response (transform)
 * 4. Route based on intent:
 * - billing -> fetch billing data
 * - technical -> search documentation
 * - general -> generic response
 * 5. Generate final response with LLM
 */
public class CustomerSupportWorkflow {

    public static void main(String[] args) {
        // Build the workflow
        Workflow workflow = Workflow.create()

                // ===== Define Vertices =====

                // Trigger: webhook receives support request
                .trigger("webhookTrigger", new WebhookTriggerNode())

                // Action: Classify user intent with LLM
                .action("classifyIntent", new LLMNode(
                        "anthropic",
                        "claude-sonnet-4",
                        "You are a customer support classifier. Classify intent as: billing, technical, or general. Respond with just the category."
                ), Duration.ofSeconds(30))

                // Logic: Extract intent from LLM response
                .logic("extractIntent", TransformNode.create(data -> {
                    String response = (String) data.get("response");
                    String intent = response.toLowerCase().trim();
                    return Map.of(
                            "intent", intent,
                            "originalMessage", data.get("message")
                    );
                }))

                // Action: Fetch billing data
                .action("fetchBillingData", new BillingDataNode())

                // Action: Search technical docs
                .action("searchDocs", new DocumentSearchNode())

                // Action: Generate final response
                .action("generateResponse", new LLMNode(
                        "anthropic",
                        "claude-sonnet-4",
                        "You are a helpful customer support agent. Use the provided context to answer the customer's question."
                ), Duration.ofSeconds(45))

                // Action: Send response to customer
                .action("sendResponse", new SendEmailNode())

                // ===== Define Edges =====

                // Webhook -> Classify Intent
                .edge("webhookTrigger", "classifyIntent", Edge.sequential()
                        .withRetry(RetryPolicy.exponentialBackoff(3, Duration.ofSeconds(1)))
                )

                // Classify -> Extract Intent
                .edge("classifyIntent", "extractIntent")

                // Conditional routing based on intent
                .edge("extractIntent", "fetchBillingData",
                        Edge.when(ctx -> "billing".equals(ctx.get("intent")))
                )

                .edge("extractIntent", "searchDocs",
                        Edge.when(ctx -> "technical".equals(ctx.get("intent")))
                )

                .edge("extractIntent", "generateResponse",
                        Edge.when(ctx -> "general".equals(ctx.get("intent")))
                )

                // Data fetching -> Generate response
                .edge("fetchBillingData", "generateResponse")
                .edge("searchDocs", "generateResponse")

                // Generate -> Send (with retry for email delivery)
                .edge("generateResponse", "sendResponse", Edge.sequential()
                        .withRetry(RetryPolicy.fixedDelay(5, Duration.ofSeconds(2)))
                )

                .build();

        // Create executor with logging
        WorkflowExecutor executor = new WorkflowExecutor(workflow)
                .addVisitor(new LoggingVisitor());

        // Execute workflow
        System.out.println("Starting customer support workflow...");

        WorkflowResult result = executor.executeFrom(
                "webhookTrigger",
                Map.of(
                        "customerEmail", "user@example.com",
                        "message", "I can't access my invoice from last month",
                        "customerId", "CUST-12345"
                )
        ).join();

        // Print results
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Workflow Result: " + result);
        System.out.println("=".repeat(50));

        if (result.isSuccess()) {
            System.out.println("✓ Workflow completed successfully!");
            System.out.println("\nExecution Trace:");
            result.getTrace().forEach(event ->
                    System.out.println("  " + event)
            );
        } else {
            System.out.println("✗ Workflow failed!");
            result.finalOutput().error().ifPresent(error ->
                    System.out.println("Error: " + error)
            );
        }

        // Cleanup
        executor.shutdown();
    }

    // ===== Mock Node Implementations =====

    static class WebhookTriggerNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            // In real implementation, this would listen for webhooks
            return CompletableFuture.completedFuture(
                    NodeOutput.success(input.data())
            );
        }
    }

    static class BillingDataNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            // Mock fetching billing data
            String customerId = input.getString("customerId");
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of(
                            "billingData", "Invoice #12345: $99.99 - Paid on 2024-10-15",
                            "customerId", customerId
                    ))
            );
        }
    }

    static class DocumentSearchNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            // Mock searching documentation
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of(
                            "documentation", "Here are the relevant docs for your technical issue..."
                    ))
            );
        }
    }

    static class SendEmailNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            String email = input.getString("customerEmail");
            String response = input.getString("response");

            System.out.println("\n📧 Sending email to: " + email);
            System.out.println("Response: " + response);

            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("sent", true))
            );
        }
    }
}
