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
        // Respect explicit provider overrides before falling back
        var explicitProvider = resolveExplicitProvider(config);
        if ("gemini".equals(explicitProvider)) {
            var geminiConfig = config.provider("gemini")
                    .orElseThrow(() -> new IllegalArgumentException("Gemini provider selected but not configured"));
            return createGeminiChatModel(geminiConfig);
        }

        try {
            // Use factory for standard providers (ollama, mistral, openai, anthropic)
            return ChatModelFactory.createChatModel(config);
        } catch (IllegalArgumentException ex) {
            // If no standard provider available but Gemini is configured, fall back to it
            var geminiConfig = config.provider("gemini");
            if (geminiConfig.isPresent()) {
                return createGeminiChatModel(geminiConfig.get());
            }
            throw ex;
        }
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
        var explicitProvider = resolveExplicitProvider(config);
        if ("gemini".equals(explicitProvider)) {
            // Gemini streaming not supported in current langchain4j version
            return null;
        }

        var streamingChatModel = ChatModelFactory.createStreamingChatModel(config);
        if (streamingChatModel != null) {
            return streamingChatModel;
        }

        // No streaming-capable provider available. If explicit provider was not set but Gemini
        // is configured, fall back to null (Gemini has no streaming support).
        return null;
    }

    private String resolveExplicitProvider(AILibraryConfig config) {
        Object provider = config.libraryOptions().get("provider");
        if (provider != null) {
            return provider.toString().toLowerCase();
        }

        String envProvider = System.getenv("AI_PROVIDER");
        if (envProvider != null && !envProvider.isBlank()) {
            return envProvider.toLowerCase();
        }

        return null;
    }
}



