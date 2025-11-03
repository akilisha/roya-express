package com.akilisha.oss.roya.plugins.ai.langchain;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.library.AILibrary;
import com.akilisha.oss.roya.plugins.ai.library.AILibraryConfig;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;

/**
 * LangChain library adapter.
 *
 * Bridges LangChain4j to Roya's unified AI interface.
 *
 * Status: Placeholder - waiting for LangChain4j API documentation/examples
 * to implement actual integration.
 *
 * Dependencies are configured but API classes need to be verified.
 * Will implement once we have confirmed package paths and API patterns.
 */
public class LangChainLibrary implements AILibrary {
    @Override
    public String name() {
        return "langchain";
    }

    @Override
    public AI create(AILibraryConfig config) {
        // Create ChatModel from provider config
        ChatModel chatModel = createChatModel(config);

        // Create EmbeddingModel from provider config
        EmbeddingModel embeddingModel = createEmbeddingModel(config);

        // TODO: Create StreamingChatModel when needed
        StreamingChatModel streamingChatModel = null;

        // Create adapter wrapper around these models
        return new LangChainAdapter(chatModel, streamingChatModel, embeddingModel);
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



