package com.akilisha.oss.roya.workflow.nested;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A node that executes multiple child workflows in parallel and aggregates their results.
 * 
 * This enables fan-out/fan-in patterns where a parent workflow spawns multiple
 * child workflows, waits for them to complete, and aggregates the results.
 */
public class NestedWorkflowNode implements WorkflowNode {
    
    private final List<Workflow> childWorkflows;
    private final ResultAggregator aggregator;
    private final NestedExecutionStrategy strategy;
    private final Duration timeout;
    
    /**
     * Create with default strategy (WAIT_FOR_ALL) and 5-minute timeout
     */
    public NestedWorkflowNode(List<Workflow> childWorkflows, ResultAggregator aggregator) {
        this(childWorkflows, aggregator, NestedExecutionStrategy.WAIT_FOR_ALL, Duration.ofMinutes(5));
    }
    
    /**
     * Create with custom strategy and default 5-minute timeout
     */
    public NestedWorkflowNode(
        List<Workflow> childWorkflows,
        ResultAggregator aggregator,
        NestedExecutionStrategy strategy
    ) {
        this(childWorkflows, aggregator, strategy, Duration.ofMinutes(5));
    }
    
    /**
     * Create with full configuration
     */
    public NestedWorkflowNode(
        List<Workflow> childWorkflows,
        ResultAggregator aggregator,
        NestedExecutionStrategy strategy,
        Duration timeout
    ) {
        if (childWorkflows == null || childWorkflows.isEmpty()) {
            throw new IllegalArgumentException("Must provide at least one child workflow");
        }
        this.childWorkflows = childWorkflows;
        this.aggregator = aggregator;
        this.strategy = strategy;
        this.timeout = timeout;
    }
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        // Get snapshot of parent context for children
        Map<String, Object> parentSnapshot = input.context().snapshot();
        
        // Execute all child workflows in parallel
        List<CompletableFuture<WorkflowResult>> childFutures = new ArrayList<>();
        
        for (Workflow workflow : childWorkflows) {
            CompletableFuture<WorkflowResult> future = executeChildWorkflow(workflow, parentSnapshot);
            childFutures.add(future);
        }
        
        // Apply execution strategy
        return applyStrategy(childFutures);
    }
    
    private CompletableFuture<WorkflowResult> executeChildWorkflow(
        Workflow workflow,
        Map<String, Object> parentContext
    ) {
        WorkflowExecutor childExecutor = new WorkflowExecutor(workflow);
        
        // Find a trigger node to start from
        String startNode = workflow.getTriggerNodes().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "Child workflow must have at least one trigger node"
            ));
        
        return childExecutor.executeFrom(startNode, parentContext)
            .orTimeout(timeout.toSeconds(), TimeUnit.SECONDS)
            .exceptionally(ex -> {
                // Create a failed result for timeout/error
                return createFailedResult(ex.getMessage());
            });
    }
    
    private CompletableFuture<NodeOutput> applyStrategy(
        List<CompletableFuture<WorkflowResult>> childFutures
    ) {
        switch (strategy) {
            case WAIT_FOR_ALL:
                return waitForAll(childFutures, true);
            
            case WAIT_FOR_ALL_BEST_EFFORT:
                return waitForAll(childFutures, false);
            
            case FIRST_SUCCESS:
                return firstSuccess(childFutures);
            
            case BEST_OF_ALL:
                return waitForAll(childFutures, false); // Same as best effort for execution
            
            default:
                return waitForAll(childFutures, true);
        }
    }
    
    private CompletableFuture<NodeOutput> waitForAll(
        List<CompletableFuture<WorkflowResult>> childFutures,
        boolean failOnAnyFailure
    ) {
        return CompletableFuture.allOf(childFutures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                List<WorkflowResult> results = childFutures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
                
                // Check for failures if required
                if (failOnAnyFailure) {
                    boolean anyFailed = results.stream().anyMatch(WorkflowResult::isFailure);
                    if (anyFailed) {
                        long failedCount = results.stream()
                            .filter(WorkflowResult::isFailure)
                            .count();
                        return NodeOutput.failure(
                            String.format("%d of %d child workflows failed", 
                                failedCount, results.size())
                        );
                    }
                }
                
                // Aggregate successful results
                Map<String, Object> aggregated = aggregator.aggregate(results);
                return NodeOutput.success(aggregated);
            })
            .exceptionally(ex -> 
                NodeOutput.failure("Nested workflow execution error: " + ex.getMessage())
            );
    }
    
    private CompletableFuture<NodeOutput> firstSuccess(
        List<CompletableFuture<WorkflowResult>> childFutures
    ) {
        // Use anyOf to return as soon as one completes successfully
        CompletableFuture<Object> anyComplete = CompletableFuture.anyOf(
            childFutures.toArray(new CompletableFuture[0])
        );
        
        return anyComplete.thenCompose(result -> {
            WorkflowResult firstResult = (WorkflowResult) result;
            
            if (firstResult.isSuccess()) {
                Map<String, Object> data = firstResult.finalOutput().data();
                data.putAll(firstResult.context().snapshot());
                return CompletableFuture.completedFuture(NodeOutput.success(data));
            }
            
            // If first to complete failed, wait for all and check if any succeeded
            return CompletableFuture.allOf(childFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<WorkflowResult> results = childFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
                    
                    // Find first successful result
                    return results.stream()
                        .filter(WorkflowResult::isSuccess)
                        .findFirst()
                        .map(success -> {
                            Map<String, Object> data = success.finalOutput().data();
                            data.putAll(success.context().snapshot());
                            return NodeOutput.success(data);
                        })
                        .orElse(NodeOutput.failure("All child workflows failed"));
                });
        });
    }
    
    private WorkflowResult createFailedResult(String error) {
        // Create a minimal failed result for error cases
        return new WorkflowResult(
            NodeOutput.failure(error),
            new com.akilisha.oss.roya.workflow.core.ExecutionContext()
        );
    }
    
    /**
     * Get the child workflows
     */
    public List<Workflow> getChildWorkflows() {
        return List.copyOf(childWorkflows);
    }
    
    /**
     * Get the result aggregator
     */
    public ResultAggregator getAggregator() {
        return aggregator;
    }
    
    /**
     * Get the execution strategy
     */
    public NestedExecutionStrategy getStrategy() {
        return strategy;
    }
    
    /**
     * Get the timeout duration
     */
    public Duration getTimeout() {
        return timeout;
    }
}
