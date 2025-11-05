# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Roya Workflow** is a lightweight, graph-oriented workflow orchestration framework for Java, designed specifically for AI agent workflows. It provides a zero-dependency, async-first execution engine using Java 21+ virtual threads.

**Key Philosophy:**
- Graph-first architecture (workflows are DAGs, not linear chains)
- Zero external dependencies (self-contained, ships anywhere)
- Async-first (everything uses CompletableFuture)
- Immutable workflows (once built, workflows don't change)
- Isolated execution contexts (each run has isolated state)

## Build Commands

```bash
# Gradle (primary build system)
./gradlew build              # Build project
./gradlew test               # Run all tests
./gradlew runExample         # Run CustomerSupportWorkflow example
./gradlew clean build        # Clean build

# Maven (also supported)
mvn clean install            # Build and install
mvn test                     # Run tests
mvn exec:java -Dexec.mainClass="com.akilisha.oss.roya.workflow.examples.CustomerSupportWorkflow"

# Run specific examples
./gradlew run -PmainClass=com.akilisha.oss.roya.workflow.examples.ContinuationWorkflowExample
```

**Important:** All commands must use `--enable-preview` JVM flag (already configured in build.gradle).

## Core Architecture

### Graph-Based Execution Model

Workflows are directed acyclic graphs (DAGs) where:
- **Nodes (vertices)** = units of work (WorkflowNode interface)
- **Edges** = connections with routing logic, retry policies, error strategies
- **Context** = shared state that flows through the workflow

```
Workflow (immutable)
├── Nodes: Map<String, WorkflowNode>
├── Metadata: Map<String, NodeMetadata>
└── Edges: Map<String, List<Edge>> (adjacency list)

WorkflowExecutor (stateful)
├── Executes nodes reactively based on edge conditions
├── Manages retry logic and error handling
├── Notifies visitors for observability
└── Uses virtual threads for concurrency
```

### Key Abstractions

**WorkflowNode** (core/WorkflowNode.java:18)
```java
@FunctionalInterface
CompletableFuture<NodeOutput> execute(NodeInput input);
```
- All nodes implement this single interface
- Enables lambda-based inline nodes: `.trigger("id", input -> ...)`

**Edge** (edges/Edge.java)
- Defines execution mode (SEQUENTIAL, PARALLEL, ASYNC)
- Contains routing conditions: `Edge.when(ctx -> condition)`
- Specifies retry policy and error strategy
- Edges are first-class citizens in the workflow graph

**ExecutionContext** (core/ExecutionContext.java)
- Thread-safe shared state across workflow execution
- Stores node results, execution trace, metadata
- Supports namespace isolation for child workflows
- Accessed via `input.context().get("key")`

**WorkflowVisitor** (visitor/WorkflowVisitor.java)
- Observer pattern for workflow events
- Used for logging, metrics, tracing, cost tracking
- Multiple visitors can be attached to executor
- Examples: LoggingVisitor, CostTracker

## Package Structure

```
com.akilisha.oss.roya.workflow/
├── core/           - Core abstractions (Workflow, WorkflowNode, NodeInput/Output, ExecutionContext)
├── execution/      - WorkflowExecutor, WorkflowResult, ExecutionEvent
├── edges/          - Edge, ErrorStrategy, ExecutionMode
├── retry/          - RetryPolicy (exponential/linear backoff)
├── visitor/        - WorkflowVisitor interface + implementations
├── nodes/          - Built-in nodes (HttpNode, LLMNode, TransformNode)
├── streaming/      - StreamingNode interface for token streaming
├── continuation/   - ContinuationNode (sequential workflow composition)
├── nested/         - NestedWorkflowNode (parallel child workflows + aggregation)
├── hitm/           - Human-in-the-loop (HumanApprovalNode, ApprovalProvider)
├── cost/           - CostTracker visitor for budget management
├── resilience/     - CircuitBreaker for fault tolerance
└── examples/       - Comprehensive usage examples
```

## Workflow Construction Pattern

Workflows use a **fluent builder** pattern with type-specific methods:

```java
Workflow workflow = Workflow.create()
    // Add nodes by semantic type
    .trigger("start", node)        // Initiates workflow
    .action("process", node)       // Side effects (HTTP, DB, LLM)
    .logic("transform", node)      // Pure transformations
    .conditional("route", node)    // Branching logic
    .custom("special", node)       // User-defined

    // Add edges with configuration
    .edge("start", "process", Edge.sequential()
        .withRetry(RetryPolicy.exponentialBackoff(3, Duration.ofSeconds(1)))
        .onError(ErrorStrategy.SKIP_AND_CONTINUE))

    // Conditional routing
    .edge("route", "pathA", Edge.when(ctx -> ctx.get("type").equals("A")))
    .edge("route", "pathB", Edge.when(ctx -> ctx.get("type").equals("B")))

    .build();  // Returns immutable Workflow
```

**Node Types** (semantic only, doesn't affect execution):
- `TRIGGER` - Entry points (webhooks, cron, events)
- `ACTION` - Side effects (HTTP calls, LLM invocations, DB writes)
- `LOGIC` - Pure transformations (data mapping, routing decisions)
- `CONDITIONAL` - Branching logic
- `CUSTOM` - User-defined

## Advanced Features

### 1. Continuation Workflows (Sequential Composition)
Chain workflows where output of workflow A → input of workflow B.

```java
.continuation("step2", childWorkflow, "startNodeId")
.continuationWithNamespace("step2", childWorkflow, "start", "child1")  // Namespace isolation
```

Context merging: Parent → Child → Parent (child results merge back).

### 2. Nested Workflows (Parallel Composition)
Execute multiple child workflows in parallel, aggregate results.

```java
.nested("parallel", List.of(w1, w2, w3), new MergeAllAggregator())
.nested("parallel", workflows, aggregator, NestedExecutionStrategy.FIRST_SUCCESS)
```

**Execution Strategies:**
- `WAIT_FOR_ALL` - All must succeed
- `WAIT_FOR_ALL_BEST_EFFORT` - Wait for all, continue if some fail
- `FIRST_SUCCESS` - Return as soon as one succeeds
- `BEST_OF_ALL` - Run all, pick best based on scoring

**Built-in Aggregators:**
- `MergeAllAggregator` - Merge all child contexts (later overwrites earlier)
- `CollectAllAggregator` - Keep results separate as list
- `SelectBestAggregator` - Pick best based on custom scoring function

### 3. Human-in-the-Loop (HITM)
Pause workflow for human approval/input/choice.

```java
ApprovalProvider provider = new PollingApprovalProvider();
.action("humanReview", new HumanApprovalNode(provider, "Approve draft email?"))

// Later: provider.submitApproval(requestId, true);
```

Approval types: Boolean, Text, Choice. Pluggable providers (polling, webhook, queue-based).

### 4. Cost Tracking
Track per-node costs, enforce budget limits.

```java
CostTracker tracker = new CostTracker(5.00)  // $5 budget
    .withNodeCost("llmCall", 0.002)
    .withNodeCost("embedding", 0.0001);

executor.addVisitor(tracker);
```

**Cost Calculators:** Fixed, Time-based, Output-based (tokens), Combined, Custom.

### 5. Circuit Breaker
Protect against cascading failures when calling external services.

```java
CircuitBreaker breaker = CircuitBreaker.withThreshold(5, Duration.ofMinutes(1));
.action("llm", new CircuitBreakerNode(new LLMNode(...), breaker))
```

States: CLOSED → OPEN (fail fast) → HALF_OPEN (testing) → CLOSED.

## Creating Custom Nodes

**Simple inline node:**
```java
.action("myNode", input -> {
    String value = input.getString("key");
    // Do work...
    return CompletableFuture.completedFuture(
        NodeOutput.success("result", value)
    );
})
```

**Full class implementation:**
```java
public class MyNode implements WorkflowNode {
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        // Read from context
        String value = input.context().get("key");

        // Write to context
        input.context().set("result", "some value");

        // Return async result
        return CompletableFuture.supplyAsync(() -> {
            // Long-running work...
            return NodeOutput.success("output", result);
        });
    }
}
```

**Streaming node** (for AI token streaming):
```java
public class StreamingLLMNode implements StreamingNode {
    @Override
    public Flow.Publisher<StreamChunk> stream(NodeInput input) {
        return subscriber -> {
            // Stream tokens as they arrive
            subscriber.onNext(StreamChunk.text("token"));
            subscriber.onComplete();
        };
    }
}
```

## Testing Workflows

**Test structure:**
```java
@Test
void testWorkflow() {
    Workflow workflow = Workflow.create()
        .trigger("start", input -> CompletableFuture.completedFuture(
            NodeOutput.success("value", 42)))
        .action("process", input -> CompletableFuture.completedFuture(
            NodeOutput.success("result", input.getInt("value") * 2)))
        .edge("start", "process")
        .build();

    WorkflowResult result = new WorkflowExecutor(workflow)
        .executeFrom("start", Map.of())
        .join();

    assertTrue(result.isSuccess());
    assertEquals(84, result.context().get("result"));
}
```

**Test utilities:** See `src/test/java/com/akilisha/oss/roya/workflow/TestUtils.java` for helpers.

## Important Design Patterns

### Decorator Pattern
CircuitBreakerNode wraps any WorkflowNode:
```java
new CircuitBreakerNode(delegate, breaker)  // Adds circuit breaker behavior
```

### Visitor Pattern
WorkflowVisitor for cross-cutting concerns without coupling:
```java
executor.addVisitor(new LoggingVisitor());
executor.addVisitor(new CostTracker(budget));
executor.addVisitor(new MetricsVisitor());
```

### Builder Pattern
Fluent DSL for workflow construction with compile-time safety.

### Strategy Pattern
- RetryPolicy: exponentialBackoff, linearBackoff, fixedDelay, noRetry
- ErrorStrategy: PROPAGATE, SKIP_AND_CONTINUE, FALLBACK, COMPENSATE
- ExecutionMode: SEQUENTIAL, PARALLEL, ASYNC
- NestedExecutionStrategy: WAIT_FOR_ALL, FIRST_SUCCESS, BEST_OF_ALL

## Error Handling

**Retry Logic:** Configured per-edge
```java
.edge("a", "b", Edge.sequential()
    .withRetry(RetryPolicy.exponentialBackoff(
        5,                        // max attempts
        Duration.ofSeconds(1),    // initial delay
        2.0,                      // multiplier
        Duration.ofMinutes(5)     // max delay cap
    )))
```

**Error Strategies:**
- `PROPAGATE` - Stop workflow, return failure (default)
- `SKIP_AND_CONTINUE` - Log error, continue to next node
- `FALLBACK` - Execute fallback node
- `COMPENSATE` - Execute compensation logic

**Circuit Breaker:** Use for all external service calls to prevent cascade failures.

## Key Files Reference

**Documentation:**
- `README.md` - Complete API reference and quick start
- `PHASE_1_2_SUMMARY.md` - All Phase 1 & 2 features explained
- `FEATURE_BACKLOG.md` - Future features (Phase 3/4, not yet implemented)

**Core Implementation:**
- `core/Workflow.java:29` - `Workflow.create()` entry point
- `core/Workflow.java:74` - WorkflowBuilder with all methods
- `execution/WorkflowExecutor.java:44` - `executeFrom()` execution entry
- `execution/WorkflowExecutor.java:90` - Reactive execution engine

**Examples:** All in `src/main/java/com/akilisha/oss/roya/workflow/examples/`
- `CustomerSupportWorkflow.java` - Complete AI agent workflow
- `ContinuationWorkflowExample.java` - Sequential workflow composition
- `NestedWorkflowExample.java` - Parallel workflows with aggregation
- `CostTrackingExample.java` - Budget management
- `CircuitBreakerExample.java` - Fault tolerance
- `RetryExample.java` - Retry policies
- `StreamingExample.java` - Token streaming
- `ConditionalEdgeExample.java` - Routing logic
- `CompleteAIAgentExample.java` - Kitchen sink example

## Common Development Tasks

**Adding a new feature package:**
1. Create package under `com.akilisha.oss.roya.workflow/`
2. Implement core interfaces (WorkflowNode or WorkflowVisitor)
3. Add builder methods to `Workflow.WorkflowBuilder` if needed
4. Create comprehensive example in `examples/`
5. Add tests under `src/test/java/`

**Extending with custom nodes:**
- Implement `WorkflowNode` interface
- Use existing nodes as reference: `nodes/HttpNode.java`, `nodes/LLMNode.java`
- For streaming: implement `StreamingNode` interface

**Adding observability:**
- Implement `WorkflowVisitor` interface
- See `visitor/LoggingVisitor.java` or `cost/CostTracker.java` as examples
- Add via `executor.addVisitor(new MyVisitor())`

## Current Status

**Phase 1 & 2 Complete:** 5 major features implemented
- ✅ Continuation workflows (sequential composition)
- ✅ Nested workflows (parallel composition)
- ✅ Human-in-the-loop (approval/input)
- ✅ Cost tracking (budget management)
- ✅ Circuit breaker (fault tolerance)

**Total Implementation:**
- 15 new files across 5 packages
- ~1,950 lines of production code
- Zero breaking changes
- 100% backward compatible

**Examples Status:**
- ✅ ContinuationWorkflowExample
- ✅ NestedWorkflowExample
- ✅ CostTrackingExample
- ✅ CircuitBreakerExample
- 🔨 Additional examples in progress

## Notes for AI Assistants

**When adding new features:**
- Maintain zero-dependency principle
- Use CompletableFuture for all async operations
- Implement visitor hooks for observability
- Add comprehensive examples
- Follow existing patterns (decorator, visitor, builder)

**When debugging:**
- Check WorkflowVisitor logs (add LoggingVisitor to executor)
- Inspect ExecutionContext for node results and state
- Review edge conditions and retry policies
- Verify node timeouts (default 60 seconds)

**Architecture constraints:**
- Workflows are immutable after build()
- ExecutionContext is thread-safe (uses ConcurrentHashMap)
- Node execution is async (always return CompletableFuture)
- Virtual threads handle concurrency (no manual thread pools)
