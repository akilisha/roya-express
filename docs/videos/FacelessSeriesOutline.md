## Faceless Tutorial Series – Roya Express Zero-to-Alpha

**Audience:** Java & JavaScript developers who already build web backends and want an Express-inspired experience on the JVM (with AI baked in).  
**Format:** Faceless (screen-first), narrated walkthroughs with crisp callouts and captions. Target 7–10 minutes per episode.

---

### Episode 1 · Origin Story & Architecture
- **Goal:** Understand why Roya exists and how Helidon Níma + virtual threads underpin the framework.
- **Key beats:**
  - Inspiration from Express.js and the pain points Roya solves.
  - Architecture layers: `roya-api`, `roya-core`, plugins, showcase.
  - Helidon Níma overview (virtual threads, async vs structured).
- **Demo:** Repo tour; show `Roya` bootstrap registering `ObjectMapper` and `Config`.
- **CTA:** Clone repo & run Support Desk example; prep questions for Express parity.

### Episode 2 · Express Parity in Java
- **Goal:** Show how Express idioms translate 1:1 into Roya.
- **Key beats:**
  - Side-by-side GET handler, middleware chaining (`next()` semantics).
  - Routing patterns (`/users/:id`, nested routers).
  - Removal of `ConfigMiddleware` → application-level Config service.
- **Demo:** Build a tiny app twice (Express vs Roya) with identical handler logic.
- **CTA:** Port an existing Express route to Roya; share snippets on Discord.

### Episode 3 · Services & Plugins (Extension System)
- **Goal:** Explain service registry + plugin lifecycle and why it matters.
- **Key beats:**
  - `services().singleton/request/prototype` vs request-level access (`req.get`).
  - Plugin anatomy (`register`, `setup`, `start/stop`).
  - Config/Secrets precedence refactor (`RoyaConfig`, `ExpressionFilter`).
- **Demo:** Wire the Database plugin; show Config availability without middleware.
- **CTA:** Scaffold a custom plugin (e.g., logging) and register a singleton service.

### Episode 4 · Differentiation Through AI (LangChain4j)
- **Goal:** Anchor Roya’s AI-first positioning and zero-reflection stance.
- **Key beats:**
  - Unified AI surface (`llm`, `ragApi`, `vectors`, `vision`).
  - Provider overrides (OpenAI, Mistral, Ollama, Hugging Face, Gemini).
  - Type-safe AI services (`@SystemMessage`, `@UserMessage`, `@V` patterns).
- **Demo:** Simple AI extraction (record result) + provider override with env vars.
- **CTA:** Configure a local Ollama model and call it via Roya.

### Episode 5 · Foundations: Hello World → Forms → Uploads
- **Goal:** Build confidence with everyday HTTP flows.
- **Key beats:**
  - Minimal `Roya.create()` Hello World.
  - JSON form submission (`BodyParser`, request body access).
  - Multipart upload using built-in helpers.
- **Demo:** Step-by-step creation of a “Bug Reporter” mini-app.
- **CTA:** Extend the mini-app with validation middleware.

### Episode 6 · Advanced HTTP Features
- **Goal:** Showcase middleware, WebSocket, and SSE capabilities.
- **Key beats:**
  - Morgan logging fix (OffsetSeconds) and custom middleware composition.
  - WebSocket chat room with session-backed services.
  - SSE stream for long-running operations.
- **Demo:** Combine Morgan + Compression + custom auth middleware, then add WebSocket + SSE endpoints.
- **CTA:** Instrument an SSE feed with client retry logic.

### Episode 7 · AI Integration Walkthroughs
- **Goal:** Demonstrate core AI interactions before workflows.
- **Key beats:**
  - AI `ask`, `extract`, `stream` with token + cost metadata.
  - Embeddings & vector operations; skipping workflow orchestration for now.
  - Hugging Face provider wiring + config snippet.
- **Demo:** Build an AI FAQ assistant that indexes docs directory → `ragApi().ask`.
- **CTA:** Run `scripts/supportdesk_acceptance.ps1` with AI reply enabled.

### Episode 8 · Workflow Deep Dive (Support Desk)
- **Goal:** Bring it all together with the Support Desk example.
- **Key beats:**
  - Workflow nodes: triggers, LLM, RAG, notifications.
  - Config precedence in action (env/system/classpath/Vault).
  - Observability targets (metrics/tracing TODOs).
- **Demo:** Walk through the final acceptance script, highlight AI reply path.
- **CTA:** Fork Support Desk, add a custom workflow node (e.g., sentiment analysis).

---

### Production Checklist (applies to every episode)
- **Script skeleton:** Hook → Context → Demo → Recap → CTA (~800–1,000 words).
- **Assets:** VS Code workspace, terminal presets, PIP deck overlays for callouts.
- **Recording stack:** OBS or ScreenFlow (4K capture), CapCut/Descript for editing, ElevenLabs or in-house narration for voiceover.
- **Post-production:** Chapters, subtitles (WebVTT), blog embed summary, docs site cross-link.
- **Distribution:** YouTube (unlisted until launch), docs site embed, Discord announcement, Twitter/LinkedIn snippet.

---

### Next Actions
1. Flesh out episode scripts/storyboards (start with Ep1–Ep3).
2. Prepare demo branches or git tags for reproducible recordings.
3. Lock narration voice/settings and intro/outro stingers.




