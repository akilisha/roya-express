# LangChain4j Tutorials Recreation Project

> **Recreating LangChain4j tutorials using Roya's workflow-first approach**

This module mirrors the structure of the [official LangChain4j tutorials](https://github.com/langchain4j/langchain4j-examples/tree/main/tutorials/src/main/java), demonstrating how Roya's AI workflow framework simplifies complex AI operations.

## Structure

```
roya-examples-langchain4j-tutorials/
└── src/main/java/com/akilisha/oss/roya/examples/langchain4j/
    ├── tutorial01_basic_llm/
    ├── tutorial02_model_parameters/
    ├── tutorial03_image_generation/
    ├── tutorial04_prompt_templates/
    ├── tutorial05_streaming/
    ├── tutorial06_memory/
    ├── tutorial07_few_shot/
    ├── tutorial08_ai_services/
    ├── tutorial09_persistent_memory/
    ├── tutorial10_tools/
    ├── tutorial11_dynamic_tools/
    └── tutorial12_rag_documents/
```

## Running Tutorials

Each tutorial can be run individually:

```bash
# Tutorial 01: Basic LLM Usage
./gradlew :roya-examples-langchain4j-tutorials:runTutorial01

# Or run any tutorial directly
./gradlew :roya-examples-langchain4j-tutorials:run --args="tutorial01_basic_llm.Tutorial01BasicLLM"
```

## Tutorial Mapping

| LangChain4j Tutorial | Roya Tutorial | Status |
|----------------------|---------------|--------|
| `_00_HelloWorld.java` | Tutorial 01: Basic LLM Usage | ✅ Created |
| `_01_ModelParameters.java` | Tutorial 02: Model Parameters | ⏳ Planned |
| `_02_OpenAiImageModelExamples.java` | Tutorial 03: Image Generation | ⏳ Planned |
| `_03_PromptTemplate.java` | Tutorial 04: Prompt Templates | ⏳ Planned |
| `_04_Streaming.java` | Tutorial 05: Streaming | ⏳ Planned |
| `_05_Memory.java` | Tutorial 06: Memory/Conversation | ⏳ Planned |
| `_06_FewShot.java` | Tutorial 07: Few-Shot Learning | ⏳ Planned |
| `_08_AIServiceExamples.java` | Tutorial 08: AI Services | ⏳ Planned |
| `_09_ServiceWithPersistentMemoryForEachUserExample.java` | Tutorial 09: Persistent Memory | ⏳ Planned |
| `_10_ServiceWithToolsExample.java` | Tutorial 10: Tools/Function Calling | ⏳ Planned |
| `_11_ServiceWithDynamicToolsExample.java` | Tutorial 11: Dynamic Tools | ⏳ Planned |
| `_12_ChatWithDocumentsExamples.java` | Tutorial 12: RAG with Documents | ⏳ Planned |

## Key Differences

**LangChain4j Approach:**
- Low-level API usage (`ChatModel`, `StreamingChatModel`, etc.)
- Manual message construction
- Manual JSON parsing for structured outputs
- Manual embedding and vector store management

**Roya Approach:**
- High-level, unified API (`ai.llm()`, `ai.ragApi()`, etc.)
- Declarative AI Services interfaces
- Type-safe extraction with records
- Automatic caching and cost tracking
- Workflow-first design for complex operations

## Requirements

- Java 21+
- OpenAI API key (set `OPENAI_API_KEY` environment variable)
- For RAG tutorials: Qdrant running (see `docker-compose.yml`)

## Documentation

See `docs/LANGCHAIN4J_TUTORIALS_RECREATION.md` for detailed implementation plan and comparison guide.

