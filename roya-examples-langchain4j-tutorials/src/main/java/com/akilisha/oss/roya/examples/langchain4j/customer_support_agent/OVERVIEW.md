# Customer Support Agent - Overview

## What We Created

A **Customer Support Agent** powered by **RAG (Retrieval-Augmented Generation)** that:
- Answers customer questions using document context
- Maintains conversation history across multiple turns
- Uses LangChain4j AI Services with ContentRetriever
- Provides both HTTP API and interactive console interfaces

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│  Customer Support Agent                                 │
├─────────────────────────────────────────────────────────┤
│  1. Document Ingestion:                                │
│     • Loads text document                               │
│     • Splits into chunks (300 chars, recursive)        │
│     • Generates embeddings (AllMiniLmL6V2)             │
│     • Stores in InMemoryEmbeddingStore                  │
│                                                          │
│  2. ContentRetriever (RAG):                             │
│     • Searches embeddings for relevant context          │
│     • Returns top 1 match (minScore: 0.6)             │
│                                                          │
│  3. AI Service with RAG:                               │
│     • CustomerSupportAgentService interface            │
│     • @SystemMessage: Instructions                      │
│     • @UserMessage: Question                            │
│     • ContentRetriever auto-injects context            │
│                                                          │
│  4. ChatMemory:                                        │
│     • MessageWindowChatMemory (30 messages)            │
│     • Maintains conversation context                   │
└─────────────────────────────────────────────────────────┘
```

## Key Components

### 1. AI Service Interface

```java
interface CustomerSupportAgentService {
    @SystemMessage("You are a customer support agent. " +
            "Answer questions based only on the information provided in the context. " +
            "If the answer is not in the context, say so.")
    String answer(@UserMessage String question);
}
```

**Key Points:**
- Uses LangChain4j's declarative AI Service pattern
- `@SystemMessage` defines the agent's role and behavior
- `@UserMessage` marks the user's question parameter
- ContentRetriever automatically injects relevant document context
- ChatMemory maintains conversation history

### 2. Document Ingestion Pipeline

```java
// Step 1: Load document
Document document = loadDocument(documentPath, new TextDocumentParser());

// Step 2: Split into chunks
DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);

// Step 3: Create ingestor
EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
    .documentSplitter(splitter)
    .embeddingModel(embeddingModel)
    .embeddingModel(embeddingModel)
    .embeddingStore(embeddingStore)
    .build();

// Step 4: Ingest (split + embed + store)
ingestor.ingest(document);
```

**Process:**
1. **Load**: Reads text file using `TextDocumentParser`
2. **Split**: Recursive splitting at 300 characters (no overlap)
3. **Embed**: Converts text chunks to vectors using `AllMiniLmL6V2EmbeddingModel`
4. **Store**: Saves embeddings in `InMemoryEmbeddingStore`

### 3. ContentRetriever Setup

```java
ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
    .embeddingStore(embeddingStore)
    .embeddingModel(embeddingModel)
    .maxResults(1)      // Return top 1 relevant document
    .minScore(0.6)      // Minimum similarity score (0.0-1.0)
    .build();
```

**How It Works:**
- When a question is asked, the question is embedded
- Similarity search finds the most relevant document chunks
- Only chunks with similarity ≥ 0.6 are returned
- Top 1 result is injected as context into the LLM prompt

### 4. AI Service with RAG + Memory

```java
CustomerSupportAgentService agent = ai.aiService(
    CustomerSupportAgentService.class, 
    builder -> {
        builder.chatMemory(chatMemory);           // Conversation history
        builder.contentRetriever(contentRetriever); // RAG context injection
    }
);
```

**What Happens:**
1. User asks a question
2. ContentRetriever finds relevant document chunks
3. Context is injected into the prompt automatically
4. LLM generates answer based on context + conversation history
5. Response is returned to user

## How It Was Put Together

### Step 1: Dependencies

Added to `build.gradle`:
```gradle
implementation "dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2:1.8.0-beta15"
```

This provides:
- `AllMiniLmL6V2EmbeddingModel` - Local embedding model (no API key needed)
- `InMemoryEmbeddingStore` - In-memory vector store

### Step 2: Document Preparation

Created `miles-of-smiles-terms-of-use.txt` with customer support content:
- Refund policy
- Returns policy
- Shipping information
- Warranty details
- Contact information

### Step 3: Embedding Model Selection

```java
// Try to get from AI provider first
EmbeddingModel embeddingModel = adapter.provider(EmbeddingModel.class);

// Fallback to local model (no API key needed)
if (embeddingModel == null) {
    embeddingModel = new AllMiniLmL6V2EmbeddingModel();
}
```

**Why This Approach:**
- Uses configured embedding model if available (via OPENAI_API_KEY)
- Falls back to local model if not configured
- No reflection - direct instantiation

### Step 4: ChatMemory Selection

```java
ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(30);
```

**Why MessageWindowChatMemory:**
- Simpler than TokenWindowChatMemory (no tokenizer needed)
- Maintains last 30 messages
- No reflection required
- Good enough for demo purposes

### Step 5: Fixed Provider Access

Updated `UnifiedAIService.provider()` to return `LangChainAdapter`:

```java
// Special case: return LangChainAdapter instance if requested
if (providerType.isInstance(langChain)) {
    return providerType.cast(langChain);
}
```

This allows: `ai.provider(LangChainAdapter.class)` to work correctly.

### Step 6: Document Path Resolution

Checks multiple locations:
1. Command-line argument (if provided)
2. `miles-of-smiles-terms-of-use.txt` (root)
3. `roya-examples-langchain4j-tutorials/miles-of-smiles-terms-of-use.txt`
4. `src/main/resources/miles-of-smiles-terms-of-use.txt`

## Step-by-Step Validation Process

### Step 1: Verify Server Started

**Check server is running:**
```bash
curl http://localhost:3015/
```

**Expected Response:**
```json
{
  "application": "Customer Support Agent",
  "description": "AI-powered customer support agent with RAG",
  "features": [
    "RAG (Retrieval-Augmented Generation) - answers based on document context",
    "TokenWindowChatMemory - maintains conversation history (1000 tokens)",
    "Document ingestion - automatically chunks and embeds documents",
    "In-memory embedding store - fast retrieval for demo purposes"
  ],
  "endpoints": {
    "POST /ask": "Ask a question (stateless)",
    "POST /chat": "Chat with agent (maintains conversation)",
    "GET /": "API documentation"
  }
}
```

### Step 2: Test Document Ingestion

**Verify document was loaded:**
Look for startup message:
```
✓ Document ingested: miles-of-smiles-terms-of-use.txt
  Segments created and embedded into store
```

### Step 3: Test Stateless Endpoint (`/ask`)

**Ask a question about refunds:**
```bash
curl -X POST http://localhost:3015/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "What is your refund policy?"}'
```

**Expected Response:**
```json
{
  "question": "What is your refund policy?",
  "answer": "We offer full refunds within 30 days of purchase. To request a refund, please contact our support team with your order number. Refunds will be processed within 5-7 business days.",
  "note": "Answer generated using RAG (Retrieval-Augmented Generation)"
}
```

**Validation Points:**
- ✅ Answer comes from document (not hallucinated)
- ✅ Answer is specific and accurate
- ✅ Response includes RAG note

### Step 4: Test Conversation Endpoint (`/chat`)

**Start a conversation:**
```bash
curl -X POST http://localhost:3015/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "Hello, I have a question about returns"}'
```

**Expected Response:**
```json
{
  "question": "Hello, I have a question about returns",
  "answer": "I'd be happy to help you with returns. What specifically would you like to know?",
  "note": "Conversation maintained using ChatMemory"
}
```

**Continue conversation:**
```bash
curl -X POST http://localhost:3015/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "How long do I have to return items?"}'
```

**Expected Response:**
```json
{
  "question": "How long do I have to return items?",
  "answer": "You have 30 days to return items in their original packaging. Return shipping costs are the responsibility of the customer unless the item was defective.",
  "note": "Conversation maintained using ChatMemory"
}
```

**Validation Points:**
- ✅ Agent remembers previous context ("returns" conversation)
- ✅ Answer is context-aware
- ✅ Conversation flows naturally

### Step 5: Test Question Outside Document Scope

**Ask about something not in the document:**
```bash
curl -X POST http://localhost:3015/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "What is your store hours?"}'
```

**Expected Response:**
```json
{
  "question": "What is your store hours?",
  "answer": "I don't have information about store hours in the provided context. However, our support hours are Monday-Friday, 9 AM - 5 PM EST. You can contact us at support@milesofsmiles.com or call 1-800-MILES-SMILE.",
  "note": "Answer generated using RAG (Retrieval-Augmented Generation)"
}
```

**Validation Points:**
- ✅ Agent acknowledges missing information
- ✅ Provides relevant alternative (support hours)
- ✅ Doesn't hallucinate fake store hours

### Step 6: Test Interactive Console Mode

**Start the server** (if not already running):
```bash
./gradlew :roya-examples-langchain4j-tutorials:runCustomerSupportAgent
```

**Use interactive mode:**
- Type questions and press Enter
- Agent responds with answers
- Type `exit` to quit

**Example Session:**
```
You: What is your refund policy?
Agent: We offer full refunds within 30 days of purchase. To request a refund, please contact our support team with your order number. Refunds will be processed within 5-7 business days.

You: How do I contact support?
Agent: You can contact our support team by email at support@milesofsmiles.com or by calling 1-800-MILES-SMILE. Our support hours are Monday-Friday, 9 AM - 5 PM EST.

You: exit
```

**Validation Points:**
- ✅ Interactive mode works
- ✅ Conversation history maintained
- ✅ Questions answered accurately

## Testing Checklist

- [ ] Server starts successfully
- [ ] Document ingestion completes (check startup logs)
- [ ] `/ask` endpoint returns accurate answers
- [ ] `/chat` endpoint maintains conversation
- [ ] Questions outside document scope are handled gracefully
- [ ] Interactive console mode works
- [ ] Multiple questions in a row work correctly
- [ ] Answers are based on document content (not hallucinated)

## Technical Details

### Embedding Model
- **Primary**: Uses configured embedding model from AI plugin (if available)
- **Fallback**: `AllMiniLmL6V2EmbeddingModel` (local, no API key needed)
- **Dimension**: 384 dimensions
- **Type**: ONNX-based local model

### Document Splitting
- **Method**: Recursive splitting
- **Size**: 300 characters per chunk
- **Overlap**: 0 characters

### RAG Configuration
- **Max Results**: 1 (top match only)
- **Min Score**: 0.6 (similarity threshold)
- **Store Type**: InMemoryEmbeddingStore (non-persistent)

### ChatMemory
- **Type**: MessageWindowChatMemory
- **Capacity**: 30 messages
- **Scope**: Per-agent instance (shared across HTTP requests)

## Key Learnings

1. **RAG Integration**: ContentRetriever automatically injects context into AI Service prompts
2. **No Reflection**: Direct instantiation of `AllMiniLmL6V2EmbeddingModel` works perfectly
3. **Provider Pattern**: Fixed `UnifiedAIService.provider()` to return adapters correctly
4. **Document Path Resolution**: Multiple fallback locations for flexibility
5. **Memory Management**: `MessageWindowChatMemory` is simpler than `TokenWindowChatMemory` for demos

## Next Steps / Enhancements

- [ ] Add persistent embedding store (Qdrant) for production
- [ ] Support multiple documents
- [ ] Add document update/refresh capability
- [ ] Implement TokenWindowChatMemory for token-based limits
- [ ] Add logging/metrics for RAG performance
- [ ] Support multiple conversations (per-user memory)
- [ ] Add document citation in responses
- [ ] Implement streaming responses

## Related Files

- **Main Class**: `CustomerSupportAgent.java`
- **Document**: `miles-of-smiles-terms-of-use.txt`
- **Build Config**: `roya-examples-langchain4j-tutorials/build.gradle`
- **Core AI**: `roya-plugins/ai/src/main/java/com/akilisha/oss/roya/plugins/ai/`
- **LangChain Adapter**: `roya-plugins/ai/src/main/java/com/akilisha/oss/roya/plugins/ai/langchain/LangChainAdapter.java`

## References

- [LangChain4j AI Services](https://docs.langchain4j.dev/ai-services)
- [LangChain4j RAG](https://docs.langchain4j.dev/rag)
- [Customer Support Agent Example](https://github.com/langchain4j/langchain4j-examples/tree/main/customer-support-agent-example)
