package com.akilisha.oss.roya.workflow.nested;

import com.akilisha.oss.roya.workflow.core.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NestedWorkflowNode
 */
class NestedWorkflowNodeTest {

    @Test
    void testSimpleNestedExecution() throws ExecutionException, InterruptedException {
        // Arrange
        Workflow child1 = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("result1", "child1")
            ))
            .build();

        Workflow child2 = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("result2", "child2")
            ))
            .build();

        NestedWorkflowNode node = new NestedWorkflowNode(
            List.of(child1, child2),
            new MergeAllAggregator()
        );

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals("child1", output.data().get("result1"));
        assertEquals("child2", output.data().get("result2"));
    }

    @Test
    void testNestedWithDataPassingToChildren() throws ExecutionException, InterruptedException {
        // Arrange
        Workflow child1 = Workflow.create()
            .trigger("start", input -> {
                int value = (int) input.data().get("baseValue");
                return CompletableFuture.completedFuture(
                    NodeOutput.success("doubled", value * 2)
                );
            })
            .build();

        Workflow child2 = Workflow.create()
            .trigger("start", input -> {
                int value = (int) input.data().get("baseValue");
                return CompletableFuture.completedFuture(
                    NodeOutput.success("tripled", value * 3)
                );
            })
            .build();

        NestedWorkflowNode node = new NestedWorkflowNode(
            List.of(child1, child2),
            new MergeAllAggregator()
        );

        ExecutionContext context = new ExecutionContext();
        context.set("baseValue", 10);
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals(20, output.data().get("doubled"));
        assertEquals(30, output.data().get("tripled"));
    }

    @Test
    void testNestedWithWaitForAllStrategy() throws ExecutionException, InterruptedException {
        // Arrange - one child fails
        Workflow child1 = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("result", "success")
            ))
            .build();

        Workflow child2 = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.failure("child2 failed")
            ))
            .build();

        NestedWorkflowNode node = new NestedWorkflowNode(
            List.of(child1, child2),
            new MergeAllAggregator(),
            NestedExecutionStrategy.WAIT_FOR_ALL
        );

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isFailure());
        assertTrue(output.error().get().contains("1 of 2 child workflows failed"));
    }

    @Test
    void testNestedWithBestEffortStrategy() throws ExecutionException, InterruptedException {
        // Arrange - one child fails
        Workflow child1 = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("result", "success")
            ))
            .build();

        Workflow child2 = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.failure("child2 failed")
            ))
            .build();

        NestedWorkflowNode node = new NestedWorkflowNode(
            List.of(child1, child2),
            new MergeAllAggregator(),
            NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT
        );

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert - should succeed with available data
        assertTrue(output.isSuccess());
        assertEquals("success", output.data().get("result"));
    }

    @Test
    void testNestedWithFirstSuccessStrategy() throws ExecutionException, InterruptedException {
        // Arrange - first completes successfully
        Workflow fastChild = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("result", "fast")
            ))
            .build();

        Workflow slowChild = Workflow.create()
            .trigger("start", input -> CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return NodeOutput.success("result", "slow");
            }))
            .build();

        NestedWorkflowNode node = new NestedWorkflowNode(
            List.of(fastChild, slowChild),
            new MergeAllAggregator(),
            NestedExecutionStrategy.FIRST_SUCCESS
        );

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals("fast", output.data().get("result"));
    }

    @Test
    void testNestedWithCollectAllAggregator() throws ExecutionException, InterruptedException {
        // Arrange
        Workflow child1 = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "child1")
            ))
            .build();

        Workflow child2 = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "child2")
            ))
            .build();

        NestedWorkflowNode node = new NestedWorkflowNode(
            List.of(child1, child2),
            new CollectAllAggregator()
        );

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertTrue(output.data().containsKey("childResults"));
        assertEquals(2, output.data().get("totalChildren"));
        assertEquals(2L, output.data().get("successCount"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>) output.data().get("childResults");
        assertEquals(2, results.size());
    }

    @Test
    void testNestedWithMergeAllAggregatorWithMetadata() throws ExecutionException, InterruptedException {
        // Arrange
        Workflow child = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("result", "test")
            ))
            .build();

        NestedWorkflowNode node = new NestedWorkflowNode(
            List.of(child),
            new MergeAllAggregator(true)  // Include metadata
        );

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals("test", output.data().get("result"));
        assertTrue(output.data().containsKey("child_0_success"));
        assertTrue(output.data().containsKey("child_0_executionId"));
        assertTrue(output.data().containsKey("child_0_nodeCount"));
    }

    @Test
    void testNestedWithEmptyChildListThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new NestedWorkflowNode(
                List.of(),
                new MergeAllAggregator()
            );
        });
    }

    @Test
    void testNestedWithNullChildListThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new NestedWorkflowNode(
                null,
                new MergeAllAggregator()
            );
        });
    }

    @Test
    void testNestedGetters() {
        // Arrange
        Workflow child = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .build();

        MergeAllAggregator aggregator = new MergeAllAggregator();
        Duration timeout = Duration.ofMinutes(10);

        NestedWorkflowNode node = new NestedWorkflowNode(
            List.of(child),
            aggregator,
            NestedExecutionStrategy.FIRST_SUCCESS,
            timeout
        );

        // Assert
        assertEquals(1, node.getChildWorkflows().size());
        assertEquals(aggregator, node.getAggregator());
        assertEquals(NestedExecutionStrategy.FIRST_SUCCESS, node.getStrategy());
        assertEquals(timeout, node.getTimeout());
    }

    @Test
    void testNestedWithMultipleNodesInChild() throws ExecutionException, InterruptedException {
        // Arrange - child workflow with multiple nodes
        Workflow child = Workflow.create()
            .trigger("start", input -> {
                int value = (int) input.data().get("value");
                return CompletableFuture.completedFuture(
                    NodeOutput.success("step1", value * 2)
                );
            })
            .action("process", input -> {
                int step1 = (int) input.data().get("step1");
                return CompletableFuture.completedFuture(
                    NodeOutput.success("step2", step1 + 10)
                );
            })
            .edge("start", "process")
            .build();

        NestedWorkflowNode node = new NestedWorkflowNode(
            List.of(child),
            new MergeAllAggregator()
        );

        ExecutionContext context = new ExecutionContext();
        context.set("value", 5);
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals(10, output.data().get("step1"));
        assertEquals(20, output.data().get("step2"));
    }

    @Test
    void testNestedWithCustomTimeout() throws ExecutionException, InterruptedException {
        // Arrange
        Workflow child = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("result", "done")
            ))
            .build();

        Duration customTimeout = Duration.ofSeconds(30);
        NestedWorkflowNode node = new NestedWorkflowNode(
            List.of(child),
            new MergeAllAggregator(),
            NestedExecutionStrategy.WAIT_FOR_ALL,
            customTimeout
        );

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals(customTimeout, node.getTimeout());
    }

    @Test
    void testNestedWithFirstSuccessAllFail() throws ExecutionException, InterruptedException {
        // Arrange - all children fail
        Workflow child1 = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.failure("child1 failed")
            ))
            .build();

        Workflow child2 = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.failure("child2 failed")
            ))
            .build();

        NestedWorkflowNode node = new NestedWorkflowNode(
            List.of(child1, child2),
            new MergeAllAggregator(),
            NestedExecutionStrategy.FIRST_SUCCESS
        );

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isFailure());
        assertEquals("All child workflows failed", output.error().get());
    }
}
