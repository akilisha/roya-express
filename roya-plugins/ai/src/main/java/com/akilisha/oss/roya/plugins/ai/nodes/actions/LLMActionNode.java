package com.akilisha.oss.roya.plugins.ai.nodes.actions;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIOptions;
import com.akilisha.oss.roya.plugins.ai.langchain.LangChainAdapter;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;
import dev.langchain4j.memory.ChatMemory;
// Note: ContentRetriever, ChatMemory, and ToolSpecification are configured
// via AiServices.builder() pattern, not as direct dependencies.
// We store them as Object to avoid package dependencies.
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * LLM action node - wraps AI.llm() operations in a workflow node.
 *
 * Supports:
 * - System prompts
 * - Custom AI options (temperature, model, etc.)
 * - Configurable input/output keys
 * - RAG (Retrieval-Augmented Generation) via ContentRetriever
 * - Tools via ToolSpecification
 * - Memory via ChatMemory
 *
 * Example:
 * <pre>
 * LLMActionNode node = LLMActionNode.builder(ai)
 *     .systemPrompt("You are a helpful assistant")
 *     .inputKey("message")
 *     .outputKey("response")
 *     .options(AIOptions.builder().temperature(0.7).build())
 *     .rag(contentRetriever)  // Enable RAG
 *     .tools(toolSpecs)       // Enable tools
 *     .memory(chatMemory, "user-123")  // Enable memory
 *     .build();
 * </pre>
 */
public class LLMActionNode implements WorkflowNode {

    private final AI ai;
    private final String systemPrompt;
    private final AIOptions options;
    private final String inputKey;
    private final String outputKey;

    // AI Services features (stored as Object to avoid package dependencies)
    // These are configured via AiServices.builder() pattern
    private final Object contentRetriever;  // dev.langchain4j.data.retriever.ContentRetriever
    private final Object tools;  // List<dev.langchain4j.agent.tool.ToolSpecification>
    private final Object chatMemory;  // dev.langchain4j.memory.ChatMemory
    private final String memoryIdKey;  // Key in context to get memory ID

    // Constructor for simple cases
    public LLMActionNode(AI ai, String systemPrompt, String inputKey, String outputKey) {
        this(ai, systemPrompt, AIOptions.defaults(), inputKey, outputKey, null, null, null, null);
    }

    // Full constructor
    public LLMActionNode(AI ai, String systemPrompt, AIOptions options,
                        String inputKey, String outputKey,
                        Object contentRetriever,
                        Object tools,
                        Object chatMemory,
                        String memoryIdKey) {
        this.ai = ai;
        this.systemPrompt = systemPrompt;
        this.options = options != null ? options : AIOptions.defaults();
        this.inputKey = inputKey;
        this.outputKey = outputKey;
        this.contentRetriever = contentRetriever;
        this.tools = tools;
        this.chatMemory = chatMemory;
        this.memoryIdKey = memoryIdKey;
    }

    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get input from context
                String userMessage = input.getString(inputKey);
                if (userMessage == null) {
                    return NodeOutput.failure("Input key '" + inputKey + "' not found or null");
                }

                String response;

                // Use AI Services if RAG, tools, or memory are configured
                if (contentRetriever != null || tools != null || chatMemory != null) {
                    // Create specialized AI Service with RAG/tools/memory
                    response = executeWithAIServices(input, userMessage);
                } else {
                    // Use simple LLM call
                    response = ai.llm().ask(systemPrompt, userMessage, options);
                }

                // Return output - also merge into global context for downstream nodes
                input.context().set(outputKey, response);

                return NodeOutput.success(Map.of(outputKey, response));

            } catch (Exception e) {
                return NodeOutput.failure("LLM call failed: " + e.getMessage());
            }
        });
    }

    /**
     * Execute LLM call using AI Services with RAG/tools/memory support.
     */
    @SuppressWarnings("unchecked")
    private String executeWithAIServices(NodeInput input, String userMessage) {
        // Get the LangChainAdapter to access advanced aiService() builder
        var langChainAdapter = getLangChainAdapter();
        if (langChainAdapter == null) {
            // Fallback to simple call if not using LangChain
            return ai.llm().ask(systemPrompt, userMessage, options);
        }

        // Create a simple interface for this specific call
        interface SimpleLLMService {
            @dev.langchain4j.service.SystemMessage("{{systemPrompt}}")
            @dev.langchain4j.service.UserMessage("{{userMessage}}")
            String ask(String systemPrompt, String userMessage);
        }

        // Build AI Service with RAG/tools/memory support
        SimpleLLMService service = langChainAdapter.aiService(SimpleLLMService.class, builder -> {
            // Configure RAG if enabled
            if (contentRetriever != null) {
                try {
                    // Use reflection to call builder.contentRetriever()
                    var method = builder.getClass().getMethod("contentRetriever", Object.class);
                    method.invoke(builder, contentRetriever);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to configure ContentRetriever: " + e.getMessage(), e);
                }
            }

            // Configure tools if enabled
            if (tools != null) {
                try {
                    // Use reflection to call builder.tools()
                    var method = builder.getClass().getMethod("tools", java.util.List.class);
                    method.invoke(builder, tools);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to configure tools: " + e.getMessage(), e);
                }
            }

            // Configure memory if enabled
            if (chatMemory != null && memoryIdKey != null) {
                String memoryId = input.getString(memoryIdKey);
                if (memoryId != null) {
                    try {
                        // Use reflection to call builder.chatMemory() with memory ID
                        // Note: LangChain4j's ChatMemory builder needs to be configured with ID
                        // We'll need to create a memory instance with the ID
                        var method = builder.getClass().getMethod("chatMemory", dev.langchain4j.memory.ChatMemory.class);
                        method.invoke(builder, createMemoryWithId(chatMemory, memoryId));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to configure ChatMemory: " + e.getMessage(), e);
                    }
                }
            }
        });

        return service.ask(systemPrompt != null ? systemPrompt : "", userMessage);
    }

    /**
     * Get LangChainAdapter from AI service if available.
     */
    private LangChainAdapter getLangChainAdapter() {
        if (ai instanceof com.akilisha.oss.roya.plugins.ai.UnifiedAIService unified) {
            return unified.langChain();
        } else if (ai instanceof LangChainAdapter langChainAdapter) {
            return langChainAdapter;
        }
        return null;
    }

    /**
     * Create a ChatMemory instance with the specified ID.
     * This is a helper to configure memory with a specific conversation ID.
     */
    @SuppressWarnings("unchecked")
    private ChatMemory createMemoryWithId(Object memory, String memoryId) {
        // If memory is already a ChatMemory, we need to create a new one with the ID
        // For now, we'll assume the memory object can be used directly
        // In practice, you'd create a new MessageWindowChatMemory with the ID
        try {
            // Try to create a new memory instance with the ID
            var memoryClass = Class.forName("dev.langchain4j.memory.chat.MessageWindowChatMemory");
            var builderMethod = memoryClass.getMethod("builder");
            var builder = builderMethod.invoke(null);

            var idMethod = builder.getClass().getMethod("id", String.class);
            idMethod.invoke(builder, memoryId);

            // Copy maxMessages from existing memory if possible
            try {
                var maxMessagesMethod = memory.getClass().getMethod("maxMessages");
                var maxMessages = (Integer) maxMessagesMethod.invoke(memory);
                var maxMessagesBuilderMethod = builder.getClass().getMethod("maxMessages", int.class);
                maxMessagesBuilderMethod.invoke(builder, maxMessages);
            } catch (Exception e) {
                // Default to 10 messages if we can't get maxMessages
                var maxMessagesBuilderMethod = builder.getClass().getMethod("maxMessages", int.class);
                maxMessagesBuilderMethod.invoke(builder, 10);
            }

            var buildMethod = builder.getClass().getMethod("build");
            return (ChatMemory) buildMethod.invoke(builder);
        } catch (Exception e) {
            // Fallback: return the original memory if we can't create a new one
            return (ChatMemory) memory;
        }
    }

    /**
     * Create a builder for fluent configuration.
     */
    public static Builder builder(AI ai) {
        return new Builder(ai);
    }

    /**
     * Builder for LLMActionNode.
     */
    public static class Builder {
        private final AI ai;
        private String systemPrompt;
        private AIOptions options = AIOptions.defaults();
        private String inputKey = "message";
        private String outputKey = "response";

        // AI Services features (stored as Object to avoid package dependencies)
        private Object contentRetriever;  // dev.langchain4j.data.retriever.ContentRetriever
        private Object tools;  // List<dev.langchain4j.agent.tool.ToolSpecification>
        private Object chatMemory;  // dev.langchain4j.memory.ChatMemory
        private String memoryIdKey;

        Builder(AI ai) {
            this.ai = ai;
        }

        public Builder systemPrompt(String prompt) {
            this.systemPrompt = prompt;
            return this;
        }

        public Builder options(AIOptions opts) {
            this.options = opts;
            return this;
        }

        public Builder inputKey(String key) {
            this.inputKey = key;
            return this;
        }

        public Builder outputKey(String key) {
            this.outputKey = key;
            return this;
        }

        /**
         * Enable RAG (Retrieval-Augmented Generation).
         *
         * The ContentRetriever will automatically retrieve relevant context
         * from a vector store before generating the response.
         *
         * @param retriever ContentRetriever instance (dev.langchain4j.data.retriever.ContentRetriever)
         * @return This builder
         */
        public Builder rag(Object retriever) {
            this.contentRetriever = retriever;
            return this;
        }

        /**
         * Enable tool calling (function calling).
         *
         * The AI can automatically decide when to call tools and execute them.
         *
         * @param toolSpecs List of ToolSpecification instances (List&lt;dev.langchain4j.agent.tool.ToolSpecification&gt;)
         * @return This builder
         */
        public Builder tools(Object toolSpecs) {
            this.tools = toolSpecs;
            return this;
        }

        /**
         * Enable conversation memory.
         *
         * Maintains conversation history across multiple interactions.
         * The memoryIdKey should point to a value in the workflow context
         * that contains the unique conversation ID (e.g., user ID, session ID).
         *
         * @param memory ChatMemory instance (dev.langchain4j.memory.ChatMemory)
         * @param memoryIdKey Key in context to get memory ID
         * @return This builder
         */
        public Builder memory(Object memory, String memoryIdKey) {
            this.chatMemory = memory;
            this.memoryIdKey = memoryIdKey;
            return this;
        }

        public LLMActionNode build() {
            if (systemPrompt == null || systemPrompt.isBlank()) {
                systemPrompt = "You are a helpful assistant";
            }
            return new LLMActionNode(ai, systemPrompt, options, inputKey, outputKey,
                                    contentRetriever, tools, chatMemory, memoryIdKey);
        }
    }
}



