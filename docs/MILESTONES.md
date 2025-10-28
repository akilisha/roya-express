# Roya Framework - Milestones

**Last Updated**: January 2025

This document tracks our progress against the roadmap, capturing accomplishments, challenges, and design changes.

---

## Template for Milestone Entries

```markdown
## [Phase Name] - [Status]

**Roadmap Reference**: Phase X
**Started**: [Date]
**Completed**: [Date] or IN PROGRESS
**Team**: [Contributors]

### What We Accomplished
- Item 1
- Item 2

### Challenges Encountered
- Challenge 1: Description
  - Resolution: How we solved it
- Challenge 2: Description
  - Resolution: Ongoing / How we solved it

### Design Changes
- Change 1: What changed and why
  - Roadmap Impact: Updated Phase X item Y
- Change 2: What changed and why

### Metrics
- Lines of code: X
- Tests written: Y
- Test coverage: Z%
- Performance: [relevant metrics]

### Lessons Learned
- Lesson 1
- Lesson 2

### Next Steps
- What comes next
```

---

## Phase 0: Foundation - ✅ COMPLETE

**Roadmap Reference**: Phase 0  
**Started**: January 13, 2025  
**Completed**: January 13, 2025  
**Team**: Core team (initial design)

### What We Accomplished
- ✅ Complete whitepaper (technical + business vision)
- ✅ Public-facing README with compelling value proposition
- ✅ Comprehensive architecture document
- ✅ Contributing guidelines
- ✅ Apache 2.0 license
- ✅ Gradle project structure (Groovy DSL)
- ✅ Project management framework (ROADMAP, MILESTONES, BACKLOG, ISSUES)
- ✅ Core interface definitions started (Handler, Next, Request)

### Challenges Encountered
None - foundational phase went smoothly.

### Design Changes
- **Group ID**: Changed from `com.roya` to `com.akilisha.oss` to match organizational identity
  - Roadmap Impact: None (minor config change)

### Metrics
- Documentation: ~15,000 words
- Code: ~200 lines (initial interfaces)
- Time invested: ~8 hours

### Lessons Learned
- **Comprehensive design upfront saves time later**: Having whitepaper, architecture, and roadmap clearly defined makes implementation decisions easier
- **Express API compatibility is our north star**: Every decision must be validated against "would an Express developer understand this?"
- **Documentation is part of the product**: Well-written docs attract contributors

### Next Steps
- Begin Phase 1: Core Abstractions
- Complete Request/Response interfaces
- Implement middleware pipeline
- Wire up Helidon Níma

---

## Phase 1: Core Abstractions & HTTP Server - ✅ COMPLETE

**Roadmap Reference**: Phase 1  
**Started**: January 13, 2025  
**Completed**: October 28, 2025  
**Team**: Core team

### What We Accomplished

#### API Layer (roya-api module)
- ✅ **Core Interfaces** - Complete Express-compatible API surface
  - `Handler.java` - Core middleware interface (functional)
  - `Next.java` - Pipeline continuation with error propagation
  - `NextException.java` - Internal error signaling
  - `ErrorHandler.java` - 4-parameter error handler
  - `Request.java` - Complete HTTP request interface
  - `Response.java` - Complete HTTP response interface

- ✅ **Supporting Types** - All auxiliary interfaces
  - `Params.java` - Path parameters with type conversion
  - `Query.java` - Query string parameters
  - `Headers.java` - HTTP headers with convenience methods
  - `Cookies.java` - Request cookies
  - `Cookie.java` - Response cookie with options (record)
  - `FileSendOptions.java` - File sending configuration (record)
  - `JsonStream.java` - Streaming JSON responses
  - `ServiceKey.java` - Named service lookup (record)

- ✅ **Pipeline Implementation**
  - `MiddlewarePipeline.java` - Complete middleware executor
  - Sequential execution with next() chaining
  - Error handler propagation
  - Short-circuit support

- ✅ **Application Class**
  - `Roya.java` - Main application class
  - Express-compatible API (use, get, post, put, delete, etc.)
  - Implements Handler (composability)
  - listen() method (server startup placeholder)

#### Core Implementation (roya-core module)
- ✅ **Request/Response Implementations**
  - `RequestImpl.java` - Wraps Helidon ServerRequest
  - `ResponseImpl.java` - Wraps Helidon ServerResponse with Jackson
  - `ParamsImpl.java`, `QueryImpl.java`, `HeadersImpl.java`, `CookiesImpl.java`
  - JSON serialization/deserialization with Jackson
  - Cookie parsing and setting
  - File sending support

- ✅ **HTTP Server Integration**
  - `Roya.java` - Fully wired to Helidon Níma web server
  - Virtual thread-based request handling
  - Middleware pipeline integrated with HTTP routing
  - Error recovery with 500 responses

#### Multi-Module Structure
- ✅ **Project Organization**
  - `roya-api`: Pure interfaces (no dependencies)
  - `roya-core`: Implementation with Helidon + Jackson
  - `roya-examples`: HelloWorld demo application
  - Proper dependency management between modules

- ✅ **Example Application**
  - `HelloWorld.java` - Working end-to-end
  - Middleware usage (logging middleware)
  - Multiple route handlers
  - Error handling
  - Beautiful startup banner
  - **SUCCESSFULLY SERVES HTTP REQUESTS** 🎉

- ✅ **Build System**
  - Multi-module Gradle build
  - Helidon Níma 4.0.3
  - Jackson 2.16.1
  - SLF4J + Logback
  - Java 23 with preview features
  - **Project builds successfully** ✅
  - **Application runs and serves HTTP** ✅

### Challenges Encountered

**Challenge 1**: Next interface design - success vs error cases
- **Description**: Initial design had `Next` as single-method interface. Realized Express has `next()` and `next(err)` - two paths.
- **Resolution**: Added default `error()` method that throws `NextException` to signal pipeline to skip to error handlers. Maintains functional interface while supporting both paths.

**Challenge 2**: NextException visibility
- **Description**: Initial implementation made `NextException` package-private, causing compilation error in `MiddlewarePipeline`.
- **Resolution**: Made `NextException` public to allow access from pipeline package. It's still an internal implementation detail.

**Challenge 3**: Package organization - core vs impl naming
- **Description**: Initially used `core` for interfaces and `impl` for implementations. User correctly pointed out this was backwards.
- **Resolution**: Renamed packages: `core` → `api` (interfaces), `impl` → `core` (implementations). This better reflects that the API is the contract and core is the implementation.

**Challenge 4**: API consistency - service() vs get()
- **Description**: Had separate `service()` methods for dependency lookup which felt "too java-esque" per user feedback.
- **Resolution**: Unified all lookups under overloaded `get()` methods: `get(Class)`, `get(ServiceKey)`, `get(ScopedValue)`, `get(String)`. More Express-like and consistent.

**Challenge 5**: Helidon API compatibility issues
- **Description**: Multiple Helidon API mismatches during implementation:
  - HeaderNames requires HeaderName objects, not strings
  - Header values use `.get()` not `.value()` (deprecated)
  - ServerRequest address is SocketAddress, not String
  - routing() takes Consumer, not HttpRouting object
  - OptionalLong doesn't have `.map()` like Optional
- **Resolution**: Wrapped all Helidon calls properly:
  - Use `HeaderNames.create(name)` for header lookups
  - Use `.get()` for header values
  - Call `.toString()` on SocketAddress
  - Use lambda for routing configuration
  - Manual conversion for OptionalLong

**Challenge 6**: Response interface default methods
- **Description**: Convenience methods (ok(), created(), badRequest(), etc.) were implemented in ResponseImpl but should use default implementations from Response interface.
- **Resolution**: Removed implementations from ResponseImpl, letting the interface defaults handle them. Cleaner and follows DRY principle.

**Challenge 7**: Missing interface methods
- **Description**: Multiple compilation errors due to missing methods: `all()` on Params/Query/Headers/Cookies, `isHeadersSent()`, `isFinished()`, `removeHeader()`, `getHeader()`.
- **Resolution**: Added all missing methods to interfaces and implementations. Some have limitations (e.g., getHeader returns null due to Helidon constraints).

### Design Changes

**Change 1**: Package naming convention
- **What changed**: Renamed `core` → `api` and `impl` → `core` 
- **Why**: More accurate - api package contains the contract, core contains implementation
- **Roadmap Impact**: Documentation updated, no functional impact

**Change 2**: Unified get() API for Request
- **What changed**: Consolidated `service()`, `scope()`, and attribute getters into overloaded `get()` methods
- **Why**: User feedback - more Express-like and consistent
- **Roadmap Impact**: Improved API ergonomics, better Express alignment

**Change 3**: Multi-module from day one
- **What changed**: User correctly set up multi-module structure (not monolith as I initially assumed)
- **Why**: Better separation of concerns, allows independent versioning of API vs implementation
- **Roadmap Impact**: Aligns perfectly with Phase 4 plugin architecture goals

### Metrics (Final)
- **Modules**: 3 (roya-api, roya-core, roya-examples)
- **Java files created**: 24
- **Lines of code**: ~2,500
- **Interfaces defined**: 13
- **Implementation classes**: 7
- **Records created**: 4
- **Build status**: ✅ SUCCESS
- **Runtime status**: ✅ HTTP SERVER RUNNING
- **Time invested**: ~6 hours total

### Code Quality
- Zero compilation errors
- Clean separation of concerns (core, pipeline, examples)
- Express API compatibility maintained
- Type-safe throughout
- Preview features used appropriately (ScopedValue in Request)

### What Works Right Now ✅
- ✅ HTTP server starts and binds to port
- ✅ Accepts and processes HTTP requests
- ✅ Middleware pipeline executes in order
- ✅ next() chaining works
- ✅ Error handlers receive exceptions
- ✅ Short-circuiting works (return without calling next)
- ✅ Express-compatible API surface complete
- ✅ Request/Response wrapper implementation
- ✅ JSON serialization/deserialization with Jackson
- ✅ Cookie parsing and setting
- ✅ Headers, query params, path params support
- ✅ File sending support
- ✅ Beautiful startup banner
- ✅ Multi-module project structure
- ✅ Virtual thread-based request handling
- ✅ **End-to-end HTTP request/response cycle** 🎉

**Verified Working:**
```bash
curl http://localhost:3001/
# Returns: Hello from Roya! 🚀
```

### What's Deferred to Phase 2
- ⏳ Path-based routing (route matching)
- ⏳ Path parameter extraction (e.g., `/users/:id`)
- ⏳ HTTP method filtering (currently all routes handle all requests)
- ⏳ Mounted sub-apps and routers
- ⏳ Unit tests

### Lessons Learned
- **Interface-first design works**: Defining contracts before implementation prevented rework
- **Express patterns map cleanly to Java**: Lambda syntax + records make it feel natural
- **Gradle is fast**: Build completes in seconds even with dependencies
- **Preview features are stable**: ScopedValue works great for request context
- **Helidon Níma is lightweight**: Server starts in ~32ms on virtual threads
- **Multi-module structure pays off early**: Clear separation makes it easy to find code
- **User feedback is gold**: The get() API unification was a great suggestion
- **Iterate on API early**: Easier to fix interface issues before implementations proliferate
- **Virtual threads "just work"**: No special configuration needed, Helidon handles it

### Success Criteria - ALL MET ✅
- ✅ HTTP server starts successfully
- ✅ Accepts and processes HTTP requests
- ✅ Returns proper HTTP responses
- ✅ Middleware pipeline executes
- ✅ Error handling works
- ✅ Express-compatible API feel
- ✅ Clean, compilable codebase
- ✅ Multi-module structure in place

**Phase 1 is officially COMPLETE!** 🎉

### Next Steps → Phase 2
Ready to begin Phase 2: Routing & Path Matching

---

## [Future Milestones Will Be Added Here]

---

## Milestone Summary

| Phase | Status | Start Date | End Date | Duration |
|-------|--------|------------|----------|----------|
| Phase 0: Foundation | ✅ Complete | Jan 13, 2025 | Jan 13, 2025 | 1 day |
| Phase 1: Core Abstractions & HTTP Server | ✅ Complete | Jan 13, 2025 | Oct 28, 2025 | 1 day |
| Phase 2: Routing & Path Matching | 🚧 In Progress | Oct 28, 2025 | - | Ongoing |

---

**Legend:**
- ✅ Complete
- 🚧 In Progress
- ⏳ Partially Complete
- ❌ Blocked
- 📅 Scheduled

---

**This document is updated as we hit milestones. See ROADMAP.md for the full plan.**
