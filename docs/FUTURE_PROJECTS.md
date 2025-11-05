# Future Projects - Roya AI Framework

**Last Updated**: January 31, 2025

This document captures ambitious future projects and demonstrations that showcase the full power of the Roya AI framework. These are marked for implementation after the core foundation is complete.

---

## LangChain4j Demo Recreation

**Reference**: [jdubois-langchain4j-demo](https://github.com/jdubois/jdubois-langchain4j-demo/tree/main)

**Status**: Planned for Future

**Goal**: Recreate a comprehensive demo suite similar to Julien Dubois's LangChain4j demo, but using Roya AI framework instead. This will serve as a powerful showcase of Roya's capabilities and provide real-world examples for developers.

### Original Demo Features

The original repository demonstrates:

1. **Image Generation** using Dalle-3
2. **Text Generation** using GPT-4o, GPT-4o-mini, Phi-4, and tinyllama
3. **Chat Conversation** with memory/context
4. **Vector Database Ingestion** and retrieval
5. **Easy RAG** implementation and examples
6. **Function Calling** (tools)
7. **Structured Outputs** (JSON Schemas)
8. **AI Agents** with LangChain4j's agentic module

### Roya Implementation Plan

**Prerequisites**:
- ✅ Complete AI workflow foundation (`.agents()`, `.llmWithTools()`, `.mcp()`, etc.)
- ✅ MCP client integration
- ✅ Vector & RAG polishing
- ✅ Workflow convenience methods (`.loop()`, `.circuit()`, `.approval()`, `.costing()`)

**Proposed Demos**:

1. **Image Generation Demo**
   - Use Roya's multimodal support (when implemented)
   - Show workflow-based image generation
   - Example: `.vision("generate", builder -> builder.imageModel(...))`

2. **Text Generation Demo**
   - Multiple LLM providers (OpenAI, Azure, Ollama, GitHub Models)
   - Workflow-based generation with different models
   - Comparison workflows

3. **Chat with Memory Demo**
   - `.llmWithMemory()` workflow
   - Conversation context management
   - Multi-turn workflows

4. **Vector Database Demo**
   - `.vectors()` indexing workflows
   - Directory indexing with chunking
   - Collection management

5. **Easy RAG Demo**
   - `.rag()` workflow nodes
   - Document ingestion → indexing → retrieval → generation
   - Complete RAG pipeline as a workflow

6. **Function Calling Demo**
   - `.llmWithTools()` workflows
   - Custom tool creation
   - Tool execution tracing

7. **Structured Outputs Demo**
   - `.extract()` workflows
   - Type-safe extraction with records
   - Validation and error handling

8. **AI Agents Demo**
   - `.agents()` workflows
   - Autonomous tool usage
   - Agent orchestration
   - Multi-agent workflows

9. **MCP Integration Demo** (NEW - Roya-specific)
   - `.mcp()` workflows
   - Discoverable tools from MCP servers
   - Multi-server MCP integration

10. **Advanced Workflow Patterns Demo** (NEW - Roya-specific)
    - `.nested()` workflows (parallel child workflows)
    - `.continuation()` workflows (sequential chaining)
    - `.loop()` for retry/iteration patterns
    - `.circuit()` for resilience
    - `.approval()` for human-in-the-loop
    - `.costing()` for budget management

### Configuration Profiles

Similar to the original, support multiple configurations:

- **Azure Profile**: Azure OpenAI + Azure AI Search
- **Local Profile (Small)**: Ollama (tinyllama) + Qdrant
- **Local Profile (Good)**: Ollama (Phi-4) + Qdrant
- **GitHub Models Profile**: GitHub Models + Qdrant
- **Elasticsearch Profile**: Ollama + Elasticsearch

### Implementation Structure

```
roya-examples/
├── ai-demo/
│   ├── src/main/java/com/akilisha/oss/roya/examples/ai/
│   │   ├── Demo1ImageGeneration.java
│   │   ├── Demo2TextGeneration.java
│   │   ├── Demo3ChatWithMemory.java
│   │   ├── Demo4VectorDatabase.java
│   │   ├── Demo5EasyRAG.java
│   │   ├── Demo6FunctionCalling.java
│   │   ├── Demo7StructuredOutputs.java
│   │   ├── Demo8AIAgents.java
│   │   ├── Demo9MCPIntegration.java
│   │   └── Demo10AdvancedWorkflows.java
│   ├── src/main/resources/
│   │   ├── application-azure.properties
│   │   ├── application-small.properties
│   │   ├── application-good.properties
│   │   ├── application-github.properties
│   │   └── application-elasticsearch.properties
│   └── README.md
```

### Key Differentiators

What makes the Roya version unique:

1. **Workflow-First Approach**: Every demo is a workflow, not just isolated API calls
2. **Declarative API**: Using `.agents()`, `.llm()`, `.extract()` instead of low-level APIs
3. **Built-in Resilience**: `.circuit()`, `.loop()`, `.approval()` patterns
4. **MCP Integration**: First-class support for Model Context Protocol
5. **Cost Tracking**: Built-in `.costing()` for budget management
6. **Nested/Continuation**: Advanced workflow composition patterns
7. **Trigger Integration**: Webhooks, cron jobs, file watching, polling all as workflow triggers

### Web UI

Similar to the original, create a web UI that:
- Lists all demos
- Allows configuration profile selection
- Shows workflow execution
- Displays results and traces
- Provides interactive demos where appropriate

### Documentation

- README with setup instructions
- Each demo should have inline documentation
- Architecture diagrams showing workflow structure
- Comparison with original LangChain4j approach
- Best practices guide

### Estimated Effort

- **Phase 1**: Core demos (1-8) - 40 hours
- **Phase 2**: MCP and advanced workflows (9-10) - 20 hours
- **Phase 3**: Web UI and polish - 20 hours
- **Total**: ~80 hours

### Success Criteria

- [ ] All original demos recreated with Roya workflows
- [ ] MCP integration demo working
- [ ] Advanced workflow patterns demonstrated
- [ ] Web UI functional
- [ ] Documentation complete
- [ ] Code examples are clear and educational
- [ ] Performance comparable or better than original

### Notes

- This project will be a **showcase** of Roya's capabilities
- Should be **production-ready** code, not just demos
- Can serve as **reference implementation** for other developers
- May influence future Roya features based on usage patterns

---

## Other Future Projects

_Add more ambitious projects here as they are identified._

