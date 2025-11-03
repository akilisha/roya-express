package com.akilisha.oss.roya.plugins.ai.library;

import java.util.Map;
import java.util.Optional;

/**
 * Configuration for AI library adapters.
 *
 * Provides common configuration across all libraries plus library-specific options.
 */
public record AILibraryConfig(
    /**
     * Provider configuration map.
     * Key: provider name (e.g., "openai", "anthropic")
     * Value: provider-specific config (API keys, endpoints, etc.)
     */
    Map<String, ProviderConfig> providers,

    /**
     * Enable caching of AI responses.
     */
    boolean caching,

    /**
     * Library-specific configuration options.
     * Each library adapter interprets this map according to its needs.
     */
    Map<String, Object> libraryOptions
) {
    public static AILibraryConfig defaults() {
        return new AILibraryConfig(Map.of(), true, Map.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Get a specific provider's configuration.
     */
    public Optional<ProviderConfig> provider(String name) {
        return Optional.ofNullable(providers.get(name));
    }

    public static class Builder {
        private Map<String, ProviderConfig> providers = Map.of();
        private boolean caching = true;
        private Map<String, Object> libraryOptions = Map.of();

        public Builder provider(String name, ProviderConfig config) {
            var map = new java.util.HashMap<>(providers);
            map.put(name, config);
            this.providers = Map.copyOf(map);
            return this;
        }

        public Builder caching(boolean enabled) {
            this.caching = enabled;
            return this;
        }

        public Builder libraryOption(String key, Object value) {
            var map = new java.util.HashMap<>(libraryOptions);
            map.put(key, value);
            this.libraryOptions = Map.copyOf(map);
            return this;
        }

        public AILibraryConfig build() {
            return new AILibraryConfig(providers, caching, libraryOptions);
        }
    }

    /**
     * Provider-specific configuration.
     */
    public record ProviderConfig(
        /**
         * API key or authentication token.
         */
        String apiKey,

        /**
         * Custom endpoint (for self-hosted or custom services).
         */
        Optional<String> endpoint,

        /**
         * Provider-specific options.
         */
        Map<String, Object> options
    ) {
        public static ProviderConfig simple(String apiKey) {
            return new ProviderConfig(apiKey, Optional.empty(), Map.of());
        }

        public static ProviderConfig withEndpoint(String apiKey, String endpoint) {
            return new ProviderConfig(apiKey, Optional.of(endpoint), Map.of());
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String apiKey;
            private Optional<String> endpoint = Optional.empty();
            private Map<String, Object> options = Map.of();

            public Builder apiKey(String apiKey) {
                this.apiKey = apiKey;
                return this;
            }

            public Builder endpoint(String endpoint) {
                this.endpoint = Optional.of(endpoint);
                return this;
            }

            public Builder option(String key, Object value) {
                var map = new java.util.HashMap<>(options);
                map.put(key, value);
                this.options = Map.copyOf(map);
                return this;
            }

            public ProviderConfig build() {
                return new ProviderConfig(apiKey, endpoint, options);
            }
        }
    }
}



