package com.akilisha.oss.roya.workflow.continuation;

import com.akilisha.oss.roya.workflow.core.*;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ContinuationNode
 */
class ContinuationNodeTest {

    @Test
    void testSimpleContinuation() throws ExecutionException, InterruptedException {
        // Arrange
        Workflow childWorkflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("childResult", "child executed")
            ))
            .build();

        ContinuationNode node = new ContinuationNode(childWorkflow, "start");

        ExecutionContext parentContext = new ExecutionContext();
        parentContext.set("parentData", "parent value");
        NodeInput input = new NodeInput(Map.of(), parentContext);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals("child executed", output.data().get("childResult"));

        // Verify context merge - parent should have child's data
        assertEquals("child executed", parentContext.get("childResult"));
        assertEquals("parent value", parentContext.get("parentData"));
    }

    @Test
    void testContinuationWithDataPassing() throws ExecutionException, InterruptedException {
        // Arrange
        Workflow childWorkflow = Workflow.create()
            .trigger("start", input -> {
                // Child reads parent data
                String parentValue = (String) input.data().get("inputData");
                return CompletableFuture.completedFuture(
                    NodeOutput.success("processedData", parentValue.toUpperCase())
                );
            })
            .build();

        ContinuationNode node = new ContinuationNode(childWorkflow, "start");

        ExecutionContext parentContext = new ExecutionContext();
        parentContext.set("inputData", "hello");
        NodeInput input = new NodeInput(Map.of(), parentContext);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals("HELLO", output.data().get("processedData"));
        assertEquals("HELLO", parentContext.get("processedData"));
    }

    @Test
    void testContinuationWithNamespace() throws ExecutionException, InterruptedException {
        // Arrange
        Workflow childWorkflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("result", "child result")
            ))
            .build();

        ContinuationNode node = ContinuationNode.withNamespace(childWorkflow, "start", "child1");

        ExecutionContext parentContext = new ExecutionContext();
        parentContext.set("result", "parent result");
        NodeInput input = new NodeInput(Map.of(), parentContext);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());

        // Parent's original result should be unchanged
        assertEquals("parent result", parentContext.get("result"));

        // Child's result should be namespaced
        assertEquals("child result", parentContext.get("child1.result"));
    }

    @Test
    void testContinuationWithoutContextMerge() throws ExecutionException, InterruptedException {
        // Arrange
        Workflow childWorkflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("childData", "child value")
            ))
            .build();

        ContinuationNode node = new ContinuationNode(childWorkflow, "start", false);

        ExecutionContext parentContext = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), parentContext);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals("child value", output.data().get("childData"));

        // Child data should NOT be merged into parent context
        assertNull(parentContext.get("childData"));
    }

    @Test
    void testContinuationWithFailedChildWorkflow() throws ExecutionException, InterruptedException {
        // Arrange
        Workflow childWorkflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.failure("Child workflow error")
            ))
            .build();

        ContinuationNode node = new ContinuationNode(childWorkflow, "start");

        ExecutionContext parentContext = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), parentContext);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isFailure());
        assertTrue(output.error().isPresent());
        assertTrue(output.error().get().contains("Continuation workflow failed"));
        assertTrue(output.error().get().contains("Child workflow error"));
    }

    @Test
    void testContinuationWithMultipleNodes() throws ExecutionException, InterruptedException {
        // Arrange
        Workflow childWorkflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("step1", "first")
            ))
            .action("process", input -> {
                String step1Value = (String) input.data().get("step1");
                return CompletableFuture.completedFuture(
                    NodeOutput.success("step2", step1Value + "_processed")
                );
            })
            .edge("start", "process")
            .build();

        ContinuationNode node = new ContinuationNode(childWorkflow, "start");

        ExecutionContext parentContext = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), parentContext);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());

        // Both step results should be in parent context
        assertEquals("first", parentContext.get("step1"));
        assertEquals("first_processed", parentContext.get("step2"));
    }

    @Test
    void testContinuationWithNamespaceAvoidingCollision() throws ExecutionException, InterruptedException {
        // Arrange
        Workflow childWorkflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success(Map.of(
                    "name", "child",
                    "value", 42
                ))
            ))
            .build();

        ContinuationNode node = ContinuationNode.withNamespace(childWorkflow, "start", "ns");

        ExecutionContext parentContext = new ExecutionContext();
        parentContext.set("name", "parent");
        parentContext.set("value", 100);
        NodeInput input = new NodeInput(Map.of(), parentContext);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());

        // Parent values should be unchanged
        assertEquals("parent", parentContext.get("name"));
        assertEquals(100, (Integer) parentContext.get("value"));

        // Child values should be namespaced
        assertEquals("child", parentContext.get("ns.name"));
        assertEquals(42, (Integer) parentContext.get("ns.value"));
    }

    @Test
    void testContinuationGetters() {
        // Arrange
        Workflow childWorkflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .build();

        ContinuationNode node1 = new ContinuationNode(childWorkflow, "start");
        ContinuationNode node2 = new ContinuationNode(childWorkflow, "start", false);
        ContinuationNode node3 = ContinuationNode.withNamespace(childWorkflow, "start", "ns");

        // Assert
        assertEquals(childWorkflow, node1.getChildWorkflow());
        assertEquals("start", node1.getStartNodeId());
        assertTrue(node1.isMergeContext());
        assertNull(node1.getNamespace());

        assertFalse(node2.isMergeContext());

        assertEquals("ns", node3.getNamespace());
        assertTrue(node3.isMergeContext());
    }

    @Test
    void testContinuationWithComplexDataFlow() throws ExecutionException, InterruptedException {
        // Arrange - child workflow that transforms data
        Workflow childWorkflow = Workflow.create()
            .trigger("start", input -> {
                int x = (int) input.data().get("x");
                int y = (int) input.data().get("y");
                return CompletableFuture.completedFuture(
                    NodeOutput.success("sum", x + y)
                );
            })
            .action("multiply", input -> {
                int sum = (int) input.data().get("sum");
                return CompletableFuture.completedFuture(
                    NodeOutput.success("result", sum * 2)
                );
            })
            .edge("start", "multiply")
            .build();

        ContinuationNode node = new ContinuationNode(childWorkflow, "start");

        ExecutionContext parentContext = new ExecutionContext();
        parentContext.set("x", 5);
        parentContext.set("y", 10);
        NodeInput input = new NodeInput(Map.of(), parentContext);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals(15, (Integer) parentContext.get("sum"));
        assertEquals(30, (Integer) parentContext.get("result"));
    }

    @Test
    void testContinuationWithEmptyChildOutput() throws ExecutionException, InterruptedException {
        // Arrange
        Workflow childWorkflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success(Map.of())
            ))
            .build();

        ContinuationNode node = new ContinuationNode(childWorkflow, "start");

        ExecutionContext parentContext = new ExecutionContext();
        parentContext.set("parentData", "value");
        NodeInput input = new NodeInput(Map.of(), parentContext);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertTrue(output.data().isEmpty());

        // Parent data should remain
        assertEquals("value", parentContext.get("parentData"));
    }
}
