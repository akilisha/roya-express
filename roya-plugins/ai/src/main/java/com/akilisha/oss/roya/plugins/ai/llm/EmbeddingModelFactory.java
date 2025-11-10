package com.akilisha.oss.roya.plugins.ai.llm;

import com.akilisha.oss.roya.plugins.ai.library.AILibraryConfig;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;

import java.time.Duration;
import java.util.Optional;

/**
 * Factory for creating EmbeddingModel instances.
 * 
 * <p>Provides intelligent provider selection with fallback logic:
 * <ol>
 *   <li>AllMiniLmL6V2EmbeddingModel (default, local, no API key required)</li>
 *   <li>OpenAI Embeddings (if API key available)</li>
 * </ol>
 * 
 * <p>Fallback behavior:
 * <ul>
 *   <li>If AllMiniLmL6V2EmbeddingModel is available, use it by default (local, fast, no cost)</li>
 *   <li>If OpenAI API key is configured, use OpenAI embeddings (higher quality, requires API key)</li>
 *   <li>If explicit provider is configured via API key, use that provider</li>
 * </ul>
 * 
 * <p>Note: Embedding models are cross-cutting across all AI library implementations
 * (LangChain, LangGraph, Google ADK), so this factory ensures consistent initialization.
 */
public class EmbeddingModelFactory {

    /**
     * Create an EmbeddingModel with intelligent provider selection.
     * 
     * Priority order:
     * 1. Explicit provider configuration (if API key present)
     * 2. AllMiniLmL6V2EmbeddingModel (default, local, no API key)
     * 3. OpenAI Embeddings (if API key available)
     * 
     * @param config AI library configuration
     * @return EmbeddingModel instance
     * @throws IllegalArgumentException if no provider is available
     */
    public static EmbeddingModel createEmbeddingModel(AILibraryConfig config) {
        // 1. Check for explicit provider configuration (highest priority)
        var explicitProvider = getExplicitEmbeddingProvider(config);
        if (explicitProvider.isPresent()) {
            return createEmbeddingModelForProvider(explicitProvider.get(), config);
        }

        // 2. Try OpenAI embeddings (if API key available)
        var openaiConfig = config.provider("openai");
        if (openaiConfig.isPresent() && openaiConfig.get().apiKey() != null && !openaiConfig.get().apiKey().isEmpty()) {
            return createOpenAiEmbeddingModel(openaiConfig.get());
        }

        // 3. Try Hugging Face embeddings (if API key available)
        var huggingFaceConfig = config.provider("huggingface");
        if (huggingFaceConfig.isPresent() && huggingFaceConfig.get().apiKey() != null && !huggingFaceConfig.get().apiKey().isEmpty()) {
            return createHuggingFaceEmbeddingModel(huggingFaceConfig.get());
        }

        // 3. Try AllMiniLmL6V2EmbeddingModel (default, local, no API key required)
        // This is a good default as it runs locally and doesn't require external services
        // The dependency is in build.gradle, so it's always available
        return createAllMiniLmL6V2EmbeddingModel();
    }

    /**
     * Get explicit embedding provider if configured via environment variable or config option.
     * Checks for AI_EMBEDDING_PROVIDER environment variable or config option.
     */
    private static Optional<String> getExplicitEmbeddingProvider(AILibraryConfig config) {
        // Check environment variable
        String envProvider = System.getenv("AI_EMBEDDING_PROVIDER");
        if (envProvider != null && !envProvider.isEmpty()) {
            return Optional.of(envProvider.toLowerCase());
        }

        // Check config option
        Object configProvider = config.libraryOptions().get("embeddingProvider");
        if (configProvider != null) {
            return Optional.of(configProvider.toString().toLowerCase());
        }

        return Optional.empty();
    }


    /**
     * Extract model name from provider config options, with fallback to default.
     */
    private static String getModelName(AILibraryConfig.ProviderConfig config, String defaultModel) {
        if (config == null) {
            return defaultModel;
        }
        var options = config.options();
        if (options.containsKey("model")) {
            return options.get("model").toString();
        }
        if (options.containsKey("modelName")) {
            return options.get("modelName").toString();
        }
        if (options.containsKey("embeddingModel")) {
            return options.get("embeddingModel").toString();
        }
        if (options.containsKey("modelId")) {
            return options.get("modelId").toString();
        }
        return defaultModel;
    }

    // ========== Provider-Specific Creation Methods ==========

    /**
     * Create AllMiniLmL6V2EmbeddingModel (local, no API key required).
     * This is a good default embedding model that runs locally.
     */
    private static EmbeddingModel createAllMiniLmL6V2EmbeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    /**
     * Create OpenAI EmbeddingModel.
     */
    private static EmbeddingModel createOpenAiEmbeddingModel(AILibraryConfig.ProviderConfig config) {
        String modelName = getModelName(config, "text-embedding-3-small");
        return OpenAiEmbeddingModel.builder()
                .apiKey(config.apiKey())
                .modelName(modelName)
                .build();
    }

    /**
     * Create Hugging Face EmbeddingModel.
     */
    private static EmbeddingModel createHuggingFaceEmbeddingModel(AILibraryConfig.ProviderConfig config) {
        String modelId = getModelName(config, "sentence-transformers/all-MiniLM-L6-v2");
        Integer timeoutSeconds = getOptionAsInteger(config, "timeoutSeconds");

        var builder = HuggingFaceEmbeddingModel.builder()
                .accessToken(config.apiKey())
                .modelId(modelId);

        if (timeoutSeconds != null) {
            builder = builder.timeout(Duration.ofSeconds(timeoutSeconds));
        }

        return builder.build();
    }

    /**
     * Create EmbeddingModel for a specific provider.
     */
    private static EmbeddingModel createEmbeddingModelForProvider(String provider, AILibraryConfig config) {
        return switch (provider.toLowerCase()) {
            case "openai" -> {
                var openaiConfig = config.provider("openai");
                if (openaiConfig.isEmpty() || openaiConfig.get().apiKey() == null || openaiConfig.get().apiKey().isEmpty()) {
                    throw new IllegalArgumentException("OpenAI embedding provider requires API key");
                }
                yield createOpenAiEmbeddingModel(openaiConfig.get());
            }
            case "huggingface" -> {
                var huggingFaceConfig = config.provider("huggingface");
                if (huggingFaceConfig.isEmpty() || huggingFaceConfig.get().apiKey() == null || huggingFaceConfig.get().apiKey().isEmpty()) {
                    throw new IllegalArgumentException("Hugging Face embedding provider requires API key");
                }
                yield createHuggingFaceEmbeddingModel(huggingFaceConfig.get());
            }
            case "all-minilm-l6-v2", "allminilml6v2", "local" -> createAllMiniLmL6V2EmbeddingModel();
            default -> throw new IllegalArgumentException("Unknown embedding provider: " + provider);
        };
    }

    private static Integer getOptionAsInteger(AILibraryConfig.ProviderConfig config, String key) {
        if (config == null) {
            return null;
        }
        Object value = config.options().get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String str && !str.isBlank()) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}

