package com.akilisha.oss.roya.workflow;

import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.edges.Edge;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Example test demonstrating workflow testing patterns
 */
public class WorkflowTest {

    @Test
    void testSimpleSequentialWorkflow() {
        // Given: A simple two-node workflow
        Workflow workflow = Workflow.create()
            .trigger("start", input ->
                CompletableFuture.completedFuture(
                    NodeOutput.success("value", 10)
                ))
            .action("double", input -> {
                int value = input.getInt("value");
                return CompletableFuture.completedFuture(
                    NodeOutput.success("result", value * 2)
                );
            })
            .edge("start", "double")
            .build();

        // When: Execute workflow
        WorkflowResult result = new WorkflowExecutor(workflow)
            .executeFrom("start", Map.of())
            .join();

        // Then: Verify success and result
        assertTrue(result.isSuccess());
        assertEquals(20, (Integer) result.context().get("result"));
    }

    @Test
    void testConditionalBranching() {
        // Given: Workflow with conditional branching
        Workflow workflow = Workflow.create()
            .trigger("start", input ->
                CompletableFuture.completedFuture(NodeOutput.success(input.data())))
            .action("pathA", input ->
                CompletableFuture.completedFuture(NodeOutput.success("path", "A")))
            .action("pathB", input ->
                CompletableFuture.completedFuture(NodeOutput.success("path", "B")))

            .edge("start", "pathA", Edge.when(ctx -> ctx.<Integer>get("age") >= 18))
            .edge("start", "pathB", Edge.when(ctx -> ctx.<Integer>get("age") < 18))
            .build();

        // When: Execute with age >= 18
        WorkflowResult result = new WorkflowExecutor(workflow)
            .executeFrom("start", Map.of("age", 25))
            .join();

        // Then: Should take path A
        assertTrue(result.isSuccess());
        assertEquals("A", result.context().get("path"));
    }

    @Test
    void testParallelExecution() {
        // Given: Workflow with parallel branches
        Workflow workflow = Workflow.create()
            .trigger("start", input ->
                CompletableFuture.completedFuture(NodeOutput.success(input.data())))
            .action("taskA", input -> {
                input.context().set("completedA", true);
                return CompletableFuture.completedFuture(NodeOutput.success());
            })
            .action("taskB", input -> {
                input.context().set("completedB", true);
                return CompletableFuture.completedFuture(NodeOutput.success());
            })

            .edge("start", "taskA", Edge.parallel())
            .edge("start", "taskB", Edge.parallel())
            .build();

        // When: Execute
        WorkflowResult result = new WorkflowExecutor(workflow)
            .executeFrom("start", Map.of())
            .join();

        // Then: Both tasks should complete
        assertTrue(result.isSuccess());
        assertTrue((Boolean) result.context().get("completedA"));
        assertTrue((Boolean) result.context().get("completedB"));
    }

    @Test
    void testNodeFailure() {
        // Given: Workflow with failing node
        Workflow workflow = Workflow.create()
            .trigger("start", input ->
                CompletableFuture.completedFuture(NodeOutput.success()))
            .action("fail", input ->
                CompletableFuture.completedFuture(NodeOutput.failure("Intentional failure")))

            .edge("start", "fail")
            .build();

        // When: Execute
        WorkflowResult result = new WorkflowExecutor(workflow)
            .executeFrom("start", Map.of())
            .join();

        // Then: Workflow should fail
        assertTrue(result.isFailure());
        assertTrue(result.finalOutput().error().isPresent());
        assertTrue(result.finalOutput().error().get().contains("Intentional failure"));
    }

    @Test
    void testExecutionTrace() {
        // Given: Multi-node workflow
        Workflow workflow = Workflow.create()
            .trigger("start", input -> CompletableFuture.completedFuture(NodeOutput.success()))
            .action("step1", input -> CompletableFuture.completedFuture(NodeOutput.success()))
            .action("step2", input -> CompletableFuture.completedFuture(NodeOutput.success()))

            .edge("start", "step1")
            .edge("step1", "step2")
            .build();

        // When: Execute
        WorkflowResult result = new WorkflowExecutor(workflow)
            .executeFrom("start", Map.of())
            .join();

        // Then: All nodes should be in trace
        assertEquals(3, result.getTrace().size());
        assertEquals("start", result.getTrace().get(0).nodeId());
        assertEquals("step1", result.getTrace().get(1).nodeId());
        assertEquals("step2", result.getTrace().get(2).nodeId());
    }

    @Test
    void testContextDataPropagation() {
        // Given: Workflow that transforms data
        Workflow workflow = Workflow.create()
            .trigger("start", input ->
                CompletableFuture.completedFuture(NodeOutput.success("name", "Alice")))
            .action("greet", input -> {
                String name = input.getString("name");
                return CompletableFuture.completedFuture(
                    NodeOutput.success("greeting", "Hello, " + name + "!")
                );
            })

            .edge("start", "greet")
            .build();

        // When: Execute
        WorkflowResult result = new WorkflowExecutor(workflow)
            .executeFrom("start", Map.of())
            .join();

        // Then: Data should propagate through context
        assertEquals("Alice", result.context().get("name"));
        assertEquals("Hello, Alice!", result.context().get("greeting"));
    }
}
