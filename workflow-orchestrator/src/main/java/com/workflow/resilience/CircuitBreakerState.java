package com.akilisha.oss.roya.workflow.resilience;

/**
 * State of a circuit breaker
 */
public enum CircuitBreakerState {
    /**
     * Circuit is closed - requests flow normally
     */
    CLOSED,
    
    /**
     * Circuit is open - requests are blocked, fail fast
     */
    OPEN,
    
    /**
     * Circuit is half-open - testing if service has recovered
     */
    HALF_OPEN
}
