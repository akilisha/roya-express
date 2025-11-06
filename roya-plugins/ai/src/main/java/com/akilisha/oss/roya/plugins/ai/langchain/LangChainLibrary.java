package com.akilisha.oss.roya.plugins.ai.langchain;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.library.AILibrary;
import com.akilisha.oss.roya.plugins.ai.library.AILibraryConfig;
import com.akilisha.oss.roya.plugins.ai.llm.ChatModelFactory;
import com.akilisha.oss.roya.plugins.ai.llm.EmbeddingModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
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
        // Use ChatModelFactory for uniform provider selection with intelligent fallback
        // Priority: Ollama (default) -> Mistral -> OpenAI -> Anthropic
        // Gemini (Vertex AI) is handled separately as it requires special configuration
        
        // Try Gemini (Vertex AI) first if configured (requires special handling)
        var geminiConfig = config.provider("gemini");
        if (geminiConfig.isPresent()) {
            return createGeminiChatModel(geminiConfig.get());
        }

        // Use factory for all other providers (Ollama, Mistral, OpenAI, Anthropic)
        return ChatModelFactory.createChatModel(config);
    }

    /**
     * Extract model name from provider config options, with fallback to default.
     * Note: This is only used for Gemini (Vertex AI) which requires special handling.
     * All other providers use ChatModelFactory.getModelName().
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
        // Use EmbeddingModelFactory for uniform provider selection with intelligent fallback
        // Priority: OpenAI (if API key available) -> AllMiniLmL6V2EmbeddingModel (default, local)
        return EmbeddingModelFactory.createEmbeddingModel(config);
    }

    /**
     * Create StreamingChatModel from provider config.
     * Uses ChatModelFactory for uniform provider selection.
     */
    private StreamingChatModel createStreamingChatModel(AILibraryConfig config) {
        // Try Gemini (Vertex AI) first if configured
        // Note: VertexAiChatModel in 1.8.0-beta15 does NOT implement StreamingChatModel.
        // Streaming is not currently supported for Gemini in this version.
        var geminiConfig = config.provider("gemini");
        if (geminiConfig.isPresent()) {
            // Gemini streaming not supported in langchain4j-vertex-ai 1.8.0-beta15
            return null;
        }

        // Use factory for all other providers (Ollama, Mistral, OpenAI, Anthropic)
        return ChatModelFactory.createStreamingChatModel(config);
    }

}



