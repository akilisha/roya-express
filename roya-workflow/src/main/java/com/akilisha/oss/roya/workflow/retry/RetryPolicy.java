package com.akilisha.oss.roya.workflow.retry;

import java.time.Duration;

/**
 * Defines retry behavior for node execution failures
 */
public class RetryPolicy {

    private final int maxAttempts;
    private final Duration initialDelay;
    private final BackoffStrategy backoffStrategy;
    private final double backoffMultiplier;
    private final Duration maxDelay;

    private RetryPolicy(int maxAttempts,
                        Duration initialDelay,
                        BackoffStrategy backoffStrategy,
                        double backoffMultiplier,
                        Duration maxDelay) {
        this.maxAttempts = maxAttempts;
        this.initialDelay = initialDelay;
        this.backoffStrategy = backoffStrategy;
        this.backoffMultiplier = backoffMultiplier;
        this.maxDelay = maxDelay;
    }

    /**
     * No retry policy
     */
    public static RetryPolicy noRetry() {
        return new RetryPolicy(0, Duration.ZERO, BackoffStrategy.NONE, 1.0, Duration.ZERO);
    }

    /**
     * Fixed delay between retries
     */
    public static RetryPolicy fixedDelay(int maxAttempts, Duration delay) {
        return new RetryPolicy(maxAttempts, delay, BackoffStrategy.FIXED, 1.0, delay);
    }

    /**
     * Exponential backoff: delay doubles each retry
     */
    public static RetryPolicy exponentialBackoff(int maxAttempts, Duration initialDelay) {
        return exponentialBackoff(maxAttempts, initialDelay, 2.0, Duration.ofMinutes(5));
    }

    /**
     * Exponential backoff with custom multiplier and max delay
     */
    public static RetryPolicy exponentialBackoff(int maxAttempts,
                                                  Duration initialDelay,
                                                  double multiplier,
                                                  Duration maxDelay) {
        return new RetryPolicy(maxAttempts, initialDelay, BackoffStrategy.EXPONENTIAL, multiplier, maxDelay);
    }

    /**
     * Linear backoff: delay increases linearly
     */
    public static RetryPolicy linearBackoff(int maxAttempts, Duration initialDelay) {
        return new RetryPolicy(maxAttempts, initialDelay, BackoffStrategy.LINEAR, 1.0, Duration.ofMinutes(5));
    }

    /**
     * Check if retry should be attempted
     */
    public boolean shouldRetry(int attemptNumber) {
        return attemptNumber < maxAttempts;
    }

    /**
     * Calculate delay for a specific attempt
     */
    public Duration calculateDelay(int attemptNumber) {
        if (attemptNumber == 0 || maxAttempts == 0) {
            return Duration.ZERO;
        }

        Duration calculatedDelay = switch (backoffStrategy) {
            case NONE -> Duration.ZERO;
            case FIXED -> initialDelay;
            case EXPONENTIAL -> {
                long delayMs = (long) (initialDelay.toMillis() *
                    Math.pow(backoffMultiplier, attemptNumber - 1));
                yield Duration.ofMillis(delayMs);
            }
            case LINEAR -> initialDelay.multipliedBy(attemptNumber);
        };

        // Cap at max delay
        if (calculatedDelay.compareTo(maxDelay) > 0) {
            return maxDelay;
        }

        return calculatedDelay;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Duration getInitialDelay() {
        return initialDelay;
    }

    public BackoffStrategy getBackoffStrategy() {
        return backoffStrategy;
    }

    @Override
    public String toString() {
        return String.format("RetryPolicy[attempts=%d, strategy=%s, initialDelay=%s]",
            maxAttempts, backoffStrategy, initialDelay);
    }
}
