# General-Purpose Workflow Framework Design

## Vision

A lightweight, composable workflow framework inspired by:
- **n8n**: Visual workflow orchestration (without the GUI)
- **Express.js**: Chainable handlers and middleware pattern
- **Standard Design Patterns**: Builder, Factory, Strategy, Visitor, Chain of Responsibility

## Problem Statement

Create a workflow framework that can orchestrate arbitrary tasks with flexible execution strategies, initially for AI pipelines but extensible to any domain.

## Core Principles

1. **Express.js Semantics**: Handlers are composable, chainable functions
2. **n8n Inspiration**: Nodes can be triggers, actions, logic, or custom functions
3. **Design Pattern Driven**: Leverage proven patterns for flexibility
4. **Execution Strategy Agnostic**: Support linear, parallel, conditional, graph
5. **Context-Driven**: Execution context carries state, services, and metadata

## Architecture

### Core Interfaces

```java
// Core handler interface (like Express Handler)
interface WorkflowNode {
    void execute(WorkflowContext ctx, WorkflowNext next) throws Exception;
}

// Execution context (carries state and services)
interface WorkflowContext {
    // State management
    Map<String, Object> state();
    <T> T get(String key);
    void set(String key, Object value);
    
    // Services access
    <T> T service(Class<T> type);
    
    // Metadata
    String workflowName();
    String nodeName();
    Map<String, Object> metadata();
}

// Flow control (like Express Next)
interface WorkflowNext {
    void proceed() throws Exception;  // Continue to next node
    void skip() throws Exception;     // Skip to next sibling
    void error(Throwable e) throws Exception;  // Propagate error
    void branch(String nodeName) throws Exception;  // Branch to specific node
}
```

### Node Types

1. **Trigger Nodes**: Start workflows (webhook, schedule, event)
2. **Action Nodes**: Perform operations (LLM, database, HTTP, etc.)
3. **Logic Nodes**: Control flow (condition, switch, loop)
4. **Custom Nodes**: User-defined handlers

### Execution Strategies

| Strategy | Implementation | Pattern |
|----------|---------------|---------|
| Linear | Chain nodes sequentially | Chain of Responsibility |
| Parallel | Execute nodes concurrently, wait for all | Command + Thread Pool |
| Conditional | Evaluate condition, branch to path | Strategy |
| Graph | DAG-based execution with dependencies | Visitor + Topological Sort |

### Design Patterns Usage

#### 1. Builder Pattern
```java
Workflow workflow = Workflow.builder("my-workflow")
    .node("trigger", triggerNode)
    .node("action1", actionNode1)
    .node("action2", actionNode2)
    .edge("trigger", "action1")
    .edge("trigger", "action2", Edge.parallel())
    .build();
```

#### 2. Factory Pattern
```java
WorkflowNode node = NodeFactory.create("http-request", config);
WorkflowNode aiNode = NodeFactory.create("llm-chat", llmConfig);
```

#### 3. Strategy Pattern
```java
// Conditional node uses strategy for condition evaluation
ConditionalNode condition = new ConditionalNode(ctx -> ctx.get("value") > 100);
condition.setTrueStrategy(new LinearStrategy());
condition.setFalseStrategy(new ParallelStrategy());
```

#### 4. Visitor Pattern
```java
// Visit workflow for logging, auditing, transformation
WorkflowVisitor logger = new LoggingVisitor();
workflow.accept(logger);

WorkflowVisitor optimizer = new OptimizationVisitor();
workflow.accept(optimizer);
```

#### 5. Chain of Responsibility
```java
// Linear execution is a chain
Chain chain = new Chain(nodes);
chain.execute(ctx);
```

## API Design

### Express.js-Style Handlers

```java
// Simple handler (like Express route handler)
WorkflowNode simpleNode = (ctx, next) -> {
    String input = ctx.get("input");
    String output = process(input);
    ctx.set("output", output);
    next.proceed();
};

// Middleware handler
WorkflowNode authMiddleware = (ctx, next) -> {
    String token = ctx.get("authToken");
    if (!isValid(token)) {
        next.error(new SecurityException("Invalid token"));
        return;
    }
    ctx.set("user", getUser(token));
    next.proceed();
};
```

### Builder API

```java
Workflow workflow = Workflow.builder("receipt-processor")
    // Trigger: HTTP webhook
    .trigger(WebhookTrigger.builder()
        .path("/webhook/receipt")
        .build())
    
    // Middleware: Authentication
    .middleware(AuthMiddleware.builder()
        .required(true)
        .build())
    
    // Action: Extract data
    .node("extract", ExtractNode.builder()
        .input("${receiptText}")
        .output("structuredData")
        .build())
    
    // Logic: Conditional branch
    .node("check-amount", ConditionalNode.builder()
        .condition(ctx -> (Double) ctx.get("total") > 1000)
        .then("flag-large-purchase")
        .elseThen("normal-processing")
        .build())
    
    // Action: Parallel execution
    .node("parallel-tasks", ParallelNode.builder()
        .addNode("embed-items", EmbeddingNode.builder()...build())
        .addNode("analyze-spending", AnalysisNode.builder()...build())
        .build())
    
    // Action: Save result
    .node("save", StorageNode.builder()
        .path("reports/${reportId}.json")
        .data("${results}")
        .build())
    
    .build();
```

### Execution

```java
// Execute workflow
WorkflowContext ctx = WorkflowContext.builder()
    .workflow("receipt-processor")
    .state(Map.of("receiptText", "..."))
    .service(AI.class, aiService)
    .service(Database.class, dbService)
    .build();

WorkflowResult result = workflow.execute(ctx);
```

## Execution Strategies

### 1. Linear (Chain of Responsibility)

```java
class LinearStrategy implements ExecutionStrategy {
    public void execute(List<WorkflowNode> nodes, WorkflowContext ctx) {
        Chain chain = new Chain(nodes);
        chain.execute(ctx);
    }
}
```

### 2. Parallel (Command Pattern)

```java
class ParallelStrategy implements ExecutionStrategy {
    private ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    
    public void execute(List<WorkflowNode> nodes, WorkflowContext ctx) {
        List<CompletableFuture<Void>> futures = nodes.stream()
            .map(node -> CompletableFuture.runAsync(() -> {
                node.execute(ctx, next);
            }, executor))
            .toList();
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}
```

### 3. Conditional (Strategy Pattern)

```java
class ConditionalStrategy implements ExecutionStrategy {
    private Predicate<WorkflowContext> condition;
    private ExecutionStrategy trueStrategy;
    private ExecutionStrategy falseStrategy;
    
    public void execute(List<WorkflowNode> nodes, WorkflowContext ctx) {
        if (condition.test(ctx)) {
            trueStrategy.execute(nodes, ctx);
        } else {
            falseStrategy.execute(nodes, ctx);
        }
    }
}
```

### 4. Graph (Topological Sort)

```java
class GraphStrategy implements ExecutionStrategy {
    private Map<String, List<String>> adjacencyList;
    
    public void execute(Map<String, WorkflowNode> nodes, WorkflowContext ctx) {
        List<String> executionOrder = topologicalSort(adjacencyList);
        for (String nodeName : executionOrder) {
            nodes.get(nodeName).execute(ctx, next);
        }
    }
}
```

## Middleware Support

Middleware nodes can:
- Handle cross-cutting concerns (auth, logging, tracing)
- Alter execution flow (conditional branching)
- Transform context data

```java
// Logging middleware
WorkflowNode loggingMiddleware = (ctx, next) -> {
    System.out.println("Executing node: " + ctx.nodeName());
    long start = System.currentTimeMillis();
    next.proceed();
    long duration = System.currentTimeMillis() - start;
    System.out.println("Node completed in " + duration + "ms");
};

// Auth middleware
WorkflowNode authMiddleware = (ctx, next) -> {
    String token = ctx.get("authToken");
    if (token == null) {
        next.error(new AuthenticationException("Missing token"));
        return;
    }
    ctx.set("user", validateToken(token));
    next.proceed();
};
```

## Benefits

1. **General Purpose**: Not tied to AI - can orchestrate any tasks
2. **Composable**: Express.js-style middleware and handlers
3. **Extensible**: Add new node types via Factory pattern
4. **Testable**: Nodes are simple functions
5. **Flexible**: Multiple execution strategies
6. **Type-Safe**: Java's type system ensures correctness
7. **Performance**: Support virtual threads for parallelism

## Migration Path

1. Create new general-purpose workflow framework (`roya-workflow`)
2. Refactor AI workflow to use it as a domain-specific implementation
3. AI nodes become specialized handlers
4. Keep existing API for backward compatibility

## Implementation Plan

1. **Phase 1**: Core interfaces and context
2. **Phase 2**: Linear execution strategy
3. **Phase 3**: Parallel and conditional strategies
4. **Phase 4**: Graph/DAG execution
5. **Phase 5**: Middleware support
6. **Phase 6**: Visitor pattern for introspection
7. **Phase 7**: Migrate AI workflows to use new framework

