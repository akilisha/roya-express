# Phase 7: Vector Store & RAG - Design & Architecture

**Status**: Design Phase  
**Timeline**: Weeks 19-21 (April-May 2025)  
**Goal**: Make `ai().rag(question)` fully functional with real vector search

---

## Vision

**The Goal:**
```java
// Index documents (automatic chunking + embedding)
vectorStore.index("docs/", "my-knowledge-base");

// Ask questions - gets REAL answers from your docs
RAGResponse answer = ai.rag("How do I configure caching?");
// Returns: answer + citations to actual docs

// Full integration
Database db = req.get(Database.class);
VectorStore vectors = req.get(VectorStore.class);
AI ai = req.get(AI.class);

// Index database content
List<Document> docs = db.query("SELECT * FROM articles");
vectors.index(docs, "articles");

// RAG query
RAGResponse response = ai.rag("What articles discuss AI?", 
    RAGOptions.builder().topK(5).build());
```

---

## Architecture Overview

### Three-Layer Design

```
┌─────────────────────────────────────────────┐
│  RAG API (ai().rag())                        │  ← What developers use
│  - Question → Answer + Citations             │
└─────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────┐
│  RAG Pipeline                                │  ← Orchestration
│  - Query embedding                          │
│  - Vector search                             │
│  - Reranking (optional)                      │
│  - Context assembly                          │
│  - LLM generation                            │
└─────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────┐
│  Vector Store Plugin                         │  ← Storage layer
│  - Document indexing                        │
│  - Embedding generation                      │
│  - Vector search                            │
│  - Multiple backends (Qdrant, embedded)     │
└─────────────────────────────────────────────┘
```

---

## Component Design

### 1. VectorStore Interface

```java
public interface VectorStore {
    /**
     * Index documents (with automatic chunking and embedding).
     */
    CompletableFuture<Void> index(String collection, List<Document> documents);
    
    /**
     * Index files from directory.
     */
    CompletableFuture<Void> index(String collection, Path directory);
    
    /**
     * Search for similar documents.
     */
    List<DocumentMatch> search(String collection, String query, int topK);
    
    /**
     * Search with minimum score threshold.
     */
    List<DocumentMatch> search(String collection, String query, int topK, double minScore);
    
    /**
     * Delete a collection.
     */
    void deleteCollection(String collection);
    
    /**
     * Get collection stats.
     */
    CollectionStats getStats(String collection);
    
    /**
     * Provider-specific access.
     */
    <T> T provider(Class<T> providerType);
}
```

### 2. Document & DocumentMatch

```java
public record Document(
    String id,
    String content,
    Map<String, Object> metadata,  // source, title, timestamp, etc.
    Optional<float[]> embedding     // Optional - can be lazy-loaded
) {}

public record DocumentMatch(
    Document document,
    double score  // Similarity score (0.0 - 1.0)
) {}
```

### 3. Embedding Service

```java
public interface EmbeddingService {
    /**
     * Generate embedding for text.
     */
    CompletableFuture<float[]> embed(String text);
    
    /**
     * Generate embeddings in batch (more efficient).
     */
    CompletableFuture<List<float[]>> embed(List<String> texts);
    
    /**
     * Get embedding dimensions.
     */
    int dimensions();
    
    /**
     * Provider name (e.g., "openai", "local").
     */
    String provider();
}
```

### 4. Enhanced RAGResponse

```java
public record RAGResponse(
    String answer,              // Generated answer
    List<Citation> citations,    // Source documents with relevance scores
    double confidence,          // Overall confidence score
    Map<String, Object> metadata  // Additional metadata
) {
    public record Citation(
        Document document,
        double relevanceScore,
        String excerpt          // Relevant excerpt from document
    ) {}
}
```

---

## Implementation Strategy

### Phase 7.1: Foundation (Week 19)

**Goal**: Basic vector store + embedding generation

1. **VectorStore Interface** ✅
   - Define interface
   - Design document models
   - Design search API

2. **Embedding Service**
   - OpenAI embedding API integration
   - Batch embedding support
   - Caching (via Cache plugin)

3. **Embedded Vector Store** (for dev/testing)
   - In-memory vector storage
   - Simple cosine similarity
   - Perfect for local dev

**Acceptance Criteria:**
- Can index documents
- Can search by text (auto-embedding)
- Embedded backend works

### Phase 7.2: Qdrant Integration (Week 20)

**Goal**: Production-ready vector database

1. **Qdrant Client**
   - Thin wrapper around Qdrant Java SDK
   - Collection management
   - Vector insertion/search
   - Docker Compose setup

2. **Document Chunking**
   - Text chunking strategies
   - Metadata preservation
   - Overlap handling

3. **RAG Pipeline**
   - Query → embedding → search → context → LLM
   - Reranking support
   - Citation extraction

**Acceptance Criteria:**
- Qdrant integration works
- Document chunking works
- Full RAG pipeline works end-to-end

### Phase 7.3: Polish & Extensions (Week 21)

**Goal**: Production-ready features

1. **Advanced Features**
   - Reranking (cross-encoder)
   - Hybrid search (keyword + vector)
   - Multi-modal support (future)

2. **Optimization**
   - Batch operations
   - Async indexing
   - Cache embeddings

3. **Integration**
   - Database plugin integration
   - Email plugin integration
   - Metrics plugin integration

**Acceptance Criteria:**
- All features working
- Performance optimized
- Production-ready

---

## Design Decisions

### Decision 1: Embedding Provider Strategy

**Chosen**: OpenAI embeddings (MVP), designed for multiple providers

**Rationale:**
- OpenAI embeddings are high quality and widely used
- Simple API (text → vector)
- Easy to add other providers later (Anthropic, Cohere, local models)

**Future Enhancements:**
- Local embedding models (all-MiniLM, etc.)
- Provider-specific optimizations

### Decision 2: Vector Store Backend

**Chosen**: Qdrant (production) + Embedded (dev)

**Rationale:**
- Qdrant: Open-source, fast, Docker-friendly
- Embedded: Zero dependencies for dev/testing
- Both support same interface

**Alternative Considered:**
- Pinecone: Great but proprietary/expensive
- Weaviate: Good but more complex
- Qdrant: Best balance of features + simplicity

### Decision 3: Document Chunking Strategy

**Chosen**: Configurable chunking (sentence-aware, overlap support)

**Strategies:**
1. Fixed-size chunks (simple, fast)
2. Sentence-aware (better semantic boundaries)
3. Paragraph-aware (for long-form docs)

**Default**: Sentence-aware with 200-token chunks, 50-token overlap

### Decision 4: RAG Pipeline Design

**Chosen**: Modular pipeline (can swap components)

**Pipeline Steps:**
1. Query embedding (EmbeddingService)
2. Vector search (VectorStore)
3. Reranking (optional, future)
4. Context assembly (top K documents)
5. LLM generation (AI service)

**Benefits:**
- Can swap reranking algorithms
- Can use different LLMs
- Easy to optimize individual steps

### Decision 5: Metadata Handling

**Chosen**: Flexible metadata (Map<String, Object>)

**Standard Fields:**
- `source`: Document source (file path, URL, etc.)
- `title`: Document title
- `timestamp`: Indexing timestamp
- `chunkIndex`: Chunk number within document

**Benefits:**
- Flexible (can add custom fields)
- Easy to filter/search
- Good for citations

---

## File Structure

```
roya-plugins/vector/
├── src/main/java/com/akilisha/oss/roya/plugins/vector/
│   ├── VectorStore.java              # Main interface
│   ├── VectorStorePlugin.java        # Plugin registration
│   ├── VectorStoreServiceImpl.java   # Implementation
│   ├── Document.java                 # Document model
│   ├── DocumentMatch.java           # Search result
│   ├── CollectionStats.java          # Statistics
│   ├── embeddings/
│   │   ├── EmbeddingService.java     # Embedding interface
│   │   ├── OpenAIEmbeddingService.java
│   │   └── LocalEmbeddingService.java (future)
│   ├── backends/
│   │   ├── VectorBackend.java        # Backend abstraction
│   │   ├── EmbeddedVectorBackend.java # In-memory
│   │   └── QdrantVectorBackend.java  # Qdrant integration
│   └── chunking/
│       ├── ChunkingStrategy.java
│       ├── FixedSizeChunker.java
│       └── SentenceAwareChunker.java
```

---

## Integration Points

### With AI Plugin

```java
// In AIServiceImpl.rag()
public RAGResponse rag(String question, RAGOptions options) {
    VectorStore vectors = ...; // Get from services
    
    // 1. Search for relevant docs
    List<DocumentMatch> matches = vectors.search(
        "knowledge-base",
        question,
        options.topK()
    );
    
    // 2. Filter by min score
    List<Document> context = matches.stream()
        .filter(m -> m.score() >= options.minScore())
        .map(DocumentMatch::document)
        .toList();
    
    // 3. Build context prompt
    String contextText = buildContext(context);
    String prompt = buildRAGPrompt(question, contextText);
    
    // 4. Generate answer
    String answer = ask("You answer questions based on provided context.", prompt);
    
    // 5. Build citations
    List<Citation> citations = buildCitations(matches);
    
    return new RAGResponse(answer, citations, calculateConfidence(matches), Map.of());
}
```

### With Cache Plugin

```java
// Cache embeddings (expensive operation)
Optional<float[]> cached = cache.get("embedding:" + textHash, float[].class);
if (cached.isPresent()) {
    return cached.get();
}

float[] embedding = embeddingService.embed(text);
cache.set("embedding:" + textHash, embedding, Duration.ofDays(30));
```

### With Database Plugin

```java
// Index database content as documents
Database db = req.get(Database.class);
VectorStore vectors = req.get(VectorStore.class);

List<Article> articles = db.query("SELECT * FROM articles", Article.class);
List<Document> docs = articles.stream()
    .map(a -> new Document(a.id(), a.content(), Map.of("source", "database")))
    .toList();

vectors.index("articles", docs);
```

---

## Success Metrics

### Functionality
- ✅ Can index documents (files, database, API responses)
- ✅ Semantic search returns relevant results
- ✅ `ai().rag(question)` works end-to-end
- ✅ Citations link to source documents
- ✅ Multiple vector store backends work

### Performance
- Index 1000 docs in < 10 seconds
- Search latency < 100ms (excluding LLM)
- Embedding caching reduces API calls by 90%+

### Developer Experience
- One method to index: `vectorStore.index("collection", docs)`
- One method to RAG: `ai.rag(question)`
- Type-safe: Returns `RAGResponse` with citations
- Works with existing plugins (Database, Cache, etc.)

---

## Future Enhancements (Phase 7+)

### Advanced RAG
- Multi-query retrieval (rephrase query multiple ways)
- Re-ranking with cross-encoders
- Citation extraction (link answer sentences to sources)

### Advanced Features
- Hybrid search (keyword + vector)
- Multi-modal (text + images)
- Streaming RAG (stream answers as they're generated)

### Performance
- Async indexing pipeline
- Batch embedding optimization
- Vector quantization (reduce storage)

### Integration
- File upload endpoint (index uploaded docs)
- Webhook support (index on webhook events)
- Scheduled re-indexing

---

## Risks & Mitigations

### Risk 1: Embedding Costs
**Mitigation**: Aggressive caching (Cache plugin), batch operations

### Risk 2: Vector Search Accuracy
**Mitigation**: Reranking, hybrid search, configurable topK

### Risk 3: Complexity
**Mitigation**: Simple API (index + search), good defaults, embedded backend for dev

### Risk 4: Qdrant Setup Complexity
**Mitigation**: Docker Compose, embedded backend for dev, clear docs

---

## Next Steps

1. ✅ Design complete (this document)
2. ⏳ Create VectorStore interface
3. ⏳ Implement EmbeddingService (OpenAI)
4. ⏳ Implement EmbeddedVectorBackend
5. ⏳ Implement RAG pipeline in AIServiceImpl
6. ⏳ Add Qdrant integration
7. ⏳ Write comprehensive tests
8. ⏳ Create VectorStoreDemo example

**Ready to build!** 🚀

