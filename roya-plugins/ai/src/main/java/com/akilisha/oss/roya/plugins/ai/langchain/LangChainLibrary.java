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

        // Try Gemini (Vertex AI) - using reflection to avoid dependency issues
        var geminiConfig = config.provider("gemini");
        if (geminiConfig.isPresent()) {
            return createGeminiChatModel(geminiConfig.get());
        }

        throw new IllegalArgumentException(
            "No provider configured. Configure either 'openai', 'anthropic', or 'gemini' in providers."
        );
    }

    /**
     * Create Vertex AI Gemini ChatModel using reflection.
     * This avoids direct dependency issues if the module isn't loaded.
     */
    private ChatModel createGeminiChatModel(AILibraryConfig.ProviderConfig config) {
        try {
            Class<?> vertexAiGeminiClass = Class.forName("dev.langchain4j.model.vertexai.VertexAiGeminiChatModel");
            var options = config.options();
            
            String project = (String) options.get("project");
            String location = (String) options.get("location");
            
            if (project == null || location == null) {
                throw new IllegalArgumentException(
                    "Gemini provider requires both 'project' and 'location' options. " +
                    "Set AI_GEMINI_PROJECT and AI_GEMINI_LOCATION environment variables."
                );
            }
            
            // Get builder method
            java.lang.reflect.Method builderMethod = vertexAiGeminiClass.getMethod("builder");
            Object builder = builderMethod.invoke(null);
            
            // Set API key
            java.lang.reflect.Method apiKeyMethod = builder.getClass().getMethod("apiKey", String.class);
            apiKeyMethod.invoke(builder, config.apiKey());
            
            // Set project
            java.lang.reflect.Method projectMethod = builder.getClass().getMethod("project", String.class);
            projectMethod.invoke(builder, project);
            
            // Set location
            java.lang.reflect.Method locationMethod = builder.getClass().getMethod("location", String.class);
            locationMethod.invoke(builder, location);
            
            // Set model name (default to gemini-1.5-pro for multimodal support)
            java.lang.reflect.Method modelNameMethod = builder.getClass().getMethod("modelName", String.class);
            modelNameMethod.invoke(builder, "gemini-1.5-pro");
            
            // Build
            java.lang.reflect.Method buildMethod = builder.getClass().getMethod("build");
            return (ChatModel) buildMethod.invoke(builder);
            
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                "Gemini support requires langchain4j-google-ai-gemini dependency. " +
                "Please ensure the dependency is added to your build file.", e
            );
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Failed to create Gemini ChatModel: " + e.getMessage(), e
            );
        }
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



