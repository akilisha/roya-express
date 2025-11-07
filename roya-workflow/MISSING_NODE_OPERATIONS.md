# Missing Node Operations Analysis

## Executive Summary

This document identifies node operations that are currently missing from the roya-workflow module but would significantly enhance its utility for real-world workflow orchestration scenarios.

**Current Status:**
- ✅ Strong foundation with core node types (Trigger, Action, Logic, Conditional, Custom)
- ✅ Specialized nodes: Transform, HTTP, LLM, Loop, Continuation, Nested, HumanApproval, CircuitBreaker
- ✅ Good execution patterns: Sequential, Parallel, Async, Conditional routing

**Gap Analysis:**
The following node operations are missing and would add significant value based on common patterns in workflow orchestrators (n8n, Temporal, Airflow, etc.).

---

## High Priority Missing Operations

### 1. **Delay/Wait Nodes** ⚠️ HIGH PRIORITY
**Why Missing:** No built-in way to pause workflow execution for a duration or until a condition.

**Use Cases:**
- Rate limiting (wait between API calls)
- Scheduled delays (wait 5 minutes before retry)
- Polling patterns (wait and check condition repeatedly)
- Exponential backoff (currently only at edge level)
- Throttling concurrent operations

**Suggested Implementation:**
```java
// Fixed duration wait
WaitNode.wait(Duration.ofSeconds(30))

// Conditional wait (poll until condition met)
WaitNode.waitUntil(ctx -> ctx.get("status") == "ready", 
                   Duration.ofMinutes(5), 
                   Duration.ofSeconds(5))

// Wait for specific time
WaitNode.waitUntil(LocalDateTime.parse("2024-12-25T00:00:00"))
```

**Complexity:** ⭐ (Low)  
**Impact:** 🔥🔥🔥🔥 (High)  
**Estimated LOC:** ~150

---

### 2. **Switch/Multi-Way Router Nodes** ⚠️ HIGH PRIORITY
**Why Missing:** Only conditional edges exist; no explicit switch-case style routing node.

**Use Cases:**
- Route based on multiple discrete values (status codes, types, categories)
- Default case handling
- Explicit routing logic that's easier to understand than multiple conditional edges
- Pattern matching on values

**Suggested Implementation:**
```java
SwitchNode.builder()
    .case("urgent", routeToUrgentHandler)
    .case("normal", routeToNormalHandler)
    .case("low", routeToLowHandler)
    .defaultCase(routeToDefaultHandler)
    .build()
```

**Complexity:** ⭐⭐ (Medium-Low)  
**Impact:** 🔥🔥🔥🔥 (High)  
**Estimated LOC:** ~200

---

### 3. **Merge/Join Nodes** ⚠️ HIGH PRIORITY
**Why Missing:** Multiple execution paths can't explicitly synchronize before continuing.

**Use Cases:**
- Wait for all parallel branches to complete (fan-in pattern)
- Join multiple data sources
- Synchronization points in workflows
- Explicit merge logic (choose merge strategy: first, last, all, custom)

**Current Workaround:** Use a node with multiple incoming edges, but no explicit merge logic.

**Suggested Implementation:**
```java
MergeNode.builder()
    .strategy(MergeStrategy.WAIT_FOR_ALL)  // or FIRST_COMPLETE, ANY_SUCCESS, etc.
    .build()

// With custom merge function
MergeNode.builder()
    .customMerge((results) -> aggregateResults(results))
    .build()
```

**Complexity:** ⭐⭐ (Medium-Low)  
**Impact:** 🔥🔥🔥🔥🔥 (Very High)  
**Estimated LOC:** ~250

---

### 4. **Split/Iterator Nodes** ⚠️ HIGH PRIORITY
**Why Missing:** Can't process collections item-by-item in a workflow.

**Use Cases:**
- Process each item in a list (batch processing)
- Fan-out processing (one input → multiple parallel executions)
- Iterator patterns over arrays/collections
- Split batches into individual items

**Suggested Implementation:**
```java
// Split array into individual items
SplitNode.split("items", "item")  // context.items → multiple executions with context.item

// With parallel processing
SplitNode.split("items", "item", ExecutionMode.PARALLEL)

// With sequential processing
SplitNode.split("items", "item", ExecutionMode.SEQUENTIAL)
```

**Complexity:** ⭐⭐⭐ (Medium)  
**Impact:** 🔥🔥🔥🔥🔥 (Very High)  
**Estimated LOC:** ~300

---

### 5. **Filter Nodes** ⚠️ MEDIUM PRIORITY
**Why Missing:** No way to filter collections or conditionally include items.

**Use Cases:**
- Filter array items based on conditions
- Conditional inclusion in batches
- Data cleaning/filtering
- Quality gates

**Suggested Implementation:**
```java
FilterNode.filter("items", item -> item.get("status") == "active")

// Multiple filters
FilterNode.filter("items", 
    Filter.condition(item -> item.get("status") == "active")
          .and(item -> item.get("score") > 0.8))
```

**Complexity:** ⭐⭐ (Medium-Low)  
**Impact:** 🔥🔥🔥 (Medium-High)  
**Estimated LOC:** ~150

---

### 6. **Aggregate/Reduce Nodes** ⚠️ MEDIUM PRIORITY
**Why Missing:** No built-in aggregation operations for collections.

**Use Cases:**
- Sum, average, count, min, max operations
- Custom aggregation logic
- Reduce operations
- Collect results from parallel executions

**Suggested Implementation:**
```java
// Built-in aggregations
AggregateNode.sum("items", "price", "totalPrice")
AggregateNode.average("items", "score", "avgScore")
AggregateNode.count("items", "count")
AggregateNode.min("items", "timestamp", "earliest")
AggregateNode.max("items", "score", "maxScore")

// Custom aggregation
AggregateNode.custom("items", (items) -> {
    // custom logic
    return result;
})
```

**Complexity:** ⭐⭐ (Medium-Low)  
**Impact:** 🔥🔥🔥 (Medium-High)  
**Estimated LOC:** ~200

---

## Medium Priority Missing Operations

### 7. **Rate Limit/Throttle Nodes**
**Why Missing:** No control over execution rate per node or workflow.

**Use Cases:**
- Respect API rate limits (e.g., 100 requests/minute)
- Throttle expensive operations
- Control resource usage

**Suggested Implementation:**
```java
RateLimitNode.rateLimit(node, 100, Duration.ofMinutes(1))  // 100 executions per minute
ThrottleNode.throttle(node, Duration.ofMillis(100))  // Max 1 execution per 100ms
```

**Complexity:** ⭐⭐⭐ (Medium)  
**Impact:** 🔥🔥🔥 (Medium-High)  
**Estimated LOC:** ~250

---

### 8. **Set/Variable Nodes**
**Why Missing:** No explicit way to set context variables without side effects.

**Use Cases:**
- Set intermediate variables
- Initialize context values
- Variable manipulation
- Clear variables

**Suggested Implementation:**
```java
SetNode.set("variableName", "value")
SetNode.set("variableName", ctx -> computeValue(ctx))
SetNode.set(Map.of("key1", "value1", "key2", "value2"))
```

**Complexity:** ⭐ (Low)  
**Impact:** 🔥🔥 (Medium)  
**Estimated LOC:** ~100

---

### 9. **Validation/Assert Nodes**
**Why Missing:** No built-in data validation or assertion mechanism.

**Use Cases:**
- Validate input data
- Schema validation
- Business rule validation
- Quality checks

**Suggested Implementation:**
```java
ValidateNode.validate(ctx -> {
    if (ctx.get("email") == null) {
        throw new ValidationException("Email is required");
    }
    return true;
})

// Schema validation
ValidateNode.validateSchema("data", jsonSchema)
```

**Complexity:** ⭐⭐ (Medium-Low)  
**Impact:** 🔥🔥🔥 (Medium-High)  
**Estimated LOC:** ~200

---

### 10. **Error Handler/Throw Nodes**
**Why Missing:** No explicit error throwing or structured error handling nodes.

**Use Cases:**
- Explicit error throwing for business logic failures
- Error transformation
- Structured error handling
- Error categorization

**Suggested Implementation:**
```java
// Throw error
ThrowNode.throwError("Validation failed", errorCode)

// Catch and handle
CatchNode.catchError(ErrorType.VALIDATION_ERROR, handlerNode)

// Error handler
ErrorHandlerNode.builder()
    .catch(ValidationException.class, validationHandler)
    .catch(RuntimeException.class, genericHandler)
    .build()
```

**Complexity:** ⭐⭐⭐ (Medium)  
**Impact:** 🔥🔥🔥 (Medium-High)  
**Estimated LOC:** ~300

---

### 11. **Log/Debug Nodes**
**Why Missing:** While LoggingVisitor exists, no explicit logging nodes for workflow-level logging.

**Use Cases:**
- Explicit logging at specific points
- Debug output
- Audit logging
- Structured logging

**Suggested Implementation:**
```java
LogNode.log("Processing item: {}", ctx -> ctx.get("itemId"))
LogNode.logLevel(LogLevel.DEBUG, "Debug info: {}", ctx -> ctx.get("data"))
LogNode.logJSON("context", ctx -> ctx.toMap())
```

**Complexity:** ⭐ (Low)  
**Impact:** 🔥🔥 (Medium)  
**Estimated LOC:** ~100

---

### 12. **No-Op/Pass-Through Nodes**
**Why Missing:** Sometimes you need a node that does nothing (placeholder, debugging, routing).

**Use Cases:**
- Placeholder nodes during development
- Routing helper (multiple edges need a common target)
- Debugging (pause point)
- Temporary nodes

**Suggested Implementation:**
```java
NoOpNode.create()
PassThroughNode.create()  // Passes input through unchanged
```

**Complexity:** ⭐ (Low)  
**Impact:** 🔥 (Low-Medium)  
**Estimated LOC:** ~50

---

## Lower Priority / Advanced Operations

### 13. **Code/Script Execution Nodes**
**Why Missing:** No way to execute arbitrary code/scripts within workflow.

**Use Cases:**
- Custom business logic
- Dynamic transformations
- Script execution (JavaScript, Python, etc.)
- Formula evaluation

**Note:** This might be too powerful/dangerous for a workflow orchestrator. Consider security implications.

**Complexity:** ⭐⭐⭐⭐ (High)  
**Impact:** 🔥🔥🔥 (Medium-High)  
**Estimated LOC:** ~500+

---

### 14. **Schedule/Cron Trigger Nodes**
**Why Missing:** No time-based triggers (mentioned in backlog but not implemented).

**Use Cases:**
- Scheduled workflows
- Cron jobs
- Periodic tasks
- Time-based triggers

**Note:** This is in the backlog (Event Triggers feature).

**Complexity:** ⭐⭐⭐ (Medium)  
**Impact:** 🔥🔥🔥🔥 (High)  
**Estimated LOC:** ~400

---

### 15. **Event Wait Nodes**
**Why Missing:** No way to wait for external events (mentioned in backlog).

**Use Cases:**
- Wait for webhook
- Wait for file upload
- Wait for database change
- Wait for message queue event

**Note:** This is in the backlog (Event Triggers feature).

**Complexity:** ⭐⭐⭐ (Medium)  
**Impact:** 🔥🔥🔥🔥 (High)  
**Estimated LOC:** ~400

---

### 16. **Batch/Chunk Nodes**
**Why Missing:** No way to process data in batches/chunks.

**Use Cases:**
- Process large datasets in batches
- Chunk arrays for processing
- Batch API calls
- Memory-efficient processing

**Suggested Implementation:**
```java
BatchNode.batch("items", 100, "batch")  // Split into batches of 100
ChunkNode.chunk("data", chunkSize)
```

**Complexity:** ⭐⭐ (Medium-Low)  
**Impact:** 🔥🔥 (Medium)  
**Estimated LOC:** ~150

---

### 17. **Sort Nodes**
**Why Missing:** No built-in sorting operations.

**Use Cases:**
- Sort collections
- Order results
- Ranking operations

**Suggested Implementation:**
```java
SortNode.sort("items", "timestamp", SortOrder.DESCENDING)
SortNode.sort("items", Comparator.comparing(item -> item.get("score")))
```

**Complexity:** ⭐ (Low)  
**Impact:** 🔥🔥 (Medium)  
**Estimated LOC:** ~100

---

### 18. **Format/Transform Nodes (Advanced)**
**Why Missing:** While TransformNode exists, more specialized format operations would help.

**Use Cases:**
- JSON parsing/stringifying
- CSV parsing/generation
- XML parsing
- Date formatting
- Data type conversion

**Note:** Some of this might be better as utility functions rather than nodes.

**Complexity:** ⭐⭐ (Medium-Low)  
**Impact:** 🔥🔥 (Medium)  
**Estimated LOC:** ~300

---

### 19. **Cache Nodes**
**Why Missing:** No built-in caching mechanism.

**Use Cases:**
- Cache expensive computations
- Cache API responses
- Reduce redundant operations
- Performance optimization

**Suggested Implementation:**
```java
CacheNode.cache(node, CacheKey.from("userId", "requestId"), Duration.ofHours(1))
```

**Complexity:** ⭐⭐⭐ (Medium)  
**Impact:** 🔥🔥🔥 (Medium-High)  
**Estimated LOC:** ~250

---

### 20. **Retry Node (Explicit)**
**Why Missing:** Retry exists at edge level, but no explicit retry node.

**Use Cases:**
- Node-level retry logic
- Different retry strategies per node
- Retry with exponential backoff at node level

**Note:** This might be redundant with edge-level retry, but explicit nodes are sometimes clearer.

**Complexity:** ⭐⭐ (Medium-Low)  
**Impact:** 🔥🔥 (Medium)  
**Estimated LOC:** ~150

---

## Summary Table

| Node Operation | Priority | Complexity | Impact | Estimated LOC | Status |
|----------------|----------|------------|--------|---------------|--------|
| **Delay/Wait** | High | ⭐ | 🔥🔥🔥🔥 | ~150 | Missing |
| **Switch/Router** | High | ⭐⭐ | 🔥🔥🔥🔥 | ~200 | Missing |
| **Merge/Join** | High | ⭐⭐ | 🔥🔥🔥🔥🔥 | ~250 | Missing |
| **Split/Iterator** | High | ⭐⭐⭐ | 🔥🔥🔥🔥🔥 | ~300 | Missing |
| **Filter** | Medium | ⭐⭐ | 🔥🔥🔥 | ~150 | Missing |
| **Aggregate/Reduce** | Medium | ⭐⭐ | 🔥🔥🔥 | ~200 | Missing |
| **Rate Limit/Throttle** | Medium | ⭐⭐⭐ | 🔥🔥🔥 | ~250 | Missing |
| **Set/Variable** | Medium | ⭐ | 🔥🔥 | ~100 | Missing |
| **Validation/Assert** | Medium | ⭐⭐ | 🔥🔥🔥 | ~200 | Missing |
| **Error Handler/Throw** | Medium | ⭐⭐⭐ | 🔥🔥🔥 | ~300 | Missing |
| **Log/Debug** | Medium | ⭐ | 🔥🔥 | ~100 | Missing |
| **No-Op** | Low | ⭐ | 🔥 | ~50 | Missing |
| **Code/Script** | Low | ⭐⭐⭐⭐ | 🔥🔥🔥 | ~500+ | Missing |
| **Schedule/Cron** | Low* | ⭐⭐⭐ | 🔥🔥🔥🔥 | ~400 | In Backlog |
| **Event Wait** | Low* | ⭐⭐⭐ | 🔥🔥🔥🔥 | ~400 | In Backlog |
| **Batch/Chunk** | Low | ⭐⭐ | 🔥🔥 | ~150 | Missing |
| **Sort** | Low | ⭐ | 🔥🔥 | ~100 | Missing |
| **Cache** | Low | ⭐⭐⭐ | 🔥🔥🔥 | ~250 | Missing |

*Low priority because already planned in backlog

---

## Recommended Implementation Order

### Phase 1: Critical Operations (Highest Impact)
1. **Merge/Join Node** - Essential for fan-in patterns
2. **Split/Iterator Node** - Essential for batch processing
3. **Switch/Router Node** - Better routing UX than multiple conditional edges
4. **Delay/Wait Node** - Essential for rate limiting and polling

### Phase 2: Data Processing (High Utility)
5. **Filter Node** - Common data processing need
6. **Aggregate/Reduce Node** - Essential for data aggregation
7. **Sort Node** - Common data operation

### Phase 3: Control Flow (Medium Utility)
8. **Set/Variable Node** - Useful for context manipulation
9. **Validation/Assert Node** - Data quality gates
10. **Error Handler/Throw Node** - Better error handling
11. **Rate Limit/Throttle Node** - API rate limit compliance

### Phase 4: Developer Experience (Nice to Have)
12. **Log/Debug Node** - Explicit logging
13. **No-Op Node** - Development helper
14. **Cache Node** - Performance optimization

---

## Design Considerations

### 1. **Composition vs. New Nodes**
Some operations could be achieved through composition (e.g., Merge could be a node with multiple incoming edges). However, explicit nodes provide:
- Better UX
- Clearer intent
- Built-in optimizations
- Better error messages

### 2. **Node vs. Edge Operations**
Some operations (like retry) exist at edge level. Consider:
- **Node-level:** When operation is part of the logic
- **Edge-level:** When operation is about routing/execution

### 3. **Generic vs. Specific**
Balance between:
- Generic nodes (TransformNode - flexible but requires code)
- Specific nodes (SumNode - easy to use but many variations needed)

### 4. **Visitor Pattern Integration**
All new nodes should integrate with:
- Visitor pattern (observability)
- Cost tracking
- Circuit breakers
- Error handling

---

## Conclusion

The roya-workflow module has a solid foundation, but is missing several **high-impact node operations** that are common in workflow orchestrators:

**Must-Have (Phase 1):**
- Merge/Join Node
- Split/Iterator Node
- Switch/Router Node
- Delay/Wait Node

**Should-Have (Phase 2-3):**
- Filter, Aggregate, Sort nodes
- Set/Variable, Validation, Error Handler nodes
- Rate Limit/Throttle node

**Nice-to-Have (Phase 4):**
- Log, No-Op, Cache nodes

These additions would significantly increase the utility and make roya-workflow competitive with established workflow orchestrators while maintaining its lightweight, Java-focused design philosophy.
