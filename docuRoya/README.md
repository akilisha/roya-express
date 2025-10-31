# DocuRoya - End-to-End Roya Application

A comprehensive documentation/wiki platform built with Roya that exercises every major feature of the framework.

## Features Demonstrated

✅ **Core Routing** - RESTful API with all HTTP methods  
✅ **Database Plugin** - PostgreSQL + JOOQ + Flyway migrations  
✅ **Auth Plugin** - JWT + sessions, protected routes  
✅ **AI Plugin** - RAG-powered semantic search + chat  
✅ **Email Plugin** - Welcome emails, notifications  
✅ **Cache Plugin** - Hot article caching  
✅ **Object Storage** - File uploads (images, PDFs)  
✅ **WebSocket** - Real-time collaborative editing  
✅ **SSE** - Live notifications stream  
✅ **Middleware** - Rate limiting, logging, CORS  
✅ **Metrics** - Usage tracking  
✅ **Health Checks** - K8s-ready  
✅ **OpenAPI** - Auto-generated API docs  

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    DocuRoya App                         │
├─────────────────────────────────────────────────────────┤
│  Routes: Articles, Auth, Search, Upload, Notifications │
│  Services: ArticleService, SearchService (RAG)         │
│  Middleware: Auth, RateLimit, Morgan                   │
├─────────────────────────────────────────────────────────┤
│  Plugins:                                               │
│  • Database (Postgres + JOOQ)                           │
│  • Auth (JWT + Sessions)                                │
│  • AI (RAG with Qdrant)                                 │
│  • Email (SendGrid/SMTP)                                │
│  • Cache (FFM)                                          │
│  • Object Storage (MinIO)                               │
│  • Metrics (Prometheus)                                 │
└─────────────────────────────────────────────────────────┘
```

## API Endpoints

### Articles
- `GET /api/articles` - List articles (paginated)
- `POST /api/articles` - Create (auth required)
- `GET /api/articles/:id` - Get article
- `PUT /api/articles/:id` - Update (auth + owner)
- `DELETE /api/articles/:id` - Delete (auth + owner)
- `POST /api/articles/:id/comments` - Add comment

### Search
- `POST /api/search` - Semantic search via RAG
- `POST /api/chat` - AI chat over documentation

### Auth
- `POST /api/auth/register` - Register user
- `POST /api/auth/login` - Login (returns JWT)
- `GET /api/me` - Current user (JWT)
- `POST /api/auth/logout` - Logout

### Uploads
- `POST /api/upload` - Upload file → object storage
- `GET /api/upload/:key` - Presigned download URL

### Real-time
- `WS /api/collab/:articleId` - Collaborative editing
- `GET /api/notifications/stream` - SSE notifications

### Admin
- `GET /metrics` - Prometheus metrics
- `GET /health` - Health check
- `GET /openapi.json` - OpenAPI spec

## Setup

### Prerequisites
- Java 21+
- Docker & Docker Compose
- OpenAI API key (for RAG)

### 1. Start Services

```bash
docker compose up -d postgres qdrant minio vault
```

### 2. Configure

```bash
# .env
DATABASE_URL=jdbc:postgresql://localhost:5432/docuRoya
OPENAI_API_KEY=sk-...
EMAIL_PROVIDER=sendgrid
EMAIL_SENDGRID_API_KEY=SG...
OBJECT_STORAGE_ENDPOINT=http://localhost:9000
QDRANT_URL=http://localhost:6333
```

### 3. Run

```bash
./gradlew :docuRoya:run
```

## Database Schema

### Users
- id, email, password_hash, name, created_at

### Articles
- id, user_id, title, content, tags, created_at, updated_at

### Comments
- id, article_id, user_id, content, created_at

## RAG Workflow

1. **Index**: When article created/updated, embed content → Qdrant
2. **Search**: User query → embed → Qdrant search → RAG → answer
3. **Chat**: Multi-turn conversation with RAG context

## Success Criteria

- ✅ All plugins integrated and working
- ✅ Real-time features functional (WS + SSE)
- ✅ AI search returns relevant results
- ✅ File uploads work with presigned URLs
- ✅ Auth protects routes correctly
- ✅ Caching improves response times
- ✅ Metrics exported to Prometheus
- ✅ Health checks pass
- ✅ OpenAPI spec generated
- ✅ Handles 100+ concurrent users
- ✅ Clean, maintainable code structure

## Future Enhancements (Backlog)

- Full-text search (Postgres) + RAG hybrid
- Article versioning/history
- User profiles and avatars
- Email templates (Handlebars)
- OAuth providers (GitHub, Google)
- Export articles (Markdown, PDF)
- Article analytics dashboard

