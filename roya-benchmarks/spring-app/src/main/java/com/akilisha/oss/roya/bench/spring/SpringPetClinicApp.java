package com.akilisha.oss.roya.bench.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class SpringPetClinicApp {
    public static void main(String[] args) {
        SpringApplication.run(SpringPetClinicApp.class, args);
    }

    record Owner(String id, String name) {}

    @RestController
    @RequestMapping
    static class OwnersController {
        private final Map<String, Owner> owners = new ConcurrentHashMap<>();

        @GetMapping("/health")
        Map<String, Object> health() { return Map.of("status","ok"); }

        @GetMapping("/owners")
        List<Owner> list() { return new ArrayList<>(owners.values()); }

        @PostMapping("/owners")
        Owner create(@RequestBody Map<String,Object> body) {
            String id = UUID.randomUUID().toString();
            String name = (String) body.getOrDefault("name", "Anonymous");
            Owner o = new Owner(id, name);
            owners.put(id, o);
            return o;
        }

        @GetMapping("/owners/{id}")
        Owner get(@PathVariable String id) {
            Owner o = owners.get(id);
            if (o == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "not_found");
            return o;
        }
    }
}


