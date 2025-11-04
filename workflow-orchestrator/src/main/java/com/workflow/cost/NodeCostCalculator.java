package com.akilisha.oss.roya.workflow.cost;

import com.akilisha.oss.roya.workflow.core.NodeOutput;

import java.time.Duration;

/**
 * Calculator for determining the cost of executing a node.
 * Useful for tracking AI API costs, compute costs, etc.
 */
@FunctionalInterface
public interface NodeCostCalculator {
    
    /**
     * Calculate the cost of a node execution
     * 
     * @param nodeId The node that was executed
     * @param output The node's output
     * @param executionTime How long the node took to execute
     * @return Cost in dollars (or your preferred currency unit)
     */
    double calculateCost(String nodeId, NodeOutput output, Duration executionTime);
    
    /**
     * Fixed cost per execution (e.g., $0.002 per API call)
     */
    static NodeCostCalculator fixed(double costPerExecution) {
        return (nodeId, output, executionTime) -> costPerExecution;
    }
    
    /**
     * Cost based on execution time (e.g., $0.01 per second)
     */
    static NodeCostCalculator timeBasedCost(double costPerSecond) {
        return (nodeId, output, executionTime) -> 
            costPerSecond * executionTime.toMillis() / 1000.0;
    }
    
    /**
     * Cost based on output size (e.g., for AI tokens)
     */
    static NodeCostCalculator outputBased(String outputKey, double costPerUnit) {
        return (nodeId, output, executionTime) -> {
            Object value = output.data().get(outputKey);
            if (value instanceof Number) {
                return ((Number) value).doubleValue() * costPerUnit;
            }
            return 0.0;
        };
    }
    
    /**
     * Combined cost calculator (sum of multiple calculators)
     */
    static NodeCostCalculator combined(NodeCostCalculator... calculators) {
        return (nodeId, output, executionTime) -> {
            double total = 0.0;
            for (NodeCostCalculator calc : calculators) {
                total += calc.calculateCost(nodeId, output, executionTime);
            }
            return total;
        };
    }
    
    /**
     * No cost
     */
    static NodeCostCalculator free() {
        return (nodeId, output, executionTime) -> 0.0;
    }
}
