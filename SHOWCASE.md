# 🎬 AI Plugin Showcase Guide

## Quick Start

### 1. Set Your OpenAI API Key

```bash
# Option A: Environment variable
export AI_OPENAI_API_KEY=your-api-key-here

# Option B: System property (for Java)
-Dai.openai.apiKey=your-api-key-here
```

### 2. Run the Showcase

```bash
# Using Gradle
./gradlew :roya-examples:run --args="AIShowcase" \
  -Dai.openai.apiKey=your-api-key-here

# Or compile and run manually
javac -cp "build/libs/*:roya-examples/build/libs/*" \
  roya-examples/src/main/java/com/akilisha/oss/roya/examples/AIShowcase.java

java -cp "build/libs/*:roya-examples/build/libs/*" \
  -Dai.openai.apiKey=your-api-key-here \
  com.akilisha.oss.roya.examples.AIShowcase
```

### 3. Test It Out

**Server starts on:** `http://localhost:3000`

**Try these endpoints:**

#### 1. Type-Safe Extraction (THE KILLER FEATURE)
```bash
curl -X POST http://localhost:3000/demo/extract/product \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Widget Pro is a premium product selling for $29.99 in the Electronics category. Features: durable, lightweight, smart."
  }'
```

**Expected Response:**
```json
{
  "description": "...",
  "extracted": {
    "name": "Widget Pro",
    "price": 29.99,
    "category": "Electronics",
    "tags": ["durable", "lightweight", "smart"]
  },
  "type": "ProductInfo",
  "note": "This is a real Java object - type-safe, IDE-autocomplete works!"
}
```

#### 2. Simple Chat
```bash
curl -X POST http://localhost:3000/demo/chat \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What makes Roya Framework special?"
  }'
```

#### 3. User Profile Extraction
```bash
curl -X POST http://localhost:3000/demo/extract/profile \
  -H "Content-Type: application/json" \
  -d '{
    "text": "John Doe, age 30, software engineer at TechCorp. Email: john@example.com. Loves Java, AI, and open source."
  }'
```

#### 4. Sentiment Analysis
```bash
curl -X POST http://localhost:3000/demo/sentiment \
  -H "Content-Type: application/json" \
  -d '{
    "review": "This product is absolutely amazing! The quality is exceptional and the price is fair. Highly recommend!"
  }'
```

#### 5. Code Review
```bash
curl -X POST http://localhost:3000/demo/code-review \
  -H "Content-Type: application/json" \
  -d '{
    "code": "public class User { private String name; public String getName() { return name; } }"
  }'
```

#### 6. View Status
```bash
curl http://localhost:3000/demo/status
```

## What to Watch For

### 🎯 Cost Savings Demonstration

1. **First call**: Makes API request (costs money)
2. **Second call** (same prompt): Uses cache (FREE!)
3. Check cache stats to see hits/misses

### 🎯 Type Safety Demonstration

- Notice the response is a **real Java object**
- Try accessing properties: `product.name()`, `product.price()`
- IDE autocomplete works!
- No JSON parsing needed

### 🎯 Simplicity Demonstration

**Compare:**

**Traditional Approach:**
- Setup OpenAI client
- Make API call
- Parse JSON manually
- Handle errors
- Count tokens
- Track costs
- Implement caching
- ... 100+ lines of code

**Roya Approach:**
```java
// New unified AI surfaces
ProductInfo product = ai.llm().extract(ProductInfo.class, description);
String answer = ai.llm().ask("You are helpful", question);
var rag = ai.ragApi().ask("How do I configure caching?");
```

## 🧠 Why This Works: The Matrix Moment

When you first see this in action, it feels like you're in **The Matrix** - seeing code for what it really is. Let us break down **why** this works so beautifully.

### The Core Insight: AI as a First-Class Service

Traditional frameworks treat AI as an **external tool**:
- You import an SDK
- You make manual API calls
- You parse JSON manually
- You handle errors yourself
- You track costs yourself

**Roya treats AI like Database or Email** - just another service you `req.get(AI.class)`. This fundamental shift changes everything.

### Why This Is Revolutionary

1. **Type Safety by Default**
   - Your Java records **become** your AI schemas
   - No separate schema definitions
   - No code generation
   - IDE autocomplete works everywhere

2. **Automatic Optimization**
   - Caching happens automatically (90%+ cost savings)
   - Token counting is built-in
   - Cost tracking is transparent
   - You don't have to think about it

3. **Zero Boilerplate**
   - Compare: 100+ lines of traditional code → 1 line of Roya code
   - All the "plumbing" is handled
   - You focus on **what you want**, not **how to get it**

4. **Provider-Agnostic**
   - Same API works with OpenAI today
   - Works with Anthropic tomorrow
   - Works with local models in the future
   - Switch providers without changing your code

### The "Matrix Moment" Realized

When you write:
```java
ProductInfo product = ai.extract(ProductInfo.class, description);
```

You're not just calling an API. You're:
- ✅ Defining a schema (the record)
- ✅ Parsing unstructured text (automatic)
- ✅ Converting to typed objects (automatic)
- ✅ Caching for future calls (automatic)
- ✅ Tracking costs (automatic)
- ✅ Handling errors gracefully (automatic)

**One line. Infinite power.** That's the matrix moment.

---

## 🔧 How This Works: Under the Hood

Let's pull back the curtain and see what actually happens when you call `ai.extract()`.

### The Complete Flow (Step-by-Step)

```
┌─────────────────────────────────────────────────────────────┐
│ 1. YOUR CODE (The One Line)                                │
│                                                             │
│    ProductInfo product = ai.extract(                       │
│        ProductInfo.class,                                  │
│        "Extract product info from: Widget Pro..."          │
│    );                                                       │
└─────────────────────────────────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. AI SERVICE LAYER (AIServiceImpl.extract)                │
│                                                             │
│    a) Validates: Is ProductInfo a record? ✅              │
│    b) Builds system prompt:                                │
│       "Extract information...return as JSON matching:        │
│        ProductInfo"                                         │
│    c) Checks cache first:                                   │
│       cache.get("ai:extract:ProductInfo:...",               │
│                 ProductInfo.class)                         │
│       → Cache hit? Return immediately (FREE!)               │
│       → Cache miss? Continue...                            │
└─────────────────────────────────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. PROVIDER LAYER (OpenAIClient.completeJson)              │
│                                                             │
│    a) Sends to OpenAI API:                                 │
│       POST https://api.openai.com/v1/chat/completions      │
│       {                                                     │
│         "model": "gpt-3.5-turbo",                          │
│         "messages": [                                      │
│           {"role": "system", "content": "..."},              │
│           {"role": "user", "content": "..."}               │
│         ],                                                  │
│         "temperature": 0.3  // Lower for structured output  │
│       }                                                     │
│                                                             │
│    b) OpenAI processes request:                             │
│       - Tokenizes input (jtokkit library)                  │
│       - Sends to GPT-3.5-turbo                             │
│       - Gets JSON response back                             │
│                                                             │
│    c) Returns LLMResponse:                                  │
│       {                                                     │
│         text: "{\"name\":\"Widget Pro\",...}",             │
│         model: "gpt-3.5-turbo",                            │
│         promptTokens: 45,                                  │
│         completionTokens: 23,                              │
│         totalTokens: 68,                                   │
│         finishReason: "stop"                               │
│       }                                                     │
└─────────────────────────────────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. PROCESSING LAYER (Back in AIServiceImpl)                │
│                                                             │
│    a) Cleans JSON response:                                 │
│       - Removes markdown code blocks (if any)              │
│       - Trims whitespace                                    │
│                                                             │
│    b) Deserializes with Jackson:                            │
│       objectMapper.readValue(jsonText, ProductInfo.class)  │
│       → Converts JSON → Java Record                         │
│                                                             │
│    c) Caches the result:                                    │
│       cache.set(cacheKey, product, Duration.ofHours(24))    │
│                                                             │
│    d) Returns ProductInfo object:                           │
│       ProductInfo(                                          │
│         name="Widget Pro",                                 │
│         price=9.99,                                        │
│         category="Electronics",                            │
│         tags=null,                                          │
│         description=null                                   │
│       )                                                     │
└─────────────────────────────────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. BACK TO YOUR CODE                                        │
│                                                             │
│    You have a REAL Java object:                            │
│    - product.name() → "Widget Pro"                         │
│    - product.price() → 9.99                                │
│    - IDE autocomplete works!                               │
│    - Type-safe everywhere!                                  │
│    - Zero JSON parsing needed!                             │
└─────────────────────────────────────────────────────────────┘
```

### Key Components Explained

#### 1. **The Record Schema**
```java
record ProductInfo(
    String name,
    BigDecimal price,
    String category,
    List<String> tags,
    String description
) {}
```
- This **is** your AI schema
- No separate JSON schema files
- No code generation
- Java's type system ensures correctness

#### 2. **The Cache Layer**
- Uses Roya's **Cache plugin** (FFM-based)
- Key: `ai:extract:ProductInfo:hash-of-prompt`
- TTL: 24 hours (configurable)
- **90%+ cost savings** on repeated calls

#### 3. **The Provider Abstraction**
- `LLMProvider` interface = any AI provider
- `OpenAIClient` = OpenAI implementation
- Future: `AnthropicClient`, `CohereClient`, etc.
- Switch providers without changing your code

#### 4. **Token Counting**
- Uses `jtokkit` library (OpenAI's official tokenizer)
- Accurate token counts for cost calculation
- Tracks prompt tokens, completion tokens, total

#### 5. **Cost Calculation**
- Automatic based on model pricing:
  - GPT-3.5-turbo: $0.0015/1K prompt + $0.002/1K completion
  - GPT-4: $0.03/1K prompt + $0.06/1K completion
- Returns cost in `AIResponse` metadata
- **Cached responses = $0.00** (FREE!)

### Comprehensive Parameter Configuration Guide

Roya's `AIOptions` supports extensive LLM parameter configuration. Here's a complete guide:

#### Core Parameters

**Temperature** (0.0-2.0): Controls randomness
- **0.0 - 0.3**: Very deterministic, consistent outputs
  - ✅ **Best for**: Structured extraction, data parsing, code generation
  - ✅ **Default for**: `extract()` calls (via `AIOptions.forExtraction()`)
- **0.4 - 0.7**: Balanced creativity and consistency
  - ✅ **Best for**: General chat, explanations
  - ✅ **Default for**: `ask()` calls (0.7)
- **0.8 - 1.0**: Very creative, varied outputs
  - ✅ **Best for**: Creative writing, brainstorming

**Max Tokens**: Maximum tokens in response (default: 1000)

**Model**: Model name (e.g., "gpt-3.5-turbo", "gpt-4")

#### Sampling Parameters

**Top-P** (0.0-1.0): Nucleus sampling - consider tokens with cumulative probability
- Default: 1.0 (consider all tokens)
- Lower = more focused, higher = more diverse

**Top-K** (Integer): Top-k sampling - consider top K tokens by probability
- `null` = not set (uses all tokens)
- Lower = more deterministic, higher = more diverse

**Typical-P** (0.0-1.0): Typical sampling - filter tokens with atypical probability
- Optional parameter, provider-specific

#### Penalty Parameters

**Frequency Penalty** (-2.0 to 2.0): Penalize tokens based on frequency in prompt
- Negative = favor frequent tokens, positive = discourage repetition
- Default: 0.0 (no penalty)

**Presence Penalty** (-2.0 to 2.0): Penalize tokens based on presence in prompt
- Negative = favor mentioned tokens, positive = encourage new topics
- Default: 0.0 (no penalty)

**Length Penalty** (Double): Favor longer or shorter outputs
- >1.0 = favor longer, <1.0 = favor shorter, 1.0 = neutral
- Default: 1.0

#### Generation Control

**Stop Sequences** (List<String>): Stop generation when these strings appear
- Useful for code generation (stop at `"```"`)
- Useful for structured output (stop at delimiter)

**Seed** (Integer): Random seed for reproducibility
- `null` = random (default)
- Same seed + same prompt = same output

**N** (Integer): Number of completions to generate
- Default: 1

#### Advanced Parameters

**Logprobs** (Boolean): Include log probabilities in response
- Default: false

**Top Logprobs** (Integer): Number of top logprobs to return
- Only used if `logprobs=true`

**Echo** (Boolean): Echo back the prompt in the response
- Default: false

#### Preset Configurations

Roya provides optimized presets:

```java
// For structured extraction (low temperature, deterministic)
AIOptions.forExtraction()
// → temperature: 0.1, topP: 0.95, no penalties

// For creative tasks (high temperature, varied)
AIOptions.forCreative()
// → temperature: 0.9, topP: 1.0, topK: 50, penalties: 0.3

// For code generation (balanced, stops at code blocks)
AIOptions.forCode()
// → temperature: 0.2, topP: 0.95, stop: ["```"], maxTokens: 2000
```

#### Provider-Specific Extensions

Use `additionalOptions` for provider-specific parameters:

```java
AIOptions.builder()
    .temperature(0.7)
    .additionalOption("anthropic.max_tokens_to_sample", 500)
    .additionalOption("openai.response_format", Map.of("type", "json_object"))
    .additionalOption("cohere.num_generations", 3)
    .build();
```

**Note**: Some advanced parameters (topK, seed, logprobs, etc.) are in `AIOptions` for extensibility but may need to be passed via `additionalOptions` depending on the provider SDK version.

#### Example: Full Configuration

```java
// Highly controlled extraction with reproducibility
AIOptions precisionExtraction = AIOptions.builder()
    .model("gpt-4")
    .temperature(0.1)
    .topP(0.95)
    .topK(40)
    .frequencyPenalty(0.0)
    .presencePenalty(0.0)
    .maxTokens(500)
    .stop(List.of("\n\n\n"))  // Stop at triple newline
    .seed(42)  // Reproducible results
    .build();

ProductInfo product = ai.extract(ProductInfo.class, description, precisionExtraction);

// Creative brainstorming with variety
AIOptions creative = AIOptions.builder()
    .model("gpt-4")
    .temperature(0.9)
    .topP(1.0)
    .topK(50)
    .frequencyPenalty(0.3)  // Reduce repetition
    .presencePenalty(0.3)    // Encourage new topics
    .maxTokens(2000)
    .build();

String story = ai.ask("You are a creative writer", "Write a story", creative);
```

### Metadata in Responses

When you use `askWithMetadata()` or `extractWithMetadata()`, you get:

```java
AIResponse<ProductInfo> response = ai.extractWithMetadata(...);

// The data
ProductInfo product = response.data();

// The metadata (THE TREASURE! 🏆)
response.model();           // "gpt-3.5-turbo"
response.promptTokens();     // 45
response.completionTokens(); // 23
response.totalTokens();      // 68
response.cost();             // 0.000123 (in USD)
response.cached();           // false (was this cached?)
response.costFormatted();    // "$0.00" (readable string)
```

This metadata is **priceless** for:
- **Cost monitoring**: Track spend per request
- **Performance tuning**: See which prompts use more tokens
- **Cache effectiveness**: See which calls hit cache
- **Debugging**: Understand what the AI actually saw

### Why Caching Is Critical

Without caching:
```
Request 1: API call → $0.001
Request 2: API call → $0.001
Request 3: API call → $0.001
...
1000 requests = $1.00
```

With caching (Roya):
```
Request 1: API call → $0.001 (cache miss)
Request 2: Cache hit → $0.000 (FREE!)
Request 3: Cache hit → $0.000 (FREE!)
...
1000 requests = $0.001 (only first one costs!)
```

**Cost savings: 99.9%+** on repeated queries!

---

## Troubleshooting

### API Key Issues
```
Error: AI plugin failed to start
```
**Solution:** Set `-Dai.openai.apiKey=your-key` or set environment variable

### Cache Plugin Warnings
```
Warning: Cache plugin failed to start
```
**Solution:** This is OK - AI works without cache (just no cost savings)

### Port Already in Use
```
Port 3000 already in use
```
**Solution:** Change port in `app.listen(3000, ...)` or kill existing process

## Next Steps

After running the showcase:
1. Try different extraction types (ProductInfo, UserProfile, etc.)
2. Test caching (make same call twice, check response time)
3. Experiment with custom models/temperature
4. See how AI integrates with Database (in real apps)

---

**Ready to revolutionize how you use AI in Java?** 🚀

