# Roya Framework - Backlog

**Last Updated**: January 2025

This document captures items we identify as necessary and queue for implementation. Items move from here → ROADMAP → MILESTONES → CLOSED.

---

## Template for Backlog Items

```markdown
### [Item Name]
**Category**: [Feature / Enhancement / Investigation / Documentation]
**Priority**: [P0-Critical / P1-High / P2-Medium / P3-Low]
**Estimated Effort**: [Hours or Story Points]
**Proposed For**: [Phase number or "Future"]
**Status**: [New / Under Review / Approved / In Roadmap]

**Description**:
What is this item?

**Motivation**:
Why do we need this?

**Acceptance Criteria**:
- Criterion 1
- Criterion 2

**Dependencies**:
- Depends on X
- Blocks Y

**Notes**:
Additional context
```

---

## High Priority (P0-P1)

### Phase 7: Vector & RAG Polishing
**Category**: Feature
**Priority**: P1-High
**Estimated Effort**: 24 hours
**Proposed For**: Phase 7
**Status**: New

**Description**:
Finalize Qdrant-only RAG with production-grade UX and ops.

**Acceptance Criteria**:
- Optional reranking stage for retrieved chunks (LLM/model-based)
- Collection management APIs (create/delete/list, basic stats)
- Example server adds POST /mcp/tools/docs.indexPath to index a directory
- Richer chunking presets; support .md/.markdown/.txt (extensible)
- Config defaults and precedence for collection/topK/minScore
- Observability for index/search (metrics) and simple RAG tracing
- Live Qdrant tests: index/search success, failure paths (timeouts/5xx), golden snapshots

**Notes**:
404 from Qdrant means collection missing (must index first); document this.

---

### Structured Logging Redaction/Sampling
**Category**: Enhancement  
**Priority**: P1-High  
**Proposed For**: Phase 8  
**Status**: New

**Description**:
Expand Morgan JSON mode with preset redaction profiles and optional sampling.

**Acceptance Criteria**:
- Presets: minimal, standard (default), strict redaction
- Probabilistic sampling (morgan.sampleRate)
- Configurable service/env fields

### HTTP/2 Server Push Support
**Category**: Feature  
**Priority**: P2-Medium  
**Estimated Effort**: 16 hours  
**Proposed For**: Phase 2 (Routing & Path Matching)  
**Status**: New

**Description**:
Support HTTP/2 server push for proactive resource delivery.

**Motivation**:
Modern browsers support HTTP/2 push. Could significantly improve initial page load times for web applications built with Roya.

**Acceptance Criteria**:
- res.push(path, headers) API
- Works with Helidon Níma HTTP/2 support
- Respects client hints

**Dependencies**:
- Requires HTTP/2 support in Helidon Níma

**Notes**:
Express doesn't have this - this would be a Roya advantage.

---

### WebSocket Support
**Category**: Feature  
**Priority**: P1-High  
**Estimated Effort**: 24 hours  
**Proposed For**: Phase 3 (Essential Middleware)  
**Status**: New

**Description**:
Add WebSocket support with Express-compatible API.

**Motivation**:
Real-time features (chat, notifications, live updates) require WebSocket. Express supports this via `express-ws`.

**Acceptance Criteria**:
- `app.ws(path, handler)` API
- Upgrade from HTTP to WebSocket
- Send/receive messages
- Connection lifecycle events (open, close, error)

**Dependencies**:
- Helidon Níma WebSocket support

**Notes**:
Should feel like Express: `app.ws('/chat', (ws, req) => { ws.on('message', ...) })`

---

### Session Management
**Category**: Feature  
**Priority**: P1-High  
**Estimated Effort**: 16 hours  
**Proposed For**: Phase 3 (Essential Middleware)  
**Status**: New

**Description**:
Server-side session management (memory, Redis, database backends).

**Motivation**:
Stateful applications need sessions. Express has `express-session`.

**Acceptance Criteria**:
- `req.session` API
- Multiple storage backends (memory, Redis, DB)
- Session expiration
- CSRF protection integration

**Dependencies**:
- Cookie parsing middleware

**Notes**:
Should support virtual threads (no ThreadLocal issues).

---

### File Upload Handling
**Category**: Feature  
**Priority**: P1-High  
**Estimated Effort**: 20 hours  
**Proposed For**: Phase 3 (Essential Middleware)  
**Status**: New

**Description**:
Multipart form data handling for file uploads.

**Motivation**:
File uploads are common. Express uses `multer`.

**Acceptance Criteria**:
- Parse multipart/form-data
- Stream uploads (no temp files for large files)
- `req.file` and `req.files` API
- Size limits and validation

**Dependencies**:
- Request body parsing

**Notes**:
Should leverage virtual threads for concurrent upload processing.

---

## Medium Priority (P2)

### Rate Limiting Middleware
**Category**: Feature  
**Priority**: P2-Medium  
**Estimated Effort**: 12 hours  
**Proposed For**: Phase 3 (Essential Middleware)  
**Status**: New

**Description**:
Built-in rate limiting to prevent abuse.

**Motivation**:
API protection. Express uses `express-rate-limit`.

**Acceptance Criteria**:
- Request-per-window limiting
- IP-based and custom key functions
- Storage backends (memory, Redis)
- Configurable responses (429 status)

**Dependencies**:
- None

---

### GraphQL Support
**Category**: Feature  
**Priority**: P2-Medium  
**Estimated Effort**: 32 hours  
**Proposed For**: Phase 11 (Community & Ecosystem)  
**Status**: New

**Description**:
GraphQL server support as a plugin.

**Motivation**:
GraphQL is popular alternative to REST. Should be easy to use with Roya.

**Acceptance Criteria**:
- Define schema with records
- Resolver methods
- Subscriptions (over WebSocket)
- Dataloader integration

**Dependencies**:
- WebSocket support (for subscriptions)

**Notes**:
Could use graphql-java under the hood.

---

### Server-Sent Events (SSE)
**Category**: Feature  
**Priority**: P2-Medium  
**Estimated Effort**: 8 hours  
**Proposed For**: Phase 3 (Essential Middleware)  
**Status**: New

**Description**:
Support for Server-Sent Events (one-way real-time updates).

**Motivation**:
Simpler than WebSocket for server → client streaming (AI responses, live logs, etc.).

**Acceptance Criteria**:
- `res.sse()` API
- Send events with data/id/retry
- Keep-alive support
- Client reconnection handling

**Dependencies**:
- Response streaming

**Notes**:
Perfect for AI streaming responses (`ai().stream()`).

---

### Template Engine Integration (JTE)
**Category**: Feature  
**Priority**: P2-Medium  
**Estimated Effort**: 12 hours  
**Proposed For**: Phase 3 (Essential Middleware)  
**Status**: New

**Description**:
Integrate JTE (Java Template Engine) for server-side rendering.

**Motivation**:
Not everyone builds SPAs. Server-rendered HTML is making a comeback.

**Acceptance Criteria**:
- `res.render(template, data)` API
- Type-safe templates
- Hot reload in dev mode
- Precompiled in production

**Dependencies**:
- None

**Notes**:
JTE is perfect - compile-time type checking, fast.

---

### HTMX Integration
**Category**: Feature  
**Priority**: P2-Medium  
**Estimated Effort**: 8 hours  
**Proposed For**: Phase 11 (Community & Ecosystem)  
**Status**: New

**Description**:
First-class HTMX support (helpers for hx-* attributes).

**Motivation**:
HTMX is gaining popularity for building interactive UIs without heavy JS frameworks.

**Acceptance Criteria**:
- Helper methods for hx-* responses
- Partial template rendering
- Trigger client-side events

**Dependencies**:
- Template engine

**Notes**:
Roya + JTE + HTMX = powerful full-stack Java without JS frameworks.

---

## Low Priority (P3)

### CLI Rich Templates (rest-api, ai-rag, object-storage)
**Category**: Enhancement  
**Priority**: P3-Low  
**Estimated Effort**: 12 hours  
**Proposed For**: Phase 9+  
**Status**: New

**Description**:
Add richer scaffold templates beyond minimal app.

**Acceptance Criteria**:
- `roya new --template rest-api|ai-rag|object-storage`
- Generates routes, config, and docker-compose fragments

---

### CLI Convenience Commands (db/ai/email/storage/openapi/secrets)
**Category**: Enhancement  
**Priority**: P3-Low  
**Estimated Effort**: 16 hours  
**Proposed For**: Phase 9+  
**Status**: New

**Description**:
Add small wrappers over existing plugin features.

**Acceptance Criteria**:
- `roya db migrate|generate`
- `roya ai index --collection <name> --path <dir>`
- `roya email test --provider <id>`
- `roya storage put|get|presign`
- `roya openapi serve` (serve spec and open browser)
- `roya secrets get|set` (Vault dev KV)

---

### CLI Plugin Add Helper
**Category**: Enhancement  
**Priority**: P3-Low  
**Estimated Effort**: 8 hours  
**Proposed For**: Phase 9+  
**Status**: New

**Description**:
Scaffold dependency and wiring hints for known plugins.

**Acceptance Criteria**:
- `roya plugin add <plugin-id>` updates Gradle and prints usage hints

---

### gRPC Support
**Category**: Feature  
**Priority**: P3-Low  
**Estimated Effort**: 40 hours  
**Proposed For**: Future  
**Status**: New

**Description**:
gRPC server support.

**Motivation**:
Microservice communication. More efficient than REST for service-to-service.

**Acceptance Criteria**:
- Define services with records
- Streaming support
- Reflection API

**Dependencies**:
- HTTP/2 support

---

### Kotlin DSL
**Category**: Enhancement  
**Priority**: P3-Low  
**Estimated Effort**: 16 hours  
**Proposed For**: Future  
**Status**: New

**Description**:
Kotlin-specific DSL for even more concise route definitions.

**Motivation**:
Kotlin developers deserve great DX too.

**Acceptance Criteria**:
- Kotlin extension functions
- Coroutine support
- Type-safe builders

**Dependencies**:
- Core framework stable

**Notes**:
Could be community-contributed.

---

## Email Plugin Enhancements

### Brevo (Sendinblue) Provider for Email Plugin
**Category**: Enhancement  
**Priority**: P2-Medium  
**Estimated Effort**: 4 hours  
**Proposed For**: Future  
**Status**: New

**Description**:
Add Brevo (formerly Sendinblue) email provider to the Email plugin, following the same thin wrapper pattern as SendGrid and MailerSend.

**Motivation**:
Brevo is a popular email service provider with a Java SDK. Adding it increases provider choice and demonstrates the extensibility of the Email plugin architecture.

**Acceptance Criteria**:
- BrevoProvider implementation (thin wrapper around Brevo SDK)
- System property configuration: `email.provider=brevo`, `email.brevo.apiKey`
- Integration with EmailPlugin provider selection
- Test with Brevo SDK: `com.brevo:sib-api-v3-sdk:7.0.0`
- Feature support check (analytics, tags, webhooks)

**Dependencies**:
- Brevo Java SDK: `com.brevo:sib-api-v3-sdk`

**Notes**:
Follow the same pattern as SendGridProvider and MailerSendProvider - thin wrapper, delegate to SDK, expose via provider() method.

---

### Resend Provider for Email Plugin
**Category**: Enhancement  
**Priority**: P2-Medium  
**Estimated Effort**: 4 hours  
**Proposed For**: Future  
**Status**: New

**Description**:
Add Resend email provider to the Email plugin, following the same thin wrapper pattern.

**Motivation**:
Resend is a modern email API for developers. Adding it provides another provider option with excellent developer experience.

**Acceptance Criteria**:
- ResendProvider implementation (thin wrapper around Resend SDK)
- System property configuration: `email.provider=resend`, `email.resend.apiKey`
- Integration with EmailPlugin provider selection
- Test with Resend SDK (check latest version)
- Feature support check (analytics, webhooks)

**Dependencies**:
- Resend Java SDK (check latest version on Maven Central)

**Notes**:
Follow the same pattern as other providers. Resend has excellent API design and developer experience.

---

### Postmark Provider for Email Plugin
**Category**: Enhancement  
**Priority**: P2-Medium  
**Estimated Effort**: 4 hours  
**Proposed For**: Future  
**Status**: New

**Description**:
Add Postmark email provider to the Email plugin, following the same thin wrapper pattern.

**Motivation**:
Postmark specializes in transactional emails with excellent deliverability. Adding it provides a focused transactional email option.

**Acceptance Criteria**:
- PostmarkProvider implementation (thin wrapper around Postmark SDK)
- System property configuration: `email.provider=postmark`, `email.postmark.apiKey`
- Integration with EmailPlugin provider selection
- Test with Postmark SDK: `com.postmarkapp:postmark-client:1.10.0`
- Feature support check (analytics, webhooks, templates)

**Dependencies**:
- Postmark Java SDK: `com.postmarkapp:postmark-client`

**Notes**:
Postmark is excellent for transactional emails. Follow the same thin wrapper pattern as other providers.

---

## AI Plugin Enhancements

### Function Calling for Structured Outputs
**Category**: Enhancement  
**Priority**: P2-Medium  
**Estimated Effort**: 8 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: New

**Description**:
Add function calling support (OpenAI-style) for structured outputs, in addition to JSON mode. Function calling is more reliable for complex schemas and provider-specific optimizations.

**Motivation**:
While JSON mode works well, function calling provides more reliable structured outputs, especially for nested/complex record structures. It's also provider-specific optimization (OpenAI supports this natively).

**Acceptance Criteria**:
- Function calling implementation alongside JSON mode
- Automatic function schema generation from Java records
- Provider-aware: Use function calling for OpenAI, JSON mode for others
- Fallback to JSON mode if function calling fails
- Performance comparison: function calling vs JSON mode

**Dependencies**:
- OpenAI function calling API
- Ability to generate JSON Schema from Java records

**Notes**:
This is an enhancement on top of MVP JSON mode implementation. JSON mode is simpler and works across providers, function calling is provider-specific but more reliable.

---

### Per-User and Per-Organization Budget Tracking
**Category**: Enhancement  
**Priority**: P2-Medium  
**Estimated Effort**: 16 hours  
**Proposed For**: Future  
**Status**: New

**Description**:
Add budget tracking at user and organization levels, not just per-request. Enable budget alerts and rate limiting based on AI usage costs.

**Motivation**:
Per-request tracking is MVP, but production systems need budget controls at user/org levels to prevent cost explosions.

**Acceptance Criteria**:
- Per-user budget tracking (daily/weekly/monthly limits)
- Per-organization budget tracking
- Budget alerts (configurable thresholds)
- Rate limiting based on budget remaining
- Budget reset schedules (daily/weekly/monthly)
- Integration with Auth plugin for user identification

**Dependencies**:
- Auth plugin (user identification)
- Database plugin (budget storage)
- Metrics plugin (budget metrics)

**Notes**:
MVP has per-request tracking. This enhancement adds budget management layer. Consider storage in Database plugin, metrics in Metrics plugin.

---

## Infrastructure & DevOps Enhancements

### Health Check Endpoints
**Category**: Feature  
**Priority**: P1-High  
**Estimated Effort**: 4 hours  
**Proposed For**: Phase 6 or 7  
**Status**: New

**Description**:
Add health check endpoints leveraging Helidon's native health check capabilities. Essential for Kubernetes liveness and readiness probes, deployment frameworks, and monitoring.

**Motivation**:
Modern deployment frameworks (Kubernetes, Docker Swarm, etc.) require health check endpoints to ensure services are running and ready. Helidon has native support for this - we should expose it.

**Acceptance Criteria**:
- `/health` endpoint (basic health check)
- `/health/live` endpoint (liveness probe)
- `/health/ready` endpoint (readiness probe)
- Optional: `/health/started` endpoint (startup probe)
- Integration with plugin system (plugins can register health checks)
- Configured via system properties or config file

**Dependencies**:
- Helidon health check module (likely already available)
- Plugin registration for custom health checks

**Notes**:
Helidon has `io.helidon.health` module. We should integrate this and expose standard endpoints. Plugins can register custom health checks (e.g., Database plugin checks DB connection, Cache plugin checks cache availability).

---

### Object Storage Service Plugin
**Category**: Feature  
**Priority**: P2-Medium  
**Estimated Effort**: 12 hours  
**Proposed For**: Phase 7  
**Status**: New

**Description**:
Object storage plugin for storing infrequently accessed and unstructured data. Uses MinIO (S3-compatible) via Docker container. Provides `ObjectStorage` service interface similar to `Database` and `Email`.

**Motivation**:
Many applications need to store files, images, documents, backups, etc. MinIO provides S3-compatible object storage that works great with Docker. This should feel like using Database or Email - just another service.

**Proposed API**:
```java
ObjectStorage storage = req.get(ObjectStorage.class);

// Upload
String objectId = storage.put("bucket-name", "path/to/file", fileBytes);
storage.put("bucket-name", "path/to/file", inputStream);

// Download
Optional<byte[]> data = storage.get("bucket-name", "path/to/file");
storage.get("bucket-name", "path/to/file", outputStream);

// Metadata
Optional<ObjectMetadata> meta = storage.metadata("bucket-name", "path/to/file");

// List
List<ObjectInfo> objects = storage.list("bucket-name", "prefix/");

// Delete
storage.delete("bucket-name", "path/to/file");
```

**Acceptance Criteria**:
- `ObjectStorage` interface (consistent with Database/Email pattern)
- MinIO client integration (aws-java-sdk-s3 or MinIO Java SDK)
- Docker Compose setup for local development
- Bucket management (create, delete, list)
- File operations (put, get, delete, list, metadata)
- Streaming support for large files
- Optional: Presigned URLs for temporary access
- Configuration via system properties (endpoint, access key, secret key)

**Dependencies**:
- MinIO Docker container (for local development)
- MinIO Java SDK or AWS S3 SDK
- Optional: Docker Compose integration

**Notes**:
MinIO is S3-compatible and perfect for local development. Production deployments can use MinIO, AWS S3, or any S3-compatible storage. Follow same thin wrapper pattern as Email plugin.

---

### Configuration Variables and Secrets Service
**Category**: Feature  
**Priority**: P2-Medium  
**Estimated Effort**: 16 hours  
**Proposed For**: Phase 7  
**Status**: New

**Description**:
Configuration and secrets management service similar to Kubernetes ConfigMap and Secrets. Provides centralized, secure storage for application configuration and sensitive data.

**Motivation**:
Modern applications need centralized configuration and secrets management. Kubernetes has ConfigMap and Secrets, but for local development and non-k8s deployments, we need an alternative. HashiCorp Vault is industry standard for secrets management.

**Proposed Solution**:
- **Secrets**: HashiCorp Vault (via Docker container)
- **Config**: Could use Vault's KV store, or a simple file-based approach for development
- **API**: `Config` and `Secrets` services

**Proposed API**:
```java
// Config (non-sensitive)
Config config = req.get(Config.class);
String dbHost = config.get("database.host", "localhost");
Integer port = config.getInt("server.port", 3000);

// Secrets (sensitive)
Secrets secrets = req.get(Secrets.class);
String apiKey = secrets.get("sendgrid.apiKey"); // Throws if not found
Optional<String> token = secrets.getOptional("stripe.token");
```

**Alternative Approaches**:
1. **Vault only**: Use Vault for both config and secrets (KV v2 for config, secret mounts for secrets)
2. **Vault + Consul**: Vault for secrets, Consul for config
3. **Simple fallback**: Vault for production, file-based for development

**Recommendation**: Vault only (simpler, industry standard, supports both config and secrets)

**Acceptance Criteria**:
- `Config` service interface (get, getInt, getBoolean, etc.)
- `Secrets` service interface (get, getOptional, etc.)
- Vault integration (HashiCorp Vault Java client)
- Docker Compose setup for local Vault instance
- Environment variable fallback (for development)
- Vault authentication (token, app role, etc.)
- Support for Vault KV v2 engine
- Configuration via system properties (vault endpoint, auth method, etc.)
- Graceful degradation (fallback to env vars or files if Vault unavailable)

**Dependencies**:
- HashiCorp Vault Docker container
- Vault Java client library
- Optional: Spring Cloud Vault (if compatible) or native Vault client

**Notes**:
Vault is the industry standard, but may be overkill for simple apps. Consider a "simple" mode for development that reads from environment variables or files. For production, Vault provides audit trails, secret rotation, etc.

---

## API Documentation

### OpenAPI/Swagger Integration
**Category**: Feature  
**Priority**: P2-Medium  
**Estimated Effort**: 8-12 hours  
**Proposed For**: Phase 6 or 7  
**Status**: Investigation

**Description**:
Automatic API documentation generation using OpenAPI/Swagger. Leverage Helidon's native OpenAPI capabilities to generate documentation from route definitions.

**Motivation**:
API documentation is essential for developers. Manual documentation is error-prone and gets out of sync. Automatic generation from code is ideal.

**Investigation Questions**:
1. Does Helidon have built-in OpenAPI support?
2. Can we generate OpenAPI from Roya route definitions?
3. Should we use annotations (like JAX-RS) or infer from handlers?
4. Can we integrate with Swagger UI for interactive docs?

**Proposed Approach**:
- **Option A**: Leverage Helidon's OpenAPI module (if available)
  - Generate OpenAPI spec from route definitions
  - Serve `/openapi.json` endpoint
  - Optional: Integrate Swagger UI at `/swagger-ui`
  
- **Option B**: Manual OpenAPI spec
  - Hand-crafted OpenAPI YAML/JSON
  - Keep in sync with routes manually
  - Less ideal but simpler to implement

- **Option C**: Annotations-based
  - Add annotations to route handlers
  - Generate OpenAPI from annotations
  - Similar to Spring Boot or JAX-RS approach

**Recommendation**: Option A if Helidon supports it, otherwise Option C (annotations). We should investigate Helidon's OpenAPI capabilities first.

**Acceptance Criteria**:
- OpenAPI spec generation (JSON/YAML)
- `/openapi.json` or `/openapi.yaml` endpoint
- Optional: Swagger UI integration at `/swagger-ui`
- Support for route parameters, request bodies, responses
- Documentation of middleware/handlers
- Optional: Example values and descriptions

**Dependencies**:
- Helidon OpenAPI module (if available)
- Or: OpenAPI generator library
- Optional: Swagger UI for interactive documentation

**Notes**:
This requires investigation into Helidon's capabilities. If Helidon has native support, we should leverage it. Otherwise, we may need to build our own route introspection system.

---

## Investigation Items

### True FFM Direct Mapping for Cache Plugin
**Category**: Enhancement  
**Priority**: P2-Medium  
**Estimated Effort**: 24 hours  
**Proposed For**: Future  
**Status**: New

**Description**:
True FFM Direct Mapping - Currently using `MemorySegment.ofBuffer(MappedByteBuffer)`, which still has MappedByteBuffer's 2GB limit per segment. True FFM direct mapping (without 2GB limit) would require a different API or multiple segments.

**Motivation**:
Allow cache files to exceed 2GB per segment, leveraging FFM's full capabilities without the MappedByteBuffer limitation.

**Acceptance Criteria**:
- Cache files can exceed 2GB per segment
- Use true FFM direct file mapping API (when available) or multi-segment approach
- Maintain backward compatibility with existing cache files
- Performance benchmarks show no degradation

**Dependencies**:
- FFM direct file mapping API (may require newer Java version)
- Or implementation of multi-segment file mapping

**Notes**:
Current implementation wraps MappedByteBuffer, which has hard 2GB limit. True FFM MemorySegment doesn't have this limitation but requires different mapping approach.

---

### FFM-Based HTTP Parser
**Category**: Investigation  
**Priority**: P2-Medium  
**Estimated Effort**: 24 hours (spike)  
**Proposed For**: Phase 10 (Production Hardening)  
**Status**: New

**Description**:
Investigate replacing Helidon Níma's HTTP parser with custom FFM-based parser.

**Motivation**:
Potential 20-30% performance improvement with zero-copy parsing.

**Acceptance Criteria**:
- Spike: benchmark FFM parser vs Helidon default
- If >15% improvement, implement
- If <15% improvement, document and close

**Dependencies**:
- FFM API stable (Java 21+)

**Notes**:
Only worth it if measurable performance gain.

---

### Native Compilation Optimizations
**Category**: Investigation  
**Priority**: P2-Medium  
**Estimated Effort**: 16 hours  
**Proposed For**: Phase 10 (Production Hardening)  
**Status**: New

**Description**:
Investigate GraalVM native image optimizations specific to Roya.

**Motivation**:
Could achieve <50ms cold start and <30MB binary.

**Acceptance Criteria**:
- Profile native image build
- Identify optimization opportunities
- Document findings

**Dependencies**:
- Core framework complete

---

## Documentation Items

### Express Migration Guide
**Category**: Documentation  
**Priority**: P1-High  
**Estimated Effort**: 16 hours  
**Proposed For**: Phase 11 (Documentation & Examples)  
**Status**: New

**Description**:
Comprehensive guide for migrating Express.js apps to Roya.

**Motivation**:
Primary target audience is Express developers.

**Acceptance Criteria**:
- Side-by-side code comparisons
- Migration checklist
- Common patterns translation
- Gotchas and differences documented

**Dependencies**:
- Core framework complete

---

### Video Tutorial Series
**Category**: Documentation  
**Priority**: P2-Medium  
**Estimated Effort**: 40 hours  
**Proposed For**: Phase 11 (Documentation & Examples)  
**Status**: New

**Description**:
YouTube series covering Roya fundamentals.

**Motivation**:
Video content reaches wider audience.

**Acceptance Criteria**:
- "Hello World" tutorial (5 min)
- "Building a REST API" (15 min)
- "AI Integration" (20 min)
- "Database & ORM" (20 min)
- "Deployment" (15 min)

**Dependencies**:
- Core features complete

---

## Moved to Roadmap

*Items that have been promoted to the roadmap will be listed here with links*

---

## Rejected / Closed

*Items we decided not to pursue will be documented here with reasoning*

---

**This backlog is continuously updated. Items are prioritized and scheduled into the roadmap as capacity allows.**
