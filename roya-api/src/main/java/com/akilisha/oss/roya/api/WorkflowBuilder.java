package com.akilisha.oss.roya.api;

import java.util.function.Consumer;

/**
 * Builder for creating stateful, multi-node AI workflows.
 *
 * Workflows enable complex AI operations with automatic state management,
 * conditional logic, and plugin integrations.
 *
 * Example:
 * <pre>
 * Workflow workflow = ai.workflow("receipt-processor")
 *     .node("extract", node -> node.llm()
 *         .input("${receiptText}")
 *         .output("${structuredData}")
 *     )
 *     .edge("extract", "save", Edge.always())
 *     .build();
 * </pre>
 */
public interface WorkflowBuilder {
    /**
     * Add a node to the workflow.
     *
     * @param name Unique node name within the workflow
     * @param config Node configuration
     * @return This builder for method chaining
     */
    WorkflowBuilder node(String name, Consumer<NodeBuilder> config);

    /**
     * Add an edge between nodes.
     *
     * @param from Source node name
     * @param to Target node name
     * @param edge Edge configuration (conditional, parallel, etc.)
     * @return This builder for method chaining
     */
    WorkflowBuilder edge(String from, String to, Edge edge);

    /**
     * Build the workflow.
     *
     * @return Compiled workflow ready for execution
     */
    Workflow build();
}

