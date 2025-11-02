# Roya AI API - Master Design Document

**Status**: Design Phase  
**Date**: November 2025  
**Vision**: Build the most expressive, yet cohesive AI API in the Java ecosystem

---

## Design Philosophy

### Core Principles

1. **Library-First, Not Provider-First**
   - Users choose abstraction layer (LangChain, Google ADK, LangGraph) based on needs
   - Provider selection is a configuration detail, not an architectural decision

2. **Expressiveness Without Complexity**
   - Rich API surface area to enable complex workflows
   - Cohesive design with clear boundaries between concerns
   - No "one-size-fits-all" forcing users into narrow patterns

3. **Composability**
   - AI capabilities compose naturally with other Roya services
   - Database + AI, Email + AI, Cache + AI just works
   - Middleware pattern applies to AI workflows

4. **Type Safety**
   - Java records are AI schemas
   - Zero boilerplate for structured extraction
   - Compile-time guarantees

---

## API Surface Area

### Primary API Structure

```java
public interface AI {
    // Core LLM operations
    LLM llm();
    
    // Embeddings & Vectors
    Embeddings embeddings();
    Vectors vectors();
    
    // RAG
    RAG rag();
    
    // Agents & Orchestration
    Agents agents();
    
    // Model Context Protocol
    MCP mcp();
    
    // NLP & Analysis
    NLP nlp();
    
    // Fine-tuning & Training
    FineTuning fineTuning();
    
    // Vision
    Vision vision();
    
    // Audio
    Audio audio();
    
    // Function Calling
    Functions functions();
    
    // Provider-specific access
    <T> T provider(Class<T> providerType);
    
    // Convenience methods (delegates to sub-APIs)
    String ask(String systemPrompt, String userMessage);
    <T> T extract(Class<T> type, String prompt);
    void stream(String systemPrompt, String userMessage, Consumer<String> onToken);
}
```

---

## API 1: LLM

**Purpose**: Core text generation, completion, and chat

```java
public interface LLM {
    // Basic completion
    String ask(String systemPrompt, String userMessage);
    String ask(String systemPrompt, String userMessage, AIOptions options);
    
    // Structured extraction
    <T> T extract(Class<T> type, String prompt);
    <T> T extract(Class<T> type, String prompt, AIOptions options);
    
    // Streaming
    void stream(String systemPrompt, String userMessage, Consumer<String> onToken);
    void stream(String systemPrompt, String userMessage, AIOptions options, Consumer<String> onToken);
    
    // Metadata-aware (tokens, cost, cache)
    AIResponse<String> askWithMetadata(String systemPrompt, String userMessage);
    AIResponse<String> askWithMetadata(String systemPrompt, String userMessage, AIOptions options);
    
    <T> AIResponse<T> extractWithMetadata(Class<T> type, String prompt);
    <T> AIResponse<T> extractWithMetadata(Class<T> type, String prompt, AIOptions options);
    
    // Multi-turn conversations
    Chat conversation();
    
    // Batch operations
    List<String> askBatch(List<Prompt> prompts);
    <T> List<T> extractBatch(Class<T> type, List<String> prompts);
    
    // Cache management
    void clearCache();
    void clearCache(String key);
    CacheStats cacheStats();
}

// Conversation management
public interface Chat {
    Chat user(String message);
    Chat system(String prompt);
    Chat assistant(String response);
    String respond();
    void respondStream(Consumer<String> onToken);
    void clear();
}
```

**Design Rationale**:
- `ask()` + `extract()` cover 80% of use cases (simple, discoverable)
- Metadata variants for observability (cost tracking, caching)
- `conversation()` for stateful multi-turn chats
- Batch operations for efficiency
- Cache management for optimization

---

## API 2: Embeddings

**Purpose**: Text-to-vector conversion, semantic search

```java
public interface Embeddings {
    // Single embedding
    float[] embed(String text);
    
    // Batch embeddings (more efficient)
    List<float[]> embed(List<String> texts);
    
    // Model selection
    Embeddings model(String modelName);
    
    // Dimensions
    int dimensions();
    
    // Similarity operations
    double similarity(float[] vec1, float[] vec2);
    List<String> findSimilar(String query, List<String> candidates, int topK);
}

// Usage:
ai.embeddings().embed("Hello world");  // [0.123, -0.456, ...]
ai.embeddings().findSimilar("cat", ["dog", "frog", "car"], 2);  // ["dog", "frog"]
```

**Design Rationale**:
- Batch operations crucial for efficiency
- Model selection allows switching between embedding providers
- Similarity operations reduce boilerplate
- Dimensions for validation

---

## API 3: Vectors

**Purpose**: Vector database operations, document indexing

```java
public interface Vectors {
    // Index operations
    void createCollection(String name, VectorCollectionConfig config);
    void deleteCollection(String name);
    List<String> listCollections();
    
    // Document indexing
    void index(String collection, VectorDoc document);
    void index(String collection, List<VectorDoc> documents);
    void indexPath(String collection, Path directory, ChunkingOptions options);
    
    // Search operations
    List<DocumentMatch> search(String collection, String query, int topK);
    List<DocumentMatch> search(String collection, float[] vector, int topK, SearchFilter filter);
    List<DocumentMatch> hybrid(String collection, String query, float[] vector, int topK);
    
    // Collection management
    void update(String collection, VectorDoc document);
    void delete(String collection, String docId);
    CollectionStats stats(String collection);
    
    // Advanced
    QueryBuilder query(String collection);
}

// Search filtering
public interface SearchFilter {
    SearchFilter where(String field, Object value);
    SearchFilter range(String field, double min, double max);
    SearchFilter limit(int max);
}

// Query builder
public interface QueryBuilder {
    QueryBuilder vector(float[] vector);
    QueryBuilder query(String text);
    QueryBuilder filter(SearchFilter filter);
    QueryBuilder topK(int k);
    List<DocumentMatch> execute();
}

// Advanced search
public interface HybridSearch {
    List<DocumentMatch> execute();
}

// Usage:
ai.vectors().index("kb", new VectorDoc("doc1", "content", Map.of("author", "Alice")));
List<DocumentMatch> results = ai.vectors().search("kb", "What is Java?", 5);
CollectionStats stats = ai.vectors().stats("kb");
```

**Design Rationale**:
- CRUD operations for collections and documents
- Chunking support for large document indexing
- Hybrid search (keyword + semantic) for better results
- Query builder for complex searches
- Filtering for metadata-based search
- Stats for observability

---

## API 4: RAG

**Purpose**: Retrieval-Augmented Generation with citations

```java
public interface RAG {
    // Basic RAG
    RAGResponse ask(String question);
    RAGResponse ask(String question, RAGOptions options);
    
    // Streaming RAG
    void askStream(String question, Consumer<String> onToken);
    void askStream(String question, RAGOptions options, Consumer<String> onToken);
    
    // Manual retrieval + generation
    List<DocumentMatch> retrieve(String question, RAGOptions options);
    String generate(String context, String question);
    
    // Multi-step RAG
    MultiStepRAG multiStep(String question);
}

// RAG options
public record RAGOptions(
    int topK,                              // Number of docs to retrieve
    double minScore,                       // Similarity threshold
    boolean rerank,                        // Rerank results
    String collection,                     // Vector collection
    SearchFilter filter,                   // Metadata filter
    AIOptions llmOptions,                  // LLM options
    String systemPrompt                    // Custom RAG prompt
) {
    public static RAGOptions defaults() { ... }
    public static Builder builder() { ... }
}

// Multi-step RAG
public interface MultiStepRAG {
    MultiStepRAG step(String instruction);  // Add step
    MultiStepRAG refine();                  // Refine answer
    MultiStepRAG verify();                  // Verify citations
    RAGResponse execute();
}

// Usage:
RAGResponse response = ai.rag().ask("What is Roya?");
// response.answer(), response.sources(), response.citations()

// Advanced
RAGOptions opts = RAGOptions.builder()
    .topK(10)
    .rerank(true)
    .filter(SearchFilter.where("author", "Alice"))
    .build();
RAGResponse response = ai.rag().ask("Explain X", opts);

// Multi-step
MultiStepRAG rag = ai.rag().multiStep("What is AI?");
rag.step("Retrieve technical docs")
   .refine()
   .verify();
RAGResponse response = rag.execute();
```

**Design Rationale**:
- Single `ask()` method covers 90% of use cases
- Options pattern for power users
- Streaming for real-time responses
- Multi-step for complex workflows
- Manual retrieve+generate for custom pipelines
- Citations built-in

---

## API 5: Agents

**Purpose**: Autonomous agents with tools, planning, and execution

```java
public interface Agents {
    // Agent creation
    AgentAgent create(Consumer<AgentBuilder> config);
    
    // Pre-built agent types
    AgentAgent research();
    AgentAgent code();
    AgentAgent data();
    AgentAgent personalAssistant();
    
    // Multi-agent orchestration
    Orchestrator orchestrate();
}

// Agent builder
public interface AgentBuilder {
    AgentBuilder name(String name);
    AgentBuilder goal(String goal);
    AgentBuilder systemPrompt(String prompt);
    AgentBuilder model(AI model);
    AgentBuilder tools(List<Tool> tools);
    AgentBuilder maxIterations(int n);
    AgentBuilder allowDelegation(boolean allow);
}

// Agent execution
public interface AgentAgent {
    AgentResult run(String task);
    void runAsync(String task, Consumer<AgentResult> callback);
    void stream(String task, Consumer<AgentStep> onStep);
    
    // State management
    void clearMemory();
    AgentMemory getMemory();
    void setMemory(AgentMemory memory);
}

// Tools
public interface Tool {
    String name();
    String description();
    Map<String, Object> parameters();
    ToolResult execute(Map<String, Object> params);
}

// Orchestration
public interface Orchestrator {
    Orchestrator addAgent(String name, AgentAgent agent);
    Orchestrator workflow(Consumer<WorkflowBuilder> config);
    OrchestratorResult execute(String input);
}

// Usage:
// Simple agent
AgentAgent assistant = ai.agents().create(builder -> {
    builder.goal("Help users with questions")
           .tools(List.of(webSearch, calculator, dbQuery));
});
AgentResult result = assistant.run("What's the weather?");

// Pre-built agents
AgentAgent researcher = ai.agents().research();
AgentResult research = researcher.run("Analyze Q4 sales data");

// Multi-agent
Orchestrator team = ai.agents().orchestrate();
team.addAgent("researcher", ai.agents().research());
team.addAgent("analyst", ai.agents().data());
team.addAgent("writer", ai.agents().personalAssistant());

OrchestratorResult report = team.execute("Create Q4 sales report");
```

**Design Rationale**:
- Builder pattern for flexible configuration
- Pre-built agents for common use cases
- Tool abstraction for extensibility
- Orchestration for multi-agent workflows
- Memory management for stateful agents
- Streaming for real-time execution

---

## API 6: MCP (Model Context Protocol)

**Purpose**: Anthropic's protocol for tool and context management

```java
public interface MCP {
    // Tool registration
    void registerTool(String name, Tool tool);
    void registerTool(Tool tool);
    void unregisterTool(String name);
    
    // Context management
    void addContext(String name, ContextProvider provider);
    void removeContext(String name);
    
    // Execution
    MCPResult execute(String request);
    void executeStream(String request, Consumer<MCPUpdate> callback);
    
    // Discovery
    List<MCPTool> listTools();
    List<ContextProvider> listContexts();
}

// Context provider
public interface ContextProvider {
    String name();
    List<ContextResource> resources();
    ContextResource get(String resourceId);
}

// Usage:
ai.mcp().registerTool("search", webSearchTool);
ai.mcp().addContext("docs", docsContextProvider);
MCPResult result = ai.mcp().execute("Search for Java best practices");
```

**Design Rationale**:
- Tool registration for dynamic capabilities
- Context management for external resources
- Streaming for real-time updates
- Discovery for introspection

---

## API 7: NLP

**Purpose**: Text analysis, classification, summarization

```java
public interface NLP {
    // Sentiment analysis
    SentimentResult sentiment(String text);
    List<SentimentResult> sentimentBatch(List<String> texts);
    
    // Classification
    <T extends Enum<T>> T classify(String text, Class<T> categories);
    <T extends Enum<T>> Map<T, Double> classifyProbabilities(String text, Class<T> categories);
    
    // Summarization
    String summarize(String text);
    String summarize(String text, SummarizeOptions options);
    
    // Named Entity Recognition
    List<Entity> entities(String text);
    List<Entity> entities(String text, EntityType... types);
    
    // Keywords & topics
    List<String> keywords(String text, int topK);
    List<Topic> topics(String text, int topK);
    
    // Text similarity
    double similarity(String text1, String text2);
    
    // Language detection
    Language detectLanguage(String text);
    
    // Translation
    String translate(String text, Language target);
}

// Usage:
SentimentResult sentiment = ai.nlp().sentiment("This product is amazing!");
String summary = ai.nlp().summarize(longDocument);
List<Entity> people = ai.nlp().entities(text, EntityType.PERSON);
List<String> keyTerms = ai.nlp().keywords(text, 10);
```

**Design Rationale**:
- Common NLP tasks as first-class operations
- Batch operations for efficiency
- Type-safe classification with enums
- Extensible with options patterns
- Language detection and translation

---

## API 8: Fine-Tuning

**Purpose**: Model training, fine-tuning, evaluation

```java
public interface FineTuning {
    // Fine-tune a model
    TrainingJob fineTune(TrainingDataset dataset, FineTuneConfig config);
    
    // Monitor training
    TrainingStatus status(String jobId);
    void waitForCompletion(String jobId);
    
    // Deploy fine-tuned models
    void deploy(String jobId, DeployConfig config);
    void undeploy(String deploymentId);
    
    // Evaluation
    EvaluationResult evaluate(String modelId, EvaluationDataset dataset);
    
    // Training data management
    DatasetManager datasets();
}

// Usage:
TrainingDataset dataset = ai.fineTuning().datasets().create("my-dataset", trainingData);
FineTuneConfig config = FineTuneConfig.builder()
    .model("gpt-3.5-turbo")
    .epochs(3)
    .learningRate(1e-5)
    .build();

TrainingJob job = ai.fineTuning().fineTune(dataset, config);
job.waitForCompletion();
ai.fineTuning().deploy(job.id(), DeployConfig.defaults());
```

**Design Rationale**:
- Full lifecycle: train → deploy → evaluate
- Async training with status monitoring
- Dataset management
- Evaluation support

---

## API 9: Vision

**Purpose**: Image analysis, generation, OCR

```java
public interface Vision {
    // Image analysis
    VisionResult analyze(Image image, String prompt);
    VisionResult analyze(Image image, VisionOptions options);
    
    // Image generation
    Image generate(String prompt);
    Image generate(String prompt, GenerateOptions options);
    
    // Image editing
    Image edit(Image image, String instruction);
    Image edit(Image image, String instruction, EditMask mask);
    
    // Variants
    Image variant(Image image);
    List<Image> variants(Image image, int count);
    
    // OCR
    String extractText(Image image);
    List<BoundingBox> extractTextWithLayout(Image image);
}

// Usage:
VisionResult analysis = ai.vision().analyze(image, "Describe what you see");
Image generated = ai.vision().generate("A sunset over mountains");
String text = ai.vision().extractText(scannedDocument);
```

**Design Rationale**:
- Core vision tasks as first-class operations
- Image generation and editing
- OCR for document processing
- Flexible options patterns

---

## API 10: Audio

**Purpose**: Speech-to-text, text-to-speech, audio analysis

```java
public interface Audio {
    // Speech-to-text
    String transcribe(AudioFile audio);
    TranscriptionResult transcribeDetailed(AudioFile audio);
    
    // Text-to-speech
    AudioFile speak(String text);
    AudioFile speak(String text, VoiceOptions options);
    
    // Voice cloning
    void createVoice(String name, AudioFile sample);
    void deleteVoice(String name);
    AudioFile speakWithVoice(String text, String voiceName);
    
    // Audio analysis
    AudioAnalysis analyze(AudioFile audio);
}

// Usage:
String transcript = ai.audio().transcribe(recording);
AudioFile speech = ai.audio().speak("Hello world");
AudioAnalysis analysis = ai.audio().analyze(audio);
```

**Design Rationale**:
- Bidirectional speech processing
- Voice cloning for brand consistency
- Audio analysis for metadata

---

## API 11: Functions

**Purpose**: Function calling, tool use, code generation

```java
public interface Functions {
    // Function calling
    FunctionCall invoke(String name, Map<String, Object> args);
    <T> T invoke(Class<T> functionClass, String method, Map<String, Object> args);
    
    // Tool definition
    void define(Tool tool);
    void defineBatch(List<Tool> tools);
    
    // Code generation
    CodeSnippet generateCode(String language, String specification);
    TestCode generateTests(String language, CodeSnippet code);
    
    // Documentation generation
    String generateDocs(String language, CodeSnippet code);
}

// Usage:
FunctionCall result = ai.functions().invoke("calculateTax", Map.of("amount", 1000));
CodeSnippet code = ai.functions().generateCode("java", "Create a REST controller");
```

**Design Rationale**:
- Function calling for tool use
- Code generation for productivity
- Documentation generation

---

## Library-First Architecture

### The Problem

The current implementation is **provider-first**: it forces users into provider abstractions (OpenAI, Anthropic, etc.) and hardcodes defaults. This violates the design intent.

**Instead**, users should choose their **abstraction layer** (LangChain, Google ADK, LangGraph) based on their use case, not their provider.

### Why Library-First?

**Library Choice = Use Case Choice:**
- **LangChain**: Chains, RAG workflows, tool integration, multi-step reasoning
- **Google ADK**: Google ecosystem integration, Gemini-first, Vertex AI
- **LangGraph**: Complex agent orchestration, multi-agent workflows, state machines

Each library offers different capabilities. Users shouldn't be forced into provider abstractions when they need library-specific features.

### Library Adapters

Each library adapter implements the **full** Roya `AI` interface:

```java
// LangChain adapter
public class LangChainAdapter implements AI {
    private final ChatLanguageModel chatModel;
    private final EmbeddingModel embeddingModel;
    
    @Override
    public LLM llm() {
        return new LangChainLLM(chatModel);
    }
    
    @Override
    public Embeddings embeddings() {
        return new LangChainEmbeddings(embeddingModel);
    }
    
    @Override
    public RAG rag() {
        return new LangChainRAG(chatModel, vectorStore);
    }
    
    @Override
    public Agents agents() {
        return new LangChainAgents(agentExecutor);
    }
    
    @Override
    public MCP mcp() {
        return new LangChainMCP(toolExecutor);
    }
    
    // ... implement all AI interfaces
}

// Google ADK adapter
public class GoogleADKAdapter implements AI {
    private final GenerativeModel model;
    private final VertexAI vertexAI;
    
    @Override
    public LLM llm() {
        return new GoogleADKLLM(model);
    }
    
    @Override
    public Vision vision() {
        return new GoogleADKVision(vertexAI);
    }
    
    @Override
    public Audio audio() {
        return new GoogleADKAudio(vertexAI);
    }
    
    // ... implement all AI interfaces
}

// LangGraph adapter
public class LangGraphAdapter implements AI {
    private final GraphExecutor graph;
    
    @Override
    public Agents agents() {
        return new LangGraphAgents(graph);
    }
    
    @Override
    public MCP mcp() {
        return new LangGraphMCP(graph);
    }
    
    // ... implement all AI interfaces
}
```

### Provider Configuration

Libraries handle their own provider configuration internally:

```java
// LangChain can use ANY provider
LangChainAdapter langchain = LangChainAdapter.builder()
    .chatModel(OpenAIChatModel.builder().apiKey(apiKey).build())
    .embeddingModel(OpenAIEmbeddingModel.builder().apiKey(apiKey).build())
    .vectorStore(QdrantClient.create())
    .build();

// Or with Anthropic
LangChainAdapter langchain = LangChainAdapter.builder()
    .chatModel(AnthropicChatModel.builder().apiKey(apiKey).build())
    .embeddingModel(OpenAIEmbeddingModel.builder().apiKey(apiKey).build())
    .build();

// Google ADK uses Gemini
GoogleADKAdapter google = GoogleADKAdapter.builder()
    .project("my-project")
    .location("us-central1")
    .credentials(credentials)
    .build();

// LangGraph orchestrates multiple models
LangGraphAdapter graph = LangGraphAdapter.builder()
    .addModel("primary", OpenAIChatModel.builder().apiKey(openaiKey).build())
    .addModel("secondary", AnthropicChatModel.builder().apiKey(anthropicKey).build())
    .build();
```

### Plugin Registration

```java
// Option 1: Simple configuration (chooses library)
public class AIPlugin implements RoyaPlugin {
    @Override
    public void register(Services services) {
        services.singleton(AI.class, () -> {
            String library = System.getProperty("ai.library", 
                System.getenv().getOrDefault("AI_LIBRARY", "langchain"));
            
            AILibraryAdapter adapter = AILibraryFactory.create(library);
            return adapter.create(buildConfig());
        });
    }
}

// Option 2: Direct registration (user controls everything)
AI langchain = LangChainAdapter.builder()
    .provider("anthropic", apiKey)
    .provider("openai", embeddingKey)
    .vectorStore("qdrant", "http://localhost:6333")
    .cache(true)
    .build();

app.use(ai);
```

### Usage

**Same API regardless of library:**

```java
AI ai = req.get(AI.class);

// These work identically across all libraries
String answer = ai.llm().ask("You are helpful", "What is Java?");
List<DocumentMatch> docs = ai.vectors().search("kb", "Roya framework", 5);
AgentResult result = ai.agents().research().run("Analyze Q4 sales");
RAGResponse rag = ai.rag().ask("What is vector search?");
SentimentResult sentiment = ai.nlp().sentiment("Great product!");
```

---

## Design Decisions

### 1. Why So Many Sub-APIs?

**Answer**: Different workflows need different abstractions. A RAG workflow is fundamentally different from an agent workflow. By providing dedicated APIs for each concern, we:
- Make intent clear in code
- Enable optimizations specific to each workflow
- Reduce cognitive load
- Allow parallel development

### 2. Why Not Just Provider SDKs?

**Answer**: Provider SDKs are too low-level. Users shouldn't have to:
- Learn provider-specific APIs
- Handle rate limits
- Implement caching
- Manage vector stores
- Build RAG pipelines from scratch

Roya AI provides **workflow-level** abstractions.

### 3. How Do You Avoid Feature Creep?

**Answer**: Every API method must:
1. Support a common workflow (not a niche use case)
2. Have a clear, simple signature
3. Compose with other Roya services
4. Be testable and mockable

### 4. What About Performance?

**Answer**: Each adapter is responsible for:
- Batch operations
- Streaming when appropriate
- Caching (via Cache plugin)
- Parallel execution

### 5. How Do You Keep It Simple?

**Answer**: 
- 80% of use cases covered by `llm().ask()` and `extract()`
- Power users can access advanced APIs
- Each API has clear boundaries
- Examples and demos for common workflows

---

## Migration Path

### Phase 1: Core APIs (Current)
- [x] LLM (ask, extract, stream)
- [x] Embeddings
- [x] Vectors (basic)
- [x] RAG (basic)

### Phase 2: Advanced Features
- [ ] Agents (with tools)
- [ ] MCP integration
- [ ] NLP (sentiment, NER, etc.)
- [ ] Multi-step RAG

### Phase 3: Specialized APIs
- [ ] Vision
- [ ] Audio
- [ ] Fine-tuning
- [ ] Functions

### Phase 4: Library Adapters
- [ ] LangChain adapter
- [ ] Google ADK adapter
- [ ] LangGraph adapter

### Phase 5: Optimization
- [ ] Advanced caching strategies
- [ ] Parallel execution
- [ ] Cost optimization
- [ ] Performance tuning

---

## Success Criteria

1. **Expressiveness**: Users can build complex AI workflows without fighting the API
2. **Simplicity**: 80% of use cases require minimal API surface
3. **Performance**: No significant overhead vs. direct provider SDK usage
4. **Composability**: AI + Database + Email works seamlessly
5. **Extensibility**: New libraries/providers easy to add

---

## Example: Complex Workflow

```java
// Multi-agent RAG workflow with NLP and MCP tools

// 1. Setup
ai.vectors().indexPath("docs", Path.of("./documentation"), ChunkingOptions.fixed(800, 200));
ai.mcp().registerTool("calculator", new CalculatorTool());
ai.mcp().registerTool("weather", new WeatherTool());

// 2. Create specialized agents
AgentAgent researcher = ai.agents().create(builder -> {
    builder.goal("Research topics using RAG")
           .tools(ai.mcp().listTools());
});

AgentAgent analyst = ai.agents().create(builder -> {
    builder.goal("Analyze sentiment and trends")
           .tools(List.of());
});

// 3. Orchestrate multi-agent workflow
OrchestratorResult result = ai.agents().orchestrate()
    .addAgent("researcher", researcher)
    .addAgent("analyst", analyst)
    .execute("Analyze customer feedback trends");

// 4. Process results with NLP
SentimentResult overall = ai.nlp().sentiment(result.summary());
List<Topic> themes = ai.nlp().topics(result.summary(), 5);

// 5. Generate report
String report = ai.llm().extract(Report.class, result.toPrompt()).toMarkdown();

// All in one flow, type-safe, no boilerplate!
```

---

**This is the vision. Ready to build it?**
