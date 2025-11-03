package com.akilisha.oss.roya.examples;

import java.util.Map;

/**
 * Demonstration of the generic workflow framework usage.
 *
 * This shows various usage patterns to help identify design issues.
 */
public class WorkflowUsageDemo {

    public static void main(String[] args) {
        // Example 1: Simple linear workflow
        example1_SimpleLinear();

        // Example 2: With middleware
        example2_WithMiddleware();

        // Example 3: Conditional branching
        example3_Conditional();

        // Example 4: Parallel execution (when implemented)
        // example4_Parallel();
    }

    /**
     * Example 1: Simple linear workflow
     */
    static void example1_SimpleLinear() {
        System.out.println("\n=== Example 1: Simple Linear Workflow ===\n");

        com.akilisha.oss.roya.plugins.agentic.Workflow workflow = Workflows.builder("simple-pipeline")
            .node("step1", (ctx, next) -> {
                String input = (String) ctx.get("input");
                ctx.set("step1Result", input.toUpperCase());
                System.out.println("Step 1: " + ctx.get("step1Result"));
                next.proceed();
            })
            .node("step2", (ctx, next) -> {
                String result = (String) ctx.get("step1Result");
                ctx.set("step2Result", result + " processed");
                System.out.println("Step 2: " + ctx.get("step2Result"));
                next.proceed();
            })
            .node("step3", (ctx, next) -> {
                String result = (String) ctx.get("step2Result");
                ctx.set("finalResult", "Final: " + result);
                System.out.println("Step 3: " + ctx.get("finalResult"));
                next.proceed();
            })
            .edge("step1", "step2")
            .edge("step2", "step3")
            .build();

        com.akilisha.oss.roya.plugins.agentic.WorkflowResult result = workflow.execute(Map.of("input", "hello world"));
        System.out.println("\nFinal state: " + result.state());
        System.out.println("Success: " + result.success());
    }

    /**
     * Example 2: With middleware
     */
    static void example2_WithMiddleware() {
        System.out.println("\n=== Example 2: Workflow with Middleware ===\n");

        Workflow workflow = Workflows.builder("middleware-demo")
            .middleware((ctx, next) -> {
                System.out.println("[Middleware] Before: " + ctx.nodeName());
                long start = System.currentTimeMillis();
                next.proceed();
                long duration = System.currentTimeMillis() - start;
                System.out.println("[Middleware] After: " + ctx.nodeName() + " (" + duration + "ms)");
            })
            .node("process", (ctx, next) -> {
                Integer value = (Integer) ctx.get("value");
                ctx.set("doubled", value * 2);
                next.proceed();
            })
            .node("save", (ctx, next) -> {
                Integer doubled = (Integer) ctx.get("doubled");
                System.out.println("Saving value: " + doubled);
                next.proceed();
            })
            .edge("process", "save")
            .build();

        WorkflowResult result = workflow.execute(Map.of("value", 42));
        System.out.println("\nResult: " + result.get("doubled"));
    }

    /**
     * Example 3: Conditional branching (not yet fully implemented)
     */
    static void example3_Conditional() {
        System.out.println("\n=== Example 3: Conditional Branching ===\n");

        Workflow workflow = Workflows.builder("conditional-demo")
            .node("check", (ctx, next) -> {
                Integer value = (Integer) ctx.get("value");
                ctx.set("isLarge", value > 100);
                next.proceed();
            })
            .node("process-large", (ctx, next) -> {
                Integer value = (Integer) ctx.get("value");
                ctx.set("result", "Large: " + value);
                System.out.println("Processing large value");
                next.proceed();
            })
            .node("process-small", (ctx, next) -> {
                Integer value = (Integer) ctx.get("value");
                ctx.set("result", "Small: " + value);
                System.out.println("Processing small value");
                next.proceed();
            })
            .edge("check", "process-large")
            .edgeIf("check", "process-small", ctx -> {
                Boolean isLarge = (Boolean) ctx.get("isLarge");
                return isLarge != null && !isLarge;
            })
            .build();

        // Test with large value
        System.out.println("Testing with value 150:");
        workflow.execute(Map.of("value", 150));

        // Test with small value
        System.out.println("\nTesting with value 50:");
        workflow.execute(Map.of("value", 50));
    }
}

