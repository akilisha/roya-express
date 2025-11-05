package com.akilisha.oss.roya.workflow.examples;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;
import com.akilisha.oss.roya.workflow.cost.BudgetExceededException;
import com.akilisha.oss.roya.workflow.cost.CostTracker;
import com.akilisha.oss.roya.workflow.cost.NodeCostCalculator;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import com.akilisha.oss.roya.workflow.visitor.LoggingVisitor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Example demonstrating COST TRACKING.
 * <p>
 * Use Case: AI content generation workflow with multiple LLM calls.
 * Track and control costs across:
 * - Text embedding ($0.0001 per call)
 * - Classification ($0.001 per call)
 * - Content generation ($0.005 per call)
 * - Summary generation ($0.002 per call)
 * <p>
 * Demonstrates:
 * 1. Setting budget limits
 * 2. Per-node cost tracking
 * 3. Budget enforcement (strict vs warning mode)
 * 4. Cost reporting
 * <p>
 * Demonstrates:
 * <p>
 * ✅ Setting budget limits ($0.05)
 * ✅ Per-node cost tracking (embedding, classification, generation, summary)
 * ✅ Strict mode - Workflow stops when budget exceeded (throws BudgetExceededException)
 * ✅ Warning mode - Workflow continues but logs warnings
 * ✅ Token-based pricing - Cost calculated from output tokens (realistic LLM pricing)
 * ✅ Multiple cost calculators (fixed, output-based, combined)
 * ✅ Cost reports with node breakdown
 * <p>
 * Realistic Costs:
 * <p>
 * Embedding: $0.0001 per call
 * Classification: $0.001 per call
 * Content Generation: $0.005 per call
 * Summary: $0.002 per call
 * <p>
 * 3 Different Examples:
 * <p>
 * Strict budget enforcement (stops at limit)
 * Warning mode (continues with warnings)
 * Token-based pricing (like OpenAI/Anthropic)
 */
public class CostTrackingExample {

    public static void main(String[] args) {
        System.out.println("=== Cost Tracking Example ===\n");

        // Example 1: Strict budget enforcement
        System.out.println("📊 Example 1: Strict Budget ($0.05 limit)\n");
        runWithStrictBudget();

        System.out.println("\n" + "=".repeat(60) + "\n");

        // Example 2: Warning mode (soft limit)
        System.out.println("📊 Example 2: Warning Mode ($0.05 limit)\n");
        runWithWarningMode();

        System.out.println("\n" + "=".repeat(60) + "\n");

        // Example 3: Output-based cost tracking (token counting)
        System.out.println("📊 Example 3: Token-Based Pricing\n");
        runWithTokenBasedPricing();
    }

    /**
     * Example 1: Strict budget enforcement
     * Workflow stops immediately when budget is exceeded
     */
    private static void runWithStrictBudget() {
        // Create cost tracker with $0.05 budget (strict mode)
        CostTracker costTracker = new CostTracker(0.05) // $0.05 budget
                .withNodeCost("embed", 0.0001)       // $0.0001 per embedding
                .withNodeCost("classify", 0.001)     // $0.001 per classification
                .withNodeCost("generate", 0.005)     // $0.005 per generation
                .withNodeCost("summarize", 0.002);   // $0.002 per summary

        Workflow workflow = Workflow.create()
                .trigger("start", new InputNode())
                .action("embed", new EmbeddingNode())
                .action("classify", new ClassificationNode())
                .action("generate", new ContentGenerationNode())
                .action("summarize", new SummaryNode())
                .edge("start", "embed")
                .edge("embed", "classify")
                .edge("classify", "generate")
                .edge("generate", "summarize")
                .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow)
                .addVisitor(costTracker)
                .addVisitor(new LoggingVisitor());

        try {
            WorkflowResult result = executor.executeFrom(
                    "start",
                    Map.of("content", "Write a blog post about AI")
            ).join();

            if (result.isSuccess()) {
                System.out.println("✅ Workflow completed successfully!");
                System.out.println("   Output: " + result.context().get("finalContent"));
            } else {
                System.out.println("❌ Workflow failed: " + result.finalOutput().error().orElse("Unknown"));
            }
        } catch (Exception e) {
            if (e.getCause() instanceof BudgetExceededException) {
                BudgetExceededException budgetEx = (BudgetExceededException) e.getCause();
                System.out.println("💰 BUDGET EXCEEDED!");
                System.out.println("   Total cost: $" + String.format("%.4f", budgetEx.getTotalCost()));
                System.out.println("   Budget limit: $" + String.format("%.4f", budgetEx.getBudgetLimit()));
                System.out.println("   Overage: $" + String.format("%.4f", budgetEx.getOverage()));
            }
        }

        // Print cost report
        System.out.println("\n💵 Cost Report:");
        CostTracker.CostReport report = costTracker.getReport();
        System.out.println("   " + report);
        System.out.println("   Node breakdown:");
        report.nodeCosts().forEach((nodeId, cost) ->
                System.out.println("      • " + nodeId + ": $" + String.format("%.4f", cost))
        );

        executor.shutdown();
    }

    /**
     * Example 2: Warning mode (soft budget limit)
     * Workflow continues but logs warnings when budget exceeded
     */
    private static void runWithWarningMode() {
        // Create cost tracker with warning mode (non-strict)
        CostTracker costTracker = new CostTracker(
                0.05,                               // $0.05 budget
                NodeCostCalculator.free(),          // Default calculator
                false                               // Warning mode (not strict)
        )
                .withNodeCost("embed", 0.0001)
                .withNodeCost("classify", 0.001)
                .withNodeCost("generate", 0.005)
                .withNodeCost("summarize", 0.002);

        Workflow workflow = Workflow.create()
                .trigger("start", new InputNode())
                .action("embed", new EmbeddingNode())
                .action("classify", new ClassificationNode())
                .action("generate", new ContentGenerationNode())
                .action("summarize", new SummaryNode())
                .edge("start", "embed")
                .edge("embed", "classify")
                .edge("classify", "generate")
                .edge("generate", "summarize")
                .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow)
                .addVisitor(costTracker)
                .addVisitor(new LoggingVisitor());

        WorkflowResult result = executor.executeFrom(
                "start",
                Map.of("content", "Write a blog post about AI")
        ).join();

        if (result.isSuccess()) {
            System.out.println("✅ Workflow completed (with budget warnings)!");
        }

        // Check if budget was exceeded
        if (costTracker.isBudgetExceeded()) {
            System.out.println("\n⚠️  Budget was exceeded but workflow continued");
            System.out.println("   Final cost: $" + String.format("%.4f", costTracker.getTotalCost()));
            System.out.println("   Budget: $0.0500");
            System.out.println("   Overage: $" + String.format("%.4f",
                    costTracker.getTotalCost() - 0.05));
        }

        System.out.println("\n💵 Final Cost Report:");
        System.out.println("   " + costTracker.getReport());
        System.out.println("   Budget utilization: " +
                String.format("%.1f%%", costTracker.getBudgetUtilization()));

        executor.shutdown();
    }

    /**
     * Example 3: Token-based pricing (cost based on output)
     * Cost calculated based on number of tokens generated
     */
    private static void runWithTokenBasedPricing() {
        // Cost calculators based on output tokens
        NodeCostCalculator embeddingCost = NodeCostCalculator.outputBased("tokens", 0.00001);
        NodeCostCalculator generationCost = NodeCostCalculator.combined(
                NodeCostCalculator.fixed(0.001),                    // Base cost
                NodeCostCalculator.outputBased("tokens", 0.00002)   // Per-token cost
        );

        CostTracker costTracker = new CostTracker(1.00) // $1.00 budget
                .withNodeCost("embed", embeddingCost)
                .withNodeCost("generate", generationCost);

        Workflow workflow = Workflow.create()
                .trigger("start", new InputNode())
                .action("embed", new EmbeddingWithTokensNode())
                .action("generate", new GenerationWithTokensNode())
                .edge("start", "embed")
                .edge("embed", "generate")
                .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow)
                .addVisitor(costTracker);

        WorkflowResult result = executor.executeFrom(
                "start",
                Map.of("prompt", "Explain quantum computing in simple terms")
        ).join();

        if (result.isSuccess()) {
            System.out.println("✅ Workflow completed!");
            System.out.println("   Input tokens: " + result.context().get("inputTokens"));
            System.out.println("   Output tokens: " + result.context().get("outputTokens"));
        }

        System.out.println("\n💵 Token-Based Cost Report:");
        System.out.println("   " + costTracker.getReport());
        System.out.println("   Remaining budget: $" +
                String.format("%.4f", costTracker.getRemainingBudget()));

        executor.shutdown();
    }

    // ===== Example Nodes =====

    private static void simulateWork(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static class InputNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("📥 Input: " + input.getString("content"));
            return CompletableFuture.completedFuture(
                    NodeOutput.success(input.data())
            );
        }
    }

    static class EmbeddingNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("🔢 Creating embeddings... (cost: $0.0001)");
            simulateWork(100);
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("embedding", "[0.1, 0.2, ...]"))
            );
        }
    }

    static class ClassificationNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("🏷️  Classifying content... (cost: $0.001)");
            simulateWork(200);
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("category", "technology"))
            );
        }
    }

    static class ContentGenerationNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("✍️  Generating content... (cost: $0.005)");
            simulateWork(500);
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of(
                            "generatedContent",
                            "AI is transforming how we work and live..."
                    ))
            );
        }
    }

    static class SummaryNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("📝 Generating summary... (cost: $0.002)");
            simulateWork(300);
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of(
                            "finalContent",
                            "Summary: AI is changing the world"
                    ))
            );
        }
    }

    static class EmbeddingWithTokensNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            int tokens = 512; // Simulate 512 tokens
            System.out.println("🔢 Creating embeddings... (" + tokens + " tokens)");
            simulateWork(100);
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of(
                            "embedding", "[vectors...]",
                            "tokens", tokens
                    ))
            );
        }
    }

    static class GenerationWithTokensNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            int inputTokens = 50;
            int outputTokens = 1500;
            System.out.println("✍️  Generating content... (" + outputTokens + " tokens)");
            simulateWork(500);
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of(
                            "content", "Quantum computing leverages quantum mechanics...",
                            "inputTokens", inputTokens,
                            "outputTokens", outputTokens,
                            "tokens", outputTokens // For cost calculation
                    ))
            );
        }
    }
}
