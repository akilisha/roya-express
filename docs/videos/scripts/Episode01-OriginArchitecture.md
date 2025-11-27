## Episode 01 Script – Origin Story & Architecture

**Working title:** “Why Roya Exists: The Express-Inspired Java Framework”  
**Length target:** 7–8 minutes (≈ 950 narrated words)  
**Format:** Faceless screen recording with voiceover, kinetic captions, minimal lo-fi bed.

---

### 1. Hook (0:00 – 0:30)
- **Narration draft (~80 words):**
  > “Express.js changed what web development felt like—small files, middleware everywhere, and a mental model that just clicked. Roya brings that feeling to the JVM, but with modern foundations: Helidon Níma, virtual threads, and AI-first primitives. In this episode, we’ll unpack the inspiration, the architecture, and the reason Roya even exists.”
- **On-screen plan:** Quick-cut montage of Express code, Roya code, Helidon docs; overlay text “Express DNA · Java Power · AI Ready”.
- **Callout:** “Episode 1 / 8 · Zero-to-Alpha Series”.

### 2. Context & Problem Statement (0:30 – 1:20)
- **Narration highlights:**
  - Express dominance vs Java enterprise heaviness.
  - Pain points: servlet legacy, thread-per-request scaling, boilerplate.
  - Roya’s thesis: keep the ergonomics, upgrade the runtime.
- **Visuals:** Slide-in bullet list; show GitHub repo stars/downloads (once public).

### 3. Architecture Overview (1:20 – 3:30)
- **Key talking points (structured):**
  1. **Module layout:** `roya-api`, `roya-core`, `roya-plugins/*`, `roya-examples`, `roya-showcase`.
  2. **Entry point:** `Roya` constructor (registers `ObjectMapper`, `Config` via `RoyaConfig.create()`).
  3. **Service registry:** Singleton/request/prototype scopes available immediately—no middleware required.
  4. **Helidon Níma:** Virtual threads, structured concurrency, HTTP server integration.
  5. **Custom config pipeline:** `ConfigFilters.valueResolving()` + `ExpressionFilter` to resolve `${KEY:default}`.
- **Screen capture plan:**
  - VS Code workspace overview (`tree` command).
  - Open `roya-core/src/main/java/.../Roya.java` and highlight constructor.
  - Show `RoyaConfig.create()` and `ExpressionFilter`.
  - Brief look at `services.singleton(...)` usage.
- **Narration cue:** emphasize “Config is now application-level—no more middleware to opt-in.”

### 4. Inspiration Shout-out (3:30 – 4:30)
- **Content:**
  - Compare Express `const app = express()` to Roya `var app = Roya.create()`.
  - Mention Morgan, BodyParser parity.
  - Note removal of `ConfigMiddleware` as response to real-world feedback.
- **Visual:** Split-screen Express vs Roya snippet with animated highlights.

### 5. Roadmap of What’s Coming (4:30 – 5:30)
- **Talking points:**
  - Episode previews: Express parity, plugins/services, AI integration, hands-on build, advanced HTTP, AI demos, Support Desk workflow.
  - Encourage subscribing/playlist follow.
- **On-screen:** Timeline graphic with episode titles.

### 6. Demo: Repo Tour & First Run (5:30 – 6:45)
- **Steps:**
  1. `git clone` (if public) or open existing repo.
  2. `./gradlew :roya-examples-supportdesk:run` (assuming environment ready) or `./gradlew :roya-examples-supportdesk:shadowJar`.
  3. Show startup log snippet proving Config + Morgan fix in place.
  4. Hit `/health` endpoint in terminal (`curl http://localhost:8079/health`).
- **Narration emphasis:** Highlight offset timestamp fix, Config availability without middleware.
- **Fallback plan:** If running the full example is too heavy for video 1, use `./gradlew :roya-examples:HelloWorld:run` (ensure runnable target).

### 7. Recap & CTA (6:45 – 7:45)
- **Narration draft (~90 words):**
  > “You’ve seen the ‘why’ behind Roya, the architecture that keeps it lean, and the services that come alive before the first request hits the wire. In the next episode we’ll go route-for-route with Express, translating idioms line by line. Clone the repo, run the Support Desk baseline, and drop your questions in the comments or Discord—we’re building this playbook together.”
- **CTA overlay:** “Next: Express parity in Roya · Subscribe · Docs: roya.dev/docs”.

---

### Production Notes
- **Prerequisites for recording:**
  - Ensure `./gradlew :roya-examples-supportdesk:run` works on the capture machine (Java 23 installed).
  - Terminal profile with high-contrast theme and enlarged font.
  - Pre-record B-roll for repo structure to avoid typing delays.
- **Voiceover tips:** Conversational pace, minimal jargon, clarify Helidon & LangChain references for JS audience.
- **Graphics checklist:**
  - Intro title card.
  - Module architecture diagram (simple layered illustration).
  - Episode timeline banner for section 5.
  - Lower-third caption template for key takeaways.
- **Post tasks:** Generate captions (English), produce blog summary linking to docs sections (`Architecture`, `RoyaConfig`, `SupportDesk`).

---

### Open Questions / TODO
- Confirm final port/command to demo (Support Desk vs HelloWorld).
- Gather Helidon Níma visualization asset (diagram or screenshot) with usage rights.
- Decide on music/no music baseline for faceless format.




