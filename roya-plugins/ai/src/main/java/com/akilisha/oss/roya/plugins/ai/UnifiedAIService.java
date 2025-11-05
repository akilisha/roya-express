package com.akilisha.oss.roya.plugins.ai;

import com.akilisha.oss.roya.plugins.ai.builder.AIWorkflowBuilder;
import com.akilisha.oss.roya.plugins.ai.googleadk.GoogleADKAdapter;
import com.akilisha.oss.roya.plugins.ai.langchain.LangChainAdapter;
import com.akilisha.oss.roya.plugins.ai.langgraph.LangGraphAdapter;

import java.util.function.Consumer;

/**
 * Unified AI service that orchestrates LangChain, LangGraph, and Google ADK.
 *
 * Each library plays to its strengths:
 * - LangChain4j: LLM primitives (chat, embeddings), tool integration
 * - LangGraph4j: Stateful agent workflows, multi-node graphs, context management
 * - Google ADK: High-level agent orchestration, multi-agent systems, advanced workflows
 *
 * The unified service delegates to the appropriate library based on the operation,
 * allowing developers to use the best tool for each task while maintaining a single API.
 */
public class UnifiedAIService implements AI {

    // Core library adapters
    private final LangChainAdapter langChain;    // LLM primitives, embeddings, tools
    private final LangGraphAdapter langGraph;    // Stateful workflows, multi-node agents
    private final GoogleADKAdapter googleADK;   // High-level orchestration (when available)

    /**
     * Create unified service with all three libraries.
     */
    public UnifiedAIService(LangChainAdapter langChain, LangGraphAdapter langGraph, GoogleADKAdapter googleADK) {
        this.langChain = langChain;
        this.langGraph = langGraph;
        this.googleADK = googleADK;
    }

    /**
     * Create unified service with LangChain and LangGraph (ADK optional).
     */
    public UnifiedAIService(LangChainAdapter langChain, LangGraphAdapter langGraph) {
        this(langChain, langGraph, null);
    }

    @Override
    public AIWorkflowBuilder workflow(String name) {
        return AIWorkflowBuilder.create(this, name);
    }

    @Override
    public LangGraphService langGraph() {
        if (langGraph == null) {
            return null;
        }
        return new LangGraphServiceImpl(langGraph);
    }

    @Override
    public GoogleADKService googleADK() {
        if (googleADK == null) {
            return null;
        }
        return new GoogleADKServiceImpl(googleADK);
    }
    
    @Override
    public <T> T aiService(Class<T> serviceClass) {
        // Delegate to LangChain adapter (primary implementation)
        // LangChain4j's AI Services will be used here
        if (langChain != null) {
            return langChain.aiService(serviceClass);
        }
        throw new UnsupportedOperationException("AI Services require LangChain adapter");
    }

    @Override
    public LLM llm() {
        // LangChain4j is the best for basic LLM operations
        return langChain.llm();
    }

    @Override
    public Embeddings embeddings() {
        // LangChain4j excels at embeddings
        return langChain.embeddings();
    }

    @Override
    public Vectors vectors() {
        // Try LangChain first, fall back to LangGraph if available
        try {
            return langChain.vectors();
        } catch (UnsupportedOperationException e) {
            return langGraph.vectors();
        }
    }

    @Override
    public RAGApi ragApi() {
        // LangGraph4j has adaptive-rag module (more advanced)
        // Try LangGraph first, fall back to LangChain
        try {
            return langGraph.ragApi();
        } catch (UnsupportedOperationException e) {
            try {
                return langChain.ragApi();
            } catch (UnsupportedOperationException e2) {
                throw new UnsupportedOperationException("RAG not available in either library");
            }
        }
    }

    @Override
    public Agents agents() {
        // Agents can use different libraries based on complexity:
        // - Simple agents: LangChain4j
        // - Stateful workflows: LangGraph4j
        // - Complex orchestration: Google ADK
        return new Agents() {
            @Override
            public Agent create(Consumer<AgentBuilder> config) {
                // For now, use LangGraph (stateful workflows are its strength)
                // TODO: Add logic to choose library based on config complexity
                // Simple agent → LangChain
                // Stateful/multi-node → LangGraph
                // Multi-agent orchestration → Google ADK
                return langGraph.agents().create(config);
            }
        };
    }
    
    @Override
    public Vision vision() {
        // Vision operations use LangChain4j's multimodal support
        if (langChain != null) {
            return langChain.vision();
        } else {
            throw new IllegalStateException("LangChain service not configured - required for vision operations");
        }
    }

    // Convenience methods delegate to LangChain (best for basic operations)

    @Override
    public String ask(String systemPrompt, String userMessage) {
        return llm().ask(systemPrompt, userMessage);
    }

    @Override
    public String ask(String systemPrompt, String userMessage, AIOptions options) {
        return llm().ask(systemPrompt, userMessage, options);
    }

    @Override
    public <T> T extract(Class<T> type, String prompt) {
        return llm().extract(type, prompt);
    }

    @Override
    public <T> T extract(Class<T> type, String prompt, AIOptions options) {
        return llm().extract(type, prompt, options);
    }

    @Override
    public void stream(String systemPrompt, String userMessage, Consumer<String> onToken) {
        llm().stream(systemPrompt, userMessage, onToken);
    }

    @Override
    public void stream(String systemPrompt, String userMessage, AIOptions options, Consumer<String> onToken) {
        llm().stream(systemPrompt, userMessage, options, onToken);
    }

    @Override
    public RAGResponse rag(String question) {
        return ragApi().ask(question);
    }

    @Override
    public RAGResponse rag(String question, RAGOptions options) {
        return ragApi().ask(question, options);
    }

    @Override
    public <T> T provider(Class<T> providerType) {
        // Try each library in order of preference
        // Developers can access underlying libraries directly
        T provider = langChain.provider(providerType);
        if (provider != null) return provider;

        provider = langGraph.provider(providerType);
        if (provider != null) return provider;

        if (googleADK != null) {
            provider = googleADK.provider(providerType);
            if (provider != null) return provider;
        }

        return null;
    }

    @Override
    public AIResponse<String> askWithMetadata(String systemPrompt, String userMessage, AIOptions options) {
        return langChain.askWithMetadata(systemPrompt, userMessage, options);
    }

    @Override
    public <T> AIResponse<T> extractWithMetadata(Class<T> type, String prompt, AIOptions options) {
        return langChain.extractWithMetadata(type, prompt, options);
    }

    /**
     * Direct access to underlying libraries for advanced use cases.
     */
    public LangChainAdapter langChain() {
        return langChain;
    }
}



