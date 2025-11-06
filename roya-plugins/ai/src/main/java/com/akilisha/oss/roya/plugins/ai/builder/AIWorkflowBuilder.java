package com.akilisha.oss.roya.plugins.ai.builder;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.AIServiceNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.AgentNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.VisionNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.EmbeddingNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.ExtractNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.ForkNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.LLMActionNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.RAGNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.StreamingLLMNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.VectorNode;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.WebhookTrigger;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.CronJobTrigger;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.FileWatchTrigger;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.PollingTrigger;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.WorkflowTrigger;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;
import com.akilisha.oss.roya.workflow.edges.Edge;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.List;

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
     * <p>
     * Automatically registers WebhookTrigger nodes with HTTP routing.
     *
     * @param nodeId Node identifier
     * @param triggerNode The trigger node implementation
     * @return This builder
     */
    public AIWorkflowBuilder trigger(String nodeId, WorkflowNode triggerNode) {
        // If it's a WebhookTrigger, register it with workflow metadata
        if (triggerNode instanceof WebhookTrigger webhookTrigger) {
            webhookTrigger.setWorkflowMetadata(workflowName, nodeId);
        }

        // If it's a CronJobTrigger, register it with workflow metadata
        if (triggerNode instanceof CronJobTrigger cronJobTrigger) {
            cronJobTrigger.setWorkflowMetadata(workflowName, nodeId);
        }

        // If it's a FileWatchTrigger, register it with workflow metadata
        if (triggerNode instanceof FileWatchTrigger fileWatchTrigger) {
            fileWatchTrigger.setWorkflowMetadata(workflowName, nodeId);
        }

        // If it's a PollingTrigger, register it with workflow metadata
        if (triggerNode instanceof PollingTrigger pollingTrigger) {
            pollingTrigger.setWorkflowMetadata(workflowName, nodeId);
        }

        // If it's a WorkflowTrigger, register it with workflow metadata
        if (triggerNode instanceof WorkflowTrigger workflowTrigger) {
            workflowTrigger.setWorkflowMetadata(workflowName, nodeId);
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
        if (triggerNode instanceof WebhookTrigger webhookTrigger) {
            webhookTrigger.setWorkflowMetadata(workflowName, nodeId);
        }

        // If it's a CronJobTrigger, register it with workflow metadata
        if (triggerNode instanceof CronJobTrigger cronJobTrigger) {
            cronJobTrigger.setWorkflowMetadata(workflowName, nodeId);
        }

        // If it's a FileWatchTrigger, register it with workflow metadata
        if (triggerNode instanceof FileWatchTrigger fileWatchTrigger) {
            fileWatchTrigger.setWorkflowMetadata(workflowName, nodeId);
        }

        // If it's a PollingTrigger, register it with workflow metadata
        if (triggerNode instanceof PollingTrigger pollingTrigger) {
            pollingTrigger.setWorkflowMetadata(workflowName, nodeId);
        }

        // If it's a WorkflowTrigger, register it with workflow metadata
        if (triggerNode instanceof WorkflowTrigger workflowTrigger) {
            workflowTrigger.setWorkflowMetadata(workflowName, nodeId);
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
     * Add an agent node (LLM with tools that can autonomously use tools).
     *
     * <p>Agents are similar to LLMs with tools, but they have autonomy to decide when and how to use tools.
     * This is the recommended way to create tool-enabled workflows where the AI decides tool usage.
     *
     * <p>Example:
     * <pre>
     * List&lt;ToolSpecification&gt; tools = List.of(
     *     ToolSpecification.builder()
     *         .name("get_weather")
     *         .description("Get current weather for a location")
     *         .build(),
     *     ToolSpecification.builder()
     *         .name("send_email")
     *         .description("Send an email")
     *         .build()
     * );
     *
     * ai.workflow("agent-workflow")
     *     .trigger("webhook", WebhookTrigger.create(...))
     *     .agents("assistant", builder -> builder
     *         .inputKey("query")
     *         .outputKey("response")
     *         .tools(tools)
     *         .systemPrompt("You are a helpful assistant with access to weather and email tools")
     *     )
     *     .build();
     * </pre>
     *
     * <p><b>Note:</b> Agents automatically decide when to use tools. Use {@link #llmWithTools(String, Object, Consumer)}
     * if you want more control over tool invocation.
     *
     * @param nodeId Node identifier
     * @param config Node configuration
     * @return This builder
     */
    public AIWorkflowBuilder agents(String nodeId,
                                   Consumer<AgentNode.Builder> config) {
        AgentNode.Builder builder = AgentNode.Builder.builder(ai);
        config.accept(builder);
        workflowBuilder.action(nodeId, builder.build());
        return this;
    }

    /**
     * Convenience method for agent nodes with tools.
     *
     * <p>Equivalent to calling {@link #agents(String, Consumer)} with tools configured in the builder.
     *
     * @param nodeId Node identifier
     * @param tools List of ToolSpecification instances
     * @param config Node configuration
     * @return This builder
     */
    public AIWorkflowBuilder agents(String nodeId,
                                   Object tools,  // List<dev.langchain4j.agent.tool.ToolSpecification>
                                   Consumer<AgentNode.Builder> config) {
        AgentNode.Builder builder = AgentNode.Builder.builder(ai);
        builder.tools(tools);
        config.accept(builder);
        workflowBuilder.action(nodeId, builder.build());
        return this;
    }

    /**
     * Add a vision node for multimodal operations (image analysis, audio transcription, video description).
     *
     * <p>Vision nodes support:
     * <ul>
     *   <li>Image analysis using vision-capable models (GPT-4 Vision, Claude, etc.)</li>
     *   <li>Audio transcription (when audio transcription models are available)</li>
     *   <li>Video description (when video analysis is available)</li>
     *   <li>PDF processing</li>
     * </ul>
     *
     * <p>Example:
     * <pre>
     * ai.workflow("image-analysis")
     *     .trigger("webhook", WebhookTrigger.create(...))
     *     .vision("analyze", builder -> builder
     *         .operation(VisionNode.VisionOperation.ANALYZE_IMAGE)
     *         .inputKey("imageUrl")
     *         .prompt("What's in this image?")
     *         .outputKey("description")
     *     )
     *     .build();
     * </pre>
     *
     * @param nodeId Node identifier
     * @param config Node configuration
     * @return This builder
     */
    public AIWorkflowBuilder vision(String nodeId,
                                    Consumer<VisionNode.Builder> config) {
        VisionNode.Builder builder = VisionNode.Builder.builder(ai);
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


    /**
     * Add an audio transcription node.
     *
     * Convenience method for audio transcription operations. Wraps VisionNode
     * with VisionOperation.TRANSCRIBE_AUDIO automatically set.
     *
     * <p>Example:
     * <pre>
     * ai.workflow("transcribe")
     *     .trigger("start", ManualTrigger.create())
     *     .audio("transcribe", builder -> builder
     *         .inputKey("audioUrl")
     *         .outputKey("transcript")
     *     )
     *     .build();
     * </pre>
     *
     * @param nodeId Node identifier
     * @param config Node configuration
     * @return This builder
     */
    public AIWorkflowBuilder audio(String nodeId,
                                   Consumer<VisionNode.Builder> config) {
        VisionNode.Builder builder = VisionNode.Builder.builder(ai);
        builder.operation(VisionNode.VisionOperation.TRANSCRIBE_AUDIO);
        config.accept(builder);
        workflowBuilder.action(nodeId, builder.build());
        return this;
    }

    /**
     * Fork a child workflow (nested execution).
     *
     * Executes a child workflow within parent workflow context.
     * Parent waits for child completion and receives child's result.
     *
     * Supports parallel execution when multiple fork nodes are connected with parallel edges.
     *
     * Example:
     * <pre>
     * Workflow child = ai.workflow("child")
     *     .trigger("start", ManualTrigger.create())
     *     .llm("process", builder -> builder.outputKey("result"))
     *     .build();
     *
     * ai.workflow("parent")
     *     .trigger("start", ManualTrigger.create())
     *     .fork("child", child, builder -> builder
     *         .inputKey("data")  // Pass parent's data to child
     *         .outputKey("childResult")  // Get child's result back
     *     )
     *     .llm("finalize", builder -> builder.inputKey("childResult"))
     *     .edge("start", "child")
     *     .edge("child", "finalize")
     *     .build();
     * </pre>
     *
     * @param nodeId Node identifier
     * @param childWorkflow Child workflow to execute
     * @param config Fork configuration
     * @return This builder
     */
    public AIWorkflowBuilder fork(String nodeId, Workflow childWorkflow, Consumer<ForkNode.Builder> config) {
        ForkNode.Builder builder = ForkNode.builder(childWorkflow);
        config.accept(builder);
        workflowBuilder.action(nodeId, builder.build());
        return this;
    }

    /**
     * Fork a child workflow with default configuration.
     *
     * Passes entire parent context to child and merges child result back into parent context.
     *
     * @param nodeId Node identifier
     * @param childWorkflow Child workflow to execute
     * @return This builder
     */
    public AIWorkflowBuilder fork(String nodeId, Workflow childWorkflow) {
        ForkNode.Builder builder = ForkNode.builder(childWorkflow);
        workflowBuilder.action(nodeId, builder.build());
        return this;
    }

    // ========== Workflow Pattern Convenience Methods ==========

    /**
     * Wrap an LLM node with circuit breaker protection.
     *
     * <p>Protects LLM calls from cascading failures. Circuit opens after threshold failures.
     *
     * <p>Example:
     * <pre>
     * CircuitBreaker breaker = CircuitBreaker.withThreshold(3, Duration.ofSeconds(5));
     *
     * ai.workflow("agent")
     *     .trigger("webhook", WebhookTrigger.create(...))
     *     .circuit("analyze", breaker, builder -> builder
     *         .systemPrompt("Analyze data")
     *         .inputKey("data")
     *         .outputKey("analysis")
     *     )
     *     .build();
     * </pre>
     *
     * @param nodeId Node identifier
     * @param breaker Circuit breaker instance
     * @param config LLM node configuration
     * @return This builder
     */
    public AIWorkflowBuilder circuit(String nodeId,
                                    com.akilisha.oss.roya.workflow.resilience.CircuitBreaker breaker,
                                    Consumer<LLMActionNode.Builder> config) {
        LLMActionNode.Builder llmBuilder = LLMActionNode.builder(ai);
        config.accept(llmBuilder);
        LLMActionNode llmNode = llmBuilder.build();

        com.akilisha.oss.roya.workflow.resilience.CircuitBreakerNode protectedNode =
            new com.akilisha.oss.roya.workflow.resilience.CircuitBreakerNode(llmNode, breaker);
        workflowBuilder.action(nodeId, protectedNode);
        return this;
    }

    /**
     * Wrap an LLM node with a loop for retry/repeat execution.
     *
     * <p>Example:
     * <pre>
     * ai.workflow("agent")
     *     .trigger("webhook", WebhookTrigger.create(...))
     *     .loop("retry", 3, LoopNode.LoopStrategy.UNTIL_SUCCESS, builder -> builder
     *         .systemPrompt("Generate response")
     *         .inputKey("message")
     *         .outputKey("response")
     *     )
     *     .build();
     * </pre>
     *
     * @param nodeId Node identifier
     * @param iterations Number of iterations (or max attempts for UNTIL_SUCCESS)
     * @param strategy Loop strategy (SEQUENTIAL, PARALLEL, UNTIL_SUCCESS, UNTIL_FAILURE)
     * @param config LLM node configuration
     * @return This builder
     */
    public AIWorkflowBuilder loop(String nodeId,
                                  int iterations,
                                  com.akilisha.oss.roya.workflow.loop.LoopNode.LoopStrategy strategy,
                                  Consumer<LLMActionNode.Builder> config) {
        LLMActionNode.Builder llmBuilder = LLMActionNode.builder(ai);
        config.accept(llmBuilder);
        LLMActionNode llmNode = llmBuilder.build();

        com.akilisha.oss.roya.workflow.loop.LoopNode loopNode =
            new com.akilisha.oss.roya.workflow.loop.LoopNode(llmNode, iterations, strategy);
        workflowBuilder.action(nodeId, loopNode);
        return this;
    }

    /**
     * Chain human approval before LLM execution.
     *
     * <p>Creates an approval node that precedes the LLM node. The LLM only executes if approval is granted.
     *
     * <p>Example:
     * <pre>
     * ApprovalProvider provider = new PollingApprovalProvider();
     *
     * ai.workflow("agent")
     *     .trigger("webhook", WebhookTrigger.create(...))
     *     .approval("generate", provider, "Approve this action?", builder -> builder
     *         .systemPrompt("Generate response")
     *         .inputKey("message")
     *         .outputKey("response")
     *     )
     *     .edge("webhook", "generate-approval")  // Webhook → Approval node
     *     .edge("generate-approval", "generate")  // Approval → LLM node (created automatically)
     *     .build();
     * </pre>
     *
     * <p><b>Note:</b> The approval node ID will be `{nodeId}-approval` and the LLM node will be `{nodeId}`.
     * The edge from approval to LLM is created automatically. You only need to connect to `{nodeId}-approval`.
     *
     * @param nodeId Node identifier (for the LLM node; approval node will be `{nodeId}-approval`)
     * @param provider Approval provider instance
     * @param prompt Approval prompt/question
     * @param config LLM node configuration
     * @return This builder
     */
    public AIWorkflowBuilder approval(String nodeId,
                                     com.akilisha.oss.roya.workflow.hitm.ApprovalProvider provider,
                                     String prompt,
                                     Consumer<LLMActionNode.Builder> config) {
        LLMActionNode.Builder llmBuilder = LLMActionNode.builder(ai);
        config.accept(llmBuilder);
        LLMActionNode llmNode = llmBuilder.build();

        // Chain: approval → LLM
        String approvalNodeId = nodeId + "-approval";
        workflowBuilder.action(approvalNodeId,
            new com.akilisha.oss.roya.workflow.hitm.HumanApprovalNode(provider, prompt));
        workflowBuilder.action(nodeId, llmNode);
        workflowBuilder.edge(approvalNodeId, nodeId);

        return this;
    }

    /**
     * Create a cost tracker for budget management.
     *
     * <p>This is a convenience method that returns a `CostTracker` instance.
     * The tracker should be added as a visitor to the `WorkflowExecutor` (not part of workflow builder chain).
     *
     * <p>Example:
     * <pre>
     * Workflow workflow = ai.workflow("agent")
     *     .trigger("webhook", WebhookTrigger.create(...))
     *     .llm("analyze", builder -> builder.systemPrompt("..."))
     *     .build();
     *
     * // Create cost tracker (not part of builder chain)
     * CostTracker tracker = ai.workflow("agent").costing(10.00)  // $10 budget
     *     .withNodeCost("analyze", 0.01);
     *
     * // Add tracker to executor
     * WorkflowExecutor executor = new WorkflowExecutor(workflow)
     *     .addVisitor(tracker);
     * </pre>
     *
     * <p><b>Note:</b> This method breaks the fluent builder chain because it returns a `CostTracker` visitor,
     * not a builder. Use it after calling `.build()` or create the tracker separately.
     *
     * @param budget Budget limit in dollars
     * @return CostTracker instance for visitor registration
     */
    public com.akilisha.oss.roya.workflow.cost.CostTracker costing(double budget) {
        return new com.akilisha.oss.roya.workflow.cost.CostTracker(budget);
    }

    /**
     * Integrate MCP (Model Context Protocol) tools with an LLM node.
     *
     * <p>Discovers tools from MCP servers and configures them for use with the LLM.
     * Uses the MCPClient to discover and cache tools automatically.
     *
     * <p>Example:
     * <pre>
     * MCPClient mcpClient = MCPClient.builder()
     *     .server("mcp-server-1", "https://mcp-server.example.com")
     *     .server("mcp-server-2", "https://another-mcp.example.com")
     *     .cacheDuration(Duration.ofMinutes(10))
     *     .healthCheckInterval(Duration.ofMinutes(5))
     *     .build();
     *
     * ai.workflow("mcp-agent")
     *     .trigger("webhook", WebhookTrigger.create(...))
     *     .mcp("agent", mcpClient, builder -> builder
     *         .systemPrompt("You have access to MCP tools")
     *         .inputKey("message")
     *         .outputKey("response")
     *     )
     *     .build();
     * </pre>
     *
     * @param nodeId Node identifier
     * @param mcpClient MCP client instance
     * @param config LLM node configuration
     * @return This builder
     */
    public AIWorkflowBuilder mcp(String nodeId,
                                com.akilisha.oss.roya.plugins.ai.mcp.MCPClient mcpClient,
                                Consumer<LLMActionNode.Builder> config) {
        // Discover tools from MCP client
        List<Object> tools = mcpClient.discoverAllTools();

        // Use llmWithTools to configure LLM with MCP tools
        return llmWithTools(nodeId, tools, config);
    }

    // ========== Nested & Continuation Workflows ==========

    /**
     * Execute multiple child workflows in parallel and aggregate results.
     *
     * <p>This is a convenience method that delegates to {@link Workflow.WorkflowBuilder#nested(String, java.util.List, com.akilisha.oss.roya.workflow.nested.ResultAggregator, com.akilisha.oss.roya.workflow.nested.NestedExecutionStrategy)}.
     *
     * <p>Example:
     * <pre>
     * Workflow billingWorkflow = ai.workflow("billing").trigger("start", ...).build();
     * Workflow historyWorkflow = ai.workflow("history").trigger("start", ...).build();
     *
     * Workflow main = ai.workflow("support-agent")
     *     .trigger("webhook", WebhookTrigger.create(...))
     *     .nested("gatherContext",
     *         List.of(billingWorkflow, historyWorkflow),
     *         new MergeAllAggregator(true),
     *         NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT
     *     )
     *     .build();
     * </pre>
     *
     * @param nodeId Node identifier
     * @param childWorkflows List of child workflows to execute in parallel
     * @param aggregator Result aggregator (MergeAllAggregator, CollectAllAggregator, SelectBestAggregator)
     * @param strategy Execution strategy (WAIT_FOR_ALL, WAIT_FOR_ALL_BEST_EFFORT, FIRST_SUCCESS, BEST_OF_ALL)
     * @return This builder
     */
    public AIWorkflowBuilder nested(String nodeId,
                                    java.util.List<Workflow> childWorkflows,
                                    com.akilisha.oss.roya.workflow.nested.ResultAggregator aggregator,
                                    com.akilisha.oss.roya.workflow.nested.NestedExecutionStrategy strategy) {
        workflowBuilder.nested(nodeId, childWorkflows, aggregator, strategy);
        return this;
    }

    /**
     * Execute multiple child workflows in parallel with default timeout.
     *
     * @param nodeId Node identifier
     * @param childWorkflows List of child workflows to execute in parallel
     * @param aggregator Result aggregator
     * @return This builder
     */
    public AIWorkflowBuilder nested(String nodeId,
                                    java.util.List<Workflow> childWorkflows,
                                    com.akilisha.oss.roya.workflow.nested.ResultAggregator aggregator) {
        workflowBuilder.nested(nodeId, childWorkflows, aggregator);
        return this;
    }

    /**
     * Chain a child workflow sequentially (continuation pattern).
     *
     * <p>This is a convenience method that delegates to {@link Workflow.WorkflowBuilder#continuation(String, Workflow, String)}.
     *
     * <p>Example:
     * <pre>
     * Workflow scrapeWorkflow = ai.workflow("scrape").trigger("start", ...).build();
     * Workflow analyzeWorkflow = ai.workflow("analyze").trigger("start", ...).build();
     *
     * Workflow pipeline = ai.workflow("data-pipeline")
     *     .trigger("init", input -> ...)
     *     .continuation("scrape", scrapeWorkflow, "start")
     *     .continuation("analyze", analyzeWorkflow, "start")
     *     .edge("init", "scrape")
     *     .edge("scrape", "analyze")
     *     .build();
     * </pre>
     *
     * @param nodeId Node identifier
     * @param childWorkflow Child workflow to execute
     * @param startNodeId Entry point node ID in the child workflow
     * @return This builder
     */
    public AIWorkflowBuilder continuation(String nodeId, Workflow childWorkflow, String startNodeId) {
        workflowBuilder.continuation(nodeId, childWorkflow, startNodeId);
        return this;
    }

    /**
     * Chain a child workflow sequentially with timeout.
     *
     * @param nodeId Node identifier
     * @param childWorkflow Child workflow to execute
     * @param startNodeId Entry point node ID in the child workflow
     * @param timeout Timeout for the continuation
     * @return This builder
     */
    public AIWorkflowBuilder continuation(String nodeId, Workflow childWorkflow, String startNodeId, Duration timeout) {
        workflowBuilder.continuation(nodeId, childWorkflow, startNodeId, timeout);
        return this;
    }

    /**
     * Chain a child workflow sequentially with namespace isolation.
     *
     * <p>Use this when you want to prevent context key collisions between parent and child workflows.
     *
     * @param nodeId Node identifier
     * @param childWorkflow Child workflow to execute
     * @param startNodeId Entry point node ID in the child workflow
     * @param namespace Namespace prefix for child workflow context keys
     * @return This builder
     */
    public AIWorkflowBuilder continuationWithNamespace(String nodeId, Workflow childWorkflow, String startNodeId, String namespace) {
        workflowBuilder.continuationWithNamespace(nodeId, childWorkflow, startNodeId, namespace);
        return this;
    }

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
     * Automatically registers any CronJobTrigger nodes with the Quartz scheduler.
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
            if (node instanceof WebhookTrigger webhookTrigger) {
                webhookTrigger.register(workflow);
            }
            // Register any CronJobTrigger nodes with the scheduler
            if (node instanceof CronJobTrigger cronJobTrigger) {
                cronJobTrigger.register(workflow);
            }
            // Register any FileWatchTrigger nodes with the registry
            if (node instanceof FileWatchTrigger fileWatchTrigger) {
                fileWatchTrigger.register(workflow);
            }
            // Register any PollingTrigger nodes with the registry
            if (node instanceof PollingTrigger pollingTrigger) {
                pollingTrigger.register(workflow);
            }
            // Register any WorkflowTrigger nodes with the chain registry
            if (node instanceof WorkflowTrigger workflowTrigger) {
                workflowTrigger.register(workflow);
            }
        });

        return workflow;
    }

    // ========== Direct Access (for advanced use) ==========

    /**
     * Add a custom action node (for advanced use cases).
     *
     * Use this when you need to add a custom WorkflowNode that doesn't have
     * a convenience method in AIWorkflowBuilder.
     *
     * @param nodeId Node identifier
     * @param node Custom workflow node
     * @return This builder
     */
    public AIWorkflowBuilder action(String nodeId, WorkflowNode node) {
        workflowBuilder.action(nodeId, node);
        return this;
    }

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



