package com.akilisha.oss.roya.plugins.ai.builder;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.EmbeddingNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.ExtractNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.LLMActionNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.StreamingLLMNode;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;
import com.akilisha.oss.roya.workflow.edges.Edge;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Fluent builder specifically for AI workflows.
 *
 * Wraps roya-workflow's Workflow builder with AI-specific convenience methods.
 * Provides semantic clarity: `.llm()`, `.extract()`, `.embeddings()` instead of
 * generic `.action()` calls.
 *
 * Example:
 * <pre>
 * Workflow workflow = AIWorkflowBuilder.create(ai, "receipt-processor")
 *     .llm("classify", builder -> builder
 *         .systemPrompt("Classify intent")
 *         .inputKey("message")
 *         .outputKey("intent")
 *     )
 *     .extract("extractDetails", ReceiptDetails.class, builder -> builder
 *         .systemPrompt("Extract receipt details")
 *         .inputKey("receiptText")
 *         .outputKey("details")
 *     )
 *     .embeddings("embed", builder -> builder
 *         .inputKey("text")
 *         .outputKey("embedding")
 *     )
 *     .edge("classify", "extract")
 *     .edge("extract", "embed")
 *     .build();
 * </pre>
 */
public class AIWorkflowBuilder {
    private final AI ai;
    private final Workflow.WorkflowBuilder workflowBuilder;

    private AIWorkflowBuilder(AI ai, String workflowName) {
        this.ai = ai;
        this.workflowBuilder = Workflow.create();
    }

    /**
     * Create a new AI workflow builder.
     *
     * @param ai AI service instance
     * @param name Workflow name
     * @return Builder instance
     */
    public static AIWorkflowBuilder create(AI ai, String name) {
        return new AIWorkflowBuilder(ai, name);
    }

    // ========== Trigger Node Methods ==========

    /**
     * Add a manual trigger node (for programmatically started workflows).
     *
     * @param nodeId Node identifier
     * @param triggerNode The trigger node implementation
     * @return This builder
     */
    public AIWorkflowBuilder trigger(String nodeId, WorkflowNode triggerNode) {
        workflowBuilder.trigger(nodeId, triggerNode);
        return this;
    }

    /**
     * Add a manual trigger node with timeout.
     *
     * @param nodeId Node identifier
     * @param triggerNode The trigger node implementation
     * @param timeout Timeout for the trigger
     * @return This builder
     */
    public AIWorkflowBuilder trigger(String nodeId, WorkflowNode triggerNode, Duration timeout) {
        workflowBuilder.trigger(nodeId, triggerNode, timeout);
        return this;
    }

    // ========== AI Node Convenience Methods ==========

    /**
     * Add an LLM action node.
     *
     * @param nodeId Node identifier
     * @param config Node configuration
     * @return This builder
     */
    public AIWorkflowBuilder llm(String nodeId, Consumer<LLMActionNode.Builder> config) {
        LLMActionNode.Builder builder = LLMActionNode.builder(ai);
        config.accept(builder);
        workflowBuilder.action(nodeId, builder.build());
        return this;
    }

    /**
     * Add a type-safe extraction node.
     *
     * @param nodeId Node identifier
     * @param extractType Target type (Java record/class)
     * @param config Node configuration
     * @return This builder
     */
    public <T> AIWorkflowBuilder extract(String nodeId, Class<T> extractType,
                                        Consumer<ExtractNode.Builder<T>> config) {
        ExtractNode.Builder<T> builder = ExtractNode.builder(ai, extractType);
        config.accept(builder);
        workflowBuilder.action(nodeId, builder.build());
        return this;
    }

    /**
     * Add an embeddings node.
     *
     * @param nodeId Node identifier
     * @param config Node configuration
     * @return This builder
     */
    public AIWorkflowBuilder embeddings(String nodeId, Consumer<EmbeddingNode.Builder> config) {
        EmbeddingNode.Builder builder = EmbeddingNode.builder(ai);
        config.accept(builder);
        workflowBuilder.action(nodeId, builder.build());
        return this;
    }

    /**
     * Add a streaming LLM node that streams tokens as they arrive.
     *
     * @param nodeId Node identifier
     * @param config Node configuration
     * @return This builder
     */
    public AIWorkflowBuilder stream(String nodeId, Consumer<StreamingLLMNode.Builder> config) {
        StreamingLLMNode.Builder builder = StreamingLLMNode.builder(ai);
        config.accept(builder);
        workflowBuilder.action(nodeId, builder.build());
        return this;
    }

    // TODO: Add more convenience methods as we implement more node types:
    // - .rag()
    // - .vision()
    // - .audio()
    // - .agent()
    // - .mcp()
    // - .vectors()

    // ========== Edge Delegation ==========

    /**
     * Add a sequential edge (default).
     */
    public AIWorkflowBuilder edge(String from, String to) {
        workflowBuilder.edge(from, to);
        return this;
    }

    /**
     * Add an edge with configuration.
     */
    public AIWorkflowBuilder edge(String from, String to, Edge edge) {
        workflowBuilder.edge(from, to, edge);
        return this;
    }

    /**
     * Add a conditional edge.
     */
    public AIWorkflowBuilder edge(String from, String to,
                                  java.util.function.Predicate<com.akilisha.oss.roya.workflow.core.ExecutionContext> condition) {
        workflowBuilder.edge(from, to, Edge.when(condition));
        return this;
    }

    /**
     * Add a parallel edge.
     */
    public AIWorkflowBuilder edgeParallel(String from, String to) {
        workflowBuilder.edge(from, to, Edge.parallel());
        return this;
    }

    /**
     * Build the workflow.
     *
     * @return Compiled workflow
     */
    public Workflow build() {
        return workflowBuilder.build();
    }

    // ========== Direct Access (for advanced use) ==========

    /**
     * Get the underlying Workflow builder for direct access.
     * Use this when you need roya-workflow features not exposed by AIWorkflowBuilder.
     *
     * @return Underlying Workflow builder
     */
    public Workflow.WorkflowBuilder workflowBuilder() {
        return workflowBuilder;
    }
}



