package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.core.middleware.Sse;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class SseDemo {
    public static void main(String[] args) {
        var app = Roya.create();

        app.get("/", (req, res, next) -> res.json(Map.of("status","ok")));

        app.get("/events", Sse.stream(() -> "time=" + Instant.now(), Duration.ofSeconds(1)));

        app.listen(3005, () -> System.out.println("SSE demo on http://localhost:3005/events"));
    }
}


