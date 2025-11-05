package com.akilisha.oss.roya.workflow.examples;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import com.akilisha.oss.roya.workflow.loop.LoopNode;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Demonstrates LOOP NODE - repeating task execution
 */
public class LoopNodeExample {

    public static void main(String[] args) {
        System.out.println("=== Loop Node Example ===\n");

        // Example 1: Sequential loop
        System.out.println("Example 1: Sequential Loop (5 iterations)\n");
        runSequentialLoop();

        System.out.println("\n" + "=".repeat(60) + "\n");

        // Example 2: Parallel loop
        System.out.println("Example 2: Parallel Loop (5 iterations)\n");
        runParallelLoop();

        System.out.println("\n" + "=".repeat(60) + "\n");

        // Example 3: Until success
        System.out.println("Example 3: Loop Until Success\n");
        runUntilSuccess();

        System.out.println("\n" + "=".repeat(60) + "\n");

        // Example 4: Average results
        System.out.println("Example 4: Average Results\n");
        runAverageResults();
    }

    private static void runSequentialLoop() {
        LoopNode loopNode = new LoopNode(
                new CounterNode(),
                5,
                LoopNode.LoopStrategy.SEQUENTIAL
        );

        Workflow workflow = Workflow.create()
                .trigger("start", new InputNode())
                .action("loop", loopNode)
                .edge("start", "loop")
                .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        WorkflowResult result = executor.executeFrom("start", Map.of()).join();

        if (result.isSuccess()) {
            System.out.println("\n✅ Loop completed!");
            System.out.println("Total iterations: " + result.context().get("iterations"));
            System.out.println("Success count: " + result.context().get("successCount"));
        }

        executor.shutdown();
    }

    private static void runParallelLoop() {
        LoopNode loopNode = new LoopNode(
                new RandomDelayNode(),
                5,
                LoopNode.LoopStrategy.PARALLEL
        );

        Workflow workflow = Workflow.create()
                .trigger("start", new InputNode())
                .action("loop", loopNode)
                .edge("start", "loop")
                .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        long startTime = System.currentTimeMillis();
        WorkflowResult result = executor.executeFrom("start", Map.of()).join();
        long elapsed = System.currentTimeMillis() - startTime;

        if (result.isSuccess()) {
            System.out.println("\n✅ Parallel loop completed!");
            System.out.println("Total time: " + elapsed + "ms (all ran in parallel)");
            System.out.println("Iterations: " + result.context().get("iterations"));
        }

        executor.shutdown();
    }

    private static void runUntilSuccess() {
        LoopNode loopNode = new LoopNode(
                new FlakyNode(0.6), // 60% failure rate
                10,
                LoopNode.LoopStrategy.UNTIL_SUCCESS
        );

        Workflow workflow = Workflow.create()
                .trigger("start", new InputNode())
                .action("loop", loopNode)
                .edge("start", "loop")
                .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        WorkflowResult result = executor.executeFrom("start", Map.of()).join();

        if (result.isSuccess()) {
            System.out.println("\n✅ Success achieved!");
            System.out.println("Result: " + result.context().get("result"));
        } else {
            System.out.println("\n❌ Failed after max attempts");
        }

        executor.shutdown();
    }

    private static void runAverageResults() {
        LoopNode loopNode = new LoopNode(
                new ScoreNode(),
                10,
                LoopNode.LoopStrategy.PARALLEL,
                new LoopNode.AverageResults("score"),
                java.time.Duration.ofMinutes(1)
        );

        Workflow workflow = Workflow.create()
                .trigger("start", new InputNode())
                .action("loop", loopNode)
                .edge("start", "loop")
                .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        WorkflowResult result = executor.executeFrom("start", Map.of()).join();

        if (result.isSuccess()) {
            System.out.println("\n✅ Averaging complete!");
            System.out.println("Average score: " + result.context().get("average"));
            System.out.println("Successful samples: " + result.context().get("successfulSamples"));
        }

        executor.shutdown();
    }

    // ===== Example Nodes =====

    static class InputNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(NodeOutput.success(input.data()));
        }
    }

    static class CounterNode implements WorkflowNode {
        private static int counter = 0;

        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            int current = ++counter;
            System.out.println("  Iteration " + current);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("iteration", current))
            );
        }
    }

    static class RandomDelayNode implements WorkflowNode {
        private static final Random random = new Random();
        private static int counter = 0;

        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            int id = ++counter;
            int delay = 100 + random.nextInt(400); // 100-500ms

            System.out.println("  Task " + id + " starting (will take " + delay + "ms)");

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println("  Task " + id + " completed");

            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("taskId", id, "delay", delay))
            );
        }
    }

    static class FlakyNode implements WorkflowNode {
        private final double failureRate;
        private final Random random = new Random();
        private int attempts = 0;

        public FlakyNode(double failureRate) {
            this.failureRate = failureRate;
        }

        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            attempts++;
            System.out.println("  Attempt " + attempts + "...");

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (random.nextDouble() < failureRate) {
                System.out.println("    ❌ Failed");
                return CompletableFuture.completedFuture(
                        NodeOutput.failure("Service unavailable")
                );
            } else {
                System.out.println("    ✅ Success!");
                return CompletableFuture.completedFuture(
                        NodeOutput.success(Map.of("result", "Task completed", "attempts", attempts))
                );
            }
        }
    }

    static class ScoreNode implements WorkflowNode {
        private final Random random = new Random();

        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            double score = 50 + random.nextDouble() * 50; // Score between 50-100
            System.out.println("  Generated score: " + String.format("%.2f", score));

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("score", score))
            );
        }
    }
}
