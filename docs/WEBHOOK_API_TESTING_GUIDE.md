# Webhook Management API - Testing Guide

## Quick Start

### Option 1: Manual Test Script (Recommended)

```bash
# Run the manual test server
./gradlew :roya-examples:run --args="com.akilisha.oss.roya.examples.WebhookManagementAPIManualTest"
```

This starts a server on `http://localhost:8080` with:
- Webhook Management API at `/api/webhooks`
- Test workflow with webhook at `/api/test-webhook`

### Option 2: Integration Tests

```bash
# Run integration tests
./gradlew :roya-plugins:ai:test --tests WebhookManagementAPIIntegrationTest
```

---

## Test Scenarios

### 1. List All Webhooks

```bash
curl http://localhost:8080/api/webhooks
```

**Expected Response:**
```json
{
  "success": true,
  "webhooks": [
    {
      "id": "POST:/api/test-webhook",
      "path": "/api/test-webhook",
      "method": "POST",
      "workflowName": "test-webhook-workflow",
      "triggerNodeId": "webhook",
      "enabled": true,
      "hasSecret": false,
      "algorithm": null,
      "headerName": null
    }
  ],
  "count": 1
}
```

---

### 2. Create New Webhook

```bash
curl -X POST http://localhost:8080/api/webhooks \
  -H "Content-Type: application/json" \
  -d '{
    "path": "/api/github-webhook",
    "method": "POST",
    "workflowName": "test-webhook-workflow",
    "triggerNodeId": "webhook",
    "secret": "github-secret",
    "algorithm": "sha256",
    "headerName": "X-Hub-Signature-256",
    "enabled": true
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "webhook": {
    "id": "POST:/api/github-webhook",
    "path": "/api/github-webhook",
    "method": "POST",
    "workflowName": "test-webhook-workflow",
    "triggerNodeId": "webhook",
    "enabled": true,
    "hasSecret": true,
    "algorithm": "sha256",
    "headerName": "X-Hub-Signature-256"
  },
  "message": "Webhook registered successfully"
}
```

---

### 3. Get Specific Webhook

```bash
curl http://localhost:8080/api/webhooks/POST:/api/github-webhook
```

**Expected Response:**
```json
{
  "success": true,
  "webhook": {
    "id": "POST:/api/github-webhook",
    "path": "/api/github-webhook",
    "method": "POST",
    "workflowName": "test-webhook-workflow",
    "triggerNodeId": "webhook",
    "enabled": true,
    "hasSecret": true
  }
}
```

---

### 4. Update Webhook

```bash
curl -X PATCH http://localhost:8080/api/webhooks/POST:/api/github-webhook \
  -H "Content-Type: application/json" \
  -d '{
    "enabled": false
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "webhook": {
    "id": "POST:/api/github-webhook",
    "enabled": false,
    ...
  },
  "message": "Webhook updated successfully"
}
```

---

### 5. Delete Webhook

```bash
curl -X DELETE http://localhost:8080/api/webhooks/POST:/api/github-webhook
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Webhook deleted successfully"
}
```

---

### 6. Execute Webhook

```bash
curl -X POST http://localhost:8080/api/test-webhook \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Hello from webhook test",
    "userId": "123"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "result": {
    "processed": true,
    "message": "Hello from webhook test",
    "timestamp": 1234567890
  }
}
```

---

## Error Scenarios

### Missing Required Fields

```bash
curl -X POST http://localhost:8080/api/webhooks \
  -H "Content-Type: application/json" \
  -d '{"path": "/api/test"}'
```

**Expected Response (400):**
```json
{
  "success": false,
  "error": "Missing required fields: path, method, workflowName, triggerNodeId"
}
```

### Invalid Workflow

```bash
curl -X POST http://localhost:8080/api/webhooks \
  -H "Content-Type: application/json" \
  -d '{
    "path": "/api/test",
    "method": "POST",
    "workflowName": "non-existent-workflow",
    "triggerNodeId": "webhook"
  }'
```

**Expected Response (400):**
```json
{
  "success": false,
  "error": "Workflow not found: non-existent-workflow"
}
```

### Webhook Not Found

```bash
curl http://localhost:8080/api/webhooks/INVALID
```

**Expected Response (404):**
```json
{
  "success": false,
  "error": "Webhook not found: INVALID"
}
```

---

## Test Checklist

- [ ] List webhooks (empty and populated)
- [ ] Create webhook (valid request)
- [ ] Create webhook (missing fields)
- [ ] Create webhook (invalid workflow)
- [ ] Get webhook (valid ID)
- [ ] Get webhook (invalid ID)
- [ ] Update webhook (enable/disable)
- [ ] Update webhook (change secret)
- [ ] Delete webhook (valid ID)
- [ ] Delete webhook (invalid ID)
- [ ] Execute webhook (workflow triggers)
- [ ] Secret not exposed in responses
- [ ] Signature verification (if secret configured)

---

## Database Persistence Test

If Database plugin is installed:

1. Create webhook via API
2. Restart server
3. Verify webhook persists and auto-registers

```bash
# Step 1: Create webhook
curl -X POST http://localhost:8080/api/webhooks -H "Content-Type: application/json" -d '{...}'

# Step 2: Restart server

# Step 3: Verify webhook still exists
curl http://localhost:8080/api/webhooks
```

---

## Performance Testing

For load testing:

```bash
# List webhooks (100 requests)
for i in {1..100}; do
  curl -s http://localhost:8080/api/webhooks > /dev/null
done

# Create webhook (10 requests)
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/webhooks \
    -H "Content-Type: application/json" \
    -d "{\"path\": \"/api/test$i\", \"method\": \"POST\", \"workflowName\": \"test-webhook-workflow\", \"triggerNodeId\": \"webhook\"}"
done
```

