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
ProductInfo product = ai.extract(ProductInfo.class, description);
// Done. One line.
```

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

