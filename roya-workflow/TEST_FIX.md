# Test Type Casting Fix

## Issue

The original `WorkflowTest.java` had type casting issues when using `assertTrue()` with values retrieved from the execution context:

```java
// ❌ WRONG - Causes ClassCastException
assertTrue(result.context().get("completedA"));
```

**Error:**
```
class java.lang.Boolean cannot be cast to class java.util.function.BooleanSupplier
```

## Root Cause

`ExecutionContext.get()` returns `Object`, but JUnit's `assertTrue()` expects either:
- A primitive `boolean`, OR
- A `BooleanSupplier` (functional interface)

When you pass an `Object` (even if it's a `Boolean`), Java tries to cast it to `BooleanSupplier`, which fails.

## Solutions

### Solution 1: Explicit Casting (Quick Fix)

Cast the Object to Boolean:

```java
// ✅ CORRECT
assertTrue((Boolean) result.context().get("completedA"));
```

This has been applied to `WorkflowTest.java`.

### Solution 2: Use assertEquals (Alternative)

```java
// ✅ ALSO CORRECT
assertEquals(true, result.context().get("completedA"));
```

`assertEquals` handles Object types properly.

### Solution 3: Use TestUtils (Recommended)

We've created `TestUtils.java` with type-safe helper methods:

```java
// ✅ BEST - Type-safe and clean
import static com.akilisha.oss.roya.workflow.TestUtils.*;

assertContextTrue(result.context(), "completedA");
assertContextTrue(result.context(), "completedB");
```

**Benefits:**
- No manual casting
- Clear intent
- Better error messages
- Reusable across tests

## Files Updated/Added

1. **WorkflowTest.java** - Fixed with explicit casting
2. **TestUtils.java** - NEW: Helper utilities for type-safe assertions
3. **WorkflowTestWithUtils.java** - NEW: Example using TestUtils

## Available TestUtils Methods

```java
// Boolean helpers
boolean getBooleanFromContext(ExecutionContext ctx, String key)
assertContextTrue(ExecutionContext ctx, String key)
assertContextFalse(ExecutionContext ctx, String key)

// String helpers
String getStringFromContext(ExecutionContext ctx, String key)

// Integer helpers
int getIntFromContext(ExecutionContext ctx, String key)

// Generic helper
<T> T getTypedFromContext(ExecutionContext ctx, String key, Class<T> type)
```

## Usage Examples

### Original (Fixed)
```java
@Test
void testParallelExecution() {
    WorkflowResult result = executor.executeFrom("start", Map.of()).join();
    
    assertTrue((Boolean) result.context().get("completedA")); // Explicit cast
}
```

### With TestUtils (Recommended)
```java
@Test
void testParallelExecution() {
    WorkflowResult result = executor.executeFrom("start", Map.of()).join();
    
    assertContextTrue(result.context(), "completedA"); // Clean!
}
```

### Multiple Types
```java
@Test
void testMixedTypes() {
    WorkflowResult result = ...;
    
    // Type-safe retrieval
    String name = getStringFromContext(result.context(), "name");
    int age = getIntFromContext(result.context(), "age");
    boolean active = getBooleanFromContext(result.context(), "active");
    
    // Or use assertions directly
    assertContextTrue(result.context(), "active");
}
```

## Best Practices

1. **For boolean assertions**: Use `assertContextTrue()` / `assertContextFalse()`
2. **For value retrieval**: Use typed getters from `TestUtils`
3. **For comparison**: Use `assertEquals()` with context values
4. **Avoid**: Direct casting unless necessary

## Why This Matters

Java's generics use type erasure at runtime, so `ExecutionContext.get()` returns `Object`. You must either:
- Cast explicitly to the expected type
- Use methods that handle Object types (like `assertEquals`)
- Use helper methods that perform safe casting

The TestUtils approach is recommended because it:
- Centralizes casting logic
- Provides clear error messages
- Prevents runtime ClassCastException
- Makes tests more readable

## Running Tests

Both test files should now pass without issues:

```bash
# Maven
mvn test

# Gradle
./gradlew test

# Run specific test
mvn test -Dtest=WorkflowTest
mvn test -Dtest=WorkflowTestWithUtils
```

## Migration Path

If you have existing tests with similar issues:

1. **Quick fix**: Add explicit casts: `(Boolean) context.get(...)`
2. **Better**: Use `assertEquals` instead of `assertTrue`
3. **Best**: Use `TestUtils` helpers

All approaches are valid - choose based on your preference!

---

**Status**: ✅ Fixed and tested

The original test file has been corrected, and we've added `TestUtils` as a best practice example for future tests.
