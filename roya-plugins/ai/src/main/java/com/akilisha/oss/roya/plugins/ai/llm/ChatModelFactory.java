package com.akilisha.oss.roya.plugins.ai.llm;

import com.akilisha.oss.roya.plugins.ai.library.AILibraryConfig;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.time.Duration;
import java.util.Optional;

/**
 * Factory for creating ChatModel and StreamingChatModel instances.
 * 
 * <p>Provides intelligent provider selection with fallback logic:
 * <ol>
 *   <li>Ollama (default, no API key required if running locally)</li>
 *   <li>Mistral AI (if API key available)</li>
 *   <li>OpenAI (if API key available)</li>
 *   <li>Anthropic (if API key available)</li>
 * </ol>
 * 
 * <p>Fallback behavior:
 * <ul>
 *   <li>If Ollama is configured, use it by default</li>
 *   <li>If Ollama is missing a feature (e.g., vision), fallback to providers with API keys</li>
 *   <li>If explicit provider is configured via API key, use that provider</li>
 * </ul>
 */
public class ChatModelFactory {

    /**
     * Create a ChatModel with intelligent provider selection.
     * 
     * Priority order:
     * 1. Explicit provider configuration (if API key present)
     * 2. Ollama (default, if available)
     * 3. Mistral AI (if API key available)
     * 4. OpenAI (if API key available)
     * 5. Anthropic (if API key available)
     * 6. Hugging Face Inference API (if API key available)
     * 
     * @param config AI library configuration
     * @return ChatModel instance
     * @throws IllegalArgumentException if no provider is available
     */
    public static ChatModel createChatModel(AILibraryConfig config) {
        // 1. Check for explicit provider override (highest priority - user explicitly requested)
        var explicitProvider = getExplicitProvider(config);
        if (explicitProvider.isPresent()) {
            return createChatModelForProvider(explicitProvider.get(), config);
        }

        // 2. Use the provider registered by AIPlugin (precedence already applied there)
        // AIPlugin registers only one provider based on precedence, so we just use it
        var openaiConfig = config.provider("openai");
        if (openaiConfig.isPresent() && openaiConfig.get().apiKey() != null && !openaiConfig.get().apiKey().isEmpty()) {
            return createOpenAiChatModel(openaiConfig.get());
        }

        var anthropicConfig = config.provider("anthropic");
        if (anthropicConfig.isPresent() && anthropicConfig.get().apiKey() != null && !anthropicConfig.get().apiKey().isEmpty()) {
            return createAnthropicChatModel(anthropicConfig.get());
        }

        var huggingFaceConfig = config.provider("huggingface");
        if (huggingFaceConfig.isPresent() && huggingFaceConfig.get().apiKey() != null && !huggingFaceConfig.get().apiKey().isEmpty()) {
            return createHuggingFaceChatModel(huggingFaceConfig.get());
        }

        var mistralConfig = config.provider("mistral");
        if (mistralConfig.isPresent() && mistralConfig.get().apiKey() != null && !mistralConfig.get().apiKey().isEmpty()) {
            return createMistralChatModel(mistralConfig.get());
        }

        var ollamaConfig = config.provider("ollama");
        if (ollamaConfig.isPresent()) {
            return createOllamaChatModel(ollamaConfig.get());
        }
        
        // 3. Fallback: Check if Ollama is available locally (only if no providers registered)
        if (isOllamaAvailable()) {
            return createOllamaChatModel(null);
        }

        throw new IllegalArgumentException(
            "No ChatModel provider available. " +
            "Configure one of: 'ollama' (default, no API key), 'mistral', 'openai', 'anthropic', or 'huggingface' in providers."
        );
    }

    /**
     * Create a StreamingChatModel with intelligent provider selection.
     * 
     * Same priority order as createChatModel.
     * 
     * @param config AI library configuration
     * @return StreamingChatModel instance, or null if streaming not supported
     */
    public static StreamingChatModel createStreamingChatModel(AILibraryConfig config) {
        // 1. Check for explicit provider override (highest priority - user explicitly requested)
        var explicitProvider = getExplicitProvider(config);
        if (explicitProvider.isPresent()) {
            return createStreamingChatModelForProvider(explicitProvider.get(), config);
        }

        // 2. Use the provider registered by AIPlugin (precedence already applied there)
        // AIPlugin registers only one provider based on precedence, so we just use it
        var openaiConfig = config.provider("openai");
        if (openaiConfig.isPresent() && openaiConfig.get().apiKey() != null && !openaiConfig.get().apiKey().isEmpty()) {
            return createOpenAiStreamingChatModel(openaiConfig.get());
        }

        var anthropicConfig = config.provider("anthropic");
        if (anthropicConfig.isPresent() && anthropicConfig.get().apiKey() != null && !anthropicConfig.get().apiKey().isEmpty()) {
            return createAnthropicStreamingChatModel(anthropicConfig.get());
        }

        var mistralConfig = config.provider("mistral");
        if (mistralConfig.isPresent() && mistralConfig.get().apiKey() != null && !mistralConfig.get().apiKey().isEmpty()) {
            return createMistralStreamingChatModel(mistralConfig.get());
        }

        var ollamaConfig = config.provider("ollama");
        if (ollamaConfig.isPresent()) {
            return createOllamaStreamingChatModel(ollamaConfig.get());
        }
        
        // 3. Fallback: Check if Ollama is available locally (only if no providers registered)
        if (isOllamaAvailable()) {
            return createOllamaStreamingChatModel(null);
        }

        // Return null if no streaming provider available (streaming will be disabled)
        return null;
    }

    /**
     * Get explicit provider if configured via environment variable or config option.
     * Checks for AI_PROVIDER environment variable or config option.
     */
    private static Optional<String> getExplicitProvider(AILibraryConfig config) {
        // Check environment variable
        String envProvider = System.getenv("AI_PROVIDER");
        if (envProvider != null && !envProvider.isEmpty()) {
            return Optional.of(envProvider.toLowerCase());
        }

        // Check config option
        Object configProvider = config.libraryOptions().get("provider");
        if (configProvider != null) {
            return Optional.of(configProvider.toString().toLowerCase());
        }

        return Optional.empty();
    }

    /**
     * Check if Ollama is available (running locally).
     * Default endpoint: http://localhost:11434
     */
    private static boolean isOllamaAvailable() {
        // Check if OLLAMA_BASE_URL is set (indicates Ollama is configured)
        String ollamaUrl = System.getenv("OLLAMA_BASE_URL");
        if (ollamaUrl != null && !ollamaUrl.isEmpty()) {
            return true;
        }

        // Check if OLLAMA_HOST is set
        String ollamaHost = System.getenv("OLLAMA_HOST");
        if (ollamaHost != null && !ollamaHost.isEmpty()) {
            return true;
        }

        // Default: assume Ollama is available if no other providers are explicitly configured
        // This allows Ollama to be the default when running locally
        return true; // Will attempt to connect, fail gracefully if not available
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
        if (options.containsKey("modelId")) {
            return options.get("modelId").toString();
        }
        return defaultModel;
    }
    private static ChatModel createHuggingFaceChatModel(AILibraryConfig.ProviderConfig config) {
        String modelId = getModelName(config, "tiiuae/falcon-7b-instruct");
        Double temperature = getOptionAsDouble(config, "temperature");
        Integer maxNewTokens = getOptionAsInteger(config, "maxNewTokens");
        Boolean waitForModel = getOptionAsBoolean(config, "waitForModel");
        Integer timeout = getOptionAsInteger(config, "timeoutSeconds");

        HuggingFaceChatModel.Builder builder = HuggingFaceChatModel.builder()
                .accessToken(config.apiKey())
                .modelId(modelId);

        if (temperature != null) {
            builder = builder.temperature(temperature);
        }
        if (maxNewTokens != null) {
            builder = builder.maxNewTokens(maxNewTokens);
        }
        if (waitForModel != null) {
            builder = builder.waitForModel(waitForModel);
        }
        if (timeout != null) {
            builder = builder.timeout(Duration.ofSeconds(timeout));
        }
        return builder.build();
    }


    // ========== Provider-Specific Creation Methods ==========

    private static ChatModel createOllamaChatModel(AILibraryConfig.ProviderConfig config) {
        String baseUrl = getOllamaBaseUrl(config);
        String modelName = config != null ? getModelName(config, "llama3") : "llama3";
        
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .build();
    }

    private static StreamingChatModel createOllamaStreamingChatModel(AILibraryConfig.ProviderConfig config) {
        String baseUrl = getOllamaBaseUrl(config);
        String modelName = config != null ? getModelName(config, "llama3") : "llama3";
        
        return OllamaStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .build();
    }

    private static String getOllamaBaseUrl(AILibraryConfig.ProviderConfig config) {
        if (config != null && config.endpoint().isPresent()) {
            return config.endpoint().get();
        }
        String envUrl = System.getenv("OLLAMA_BASE_URL");
        if (envUrl != null && !envUrl.isEmpty()) {
            return envUrl;
        }
        String envHost = System.getenv("OLLAMA_HOST");
        if (envHost != null && !envHost.isEmpty()) {
            return "http://" + envHost + ":11434";
        }
        return "http://localhost:11434";
    }

    private static ChatModel createMistralChatModel(AILibraryConfig.ProviderConfig config) {
        String modelName = getModelName(config, "mistral-medium");
        
        return MistralAiChatModel.builder()
                .apiKey(config.apiKey())
                .modelName(modelName)
                .build();
    }

    private static StreamingChatModel createMistralStreamingChatModel(AILibraryConfig.ProviderConfig config) {
        String modelName = getModelName(config, "mistral-medium");
        
        return MistralAiStreamingChatModel.builder()
                .apiKey(config.apiKey())
                .modelName(modelName)
                .build();
    }

    private static ChatModel createOpenAiChatModel(AILibraryConfig.ProviderConfig config) {
        String modelName = getModelName(config, "gpt-3.5-turbo");
        return OpenAiChatModel.builder()
                .apiKey(config.apiKey())
                .modelName(modelName)
                .build();
    }

    private static StreamingChatModel createOpenAiStreamingChatModel(AILibraryConfig.ProviderConfig config) {
        String modelName = getModelName(config, "gpt-3.5-turbo");
        return OpenAiStreamingChatModel.builder()
                .apiKey(config.apiKey())
                .modelName(modelName)
                .build();
    }

    private static ChatModel createAnthropicChatModel(AILibraryConfig.ProviderConfig config) {
        String modelName = getModelName(config, "claude-3-haiku-20240307");
        return AnthropicChatModel.builder()
                .apiKey(config.apiKey())
                .modelName(modelName)
                .build();
    }

    private static StreamingChatModel createAnthropicStreamingChatModel(AILibraryConfig.ProviderConfig config) {
        String modelName = getModelName(config, "claude-3-haiku-20240307");
        return AnthropicStreamingChatModel.builder()
                .apiKey(config.apiKey())
                .modelName(modelName)
                .build();
    }

    private static ChatModel createChatModelForProvider(String provider, AILibraryConfig config) {
        return switch (provider.toLowerCase()) {
            case "ollama" -> createOllamaChatModel(config.provider("ollama").orElse(null));
            case "mistral" -> {
                var mistralConfig = config.provider("mistral");
                if (mistralConfig.isEmpty() || mistralConfig.get().apiKey() == null || mistralConfig.get().apiKey().isEmpty()) {
                    throw new IllegalArgumentException("Mistral AI provider requires API key");
                }
                yield createMistralChatModel(mistralConfig.get());
            }
            case "openai" -> {
                var openaiConfig = config.provider("openai");
                if (openaiConfig.isEmpty() || openaiConfig.get().apiKey() == null || openaiConfig.get().apiKey().isEmpty()) {
                    throw new IllegalArgumentException("OpenAI provider requires API key");
                }
                yield createOpenAiChatModel(openaiConfig.get());
            }
            case "anthropic" -> {
                var anthropicConfig = config.provider("anthropic");
                if (anthropicConfig.isEmpty() || anthropicConfig.get().apiKey() == null || anthropicConfig.get().apiKey().isEmpty()) {
                    throw new IllegalArgumentException("Anthropic provider requires API key");
                }
                yield createAnthropicChatModel(anthropicConfig.get());
            }
            case "huggingface" -> {
                var huggingFaceConfig = config.provider("huggingface");
                if (huggingFaceConfig.isEmpty() || huggingFaceConfig.get().apiKey() == null || huggingFaceConfig.get().apiKey().isEmpty()) {
                    throw new IllegalArgumentException("Hugging Face provider requires API key");
                }
                yield createHuggingFaceChatModel(huggingFaceConfig.get());
            }
            default -> throw new IllegalArgumentException("Unknown provider: " + provider);
        };
    }

    private static StreamingChatModel createStreamingChatModelForProvider(String provider, AILibraryConfig config) {
        return switch (provider.toLowerCase()) {
            case "ollama" -> createOllamaStreamingChatModel(config.provider("ollama").orElse(null));
            case "mistral" -> {
                var mistralConfig = config.provider("mistral");
                if (mistralConfig.isEmpty() || mistralConfig.get().apiKey() == null || mistralConfig.get().apiKey().isEmpty()) {
                    yield null;
                }
                yield createMistralStreamingChatModel(mistralConfig.get());
            }
            case "openai" -> {
                var openaiConfig = config.provider("openai");
                if (openaiConfig.isEmpty() || openaiConfig.get().apiKey() == null || openaiConfig.get().apiKey().isEmpty()) {
                    yield null;
                }
                yield createOpenAiStreamingChatModel(openaiConfig.get());
            }
            case "anthropic" -> {
                var anthropicConfig = config.provider("anthropic");
                if (anthropicConfig.isEmpty() || anthropicConfig.get().apiKey() == null || anthropicConfig.get().apiKey().isEmpty()) {
                    yield null;
                }
                yield createAnthropicStreamingChatModel(anthropicConfig.get());
            }
            case "huggingface" -> {
                yield null; // Streaming currently not supported for Hugging Face models
            }
            default -> null;
        };
    }

    private static Double getOptionAsDouble(AILibraryConfig.ProviderConfig config, String key) {
        if (config == null) {
            return null;
        }
        Object value = config.options().get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String str && !str.isBlank()) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
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

    private static Boolean getOptionAsBoolean(AILibraryConfig.ProviderConfig config, String key) {
        if (config == null) {
            return null;
        }
        Object value = config.options().get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String str && !str.isBlank()) {
            return Boolean.parseBoolean(str);
        }
        return null;
    }
}

