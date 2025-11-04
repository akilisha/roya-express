# WorkflowTrigger vs SubWorkflow: Two Complementary Patterns

## The Two Patterns

### 1. **WorkflowTrigger** (Event-Driven, Separate Workflows)
**Use case:** Loosely coupled, event-driven workflow chaining

```java
// Parent workflow completes → triggers separate child workflow
Workflow parent = ai.workflow("data-processing")
    .trigger("cron", CronJobTrigger.create("0 9 * * *"))
    .llm("process", ...)
    .build();

// Separate child workflow (triggered by parent completion event)
Workflow child = ai.workflow("notification")
    .trigger("parent-complete", WorkflowTrigger.onSuccess(parent))
    .llm("send-email", ...)
    .build();
```

**Characteristics:**
- **Separate workflows** - each is independent
- **Event-driven** - child subscribes to parent completion event
- **Loosely coupled** - parent doesn't know about child
- **Asynchronous** - parent completes, child starts later
- **Distributed** - workflows can be on different systems

**When to use:**
- Long-running processes that shouldn't block parent
- Event-driven architecture
- Distributed systems
- Loose coupling desired
- Parent workflow should complete independently

---

### 2. **SubWorkflow** (Nested Orchestration)
**Use case:** Tightly coupled orchestration within a single workflow

```java
// Parent workflow orchestrates child workflows
Workflow parent = ai.workflow("orchestration")
    .trigger("start", ManualTrigger.create())
    .llm("analyze", ...)
    
    // Execute child workflow as a step
    .subWorkflow("child", childWorkflow, builder -> builder
        .inputKey("data")  // Pass parent's data
        .outputKey("result")  // Get child's result back
    )
    
    // Continue after child completes
    .llm("finalize", builder -> builder.inputKey("result"))
    .edge("analyze", "child")
    .edge("child", "finalize")
    .build();
```

**Characteristics:**
- **Nested execution** - child executes within parent context
- **Synchronous** - parent waits for child completion
- **Tightly coupled** - parent controls child execution
- **Same context** - data flows naturally between parent and child
- **Orchestration** - parent can fork/join multiple children

**When to use:**
- Need to aggregate results from multiple children
- Fork/join patterns
- Sequential or parallel child execution
- Parent needs to continue after children
- Tight orchestration control needed

---

## Key Difference: Trigger vs Execution

**WorkflowTrigger:**
- The trigger is **external** - it's an event subscription
- Parent workflow **completes** → emits event → child workflow **starts**
- Two separate workflow executions

**SubWorkflow:**
- The execution is **internal** - it's a workflow step/node
- Parent workflow **spawns** child → **waits** → child completes → parent **continues**
- Single workflow execution with nested child execution

---

## Visual Comparison

### WorkflowTrigger Pattern:
```
Parent Workflow Execution:
[trigger] → [process] → [complete] ✅
                ↓ (workflow completes, emits event)
Child Workflow Execution (separate):
[WorkflowTrigger receives event] → [process] → [complete] ✅
```

### SubWorkflow Pattern:
```
Parent Workflow Execution (stays alive):
[trigger] → [process] → [SubWorkflowNode spawns child]
                                      ↓
                          Child Workflow Execution (nested)
                          [process] → [complete] ✅
                                      ↑
[SubWorkflowNode receives result] → [aggregate] → [finalize] → [complete] ✅
```

---

## Are They Both Needed?

**Yes!** They serve different purposes:

| Aspect | WorkflowTrigger | SubWorkflow |
|--------|----------------|-------------|
| **Coupling** | Loose | Tight |
| **Timing** | Asynchronous | Synchronous |
| **Context** | Separate | Shared |
| **Use Case** | Event-driven | Orchestration |
| **Pattern** | Pub/Sub | Function call |

---

## Example: When to Use Each

### Use WorkflowTrigger:
```java
// Scenario: Daily report generation → send notification
Workflow report = ai.workflow("daily-report")
    .trigger("cron", CronJobTrigger.create("0 9 * * *"))
    .llm("generate-report", ...)
    .build();

// Separate workflow for notifications (triggered by report completion)
Workflow notify = ai.workflow("notify-users")
    .trigger("report-ready", WorkflowTrigger.onSuccess(report))
    .llm("send-email", ...)
    .build();
```

### Use SubWorkflow:
```java
// Scenario: Process order → validate payment → check inventory → ship
Workflow order = ai.workflow("order-processing")
    .trigger("webhook", WebhookTrigger.create("/order"))
    .subWorkflow("validate-payment", paymentWorkflow)
    .subWorkflow("check-inventory", inventoryWorkflow)
    .subWorkflow("ship-order", shippingWorkflow)
    .llm("send-confirmation", ...)
    .edge("webhook", "validate-payment")
    .edge("validate-payment", "check-inventory")
    .edge("check-inventory", "ship-order")
    .edge("ship-order", "send-confirmation")
    .build();
```

---

## Conclusion

**WorkflowTrigger** and **SubWorkflow** are **complementary patterns**:

- **WorkflowTrigger** = Event-driven, separate workflows (pub/sub pattern)
- **SubWorkflow** = Orchestration, nested execution (function call pattern)

Both are needed! They solve different problems:
- Use `WorkflowTrigger` when you want loosely coupled, event-driven workflow chaining
- Use `subWorkflow()` when you want tightly coupled orchestration within a single workflow

The `.subWorkflow()` is **not** an implicit trigger - it's a **different pattern** altogether. It's a workflow node that executes another workflow synchronously within the parent context, rather than subscribing to completion events.

