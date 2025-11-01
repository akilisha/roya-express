# ObjectMapper Design: Singleton Service Pattern

## Problem

Previously, `ObjectMapper` instances were created in multiple places:
- `Roya.java` (line 34)
- `Json.java` (line 22)
- `RequestImpl.java` (line 108)
- `BodyParser.java` (line 47)
- `Morgan.java` (line 46)
- And potentially other places

This led to:
- **Inconsistent JSON serialization** across the framework
- **Date format issues** (arrays vs ISO-8601 strings)
- **Configuration drift** when ObjectMapper settings changed
- **Difficult debugging** when JSON looked wrong

## Solution: Singleton Service Pattern

The `ObjectMapper` is now **centralized** as a singleton service in the Services registry.

### Implementation

1. **Single Source of Truth**: `Roya.java` creates one `ObjectMapper` instance
2. **Service Registration**: ObjectMapper is registered as a singleton service
3. **Dependency Injection**: All middleware retrieve it via `req.get(ObjectMapper.class)`

```java
// roya-core/Roya.java
private Roya() {
    // Register ObjectMapper as singleton service
    services.singleton(ObjectMapper.class, () -> objectMapper);
}
```

### Usage Pattern

Instead of:
```java
// ❌ Old: Static ObjectMapper
private static final ObjectMapper objectMapper = new ObjectMapper()
    .registerModule(new JavaTimeModule());

// Later...
objectMapper.readValue(bodyText, Object.class);
```

Now:
```java
// ✅ New: Retrieve from services
// Get ObjectMapper from services
ObjectMapper objectMapper = req.get(ObjectMapper.class);
objectMapper.readValue(bodyText, Object.class);
```

### Benefits

1. **Single Configuration**: Date serialization configured once in `Roya.java`
2. **Consistency**: All JSON operations use the same ObjectMapper
3. **Maintainability**: Change serialization settings in one place
4. **Testability**: Easy to mock or replace ObjectMapper for tests
5. **Framework Evolution**: Plugins can customize ObjectMapper if needed

### Files Modified

**Core Framework:**
- `roya-core/Roya.java` - Creates and registers ObjectMapper
- `roya-core/ResponseImpl.java` - Already uses passed ObjectMapper (unchanged)
- `roya-core/RequestImpl.java` - Uses ObjectMapper from services
- `roya-core/middleware/BodyParser.java` - Retrieves from services
- `roya-core/middleware/Json.java` - Retrieves from services
- `roya-core/middleware/Morgan.java` - Retrieves from services

**Configuration:**
```java
private final ObjectMapper objectMapper = new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
```

This single configuration ensures:
- `LocalDateTime` serializes as ISO-8601: `"2025-10-31T18:12:54"`
- Dates are human-readable and JavaScript-friendly
- Consistent formatting across all responses

### Testing Impact

All testing endpoints now return consistent JSON:
- `/api/articles` - ISO-8601 dates
- `/api/articles/hot` - ISO-8601 dates
- `/api/test/*` - Consistent formatting
- Error responses - Same format

### Frontend Impact

Frontend `parseDate()` utility handles:
- ISO-8601 strings: `"2025-10-31T18:12:54"` ✅
- Legacy arrays: `[2025, 10, 31, 18, 12, 54]` ✅ (backward compatible)

After restart, frontend receives ISO-8601 strings exclusively.

## Future Enhancements

This design enables:
1. **Plugin Customization**: Plugins can register custom ObjectMapper modules
2. **Configuration**: Users can customize serialization settings
3. **Testing**: Easy to swap ObjectMapper for tests
4. **Performance**: Single instance reduces overhead
5. **Thread Safety**: Jackson ObjectMapper is thread-safe

## Summary

**Before**: 6+ separate ObjectMapper instances, inconsistent serialization
**After**: 1 singleton ObjectMapper, consistent ISO-8601 dates everywhere

No more "endless wars with JSON"! 🎉
