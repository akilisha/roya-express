package com.akilisha.oss.roya.workflow.cost;

/**
 * Exception thrown when workflow execution exceeds the configured budget
 */
public class BudgetExceededException extends RuntimeException {
    
    private final double totalCost;
    private final double budgetLimit;
    
    public BudgetExceededException(double totalCost, double budgetLimit) {
        super(String.format("Budget exceeded: $%.4f spent, limit was $%.4f", totalCost, budgetLimit));
        this.totalCost = totalCost;
        this.budgetLimit = budgetLimit;
    }
    
    public double getTotalCost() {
        return totalCost;
    }
    
    public double getBudgetLimit() {
        return budgetLimit;
    }
    
    public double getOverage() {
        return totalCost - budgetLimit;
    }
}
