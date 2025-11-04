package com.akilisha.oss.roya.workflow.retry;

/**
 * Strategy for calculating retry delays
 */
public enum BackoffStrategy {
    /**
     * No backoff - immediate retry (not recommended)
     */
    NONE,
    
    /**
     * Fixed delay between retries
     */
    FIXED,
    
    /**
     * Exponentially increasing delay
     */
    EXPONENTIAL,
    
    /**
     * Linearly increasing delay
     */
    LINEAR
}
