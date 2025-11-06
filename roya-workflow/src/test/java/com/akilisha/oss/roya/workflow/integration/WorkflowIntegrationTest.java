package com.akilisha.oss.roya.workflow.integration;

import com.akilisha.oss.roya.workflow.continuation.ContinuationNode;
import com.akilisha.oss.roya.workflow.core.*;
import com.akilisha.oss.roya.workflow.cost.CostTracker;
import com.akilisha.oss.roya.workflow.cost.NodeCostCalculator;
import com.akilisha.oss.roya.workflow.edges.Edge;
import com.akilisha.oss.roya.workflow.edges.ErrorStrategy;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import com.akilisha.oss.roya.workflow.nested.MergeAllAggregator;
import com.akilisha.oss.roya.workflow.nested.CollectAllAggregator;
import com.akilisha.oss.roya.workflow.nested.NestedExecutionStrategy;
import com.akilisha.oss.roya.workflow.nodes.TransformNode;
import com.akilisha.oss.roya.workflow.resilience.CircuitBreaker;
import com.akilisha.oss.roya.workflow.resilience.CircuitBreakerNode;
import com.akilisha.oss.roya.workflow.retry.RetryPolicy;
import com.akilisha.oss.roya.workflow.visitor.WorkflowVisitor;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for complete workflow scenarios
 */
class WorkflowIntegrationTest {

    @Test
    void testSimpleLinearWorkflow() throws ExecutionException, InterruptedException {
        // Arrange - Create a simple 3-node workflow
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("value", 10)
            ))
            .action("double", input -> {
                int value = (int) input.data().get("value");
                return CompletableFuture.completedFuture(
                    NodeOutput.success("value", value * 2)
                );
            })
            .action("addTen", input -> {
                int value = (int) input.data().get("value");
                return CompletableFuture.completedFuture(
                    NodeOutput.success("result", value + 10)
                );
            })
            .edge("start", "double")
            .edge("double", "addTen")
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        // Act
        WorkflowResult result = executor.executeFrom("start", Map.of()).join();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(30, result.context().get("result")); // (10 * 2) + 10 = 30
    }

    @Test
    void testConditionalRouting() throws ExecutionException, InterruptedException {
        // Arrange - Workflow with conditional branching
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("age", 25)
            ))
            .action("adult", input -> CompletableFuture.completedFuture(
                NodeOutput.success("category", "adult")
            ))
            .action("minor", input -> CompletableFuture.completedFuture(
                NodeOutput.success("category", "minor")
            ))
            .edge("start", "adult", Edge.when(ctx -> (int)ctx.get("age") >= 18))
            .edge("start", "minor", Edge.when(ctx -> (int)ctx.get("age") < 18))
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        // Act
        WorkflowResult result = executor.executeFrom("start", Map.of()).join();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("adult", result.context().get("category"));
    }

    @Test
    void testParallelExecution() throws ExecutionException, InterruptedException {
        // Arrange - Workflow with parallel branches
        AtomicInteger executionCount = new AtomicInteger(0);

        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "input")
            ))
            .action("taskA", input -> {
                executionCount.incrementAndGet();
                return CompletableFuture.completedFuture(
                    NodeOutput.success("resultA", "A")
                );
            })
            .action("taskB", input -> {
                executionCount.incrementAndGet();
                return CompletableFuture.completedFuture(
                    NodeOutput.success("resultB", "B")
                );
            })
            .action("taskC", input -> {
                executionCount.incrementAndGet();
                return CompletableFuture.completedFuture(
                    NodeOutput.success("resultC", "C")
                );
            })
            .edge("start", "taskA", Edge.parallel())
            .edge("start", "taskB", Edge.parallel())
            .edge("start", "taskC", Edge.parallel())
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        // Act
        WorkflowResult result = executor.executeFrom("start", Map.of()).join();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(3, executionCount.get());
        assertEquals("A", result.context().get("resultA"));
        assertEquals("B", result.context().get("resultB"));
        assertEquals("C", result.context().get("resultC"));
    }

    @Test
    void testRetryMechanism() throws ExecutionException, InterruptedException {
        // Arrange - Node that fails twice then succeeds
        AtomicInteger attemptCount = new AtomicInteger(0);

        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "input")
            ))
            .action("flaky", input -> {
                int attempt = attemptCount.incrementAndGet();
                if (attempt < 3) {
                    return CompletableFuture.completedFuture(
                        NodeOutput.failure("Temporary failure")
                    );
                }
                return CompletableFuture.completedFuture(
                    NodeOutput.success("result", "success")
                );
            })
            .edge("start", "flaky", Edge.sequential()
                .withRetry(RetryPolicy.fixedDelay(3, Duration.ofMillis(50))))
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        // Act
        WorkflowResult result = executor.executeFrom("start", Map.of()).join();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(3, attemptCount.get());
        assertEquals("success", result.context().get("result"));
    }

    @Test
    void testErrorHandlingWithSkipStrategy() throws ExecutionException, InterruptedException {
        // Arrange - Workflow where one node fails but continues
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "input")
            ))
            .action("failing", input -> CompletableFuture.completedFuture(
                NodeOutput.failure("This node fails")
            ))
            .action("afterFail", input -> CompletableFuture.completedFuture(
                NodeOutput.success("result", "executed anyway")
            ))
            .edge("start", "failing", Edge.sequential()
                .onError(ErrorStrategy.SKIP_AND_CONTINUE))
            .edge("failing", "afterFail")
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        // Act
        WorkflowResult result = executor.executeFrom("start", Map.of()).join();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("executed anyway", result.context().get("result"));
    }

    @Test
    void testWorkflowWithTransformNode() throws ExecutionException, InterruptedException {
        // Arrange
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("firstName", "John", "lastName", "Doe")
            ))
            .logic("transform", TransformNode.create(data -> {
                String first = (String) data.get("firstName");
                String last = (String) data.get("lastName");
                return Map.of(
                    "fullName", first + " " + last,
                    "initials", first.charAt(0) + "." + last.charAt(0) + "."
                );
            }))
            .edge("start", "transform")
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        // Act
        WorkflowResult result = executor.executeFrom("start", Map.of()).join();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("John Doe", result.context().get("fullName"));
        assertEquals("J.D.", result.context().get("initials"));
    }

    @Test
    void testWorkflowWithCircuitBreaker() throws ExecutionException, InterruptedException {
        // Arrange - Flaky node protected by circuit breaker
        AtomicInteger callCount = new AtomicInteger(0);
        CircuitBreaker breaker = new CircuitBreaker(2, Duration.ofSeconds(1));

        WorkflowNode flakyNode = input -> {
            callCount.incrementAndGet();
            return CompletableFuture.completedFuture(
                NodeOutput.failure("Service unavailable")
            );
        };

        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "input")
            ))
            .action("flaky", new CircuitBreakerNode(flakyNode, breaker))
            .edge("start", "flaky")
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        // Act - Execute multiple times
        executor.executeFrom("start", Map.of()).join(); // Call 1 - fails
        executor.executeFrom("start", Map.of()).join(); // Call 2 - fails, opens circuit
        WorkflowResult result3 = executor.executeFrom("start", Map.of()).join(); // Call 3 - circuit open

        // Assert
        assertEquals(2, callCount.get()); // Only 2 actual calls, 3rd blocked by circuit
        assertTrue(result3.isFailure());
        assertTrue(result3.finalOutput().error().get().contains("Circuit breaker is OPEN"));
    }

    @Test
    void testWorkflowWithCostTracking() throws ExecutionException, InterruptedException {
        // Arrange
        CostTracker costTracker = new CostTracker(10.00)
            .withNodeCost("expensiveOp", 2.50)
            .withNodeCost("cheapOp", 0.50);

        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "input")
            ))
            .action("expensiveOp", input -> CompletableFuture.completedFuture(
                NodeOutput.success("result1", "expensive")
            ))
            .action("cheapOp", input -> CompletableFuture.completedFuture(
                NodeOutput.success("result2", "cheap")
            ))
            .edge("start", "expensiveOp")
            .edge("expensiveOp", "cheapOp")
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow)
            .addVisitor(costTracker);

        // Act
        WorkflowResult result = executor.executeFrom("start", Map.of()).join();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(3.00, costTracker.getTotalCost(), 0.001); // 2.50 + 0.50
        assertEquals(3.00, result.context().get("totalCost"));
        assertEquals(7.00, result.context().get("budgetRemaining"));
    }

    @Test
    void testContinuationWorkflow() throws ExecutionException, InterruptedException {
        // Arrange - Create child workflow
        Workflow childWorkflow = Workflow.create()
            .trigger("childStart", input -> {
                int value = (int) input.data().get("value");
                return CompletableFuture.completedFuture(
                    NodeOutput.success("doubled", value * 2)
                );
            })
            .build();

        // Parent workflow with continuation
        Workflow parentWorkflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("value", 5)
            ))
            .continuation("child", childWorkflow, "childStart")
            .action("addTen", input -> {
                int doubled = (int) input.data().get("doubled");
                return CompletableFuture.completedFuture(
                    NodeOutput.success("result", doubled + 10)
                );
            })
            .edge("start", "child")
            .edge("child", "addTen")
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(parentWorkflow);

        // Act
        WorkflowResult result = executor.executeFrom("start", Map.of()).join();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(10, result.context().get("doubled")); // 5 * 2
        assertEquals(20, result.context().get("result")); // 10 + 10
    }

    @Test
    void testNestedWorkflowWithMergeAll() throws ExecutionException, InterruptedException {
        // Arrange - Create child workflows
        Workflow child1 = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("result1", "A")
            ))
            .build();

        Workflow child2 = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("result2", "B")
            ))
            .build();

        Workflow child3 = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("result3", "C")
            ))
            .build();

        // Parent workflow with nested execution
        Workflow parentWorkflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "input")
            ))
            .nested("parallel", List.of(child1, child2, child3), new MergeAllAggregator())
            .edge("start", "parallel")
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(parentWorkflow);

        // Act
        WorkflowResult result = executor.executeFrom("start", Map.of()).join();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("A", result.context().get("result1"));
        assertEquals("B", result.context().get("result2"));
        assertEquals("C", result.context().get("result3"));
    }

    @Test
    void testNestedWorkflowWithCollectAll() throws ExecutionException, InterruptedException {
        // Arrange
        Workflow child1 = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("value", 10)
            ))
            .build();

        Workflow child2 = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("value", 20)
            ))
            .build();

        Workflow parentWorkflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "input")
            ))
            .nested("parallel", List.of(child1, child2), new CollectAllAggregator())
            .edge("start", "parallel")
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(parentWorkflow);

        // Act
        WorkflowResult result = executor.executeFrom("start", Map.of()).join();

        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.context().snapshot().containsKey("childResults"));
        assertEquals(2, result.context().get("totalChildren"));
        assertEquals(2L, result.context().get("successCount"));
    }

    @Test
    void testComplexWorkflowWithMultipleFeatures() throws ExecutionException, InterruptedException {
        // Arrange - Comprehensive workflow using multiple features
        CostTracker costTracker = new CostTracker(100.00)
            .withNodeCost("validate", 0.10)
            .withNodeCost("process", 1.50)
            .withNodeCost("notify", 0.25);

        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofSeconds(1));

        AtomicInteger retryCount = new AtomicInteger(0);

        Workflow workflow = Workflow.create()
            // Input validation
            .trigger("input", input -> CompletableFuture.completedFuture(
                NodeOutput.success("userId", 123, "amount", 100.00)
            ))

            // Validation node
            .action("validate", input -> {
                double amount = (double) input.data().get("amount");
                if (amount > 0) {
                    return CompletableFuture.completedFuture(
                        NodeOutput.success("validated", true)
                    );
                }
                return CompletableFuture.completedFuture(
                    NodeOutput.failure("Invalid amount")
                );
            })

            // Processing with retry and circuit breaker
            .action("process", new CircuitBreakerNode(input -> {
                int attempt = retryCount.incrementAndGet();
                if (attempt < 2) {
                    return CompletableFuture.completedFuture(
                        NodeOutput.failure("Temporary processing error")
                    );
                }
                return CompletableFuture.completedFuture(
                    NodeOutput.success("transactionId", "TXN-" + System.currentTimeMillis())
                );
            }, breaker))

            // Transform result
            .logic("transform", TransformNode.create(data -> {
                String txnId = (String) data.get("transactionId");
                int userId = (int) data.get("userId");
                return Map.of(
                    "receipt", "Receipt: " + txnId + " for user " + userId,
                    "status", "completed"
                );
            }))

            // Notification
            .action("notify", input -> CompletableFuture.completedFuture(
                NodeOutput.success("notified", true)
            ))

            // Edges with retry
            .edge("input", "validate")
            .edge("validate", "process", Edge.sequential()
                .withRetry(RetryPolicy.fixedDelay(2, Duration.ofMillis(50))))
            .edge("process", "transform")
            .edge("transform", "notify")
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow)
            .addVisitor(costTracker);

        // Act
        WorkflowResult result = executor.executeFrom("input", Map.of()).join();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(true, result.context().get("validated"));
        assertNotNull(result.context().get("transactionId"));
        assertTrue(((String) result.context().get("receipt")).startsWith("Receipt:"));
        assertEquals("completed", result.context().get("status"));
        assertEquals(true, result.context().get("notified"));

        // Check cost tracking
        assertTrue(costTracker.getTotalCost() > 0);
        assertTrue(costTracker.getTotalCost() < 100.00);

        // Check retry happened
        assertEquals(2, retryCount.get());
    }

    @Test
    void testDiamondPatternWorkflow() throws ExecutionException, InterruptedException {
        // Arrange - Diamond pattern: start -> (A, B) -> merge
        AtomicInteger mergeCallCount = new AtomicInteger(0);

        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("value", 10)
            ))
            .action("branchA", input -> {
                int value = (int) input.data().get("value");
                return CompletableFuture.completedFuture(
                    NodeOutput.success("resultA", value * 2)
                );
            })
            .action("branchB", input -> {
                int value = (int) input.data().get("value");
                return CompletableFuture.completedFuture(
                    NodeOutput.success("resultB", value + 5)
                );
            })
            .action("merge", input -> {
                mergeCallCount.incrementAndGet();
                int resultA = (int) input.data().get("resultA");
                int resultB = (int) input.data().get("resultB");
                return CompletableFuture.completedFuture(
                    NodeOutput.success("final", resultA + resultB)
                );
            })
            .edge("start", "branchA", Edge.parallel())
            .edge("start", "branchB", Edge.parallel())
            .edge("branchA", "merge")
            .edge("branchB", "merge")
            .build();

        WorkflowExecutor executor = new WorkflowExecutor(workflow);

        // Act
        WorkflowResult result = executor.executeFrom("start", Map.of()).join();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(20, result.context().get("resultA")); // 10 * 2
        assertEquals(15, result.context().get("resultB")); // 10 + 5
        assertEquals(35, result.context().get("final")); // 20 + 15
    }
}
