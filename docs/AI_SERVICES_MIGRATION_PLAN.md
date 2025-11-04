# AI Services Migration Plan

## Problem Statement

We've been using LangChain4j's low-level APIs (`ChatModel`, `ChatMessage`, etc.) when we should be using **AI Services** - LangChain4j's high-level abstraction that handles all the plumbing automatically.

## Current State (Low-Level)

```java
// Manual message construction
ChatModel model = OpenAiChatModel.builder()...build();
String response = model.chat(UserMessage.from("Hello"));
```

**Problems:**
- Lots of boilerplate
- Manual system/user message handling
- Manual input/output conversion
- No built-in support for RAG, tools, memory
- Complex workflow nodes

## Target State (AI Services)

```java
// Declarative interface
interface Assistant {
    @SystemMessage("You are helpful")
    String chat(String userMessage);
}

Assistant assistant = AiServices.create(Assistant.class, model);
String response = assistant.chat("Hello");
```

**Benefits:**
- Clean, declarative API
- Automatic conversions
- Built-in RAG, tools, memory
- Type-safe structured outputs
- Much less code

## Migration Strategy

### Phase 1: Create AI Service Interfaces

Define AI Service interfaces that match our current `AI` interface methods:

```java
// roya-plugins/ai/src/main/java/com/akilisha/oss/roya/plugins/ai/langchain/services/

interface RoyaLLMService {
    @SystemMessage("{{systemPrompt}}")
    String ask(@V("systemPrompt") String systemPrompt, @V("userMessage") String userMessage);
    
    @SystemMessage("{{systemPrompt}}")
    <T> T extract(@V("systemPrompt") String systemPrompt, @V("prompt") String prompt, Class<T> type);
    
    TokenStream stream(@V("systemPrompt") String systemPrompt, @V("userMessage") String userMessage);
}
```

### Phase 2: Refactor LangChainAdapter

Instead of wrapping `ChatModel` directly, use AI Services:

```java
public class LangChainAdapter implements AI {
    private final RoyaLLMService llmService;
    private final RoyaEmbeddingService embeddingService;
    
    public LangChainAdapter(ChatModel chatModel, StreamingChatModel streamingChatModel, EmbeddingModel embeddingModel) {
        this.llmService = AiServices.builder(RoyaLLMService.class)
            .chatModel(chatModel)
            .streamingChatModel(streamingChatModel)
            .build();
            
        this.embeddingService = AiServices.builder(RoyaEmbeddingService.class)
            .embeddingModel(embeddingModel)
            .build();
    }
    
    @Override
    public String ask(String systemPrompt, String userMessage) {
        return llmService.ask(systemPrompt, userMessage);
    }
}
```

### Phase 3: Update Workflow Nodes

Workflow nodes can use AI Services directly:

```java
public class LLMActionNode implements WorkflowNode {
    private final RoyaLLMService llmService;
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        String userMessage = input.getString(inputKey);
        String response = llmService.ask(systemPrompt, userMessage);
        return CompletableFuture.completedFuture(NodeOutput.success(outputKey, response));
    }
}
```

### Phase 4: Simplify Public API

Once AI Services are in place, we can expose more LangChain4j features naturally:

- **RAG**: Built-in via `@RetrievalAugmentor`
- **Tools**: Built-in via `@Tool` annotations
- **Memory**: Built-in via `ChatMemory`
- **Streaming**: Built-in via `TokenStream`
- **Structured Outputs**: Built-in via return types

## Key Benefits

1. **Less Code**: AI Services handle message construction, parsing, etc.
2. **More Features**: RAG, tools, memory come for free
3. **Type Safety**: Structured outputs work automatically
4. **Consistency**: All LangChain4j features work the same way
5. **Maintainability**: Less custom code = less to maintain

## Breaking Changes

This will require refactoring:
- `LangChainAdapter` implementation
- Workflow nodes (`LLMActionNode`, `ExtractNode`, etc.)
- Potentially our public `AI` interface (though we can keep it as a facade)

## Next Steps

1. ✅ Create AI Service interfaces
2. ✅ Refactor `LangChainAdapter` to use AI Services
3. ✅ Update workflow nodes
4. ✅ Add RAG support via `ContentRetriever`
5. ✅ Add tools support via `ToolSpecification`
6. ✅ Add memory support via `ChatMemory`

## References

- [LangChain4j AI Services Documentation](https://docs.langchain4j.dev/tutorials/ai-services)
- [LangChain4j AI Services Tutorial](https://docs.langchain4j.dev/tutorials/ai-services)



