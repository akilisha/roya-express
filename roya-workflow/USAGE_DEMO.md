# Usage Demonstrations - Complete Guide

This guide demonstrates **every feature** of the Workflow Orchestrator through working examples.

## 📚 Table of Contents

1. [Setup & Running Examples](#setup--running-examples)
2. [Phase 1: Workflow Composition](#phase-1-workflow-composition)
    - [Continuation Workflows](#1-continuation-workflows)
    - [Nested Workflows](#2-nested-workflows)
3. [Phase 2: Production Features](#phase-2-production-features)
    - [Cost Tracking](#3-cost-tracking)
    - [Circuit Breaker](#4-circuit-breaker)
4. [Core Features](#core-features)
    - [Retry Logic](#5-retry-logic)
    - [Streaming](#6-streaming)
    - [Conditional Edges](#7-conditional-edges)
5. [Advanced Examples](#advanced-examples)
    - [Loop Node](#8-loop-node-bonus)
    - [Complete AI Agent](#9-complete-ai-agent-kitchen-sink)

---

## Setup & Running Examples

### Prerequisites
```bash
# Java 21+
java --version

# Maven or Gradle
mvn --version
# OR
gradle --version
```

### Compile & Run

```bash
# Navigate to project
cd workflow-orchestrator

# Compile
mvn clean compile
# OR
gradle build

# Run any example
mvn exec:java -Dexec.mainClass="com.workflow.examples.ContinuationWorkflowExample"
# OR
gradle runExample -PmainClass=com.workflow.examples.ContinuationWorkflowExample
```

---

## Phase 1: Workflow Composition

### 1. Continuation Workflows

**File:** `ContinuationWorkflowExample.java`

**What it demonstrates:** Sequential workflow composition - chain workflows where output of one becomes input to the next.

**Use Cases:**
- Multi-stage data pipelines
- ETL workflows (extract → transform → load)
- Document processing (scrape → clean → analyze → report)

#### Code Example

```java
// Define three separate workflows
Workflow dataCollection = Workflow.create()
    .trigger("start", new ScraperNode())
    .action("validate", new ValidationNode())
    .edge("start", "validate")
    .build();

Workflow dataProcessing = Workflow.create()
    .trigger("start", new CleanerNode())
    .action("analyze", new AnalysisNode())
    .edge("start", "analyze")
    .build();

Workflow reportGeneration = Workflow.create()
    .trigger("start", new FormatterNode())
    .action("export", new ExportNode())
    .edge("start", "export")
    .build();

// Chain them together using continuation
Workflow pipeline = Workflow.create()
    .trigger("init", new InputNode())
    .continuation("collect", dataCollection, "start")
    .continuation("process", dataProcessing, "start")
    .continuation("report", reportGeneration, "start")
    .edge("init", "collect")
    .edge("collect", "process")
    .edge("process", "report")
    .build();
```

#### Expected Output

```
=== Continuation Workflow Example ===

📥 Pipeline started with input: https://example.com/data
🕷️  Scraping data from: https://example.com/data
✓ Validating 150 records
🧹 Cleaning data...
📊 Analyzing data...
📝 Formatting report...
💾 Exporting report to: /reports/data-report-1234567890.pdf

==================================================
✅ Pipeline completed successfully!
📊 Data collected: 150
📈 Analysis score: 8.5
📄 Report: /reports/data-report-1234567890.pdf
==================================================
```

#### Key Concepts

- **Context flows automatically** between workflows
- **Each workflow is reusable** independently
- **Use namespaces** to avoid context key collisions:
  ```java
  .continuationWithNamespace("collect", dataCollection, "start", "stage1")
  ```

---

### 2. Nested Workflows

**File:** `NestedWorkflowExample.java`

**What it demonstrates:** Parallel workflow execution with result aggregation.

**Use Cases:**
- Parallel data gathering from multiple sources
- A/B testing multiple approaches
- Fan-out/fan-in patterns
- Ensemble AI models

#### Code Example

```java
// Three independent workflows
Workflow billingWorkflow = Workflow.create()
    .trigger("start", new BillingCheckNode())
    .action("format", new BillingFormatterNode())
    .edge("start", "format")
    .build();

Workflow historyWorkflow = Workflow.create()
    .trigger("start", new HistorySearchNode())
    .action("summarize", new HistorySummarizerNode())
    .edge("start", "summarize")
    .build();

Workflow knowledgeWorkflow = Workflow.create()
    .trigger("start", new KnowledgeSearchNode())
    .action("rank", new RelevanceRankerNode())
    .edge("start", "rank")
    .build();

// Execute all three in parallel
Workflow main = Workflow.create()
    .trigger("ticket", new TicketInputNode())
    .nested("gatherContext",
        List.of(billingWorkflow, historyWorkflow, knowledgeWorkflow),
        new MergeAllAggregator(true), // Merge all contexts
        NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT
    )
    .action("generateResponse", new ResponseGeneratorNode())
    .edge("ticket", "gatherContext")
    .edge("gatherContext", "generateResponse")
    .build();
```

#### Execution Strategies

| Strategy                   | Description                         | When to Use                             |
|----------------------------|-------------------------------------|-----------------------------------------|
| `WAIT_FOR_ALL`             | All must succeed                    | Critical data required from all sources |
| `WAIT_FOR_ALL_BEST_EFFORT` | Wait for all, continue if some fail | Optional data sources                   |
| `FIRST_SUCCESS`            | Return as soon as one succeeds      | Racing multiple APIs                    |
| `BEST_OF_ALL`              | Run all, pick best result           | A/B testing, ensemble models            |

#### Aggregators

| Aggregator             | What It Does          | Example                       |
|------------------------|-----------------------|-------------------------------|
| `MergeAllAggregator`   | Combine all contexts  | Gather data from multiple DBs |
| `CollectAllAggregator` | Keep results separate | Review all API responses      |
| `SelectBestAggregator` | Pick highest scoring  | Choose best AI response       |

#### Expected Output

```
=== Nested Workflow Example ===

🎫 Ticket received:
   Customer: user@example.com
   Issue: Can't access my invoice from last month

   💳 Querying billing database...
   📜 Querying ticket history...
   📚 Searching knowledge base...

🤖 Generating AI response with gathered context...

==================================================
✅ Support workflow completed!

📊 Context Gathered:
   • Billing: Last invoice: INV-2024-10-15 for $99.99 (current)
   • History: Customer has 2 previous tickets. Last issue: Account access problem
   • Knowledge: 2 articles found

💬 Response: I can help you with your invoice. I see your Last invoice: INV-2024-10-15 for $99.99 (current) is available...
==================================================
```

---

## Phase 2: Production Features

### 3. Cost Tracking

**File:** `CostTrackingExample.java`

**What it demonstrates:** Monitor and enforce budget limits for AI API calls.

**Use Cases:**
- Control AI API spending (OpenAI, Anthropic, etc.)
- Per-customer budget limits
- Cost attribution and reporting
- Prevent runaway costs

#### Code Example

```java
// Create cost tracker with $5 budget
CostTracker costTracker = new CostTracker(5.00) // Strict mode
    .withNodeCost("embed", 0.0001)       // $0.0001 per embedding
    .withNodeCost("classify", 0.001)     // $0.001 per classification
    .withNodeCost("generate", 0.005)     // $0.005 per generation
    .withNodeCost("summarize", 0.002);   // $0.002 per summary

Workflow workflow = Workflow.create()
    .trigger("start", new InputNode())
    .action("embed", new EmbeddingNode())
    .action("classify", new ClassificationNode())
    .action("generate", new ContentGenerationNode())
    .action("summarize", new SummaryNode())
    .edge("start", "embed")
    .edge("embed", "classify")
    .edge("classify", "generate")
    .edge("generate", "summarize")
    .build();

// Add cost tracker as visitor
WorkflowExecutor executor = new WorkflowExecutor(workflow)
    .addVisitor(costTracker);
```

#### Cost Calculators

```java
// Fixed cost per execution
NodeCostCalculator.fixed(0.002)

// Time-based cost
NodeCostCalculator.timeBasedCost(0.01) // $0.01 per second

// Output-based (e.g., tokens)
NodeCostCalculator.outputBased("tokens", 0.00001) // $0.00001 per token

// Combined
NodeCostCalculator.combined(
    NodeCostCalculator.fixed(0.001),
    NodeCostCalculator.outputBased("tokens", 0.00002)
)
```

#### Expected Output

```
=== Cost Tracking Example ===

📊 Example 1: Strict Budget ($0.05 limit)

📥 Input: Write a blog post about AI
🔢 Creating embeddings... (cost: $0.0001)
🏷️  Classifying content... (cost: $0.001)
✍️  Generating content... (cost: $0.005)
💰 BUDGET EXCEEDED!
   Total cost: $0.0061
   Budget limit: $0.0500
   Overage: $0.0011

💵 Cost Report:
   Cost Report: $0.0061 / $0.0500 (12.2%) - Most expensive: generate
   Node breakdown:
      • embed: $0.0001
      • classify: $0.001
      • generate: $0.005
```

#### Modes

- **Strict Mode** (default): Throws `BudgetExceededException` when limit exceeded
- **Warning Mode**: Logs warnings but continues execution

```java
// Warning mode
CostTracker tracker = new CostTracker(
    5.00,                          // Budget
    NodeCostCalculator.free(),     // Default
    false                          // Non-strict
);
```

---

### 4. Circuit Breaker

**File:** `CircuitBreakerExample.java`

**What it demonstrates:** Protect workflows from cascading failures with fail-fast pattern.

**Use Cases:**
- Calling flaky AI APIs
- External service dependencies
- Rate-limited APIs
- Preventing retry storms

#### Code Example

```java
// Create circuit breaker: 3 failures → open, wait 1 minute
CircuitBreaker breaker = CircuitBreaker.withThreshold(3, Duration.ofMinutes(1));

// Wrap node with circuit breaker protection
CircuitBreakerNode protectedNode = new CircuitBreakerNode(
    new FlakyLLMNode(),  // Your potentially failing node
    breaker,
    true                 // Include stats in output
);

Workflow workflow = Workflow.create()
    .trigger("start", new InputNode())
    .action("llmCall", protectedNode)
    .edge("start", "llmCall")
    .build();
```

#### Circuit States

```
CLOSED (Normal)
    ↓ (3 failures)
OPEN (Fail Fast)
    ↓ (1 minute timeout)
HALF_OPEN (Testing)
    ↓ (1 success)
CLOSED (Recovered)
```

#### Expected Output

```
=== Circuit Breaker Example ===

⚡ Example 1: Basic Circuit Breaker

Attempt 1:
      🔴 LLM API call failed (timeout/error)
   ❌ Failed: LLM API timeout
   🔌 Circuit: CLOSED (failures: 1/3)

Attempt 2:
      🔴 LLM API call failed (timeout/error)
   ❌ Failed: LLM API timeout
   🔌 Circuit: CLOSED (failures: 2/3)

Attempt 3:
      🔴 LLM API call failed (timeout/error)
   ❌ Failed: LLM API timeout
   🔌 Circuit: OPEN (failures: 3/3)

Attempt 4:
   ❌ Failed: Circuit breaker is OPEN (failures: 3/3). Service unavailable.
   🔌 Circuit: OPEN (failures: 3/3)

[Wait 1 minute...]

Attempt 5:
      🟢 LLM API call succeeded
   ✅ Success!
   🔌 Circuit: CLOSED (recovered)
```

#### Multiple Circuit Breakers

```java
// Different breakers for different services
CircuitBreaker llmBreaker = CircuitBreaker.withThreshold(3, Duration.ofMinutes(2));
CircuitBreaker dbBreaker = CircuitBreaker.withThreshold(5, Duration.ofSeconds(30));

Workflow workflow = Workflow.create()
    .action("llm", new CircuitBreakerNode(llmNode, llmBreaker))
    .action("db", new CircuitBreakerNode(dbNode, dbBreaker))
    // ...
```

---

## Core Features

### 5. Retry Logic

**File:** `RetryExample.java`

**What it demonstrates:** Fault tolerance with exponential backoff.

**Use Cases:**
- Transient failures
- Network timeouts
- Rate limit handling
- Service initialization delays

#### Code Example

```java
// Using RetryPolicy
RetryPolicy policy = RetryPolicy.exponentialBackoff(
    5,                          // Max attempts
    Duration.ofMillis(100)      // Initial delay
);

// Manual retry pattern
WorkflowResult result = null;
for (int attempt = 1; attempt <= policy.getMaxAttempts(); attempt++) {
    result = executor.executeFrom("start", input).join();
    
    if (result.isSuccess()) {
        break;
    } else {
        Duration delay = policy.calculateDelay(attempt);
        Thread.sleep(delay.toMillis());
    }
}
```

#### Retry Wrapper Node

```java
// Wrap any node with retry logic
WorkflowNode protected = new RetryWrapperNode(
    new FlakyNode(),
    3,                          // Max attempts
    Duration.ofMillis(500)      // Delay between attempts
);
```

#### Expected Output

```
=== Retry Example ===

Example 2: Exponential Backoff Pattern

Attempt 1:
  ❌ Failed
  Waiting 100ms before retry...

Attempt 2:
  ❌ Failed
  Waiting 200ms before retry...

Attempt 3:
  ❌ Failed
  Waiting 400ms before retry...

Attempt 4:
  ✅ Success!

Final result: ✅ Success
```

---

### 6. Streaming

**File:** `StreamingExample.java`

**What it demonstrates:** Real-time streaming responses from LLMs.

**Use Cases:**
- LLM token streaming
- Real-time progress updates
- Incremental results
- Better user experience

#### Code Example

```java
public class StreamingLLMNode implements StreamingNode {
    @Override
    public Flow.Publisher<StreamChunk> stream(NodeInput input) {
        return subscriber -> {
            subscriber.onSubscribe(new Flow.Subscription() {
                @Override
                public void request(long n) {
                    // Stream tokens one by one
                    String[] tokens = response.split(" ");
                    for (String token : tokens) {
                        subscriber.onNext(new StreamChunk(
                            token + " ",
                            ChunkType.TEXT,
                            Map.of("word", token)
                        ));
                        Thread.sleep(100); // Simulate streaming
                    }
                    
                    // Final chunk
                    subscriber.onNext(new StreamChunk(
                        "",
                        ChunkType.COMPLETION,
                        Map.of("complete", true)
                    ));
                    subscriber.onComplete();
                }
                
                @Override
                public void cancel() {
                    // Handle cancellation
                }
            });
        };
    }
}
```

#### Chunk Types

- `ChunkType.TEXT` - Regular text chunk
- `ChunkType.COMPLETION` - Final chunk (end of stream)
- `ChunkType.FUNCTION_CALL` - Function call chunk
- `ChunkType.TOOL_USE` - Tool usage chunk

#### Expected Output

```
=== Streaming Example ===

Example 1: Basic Streaming LLM

🤖 Streaming: Quantum computing uses quantum mechanics principles like superposition and entanglement to process information.

✅ Streaming complete!
Full response: Quantum computing uses quantum mechanics principles...
```

---

### 7. Conditional Edges

**File:** `ConditionalEdgeExample.java`

**What it demonstrates:** Dynamic routing based on context conditions.

**Use Cases:**
- Workflow branching
- Priority-based routing
- Content classification
- A/B testing

#### Code Example

```java
Workflow workflow = Workflow.create()
    .trigger("input", new InputNode())
    .action("classify", new SentimentClassifier())
    
    // Route based on sentiment
    .action("positive", new PositiveResponseNode())
    .action("negative", new NegativeResponseNode())
    
    .edge("input", "classify")
    .edge("classify", "positive", Edge.when(ctx -> 
        "positive".equals(ctx.get("sentiment"))
    ))
    .edge("classify", "negative", Edge.when(ctx -> 
        "negative".equals(ctx.get("sentiment"))
    ))
    .build();
```

#### Multi-Way Routing

```java
.edge("categorize", "billing", Edge.when(ctx -> 
    "billing".equals(ctx.get("category"))
))
.edge("categorize", "technical", Edge.when(ctx -> 
    "technical".equals(ctx.get("category"))
))
.edge("categorize", "sales", Edge.when(ctx -> 
    "sales".equals(ctx.get("category"))
))
.edge("categorize", "general", Edge.when(ctx -> 
    ctx.get("category") == null || "general".equals(ctx.get("category"))
))
```

#### Priority-Based Routing

```java
.edge("assess", "urgent", Edge.when(ctx -> {
    Integer priority = (Integer) ctx.get("priority");
    return priority != null && priority >= 90;
}))
.edge("assess", "high", Edge.when(ctx -> {
    Integer priority = (Integer) ctx.get("priority");
    return priority != null && priority >= 70 && priority < 90;
}))
```

#### Expected Output

```
=== Conditional Edge Example ===

Example 1: Simple Routing

Input: I love this product!
Classified as: positive
Response: Thank you for the positive feedback! 😊

Input: This is terrible.
Classified as: negative
Response: We're sorry to hear that. We'll work to improve.
```

---

## Advanced Examples

### 8. Loop Node (Bonus)

**File:** `LoopNodeExample.java`

**What it demonstrates:** Repeating task execution with various strategies.

**Use Cases:**
- A/B testing (run same task multiple times)
- Monte Carlo simulations
- Sampling and averaging
- Batch processing

#### Code Example

```java
// Sequential - one after another
LoopNode loop = new LoopNode(
    new CounterNode(),
    5,
    LoopNode.LoopStrategy.SEQUENTIAL
);

// Parallel - all at once
LoopNode loop = new LoopNode(
    new TaskNode(),
    10,
    LoopNode.LoopStrategy.PARALLEL
);

// Until success - stop on first success
LoopNode loop = new LoopNode(
    new FlakyNode(),
    5,
    LoopNode.LoopStrategy.UNTIL_SUCCESS
);

// Average results
LoopNode loop = new LoopNode(
    new ScoreNode(),
    100,
    LoopNode.LoopStrategy.PARALLEL,
    new LoopNode.AverageResults("score"),
    Duration.ofMinutes(2)
);
```

#### Strategies

| Strategy        | Description               | Use Case           |
|-----------------|---------------------------|--------------------|
| `SEQUENTIAL`    | Run iterations one by one | Ordered processing |
| `PARALLEL`      | Run all simultaneously    | Independent tasks  |
| `UNTIL_SUCCESS` | Stop on first success     | Retry until works  |
| `UNTIL_FAILURE` | Stop on first failure     | Test until breaks  |

#### Aggregators

- `CollectAllResults` - Collect all outputs
- `AverageResults` - Average numeric values
- `SelectBestResult` - Pick highest score
- `CountResults` - Success/failure ratio

#### Expected Output

```
=== Loop Node Example ===

Example 1: Sequential Loop (5 iterations)

  Iteration 1
  Iteration 2
  Iteration 3
  Iteration 4
  Iteration 5

✅ Loop completed!
Total iterations: 5
Success count: 5

Example 2: Parallel Loop (5 iterations)

  Task 1 starting (will take 234ms)
  Task 2 starting (will take 412ms)
  Task 3 starting (will take 156ms)
  Task 4 starting (will take 389ms)
  Task 5 starting (will take 278ms)
  Task 3 completed
  Task 1 completed
  Task 5 completed
  Task 4 completed
  Task 2 completed

✅ Parallel loop completed!
Total time: 412ms (all ran in parallel)
```

---

### 9. Complete AI Agent (Kitchen Sink)

**File:** `CompleteAIAgentExample.java`

**What it demonstrates:** Everything combined into a real-world AI agent.

**Features Used:**
- ✅ Continuation workflows (analysis pipeline)
- ✅ Nested workflows (parallel data gathering)
- ✅ Human-in-the-loop (approval for urgent tickets)
- ✅ Cost tracking ($1 budget)
- ✅ Circuit breaker (LLM + DB protection)
- ✅ Conditional edges (urgency routing)

#### Architecture

```
Ticket Input
    ↓
Classify Urgency (with circuit breaker)
    ↓
Route (urgent vs normal)
    ↓
Gather Context (nested parallel workflows)
    • Billing Database
    • Support History
    • Knowledge Base
    ↓
Analyze (continuation workflow)
    ↓
Generate Response (with circuit breaker)
    ↓
Human Approval (if urgent)
    ↓
Send Response
```

#### Code Example

```java
// Setup infrastructure
ApprovalProvider approvalProvider = new PollingApprovalProvider();
CircuitBreaker llmBreaker = CircuitBreaker.withThreshold(3, Duration.ofSeconds(5));
CircuitBreaker dbBreaker = CircuitBreaker.withThreshold(5, Duration.ofSeconds(3));
CostTracker costTracker = new CostTracker(1.00)
    .withNodeCost("classify", 0.001)
    .withNodeCost("generateResponse", 0.005);

// Build nested workflows
Workflow billingCheck = createBillingWorkflow(dbBreaker);
Workflow historySearch = createHistoryWorkflow(dbBreaker);
Workflow knowledgeSearch = createKnowledgeWorkflow();

// Main workflow combining everything
Workflow main = Workflow.create()
    .trigger("ticket", new TicketInputNode())
    .action("classify", new CircuitBreakerNode(
        new UrgencyClassifierNode(),
        llmBreaker
    ))
    .nested("gatherContext",
        List.of(billingCheck, historySearch, knowledgeSearch),
        new MergeAllAggregator(true),
        NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT
    )
    .action("generateResponse", new CircuitBreakerNode(
        new ResponseGeneratorNode(),
        llmBreaker
    ))
    .action("humanApproval", new HumanApprovalNode(
        approvalProvider,
        "Approve response?"
    ))
    .edge("ticket", "classify")
    .edge("classify", "gatherContext")
    .edge("gatherContext", "generateResponse")
    .edge("generateResponse", "humanApproval", Edge.when(ctx -> 
        "urgent".equals(ctx.get("urgency"))
    ))
    .edge("generateResponse", "send", Edge.when(ctx -> 
        !"urgent".equals(ctx.get("urgency"))
    ))
    .edge("humanApproval", "send")
    .build();

// Execute with cost tracking
WorkflowExecutor executor = new WorkflowExecutor(main)
    .addVisitor(costTracker)
    .addVisitor(new LoggingVisitor());
```

#### Expected Output

```
=== Complete AI Agent: Customer Support ===

🎫 Ticket received:
   Customer: user@example.com
   Issue: Cannot access my account after password reset

🔍 Classifying urgency...
   ➡️  Classified as: urgent

🚨 Routing as URGENT

   💳 Querying billing database...
   📜 Querying ticket history...
   📚 Searching knowledge base...

   😊 Analyzing sentiment...

🤖 Generating AI response...

👤 Human approving request...
✅ Approved request: abc-123

📧 Sending response to customer...

============================================================
✅ Customer support workflow completed!

📊 Summary:
   Urgency: urgent
   Billing: active
   History: 2
   Response: Based on your active status and frustrated, we can help...
   Sent: true

💰 Cost Report:
   Cost Report: $0.0060 / $1.0000 (0.6%) - Most expensive: generateResponse
   Budget remaining: $0.9940

🔌 Circuit Breaker Stats:
   LLM: CircuitBreaker[state=CLOSED, failures=0/3, successes=2]
   Database: CircuitBreaker[state=CLOSED, failures=0/5, successes=2]
============================================================
```

---

## 🎓 Best Practices

### 1. Start Simple
```java
// Start with basic workflow
Workflow simple = Workflow.create()
    .trigger("start", new MyNode())
    .build();

// Add features incrementally
```

### 2. Use Circuit Breakers for External Calls
```java
// Protect all external API calls
.action("api", new CircuitBreakerNode(apiNode, breaker))
```

### 3. Track Costs for AI APIs
```java
// Always track AI API costs
CostTracker tracker = new CostTracker(budgetLimit)
    .withNodeCost("llm", llmCost);
executor.addVisitor(tracker);
```

### 4. Add Human Oversight for High-Stakes Decisions
```java
// Require approval for important actions
.action("approval", new HumanApprovalNode(provider, "Approve?"))
```

### 5. Use Nested Workflows for Parallel Data Gathering
```java
// Gather from multiple sources simultaneously
.nested("gather", List.of(db, api, cache), aggregator)
```

### 6. Combine Features
```java
// Circuit breaker + Cost tracking + Human approval
.action("critical", 
    new CircuitBreakerNode(
        new ExpensiveAINode(),
        breaker
    )
)
.action("approval", new HumanApprovalNode(provider, "Approve?"))
```

---

## 🐛 Troubleshooting

### Compilation Errors

**Issue:** `RetryConfig not found`
```bash
# RetryConfig doesn't exist, use RetryPolicy
import com.workflow.retry.RetryPolicy;
```

**Issue:** `StreamChunk constructor error`
```bash
# Use ChunkType instead of boolean
new StreamChunk(content, ChunkType.TEXT, metadata)
new StreamChunk("", ChunkType.COMPLETION, metadata)
```

**Issue:** `containsKey() method not found`
```bash
# Use null check instead
if (context.get("key") != null) { ... }
```

### Runtime Issues

**Issue:** Circuit breaker always open
```bash
# Reset the breaker
breaker.reset();

# Or check state
System.out.println(breaker.getState());
```

**Issue:** Budget exceeded immediately
```bash
# Check your cost configuration
System.out.println("Cost: $" + tracker.getTotalCost());
System.out.println("Budget: $" + tracker.getBudgetLimit());
```

**Issue:** Nested workflows not completing
```bash
# Check timeout
.nested("gather", workflows, aggregator, strategy, Duration.ofMinutes(5))

# Use WAIT_FOR_ALL_BEST_EFFORT to continue on failures
```

---

## 📊 Feature Compatibility Matrix

| Feature           | Works With     | Notes                                |
|-------------------|----------------|--------------------------------------|
| Continuation      | All            | Can wrap any workflow                |
| Nested            | All            | Can nest workflows with any features |
| Cost Tracking     | All            | Visitor pattern, works everywhere    |
| Circuit Breaker   | All nodes      | Decorator pattern                    |
| Retry             | All nodes      | Manual or wrapper pattern            |
| Streaming         | Specific nodes | Must implement StreamingNode         |
| Conditional Edges | All            | Graph-level feature                  |
| Loop Node         | All nodes      | Decorator pattern                    |
| HITL              | All workflows  | Works anywhere                       |

---

## 🚀 Next Steps

1. **Try the examples** - Run each example to see features in action
2. **Combine features** - Use multiple features in your workflows
3. **Build your agent** - Start with `CompleteAIAgentExample` as template
4. **Extend the framework** - Create custom nodes, aggregators, visitors
5. **Read the docs** - See `README.md`, `QUICKSTART.md`, `PHASE_1_2_SUMMARY.md`

---

## 📝 Summary

| Example                     | Features Demonstrated  | Lines | Complexity |
|-----------------------------|------------------------|-------|------------|
| ContinuationWorkflowExample | Sequential composition | 168   | ⭐⭐         |
| NestedWorkflowExample       | Parallel execution     | 200   | ⭐⭐         |
| CostTrackingExample         | Budget management      | 280   | ⭐⭐         |
| CircuitBreakerExample       | Resilience             | 350   | ⭐⭐⭐        |
| RetryExample                | Fault tolerance        | 240   | ⭐⭐         |
| StreamingExample            | Real-time streaming    | 380   | ⭐⭐⭐        |
| ConditionalEdgeExample      | Dynamic routing        | 310   | ⭐⭐         |
| LoopNodeExample             | Repeated execution     | 250   | ⭐⭐         |
| CompleteAIAgentExample      | **Everything**         | 400   | ⭐⭐⭐⭐       |

**Total:** 9 examples, ~2,500 lines of working code

---

**Happy Building!** 🎉

For questions or issues, refer to the main documentation in `README.md` or explore the code in `/src/main/java/com/workflow/examples/`
