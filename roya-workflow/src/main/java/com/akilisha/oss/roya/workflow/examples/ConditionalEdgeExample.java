package com.akilisha.oss.roya.workflow.examples;

import com.akilisha.oss.roya.workflow.core.*;
import com.akilisha.oss.roya.workflow.edges.Edge;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Demonstrates CONDITIONAL EDGES for workflow routing
 */
public class ConditionalEdgeExample {

    public static void main(String[] args) {
        System.out.println("=== Conditional Edge Example ===\n");

        // Example 1: Simple condition
        System.out.println("Example 1: Simple Routing\n");
        runSimpleRouting();

        System.out.println("\n" + "=".repeat(60) + "\n");

        // Example 2: Multi-way routing
        System.out.println("Example 2: Multi-Way Routing\n");
        runMultiWayRouting();

        System.out.println("\n" + "=".repeat(60) + "\n");

        // Example 3: Priority-based routing
        System.out.println("Example 3: Priority-Based Routing\n");
        runPriorityRouting();
    }

    private static void runSimpleRouting() {
        Workflow workflow = Workflow.create()
            .trigger("input", new InputNode())
            .action("classify", new SentimentClassifier())

            // Route based on sentiment
            .action("positive", new PositiveResponseNode())
            .action("negative", new NegativeResponseNode())

            .edge("input", "classify")
            .edge("classify", "positive", Edge.when(ctx ->
                "positive".equals(ctx.get("sentiment"))
            ))
            .edge("classify", "negative", Edge.when(ctx ->
                "negative".equals(ctx.get("sentiment"))
            ))
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        // Test positive
        System.out.println("Test 1: Positive sentiment");
        WorkflowResult result1 = executor.executeFrom("input",
            Map.of("text", "I love this product!")).join();
        System.out.println("Response: " + result1.context().get("response") + "\n");

        // Test negative
        System.out.println("Test 2: Negative sentiment");
        WorkflowResult result2 = executor.executeFrom("input",
            Map.of("text", "This is terrible.")).join();
        System.out.println("Response: " + result2.context().get("response"));

        executor.shutdown();
    }

    private static void runMultiWayRouting() {
        Workflow workflow = Workflow.create()
            .trigger("ticket", new TicketInputNode())
            .action("categorize", new CategoryClassifier())

            // Multiple routing paths
            .action("billing", new BillingHandler())
            .action("technical", new TechnicalHandler())
            .action("sales", new SalesHandler())
            .action("general", new GeneralHandler())

            .edge("ticket", "categorize")
            .edge("categorize", "billing", Edge.when(ctx ->
                "billing".equals(ctx.get("category"))
            ))
            .edge("categorize", "technical", Edge.when(ctx ->
                "technical".equals(ctx.get("category"))
            ))
            .edge("categorize", "sales", Edge.when(ctx ->
                "sales".equals(ctx.get("category"))
            ))
            .edge("categorize", "general", Edge.when(ctx ->
                ctx.get("category") == null || "general".equals(ctx.get("category"))
            ))
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        String[] issues = {
            "I can't access my invoice",
            "The software keeps crashing",
            "I want to upgrade my plan",
            "How do I reset my password?"
        };

        for (String issue : issues) {
            System.out.println("Issue: " + issue);
            WorkflowResult result = executor.executeFrom("ticket",
                Map.of("issue", issue)).join();
            System.out.println("Category: " + result.context().get("category"));
            System.out.println("Response: " + result.context().get("response") + "\n");
        }

        executor.shutdown();
    }

    private static void runPriorityRouting() {
        Workflow workflow = Workflow.create()
            .trigger("request", new RequestInputNode())
            .action("assess", new PriorityAssessor())

            // Priority-based routing
            .action("urgent", new UrgentHandler())
            .action("high", new HighPriorityHandler())
            .action("normal", new NormalHandler())
            .action("low", new LowPriorityHandler())

            .edge("request", "assess")
            .edge("assess", "urgent", Edge.when(ctx -> {
                Integer priority = (Integer) ctx.get("priority");
                return priority != null && priority >= 90;
            }))
            .edge("assess", "high", Edge.when(ctx -> {
                Integer priority = (Integer) ctx.get("priority");
                return priority != null && priority >= 70 && priority < 90;
            }))
            .edge("assess", "normal", Edge.when(ctx -> {
                Integer priority = (Integer) ctx.get("priority");
                return priority != null && priority >= 40 && priority < 70;
            }))
            .edge("assess", "low", Edge.when(ctx -> {
                Integer priority = (Integer) ctx.get("priority");
                return priority != null && priority < 40;
            }))
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        int[] priorities = {95, 75, 50, 20};

        for (int priority : priorities) {
            System.out.println("Request with priority: " + priority);
            WorkflowResult result = executor.executeFrom("request",
                Map.of("requestPriority", priority)).join();
            System.out.println("Handler: " + result.context().get("handler"));
            System.out.println("SLA: " + result.context().get("sla") + "\n");
        }

        executor.shutdown();
    }

    // ===== Nodes =====

    static class InputNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("Input: " + input.getString("text"));
            return CompletableFuture.completedFuture(NodeOutput.success(input.data()));
        }
    }

    static class SentimentClassifier implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            String text = input.getString("text").toLowerCase();
            String sentiment = text.contains("love") || text.contains("great") ?
                "positive" : "negative";

            System.out.println("Classified as: " + sentiment);
            return CompletableFuture.completedFuture(
                NodeOutput.success(Map.of("sentiment", sentiment))
            );
        }
    }

    static class PositiveResponseNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            String response = "Thank you for the positive feedback! 😊";
            return CompletableFuture.completedFuture(
                NodeOutput.success(Map.of("response", response))
            );
        }
    }

    static class NegativeResponseNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            String response = "We're sorry to hear that. We'll work to improve.";
            return CompletableFuture.completedFuture(
                NodeOutput.success(Map.of("response", response))
            );
        }
    }

    static class TicketInputNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(NodeOutput.success(input.data()));
        }
    }

    static class CategoryClassifier implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            String issue = input.getString("issue").toLowerCase();

            String category;
            if (issue.contains("invoice") || issue.contains("payment") || issue.contains("bill")) {
                category = "billing";
            } else if (issue.contains("crash") || issue.contains("error") || issue.contains("bug")) {
                category = "technical";
            } else if (issue.contains("upgrade") || issue.contains("plan") || issue.contains("pricing")) {
                category = "sales";
            } else {
                category = "general";
            }

            return CompletableFuture.completedFuture(
                NodeOutput.success(Map.of("category", category))
            );
        }
    }

    static class BillingHandler implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(NodeOutput.success(Map.of(
                "response", "Billing team will assist with your invoice."
            )));
        }
    }

    static class TechnicalHandler implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(NodeOutput.success(Map.of(
                "response", "Technical support will investigate the crash."
            )));
        }
    }

    static class SalesHandler implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(NodeOutput.success(Map.of(
                "response", "Sales team will discuss upgrade options."
            )));
        }
    }

    static class GeneralHandler implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(NodeOutput.success(Map.of(
                "response", "Our support team will help you reset your password."
            )));
        }
    }

    static class RequestInputNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(NodeOutput.success(input.data()));
        }
    }

    static class PriorityAssessor implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            int priority = input.getInt("requestPriority");
            return CompletableFuture.completedFuture(
                NodeOutput.success(Map.of("priority", priority))
            );
        }
    }

    static class UrgentHandler implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(NodeOutput.success(Map.of(
                "handler", "Urgent Handler",
                "sla", "15 minutes"
            )));
        }
    }

    static class HighPriorityHandler implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(NodeOutput.success(Map.of(
                "handler", "High Priority Handler",
                "sla", "1 hour"
            )));
        }
    }

    static class NormalHandler implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(NodeOutput.success(Map.of(
                "handler", "Normal Handler",
                "sla", "4 hours"
            )));
        }
    }

    static class LowPriorityHandler implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(NodeOutput.success(Map.of(
                "handler", "Low Priority Handler",
                "sla", "24 hours"
            )));
        }
    }
}
