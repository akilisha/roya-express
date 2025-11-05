package com.akilisha.oss.roya.workflow.nodes;

import com.akilisha.oss.roya.workflow.core.ExecutionContext;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransformNode
 */
class TransformNodeTest {

    @Test
    void testSimpleTransform() throws ExecutionException, InterruptedException {
        // Arrange
        TransformNode node = new TransformNode(data -> {
            String input = (String) data.get("value");
            return Map.of("result", input.toUpperCase());
        });

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of("value", "hello"), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals("HELLO", output.data().get("result"));
    }

    @Test
    void testTransformWithMultipleFields() throws ExecutionException, InterruptedException {
        // Arrange
        TransformNode node = new TransformNode(data -> {
            int a = (int) data.get("a");
            int b = (int) data.get("b");
            return Map.of(
                "sum", a + b,
                "product", a * b,
                "difference", a - b
            );
        });

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of("a", 10, "b", 5), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals(15, output.data().get("sum"));
        assertEquals(50, output.data().get("product"));
        assertEquals(5, output.data().get("difference"));
    }

    @Test
    void testTransformWithEmptyInput() throws ExecutionException, InterruptedException {
        // Arrange
        TransformNode node = new TransformNode(data -> {
            return Map.of("default", "value");
        });

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals("value", output.data().get("default"));
    }

    @Test
    void testTransformFailureWithException() throws ExecutionException, InterruptedException {
        // Arrange
        TransformNode node = new TransformNode(data -> {
            throw new RuntimeException("Transform error");
        });

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of("value", "test"), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isFailure());
        assertTrue(output.error().isPresent());
        assertTrue(output.error().get().contains("Transform failed"));
        assertTrue(output.error().get().contains("Transform error"));
    }

    @Test
    void testTransformWithNullHandling() throws ExecutionException, InterruptedException {
        // Arrange
        TransformNode node = new TransformNode(data -> {
            String value = (String) data.get("nullable");
            if (value == null) {
                return Map.of("result", "default");
            }
            return Map.of("result", value);
        });

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals("default", output.data().get("result"));
    }

    @Test
    void testTransformFactoryMethod() throws ExecutionException, InterruptedException {
        // Arrange
        TransformNode node = TransformNode.create(data -> {
            int value = (int) data.get("num");
            return Map.of("doubled", value * 2);
        });

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of("num", 21), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals(42, output.data().get("doubled"));
    }

    @Test
    void testTransformReturnsEmptyMap() throws ExecutionException, InterruptedException {
        // Arrange
        TransformNode node = new TransformNode(data -> Map.of());

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of("value", "test"), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertTrue(output.data().isEmpty());
    }

    @Test
    void testTransformWithComplexObjectTransformation() throws ExecutionException, InterruptedException {
        // Arrange
        TransformNode node = new TransformNode(data -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) data.get("user");
            String firstName = (String) user.get("firstName");
            String lastName = (String) user.get("lastName");

            return Map.of(
                "fullName", firstName + " " + lastName,
                "initials", firstName.charAt(0) + "." + lastName.charAt(0) + "."
            );
        });

        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(
            Map.of("user", Map.of("firstName", "John", "lastName", "Doe")),
            context
        );

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals("John Doe", output.data().get("fullName"));
        assertEquals("J.D.", output.data().get("initials"));
    }
}
