# Upcoming Plugin Design Plans

## 1. Metrics Plugin

### Philosophy & Goals

**Purpose**: Provide automatic request/response metrics collection and expose Prometheus-compatible metrics endpoint.

**Design Principles**:
- **Zero-config metrics**: Automatically collect HTTP metrics (request count, duration, status codes)
- **Express-style simplicity**: `app.use(Metrics.metrics())` - that's it
- **Industry standard**: Prometheus format (most monitoring systems compatible)
- **Extensible**: Allow custom metrics registration
- **Non-blocking**: Metrics collection should not impact request handling

### Technical Approach

#### Library Choice: Micrometer

**Why Micrometer?**
- Java standard for metrics (used by Spring Boot, Micronaut, Quarkus)
- Prometheus registry included
- Built-in support for counters, gauges, histograms, timers
- Thread-safe and performant
- Easily swappable backends (Prometheus, InfluxDB, CloudWatch, etc.)

**Dependency**: `io.micrometer:micrometer-registry-prometheus:1.12.0`

#### Architecture

```java
// Service Interface
public interface Metrics {
    // Automatic HTTP metrics (registered as middleware)
    Handler middleware();
    
    // Custom metrics
    Counter counter(String name, String... tags);
    Timer timer(String name, String... tags);
    Gauge gauge(String name, Supplier<Number> value, String... tags);
    
    // Prometheus endpoint content
    String prometheus();
}

// Plugin Implementation
public class MetricsPlugin implements RoyaPlugin {
    @Override
    public void register(Services services) {
        services.singleton(Metrics.class, () -> {
            PrometheusMeterRegistry registry = new PrometheusMeterRegistry(...);
            return new MetricsServiceImpl(registry);
        });
    }
    
    @Override
    public void setup(Application app) {
        Metrics metrics = app.services().get(Metrics.class);
        app.use(metrics.middleware()); // Auto-collect HTTP metrics
        app.get("/metrics", (req, res, next) -> {
            res.type("text/plain; version=0.0.4");
            res.send(metrics.prometheus());
        });
    }
}
```

#### Metrics Collected Automatically

- `http_requests_total` - Total HTTP requests (counter)
  - Tags: `method`, `route`, `status`, `error` (if any)
- `http_request_duration_seconds` - Request duration histogram
  - Tags: `method`, `route`, `status`
  - Percentiles: 50th, 95th, 99th
- `http_request_size_bytes` - Request body size (if available)
- `http_response_size_bytes` - Response body size

#### Usage Examples

```java
// Automatic metrics collection
var app = Roya.create();
app.plugin(new MetricsPlugin()); // Registers /metrics endpoint

// Custom metrics in handlers
app.get("/users/:id", (req, res, next) -> {
    Metrics metrics = req.get(Metrics.class);
    Counter userViews = metrics.counter("user_views", "user_id", req.params().get("id"));
    userViews.increment();
    
    // ... rest of handler
});

// Prometheus scraping endpoint
// GET /metrics returns Prometheus format
```

#### Implementation Complexity

**Estimated Effort**: 1-2 days
- Micrometer setup: ~2 hours
- HTTP middleware integration: ~4 hours
- Custom metrics API: ~2 hours
- Testing & demo: ~4 hours

**Challenges**:
- Route name normalization (Express routes like `/users/:id` → `/users/:id`)
- Async response timing (need to measure end-to-end)
- Memory management (metrics can grow large)

---

## 2. Cache Plugin

### Philosophy & Goals

**Purpose**: Native FFM-based cache using memory-mapped files (Kafka-style) for zero-copy, high-performance caching.

**Design Principles**:
- **FFM-native**: Leverage Java's Foreign Function & Memory API for zero-copy efficiency
- **Kafka-inspired**: Memory-mapped file segments with offset-based keys
- **No external dependencies**: Built-in solution, no Redis required
- **Cost-efficient**: Minimal memory footprint, no cloud cache service costs
- **Configurable eviction**: Multiple strategies (LRU, LFU, TTL, size-based)
- **Minimalist**: Thin abstraction, delegate to FFM for performance

### Technical Approach

#### Why FFM-Based Cache?

**Core Tenets Alignment**:
1. **Honors Java advances**: FFM is cutting-edge Java 19+ feature (zero-copy, native memory)
2. **Cost efficiency**: No Redis subscription, minimal infrastructure
3. **Minimalism**: No external dependencies, pure Java solution
4. **Performance**: Memory-mapped files provide near-RAM speed with disk persistence

**Kafka-Inspired Architecture**:
- Memory-mapped file segments (like Kafka topics/partitions)
- Offset-based key lookup (constant-time access)
- Sequential write, random read pattern
- Efficient memory usage (only active segments in RAM)

#### FFM Implementation Strategy

```java
// Cache Service Interface
public interface Cache {
    // Basic operations
    <T> Optional<T> get(String key, Class<T> type);
    void set(String key, Object value);
    void set(String key, Object value, Duration ttl);
    void delete(String key);
    void clear();
    
    // Bulk operations
    <T> Map<String, T> getMulti(List<String> keys, Class<T> type);
    void setMulti(Map<String, Object> entries);
    void setMulti(Map<String, Object> entries, Duration ttl);
    
    // Atomic operations
    Long increment(String key);
    Long incrementBy(String key, long amount);
    
    // Cache stats
    CacheStats getStats();
}

// Cache Stats
public record CacheStats(
    long size,           // Current number of entries
    long maxSize,        // Maximum capacity
    long hits,           // Cache hits
    long misses,         // Cache misses
    double hitRatio      // Hit ratio percentage
) {}

// FFM-Based Cache Implementation
public class FFMCacheBackend implements CacheBackend {
    private final MemorySegment cacheFile;      // Memory-mapped file
    private final MemorySegment indexFile;      // Offset index (key -> offset)
    private final EvictionStrategy eviction;    // LRU, LFU, TTL, SIZE
    private final Serializer serializer;
    
    // Memory layout:
    // - Index file: HashMap-style (key hash -> file offset)
    // - Cache file: Sequential segments (data + metadata)
    // - Active segment: Current write head (like Kafka log)
    
    public FFMCacheBackend(String cacheDir, EvictionConfig config) {
        // Create memory-mapped files using FFM
        this.cacheFile = mapFile(cacheDir + "/cache.data", config.maxSizeBytes());
        this.indexFile = mapFile(cacheDir + "/index.data", config.indexSizeBytes());
        this.eviction = EvictionStrategy.create(config);
        this.serializer = new JsonSerializer(); // Or MessagePack for efficiency
    }
    
    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        // 1. Hash key to get index slot
        long keyHash = hashKey(key);
        long indexOffset = keyHash % indexSize;
        
        // 2. Read offset from index (FFM direct memory access)
        long dataOffset = indexFile.get(ValueLayout.JAVA_LONG, indexOffset);
        if (dataOffset == 0) return Optional.empty(); // Not found
        
        // 3. Read data from cache file at offset
        int dataSize = cacheFile.get(ValueLayout.JAVA_INT, dataOffset);
        byte[] data = new byte[dataSize];
        MemorySegment.copy(cacheFile, dataOffset + 4, data, 0, dataSize);
        
        // 4. Deserialize
        T value = serializer.deserialize(data, type);
        
        // 5. Update eviction metadata (LRU timestamp, LFU count)
        eviction.recordAccess(keyHash);
        
        return Optional.of(value);
    }
    
    @Override
    public void set(String key, Object value, Duration ttl) {
        // 1. Serialize value
        byte[] data = serializer.serialize(value);
        
        // 2. Check capacity, evict if needed
        if (needsEviction()) {
            eviction.evict(evictionCount());
        }
        
        // 3. Write to active segment (append-only, like Kafka)
        long writeOffset = allocateSegment(data.length + metadataSize);
        MemorySegment.copy(data, 0, cacheFile, writeOffset, data.length);
        writeMetadata(writeOffset, ttl, Instant.now());
        
        // 4. Update index (key -> offset)
        long keyHash = hashKey(key);
        long indexOffset = keyHash % indexSize;
        indexFile.set(ValueLayout.JAVA_LONG, indexOffset, writeOffset);
    }
}

// Eviction Strategies
public enum EvictionStrategy {
    LRU {  // Least Recently Used
        void evict(long count) {
            // Evict oldest accessed entries
        }
    },
    LFU {  // Least Frequently Used
        void evict(long count) {
            // Evict least accessed entries
        }
    },
    TTL {  // Time-To-Live
        void evict(long count) {
            // Evict expired entries
        }
    },
    SIZE { // Size-based
        void evict(long count) {
            // Evict largest entries first
        }
    },
    ADAPTIVE {  // Combine LRU + LFU + SIZE
        void evict(long count) {
            // Multi-factor eviction
        }
    }
}

// Plugin Configuration
public class CachePlugin implements RoyaPlugin {
    @Override
    public void register(Services services) {
        services.singleton(Cache.class, () -> {
            String cacheDir = System.getProperty("cache.dir", "./cache");
            long maxSize = Long.parseLong(System.getProperty("cache.maxSize", "1073741824")); // 1GB
            EvictionStrategy strategy = EvictionStrategy.valueOf(
                System.getProperty("cache.eviction", "LRU").toUpperCase()
            );
            
            EvictionConfig config = new EvictionConfig(
                maxSize,
                strategy,
                Duration.ofHours(24) // Default TTL
            );
            
            return new CacheServiceImpl(new FFMCacheBackend(cacheDir, config));
        });
    }
}
```

#### Memory Layout (Kafka-Inspired)

```
Index File (Memory-Mapped):
┌─────────────────────────────────────┐
│ Hash(key) → Offset in Cache File    │
│ [hash1: 0x1000] [hash2: 0x2000] ... │
└─────────────────────────────────────┘

Cache File (Memory-Mapped, Sequential):
┌──────────────────────────────────────────────┐
│ Segment 1: [size][data][metadata][ttl]     │
│ Segment 2: [size][data][metadata][ttl]     │
│ Segment 3: [size][data][metadata][ttl]     │
│ Active Segment: <-- write head               │
└──────────────────────────────────────────────┘

Benefits:
- Zero-copy reads (memory-mapped files)
- Sequential writes (append-only, like Kafka)
- Offset-based lookup (O(1) index access)
- Efficient memory usage (OS page cache)
```

#### Configuration

```bash
# Cache Configuration
CACHE_DIR=./cache                    # Cache file directory
CACHE_MAX_SIZE=1073741824             # 1GB max size
CACHE_EVICTION=LRU                    # LRU, LFU, TTL, SIZE, ADAPTIVE
CACHE_TTL=86400                       # Default TTL (seconds)
CACHE_SEGMENT_SIZE=67108864           # 64MB segments (like Kafka)
CACHE_INDEX_SIZE=134217728            # 128MB index file
```

#### Performance Characteristics

**Advantages**:
- ✅ **Zero-copy**: Direct memory access via FFM
- ✅ **Near-RAM speed**: Memory-mapped files cached by OS
- ✅ **Efficient storage**: Offset-based indexing (no hash table overhead)
- ✅ **No GC pressure**: Native memory, not heap
- ✅ **Persistent across restarts**: Files survive (optional, can be volatile)
- ✅ **Shared across processes**: Multiple JVM instances can share (with locking)

**Trade-offs**:
- ⚠️ **Volatile by default**: Like Redis, data lost on restart (can add persistence)
- ⚠️ **Memory-mapped limits**: OS limits apply (file size limits)
- ⚠️ **Single-node**: Not distributed (use for single-instance or per-instance cache)

#### Eviction Strategy Details

**LRU (Least Recently Used)**:
- Track last access timestamp per key
- Evict oldest accessed entries
- Best for: Temporal locality (recent data likely accessed again)

**LFU (Least Frequently Used)**:
- Track access count per key
- Evict least frequently accessed entries
- Best for: Popular content caching (videos, images)

**TTL (Time-To-Live)**:
- Track expiration time per key
- Evict expired entries
- Best for: Time-sensitive data (sessions, temporary data)

**SIZE (Size-based)**:
- Track size per entry
- Evict largest entries first
- Best for: Memory-constrained environments

**ADAPTIVE (Hybrid)**:
- Combine LRU + LFU + SIZE scores
- Evict based on weighted score
- Best for: General-purpose caching

#### Configuration

```java
// Environment variables
CACHE_TYPE=redis|memory
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=optional
REDIS_DATABASE=0

// Or system properties
-Dcache.type=redis
-Dredis.host=localhost
```

#### Usage Examples

```java
// In handler
app.get("/users/:id", (req, res, next) -> {
    Cache cache = req.get(Cache.class);
    
    // Try cache first
    Optional<User> cached = cache.get("user:" + userId, User.class);
    if (cached.isPresent()) {
        return res.json(cached.get());
    }
    
    // Cache miss - fetch from DB
    User user = database.dsl().selectFrom(...).fetchOne();
    
    // Store in cache (1 hour TTL)
    cache.set("user:" + userId, user, Duration.ofHours(1));
    
    res.json(user);
});

// Session storage (if Session plugin uses Cache)
cache.set("session:" + sessionId, sessionData, Duration.ofDays(7));
```

#### Implementation Complexity

**Estimated Effort**: 4-5 days
- Cache interface design: ~2 hours
- FFM memory mapping: ~8 hours (learning curve, JEP 454 documentation)
- Offset index implementation: ~6 hours
- Eviction strategies (LRU, LFU, TTL, SIZE): ~8 hours
- Serialization (JSON or MessagePack): ~3 hours
- Segment management (Kafka-style): ~4 hours
- Testing & demo: ~6 hours

**Challenges**:
- **FFM API complexity**: New API, requires understanding of memory layouts
- **Segment management**: Like Kafka, need segment rotation, compaction
- **Concurrency**: Thread-safe offset management
- **Memory alignment**: FFM requires proper alignment
- **Serialization efficiency**: MessagePack vs JSON (size vs readability)

**Learning Curve**:
- FFM is Java 19+ feature (Java 21 in our case)
- Memory-mapped files require OS-level understanding
- Kafka-style log structure needs careful design

**Benefits Worth It**:
- ✅ Zero external dependencies
- ✅ Cost-efficient (no Redis needed)
- ✅ Honors Java advances (FFM is cutting-edge)
- ✅ Minimal memory footprint
- ✅ Near-RAM performance

---

## 3. Email Plugin

### Philosophy & Goals

**Purpose**: Provider-agnostic email service with thin wrappers around provider SDKs (SendGrid, MailerSend, Brevo, etc.).

**Design Principles**:
- **Provider delegation**: Leverage provider SDKs, don't reinvent email plumbing
- **Thin wrappers**: Minimal abstraction over provider-specific APIs
- **Unified interface**: Same API regardless of provider
- **Provider features**: Expose provider-specific features when available
- **Async by default**: Non-blocking email sending
- **Template support**: Handlebars templates for all providers

### Technical Approach

#### Provider Abstraction Strategy

**Core Philosophy**: 
- Delegate email sending to provider SDKs (they handle retries, rate limits, bounces)
- Provide thin abstraction layer for consistency
- Expose provider-specific features (analytics, tags, webhooks) when needed
- SMTP as universal fallback (all providers support SMTP)

**Provider-Specific SDKs**:
- **SendGrid**: `com.sendgrid:sendgrid-java:4.10.0`
- **MailerSend**: `com.mailersend:mailersend-java:2.1.0`
- **Brevo** (Sendinblue): `com.brevo:sib-api-v3-sdk:7.0.0`
- **Resend**: `com.resend:resend-java:1.0.0`
- **Postmark**: `com.postmarkapp:postmark-client:1.10.0`
- **Amazon SES**: AWS SDK (or SMTP)
- **SMTP Fallback**: Jakarta Mail for any SMTP-compatible provider

#### Architecture

```java
// Unified Email Service Interface
public interface Email {
    // Core operations (all providers support)
    CompletableFuture<EmailResult> send(String to, String subject, String body);
    CompletableFuture<EmailResult> sendTemplate(
        String to, String subject, String templateName, Map<String, Object> data
    );
    
    // Advanced operations (provider-specific features exposed)
    EmailMessage message();
    
    // Provider-specific access (when needed)
    <T> T provider(Class<T> providerType); // SendGrid, MailerSend, etc.
}

// Provider Abstraction
public interface EmailProvider {
    String name();
    CompletableFuture<EmailResult> send(EmailRequest request);
    boolean supportsFeature(EmailFeature feature); // Analytics, tags, webhooks
}

// Email Result
public record EmailResult(
    String messageId,
    String provider,
    boolean success,
    Optional<String> error
) {}

// Provider Implementations (thin wrappers)
public class SendGridProvider implements EmailProvider {
    private final SendGrid sendGrid;
    
    @Override
    public CompletableFuture<EmailResult> send(EmailRequest request) {
        // Delegate to SendGrid SDK
        com.sendgrid.Request sgRequest = new com.sendgrid.Request();
        sgRequest.setMethod(Method.POST);
        sgRequest.setEndpoint("mail/send");
        sgRequest.setBody(buildSendGridRequest(request).toString());
        
        return sendGrid.api(sgRequest)
            .thenApply(response -> new EmailResult(
                extractMessageId(response),
                "sendgrid",
                response.getStatusCode() == 202,
                Optional.empty()
            ))
            .exceptionally(e -> new EmailResult(
                null, "sendgrid", false, Optional.of(e.getMessage())
            ));
    }
    
    @Override
    public boolean supportsFeature(EmailFeature feature) {
        return switch (feature) {
            case ANALYTICS, TAGS, WEBHOOKS, TEMPLATES -> true;
            default -> false;
        };
    }
}

public class MailerSendProvider implements EmailProvider {
    private final MailerSend mailerSend;
    
    @Override
    public CompletableFuture<EmailResult> send(EmailRequest request) {
        // Delegate to MailerSend SDK
        Email email = new Email.Builder()
            .setFrom(new EmailAddress(request.fromEmail(), request.fromName()))
            .setTo(List.of(new EmailAddress(request.to())))
            .setSubject(request.subject())
            .setHtml(request.htmlBody())
            .build();
        
        return mailerSend.email().send(email)
            .thenApply(response -> new EmailResult(
                response.get("message_id").getAsString(),
                "mailersend",
                true,
                Optional.empty()
            ));
    }
}

public class BrevoProvider implements EmailProvider {
    private final ApiClient brevoClient;
    
    @Override
    public CompletableFuture<EmailResult> send(EmailRequest request) {
        // Delegate to Brevo SDK
        SendSmtpEmail email = new SendSmtpEmail()
            .sender(new SendSmtpEmailSender().email(request.fromEmail()))
            .to(List.of(new SendSmtpEmailTo().email(request.to())))
            .subject(request.subject())
            .htmlContent(request.htmlBody());
        
        return brevoClient.transactionalEmailsApi().sendTransacEmail(email)
            .thenApply(response -> new EmailResult(
                response.getMessageId(),
                "brevo",
                true,
                Optional.empty()
            ));
    }
}

public class SmtpProvider implements EmailProvider {
    private final Session smtpSession;
    
    @Override
    public CompletableFuture<EmailResult> send(EmailRequest request) {
        // Fallback to SMTP (Jakarta Mail)
        // Works with any SMTP-compatible provider
        return CompletableFuture.supplyAsync(() -> {
            try {
                MimeMessage message = new MimeMessage(smtpSession);
                message.setFrom(new InternetAddress(request.fromEmail()));
                message.setRecipient(RecipientType.TO, new InternetAddress(request.to()));
                message.setSubject(request.subject());
                message.setContent(request.htmlBody(), "text/html");
                
                Transport.send(message);
                return new EmailResult(null, "smtp", true, Optional.empty());
            } catch (MessagingException e) {
                return new EmailResult(null, "smtp", false, Optional.of(e.getMessage()));
            }
        });
    }
}

// Plugin Implementation
public class EmailPlugin implements RoyaPlugin {
    @Override
    public void register(Services services) {
        services.singleton(Email.class, () -> {
            String providerName = System.getenv("EMAIL_PROVIDER", "smtp").toLowerCase();
            EmailProvider provider = createProvider(providerName);
            TemplateEngine templates = new HandlebarsTemplateEngine(
                Paths.get(System.getProperty("email.templates", "templates/email"))
            );
            
            return new EmailServiceImpl(provider, templates);
        });
    }
    
    private EmailProvider createProvider(String name) {
        return switch (name) {
            case "sendgrid" -> {
                String apiKey = System.getenv("SENDGRID_API_KEY");
                yield new SendGridProvider(new SendGrid(apiKey));
            }
            case "mailersend" -> {
                String apiKey = System.getenv("MAILERSEND_API_KEY");
                yield new MailerSendProvider(MailerSend.getMailerSendClient(apiKey));
            }
            case "brevo" -> {
                String apiKey = System.getenv("BREVO_API_KEY");
                yield new BrevoProvider(ApiClient.getDefault().setApiKey(apiKey));
            }
            case "resend" -> {
                String apiKey = System.getenv("RESEND_API_KEY");
                yield new ResendProvider(new Resend(apiKey));
            }
            case "postmark" -> {
                String apiKey = System.getenv("POSTMARK_API_KEY");
                yield new PostmarkProvider(Client.getDefault(apiKey));
            }
            default -> {
                // SMTP fallback
                String host = System.getenv("SMTP_HOST");
                int port = Integer.parseInt(System.getenv("SMTP_PORT", "587"));
                String user = System.getenv("SMTP_USER");
                String pass = System.getenv("SMTP_PASSWORD");
                yield new SmtpProvider(createSmtpSession(host, port, user, pass));
            }
        };
    }
}
```

#### Template Example

```html
<!-- templates/email/welcome.hbs -->
<!DOCTYPE html>
<html>
<body>
    <h1>Welcome, {{name}}!</h1>
    <p>Thanks for signing up, {{email}}.</p>
    <p>Get started: <a href="{{verifyUrl}}">Verify your account</a></p>
</body>
</html>
```

```java
// Usage
email.sendTemplate(
    "user@example.com",
    "Welcome!",
    "welcome",
    Map.of(
        "name", user.getName(),
        "email", user.getEmail(),
        "verifyUrl", "https://app.com/verify?token=" + token
    )
);
```

#### Integration with Auth Plugin

```java
// In AuthServiceImpl password reset
public String requestPasswordReset(String email) {
    String token = generateResetToken();
    // ... store token in DB ...
    
    // Send email via Email plugin
    Email emailService = req.get(Email.class); // Would need Request in context
    emailService.sendTemplate(
        email,
        "Password Reset",
        "password-reset",
        Map.of("resetUrl", "https://app.com/reset?token=" + token)
    );
    
    return token;
}
```

**Note**: Email service access in Auth plugin would require:
1. Lazy service retrieval (not in constructor)
2. Or separate `EmailService` dependency injection
3. Or event-driven approach (email sent via event bus)

#### Providers Supported

**Phase 1: Provider SDKs (Primary)**
- ✅ SendGrid (`sendgrid-java`)
- ✅ MailerSend (`mailersend-java`)
- ✅ Brevo (`sib-api-v3-sdk`)
- ✅ Resend (`resend-java`)
- ✅ Postmark (`postmark-client`)
- ✅ AWS SES (SMTP or AWS SDK)

**Phase 2: Additional Providers**
- Mailgun (SDK or SMTP)
- Mandrill (SMTP)
- SparkPost (SMTP)
- Custom SMTP (Jakarta Mail fallback)

**Strategy**: Prioritize providers with mature Java SDKs. Use SMTP as universal fallback.

#### Configuration

```bash
# Provider Selection (choose one)
EMAIL_PROVIDER=sendgrid          # sendgrid, mailersend, brevo, resend, postmark, smtp

# SendGrid Configuration
SENDGRID_API_KEY=SG.xxxxx

# MailerSend Configuration
MAILERSEND_API_KEY=mlsn.xxxxx

# Brevo Configuration
BREVO_API_KEY=xkeysib-xxxxx

# Resend Configuration
RESEND_API_KEY=re_xxxxx

# Postmark Configuration
POSTMARK_API_KEY=xxxxx

# SMTP Fallback (when EMAIL_PROVIDER=smtp)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASSWORD=app-password
SMTP_TLS=true

# Common Settings
SMTP_FROM_NAME="Your App"
SMTP_FROM_EMAIL=noreply@yourapp.com
EMAIL_TEMPLATE_DIR=templates/email
```

#### Usage Examples

```java
// Simple email (works with any provider)
app.post("/contact", (req, res, next) -> {
    Email email = req.get(Email.class);
    ContactForm form = req.body(ContactForm.class);
    
    email.send(
        "support@yourapp.com",
        "New Contact Form Submission",
        "From: " + form.email + "\n\n" + form.message
    ).thenAccept(result -> {
        if (result.success()) {
            System.out.println("Email sent via " + result.provider());
        }
    });
    
    res.json(Map.of("message", "Email queued"));
});

// Template-based email
app.post("/users/:id/notify", (req, res, next) -> {
    Email email = req.get(Email.class);
    User user = getUser(req.params().get("id"));
    
    email.sendTemplate(
        user.getEmail(),
        "New Notification",
        "notification",
        Map.of("user", user, "notification", notification)
    );
    
    res.json(Map.of("sent", true));
});

// Provider-specific features (when needed)
app.post("/sendgrid-analytics", (req, res, next) -> {
    Email email = req.get(Email.class);
    SendGrid sendGrid = email.provider(SendGrid.class);
    
    // Use SendGrid SDK directly for advanced features
    sendGrid.stats().get(StatsRequest.builder().build());
    
    res.json(Map.of("message", "Analytics fetched"));
});
```

#### Implementation Complexity

**Estimated Effort**: 4-5 days
- Email service interface: ~2 hours
- Provider abstraction layer: ~4 hours
- SendGrid/MailerSend/Brevo wrappers: ~6 hours (2 hours each)
- SMTP fallback: ~3 hours
- Template engine integration: ~4 hours
- Async sending: ~2 hours (provider SDKs handle this)
- Error handling: ~2 hours (delegate to providers)
- Testing & demo: ~8 hours

**Challenges**:
- Provider API differences (normalize to unified interface)
- SDK dependencies (may add significant JAR sizes)
- Rate limiting (providers handle this, but need clear error messages)
- Template compatibility (ensure templates work across providers)
- Feature parity (not all providers support same features)

**Solutions**:
- **Thin wrappers**: Minimal code, delegate to SDKs
- **Feature detection**: `supportsFeature()` to check capability
- **Graceful degradation**: Fall back to basic features if advanced unavailable
- **SMTP universal fallback**: Always works, even if provider SDK fails

---

## Implementation Priority

**Recommended Order**:
1. **Metrics Plugin** (1-2 days) - Simplest, immediate value
2. **Cache Plugin** (2-3 days) - High utility, straightforward
3. **Email Plugin** (3-4 days) - Most complex, but critical for auth flows

**Total Estimated**: 6-9 days for all three plugins

## Common Patterns Across Plugins

1. **Service Registration**: All plugins use `Services.singleton()` or `Services.request()`
2. **Environment Configuration**: All use env vars for setup (no config files needed)
3. **Request Access**: All via `req.get(ServiceClass.class)`
4. **Lifecycle**: All implement `RoyaPlugin` with `register()` and `setup()`
5. **Documentation**: Each gets a usage guide (like `OAUTH_USAGE.md`)

## Testing Strategy

**Unit Tests**:
- Service interface contracts
- Backend implementations (in-memory for cache, mock SMTP for email)
- Template rendering

**Integration Tests**:
- Real Redis for cache plugin
- Test SMTP server (GreenMail) for email plugin
- Prometheus scraping for metrics plugin

**Demos**:
- Comprehensive demo app for each plugin
- Showcase real-world usage patterns

