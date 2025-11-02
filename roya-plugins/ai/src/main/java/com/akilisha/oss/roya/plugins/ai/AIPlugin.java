package com.akilisha.oss.roya.plugins.ai;

import com.akilisha.oss.roya.api.plugin.RoyaPlugin;
import com.akilisha.oss.roya.api.plugin.Services;
import com.akilisha.oss.roya.plugins.ai.library.AILibraryConfig;
import com.akilisha.oss.roya.plugins.ai.library.AILibraryFactory;

/**
 * AI plugin - first-class AI/LLM integration.
 *
 * Library-first design: Users choose abstraction layer (LangChain, Google ADK, LangGraph)
 * based on use case, not provider.
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
        return "Library-first AI integration supporting LangChain, Google ADK, and LangGraph";
    }

    @Override
    public void register(Services services) {
        services.singleton(AI.class, () -> {
            // Configuration from system properties or environment variables
            String library = System.getProperty("ai.library",
                System.getenv().getOrDefault("AI_LIBRARY", "langchain"));
            boolean enableCache = Boolean.parseBoolean(
                System.getProperty("ai.cache.enabled", "true"));

            // Build library configuration
            var configBuilder = AILibraryConfig.builder().caching(enableCache);

            // Add OpenAI provider if configured (backward compatibility + default)
            String openaiKey = getConfigValue("ai.openai.apiKey", "AI_OPENAI_API_KEY", "OPENAI_API_KEY");
            if (openaiKey != null && !openaiKey.isBlank()) {
                configBuilder.provider("openai", AILibraryConfig.ProviderConfig.simple(openaiKey));
            }

            // Add Anthropic provider if configured
            String anthropicKey = getConfigValue("ai.anthropic.apiKey", "AI_ANTHROPIC_API_KEY", "ANTHROPIC_API_KEY");
            if (anthropicKey != null && !anthropicKey.isBlank()) {
                configBuilder.provider("anthropic", AILibraryConfig.ProviderConfig.simple(anthropicKey));
            }

            // Create library adapter
            var libraryAdapter = AILibraryFactory.create(library);

            // Try to get Cache plugin (optional - AI works without it)
            com.akilisha.oss.roya.plugins.cache.Cache cache = null;
            if (enableCache && services.has(com.akilisha.oss.roya.plugins.cache.Cache.class)) {
                cache = services.get(com.akilisha.oss.roya.plugins.cache.Cache.class);
            } else if (enableCache) {
                System.out.println("Warning: Cache plugin not available, AI caching disabled");
            }

            // Create AI service from library adapter
            // For now, fall back to legacy implementation if library not ready
            try {
                return libraryAdapter.create(configBuilder.build());
            } catch (UnsupportedOperationException e) {
                // Library not yet implemented, fall back to legacy provider-based approach
                System.out.println("⚠️  Library '" + library + "' not yet implemented, using legacy provider-based approach");
                return createLegacyAI(openaiKey, cache);
            }
        });
    }

    private AI createLegacyAI(String openaiKey, com.akilisha.oss.roya.plugins.cache.Cache cache) {
        if (openaiKey == null || openaiKey.isBlank()) {
            throw new IllegalArgumentException(
                "ai.openai.apiKey is required. Set via -Dai.openai.apiKey=key or OPENAI_API_KEY env var"
            );
        }
        return new AIServiceImpl(new com.akilisha.oss.roya.plugins.ai.providers.OpenAIClient(openaiKey), cache);
    }

    private String getConfigValue(String systemProp, String... envVars) {
        String value = System.getProperty(systemProp);
        if (value != null && !value.isBlank()) {
            return value;
        }
        for (String envVar : envVars) {
            value = System.getenv(envVar);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    @Override
    public void start() throws Exception {
        String library = System.getProperty("ai.library",
            System.getenv().getOrDefault("AI_LIBRARY", "langchain"));
        String openaiKey = getConfigValue("ai.openai.apiKey", "AI_OPENAI_API_KEY", "OPENAI_API_KEY");
        boolean caching = Boolean.parseBoolean(System.getProperty("ai.cache.enabled", "true"));

        System.out.println("✓ AIPlugin: AI service initialized");
        System.out.println("  - Library: " + library);
        System.out.println("  - Available: " + AILibraryFactory.availableLibraries());
        System.out.println("  - Caching: " + (caching ? "enabled" : "disabled"));
        System.out.println("  - Providers: " + (openaiKey != null ? "openai✓" : "none"));
        System.out.println("  - Usage: AI ai = req.get(AI.class);");
    }

    @Override
    public void stop() throws Exception {
        // No cleanup needed
    }
}

