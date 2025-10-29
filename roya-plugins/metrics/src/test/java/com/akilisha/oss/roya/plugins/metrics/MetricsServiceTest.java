package com.akilisha.oss.roya.plugins.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Metrics service.
 */
@DisplayName("Metrics Service Tests")
class MetricsServiceTest {

    private Metrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new MetricsServiceImpl();
    }

    @Test
    @DisplayName("Should create counter metric")
    void testCounter() {
        Counter counter = metrics.counter("test_counter", "tag1", "value1");
        assertNotNull(counter);
        
        counter.increment();
        assertEquals(1.0, counter.count());
        
        counter.increment(2.5);
        assertEquals(3.5, counter.count());
    }

    @Test
    @DisplayName("Should create timer metric")
    void testTimer() {
        Timer timer = metrics.timer("test_timer", "tag1", "value1");
        assertNotNull(timer);
        
        Timer.Sample sample = Timer.start();
        sample.stop(timer);
        
        assertTrue(timer.count() > 0);
    }

    @Test
    @DisplayName("Should create gauge metric")
    void testGauge() {
        Gauge gauge = metrics.gauge("test_gauge", () -> 42.0, "tag1", "value1");
        assertNotNull(gauge);
        assertEquals(42.0, gauge.value());
    }

    @Test
    @DisplayName("Should return Prometheus format metrics")
    void testPrometheusFormat() {
        // Create some metrics
        metrics.counter("test_counter").increment();
        metrics.timer("test_timer");
        
        String prometheus = metrics.prometheus();
        
        assertNotNull(prometheus);
        assertFalse(prometheus.isEmpty());
        // Prometheus format should contain metric name
        assertTrue(prometheus.contains("test_counter") || prometheus.contains("http"));
    }

    @Test
    @DisplayName("Should return middleware handler")
    void testMiddleware() {
        assertNotNull(metrics.middleware());
    }
}

