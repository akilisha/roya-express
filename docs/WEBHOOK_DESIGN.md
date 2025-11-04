# Webhook Security & Auto-Registration Design

## Overview

This document outlines the design for:
1. **Generic signature verification** - Reusable across the entire application
2. **Auto-registration of webhook endpoints** - With optional database persistence

## 1. Signature Verification (Generic & Reusable)

### Design Philosophy

**Problem**: Different webhook providers use different signature formats:
- GitHub: `sha256=abc123...` in `X-Hub-Signature-256` header
- Stripe: Multiple signatures in `Stripe-Signature` header
- Generic: Various formats in custom headers

**Solution**: Create a **generic, reusable middleware** that can be used anywhere in the app, not just for AI workflows.

### Implementation: `WebhookSignatureVerifier`

Located: `roya-plugins/ai/src/main/java/com/akilisha/oss/roya/plugins/ai/webhooks/WebhookSignatureVerifier.java`

**Features**:
- ✅ **Multiple algorithms**: SHA256, SHA1 (extensible)
- ✅ **Multiple header formats**: Supports GitHub, Stripe, generic formats
- ✅ **Constant-time comparison**: Prevents timing attacks
- ✅ **Standalone middleware**: Can be used independently
- ✅ **Zero dependencies**: Pure Java, no external libs needed

### Usage Examples

#### As Middleware (Express-style)
```java
// Simple usage
app.post("/webhook", 
    WebhookSignatureVerifier.middleware("my-secret"),
    handler);

// With algorithm and header
app.post("/github-webhook",
    WebhookSignatureVerifier.middleware(
        "github-secret",
        WebhookSignatureVerifier.Algorithm.SHA256,
        "X-Hub-Signature-256"
    ),
    handler);
```

#### Inline Verification
```java
app.post("/webhook", (req, res, next) -> {
    if (WebhookSignatureVerifier.verify(req, "secret")) {
        // Process webhook
        next.handle(req, res);
    } else {
        res.status(401).json(Map.of("error", "Invalid signature"));
    }
});
```

#### For Any Endpoint (Not Just Webhooks)
```java
// Can be used for any endpoint that needs signature verification
app.post("/api/secure-endpoint",
    WebhookSignatureVerifier.middleware("api-secret"),
    handler);
```

### Security Features

1. **Constant-time comparison**: Prevents timing attacks
2. **Algorithm flexibility**: Supports multiple HMAC algorithms
3. **Header flexibility**: Works with any header name
4. **Optional verification**: Can be disabled by passing null/empty secret

### Future Enhancements

- Support for Stripe's multi-signature format
- Support for RSA signatures (for asymmetric keys)
- Rate limiting integration
- Request replay protection (nonce/timestamp validation)

---

## 2. Auto-Registration & Persistence

### Design Philosophy

**Problem**: Webhooks need to survive application restarts and be manageable:
- Webhooks should persist across restarts
- Should be able to register/unregister dynamically
- Should support multiple persistence backends (DB, file, in-memory)

**Solution**: **Abstraction layer** with pluggable persistence backends.

### Implementation: `WebhookPersistenceService`

Located: `roya-plugins/ai/src/main/java/com/akilisha/oss/roya/plugins/ai/webhooks/WebhookPersistenceService.java`

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│                 Application Startup                     │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│         WebhookPersistenceService                        │
│  ┌──────────────────────────────────────────────────┐   │
│  │  WebhookStore (Interface)                       │   │
│  │  - save(WebhookConfig)                          │   │
│  │  - load(String id)                               │   │
│  │  - loadAll()                                     │   │
│  │  - delete(String id)                             │   │
│  └──────────────────────────────────────────────────┘   │
│                    │                                     │
│        ┌───────────┼───────────┐                         │
│        │           │           │                         │
│        ▼           ▼           ▼                         │
│   Database    InMemory    FileStore                     │
│   (Postgres)  (Testing)   (Future)                      │
└─────────────────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│         WebhookRegistry                                  │
│  - Maps path+method → Workflow                          │
│  - Creates HTTP handlers                                │
└─────────────────────────────────────────────────────────┘
```

### Database Schema

**Table**: `webhook_configs`

```sql
CREATE TABLE webhook_configs (
    id VARCHAR PRIMARY KEY,                    -- Format: "METHOD:path" (e.g., "POST:/api/webhook")
    path VARCHAR NOT NULL,                     -- Webhook endpoint path
    method VARCHAR NOT NULL,                   -- HTTP method (POST, GET, etc.)
    workflow_name VARCHAR NOT NULL,            -- Workflow name
    trigger_node_id VARCHAR NOT NULL,          -- Trigger node ID in workflow
    secret VARCHAR,                            -- Secret for signature verification (encrypted?)
    algorithm VARCHAR,                         -- Signature algorithm (SHA256, SHA1)
    header_name VARCHAR,                       -- Header name containing signature
    enabled BOOLEAN DEFAULT true,              -- Enable/disable webhook
    metadata JSONB,                           -- Additional config (retries, timeout, etc.)
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    UNIQUE(method, path)                      -- Prevent duplicate registrations
);

-- Index for fast lookups
CREATE INDEX idx_webhook_configs_path_method ON webhook_configs(method, path);
CREATE INDEX idx_webhook_configs_enabled ON webhook_configs(enabled);
```

### Persistence Strategies

#### 1. In-Memory (Default)
- **Use case**: Testing, development, single-instance deployments
- **Pros**: Fast, no dependencies
- **Cons**: Lost on restart

```java
WebhookPersistenceService service = new WebhookPersistenceService(
    new WebhookPersistenceService.InMemoryWebhookStore()
);
```

#### 2. Database (PostgreSQL)
- **Use case**: Production, multi-instance deployments
- **Pros**: Persistent, scalable, queryable
- **Cons**: Requires database connection

```java
// TODO: Implement using Database plugin
Database db = req.service(Database.class);
WebhookPersistenceService service = new WebhookPersistenceService(
    new WebhookPersistenceService.DatabaseWebhookStore(db)
);
```

#### 3. File-based (Future)
- **Use case**: Simple deployments without database
- **Pros**: Persistent, no database needed
- **Cons**: Not suitable for multi-instance

### Auto-Registration Flow

#### On Application Startup

```java
// In AIPlugin or application startup code
public void start() throws Exception {
    // Initialize persistence service
    WebhookPersistenceService persistenceService = new WebhookPersistenceService(
        new DatabaseWebhookStore(db)  // or InMemoryWebhookStore()
    );
    
    // Initialize with application
    persistenceService.initialize(app);
    
    // Load all webhooks and register routes
    persistenceService.loadAndRegisterAll();
}
```

#### Runtime Registration

```java
// When building a workflow
Workflow workflow = ai.workflow("my-workflow")
    .trigger("webhook", WebhookTrigger.builder()
        .path("/api/webhook")
        .method("POST")
        .secret("webhook-secret")
        .build())
    .build();

// Register with persistence
WebhookConfig config = WebhookConfig.fromRegistration(
    registration,
    "webhook-secret",
    WebhookSignatureVerifier.Algorithm.SHA256,
    "X-Hub-Signature-256"
);

persistenceService.registerWebhook(config, workflow);
```

### Integration with Database Plugin

**Current State**: `DatabaseWebhookStore` is a placeholder that falls back to in-memory.

**To Complete**:

1. **Add JOOQ table generation**:
```java
// In database plugin or migration
public class WebhookConfigs extends TableImpl<WebhookConfigsRecord> {
    public static final WebhookConfigs WEBHOOK_CONFIGS = new WebhookConfigs();
    
    public final TableField<WebhookConfigsRecord, String> ID = ...;
    public final TableField<WebhookConfigsRecord, String> PATH = ...;
    // ... etc
}
```

2. **Implement DatabaseWebhookStore**:
```java
public class DatabaseWebhookStore implements WebhookStore {
    private final Database db;
    
    public DatabaseWebhookStore(Database db) {
        this.db = db;
    }
    
    @Override
    public void save(WebhookConfig config) {
        db.query()
            .insertInto(WEBHOOK_CONFIGS)
            .set(WEBHOOK_CONFIGS.ID, config.id())
            .set(WEBHOOK_CONFIGS.PATH, config.path())
            // ... etc
            .onConflict(WEBHOOK_CONFIGS.ID)
            .doUpdate()
            .set(...)
            .execute();
    }
    
    @Override
    public List<WebhookConfig> loadAll() {
        return db.query()
            .selectFrom(WEBHOOK_CONFIGS)
            .where(WEBHOOK_CONFIGS.ENABLED.eq(true))
            .fetch()
            .map(this::mapToConfig);
    }
}
```

3. **Register in AIPlugin**:
```java
public void start() throws Exception {
    // Get database service
    Database db = app.services().get(Database.class);
    
    // Create persistence service
    WebhookPersistenceService persistenceService = new WebhookPersistenceService(
        new DatabaseWebhookStore(db)
    );
    
    persistenceService.initialize(app);
    persistenceService.loadAndRegisterAll();
}
```

### Workflow Registry Integration

**Challenge**: Webhooks reference workflows by name, but workflows are built at runtime.

**Solution**: Create a `WorkflowRegistry` that maps workflow names to instances:

```java
public class WorkflowRegistry {
    private final Map<String, Workflow> workflows = new ConcurrentHashMap<>();
    
    public void register(String name, Workflow workflow) {
        workflows.put(name, workflow);
    }
    
    public Workflow get(String name) {
        return workflows.get(name);
    }
}
```

**Integration**:
```java
// When building workflow
Workflow workflow = ai.workflow("my-workflow")...build();

// Register workflow
WorkflowRegistry.getInstance().register("my-workflow", workflow);

// When loading webhooks, lookup workflow
WebhookConfig config = ...;
Workflow workflow = WorkflowRegistry.getInstance().get(config.workflowName());
if (workflow != null) {
    registerWebhook(config, workflow);
}
```

---

## 3. Complete Example

### Application Setup

```java
public class App {
    public static void main(String[] args) {
        Roya app = Roya.create();
        
        // Install plugins
        app.plugin(new DatabasePlugin());
        app.plugin(new AIPlugin());
        
        // Get services
        Database db = app.services().get(Database.class);
        AI ai = app.services().get(AI.class);
        
        // Initialize webhook persistence
        WebhookPersistenceService webhookService = new WebhookPersistenceService(
            new DatabaseWebhookStore(db)
        );
        webhookService.initialize(app);
        
        // Build workflows
        Workflow workflow = ai.workflow("webhook-handler")
            .trigger("webhook", WebhookTrigger.builder()
                .path("/api/webhook")
                .method("POST")
                .secret("webhook-secret")
                .build())
            .llm("process", ...)
            .build();
        
        // Register workflow
        WorkflowRegistry.getInstance().register("webhook-handler", workflow);
        
        // Load and register all webhooks (including this one)
        webhookService.loadAndRegisterAll();
        
        app.listen(3000);
    }
}
```

---

## 4. Next Steps

### Immediate (Current Sprint)
- ✅ Generic signature verification middleware
- ✅ Persistence service abstraction
- ✅ In-memory implementation
- ⏳ Database implementation (using Database plugin)
- ⏳ WorkflowRegistry integration

### Short-term
- Webhook management API endpoints (`GET /api/webhooks`, `POST /api/webhooks`, `DELETE /api/webhooks/:id`)
- Webhook health monitoring
- Retry logic for failed webhook deliveries
- Webhook event logging

### Long-term
- Multi-instance coordination (prevent duplicate registrations)
- Webhook rate limiting
- Request replay protection
- Webhook testing/debugging tools

---

## Summary

**Signature Verification**: ✅ **Generic & Reusable**
- Standalone middleware usable anywhere
- Multiple algorithms and header formats
- Security best practices (constant-time comparison)

**Auto-Registration**: ✅ **Abstraction Ready**
- Pluggable persistence backends
- In-memory implementation ready
- Database implementation ready (needs Database plugin integration)
- No database required for simple use cases

**Database Integration**: ⏳ **Ready to Implement**
- Schema designed
- Interface defined
- Needs Database plugin integration
- Uses existing PostgreSQL via Database plugin

