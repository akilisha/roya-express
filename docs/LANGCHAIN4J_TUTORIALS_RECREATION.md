# LangChain4j Tutorials Recreation Project

> **Goal**: Recreate LangChain4j tutorials using Roya's workflow-first approach to showcase the framework's capabilities

## Reference

**Source**: [LangChain4j Examples - Tutorials](https://github.com/langchain4j/langchain4j-examples/tree/main/tutorials/src/main/java)

---

## Tutorial Structure

The LangChain4j tutorials directory contains educational examples covering:
- Basic LLM usage
- Streaming responses
- Structured outputs
- RAG (Retrieval-Augmented Generation)
- Tools and function calling
- Agents
- Memory/conversation
- Embeddings
- Vector stores

---

## Project Structure

```
roya-examples-langchain4j-tutorials/
├── src/main/java/com/akilisha/oss/roya/examples/langchain4j/
│   ├── tutorial01_basic_llm/
│   │   └── Tutorial01BasicLLM.java ✅
│   ├── tutorial02_model_parameters/
│   ├── tutorial03_image_generation/
│   ├── tutorial04_prompt_templates/
│   ├── tutorial05_streaming/
│   ├── tutorial06_memory/
│   ├── tutorial07_few_shot/
│   ├── tutorial08_ai_services/
│   ├── tutorial09_persistent_memory/
│   ├── tutorial10_tools/
│   ├── tutorial11_dynamic_tools/
│   └── tutorial12_rag_documents/
├── README.md
└── build.gradle
```

**Note**: This is a **separate module** (`roya-examples-langchain4j-tutorials`) that mirrors the structure of the official LangChain4j tutorials directory.

---

## Tutorial Mapping

### Tutorial 01: Basic LLM Usage
**LangChain4j Approach**: Direct `ChatModel` usage
**Roya Approach**: `ai.llm().ask()` or workflow with `.llm()` node

**Demonstrates**:
- Simple chat completion
- System/user prompts
- Temperature and other options

**Roya Advantages**:
- Type-safe API
- Automatic caching
- Cost tracking built-in

---

### Tutorial 02: Streaming Responses
**LangChain4j Approach**: `StreamingChatModel` with callbacks
**Roya Approach**: `ai.llm().stream()` or workflow with streaming node

**Demonstrates**:
- Token-by-token streaming
- Streaming callbacks
- Real-time response handling

**Roya Advantages**:
- Same API pattern as non-streaming
- Built-in error handling

---

### Tutorial 03: Structured Outputs
**LangChain4j Approach**: JSON mode + manual parsing
**Roya Approach**: `ai.llm().extract(Record.class, prompt)`

**Demonstrates**:
- Type-safe extraction
- Record-based schemas
- Automatic JSON parsing

**Roya Advantages**:
- Zero boilerplate
- Compile-time type safety
- No manual JSON parsing

---

### Tutorial 04: RAG (Retrieval-Augmented Generation)
**LangChain4j Approach**: Manual embedding + vector search + LLM
**Roya Approach**: `ai.ragApi().ask(question)` or workflow with `.rag()` node

**Demonstrates**:
- Document indexing
- Semantic search
- Context-augmented generation

**Roya Advantages**:
- One-line RAG pipeline
- Automatic collection management
- Built-in reranking

---

### Tutorial 05: Tools and Function Calling
**LangChain4j Approach**: Manual `ToolSpecification` + execution
**Roya Approach**: `.llmWithTools()` or `.mcp()` for discoverable tools

**Demonstrates**:
- Tool definition
- Function calling
- Tool execution

**Roya Advantages**:
- MCP integration for discoverable tools
- Automatic tool discovery
- Workflow integration

---

### Tutorial 06: Agents
**LangChain4j Approach**: Manual agent orchestration
**Roya Approach**: `.agents()` workflow node

**Demonstrates**:
- Agent reasoning
- Tool selection
- Multi-step execution

**Roya Advantages**:
- Workflow-first agent design
- Parallel execution support
- Built-in error handling

---

### Tutorial 07: Memory/Conversation
**LangChain4j Approach**: Manual `ChatMemory` management
**Roya Approach**: `.llmWithMemory()` or workflow memory integration

**Demonstrates**:
- Conversation history
- Context management
- Memory persistence

**Roya Advantages**:
- Workflow-scoped memory
- Automatic context management

---

### Tutorial 08: Embeddings
**LangChain4j Approach**: Direct `EmbeddingModel` usage
**Roya Approach**: `ai.embeddings().embed()` or workflow with `.embeddings()` node

**Demonstrates**:
- Text embedding generation
- Batch embeddings
- Embedding dimensions

**Roya Advantages**:
- Unified API
- Automatic batching

---

### Tutorial 09: Vector Store
**LangChain4j Approach**: Manual `EmbeddingStore` management
**Roya Approach**: `ai.vectors().indexPath()` or workflow with `.vectors()` node

**Demonstrates**:
- Vector storage
- Similarity search
- Collection management

**Roya Advantages**:
- Automatic collection creation
- Built-in chunking strategies
- One-line indexing

---

## Implementation Plan

### Phase 1: Basic Tutorials (Layups) ✅
1. ✅ Tutorial 01: Basic LLM Usage - **CREATED** (`Tutorial01BasicLLM.java`)
   - Recreates `_00_HelloWorld.java`
   - Demonstrates `ai.llm().ask()` vs LangChain4j's `model.chat()`
   - Shows cost tracking and caching advantages

2. ⏳ Tutorial 02: Model Parameters
   - Recreates `_01_ModelParameters.java`
   - Demonstrates `AIOptions` for temperature, topP, etc.

3. ⏳ Tutorial 03: Image Generation
   - Recreates `_02_OpenAiImageModelExamples.java`
   - Uses Roya's `Vision` API

4. ⏳ Tutorial 04: Prompt Templates
   - Recreates `_03_PromptTemplate.java`
   - Demonstrates prompt templating with AI Services

5. ⏳ Tutorial 05: Streaming
   - Recreates `_04_Streaming.java`
   - Uses `ai.llm().stream()` or workflow streaming nodes

6. ⏳ Tutorial 06: Memory/Conversation
   - Recreates `_05_Memory.java`
   - Uses `.llmWithMemory()` or workflow memory integration

7. ⏳ Tutorial 07: Few-Shot Learning
   - Recreates `_06_FewShot.java`
   - Demonstrates few-shot examples in prompts

8. ⏳ Tutorial 08: AI Services (Declarative Interfaces)
   - Recreates `_08_AIServiceExamples.java`
   - Uses `ai.aiService(ServiceClass.class)` pattern

### Phase 2: Intermediate Tutorials
9. ⏳ Tutorial 09: Persistent Memory Per User
   - Recreates `_09_ServiceWithPersistentMemoryForEachUserExample.java`
   - User-scoped memory with workflow integration

10. ⏳ Tutorial 10: Tools/Function Calling
    - Recreates `_10_ServiceWithToolsExample.java`
    - Uses `.llmWithTools()` or `.mcp()` for discoverable tools

11. ⏳ Tutorial 11: Dynamic Tools
    - Recreates `_11_ServiceWithDynamicToolsExample.java`
    - Runtime tool discovery and registration

### Phase 3: Advanced Tutorials
12. ⏳ Tutorial 12: RAG with Documents
    - Recreates `_12_ChatWithDocumentsExamples.java`
    - Uses `.rag()` workflow nodes with Qdrant
    - Complete RAG pipeline as a workflow

---

## Actual Tutorials from LangChain4j

Based on the [LangChain4j tutorials directory](https://github.com/langchain4j/langchain4j-examples/tree/main/tutorials/src/main/java):

1. **`_00_HelloWorld.java`** → Tutorial 01: Basic LLM Usage ✅ **CREATED**
2. **`_01_ModelParameters.java`** → Tutorial 02: Model Parameters ⏳
3. **`_02_OpenAiImageModelExamples.java`** → Tutorial 03: Image Generation ⏳
4. **`_03_PromptTemplate.java`** → Tutorial 04: Prompt Templates ⏳
5. **`_04_Streaming.java`** → Tutorial 05: Streaming ⏳
6. **`_05_Memory.java`** → Tutorial 06: Memory/Conversation ⏳
7. **`_06_FewShot.java`** → Tutorial 07: Few-Shot Learning ⏳
8. **`_08_AIServiceExamples.java`** → Tutorial 08: AI Services ⏳
9. **`_09_ServiceWithPersistentMemoryForEachUserExample.java`** → Tutorial 09: Persistent Memory ⏳
10. **`_10_ServiceWithToolsExample.java`** → Tutorial 10: Tools/Function Calling ⏳
11. **`_11_ServiceWithDynamicToolsExample.java`** → Tutorial 11: Dynamic Tools ⏳
12. **`_12_ChatWithDocumentsExamples.java`** → Tutorial 12: RAG with Documents ⏳

---

## Success Criteria

Each tutorial should:
- ✅ Demonstrate the same capability as LangChain4j version
- ✅ Showcase Roya's workflow-first approach
- ✅ Include side-by-side comparison (optional)
- ✅ Include comprehensive comments explaining Roya patterns
- ✅ Work with minimal configuration
- ✅ Include usage instructions

---

## Comparison Format

Each tutorial should include:

```markdown
## LangChain4j Approach

[Original code]

## Roya Approach

[Roya workflow code]

## Key Differences

- **Roya Advantage 1**: Description
- **Roya Advantage 2**: Description
```

---

## Next Steps

1. ✅ Create project documentation
2. ✅ Explore actual LangChain4j tutorials structure
3. ✅ Create project skeleton
4. ✅ Implement Tutorial 01 (Basic LLM) - **CREATED**
5. ⏳ Implement Tutorial 02 (Model Parameters)
6. ⏳ Implement Tutorial 03 (Image Generation)
7. ⏳ Implement Tutorial 04 (Prompt Templates)
8. ⏳ Implement Tutorial 05 (Streaming)
9. ⏳ Continue with remaining tutorials

---

*Last Updated: January 30, 2025*
