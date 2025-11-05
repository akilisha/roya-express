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
 * Unit tests for LLMNode
 */
class LLMNodeTest {

    @Test
    void testSuccessfulLLMCall() throws ExecutionException, InterruptedException {
        // Arrange
        LLMNode node = new LLMNode("anthropic", "claude-sonnet-4", "You are a helpful assistant");
        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of("message", "Hello, world!"), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertTrue(output.data().containsKey("response"));
        assertTrue(output.data().containsKey("model"));
        assertTrue(output.data().containsKey("provider"));

        assertEquals("claude-sonnet-4", output.data().get("model"));
        assertEquals("anthropic", output.data().get("provider"));

        String response = (String) output.data().get("response");
        assertNotNull(response);
        assertTrue(response.contains("Hello, world!"), "Response should reference the input message");
    }

    @Test
    void testLLMCallWithMissingMessage() throws ExecutionException, InterruptedException {
        // Arrange
        LLMNode node = new LLMNode("openai", "gpt-4", "System prompt");
        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isFailure());
        assertTrue(output.error().isPresent());
        assertEquals("No 'message' field in input", output.error().get());
    }

    @Test
    void testLLMCallWithNullMessage() throws ExecutionException, InterruptedException {
        // Arrange
        LLMNode node = new LLMNode("anthropic", "claude-sonnet-4", "Test prompt");
        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of("message", null), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isFailure());
        assertTrue(output.error().isPresent());
        assertEquals("No 'message' field in input", output.error().get());
    }

    @Test
    void testLLMCallWithDifferentProviders() throws ExecutionException, InterruptedException {
        // Test Anthropic
        LLMNode anthropicNode = new LLMNode("anthropic", "claude-sonnet-4", "Prompt");
        ExecutionContext context1 = new ExecutionContext();
        NodeInput input1 = new NodeInput(Map.of("message", "Test"), context1);
        NodeOutput output1 = anthropicNode.execute(input1).get();

        assertTrue(output1.isSuccess());
        assertEquals("anthropic", output1.data().get("provider"));

        // Test OpenAI
        LLMNode openaiNode = new LLMNode("openai", "gpt-4", "Prompt");
        ExecutionContext context2 = new ExecutionContext();
        NodeInput input2 = new NodeInput(Map.of("message", "Test"), context2);
        NodeOutput output2 = openaiNode.execute(input2).get();

        assertTrue(output2.isSuccess());
        assertEquals("openai", output2.data().get("provider"));
    }

    @Test
    void testLLMCallWithDifferentModels() throws ExecutionException, InterruptedException {
        // Test Claude Sonnet 4
        LLMNode sonnetNode = new LLMNode("anthropic", "claude-sonnet-4", "Prompt");
        ExecutionContext context1 = new ExecutionContext();
        NodeInput input1 = new NodeInput(Map.of("message", "Test"), context1);
        NodeOutput output1 = sonnetNode.execute(input1).get();

        assertTrue(output1.isSuccess());
        assertEquals("claude-sonnet-4", output1.data().get("model"));

        // Test GPT-4
        LLMNode gptNode = new LLMNode("openai", "gpt-4-turbo", "Prompt");
        ExecutionContext context2 = new ExecutionContext();
        NodeInput input2 = new NodeInput(Map.of("message", "Test"), context2);
        NodeOutput output2 = gptNode.execute(input2).get();

        assertTrue(output2.isSuccess());
        assertEquals("gpt-4-turbo", output2.data().get("model"));
    }

    @Test
    void testLLMCallWithEmptyMessage() throws ExecutionException, InterruptedException {
        // Arrange
        LLMNode node = new LLMNode("anthropic", "claude-sonnet-4", "System prompt");
        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of("message", ""), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertTrue(output.data().containsKey("response"));
    }

    @Test
    void testLLMCallWithLongMessage() throws ExecutionException, InterruptedException {
        // Arrange
        String longMessage = "A".repeat(1000);
        LLMNode node = new LLMNode("anthropic", "claude-sonnet-4", "System prompt");
        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of("message", longMessage), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertTrue(output.data().containsKey("response"));
        String response = (String) output.data().get("response");
        assertNotNull(response);
    }

    @Test
    void testLLMCallWithSpecialCharacters() throws ExecutionException, InterruptedException {
        // Arrange
        String messageWithSpecialChars = "Hello! @#$%^&*()_+-=[]{}|;:',.<>?/~`";
        LLMNode node = new LLMNode("anthropic", "claude-sonnet-4", "System prompt");
        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of("message", messageWithSpecialChars), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertTrue(output.data().containsKey("response"));
    }

    @Test
    void testLLMCallOutputDataStructure() throws ExecutionException, InterruptedException {
        // Arrange
        LLMNode node = new LLMNode("anthropic", "claude-sonnet-4", "Test system prompt");
        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of("message", "Test message"), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);
        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals(3, output.data().size(), "Output should contain exactly 3 fields");
        assertTrue(output.data().containsKey("response"));
        assertTrue(output.data().containsKey("model"));
        assertTrue(output.data().containsKey("provider"));
    }
}
