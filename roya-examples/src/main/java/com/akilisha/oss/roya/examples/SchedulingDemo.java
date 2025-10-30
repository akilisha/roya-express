package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.SchedulingUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class SchedulingDemo {
    public static void main(String[] args) {
        var app = Roya.create();

        AtomicReference<String> lastRun = new AtomicReference<>("never");

        SchedulingUtil.fixedRate(Duration.ofSeconds(1), Duration.ofSeconds(10), () -> {
            lastRun.set(Instant.now().toString());
        });

        app.get("/", (req, res, next) -> res.json(Map.of("status","ok")));
        app.get("/status", (req, res, next) -> res.json(Map.of("lastRun", lastRun.get())));

        app.listen(3007, () -> System.out.println("Scheduling demo on http://localhost:3007/status"));
    }
}


