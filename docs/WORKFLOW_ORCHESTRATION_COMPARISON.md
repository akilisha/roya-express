# Workflow Orchestration: Separate vs Nested Approaches

## Approach 1: Separate Workflows (Current Proposal)

**Model:** Parent workflow completes → triggers separate child workflow

```java
// Parent workflow
Workflow parent = ai.workflow("parent")
    .trigger("cron", CronJobTrigger.create("0 9 * * *"))
    .llm("process", builder -> builder.outputKey("result"))
    .build();

// Separate child workflow (triggered by parent completion)
Workflow child = ai.workflow("child")
    .trigger("parent-done", WorkflowTrigger.onSuccess(parent))
    .llm("followup", builder -> builder.inputKey("result"))
    .build();
```

**Characteristics:**
- Parent workflow **completes** before child starts
- Child workflow is **independent** (separate execution context)
- Event-driven (child subscribes to parent completion)
- Loosely coupled
- Child starts with parent's final output data

**Pros:**
- Simple - workflows are independent
- Better isolation - child failure doesn't affect parent
- Easier to reason about - each workflow is self-contained
- Better for distributed systems (workflows can be on different nodes)
- Natural event-driven pattern

**Cons:**
- Parent can't aggregate results from multiple children
- Can't easily do fork/join patterns
- Less control over child execution
- Parent workflow is "done" before child starts

---

## Approach 2: Nested/Sub-workflows (Your Proposal)

**Model:** Parent workflow spawns child as sub-workflow, waits for completion, continues

```java
Workflow parent = ai.workflow("parent")
    .trigger("cron", CronJobTrigger.create("0 9 * * *"))
    .llm("process", builder -> builder.outputKey("result"))
    
    // Spawn child sub-workflow
    .subWorkflow("child", childWorkflow, builder -> builder
        .inputKey("result")  // Pass parent's result
        .outputKey("childResult")  // Get child's result back
    )
    
    // Continue after child completes
    .llm("finalize", builder -> builder
        .inputKey("childResult")  // Use child's result
        .systemPrompt("Finalize: {{childResult}}")
    )
    .edge("process", "child")
    .edge("child", "finalize")
    .build();
```

**Characteristics:**
- Parent workflow **stays alive** during child execution
- Child executes **within parent's context**
- Parent **waits** for child completion
- Parent can **continue** after children complete
- Can spawn **multiple children in parallel** and await all

**Pros:**
- Single workflow context - data flows naturally
- Parent can control child execution (wait, parallel, sequential)
- Can aggregate results from multiple children
- More like function calls - cleaner mental model
- Can have complex orchestration (fork/join patterns)
- Parent workflow stays "alive" and can continue after children
- Better for orchestration patterns

**Cons:**
- More complex implementation (workflow execution within workflow execution)
- Need to handle nested execution contexts
- Child failures need to be handled by parent (or propagate)
- Need to manage execution state carefully

---

## Comparison: When to Use Each?

### Use **Separate Workflows** when:
- Workflows are truly independent
- Event-driven architecture
- Distributed systems (different nodes/services)
- Long-running processes that shouldn't block parent
- Loose coupling desired

### Use **Nested/Sub-workflows** when:
- Need to aggregate results from multiple children
- Fork/join patterns
- Sequential or parallel child execution
- Parent needs to continue after children
- Tight orchestration control needed
- Single logical workflow with multiple phases

---

## Hybrid Approach (Best of Both Worlds?)

We could support **both** patterns:

```java
// Pattern 1: Sub-workflow (nested, synchronous)
Workflow parent = ai.workflow("parent")
    .subWorkflow("child", childWorkflow)  // Waits, gets result
    .build();

// Pattern 2: Workflow chaining (separate, asynchronous)
Workflow parent = ai.workflow("parent")
    .trigger("cron", CronJobTrigger.create("0 9 * * *"))
    .build();

Workflow child = ai.workflow("child")
    .trigger("parent-done", WorkflowTrigger.onSuccess(parent))
    .build();
```

---

## Recommendation

**I think the nested/sub-workflow approach is better** for your use case because:

1. **Natural data flow** - Parent's context flows to child, child's result flows back
2. **Orchestration patterns** - Can do fork/join, parallel execution, sequential
3. **Intuitive** - Works like function calls or subroutines
4. **Common pattern** - Similar to Airflow SubDAGs, Temporal Child Workflows, etc.
5. **Better control** - Parent can decide what to do based on child results

The separate workflow approach is better for **event-driven, loosely coupled** scenarios, but for workflow orchestration within a single logical process, nested is more powerful.

**Implementation consideration:** We'd need to:
- Add `.subWorkflow()` node type
- Handle nested execution contexts
- Support parallel child execution (fork/join)
- Handle child completion and result aggregation
- Decide on error handling (propagate vs handle in parent)

Would you like me to implement the nested/sub-workflow approach?

