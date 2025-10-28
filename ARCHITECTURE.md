# Roya Framework: Architecture

**Technical architecture and design decisions for contributors**

---

## Table of Contents

1. [Design Philosophy](#design-philosophy)
2. [Core Abstractions](#core-abstractions)
3. [Layer Architecture](#layer-architecture)
4. [Plugin System](#plugin-system)
5. [Request Lifecycle](#request-lifecycle)
6. [Middleware Pipeline](#middleware-pipeline)
7. [Routing System](#routing-system)
8. [Service Registry](#service-registry)
9. [AI Integration](#ai-integration)
10. [Performance Optimizations](#performance-optimizations)

---

## Design Philosophy

### 1. Everything Is Middleware

**Core Principle**: There is only one abstraction - `Handler`.

```java
@FunctionalInterface
public interface Handler {
    void handle(Request req, Response res, Next next) throws Exception;
}
```

**Everything implements Handler:**
- Route handlers
- Middleware (CORS, logging, auth)
- Routers (collections of handlers)
- The app itself (Roya implements Handler)
- Error handlers (via adapter pattern)

**Why?**
- **Conceptual simplicity**: One pattern to learn
- **Infinite composability**: Middleware wraps middleware wraps middleware
- **Express compatibility**: Matches Express's mental model exactly

### 2. Virtual Threads Everywhere

**Core Principle**: Never use platform threads or thread pools.

```java
// Each request gets a virtual thread
Thread.startVirtualThread(() -> {
    handleRequest(exchange);
});
```

**Why?**
- **Simplicity**: No async/await, no callbacks, no reactive streams
- **Performance**: 1M+ concurrent requests without tuning
- **Code clarity**: Write blocking code that's actually fast

### 3. Scoped Values Over ThreadLocal

**Core Principle**: Request context via `ScopedValue`, not `ThreadLocal`.

```java
public static final ScopedValue<User> CURRENT_USER = ScopedValue.newInstance();

// Set in middleware
ScopedValue.where(CURRENT_USER, user)
    .run(() -> next.handle(req, res));

// Access anywhere
var user = CURRENT_USER.get();
```

**Why?**
- **Virtual thread friendly**: No memory leaks
- **Immutable**: Can't be accidentally modified
- **Structured**: Automatic cleanup

### 4. FFM for Performance

**Core Principle**: Use Foreign Function & Memory API for zero-copy I/O.

```java
// Instead of ByteBuffer pools
try (Arena arena = Arena.ofConfined()) {
    MemorySegment buffer = arena.allocate(8192);
    // Use buffer, auto-freed when arena closes
}
```

**Why?**
- **Performance**: Zero-copy HTTP parsing
- **Safety**: Compile-time memory safety (no Unsafe)
- **Modern**: Built for Java's future

### 5. Records for Data

**Core Principle**: All DTOs, configs, and immutable data are records.

```java
record User(int id, String name, String email) {}
record CreateUserRequest(@NotBlank String name, @Email String email) {}
```

**Why?**
- **Type safety**: Compile-time validation
- **Auto-serialization**: JSON in/out with zero code
- **Pattern matching**: Future-ready for switch expressions

---

## Core Abstractions

### Handler (The Foundation)

```java
@FunctionalInterface
public interface Handler {
    void handle(Request req, Response res, Next next) throws Exception;
}

@FunctionalInterface
public interface Next {
    void handle(Request req, Response res) throws Exception;
}
```

**Design notes:**
- `Next` is a closure that captures pipeline position
- `throws Exception` allows any exception to propagate to error handlers
- Functional interface enables lambda syntax

### Request

```java
public interface Request {
    // HTTP basics
    String method();
    String path();
    String protocol();
    
    // Path parameters: /users/:id
    Params params();
    
    // Query parameters: ?page=1&limit=10
    Query query();
    
    // Headers
    Headers headers();
    
    // Body (auto-deserialized)
    <T> T body(Class<T> type);
    InputStream bodyStream();
    
    // Cookies
    Cookies cookies();
    
    // Service access (from plugins)
    <T> T service(Class<T> serviceClass);
    <T> T service(ServiceKey<T> key);
    
    // Scoped values (request context)
    <T> T scope(ScopedValue<T> key);
    
    // Dynamic attributes (Express-style)
    <T> T get(String key);
    <T> void set(String key, T value);
}
```

**Design notes:**
- **Minimal interface**: Only essential methods
- **Type-safe**: Generic methods for bodies and services
- **Extensible**: `get/set` for dynamic properties (Express compatibility)

### Response

```java
public interface Response {
    // Status
    Response status(int code);
    
    // Headers
    Response header(String name, String value);
    Response headers(Map<String, String> headers);
    
    // Body
    Response send(String text);
    Response json(Object data);
    Response html(String html);
    
    // Files
    Response sendFile(Path path);
    Response download(Path path, String filename);
    
    // Redirects
    Response redirect(String url);
    Response redirect(int status, String url);
    
    // Streaming
    OutputStream stream();
    Response streamJson(Consumer<JsonStream> streamer);
    
    // Cookies
    Response cookie(String name, String value);
    Response cookie(Cookie cookie);
    
    // Convenience
    default Response ok(Object data) {
        return status(200).json(data);
    }
    
    default Response created(Object data) {
        return status(201).json(data);
    }
    
    default Response badRequest(String message) {
        return status(400).json(Map.of("error", message));
    }
    
    default Response unauthorized(String message) {
        return status(401).json(Map.of("error", message));
    }
    
    default Response notFound(String message) {
        return status(404).json(Map.of("error", message));
    }
}
```

**Design notes:**
- **Fluent API**: All methods return `Response` for chaining
- **Express-compatible**: Same method names as Express
- **Convenience methods**: Common status codes pre-defined

---

## Layer Architecture

```
┌─────────────────────────────────────────────────────┐
│ Application Layer                                   │
│ • User code (handlers, routes, business logic)      │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│ Framework Layer (Roya Core)                         │
│ • Roya class (app instance)                         │
│ • Router (path matching, mounting)                  │
│ • Middleware pipeline                               │
│ • Request/Response implementations                  │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│ Plugin Layer                                        │
│ • ServiceRegistry (DI container)                    │
│ • Database plugin (JOOQ + virtual threads)          │
│ • AI plugin (LLM, RAG, agents, vectors)             │
│ • Auth plugin (JWT, OAuth2)                         │
│ • Observability (metrics, logs, traces)             │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│ Runtime Layer                                       │
│ • HTTP server (Helidon Níma)                        │
│ • Virtual thread scheduler (JVM)                    │
│ • FFM for I/O (zero-copy buffers)                   │
│ • ScopedValue for context                           │
└─────────────────────────────────────────────────────┘
```

### Separation of Concerns

**Application Layer**:
- Business logic
- Route definitions
- Custom middleware
- **No framework internals**

**Framework Layer**:
- HTTP request routing
- Middleware orchestration
- Error handling
- **No business logic**

**Plugin Layer**:
- Cross-cutting concerns
- Service implementations
- Third-party integrations
- **Reusable across apps**

**Runtime Layer**:
- HTTP protocol handling
- Thread management
- Memory management
- **No application knowledge**

---

## Plugin System

### Plugin Interface

```java
public interface RoyaPlugin {
    
    /**
     * Called when plugin is installed
     * @param app The Roya application instance
     * @param config Plugin configuration
     */
    void install(Roya app, Config config);
    
    /**
     * Default configuration for this plugin
     */
    default Config defaultConfig() {
        return Config.empty();
    }
    
    /**
     * Plugin as middleware (optional)
     * Allows plugin to intercept requests
     */
    default Handler asMiddleware() {
        return (req, res, next) -> next.handle(req, res);
    }
    
    /**
     * Shutdown hook (optional)
     * Called when application is shutting down
     */
    default void shutdown() {
        // Cleanup resources
    }
}
```

### Plugin Lifecycle

1. **Registration**: `app.plugin(new DatabasePlugin())`
2. **Configuration**: Merges user config with defaults
3. **Installation**: `plugin.install(app, config)` called
4. **Service Registration**: Plugin registers services in `ServiceRegistry`
5. **Middleware Addition**: `plugin.asMiddleware()` added to pipeline
6. **Shutdown**: `plugin.shutdown()` called on app shutdown

### Example Plugin

```java
public class DatabasePlugin implements RoyaPlugin {
    
    private HikariDataSource pool;
    private Database db;
    
    @Override
    public void install(Roya app, Config config) {
        // Create connection pool
        this.pool = createConnectionPool(config);
        this.db = new Database(pool);
        
        // Register service (application-scoped)
        app.services().register(Database.class, Lifecycle.SINGLETON, req -> db);
        
        // Add shutdown hook
        app.onShutdown(this::shutdown);
    }
    
    @Override
    public Handler asMiddleware() {
        // Wrap each request in transaction context (optional)
        return (req, res, next) -> {
            try (var tx = db.beginTransaction()) {
                ScopedValue.where(TRANSACTION, tx)
                    .run(() -> next.handle(req, res));
                tx.commit();
            }
        };
    }
    
    @Override
    public void shutdown() {
        if (pool != null) {
            pool.close();
        }
    }
    
    @Override
    public Config defaultConfig() {
        return Config.builder()
            .set("url", System.getenv("DATABASE_URL"))
            .set("maxPoolSize", 20)
            .set("minIdle", 5)
            .build();
    }
}
```

---

## Request Lifecycle

### 1. HTTP Request Arrives

```
Client → HTTP Server (Helidon Níma)
```

Helidon receives raw HTTP request.

### 2. Virtual Thread Creation

```java
Thread.startVirtualThread(() -> {
    handleRequest(exchange);
});
```

Each request gets its own virtual thread.

### 3. Request Object Creation

```java
var request = RequestFactory.create(exchange, serviceRegistry);
var response = ResponseFactory.create(exchange);
```

Framework creates `Request` and `Response` wrappers.

### 4. Scoped Value Initialization

```java
ScopedValue.where(TRACE_CONTEXT, new TraceContext(request))
    .where(REQUEST_METRICS, new RequestMetrics())
    .where(REQUEST_CACHE, new HashMap<>())
    .run(() -> {
        executePipeline(request, response);
    });
```

Request context established.

### 5. Middleware Pipeline Execution

```
Global Middleware → Route Matching → Route Handlers → Response
```

**Pipeline order**:
1. Global middleware (CORS, logging, auth, etc.)
2. Path-mounted middleware
3. Route-specific middleware
4. Final handler
5. Error handlers (if exception thrown)

### 6. Response Sent

```java
response.finalize(); // Writes headers + body to HTTP exchange
```

Response committed to client.

### 7. Cleanup

```
Scoped values auto-cleared
Virtual thread terminates
Arena-allocated memory freed (if FFM used)
```

---

## Middleware Pipeline

### Pipeline Structure

```java
public class MiddlewarePipeline {
    
    private final List<Handler> handlers = new ArrayList<>();
    
    public void use(Handler handler) {
        handlers.add(handler);
    }
    
    public void execute(Request req, Response res) {
        executeFrom(0, req, res, (r1, r2) -> {
            // End of pipeline
        });
    }
    
    private void executeFrom(int index, Request req, Response res, Next finalNext) {
        if (index >= handlers.size()) {
            finalNext.handle(req, res);
            return;
        }
        
        var handler = handlers.get(index);
        
        Next next = (request, response) -> {
            executeFrom(index + 1, request, response, finalNext);
        };
        
        try {
            handler.handle(req, res, next);
        } catch (Exception e) {
            handleError(e, req, res);
        }
    }
}
```

### Execution Flow

```
Request arrives
    ↓
[Middleware 1] → next() called
    ↓
[Middleware 2] → next() called
    ↓
[Middleware 3] → next() called
    ↓
[Route Handler] → res.json() called (no next())
    ↓
[Middleware 3] ← returns
    ↓
[Middleware 2] ← returns
    ↓
[Middleware 1] ← returns
    ↓
Response sent
```

### Short-Circuiting

```java
// Middleware that doesn't call next() stops the pipeline
app.use((req, res, next) -> {
    if (!isAuthorized(req)) {
        res.status(401).json(Map.of("error", "Unauthorized"));
        return; // Pipeline stops here
    }
    next.handle(req, res); // Continue to next middleware
});
```

---

## Routing System

### Path Matching

**Four types of matchers:**

1. **Static**: `/users` (exact match)
2. **Parameterized**: `/users/:id` (extract params)
3. **Pattern**: `/ab?cd`, `/[0-9]+` (Express regex syntax)
4. **Full Regex**: `Pattern.compile("/.*fly$/")` (Java regex)

### PathMatcher Interface

```java
public interface PathMatcher {
    boolean matches(String path);
    Map<String, String> extractParams(String path);
}
```

### Route Structure

```java
record Route(
    String method,              // GET, POST, etc.
    PathMatcher matcher,        // Path matching logic
    List<Handler> handlers      // Middleware + final handler
) {
    boolean matches(String method, String path) {
        return this.method.equals(method) && matcher.matches(path);
    }
    
    void execute(Request req, Response res) {
        // Populate params
        var params = matcher.extractParams(req.path());
        req.setParams(params);
        
        // Execute handler pipeline
        var pipeline = new MiddlewarePipeline();
        handlers.forEach(pipeline::use);
        pipeline.execute(req, res);
    }
}
```

### Router Implementation

```java
public class Router implements Handler {
    
    private final List<Handler> middleware = new ArrayList<>();
    private final List<Route> routes = new ArrayList<>();
    
    // Add middleware (applies to all routes in this router)
    public void use(Handler handler) {
        middleware.add(handler);
    }
    
    // Add route
    public void get(String path, Handler... handlers) {
        routes.add(new Route("GET", createMatcher(path), List.of(handlers)));
    }
    
    // Router itself is a Handler (composability!)
    @Override
    public void handle(Request req, Response res, Next next) {
        // Execute router middleware first
        var pipeline = new MiddlewarePipeline();
        middleware.forEach(pipeline::use);
        
        // Find matching route
        var route = routes.stream()
            .filter(r -> r.matches(req.method(), req.path()))
            .findFirst();
        
        if (route.isPresent()) {
            // Execute route handlers
            route.get().handlers().forEach(pipeline::use);
            pipeline.execute(req, res);
        } else {
            // No match, pass to next middleware
            next.handle(req, res);
        }
    }
}
```

---

## Service Registry

### Purpose

Manage application-scoped and request-scoped services (database, AI, metrics, etc.)

### Service Lifecycle

```java
public enum Lifecycle {
    SINGLETON,       // Created once at startup, shared
    LAZY_SINGLETON,  // Created on first use, then cached
    REQUEST,         // Created once per request, cached within request
    PROTOTYPE        // Created every time (no caching)
}
```

### Registry Implementation

```java
public class ServiceRegistry {
    
    private final Map<Class<?>, ServiceProvider<?>> providers = new ConcurrentHashMap<>();
    
    public <T> void register(Class<T> serviceClass, Lifecycle lifecycle, ServiceProvider<T> provider) {
        providers.put(serviceClass, new CachedProvider<>(provider, lifecycle));
    }
    
    public <T> T get(Class<T> serviceClass, Request request) {
        var provider = providers.get(serviceClass);
        if (provider == null) {
            throw new ServiceNotFoundException(serviceClass);
        }
        return ((CachedProvider<T>) provider).provide(request);
    }
}

@FunctionalInterface
public interface ServiceProvider<T> {
    T provide(Request request);
}
```

### Usage in Plugins

```java
// Register singleton service
app.services().register(Database.class, Lifecycle.SINGLETON, req -> {
    return new Database(connectionPool);
});

// Register request-scoped service
app.services().register(RequestMetrics.class, Lifecycle.REQUEST, req -> {
    return new RequestMetrics(req.path(), req.method());
});

// Access in handler
app.get("/users", (req, res, next) -> {
    var db = req.service(Database.class);
    var users = db.query("SELECT * FROM users");
    res.json(users);
});
```

---

## AI Integration

### AI Service Interface

```java
public interface AI {
    
    // Simple prompt
    String ask(String systemPrompt, String userMessage);
    String ask(String systemPrompt, String userMessage, AIOptions options);
    
    // Structured output
    <T> T extract(Class<T> responseType, String prompt);
    
    // Streaming
    void stream(String systemPrompt, String userMessage, Consumer<String> tokenConsumer);
    
    // RAG
    RAGResponse rag(String question);
    RAGResponse rag(String question, RAGOptions options);
    
    // Vision
    Vision vision();
    
    // Fine-tuning
    FineTuning fineTune();
}
```

### RAG Implementation

```java
public class RAGService {
    
    private final AI ai;
    private final VectorStore vectors;
    
    public RAGResponse rag(String question, RAGOptions options) {
        // 1. Retrieve relevant documents
        var relevant = vectors.search(question, options.topK());
        
        // 2. Rerank (optional)
        if (options.rerank()) {
            relevant = rerank(question, relevant);
        }
        
        // 3. Build context
        var context = relevant.stream()
            .map(Document::content)
            .collect(Collectors.joining("\n\n"));
        
        // 4. Generate answer
        var prompt = """
            Context:
            %s
            
            Question: %s
            
            Answer based only on the context provided.
            """.formatted(context, question);
        
        var answer = ai.ask("You are a helpful assistant", prompt);
        
        // 5. Return with citations
        return new RAGResponse(answer, relevant);
    }
}
```

### Agent Implementation

```java
public class Agent {
    
    private final AI ai;
    private final List<Tool> tools;
    private final String goal;
    
    public AgentResponse run() {
        var context = new AgentContext();
        var maxIterations = 10;
        
        for (int i = 0; i < maxIterations; i++) {
            // Ask AI what to do next
            var action = ai.extract(AgentAction.class, buildPrompt(context));
            
            if (action.isFinished()) {
                return new AgentResponse(action.result());
            }
            
            // Execute tool
            var tool = findTool(action.toolName());
            var result = tool.execute(action.parameters());
            
            context.addStep(action, result);
        }
        
        throw new AgentException("Max iterations reached");
    }
}
```

---

## Performance Optimizations

### 1. Virtual Threads

**Benefit**: 1M+ concurrent connections

```java
// Traditional (limited by platform threads)
executorService.submit(() -> handleRequest(req)); // Max ~10K concurrent

// Roya (virtual threads)
Thread.startVirtualThread(() -> handleRequest(req)); // Max >1M concurrent
```

### 2. FFM for HTTP Parsing

**Benefit**: Zero-copy I/O, 20-30% faster parsing

```java
// Traditional (copies data)
byte[] buffer = new byte[8192];
inputStream.read(buffer);
String line = new String(buffer); // Copy!

// FFM (zero-copy)
try (Arena arena = Arena.ofConfined()) {
    MemorySegment buffer = arena.allocate(8192);
    // Parse directly from memory segment (no copy)
    var line = parseHttpLine(buffer);
}
```

### 3. Scoped Values vs ThreadLocal

**Benefit**: No memory leaks with virtual threads

```java
// ThreadLocal (leaks with virtual threads)
static ThreadLocal<User> currentUser = new ThreadLocal<>();
currentUser.set(user); // Must remember to call remove()!

// ScopedValue (automatic cleanup)
static ScopedValue<User> CURRENT_USER = ScopedValue.newInstance();
ScopedValue.where(CURRENT_USER, user).run(() -> {
    // CURRENT_USER available here
}); // Automatically cleaned up
```

### 4. Request-Scoped Caching

**Benefit**: Avoid duplicate service lookups

```java
// First call: creates Database instance
var db1 = req.service(Database.class);

// Second call: returns cached instance (same request)
var db2 = req.service(Database.class);

assert db1 == db2; // Same instance within request
```

### 5. AI Response Caching

**Benefit**: 97%+ cost reduction for repeated prompts

```java
app.plugin(aiCaching(), config -> {
    config.ttl(Duration.ofHours(1));
    config.semanticCache(true); // Cache similar prompts
});

// First call: $0.02
ai().ask("Summarize this article", article);

// Identical call within 1 hour: $0.00 (cached)
ai().ask("Summarize this article", article);
```

---

## Code Organization

### Project Structure

```
roya-framework/
├── roya-core/                  # Core framework
│   ├── Handler.java
│   ├── Request.java
│   ├── Response.java
│   ├── Router.java
│   ├── Roya.java
│   └── pipeline/
│       └── MiddlewarePipeline.java
├── roya-plugins/               # Built-in plugins
│   ├── database/
│   ├── ai/
│   ├── observability/
│   └── security/
├── roya-cli/                   # Command-line tool
│   └── RoyaCLI.java
├── roya-examples/              # Example applications
│   ├── hello-world/
│   ├── rest-api/
│   └── ai-chat/
└── roya-docs/                  # Documentation site
```

---

## Next Steps for Contributors

1. **Read the whitepaper** - Understand the vision
2. **Study Express.js** - We're replicating its API
3. **Learn virtual threads** - JEP 444 documentation
4. **Explore Helidon Níma** - Our HTTP server base
5. **Join discussions** - GitHub Discussions for questions

---

**Let's build the future of Java web frameworks together.**

*Architecture v1.0 - January 2025*
