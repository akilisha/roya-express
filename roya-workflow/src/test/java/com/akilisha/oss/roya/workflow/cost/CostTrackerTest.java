package com.akilisha.oss.roya.workflow.cost;

import com.akilisha.oss.roya.workflow.core.ExecutionContext;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CostTracker
 */
class CostTrackerTest {

    @Test
    void testInitialState() {
        // Arrange
        CostTracker tracker = new CostTracker(10.00);

        // Assert
        assertEquals(0.0, tracker.getTotalCost());
        assertEquals(10.00, tracker.getRemainingBudget());
        assertFalse(tracker.isBudgetExceeded());
    }

    @Test
    void testFixedCostTracking() {
        // Arrange
        CostTracker tracker = new CostTracker(10.00)
            .withNodeCost("node1", 1.50);

        NodeOutput output = NodeOutput.success("result", "data");
        Duration executionTime = Duration.ofSeconds(1);

        // Act
        tracker.onNodeComplete("node1", output, executionTime);

        // Assert
        assertEquals(1.50, tracker.getTotalCost(), 0.001);
        assertEquals(8.50, tracker.getRemainingBudget(), 0.001);
        assertFalse(tracker.isBudgetExceeded());
    }

    @Test
    void testMultipleNodeCosts() {
        // Arrange
        CostTracker tracker = new CostTracker(10.00)
            .withNodeCost("node1", 2.00)
            .withNodeCost("node2", 3.00);

        NodeOutput output = NodeOutput.success("result", "data");
        Duration executionTime = Duration.ofSeconds(1);

        // Act
        tracker.onNodeComplete("node1", output, executionTime);
        tracker.onNodeComplete("node2", output, executionTime);

        // Assert
        assertEquals(5.00, tracker.getTotalCost(), 0.001);
        assertEquals(5.00, tracker.getRemainingBudget(), 0.001);
        assertFalse(tracker.isBudgetExceeded());
    }

    @Test
    void testBudgetExceededInStrictMode() {
        // Arrange
        CostTracker tracker = new CostTracker(5.00)
            .withNodeCost("expensive", 6.00);

        NodeOutput output = NodeOutput.success("result", "data");
        Duration executionTime = Duration.ofSeconds(1);

        // Act & Assert
        assertThrows(BudgetExceededException.class, () -> {
            tracker.onNodeComplete("expensive", output, executionTime);
        });
    }

    @Test
    void testBudgetExceededInWarningMode() {
        // Arrange
        CostTracker tracker = new CostTracker(5.00, NodeCostCalculator.free(), false);
        tracker.withNodeCost("expensive", 6.00);

        NodeOutput output = NodeOutput.success("result", "data");
        Duration executionTime = Duration.ofSeconds(1);

        // Act - should not throw exception
        assertDoesNotThrow(() -> {
            tracker.onNodeComplete("expensive", output, executionTime);
        });

        // Assert
        assertEquals(6.00, tracker.getTotalCost(), 0.001);
        assertTrue(tracker.isBudgetExceeded());
    }

    @Test
    void testNodeCostAccumulation() {
        // Arrange
        CostTracker tracker = new CostTracker(20.00)
            .withNodeCost("repeated", 2.00);

        NodeOutput output = NodeOutput.success("result", "data");
        Duration executionTime = Duration.ofSeconds(1);

        // Act - call same node multiple times
        tracker.onNodeComplete("repeated", output, executionTime);
        tracker.onNodeComplete("repeated", output, executionTime);
        tracker.onNodeComplete("repeated", output, executionTime);

        // Assert
        assertEquals(6.00, tracker.getTotalCost(), 0.001);
        Map<String, Double> nodeCosts = tracker.getNodeCosts();
        assertEquals(6.00, nodeCosts.get("repeated"), 0.001);
    }

    @Test
    void testGetReport() {
        // Arrange
        CostTracker tracker = new CostTracker(10.00)
            .withNodeCost("node1", 2.00)
            .withNodeCost("node2", 3.00);

        NodeOutput output = NodeOutput.success("result", "data");
        Duration executionTime = Duration.ofSeconds(1);

        tracker.onNodeComplete("node1", output, executionTime);
        tracker.onNodeComplete("node2", output, executionTime);
        tracker.onNodeComplete("node1", output, executionTime);

        // Act
        CostTracker.CostReport report = tracker.getReport();

        // Assert
        assertNotNull(report);
        assertEquals(7.00, report.totalCost(), 0.001);
        assertEquals(10.00, report.budgetLimit(), 0.001);
        assertEquals(3.00, report.remainingBudget(), 0.001);
        assertEquals(70.0, report.utilizationPercent(), 0.1);
        assertFalse(report.budgetExceeded());

        String reportString = report.toString();
        assertTrue(reportString.contains("7.0"));
        assertTrue(reportString.contains("10.0"));
    }

    @Test
    void testMostExpensiveNode() {
        // Arrange
        CostTracker tracker = new CostTracker(20.00)
            .withNodeCost("cheap", 1.00)
            .withNodeCost("expensive", 5.00);

        NodeOutput output = NodeOutput.success("result", "data");
        Duration executionTime = Duration.ofSeconds(1);

        tracker.onNodeComplete("cheap", output, executionTime);
        tracker.onNodeComplete("expensive", output, executionTime);

        // Act
        CostTracker.CostReport report = tracker.getReport();
        String mostExpensive = report.mostExpensiveNode();

        // Assert
        assertEquals("expensive", mostExpensive);
    }

    @Test
    void testNodeCostMap() {
        // Arrange
        CostTracker tracker = new CostTracker(20.00)
            .withNodeCost("node1", 2.00)
            .withNodeCost("node2", 3.00)
            .withNodeCost("node3", 1.50);

        NodeOutput output = NodeOutput.success("result", "data");
        Duration executionTime = Duration.ofSeconds(1);

        tracker.onNodeComplete("node1", output, executionTime);
        tracker.onNodeComplete("node2", output, executionTime);
        tracker.onNodeComplete("node3", output, executionTime);

        // Act
        Map<String, Double> nodeCostMap = tracker.getNodeCosts();

        // Assert
        assertEquals(3, nodeCostMap.size());
        assertEquals(2.00, nodeCostMap.get("node1"), 0.001);
        assertEquals(3.00, nodeCostMap.get("node2"), 0.001);
        assertEquals(1.50, nodeCostMap.get("node3"), 0.001);
    }

    @Test
    void testDefaultCalculator() {
        // Arrange
        CostTracker tracker = new CostTracker(10.00)
            .withNodeCost("node1", 2.00);
        // node2 has no specific calculator, uses default (free)

        NodeOutput output = NodeOutput.success("result", "data");
        Duration executionTime = Duration.ofSeconds(1);

        // Act
        tracker.onNodeComplete("node1", output, executionTime);
        tracker.onNodeComplete("node2", output, executionTime);

        // Assert
        Map<String, Double> nodeCosts = tracker.getNodeCosts();
        assertEquals(2.00, tracker.getTotalCost(), 0.001);
        assertEquals(2.00, nodeCosts.get("node1"), 0.001);
        // Free nodes may not appear in the cost map
    }

    @Test
    void testOnWorkflowComplete() {
        // Arrange
        CostTracker tracker = new CostTracker(10.00)
            .withNodeCost("node1", 3.00);

        NodeOutput output = NodeOutput.success("result", "data");
        Duration executionTime = Duration.ofSeconds(1);

        tracker.onNodeComplete("node1", output, executionTime);

        ExecutionContext context = new ExecutionContext();
        WorkflowResult result = new WorkflowResult(output, context);

        // Act
        tracker.onWorkflowComplete("workflow1", result);

        // Assert
        assertEquals(3.00, context.get("totalCost"));
        assertEquals(10.00, context.get("budgetLimit"));
        assertEquals(7.00, context.get("budgetRemaining"));
    }

    @Test
    void testBudgetUtilization() {
        // Arrange
        CostTracker tracker = new CostTracker(10.00)
            .withNodeCost("node1", 2.50);

        NodeOutput output = NodeOutput.success("result", "data");
        Duration executionTime = Duration.ofSeconds(1);

        // Act
        tracker.onNodeComplete("node1", output, executionTime);

        // Assert
        assertEquals(25.0, tracker.getBudgetUtilization(), 0.1); // 2.5/10 = 25%
    }

    @Test
    void testIsBudgetExceeded() {
        // Arrange
        CostTracker tracker = new CostTracker(10.00, NodeCostCalculator.free(), false);
        tracker.withNodeCost("node1", 5.00);

        NodeOutput output = NodeOutput.success("result", "data");
        Duration executionTime = Duration.ofSeconds(1);

        // Act & Assert
        assertFalse(tracker.isBudgetExceeded());

        tracker.onNodeComplete("node1", output, executionTime);
        assertFalse(tracker.isBudgetExceeded()); // 5/10

        tracker.onNodeComplete("node1", output, executionTime);
        assertFalse(tracker.isBudgetExceeded()); // 10/10 - not exceeded, at limit

        tracker.onNodeComplete("node1", output, executionTime);
        assertTrue(tracker.isBudgetExceeded()); // 15/10
    }

    @Test
    void testCustomCostCalculator() {
        // Arrange
        NodeCostCalculator customCalculator = (nodeId, output, duration) -> {
            // Cost based on execution time: $0.01 per second
            return duration.toMillis() / 1000.0 * 0.01;
        };

        CostTracker tracker = new CostTracker(10.00)
            .withNodeCost("timedNode", customCalculator);

        NodeOutput output = NodeOutput.success("result", "data");
        Duration executionTime = Duration.ofSeconds(5);

        // Act
        tracker.onNodeComplete("timedNode", output, executionTime);

        // Assert
        assertEquals(0.05, tracker.getTotalCost(), 0.001); // 5 seconds * $0.01
    }

    @Test
    void testOutputBasedCostCalculator() {
        // Arrange
        // Cost based on output size (e.g., AI tokens)
        NodeCostCalculator outputCalculator = NodeCostCalculator.outputBased(
            output -> {
                String text = (String) output.data().get("text");
                return text != null ? text.length() : 0;
            },
            0.0001 // $0.0001 per character
        );

        CostTracker tracker = new CostTracker(10.00)
            .withNodeCost("llmNode", outputCalculator);

        NodeOutput output = NodeOutput.success("text", "A".repeat(1000)); // 1000 chars
        Duration executionTime = Duration.ofSeconds(1);

        // Act
        tracker.onNodeComplete("llmNode", output, executionTime);

        // Assert
        assertEquals(0.1, tracker.getTotalCost(), 0.001); // 1000 * 0.0001
    }

    @Test
    void testEmptyReport() {
        // Arrange
        CostTracker tracker = new CostTracker(10.00);

        // Act
        CostTracker.CostReport report = tracker.getReport();

        // Assert
        assertNotNull(report);
        assertEquals(0.00, report.totalCost(), 0.001);
        assertEquals(10.00, report.budgetLimit(), 0.001);
        assertEquals("none", report.mostExpensiveNode());
        assertFalse(report.budgetExceeded());
    }

    @Test
    void testResetTracker() {
        // Arrange
        CostTracker tracker = new CostTracker(10.00)
            .withNodeCost("node1", 2.00);

        NodeOutput output = NodeOutput.success("result", "data");
        Duration executionTime = Duration.ofSeconds(1);

        tracker.onNodeComplete("node1", output, executionTime);
        assertEquals(2.00, tracker.getTotalCost(), 0.001);

        // Act
        tracker.reset();

        // Assert
        assertEquals(0.00, tracker.getTotalCost());
        assertTrue(tracker.getNodeCosts().isEmpty());
    }
}
