package com.akilisha.oss.roya.plugins.ai.langgraph;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.library.AILibrary;
import com.akilisha.oss.roya.plugins.ai.library.AILibraryConfig;

// LangChain4j core interfaces (LangGraph builds on LangChain4j)
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel;

/**
 * LangGraph library adapter factory.
 *
 * LangGraph4j is built on LangChain4j and provides stateful, multi-node agent workflows.
 * This factory creates LangGraphAdapter instances that leverage:
 * - LangChain4j for LLM primitives (chat, embeddings)
 * - LangGraph4j for agent orchestration with state management across nodes
 * 
 * Key capabilities:
 * - Stateful agents with context persistence across nodes
 * - Multi-agent workflows and coordination
 * - Agent executor for tool-enabled agents
 * - Graph visualization and debugging
 */
public class LangGraphLibrary implements AILibrary {
    @Override
    public String name() {
        return "langgraph";
    }

    @Override
    public AI create(AILibraryConfig config) {
        // Create ChatModel from provider config (reuse LangChain4j logic)
        ChatModel chatModel = createChatModel(config);
        
        // Create EmbeddingModel from provider config
        EmbeddingModel embeddingModel = createEmbeddingModel(config);
        
        // TODO: Create StreamingChatModel when needed
        StreamingChatModel streamingChatModel = null;
        
        // Create LangGraph adapter wrapper
        // LangGraphAdapter uses LangChain4j models for LLM/Embeddings
        // and LangGraph4j for agent orchestration
        return new LangGraphAdapter(chatModel, streamingChatModel, embeddingModel);
    }
    
    private ChatModel createChatModel(AILibraryConfig config) {
        // Try OpenAI first
        var openaiConfig = config.provider("openai");
        if (openaiConfig.isPresent()) {
            return OpenAiChatModel.builder()
                .apiKey(openaiConfig.get().apiKey())
                .modelName("gpt-3.5-turbo")
                .build();
        }
        
        // Try Anthropic
        var anthropicConfig = config.provider("anthropic");
        if (anthropicConfig.isPresent()) {
            return AnthropicChatModel.builder()
                .apiKey(anthropicConfig.get().apiKey())
                .modelName("claude-3-haiku-20240307")
                .build();
        }
        
        throw new IllegalArgumentException(
            "No provider configured. Configure either 'openai' or 'anthropic' in providers."
        );
    }
    
    private EmbeddingModel createEmbeddingModel(AILibraryConfig config) {
        // Try OpenAI embeddings
        var openaiConfig = config.provider("openai");
        if (openaiConfig.isPresent()) {
            return OpenAiEmbeddingModel.builder()
                .apiKey(openaiConfig.get().apiKey())
                .modelName("text-embedding-3-small")
                .build();
        }
        
        throw new IllegalArgumentException(
            "No embedding provider configured. Configure 'openai' in providers."
        );
    }
}



