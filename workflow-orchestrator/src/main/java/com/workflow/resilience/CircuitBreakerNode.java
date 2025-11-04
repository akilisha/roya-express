package com.akilisha.oss.roya.workflow.resilience;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Decorator node that wraps another node with circuit breaker protection.
 * Prevents cascading failures when calling unreliable external services.
 */
public class CircuitBreakerNode implements WorkflowNode {
    
    private final WorkflowNode delegate;
    private final CircuitBreaker circuitBreaker;
    private final boolean includeStats;
    
    /**
     * Wrap a node with circuit breaker protection
     */
    public CircuitBreakerNode(WorkflowNode delegate, CircuitBreaker circuitBreaker) {
        this(delegate, circuitBreaker, false);
    }
    
    /**
     * Wrap a node with circuit breaker and optional stats in output
     */
    public CircuitBreakerNode(WorkflowNode delegate, CircuitBreaker circuitBreaker, boolean includeStats) {
        this.delegate = delegate;
        this.circuitBreaker = circuitBreaker;
        this.includeStats = includeStats;
    }
    
    /**
     * Create with default circuit breaker (5 failures, 1 minute timeout)
     */
    public static CircuitBreakerNode withDefaults(WorkflowNode delegate) {
        return new CircuitBreakerNode(delegate, CircuitBreaker.withDefaults());
    }
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        // Check if circuit is open
        if (circuitBreaker.isOpen()) {
            CircuitBreaker.CircuitBreakerStats stats = circuitBreaker.getStats();
            return CompletableFuture.completedFuture(
                NodeOutput.failure(
                    String.format("Circuit breaker is OPEN (failures: %d/%d). Service unavailable.",
                        stats.failureCount(), stats.failureThreshold())
                )
            );
        }
        
        // Execute delegate node
        return delegate.execute(input)
            .handle((output, ex) -> {
                if (ex != null || (output != null && output.isFailure())) {
                    // Record failure
                    circuitBreaker.recordFailure();
                    
                    if (output == null) {
                        output = NodeOutput.failure("Node execution threw exception: " + ex.getMessage());
                    }
                    
                    // Add circuit breaker info to output
                    if (includeStats) {
                        return addStatsToOutput(output);
                    }
                    return output;
                } else {
                    // Record success
                    circuitBreaker.recordSuccess();
                    
                    // Add circuit breaker info to output
                    if (includeStats) {
                        return addStatsToOutput(output);
                    }
                    return output;
                }
            });
    }
    
    private NodeOutput addStatsToOutput(NodeOutput output) {
        CircuitBreaker.CircuitBreakerStats stats = circuitBreaker.getStats();
        
        // Create new output with stats
        var dataWithStats = new java.util.HashMap<>(output.data());
        dataWithStats.put("circuitBreakerState", stats.state().name());
        dataWithStats.put("circuitBreakerFailures", stats.failureCount());
        dataWithStats.put("circuitBreakerSuccesses", stats.successCount());
        dataWithStats.put("circuitBreakerHealthy", stats.isHealthy());
        
        return new NodeOutput(dataWithStats, output.status(), output.error());
    }
    
    /**
     * Get the wrapped delegate node
     */
    public WorkflowNode getDelegate() {
        return delegate;
    }
    
    /**
     * Get the circuit breaker
     */
    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
    
    /**
     * Get current circuit breaker stats
     */
    public CircuitBreaker.CircuitBreakerStats getStats() {
        return circuitBreaker.getStats();
    }
    
    /**
     * Manually reset the circuit breaker
     */
    public void reset() {
        circuitBreaker.reset();
    }
    
    /**
     * Manually open the circuit breaker (for emergencies)
     */
    public void forceOpen() {
        circuitBreaker.forceOpen();
    }
}
