# Workflow Architecture: Triggers vs Actions vs WorkflowTrigger

## The Three Types of Nodes

### 1. **Entry Triggers** (Starting Points)
These are **external event triggers** that START a workflow:
- `CronJobTrigger` - Scheduled time (cron expression)
- `FileWatchTrigger` - File system events  
- `PollingTrigger` - HTTP polling conditions
- `WebhookTrigger` - HTTP requests
- `ManualTrigger` - Programmatic execution

**Usage:**
```java
ai.workflow("my-workflow")
    .trigger("cron", CronJobTrigger.create("0/5 * * * * ?"))  // Entry point
    .llm("process", ...)  // Action node
    .edge("cron", "process")  // Flow from trigger to action
    .build();
```

### 2. **Action Nodes** (Workflow Steps)
These are **regular workflow nodes** that execute during workflow execution:
- `LLMActionNode` - AI/LLM operations
- `ExtractNode` - Data extraction
- `RAGNode` - RAG queries
- Custom nodes (like `WriteFileNode`, `UploadFileNode`)

**Usage:**
```java
// Using convenience methods (AI-specific nodes)
ai.workflow("my-workflow")
    .trigger("start", ManualTrigger.create())
    .llm("analyze", builder -> builder.systemPrompt("..."))  // Convenience method
    .edge("start", "analyze")
    .build();

// Using workflowBuilder() for custom nodes
ai.workflow("my-workflow")
    .trigger("start", ManualTrigger.create())
    .workflowBuilder().action("writeFile", new WriteFileNode(...))  // Custom node
    .edge("start", "writeFile")
    .build();
```

### 3. **WorkflowTrigger** (Nested/Chained Workflows)
This is a **special trigger that fires when another workflow completes**. It's both:
- A trigger (because it initiates workflow execution)
- An action node (because it's triggered by another workflow's completion)

**Usage:**
```java
// Parent workflow
Workflow parentWorkflow = ai.workflow("parent-workflow")
    .trigger("cron", CronJobTrigger.create("0 9 * * *"))
    .llm("process", ...)
    .edge("cron", "process")
    .build();

// Child workflow triggered by parent completion
Workflow childWorkflow = ai.workflow("child-workflow")
    .trigger("parent-complete", WorkflowTrigger.onSuccess(parentWorkflow))  // Triggers when parent succeeds
    .llm("followup", ...)
    .edge("parent-complete", "followup")
    .build();
```

## Why `.workflowBuilder().action()`?

`AIWorkflowBuilder` provides **convenience methods** for AI-specific nodes:
- `.llm()` → creates `LLMActionNode`
- `.extract()` → creates `ExtractNode`
- `.rag()` → creates `RAGNode`
- `.vectors()` → creates `VectorNode`

For **custom nodes** (like `WriteFileNode`), you need to:
1. Access the underlying `Workflow.WorkflowBuilder` via `.workflowBuilder()`
2. Use `.action()` to add your custom `WorkflowNode`

```java
// Custom node class
static class WriteFileNode implements WorkflowNode {
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        // Your custom logic
    }
}

// Add it to workflow
ai.workflow("my-workflow")
    .trigger("start", ManualTrigger.create())
    .workflowBuilder()  // Access underlying builder
    .action("writeFile", new WriteFileNode(...))  // Add custom node
    .edge("start", "writeFile")
    .build();
```

## The Flow

```
[External Event] → [Entry Trigger] → [Action Node] → [Action Node] → [Action Node]
                       ↓                  ↓                ↓                ↓
                    CronJob         WriteFileNode      UploadFileNode   ProcessFileNode
```

With `WorkflowTrigger`:
```
[Workflow A completes] → [WorkflowTrigger] → [Action Node] → ...
                              ↓
                    Triggers Workflow B
```

## Summary

- **Entry Triggers** (`CronJobTrigger`, `FileWatchTrigger`, etc.): External events that START workflows
- **Action Nodes**: Regular workflow steps (AI nodes or custom nodes)
- **WorkflowTrigger**: Special trigger that fires when another workflow completes (for workflow chaining)

The `.workflowBuilder()` method is needed because `AIWorkflowBuilder` only has convenience methods for AI-specific nodes. For custom nodes, you access the underlying builder to add them directly.

