# Roya Production Guide (Phase 10)

This guide covers building, containerizing, deploying, and operating Roya services in production.

## 1) Build Options

- JVM (recommended baseline)
- Native (GraalVM)

### JVM Build

```bash
./gradlew clean build
```

### Native Build (example)

```bash
# Ensure GraalVM installed (native-image)
# gu install native-image
```

Notes: Jackson/Helidon may need reflection configs for native; start with JVM first.

## 2) Container Images

Two Dockerfiles under `roya-examples/`:
- Dockerfile.jvm
- Dockerfile.native

```bash
cd roya-examples
# JVM
docker build -f Dockerfile.jvm -t roya-examples:jvm .
# Native
docker build -f Dockerfile.native -t roya-examples:native .
```

## 3) Kubernetes Manifests

`deploy/k8s/` includes Deployment + Service with probes:
- /health/live, /health/ready

```bash
kubectl apply -f deploy/k8s/
```

## 4) Observability

- Logs: Morgan JSON to stdout; includes trace/span/request IDs
- Tracing: Helidon Tracing (Zipkin/OTEL via env)
- Metrics: expose /metrics (if Metrics plugin present)

## 5) Security

- Helmet defaults; review CSP
- CORS via Helidon; restrict origins
- Secrets via Vault; never bake secrets
- Redaction enabled in logs

## 6) Performance

- Tune JVM heap/GC; verify virtual threads
- For native, compare startup/CPU/RAM before adopting

## 7) Troubleshooting

- Health failing: check DB, Qdrant, MinIO, Vault
- Traces missing: verify exporter URL
- Logs missing fields: ensure structured mode

---

This guide will evolve with native-image configs, benchmarks, and deeper automation.
