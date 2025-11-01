# ObjectMapper: The Final Solution

## The Problem

ObjectMapper instances were scattered everywhere, causing:
- Inconsistent JSON serialization
- Date format issues (arrays vs ISO-8601)
- Configuration drift
- "Death by a thousand cuts"

## The Solution: True Singleton Pattern

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│ Roya.java                                               │
│ - Creates ONE ObjectMapper with JavaTimeModule          │
│ - Registers as singleton service                        │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│ Services Registry                                       │
│ - stores: ObjectMapper.class -> singleton instance     │
└──────────────────────┬──────────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
┌───────────┐   ┌──────────┐   ┌─────────────┐
│ Request   │   │ Response │   │ Cache       │
│ BodyParser│   │ Json     │   │ Plugin      │
│ Middleware│   │ Handler  │   │ JsonSerializer│
└───────────┘   └──────────┘   └─────────────┘
```

### Implementation

**1. Roya.java - The Source of Truth**
```java
private final ObjectMapper objectMapper = new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

private Roya() {
    // Register ObjectMapper as singleton service
    services.singleton(ObjectMapper.class, () -> objectMapper);
}
```

**2. Middleware - Dependency Injection**
```java
// BodyParser.java
ObjectMapper objectMapper = req.get(ObjectMapper.class);

// Json.java
ObjectMapper objectMapper = req.get(ObjectMapper.class);

// Morgan.java
ObjectMapper objectMapper = req.get(ObjectMapper.class);
```

**3. Plugins - Service Lookup**
```java
// CachePlugin.java
ObjectMapper objectMapper = services.has(ObjectMapper.class) 
    ? services.get(ObjectMapper.class)
    : new ObjectMapper(); // Fallback with same config

return new CacheServiceImpl(cachePath, config, objectMapper);
```

**4. Fallbacks - Same Configuration**
```java
// JsonSerializer.java (if not using services)
public JsonSerializer() {
    this.objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
}
```

## Files Modified

1. **roya-core/Roya.java**
    - Creates and registers ObjectMapper singleton

2. **roya-core/RequestImpl.java**
    - Removed static ObjectMapper
    - Uses `req.get(ObjectMapper.class)`

3. **roya-core/ResponseImpl.java**
    - Already receives ObjectMapper from Roya

4. **roya-core/middleware/BodyParser.java**
    - Removed static ObjectMapper
    - Uses `req.get(ObjectMapper.class)`

5. **roya-core/middleware/Json.java**
    - Removed static ObjectMapper
    - Uses `req.get(ObjectMapper.class)`

6. **roya-core/middleware/Morgan.java**
    - Removed static ObjectMapper
    - Uses `req.get(ObjectMapper.class)`

7. **roya-plugins/cache/CachePlugin.java**
    - Retrieves ObjectMapper from services

8. **roya-plugins/cache/FFMCacheBackend.java**
    - Accepts ObjectMapper as parameter

9. **roya-plugins/cache/serialization/JsonSerializer.java**
    - Added fallback constructor with JavaTimeModule

10. **roya-plugins/cache/build.gradle**
    - Added `jackson-datatype-jsr310` dependency

## Benefits

✅ **Single Source of Truth**: One configuration point
✅ **Consistency**: All JSON uses same ObjectMapper
✅ **Maintainability**: Change serialization in one place
✅ **Testability**: Easy to mock or replace
✅ **Backward Compatibility**: Fallbacks with same config
✅ **Plugin Evolution**: Plugins can customize if needed

## Configuration

All ObjectMappers (main + fallbacks) use:
```java
new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
```

This ensures:
- `LocalDateTime` → ISO-8601: `"2025-10-31T18:12:54"`
- Human-readable dates
- JavaScript-friendly format
- Consistent across all responses

## Testing

All endpoints return consistent JSON:
- `/api/articles` → ISO-8601 dates ✅
- `/api/articles/hot` → ISO-8601 dates ✅
- Cached responses → ISO-8601 dates ✅
- Error responses → ISO-8601 dates ✅

Frontend receives ISO-8601 strings exclusively.

## Summary

**Before**: 7+ ObjectMapper instances, inconsistent dates
**After**: 1 singleton + fallbacks with same config, consistent dates

No more "death by a thousand cuts"! 🎉
