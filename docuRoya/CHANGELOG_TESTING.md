# Testing Infrastructure Changelog

## Summary

Complete testing infrastructure added to DocuRoya including backend testing endpoints and a Preact frontend application.

## Changes Made

### Backend Testing Infrastructure

#### New Router: `TestingRouter.java`

Added comprehensive testing endpoints under `/api/test/*`:

**Cache Testing:**
- `POST /api/test/cache/set` - Store arbitrary data with TTL
- `GET /api/test/cache/get/:key` - Retrieve cached data
- `GET /api/test/cache/stats` - Get cache statistics
- `DELETE /api/test/cache/invalidate/:key` - Delete cache entry

**Object Storage Testing:**
- `GET /api/test/storage/list?bucket=X&prefix=Y` - List all files
- `DELETE /api/test/storage/delete/:key?bucket=X` - Delete file
- `POST /api/test/storage/presigned-put` - Generate presigned PUT URL
- `GET /api/test/storage/get/:key?bucket=X` - Get object metadata

**Metrics Testing:**
- `POST /api/test/metrics/counter` - Increment custom counter
- `POST /api/test/metrics/gauge` - Set gauge value
- `POST /api/test/metrics/timer` - Record timer duration

**Rate Limiting Testing:**
- `GET /api/test/rate-limit/aggressive` - 10 requests/minute limit
- `GET /api/test/rate-limit/moderate` - 50 requests/minute limit
- `GET /api/test/rate-limit/per-ip` - 20 requests/minute per IP

**General Testing:**
- `GET /api/test/cors` - Verify CORS headers
- `GET /api/test/logging` - Verify Morgan logging
- `POST /api/test/echo` - Echo request details
- `GET /api/test/slow?delay=N` - Simulate slow response
- `GET /api/test/error` - Test error handling

### Frontend Application

Created a complete Preact-based web application in `docuRoya/frontend/`:

**Tech Stack:**
- Preact 10.19.0
- Vite 5.0.11
- Tailwind CSS 3.4.1
- Preact Router for routing
- date-fns for date formatting

**Features:**
- Authentication UI (Login, Register)
- Articles CRUD interface
- File upload with drag-and-drop
- Testing dashboard for cache, metrics, rate limiting
- Responsive design with Tailwind CSS
- CORS testing (port 3000 → 3003)

**Pages:**
- `/` - Home with hot articles
- `/login` - User login
- `/register` - User registration
- `/articles` - Article list
- `/articles/:id` - Article view
- `/articles/new` - Create article
- `/articles/:id/edit` - Edit article
- `/upload` - File upload
- `/testing` - Testing dashboard

### Bug Fixes

#### Date Serialization Fix

Fixed `LocalDateTime` serialization across all `ObjectMapper` instances:

**Problem:** Jackson was serializing `LocalDateTime` as arrays `[year, month, day, hour, minute, second, nano]` instead of ISO-8601 strings.

**Solution:** Added to all `ObjectMapper` instances:
```java
private static final ObjectMapper objectMapper = new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
```

**Files Updated:**
- `roya-core/Roya.java`
- `roya-core/ResponseImpl.java`
- `roya-core/RequestImpl.java`
- `roya-core/middleware/BodyParser.java`
- `roya-core/middleware/Json.java`
- `roya-core/middleware/Morgan.java`

**Frontend Fix:**
Added `parseDate()` utility in `frontend/src/utils/date.js` to handle both ISO-8601 strings and legacy array format for compatibility.

### Documentation

**New Files:**
- `docuRoya/TESTING_PLAN.md` - Comprehensive testing plan with 841 lines
- `docuRoya/QUICK_START.md` - Quick setup guide
- `docuRoya/CHANGELOG_TESTING.md` - This file
- `docuRoya/frontend/README.md` - Frontend documentation
- `docuRoya/TESTING.md` - Updated with testing status

**Updated Files:**
- `docuRoya/TESTING.md` - Added testing status summary
- `docuRoya/DocuRoyaApp.java` - Registered `TestingRouter`

## Testing Coverage

### ✅ Fully Implemented
- Authentication (register, login, protected routes)
- Article CRUD operations
- File upload/download
- Cache operations
- Custom metrics
- Rate limiting
- CORS headers
- Morgan structured logging

### ⚠️ Partially Implemented
- Email (sent but not verified)
- AI chat/summarize endpoints
- Multipart file upload
- Advanced search features

### ❌ Not Yet Implemented
- WebSocket real-time collaboration
- SSE live notifications
- Client streaming
- Load/stress testing scripts
- Automated integration tests

## Quick Test

```bash
# Start backend
cd docuRoya
java --enable-preview -Dport=3003 -jar build/libs/docuRoya-*.jar

# Start frontend (in another terminal)
cd docuRoya/frontend
npm install
npm run dev

# Open browser
# http://localhost:3000
```

## Next Steps

1. **Load Testing** - Create k6 scripts for performance testing
2. **Stress Testing** - Long-running stability tests
3. **Automated Tests** - Unit and integration tests
4. **WebSocket** - Real-time collaboration features
5. **SSE** - Live notifications

## Breaking Changes

None - all changes are additive and backward compatible.

## Migration Notes

If you have existing code using `LocalDateTime` serialization, you'll need to:
1. Rebuild `roya-core`
2. Rebuild your application
3. Restart the server
4. Dates will now serialize as ISO-8601 strings instead of arrays
