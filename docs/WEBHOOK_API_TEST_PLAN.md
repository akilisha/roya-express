# Webhook Management API Test Plan

## Test Strategy

### 1. Unit Tests (WebhookManagementAPI)
- **Test each endpoint handler independently**
- Mock `WebhookPersistenceService` and `WorkflowRegistry`
- Verify request/response handling
- Test error scenarios

### 2. Integration Tests (Full Stack)
- **Test with real database (if available)**
- Test workflow registration and execution
- Test route registration with HTTP server
- Test persistence across restarts

### 3. Manual Testing (End-to-End)
- **Test via HTTP client (curl/Postman)**
- Verify webhook execution
- Test signature verification

---

## Test Cases

### GET /api/webhooks (List All)

**Test Cases:**
1. ✅ Empty list - returns empty array
2. ✅ Multiple webhooks - returns all webhooks
3. ✅ Secret not exposed - `hasSecret` field present, no `secret` field
4. ✅ Error handling - database errors return 500

**Expected Response:**
```json
{
  "success": true,
  "webhooks": [
    {
      "id": "POST:/api/webhook",
      "path": "/api/webhook",
      "method": "POST",
      "workflowName": "test-workflow",
      "triggerNodeId": "webhook",
      "enabled": true,
      "hasSecret": true,
      "algorithm": "sha256",
      "headerName": "X-Hub-Signature-256"
    }
  ],
  "count": 1
}
```

---

### GET /api/webhooks/:id (Get Specific)

**Test Cases:**
1. ✅ Valid ID - returns webhook config
2. ✅ Invalid ID - returns 404
3. ✅ Missing ID - returns 400

---

### POST /api/webhooks (Create)

**Test Cases:**
1. ✅ Valid request - creates webhook, returns 201
2. ✅ Missing required fields - returns 400
3. ✅ Invalid workflow name - returns 400 (workflow not found)
4. ✅ Duplicate webhook - updates existing (upsert behavior)
5. ✅ Route registration - webhook becomes accessible
6. ✅ Secret validation - optional field works
7. ✅ Algorithm defaults - SHA256 if not specified

**Request Body:**
```json
{
  "path": "/api/test-webhook",
  "method": "POST",
  "workflowName": "test-workflow",
  "triggerNodeId": "webhook",
  "secret": "test-secret",
  "algorithm": "sha256",
  "headerName": "X-Hub-Signature-256",
  "enabled": true,
  "metadata": {
    "retries": 3,
    "timeout": 5000
  }
}
```

---

### PATCH /api/webhooks/:id (Update)

**Test Cases:**
1. ✅ Update enabled status - disables/enables webhook
2. ✅ Update secret - changes signature verification
3. ✅ Partial update - only updates provided fields
4. ✅ Invalid ID - returns 404
5. ✅ Empty body - returns 400

**Request Body:**
```json
{
  "enabled": false,
  "secret": "new-secret"
}
```

---

### DELETE /api/webhooks/:id

**Test Cases:**
1. ✅ Valid ID - deletes webhook, returns 200
2. ✅ Invalid ID - returns 404
3. ✅ Route unregistration - webhook no longer accessible

---

### Integration Tests

**Test Cases:**
1. ✅ Database persistence - webhook survives restart
2. ✅ In-memory fallback - works without database
3. ✅ Workflow execution - webhook triggers workflow correctly
4. ✅ Signature verification - middleware blocks invalid signatures
5. ✅ Multiple webhooks - can register multiple webhooks
6. ✅ Auto-registration on startup - persisted webhooks load correctly

---

## Test Implementation

### Unit Tests
- `WebhookManagementAPITest.java` - Test each endpoint handler
- Mock dependencies: `WebhookPersistenceService`, `WorkflowRegistry`

### Integration Tests
- `WebhookManagementAPIIntegrationTest.java` - Full stack tests
- Requires: Database plugin (optional), HTTP server

### Manual Test Script
- `WebhookManagementAPIManualTest.java` - Main class for manual testing
- Can be run to test against real server

---

## Execution Order

1. **Unit Tests** (fast, isolated)
2. **Integration Tests** (requires database)
3. **Manual Testing** (end-to-end verification)

---

## Success Criteria

- ✅ All unit tests pass
- ✅ Integration tests pass with database
- ✅ Integration tests pass with in-memory fallback
- ✅ Manual testing confirms webhook execution
- ✅ No compilation errors
- ✅ Proper error handling and status codes

