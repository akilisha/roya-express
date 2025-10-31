package com.akilisha.oss.roya.bench.roya;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.Next;
import com.akilisha.oss.roya.api.Request;
import com.akilisha.oss.roya.api.Response;
import com.akilisha.oss.roya.core.middleware.BodyParser;
import com.akilisha.oss.roya.core.middleware.Morgan;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class PetClinicApp {

    record Owner(String id, String name) {}

    public static void main(String[] args) {
        var app = Roya.create();
        app.use(Morgan.builder().structured(true).build());
        app.use(BodyParser.bodyParser());

        Map<String, Owner> owners = new java.util.concurrent.ConcurrentHashMap<>();

        app.get("/health", (req, res, next) -> res.json(Map.of("status","ok")));

        app.get("/owners", (req, res, next) -> res.json(new ArrayList<>(owners.values())));
        app.post("/owners", (Request req, Response res, Next next) -> {
            Object raw = req.get("body");
            Map body = (raw instanceof Map)
                    ? (Map) raw
                    : new ObjectMapper().readValue(String.valueOf(raw), Map.class);
            String id = UUID.randomUUID().toString();
            String name = (String) body.getOrDefault("name", "Anonymous");
            Owner o = new Owner(id, name);
            owners.put(id, o);
            res.status(201).json(o);
        });
        app.get("/owners/:id", (req, res, next) -> {
            String id = req.params().get("id").orElse("");
            Owner o = owners.get(id);
            if (o == null) { res.status(404).json(Map.of("error","not_found")); return; }
            res.json(o);
        });

        app.listen(3101, () -> System.out.println("Roya PetClinic on http://localhost:3101"));
    }
}


