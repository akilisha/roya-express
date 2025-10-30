package com.akilisha.oss.roya.core.middleware;

import io.helidon.scheduling.FixedRate;

import java.time.Duration;

/**
 * Lightweight wrapper around Helidon Scheduling for fixed-rate background tasks.
 */
public final class SchedulingUtil {

    private SchedulingUtil(){}

    /**
     * Schedule a Runnable at a fixed rate with initial delay. Returns a handle you can close to stop.
     */
    public static AutoCloseable fixedRate(Duration initialDelay, Duration interval, Runnable task) {
        var control = FixedRate.builder()
                .interval(interval)
                .delayBy(initialDelay)
                .task(inv -> task.run())
                .build();
        return control::close;
    }
}
