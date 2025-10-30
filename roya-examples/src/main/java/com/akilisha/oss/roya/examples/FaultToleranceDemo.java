package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.FaultTolerance;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FaultToleranceDemo {
    public static void main(String[] args) {
        var app = Roya.create();

        app.get("/", (req, res, next) -> res.json(Map.of("status","ok")));

        AtomicInteger attempts = new AtomicInteger();
        var fragile = FaultTolerance.builder()
            .timeout(Duration.ofSeconds(1))
            .retries(3, Duration.ofMillis(100))
            .wrap((req, res, next) -> {
                int n = attempts.incrementAndGet();
                if (n % 3 != 0) {
                    // fail 2 times then succeed
                    throw new RuntimeException("transient failure " + n);
                }
                res.json(Map.of("attempts", n, "result", "ok"));
            });

        app.get("/fragile", fragile);

        app.listen(3006, () -> System.out.println("FaultTolerance demo on http://localhost:3006/fragile"));
    }
}


