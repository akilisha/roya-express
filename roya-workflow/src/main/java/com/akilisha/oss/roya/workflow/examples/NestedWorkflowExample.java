package com.akilisha.oss.roya.workflow.examples;

import com.akilisha.oss.roya.workflow.core.*;
import com.akilisha.oss.roya.workflow.edges.Edge;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import com.akilisha.oss.roya.workflow.nested.*;
import com.akilisha.oss.roya.workflow.visitor.LoggingVisitor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Example demonstrating NESTED WORKFLOWS (Parallel Execution).
 *
 * Use Case: AI customer support agent gathering context from multiple sources:
 * - Billing database
 * - Support ticket history
 * - Knowledge base
 *
 * All three sources are queried in parallel, then results are aggregated
 * before generating a response.
 */
public class NestedWorkflowExample {

    public static void main(String[] args) {
        System.out.println("=== Nested Workflow Example ===\n");

        // Child Workflow 1: Check billing information
        Workflow billingWorkflow = Workflow.create()
            .trigger("start", new BillingCheckNode())
            .action("format", new BillingFormatterNode())
            .edge("start", "format")
            .build();

        // Child Workflow 2: Search support history
        Workflow historyWorkflow = Workflow.create()
            .trigger("start", new HistorySearchNode())
            .action("summarize", new HistorySummarizerNode())
            .edge("start", "summarize")
            .build();

        // Child Workflow 3: Search knowledge base
        Workflow knowledgeWorkflow = Workflow.create()
            .trigger("start", new KnowledgeSearchNode())
            .action("rank", new RelevanceRankerNode())
            .edge("start", "rank")
            .build();

        // Main workflow with nested parallel execution
        Workflow main = Workflow.create()
            .trigger("ticket", input -> {
                System.out.println("🎫 Support ticket received from: " + input.getString("customerEmail"));
                System.out.println("   Issue: " + input.getString("issue"));
                return CompletableFuture.completedFuture(
                    NodeOutput.success(input.data())
                );
            })

            // Execute all three child workflows in parallel
            .nested("gatherContext",
                List.of(billingWorkflow, historyWorkflow, knowledgeWorkflow),
                new MergeAllAggregator(true), // Include metadata
                NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT // Continue even if one fails
            )

            .action("generateResponse", new ResponseGeneratorNode())

            .edge("ticket", "gatherContext")
            .edge("gatherContext", "generateResponse")
            .build();

        // Execute
        WorkflowExecutor executor = new WorkflowExecutor(main)
            .addVisitor(new LoggingVisitor());

        WorkflowResult result = executor.executeFrom(
            "ticket",
            Map.of(
                "customerEmail", "user@example.com",
                "customerId", "CUST-12345",
                "issue", "Can't access my invoice from last month"
            )
        ).join();

        // Display results
        System.out.println("\n" + "=".repeat(50));
        if (result.isSuccess()) {
            System.out.println("✅ Support workflow completed!");
            System.out.println("\n📊 Context Gathered:");
            System.out.println("   • Billing: " + result.context().get("billingInfo"));
            System.out.println("   • History: " + result.context().get("pastIssues"));
            System.out.println("   • Knowledge: " + result.context().get("relevantArticles"));
            System.out.println("\n💬 Response: " + result.context().get("response"));
        } else {
            System.out.println("❌ Workflow failed!");
        }
        System.out.println("=".repeat(50));

        executor.shutdown();
    }

    // Billing workflow nodes
    static class BillingCheckNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("💳 Checking billing for customer: " + input.getString("customerId"));
            try {
                Thread.sleep(300); // Simulate DB query
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return CompletableFuture.completedFuture(
                NodeOutput.success(Map.of(
                    "billingStatus", "current",
                    "lastInvoice", "INV-2024-10-15",
                    "amount", "$99.99"
                ))
            );
        }
    }

    static class BillingFormatterNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            String formatted = String.format("Last invoice: %s for %s (%s)",
                input.context().get("lastInvoice"),
                input.context().get("amount"),
                input.context().get("billingStatus"));
            return CompletableFuture.completedFuture(
                NodeOutput.success(Map.of("billingInfo", formatted))
            );
        }
    }

    // History workflow nodes
    static class HistorySearchNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("📜 Searching support history...");
            try {
                Thread.sleep(400); // Simulate search
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return CompletableFuture.completedFuture(
                NodeOutput.success(Map.of(
                    "previousTickets", 3,
                    "lastIssue", "Account access problem"
                ))
            );
        }
    }

    static class HistorySummarizerNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            String summary = String.format("Customer has %d previous tickets. Last issue: %s",
                input.context().get("previousTickets"),
                input.context().get("lastIssue"));
            return CompletableFuture.completedFuture(
                NodeOutput.success(Map.of("pastIssues", summary))
            );
        }
    }

    // Knowledge base workflow nodes
    static class KnowledgeSearchNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("📚 Searching knowledge base...");
            try {
                Thread.sleep(250); // Simulate vector search
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return CompletableFuture.completedFuture(
                NodeOutput.success(Map.of(
                    "articles", List.of(
                        "How to access invoices",
                        "Invoice download troubleshooting"
                    )
                ))
            );
        }
    }

    static class RelevanceRankerNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(
                NodeOutput.success(Map.of("relevantArticles", "2 articles found"))
            );
        }
    }

    // Response generator
    static class ResponseGeneratorNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("🤖 Generating AI response with gathered context...");
            try {
                Thread.sleep(500); // Simulate LLM call
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            String response = String.format(
                "I can help you with your invoice. I see your %s is available. " +
                "You've had %s before. Here are some relevant help articles: %s",
                input.context().get("billingInfo"),
                input.context().get("pastIssues"),
                input.context().get("relevantArticles")
            );

            return CompletableFuture.completedFuture(
                NodeOutput.success(Map.of("response", response))
            );
        }
    }
}
