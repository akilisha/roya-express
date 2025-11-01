# DocuRoya Quick Start Guide

Get the full DocuRoya platform (backend + frontend) running in minutes.

## Prerequisites

- Java 23+ with `--enable-preview`
- Docker and Docker Compose
- Node.js 18+ and npm
- Gradle (or use `./gradlew`)

## Step 1: Start Docker Services

```bash
docker-compose up -d
```

This starts:
- PostgreSQL on port `5432`
- Qdrant on port `6333`
- MinIO on ports `9000` (API) and `9001` (Console)
- Vault on port `8200`

Wait for all services to be healthy:
```bash
docker-compose ps
```

## Step 2: Setup Database

```bash
# Create DocuRoya database
docker exec -it roya-postgres psql -U postgres -c 'CREATE DATABASE "docuRoya";'

# Run migrations manually (REQUIRED before JOOQ generation)
cat docuRoya/src/main/resources/db/migration/V0__create_auth_schema.sql | docker exec -i roya-postgres psql -U postgres -d docuRoya
cat docuRoya/src/main/resources/db/migration/V2__create_articles.sql | docker exec -i roya-postgres psql -U postgres -d docuRoya
cat docuRoya/src/main/resources/db/migration/V3__create_comments.sql | docker exec -i roya-postgres psql -U postgres -d docuRoya

# Verify tables created
docker exec roya-postgres psql -U postgres -d docuRoya -c '\dt'
```

**On Windows (PowerShell):**
```powershell
Get-Content docuRoya/src/main/resources/db/migration/V0__create_auth_schema.sql | docker exec -i roya-postgres psql -U postgres -d docuRoya
Get-Content docuRoya/src/main/resources/db/migration/V2__create_articles.sql | docker exec -i roya-postgres psql -U postgres -d docuRoya
Get-Content docuRoya/src/main/resources/db/migration/V3__create_comments.sql | docker exec -i roya-postgres psql -U postgres -d docuRoya
```

## Step 3: Generate JOOQ Classes

```bash
./gradlew :docuRoya:jooqCodegen
```

Verify generated files:
```bash
ls docuRoya/build/generated/jooq/com/akilisha/oss/roya/docuRoya/jooq/
```

## Step 4: Build Backend

```bash
./gradlew :roya-core:build -x test  # Build roya-core first
./gradlew :docuRoya:build
```

**Note**: After any changes to `roya-core` or `docuRoya`, rebuild before running.

## Step 5: Setup Frontend

```bash
cd docuRoya/frontend
npm install
```

## Step 6: Start Backend

In one terminal:

```bash
cd docuRoya
./gradlew :docuRoya:run --args='--enable-preview -Dport=3003'
```

Or use the generated JAR:
```bash
cd docuRoya
java --enable-preview -Dport=3003 -jar build/libs/docuRoya-*.jar
```

Backend should start on `http://localhost:3003`.

## Step 7: Start Frontend

In another terminal:

```bash
cd docuRoya/frontend
npm run dev
```

Frontend should start on `http://localhost:3000`.

## Step 8: Test Everything

### Browser Testing

1. Open `http://localhost:3000`
2. Click "Register" to create an account
3. Log in with your credentials
4. Click "Articles" → "New Article" to create content
5. Visit "Upload" to upload files
6. Go to "Testing" dashboard to test cache, metrics, rate limiting

### CLI Testing

```bash
# Health check
curl http://localhost:3003/health

# Register user
curl -X POST http://localhost:3003/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password","name":"Test User"}'

# Login
TOKEN=$(curl -s -X POST http://localhost:3003/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}' \
  | jq -r .token)

# Create article
curl -X POST http://localhost:3003/api/articles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Getting Started with Roya",
    "content": "Roya is a powerful Express.js-compatible framework...",
    "tags": ["java", "framework", "web"]
  }'

# List articles
curl http://localhost:3003/api/articles

# View metrics
curl http://localhost:3003/metrics
```

### Testing Dashboard

Visit `http://localhost:3000/testing` (login required) to test:

- **Cache**: Set, get, delete cache entries
- **Metrics**: Custom counters, gauges, timers
- **Rate Limiting**: Test different limit configurations

### CORS Testing

The frontend runs on port 3000 while backend is on 3003, so every request tests CORS. Open DevTools Network tab to inspect CORS headers.

## Optional: Configure Email

Set up email testing (optional):

```bash
export SENDGRID_API_KEY="your_sendgrid_key"
# Or configure SMTP in application.yaml
```

## Optional: Configure AI Search

```bash
export OPENAI_API_KEY="sk-proj-your_key_here"
```

Then test search:
```bash
curl -X POST http://localhost:3003/api/search \
  -H "Content-Type: application/json" \
  -d '{"query": "What is the documentation about?"}'
```

## Troubleshooting

### Backend won't start

1. Check Docker services: `docker-compose ps`
2. Verify database exists: `docker exec -it roya-postgres psql -U postgres -c '\l'`
3. Check JOOQ classes generated: `ls docuRoya/build/generated/jooq/...`
4. Check port 3003 is available

### Frontend can't connect

1. Verify backend is running on port 3003
2. Check proxy config in `vite.config.js`
3. Open browser console for CORS errors
4. Verify `npm install` completed successfully

### Database errors

1. Re-run migrations (see Step 2)
2. Check `docuRoya/src/main/resources/application.yaml` for DB config
3. Verify PostgreSQL container is healthy

## Next Steps

Once everything is running:

1. **Create Articles** - Build your knowledge base
2. **Upload Files** - Test object storage
3. **Run Tests** - Use testing dashboard
4. **Check Metrics** - Visit `/metrics`
5. **Explore Code** - Learn from the examples

## Documentation

- **TESTING.md** - Comprehensive testing guide
- **TESTING_PLAN.md** - Complete feature testing plan
- **frontend/README.md** - Frontend-specific documentation
- **PLUGIN_USAGE.md** - Plugin usage examples

## What's Next?

- Load testing with k6
- Stress testing scripts
- WebSocket real-time features
- SSE live notifications
- Advanced metrics visualization

Enjoy testing DocuRoya! 🚀

