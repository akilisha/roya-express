package com.akilisha.oss.roya.workflow.examples;

import com.akilisha.oss.roya.workflow.core.*;
import com.akilisha.oss.roya.workflow.edges.Edge;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import com.akilisha.oss.roya.workflow.resilience.CircuitBreaker;
import com.akilisha.oss.roya.workflow.resilience.CircuitBreakerNode;
import com.akilisha.oss.roya.workflow.resilience.CircuitBreakerState;
import com.akilisha.oss.roya.workflow.visitor.LoggingVisitor;

import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Example demonstrating CIRCUIT BREAKER pattern.
 *
 * Use Case: AI workflow calling unreliable external services:
 * - LLM API (sometimes times out)
 * - External knowledge base (flaky)
 * - Image generation service (rate limited)
 *
 * Demonstrates:
 * 1. Circuit breaker protecting against failures
 * 2. Fail-fast when service is down (circuit open)
 * 3. Automatic recovery testing (half-open state)
 * 4. Circuit breaker statistics and monitoring
 *
 * Demonstrates:
 *
 * ✅ Basic circuit breaker usage (3 failures → open)
 * ✅ Fail-fast when circuit is open
 * ✅ Circuit states: CLOSED → OPEN → HALF_OPEN → CLOSED
 * ✅ Automatic recovery testing
 * ✅ Multiple circuit breakers in one workflow
 * ✅ Circuit breaker statistics
 * ✅ Different thresholds for different services
 *
 * Realistic Scenarios:
 *
 * Flaky LLM API (70% failure rate initially)
 * Database with connection issues (30% failure)
 * Rate-limited external API (60% failure)
 * Service that recovers after N failures
 *
 * 3 Different Examples:
 *
 * Basic circuit breaker (single service)
 * Recovery flow (OPEN → HALF_OPEN → CLOSED)
 * Multiple circuit breakers (LLM + DB + API)
 */
public class CircuitBreakerExample {

    public static void main(String[] args) {
        System.out.println("=== Circuit Breaker Example ===\n");

        // Example 1: Basic circuit breaker with single node
        System.out.println("⚡ Example 1: Basic Circuit Breaker\n");
        runBasicExample();

        System.out.println("\n" + "=".repeat(60) + "\n");

        // Example 2: Circuit breaker with recovery
        System.out.println("⚡ Example 2: Circuit Breaker Recovery\n");
        runRecoveryExample();

        System.out.println("\n" + "=".repeat(60) + "\n");

        // Example 3: Multiple circuit breakers in workflow
        System.out.println("⚡ Example 3: Multiple Circuit Breakers\n");
        runMultipleCircuitBreakersExample();
    }

    /**
     * Example 1: Basic circuit breaker usage
     * Shows circuit opening after threshold failures
     */
    private static void runBasicExample() {
        // Create circuit breaker: 3 failures → open, wait 2 seconds before retry
        CircuitBreaker breaker = CircuitBreaker.withThreshold(3, Duration.ofSeconds(2));

        // Create a flaky API node
        FlakyLLMNode flakyNode = new FlakyLLMNode(0.7); // 70% failure rate

        // Wrap with circuit breaker
        CircuitBreakerNode protectedNode = new CircuitBreakerNode(flakyNode, breaker, true);

        Workflow workflow = Workflow.create()
            .trigger("start", new InputNode())
            .action("llmCall", protectedNode)
            .edge("start", "llmCall")
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        // Execute multiple times to trigger circuit breaker
        for (int i = 1; i <= 10; i++) {
            System.out.println("Attempt " + i + ":");

            WorkflowResult result = executor.executeFrom(
                "start",
                Map.of("prompt", "Generate a blog post")
            ).join();

            if (result.isSuccess()) {
                System.out.println("   ✅ Success! Response: " + result.context().get("response"));

                // Show circuit breaker stats from output
//                if (result.context().containsKey("circuitBreakerState")) {
//                    System.out.println("   📊 Circuit State: " + result.context().get("circuitBreakerState"));
//                    System.out.println("   📊 Failures: " + result.context().get("circuitBreakerFailures"));
//                    System.out.println("   📊 Successes: " + result.context().get("circuitBreakerSuccesses"));
//                }
            } else {
                System.out.println("   ❌ Failed: " + result.finalOutput().error().orElse("Unknown"));
            }

            // Check circuit breaker state
            CircuitBreaker.CircuitBreakerStats stats = breaker.getStats();
            System.out.println("   🔌 Circuit: " + stats.state() +
                " (failures: " + stats.failureCount() + "/" + stats.failureThreshold() + ")");

            // Small delay between attempts
            sleep(100);
        }

        executor.shutdown();
    }

    /**
     * Example 2: Circuit breaker recovery
     * Shows circuit moving from OPEN → HALF_OPEN → CLOSED
     */
    private static void runRecoveryExample() {
        // Create circuit breaker with short timeout for demo
        CircuitBreaker breaker = CircuitBreaker.withThreshold(3, Duration.ofSeconds(1));

        // Create a node that fails initially but recovers
        RecoveringServiceNode recoveringNode = new RecoveringServiceNode(5); // Fails first 5 times

        CircuitBreakerNode protectedNode = new CircuitBreakerNode(recoveringNode, breaker);

        Workflow workflow = Workflow.create()
            .trigger("start", new InputNode())
            .action("service", protectedNode)
            .edge("start", "service")
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        // Phase 1: Trigger failures to open circuit
        System.out.println("Phase 1: Triggering failures...");
        for (int i = 1; i <= 5; i++) {
            System.out.println("  Attempt " + i + ":");
            WorkflowResult result = executor.executeFrom("start", Map.of()).join();
            System.out.println("    Status: " + (result.isSuccess() ? "✅ Success" : "❌ Failed"));
            System.out.println("    Circuit: " + breaker.getState());
            sleep(100);
        }

        // Phase 2: Wait for reset timeout
        System.out.println("\nPhase 2: Waiting for circuit to allow retry...");
        sleep(1200); // Wait for reset timeout

        // Phase 3: Recovery - service is now healthy
        System.out.println("\nPhase 3: Service recovered, testing...");
        for (int i = 1; i <= 3; i++) {
            System.out.println("  Attempt " + i + ":");
            WorkflowResult result = executor.executeFrom("start", Map.of()).join();
            System.out.println("    Status: " + (result.isSuccess() ? "✅ Success" : "❌ Failed"));
            System.out.println("    Circuit: " + breaker.getState());
            sleep(100);
        }

        System.out.println("\n📊 Final Circuit Stats: " + breaker.getStats());

        executor.shutdown();
    }

    /**
     * Example 3: Multiple circuit breakers in a workflow
     * Shows using different circuit breakers for different services
     */
    private static void runMultipleCircuitBreakersExample() {
        // Different circuit breakers for different services
        CircuitBreaker llmBreaker = CircuitBreaker.withThreshold(3, Duration.ofSeconds(2));
        CircuitBreaker dbBreaker = CircuitBreaker.withThreshold(5, Duration.ofSeconds(3));
        CircuitBreaker apiBreaker = CircuitBreaker.withThreshold(2, Duration.ofSeconds(1));

        // Create nodes with different failure rates
        FlakyLLMNode llmNode = new FlakyLLMNode(0.4);         // 40% failure
        FlakyDatabaseNode dbNode = new FlakyDatabaseNode(0.3); // 30% failure
        FlakyAPINode apiNode = new FlakyAPINode(0.6);          // 60% failure

        Workflow workflow = Workflow.create()
            .trigger("start", new InputNode())

            // Each service protected by its own circuit breaker
            .action("llm", new CircuitBreakerNode(llmNode, llmBreaker))
            .action("database", new CircuitBreakerNode(dbNode, dbBreaker))
            .action("api", new CircuitBreakerNode(apiNode, apiBreaker))

            .action("aggregate", new AggregateResultsNode())

            .edge("start", "llm")
            .edge("start", "database")
            .edge("start", "api")
            .edge("llm", "aggregate")
            .edge("database", "aggregate")
            .edge("api", "aggregate")
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        // Execute multiple times
        for (int i = 1; i <= 8; i++) {
            System.out.println("Execution " + i + ":");

            WorkflowResult result = executor.executeFrom("start", Map.of()).join();

            System.out.println("  Status: " + (result.isSuccess() ? "✅ Success" : "❌ Failed"));
            System.out.println("  Circuit Breakers:");
            System.out.println("    • LLM: " + llmBreaker.getState() +
                " (failures: " + llmBreaker.getFailureCount() + "/3)");
            System.out.println("    • Database: " + dbBreaker.getState() +
                " (failures: " + dbBreaker.getFailureCount() + "/5)");
            System.out.println("    • API: " + apiBreaker.getState() +
                " (failures: " + apiBreaker.getFailureCount() + "/2)");

            sleep(200);
        }

        System.out.println("\n📊 Final Statistics:");
        System.out.println("  LLM: " + llmBreaker.getStats());
        System.out.println("  Database: " + dbBreaker.getStats());
        System.out.println("  API: " + apiBreaker.getStats());

        executor.shutdown();
    }

    // ===== Example Nodes =====

    static class InputNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            return CompletableFuture.completedFuture(NodeOutput.success(input.data()));
        }
    }

    /**
     * Simulates a flaky LLM API with configurable failure rate
     */
    static class FlakyLLMNode implements WorkflowNode {
        private final double failureRate;
        private final Random random = new Random();

        public FlakyLLMNode(double failureRate) {
            this.failureRate = failureRate;
        }

        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            sleep(100); // Simulate API call

            if (random.nextDouble() < failureRate) {
                System.out.println("      🔴 LLM API call failed (timeout/error)");
                return CompletableFuture.completedFuture(
                    NodeOutput.failure("LLM API timeout")
                );
            } else {
                System.out.println("      🟢 LLM API call succeeded");
                return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("response", "Generated content..."))
                );
            }
        }
    }

    /**
     * Simulates a service that fails initially but recovers
     */
    static class RecoveringServiceNode implements WorkflowNode {
        private final AtomicInteger callCount = new AtomicInteger(0);
        private final int failuresBeforeRecovery;

        public RecoveringServiceNode(int failuresBeforeRecovery) {
            this.failuresBeforeRecovery = failuresBeforeRecovery;
        }

        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            int count = callCount.incrementAndGet();
            sleep(50);

            if (count <= failuresBeforeRecovery) {
                System.out.println("      🔴 Service down (attempt " + count + ")");
                return CompletableFuture.completedFuture(
                    NodeOutput.failure("Service unavailable")
                );
            } else {
                System.out.println("      🟢 Service healthy (recovered!)");
                return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("result", "Service working"))
                );
            }
        }
    }

    static class FlakyDatabaseNode implements WorkflowNode {
        private final double failureRate;
        private final Random random = new Random();

        public FlakyDatabaseNode(double failureRate) {
            this.failureRate = failureRate;
        }

        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            sleep(80);

            if (random.nextDouble() < failureRate) {
                System.out.println("      🔴 Database query failed");
                return CompletableFuture.completedFuture(
                    NodeOutput.failure("Database connection timeout")
                );
            } else {
                System.out.println("      🟢 Database query succeeded");
                return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("dbResult", "Query results..."))
                );
            }
        }
    }

    static class FlakyAPINode implements WorkflowNode {
        private final double failureRate;
        private final Random random = new Random();

        public FlakyAPINode(double failureRate) {
            this.failureRate = failureRate;
        }

        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            sleep(120);

            if (random.nextDouble() < failureRate) {
                System.out.println("      🔴 External API failed");
                return CompletableFuture.completedFuture(
                    NodeOutput.failure("API rate limit exceeded")
                );
            } else {
                System.out.println("      🟢 External API succeeded");
                return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("apiData", "API response..."))
                );
            }
        }
    }

    static class AggregateResultsNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("      📦 Aggregating results from all services");
            return CompletableFuture.completedFuture(
                NodeOutput.success(Map.of("aggregated", "Combined results"))
            );
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
