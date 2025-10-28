# Roya Framework - Quick Start for Contributors

**Status**: Phase 1 (Core Abstractions) - Foundation Complete ✅

---

## 📁 Project Structure

```
roya-express/
├── docs/
│   ├── ROADMAP.md          ← Implementation plan (12 phases)
│   ├── MILESTONES.md       ← Progress tracking
│   ├── BACKLOG.md          ← Feature queue
│   └── ISSUES.md           ← Problem tracking
├── src/
│   └── main/java/com/akilisha/oss/roya/
│       └── core/
│           ├── Handler.java         ← Middleware interface ✅
│           ├── Next.java            ← Pipeline continuation ✅
│           ├── NextException.java   ← Error signaling ✅
│           └── Request.java         ← HTTP request ✅
├── WHITEPAPER.md          ← Complete vision (READ THIS FIRST)
├── README.md              ← Public face
├── ARCHITECTURE.md        ← Technical design
├── CONTRIBUTING.md        ← How to contribute
├── STATUS.md              ← Current status snapshot
├── build.gradle           ← Gradle config
└── settings.gradle        ← Project settings
```

---

## 🎯 Current Focus

**Phase 1: Core Abstractions**

**What's Done:**
- ✅ Project structure
- ✅ Foundation docs (whitepaper, README, architecture)
- ✅ Core interfaces (Handler, Next, Request)

**What's Next:**
1. Complete `Response.java` interface
2. Create supporting types (Params, Query, Headers, Cookies)
3. Implement middleware pipeline
4. Wire up Helidon Níma HTTP server
5. Build "Hello World" example

---

## 🛠️ Development Workflow

### Build the Project

```bash
# From project root
./gradlew build
```

### Run Tests

```bash
./gradlew test
```

### Run Example (once available)

```bash
./gradlew run
```

---

## 📖 Documentation Workflow

### When You Complete a Task

1. **Update MILESTONES.md**
   - Add accomplishment to current phase
   - Note any challenges
   - Document design changes

2. **Update ROADMAP.md**
   - Check off completed items
   - Update status if phase completes

3. **Update STATUS.md**
   - Refresh metrics
   - Update "What We're Working On"

### When You Hit a Problem

1. **Create entry in ISSUES.md**
   - Use template provided
   - Assign priority and category
   - Describe problem and impact

### When You Identify New Work

1. **Add to BACKLOG.md**
   - Use template provided
   - Set priority and effort estimate
   - Link to related roadmap items

---

## 🎨 Design Principles (Remember These!)

1. **Everything is Middleware**
   - Handler interface is the only abstraction
   - Routers are handlers, apps are handlers, middleware are handlers

2. **Express Compatibility First**
   - If Express does it one way, we do it that way
   - API should be 90% copy-paste compatible

3. **Virtual Threads Everywhere**
   - Never use platform threads
   - Never use thread pools
   - Block freely (virtual threads make it efficient)

4. **Type Safety via Records**
   - DTOs are records
   - Config objects are records
   - Immutable by default

5. **Modern Java, No Magic**
   - Use language features, not reflection
   - Method handles over reflection
   - No annotation processing unless necessary

---

## 🧪 Testing Guidelines

**When Tests Are Added (Phase 1):**

### Unit Tests

```java
// Test naming: shouldDoSomethingWhenCondition
@Test
void shouldContinuePipelineWhenNextCalled() {
    // Given
    var handler = (req, res, next) -> next.handle(req, res);
    
    // When
    handler.handle(mockReq, mockRes, mockNext);
    
    // Then
    verify(mockNext).handle(mockReq, mockRes);
}
```

### Integration Tests

```java
@Test
void shouldHandleRequestEndToEnd() {
    // Given
    var app = Roya.create();
    app.get("/test", (req, res, next) -> res.send("OK"));
    
    // When
    var response = testClient.get("/test");
    
    // Then
    assertThat(response.status()).isEqualTo(200);
    assertThat(response.body()).isEqualTo("OK");
}
```

---

## 💬 Communication

**Questions?**
- Check ISSUES.md first
- Ask in GitHub Discussions
- Tag in PR/issue

**Suggestions?**
- Add to BACKLOG.md
- Discuss in GitHub Discussions

**Found a Bug?**
- Add to ISSUES.md
- Create GitHub issue
- Fix it yourself (even better!)

---

## 📋 Checklist for First Contribution

- [ ] Read WHITEPAPER.md (understand the vision)
- [ ] Read ARCHITECTURE.md (understand the design)
- [ ] Review ROADMAP.md (see the plan)
- [ ] Check STATUS.md (know current state)
- [ ] Look at BACKLOG.md (find something to work on)
- [ ] Build the project (`./gradlew build`)
- [ ] Read code (Handler, Next, Request interfaces)
- [ ] Pick a task from "What's Next" above
- [ ] Write code following design principles
- [ ] Update docs (MILESTONES.md, STATUS.md)
- [ ] Submit PR

---

## 🚀 Let's Build This!

**Remember:**
- We're building Express for Java
- Simplicity beats complexity
- Documentation is part of the product
- Have fun!

**Questions? Check the docs first, then ask!**
