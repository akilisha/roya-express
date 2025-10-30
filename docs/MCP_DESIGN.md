# roya-mcp: Design and MVP

## Purpose
Provide a production-grade scaffold to build MCP servers with Roya. Make tools/resources/events type-safe, secure, observable, and easy to ship.

## Pain Points for MCP Authors
- Protocol plumbing (envelopes, errors, schemas)
- Auth/policy (API keys, OAuth, quotas)
- Data access (DB, object storage, secrets, vector stores)
- Observability (traces, metrics, logs, costs)
- Local DX (scaffold, hot reload, conformance tests)

## Roya Value
- Handler-first: MCP endpoints are just Handlers.
- Type-safe records → JSON Schema generation.
- Built-in auth/policy middleware and rate limiting.
- First-class adapters: Database, Email, AI (llm/rag), Object Storage, Qdrant.
- Observability via Micrometer; structured logs; tracing.
- Docker/K8s friendly packaging and health endpoints.

## MVP Scope
- MCPServer (installable plugin) with routing for tools/resources/events.
- MCPRequest/MCPResponse envelopes; error model.
- Record-to-JSON-Schema generator and validation.
- Auth: API keys (env/config), per-tool scopes, rate limiting.
- Adapters: expose AI/Qdrant and Object Storage as tools quickly.
- Dev kit: hot-reload, conformance tests, example client.

## Example: Docs Assistant MCP
Goal: Search internal markdown/docs and return a concise, cited summary.

Schemas:
```java
public record DocQuery(String query, int topK) {}
public record Citation(String source, int chunkIndex, double score) {}
public record DocAnswer(String summary, java.util.List<Citation> citations) {}
```

Registration:
```java
mcp.tools().register("docs.searchAndSummarize", DocQuery.class, DocAnswer.class,
  (req, ctx) -> {
    var q = req.input();
    // Retrieval over Qdrant
    var matches = ctx.ai().ragApi().ask(
        q.query(),
        RAGOptions.builder().topK(q.topK()).build()
    );
    // Convert citations (example: payload contains source/chunkIndex)
    var cites = new java.util.ArrayList<Citation>();
    for (var d : matches.sources()) {
      String source = String.valueOf(d.metadata().getOrDefault("source", ""));
      int idx = (int) java.util.Optional.ofNullable(d.metadata().get("chunkIndex")).orElse(0);
      cites.add(new Citation(source, idx, 0.0));
    }
    return new DocAnswer(matches.answer(), cites);
  });
```

Local Run:
```bash
# Start vector DB
docker compose up -d qdrant

# Env
export QDRANT_URL=http://localhost:6333
export OPENAI_API_KEY=your-api-key

# Run MCP server (dev)
./gradlew :roya-mcp:run
```

## Security & Policy
- API-key auth middleware (per-tool scopes)
- Optional OAuth; tenancy via headers/claims
- Rate limits; quotas; audit logs

## Observability
- Micrometer metrics per-tool: count, duration, errors
- Structured logs with redaction
- Optional cost/usage summarization (AI calls)

## Packaging
- Dockerfile + docker-compose
- Health endpoints (liveness/readiness)
- Helm chart (future)

## Next Steps
- Scaffold `roya-mcp` module with server skeleton and Docs Assistant demo.
- Provide generators: `roya create mcp <name>`.
- Conformance test suite and sample MCP client.
