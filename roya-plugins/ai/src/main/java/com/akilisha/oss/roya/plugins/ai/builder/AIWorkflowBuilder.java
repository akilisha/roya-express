package com.akilisha.oss.roya.plugins.ai.builder;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.AIServiceNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.EmbeddingNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.ExtractNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.LLMActionNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.RAGNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.StreamingLLMNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.VectorNode;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.WebhookTrigger;
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
    private final String workflowName;
    
    private AIWorkflowBuilder(AI ai, String workflowName) {
        this.ai = ai;
        this.workflowName = workflowName;
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
     * Add a trigger node (initiates workflow).
     * 
     * Automatically registers WebhookTrigger nodes with HTTP routing.
     *
     * @param nodeId Node identifier
     * @param triggerNode The trigger node implementation
     * @return This builder
     */
    public AIWorkflowBuilder trigger(String nodeId, WorkflowNode triggerNode) {
        // If it's a WebhookTrigger, register it with workflow metadata
        if (triggerNode instanceof WebhookTrigger) {
            WebhookTrigger webhookTrigger = (WebhookTrigger) triggerNode;
            webhookTrigger.setWorkflowMetadata(workflowName, nodeId);
        }
        
        workflowBuilder.trigger(nodeId, triggerNode);
        return this;
    }
    
    /**
     * Add a trigger node with timeout.
     * 
     * Automatically registers WebhookTrigger nodes with HTTP routing.
     *
     * @param nodeId Node identifier
     * @param triggerNode The trigger node implementation
     * @param timeout Timeout for the trigger
     * @return This builder
     */
    public AIWorkflowBuilder trigger(String nodeId, WorkflowNode triggerNode, Duration timeout) {
        // If it's a WebhookTrigger, register it with workflow metadata
        if (triggerNode instanceof WebhookTrigger) {
            WebhookTrigger webhookTrigger = (WebhookTrigger) triggerNode;
            webhookTrigger.setWorkflowMetadata(workflowName, nodeId);
        }
        
        workflowBuilder.trigger(nodeId, triggerNode, timeout);
        return this;
    }

    // ========== AI Node Convenience Methods ==========

    /**
     * Add an LLM action node.
     *
     * Supports RAG, tools, and memory via the builder using AI Services pattern:
     * <pre>
     * .llm("analyze", builder -> builder
     *     .systemPrompt("Analyze the text")
     *     .inputKey("text")
     *     .outputKey("analysis")
     *     .rag(contentRetriever)  // Enable RAG via ContentRetriever
     *     .tools(toolSpecs)        // Enable tools via ToolSpecification
     *     .memory(chatMemory, "userId")  // Enable memory via ChatMemory
     * )
     * </pre>
     * 
     * When RAG, tools, or memory are configured, the node uses LangChain4j's AI Services
     * internally for a declarative, type-safe approach.
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
     * Add an LLM action node with RAG (Retrieval-Augmented Generation).
     * 
     * Convenience method for RAG-enabled LLM nodes.
     * 
     * @param nodeId Node identifier
     * @param contentRetriever ContentRetriever for RAG
     * @param config Node configuration
     * @return This builder
     */
    public AIWorkflowBuilder llmWithRAG(String nodeId, 
                                        Object contentRetriever,  // dev.langchain4j.data.retriever.ContentRetriever
                                        Consumer<LLMActionNode.Builder> config) {
        LLMActionNode.Builder builder = LLMActionNode.builder(ai);
        builder.rag(contentRetriever);
        config.accept(builder);
        workflowBuilder.action(nodeId, builder.build());
        return this;
    }
    
    /**
     * Add an LLM action node with tools (function calling).
     * 
     * Convenience method for tool-enabled LLM nodes.
     * 
     * @param nodeId Node identifier
     * @param tools List of ToolSpecification instances
     * @param config Node configuration
     * @return This builder
     */
    public AIWorkflowBuilder llmWithTools(String nodeId,
                                         Object tools,  // List<dev.langchain4j.agent.tool.ToolSpecification>
                                         Consumer<LLMActionNode.Builder> config) {
        LLMActionNode.Builder builder = LLMActionNode.builder(ai);
        builder.tools(tools);
        config.accept(builder);
        workflowBuilder.action(nodeId, builder.build());
        return this;
    }
    
    /**
     * Add an LLM action node with memory (conversation history).
     * 
     * Convenience method for memory-enabled LLM nodes.
     * 
     * @param nodeId Node identifier
     * @param chatMemory ChatMemory instance
     * @param memoryIdKey Key in context to get memory ID
     * @param config Node configuration
     * @return This builder
     */
    public AIWorkflowBuilder llmWithMemory(String nodeId,
                                          Object chatMemory,  // dev.langchain4j.memory.ChatMemory
                                          String memoryIdKey,
                                          Consumer<LLMActionNode.Builder> config) {
        LLMActionNode.Builder builder = LLMActionNode.builder(ai);
        builder.memory(chatMemory, memoryIdKey);
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

    /**
     * Add a RAG (Retrieval-Augmented Generation) node.
     * 
     * Performs RAG queries using Qdrant for retrieval and LLM for generation.
     * 
     * Example:
     * <pre>
     * .rag("answer-question", builder -> builder
     *     .inputKey("question")
     *     .outputKey("answer")
     *     .options(RAGOptions.builder().topK(5).build())
     * )
     * </pre>
     * 
     * @param nodeId Node identifier
     * @param config Node configuration
     * @return This builder
     */
    public AIWorkflowBuilder rag(String nodeId, Consumer<RAGNode.Builder> config) {
        RAGNode.Builder builder = RAGNode.builder(ai);
        config.accept(builder);
        workflowBuilder.action(nodeId, builder.build());
        return this;
    }

    /**
     * Add a vector indexing node.
     * 
     * Indexes documents into Qdrant vector store.
     * Supports both individual documents from context and directory indexing.
     * 
     * Example (index documents from context):
     * <pre>
     * .vectors("index-docs", builder -> builder
     *     .inputKey("documents")
     *     .collection("my-collection")
     * )
     * </pre>
     * 
     * Example (index directory):
     * <pre>
     * .vectors("index-dir", builder -> builder
     *     .collection("kb")
     *     .directory(Path.of("docs/"), AI.ChunkingOptions.fixed(800, 200))
     * )
     * </pre>
     * 
     * @param nodeId Node identifier
     * @param config Node configuration
     * @return This builder
     */
    public AIWorkflowBuilder vectors(String nodeId, Consumer<VectorNode.Builder> config) {
        VectorNode.Builder builder = VectorNode.builder(ai);
        config.accept(builder);
        workflowBuilder.action(nodeId, builder.build());
        return this;
    }

    /**
     * Add an AI Service node for custom AI Service interfaces.
     * 
     * Allows creating custom AI Service interfaces within workflows using LangChain4j's AI Services pattern.
     * This enables developers to define their own AI Service interfaces with annotations.
     * 
     * Example:
     * <pre>
     * interface MyService {
     *     @dev.langchain4j.service.SystemMessage("You are a helpful assistant")
     *     @dev.langchain4j.service.UserMessage("{{question}}")
     *     String answer(String question);
     * }
     * 
     * .aiService("my-service", MyService.class, builder -> builder
     *     .outputKey("answer")
     *     .execute((service, input) -> {
     *         String question = input.getString("question");
     *         return service.answer(question);
     *     })
     *     .configure(serviceBuilder -> {
     *         // Configure RAG, tools, memory, etc.
     *         serviceBuilder.contentRetriever(retriever);
     *     })
     * )
     * </pre>
     * 
     * @param nodeId Node identifier
     * @param serviceClass AI Service interface class
     * @param config Node configuration
     * @return This builder
     */
    public <T> AIWorkflowBuilder aiService(String nodeId, Class<T> serviceClass, 
                                          Consumer<AIServiceNode.Builder<T>> config) {
        AIServiceNode.Builder<T> builder = AIServiceNode.builder(ai, serviceClass);
        config.accept(builder);
        workflowBuilder.action(nodeId, builder.build());
        return this;
    }

    // TODO: Add more convenience methods as we implement more node types:
    // - .vision()
    // - .audio()
    // - .agent()
    // - .mcp()

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
     * Automatically registers any WebhookTrigger nodes with the webhook registry.
     * Also registers the workflow with WorkflowRegistry for persistence.
     *
     * @return Compiled workflow
     */
    public Workflow build() {
        Workflow workflow = workflowBuilder.build();
        
        // Register workflow with WorkflowRegistry (for webhook persistence)
        com.akilisha.oss.roya.plugins.ai.workflow.WorkflowRegistry.getInstance()
            .register(workflowName, workflow);
        
        // Register any WebhookTrigger nodes with the registry
        workflow.getTriggerNodes().forEach(nodeId -> {
            WorkflowNode node = workflow.getNode(nodeId);
            if (node instanceof WebhookTrigger) {
                WebhookTrigger webhookTrigger = (WebhookTrigger) node;
                webhookTrigger.register(workflow);
            }
        });
        
        return workflow;
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



