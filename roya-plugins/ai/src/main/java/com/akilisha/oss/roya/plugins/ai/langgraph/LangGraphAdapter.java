package com.akilisha.oss.roya.plugins.ai.langgraph;

import com.akilisha.oss.roya.plugins.ai.*;
import com.akilisha.oss.roya.plugins.ai.builder.AIWorkflowBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * LangGraph adapter implementation of Roya's AI interface.
 *
 * LangGraph4j is built on LangChain4j and provides stateful, multi-node agent workflows.
 * This adapter leverages:
 * - LangChain4j for LLM primitives (chat, embeddings)
 * - LangGraph4j for agent orchestration and state management
 *
 * Key features:
 * - Stateful agents with context across multiple nodes
 * - Multi-agent workflows and coordination
 * - Agent executor for tool-enabled agents
 * - Graph visualization and debugging
 */
public class LangGraphAdapter implements AI {

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;
    private final EmbeddingModel embeddingModel;
    private final ObjectMapper objectMapper;

    public LangGraphAdapter(ChatModel chatModel, StreamingChatModel streamingChatModel, EmbeddingModel embeddingModel) {
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
        this.embeddingModel = embeddingModel;
        this.objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public LLM llm() {
        // Delegate to LangChain4j ChatModel (same as LangChainAdapter)
        return new LLM() {
            @Override
            public String ask(String systemPrompt, String userMessage) {
                return LangGraphAdapter.this.ask(systemPrompt, userMessage);
            }

            @Override
            public String ask(String systemPrompt, String userMessage, AIOptions options) {
                return LangGraphAdapter.this.ask(systemPrompt, userMessage, options);
            }

            @Override
            public <T> T extract(Class<T> type, String prompt) {
                return LangGraphAdapter.this.extract(type, prompt);
            }

            @Override
            public <T> T extract(Class<T> type, String prompt, AIOptions options) {
                return LangGraphAdapter.this.extract(type, prompt, options);
            }

            @Override
            public void stream(String systemPrompt, String userMessage, Consumer<String> onToken) {
                LangGraphAdapter.this.stream(systemPrompt, userMessage, onToken);
            }

            @Override
            public void stream(String systemPrompt, String userMessage, AIOptions options, Consumer<String> onToken) {
                LangGraphAdapter.this.stream(systemPrompt, userMessage, options, onToken);
            }
        };
    }

    @Override
    public Embeddings embeddings() {
        // Delegate to LangChain4j EmbeddingModel
        return new Embeddings() {
            @Override
            public float[] embed(String text) {
                try {
                    Response<Embedding> response = embeddingModel.embed(text);
                    Embedding embedding = response.content();
                    return embedding.vector();
                } catch (Exception e) {
                    throw new AIException("LangGraph embedding failed: " + e.getMessage(), e);
                }
            }

            @Override
            public List<float[]> embed(List<String> texts) {
                try {
                    List<dev.langchain4j.data.segment.TextSegment> segments = texts.stream()
                        .map(dev.langchain4j.data.segment.TextSegment::from)
                        .collect(Collectors.toList());

                    Response<List<Embedding>> response = embeddingModel.embedAll(segments);
                    List<Embedding> embeddings = response.content();

                    return embeddings.stream()
                        .map(Embedding::vector)
                        .collect(Collectors.toList());
                } catch (Exception e) {
                    throw new AIException("LangGraph batch embedding failed: " + e.getMessage(), e);
                }
            }
        };
    }

    @Override
    public Vectors vectors() {
        // TODO: Implement vector operations
        throw new UnsupportedOperationException("Vectors not yet implemented in LangGraph adapter");
    }

    @Override
    public RAGApi ragApi() {
        // TODO: Implement RAG using adaptive-rag module
        throw new UnsupportedOperationException("RAG not yet implemented in LangGraph adapter");
    }

    @Override
    public Agents agents() {
        // LangGraph's primary strength: stateful agent orchestration
        return new Agents() {
            @Override
            public Agent create(Consumer<AgentBuilder> config) {
                // TODO: Implement LangGraph agent creation
                // LangGraph4j provides StateGraph for building stateful agent workflows
                // For now, create a simple wrapper that uses ChatModel directly
                // Full LangGraph integration requires StateGraph setup
                AgentBuilderImpl builder = new AgentBuilderImpl();
                config.accept(builder);

                // Use ChatModel directly for now until we have StateGraph integration
                // TODO: Replace with AgentExecutor or StateGraph-based agent
                ChatModel agentModel = builder.chatLanguageModel != null
                    ? (ChatModel) builder.chatLanguageModel
                    : chatModel;

                return new Agent() {
                    @Override
                    public AgentResult run(String input) {
                        try {
                            // Build prompt with system message if provided
                            String prompt = builder.systemPrompt != null
                                ? builder.systemPrompt + "\n\nUser: " + input
                                : input;

                            // Execute using ChatModel
                            String response = agentModel.chat(prompt);

                            // TODO: Extract trace/step information from LangGraph execution
                            // When using StateGraph, we'll have node execution traces
                            return new AgentResult(response, List.of());
                        } catch (Exception e) {
                            throw new AIException("LangGraph agent execution failed: " + e.getMessage(), e);
                        }
                    }
                };
            }
        };
    }

    // Convenience methods - delegate to llm()

    @Override
    public String ask(String systemPrompt, String userMessage) {
        return ask(systemPrompt, userMessage, AIOptions.defaults());
    }

    @Override
    public String ask(String systemPrompt, String userMessage, AIOptions options) {
        try {
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                userMessage = systemPrompt + "\n\nUser: " + userMessage;
            }
            return chatModel.chat(userMessage);
        } catch (Exception e) {
            throw new AIException("LangGraph chat completion failed: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> T extract(Class<T> type, String prompt) {
        return extract(type, prompt, AIOptions.defaults());
    }

    @Override
    public <T> T extract(Class<T> type, String prompt, AIOptions options) {
        try {
            String jsonPrompt = "Extract information from the following text into JSON format matching this schema: "
                + type.getSimpleName() + "\n\n" + prompt
                + "\n\nRespond with JSON only.";
            String response = chatModel.chat(jsonPrompt);

            return objectMapper.readValue(response, type);
        } catch (Exception e) {
            throw new AIException("LangGraph extraction failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void stream(String systemPrompt, String userMessage, Consumer<String> onToken) {
        stream(systemPrompt, userMessage, AIOptions.defaults(), onToken);
    }

    @Override
    public void stream(String systemPrompt, String userMessage, AIOptions options, Consumer<String> onToken) {
        try {
            if (streamingChatModel == null) {
                throw new AIException("Streaming not supported - no StreamingChatModel configured");
            }
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                userMessage = systemPrompt + "\n\nUser: " + userMessage;
            }

            streamingChatModel.chat(userMessage, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    onToken.accept(partialResponse);
                }

                @Override
                public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse completeResponse) {
                    // Streaming complete
                }

                @Override
                public void onError(Throwable error) {
                    throw new AIException("LangGraph streaming error: " + error.getMessage(), error);
                }
            });
        } catch (Exception e) {
            throw new AIException("LangGraph streaming failed: " + e.getMessage(), e);
        }
    }

    @Override
    public RAGResponse rag(String question) {
        return rag(question, RAGOptions.builder().build());
    }

    @Override
    public RAGResponse rag(String question, RAGOptions options) {
        // TODO: Implement RAG
        throw new UnsupportedOperationException("RAG not yet implemented in LangGraph adapter");
    }

    @Override
    public <T> T provider(Class<T> providerType) {
        if (providerType.isInstance(chatModel)) {
            return providerType.cast(chatModel);
        }
        if (providerType.isInstance(streamingChatModel)) {
            return providerType.cast(streamingChatModel);
        }
        if (providerType.isInstance(embeddingModel)) {
            return providerType.cast(embeddingModel);
        }
        return null;
    }

    @Override
    public AIResponse<String> askWithMetadata(String systemPrompt, String userMessage, AIOptions options) {
        String answer = ask(systemPrompt, userMessage, options);
        // TODO: Extract token usage from response
        return new AIResponse<>(answer, options.model(), 0, 0, 0, 0.0, false);
    }

    @Override
    public <T> AIResponse<T> extractWithMetadata(Class<T> type, String prompt, AIOptions options) {
        T data = extract(type, prompt, options);
        // TODO: Extract token usage from response
        return new AIResponse<>(data, options.model(), 0, 0, 0, 0.0, false);
    }

    @Override
    public AIWorkflowBuilder workflow(String name) {
        throw new UnsupportedOperationException(
            "Workflow API not available in LangGraphAdapter. Use UnifiedAIService for workflows."
        );
    }

    @Override
    public LangGraphService langGraph() {
        // Return self wrapped in service
        return new LangGraphServiceImpl(this);
    }

    @Override
    public GoogleADKService googleADK() {
        return null; // Not available in LangGraphAdapter
    }
    
    @Override
    public <T> T aiService(Class<T> serviceClass) {
        throw new UnsupportedOperationException("AI Services implementation coming soon - use AiServices.create() directly for now");
    }

    /**
     * Internal AgentBuilder implementation.
     */
    private static class AgentBuilderImpl implements AgentBuilder {
        private Object chatLanguageModel;
        private List<Object> tools = List.of();
        private String systemPrompt;

        @Override
        public AgentBuilder model(Object chatLanguageModel) {
            this.chatLanguageModel = chatLanguageModel;
            return this;
        }

        @Override
        public AgentBuilder tools(List<Object> tools) {
            this.tools = tools != null ? tools : List.of();
            return this;
        }

        @Override
        public AgentBuilder systemPrompt(String prompt) {
            this.systemPrompt = prompt;
            return this;
        }
    }
}



