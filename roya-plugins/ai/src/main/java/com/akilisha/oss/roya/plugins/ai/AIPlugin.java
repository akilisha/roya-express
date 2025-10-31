package com.akilisha.oss.roya.plugins.ai;

import com.akilisha.oss.roya.api.plugin.RoyaPlugin;
import com.akilisha.oss.roya.api.plugin.Services;
import com.akilisha.oss.roya.plugins.ai.providers.OpenAIClient;

/**
 * AI plugin - first-class AI/LLM integration.
 *
 * Makes AI as natural as Database or Email - just another service.
 */
public class AIPlugin implements RoyaPlugin {

    @Override
    public String id() {
        return "ai";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String description() {
        return "First-class AI/LLM integration with type-safe structured outputs";
    }

    @Override
    public void register(Services services) {
        services.singleton(AI.class, () -> {
            // Configuration from system properties or environment variables
            String provider = System.getProperty("ai.provider",
                System.getenv().getOrDefault("AI_PROVIDER", "openai"));
            String apiKey = System.getProperty("ai.openai.apiKey");
            if (apiKey == null || apiKey.isBlank()) {
                apiKey = System.getenv("AI_OPENAI_API_KEY");
            }
            if (apiKey == null || apiKey.isBlank()) {
                apiKey = System.getenv("OPENAI_API_KEY"); // Common env var name
            }
            boolean enableCache = Boolean.parseBoolean(
                System.getProperty("ai.cache.enabled", "true"));

            com.akilisha.oss.roya.plugins.ai.providers.LLMProvider llmProvider;

            switch (provider.toLowerCase()) {
                case "openai" -> {
                    if (apiKey == null || apiKey.isBlank()) {
                        throw new IllegalArgumentException(
                            "ai.openai.apiKey is required when using OpenAI provider"
                        );
                    }
                    llmProvider = new OpenAIClient(apiKey);
                }
                // Future providers: Anthropic, Cohere, etc.
                default -> throw new IllegalArgumentException(
                    "Unknown AI provider: " + provider + ". Supported: openai"
                );
            }

            // Try to get Cache plugin (optional - AI works without it)
            com.akilisha.oss.roya.plugins.cache.Cache cache = null;
            if (enableCache && services.has(com.akilisha.oss.roya.plugins.cache.Cache.class)) {
                cache = services.get(com.akilisha.oss.roya.plugins.cache.Cache.class);
            } else if (enableCache) {
                System.out.println("Warning: Cache plugin not available, AI caching disabled");
            }

            // Register AI service without any VectorStore reflection; RAG will use Qdrant directly
            return new AIServiceImpl(llmProvider, cache);
        });
    }

    @Override
    public void start() throws Exception {
        // Check if API key is available (for debugging)
        String apiKey = System.getProperty("ai.openai.apiKey");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("AI_OPENAI_API_KEY");
        }
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("OPENAI_API_KEY");
        }

        System.out.println("✓ AIPlugin: AI service initialized");
        System.out.println("  - Provider: " + System.getProperty("ai.provider",
            System.getenv().getOrDefault("AI_PROVIDER", "openai")));
        System.out.println("  - Caching: " + System.getProperty("ai.cache.enabled", "true"));
        System.out.println("  - API Key: " + (apiKey != null && !apiKey.isBlank()
            ? "✓ Set (" + apiKey.substring(0, Math.min(8, apiKey.length())) + "...)"
            : "✗ Not set"));
        System.out.println("  - Usage: AI ai = req.get(AI.class);");
    }

    @Override
    public void stop() throws Exception {
        // No cleanup needed
    }
}

