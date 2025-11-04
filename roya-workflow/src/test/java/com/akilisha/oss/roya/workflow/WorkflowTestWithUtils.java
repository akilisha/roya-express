package com.akilisha.oss.roya.workflow;

import com.akilisha.oss.roya.workflow.core.*;
import com.akilisha.oss.roya.workflow.edges.Edge;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.akilisha.oss.roya.workflow.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Alternative test implementation using TestUtils for better type safety.
 * This shows the recommended approach to avoid casting issues.
 */
public class WorkflowTestWithUtils {
    
    @Test
    void testParallelExecutionWithUtils() {
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
        
        // Using TestUtils - much cleaner!
        assertContextTrue(result.context(), "completedA");
        assertContextTrue(result.context(), "completedB");
    }
    
    @Test
    void testContextDataWithUtils() {
        // Given: Workflow that sets various data types
        Workflow workflow = Workflow.create()
            .trigger("start", input -> {
                input.context().set("name", "Alice");
                input.context().set("age", 30);
                input.context().set("active", true);
                return CompletableFuture.completedFuture(NodeOutput.success());
            })
            .build();
        
        // When: Execute
        WorkflowResult result = new WorkflowExecutor(workflow)
            .executeFrom("start", Map.of())
            .join();
        
        // Then: Use type-safe getters
        assertEquals("Alice", getStringFromContext(result.context(), "name"));
        assertEquals(30, getIntFromContext(result.context(), "age"));
        assertTrue(getBooleanFromContext(result.context(), "active"));
        
        // Or use generic method
        assertEquals("Alice", getTypedFromContext(result.context(), "name", String.class));
        assertEquals(30, getTypedFromContext(result.context(), "age", Integer.class));
        assertEquals(true, getTypedFromContext(result.context(), "active", Boolean.class));
    }
    
    @Test
    void testComplexDataTypes() {
        // Given: Workflow that stores complex objects
        Workflow workflow = Workflow.create()
            .trigger("start", input -> {
                input.context().set("list", java.util.List.of("a", "b", "c"));
                input.context().set("map", Map.of("key", "value"));
                return CompletableFuture.completedFuture(NodeOutput.success());
            })
            .build();
        
        // When: Execute
        WorkflowResult result = new WorkflowExecutor(workflow)
            .executeFrom("start", Map.of())
            .join();
        
        // Then: Use generic method for complex types
        var list = getTypedFromContext(result.context(), "list", java.util.List.class);
        assertEquals(3, list.size());
        
        @SuppressWarnings("unchecked")
        var map = getTypedFromContext(result.context(), "map", Map.class);
        assertEquals("value", map.get("key"));
    }
}
