package com.akilisha.oss.roya.workflow.examples;

import com.akilisha.oss.roya.workflow.core.*;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import com.akilisha.oss.roya.workflow.retry.RetryPolicy;
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

    public static void main(String[] args) {
        System.out.println("=== Retry Example ===\n");

        // Note: Retry policy is set on nodes via metadata in the workflow
        // This example shows the pattern even though full integration pending

        // Example 1: Simulated retry with manual logic
        System.out.println("Example 1: Manual Retry Pattern\n");
        runManualRetry();

        System.out.println("\n" + "=".repeat(60) + "\n");

        // Example 2: Exponential backoff pattern
        System.out.println("Example 2: Exponential Backoff Pattern\n");
        runExponentialBackoff();

        System.out.println("\n" + "=".repeat(60) + "\n");

        // Example 3: Retry wrapper node
        System.out.println("Example 3: Retry Wrapper Node\n");
        runRetryWrapper();
    }

    private static void runManualRetry() {
        Workflow workflow = Workflow.create()
            .trigger("start", new InputNode())
            .action("flaky", new FlakyNode(0.6))
            .edge("start", "flaky")
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow)
            .addVisitor(new LoggingVisitor());

        // Manual retry loop
        int maxAttempts = 3;
        WorkflowResult result = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            System.out.println("Attempt " + attempt + ":");
            result = executor.executeFrom("start", Map.of()).join();

            if (result.isSuccess()) {
                System.out.println("  ✅ Success!");
                break;
            } else {
                System.out.println("  ❌ Failed");
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(500 * attempt); // Increasing delay
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        System.out.println("\nFinal result: " + (result != null && result.isSuccess() ? "✅ Success" : "❌ Failed"));

        executor.shutdown();
    }

    private static void runExponentialBackoff() {
        RetryPolicy policy = RetryPolicy.exponentialBackoff(5, Duration.ofMillis(100));

        Workflow workflow = Workflow.create()
            .trigger("start", new InputNode())
            .action("unstable", new UnstableNode(4))
            .edge("start", "unstable")
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow)
            .addVisitor(new LoggingVisitor());

        WorkflowResult result = null;
        for (int attempt = 1; attempt <= policy.getMaxAttempts(); attempt++) {
            System.out.println("Attempt " + attempt + ":");
            result = executor.executeFrom("start", Map.of()).join();

            if (result.isSuccess()) {
                System.out.println("  ✅ Success!");
                break;
            } else {
                System.out.println("  ❌ Failed");
                Duration delay = policy.calculateDelay(attempt);
                System.out.println("  Waiting " + delay.toMillis() + "ms before retry...");
                try {
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        System.out.println("\nFinal result: " + (result != null && result.isSuccess() ? "✅ Success" : "❌ Failed"));

        executor.shutdown();
    }

    private static void runRetryWrapper() {
        // Using a retry wrapper node
        Workflow workflow = Workflow.create()
            .trigger("start", new InputNode())
            .action("protected", new RetryWrapperNode(
                new SelectiveFailureNode(),
                3,
                Duration.ofMillis(200)
            ))
            .edge("start", "protected")
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        WorkflowResult result = executor.executeFrom("start", Map.of()).join();

        System.out.println("\nResult: " + (result.isSuccess() ? "✅ Success" : "❌ Failed"));

        executor.shutdown();
    }

    // ===== Helper Nodes =====

    /**
     * Wrapper node that adds retry logic to any node
     */
    static class RetryWrapperNode implements WorkflowNode {
        private final WorkflowNode delegate;
        private final int maxAttempts;
        private final Duration delay;

        public RetryWrapperNode(WorkflowNode delegate, int maxAttempts, Duration delay) {
            this.delegate = delegate;
            this.maxAttempts = maxAttempts;
            this.delay = delay;
        }

        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return retryExecution(input, 1);
        }

        private CompletableFuture<NodeOutput> retryExecution(NodeInput input, int attempt) {
            System.out.println("  Attempt " + attempt + "...");

            return delegate.execute(input).thenCompose(output -> {
                if (output.isSuccess() || attempt >= maxAttempts) {
                    if (output.isSuccess()) {
                        System.out.println("    ✅ Success!");
                    } else {
                        System.out.println("    ❌ Failed (max attempts reached)");
                    }
                    return CompletableFuture.completedFuture(output);
                } else {
                    System.out.println("    ❌ Failed, retrying...");
                    return CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(delay.toMillis());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).thenCompose(v -> retryExecution(input, attempt + 1));
                }
            });
        }
    }

    static class InputNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(NodeOutput.success(input.data()));
        }
    }

    static class FlakyNode implements WorkflowNode {
        private final double failureRate;
        private final Random random = new Random();
        private final AtomicInteger attempts = new AtomicInteger(0);

        public FlakyNode(double failureRate) {
            this.failureRate = failureRate;
        }

        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            int attempt = attempts.incrementAndGet();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (random.nextDouble() < failureRate) {
                return CompletableFuture.completedFuture(
                    NodeOutput.failure("Service temporarily unavailable")
                );
            } else {
                return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("result", "Data retrieved"))
                );
            }
        }
    }

    static class UnstableNode implements WorkflowNode {
        private final AtomicInteger attempts = new AtomicInteger(0);
        private final int successAfter;

        public UnstableNode(int successAfter) {
            this.successAfter = successAfter;
        }

        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            int attempt = attempts.incrementAndGet();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (attempt < successAfter) {
                return CompletableFuture.completedFuture(
                    NodeOutput.failure("Service initializing...")
                );
            } else {
                return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("result", "Service operational"))
                );
            }
        }
    }

    static class SelectiveFailureNode implements WorkflowNode {
        private final AtomicInteger attempts = new AtomicInteger(0);
        private final String[] errors = {
            "timeout",
            "rate limit exceeded",
            "invalid request", // Won't retry this
            "timeout"
        };

        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            int attempt = attempts.getAndIncrement();

            if (attempt < errors.length) {
                String error = errors[attempt];
                return CompletableFuture.completedFuture(
                    NodeOutput.failure(error)
                );
            } else {
                return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("result", "Success"))
                );
            }
        }
    }
}
