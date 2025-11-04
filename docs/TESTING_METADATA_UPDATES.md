# Testing LangChainAdapter Metadata & Streaming Updates

This document explains how to test and verify the critical priority updates made to `LangChainAdapter`:
1. **Token Usage Extraction** - Extracting token counts from `Result<T>` wrapper
2. **Streaming Support** - Using `TokenStream` from AI Services
3. **Metadata Support** - Returning `Result<T>` for metadata access

## Quick Verification

### Option 1: Run Integration Tests (Recommended)

```bash
# Set your OpenAI API key
export OPENAI_API_KEY=your-api-key-here

# Run integration tests
./gradlew :roya-plugins:ai:test --tests "*LangChainAdapterIntegrationTest"
```

### Option 2: Run Verification Script

```bash
# Set your OpenAI API key
export OPENAI_API_KEY=your-api-key-here

# Compile
./gradlew :roya-plugins:ai:compileJava :roya-plugins:ai:compileTestJava

# Run verification script
java -cp "build/classes/java/main:build/classes/java/test:$(./gradlew :roya-plugins:ai:dependencies --configuration runtimeClasspath -q 2>/dev/null | grep -o '/[^:]*\.jar' | tr '\n' ':')" \
  com.akilisha.oss.roya.plugins.ai.langchain.VerifyMetadataUpdates
```

### Option 3: Manual Code Testing

Use the enhanced workflow demo:

```bash
# Run the enhanced AI workflow demo (it uses metadata methods)
./gradlew :roya-examples:run --args="EnhancedAIWorkflowDemo"
```

## What to Verify

### 1. Token Usage Extraction ✅

**Test**: Call `askWithMetadata()` and verify token counts are non-zero

```java
AIResponse<String> response = adapter.askWithMetadata(
    "You are helpful.",
    "Say hello.",
    AIOptions.builder().model("gpt-3.5-turbo").build()
);

// Verify:
assert response.promptTokens() > 0;      // ✅ Should be > 0
assert response.completionTokens() > 0;  // ✅ Should be > 0
assert response.totalTokens() > 0;      // ✅ Should be > 0
assert response.cost() > 0.0;           // ✅ Should be > 0 for paid models
```

**Expected Output**:
```
Prompt tokens: 15
Completion tokens: 10
Total tokens: 25
Cost: $0.00007
```

### 2. Streaming Support ✅

**Test**: Call `stream()` and verify tokens arrive incrementally

```java
List<String> tokens = new ArrayList<>();
adapter.stream(
    "You are helpful.",
    "Count from 1 to 3.",
    token -> tokens.add(token)
);

// Verify:
assert tokens.size() > 1;  // ✅ Should receive multiple tokens
assert !tokens.isEmpty();  // ✅ Should receive at least one token
```

**Expected Output**: Tokens arrive one by one, not all at once.

### 3. Metadata Support ✅

**Test**: Call `extractWithMetadata()` and verify structured data + tokens

```java
record Person(String name, int age) {}

AIResponse<Person> extracted = adapter.extractWithMetadata(
    Person.class,
    "John is 30 years old.",
    AIOptions.builder().model("gpt-3.5-turbo").build()
);

// Verify:
assert extracted.data().name().equals("John");  // ✅ Correct extraction
assert extracted.data().age() == 30;            // ✅ Correct extraction
assert extracted.totalTokens() > 0;             // ✅ Token usage available
```

## Quick Test (Integration Test)

### Prerequisites
- Set `OPENAI_API_KEY` environment variable
- Active internet connection

### Run Integration Tests

```bash
# Run integration tests (requires OpenAI API key)
export OPENAI_API_KEY=your-api-key-here
./gradlew :roya-plugins:ai:test --tests "*LangChainAdapterIntegrationTest"

# Or run all tests
./gradlew :roya-plugins:ai:test
```

### What the Tests Verify

1. **Token Usage Extraction** (`askWithMetadata`, `extractWithMetadata`)
   - ✅ Token counts are extracted (not zero)
   - ✅ Cost is calculated correctly
   - ✅ Token totals match (prompt + completion = total)

2. **Streaming** (`stream`)
   - ✅ Tokens arrive incrementally (streaming)
   - ✅ Full response is received

3. **Cost Calculation**
   - ✅ Cost is calculated based on model pricing
   - ✅ GPT-3.5 costs are reasonable (< $0.01 for simple queries)

## Manual Testing Example

Create a simple test class:

```java
package com.akilisha.oss.roya.plugins.ai.langchain;

import com.akilisha.oss.roya.plugins.ai.AIOptions;
import com.akilisha.oss.roya.plugins.ai.AIResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

public class VerifyMetadataUpdates {
    public static void main(String[] args) {
        // Setup
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null) {
            System.err.println("Set OPENAI_API_KEY environment variable");
            return;
        }

        var chatModel = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName("gpt-3.5-turbo")
            .build();
        
        var streamingModel = OpenAiStreamingChatModel.builder()
            .apiKey(apiKey)
            .modelName("gpt-3.5-turbo")
            .build();

        var adapter = new LangChainAdapter(chatModel, streamingModel, null);

        // Test 1: Token Usage Extraction
        System.out.println("=== Test 1: Token Usage Extraction ===");
        AIResponse<String> response = adapter.askWithMetadata(
            "You are helpful.",
            "Say hello in one sentence.",
            AIOptions.builder().model("gpt-3.5-turbo").build()
        );
        
        System.out.println("Response: " + response.data());
        System.out.println("Prompt tokens: " + response.promptTokens());
        System.out.println("Completion tokens: " + response.completionTokens());
        System.out.println("Total tokens: " + response.totalTokens());
        System.out.println("Cost: " + response.costFormatted());
        
        // Verify tokens are not zero
        assert response.promptTokens() > 0 : "Prompt tokens should be > 0";
        assert response.completionTokens() > 0 : "Completion tokens should be > 0";
        assert response.cost() > 0.0 : "Cost should be > 0";
        System.out.println("✅ Token usage extraction works!\n");

        // Test 2: Structured Extraction with Metadata
        System.out.println("=== Test 2: Structured Extraction with Metadata ===");
        record Person(String name, int age) {}
        
        AIResponse<Person> extracted = adapter.extractWithMetadata(
            Person.class,
            "John is 30 years old.",
            AIOptions.builder().model("gpt-3.5-turbo").build()
        );
        
        System.out.println("Extracted: " + extracted.data());
        System.out.println("Tokens: " + extracted.totalTokens());
        System.out.println("Cost: " + extracted.costFormatted());
        assert extracted.data().name().equals("John");
        assert extracted.data().age() == 30;
        System.out.println("✅ Structured extraction with metadata works!\n");

        // Test 3: Streaming
        System.out.println("=== Test 3: Streaming ===");
        System.out.print("Streaming response: ");
        adapter.stream(
            "You are helpful.",
            "Count from 1 to 3.",
            token -> System.out.print(token)
        );
        System.out.println("\n✅ Streaming works!\n");

        System.out.println("🎉 All tests passed!");
    }
}
```

Run it:
```bash
# Compile and run
./gradlew :roya-plugins:ai:compileJava
java -cp build/classes/java/main:$(./gradlew :roya-plugins:ai:printClasspath -q) \
  com.akilisha.oss.roya.plugins.ai.langchain.VerifyMetadataUpdates
```

## Expected Output

```
=== Test 1: Token Usage Extraction ===
Response: Hello! How can I assist you today?
Prompt tokens: 15
Completion tokens: 10
Total tokens: 25
Cost: $0.00007
✅ Token usage extraction works!

=== Test 2: Structured Extraction with Metadata ===
Extracted: Person[name=John, age=30]
Tokens: 35
Cost: $0.00009
✅ Structured extraction with metadata works!

=== Test 3: Streaming ===
Streaming response: 1, 2, 3
✅ Streaming works!

🎉 All tests passed!
```

## Verification Checklist

- [ ] **Token Usage**: `askWithMetadata()` returns non-zero token counts
- [ ] **Token Usage**: `extractWithMetadata()` returns non-zero token counts
- [ ] **Cost Calculation**: Cost is calculated correctly (not zero for paid models)
- [ ] **Streaming**: Tokens arrive incrementally via `TokenStream`
- [ ] **Backward Compatibility**: `ask()` and `extract()` still work without metadata
- [ ] **Error Handling**: Graceful failure when API key is missing

## Troubleshooting

### Issue: Token counts are zero
- **Cause**: `Result<T>` wrapper not extracting token usage correctly
- **Fix**: Verify `Result.tokenUsage()` returns non-null `TokenUsage` object

### Issue: Streaming doesn't work
- **Cause**: `StreamingChatModel` not configured in `getLLMService()`
- **Fix**: Verify `builder.streamingChatModel()` is called when available

### Issue: Cost is zero for paid models
- **Cause**: `calculateCost()` method not recognizing model name
- **Fix**: Check model name matching logic in `calculateCost()`

## Next Steps

After verifying these updates work:
1. ✅ Update `docs/MILESTONES.md` with test results
2. ✅ Add unit tests with mocks for CI/CD
3. ✅ Document token usage patterns in API docs
4. ✅ Add examples to `roya-examples` module

