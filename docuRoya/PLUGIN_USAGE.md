# DocuRoya - Plugin & Feature Usage

This document maps each Roya plugin/feature to a practical use case in DocuRoya.

## Database Plugin ✅
**Use Case**: All data persistence
- Users table (JOOQ generated)
- Articles table (JOOQ generated)
- Comments table (JOOQ generated)
- JOOQ code generation from migrations
- Transaction support for multi-step operations

**Endpoints**:
- All CRUD operations use Database plugin

---

## Auth Plugin ✅
**Use Case**: User authentication & authorization
- Registration: `POST /api/auth/register` → create user + send welcome email
- Login: `POST /api/auth/login` → JWT token + session cookie
- Protected routes: `auth.required()` middleware
- Current user: `GET /api/me` → from JWT

**Endpoints**:
- `POST /api/auth/register` - Register new user (uses Database + Email)
- `POST /api/auth/login` - Login (returns JWT)
- `GET /api/me` - Current user (JWT protected)
- All article creation/editing requires auth

---

## AI Plugin ✅
**Use Case**: Intelligent search and summarization
- **RAG Search**: Index articles → semantic search over content
- **Article Summaries**: Generate summaries for long articles
- **Chat Assistant**: Ask questions about documentation
- **Auto-tagging**: AI suggests tags for articles

**Endpoints**:
- `POST /api/search` - Semantic search (RAG over indexed articles)
- `POST /api/articles/:id/summarize` - Generate article summary
- `POST /api/chat` - AI chat interface (RAG-powered)
- `POST /api/articles/:id/suggest-tags` - AI suggests tags

**Caching**: Cache AI responses (demonstrates Cache + AI integration)

---

## Email Plugin ✅
**Use Case**: User notifications and communication
- Welcome email on registration
- Comment notifications (email user when article receives comment)
- Password reset emails
- Weekly digest (scheduled via Scheduling middleware)

**Endpoints**:
- Triggered automatically:
  - User registration → welcome email
  - Comment posted → notify article author
  - Password reset requested → reset link email
- `POST /api/notifications/test-email` - Admin endpoint to test email delivery

---

## Cache Plugin ✅
**Use Case**: Performance optimization
- Cache hot articles (top 100 most viewed)
- Cache AI search results (same query = cached response)
- Cache user profile lookups
- Cache article counts/statistics

**Endpoints**:
- `GET /api/articles/hot` - Returns cached hot articles
- `POST /api/search` - Cached semantic search results
- `GET /api/stats` - Cached dashboard statistics

---

## Object Storage Plugin ✅
**Use Case**: File uploads and attachments
- Article images: upload images for articles
- Article attachments: upload PDFs/docs
- User avatars: profile pictures
- Presigned URLs: secure download links

**Endpoints**:
- `POST /api/upload` - Upload file → returns key
- `GET /api/upload/:key` - Get presigned download URL
- `POST /api/articles/:id/attach` - Attach file to article
- `POST /api/users/avatar` - Upload user avatar

---

## Metrics Plugin ✅
**Use Case**: Observability and analytics
- Track article views
- Track API endpoint usage
- Track search queries
- Track user registrations
- Expose `/metrics` for Prometheus scraping

**Endpoints**:
- `GET /metrics` - Prometheus metrics
- All endpoints automatically tracked
- Custom metrics: article.views, search.queries, user.registrations

---

## WebSocket ✅
**Use Case**: Real-time collaboration
- Collaborative editing: multiple users editing same article
- Live presence: show who's viewing/editing
- Real-time comment updates

**Endpoints**:
- `WS /api/collab/:articleId` - Real-time collaborative editing
- Send/receive edit events, cursor positions, presence updates

---

## SSE (Server-Sent Events) ✅
**Use Case**: Live notifications
- Real-time notifications feed
- Article update streams
- New comment notifications

**Endpoints**:
- `GET /api/notifications/stream` - SSE stream of user notifications
- `GET /api/articles/:id/updates` - SSE stream of article updates

---

## Rate Limiting Middleware ✅
**Use Case**: API protection
- Protect search endpoint (expensive AI calls)
- Protect registration endpoint (prevent spam)
- Different limits for authenticated vs anonymous users

**Usage**:
- Global rate limit: 100 req/15min per IP
- Search endpoint: 20 req/min per IP (stricter)
- Registration: 5 req/hour per IP

---

## Health Checks ✅
**Use Case**: Production readiness
- K8s liveness/readiness probes
- Dependency health checks (DB, Qdrant, MinIO)

**Endpoints**:
- `GET /health` - Overall health
- `GET /health/live` - Liveness probe
- `GET /health/ready` - Readiness probe (checks DB, Qdrant, MinIO)

---

## OpenAPI ✅
**Use Case**: API documentation
- Auto-generated API docs from routes
- Interactive Swagger UI

**Endpoints**:
- `GET /openapi.json` - OpenAPI specification
- `GET /swagger-ui` - Interactive documentation

---

## Additional Features

### Scheduling Middleware
**Use Case**: Background jobs
- Weekly digest email (every Monday 9am)
- Re-index articles for RAG (nightly)
- Cleanup expired sessions (hourly)

### Fault Tolerance
**Use Case**: Resilience
- Retry failed AI API calls
- Circuit breaker for external services
- Timeout protection for long-running operations

### Reactive Streams
**Use Case**: Streaming responses
- Stream large article lists
- Stream search results as they're found
- Stream file downloads

---

## Integration Points

1. **AI + Cache**: Cache AI search results to reduce costs
2. **Auth + Email**: Welcome email on registration
3. **Database + JOOQ**: Generated models from schema
4. **Object Storage + Articles**: Attach files to articles
5. **Metrics + All**: Track everything for observability
6. **Health + All**: Check all plugin dependencies
7. **WebSocket + SSE**: Real-time features
8. **Rate Limit + All**: Protect all endpoints

---

## Success Criteria

- ✅ All 6 plugins actively used (Database, Auth, AI, Email, Cache, Object Storage, Metrics)
- ✅ All core features used (WebSocket, SSE, RateLimit, Health, OpenAPI)
- ✅ Demonstrates plugin integration (AI+Cache, Auth+Email, etc.)
- ✅ Real-world use cases (not contrived)
- ✅ Production-ready patterns

