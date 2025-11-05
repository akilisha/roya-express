package com.akilisha.oss.roya.workflow.loop;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Node that repeats execution of a delegate node N times.
 * Results can be collected and aggregated.
 * <p>
 * Use cases:
 * - A/B testing (run same task multiple times)
 * - Sampling/averaging results
 * - Batch processing
 * - Monte Carlo simulations
 */
public class LoopNode implements WorkflowNode {

    private final WorkflowNode delegate;
    private final int iterations;
    private final LoopStrategy strategy;
    private final LoopResultAggregator aggregator;
    private final Duration timeout;

    /**
     * Create loop node with sequential execution
     */
    public LoopNode(WorkflowNode delegate, int iterations) {
        this(delegate, iterations, LoopStrategy.SEQUENTIAL, new CollectAllResults(), Duration.ofMinutes(5));
    }

    /**
     * Create loop node with custom strategy
     */
    public LoopNode(WorkflowNode delegate, int iterations, LoopStrategy strategy) {
        this(delegate, iterations, strategy, new CollectAllResults(), Duration.ofMinutes(5));
    }

    /**
     * Create loop node with full configuration
     */
    public LoopNode(
            WorkflowNode delegate,
            int iterations,
            LoopStrategy strategy,
            LoopResultAggregator aggregator,
            Duration timeout
    ) {
        if (iterations < 1) {
            throw new IllegalArgumentException("Iterations must be at least 1");
        }
        this.delegate = delegate;
        this.iterations = iterations;
        this.strategy = strategy;
        this.aggregator = aggregator;
        this.timeout = timeout;
    }

    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        return switch (strategy) {
            case SEQUENTIAL -> executeSequential(input);
            case PARALLEL -> executeParallel(input);
            case UNTIL_SUCCESS -> executeUntilSuccess(input);
            case UNTIL_FAILURE -> executeUntilFailure(input);
        };
    }

    /**
     * Execute iterations sequentially
     */
    private CompletableFuture<NodeOutput> executeSequential(NodeInput input) {
        List<NodeOutput> results = new ArrayList<>();

        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);

        for (int i = 0; i < iterations; i++) {
            final int iteration = i;
            chain = chain.thenCompose(v ->
                    delegate.execute(input)
                            .thenApply(output -> {
                                results.add(output);
                                return null;
                            })
            );
        }

        return chain.thenApply(v -> aggregator.aggregate(results, iterations));
    }

    /**
     * Execute iterations in parallel
     */
    private CompletableFuture<NodeOutput> executeParallel(NodeInput input) {
        List<CompletableFuture<NodeOutput>> futures = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            futures.add(
                    delegate.execute(input)
                            .orTimeout(timeout.toSeconds(), TimeUnit.SECONDS)
                            .exceptionally(ex -> NodeOutput.failure("Iteration failed: " + ex.getMessage()))
            );
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<NodeOutput> results = futures.stream()
                            .map(CompletableFuture::join)
                            .toList();
                    return aggregator.aggregate(results, iterations);
                });
    }

    /**
     * Execute until first success (short-circuit)
     */
    private CompletableFuture<NodeOutput> executeUntilSuccess(NodeInput input) {
        return executeWithCondition(input, 0, NodeOutput::isSuccess, true);
    }

    /**
     * Execute until first failure (short-circuit)
     */
    private CompletableFuture<NodeOutput> executeUntilFailure(NodeInput input) {
        return executeWithCondition(input, 0, NodeOutput::isFailure, false);
    }

    private CompletableFuture<NodeOutput> executeWithCondition(
            NodeInput input,
            int currentIteration,
            java.util.function.Predicate<NodeOutput> condition,
            boolean returnOnCondition
    ) {
        if (currentIteration >= iterations) {
            return CompletableFuture.completedFuture(
                    NodeOutput.failure("Reached max iterations without " +
                            (returnOnCondition ? "success" : "failure"))
            );
        }

        return delegate.execute(input)
                .thenCompose(output -> {
                    if (condition.test(output)) {
                        // Condition met, return
                        return CompletableFuture.completedFuture(output);
                    } else {
                        // Continue to next iteration
                        return executeWithCondition(input, currentIteration + 1, condition, returnOnCondition);
                    }
                });
    }

    /**
     * Strategy for executing loop iterations
     */
    public enum LoopStrategy {
        /**
         * Execute iterations one after another
         */
        SEQUENTIAL,

        /**
         * Execute all iterations simultaneously
         */
        PARALLEL,

        /**
         * Execute until first success, then stop
         */
        UNTIL_SUCCESS,

        /**
         * Execute until first failure, then stop
         */
        UNTIL_FAILURE
    }

    /**
     * Interface for aggregating loop results
     */
    @FunctionalInterface
    public interface LoopResultAggregator {
        NodeOutput aggregate(List<NodeOutput> results, int totalIterations);
    }

    /**
     * Collect all results into a list
     */
    public static class CollectAllResults implements LoopResultAggregator {
        @Override
        public NodeOutput aggregate(List<NodeOutput> results, int totalIterations) {
            List<Map<String, Object>> allData = results.stream()
                    .map(NodeOutput::data)
                    .toList();

            long successCount = results.stream().filter(NodeOutput::isSuccess).count();
            long failureCount = results.stream().filter(NodeOutput::isFailure).count();

            return NodeOutput.success(Map.of(
                    "iterations", totalIterations,
                    "results", allData,
                    "successCount", successCount,
                    "failureCount", failureCount
            ));
        }
    }

    /**
     * Average numeric results
     */
    public static class AverageResults implements LoopResultAggregator {
        private final String key;

        public AverageResults(String key) {
            this.key = key;
        }

        @Override
        public NodeOutput aggregate(List<NodeOutput> results, int totalIterations) {
            double sum = results.stream()
                    .filter(NodeOutput::isSuccess)
                    .map(output -> output.data().get(key))
                    .filter(val -> val instanceof Number)
                    .mapToDouble(val -> ((Number) val).doubleValue())
                    .sum();

            long count = results.stream().filter(NodeOutput::isSuccess).count();
            double average = count > 0 ? sum / count : 0.0;

            return NodeOutput.success(Map.of(
                    "average", average,
                    "iterations", totalIterations,
                    "successfulSamples", count
            ));
        }
    }

    /**
     * Select best result based on a score
     */
    public static class SelectBestResult implements LoopResultAggregator {
        private final String scoreKey;

        public SelectBestResult(String scoreKey) {
            this.scoreKey = scoreKey;
        }

        @Override
        public NodeOutput aggregate(List<NodeOutput> results, int totalIterations) {
            return results.stream()
                    .filter(NodeOutput::isSuccess)
                    .max((a, b) -> {
                        Object scoreA = a.data().get(scoreKey);
                        Object scoreB = b.data().get(scoreKey);
                        if (scoreA instanceof Number && scoreB instanceof Number) {
                            return Double.compare(
                                    ((Number) scoreA).doubleValue(),
                                    ((Number) scoreB).doubleValue()
                            );
                        }
                        return 0;
                    })
                    .orElse(NodeOutput.failure("No successful results"));
        }
    }

    /**
     * Count successes vs failures
     */
    public static class CountResults implements LoopResultAggregator {
        @Override
        public NodeOutput aggregate(List<NodeOutput> results, int totalIterations) {
            long successCount = results.stream().filter(NodeOutput::isSuccess).count();
            long failureCount = results.stream().filter(NodeOutput::isFailure).count();

            return NodeOutput.success(Map.of(
                    "totalIterations", totalIterations,
                    "successCount", successCount,
                    "failureCount", failureCount,
                    "successRate", (double) successCount / totalIterations
            ));
        }
    }
}
