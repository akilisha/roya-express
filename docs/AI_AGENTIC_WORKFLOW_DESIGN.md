# AI Agentic Workflow Design
## Building on roya-workflow Foundation

**Status**: Design Proposal  
**Date**: Based on roya-workflow v1.0  
**Foundation**: `roya-workflow` (graph-based workflow orchestrator)

---

## Executive Summary

This design integrates the Roya AI plugin with the `roya-workflow` framework to create a powerful, type-safe, composable AI agent workflow system. We leverage roya-workflow's excellent graph-based execution model and build AI-specific nodes that wrap our AI capabilities (LLM, embeddings, RAG, agents, etc.).

**Key Insight**: AI operations are just nodes in a workflow graph. The workflow framework handles execution, state, retry, and observability. We focus on making AI operations feel natural and type-safe.

---

## Design Principles

### 1. **roya-workflow is the Foundation**
- ✅ Use `Workflow.create()` builder
- ✅ Use `.trigger()`, `.action()`, `.logic()`, `.conditional()` node types
- ✅ Use `Edge` configuration for routing, retry, parallel execution
- ✅ Use `ExecutionContext` for state management
- ✅ Use `WorkflowExecutor` for execution

### 2. **AI Operations as Nodes**
- LLM calls → `LLMActionNode`
- Embeddings → `EmbeddingNode`
- RAG → `RAGNode`
- Vision → `VisionNode`
- Agents → `AgentNode`
- MCP tools → `MCPNode`

### 3. **Plugin Integration via Nodes**
- Cache operations → `CacheNode`
- Database queries → `DatabaseNode`
- Storage operations → `StorageNode`
- Email → `EmailNode`

### 4. **Type Safety Throughout**
- Java records as AI schemas
- Type-safe node configuration
- Compile-time guarantees

---

## Core Architecture

```
roya-plugins/agentic/
├── nodes/
│   ├── ai/
│   │   ├── LLMActionNode.java          # LLM chat, extraction
│   │   ├── EmbeddingNode.java          # Text-to-vector
│   │   ├── RAGNode.java                # RAG queries
│   │   ├── VisionNode.java              # OCR, image analysis
│   │   ├── AudioNode.java              # Speech-to-text
│   │   ├── AgentNode.java              # Agent execution
│   │   └── MCPNode.java                # MCP tool calls
│   ├── integration/
│   │   ├── CacheNode.java              # Cache plugin
│   │   ├── DatabaseNode.java           # Database plugin
│   │   ├── StorageNode.java             # Storage plugin
│   │   └── EmailNode.java              # Email plugin
│   └── logic/
│       ├── VectorSearchNode.java       # Vector DB search
│       ├── ConditionalRouterNode.java  # Intent routing
│       └── TransformNode.java          # Data transformation
└── builder/
    └── AIWorkflowBuilder.java          # Fluent builder on top of Workflow
```

---

## AI Node Implementations

### Example: LLMActionNode

```java
package com.akilisha.oss.roya.plugins.agentic.nodes.ai;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIOptions;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * LLM action node - wraps AI.llm() operations in a workflow node.
 */
public class LLMActionNode implements WorkflowNode {
    
    private final AI ai;
    private final String systemPrompt;
    private final AIOptions options;
    private final String inputKey;
    private final String outputKey;
    
    public LLMActionNode(AI ai, String systemPrompt, AIOptions options, 
                        String inputKey, String outputKey) {
        this.ai = ai;
        this.systemPrompt = systemPrompt;
        this.options = options;
        this.inputKey = inputKey;
        this.outputKey = outputKey;
    }
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get input from context
                String userMessage = input.getString(inputKey);
                
                // Call LLM
                String response = ai.llm().ask(systemPrompt, userMessage, options);
                
                // Return output
                return NodeOutput.success(Map.of(outputKey, response));
                
            } catch (Exception e) {
                return NodeOutput.failure("LLM call failed: " + e.getMessage());
            }
        });
    }
    
    // Builder for fluent configuration
    public static Builder builder(AI ai) {
        return new Builder(ai);
    }
    
    public static class Builder {
        private final AI ai;
        private String systemPrompt;
        private AIOptions options = AIOptions.defaults();
        private String inputKey = "message";
        private String outputKey = "response";
        
        Builder(AI ai) {
            this.ai = ai;
        }
        
        public Builder systemPrompt(String prompt) {
            this.systemPrompt = prompt;
            return this;
        }
        
        public Builder options(AIOptions opts) {
            this.options = opts;
            return this;
        }
        
        public Builder inputKey(String key) {
            this.inputKey = key;
            return this;
        }
        
        public Builder outputKey(String key) {
            this.outputKey = key;
            return this;
        }
        
        public LLMActionNode build() {
            return new LLMActionNode(ai, systemPrompt, options, inputKey, outputKey);
        }
    }
}
```

### Example: ExtractNode (Type-Safe Extraction)

```java
public class ExtractNode<T> implements WorkflowNode {
    private final AI ai;
    private final Class<T> extractType;
    private final String inputKey;
    private final String outputKey;
    private final AIOptions options;
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String prompt = input.getString(inputKey);
                T extracted = ai.llm().extract(extractType, prompt, options);
                return NodeOutput.success(Map.of(outputKey, extracted));
            } catch (Exception e) {
                return NodeOutput.failure("Extraction failed: " + e.getMessage());
            }
        });
    }
}
```

### Example: EmbeddingNode

```java
public class EmbeddingNode implements WorkflowNode {
    private final AI ai;
    private final String inputKey;
    private final String outputKey;
    private final boolean batch;
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (batch) {
                    @SuppressWarnings("unchecked")
                    List<String> texts = (List<String>) input.get(inputKey);
                    List<float[]> embeddings = ai.embeddings().embed(texts);
                    return NodeOutput.success(Map.of(outputKey, embeddings));
                } else {
                    String text = input.getString(inputKey);
                    float[] embedding = ai.embeddings().embed(text);
                    return NodeOutput.success(Map.of(outputKey, embedding));
                }
            } catch (Exception e) {
                return NodeOutput.failure("Embedding failed: " + e.getMessage());
            }
        });
    }
}
```

### Example: MCPNode

```java
public class MCPNode implements WorkflowNode {
    private final AI ai;
    private final String toolName;
    private final Map<String, String> inputMapping;
    private final String outputKey;
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Map inputs to tool parameters
                Map<String, Object> params = new HashMap<>();
                for (Map.Entry<String, String> entry : inputMapping.entrySet()) {
                    params.put(entry.getKey(), input.get(entry.getValue()));
                }
                
                // Execute MCP tool
                Object result = ai.mcp().executeTool(toolName, params);
                
                return NodeOutput.success(Map.of(outputKey, result));
            } catch (Exception e) {
                return NodeOutput.failure("MCP tool failed: " + e.getMessage());
            }
        });
    }
}
```

---

## Fluent Builder API

While roya-workflow's builder is excellent, we can add a convenience layer for AI workflows:

```java
package com.akilisha.oss.roya.plugins.agentic.builder;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.workflow.core.Workflow;

/**
 * Fluent builder specifically for AI workflows.
 * Wraps roya-workflow's Workflow builder with AI-specific convenience methods.
 */
public class AIWorkflowBuilder {
    private final AI ai;
    private final Workflow.WorkflowBuilder workflowBuilder;
    
    private AIWorkflowBuilder(AI ai, String workflowName) {
        this.ai = ai;
        this.workflowBuilder = Workflow.create();
    }
    
    public static AIWorkflowBuilder create(AI ai, String name) {
        return new AIWorkflowBuilder(ai, name);
    }
    
    // Convenience methods that create nodes and add them
    public AIWorkflowBuilder llm(String nodeId, Consumer<LLMActionNode.Builder> config) {
        LLMActionNode.Builder builder = LLMActionNode.builder(ai);
        config.accept(builder);
        workflowBuilder.action(nodeId, builder.build());
        return this;
    }
    
    public AIWorkflowBuilder extract(String nodeId, Class<?> type, Consumer<ExtractNode.Builder> config) {
        ExtractNode.Builder builder = ExtractNode.builder(ai, type);
        config.accept(builder);
        workflowBuilder.action(nodeId, builder.build());
        return this;
    }
    
    public AIWorkflowBuilder embeddings(String nodeId, Consumer<EmbeddingNode.Builder> config) {
        EmbeddingNode.Builder builder = EmbeddingNode.builder(ai);
        config.accept(builder);
        workflowBuilder.action(nodeId, builder.build());
        return this;
    }
    
    // Delegate edge methods to underlying workflow builder
    public AIWorkflowBuilder edge(String from, String to) {
        workflowBuilder.edge(from, to);
        return this;
    }
    
    public AIWorkflowBuilder edge(String from, String to, Edge edge) {
        workflowBuilder.edge(from, to, edge);
        return this;
    }
    
    public Workflow build() {
        return workflowBuilder.build();
    }
}
```

---

## Redesigned Receipt Processing Workflow

Using roya-workflow primitives:

```java
public class ReceiptProcessorWorkflow {
    
    public static Workflow buildWorkflow(AI ai, Database db, Storage storage, Email email) {
        return Workflow.create()
            
            // ===== Trigger: Receipt Upload =====
            .trigger("receiptUpload", new ReceiptUploadTriggerNode())
            
            // ===== Vision: OCR Extract =====
            .action("ocrExtract", new VisionNode(ai)
                .tasks("ocr", "object-detection")
                .inputKey("receiptImage")
                .outputKeys("receiptText", "detectedItems")
            )
            
            // ===== Audio: Optional Notes =====
            .action("audioNotes", new AudioNode(ai)
                .inputKey("audioFile")
                .outputKey("userNotes")
                .optional(true)
            )
            
            // ===== LLM: Extract Structured Data =====
            .action("extractDetails", ExtractNode.builder(ai, ReceiptDetails.class)
                .systemPrompt("Extract receipt information into structured format")
                .inputKey("receiptText")
                .outputKey("receiptDetails")
                .build()
            )
            
            // ===== Cache: Store for Batch Processing =====
            .action("cacheReceipt", new CacheNode(cache)
                .keyPattern("receipt:${receiptId}")
                .valueKey("receiptDetails")
                .ttl(Duration.ofHours(24))
            )
            
            // ===== Embeddings: Generate Item Embeddings =====
            .action("embedItems", new EmbeddingNode(ai)
                .inputKey("receiptDetails.items")
                .outputKey("itemEmbeddings")
                .batch(true)
            )
            
            // ===== MCP: Find Nearby Stores =====
            .action("findNearbyStores", new MCPNode(ai)
                .toolName("geolocation")
                .inputMapping(Map.of(
                    "location", "userLocation",
                    "radius", "5"
                ))
                .outputKey("nearbyStores")
            )
            
            // ===== Vector Search: Find Similar Items =====
            .action("vectorSearch", new VectorSearchNode(vectorDB)
                .collection("store-items")
                .queryKey("itemEmbeddings")
                .topK(10)
                .outputKey("similarItems")
            )
            
            // ===== RAG: Price Comparison =====
            .action("ragPriceComparison", new RAGNode(ai)
                .question("Compare prices for ${receiptDetails.items} at ${nearbyStores}")
                .contextKey("similarItems")
                .collection("store-items")
                .outputKey("priceComparison")
            )
            
            // ===== Logic: Check if All Receipts Processed =====
            .logic("checkBatch", TransformNode.create(data -> {
                int receiptCount = (Integer) data.get("receiptCount");
                int processedCount = (Integer) data.get("processedCount");
                return Map.of("allProcessed", receiptCount == processedCount);
            }))
            
            // ===== LLM: Analyze Spending =====
            .action("analyzeSpending", LLMActionNode.builder(ai)
                .systemPrompt("Analyze spending patterns and financial health")
                .inputKey("allReceipts")
                .outputKey("analysisReport")
                .build()
            )
            
            // ===== Storage: Save Report =====
            .action("saveReport", new StorageNode(storage)
                .pathPattern("reports/${userId}/${reportId}.pdf")
                .contentKey("analysisReport")
            )
            
            // ===== Database: Update Receipt Data =====
            .action("updateDatabase", new DatabaseNode(db)
                .query("INSERT INTO receipts (userId, vendor, amount, date, items) VALUES (?, ?, ?, ?, ?)")
                .params("${userId}", "${receiptDetails.vendor}", 
                        "${receiptDetails.amount}", "${receiptDetails.date}",
                        "${receiptDetails.items}")
            )
            
            // ===== Email: Send Notification =====
            .action("sendEmail", new EmailNode(email)
                .toKey("userEmail")
                .subject("Receipt Analysis Complete")
                .bodyKey("analysisReport")
                .attachmentKey("reportPath")
            )
            
            // ===== Edges =====
            .edge("receiptUpload", "ocrExtract")
            .edge("ocrExtract", "audioNotes", Edge.when(ctx -> ctx.get("audioFile") != null))
            .edge("ocrExtract", "extractDetails")
            .edge("audioNotes", "extractDetails")
            .edge("extractDetails", "cacheReceipt")
            .edge("cacheReceipt", "embedItems")
            .edge("cacheReceipt", "findNearbyStores", Edge.parallel())
            .edge("embedItems", "vectorSearch")
            .edge("findNearbyStores", "ragPriceComparison")
            .edge("vectorSearch", "ragPriceComparison")
            .edge("ragPriceComparison", "checkBatch")
            .edge("checkBatch", "analyzeSpending", 
                Edge.when(ctx -> Boolean.TRUE.equals(ctx.get("allProcessed"))))
            .edge("analyzeSpending", "saveReport")
            .edge("saveReport", "updateDatabase", Edge.parallel())
            .edge("saveReport", "sendEmail", Edge.parallel())
            
            .build();
    }
}
```

---

## Key Design Decisions

### ✅ What We Keep from Original Design

1. **Workflow Builder Concept** - Fluent API for building workflows
2. **Node Types** - Action, Logic, Conditional (now using roya-workflow types)
3. **MCP Integration** - MCP tools as workflow nodes
4. **Vector Database** - Vector search as a node
5. **State Management** - Automatic via ExecutionContext
6. **Library Selection** - AI service delegates to best library
7. **Plugin Integration** - Cache, Database, Storage, Email as nodes

### ❌ What We Remove

1. **Express.js Pipeline Pattern** - Replaced with roya-workflow graph execution
2. **Custom Workflow Executor** - Use roya-workflow's WorkflowExecutor
3. **Manual State Passing** - ExecutionContext handles it automatically
4. **next.proceed()** pattern - Replaced with CompletableFuture

### 🎯 What We Add

1. **Type-Safe Node Builders** - Builder pattern for each node type
2. **AI-Specific Convenience Methods** - Fluent API on top of roya-workflow
3. **Streaming Support** - Use roya-workflow's StreamingNode interface
4. **Retry Policies** - Built into roya-workflow edges
5. **Observability** - Use roya-workflow's visitor pattern

---

## Implementation Strategy

### Phase 1: Core AI Nodes (Week 1)
- [ ] LLMActionNode
- [ ] ExtractNode (type-safe extraction)
- [ ] EmbeddingNode
- [ ] StreamingLLMNode (for token streaming)

### Phase 2: Advanced AI Nodes (Week 2)
- [ ] RAGNode
- [ ] VisionNode
- [ ] AudioNode
- [ ] AgentNode
- [ ] MCPNode

### Phase 3: Integration Nodes (Week 3)
- [ ] CacheNode
- [ ] DatabaseNode
- [ ] StorageNode
- [ ] EmailNode

### Phase 4: Vector & Logic Nodes (Week 4)
- [ ] VectorSearchNode
- [ ] VectorIndexNode
- [ ] ConditionalRouterNode

### Phase 5: Builder & Examples (Week 5)
- [ ] AIWorkflowBuilder (convenience layer)
- [ ] Receipt processing workflow example
- [ ] Documentation

---

## Usage Example Comparison

### Before (Express-style - REMOVED)
```java
Workflow workflow = ai.workflow("receipt-processor")
    .node("extract", node -> node.llm()
        .input("${receiptText}")
        .output("${structuredData}")
    )
    .edge("extract", "save", Edge.always())
    .build();
```

### After (roya-workflow style)
```java
Workflow workflow = Workflow.create()
    .action("extract", LLMActionNode.builder(ai)
        .systemPrompt("Extract receipt details")
        .inputKey("receiptText")
        .outputKey("structuredData")
        .build()
    )
    .action("save", new StorageNode(storage)
        .pathPattern("receipts/${receiptId}.json")
        .contentKey("structuredData")
    )
    .edge("extract", "save")
    .build();
```

---

## Design Decisions (Finalized)

### 1. ✅ AIWorkflowBuilder - CONFIRMED
**Decision**: Implement `AIWorkflowBuilder` as a semantic convenience layer on top of roya-workflow's `Workflow.create()` builder.

**Rationale**: 
- Provides AI-specific, semantically clear methods (`.llm()`, `.extract()`, `.embeddings()`)
- Makes intent obvious at a glance
- Wraps roya-workflow without hiding it - users can drop down to raw `Workflow.create()` if needed

### 2. ✅ Node Configuration - BOTH Builder AND Constructor
**Decision**: Provide both builder pattern AND constructor-based initialization.

**Rationale**:
- **Builder**: Excellent for discoverability, self-documenting, handles optional parameters gracefully
- **Constructor**: Terse, efficient for simple cases, works well with `final` immutable fields
- **Best of both worlds**: Flexibility for different use cases

**Pattern**:
```java
// Builder (preferred for complex configuration)
LLMActionNode node = LLMActionNode.builder(ai)
    .systemPrompt("...")
    .inputKey("message")
    .outputKey("response")
    .options(AIOptions.builder().temperature(0.7).build())
    .build();

// Constructor (for simple cases)
LLMActionNode node = new LLMActionNode(ai, "prompt", "message", "response");
```

### 3. ✅ Streaming - Use roya-workflow's StreamingNode Interface
**Decision**: Implement `StreamingNode` interface for AI token streaming, compatible with Java Flow APIs.

**Rationale**:
- roya-workflow's `StreamingNode` is provider-agnostic (no concrete implementation)
- Compatible with Java Flow APIs (Reactive Streams)
- We provide the backing implementation using AI streaming capabilities

**Implementation**:
```java
public class StreamingLLMNode implements StreamingNode {
    @Override
    public Flow.Publisher<StreamChunk> stream(NodeInput input) {
        // Use AI service streaming capabilities
        return ai.llm().stream(...); // Returns Flow.Publisher
    }
}
```

### 4. ✅ Error Handling - AI-Specific
**Decision**: Implement AI-specific error handling strategies that extend/override roya-workflow's generic error strategies.

**Rationale**:
- AI operations have unique error characteristics (rate limits, token limits, model failures)
- Can override generic behavior while leveraging roya-workflow's foundation
- Provides domain-specific error recovery strategies

**AI-Specific Error Strategies**:
- `RATE_LIMIT_RETRY` - Exponential backoff for rate limit errors
- `TOKEN_LIMIT_SPLIT` - Split input and retry for token limit errors
- `MODEL_FALLBACK` - Fallback to cheaper/faster model on failure
- `CACHE_ON_ERROR` - Cache partial results even on failure

### 5. ✅ Library Selection Strategy - Hybrid Approach
**Decision**: Multi-tiered approach based on developer expertise and use case.

**Strategy**:
1. **LangChain4j** = Primary Backend
   - All workflow nodes use LangChain4j as the implementation
   - Provides consistent, n8n-inspired workflow experience
   - Developer doesn't need to know LangChain internals

2. **LangGraph4j** = Exposed Service (Direct Access)
   - Exposed as `ai.langGraph()` service for direct use
   - For developers already proficient with LangGraph
   - Roya framework is just the "backend vehicle"
   - No workflow abstraction layer - use LangGraph's own orchestration

3. **Google ADK** = Exposed Service (Direct Access)
   - Exposed as `ai.googleADK()` service for direct use
   - For developers familiar with Google ADK
   - Roya framework provides the runtime environment
   - Use ADK's own opinionated orchestration patterns

**API Structure**:
```java
public interface AI {
    // Primary: Workflow-based API (uses LangChain4j backend)
    AIWorkflowBuilder workflow(String name);
    
    // Core sub-APIs (backed by LangChain4j)
    LLM llm();
    Embeddings embeddings();
    RAG rag();
    // ... etc
    
    // Direct library access (for advanced users)
    LangGraphService langGraph();  // Direct LangGraph4j access
    GoogleADKService googleADK();   // Direct Google ADK access
    
    // Provider access (underlying LangChain4j models)
    <T> T provider(Class<T> providerType);
}
```

**Usage Examples**:

```java
// Option 1: Use Roya's workflow API (LangChain4j backend, transparent)
Workflow workflow = AIWorkflowBuilder.create(ai, "my-workflow")
    .llm("classify", builder -> builder.systemPrompt("...").inputKey("message"))
    .edge("classify", "respond")
    .build();

// Option 2: Use LangGraph directly (for LangGraph experts)
LangGraphService langGraph = ai.langGraph();
StateGraph<MyState> graph = langGraph.buildGraph()
    .node("node1", ...)
    .edge("node1", "node2")
    .compile();
graph.invoke(initialState);

// Option 3: Use Google ADK directly (for ADK experts)
GoogleADKService adk = ai.googleADK();
LlmAgent agent = adk.createAgent(...);
agent.run(input);
```

---

## Next Steps

1. ✅ **Design finalized** - All questions answered
2. **Start implementation** - Begin with Phase 1 core nodes
3. **Build AIWorkflowBuilder** - Implement the convenience layer
4. **Implement core AI nodes** - LLM, Extract, Embeddings
5. **Add streaming support** - StreamingLLMNode using Flow APIs
6. **Iterate and refine** - Based on real workflow usage

---

**Status**: Design Complete - Ready for Implementation 🚀

