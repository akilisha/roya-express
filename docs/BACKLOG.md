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

## Documentation & Marketing (Phase 11)

### Phase 11: Documentation & Examples - Marketing & Showcasing Plan
**Category**: Documentation / Marketing  
**Priority**: P1-High  
**Estimated Effort**: 120+ hours  
**Proposed For**: Phase 11  
**Status**: Ready to Begin

**Description**:
Build comprehensive marketing materials, showcase examples, and documentation to bring awareness to Roya Framework. Focus on demonstrating unique value proposition and real-world capabilities.

**See**: `docs/PHASE11_MARKETING_PLAN.md` for complete details.

**Key Deliverables**:
- Interactive demo website with live code playground
- Video tutorial series (5 videos)
- Comprehensive documentation website
- Showcase project: "Roya Playground"
- Marketing materials package (blog posts, one-pager, social assets)
- Developer advocacy program
- Migration tools & guides

**Motivation**:
- We've built something impressive (12 LangChain4j tutorials, 4 real-world examples, complete AI workflow system)
- Need to showcase this to gain traction, attract contributors, and demonstrate production readiness
- Marketing materials will help developers understand why Roya is different and valuable

**Target Audiences**:
1. Express.js Developers - "Express for Java" messaging
2. Java Developers - Modern Java features, performance, AI-native
3. AI/ML Practitioners - Built-in RAG, agents, workflows
4. Enterprise Decision Makers - Cost savings, scalability, production-ready

**Success Metrics**:
- Short-term (3 months): 1,000+ GitHub stars, 10,000+ doc page views, 5,000+ video views
- Long-term (6 months): 5,000+ GitHub stars, 100,000+ page views, 50,000+ video views

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

### RAG Observability & Metrics (Proper Implementation)
**Category**: Enhancement
**Priority**: P2-Medium
**Estimated Effort**: 16 hours
**Proposed For**: Post-Foundation
**Status**: Backlog

**Description**:
Implement proper observability and metrics for RAG operations. Current implementation in `RAGMetrics.java` is a placeholder (simple in-memory counters).

**Motivation**:
Placeholder implementation exists but is not production-ready. Need proper metrics infrastructure.

**Acceptance Criteria**:
- Use proper metrics library (Micrometer/Prometheus integration)
- Integrate with observability infrastructure
- Follow production-grade patterns
- Track RAG query metrics (count, duration, success/failure)
- Track vector index operation metrics
- Track collection management metrics
- Expose metrics via `/metrics` endpoint or observability platform
- Support RAG tracing for debugging

**Dependencies**:
- Metrics plugin/infrastructure
- Observability platform (Prometheus/Grafana or equivalent)

**Notes**:
Current placeholder implementation in `roya-plugins/ai/src/main/java/com/akilisha/oss/roya/plugins/ai/rag/RAGMetrics.java` marked as placeholder. Proper implementation should replace this.

---

### RAG Configuration System (Proper Implementation)
**Category**: Enhancement
**Priority**: P2-Medium
**Estimated Effort**: 12 hours
**Proposed For**: Post-Foundation
**Status**: Backlog

**Description**:
Implement proper configuration system for RAG options. Current implementation in `RAGOptions.Builder` is a placeholder using System.getenv() and System.getProperty().

**Motivation**:
Placeholder implementation exists but is not production-ready. Need proper configuration library/pattern.

**Acceptance Criteria**:
- Use dedicated configuration library/pattern (e.g., Config4j, Typesafe Config, or framework config)
- Support multiple configuration sources (files, environment, system properties)
- Proper precedence handling
- Configuration validation
- Hot-reload support (optional)
- Type-safe configuration access
- Documentation for configuration options

**Dependencies**:
- Configuration library/infrastructure
- Framework configuration system

**Notes**:
Current placeholder implementation in `roya-plugins/ai/src/main/java/com/akilisha/oss/roya/plugins/ai/RAGOptions.java` marked as placeholder. Proper implementation should use a dedicated configuration system.

---

### AI Cost Calculation System (Configurable Pricing)
**Category**: Enhancement
**Priority**: P2-Medium
**Estimated Effort**: 8 hours
**Proposed For**: Post-Foundation
**Status**: Backlog

**Description**:
Make AI cost calculation configurable and support all models/pricing tiers. Current implementation in `LangChainAdapter.calculateCost()` has hardcoded pricing for a limited set of models (OpenAI GPT-3.5/GPT-4, Anthropic Claude, Mistral).

**Motivation**:
- Pricing changes frequently and varies by model/region
- Current hardcoded values become outdated quickly
- Need to support all providers/models (Gemini, Cohere, local models, etc.)
- Users may have custom pricing tiers or enterprise agreements
- Need accurate cost tracking for budget management

**Acceptance Criteria**:
- Configurable pricing via configuration file/database
- Support for all major providers and models
- Pricing tiers (standard, enterprise, custom)
- Per-model pricing configuration
- Per-region pricing (if applicable)
- Default fallback pricing when model not configured
- Documentation for pricing configuration
- Hot-reload support for pricing updates (optional)

**Dependencies**:
- Configuration system (see "RAG Configuration System" backlog item)
- Database or configuration file storage

**Notes**:
Current implementation in `roya-plugins/ai/src/main/java/com/akilisha/oss/roya/plugins/ai/langchain/LangChainAdapter.java` method `calculateCost()` has TODO comment: "Make this configurable and support all models/pricing tiers."

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

### Benchmark Harness Automation (One-Command Bench)
**Category**: Enhancement  
**Priority**: P1-High  
**Estimated Effort**: 6 hours  
**Proposed For**: Phase 10 (Production Hardening)  
**Status**: New

**Description**:
Automate capstone benchmark runs into a single command (PowerShell/Gradle), producing k6 summaries and steady-state CPU/RAM, and emitting a Markdown report.

**Acceptance Criteria**:
- Script runs Roya and Spring tests separately using a shared k6 script
- Samples CPU/RAM during steady-state and prints averages
- Saves k6 JSON summaries and renders a concise Markdown comparison
- Optional flags: VUs, duration, target base URL(s)

**Notes**:
Current `bench.ps1` exists; wire into Gradle and add Markdown output.

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
**Status**: ✅ COMPLETE (October 2025)

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

### k6 Script Parametrization & Warmup
**Category**: Enhancement  
**Priority**: P2-Medium  
**Estimated Effort**: 4 hours  
**Proposed For**: Phase 10  
**Status**: New

**Description**:
Parameterize k6 scripts (BASE_URL, PATH, VUS, DURATION), add warmup and steady-state phases, and optional CSV/JSON export.

**Acceptance Criteria**:
- Single k6 file supporting env-based targets
- Warmup stage (e.g., 20s) before measurement
- Summary export to JSON/CSV for post-processing

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

### Containerized Benchmark Compose
**Category**: Enhancement  
**Priority**: P3-Low  
**Estimated Effort**: 8 hours  
**Proposed For**: Future  
**Status**: New

**Description**:
Docker Compose to run both apps, k6, and optional Zipkin/Prometheus for apples-to-apples environment.

**Acceptance Criteria**:
- Compose file spins up Roya, Spring, k6
- Optional Zipkin/Prometheus services
- One command to run and a single report artifact

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

### Token Usage Extraction in LangChainAdapter
**Category**: Bug Fix / Feature Completion  
**Priority**: P0-Critical  
**Estimated Effort**: 4 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: ✅ COMPLETE (January 30, 2025)

**Description**:
Currently, `askWithMetadata()` and `extractWithMetadata()` methods in `LangChainAdapter` return zero values for token usage. Need to extract actual token usage from LangChain4j's `Result<T>` wrapper.

**Location**: `LangChainAdapter.java:765, 772`

**Motivation**:
Metadata methods are returning incorrect token counts (all zeros), which affects cost tracking accuracy. This is a critical bug that needs immediate fixing.

**Acceptance Criteria**:
- Wrap `LLMService.ask()` calls to use `Result<String>` instead of `String`
- Extract `TokenUsage` from `Result` and populate `AIResponse` with actual values
- Same for `extractWithMetadata()` method
- Verify token usage matches actual API calls

**Dependencies**:
- LangChain4j `Result<T>` wrapper support
- `TokenUsage` extraction from AI Services responses

**Notes**:
LangChain4j AI Services support `Result<T>` wrapper which provides `tokenUsage()`. Need to change return types from `String` to `Result<String>` and extract metadata.

---

### Streaming Support in LLMService
**Category**: Feature Completion  
**Priority**: P0-Critical  
**Estimated Effort**: 6 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: ✅ COMPLETE (January 30, 2025)

**Description**:
Add streaming support to `LLMService` interface using LangChain4j's `TokenStream` return type. Currently streaming uses low-level `StreamingChatModel` API instead of AI Services pattern.

**Location**: `LLMService.java:58`

**Motivation**:
Streaming currently uses low-level API. Should use AI Services for consistency and to leverage LangChain4j's built-in streaming capabilities.

**Acceptance Criteria**:
- Add `TokenStream` return type method to `LLMService`
- Example signature: `TokenStream stream(@V("systemPrompt") String systemPrompt, @V("userMessage") String userMessage)`
- Update `LangChainAdapter.stream()` to use AI Services instead of direct `StreamingChatModel` calls
- Handle `TokenStream` callbacks (onPartialResponse, onComplete, onError)
- Maintain backward compatibility with existing `stream()` API

**Dependencies**:
- LangChain4j `TokenStream` support
- AI Services streaming pattern

**Notes**:
LangChain4j AI Services support `TokenStream` return type. Need to add streaming methods to `LLMService` interface and update adapter implementation.

---

### Metadata Support in LLMService
**Category**: Feature Completion  
**Priority**: P0-Critical  
**Estimated Effort**: 4 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: ✅ COMPLETE (January 30, 2025)

**Description**:
Add metadata support to `LLMService` interface using LangChain4j's `Result<T>` wrapper to get token usage, finish reason, sources, and other metadata.

**Location**: `LLMService.java:61`

**Motivation**:
Complete metadata support for AI operations. Currently metadata methods exist but don't extract actual values from LangChain4j responses.

**Acceptance Criteria**:
- Add `Result<T>` return type methods to `LLMService`
- Example signature: `Result<String> askWithMetadata(@V("systemPrompt") String systemPrompt, @V("userMessage") String userMessage)`
- Extract `TokenUsage`, `FinishReason`, `Sources` from `Result`
- Update `LangChainAdapter` to use these methods
- Ensure `AIResponse` is populated with actual metadata values

**Dependencies**:
- Related to Token Usage Extraction (can be done together)
- LangChain4j `Result<T>` wrapper support

**Notes**:
This completes the metadata feature. Can be implemented alongside Token Usage Extraction as they're related.

---

### MCP (Model Context Protocol) Client Integration ⭐ MUST HAVE - USER PRIORITY
**Category**: Feature  
**Priority**: P0-Critical (100% must-have)  
**Estimated Effort**: 20 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: ✅ COMPLETE (January 30, 2025)

**Description**:
Implement MCP (Model Context Protocol) client integration with built-in caching. MCPs provide discoverable tools hosted in repositories, enabling AI agents to discover and leverage "superpowers" automatically.

**Motivation**:
User explicitly stated: "100% must have. It would be such a disservice and disingenuous not to have mcp." MCP is essential for AI agents to discover and use external capabilities.

**Acceptance Criteria**:
- `MCPClient` class with built-in caching (configurable TTL)
- Multiple server registration support
- Health checks and monitoring for MCP servers
- Automatic tool discovery from registered servers
- Convert MCP tool definitions to LangChain4j `ToolSpecification`
- `MCPClient.builder()` API for flexible configuration
- `.discoverTools()` and `.discoverAllTools()` methods
- `.mcp()` convenience method on `AIWorkflowBuilder`
- Error handling and retries
- Documentation with examples

**Proposed API**:
```java
// MCP Client with built-in caching
MCPClient mcpClient = MCPClient.builder()
    .server("mcp-server-1", "https://mcp-server.example.com")
    .server("mcp-server-2", "https://another-mcp.example.com")
    .cacheDuration(Duration.ofMinutes(10))
    .healthCheckInterval(Duration.ofMinutes(5))
    .build();

// Use in workflow
Workflow workflow = ai.workflow("mcp-agent")
    .mcp("agent", mcpClient, builder -> builder
        .systemPrompt("You have access to MCP tools")
    )
    .build();
```

**Dependencies**:
- MCP protocol specification and implementation
- HTTP/WebSocket client for MCP communication
- Tool discovery and conversion logic

**Notes**:
User prefers Option A (MCP Client) but with built-in caching (combining benefits of Option A and C). The MCPClient should abstract away caching complexity automatically.

---

### LangChain4j Demo Recreation Project
**Category**: Example/Showcase
**Priority**: P2-Medium
**Estimated Effort**: 80 hours
**Proposed For**: Post-Foundation (After MCP & Convenience Methods Complete)
**Status**: Planned

**Description**:
Recreate Julien Dubois's comprehensive LangChain4j demo suite using Roya AI framework. This will serve as a powerful showcase and reference implementation.

**Reference**: [jdubois-langchain4j-demo](https://github.com/jdubois/jdubois-langchain4j-demo/tree/main)

**Motivation**:
- Showcase Roya's workflow-first approach vs. raw LangChain4j
- Provide real-world examples for developers
- Demonstrate advanced patterns (nested/continuation, MCP, resilience)
- Create production-ready reference implementations

**Acceptance Criteria**:
- All original demos recreated (image generation, text generation, chat, vectors, RAG, tools, structured outputs, agents)
- MCP integration demo (Roya-specific)
- Advanced workflow patterns demo (nested/continuation/loop/circuit/approval/costing)
- Web UI for demo selection and execution
- Multiple configuration profiles (Azure, local, GitHub Models)
- Complete documentation with comparison to original

**Dependencies**:
- Requires complete AI workflow foundation ✅
- Requires MCP client integration ⏳
- Requires vector & RAG polishing ⏳
- Requires all convenience methods (`.loop()`, `.circuit()`, `.approval()`, `.costing()`, `.mcp()`) ⏳

**Notes**:
See `docs/FUTURE_PROJECTS.md` for detailed implementation plan. This is a showcase project that will demonstrate Roya's unique advantages over raw LangChain4j.

---

### Audio & Video Capabilities Integration ⭐ USER REQUESTED
**Category**: Feature  
**Priority**: P1-High  
**Estimated Effort**: 16 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: ✅ COMPLETE (January 30, 2025)

**Description**:
Add multimodal support (audio, video, images) to the AI plugin. LangChain4j supports multimodal content via `Content` interface: `TextContent`, `ImageContent`, `AudioContent`, `VideoContent`, `PdfFileContent`.

**Motivation**:
User specifically requested audio and visual capabilities integration. This enables image analysis, audio transcription, video description, and PDF processing within workflows.

**Acceptance Criteria**:
- Add multimodal support to `LLMService`:
  - `String askWithContent(@V("systemPrompt") String systemPrompt, @V("userMessage") String userMessage, Content... contents)`
- Add convenience methods to `AI` interface:
  - `Vision vision()` method returning `Vision` interface
  - `Vision` interface with methods: `analyzeImage(String imageUrl, String prompt)`, `transcribeAudio(String audioUrl)`, `describeVideo(String videoUrl)`
- Create `VisionNode` workflow node for multimodal operations
- Update `AIWorkflowBuilder` with `.vision()` method
- Support image URLs, base64, and file paths
- Support audio/video URLs and file paths
- Example usage: `ai.vision().analyzeImage("https://example.com/image.jpg", "What's in this image?")`

**Dependencies**:
- LangChain4j multimodal support (`Content` interface)
- Provider support for multimodal models (OpenAI GPT-4 Vision, Whisper, etc.)

**Notes**:
LangChain4j supports `UserMessage.from(TextContent.from("..."), ImageContent.from("..."))`. Need to wrap this in a clean API and integrate with workflow system.

---

### WebhookTrigger Implementation
**Category**: Feature  
**Priority**: P1-High  
**Estimated Effort**: 8 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: New

**Description**:
Implement full functionality for `WebhookTrigger` to enable HTTP-triggered workflows. Currently placeholder implementation exists.

**Location**: `WebhookTrigger.java`

**Motivation**:
WebhookTrigger is foundational for automated workflows. Enables external systems to trigger AI workflows via HTTP requests.

**Acceptance Criteria**:
- Integrate with Roya's HTTP routing to auto-register webhook endpoints
- Support webhook signature verification (HMAC, JWT, etc.)
- Support different HTTP methods (GET, POST, PUT, etc.)
- Support webhook path configuration
- Parse webhook payloads (JSON, form-data, etc.)
- Trigger workflow execution on webhook receipt
- Pass webhook data (headers, body, query params) to workflow context

**Dependencies**:
- Roya HTTP routing system
- Webhook signature verification libraries

**Notes**:
Placeholder implementation exists at `roya-plugins/ai/src/main/java/com/akilisha/oss/roya/plugins/ai/nodes/triggers/WebhookTrigger.java`. Detailed TODOs in source code comments.

---

### CronJobTrigger Implementation
**Category**: Feature  
**Priority**: P1-High  
**Estimated Effort**: 10 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: New

**Description**:
Implement full functionality for `CronJobTrigger` to enable time-based workflow triggers. Currently placeholder implementation exists.

**Location**: `CronJobTrigger.java`

**Motivation**:
CronJobTrigger is foundational for scheduled workflows. Enables workflows to run on schedules (daily reports, periodic indexing, etc.).

**Acceptance Criteria**:
- Integrate with scheduling library (Quartz recommended)
- Parse and validate cron expressions (5-field or 6-field with seconds)
- Register scheduled tasks with the workflow executor
- Support timezone configuration
- Support one-time execution vs recurring schedules
- Handle task registration errors gracefully
- Support task cancellation and rescheduling

**Dependencies**:
- Scheduling library (Quartz or similar)
- Cron expression parser

**Notes**:
Placeholder implementation exists at `roya-plugins/ai/src/main/java/com/akilisha/oss/roya/plugins/ai/nodes/triggers/CronJobTrigger.java`. Detailed TODOs in source code comments.

---

### FileWatchTrigger Implementation
**Category**: Feature  
**Priority**: P2-Medium  
**Estimated Effort**: 8 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: New

**Description**:
Implement full functionality for `FileWatchTrigger` to enable file system event-triggered workflows.

**Location**: `FileWatchTrigger.java`

**Motivation**:
Enables workflows to trigger when files are created, modified, or deleted. Useful for document processing, backup workflows, etc.

**Acceptance Criteria**:
- Integrate with Java NIO WatchService
- Support recursive directory watching
- Support file filters (by extension, pattern, etc.)
- Support event types (CREATE, MODIFY, DELETE)
- Handle file locking and partial writes
- Support multiple watch directories
- Pass file path and event type to workflow context

**Dependencies**:
- Java NIO WatchService (built-in)
- Optional: Apache Commons VFS for advanced watching

**Notes**:
Placeholder implementation exists with detailed TODOs in source code comments.

---

### PollingTrigger Implementation
**Category**: Feature  
**Priority**: P2-Medium  
**Estimated Effort**: 8 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: New

**Description**:
Implement full functionality for `PollingTrigger` to enable polling-based workflow triggers.

**Location**: `PollingTrigger.java`

**Motivation**:
Enables workflows to trigger based on polling conditions (database changes, API responses, etc.).

**Acceptance Criteria**:
- Implement polling mechanism with ScheduledExecutorService
- Support custom polling interval
- Support conditional polling (only trigger if condition is true)
- Support backoff strategies when condition is false
- Support max polling attempts or infinite polling
- Support polling data source (database query, API call, etc.)
- Pass polling results to workflow context

**Dependencies**:
- Java ScheduledExecutorService (built-in)
- Optional: Database or HTTP client for polling sources

**Notes**:
Placeholder implementation exists with detailed TODOs in source code comments.

---

### SubscriptionTrigger Implementation
**Category**: Feature  
**Priority**: P2-Medium  
**Estimated Effort**: 10 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: New

**Description**:
Implement full functionality for `SubscriptionTrigger` to enable WebSocket/SSE-triggered workflows.

**Location**: `SubscriptionTrigger.java`

**Motivation**:
Enables workflows to trigger on real-time events via WebSocket or Server-Sent Events.

**Acceptance Criteria**:
- Integrate with Roya's WebSocket/SSE support
- Support WebSocket connection events (connect, disconnect, message)
- Support SSE event streams
- Support subscription filters/topics
- Handle connection lifecycle (reconnect, heartbeat, etc.)
- Support multiple subscription types (pub/sub, topics, etc.)
- Pass subscription events to workflow context

**Dependencies**:
- Roya WebSocket/SSE infrastructure
- Event subscription system

**Notes**:
Placeholder implementation exists with detailed TODOs in source code comments.

---

### ChatTrigger Implementation
**Category**: Feature  
**Priority**: P2-Medium  
**Estimated Effort**: 12 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: New

**Description**:
Implement full functionality for `ChatTrigger` to enable chat platform-triggered workflows (Slack, Discord, Teams, etc.).

**Location**: `ChatTrigger.java`

**Motivation**:
Enables workflows to trigger on chat platform events (messages, mentions, user events, etc.).

**Acceptance Criteria**:
- Integrate with chat platforms (Slack, Discord, Teams, etc.)
- Support message events (new message, reply, mention)
- Support user events (user joined, left)
- Support channel/room filtering
- Handle platform-specific authentication
- Support webhook-style integration vs polling
- Pass chat events to workflow context

**Dependencies**:
- External SDKs for chat platforms (Slack SDK, Discord API, Teams SDK)
- Authentication/authorization system

**Notes**:
Placeholder implementation exists with detailed TODOs in source code comments.

---

### EmailTrigger Implementation
**Category**: Feature  
**Priority**: P2-Medium  
**Estimated Effort**: 10 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: New

**Description**:
Implement full functionality for `EmailTrigger` to enable email-triggered workflows.

**Location**: `EmailTrigger.java`

**Motivation**:
Enables workflows to trigger on email events (new emails, replies, attachments, etc.).

**Acceptance Criteria**:
- Integrate with email plugins (IMAP, POP3, Exchange, etc.)
- Support email filtering (sender, subject, attachments, etc.)
- Support email parsing (body, attachments, headers)
- Handle email authentication/authorization
- Support multiple email providers
- Support polling vs push (webhook) email delivery
- Pass email content to workflow context

**Dependencies**:
- Email plugin (IMAP/POP3 support)
- JavaMail API or similar

**Notes**:
Placeholder implementation exists with detailed TODOs in source code comments.

---

### AppEventTrigger Implementation
**Category**: Feature  
**Priority**: P2-Medium  
**Estimated Effort**: 8 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: New

**Description**:
Implement full functionality for `AppEventTrigger` to enable application event-triggered workflows.

**Location**: `AppEventTrigger.java`

**Motivation**:
Enables workflows to trigger on internal application events (user registration, order placed, etc.).

**Acceptance Criteria**:
- Integrate with application event bus/event system
- Support event filtering by type, source, etc.
- Support event subscription/registration
- Handle event payload passing
- Support async event processing
- Integrate with Roya's event system if one exists
- Pass event data to workflow context

**Dependencies**:
- Application event bus system
- Event filtering/subscription mechanism

**Notes**:
Placeholder implementation exists with detailed TODOs in source code comments.

---

### WorkflowTrigger Implementation
**Category**: Feature  
**Priority**: P2-Medium  
**Estimated Effort**: 10 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: New

**Description**:
Implement full functionality for `WorkflowTrigger` to enable workflow-triggered workflows (workflow chaining).

**Location**: `WorkflowTrigger.java`

**Motivation**:
Enables workflows to trigger other workflows, enabling complex orchestration and workflow chaining.

**Acceptance Criteria**:
- Implement workflow execution tracking
- Support workflow completion event subscription
- Support workflow result filtering (only trigger on success/failure)
- Support workflow context/data passing between workflows
- Handle workflow dependencies and orchestration
- Support workflow result aggregation

**Dependencies**:
- Workflow execution tracking system
- Workflow context/data passing mechanism

**Notes**:
Placeholder implementation exists with detailed TODOs in source code comments.

---

### Streaming Model Access in AI Interface
**Category**: Enhancement  
**Priority**: P1-High  
**Estimated Effort**: 2 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: New

**Description**:
Currently `StreamingLLMNode` needs to access underlying adapter to get streaming model. Should expose proper API or complete streaming via AI Services.

**Location**: `StreamingLLMNode.java:126`

**Motivation**:
Cleaner API for streaming operations. Currently uses hacky workaround to access adapter.

**Acceptance Criteria**:
- Option A: Add `StreamingChatModel streamingModel()` method to `AI` interface
- Option B: Complete streaming via AI Services (preferred - see Streaming Support in LLMService)
- Update `StreamingLLMNode` to use proper API
- Remove hacky workaround

**Dependencies**:
- Related to Streaming Support in LLMService (TODO #2)

**Notes**:
This TODO may be resolved by completing Streaming Support in LLMService. Check if AI Services streaming eliminates the need for direct model access.

---

### LangGraph Integration Completion
**Category**: Feature Completion  
**Priority**: P2-Medium  
**Estimated Effort**: 20 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: New

**Description**:
Complete LangGraph adapter implementation with vector operations, RAG, agents, and token usage extraction.

**Location**: `LangGraphAdapter.java`

**Current TODOs**:
- Implement vector operations
- Implement RAG using adaptive-rag module
- Implement LangGraph agent creation
- Replace with AgentExecutor or StateGraph-based agent
- Extract trace/step information from LangGraph execution
- Extract token usage from response

**Acceptance Criteria**:
- Complete LangGraph adapter implementation
- Integrate LangGraph's adaptive RAG
- Implement stateful workflow support
- Add agent orchestration via LangGraph
- Extract execution traces and metrics
- Token usage extraction from LangGraph responses

**Dependencies**:
- LangGraph4j library
- Adaptive RAG module

**Notes**:
Current implementation has placeholder methods. Need to complete full LangGraph integration.

---

### Google ADK Integration Completion
**Category**: Feature Implementation  
**Priority**: P2-Medium  
**Estimated Effort**: 32 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: New

**Description**:
Complete Google ADK (Agent Development Kit) integration for multi-agent orchestration.

**Location**: `GoogleADKAdapter.java`, `GoogleADKLibrary.java`

**Current TODOs**:
- Implement Google ADK integration
- Implement full Google ADK integration

**Acceptance Criteria**:
- Research Google ADK (Agent Development Kit) APIs
- Implement `GoogleADKAdapter` similar to `LangChainAdapter`
- Create `GoogleADKService` interface
- Integrate with workflow system
- Add examples and documentation
- Support multi-agent orchestration

**Dependencies**:
- Google ADK Java SDK
- Documentation and examples

**Notes**:
Placeholder implementations exist. Need to research Google ADK APIs and complete implementation.

---

### UnifiedAIService Library Selection Logic
**Category**: Enhancement  
**Priority**: P2-Medium  
**Estimated Effort**: 4 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: New

**Description**:
Add intelligent library selection logic to choose between LangChain, LangGraph, and Google ADK based on agent configuration complexity.

**Location**: `UnifiedAIService.java:122`

**Current State**:
```java
// TODO: Add logic to choose library based on config complexity
// Simple agent → LangChain
// Stateful/multi-node → LangGraph
// Multi-agent orchestration → Google ADK

// Currently defaults to LangGraph for all agents
return langGraph.agents().create(config);
```

**What Needs to Be Implemented**:

1. **Agent Configuration Analysis Logic**:
   - Analyze `AgentBuilder` configuration to determine complexity
   - **Heuristics to consider**:
     - **Simple agents**: Few tools (< 5), simple system prompt → LangChain
     - **Stateful workflows**: Many tools (> 5), complex prompt → LangGraph
     - **Multi-agent**: Need to detect multiple agents (API extension may be needed) → Google ADK

2. **Library Routing Logic**:
   - Route to LangChain for simple agents
   - Route to LangGraph for stateful/multi-node workflows
   - Route to Google ADK for multi-agent orchestration
   - Fallback to LangChain if analysis fails or library unavailable

**Current Limitations**:

The `AgentBuilder` interface is simple:
```java
interface AgentBuilder {
    AgentBuilder model(Object chatLanguageModel);
    AgentBuilder tools(List<Object> tools);
    AgentBuilder systemPrompt(String prompt);
}
```

**No explicit indicator for**:
- Multi-agent scenarios
- Stateful workflow requirements
- Orchestration complexity

**Possible Approaches**:
1. **Heuristics-based**: Use tool count, prompt complexity as indicators
2. **Extend AgentBuilder**: Add explicit complexity hints (e.g., `.stateful()`, `.multiAgent()`)
3. **Configuration hints**: Add optional hints for manual selection (e.g., `.useLibrary(Library.LANGGRAPH)`)

**Acceptance Criteria**:
- Analyze `AgentBuilder` configuration to determine complexity
- Route simple agents to LangChain
- Route stateful workflows to LangGraph
- Route multi-agent scenarios to Google ADK
- Add configuration hints for manual selection (optional)
- Fallback to LangChain if analysis fails or library unavailable
- Graceful degradation when adapters aren't available

**Dependencies**:
- ⚠️ **BLOCKING**: Complete LangGraph adapter implementation
  - Current: Has basic implementation but needs full StateGraph integration
  - TODO: "Replace with AgentExecutor or StateGraph-based agent"
  - Location: `LangGraphAdapter.java:154`
- ⚠️ **BLOCKING**: Complete Google ADK adapter implementation
  - Current: Completely stub - throws `UnsupportedOperationException`
  - TODO: "Google ADK adapter not yet implemented"
  - Location: `GoogleADKAdapter.java:40`
- Agent configuration analysis logic (heuristics or extended API)

**Recommendation**:
**Complete LangGraph and Google ADK implementations FIRST**, then add intelligent routing logic. 

Currently, LangGraph is the only fully functional option (despite being incomplete), so defaulting to it is reasonable until the others are complete. The routing logic can be implemented with fallbacks once all adapters are functional.

**Notes**:
- Current implementation defaults to LangGraph for all agents
- Need to add intelligent routing based on agent requirements
- Routing logic is straightforward but depends on adapter completeness
- Consider extending `AgentBuilder` API if heuristics prove insufficient

---

### StreamingChatModel Lazy Creation
**Category**: Enhancement  
**Priority**: P2-Medium  
**Estimated Effort**: 4 hours  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: New

**Description**:
Lazy-create `StreamingChatModel` instances when streaming is requested, rather than creating them upfront.

**Location**: `LangChainLibrary.java:38`, `LangGraphLibrary.java:41`

**Current State**:
```java
// TODO: Create StreamingChatModel when needed
```

**Acceptance Criteria**:
- Lazy-create `StreamingChatModel` when streaming is requested
- Reuse same configuration as `ChatModel`
- Handle provider-specific streaming support
- Graceful fallback if streaming not supported

**Dependencies**:
- Provider streaming support detection

**Notes**:
Better resource management - only create streaming models when actually needed.

---

### AIWorkflowBuilder Convenience Methods ⭐ PRIORITY UPDATE
**Category**: Enhancement  
**Priority**: P1-High (Updated from P2)  
**Estimated Effort**: 10 hours (Updated from 4)  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: Approved

**Description**:
Add concise convenience methods to `AIWorkflowBuilder` for common AI + workflow patterns. Method names should be short and natural: `.loop()`, `.circuit()`, `.approval()`, `.costing()`, `.mcp()`. Also add delegation methods for `.nested()` and `.continuation()` workflows.

**Location**: `AIWorkflowBuilder.java`

**Acceptance Criteria**:
- **Workflow Composition** (Delegation):
  - `.nested(nodeId, childWorkflows, aggregator, strategy)` - Delegate to workflow library
  - `.continuation(nodeId, childWorkflow, startNodeId)` - Delegate to workflow library
  - `.continuationWithNamespace(nodeId, childWorkflow, startNodeId, namespace)` - Delegate with namespace
- **Workflow Patterns** (New convenience methods):
  - `.loop(nodeId, iterations, strategy, config)` - Wrap LLM node with LoopNode
  - `.circuit(nodeId, breaker, config)` - Wrap LLM node with CircuitBreakerNode
  - `.approval(nodeId, provider, prompt, config)` - Chain HumanApprovalNode → LLM node
  - `.costing(budget)` - Create and return CostTracker instance
  - `.mcp(nodeId, mcpClient, config)` - Discover MCP tools and use with LLM
- All methods maintain fluent builder pattern
- Natural, concise API (no verbose names like `llmWithLoop`)
- Document all methods with examples
- Integration tests showing nested/continuation workflows with AI nodes

### AIWorkflowBuilder Convenience Methods ⭐ PRIORITY UPDATE
**Category**: Enhancement  
**Priority**: P1-High (Updated from P2)  
**Estimated Effort**: 10 hours (Updated from 4)  
**Proposed For**: Phase 6 (AI Integration)  
**Status**: ✅ COMPLETE (January 30, 2025)

**Description**:
Add concise convenience methods to `AIWorkflowBuilder` for common AI + workflow patterns. Method names should be short and natural: `.loop()`, `.circuit()`, `.approval()`, `.costing()`, `.mcp()`. Also add delegation methods for `.nested()` and `.continuation()` workflows.

**Location**: `AIWorkflowBuilder.java`

**Acceptance Criteria**:
- **Workflow Composition** (Delegation):
  - `.nested(nodeId, childWorkflows, aggregator, strategy)` - Delegate to workflow library ✅
  - `.continuation(nodeId, childWorkflow, startNodeId)` - Delegate to workflow library ✅
  - `.continuationWithNamespace(nodeId, childWorkflow, startNodeId, namespace)` - Delegate with namespace ✅
- **Workflow Patterns** (New convenience methods):
  - `.loop(nodeId, iterations, strategy, config)` - Wrap LLM node with LoopNode ✅
  - `.circuit(nodeId, breaker, config)` - Wrap LLM node with CircuitBreakerNode ✅
  - `.approval(nodeId, provider, prompt, config)` - Chain HumanApprovalNode → LLM node ✅
  - `.costing(budget)` - Create and return CostTracker instance ✅
  - `.mcp(nodeId, mcpClient, config)` - Discover MCP tools and use with LLM ✅
  - `.agents(nodeId, config)` - Agent orchestration with tools ✅
- All methods maintain fluent builder pattern ✅
- Natural, concise API (no verbose names like `llmWithLoop`) ✅
- Document all methods with examples ✅
- Integration tests showing nested/continuation workflows with AI nodes ✅
- **Integration Documentation**: Created comprehensive `NESTED_CONTINUATION_AI_WORKFLOWS.md` guide ✅

**Dependencies**:
- Workflow library features (NestedWorkflowNode, ContinuationNode, LoopNode, CircuitBreakerNode, HumanApprovalNode, CostTracker) ✅
- MCP client integration ✅

**Notes**:
User explicitly requested shorter, more natural method names. Avoid forced/unimaginative naming patterns. Nested and continuation are already in workflow library - just need delegation methods for fluent API access.

**Completed**:
- All convenience methods implemented ✅
- Delegation methods for nested/continuation workflows ✅
- Comprehensive integration documentation created (`NESTED_CONTINUATION_AI_WORKFLOWS.md`) ✅
- Examples demonstrating real-world usage ✅

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

### AI Cost Calculation Configuration
**Category**: Enhancement
**Priority**: P2-Medium
**Estimated Effort**: 8 hours
**Proposed For**: Post-Foundation
**Status**: New

**Description**:
Make cost calculation configurable and support all models/pricing tiers. Currently `calculateCost()` in `LangChainAdapter` uses hardcoded pricing values that are approximate and don't support all models or pricing tiers.

**Motivation**:
Current implementation has hardcoded pricing that:
- Is approximate and may be outdated
- Doesn't support all models/providers
- Doesn't support pricing tiers (e.g., different pricing for different regions, enterprise pricing)
- Cannot be customized per deployment

**Acceptance Criteria**:
- Extract pricing configuration to a configurable system (file, environment, or config API)
- Support all major providers (OpenAI, Anthropic, Mistral, Google, etc.)
- Support pricing tiers (standard, enterprise, region-specific)
- Allow per-model pricing override
- Support pricing updates without code changes
- Document pricing configuration format
- Backward compatible (defaults to current hardcoded values if not configured)

**Dependencies**:
- Configuration system (may depend on framework config infrastructure)

**Notes**:
TODO comment in `LangChainAdapter.calculateCost()` method (line ~1911). Current implementation has hardcoded pricing for GPT-4, GPT-3.5, Claude, and Mistral models. Should be made configurable and extensible.

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
