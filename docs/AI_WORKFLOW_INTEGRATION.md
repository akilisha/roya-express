# AI Workflow Integration with New Workflow Library Features

## Overview

The workflow library (`roya-workflow`) includes several powerful features as first-class citizens:
- **Nested Workflows** (`.nested()`) - Parallel execution of child workflows with aggregation
- **Continuation Workflows** (`.continuation()`) - Sequential chaining of workflows
- **Loop Nodes** (`LoopNode`) - Repeat tasks with various strategies (sequential, parallel, until success)
- **Cost Tracking** (`CostTracker`) - Budget enforcement and cost reporting via visitor pattern
- **Circuit Breaker** (`CircuitBreakerNode`) - Resilience patterns for flaky services
- **Human Approval** (`HumanApprovalNode`) - Human-in-the-middle workflows for oversight

## 1. Incorporating New Nodes into AI Workflow

### Current State ✅

The `AIWorkflowBuilder` delegates to `Workflow.WorkflowBuilder`, so **all workflow library features are available** via delegation methods:

```java
import com.akilisha.oss.roya.workflow.loop.LoopNode;
import com.akilisha.oss.roya.workflow.resilience.CircuitBreaker;
import com.akilisha.oss.roya.workflow.resilience.CircuitBreakerNode;
import com.akilisha.oss.roya.workflow.hitm.HumanApprovalNode;
import com.akilisha.oss.roya.workflow.nested.NestedExecutionStrategy;
import com.akilisha.oss.roya.workflow.nested.MergeAllAggregator;

Workflow workflow = ai.workflow("my-workflow")
    .trigger("webhook", WebhookTrigger.create(...))
    
    // AI-specific nodes
    .llm("analyze", builder -> builder.systemPrompt("..."))
    .rag("search", builder -> builder.collection("kb"))
    
    // Workflow composition (delegation methods - now available!)
    .nested("gatherContext", 
        List.of(child1, child2, child3), 
        new MergeAllAggregator(true),
        NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT)
    .continuation("process", nextWorkflow, "start")
    
    // Workflow library nodes via .action()
    .action("loop", new LoopNode(taskNode, 5, LoopNode.LoopStrategy.PARALLEL))
    .action("approval", new HumanApprovalNode(provider, "Approve this action?"))
    .action("protected", new CircuitBreakerNode(llmNode, circuitBreaker))
    
    .edge("webhook", "analyze")
    .edge("analyze", "gatherContext")
    .build();
    
// Add cost tracking via visitor
CostTracker costTracker = new CostTracker(10.00)
    .withNodeCost("analyze", 0.01)
    .withNodeCost("loop", 0.001);

WorkflowExecutor executor = new WorkflowExecutor(workflow)
    .addVisitor(costTracker);
```

### Recommended Integration Strategy

**Option A: Direct Access (Current)** ✅
- Developers can use `.action()` to add any workflow library node
- Pros: Simple, flexible, no changes needed, fully compatible
- Cons: Less discoverable, requires importing workflow library classes

**Example Using Direct Access:**
```java
import com.akilisha.oss.roya.workflow.loop.LoopNode;
import com.akilisha.oss.roya.workflow.resilience.CircuitBreaker;
import com.akilisha.oss.roya.workflow.resilience.CircuitBreakerNode;
import com.akilisha.oss.roya.workflow.hitm.HumanApprovalNode;
import com.akilisha.oss.roya.workflow.nested.*;

// Create child workflows for parallel execution
Workflow billingWorkflow = ai.workflow("billing")
    .trigger("start", new BillingCheckNode())
    .build();

// Main workflow with all features
Workflow workflow = ai.workflow("support-agent")
    .trigger("webhook", WebhookTrigger.create(...))
    
    // Nested workflows (parallel execution)
    .nested("gatherContext", 
        List.of(billingWorkflow, historyWorkflow),
        new MergeAllAggregator(true),
        NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT)
    
    // LLM with circuit breaker protection
    .action("analyze", new CircuitBreakerNode(
        LLMActionNode.builder(ai)
            .systemPrompt("Analyze customer context")
            .build(),
        CircuitBreaker.withThreshold(3, Duration.ofSeconds(5))
    ))
    
    // Human approval for sensitive actions
    .action("approval", new HumanApprovalNode(
        approvalProvider, 
        "Approve customer refund request?"))
    
    // Retry LLM call if needed (loop)
    .action("retry", new LoopNode(
        LLMActionNode.builder(ai)
            .systemPrompt("Generate response")
            .build(),
        3,
        LoopNode.LoopStrategy.UNTIL_SUCCESS
    ))
    
    .edge("webhook", "gatherContext")
    .edge("gatherContext", "analyze")
    .edge("analyze", "approval")
    .edge("approval", "retry")
    .build();
```

**Option B: Add Convenience Methods** (Recommended for common patterns)
Add concise convenience methods to `AIWorkflowBuilder` for common AI + workflow patterns:

```java
// Shorter, more natural method names
public AIWorkflowBuilder loop(String nodeId, 
                              int iterations,
                              LoopNode.LoopStrategy strategy,
                              Consumer<LLMActionNode.Builder> config) {
    LLMActionNode.Builder llmBuilder = LLMActionNode.builder(ai);
    config.accept(llmBuilder);
    LLMActionNode llmNode = llmBuilder.build();
    
    LoopNode loopNode = new LoopNode(llmNode, iterations, strategy);
    workflowBuilder.action(nodeId, loopNode);
    return this;
}

// Circuit breaker protection for LLM calls
public AIWorkflowBuilder circuit(String nodeId,
                                CircuitBreaker breaker,
                                Consumer<LLMActionNode.Builder> config) {
    LLMActionNode.Builder llmBuilder = LLMActionNode.builder(ai);
    config.accept(llmBuilder);
    LLMActionNode llmNode = llmBuilder.build();
    
    CircuitBreakerNode protectedNode = new CircuitBreakerNode(llmNode, breaker);
    workflowBuilder.action(nodeId, protectedNode);
    return this;
}

// Human approval before LLM execution
public AIWorkflowBuilder approval(String nodeId,
                                 ApprovalProvider provider,
                                 String prompt,
                                 Consumer<LLMActionNode.Builder> config) {
    LLMActionNode.Builder llmBuilder = LLMActionNode.builder(ai);
    config.accept(llmBuilder);
    LLMActionNode llmNode = llmBuilder.build();
    
    // Chain: approval → LLM
    workflowBuilder.action(nodeId + "-approval", 
        new HumanApprovalNode(provider, prompt));
    workflowBuilder.action(nodeId, llmNode);
    workflowBuilder.edge(nodeId + "-approval", nodeId);
    
    return this;
}

// Cost tracking integration (returns CostTracker for visitor registration)
public CostTracker costing(double budget) {
    return new CostTracker(budget);
}
```

**Usage Example:**
```java
Workflow workflow = ai.workflow("agent")
    .trigger("webhook", WebhookTrigger.create(...))
    
    // Natural, concise API
    .circuit("analyze", breaker, builder -> builder
        .systemPrompt("Analyze data")
    )
    .approval("review", provider, "Approve?", builder -> builder
        .systemPrompt("Generate response")
    )
    .loop("retry", 3, LoopStrategy.UNTIL_SUCCESS, builder -> builder
        .systemPrompt("Retry if needed")
    )
    .build();

// Cost tracking
CostTracker tracker = ai.costing(10.00)
    .withNodeCost("analyze", 0.01);

WorkflowExecutor executor = new WorkflowExecutor(workflow)
    .addVisitor(tracker);
```

**Recommendation**: Start with Option A (current state). Add Option B convenience methods based on real usage patterns when common patterns emerge.

### Complete Example: AI Workflow with All Features

```java
import com.akilisha.oss.roya.workflow.loop.LoopNode;
import com.akilisha.oss.roya.workflow.resilience.CircuitBreaker;
import com.akilisha.oss.roya.workflow.resilience.CircuitBreakerNode;
import com.akilisha.oss.roya.workflow.hitm.HumanApprovalNode;
import com.akilisha.oss.roya.workflow.hitm.PollingApprovalProvider;
import com.akilisha.oss.roya.workflow.nested.*;
import com.akilisha.oss.roya.workflow.cost.CostTracker;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;

// Setup infrastructure
ApprovalProvider approvalProvider = new PollingApprovalProvider();
CircuitBreaker llmBreaker = CircuitBreaker.withThreshold(3, Duration.ofSeconds(5));
CostTracker costTracker = new CostTracker(10.00)  // $10 budget
    .withNodeCost("analyze", 0.01)
    .withNodeCost("retry", 0.001);

// Child workflows for parallel execution
Workflow billingWorkflow = ai.workflow("billing")
    .trigger("start", new BillingCheckNode())
    .build();

Workflow historyWorkflow = ai.workflow("history")
    .trigger("start", new HistorySearchNode())
    .build();

// Main workflow combining all features
Workflow main = ai.workflow("support-agent")
    .trigger("webhook", WebhookTrigger.create(...))
    
    // Parallel context gathering (nested workflows)
    .nested("gatherContext", 
        List.of(billingWorkflow, historyWorkflow),
        new MergeAllAggregator(true),
        NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT
    )
    
    // LLM analysis with circuit breaker protection
    .action("analyze", new CircuitBreakerNode(
        LLMActionNode.builder(ai)
            .systemPrompt("Analyze customer context")
            .inputKey("context")
            .outputKey("analysis")
            .build(),
        llmBreaker
    ))
    
    // Human approval for sensitive actions
    .action("approval", new HumanApprovalNode(approvalProvider, 
        "Approve customer refund request?"))
    
    // Retry LLM call if needed (loop)
    .action("retry", new LoopNode(
        LLMActionNode.builder(ai)
            .systemPrompt("Generate response")
            .inputKey("analysis")
            .outputKey("response")
            .build(),
        3,
        LoopNode.LoopStrategy.UNTIL_SUCCESS
    ))
    
    .edge("webhook", "gatherContext")
    .edge("gatherContext", "analyze")
    .edge("analyze", "approval")
    .edge("approval", "retry")
    .build();

// Execute with cost tracking
WorkflowExecutor executor = new WorkflowExecutor(main)
    .addVisitor(costTracker);

WorkflowResult result = executor.executeFrom("webhook", inputData).join();

// Check cost report
if (costTracker.isBudgetExceeded()) {
    System.out.println("Budget exceeded! Total: $" + costTracker.getTotalCost());
}
```

## 2. Tools Support in AI Workflow

### Current Implementation ✅

**Tools are FULLY SUPPORTED** via LangChain4j's `ToolSpecification`:

```java
// Create tools using LangChain4j
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolExecutionRequest;

List<ToolSpecification> tools = List.of(
    ToolSpecification.builder()
        .name("get_weather")
        .description("Get current weather for a location")
        .build(),
    ToolSpecification.builder()
        .name("search_database")
        .description("Search customer database by ID")
        .build()
);

// Use in workflow
Workflow workflow = ai.workflow("agent-workflow")
    .trigger("webhook", WebhookTrigger.create(...))
    
    // Method 1: llmWithTools convenience method
    .llmWithTools("agent", tools, builder -> builder
        .systemPrompt("You are a helpful assistant with access to tools")
        .inputKey("message")
        .outputKey("response")
    )
    
    // Method 2: Direct LLMActionNode builder
    .action("agent2", LLMActionNode.builder(ai)
        .systemPrompt("You have access to tools")
        .tools(tools)  // Enable tools
        .inputKey("message")
        .outputKey("response")
        .build()
    )
    
    .edge("webhook", "agent")
    .build();
```

### How It Works

1. **Tool Definition**: Tools are LangChain4j `ToolSpecification` objects
2. **AI Service Configuration**: Tools are passed to `AiServices.builder().tools(tools)` via reflection
3. **Automatic Execution**: LangChain4j AI Services automatically:
   - Decides when to call tools (based on LLM reasoning)
   - Executes tool functions
   - Includes tool results in conversation context
   - Returns final response

### Example: RAG + Tools Combined

```java
// ContentRetriever for RAG
ContentRetriever retriever = ai.ragApi()
    .retriever("kb", 5);  // Top 5 results

// Tools for function calling
List<ToolSpecification> tools = List.of(
    ToolSpecification.builder()
        .name("get_customer_info")
        .description("Get customer information by ID")
        .build(),
    ToolSpecification.builder()
        .name("update_order_status")
        .description("Update order status")
        .build()
);

// Workflow with both RAG and Tools
Workflow workflow = ai.workflow("smart-agent")
    .trigger("webhook", WebhookTrigger.create(...))
    .llm("agent", builder -> builder
        .systemPrompt("You have access to knowledge base and tools")
        .rag(retriever)      // RAG for knowledge retrieval
        .tools(tools)        // Tools for actions
        .memory(chatMemory, "userId")  // Memory for conversation
        .inputKey("message")
        .outputKey("response")
    )
    .edge("webhook", "agent")
    .build();
```

### Current API

- `LLMActionNode.Builder.tools(Object toolSpecs)` - Direct method
- `AIWorkflowBuilder.llmWithTools(String nodeId, Object tools, Consumer<Builder>)` - Convenience method

**Recommendation**: Current API is sufficient. Consider adding:
- Javadoc examples showing tool creation patterns
- Helper methods for common tool types (HTTP calls, database queries, etc.)

## 3. Model Context Protocol (MCP) Support

### Current State ❌

**MCPs are NOT currently implemented** in the AI workflow.

### What Are MCPs?

Model Context Protocol (MCP) is a standardized protocol for:
- **Discoverable Tools**: Tools hosted in repositories, discoverable by AI agents
- **Protocol-Based**: Standardized communication protocol (HTTP/WebSocket)
- **Superpowers**: Pre-built capabilities (database access, file operations, API integrations)
- **Self-Discovery**: AI agents can discover and use MCP tools automatically

### Proposed Integration

**Option A: MCP Client Integration** (Recommended - with built-in caching)
Create an `MCPClient` that discovers and converts MCP tools to LangChain4j `ToolSpecification`:

```java
// MCP Client with built-in caching discovers tools from MCP server
MCPClient mcpClient = new MCPClient("https://mcp-server.example.com")
    .withCache(Duration.ofMinutes(10));  // Cache discovered tools for 10 minutes

// Discover tools (cached automatically)
List<ToolSpecification> mcpTools = mcpClient.discoverTools();

// Use in workflow (same as regular tools)
Workflow workflow = ai.workflow("mcp-agent")
    .trigger("webhook", WebhookTrigger.create(...))
    .llmWithTools("agent", mcpTools, builder -> builder
        .systemPrompt("You have access to MCP tools")
        .inputKey("message")
        .outputKey("response")
    )
    .edge("webhook", "agent")
    .build();
```

**Enhanced Option A: MCP Client with Registry-Like Features**
Combine the simplicity of Option A with the benefits of Option C:

```java
// MCP Client acts as both client and registry
MCPClient mcpClient = MCPClient.builder()
    .server("mcp-server-1", "https://mcp-server.example.com")
    .server("mcp-server-2", "https://another-mcp.example.com")
    .cacheDuration(Duration.ofMinutes(10))
    .healthCheckInterval(Duration.ofMinutes(5))
    .build();

// Auto-discover all tools from all registered servers
List<ToolSpecification> allTools = mcpClient.discoverAllTools();

// Or discover from specific server
List<ToolSpecification> server1Tools = mcpClient.discoverTools("mcp-server-1");

// Use in workflow
Workflow workflow = ai.workflow("mcp-agent")
    .trigger("webhook", WebhookTrigger.create(...))
    .llmWithTools("agent", allTools, builder -> builder
        .systemPrompt("You have access to MCP tools")
    )
    .edge("webhook", "agent")
    .build();
```

**Convenience Method:**
```java
// Add to AIWorkflowBuilder
public AIWorkflowBuilder mcp(String nodeId,
                            MCPClient mcpClient,
                            Consumer<LLMActionNode.Builder> config) {
    List<ToolSpecification> tools = mcpClient.discoverAllTools();
    return llmWithTools(nodeId, tools, config);
}
```

**Option B: MCP Node Type**
Create a dedicated `MCPNode` that automatically discovers and uses MCP tools:

```java
Workflow workflow = ai.workflow("mcp-agent")
    .trigger("webhook", WebhookTrigger.create(...))
    
    // MCP node automatically discovers and uses tools
    .mcp("agent", builder -> builder
        .server("https://mcp-server.example.com")
        .tools("database", "file-system", "api-client")  // Specific tools
        .systemPrompt("You have access to MCP tools")
    )
    
    .edge("webhook", "agent")
    .build();
```

**Option C: MCP Registry**
Create a global MCP registry that caches discovered tools:

```java
// Register MCP servers at startup
MCPRegistry registry = MCPRegistry.getInstance();
registry.register("mcp-server-1", "https://mcp-server.example.com");
registry.register("mcp-server-2", "https://another-mcp.example.com");

// Use in workflow
Workflow workflow = ai.workflow("mcp-agent")
    .trigger("webhook", WebhookTrigger.create(...))
    .llmWithMCP("agent", "mcp-server-1", builder -> builder
        .systemPrompt("...")
        .autoDiscover(true)  // Auto-discover all tools
    )
    .edge("webhook", "agent")
    .build();
```

### Implementation Plan

1. **Phase 1: MCP Client** (Foundation)
   - Create `MCPClient` class
   - Implement MCP protocol communication (HTTP/WebSocket)
   - Parse MCP tool definitions
   - Convert to `ToolSpecification`

2. **Phase 2: MCP Integration** (AI Workflow)
   - Add `MCPNode` to AI workflow builder
   - Implement automatic tool discovery
   - Cache discovered tools

3. **Phase 3: MCP Registry** (Production)
   - Global registry for MCP servers
   - Health checks and monitoring
   - Tool versioning and updates

### Recommendation

Start with **Option A (MCP Client with built-in caching)** because:
- Reuses existing tools infrastructure
- Minimal changes to AI workflow builder
- Built-in caching handles tool discovery efficiently
- Can register multiple servers (registry-like features)
- Health checks and monitoring built-in
- Foundation for future MCP features
- Simple API: `MCPClient.discoverTools()` abstracts away complexity

The MCPClient will handle:
- ✅ Caching discovered tools (configurable TTL)
- ✅ Health checks for MCP servers
- ✅ Multiple server registration
- ✅ Automatic tool conversion to `ToolSpecification`
- ✅ Error handling and retries

## Summary

### ✅ Already Implemented & Available

**Workflow Library Features** (via `.action()` or direct builder methods):
- ✅ **Nested workflows** (via `.nested()` with aggregators and execution strategies)
  - `MergeAllAggregator`, `CollectAllAggregator`, `SelectBestAggregator`
  - `NestedExecutionStrategy.WAIT_FOR_ALL`, `WAIT_FOR_ALL_BEST_EFFORT`, `FIRST_SUCCESS`, `BEST_OF_ALL`
- ✅ **Continuation workflows** (via `.continuation()` for sequential chaining)
- ✅ **Loop nodes** (via `LoopNode` with sequential/parallel/until-success strategies)
- ✅ **Cost tracking** (via `CostTracker` visitor pattern with budget enforcement)
- ✅ **Circuit breaker** (via `CircuitBreakerNode` wrapper for resilience)
- ✅ **Human approval** (via `HumanApprovalNode` for HITM workflows)

**AI Plugin Features**:
- ✅ **Tools support** (via `llmWithTools()` and `LLMActionNode.Builder.tools()`)
  - Full LangChain4j `ToolSpecification` support
  - Automatic tool execution by AI Services
- ✅ **RAG support** (via `rag()` and `llmWithRAG()`)
- ✅ **Memory support** (via `memory()` and `llmWithMemory()`)
- ✅ **All AI workflow builder methods** (`.llm()`, `.extract()`, `.rag()`, `.vectors()`, `.aiService()`, etc.)

### 🔄 Recommended Enhancements

1. **Add convenience methods** for common AI + workflow patterns:
   - `.loop()` - Retry LLM calls with loop
   - `.circuit()` - Protect LLM calls with circuit breaker
   - `.approval()` - Human approval before LLM execution
   - `.costing()` - Create cost tracker for budget management
   - `.mcp()` - MCP client integration for discoverable tools

2. **Add MCP client integration** for discoverable tools:
   - `MCPClient` class with built-in caching
   - Multiple server registration support
   - Health checks and monitoring
   - `.mcp()` convenience method on `AIWorkflowBuilder`
   - Automatic tool discovery and conversion to `ToolSpecification`

3. **Enhance documentation** with:
   - Complete examples combining AI + workflow features
   - Best practices guide
   - Cost tracking integration examples
   - Circuit breaker patterns for AI APIs

### ❌ Not Implemented

- **MCP (Model Context Protocol)** support - needs implementation (see Phase 3 plan above)

### 📚 Key Resources

- **Workflow Library Docs**: See `roya-workflow/README.md`, `QUICKSTART.md`, `USAGE_DEMO.md`
- **AI Plugin Docs**: See `docs/AI_WORKFLOW_MAGIC.md` for AI workflow concepts
- **Examples**: See `roya-workflow/src/main/java/com/akilisha/oss/roya/workflow/examples/` for complete examples

