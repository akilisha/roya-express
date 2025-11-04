package com.akilisha.oss.roya.workflow.resilience;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit breaker pattern implementation for protecting against cascading failures.
 * Essential for workflows that call unreliable external services.
 */
public class CircuitBreaker {
    
    private final int failureThreshold;
    private final Duration resetTimeout;
    private final int halfOpenMaxAttempts;
    
    private final AtomicInteger failureCount;
    private final AtomicInteger successCount;
    private final AtomicReference<CircuitBreakerState> state;
    private final AtomicReference<Instant> lastFailureTime;
    private final AtomicInteger halfOpenAttempts;
    
    /**
     * Create circuit breaker with threshold and timeout
     * 
     * @param failureThreshold Number of failures before opening circuit
     * @param resetTimeout Duration to wait before attempting reset
     */
    public CircuitBreaker(int failureThreshold, Duration resetTimeout) {
        this(failureThreshold, resetTimeout, 1);
    }
    
    /**
     * Create circuit breaker with full configuration
     * 
     * @param failureThreshold Number of failures before opening circuit
     * @param resetTimeout Duration to wait before attempting reset
     * @param halfOpenMaxAttempts Number of attempts allowed in half-open state
     */
    public CircuitBreaker(int failureThreshold, Duration resetTimeout, int halfOpenMaxAttempts) {
        this.failureThreshold = failureThreshold;
        this.resetTimeout = resetTimeout;
        this.halfOpenMaxAttempts = halfOpenMaxAttempts;
        
        this.failureCount = new AtomicInteger(0);
        this.successCount = new AtomicInteger(0);
        this.state = new AtomicReference<>(CircuitBreakerState.CLOSED);
        this.lastFailureTime = new AtomicReference<>();
        this.halfOpenAttempts = new AtomicInteger(0);
    }
    
    /**
     * Create circuit breaker with defaults (5 failures, 1 minute timeout)
     */
    public static CircuitBreaker withDefaults() {
        return new CircuitBreaker(5, Duration.ofMinutes(1));
    }
    
    /**
     * Create circuit breaker with specific threshold and timeout
     */
    public static CircuitBreaker withThreshold(int failureThreshold, Duration resetTimeout) {
        return new CircuitBreaker(failureThreshold, resetTimeout);
    }
    
    /**
     * Check if circuit is currently open
     */
    public boolean isOpen() {
        CircuitBreakerState currentState = state.get();
        
        if (currentState == CircuitBreakerState.OPEN) {
            // Check if we should transition to half-open
            Instant lastFailure = lastFailureTime.get();
            if (lastFailure != null && 
                Duration.between(lastFailure, Instant.now()).compareTo(resetTimeout) >= 0) {
                // Attempt to transition to half-open
                if (state.compareAndSet(CircuitBreakerState.OPEN, CircuitBreakerState.HALF_OPEN)) {
                    halfOpenAttempts.set(0);
                    return false; // Allow request through
                }
            }
            return true; // Still open
        }
        
        return false;
    }
    
    /**
     * Check if circuit is closed (normal operation)
     */
    public boolean isClosed() {
        return state.get() == CircuitBreakerState.CLOSED;
    }
    
    /**
     * Check if circuit is half-open (testing)
     */
    public boolean isHalfOpen() {
        return state.get() == CircuitBreakerState.HALF_OPEN;
    }
    
    /**
     * Record a successful execution
     */
    public void recordSuccess() {
        successCount.incrementAndGet();
        
        CircuitBreakerState currentState = state.get();
        
        if (currentState == CircuitBreakerState.HALF_OPEN) {
            // Success in half-open state - close the circuit
            if (state.compareAndSet(CircuitBreakerState.HALF_OPEN, CircuitBreakerState.CLOSED)) {
                failureCount.set(0);
                halfOpenAttempts.set(0);
            }
        } else if (currentState == CircuitBreakerState.CLOSED) {
            // Reset failure count on success
            failureCount.set(0);
        }
    }
    
    /**
     * Record a failed execution
     */
    public void recordFailure() {
        lastFailureTime.set(Instant.now());
        int failures = failureCount.incrementAndGet();
        
        CircuitBreakerState currentState = state.get();
        
        if (currentState == CircuitBreakerState.HALF_OPEN) {
            // Failure in half-open state - reopen circuit
            state.set(CircuitBreakerState.OPEN);
            halfOpenAttempts.set(0);
        } else if (currentState == CircuitBreakerState.CLOSED && failures >= failureThreshold) {
            // Too many failures - open circuit
            state.set(CircuitBreakerState.OPEN);
        }
    }
    
    /**
     * Manually reset the circuit breaker
     */
    public void reset() {
        state.set(CircuitBreakerState.CLOSED);
        failureCount.set(0);
        successCount.set(0);
        halfOpenAttempts.set(0);
        lastFailureTime.set(null);
    }
    
    /**
     * Manually open the circuit (for testing or emergency)
     */
    public void forceOpen() {
        state.set(CircuitBreakerState.OPEN);
        lastFailureTime.set(Instant.now());
    }
    
    /**
     * Get current state
     */
    public CircuitBreakerState getState() {
        // Update state if needed before returning
        isOpen(); // This checks for half-open transition
        return state.get();
    }
    
    /**
     * Get current failure count
     */
    public int getFailureCount() {
        return failureCount.get();
    }
    
    /**
     * Get current success count
     */
    public int getSuccessCount() {
        return successCount.get();
    }
    
    /**
     * Get failure threshold
     */
    public int getFailureThreshold() {
        return failureThreshold;
    }
    
    /**
     * Get last failure time
     */
    public Instant getLastFailureTime() {
        return lastFailureTime.get();
    }
    
    /**
     * Get statistics
     */
    public CircuitBreakerStats getStats() {
        return new CircuitBreakerStats(
            state.get(),
            failureCount.get(),
            successCount.get(),
            failureThreshold,
            lastFailureTime.get()
        );
    }
    
    /**
     * Circuit breaker statistics
     */
    public record CircuitBreakerStats(
        CircuitBreakerState state,
        int failureCount,
        int successCount,
        int failureThreshold,
        Instant lastFailureTime
    ) {
        public boolean isHealthy() {
            return state == CircuitBreakerState.CLOSED;
        }
        
        public double failureRate() {
            int total = failureCount + successCount;
            if (total == 0) return 0.0;
            return (double) failureCount / total;
        }
        
        @Override
        public String toString() {
            return String.format("CircuitBreaker[state=%s, failures=%d/%d, successes=%d]",
                state, failureCount, failureThreshold, successCount);
        }
    }
}
