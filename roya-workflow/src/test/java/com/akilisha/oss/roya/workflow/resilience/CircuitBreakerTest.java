package com.akilisha.oss.roya.workflow.resilience;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CircuitBreaker
 */
class CircuitBreakerTest {

    @Test
    void testInitialStateClosed() {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofSeconds(1));

        // Assert
        assertTrue(breaker.isClosed());
        assertFalse(breaker.isOpen());
        assertFalse(breaker.isHalfOpen());
        assertEquals(CircuitBreakerState.CLOSED, breaker.getState());
        assertEquals(0, breaker.getFailureCount());
        assertEquals(0, breaker.getSuccessCount());
    }

    @Test
    void testCircuitOpensAfterThresholdFailures() {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofSeconds(1));

        // Act - record 3 failures
        breaker.recordFailure();
        breaker.recordFailure();
        assertFalse(breaker.isOpen()); // Still closed after 2 failures

        breaker.recordFailure();

        // Assert
        assertTrue(breaker.isOpen());
        assertEquals(CircuitBreakerState.OPEN, breaker.getState());
        assertEquals(3, breaker.getFailureCount());
    }

    @Test
    void testSuccessResetsFailureCount() {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofSeconds(1));

        // Act
        breaker.recordFailure();
        breaker.recordFailure();
        assertEquals(2, breaker.getFailureCount());

        breaker.recordSuccess(); // Should reset count

        // Assert
        assertEquals(0, breaker.getFailureCount());
        assertEquals(1, breaker.getSuccessCount());
        assertTrue(breaker.isClosed());
    }

    @Test
    void testCircuitTransitionsToHalfOpenAfterTimeout() throws InterruptedException {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(2, Duration.ofMillis(100));

        // Act - open the circuit
        breaker.recordFailure();
        breaker.recordFailure();
        assertTrue(breaker.isOpen());

        // Wait for timeout
        Thread.sleep(150);

        // Check state - should transition to half-open
        assertFalse(breaker.isOpen()); // isOpen() triggers transition check
        assertTrue(breaker.isHalfOpen());
    }

    @Test
    void testHalfOpenClosesOnSuccess() throws InterruptedException {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(2, Duration.ofMillis(100));

        // Open the circuit
        breaker.recordFailure();
        breaker.recordFailure();
        assertTrue(breaker.isOpen());

        // Wait for timeout to transition to half-open
        Thread.sleep(150);
        breaker.isOpen(); // Trigger transition check

        assertTrue(breaker.isHalfOpen());

        // Act - record success in half-open
        breaker.recordSuccess();

        // Assert - should close
        assertTrue(breaker.isClosed());
        assertEquals(0, breaker.getFailureCount());
    }

    @Test
    void testHalfOpenReopensOnFailure() throws InterruptedException {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(2, Duration.ofMillis(100));

        // Open the circuit
        breaker.recordFailure();
        breaker.recordFailure();

        // Wait for timeout to transition to half-open
        Thread.sleep(150);
        breaker.isOpen(); // Trigger transition

        assertTrue(breaker.isHalfOpen());

        // Act - record failure in half-open
        breaker.recordFailure();

        // Assert - should reopen
        assertTrue(breaker.isOpen());
        assertEquals(CircuitBreakerState.OPEN, breaker.getState());
    }

    @Test
    void testManualReset() {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(2, Duration.ofSeconds(1));

        // Open the circuit
        breaker.recordFailure();
        breaker.recordFailure();
        assertTrue(breaker.isOpen());

        // Act
        breaker.reset();

        // Assert
        assertTrue(breaker.isClosed());
        assertEquals(0, breaker.getFailureCount());
        assertEquals(0, breaker.getSuccessCount());
    }

    @Test
    void testForceOpen() {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(5, Duration.ofSeconds(1));

        // Act
        breaker.forceOpen();

        // Assert
        assertTrue(breaker.isOpen());
        assertNotNull(breaker.getLastFailureTime());
    }

    @Test
    void testGetStats() {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofSeconds(1));

        // Act
        breaker.recordSuccess();
        breaker.recordSuccess();
        breaker.recordFailure();

        CircuitBreaker.CircuitBreakerStats stats = breaker.getStats();

        // Assert
        assertEquals(CircuitBreakerState.CLOSED, stats.state());
        assertEquals(1, stats.failureCount());
        assertEquals(2, stats.successCount());
        assertEquals(3, stats.failureThreshold());
        assertTrue(stats.isHealthy());
        assertEquals(1.0 / 3.0, stats.failureRate(), 0.001);
    }

    @Test
    void testStatsFail ureRateWithNoRequests() {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofSeconds(1));

        // Act
        CircuitBreaker.CircuitBreakerStats stats = breaker.getStats();

        // Assert
        assertEquals(0.0, stats.failureRate());
    }

    @Test
    void testStatsFailureRateAllFailures() {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofSeconds(1));

        // Act
        breaker.recordFailure();
        breaker.recordFailure();

        CircuitBreaker.CircuitBreakerStats stats = breaker.getStats();

        // Assert
        assertEquals(1.0, stats.failureRate());
    }

    @Test
    void testStatsToString() {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(5, Duration.ofSeconds(1));
        breaker.recordSuccess();
        breaker.recordFailure();

        // Act
        CircuitBreaker.CircuitBreakerStats stats = breaker.getStats();
        String toString = stats.toString();

        // Assert
        assertTrue(toString.contains("CLOSED"));
        assertTrue(toString.contains("failures=1/5"));
        assertTrue(toString.contains("successes=1"));
    }

    @Test
    void testDefaultFactoryMethod() {
        // Act
        CircuitBreaker breaker = CircuitBreaker.withDefaults();

        // Assert
        assertTrue(breaker.isClosed());
        assertEquals(5, breaker.getFailureThreshold());
    }

    @Test
    void testThresholdFactoryMethod() {
        // Act
        CircuitBreaker breaker = CircuitBreaker.withThreshold(10, Duration.ofMinutes(2));

        // Assert
        assertTrue(breaker.isClosed());
        assertEquals(10, breaker.getFailureThreshold());
    }

    @Test
    void testMultipleSuccessesIncrementCount() {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofSeconds(1));

        // Act
        for (int i = 0; i < 10; i++) {
            breaker.recordSuccess();
        }

        // Assert
        assertEquals(10, breaker.getSuccessCount());
        assertEquals(0, breaker.getFailureCount());
        assertTrue(breaker.isClosed());
    }

    @Test
    void testFailuresBelowThresholdDoNotOpenCircuit() {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(5, Duration.ofSeconds(1));

        // Act
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();
        breaker.recordFailure();

        // Assert - 4 failures, threshold is 5
        assertFalse(breaker.isOpen());
        assertTrue(breaker.isClosed());
        assertEquals(4, breaker.getFailureCount());
    }

    @Test
    void testCircuitBreakerWithSingleFailureThreshold() {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(1, Duration.ofSeconds(1));

        // Act
        breaker.recordFailure();

        // Assert
        assertTrue(breaker.isOpen());
        assertEquals(1, breaker.getFailureCount());
    }

    @Test
    void testLastFailureTimeUpdated() {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofSeconds(1));

        // Act
        assertNull(breaker.getLastFailureTime());

        breaker.recordFailure();

        // Assert
        assertNotNull(breaker.getLastFailureTime());
    }

    @Test
    void testMultipleResets() {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(2, Duration.ofSeconds(1));

        // Act & Assert - cycle through multiple open/reset cycles
        breaker.recordFailure();
        breaker.recordFailure();
        assertTrue(breaker.isOpen());

        breaker.reset();
        assertTrue(breaker.isClosed());

        breaker.recordFailure();
        breaker.recordFailure();
        assertTrue(breaker.isOpen());

        breaker.reset();
        assertTrue(breaker.isClosed());
        assertEquals(0, breaker.getFailureCount());
    }

    @Test
    void testStatsIsHealthyWhenClosed() {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(3, Duration.ofSeconds(1));

        // Assert
        assertTrue(breaker.getStats().isHealthy());
    }

    @Test
    void testStatsIsUnhealthyWhenOpen() {
        // Arrange
        CircuitBreaker breaker = new CircuitBreaker(2, Duration.ofSeconds(1));

        // Act
        breaker.recordFailure();
        breaker.recordFailure();

        // Assert
        assertFalse(breaker.getStats().isHealthy());
    }
}
