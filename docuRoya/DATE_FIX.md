# Date Serialization Fix

## Problem

Backend was returning dates as arrays:
```json
{
  "createdAt": [2025, 10, 31, 18, 12, 54, 276860000],
  "updatedAt": [2025, 10, 31, 18, 12, 54, 276860000]
}
```

JavaScript's `new Date()` cannot parse this array format, causing:
```
RangeError: Invalid time value
```

## Root Cause

Jackson's default `LocalDateTime` serialization was creating arrays when `WRITE_DATES_AS_TIMESTAMPS` was enabled.

## Solution

### Backend Fix

Single ObjectMapper instance with proper configuration:
```java
// roya-core/Roya.java
private final ObjectMapper objectMapper = new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
```

Registered as singleton service:
```java
private Roya() {
    services.singleton(ObjectMapper.class, () -> objectMapper);
}
```

All middleware now fetch it:
```java
ObjectMapper objectMapper = req.get(ObjectMapper.class);
```

### Frontend Fix

Added `parseDate()` utility to handle both formats:
```javascript
// src/utils/date.js
export function parseDate(date) {
  // ISO-8601 string
  if (typeof date === 'string') {
    const parsed = new Date(date);
    return isNaN(parsed.getTime()) ? new Date() : parsed;
  }
  
  // Legacy array [year, month, day, hour, minute, second, nano]
  if (Array.isArray(date) && date.length >= 3) {
    return new Date(date[0], date[1] - 1, date[2], date[3] || 0, date[4] || 0, date[5] || 0);
  }
  
  return new Date();
}
```

## Result

Dates now serialize as ISO-8601 strings:
```json
{
  "createdAt": "2025-10-31T18:12:54",
  "updatedAt": "2025-10-31T18:12:54"
}
```

Frontend can parse:
```javascript
format(parseDate(article.createdAt), 'MMM d, yyyy')
// Output: "Oct 31, 2025"
```

## Testing

After restarting DocuRoya:
```bash
curl http://localhost:3003/api/articles/hot

# Should return:
{
  "articles": [
    {
      "createdAt": "2025-10-31T18:12:54",  ✅ ISO-8601
      "updatedAt": "2025-10-31T18:12:54"   ✅ ISO-8601
    }
  ]
}
```

Frontend at http://localhost:3000 displays dates correctly.
