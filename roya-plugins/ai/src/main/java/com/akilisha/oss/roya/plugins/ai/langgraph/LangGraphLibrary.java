package com.akilisha.oss.roya.plugins.ai.langgraph;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.library.AILibrary;
import com.akilisha.oss.roya.plugins.ai.library.AILibraryConfig;
import com.akilisha.oss.roya.plugins.ai.llm.ChatModelFactory;
import com.akilisha.oss.roya.plugins.ai.llm.EmbeddingModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

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
        // Create ChatModel from provider config (use ChatModelFactory for uniform selection)
        ChatModel chatModel = ChatModelFactory.createChatModel(config);

        // Create EmbeddingModel from provider config
        EmbeddingModel embeddingModel = createEmbeddingModel(config);

        // Create StreamingChatModel using factory
        StreamingChatModel streamingChatModel = ChatModelFactory.createStreamingChatModel(config);

        // Create LangGraph adapter wrapper
        // LangGraphAdapter uses LangChain4j models for LLM/Embeddings
        // and LangGraph4j for agent orchestration
        return new LangGraphAdapter(chatModel, streamingChatModel, embeddingModel);
    }

    private EmbeddingModel createEmbeddingModel(AILibraryConfig config) {
        // Use EmbeddingModelFactory for uniform provider selection with intelligent fallback
        // Priority: OpenAI (if API key available) -> AllMiniLmL6V2EmbeddingModel (default, local)
        return EmbeddingModelFactory.createEmbeddingModel(config);
    }
}



