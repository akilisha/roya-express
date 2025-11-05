# Nested & Continuation Workflows with AI

> **Complete guide to combining workflow orchestration patterns with AI operations**

This document demonstrates how to leverage **nested workflows** (parallel execution) and **continuation workflows** (sequential chaining) in AI-powered workflows. These patterns enable complex, production-ready AI agents that gather context, process data, and make decisions.

---

## Table of Contents

1. [Overview](#overview)
2. [Nested Workflows with AI](#nested-workflows-with-ai)
3. [Continuation Workflows with AI](#continuation-workflows-with-ai)
4. [Combining Both Patterns](#combining-both-patterns)
5. [Real-World Examples](#real-world-examples)
6. [Best Practices](#best-practices)

---

## Overview

### What Are Nested Workflows?

**Nested workflows** execute multiple child workflows **in parallel** and aggregate their results. Perfect for:
- Gathering context from multiple sources simultaneously
- Running independent AI operations concurrently
- Aggregating results from parallel data processing

### What Are Continuation Workflows?

**Continuation workflows** chain workflows **sequentially**, where each workflow's output becomes the next workflow's input. Perfect for:
- Multi-stage data processing pipelines
- Sequential AI operations (extract → analyze → generate)
- Reusable workflow components

### Key Differences

| Pattern | Execution | Use Case | Aggregation |
|---------|-----------|----------|-------------|
| **Nested** | Parallel | Gather context from multiple sources | Required (MergeAllAggregator, etc.) |
| **Continuation** | Sequential | Multi-stage pipeline | Automatic (context passes through) |

---

## Nested Workflows with AI

### Basic Pattern

```java
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.WebhookTrigger;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.nested.*;

// Create child workflows
Workflow billingWorkflow = ai.workflow("billing-check")
    .trigger("start", input -> {
        String customerId = input.getString("customerId");
        // Simulate billing check
        return CompletableFuture.completedFuture(
            NodeOutput.success(Map.of(
                "billingStatus", "current",
                "lastInvoice", "INV-2024-10-15",
                "amount", "$99.99"
            ))
        );
    })
    .build();

Workflow historyWorkflow = ai.workflow("history-search")
    .trigger("start", input -> {
        String customerId = input.getString("customerId");
        // Simulate history search
        return CompletableFuture.completedFuture(
            NodeOutput.success(Map.of(
                "previousTickets", 3,
                "lastIssue", "Account access problem"
            ))
        );
    })
    .build();

Workflow ragWorkflow = ai.workflow("knowledge-search")
    .trigger("start", input -> {
        String issue = input.getString("issue");
        // Use RAG to search knowledge base
        return CompletableFuture.completedFuture(
            NodeOutput.success(Map.of(
                "relevantArticles", List.of(
                    "How to access invoices",
                    "Invoice download troubleshooting"
                )
            ))
        );
    })
    .rag("search", builder -> builder
        .inputKey("issue")
        .outputKey("knowledge")
        .options(RAGOptions.builder()
            .collection("support-kb")
            .topK(5)
            .build())
    )
    .edge("start", "search")
    .build();

// Main workflow with nested parallel execution
Workflow supportAgent = ai.workflow("support-agent")
    .trigger("webhook", WebhookTrigger.create("/support", "POST"))
    
    // Execute all three workflows in parallel
    .nested("gatherContext",
        List.of(billingWorkflow, historyWorkflow, ragWorkflow),
        new MergeAllAggregator(true),  // Include metadata
        NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT  // Continue even if one fails
    )
    
    // Generate AI response with gathered context
    .llm("generateResponse", builder -> builder
        .systemPrompt("You are a customer support agent. " +
                     "Use the gathered context to provide a helpful response.")
        .inputKey("context")  // Contains merged results from nested workflows
        .outputKey("response")
    )
    
    .edge("webhook", "gatherContext")
    .edge("gatherContext", "generateResponse")
    .build();
```

### What Happens

1. **Webhook triggered** → Customer sends support request
2. **Nested workflows execute in parallel:**
   - `billingWorkflow` checks billing status
   - `historyWorkflow` searches support history
   - `ragWorkflow` searches knowledge base with RAG
3. **Aggregation** → `MergeAllAggregator` combines all results into context
4. **AI generation** → LLM generates response using combined context

### Aggregation Strategies

#### MergeAllAggregator (Default)
Merges all child workflow results into a single context map:

```java
.nested("gatherContext",
    List.of(workflow1, workflow2, workflow3),
    new MergeAllAggregator(true),  // includeMetadata = true
    NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT
)
```

**Result:** All data from all workflows available in context.

#### CollectAllAggregator
Collects results as a list:

```java
import com.akilisha.oss.roya.workflow.nested.CollectAllAggregator;

.nested("parallelResults",
    List.of(workflow1, workflow2, workflow3),
    new CollectAllAggregator(),
    NestedExecutionStrategy.WAIT_FOR_ALL
)
```

**Result:** `context.get("results")` contains `List<NodeOutput>`.

#### SelectBestAggregator
Selects the best result based on a scoring function:

```java
import com.akilisha.oss.roya.workflow.nested.SelectBestAggregator;

.nested("bestResult",
    List.of(workflow1, workflow2, workflow3),
    new SelectBestAggregator(result -> {
        // Score based on relevance, confidence, etc.
        return result.context().get("score", 0.0);
    }),
    NestedExecutionStrategy.BEST_OF_ALL
)
```

**Result:** Only the highest-scoring result available in context.

### Execution Strategies

| Strategy | Behavior | Use Case |
|----------|----------|----------|
| `WAIT_FOR_ALL` | Wait for all workflows to complete (fail fast on error) | All data required |
| `WAIT_FOR_ALL_BEST_EFFORT` | Wait for all, continue even if some fail | Partial data acceptable |
| `FIRST_SUCCESS` | Use first successful result | Fastest response wins |
| `BEST_OF_ALL` | Compare all results and pick best | Quality over speed |

---

## Continuation Workflows with AI

### Basic Pattern

```java
// Stage 1: Data Collection Workflow
Workflow dataCollection = ai.workflow("data-collection")
    .trigger("start", input -> {
        String source = input.getString("source");
        // Simulate data collection
        return CompletableFuture.completedFuture(
            NodeOutput.success(Map.of(
                "rawData", "[scraped content...]",
                "recordCount", 150
            ))
        );
    })
    .build();

// Stage 2: Data Processing Workflow (with AI)
Workflow dataProcessing = ai.workflow("data-processing")
    .trigger("start", input -> {
        String rawData = input.getString("rawData");
        
        // Clean data
        return CompletableFuture.completedFuture(
            NodeOutput.success(Map.of("cleanedData", "[cleaned content]"))
        );
    })
    .llm("analyze", builder -> builder
        .systemPrompt("Analyze the data and extract insights")
        .inputKey("cleanedData")
        .outputKey("analysis")
    )
    .edge("start", "analyze")
    .build();

// Stage 3: Report Generation Workflow (with AI)
Workflow reportGeneration = ai.workflow("report-generation")
    .trigger("start", input -> {
        String analysis = input.getString("analysis");
        
        // Format report
        return CompletableFuture.completedFuture(
            NodeOutput.success(Map.of("formattedReport", "[report content]"))
        );
    })
    .llm("generateSummary", builder -> builder
        .systemPrompt("Generate a concise executive summary")
        .inputKey("formattedReport")
        .outputKey("summary")
    )
    .edge("start", "generateSummary")
    .build();

// Main pipeline: Chain all three stages
Workflow pipeline = ai.workflow("data-pipeline")
    .trigger("webhook", WebhookTrigger.create("/pipeline", "POST"))
    
    // Continuation 1: Data collection
    .continuation("collect", dataCollection, "start")
    
    // Continuation 2: Data processing
    .continuation("process", dataProcessing, "start")
    
    // Continuation 3: Report generation
    .continuation("report", reportGeneration, "start")
    
    .edge("webhook", "collect")
    .edge("collect", "process")
    .edge("process", "report")
    .build();
```

### What Happens

1. **Webhook triggered** → Input data received
2. **Stage 1 executes** → Data collected, results in context
3. **Stage 2 executes** → Processes data with AI, results added to context
4. **Stage 3 executes** → Generates report with AI, final results in context

### Context Flow

Each continuation workflow receives the **full context** from the previous stage:

```java
// Stage 1 output
context: { "rawData": "...", "recordCount": 150 }

// Stage 2 input (receives Stage 1 context)
context: { "rawData": "...", "recordCount": 150 }
// Stage 2 adds to context
context: { "rawData": "...", "recordCount": 150, "cleanedData": "...", "analysis": "..." }

// Stage 3 input (receives Stage 1 + Stage 2 context)
context: { "rawData": "...", "recordCount": 150, "cleanedData": "...", "analysis": "...", "formattedReport": "...", "summary": "..." }
```

---

## Combining Both Patterns

### Real-World Example: Customer Support Agent

```java
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.WebhookTrigger;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.nested.*;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;

// ========== Stage 1: Context Gathering (Nested) ==========

// Child workflow: Billing check
Workflow billingCheck = ai.workflow("billing-check")
    .trigger("start", input -> {
        String customerId = input.getString("customerId");
        // Simulate billing check
        return CompletableFuture.completedFuture(
            NodeOutput.success(Map.of(
                "billingStatus", "current",
                "lastInvoice", "INV-2024-10-15"
            ))
        );
    })
    .build();

// Child workflow: Support history search
Workflow historySearch = ai.workflow("history-search")
    .trigger("start", input -> {
        String customerId = input.getString("customerId");
        // Simulate history search
        return CompletableFuture.completedFuture(
            NodeOutput.success(Map.of(
                "previousTickets", 3,
                "lastIssue", "Account access problem"
            ))
        );
    })
    .build();

// Child workflow: Knowledge base search (RAG)
Workflow knowledgeSearch = ai.workflow("knowledge-search")
    .trigger("start", input -> {
        String issue = input.getString("issue");
        return CompletableFuture.completedFuture(
            NodeOutput.success(Map.of("issue", issue))
        );
    })
    .rag("search", builder -> builder
        .inputKey("issue")
        .outputKey("knowledge")
        .options(RAGOptions.builder()
            .collection("support-kb")
            .topK(5)
            .minScore(0.7)
            .build())
    )
    .edge("start", "search")
    .build();

// ========== Stage 2: Response Generation (Continuation) ==========

Workflow responseGeneration = ai.workflow("response-generation")
    .trigger("start", input -> {
        // Context from Stage 1 is automatically available
        return CompletableFuture.completedFuture(
            NodeOutput.success(input.data())
        );
    })
    .llm("generateResponse", builder -> builder
        .systemPrompt("You are a customer support agent. " +
                     "Use the gathered context to provide a helpful, personalized response. " +
                     "Reference billing status, previous tickets, and knowledge base articles.")
        .inputKey("context")
        .outputKey("response")
    )
    .llm("generateFollowUp", builder -> builder
        .systemPrompt("Generate helpful follow-up questions based on the issue")
        .inputKey("response")
        .outputKey("followUp")
    )
    .edge("start", "generateResponse")
    .edge("generateResponse", "generateFollowUp")
    .build();

// ========== Main Workflow: Combine Both Patterns ==========

Workflow supportAgent = ai.workflow("support-agent")
    .trigger("webhook", WebhookTrigger.create("/support", "POST"))
    
    // NESTED: Gather context from multiple sources in parallel
    .nested("gatherContext",
        List.of(billingCheck, historySearch, knowledgeSearch),
        new MergeAllAggregator(true),  // Merge all results
        NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT  // Continue even if one fails
    )
    
    // CONTINUATION: Generate response using gathered context
    .continuation("generateResponse", responseGeneration, "start")
    
    .edge("webhook", "gatherContext")
    .edge("gatherContext", "generateResponse")
    .build();

// ========== Execution ==========

WorkflowExecutor executor = new WorkflowExecutor(supportAgent);

WorkflowResult result = executor.executeFrom(
    "webhook",
    Map.of(
        "customerId", "CUST-12345",
        "issue", "Can't access my invoice from last month"
    )
).join();

if (result.isSuccess()) {
    System.out.println("Response: " + result.context().get("response"));
    System.out.println("Follow-up: " + result.context().get("followUp"));
}
```

### Execution Flow

```
1. Webhook triggered
   ↓
2. NESTED: Parallel execution
   ├─→ billingCheck (billing status)
   ├─→ historySearch (previous tickets)
   └─→ knowledgeSearch (RAG search)
   ↓
3. Aggregation: MergeAllAggregator combines results
   ↓
4. CONTINUATION: Sequential execution
   ├─→ generateResponse (LLM generates response)
   └─→ generateFollowUp (LLM generates follow-up)
   ↓
5. Final result with response + follow-up
```

---

## Real-World Examples

### Example 1: Document Processing Pipeline

```java
// Stage 1: Document Ingestion (Parallel)
Workflow documentIngestion = ai.workflow("document-ingestion")
    .trigger("start", input -> {
        String documentUrl = input.getString("documentUrl");
        return CompletableFuture.completedFuture(
            NodeOutput.success(Map.of("documentUrl", documentUrl))
        );
    })
    .vision("extractText", builder -> builder
        .operation(VisionNode.VisionOperation.PROCESS_PDF)
        .inputKey("documentUrl")
        .prompt("Extract all text from this PDF document")
        .outputKey("extractedText")
    )
    .edge("start", "extractText")
    .build();

// Stage 2: Document Analysis (Parallel)
Workflow analysis = ai.workflow("analysis")
    .trigger("start", input -> {
        String text = input.getString("extractedText");
        return CompletableFuture.completedFuture(
            NodeOutput.success(Map.of("text", text))
        );
    })
    .llm("summarize", builder -> builder
        .systemPrompt("Generate a concise summary")
        .inputKey("text")
        .outputKey("summary")
    )
    .extract("extractEntities", EntityInfo.class, builder -> builder
        .systemPrompt("Extract key entities (people, places, dates)")
        .inputKey("text")
        .outputKey("entities")
    )
    .edge("start", "summarize")
    .edge("start", "extractEntities")
    .build();

// Stage 3: Storage (Sequential)
Workflow storage = ai.workflow("storage")
    .trigger("start", input -> {
        // Store in vector database
        return CompletableFuture.completedFuture(
            NodeOutput.success(Map.of("stored", true))
        );
    })
    .vectors("index", builder -> builder
        .collection("documents")
        .inputKey("text")
        .outputKey("indexed")
    )
    .edge("start", "index")
    .build();

// Main pipeline
Workflow documentPipeline = ai.workflow("document-pipeline")
    .trigger("webhook", WebhookTrigger.create("/documents", "POST"))
    
    .continuation("ingest", documentIngestion, "start")
    .continuation("analyze", analysis, "start")
    .continuation("store", storage, "start")
    
    .edge("webhook", "ingest")
    .edge("ingest", "analyze")
    .edge("analyze", "store")
    .build();
```

### Example 2: Multi-Agent Research System

```java
// Agent 1: Web Researcher
Workflow webResearcher = ai.workflow("web-researcher")
    .trigger("start", input -> {
        String query = input.getString("query");
        // Simulate web search
        return CompletableFuture.completedFuture(
            NodeOutput.success(Map.of("webResults", "[search results...]"))
        );
    })
    .llm("synthesize", builder -> builder
        .systemPrompt("Synthesize web search results into key findings")
        .inputKey("webResults")
        .outputKey("webFindings")
    )
    .edge("start", "synthesize")
    .build();

// Agent 2: Database Researcher
Workflow dbResearcher = ai.workflow("db-researcher")
    .trigger("start", input -> {
        String query = input.getString("query");
        // Simulate database search
        return CompletableFuture.completedFuture(
            NodeOutput.success(Map.of("dbResults", "[database results...]"))
        );
    })
    .llm("analyze", builder -> builder
        .systemPrompt("Analyze database results")
        .inputKey("dbResults")
        .outputKey("dbFindings")
    )
    .edge("start", "analyze")
    .build();

// Agent 3: Knowledge Base Researcher (RAG)
Workflow kbResearcher = ai.workflow("kb-researcher")
    .trigger("start", input -> {
        String query = input.getString("query");
        return CompletableFuture.completedFuture(
            NodeOutput.success(Map.of("query", query))
        );
    })
    .rag("search", builder -> builder
        .inputKey("query")
        .outputKey("kbFindings")
        .options(RAGOptions.builder()
            .collection("research-kb")
            .topK(10)
            .build())
    )
    .edge("start", "search")
    .build();

// Main workflow: Parallel research → Aggregation → Final report
Workflow researchSystem = ai.workflow("research-system")
    .trigger("webhook", WebhookTrigger.create("/research", "POST"))
    
    // NESTED: All agents research in parallel
    .nested("research",
        List.of(webResearcher, dbResearcher, kbResearcher),
        new MergeAllAggregator(true),
        NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT
    )
    
    // CONTINUATION: Generate final report
    .continuation("generateReport", reportGenerator, "start")
    
    .edge("webhook", "research")
    .edge("research", "generateReport")
    .build();
```

---

## Best Practices

### 1. When to Use Nested Workflows

✅ **Use nested workflows when:**
- Multiple independent data sources need to be queried
- Operations can run in parallel for performance
- Results need to be aggregated before proceeding

❌ **Avoid nested workflows when:**
- Workflows depend on each other's output
- Sequential execution is required
- One workflow's failure should stop everything

### 2. When to Use Continuation Workflows

✅ **Use continuation workflows when:**
- Multi-stage pipeline with clear dependencies
- Each stage transforms data for the next stage
- Reusable workflow components

❌ **Avoid continuation workflows when:**
- Operations can run in parallel
- No data dependencies between stages
- Need to aggregate results from multiple sources

### 3. Combining Patterns

✅ **Best practice:**
- Use **nested workflows** for parallel context gathering
- Use **continuation workflows** for sequential processing
- Combine both: Nested → Aggregate → Continue

### 4. Error Handling

```java
// Best effort: Continue even if some nested workflows fail
.nested("gatherContext",
    List.of(workflow1, workflow2, workflow3),
    new MergeAllAggregator(true),
    NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT  // ✅ Recommended
)

// Fail fast: Stop if any workflow fails
.nested("criticalData",
    List.of(workflow1, workflow2),
    new MergeAllAggregator(true),
    NestedExecutionStrategy.WAIT_FOR_ALL  // ⚠️ Only if all data required
)
```

### 5. Context Management

```java
// ✅ Good: Clear input/output keys
.llm("analyze", builder -> builder
    .inputKey("context")  // Clear input
    .outputKey("analysis")  // Clear output
)

// ❌ Bad: Ambiguous context keys
.llm("analyze", builder -> builder
    .inputKey("data")  // What data?
    .outputKey("result")  // What result?
)
```

### 6. Performance Optimization

```java
// ✅ Good: Parallel nested workflows
.nested("gatherContext",
    List.of(workflow1, workflow2, workflow3),  // Run in parallel
    new MergeAllAggregator(true),
    NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT
)

// ❌ Bad: Sequential when parallel is possible
.continuation("stage1", workflow1, "start")
.continuation("stage2", workflow2, "start")  // These could run in parallel!
.continuation("stage3", workflow3, "start")
```

---

## Quick Reference

### Nested Workflow Pattern

```java
Workflow child1 = ai.workflow("child1")...build();
Workflow child2 = ai.workflow("child2")...build();

Workflow main = ai.workflow("main")
    .trigger("start", ...)
    .nested("parallel",
        List.of(child1, child2),
        new MergeAllAggregator(true),
        NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT
    )
    .edge("start", "parallel")
    .build();
```

### Continuation Workflow Pattern

```java
Workflow stage1 = ai.workflow("stage1")...build();
Workflow stage2 = ai.workflow("stage2")...build();

Workflow pipeline = ai.workflow("pipeline")
    .trigger("start", ...)
    .continuation("step1", stage1, "start")
    .continuation("step2", stage2, "start")
    .edge("start", "step1")
    .edge("step1", "step2")
    .build();
```

### Combined Pattern

```java
Workflow main = ai.workflow("main")
    .trigger("start", ...)
    // Nested: Parallel gathering
    .nested("gather", List.of(child1, child2), ...)
    // Continuation: Sequential processing
    .continuation("process", processor, "start")
    .edge("start", "gather")
    .edge("gather", "process")
    .build();
```

---

## Troubleshooting

### Problem: Nested workflow results not available

**Solution:** Check aggregation strategy:

```java
// ✅ Correct: MergeAllAggregator merges results
.nested("gather", List.of(w1, w2), new MergeAllAggregator(true), ...)

// ❌ Wrong: No aggregator specified
.nested("gather", List.of(w1, w2), ...)  // Missing aggregator!
```

### Problem: Continuation workflow doesn't receive context

**Solution:** Ensure child workflow's trigger accepts context:

```java
// ✅ Correct: Trigger passes through context
.trigger("start", input -> {
    return CompletableFuture.completedFuture(
        NodeOutput.success(input.data())  // Pass through context
    );
})

// ❌ Wrong: Trigger creates new context
.trigger("start", input -> {
    return CompletableFuture.completedFuture(
        NodeOutput.success(Map.of("new", "data"))  // Loses parent context!
    );
})
```

### Problem: Performance issues with nested workflows

**Solution:** Use `WAIT_FOR_ALL_BEST_EFFORT` for better resilience:

```java
// ✅ Recommended: Best effort continues even if one fails
.nested("gather", List.of(w1, w2, w3),
    new MergeAllAggregator(true),
    NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT
)

// ⚠️ Can be slow: Waits for all, fails fast
.nested("gather", List.of(w1, w2, w3),
    new MergeAllAggregator(true),
    NestedExecutionStrategy.WAIT_FOR_ALL  // Slower, less resilient
)
```

---

## Summary

**Nested workflows** = Parallel execution + Aggregation  
**Continuation workflows** = Sequential chaining + Context passing  
**Combined** = Powerful, production-ready AI agents

These patterns enable you to build sophisticated AI workflows that:
- ✅ Gather context from multiple sources in parallel
- ✅ Process data through sequential stages
- ✅ Aggregate results intelligently
- ✅ Handle errors gracefully
- ✅ Scale to complex real-world scenarios

---

*Document created: 2025-01-30*  
*Last updated: 2025-01-30*  
*Status: ✅ Complete*

