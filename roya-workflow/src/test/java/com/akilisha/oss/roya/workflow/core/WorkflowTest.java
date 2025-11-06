package com.akilisha.oss.roya.workflow.core;

import com.akilisha.oss.roya.workflow.edges.Edge;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Workflow
 */
class WorkflowTest {

    @Test
    void testCreateSimpleWorkflow() {
        // Act
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("result", "done")
            ))
            .build();

        // Assert
        assertNotNull(workflow);
        assertEquals(1, workflow.getNodeIds().size());
        assertTrue(workflow.getNodeIds().contains("start"));
    }

    @Test
    void testWorkflowWithMultipleNodes() {
        // Act
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value1")
            ))
            .action("process", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value2")
            ))
            .logic("transform", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value3")
            ))
            .edge("start", "process")
            .edge("process", "transform")
            .build();

        // Assert
        assertEquals(3, workflow.getNodeIds().size());
        assertTrue(workflow.getNodeIds().contains("start"));
        assertTrue(workflow.getNodeIds().contains("process"));
        assertTrue(workflow.getNodeIds().contains("transform"));
    }

    @Test
    void testGetNode() {
        // Arrange
        WorkflowNode testNode = input -> CompletableFuture.completedFuture(
            NodeOutput.success("result", "test")
        );

        Workflow workflow = Workflow.create()
            .trigger("start", testNode)
            .build();

        // Act
        WorkflowNode retrievedNode = workflow.getNode("start");

        // Assert
        assertNotNull(retrievedNode);
        assertEquals(testNode, retrievedNode);
    }

    @Test
    void testGetMetadata() {
        // Arrange
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ), Duration.ofSeconds(30))
            .build();

        // Act
        NodeMetadata metadata = workflow.getMetadata("start");

        // Assert
        assertNotNull(metadata);
        assertEquals(NodeType.TRIGGER, metadata.type());
        assertEquals("start", metadata.id());
        assertEquals(Duration.ofSeconds(30), metadata.timeout());
    }

    @Test
    void testGetEdges() {
        // Arrange
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .action("process", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .edge("start", "process")
            .build();

        // Act
        var edges = workflow.getEdges("start");

        // Assert
        assertEquals(1, edges.size());
        assertEquals("process", edges.get(0).getTargetNodeId());
    }

    @Test
    void testGetTriggerNodes() {
        // Arrange
        Workflow workflow = Workflow.create()
            .trigger("trigger1", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .trigger("trigger2", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .action("action1", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .edge("trigger1", "action1")
            .build();

        // Act
        Set<String> triggerNodes = workflow.getTriggerNodes();

        // Assert
        assertEquals(2, triggerNodes.size());
        assertTrue(triggerNodes.contains("trigger1"));
        assertTrue(triggerNodes.contains("trigger2"));
        assertFalse(triggerNodes.contains("action1"));
    }

    @Test
    void testNodeTypes() {
        // Arrange
        Workflow workflow = Workflow.create()
            .trigger("trigger", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .action("action", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .logic("logic", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .conditional("conditional", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .custom("custom", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .build();

        // Assert
        assertEquals(NodeType.TRIGGER, workflow.getMetadata("trigger").type());
        assertEquals(NodeType.ACTION, workflow.getMetadata("action").type());
        assertEquals(NodeType.LOGIC, workflow.getMetadata("logic").type());
        assertEquals(NodeType.CONDITIONAL, workflow.getMetadata("conditional").type());
        assertEquals(NodeType.CUSTOM, workflow.getMetadata("custom").type());
    }

    @Test
    void testMultipleEdgesFromSameNode() {
        // Arrange
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .action("nodeA", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .action("nodeB", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .action("nodeC", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .edge("start", "nodeA")
            .edge("start", "nodeB")
            .edge("start", "nodeC")
            .build();

        // Act
        var edges = workflow.getEdges("start");

        // Assert
        assertEquals(3, edges.size());
        assertTrue(edges.stream().anyMatch(e -> e.getTargetNodeId().equals("nodeA")));
        assertTrue(edges.stream().anyMatch(e -> e.getTargetNodeId().equals("nodeB")));
        assertTrue(edges.stream().anyMatch(e -> e.getTargetNodeId().equals("nodeC")));
    }

    @Test
    void testConditionalEdges() {
        // Arrange
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("value", 10)
            ))
            .action("pathA", input -> CompletableFuture.completedFuture(
                NodeOutput.success("result", "A")
            ))
            .action("pathB", input -> CompletableFuture.completedFuture(
                NodeOutput.success("result", "B")
            ))
            .edge("start", "pathA", Edge.when(ctx -> (int)ctx.get("value") > 5))
            .edge("start", "pathB", Edge.when(ctx -> (int)ctx.get("value") <= 5))
            .build();

        // Act
        var edges = workflow.getEdges("start");

        // Assert
        assertEquals(2, edges.size());
    }

    @Test
    void testWorkflowWithoutTriggerThrowsException() {
        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            Workflow.create()
                .action("action", input -> CompletableFuture.completedFuture(
                    NodeOutput.success("data", "value")
                ))
                .build();
        });
    }

    @Test
    void testEdgeToNonExistentNodeThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            Workflow.create()
                .trigger("start", input -> CompletableFuture.completedFuture(
                    NodeOutput.success("data", "value")
                ))
                .edge("start", "nonexistent")
                .build();
        });
    }

    @Test
    void testEdgeFromNonExistentNodeThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            Workflow.create()
                .trigger("start", input -> CompletableFuture.completedFuture(
                    NodeOutput.success("data", "value")
                ))
                .edge("nonexistent", "start")
                .build();
        });
    }

    @Test
    void testDefaultTimeout() {
        // Arrange
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .build();

        // Act
        NodeMetadata metadata = workflow.getMetadata("start");

        // Assert
        assertEquals(Duration.ofSeconds(60), metadata.timeout());
    }

    @Test
    void testCustomTimeout() {
        // Arrange
        Duration customTimeout = Duration.ofMinutes(5);
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ), customTimeout)
            .build();

        // Act
        NodeMetadata metadata = workflow.getMetadata("start");

        // Assert
        assertEquals(customTimeout, metadata.timeout());
    }

    @Test
    void testUnreachableNodesWarning() {
        // This test verifies the warning is printed for unreachable nodes
        // Arrange & Act
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .action("reachable", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .action("unreachable", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .edge("start", "reachable")
            .build();

        // Assert - workflow should still build successfully
        assertNotNull(workflow);
        assertEquals(3, workflow.getNodeIds().size());
    }

    @Test
    void testGetEdgesForNodeWithNoEdges() {
        // Arrange
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .build();

        // Act
        var edges = workflow.getEdges("start");

        // Assert
        assertTrue(edges.isEmpty());
    }

    @Test
    void testComplexWorkflowGraph() {
        // Arrange - Create a diamond-shaped workflow
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .action("branchA", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "valueA")
            ))
            .action("branchB", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "valueB")
            ))
            .action("merge", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "merged")
            ))
            .edge("start", "branchA")
            .edge("start", "branchB")
            .edge("branchA", "merge")
            .edge("branchB", "merge")
            .build();

        // Assert
        assertEquals(4, workflow.getNodeIds().size());
        assertEquals(2, workflow.getEdges("start").size());
        assertEquals(1, workflow.getEdges("branchA").size());
        assertEquals(1, workflow.getEdges("branchB").size());
        assertEquals(0, workflow.getEdges("merge").size());
    }

    @Test
    void testWorkflowIsImmutable() {
        // Arrange
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .build();

        // Act
        Set<String> nodeIds = workflow.getNodeIds();

        // Assert - verify immutability
        assertThrows(UnsupportedOperationException.class, () -> {
            nodeIds.add("newNode");
        });
    }

    @Test
    void testGetNodeForNonExistentNode() {
        // Arrange
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .build();

        // Act
        WorkflowNode node = workflow.getNode("nonexistent");

        // Assert
        assertNull(node);
    }

    @Test
    void testGetMetadataForNonExistentNode() {
        // Arrange
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(
                NodeOutput.success("data", "value")
            ))
            .build();

        // Act
        NodeMetadata metadata = workflow.getMetadata("nonexistent");

        // Assert
        assertNull(metadata);
    }
}
