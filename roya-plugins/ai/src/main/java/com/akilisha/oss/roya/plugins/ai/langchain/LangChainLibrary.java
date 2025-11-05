package com.akilisha.oss.roya.plugins.ai.langchain;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.library.AILibrary;
import com.akilisha.oss.roya.plugins.ai.library.AILibraryConfig;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.vertexai.VertexAiChatModel;

/**
 * LangChain library adapter.
 * <p>
 * Bridges LangChain4j to Roya's unified AI interface.
 * <p>
 * Status: Placeholder - waiting for LangChain4j API documentation/examples
 * to implement actual integration.
 * <p>
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

        // Create StreamingChatModel from provider config
        StreamingChatModel streamingChatModel = createStreamingChatModel(config);

        // Create adapter wrapper around these models
        return new LangChainAdapter(chatModel, streamingChatModel, embeddingModel);
    }

    private ChatModel createChatModel(AILibraryConfig config) {
        // Try OpenAI first
        var openaiConfig = config.provider("openai");
        if (openaiConfig.isPresent()) {
            var providerConfig = openaiConfig.get();
            String modelName = getModelName(providerConfig, "gpt-3.5-turbo");
            return OpenAiChatModel.builder()
                    .apiKey(providerConfig.apiKey())
                    .modelName(modelName)
                    .build();
        }

        // Try Anthropic
        var anthropicConfig = config.provider("anthropic");
        if (anthropicConfig.isPresent()) {
            var providerConfig = anthropicConfig.get();
            String modelName = getModelName(providerConfig, "claude-3-haiku-20240307");
            return AnthropicChatModel.builder()
                    .apiKey(providerConfig.apiKey())
                    .modelName(modelName)
                    .build();
        }

        // Try Gemini (Vertex AI)
        var geminiConfig = config.provider("gemini");
        if (geminiConfig.isPresent()) {
            return createGeminiChatModel(geminiConfig.get());
        }

        throw new IllegalArgumentException(
                "No provider configured. Configure either 'openai', 'anthropic', or 'gemini' in providers."
        );
    }

    /**
     * Extract model name from provider config options, with fallback to default.
     */
    private String getModelName(AILibraryConfig.ProviderConfig config, String defaultModel) {
        var options = config.options();
        // Check both "model" and "modelName" keys for flexibility
        if (options.containsKey("model")) {
            return options.get("model").toString();
        }
        if (options.containsKey("modelName")) {
            return options.get("modelName").toString();
        }
        return defaultModel;
    }

    /**
     * Create Vertex AI Gemini ChatModel.
     * 
     * <p>Note: In langchain4j-vertex-ai 1.8.0-beta15, VertexAiChatModel only implements
     * ChatModel, NOT StreamingChatModel. Streaming is not currently supported for Gemini.
     */
    private ChatModel createGeminiChatModel(AILibraryConfig.ProviderConfig config) {
        var options = config.options();

        String project = (String) options.get("project");
        String location = (String) options.get("location");

        if (project == null || location == null) {
            throw new IllegalArgumentException(
                    "Gemini provider requires both 'project' and 'location' options. " +
                            "Set AI_GEMINI_PROJECT and AI_GEMINI_LOCATION environment variables."
            );
        }

        String modelName = getModelName(config, "gemini-1.5-pro");

        return VertexAiChatModel.builder()
                .project(project)
                .location(location)
                .modelName(modelName)
                .build();
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

    /**
     * Create StreamingChatModel from provider config.
     * Mirrors createChatModel but uses streaming-specific implementations.
     */
    private StreamingChatModel createStreamingChatModel(AILibraryConfig config) {
        // Try OpenAI first
        var openaiConfig = config.provider("openai");
        if (openaiConfig.isPresent()) {
            var providerConfig = openaiConfig.get();
            String modelName = getModelName(providerConfig, "gpt-3.5-turbo");
            return OpenAiStreamingChatModel.builder()
                    .apiKey(providerConfig.apiKey())
                    .modelName(modelName)
                    .build();
        }

        // Try Anthropic
        var anthropicConfig = config.provider("anthropic");
        if (anthropicConfig.isPresent()) {
            var providerConfig = anthropicConfig.get();
            String modelName = getModelName(providerConfig, "claude-3-haiku-20240307");
            return AnthropicStreamingChatModel.builder()
                    .apiKey(providerConfig.apiKey())
                    .modelName(modelName)
                    .build();
        }

        // Try Gemini (Vertex AI)
        // Note: VertexAiChatModel in 1.8.0-beta15 does NOT implement StreamingChatModel.
        // Streaming is not currently supported for Gemini in this version.
        var geminiConfig = config.provider("gemini");
        if (geminiConfig.isPresent()) {
            // Gemini streaming not supported in langchain4j-vertex-ai 1.8.0-beta15
            return null;
        }

        // Return null if no provider configured (streaming will be disabled)
        return null;
    }

}



