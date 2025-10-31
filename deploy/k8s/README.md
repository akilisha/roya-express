# Kubernetes Deployment

Kubernetes manifests for deploying Roya applications.

## Files

- `deployment.yaml` - Deployment with health probes
- `service.yaml` - ClusterIP service exposing port 80 → 3000

## Usage

```bash
# Build and push image first
docker build -f roya-examples/Dockerfile.jvm -t your-registry/roya-example:latest .
docker push your-registry/roya-example:latest

# Update image in deployment.yaml, then:
kubectl apply -f deploy/k8s/

# Check status
kubectl get pods -l app=roya-example
kubectl logs -l app=roya-example

# Test health endpoints
kubectl port-forward svc/roya-example 8080:80
curl http://localhost:8080/health
curl http://localhost:8080/health/live
curl http://localhost:8080/health/ready
```

## Health Probes

- **Readiness**: `/health/ready` - App is ready to serve traffic
- **Liveness**: `/health/live` - App is alive and should not be restarted

Both endpoints are provided by Helidon Health (registered automatically when present on classpath).

