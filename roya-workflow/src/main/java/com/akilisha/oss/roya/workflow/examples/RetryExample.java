package com.akilisha.oss.roya.workflow.examples;

import com.akilisha.oss.roya.workflow.core.*;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import com.akilisha.oss.roya.workflow.visitor.LoggingVisitor;

import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates RETRY LOGIC with exponential backoff
 */
public class RetryExample {

//    public static void main(String[] args) {
//        System.out.println("=== Retry Example ===\n");
//
//        // Example 1: Basic retry
//        System.out.println("Example 1: Basic Retry (3 attempts)\n");
//        runBasicRetry();
//
//        System.out.println("\n" + "=".repeat(60) + "\n");
//
//        // Example 2: Exponential backoff
//        System.out.println("Example 2: Exponential Backoff\n");
//        runExponentialBackoff();
//
//        System.out.println("\n" + "=".repeat(60) + "\n");
//
//        // Example 3: Custom retry strategy
//        System.out.println("Example 3: Custom Retry Strategy\n");
//        runCustomRetry();
//    }
//
//    private static void runBasicRetry() {
//        RetryConfig retryConfig = RetryConfig.builder()
//            .maxAttempts(3)
//            .initialDelay(Duration.ofMillis(500))
//            .build();
//
//        Workflow workflow = Workflow.create()
//            .trigger("start", new InputNode())
//            .action("flaky", new FlakyNode(0.6), retryConfig)
//            .edge("start", "flaky")
//            .build();
//
//        WorkflowExecutor executor = new WorkflowExecutor(workflow)
//            .addVisitor(new LoggingVisitor());
//
//        WorkflowResult result = executor.executeFrom("start", Map.of()).join();
//
//        System.out.println("\nResult: " + (result.isSuccess() ? "✅ Success" : "❌ Failed"));
//
//        executor.shutdown();
//    }
//
//    private static void runExponentialBackoff() {
//        RetryConfig retryConfig = RetryConfig.builder()
//            .maxAttempts(5)
//            .initialDelay(Duration.ofMillis(100))
//            .maxDelay(Duration.ofSeconds(5))
//            .backoffMultiplier(2.0)
//            .build();
//
//        Workflow workflow = Workflow.create()
//            .trigger("start", new InputNode())
//            .action("unstable", new UnstableNode(4), retryConfig)
//            .edge("start", "unstable")
//            .build();
//
//        WorkflowExecutor executor = new WorkflowExecutor(workflow)
//            .addVisitor(new LoggingVisitor());
//
//        WorkflowResult result = executor.executeFrom("start", Map.of()).join();
//
//        System.out.println("\nResult: " + (result.isSuccess() ? "✅ Success" : "❌ Failed"));
//
//        executor.shutdown();
//    }
//
//    private static void runCustomRetry() {
//        RetryConfig retryConfig = RetryConfig.builder()
//            .maxAttempts(4)
//            .initialDelay(Duration.ofMillis(200))
//            .retryPredicate(output -> {
//                // Only retry on timeout errors
//                return output.error()
//                    .map(err -> err.contains("timeout") || err.contains("rate limit"))
//                    .orElse(false);
//            })
//            .build();
//
//        Workflow workflow = Workflow.create()
//            .trigger("start", new InputNode())
//            .action("selective", new SelectiveFailureNode(), retryConfig)
//            .edge("start", "selective")
//            .build();
//
//        WorkflowExecutor executor = new WorkflowExecutor(workflow)
//            .addVisitor(new LoggingVisitor());
//
//        WorkflowResult result = executor.executeFrom("start", Map.of()).join();
//
//        System.out.println("\nResult: " + (result.isSuccess() ? "✅ Success" : "❌ Failed"));
//
//        executor.shutdown();
//    }
//
//    static class InputNode implements WorkflowNode {
//        @Override
//        public CompletableFuture<NodeOutput> execute(NodeInput input) {
//            return CompletableFuture.completedFuture(NodeOutput.success(input.data()));
//        }
//    }
//
//    static class FlakyNode implements WorkflowNode {
//        private final double failureRate;
//        private final Random random = new Random();
//        private final AtomicInteger attempts = new AtomicInteger(0);
//
//        public FlakyNode(double failureRate) {
//            this.failureRate = failureRate;
//        }
//
//        @Override
//        public CompletableFuture<NodeOutput> execute(NodeInput input) {
//            int attempt = attempts.incrementAndGet();
//            System.out.println("  Attempt " + attempt + "...");
//
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//
//            if (random.nextDouble() < failureRate) {
//                System.out.println("    ❌ Failed!");
//                return CompletableFuture.completedFuture(
//                    NodeOutput.failure("Service temporarily unavailable")
//                );
//            } else {
//                System.out.println("    ✅ Success!");
//                return CompletableFuture.completedFuture(
//                    NodeOutput.success(Map.of("result", "Data retrieved"))
//                );
//            }
//        }
//    }
//
//    static class UnstableNode implements WorkflowNode {
//        private final AtomicInteger attempts = new AtomicInteger(0);
//        private final int successAfter;
//
//        public UnstableNode(int successAfter) {
//            this.successAfter = successAfter;
//        }
//
//        @Override
//        public CompletableFuture<NodeOutput> execute(NodeInput input) {
//            int attempt = attempts.incrementAndGet();
//            System.out.println("  Attempt " + attempt + " (will succeed after " + successAfter + ")");
//
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//
//            if (attempt < successAfter) {
//                System.out.println("    ❌ Failed (not ready yet)");
//                return CompletableFuture.completedFuture(
//                    NodeOutput.failure("Service initializing...")
//                );
//            } else {
//                System.out.println("    ✅ Success (service ready!)");
//                return CompletableFuture.completedFuture(
//                    NodeOutput.success(Map.of("result", "Service operational"))
//                );
//            }
//        }
//    }
//
//    static class SelectiveFailureNode implements WorkflowNode {
//        private final AtomicInteger attempts = new AtomicInteger(0);
//        private final String[] errors = {
//            "timeout",
//            "rate limit exceeded",
//            "invalid request", // Won't retry this
//            "timeout"
//        };
//
//        @Override
//        public CompletableFuture<NodeOutput> execute(NodeInput input) {
//            int attempt = attempts.getAndIncrement();
//
//            if (attempt < errors.length) {
//                String error = errors[attempt];
//                System.out.println("  Attempt " + (attempt + 1) + ": Error - " + error);
//                return CompletableFuture.completedFuture(
//                    NodeOutput.failure(error)
//                );
//            } else {
//                System.out.println("  Attempt " + (attempt + 1) + ": Success");
//                return CompletableFuture.completedFuture(
//                    NodeOutput.success(Map.of("result", "Success"))
//                );
//            }
//        }
//    }
}
