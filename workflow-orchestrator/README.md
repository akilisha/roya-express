# Workflow Orchestrator for AI Agents

A lightweight, graph-oriented workflow orchestration framework for Java, designed specifically for AI agent workflows (inspired by n8n).

## Features

✅ **Graph-First Design** - Define workflows as vertices (nodes) and edges with explicit routing logic  
✅ **Type-Safe Nodes** - `.trigger()`, `.action()`, `.logic()`, `.conditional()`, `.custom()` for clear intent  
✅ **Flexible Execution** - Sequential, parallel, and async execution modes  
✅ **Retry Logic** - Built-in exponential/linear backoff with configurable retry policies  
✅ **Error Handling** - Propagate, skip, fallback, or compensate strategies  
✅ **Streaming Support** - First-class support for AI model token streaming  
✅ **Observability** - Visitor pattern for logging, metrics, and tracing  
✅ **Lightweight** - Minimal abstractions, ~2000 lines of code  
✅ **Virtual Threads** - Uses Java 21+ virtual threads for efficient concurrency  

## Quick Start

### 1. Create a Workflow

```java
Workflow workflow = Workflow.create()
    // Add vertices by type
    .trigger("webhookListener", new WebhookNode())
    .action("classifyIntent", new LLMNode("anthropic", "claude-sonnet-4", "Classify user intent"))
    .logic("extractIntent", new TransformNode(data -> {...}))
    .conditional("routeByIntent", new RouterNode())
    .action("generateResponse", new LLMNode("anthropic", "claude-sonnet-4", "Generate response"))
    
    // Add edges with routing logic
    .edge("webhookListener", "classifyIntent")
    .edge("classifyIntent", "extractIntent")
    .edge("extractIntent", "routeByIntent")
    
    // Conditional edges
    .edge("routeByIntent", "fetchBilling", 
        Edge.when(ctx -> "billing".equals(ctx.get("intent"))))
    .edge("routeByIntent", "searchDocs", 
        Edge.when(ctx -> "technical".equals(ctx.get("intent"))))
    
    .edge("fetchBilling", "generateResponse")
    .edge("searchDocs", "generateResponse")
    
    .build();
```

### 2. Execute Workflow

```java
WorkflowExecutor executor = new WorkflowExecutor(workflow)
    .addVisitor(new LoggingVisitor());

WorkflowResult result = executor.executeFrom(
    "webhookListener",
    Map.of("message", "I need help with billing")
).join();

if (result.isSuccess()) {
    System.out.println("Workflow completed!");
}
```

## Core Concepts

### Node Types

- **Trigger** - Initiates workflow (webhooks, cron, events)
- **Action** - Performs side effects (HTTP, DB, LLM calls)
- **Logic** - Data transformation and routing decisions
- **Conditional** - Branching based on conditions
- **Custom** - User-defined functionality

### Edge Configuration

```java
// Sequential execution (default)
.edge("nodeA", "nodeB", Edge.sequential())

// Parallel execution
.edge("nodeA", "nodeB", Edge.parallel())
.edge("nodeA", "nodeC", Edge.parallel())

// Async (fire and forget)
.edge("nodeA", "nodeB", Edge.async())

// Conditional routing
.edge("nodeA", "nodeB", Edge.when(ctx -> ctx.get("status").equals("success")))

// With retry policy
.edge("nodeA", "nodeB", Edge.sequential()
    .withRetry(RetryPolicy.exponentialBackoff(3, Duration.ofSeconds(1))))

// With error handling
.edge("nodeA", "nodeB", Edge.sequential()
    .onError(ErrorStrategy.SKIP_AND_CONTINUE))
```

### Execution Context

Share state across the workflow:

```java
class MyNode implements WorkflowNode {
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        // Read from context
        String value = input.context().get("key");
        
        // Write to context
        input.context().set("result", "some value");
        
        // Access previous node results
        Optional<NodeOutput> previous = input.context().getNodeResult("previousNodeId");
        
        return CompletableFuture.completedFuture(NodeOutput.success(Map.of()));
    }
}
```

### Retry Policies

```java
// No retry
RetryPolicy.noRetry()

// Fixed delay
RetryPolicy.fixedDelay(3, Duration.ofSeconds(2))

// Exponential backoff (doubles each time)
RetryPolicy.exponentialBackoff(5, Duration.ofSeconds(1))

// Custom exponential backoff
RetryPolicy.exponentialBackoff(
    5,                          // max attempts
    Duration.ofSeconds(1),      // initial delay
    2.0,                        // multiplier
    Duration.ofMinutes(5)       // max delay cap
)

// Linear backoff
RetryPolicy.linearBackoff(3, Duration.ofSeconds(1))
```

### Streaming Support

For AI models that stream tokens:

```java
class StreamingLLMNode implements StreamingNode {
    @Override
    public Flow.Publisher<StreamChunk> stream(NodeInput input) {
        return subscriber -> {
            // Stream tokens as they arrive
            subscriber.onNext(StreamChunk.text("Hello"));
            subscriber.onNext(StreamChunk.text(" world"));
            subscriber.onComplete();
        };
    }
}
```

### Observability with Visitors

```java
class MetricsVisitor implements WorkflowVisitor {
    @Override
    public void onNodeComplete(String nodeId, NodeOutput output, Duration time) {
        metrics.timer("workflow.node.duration", "node", nodeId).record(time);
    }
    
    @Override
    public void onNodeError(String nodeId, Throwable error) {
        metrics.counter("workflow.node.errors", "node", nodeId).increment();
    }
}

executor.addVisitor(new MetricsVisitor());
executor.addVisitor(new LoggingVisitor());
executor.addVisitor(new TracingVisitor());
```

## Example Nodes

### LLM Node

```java
public class LLMNode implements WorkflowNode {
    private final String model;
    private final String systemPrompt;
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        String message = input.getString("message");
        String response = callLLMAPI(message);
        return CompletableFuture.completedFuture(
            NodeOutput.success("response", response)
        );
    }
}
```

### HTTP Node

```java
public class HttpNode implements WorkflowNode {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String url;
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
            
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> NodeOutput.success(
                "body", response.body(),
                "statusCode", response.statusCode()
            ));
    }
}
```

### Transform Node

```java
.logic("extractData", TransformNode.create(data -> {
    String response = (String) data.get("llmResponse");
    String intent = extractIntent(response);
    return Map.of("intent", intent);
}))
```

## Architecture

```
Workflow
├── Nodes (vertices)
│   ├── Trigger nodes
│   ├── Action nodes
│   ├── Logic nodes
│   └── Conditional nodes
│
├── Edges (connections)
│   ├── Execution mode (sequential/parallel/async)
│   ├── Routing conditions
│   ├── Retry policies
│   └── Error strategies
│
├── Executor
│   ├── Reactive execution
│   ├── Retry logic
│   ├── Error handling
│   └── Visitor notifications
│
└── Context
    ├── Global state
    ├── Node results
    └── Execution trace
```

## Requirements

- Java 21+ (for virtual threads)
- Maven 3.8+ OR Gradle 8.0+ (both supported)
- No external dependencies for core framework
- Optional: SLF4J, Micrometer for advanced observability

## Testing

```java
@Test
void testWorkflow() {
    Workflow workflow = Workflow.create()
        .trigger("start", input -> CompletableFuture.completedFuture(
            NodeOutput.success("value", 42)
        ))
        .action("process", input -> CompletableFuture.completedFuture(
            NodeOutput.success("result", input.getInt("value") * 2)
        ))
        .edge("start", "process")
        .build();
    
    WorkflowResult result = new WorkflowExecutor(workflow)
        .executeFrom("start", Map.of())
        .join();
    
    assertTrue(result.isSuccess());
    assertEquals(84, result.context().get("result"));
}
```

## Roadmap

- [ ] Workflow state persistence
- [ ] Dynamic graph modification
- [ ] Dead letter queue
- [ ] Circuit breaker pattern
- [ ] Rate limiting
- [ ] Saga pattern for compensation
- [ ] Visual workflow editor (separate project)

## License

MIT License

## Contributing

Contributions welcome! This is a foundational implementation designed to be extended.

## Example: Complete AI Agent Workflow

See `CustomerSupportWorkflow.java` for a complete example of an AI customer support agent that:
1. Receives support requests via webhook
2. Classifies intent with LLM
3. Routes to appropriate data sources (billing DB, documentation)
4. Generates responses with LLM
5. Sends email with retry logic

---

**Built for AI agent workflows. Simple, flexible, production-ready.**
