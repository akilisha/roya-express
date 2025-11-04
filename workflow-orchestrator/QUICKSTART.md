# Quick Start Guide

## Project Structure

```
workflow-orchestrator/
├── src/main/java/com/workflow/
│   ├── core/                      # Core abstractions
│   │   ├── WorkflowNode.java      # Base node interface
│   │   ├── NodeInput.java         # Input data container
│   │   ├── NodeOutput.java        # Output data container
│   │   ├── NodeStatus.java        # Execution status enum
│   │   ├── NodeType.java          # Node categorization
│   │   ├── NodeMetadata.java      # Node metadata
│   │   ├── ExecutionContext.java  # Workflow state management
│   │   └── Workflow.java          # Workflow builder & graph
│   │
│   ├── edges/                     # Edge configuration
│   │   ├── Edge.java              # Edge with routing logic
│   │   ├── ExecutionMode.java     # Sequential/Parallel/Async
│   │   └── ErrorStrategy.java     # Error handling strategies
│   │
│   ├── execution/                 # Workflow execution
│   │   ├── WorkflowExecutor.java  # Main orchestrator
│   │   ├── WorkflowResult.java    # Execution result
│   │   └── ExecutionEvent.java    # Event tracing
│   │
│   ├── retry/                     # Retry logic
│   │   ├── RetryPolicy.java       # Retry configuration
│   │   └── BackoffStrategy.java   # Backoff algorithms
│   │
│   ├── streaming/                 # Streaming support
│   │   ├── StreamingNode.java     # Streaming node interface
│   │   ├── StreamChunk.java       # Stream data chunk
│   │   └── ChunkType.java         # Chunk types
│   │
│   ├── visitor/                   # Observability
│   │   ├── WorkflowVisitor.java   # Visitor interface
│   │   └── LoggingVisitor.java    # Logging implementation
│   │
│   ├── nodes/                     # Example node implementations
│   │   ├── LLMNode.java           # AI model calls
│   │   ├── HttpNode.java          # HTTP requests
│   │   └── TransformNode.java     # Data transformation
│   │
│   └── examples/                  # Complete examples
│       └── CustomerSupportWorkflow.java
│
├── pom.xml                        # Maven build file
└── README.md                      # Full documentation
```

## Build & Run

### Prerequisites
- Java 21 or higher
- Maven 3.8+ OR Gradle 8.0+

### Compile

**Using Maven:**
```bash
cd workflow-orchestrator
mvn clean compile
```

**Using Gradle:**
```bash
cd workflow-orchestrator
./gradlew build
```

### Run Example

**Using Maven:**
```bash
mvn exec:java -Dexec.mainClass="com.akilisha.oss.roya.workflow.examples.CustomerSupportWorkflow"
```

**Using Gradle:**
```bash
./gradlew runExample
```

### Package

**Using Maven:**
```bash
mvn clean package
```

**Using Gradle:**
```bash
./gradlew jar
```

### Note on Gradle Wrapper
If `gradlew` is not present, see [GRADLE_SETUP.md](GRADLE_SETUP.md) for setup instructions.
You can always use Maven instead - both build systems are fully supported!

## 5-Minute Tutorial

### 1. Create Your First Node

```java
package com.myapp;

import com.akilisha.oss.roya.workflow.core.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GreetingNode implements WorkflowNode {
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        String name = input.getString("name");
        return CompletableFuture.completedFuture(
            NodeOutput.success("greeting", "Hello, " + name + "!")
        );
    }
}
```

### 2. Build a Simple Workflow

```java
package com.myapp;

import com.akilisha.oss.roya.workflow.core.*;
import com.akilisha.oss.roya.workflow.edges.Edge;
import com.akilisha.oss.roya.workflow.execution.*;
import com.akilisha.oss.roya.workflow.visitor.LoggingVisitor;
import java.util.Map;

public class SimpleWorkflowExample {
    public static void main(String[] args) {
        // Build workflow
        Workflow workflow = Workflow.create()
            .trigger("start", input -> 
                CompletableFuture.completedFuture(NodeOutput.success(input.data())))
            .action("greet", new GreetingNode())
            .action("print", input -> {
                System.out.println(input.getString("greeting"));
                return CompletableFuture.completedFuture(NodeOutput.success());
            })
            .edge("start", "greet")
            .edge("greet", "print")
            .build();
        
        // Execute
        WorkflowExecutor executor = new WorkflowExecutor(workflow)
            .addVisitor(new LoggingVisitor());
        
        WorkflowResult result = executor.executeFrom(
            "start",
            Map.of("name", "World")
        ).join();
        
        System.out.println("Success: " + result.isSuccess());
        
        executor.shutdown();
    }
}
```

### 3. Add Conditional Branching

```java
Workflow workflow = Workflow.create()
    .trigger("start", ...)
    .action("checkAge", ...)
    .action("sendAdultMessage", ...)
    .action("sendMinorMessage", ...)
    
    .edge("start", "checkAge")
    .edge("checkAge", "sendAdultMessage", 
        Edge.when(ctx -> ctx.<Integer>get("age") >= 18))
    .edge("checkAge", "sendMinorMessage",
        Edge.when(ctx -> ctx.<Integer>get("age") < 18))
    
    .build();
```

### 4. Add Retry Logic

```java
import com.akilisha.oss.roya.workflow.retry.RetryPolicy;
import java.time.Duration;

.edge("apiCall", "nextNode", Edge.sequential()
    .withRetry(RetryPolicy.exponentialBackoff(3, Duration.ofSeconds(1)))
)
```

### 5. Parallel Execution

```java
.edge("fetchData", "processA", Edge.parallel())
.edge("fetchData", "processB", Edge.parallel())
.edge("fetchData", "processC", Edge.parallel())

// All three processX nodes will execute concurrently
```

## Common Patterns

### Pattern 1: AI Agent with Retry
```java
Workflow aiAgent = Workflow.create()
    .action("callLLM", new LLMNode("anthropic", "claude-sonnet-4", "..."))
    .action("processResponse", ...)
    
    .edge("callLLM", "processResponse", Edge.sequential()
        .withRetry(RetryPolicy.exponentialBackoff(3, Duration.ofSeconds(2)))
    )
    .build();
```

### Pattern 2: Conditional Routing
```java
.logic("routingDecision", TransformNode.create(data -> {
    String intent = classifyIntent(data);
    return Map.of("route", intent);
}))

.edge("routingDecision", "pathA", Edge.when(ctx -> "A".equals(ctx.get("route"))))
.edge("routingDecision", "pathB", Edge.when(ctx -> "B".equals(ctx.get("route"))))
```

### Pattern 3: Fan-Out/Fan-In
```java
// Fan-out: parallel processing
.edge("fetchData", "process1", Edge.parallel())
.edge("fetchData", "process2", Edge.parallel())
.edge("fetchData", "process3", Edge.parallel())

// Fan-in: merge results
.edge("process1", "merge")
.edge("process2", "merge")
.edge("process3", "merge")
```

## Next Steps

1. **Customize Nodes**: Implement your own `WorkflowNode` for domain-specific logic
2. **Add Observability**: Create custom `WorkflowVisitor` for metrics/tracing
3. **Integrate APIs**: Use `HttpNode` as template for your API integrations
4. **Stream AI Responses**: Implement `StreamingNode` for token streaming
5. **Add Tests**: Write unit tests using the provided patterns

## Tips

- Use `.trigger()` for workflow entry points
- Use `.action()` for side effects (API calls, DB ops)
- Use `.logic()` for data transformation
- Use `.conditional()` for complex branching
- Add retry policies to unreliable operations (API calls)
- Use visitors for debugging and observability
- Keep nodes small and focused (single responsibility)

## Troubleshooting

**Issue**: Workflow hangs
- **Solution**: Check node timeouts, add `.withTimeout()` to nodes

**Issue**: Nodes not executing in order
- **Solution**: Use `Edge.sequential()` instead of `Edge.parallel()`

**Issue**: No logs appearing
- **Solution**: Add `LoggingVisitor` to executor

**Issue**: Compilation errors
- **Solution**: Ensure Java 21+ and Maven are properly installed

## Getting Help

- Check `README.md` for full documentation
- See `CustomerSupportWorkflow.java` for complete example
- Review node implementations in `com.akilisha.oss.roya.workflow.nodes` package

---

**Ready to build your AI agent workflow? Start coding!** 🚀
