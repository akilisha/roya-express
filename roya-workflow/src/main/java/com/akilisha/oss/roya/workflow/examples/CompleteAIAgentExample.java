package com.akilisha.oss.roya.workflow.examples;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;
import com.akilisha.oss.roya.workflow.cost.CostTracker;
import com.akilisha.oss.roya.workflow.edges.Edge;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import com.akilisha.oss.roya.workflow.hitm.ApprovalProvider;
import com.akilisha.oss.roya.workflow.hitm.HumanApprovalNode;
import com.akilisha.oss.roya.workflow.hitm.PollingApprovalProvider;
import com.akilisha.oss.roya.workflow.nested.MergeAllAggregator;
import com.akilisha.oss.roya.workflow.nested.NestedExecutionStrategy;
import com.akilisha.oss.roya.workflow.resilience.CircuitBreaker;
import com.akilisha.oss.roya.workflow.resilience.CircuitBreakerNode;
import com.akilisha.oss.roya.workflow.visitor.LoggingVisitor;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Complete AI Agent Example - Kitchen Sink
 * <p>
 * Combines ALL features:
 * - Continuation workflows (sequential composition)
 * - Nested workflows (parallel execution)
 * - Human-in-the-loop (approval)
 * - Cost tracking (budget management)
 * - Circuit breaker (resilience)
 * - Retry logic (fault tolerance)
 * - Conditional edges (routing)
 * <p>
 * Use Case: AI-powered customer support agent
 */
public class CompleteAIAgentExample {

    public static void main(String[] args) {
        System.out.println("=== Complete AI Agent: Customer Support ===\n");

        // Setup infrastructure
        ApprovalProvider approvalProvider = new PollingApprovalProvider(
                Duration.ofSeconds(1),
                Duration.ofMinutes(5)
        );

        CircuitBreaker llmBreaker = CircuitBreaker.withThreshold(3, Duration.ofSeconds(5));
        CircuitBreaker dbBreaker = CircuitBreaker.withThreshold(5, Duration.ofSeconds(3));

        CostTracker costTracker = new CostTracker(1.00) // $1 budget
                .withNodeCost("classify", 0.001)
                .withNodeCost("generateResponse", 0.005)
                .withNodeCost("refine", 0.002);

        // Build nested workflows for data gathering
        Workflow billingCheck = createBillingWorkflow(dbBreaker);
        Workflow historySearch = createHistoryWorkflow(dbBreaker);
        Workflow knowledgeSearch = createKnowledgeWorkflow();

        // Build analysis workflow
        Workflow analysisWorkflow = createAnalysisWorkflow(llmBreaker);

        // Main workflow
        Workflow mainWorkflow = Workflow.create()
                .trigger("ticket", new TicketInputNode())

                // Step 1: Classify urgency (with circuit breaker)
                .action("classify",
                        new CircuitBreakerNode(
                                new UrgencyClassifierNode(),
                                llmBreaker
                        )
                )

                // Step 2: Route based on urgency
                .action("routeUrgent", new UrgentRouteNode())
                .action("routeNormal", new NormalRouteNode())

                // Step 3: Gather context in parallel (nested workflows)
                .nested("gatherContext",
                        List.of(billingCheck, historySearch, knowledgeSearch),
                        new MergeAllAggregator(true),
                        NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT
                )

                // Step 4: Analyze with continuation workflow
                .continuation("analyze", analysisWorkflow, "start")

                // Step 5: Generate response (with circuit breaker)
                .action("generateResponse",
                        new CircuitBreakerNode(
                                new ResponseGeneratorNode(),
                                llmBreaker
                        )
                )

                // Step 6: Human approval for high-urgency tickets
                .action("humanApproval", new HumanApprovalNode(
                        approvalProvider,
                        "Approve response for urgent ticket?"
                ))

                // Step 7: Send response
                .action("send", new SendResponseNode())

                // Edges with conditions
                .edge("ticket", "classify")
                .edge("classify", "routeUrgent", Edge.when(ctx ->
                        "urgent".equals(ctx.get("urgency"))
                ))
                .edge("classify", "routeNormal", Edge.when(ctx ->
                        !"urgent".equals(ctx.get("urgency"))
                ))
                .edge("routeUrgent", "gatherContext")
                .edge("routeNormal", "gatherContext")
                .edge("gatherContext", "analyze")
                .edge("analyze", "generateResponse")
                .edge("generateResponse", "humanApproval", Edge.when(ctx ->
                        "urgent".equals(ctx.get("urgency"))
                ))
                .edge("generateResponse", "send", Edge.when(ctx ->
                        !"urgent".equals(ctx.get("urgency"))
                ))
                .edge("humanApproval", "send")

                .build();

        // Execute workflow
        WorkflowExecutor executor = new WorkflowExecutor(mainWorkflow)
                .addVisitor(costTracker)
                .addVisitor(new LoggingVisitor());

        // Test ticket
        Map<String, Object> ticket = Map.of(
                "customerId", "CUST-12345",
                "customerEmail", "user@example.com",
                "issue", "Cannot access my account after password reset",
                "urgencyLevel", "high"
        );

        // Execute workflow in background
        CompletableFuture<WorkflowResult> futureResult =
                executor.executeFrom("ticket", ticket);

        // Simulate human approval after 2 seconds
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("\n👤 Human approving request...");

                // Find pending approval requests
                if (approvalProvider instanceof PollingApprovalProvider) {
                    PollingApprovalProvider pollingProvider =
                            (PollingApprovalProvider) approvalProvider;

                    pollingProvider.getPendingRequests().keySet().forEach(requestId -> {
                        pollingProvider.submitApproval(requestId, true);
                        System.out.println("✅ Approved request: " + requestId);
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        // Wait for result
        WorkflowResult result = futureResult.join();

        // Display results
        System.out.println("\n" + "=".repeat(60));
        if (result.isSuccess()) {
            System.out.println("✅ Customer support workflow completed!");
            System.out.println("\n📊 Summary:");
            System.out.println("   Urgency: " + result.context().get("urgency"));
            System.out.println("   Billing: " + result.context().get("billingStatus"));
            System.out.println("   History: " + result.context().get("previousTickets"));
            System.out.println("   Response: " + result.context().get("finalResponse"));
            System.out.println("   Sent: " + result.context().get("sent"));
        } else {
            System.out.println("❌ Workflow failed!");
        }

        // Cost report
        System.out.println("\n💰 Cost Report:");
        System.out.println("   " + costTracker.getReport());
        System.out.println("   Budget remaining: $" +
                String.format("%.4f", costTracker.getRemainingBudget()));

        // Circuit breaker stats
        System.out.println("\n🔌 Circuit Breaker Stats:");
        System.out.println("   LLM: " + llmBreaker.getStats());
        System.out.println("   Database: " + dbBreaker.getStats());

        System.out.println("=".repeat(60));

        executor.shutdown();
        if (approvalProvider instanceof PollingApprovalProvider) {
            ((PollingApprovalProvider) approvalProvider).shutdown();
        }
    }

    private static Workflow createBillingWorkflow(CircuitBreaker breaker) {
        return Workflow.create()
                .trigger("start", new CircuitBreakerNode(
                        new BillingQueryNode(),
                        breaker
                ))
                .action("format", new BillingFormatNode())
                .edge("start", "format")
                .build();
    }

    private static Workflow createHistoryWorkflow(CircuitBreaker breaker) {
        return Workflow.create()
                .trigger("start", new CircuitBreakerNode(
                        new HistoryQueryNode(),
                        breaker
                ))
                .action("summarize", new HistorySummaryNode())
                .edge("start", "summarize")
                .build();
    }

    private static Workflow createKnowledgeWorkflow() {
        return Workflow.create()
                .trigger("start", new KnowledgeSearchNode())
                .action("rank", new RelevanceRankNode())
                .edge("start", "rank")
                .build();
    }

    private static Workflow createAnalysisWorkflow(CircuitBreaker breaker) {
        return Workflow.create()
                .trigger("start", new CircuitBreakerNode(
                        new SentimentAnalysisNode(),
                        breaker
                ))
                .action("refine", new ContextRefinementNode())
                .edge("start", "refine")
                .build();
    }

    // ===== Main Workflow Nodes =====

    static class TicketInputNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("🎫 Ticket received:");
            System.out.println("   Customer: " + input.getString("customerEmail"));
            System.out.println("   Issue: " + input.getString("issue"));
            return CompletableFuture.completedFuture(NodeOutput.success(input.data()));
        }
    }

    static class UrgencyClassifierNode implements WorkflowNode {
        private final Random random = new Random();

        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("🔍 Classifying urgency...");

            // Simulate occasional failure
            if (random.nextDouble() < 0.2) {
                return CompletableFuture.completedFuture(
                        NodeOutput.failure("Classification timeout")
                );
            }

            String urgencyLevel = input.getString("urgencyLevel");
            String urgency = urgencyLevel.equals("high") ? "urgent" : "normal";

            System.out.println("   ➡️  Classified as: " + urgency);
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("urgency", urgency))
            );
        }
    }

    static class UrgentRouteNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("🚨 Routing as URGENT");
            return CompletableFuture.completedFuture(NodeOutput.success(Map.of()));
        }
    }

    static class NormalRouteNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("📋 Routing as NORMAL");
            return CompletableFuture.completedFuture(NodeOutput.success(Map.of()));
        }
    }

    static class ResponseGeneratorNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("🤖 Generating AI response...");

            String response = String.format(
                    "Based on your %s status and %s, we can help reset your account access.",
                    input.context().get("billingStatus"),
                    input.context().get("sentiment")
            );

            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("finalResponse", response))
            );
        }
    }

    static class SendResponseNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("📧 Sending response to customer...");
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("sent", true))
            );
        }
    }

    // ===== Billing Workflow Nodes =====

    static class BillingQueryNode implements WorkflowNode {
        private final Random random = new Random();

        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("   💳 Querying billing database...");

            if (random.nextDouble() < 0.15) {
                return CompletableFuture.completedFuture(
                        NodeOutput.failure("Database connection timeout")
                );
            }

            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of(
                            "billingStatus", "active",
                            "plan", "premium"
                    ))
            );
        }
    }

    static class BillingFormatNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(NodeOutput.success(Map.of()));
        }
    }

    // ===== History Workflow Nodes =====

    static class HistoryQueryNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("   📜 Querying ticket history...");
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("previousTickets", 2))
            );
        }
    }

    static class HistorySummaryNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(NodeOutput.success(Map.of()));
        }
    }

    // ===== Knowledge Workflow Nodes =====

    static class KnowledgeSearchNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("   📚 Searching knowledge base...");
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("articles", 3))
            );
        }
    }

    static class RelevanceRankNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(NodeOutput.success(Map.of()));
        }
    }

    // ===== Analysis Workflow Nodes =====

    static class SentimentAnalysisNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("   😊 Analyzing sentiment...");
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("sentiment", "frustrated"))
            );
        }
    }

    static class ContextRefinementNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(NodeOutput.success(Map.of()));
        }
    }
}
