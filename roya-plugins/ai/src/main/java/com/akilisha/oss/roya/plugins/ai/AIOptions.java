package com.akilisha.oss.roya.plugins.ai;

/**
 * AI request options.
 */
public record AIOptions(
    String model,           // Model name (e.g., "gpt-4", "gpt-3.5-turbo")
    Double temperature,     // 0.0-2.0, controls randomness
    Integer maxTokens,       // Maximum tokens in response
    Double topP,            // Nucleus sampling parameter
    Integer n                // Number of completions to generate
) {
    public static AIOptions defaults() {
        return new AIOptions(
            "gpt-3.5-turbo",  // Default model
            0.7,               // Default temperature
            1000,              // Default max tokens
            1.0,               // Default topP
            1                  // Default 1 completion
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String model = "gpt-3.5-turbo";
        private Double temperature = 0.7;
        private Integer maxTokens = 1000;
        private Double topP = 1.0;
        private Integer n = 1;

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        public Builder n(Integer n) {
            this.n = n;
            return this;
        }

        public AIOptions build() {
            return new AIOptions(model, temperature, maxTokens, topP, n);
        }
    }
}

