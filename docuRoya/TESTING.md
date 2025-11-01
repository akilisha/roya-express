# DocuRoya Testing Guide

This guide walks you through setting up and testing the DocuRoya application, which serves as a comprehensive integration test for the Roya framework.

## Overview

DocuRoya is a knowledge base/documentation platform that exercises all major Roya framework features:
- **Database Plugin**: JOOQ code generation, Flyway migrations
- **Auth Plugin**: JWT authentication, protected routes
- **AI Plugin**: RAG search, summaries, chat
- **Cache Plugin**: Hot articles caching
- **Email Plugin**: Welcome emails
- **Metrics Plugin**: Request tracking
- **Object Storage Plugin**: File uploads, presigned URLs
- **Middleware**: Morgan (structured logging), CORS, BodyParser, RateLimit

## Prerequisites

- Java 23+ with `--enable-preview`
- Docker and Docker Compose
- Gradle (or use `./gradlew` wrapper)

## ⚠️ Critical Setup Order

**IMPORTANT**: The setup must follow this exact order - skipping steps will cause failures:

1. ✅ Start Docker services (PostgreSQL, Qdrant, MinIO, Vault)
2. ✅ Create `docuRoya` database
3. ✅ **Run migrations manually** (creates tables in database)
4. ✅ **Generate JOOQ classes** (requires tables to exist)
5. ✅ Build the application (requires JOOQ classes)
6. ✅ Run the application

**Why this order?**
- JOOQ code generation reads from the database schema to generate Java classes
- If the database is empty (no migrations), JOOQ generates nothing ("Tables fetched: 0")
- The app cannot build without generated JOOQ classes
- Therefore: Migrations → JOOQ Generation → Build → Run

## Step 1: Start Docker Services

The DocuRoya app requires several Docker services. Start them using the root `docker-compose.yml`:

```bash
# From project root
docker-compose up -d
```

This starts:
- **PostgreSQL** on port `5432`
- **Qdrant** (vector database) on port `6333`
- **MinIO** (object storage) on ports `9000` (API) and `9001` (Console)
- **Vault** (secrets) on port `8200`

Wait for services to be healthy:
```bash
docker-compose ps
```

All services should show "healthy" status.

## Step 2: Create the DocuRoya Database

The PostgreSQL container is initialized with a default database called `roya`. We need to create a separate database for DocuRoya.

### Option A: Using psql (Recommended)

Connect to the PostgreSQL container:

```bash
docker exec -it roya-postgres psql -U postgres
```

Then create the database:

```sql
CREATE DATABASE "docuRoya";
\q
```

### Option B: Using docker exec with a command

```bash
docker exec -it roya-postgres psql -U postgres -c 'CREATE DATABASE "docuRoya";'
```

### Option C: Create via a SQL script

Create a file `create-docuRoya-db.sql`:

```sql
CREATE DATABASE "docuRoya";
```

Then run:

```bash
docker exec -i roya-postgres psql -U postgres < create-docuRoya-db.sql
```

**Verify the database was created:**

```bash
docker exec -it roya-postgres psql -U postgres -c '\l' | grep docuRoya
```

You should see `docuRoya` in the list of databases.

## Step 3: Run Database Migrations

**⚠️ CRITICAL**: Migrations MUST be run BEFORE JOOQ code generation. JOOQ cannot generate classes from an empty database.

The DocuRoya app uses Flyway migrations to set up the schema. While migrations can run automatically when the app starts, you need to run them manually first to prepare for JOOQ generation.

Migrations are located in `docuRoya/src/main/resources/db/migration/`:
- `V0__create_auth_schema.sql` - Auth plugin tables (`auth_users`, `auth_refresh_tokens`, `auth_password_resets`)
- `V1__create_users.sql` - Users table (optional, if needed for app-specific user data)
- `V2__create_articles.sql` - Articles table
- `V3__create_comments.sql` - Comments table

### Option A: Run Migrations Manually (Required for JOOQ Generation)

**On Linux/Mac:**
```bash
# Run all migrations in order
cat docuRoya/src/main/resources/db/migration/V0__create_auth_schema.sql | docker exec -i roya-postgres psql -U postgres -d docuRoya
cat docuRoya/src/main/resources/db/migration/V2__create_articles.sql | docker exec -i roya-postgres psql -U postgres -d docuRoya
cat docuRoya/src/main/resources/db/migration/V3__create_comments.sql | docker exec -i roya-postgres psql -U postgres -d docuRoya
```

**On Windows (PowerShell):**
```powershell
# Run all migrations in order
Get-Content docuRoya/src/main/resources/db/migration/V0__create_auth_schema.sql | docker exec -i roya-postgres psql -U postgres -d docuRoya
Get-Content docuRoya/src/main/resources/db/migration/V2__create_articles.sql | docker exec -i roya-postgres psql -U postgres -d docuRoya
Get-Content docuRoya/src/main/resources/db/migration/V3__create_comments.sql | docker exec -i roya-postgres psql -U postgres -d docuRoya
```

**Verify migrations succeeded:**
```bash
docker exec roya-postgres psql -U postgres -d docuRoya -c '\dt'
```

You should see tables: `auth_users`, `auth_refresh_tokens`, `auth_password_resets`, `articles`, `comments`.

### Option B: Run Migrations via App Startup (Not Recommended for First Setup)

Migrations can run automatically when `Database.migrate()` is called during app startup (see `DocuRoyaApp.java` line 74). However, this won't work for the initial JOOQ generation since the app can't build without generated classes.

**Note**: After the initial setup, subsequent migrations can run automatically when the app starts.

## Step 4: Generate JOOQ Classes (Critical Step)

**⚠️ IMPORTANT**: JOOQ code generation MUST be run before building the app. The generated classes are required by `ArticleService` and other components.

**⚠️ PREREQUISITE**: Ensure migrations have been run (Step 3) before generating JOOQ classes. JOOQ needs existing tables in the database to generate code from.

### Why JOOQ Generation is Required

The DocuRoya app uses JOOQ's code generation feature to create type-safe Java classes from the database schema:
- **Tables**: `Tables.ARTICLES`, `Tables.COMMENTS`, `Tables.AUTH_USERS`
- **POJOs**: `tables.pojos.Articles`, `tables.pojos.Comments` (if enabled)
- **Records**: Type-safe record classes like `ArticlesRecord` for database operations

These classes are generated at build time and placed in `docuRoya/build/generated/jooq/com/akilisha/oss/roya/docuRoya/jooq/`.

**Important**: JOOQ scans the database schema to generate these classes. If the database has no tables (migrations haven't run), JOOQ will generate nothing and the build will fail.

### How to Generate JOOQ Classes

**Option 1: Using Gradle Task (Recommended)**

From the project root:

```bash
./gradlew :docuRoya:jooqCodegen
```

Or on Windows:

```bash
gradlew.bat :docuRoya:jooqCodegen
```

**Option 2: Using Gradle Wrapper in docuRoya directory**

```bash
cd docuRoya
../gradlew jooqCodegen
```

### Verify JOOQ Generation

After running `jooqCodegen`, check that files were generated:

```bash
ls docuRoya/build/generated/jooq/com/akilisha/oss/roya/docuRoya/jooq/
```

You should see:
- `Tables.java` - Contains table references
- `tables/pojos/Articles.java` - Article POJO
- `tables/pojos/Comments.java` - Comment POJO
- `tables/pojos/Users.java` - User POJO (if V1 migration creates users table)
- `tables/Articles.java` - Articles table definition
- `tables/Comments.java` - Comments table definition
- `tables/Users.java` - Users table definition (if exists)

### JOOQ Configuration

The JOOQ code generation is configured in `docuRoya/build.gradle`:

```gradle
jooq {
    configuration {
        jdbc {
            url = 'jdbc:postgresql://localhost:5432/docuRoya'
            user = 'postgres'
            password = 'postgres'
        }
        generator {
            database {
                includes = 'users|articles|comments|auth_users'
            }
            target {
                packageName = 'com.akilisha.oss.roya.docuRoya.jooq'
            }
        }
    }
}
```

You can override database connection via environment variables:
- `DATABASE_URL` - JDBC URL (default: `jdbc:postgresql://localhost:5432/docuRoya`)
- `DATABASE_USER` - Database user (default: `postgres`)
- `DATABASE_PASSWORD` - Database password (default: `postgres`)

### Common Issues & Troubleshooting

This section documents lessons learned from real-world setup experiences. Database configuration is the most common source of friction.

#### Database Configuration Issues

**Error: "Could not connect to database"**
- Ensure PostgreSQL container is running: `docker-compose ps`
- Verify database exists: `docker exec -it roya-postgres psql -U postgres -c '\l' | grep docuRoya`
- Check connection string in `docuRoya/src/main/resources/application.yaml` or environment variables
- **Configuration Priority**: 
  1. Environment variables (`DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`)
  2. `application.yaml` file (`database.url`, `database.username`, `database.password`)
  3. Plugin defaults (generic `postgres` database)

**Error: "No database found to handle jdbc:postgresql://..."**
- **Cause**: Flyway 10+ requires separate database module dependency
- **Solution**: Ensure `roya-plugins/database/build.gradle` includes:
  ```gradle
  implementation 'org.flywaydb:flyway-core:11.7.2'
  implementation 'org.flywaydb:flyway-database-postgresql:11.7.2'  // Required for PostgreSQL
  ```
- Flyway now uses separate modules per database type (not bundled)

**Error: "Found non-empty schema(s) 'public' but no schema history table"**
- **Cause**: Database has tables from manual migrations, but Flyway history table doesn't exist
- **Solution**: `baselineOnMigrate(true)` is automatically configured - Flyway will baseline existing schema
- This is a **one-time operation** - Flyway creates history table and marks existing migrations as applied
- Subsequent runs use normal idempotent migration behavior

**Error: "relation 'idx_articles_user_id' already exists" or similar index errors**
- **Cause**: Migrations are not idempotent - indexes created without `IF NOT EXISTS`
- **Solution**: All migrations now use `CREATE INDEX IF NOT EXISTS` for idempotency
- Migrations can now be safely re-run multiple times without errors

**Error: "relation 'users' does not exist" in comments migration**
- **Cause**: Migration references wrong table name (should use `auth_users` not `users`)
- **Solution**: Ensure all migrations reference correct tables:
  - Use `auth_users` (from Auth plugin), not `users`
  - Verify foreign key references match actual table names

**Error: "Found more than one migration with version 1"**
- **Cause**: Duplicate migration files with same version number
- **Solution**: 
  - Check for migrations in both plugin module AND application module
  - **Migrations should ONLY be in the application module** (`docuRoya/src/main/resources/db/migration/`)
  - Plugins should NOT contain migrations (they're application-specific)
  - Remove any `db/migration/` directories from plugin modules

#### JOOQ Code Generation Issues

**Error: "Tables fetched: 0" or empty generated directory**
- **This means migrations haven't run!** This is the most common issue.
- Run migrations first (via `db.migrate()` or manually)
- Verify tables exist: `docker exec roya-postgres psql -U postgres -d docuRoya -c '\dt'`
- You should see at least: `auth_users`, `articles`, `comments`
- Then re-run: `./gradlew :docuRoya:jooqCodegen`

**Error: "jooq-config.xml not found"**
- **Cause**: Path resolution fails when running from Gradle (working directory is project root)
- **Solution**: `generateModel()` now searches multiple locations:
  1. Classpath resource (`jooq-config.xml`)
  2. `docuRoya/src/main/resources/jooq-config.xml` (from project root)
  3. `src/main/resources/jooq-config.xml` (from module directory)
- Verify `jooq-config.xml` exists in `docuRoya/src/main/resources/`

**Error: Generated classes don't match schema**
- **Cause**: `jooq-config.xml` includes wrong table names
- **Solution**: Update `includes` in `jooq-config.xml` to match actual tables:
  ```xml
  <includes>auth_users|articles|comments</includes>
  ```
  - Use `auth_users` (from Auth plugin), not `users`
  - Ensure all referenced tables exist in the database

**Error: "Cannot find symbol: Tables"**
- JOOQ classes weren't generated - run `./gradlew :docuRoya:jooqCodegen`
- **Check that migrations ran first** - empty database = no generated files
- Check that `build/generated/jooq/com/akilisha/oss/roya/docuRoya/jooq/` directory exists and contains generated files
- Rebuild the project: `./gradlew :docuRoya:clean :docuRoya:jooqCodegen :docuRoya:build`

#### Migration Best Practices

**Making Migrations Idempotent**
- Always use `CREATE TABLE IF NOT EXISTS` for tables
- Always use `CREATE INDEX IF NOT EXISTS` for indexes
- Use `ON CONFLICT DO NOTHING` for initial data inserts
- This allows migrations to run safely multiple times

**Table Naming Consistency**
- Use `auth_users` (from Auth plugin) for user references
- Don't create duplicate `users` tables unless needed
- Verify foreign keys reference correct table names

**Flyway Baseline Behavior**
- `baselineOnMigrate(true)` automatically handles existing schemas
- First run: Creates history table, baselines existing migrations
- Subsequent runs: Normal idempotent migration (only runs new ones)
- Safe to call `migrate()` on every application startup

#### Configuration Management

**Where Database URL is Configured**
- **Primary**: `docuRoya/src/main/resources/application.yaml` (application-specific)
- **Fallback**: Environment variables (`DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`)
- **Plugin Default**: `jdbc:postgresql://localhost:5432/postgres` (generic, not app-specific)
- **Never hardcode app-specific configs in plugins** - use application config files

**Plugin vs Application Configuration**
- **Plugins**: Generic defaults (e.g., `postgres` database)
- **Applications**: Specific configs in `application.yaml` (e.g., `docuRoya` database)
- Configuration priority ensures applications override plugin defaults

## Step 5: Build the Application

Once JOOQ classes are generated, build the app:

```bash
./gradlew :docuRoya:build
```

This will:
1. Compile Java source (including generated JOOQ classes)
2. Run tests (if any)
3. Package the application

## Step 6: Configure Environment Variables (Optional)

The app uses various plugins that may require configuration:

### Auth Plugin

- `auth.jwt.secret` - JWT signing secret (default: insecure default, change in production!)

### Database Plugin

- `DATABASE_URL` - JDBC connection string
- `DATABASE_USER` - Database user
- `DATABASE_PASSWORD` - Database password

### AI Plugin

- `OPENAI_API_KEY` or `AI_OPENAI_API_KEY` - OpenAI API key for RAG/search features

### Email Plugin

- `SENDGRID_API_KEY` - SendGrid API key (optional, falls back to SMTP)
- SMTP configuration via system properties

### Object Storage Plugin

- `AWS_ACCESS_KEY_ID` - MinIO access key (default: minioadmin)
- `AWS_SECRET_ACCESS_KEY` - MinIO secret key (default: minioadmin)
- `AWS_ENDPOINT` - MinIO endpoint (default: http://localhost:9000)

Create a `.env` file in `docuRoya/` or export environment variables before running.

## Step 7: Run the Application

**Option 1: Using Gradle Run Task**

```bash
./gradlew :docuRoya:run
```

**Option 2: Using Generated JAR**

After building:

```bash
java --enable-preview -jar docuRoya/build/libs/docuRoya-*.jar
```

**Option 3: Direct Execution**

```bash
cd docuRoya
java --enable-preview -cp "build/classes/java/main:build/libs/*:../roya-core/build/libs/*:..." \
    com.akilisha.oss.roya.docuRoya.DocuRoyaApp
```

The app will:
1. Register all plugins (Database, Auth, Metrics, Cache, Email, AI, ObjectStorage)
2. Run Flyway migrations (creates tables)
3. Generate JOOQ classes (if not already done)
4. Start HTTP server on port `3000` (or port specified via `-Dport=...`)

You should see output like:

```
✓ DatabasePlugin: Starting
✓ AuthPlugin: Starting
✓ MetricsPlugin: Starting
✓ CachePlugin: Starting
✓ EmailPlugin: Starting
✓ AIPlugin: Starting
✓ ObjectStoragePlugin: Starting
DocuRoya running on http://localhost:3003
Features: Database ✅ Auth ✅ AI ✅ Email ✅ Cache ✅ Object Storage ✅ Metrics ✅
```

## Step 8: Test the Application

### Health Check

```bash
curl http://localhost:3003/health
```

Expected: `{"status":"ok"}`

### Register a User

```bash
curl -X POST http://localhost:3003/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "password123",
    "name": "Alice"
  }'
```

Expected: JSON with `user`, `token`, and `refreshToken`.

### Login

```bash
curl -X POST http://localhost:3003/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "password123"
  }'
```

Save the `token` from the response.

### Create an Article (Protected Route)

```bash
TOKEN="<token-from-login>"

curl -X POST http://localhost:3003/api/articles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Getting Started with Roya",
    "content": "Roya is an Express.js-compatible web framework for Java...",
    "tags": ["java", "framework", "web"]
  }'
```

### List Articles

```bash
curl http://localhost:3003/api/articles
```

### Get Article by ID

```bash
ARTICLE_ID="<uuid-from-create>"
curl http://localhost:3003/api/articles/$ARTICLE_ID
```

### Search (AI/RAG)

Requires `OPENAI_API_KEY` and Qdrant running:

```bash
curl -X POST http://localhost:3003/api/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "How does authentication work in Roya?"
  }'
```

### Upload File (Object Storage)

Requires MinIO running:

```bash
TOKEN="<token-from-login>"
echo "Hello, DocuRoya!" > test.txt
CONTENT=$(base64 -i test.txt)

curl -X POST http://localhost:3003/api/upload \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"fileName\": \"test.txt\",
    \"contentType\": \"text/plain\",
    \"content\": \"$CONTENT\"
  }"
```

### View Metrics

```bash
curl http://localhost:3003/metrics
```

Expected: Prometheus-formatted metrics.

## Testing Checklist

Use this checklist to verify all framework features:

### Core Framework
- [ ] Middleware chain executes in correct order (Morgan → CORS → BodyParser → RateLimit)
- [ ] Structured logging appears in console (JSON format)
- [ ] CORS headers present in responses
- [ ] Request body parsed correctly (JSON)

### Plugin System
- [ ] Services retrieved via `req.get(Class<T>)`
- [ ] Plugins registered and accessible
- [ ] Service resolution works for all plugins

### Database Plugin
- [ ] Migrations run successfully (check `flyway_schema_history` table)
- [ ] JOOQ classes generated correctly
- [ ] Type-safe queries work (`ArticleService` uses generated classes)

### Auth Plugin
- [ ] User registration works
- [ ] Login returns JWT token
- [ ] Protected routes require authentication (`auth.required()`)
- [ ] User context available in protected routes (`req.get("user")`)
- [ ] Token refresh works (`/api/auth/refresh`)

### Cache Plugin
- [ ] Hot articles endpoint caches results
- [ ] Cache headers present in responses
- [ ] Cache invalidation works (or expires after TTL)

### Metrics Plugin
- [ ] Metrics endpoint accessible (`/metrics`)
- [ ] Custom metrics tracked (article views, creations)
- [ ] Prometheus format correct

### AI Plugin (requires API key)
- [ ] RAG search works (`/api/search`)
- [ ] AI chat works (`/api/chat`)
- [ ] Summaries generated (`/api/articles/:id/summarize`)
- [ ] Cache integration works (AI responses cached)

### Email Plugin
- [ ] Welcome email sent on registration (check logs)
- [ ] Email failures don't block registration

### Object Storage Plugin
- [ ] File upload works (`/api/upload`)
- [ ] Presigned URLs generated (`/api/upload/:key/presigned`)
- [ ] Files accessible via presigned URLs

### Rate Limiting
- [ ] Global rate limit applied (check headers)
- [ ] Stricter limits on AI endpoints
- [ ] 429 responses when limit exceeded
- [ ] `Retry-After` header present

## Troubleshooting

### App Won't Start

1. **Check migrations ran first**:
   ```bash
   docker exec roya-postgres psql -U postgres -d docuRoya -c '\dt'
   ```
   Should show tables: `auth_users`, `articles`, `comments`. If empty, run migrations manually (Step 3).

2. **Check JOOQ classes generated**:
   ```bash
   # Linux/Mac
   ls docuRoya/build/generated/jooq/com/akilisha/oss/roya/docuRoya/jooq/
   # Windows PowerShell
   Get-ChildItem docuRoya/build/generated/jooq/com/akilisha/oss/roya/docuRoya/jooq/ -Recurse -File
   ```
   Should show `Tables.java`, `tables/Articles.java`, etc. If empty, ensure migrations ran first, then: `./gradlew :docuRoya:jooqCodegen`

3. **Check database connection**:
   ```bash
   docker exec -it roya-postgres psql -U postgres -d docuRoya -c 'SELECT 1;'
   ```
   Should return `1`. If connection fails, check PostgreSQL container is running.

4. **Check Docker services**:
   ```bash
   docker-compose ps
   ```
   PostgreSQL should be "healthy".

### Compilation Errors

1. **"Cannot find symbol: Tables"**:
   - Run JOOQ generation: `./gradlew :docuRoya:jooqCodegen`
   - Clean and rebuild: `./gradlew :docuRoya:clean :docuRoya:build`

2. **"Cannot find symbol: Auth"**:
   - Ensure `roya-plugins:auth` is in dependencies
   - Rebuild plugins: `./gradlew :roya-plugins:auth:build`

### Runtime Errors

1. **"Table 'auth_users' does not exist"**:
   - Migrations didn't run - check `DocuRoyaApp.java` calls `db.migrate()`
   - Verify migrations exist in `src/main/resources/db/migration/`
   - Check Flyway logs for errors

2. **"Connection refused" (database)**:
   - PostgreSQL container not running: `docker-compose up -d postgres`
   - Wrong connection string - check environment variables

3. **"401 Unauthorized"**:
   - Token expired or invalid
   - Missing `Authorization: Bearer <token>` header
   - Token format incorrect

## Quick Start Summary

For a quick test run:

```bash
# 1. Start services
docker-compose up -d

# 2. Create database
docker exec -it roya-postgres psql -U postgres -c 'CREATE DATABASE "docuRoya";'

# 3. Run migrations manually (REQUIRED before JOOQ generation!)
# Linux/Mac:
cat docuRoya/src/main/resources/db/migration/V0__create_auth_schema.sql | docker exec -i roya-postgres psql -U postgres -d docuRoya
cat docuRoya/src/main/resources/db/migration/V2__create_articles.sql | docker exec -i roya-postgres psql -U postgres -d docuRoya
cat docuRoya/src/main/resources/db/migration/V3__create_comments.sql | docker exec -i roya-postgres psql -U postgres -d docuRoya

# Windows PowerShell:
Get-Content docuRoya/src/main/resources/db/migration/V0__create_auth_schema.sql | docker exec -i roya-postgres psql -U postgres -d docuRoya
Get-Content docuRoya/src/main/resources/db/migration/V2__create_articles.sql | docker exec -i roya-postgres psql -U postgres -d docuRoya
Get-Content docuRoya/src/main/resources/db/migration/V3__create_comments.sql | docker exec -i roya-postgres psql -U postgres -d docuRoya

# Verify migrations:
docker exec roya-postgres psql -U postgres -d docuRoya -c '\dt'

# 4. Generate JOOQ classes (CRITICAL! Requires tables to exist)
./gradlew :docuRoya:jooqCodegen

# 5. Build
./gradlew :docuRoya:build

# 6. Run
./gradlew :docuRoya:run
```

## Testing Status Summary

### ✅ Fully Tested
- **Core Framework**: Tree routing, middleware chain, body parsing
- **Database Plugin**: Migrations, JOOQ generation, CRUD operations
- **Auth Plugin**: Registration, login, protected routes
- **Basic Endpoints**: Article CRUD, search

### ⚠️ Partially Tested
- **Cache Plugin**: Used in `/hot` endpoint but cache behavior not verified
- **Email Plugin**: Registration sends email but delivery not confirmed
- **Metrics Plugin**: Endpoint works but metrics data not verified
- **AI Plugin**: RAG search works but chat, summarize, caching not tested
- **Object Storage**: Upload works but presigned URLs, listing, multipart not tested

### ❌ Not Yet Implemented/Tested
- **WebSocket**: Real-time collaboration (commented as "next phase")
- **SSE**: Live notifications stream (commented as "next phase")
- **Client Streaming**: Progressive data loading
- **Advanced Object Storage**: Multipart upload, presigned upload URLs
- **Automated Tests**: Unit tests, integration tests, E2E tests

### 📋 Detailed Testing Plan

See `docuRoya/TESTING_PLAN.md` for comprehensive testing requirements covering:
- Explicit cache verification
- Email delivery confirmation
- Metrics data validation
- AI plugin full feature set
- Object storage advanced operations
- WebSocket and SSE (when implemented)
- Load testing and benchmarking

## Next Steps

Once the app is running and basic tests pass:

1. **Add React Frontend**: Create a UI that exercises all endpoints
2. **Integration Tests**: Add automated tests for critical paths
3. **Load Testing**: Use k6 or similar to test under load
4. **Real-time Features**: Add WebSocket endpoints for collaborative editing
5. **SSE**: Add server-sent events for live notifications
6. **Complete Testing**: Follow `TESTING_PLAN.md` to verify all features

## Contributing

When adding new features to DocuRoya:

1. Update migrations if schema changes
2. Regenerate JOOQ classes: `./gradlew :docuRoya:jooqCodegen`
3. Update this testing guide if setup steps change
4. Document new endpoints in this guide
5. Add test cases to `TESTING_PLAN.md`

