package com.akilisha.oss.roya.workflow.hitm;

import com.akilisha.oss.roya.workflow.core.ExecutionContext;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HumanApprovalNode
 */
class HumanApprovalNodeTest {

    private PollingApprovalProvider provider;

    @BeforeEach
    void setUp() {
        provider = new PollingApprovalProvider(Duration.ofMillis(100), Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown() {
        provider.shutdown();
    }

    @Test
    void testApprovalNodeApproved() throws ExecutionException, InterruptedException {
        // Arrange
        HumanApprovalNode node = new HumanApprovalNode(provider, "Approve this action?");
        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);

        // Simulate human approval after short delay
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(200);
                // Extract requestId from pending requests
                Map<String, PollingApprovalProvider.PendingRequest> pending = provider.getPendingRequests();
                if (!pending.isEmpty()) {
                    String requestId = pending.keySet().iterator().next();
                    provider.submitApproval(requestId, true);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertTrue((Boolean) output.data().get("approved"));
        assertNotNull(output.data().get("requestId"));
    }

    @Test
    void testApprovalNodeRejected() throws ExecutionException, InterruptedException {
        // Arrange
        HumanApprovalNode node = new HumanApprovalNode(provider, "Approve this action?");
        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);

        // Simulate human rejection
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(200);
                Map<String, PollingApprovalProvider.PendingRequest> pending = provider.getPendingRequests();
                if (!pending.isEmpty()) {
                    String requestId = pending.keySet().iterator().next();
                    provider.submitApproval(requestId, false);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isFailure());
        assertTrue(output.error().get().contains("Human rejected"));
    }

    @Test
    void testInputNode() throws ExecutionException, InterruptedException {
        // Arrange
        HumanApprovalNode node = HumanApprovalNode.forInput(provider, "Enter your name:");
        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);

        // Simulate human input
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(200);
                Map<String, PollingApprovalProvider.PendingRequest> pending = provider.getPendingRequests();
                if (!pending.isEmpty()) {
                    String requestId = pending.keySet().iterator().next();
                    provider.submitInput(requestId, "John Doe");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals("John Doe", output.data().get("humanInput"));
        assertNotNull(output.data().get("requestId"));
    }

    @Test
    void testChoiceNode() throws ExecutionException, InterruptedException {
        // Arrange
        String[] options = {"Option A", "Option B", "Option C"};
        HumanApprovalNode node = HumanApprovalNode.forChoice(
            provider,
            "Choose an option:",
            options
        );
        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);

        // Simulate human choice
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(200);
                Map<String, PollingApprovalProvider.PendingRequest> pending = provider.getPendingRequests();
                if (!pending.isEmpty()) {
                    String requestId = pending.keySet().iterator().next();
                    provider.submitChoice(requestId, 1); // Choose "Option B"
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals(1, output.data().get("choiceIndex"));
        assertEquals("Option B", output.data().get("choiceValue"));
        assertNotNull(output.data().get("requestId"));
    }

    @Test
    void testApprovalNodeWithContextData() throws ExecutionException, InterruptedException {
        // Arrange
        HumanApprovalNode node = new HumanApprovalNode(provider, "Approve transaction?");
        ExecutionContext context = new ExecutionContext();
        context.set("amount", 100.00);
        context.set("recipient", "Alice");
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);

        // Verify context is passed to provider
        Map<String, PollingApprovalProvider.PendingRequest> pending = provider.getPendingRequests();
        assertFalse(pending.isEmpty());

        PollingApprovalProvider.PendingRequest request = pending.values().iterator().next();
        assertEquals(100.00, request.getContext().get("amount"));
        assertEquals("Alice", request.getContext().get("recipient"));

        // Complete the approval
        provider.submitApproval(request.getRequestId(), true);
        NodeOutput output = future.get();

        assertTrue(output.isSuccess());
    }

    @Test
    void testApprovalNodeGetters() {
        // Arrange
        String[] options = {"A", "B", "C"};
        HumanApprovalNode booleanNode = new HumanApprovalNode(provider, "Approve?");
        HumanApprovalNode inputNode = HumanApprovalNode.forInput(provider, "Input:");
        HumanApprovalNode choiceNode = HumanApprovalNode.forChoice(provider, "Choose:", options);

        // Assert
        assertEquals(provider, booleanNode.getProvider());
        assertEquals("Approve?", booleanNode.getPrompt());
        assertEquals(HumanApprovalNode.ApprovalType.BOOLEAN, booleanNode.getType());
        assertNull(booleanNode.getOptions());

        assertEquals(HumanApprovalNode.ApprovalType.TEXT, inputNode.getType());

        assertEquals(HumanApprovalNode.ApprovalType.CHOICE, choiceNode.getType());
        assertArrayEquals(options, choiceNode.getOptions());
    }

    @Test
    void testInputNodeWithEmptyString() throws ExecutionException, InterruptedException {
        // Arrange
        HumanApprovalNode node = HumanApprovalNode.forInput(provider, "Optional input:");
        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);

        // Simulate human providing empty string
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(200);
                Map<String, PollingApprovalProvider.PendingRequest> pending = provider.getPendingRequests();
                if (!pending.isEmpty()) {
                    String requestId = pending.keySet().iterator().next();
                    provider.submitInput(requestId, "");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals("", output.data().get("humanInput"));
    }

    @Test
    void testChoiceNodeWithFirstOption() throws ExecutionException, InterruptedException {
        // Arrange
        String[] options = {"First", "Second", "Third"};
        HumanApprovalNode node = HumanApprovalNode.forChoice(provider, "Choose:", options);
        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(200);
                Map<String, PollingApprovalProvider.PendingRequest> pending = provider.getPendingRequests();
                if (!pending.isEmpty()) {
                    String requestId = pending.keySet().iterator().next();
                    provider.submitChoice(requestId, 0);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals(0, output.data().get("choiceIndex"));
        assertEquals("First", output.data().get("choiceValue"));
    }

    @Test
    void testChoiceNodeWithLastOption() throws ExecutionException, InterruptedException {
        // Arrange
        String[] options = {"First", "Second", "Third"};
        HumanApprovalNode node = HumanApprovalNode.forChoice(provider, "Choose:", options);
        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(200);
                Map<String, PollingApprovalProvider.PendingRequest> pending = provider.getPendingRequests();
                if (!pending.isEmpty()) {
                    String requestId = pending.keySet().iterator().next();
                    provider.submitChoice(requestId, 2);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        NodeOutput output = future.get();

        // Assert
        assertTrue(output.isSuccess());
        assertEquals(2, output.data().get("choiceIndex"));
        assertEquals("Third", output.data().get("choiceValue"));
    }

    @Test
    void testApprovalNodeCancellation() {
        // Arrange
        HumanApprovalNode node = new HumanApprovalNode(provider, "Approve?");
        ExecutionContext context = new ExecutionContext();
        NodeInput input = new NodeInput(Map.of(), context);

        // Act
        CompletableFuture<NodeOutput> future = node.execute(input);

        // Get request ID and cancel
        Map<String, PollingApprovalProvider.PendingRequest> pending = provider.getPendingRequests();
        assertFalse(pending.isEmpty());
        String requestId = pending.keySet().iterator().next();

        provider.cancelRequest(requestId);

        // Assert
        assertTrue(provider.getPendingRequests().isEmpty());
    }
}
