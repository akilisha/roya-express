package com.akilisha.oss.roya.workflow.resilience;

import com.akilisha.oss.roya.workflow.core.ExecutionContext;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CircuitBreakerNode
 */
class CircuitBreakerNodeTest {

    @Test
    void testSuccessfulExecution() throws ExecutionException, InterruptedException {
        // Arrange
        WorkflowNode delegate = input -> CompletableFuture.completedFuture(
            NodeOutput.success("result", "success")
        );

        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofSeconds(1));
        CircuitBreakerNode node = new CircuitBreakerNode(delegate, breaker);

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals("success", output.data().get("result"));
        assertEquals(1, breaker.getSuccessCount());
        assertEquals(0, breaker.getFailureCount());
        assertTrue(breaker.isClosed());
    }

    @Test
    void testFailedExecution() throws ExecutionException, InterruptedException {
        // Arrange
        WorkflowNode delegate = input -> CompletableFuture.completedFuture(
            NodeOutput.failure("delegate failed")
        );

        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofSeconds(1));
        CircuitBreakerNode node = new CircuitBreakerNode(delegate, breaker);

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isFailure());
        assertEquals(1, breaker.getFailureCount());
        assertTrue(breaker.isClosed()); // Still closed, below threshold
    }

    @Test
    void testCircuitOpensAfterThresholdFailures() throws ExecutionException, InterruptedException {
        // Arrange
        WorkflowNode delegate = input -> CompletableFuture.completedFuture(
            NodeOutput.failure("always fails")
        );

        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofSeconds(1));
        CircuitBreakerNode node = new CircuitBreakerNode(delegate, breaker);

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act - execute 3 times to hit threshold
        node.execute(input).get();
        node.execute(input).get();
        node.execute(input).get();

        // Assert - circuit should be open
        assertTrue(breaker.isOpen());

        // Try to execute again - should fail fast
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        assertTrue(output.isFailure());
        assertTrue(output.error().get().contains("Circuit breaker is OPEN"));
        assertTrue(output.error().get().contains("Service unavailable"));
    }

    @Test
    void testCircuitOpensAfterException() throws ExecutionException, InterruptedException {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);
        WorkflowNode delegate = input -> {
            callCount.incrementAndGet();
            CompletableFuture<NodeOutput> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Simulated error"));
            return future;
        };

        CircuitBreaker breaker = new CircuitBreaker(2, Duration.ofSeconds(1));
        CircuitBreakerNode node = new CircuitBreakerNode(delegate, breaker);

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act - execute twice to hit threshold
        node.execute(input).get();
        node.execute(input).get();

        // Assert
        assertTrue(breaker.isOpen());
        assertEquals(2, callCount.get());

        // Next call should not reach delegate
        node.execute(input).get();
        assertEquals(2, callCount.get()); // Still 2, not 3
    }

    @Test
    void testWithStatsInOutput() throws ExecutionException, InterruptedException {
        // Arrange
        WorkflowNode delegate = input -> CompletableFuture.completedFuture(
            NodeOutput.success("result", "data")
        );

        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofSeconds(1));
        CircuitBreakerNode node = new CircuitBreakerNode(delegate, breaker, true);

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertTrue(output.data().containsKey("circuitBreakerState"));
        assertTrue(output.data().containsKey("circuitBreakerFailures"));
        assertTrue(output.data().containsKey("circuitBreakerSuccesses"));
        assertTrue(output.data().containsKey("circuitBreakerHealthy"));

        assertEquals("CLOSED", output.data().get("circuitBreakerState"));
        assertEquals(0, output.data().get("circuitBreakerFailures"));
        assertEquals(1, output.data().get("circuitBreakerSuccesses"));
        assertTrue((Boolean) output.data().get("circuitBreakerHealthy"));
    }

    @Test
    void testResetCircuit() throws ExecutionException, InterruptedException {
        // Arrange
        WorkflowNode delegate = input -> CompletableFuture.completedFuture(
            NodeOutput.failure("fail")
        );

        CircuitBreaker breaker = new CircuitBreaker(2, Duration.ofSeconds(1));
        CircuitBreakerNode node = new CircuitBreakerNode(delegate, breaker);

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act - open the circuit
        node.execute(input).get();
        node.execute(input).get();
        assertTrue(breaker.isOpen());

        // Reset
        node.reset();

        // Assert
        assertTrue(breaker.isClosed());
        assertEquals(0, breaker.getFailureCount());
    }

    @Test
    void testForceOpen() throws ExecutionException, InterruptedException {
        // Arrange
        WorkflowNode delegate = input -> CompletableFuture.completedFuture(
            NodeOutput.success("result", "success")
        );

        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofSeconds(1));
        CircuitBreakerNode node = new CircuitBreakerNode(delegate, breaker);

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        node.forceOpen();

        // Assert - should fail fast
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        assertTrue(output.isFailure());
        assertTrue(output.error().get().contains("Circuit breaker is OPEN"));
    }

    @Test
    void testGetters() {
        // Arrange
        WorkflowNode delegate = input -> CompletableFuture.completedFuture(
            NodeOutput.success("data", "value")
        );

        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofSeconds(1));
        CircuitBreakerNode node = new CircuitBreakerNode(delegate, breaker);

        // Assert
        assertEquals(delegate, node.getDelegate());
        assertEquals(breaker, node.getCircuitBreaker());
        assertNotNull(node.getStats());
    }

    @Test
    void testDefaultsFactoryMethod() throws ExecutionException, InterruptedException {
        // Arrange
        WorkflowNode delegate = input -> CompletableFuture.completedFuture(
            NodeOutput.success("result", "success")
        );

        CircuitBreakerNode node = CircuitBreakerNode.withDefaults(delegate);

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertTrue(node.getCircuitBreaker().isClosed());
        assertEquals(5, node.getCircuitBreaker().getFailureThreshold());
    }

    @Test
    void testRecoveryAfterCircuitOpens() throws ExecutionException, InterruptedException {
        // Arrange
        AtomicInteger failCount = new AtomicInteger(0);
        WorkflowNode delegate = input -> {
            if (failCount.getAndIncrement() < 3) {
                return CompletableFuture.completedFuture(NodeOutput.failure("fail"));
            }
            return CompletableFuture.completedFuture(NodeOutput.success("recovered", true));
        };

        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofMillis(100));
        CircuitBreakerNode node = new CircuitBreakerNode(delegate, breaker);

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act - fail 3 times to open circuit
        node.execute(input).get();
        node.execute(input).get();
        node.execute(input).get();

        assertTrue(breaker.isOpen());

        // Wait for half-open transition
        Thread.sleep(150);

        // Execute - should succeed and close circuit
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals(true, output.data().get("recovered"));
        assertTrue(breaker.isClosed());
    }

    @Test
    void testMixedSuccessAndFailure() throws ExecutionException, InterruptedException {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);
        WorkflowNode delegate = input -> {
            int count = callCount.getAndIncrement();
            if (count % 2 == 0) {
                return CompletableFuture.completedFuture(NodeOutput.success("result", count));
            } else {
                return CompletableFuture.completedFuture(NodeOutput.failure("fail"));
            }
        };

        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofSeconds(1));
        CircuitBreakerNode node = new CircuitBreakerNode(delegate, breaker);

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act - alternate success and failure
        NodeOutput output1 = node.execute(input).get(); // success (0)
        NodeOutput output2 = node.execute(input).get(); // fail (1)
        NodeOutput output3 = node.execute(input).get(); // success (2)
        NodeOutput output4 = node.execute(input).get(); // fail (3)

        // Assert
        assertTrue(output1.isSuccess());
        assertTrue(output2.isFailure());
        assertTrue(output3.isSuccess());
        assertTrue(output4.isFailure());

        // Circuit should still be closed because successes reset the count
        assertTrue(breaker.isClosed());
    }

    @Test
    void testStatsAfterMultipleOperations() throws ExecutionException, InterruptedException {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);
        WorkflowNode delegate = input -> {
            int count = callCount.getAndIncrement();
            if (count < 2) {
                return CompletableFuture.completedFuture(NodeOutput.failure("fail"));
            }
            return CompletableFuture.completedFuture(NodeOutput.success("result", "ok"));
        };

        CircuitBreaker breaker = new CircuitBreaker(5, Duration.ofSeconds(1));
        CircuitBreakerNode node = new CircuitBreakerNode(delegate, breaker);

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        node.execute(input).get(); // fail
        node.execute(input).get(); // fail
        node.execute(input).get(); // success

        CircuitBreaker.CircuitBreakerStats stats = node.getStats();

        // Assert
        assertEquals(CircuitBreakerState.CLOSED, stats.state());
        assertEquals(0, stats.failureCount()); // Reset by success
        assertEquals(1, stats.successCount());
    }
}
