# DocuRoya Comprehensive Testing Plan

## Overview

This document outlines all testing required to verify DocuRoya's functionality and demonstrate every Roya framework feature. Testing is organized by feature area.

## Testing Status

| Category                  | Status            | Coverage | Notes                                                            |
|---------------------------|-------------------|----------|------------------------------------------------------------------|
| **Core Framework**        | ✅ Done            | 100%     | Tree routing, middleware, body parsing                           |
| **Database Plugin**       | ✅ Done            | 100%     | Migrations, JOOQ, CRUD operations                                |
| **Auth Plugin**           | ✅ Done            | 80%      | Register, login, protected routes - *needs refresh token test*   |
| **AI Plugin**             | ✅ Done            | 60%      | RAG search works - *needs chat, summarize, cache verification*   |
| **Cache Plugin**          | ⚠️ Partial        | 40%      | Used implicitly in /hot - *needs explicit cache operations*      |
| **Email Plugin**          | ⚠️ Partial        | 20%      | Registered but untested - *needs actual email send verification* |
| **Object Storage Plugin** | ⚠️ Partial        | 40%      | Upload works - *needs presigned URLs, multipart, list*           |
| **Metrics Plugin**        | ⚠️ Partial        | 30%      | Endpoint exists - *needs metrics verification, custom metrics*   |
| **WebSocket**             | ❌ Not Implemented | 0%       | Placeholder in code                                              |
| **SSE**                   | ❌ Not Implemented | 0%       | Placeholder in code                                              |
| **Client Streaming**      | ❌ Not Implemented | 0%       | Not yet added                                                    |

## Detailed Testing Requirements

### 1. Cache Plugin - Explicit Testing ⚠️

**Current State**: Cache is used in `/api/articles/hot` endpoint, but we haven't verified it's actually working.

**Missing Tests**:

1. **Cache Hit Verification**:
   ```bash
   # First request should hit database, second should hit cache
   curl http://localhost:3003/api/articles/hot
   curl http://localhost:3003/api/articles/hot
   # Verify cache headers in second response
   ```

2. **Cache Expiration**:
   ```bash
   # Set short TTL (1 minute)
   # Wait 61 seconds
   # Verify third request hits database again
   ```

3. **Cache Invalidation**:
   ```bash
   # Add/update article
   # Verify hot articles cache invalidated
   # Next request should hit database
   ```

4. **Manual Cache Operations**:
   ```bash
   # Store custom cache entry
   # Retrieve it
   # Verify serialization/deserialization works
   ```

5. **Cache Metrics**:
   ```bash
   curl http://localhost:3003/metrics | grep cache
   # Should show cache hit/miss ratios, sizes, etc.
   ```

**Acceptance Criteria**:
- ✅ Cache headers present (`X-Cache: HIT/MISS`)
- ✅ Second request to `/api/articles/hot` is significantly faster
- ✅ Cache expiration works after TTL
- ✅ Cache metrics visible in Prometheus output

### 2. Email Plugin - Full Testing ⚠️

**Current State**: Email is sent on registration but hasn't been verified.

**Missing Tests**:

1. **Welcome Email on Registration**:
   ```bash
   # Register new user
   curl -X POST http://localhost:3003/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email": "newuser@example.com", "password": "password", "name": "New User"}'
   
   # Verify email sent:
   # - Check logs for "Welcome email sent"
   # - Use email testing service (Mailtrap, Ethereal, etc.)
   # - Verify email content is correct
   ```

2. **Email Failure Handling**:
   ```bash
   # Configure invalid SMTP credentials
   # Attempt registration
   # Verify registration still succeeds (email failure doesn't block)
   # Check logs for error message
   ```

3. **Multiple Email Providers**:
   ```bash
   # Test SendGrid:
   export SENDGRID_API_KEY="<key>"
   # Send email, verify delivery
   
   # Test SMTP fallback:
   export SENDGRID_API_KEY=""
   # Configure SMTP in application.yaml
   # Send email, verify delivery
   ```

4. **Email Template Support**:
   ```bash
   # If templates are implemented, test:
   # - Welcome email with template
   # - Variable substitution works
   # - HTML emails render correctly
   ```

**Acceptance Criteria**:
- ✅ Welcome email sent and received on registration
- ✅ Email failures logged but don't block registration
- ✅ Both SendGrid and SMTP work
- ✅ Email content is properly formatted

### 3. Metrics Plugin - Full Testing ⚠️

**Current State**: Metrics endpoint exists but only returns empty output initially.

**Missing Tests**:

1. **HTTP Metrics Collection**:
   ```bash
   # Make several API calls
   curl http://localhost:3003/api/articles
   curl http://localhost:3003/api/articles
   curl http://localhost:3003/health
   curl http://localhost:3003/api/nonexistent
   
   # Verify metrics:
   curl http://localhost:3003/metrics | grep http
   
   # Should show:
   # - http_requests_total (counter)
   # - http_request_duration_seconds (histogram with percentiles)
   ```

2. **Custom Metrics**:
   ```bash
   # Create article (tracks article.created counter)
   # Get article (tracks article.views counter)
   
   curl http://localhost:3003/metrics | grep article
   
   # Should show:
   # - article_created_total
   # - article_views_total
   ```

3. **Prometheus Scraping**:
   ```bash
   # Verify Prometheus can scrape
   curl -H "Accept: application/openmetrics-text" http://localhost:3003/metrics
   
   # Should return Prometheus format with timestamps
   ```

4. **Metrics Aggregation**:
   ```bash
   # Generate load with different status codes
   # Verify metrics aggregate correctly:
   # - Status code breakdowns
   # - Method-specific metrics
   # - Route-specific metrics
   ```

**Acceptance Criteria**:
- ✅ HTTP metrics appear after making requests
- ✅ Custom metrics (article views, creations) visible
- ✅ Prometheus format is valid
- ✅ Percentiles calculated correctly (p50, p95, p99)

### 4. Object Storage Plugin - Full Testing ⚠️

**Current State**: Basic upload works, but presigned URLs, multipart upload, and listing are untested.

**Missing Tests**:

1. **Presigned URL Generation**:
   ```bash
   # Get presigned download URL
   curl http://localhost:3003/api/upload/test.txt/presigned
   
   # Should return:
   # {"url": "http://localhost:9000/ducuroya/uploads/...", "ttl": 3600}
   
   # Use URL to download file directly
   curl "http://localhost:9000/ducuroya/uploads/..."
   ```

2. **Presigned Upload URL**:
   ```bash
   # If implemented, test presigned PUT URL
   # Generate URL
   # Upload directly to MinIO
   # Verify file appears in bucket
   ```

3. **File Listing**:
   ```bash
   # List all files in bucket
   curl http://localhost:3003/api/upload
   
   # Should return JSON with:
   # - file keys
   # - sizes
   # - timestamps
   # - metadata
   ```

4. **Multipart Upload**:
   ```bash
   # Upload large file (>5MB)
   # Should use multipart upload
   # Verify all chunks uploaded
   # Verify final file is complete
   ```

5. **File Deletion**:
   ```bash
   # Delete uploaded file
   curl -X DELETE http://localhost:3003/api/upload/test.txt
   
   # Verify 204 response
   # Verify file gone from bucket
   ```

6. **MinIO Integration**:
   ```bash
   # Verify files appear in MinIO console
   # Access http://localhost:9001 (login: minioadmin/minioadmin)
   # Browse ducuroya bucket
   # Verify files uploaded by DocuRoya are visible
   ```

**Acceptance Criteria**:
- ✅ Presigned URLs work for downloads
- ✅ Files accessible via presigned URLs
- ✅ Listing returns all uploaded files
- ✅ Multipart upload works for large files
- ✅ Deletion removes files from bucket
- ✅ All operations visible in MinIO console

### 5. Auth Plugin - Additional Testing ⚠️

**Missing Tests**:

1. **Token Refresh Flow**:
   ```bash
   # Get refresh token from login
   TOKEN=$(curl -s -X POST http://localhost:3003/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"alice@example.com","password":"password123"}' \
     | jq -r .refreshToken)
   
   # Use refresh token to get new access token
   curl -X POST http://localhost:3003/api/auth/refresh \
     -H "Content-Type: application/json" \
     -d "{\"refreshToken\":\"$TOKEN\"}"
   
   # Should return new token and refresh token
   ```

2. **Protected Route Access**:
   ```bash
   # Try accessing protected route without token
   curl http://localhost:3003/api/me
   # Should return 401
   
   # Access with valid token
   curl -H "Authorization: Bearer $TOKEN" http://localhost:3003/api/me
   # Should return user data
   
   # Access with invalid token
   curl -H "Authorization: Bearer invalid-token-123" http://localhost:3003/api/me
   # Should return 401
   ```

3. **User Context in Routes**:
   ```bash
   # Create article (should capture user ID)
   # Update own article (should succeed)
   # Try to update someone else's article (should return 403)
   ```

**Acceptance Criteria**:
- ✅ Token refresh works end-to-end
- ✅ Protected routes properly require authentication
- ✅ Invalid tokens rejected with 401
- ✅ User context available in protected handlers

### 6. AI Plugin - Additional Testing ⚠️

**Missing Tests**:

1. **Chat Endpoint**:
   ```bash
   curl -X POST http://localhost:3003/api/chat \
     -H "Content-Type: application/json" \
     -d '{"message": "What is the documentation about?"}'
   
   # Should return conversational AI response
   ```

2. **Summarize Article**:
   ```bash
   curl -X POST http://localhost:3003/api/articles/$ARTICLE_ID/summarize
   
   # Should return summary of article content
   # Verify summary is shorter than original
   ```

3. **Vector Search Verification**:
   ```bash
   # Index some documents
   # Search for semantically similar content
   # Verify results are ranked by relevance
   # Verify Qdrant is storing embeddings
   ```

4. **AI Cache Integration**:
   ```bash
   # Ask same question twice
   # First: should hit OpenAI (slow, $0.02)
   # Second: should hit cache (fast, $0.00)
   # Verify cache headers present
   # Verify metrics show cache hit
   ```

5. **Cost Tracking**:
   ```bash
   # Make several AI calls
   # Check metrics for cost tracking
   curl http://localhost:3003/metrics | grep ai_cost
   
   # Should show total tokens used, total cost
   ```

**Acceptance Criteria**:
- ✅ Chat endpoint returns conversational responses
- ✅ Summarize reduces content to shorter summary
- ✅ Vector search finds semantically similar documents
- ✅ Cache reduces second request cost to zero
- ✅ Cost tracking visible in metrics

### 7. WebSocket - Implementation & Testing ❌

**Current State**: Not implemented.

**Missing Tests**:

1. **Connection**:
   ```bash
   # Connect via WebSocket client
   websocat ws://localhost:3003/ws/editing
   
   # Verify connection established
   # Verify handshake successful
   ```

2. **Real-time Collaboration**:
   ```bash
   # Open two WebSocket connections
   # Send edit from client 1
   # Verify client 2 receives update
   ```

3. **Room/Broadcasting**:
   ```bash
   # Join different rooms
   # Send message to room 1
   # Verify only room 1 clients receive it
   ```

**Acceptance Criteria**:
- ✅ WebSocket connections work
- ✅ Messages broadcast to all clients in room
- ✅ Room isolation works
- ✅ Graceful disconnection handled

### 8. SSE - Implementation & Testing ❌

**Current State**: Not implemented.

**Missing Tests**:

1. **Event Stream**:
   ```bash
   # Connect to SSE endpoint
   curl -N http://localhost:3003/api/notifications/stream
   
   # Should see:
   # data: {"event": "connected"}
   # 
   # data: {"event": "new_article", "article": {...}}
   ```

2. **Event Types**:
   ```bash
   # Create article
   # Verify notification sent
   # Create comment
   # Verify notification sent
   # Different event types should have different data
   ```

3. **Client Reconnection**:
   ```bash
   # Connect to SSE
   # Kill connection
   # Reconnect
   # Verify connection re-established
   # Verify events resume
   ```

**Acceptance Criteria**:
- ✅ SSE stream works
- ✅ Events formatted correctly
- ✅ Multiple clients can connect
- ✅ Reconnection handled gracefully

### 9. Client Streaming - Implementation & Testing ❌

**Current State**: Not implemented.

**Missing Tests**:

1. **Server-Sent JSON Streaming**:
   ```bash
   # Stream large dataset
   curl http://localhost:3003/api/articles/stream
   
   # Should return:
   # {"articles": [...], "more": true}
   # {"articles": [...], "more": true}
   # {"articles": [...], "more": false}
   ```

2. **Progressive Loading**:
   ```bash
   # Client should receive chunks as they're available
   # Verify memory efficient (not buffering entire dataset)
   ```

**Acceptance Criteria**:
- ✅ Streaming works for large datasets
- ✅ Memory usage stays constant
- ✅ Client can process chunks progressively

### 10. Middleware - Comprehensive Testing ⚠️

**Missing Tests**:

1. **Morgan Structured Logging**:
   ```bash
   # Make several requests
   # Check console output
   # Verify logs are JSON formatted
   # Verify trace IDs present
   # Verify redacted headers (authorization, cookie)
   ```

2. **CORS**:
   ```bash
   # Request from different origin
   curl -H "Origin: http://localhost:3000" http://localhost:3003/api/articles
   
   # Verify CORS headers:
   # - Access-Control-Allow-Origin
   # - Access-Control-Allow-Methods
   # - Access-Control-Allow-Headers
   ```

3. **Rate Limiting**:
   ```bash
   # Make 150 requests quickly
   # Verify first 100 succeed
   # Verify 101-150 return 429
   # Verify Retry-After header present
   
   # Wait for window reset
   # Verify requests succeed again
   ```

4. **Body Parser**:
   ```bash
   # POST JSON
   curl -X POST ... -d '{"key":"value"}'
   # Verify parsed correctly
   
   # POST form-urlencoded
   curl -X POST ... -d "key=value"
   # Verify parsed correctly
   
   # POST text/plain
   curl -X POST ... -d "plain text"
   # Verify parsed correctly
   ```

**Acceptance Criteria**:
- ✅ Morgan emits JSON logs with all required fields
- ✅ CORS headers present for cross-origin requests
- ✅ Rate limiting enforces limits correctly
- ✅ Body parser handles all content types

## Test Execution Plan

### Phase 1: Critical Path Tests (1 hour)

Run basic smoke tests to verify core functionality:

```bash
# 1. Health check
curl http://localhost:3003/health

# 2. Register user
curl -X POST http://localhost:3003/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password","name":"Test"}'

# 3. Create article
curl -X POST http://localhost:3003/api/articles \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Test","content":"Test content","tags":["test"]}'

# 4. List articles
curl http://localhost:3003/api/articles

# 5. Metrics
curl http://localhost:3003/metrics
```

### Phase 2: Plugin-Specific Tests (2 hours)

Test each plugin in isolation:

1. **Database**: CRUD operations, migrations, JOOQ
2. **Auth**: All auth flows, token management
3. **Cache**: Cache operations, invalidation, TTL
4. **Email**: Send, failure handling, providers
5. **AI**: RAG, chat, summarize, caching
6. **Object Storage**: Upload, download, presigned URLs, listing
7. **Metrics**: HTTP metrics, custom metrics, Prometheus

### Phase 3: Integration Tests (1 hour)

Test features working together:

1. **Auth + Protected Routes**: Protected endpoints require valid tokens
2. **AI + Cache**: AI responses cached, second call is instant
3. **Metrics + All Features**: All operations tracked in metrics
4. **Email + Auth**: Welcome emails sent on registration

### Phase 4: Advanced Features (2 hours)

1. **WebSocket**: Real-time collaboration
2. **SSE**: Live notifications
3. **Streaming**: Progressive data loading
4. **Multipart Upload**: Large file handling

### Phase 5: Load Testing (1 hour)

Use k6 or similar:

```bash
# Run load test
k6 run load-test.js

# Metrics to collect:
# - Requests/sec
# - Latency percentiles
# - Error rates
# - Resource usage (CPU, memory)
```

## Automated Testing

### Unit Tests Needed

- `ArticleServiceTest.java`: CRUD operations
- `SearchServiceTest.java`: AI integration, caching
- `CacheIntegrationTest.java`: FFM cache operations
- `ObjectStorageTest.java`: MinIO operations

### Integration Tests Needed

- `AuthFlowTest.java`: Register → Login → Access → Refresh
- `ArticleFlowTest.java`: Create → Read → Update → Delete
- `SearchFlowTest.java`: Index → Search → Cache → Cost tracking

### End-to-End Tests Needed

- `DocuRoyaE2ETest.java`: Full user journey
- `LoadTest.java`: Performance under load
- `ConcurrencyTest.java`: Virtual thread handling

## Testing Environment Setup

### Required Tools

```bash
# HTTP client
curl or httpie

# WebSocket client
websocat

# Load testing
k6

# Email testing
Mailtrap or Ethereal

# JSON tools
jq (for parsing JSON responses)
```

### Test Data Setup

```sql
-- Clean database
TRUNCATE TABLE articles CASCADE;
TRUNCATE TABLE auth_users CASCADE;

-- Create test users
INSERT INTO auth_users (id, email, password_hash, provider) VALUES
  ('11111111-1111-1111-1111-111111111111', 'alice@example.com', '$2a$10$...', 'email'),
  ('22222222-2222-2222-2222-222222222222', 'bob@example.com', '$2a$10$...', 'email');
```

## Expected Test Results

### Success Criteria

- ✅ All smoke tests pass
- ✅ All plugin endpoints work
- ✅ Middleware chain executes correctly
- ✅ No memory leaks during load test
- ✅ Virtual threads handle 10K+ concurrent connections
- ✅ Latency p99 < 50ms under normal load
- ✅ Prometheus metrics accurate
- ✅ Cache reduces latency by 80%+

### Failure Criteria

- ❌ Any endpoint returns 500 error
- ❌ Token refresh doesn't work
- ❌ Cache doesn't reduce latency
- ❌ Email never arrives
- ❌ Metrics show no data
- ❌ Memory usage grows unbounded

## Test Documentation

Each test should be documented with:

1. **Purpose**: What feature is being tested?
2. **Prerequisites**: What setup is needed?
3. **Steps**: Exact commands to run
4. **Expected Result**: What should happen?
5. **Actual Result**: What actually happened?
6. **Pass/Fail**: Did it meet expectations?

## Continuous Testing

Once automated tests are in place:

```bash
# Run all tests
./gradlew test

# Run integration tests only
./gradlew integrationTest

# Run load tests
./gradlew loadTest

# Generate coverage report
./gradlew jacocoTestReport
```

## Contributing

When adding features:

1. Add test cases to this document
2. Update testing checklist
3. Add automated tests where possible
4. Document manual test steps
5. Update expected results

