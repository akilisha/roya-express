package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.RateLimit;

import java.time.Duration;
import java.util.Map;

public class RateLimitDemo {
    public static void main(String[] args) {
        var app = Roya.create();

        // Apply rate limiting: 10 requests per 30 seconds per IP
        // builder().build() returns Handler directly
        app.use(RateLimit.builder()
            .max(10)
            .window(Duration.ofSeconds(30))
            .build());

        app.get("/", (req, res, next) -> res.json(Map.of("status", "ok")));

        // Try hitting this endpoint more than 10 times in 30 seconds
        app.get("/test", (req, res, next) -> {
            res.json(Map.of(
                "message", "Rate limit test",
                "ip", req.ip(),
                "timestamp", System.currentTimeMillis()
            ));
        });

        app.listen(3008, () -> System.out.println("RateLimit demo on http://localhost:3008/test\n" +
            "Try: curl http://localhost:3008/test (more than 10 times in 30s to see 429)"));
    }
}

