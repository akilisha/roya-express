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
  - Enable structured logging in code via `Morgan.builder().structured(true)`
  - Redact headers like Authorization/Cookie by default
  - Fields: http.method, path, status, duration_ms, remote.ip, request_id, trace_id, span_id
- Tracing: Helidon Tracing (Zipkin/OTEL via env)
  - Example env: `TRACING_ZIPKIN_URL=http://zipkin:9411/api/v2/spans`
  - Correlation: request_id ↔ trace/span present in logs
- Metrics: expose /metrics (if Metrics plugin present)
  - Prometheus scrape example:
    - job_name: 'roya'
    - static_configs: targets: ['service:3000']

## 5) Security

- Helmet defaults; review CSP for your app (script-src, connect-src)
- CORS via Helidon; restrict origins and allowed headers in prod
- Secrets via Vault; never bake secrets
  - Config keys: `vault.url`, `vault.token`, `vault.kvMount` (dev token only locally)
- Redaction enabled in logs (Authorization, Cookie, Set-Cookie)

## 6) Performance

- Tune JVM heap/GC; verify virtual threads
- For native, compare startup/CPU/RAM before adopting

## 7) Troubleshooting

- Health failing: check DB, Qdrant, MinIO, Vault
- Traces missing: verify exporter URL
- Logs missing fields: ensure structured mode

---

This guide will evolve with native-image configs, benchmarks, and deeper automation.
