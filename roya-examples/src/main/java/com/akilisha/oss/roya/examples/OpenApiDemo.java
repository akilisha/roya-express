package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;

import java.util.Map;

public class OpenApiDemo {
    public static void main(String[] args) {
        var app = Roya.create();

        app.get("/", (req, res, next) -> res.json(Map.of("status","ok")));
        app.get("/users/:id", (req, res, next) -> {
            String id = req.params().get("id").orElse("unknown");
            res.json(Map.of("id", id, "name", "Jane Doe"));
        });
        app.post("/users", (req, res, next) -> {
            res.status(201).json(Map.of("id", "u_123", "name", "Created"));
        });

        app.listen(3008, () -> System.out.println("OpenAPI demo on http://localhost:3008 (spec at /openapi)"));
    }
}


