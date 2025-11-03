package com.akilisha.oss.roya.plugins.ai.retry;

import com.akilisha.oss.roya.workflow.retry.RetryPolicy;

import java.time.Duration;

/**
 * AI-specific retry policies for common failure scenarios.
 * 
 * These are convenience wrappers around roya-workflow's RetryPolicy,
 * with presets optimized for AI API calls.
 * 
 * Usage:
 * <pre>
 * ai.workflow("my-workflow")
 *     .llm("analyze", builder -> builder.systemPrompt("..."))
 *     .edge("start", "analyze", Edge.sequential()
 *         .withRetry(AIRetryPolicy.forRateLimits())
 *     )
 * </pre>
 */
public class AIRetryPolicy {
    
    /**
     * Retry policy for rate limit errors (429).
     * 
     * Uses exponential backoff starting at 1 second, with max 5 retries.
     * This gives the API time to reset rate limit counters.
     */
    public static RetryPolicy forRateLimits() {
        return RetryPolicy.exponentialBackoff(
            5, 
            Duration.ofSeconds(1),
            2.0,  // Double each retry
            Duration.ofMinutes(2)  // Cap at 2 minutes
        );
    }
    
    /**
     * Retry policy for transient API errors (500, 502, 503, 504).
     * 
     * Uses exponential backoff with more aggressive retries.
     */
    public static RetryPolicy forTransientErrors() {
        return RetryPolicy.exponentialBackoff(
            3,
            Duration.ofMillis(500),
            2.0,
            Duration.ofSeconds(30)
        );
    }
    
    /**
     * Retry policy for timeout errors.
     * 
     * Uses fixed delay with fewer retries, as timeouts often indicate
     * the request is too large or complex.
     */
    public static RetryPolicy forTimeouts() {
        return RetryPolicy.fixedDelay(
            2,  // Only 2 retries for timeouts
            Duration.ofSeconds(5)
        );
    }
    
    /**
     * Retry policy for general AI API errors.
     * 
     * Balanced approach for unknown errors.
     */
    public static RetryPolicy forGeneralErrors() {
        return RetryPolicy.exponentialBackoff(
            3,
            Duration.ofSeconds(1),
            2.0,
            Duration.ofMinutes(1)
        );
    }
    
    /**
     * Retry policy for LLM streaming errors.
     * 
     * Streaming failures are often network-related, so we use
     * a moderate retry strategy.
     */
    public static RetryPolicy forStreamingErrors() {
        return RetryPolicy.linearBackoff(
            3,
            Duration.ofSeconds(2)
        );
    }
}

