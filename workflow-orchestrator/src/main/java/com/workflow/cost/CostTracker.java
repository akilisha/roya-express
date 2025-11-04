package com.akilisha.oss.roya.workflow.cost;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import com.akilisha.oss.roya.workflow.visitor.WorkflowVisitor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Workflow visitor that tracks costs and enforces budget limits.
 * Essential for controlling AI API costs.
 */
public class CostTracker implements WorkflowVisitor {
    
    private final Map<String, NodeCostCalculator> nodeCostCalculators;
    private final NodeCostCalculator defaultCalculator;
    private final double budgetLimit;
    private final AtomicReference<Double> totalCost;
    private final Map<String, Double> nodeCosts;
    private final boolean strictMode;
    
    /**
     * Create cost tracker with budget limit
     * 
     * @param budgetLimit Maximum allowed cost in dollars
     */
    public CostTracker(double budgetLimit) {
        this(budgetLimit, NodeCostCalculator.free(), true);
    }
    
    /**
     * Create cost tracker with budget and default calculator
     * 
     * @param budgetLimit Maximum allowed cost
     * @param defaultCalculator Calculator to use when node doesn't have specific calculator
     * @param strictMode If true, throw exception when budget exceeded; if false, just warn
     */
    public CostTracker(double budgetLimit, NodeCostCalculator defaultCalculator, boolean strictMode) {
        this.budgetLimit = budgetLimit;
        this.defaultCalculator = defaultCalculator;
        this.strictMode = strictMode;
        this.totalCost = new AtomicReference<>(0.0);
        this.nodeCosts = new ConcurrentHashMap<>();
        this.nodeCostCalculators = new ConcurrentHashMap<>();
    }
    
    /**
     * Register a cost calculator for a specific node
     */
    public CostTracker withNodeCost(String nodeId, NodeCostCalculator calculator) {
        nodeCostCalculators.put(nodeId, calculator);
        return this;
    }
    
    /**
     * Register a fixed cost for a specific node
     */
    public CostTracker withNodeCost(String nodeId, double costPerExecution) {
        nodeCostCalculators.put(nodeId, NodeCostCalculator.fixed(costPerExecution));
        return this;
    }
    
    @Override
    public void onNodeComplete(String nodeId, NodeOutput output, Duration executionTime) {
        // Get calculator for this node
        NodeCostCalculator calculator = nodeCostCalculators.getOrDefault(nodeId, defaultCalculator);
        
        // Calculate cost
        double cost = calculator.calculateCost(nodeId, output, executionTime);
        
        // Update totals
        totalCost.updateAndGet(current -> current + cost);
        nodeCosts.merge(nodeId, cost, Double::sum);
        
        // Check budget
        double current = totalCost.get();
        if (current > budgetLimit) {
            if (strictMode) {
                throw new BudgetExceededException(current, budgetLimit);
            } else {
                System.err.printf("WARNING: Budget exceeded! Current: $%.4f, Limit: $%.4f%n", 
                    current, budgetLimit);
            }
        }
    }
    
    @Override
    public void onWorkflowComplete(String workflowId, WorkflowResult result) {
        // Add cost summary to result context if possible
        result.context().set("totalCost", totalCost.get());
        result.context().set("budgetLimit", budgetLimit);
        result.context().set("budgetRemaining", budgetLimit - totalCost.get());
    }
    
    /**
     * Get the current total cost
     */
    public double getTotalCost() {
        return totalCost.get();
    }
    
    /**
     * Get cost breakdown by node
     */
    public Map<String, Double> getNodeCosts() {
        return Map.copyOf(nodeCosts);
    }
    
    /**
     * Get remaining budget
     */
    public double getRemainingBudget() {
        return Math.max(0, budgetLimit - totalCost.get());
    }
    
    /**
     * Check if budget has been exceeded
     */
    public boolean isBudgetExceeded() {
        return totalCost.get() > budgetLimit;
    }
    
    /**
     * Get budget utilization percentage
     */
    public double getBudgetUtilization() {
        return (totalCost.get() / budgetLimit) * 100.0;
    }
    
    /**
     * Reset cost tracking (for reusing tracker)
     */
    public void reset() {
        totalCost.set(0.0);
        nodeCosts.clear();
    }
    
    /**
     * Get summary report
     */
    public CostReport getReport() {
        return new CostReport(
            totalCost.get(),
            budgetLimit,
            Map.copyOf(nodeCosts),
            isBudgetExceeded()
        );
    }
    
    /**
     * Cost report summary
     */
    public record CostReport(
        double totalCost,
        double budgetLimit,
        Map<String, Double> nodeCosts,
        boolean budgetExceeded
    ) {
        public double remainingBudget() {
            return Math.max(0, budgetLimit - totalCost);
        }
        
        public double utilizationPercent() {
            return (totalCost / budgetLimit) * 100.0;
        }
        
        public String mostExpensiveNode() {
            return nodeCosts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("none");
        }
        
        @Override
        public String toString() {
            return String.format(
                "Cost Report: $%.4f / $%.4f (%.1f%%) - Most expensive: %s",
                totalCost, budgetLimit, utilizationPercent(), mostExpensiveNode()
            );
        }
    }
}
