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

## Phase 1: Core Abstractions 🚧 IN PROGRESS

**Timeline**: Weeks 2-3 (January 2025)

### Objectives
- [ ] Implement Express-compatible core interfaces
- [ ] Create middleware pipeline executor
- [ ] Implement basic routing (static paths only)
- [ ] Create Request/Response implementations
- [ ] Wire up to Helidon Níma HTTP server
- [ ] Achieve "Hello World" working

### Success Criteria
- ✅ Can run: `app.get("/", handler)` and get response
- ✅ Middleware pipeline executes in order
- ✅ `next()` works correctly (continue and short-circuit)
- ✅ Error handling propagates to error handlers
- ✅ Virtual threads are used for each request
- ✅ Express developer recognizes the API immediately

### Deliverables
- `Handler.java` - Core middleware interface ✅
- `Next.java` - Pipeline continuation ✅
- `Request.java` - HTTP request interface ✅
- `Response.java` - HTTP response interface
- `Roya.java` - Application class
- `Router.java` - Basic router
- `MiddlewarePipeline.java` - Pipeline executor
- `RequestImpl.java`, `ResponseImpl.java` - Implementations
- `examples/HelloWorld.java` - Working example

### Tasks
- [x] Define core interfaces (Handler, Next, Request)
- [ ] Define Response interface
- [ ] Define supporting types (Params, Query, Headers, Cookies)
- [ ] Implement MiddlewarePipeline
- [ ] Implement Roya application class
- [ ] Integrate Helidon Níma
- [ ] Implement Request/Response wrappers
- [ ] Create HelloWorld example
- [ ] Write unit tests for pipeline

---

## Phase 2: Routing & Path Matching

**Timeline**: Weeks 4-6 (February 2025)

### Objectives
- [ ] Implement parameterized paths (`/users/:id`)
- [ ] Implement Express pattern syntax (`/ab?cd`, `/ab+cd`)
- [ ] Implement character classes (`/[0-9]+`)
- [ ] Implement full regex support (`Pattern.compile("...")`)
- [ ] Support route-specific middleware
- [ ] Support nested routers (`app.use("/api", router)`)
- [ ] Path parameter extraction

### Success Criteria
- ✅ All Express path patterns work identically
- ✅ `req.params()` correctly extracts path parameters
- ✅ Route matching respects registration order
- ✅ Named groups in regex work (`(?<id>[0-9]+)`)
- ✅ Router composition works (routers contain routers)

### Deliverables
- `PathMatcher.java` - Interface for path matching
- `StaticPathMatcher.java` - Exact string match
- `ParameterizedPathMatcher.java` - `:param` extraction
- `PatternPathMatcher.java` - Express pattern syntax
- `RegexPathMatcher.java` - Full regex support
- `Route.java` - Route representation
- `Params.java`, `Query.java` - Parameter access
- Express compatibility test suite

### Tasks
- [ ] Design PathMatcher abstraction
- [ ] Implement static path matcher
- [ ] Implement parameterized path matcher
- [ ] Implement pattern path matcher (Express syntax)
- [ ] Implement regex path matcher
- [ ] Add route registration to Roya class
- [ ] Implement route matching algorithm
- [ ] Add path parameter extraction
- [ ] Write comprehensive routing tests

---

## Phase 3: Essential Middleware

**Timeline**: Weeks 7-9 (February-March 2025)

### Objectives
- [ ] Implement `json()` middleware (body parsing)
- [ ] Implement `cors()` middleware
- [ ] Implement `helmet()` middleware (security headers)
- [ ] Implement `compression()` middleware
- [ ] Implement static file serving
- [ ] Implement cookie parsing
- [ ] Create middleware factory pattern

### Success Criteria
- ✅ JSON body parsing works automatically
- ✅ CORS headers set correctly
- ✅ Security headers added by helmet
- ✅ Response compression (gzip/brotli) works
- ✅ Static files served efficiently
- ✅ All middleware is Express-compatible

### Deliverables
- `middleware/Json.java` - JSON body parser
- `middleware/Cors.java` - CORS handler
- `middleware/Helmet.java` - Security headers
- `middleware/Compression.java` - Response compression
- `middleware/Static.java` - Static file serving
- `middleware/CookieParser.java` - Cookie parsing
- `Headers.java`, `Cookies.java` - Implementations

### Tasks
- [ ] Implement JSON serialization/deserialization (Jackson)
- [ ] Implement CORS middleware with options
- [ ] Implement helmet security headers
- [ ] Implement gzip/brotli compression
- [ ] Implement static file serving (with caching)
- [ ] Implement cookie parsing
- [ ] Create middleware test utilities
- [ ] Document middleware usage

---

## Phase 4: Plugin System

**Timeline**: Weeks 10-12 (March 2025)

### Objectives
- [ ] Design and implement ServiceRegistry
- [ ] Implement plugin lifecycle management
- [ ] Create service scoping (singleton, request, prototype)
- [ ] Implement `**Aware` interfaces pattern
- [ ] Create plugin discovery mechanism
- [ ] Build first plugin: Database

### Success Criteria
- ✅ Plugins can register services
- ✅ Services accessible via `req.service(Class)`
- ✅ Lifecycle management (startup, shutdown) works
- ✅ Request-scoped vs app-scoped services work correctly
- ✅ Database plugin demonstrates full capability

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

## Phase 5: Database Integration

**Timeline**: Weeks 13-15 (March-April 2025)

### Objectives
- [ ] Integrate JOOQ for type-safe SQL
- [ ] Implement connection pooling (HikariCP)
- [ ] Support virtual thread-aware connections
- [ ] Implement transaction management
- [ ] Create record-based query results
- [ ] Support multiple databases (named instances)

### Success Criteria
- ✅ Database queries return typed records
- ✅ Connection pool works with virtual threads
- ✅ Transactions commit/rollback correctly
- ✅ Multiple database instances work
- ✅ Zero N+1 query issues

### Deliverables
- `plugin/database/Database.java` - Database service
- `plugin/database/Transaction.java` - Transaction API
- `plugin/database/Model.java` - Base interface for records
- Connection pool integration (HikariCP)
- JOOQ code generation setup
- Migration support (Flyway)

### Tasks
- [ ] Integrate JOOQ with Gradle
- [ ] Set up HikariCP connection pooling
- [ ] Implement Database service API
- [ ] Implement transaction management
- [ ] Add record mapping (JOOQ → Java records)
- [ ] Support named database instances
- [ ] Add migration support (Flyway)
- [ ] Write database integration tests

---

## Phase 6: AI Integration (OpenAI)

**Timeline**: Weeks 16-18 (April 2025)

### Objectives
- [ ] Create AI plugin architecture
- [ ] Implement OpenAI client (chat completions)
- [ ] Support structured outputs (records)
- [ ] Implement streaming responses
- [ ] Add token counting and cost tracking
- [ ] Implement response caching

### Success Criteria
- ✅ `ai().ask(prompt)` returns completions
- ✅ `ai().extract(Record.class, prompt)` returns typed data
- ✅ Streaming responses work (`ai().stream()`)
- ✅ Token usage tracked per request
- ✅ Caching reduces duplicate calls by 90%+

### Deliverables
- `plugin/ai/AIPlugin.java` - AI plugin
- `plugin/ai/AI.java` - AI service interface
- `plugin/ai/OpenAIClient.java` - OpenAI integration
- `plugin/ai/TokenCounter.java` - Usage tracking
- `plugin/ai/AICache.java` - Response caching
- `plugin/ai/StreamingResponse.java` - Streaming support

### Tasks
- [ ] Implement OpenAI HTTP client
- [ ] Add chat completion support
- [ ] Implement structured output (function calling)
- [ ] Add streaming support (SSE)
- [ ] Implement token counting
- [ ] Add response caching (memory + optional Redis)
- [ ] Add cost tracking and budgets
- [ ] Write AI integration tests

---

## Phase 7: Vector Store & RAG

**Timeline**: Weeks 19-21 (April-May 2025)

### Objectives
- [ ] Create vector store abstraction
- [ ] Implement embedding generation
- [ ] Support multiple vector backends (Qdrant, Pinecone, embedded)
- [ ] Implement semantic search
- [ ] Create RAG pipeline
- [ ] Support document indexing

### Success Criteria
- ✅ Documents can be indexed automatically
- ✅ Semantic search returns relevant results
- ✅ `ai().rag(question)` works end-to-end
- ✅ Multiple vector store backends work
- ✅ Embedding happens automatically

### Deliverables
- `plugin/vector/VectorStorePlugin.java`
- `plugin/vector/VectorStore.java` - Interface
- `plugin/vector/QdrantClient.java` - Qdrant integration
- `plugin/vector/EmbeddedVectorStore.java` - In-memory option
- `plugin/ai/RAGService.java` - RAG implementation
- `plugin/ai/Embeddings.java` - Embedding generation

### Tasks
- [ ] Design vector store interface
- [ ] Implement Qdrant client
- [ ] Implement embedded vector store (for dev)
- [ ] Add embedding generation (OpenAI)
- [ ] Implement document chunking
- [ ] Build RAG pipeline (retrieve + generate)
- [ ] Add reranking support
- [ ] Test RAG accuracy

---

## Phase 8: Observability

**Timeline**: Weeks 22-24 (May 2025)

### Objectives
- [ ] Implement metrics collection (Prometheus)
- [ ] Add distributed tracing (OpenTelemetry)
- [ ] Implement structured logging
- [ ] Create health check endpoint
- [ ] Add request/response logging middleware
- [ ] Build observability dashboard helpers

### Success Criteria
- ✅ Metrics exported in Prometheus format
- ✅ Traces show full request path
- ✅ Logs are structured (JSON)
- ✅ Health checks work (`/health`)
- ✅ Can debug production issues easily

### Deliverables
- `plugin/observability/MetricsPlugin.java`
- `plugin/observability/TracingPlugin.java`
- `plugin/observability/LoggingPlugin.java`
- OpenTelemetry integration
- Prometheus metrics endpoint
- Structured logging (Logback + JSON)

### Tasks
- [ ] Integrate OpenTelemetry SDK
- [ ] Add Prometheus metrics exporter
- [ ] Implement request metrics (RPS, latency, errors)
- [ ] Add distributed tracing
- [ ] Implement structured logging
- [ ] Create health check endpoint
- [ ] Add log correlation IDs
- [ ] Document observability setup

---

## Phase 9: CLI Tool

**Timeline**: Weeks 25-27 (May-June 2025)

### Objectives
- [ ] Build `roya` CLI tool
- [ ] Implement `roya new <name>` (project generator)
- [ ] Implement `roya dev` (hot reload dev server)
- [ ] Implement `roya build` (production build)
- [ ] Create project templates (hello-world, rest-api, ai-chat)
- [ ] Add migration tool (Express → Roya)

### Success Criteria
- ✅ Can create new project in <30 seconds
- ✅ Hot reload works during development
- ✅ Production build optimizes for performance
- ✅ Templates demonstrate best practices
- ✅ Express migration tool handles 80%+ of code

### Deliverables
- `roya-cli/` - CLI tool module
- `roya new` - Project generator
- `roya dev` - Dev server with hot reload
- `roya build` - Production builder
- Project templates (3-5 different types)
- Express migration tool (AST-based)

### Tasks
- [ ] Build CLI framework (picocli)
- [ ] Implement project generator
- [ ] Add hot reload support (file watching)
- [ ] Create production build optimization
- [ ] Design project templates
- [ ] Build Express → Roya migration tool
- [ ] Test CLI workflows
- [ ] Write CLI documentation

---

## Phase 10: Production Hardening

**Timeline**: Weeks 28-32 (June-July 2025)

### Objectives
- [ ] GraalVM native compilation support
- [ ] Kubernetes deployment templates
- [ ] Docker optimized images
- [ ] Security audit and hardening
- [ ] Performance optimization
- [ ] Load testing and benchmarking
- [ ] Production deployment guide

### Success Criteria
- ✅ Native binary starts in <50ms
- ✅ Docker image <50MB (native)
- ✅ Passes security audit (OWASP Top 10)
- ✅ Benchmarks meet targets (50K RPS)
- ✅ Can handle 1M concurrent connections
- ✅ Production deployment guide is complete

### Deliverables
- GraalVM native-image configuration
- Kubernetes manifests (deployment, service, ingress)
- Dockerfile (JVM + native variants)
- Security audit report
- Performance benchmark suite
- Load testing scripts
- Production deployment guide

### Tasks
- [ ] Configure GraalVM native-image
- [ ] Optimize native compilation
- [ ] Create Kubernetes templates
- [ ] Build optimized Docker images
- [ ] Run security audit (OWASP ZAP, etc.)
- [ ] Perform load testing (Gatling)
- [ ] Optimize hot paths (profiling)
- [ ] Document production deployment

---

## Phase 11: Documentation & Examples

**Timeline**: Weeks 33-36 (July-August 2025)

### Objectives
- [ ] Build documentation website
- [ ] Write comprehensive guides (20+ topics)
- [ ] Create 10+ example applications
- [ ] Record video tutorials
- [ ] Write migration guides (Express, Spring Boot)
- [ ] Create API reference (Javadoc)

### Success Criteria
- ✅ Docs site is live and searchable
- ✅ Getting started guide <15 minutes
- ✅ Every feature has example code
- ✅ Video tutorials cover common use cases
- ✅ Migration guides are accurate

### Deliverables
- Documentation website (VitePress or similar)
- Getting Started guide
- Middleware guide
- Routing guide
- Database guide
- AI/RAG guide
- 10+ example applications
- 5+ video tutorials
- Express migration guide
- Spring Boot migration guide

### Tasks
- [ ] Set up docs site framework
- [ ] Write getting started guide
- [ ] Document all core features
- [ ] Create example applications
- [ ] Record video tutorials
- [ ] Write migration guides
- [ ] Generate API docs from Javadoc
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
