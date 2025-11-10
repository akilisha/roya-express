# Roya Framework - Implementation Roadmap

**Last Updated**: January 2025

This roadmap outlines the phased implementation plan for Roya Framework. Each phase has clear objectives and success criteria to keep us honest.

---

## Phase 0: Foundation ✅ COMPLETE

**Timeline**: Week 1 (January 2025)

### Objectives
- [x] Complete architectural design
- [x] Write comprehensive whitepaper
- [x] Create public-facing README
- [x] Document core abstractions
- [x] Set up project structure (Gradle)
- [x] Establish project management docs

### Success Criteria
- ✅ Whitepaper captures complete vision (technical + business)
- ✅ README is compelling for developers discovering the project
- ✅ Architecture document guides contributors
- ✅ Project structure is ready for code
- ✅ GitHub repo is public and shareable

### Deliverables
- `WHITEPAPER.md` - Complete technical and business vision
- `README.md` - Public face of the project
- `ARCHITECTURE.md` - Technical blueprint for contributors
- `CONTRIBUTING.md` - Contribution guidelines
- `LICENSE` - Apache 2.0
- `build.gradle` - Gradle project setup
- `docs/ROADMAP.md`, `MILESTONES.md`, `BACKLOG.md`, `ISSUES.md`

---

## Phase 1: Core Abstractions & HTTP Server ✅ COMPLETE

**Timeline**: Weeks 2-3 (January-October 2025)
**Completed**: October 28, 2025

### Objectives
- [x] Implement Express-compatible core interfaces
- [x] Create middleware pipeline executor
- [x] Create Request/Response implementations
- [x] Wire up to Helidon Níma HTTP server
- [x] Achieve "Hello World" working
- [x] Multi-module project structure

### Success Criteria - ALL MET ✅
- ✅ Can run: `app.get("/", handler)` and get response
- ✅ Middleware pipeline executes in order
- ✅ `next()` works correctly (continue and short-circuit)
- ✅ Error handling propagates to error handlers
- ✅ Virtual threads are used for each request
- ✅ Express developer recognizes the API immediately
- ✅ HTTP server starts and accepts requests
- ✅ JSON serialization/deserialization works

### Deliverables - ALL COMPLETE ✅
- `Handler.java` - Core middleware interface ✅
- `Next.java` - Pipeline continuation ✅
- `NextException.java` - Error propagation signal ✅
- `ErrorHandler.java` - Error handler interface ✅
- `Request.java` - HTTP request interface ✅
- `Response.java` - HTTP response interface ✅
- `Params.java`, `Query.java`, `Headers.java`, `Cookies.java` - Supporting interfaces ✅
- `Cookie.java`, `FileSendOptions.java`, `JsonStream.java`, `ServiceKey.java` - Records ✅
- `Roya.java` - Application class with HTTP server integration ✅
- `MiddlewarePipeline.java` - Pipeline executor ✅
- `RequestImpl.java`, `ResponseImpl.java` - Implementations ✅
- `ParamsImpl.java`, `QueryImpl.java`, `HeadersImpl.java`, `CookiesImpl.java` - Supporting impls ✅
- `examples/HelloWorld.java` - Working example ✅

### Tasks - ALL COMPLETE ✅
- [x] Define core interfaces (Handler, Next, Request, Response, ErrorHandler)
- [x] Define supporting types (Params, Query, Headers, Cookies)
- [x] Implement MiddlewarePipeline
- [x] Implement Roya application class
- [x] Integrate Helidon Níma
- [x] Implement Request/Response wrappers
- [x] Create HelloWorld example
- [x] Multi-module structure (roya-api, roya-core, roya-examples)
- [x] JSON support with Jackson
- [x] Cookie parsing and setting
- [x] File sending support

### What Works
```bash
curl http://localhost:3001/
# Returns: Hello from Roya! 🚀
```

**Phase 1 is COMPLETE!** Server runs, accepts requests, executes middleware, and returns responses.

---

## Phase 2: Routing & Path Matching ✅ COMPLETE (Core Features)

**Timeline**: October 2025 - January 2025  
**Completed**: January 13, 2025  
**Status**: Core routing features complete, advanced features deferred

### Objectives
- [x] Implement HTTP method-based routing (GET, POST, PUT, DELETE, etc.) ✅
- [x] Implement path matching for static routes (`/users`, `/api/status`) ✅
- [x] Implement parameterized paths (`/users/:id`) ✅
- [x] Implement Express pattern syntax (`?`, `*`, `+` all working) ✅
- [x] Support nested routers (`app.use("/api", router)`) ✅ COMPLETE
- [x] Support multiple handlers per route (`router.get("/path", h1, h2)`) ✅ COMPLETE
- [x] Path parameter extraction ✅ COMPLETE
- [ ] Implement character classes (`/[0-9]+`) ⏳ DEFERRED (2 tests disabled)
- [ ] Implement full regex support (`Pattern.compile("...")`) ⏳ DEFERRED
- [ ] Path parameter type conversion (`req.param("id", Integer.class)`) ⏳ DEFERRED

### Success Criteria - ALL CORE FEATURES MET ✅
- ✅ HTTP method filtering works (GET only matches GET requests)
- ✅ Static paths match exactly
- ✅ Express path patterns work identically (`?`, `*`, `+` all supported)
- ✅ `req.params()` correctly extracts path parameters
- ✅ Route matching respects registration order
- ✅ Named parameter syntax works (`:id(\\d+)` for regex constraints)
- ✅ 404 for unmatched routes (properly calls outer next())
- ✅ Router composition works (routers contain routers with PathAdjustedRequest)
- ✅ Multiple handlers per route supported
- ✅ Middleware prefix matching works correctly
- ✅ Comprehensive test suite (46 tests, 44 passing, 2 deferred)

### Deliverables - ALL CORE DELIVERABLES COMPLETE ✅
- ✅ `PathMatcher.java` - Interface for path matching
- ✅ `StaticPathMatcher.java` - Exact string match
- ✅ `ExpressPathMatcher.java` - Full Express pattern syntax (`:param`, `*`, `?`, `+`)
- ✅ `PrefixPathMatcher.java` - Middleware prefix matching
- ✅ `PathAdjustedRequest.java` - Nested router support
- ✅ `Route.java` - Route representation
- ✅ `RouteImpl.java` - Route implementation
- ✅ `RouteMatch.java` - Match results
- ✅ `Router.java` - Router interface
- ✅ `RouterImpl.java` - Router implementation
- ✅ `Params.java`, `ParamsImpl.java` - Parameter access
- ✅ Express compatibility test suite (3 test files, 46 tests total)

### Completed Tasks ✅
- [x] Design PathMatcher abstraction
- [x] Implement static path matcher
- [x] Implement parameterized path matcher (`:id`, `:id(\\d+)`)
- [x] Implement pattern path matcher (full Express syntax: `?`, `*`, `+`)
- [x] Implement middleware prefix matcher (PrefixPathMatcher)
- [x] Implement nested router mounting with PathAdjustedRequest
- [x] Add route registration to Router interface
- [x] Implement route matching algorithm
- [x] Add path parameter extraction
- [x] Support multiple handlers per route
- [x] Fix Express.js `next()` chain behavior (only call when no routes match)
- [x] Write comprehensive routing test suite (46 tests)
- [x] Fixed Mockito compatibility issues in tests
- [x] Fixed Java version compatibility (21 instead of 23)

### Deferred Tasks (Non-Blocking for Phase 3)
- [ ] Character classes syntax (`/[0-9]+`) - Advanced feature, uncommon
- [ ] Full regex support with `RegexPathMatcher` - Rarely used in Express
- [ ] Type conversion for path parameters - Can be added when needed

### What Works Right Now ✅
```bash
# Static routes
curl http://localhost:3001/users              # ✅ Exact match
curl http://localhost:3001/api/status         # ✅ Exact match

# Parameterized routes
curl http://localhost:3001/users/123          # ✅ Works - extracts id=123
curl http://localhost:3001/users/:userId/posts/:postId  # ✅ Multiple params

# HTTP method filtering
curl -X POST http://localhost:3001/users      # ✅ Only matches POST
curl -X GET http://localhost:3001/users       # ✅ Only matches GET

# Express pattern syntax
curl http://localhost:3001/files/*           # ✅ Wildcard matches any path
curl http://localhost:3001/ab?c               # ✅ Optional character (matches abc or ac)
curl http://localhost:3001/ab+cd              # ✅ One-or-more pattern

# Middleware with prefix
router.use("/api", middleware)                # ✅ Matches /api, /api/users, etc.

# Multiple handlers per route
router.get("/path", handler1, handler2)       # ✅ Both handlers execute

# Nested routers
Router apiRouter = Router.create();
app.use("/api", apiRouter)                   # ✅ Recursive router composition

# 404 handling
curl http://localhost:3001/nonexistent        # ✅ Properly calls outer next()
```

### Deferred Features (Non-Blocking)
```bash
# Character classes - DEFERRED (advanced feature)
app.get("/users/[0-9]+", handler)             # ⏳ Not yet implemented

# Direct regex - DEFERRED (rarely used)
app.get(Pattern.compile("/users/\\d+"), handler)  # ⏳ RegexPathMatcher not created

# Type conversion - DEFERRED (can add when needed)
int id = req.param("id", Integer.class);     # ⏳ Returns String for now
```

---

## Phase 3: Essential Middleware ✅ COMPLETE

**Timeline**: January 2025  
**Completed**: January 13, 2025  
**Status**: All middleware implemented

### Objectives
- [x] Implement `json()` middleware (body parsing) ✅
- [x] Implement `cors()` middleware ✅
- [x] Implement `helmet()` middleware (security headers) ✅
- [x] Implement `compression()` middleware ✅
- [x] Implement static file serving (placeholder) ✅
- [x] Implement cookie parsing ✅
- [x] Implement bodyParser (multi-format) ✅
- [x] Implement morgan (request logging) ✅
- [x] Implement session management ✅
- [x] Create middleware factory pattern ✅

### Success Criteria - ALL MET ✅
- ✅ JSON body parsing works automatically
- ✅ CORS headers set correctly
- ✅ Security headers added by helmet
- ✅ Response compression enabled
- ✅ Static file API ready
- ✅ All middleware is Express-compatible
- ✅ Comprehensive examples created
- ✅ Factory pattern implemented

### Deliverables - ALL COMPLETE ✅
- ✅ `middleware/Json.java` - JSON body parser factory
- ✅ `middleware/Cors.java` - CORS handler factory
- ✅ `middleware/Helmet.java` - Security headers factory
- ✅ `middleware/Compression.java` - Response compression factory
- ✅ `middleware/BodyParser.java` - Multi-format body parser factory
- ✅ `middleware/Morgan.java` - Request logging factory
- ✅ `middleware/Session.java` - Session management factory
- ✅ `middleware/Static.java` - Static file serving placeholder
- ✅ `middleware/CookieParser.java` - Cookie parsing factory
- ✅ 4 complete examples demonstrating usage

### Tasks - ALL COMPLETE ✅
- [x] Implement JSON serialization/deserialization (Jackson)
- [x] Implement CORS middleware with options
- [x] Implement helmet security headers
- [x] Implement gzip compression
- [x] Implement static file API
- [x] Implement cookie parsing
- [x] Implement bodyParser for multiple formats
- [x] Implement morgan request logging
- [x] Implement session management
- [x] Create middleware test utilities
- [x] Document middleware usage with examples

---

## Phase 4: Plugin System Foundation - ✅ COMPLETE

**Timeline**: January 13, 2025

### Objectives
- [x] Design and implement ServiceRegistry
- [x] Implement plugin lifecycle management
- [x] Create service scoping (singleton, request, prototype)
- [x] Implement Service Locator pattern (`req.get(Class<T>)`)
- [ ] Create plugin discovery mechanism (deferred)
- [ ] Build first plugin: Database (deferred)

### Success Criteria
- ✅ Plugins can register services
- ✅ Services accessible via `req.get(Class<T>)`
- ✅ Lifecycle management (startup, shutdown) works
- ✅ Request-scoped vs app-scoped services work correctly
- ⏳ Database plugin demonstrates full capability (Phase 5)

### Deliverables
- `plugin/RoyaPlugin.java` - Plugin interface
- `plugin/ServiceRegistry.java` - Service container
- `plugin/ServiceProvider.java` - Service factory
- `plugin/Lifecycle.java` - Service lifecycle enum
- `plugin/ServiceKey.java` - Named service keys
- `plugin/database/DatabasePlugin.java` - First plugin
- `ScopedValue` integration - Request context

### Tasks
- [ ] Design plugin interface
- [ ] Implement service registry
- [ ] Implement lifecycle management
- [ ] Implement scoped value integration
- [ ] Create database plugin (JOOQ wrapper)
- [ ] Test plugin installation and service access
- [ ] Document plugin development

---

## Phase 5: Database & Auth Integration - ✅ COMPLETE

**Timeline**: January 2025

### Objectives - Database Plugin
- [x] Implement Database plugin infrastructure
- [x] Docker Compose with auto-migration
- [x] JOOQ wrapper for database access
- [x] HikariCP connection pooling
- [x] CRUD REST API + admin demo endpoints (migrate, generate-model)
- [x] Flyway migrations (plugin-managed)
- [x] JOOQ code generation entrypoint (plugin-managed)

### Objectives - Auth Plugin ✅
- [x] Email/password authentication
- [x] JWT token generation and verification
- [x] Session-based auth (integrated with Session middleware)
- [x] Password reset flow
- [x] Postgres-backed user management (GoTrue-inspired)
- [x] auth.required() and auth.optional() middleware helpers
- [x] OAuth 2.0 support (Google, GitHub)
- [x] OAuth provider abstraction (easy to add more)
- [x] Account linking (OAuth ↔ email accounts)
- [x] Unit tests (8+ tests)
- [x] Integration demo (OAuthDemo)

### Success Criteria  
- ✅ Database plugin architecture established
- ✅ Docker auto-migration working
- ✅ migrate() runs Flyway successfully
- ✅ generateModel() triggers JOOQ codegen
- ✅ Explicit usage via req.get(Database.class)

### Deliverables
- ✅ `plugin/database/Database.java` - Thin JOOQ wrapper + migrate/generateModel
- ✅ `plugin/database/DatabaseServiceImpl.java` - JOOQ + HikariCP + Flyway
- ✅ `plugin/database/DatabasePlugin.java` - Plugin registration
- ✅ `db/migration/` - Migration scripts
- ✅ Admin demo endpoints and usage docs

### Tasks (Completed)
- [x] Integrate JOOQ with Gradle
- [x] Set up HikariCP connection pooling
- [x] Implement Database service API (plugin level)
- [x] Docker auto-migration setup
- [x] CRUD + admin demo with manual JOOQ queries

### Next Plugins (Phase 5 continuation)
- [x] Auth Plugin (JWT, sessions, OAuth) - ✅ COMPLETE
  - Email/password authentication ✅
  - JWT token generation and verification ✅
  - Session-based auth support ✅
  - Password reset flow ✅
  - Postgres-backed (GoTrue-inspired) ✅
  - OAuth providers (Google, GitHub) ✅
  - Unit tests ✅
  - Integration demo ✅
- [ ] Metrics Plugin (Prometheus, /metrics endpoint)
  - Micrometer integration
  - Automatic HTTP metrics collection
  - Custom metrics API
  - Prometheus endpoint at /metrics
- [ ] Cache Plugin (FFM-based native cache, Kafka-style)
  - FFM memory-mapped files (zero-copy, efficient)
  - Offset-based indexing (Kafka-inspired)
  - Configurable eviction strategies (LRU, LFU, TTL, SIZE, ADAPTIVE)
  - Minimal memory footprint (OS page cache)
  - No external dependencies (pure Java, honors Java advances)
- [ ] Email Plugin (Provider-agnostic, thin SDK wrappers)
  - SendGrid, MailerSend, Brevo, Resend, Postmark providers
  - Thin wrappers around provider SDKs (delegate email plumbing)
  - Unified API regardless of provider
  - SMTP fallback (Jakarta Mail)
  - Handlebars template engine
  - Async email sending
  - Provider-specific feature exposure (analytics, tags, webhooks)

---

## Phase 6: AI Integration (OpenAI) - ✅ COMPLETE

**Timeline**: Weeks 16-18 (April 2025)  
**Completed**: January 29, 2025

### Objectives
- [x] Create AI plugin architecture ✅
- [x] Implement OpenAI client (chat completions) ✅
- [x] Support structured outputs (records) ✅
- [x] Implement streaming responses ✅
- [x] Add token counting and cost tracking ✅
- [x] Implement response caching ✅

### Success Criteria
- ✅ `ai.llm().ask(prompt)` returns completions
- ✅ `ai.llm().extract(Record.class, prompt)` returns typed data
- ✅ Streaming responses work (`ai.llm().stream()`)
- ✅ Token usage tracked per request
- ✅ Caching reduces duplicate calls by 90%+

### Deliverables
- ✅ `plugin/ai/AIPlugin.java` - Unified AI plugin and provider registry
- ✅ `plugin/ai/AI.java` - AI service interface
- ✅ Provider integrations: OpenAI, Gemini, Ollama, Mistral, Hugging Face (Inference API)
- ✅ Response orchestration with caching & token counting
- ✅ Structured logging + Helidon health/tracing wiring
- ✅ Integration with Cache plugin for response caching
- ✅ Comprehensive test suite (28 tests, 100% passing)

### Enhancements (Post-MVP)
- ✅ `AIResponse<T>` metadata wrapper (model, tokens, cost, cached)
- ✅ `askWithMetadata()` and `extractWithMetadata()` APIs
- ✅ Expanded `AIOptions` with advanced parameters (topK, penalties, stop, seed, logprobs, etc.)
- ✅ Presets: `forExtraction()`, `forCreative()`, `forCode()`

### Tasks
- [x] Implement OpenAI HTTP client ✅
- [x] Add chat completion support ✅
- [x] Implement structured output (JSON mode) ✅
- [x] Add streaming support ✅
- [x] Implement token counting (jtokkit) ✅
- [x] Add response caching (Cache plugin integration) ✅
- [x] Add cost tracking (per-request) ✅
- [x] Write AI integration tests ✅

### Key Achievements
- **Type-safe extraction**: Java records = AI schemas (zero boilerplate)
- **Automatic caching**: 90%+ cost reduction for duplicate prompts
- **Cost tracking**: Built-in per-request cost calculation
- **Provider-agnostic**: Easy to add Anthropic, Cohere, etc.
- **Comprehensive tests**: 28 tests covering all features
- **Developer experience**: Same pattern as Database/Email plugins

### Enhancements (January 30, 2025)
- ✅ **Vision API**: Multimodal support (image, audio, video, PDF)
  - `ai.vision().analyzeImage()` - Image analysis
  - `ai.vision().transcribeAudio()` - Audio transcription (Gemini)
  - `ai.vision().describeVideo()` - Video description (Gemini)
  - `ai.vision().processPdf()` - PDF processing (Gemini)
- ✅ **Gemini Integration**: Google Gemini provider support
  - `langchain4j-google-ai-gemini` dependency added
  - Environment variable configuration (API key, project, location)
  - Reflection-based model creation for flexibility
- ✅ **VisionNode**: Workflow integration for multimodal operations
  - `.vision()` convenience method on `AIWorkflowBuilder`
  - Configurable operations, input/output keys, prompts
- ✅ **Integration Documentation**: Comprehensive guide for nested/continuation workflows
  - `NESTED_CONTINUATION_AI_WORKFLOWS.md` created
  - Real-world examples and best practices
  - Troubleshooting guide included

### LangChain4j Tutorial Recreation (January 31, 2025)
- ✅ **Complete Tutorial Suite (12 Tutorials)**: All LangChain4j tutorials recreated in Roya
  - Tutorial 01: Basic LLM
  - Tutorial 02: Model Parameters
  - Tutorial 03: Image Generation
  - Tutorial 04: Prompt Templates (framework-level solution)
  - Tutorial 05: Streaming (SSE)
  - Tutorial 06: Memory (ChatMemory integration)
  - Tutorial 07: Few-Shot Learning
  - Tutorial 08: AI Services
  - Tutorial 09: Persistent Memory (database-backed)
  - Tutorial 10: Tools (function calling)
  - Tutorial 11: Dynamic Tools
  - Tutorial 12: RAG with Documents
- ✅ **Real-World Examples**: Coffee Shop Assistant, MCP GitHub Example, Customer Support Agent
- ✅ **Workflow Demo**: Customer Inquiry Processing Workflow showcasing multi-step AI orchestration
- ✅ **Framework Refinement**: Zero reflection, type-safe AI Services, native LangChain4j primitives

### Enhancements (November 2025)
- ✅ Hugging Face Inference provider wiring (`-Dai.provider=huggingface`, embeddings support)
- ✅ Provider override cleanup (system properties + env vars) for OpenAI / Ollama / Mistral / Gemini / Hugging Face
- ✅ Support Desk reference example stabilized (timestamptz casting, Morgan UTC logs, LangChain4j service bindings)

---

## Phase 7: Vector Store & RAG - ✅ COMPLETE

**Timeline**: Weeks 19-21 (April-May 2025)  
**Status**: ✅ COMPLETE  
**Design Doc**: See `PHASE7_DESIGN.md` (updated for unified AI/langchain4j)

### Objectives
- [x] Adopt `langchain4j` as core and expose primitives via unified AI API
- [x] Qdrant-only retrieval via `langchain4j-qdrant` (self-hosted Docker)
- [x] Embeddings and batch indexing
- [x] RAG pipeline (`ai.ragApi().ask`) wired to Qdrant
- [x] Document indexing helpers (`vectors().indexPath` with chunking)

### Success Criteria
- ✅ Documents can be indexed automatically
- ✅ Semantic search returns relevant results
- ✅ `ai.ragApi().ask(question)` works end-to-end
- ✅ Robustness: timeouts/retries to Qdrant
- ✅ Embedding happens automatically (batch)

### Architecture

**Three-Layer Design (updated):**
1. **RAG API** (`ai.ragApi().ask()`) - Developer-facing
2. **RAG Pipeline** - Orchestration (embed → search → context → LLM)
3. **Vector Store Plugin** - Storage layer (Qdrant, embedded)

### Deliverables
- ✅ Unified AI module (`llm()`, `embeddings()`, `vectors()`, `ragApi()`, `agents()`)
- ✅ `langchain4j` + `langchain4j-qdrant` integration
- ✅ Qdrant Docker compose + run instructions
- ✅ Examples updated to new AI API
- ✅ SHOWCASE updated (Why/How this works)

### Tasks
- [x] Refactor to `langchain4j` core
- [x] Qdrant-only retrieval
- [x] Batch embeddings + indexing helpers
- [x] RAG pipeline via `langchain4j`
- [x] Update examples and docs

### Key Design Decisions
- **Embeddings**: OpenAI (MVP), designed for multiple providers
- **Backend**: Qdrant (production) + Embedded (dev)
- **Chunking**: Sentence-aware with configurable overlap
- **Pipeline**: Modular (can swap components)
- **Metadata**: Flexible Map<String, Object>

---

## Next Priorities (Post-Foundation)

Based on completed foundation work, the following items are recommended for the next development cycle:

### High Priority (P1)

1. **RAG Observability & Metrics** (Proper Implementation)
   - Replace placeholder metrics with production-grade implementation
   - Integrate with Micrometer/Prometheus
   - See `docs/BACKLOG.md` for details

2. **RAG Configuration System** (Proper Implementation)
   - Replace placeholder config with proper configuration library
   - Support multiple sources with precedence
   - See `docs/BACKLOG.md` for details

3. **Vector & RAG Polishing** (Phase 7 Continuation)
   - LLM-based reranking (already implemented ✅)
   - Collection management APIs (already implemented ✅)
   - Enhanced chunking presets (already implemented ✅)
   - Remaining: Production-grade observability and config (see above)

### Medium Priority (P2)

4. **Remaining Trigger Nodes** (if needed)
   - SubscriptionTrigger (WebSocket/SSE)
   - ChatTrigger (chat interface)
   - EmailTrigger (email-triggered workflows)
   - AppEventTrigger (internal event triggers)
   - CustomTrigger (generic trigger pattern)

5. **LangChain4j Demo Recreation Project**
   - Comprehensive showcase project
   - See `docs/FUTURE_PROJECTS.md` for details

---

## Phase 8: Operational Enhancements - ✅ COMPLETE

**Timeline**: Weeks 22-24 (May 2025)

### What We Completed
- ✅ Health endpoints via Helidon Health (`/health`, `/health/live`, `/health/ready`)
- ✅ Tracing via Helidon Tracing (backend configurable)
- ✅ CORS via Helidon `CorsSupport`
- ✅ Structured Morgan logging (Logstash JSON) with redaction and trace/span IDs
- ✅ ConfigMiddleware (Helidon Config) registered as a service
- ✅ SecretsMiddleware with Vault-backed `Secrets` (KV v2) and config fallback
- ✅ Object Storage plugin (S3/MinIO) with presigned GET/PUT and multipart
- ✅ Demos: WebSocket, SSE, Fault Tolerance, Scheduling

### Notes
- Vault dev container added to `docker-compose.yml`
- README updated with Vault config keys and usage

---

## Phase 9: CLI Tool

**Timeline**: Weeks 25-27 (May-June 2025)
**Status**: ✅ MVP COMPLETE

### Objectives
- [x] Build `roya` CLI tool (picocli-based)
- [x] Implement `roya new <name>` (project scaffold)
- [x] Implement `roya dev` (run example main)
- [x] Implement `roya run` (module/class runner)
- [x] Implement `roya compose` (docker helpers)
- [ ] Create rich templates (rest-api, ai-rag, object-storage)
- [ ] Add migration tool (Express → Roya)

### Success Criteria
- ✅ Can scaffold a new project quickly
- ✅ Consistent run experience across platforms
- ✅ Useful docker shortcuts for local services
- ⏳ Rich templates (next)

### Deliverables
- `roya-cli/` - CLI tool module (MVP)
- `roya new` - Minimal scaffold
- `roya dev` - Example runner
- `roya run` - Module/class runner
- `roya compose` - Docker helpers
- Unit tests and docs snippets

### Tasks
- [x] Build CLI framework (picocli)
- [x] Implement minimal project generator
- [x] Implement dev/run/compose commands
- [x] Add tests (dry-run) and README docs
- [ ] Design rich templates
- [ ] Additional commands (db/ai/email/storage/openapi/secrets)
- [ ] Write full CLI documentation

---

### Additional Middleware Completed (Phase 8 addendum)
- WebSocket routing hook in `Roya`
- SSE helper (`Sse`)
- Fault Tolerance wrapper (`FaultTolerance`)
- Scheduling utility (`Scheduling`)
- Reactive Streams helper (`Reactive` with Single/Multi)

---

## Phase 10: Production Hardening 🚧 IN PROGRESS

**Timeline**: Weeks 28-32 (June-July 2025)
**Status**: Partially Complete (October 2025)

### Objectives
- [x] Kubernetes deployment templates ✅
- [x] Docker optimized images (JVM + native templates) ✅
- [x] Production deployment guide ✅
- [x] GraalVM native compilation support (plugin + reflection configs) ✅
- [ ] Security audit and hardening
- [ ] Performance optimization
- [ ] Load testing and benchmarking (automation in progress)

### Success Criteria
- ✅ Production deployment guide is complete
- ✅ Docker images build successfully (JVM)
- ✅ Kubernetes manifests with health probes
- [ ] Native binary starts in <50ms (requires reflection configs)
- [ ] Docker image <50MB (native) (requires native-image completion)
- [ ] Passes security audit (OWASP Top 10)
- [ ] Benchmarks meet targets (50K RPS)
- [ ] Can handle 1M concurrent connections

### Deliverables
- [x] Kubernetes manifests (deployment, service) ✅
- [x] Dockerfile (JVM + native variants) ✅
- [x] Production deployment guide (`docs/PRODUCTION.md`) ✅
- [x] K8s README (`deploy/k8s/README.md`) ✅
- [x] GraalVM native-image configuration (roya-plugins:graalvm) ✅
- [ ] Security audit report
- [ ] Performance benchmark suite (automation started, see `bench.ps1`)
- [ ] Load testing scripts

### Tasks
- [x] Create Kubernetes templates ✅
- [x] Build optimized Docker images (JVM multi-stage) ✅
- [x] Document production deployment ✅
- [x] Configure GraalVM native-image (plugin created with reflection/resource/proxy configs) ✅
- [ ] Optimize native compilation
- [ ] Run security audit (OWASP ZAP, etc.)
- [ ] Perform load testing (k6 automation in progress)
- [ ] Optimize hot paths (profiling)

### Notes
- Health endpoints (`/health`, `/health/live`, `/health/ready`) work automatically via Helidon Health
- OpenAPI support available via Helidon OpenAPI (see `OpenApiDemo`)
- Benchmark automation started but needs refinement (see `bench.ps1` in backlog)

---

## Phase 11: Documentation & Examples - 🚧 IN PROGRESS

**Timeline**: Weeks 33-36 (July-August 2025)  
**Started**: January 31, 2025  
**Status**: Foundation Complete, Content In Progress

### Objectives
- [x] Build documentation website ✅
- [x] Write comprehensive guides (20+ topics) ✅
- [x] Create 10+ example applications ✅ (expanded catalogue of agentic demos)
- [ ] Record video tutorials
- [x] Write migration guides (Express, Spring Boot, Quarkus) ✅
- [x] Create API reference ✅

### Success Criteria
- ✅ Docs site is live and searchable (foundation complete)
- ✅ Getting started guide <15 minutes ✅
- ✅ Every feature has example code (examples + agentic scenarios)
- ⏳ Video tutorials cover common use cases (planned)
- ✅ Migration guides are accurate ✅

### Deliverables
- ✅ Documentation website (Preact + TypeScript + Vite) ✅
- ✅ Getting Started guide ✅
- ✅ Middleware guide ✅
- ⏳ Routing guide (basic docs done, needs expansion)
- ✅ Database guide ✅
- ✅ AI/RAG guide ✅
- ✅ 10+ example applications (enhanced + new agentic demos)
- ✅ Aggregated API reference (Gradle `aggregateJavadoc` task)
- ⏳ 5+ video tutorials (planned)
- ✅ Express migration guide ✅
- ✅ Spring Boot migration guide ✅
- ✅ Quarkus migration guide ✅ (added)

### Tasks
- [x] Set up docs site framework ✅
- [x] Write getting started guide ✅
- [x] Document core API features ✅
- [x] Write migration guides ✅
- [x] Fix JSX parsing issues ✅
- [x] Implement documentation search ✅
- [x] Add analytics instrumentation ✅
- [x] Create additional example applications ✅
- [x] Publish Support Desk acceptance checklist and automation ✅
- [x] Generate aggregated API docs from Javadoc (`./gradlew aggregateJavadoc`)
- [x] Complete remaining documentation pages (database deep-dive, production guide)
- [x] Link aggregated Javadoc from docs site (`npm run sync:javadoc`, `/javadoc/index.html`)
- [ ] Record video tutorials
- [x] Add SEO metadata/sitemap automation
- [ ] Get community feedback on docs

---

## Phase 12: Community & Ecosystem

**Timeline**: Ongoing (August 2025+)

### Objectives
- [ ] Reach 1,000+ GitHub stars
- [ ] Attract 50+ contributors
- [ ] 100+ production deployments
- [ ] Conference talks (JavaOne, Devoxx)
- [ ] Active Discord community
- [ ] Plugin ecosystem (10+ community plugins)

### Success Criteria
- ✅ 1,000+ stars on GitHub
- ✅ 50+ contributors
- ✅ 100+ production deployments
- ✅ 2+ conference talks given
- ✅ Active Discord (100+ members)
- ✅ 10+ community-built plugins

### Deliverables
- Community Discord server
- Conference talk presentations
- Blog posts (technical deep-dives)
- Podcast appearances
- Hackathon presence
- Corporate partnerships

---

## Success Metrics (End of 2025)

**Technical:**
- [ ] 50K+ requests/second (single instance)
- [ ] <20ms p99 latency
- [ ] 1M+ concurrent connections
- [ ] <100ms cold start (native)
- [ ] <50MB memory baseline

**Adoption:**
- [ ] 2,000+ GitHub stars
- [ ] 100+ contributors
- [ ] 200+ production deployments
- [ ] 50K+ npm equivalents (Maven Central downloads)

**Business:**
- [ ] $200K ARR (consulting + support)
- [ ] 5+ enterprise contracts
- [ ] 10+ conference talks
- [ ] Featured in Java publications

---

**This roadmap is a living document. Updates will be tracked in `MILESTONES.md`.**
