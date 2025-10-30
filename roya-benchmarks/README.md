# Roya vs Spring Boot Benchmarks (PetClinic)

This module contains two apps implementing the same minimal PetClinic API:
- `roya-app` (Roya framework)
- `spring-app` (Spring Boot)

Endpoints:
- GET /health
- GET /owners
- POST /owners { name }
- GET /owners/:id

Run:
```bash
# Roya
./gradlew :roya-benchmarks:roya-app:run
# Spring Boot
./gradlew :roya-benchmarks:spring-app:bootRun
```

Load test (k6 example):
```bash
k6 run bench.k6.js
```

Dockerization and k8s templates are provided in top-level Phase 10 artifacts.
