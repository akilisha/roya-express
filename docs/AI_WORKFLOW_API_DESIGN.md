# AI Workflow API Design

## Overview

The Roya AI plugin provides both **high-level workflow APIs** for common patterns and **direct library access** for advanced use cases. This design enables developers to build complex, multi-node AI workflows without leaving the framework.

## Design Philosophy

1. **Workflow-First**: Primary API is workflow-oriented, not operation-oriented
2. **Stateful by Default**: Workflows maintain state across nodes automatically
3. **Plugin Integration**: Seamless integration with Cache, Database, Storage, Email plugins
4. **Library Agnostic**: High-level API works across libraries, but allows library-specific features
5. **Express-Style**: Familiar patterns for Express.js developers

## Core Concepts

### 1. Workflow Builder

A fluent API for building stateful, multi-node workflows:

```java
AI ai = req.get(AI.class);

Workflow workflow = ai.workflow("receipt-processor")
    .node("extract-receipt", node -> node
        .vision()  // OCR + image recognition
        .input("receiptImage")  // from S3 path
        .output("receiptText", "items", "vendor", "date", "total")
    )
    .node("audio-notes", node -> node
        .audio()  // Voice recognition
        .input("audioFile")
        .output("userNotes")
        .condition(notes -> !notes.isEmpty())  // Only if notes provided
    )
    .node("extract-details", node -> node
        .extract(ReceiptDetails.class)  // Structured extraction
        .input("receiptText")
        .output("structuredData")
    )
    .node("cache-data", node -> node
        .cache("receipts")  // Use cache plugin
        .key("${receiptId}")
        .value("${structuredData}")
    )
    .node("analyze-history", node -> node
        .llm()
        .systemPrompt("Analyze spending patterns...")
        .input("${allReceipts}")  // From cache
        .output("analysisReport")
        .database()  // Query spending history
    )
    .node("save-report", node -> node
        .storage()  // S3 integration
        .path("reports/${userId}/${reportId}.pdf")
        .content("${analysisReport}")
    )
    .node("update-database", node -> node
        .database()
        .query("INSERT INTO receipts ...")
        .params("${structuredData}")
    )
    .node("send-email", node -> node
        .email()
        .to("${userEmail}")
        .subject("Receipt Analysis Complete")
        .body("${analysisReport}")
    )
    .edge("extract-receipt", "audio-notes", Edge.conditional())  // Conditional edge
    .edge("audio-notes", "extract-details", Edge.always())
    .edge("extract-details", "cache-data", Edge.always())
    .edge("cache-data", "analyze-history", Edge.when("allReceiptsLoaded"))  // Conditional
    .edge("analyze-history", "save-report", Edge.always())
    .edge("save-report", "update-database", Edge.parallel())  // Parallel execution
    .edge("save-report", "send-email", Edge.parallel())
    .build();

// Execute workflow
WorkflowResult result = workflow.run(WorkflowInput.of(
    "receiptId", receiptId,
    "receiptImage", s3Path,
    "audioFile", audioPath,
    "userId", userId
));
```

### 2. Node Types

Different node types for different operations:

```java
// LLM Node - Chat, extraction, analysis
.node("analyze", node -> node.llm()
    .model("gpt-4o")  // Optional model override
    .systemPrompt("You are a financial analyst...")
    .temperature(0.7)
    .input("${data}")
    .output("${analysis}")
)

// Vision Node - Image recognition, OCR
.node("ocr", node -> node.vision()
    .input("${imagePath}")
    .output("${extractedText}")
    .tasks("ocr", "object-detection")  // Multiple vision tasks
)

// Audio Node - Speech-to-text, audio processing
.node("transcribe", node -> node.audio()
    .input("${audioFile}")
    .output("${transcription}")
    .language("en-US")
)

// Embedding Node - Vector embeddings
.node("embed", node -> node.embeddings()
    .input("${text}")
    .output("${vector}")
)

// RAG Node - Retrieval-Augmented Generation
.node("rag", node -> node.rag()
    .question("${userQuestion}")
    .collection("receipts")
    .context("${vectorSearchResults}")  // Optional: pre-retrieved context
    .output("${answer}")
    .systemPrompt("You are a helpful assistant...")
)

// MCP Node - Model Context Protocol tools (geolocation, external APIs, etc.)
.node("geolocation", node -> node.mcp()
    .tool("geolocation-api")  // MCP tool name
    .input("${location}", "${radius}")
    .output("${nearbyPlaces}")
    .config(config -> config
        .put("maxResults", 10)
        .put("distanceUnit", "miles")
    )
)

.node("distance-calc", node -> node.mcp()
    .tool("distance-calculator")
    .input("${point1}", "${point2}")
    .output("${distance}")
)

// Vector DB Node - Vector database operations
.node("vector-search", node -> node.vectors()
    .collection("store-items")
    .query("${queryEmbedding}")  // Vector embedding to search
    .textQuery("${queryText}")  // Or text (will be embedded automatically)
    .topK(10)
    .filter("distance < 5 AND category = 'electronics'")  // Metadata filter
    .output("${similarItems}")
)

.node("vector-index", node -> node.vectors()
    .collection("store-items")
    .index(docs -> docs
        .document("item-1", "Product description", embedding1, metadata1)
        .document("item-2", "Another product", embedding2, metadata2)
    )
    .upsert()  // Update if exists
)

.node("vector-delete", node -> node.vectors()
    .collection("store-items")
    .delete("${itemId}")
    // Or
    .deleteAll(filter -> filter
        .eq("storeId", "${oldStoreId}")
        .lt("timestamp", "${expiryDate}")
    )
)

// Tool Node - Execute custom tool/function
.node("fetch-data", node -> node.tool()
    .tool("database-query")
    .input("${query}")
    .output("${results}")
)

// Condition Node - Conditional logic
.node("check", node -> node.condition()
    .test("${amount} > 1000")
    .ifTrue("expensive-path")
    .ifFalse("normal-path")
)

// Cache Node - Integrate with cache plugin
.node("cache", node -> node.cache()
    .plugin("cache")  // Use cache plugin
    .key("${key}")
    .value("${value}")
    .ttl(3600)
)

// Database Node - Integrate with database plugin
.node("save", node -> node.database()
    .plugin("database")
    .query("INSERT INTO ...")
    .params("${data}")
)

// Storage Node - Integrate with storage plugin (S3)
.node("save-file", node -> node.storage()
    .plugin("storage")
    .path("${path}")
    .content("${content}")
)

// Email Node - Integrate with email plugin
.node("notify", node -> node.email()
    .plugin("email")
    .to("${to}")
    .subject("${subject}")
    .body("${body}")
)

// Parallel Node - Execute multiple nodes in parallel
.node("parallel", node -> node.parallel()
    .nodes("node1", "node2", "node3")
    .collect("${results}")
)

// Loop Node - Repeat nodes until condition
.node("process-all", node -> node.loop()
    .items("${receipts}")
    .node("process-single")
    .until("${allProcessed}")
)
```

### 3. MCP (Model Context Protocol) Integration

MCP enables AI agents to interact with external tools and services:

```java
// MCP tools can be registered and used in workflows
.node("use-mcp-tool", node -> node.mcp()
    .tool("tool-name")  // Tool registered via MCP
    .input("${param1}", "${param2}")
    .output("${result}")
    .config(config -> config
        .put("timeout", 5000)
        .put("retry", 3)
    )
)

// Common MCP tools:
// - geolocation: Find nearby places, calculate distances
// - weather: Get weather data
// - calendar: Schedule management
// - database: Database operations
// - api-client: Generic API calls
```

**MCP Tool Registration:**

```java
// Register MCP tools with the AI plugin
AI ai = req.get(AI.class);
ai.mcp().registerTool("geolocation", (params) -> {
    // Implement geolocation logic
    String location = params.get("location");
    double radius = Double.parseDouble(params.get("radius"));
    return findNearbyStores(location, radius);
});

ai.mcp().registerTool("distance", (params) -> {
    Point p1 = params.get("point1");
    Point p2 = params.get("point2");
    return calculateDistance(p1, p2);
});
```

### 4. Vector Database Integration

Vector databases enable semantic search and similarity matching:

```java
// Search for similar items using embeddings
.node("similar-items", node -> node.vectors()
    .collection("products")
    .query("${itemEmbedding}")  // Vector search
    .topK(5)
    .minScore(0.8)  // Similarity threshold
    .output("${similarProducts}")
)

// Index new items with embeddings
.node("index-item", node -> node.vectors()
    .collection("products")
    .index(doc -> doc
        .id("${itemId}")
        .content("${itemDescription}")
        .embedding("${itemEmbedding}")  // Pre-computed or auto-generated
        .metadata(meta -> meta
            .put("price", "${price}")
            .put("category", "${category}")
            .put("storeId", "${storeId}")
        )
    )
)

// Hybrid search: Vector + metadata filtering
.node("hybrid-search", node -> node.vectors()
    .collection("store-items")
    .query("${textQuery}")  // Text query (auto-embedded)
    .filter(Filter.builder()
        .eq("storeId", "${storeId}")
        .range("price", 0, 100)
        .geoDistance("location", userLocation, "5 miles")
    )
    .topK(20)
    .output("${results}")
)
```

**Vector DB Backend Support:**
- Qdrant (primary)
- Pinecone
- Weaviate
- Chroma
- PostgreSQL with pgvector

### 5. State Management

State flows automatically between nodes:

```java
// State is passed automatically between nodes
.node("node1", node -> node
    .output("result1", "result2")  // These become available in next nodes
)
.node("node2", node -> node
    .input("${result1}")  // Access previous node output
    .output("result3")
)
.node("node3", node -> node
    .input("${result1}", "${result3}")  // Access from any previous node
)
```

### 4. Library Selection

Automatic or explicit library selection:

```java
// Automatic - uses best library
.node("agent", node -> node.agents()
    .create(agent -> agent
        .systemPrompt("...")
        .tools(tool1, tool2)
    )
)

// Explicit - use specific library
.node("langgraph-agent", node -> node.agents()
    .library("langgraph")  // Use LangGraph4j specifically
    .stateGraph(...)  // Access LangGraph StateGraph directly
)
```

### 5. Integration with Other Plugins

```java
// Access other plugins seamlessly
AI ai = req.get(AI.class);
Database db = req.get(Database.class);  // Database plugin
Storage storage = req.get(Storage.class);  // Storage plugin
Cache cache = req.get(Cache.class);  // Cache plugin
Email email = req.get(Email.class);  // Email plugin

// Or use integration nodes
.node("db-query", node -> node.database()
    .plugin("database")  // Automatically uses Database plugin
    .query("SELECT * FROM receipts WHERE userId = ?")
    .params("${userId}")
)
```

## Complete Receipt Processing Workflow

```java
public class ReceiptProcessor {
    
    public Workflow createReceiptWorkflow(AI ai) {
        return ai.workflow("receipt-processor")
            // Step 1: Extract text from receipt image
            .node("ocr-extract", node -> node.vision()
                .tasks("ocr", "image-classification")
                .input("${receiptImage}")
                .output("receiptText", "imageMetadata")
            )
            
            // Step 2: Optional audio notes (conditional)
            .node("audio-notes", node -> node.audio()
                .input("${audioFile}")
                .output("userNotes")
                .optional()  // Node can be skipped if audioFile is null
            )
            
            // Step 3: Extract structured data
            .node("extract-structure", node -> node.extract(ReceiptDetails.class)
                .input("${receiptText}", "${userNotes}")
                .output("structuredData")
                .systemPrompt("Extract receipt details...")
            )
            
            // Step 4: Cache for batch processing
            .node("cache-receipt", node -> node.cache()
                .key("receipt:${receiptId}")
                .value("${structuredData}")
                .ttl(86400)
            )
            
            // Step 4.5: Price comparison - Find nearby stores and compare prices
            // This demonstrates RAG, MCP, Vector DB, and external API integration
            .node("find-nearby-stores", node -> node.mcp()
                .tool("geolocation")  // MCP tool for location services
                .input("${userLocation}")
                .radius(5)  // 5 miles
                .output("nearbyStores")
            )
            
            .node("embed-items", node -> node.embeddings()
                .input("${structuredData.items}")  // List of items from receipt
                .output("itemEmbeddings")
                .batch()  // Batch embedding for all items
            )
            
            .node("vector-search", node -> node.vectors()
                .collection("store-items")  // Vector database collection
                .query("${itemEmbeddings}")  // Query with item embeddings
                .filter("distance < 5 miles AND store IN ${nearbyStores}")
                .topK(10)  // Top 10 similar items
                .output("similarItems")
            )
            
            .node("rag-price-comparison", node -> node.rag()
                .question("Compare prices for ${structuredData.items} at stores in ${nearbyStores}")
                .context("${similarItems}")  // From vector search
                .collection("store-items")
                .output("priceComparison")
                .systemPrompt("You are a price comparison assistant...")
            )
            
            .node("fetch-live-prices", node -> node.tool()
                .tool("store-api")  // External API tool
                .input("${structuredData.items}", "${nearbyStores}")
                .output("livePrices")
                .parallel()  // Fetch from multiple stores in parallel
            )
            
            .node("update-vector-db", node -> node.vectors()
                .collection("store-items")
                .index(vectors -> vectors
                    .documents(itemsFromReceipt)
                    .embeddings("${itemEmbeddings}")
                    .metadata(metadata -> metadata
                        .put("storeId", "${storeId}")
                        .put("price", "${item.price}")
                        .put("location", "${store.location}")
                        .put("distance", "${calculatedDistance}")
                        .put("timestamp", "${currentTimestamp}")
                    )
                )
            )
            
            // Step 5: Check if all receipts processed (condition)
            .node("check-batch", node -> node.condition()
                .test("${receiptCount} == ${processedCount}")
                .output("allProcessed")
            )
            
            // Step 6: Analyze spending (only when all receipts processed)
            .node("analyze-spending", node -> node.llm()
                .systemPrompt("Analyze spending patterns and financial health")
                .input("${allReceipts}")  // From cache
                .database()  // Query spending history
                .query("SELECT * FROM receipts WHERE userId = ? AND date >= ?")
                .params("${userId}", "${startDate}")
                .output("analysisReport")
            )
            
            // Step 7: Save report to S3
            .node("save-report", node -> node.storage()
                .path("reports/${userId}/${reportId}.pdf")
                .content("${analysisReport}")
                .format("pdf")
            )
            
            // Step 8: Update database (parallel with email)
            .node("update-db", node -> node.database()
                .query("INSERT INTO receipts (userId, vendor, amount, date) VALUES (?, ?, ?, ?)")
                .params("${userId}", "${structuredData.vendor}", 
                        "${structuredData.amount}", "${structuredData.date}")
            )
            
            // Step 9: Send email (parallel with database update)
            .node("send-email", node -> node.email()
                .to("${userEmail}")
                .subject("Receipt Analysis Complete")
                .body("${analysisReport}")
                .attachment("${reportPath}")
            )
            
            // Define edges with conditions
            .edge("ocr-extract", "audio-notes", Edge.conditional("audioFile != null"))
            .edge("ocr-extract", "extract-structure", Edge.always())
            .edge("audio-notes", "extract-structure", Edge.always())
            .edge("extract-structure", "cache-receipt", Edge.always())
            .edge("cache-receipt", "find-nearby-stores", Edge.always())  // Start price comparison
            .edge("cache-receipt", "embed-items", Edge.always())  // Parallel: embed items
            .edge("find-nearby-stores", "vector-search", Edge.always())
            .edge("embed-items", "vector-search", Edge.always())  // Wait for both
            .edge("vector-search", "rag-price-comparison", Edge.always())
            .edge("find-nearby-stores", "fetch-live-prices", Edge.always())  // Parallel: fetch live prices
            .edge("rag-price-comparison", "fetch-live-prices", Edge.always())  // Merge both results
            .edge("fetch-live-prices", "update-vector-db", Edge.always())
            .edge("update-vector-db", "check-batch", Edge.always())  // Continue to batch check
            .edge("check-batch", "analyze-spending", Edge.when("allProcessed == true"))
            .edge("analyze-spending", "save-report", Edge.always())
            .edge("save-report", "update-db", Edge.parallel())
            .edge("save-report", "send-email", Edge.parallel())
            
            .build();
    }
}
```

## Direct Library Access

For advanced use cases, developers can access libraries directly:

```java
AI ai = req.get(AI.class);
UnifiedAIService unified = (UnifiedAIService) ai;

// Access LangGraph4j directly for custom StateGraph
LangGraphAdapter langGraph = unified.langGraph();
StateGraph<ReceiptState> graph = StateGraph.builder(ReceiptState.class)
    .node("node1", ...)
    .edge("node1", "node2")
    .build();

// Access LangChain4j directly for custom tool chains
LangChainAdapter langChain = unified.langChain();
ChatModel chatModel = langChain.provider(ChatModel.class);
Tool tool = CustomTool.create(...);

// Access Google ADK for advanced agent orchestration
if (unified.googleADK() != null) {
    GoogleADKAdapter adk = unified.googleADK();
    LlmAgent agent = adk.createLlmAgent(...);
}
```

## Additional Libraries to Consider

1. **Apache Tika** - For document parsing (PDFs, Office docs)
2. **OpenCV Java** - For advanced image processing
3. **Tesseract OCR** - For OCR (alternative/complement to Vision APIs)
4. **FFmpeg Java** - For audio/video processing
5. **Apache PDFBox** - For PDF generation

## API Structure

```java
public interface AI {
    // Existing methods...
    
    /**
     * Create a workflow builder for multi-node, stateful AI workflows.
     */
    WorkflowBuilder workflow(String name);
}

public interface WorkflowBuilder {
    WorkflowBuilder node(String name, Consumer<NodeBuilder> config);
    WorkflowBuilder edge(String from, String to, Edge edge);
    Workflow build();
}

public interface MCP {
    /**
     * Register an MCP tool that can be used in workflows.
     */
    void registerTool(String name, MCPTool tool);
    
    /**
     * Unregister an MCP tool.
     */
    void unregisterTool(String name);
    
    /**
     * List all registered MCP tools.
     */
    List<String> listTools();
    
    /**
     * Execute an MCP tool directly (outside of workflows).
     */
    Object executeTool(String name, Map<String, Object> params);
}

public interface NodeBuilder {
    // Node type selectors
    NodeBuilder llm();
    NodeBuilder vision();
    NodeBuilder audio();
    NodeBuilder embeddings();
    NodeBuilder rag();
    NodeBuilder agents();
    NodeBuilder extract(Class<?> type);
    NodeBuilder tool(String toolName);
    NodeBuilder mcp();  // MCP tool execution
    NodeBuilder vectors();  // Vector database operations
    NodeBuilder condition();
    NodeBuilder cache();
    NodeBuilder database();
    NodeBuilder storage();
    NodeBuilder email();
    NodeBuilder parallel();
    NodeBuilder loop();
    
    // Common configuration
    NodeBuilder input(String... inputs);
    NodeBuilder output(String... outputs);
    NodeBuilder systemPrompt(String prompt);
    NodeBuilder library(String libraryName);
    
    // MCP-specific configuration
    NodeBuilder tool(String toolName);  // MCP tool name
    NodeBuilder config(Consumer<Map<String, Object>> config);
    
    // Vector DB-specific configuration
    NodeBuilder collection(String collectionName);
    NodeBuilder query(String queryEmbedding);  // Vector query
    NodeBuilder textQuery(String textQuery);  // Text query (auto-embedded)
    NodeBuilder filter(Consumer<FilterBuilder> filter);
    NodeBuilder topK(int k);
    NodeBuilder minScore(double score);
    NodeBuilder index(Consumer<IndexBuilder> index);
    NodeBuilder delete(String... ids);
    NodeBuilder deleteAll(Consumer<FilterBuilder> filter);
    
    // ... other config methods
}

public interface Workflow {
    WorkflowResult run(WorkflowInput input);
    WorkflowResult runAsync(WorkflowInput input);
    void visualize();  // Generate graph diagram
}
```

## Summary: AI Capabilities Exposed

The receipt processing workflow demonstrates **ALL major AI touchpoints** that Roya needs to support:

### Core AI Operations
1. **Vision API** - OCR and image recognition (Step 1)
2. **Audio API** - Speech-to-text transcription (Step 2)
3. **LLM API** - Structured extraction, analysis, generation (Steps 3, 6)
4. **Embeddings API** - Vector embeddings for semantic search (Step 4.5)
5. **RAG API** - Retrieval-Augmented Generation for context-aware responses (Step 4.5)
6. **Agents API** - Agent orchestration (can be used in any step)

### Advanced AI Features
7. **MCP (Model Context Protocol)** - External tool integration:
   - Geolocation services
   - Distance calculations
   - External API calls
   - Custom business logic tools

8. **Vector Database** - Semantic search and similarity matching:
   - Vector similarity search
   - Hybrid search (vector + metadata)
   - Document indexing with embeddings
   - Metadata filtering

### Workflow Orchestration
9. **Stateful Workflows** - Multi-node workflows with state management
10. **Conditional Logic** - Conditional edges and nodes
11. **Parallel Execution** - Parallel node execution
12. **Loop Nodes** - Iterative processing

### Plugin Integrations
13. **Cache Plugin** - Caching intermediate results
14. **Database Plugin** - Querying and updating databases
15. **Storage Plugin** - File storage (S3, local)
16. **Email Plugin** - Sending notifications

### Library Selection
- **LangChain4j**: LLM primitives, embeddings, basic tools
- **LangGraph4j**: Stateful workflows, multi-node graphs, RAG
- **Google ADK**: High-level agent orchestration (when available)

This comprehensive API design ensures developers can build complex, production-ready AI workflows **entirely within the Roya framework**, without needing external cloud AI services or stepping outside the framework's boundaries.

## Design Principles Achieved

- ✅ **Workflow-first API** (not operation-first)
- ✅ **Stateful multi-node workflows** with automatic state passing
- ✅ **Plugin integrations** (cache, database, storage, email) seamlessly integrated
- ✅ **Library-agnostic high-level API** that works across LangChain, LangGraph, ADK
- ✅ **Direct library access** for advanced use cases
- ✅ **Familiar Express.js-style patterns** for Java developers
- ✅ **MCP integration** for external tools and services
- ✅ **Vector database support** for semantic search and RAG
- ✅ **Complete AI coverage** - Vision, Audio, LLM, Embeddings, RAG, Agents, MCP, Vectors

