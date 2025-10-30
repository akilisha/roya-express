package com.akilisha.oss.roya.plugins.ai;

import java.util.List;
import java.util.Map;

/**
 * AI request options - comprehensive LLM parameter configuration.
 * 
 * Supports both common parameters (temperature, topP, etc.) and 
 * provider-specific extensions via additionalOptions.
 */
public record AIOptions(
    // Core Parameters
    String model,                    // Model name (e.g., "gpt-4", "gpt-3.5-turbo", "claude-3-opus")
    Double temperature,              // 0.0-2.0, controls randomness (higher = more creative)
    Integer maxTokens,               // Maximum tokens in response
    
    // Sampling Parameters
    Double topP,                     // Nucleus sampling (0.0-1.0): consider tokens with cumulative probability
    Integer topK,                    // Top-k sampling: consider top K tokens by probability
    Double typicalP,                 // Typical sampling: filter tokens with atypical probability
    
    // Penalty Parameters
    Double frequencyPenalty,         // -2.0 to 2.0: penalize tokens based on frequency in prompt
    Double presencePenalty,          // -2.0 to 2.0: penalize tokens based on presence in prompt
    Double lengthPenalty,             // Length penalty: >1.0 favors longer, <1.0 favors shorter
    
    // Generation Control
    List<String> stop,                // Stop sequences: stop generation when these strings appear
    Integer seed,                     // Random seed for reproducibility (null = random)
    Integer n,                        // Number of completions to generate
    
    // Advanced Parameters
    Boolean logprobs,                 // Include log probabilities in response
    Integer topLogprobs,             // Number of top logprobs to return (if logprobs=true)
    Boolean echo,                     // Echo back the prompt in the response
    
    // Provider-Specific Extensions
    Map<String, Object> additionalOptions  // Extensible: provider-specific parameters
) {
    public static AIOptions defaults() {
        return new AIOptions(
            "gpt-3.5-turbo",          // Default model
            0.7,                       // Default temperature (balanced)
            1000,                      // Default max tokens
            1.0,                       // Default topP (use all tokens)
            null,                      // topK (null = not set)
            null,                      // typicalP (null = not set)
            0.0,                       // Default frequencyPenalty (no penalty)
            0.0,                       // Default presencePenalty (no penalty)
            1.0,                       // Default lengthPenalty (neutral)
            null,                      // stop (null = no stop sequences)
            null,                      // seed (null = random)
            1,                         // Default 1 completion
            false,                     // logprobs (false by default)
            null,                      // topLogprobs (null = not set)
            false,                     // echo (false by default)
            null                       // additionalOptions (null = none)
        );
    }
    
    /**
     * Create options optimized for structured extraction.
     * Lower temperature, deterministic settings.
     */
    public static AIOptions forExtraction() {
        return builder()
            .temperature(0.1)
            .topP(0.95)
            .frequencyPenalty(0.0)
            .presencePenalty(0.0)
            .build();
    }
    
    /**
     * Create options optimized for creative tasks.
     * Higher temperature, more varied outputs.
     */
    public static AIOptions forCreative() {
        return builder()
            .temperature(0.9)
            .topP(1.0)
            .topK(50)
            .frequencyPenalty(0.3)
            .presencePenalty(0.3)
            .build();
    }
    
    /**
     * Create options optimized for code generation.
     * Balanced temperature with stop sequences for code blocks.
     */
    public static AIOptions forCode() {
        return builder()
            .temperature(0.2)
            .topP(0.95)
            .stop(List.of("```"))  // Stop at code block end
            .maxTokens(2000)
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String model = "gpt-3.5-turbo";
        private Double temperature = 0.7;
        private Integer maxTokens = 1000;
        private Double topP = 1.0;
        private Integer topK = null;
        private Double typicalP = null;
        private Double frequencyPenalty = 0.0;
        private Double presencePenalty = 0.0;
        private Double lengthPenalty = 1.0;
        private List<String> stop = null;
        private Integer seed = null;
        private Integer n = 1;
        private Boolean logprobs = false;
        private Integer topLogprobs = null;
        private Boolean echo = false;
        private Map<String, Object> additionalOptions = null;

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

        public Builder topK(Integer topK) {
            this.topK = topK;
            return this;
        }

        public Builder typicalP(Double typicalP) {
            this.typicalP = typicalP;
            return this;
        }

        public Builder frequencyPenalty(Double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }

        public Builder presencePenalty(Double presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }

        public Builder lengthPenalty(Double lengthPenalty) {
            this.lengthPenalty = lengthPenalty;
            return this;
        }

        public Builder stop(List<String> stop) {
            this.stop = stop;
            return this;
        }

        public Builder stopSequence(String stopSequence) {
            this.stop = List.of(stopSequence);
            return this;
        }

        public Builder seed(Integer seed) {
            this.seed = seed;
            return this;
        }

        public Builder n(Integer n) {
            this.n = n;
            return this;
        }

        public Builder logprobs(Boolean logprobs) {
            this.logprobs = logprobs;
            return this;
        }

        public Builder topLogprobs(Integer topLogprobs) {
            this.topLogprobs = topLogprobs;
            return this;
        }

        public Builder echo(Boolean echo) {
            this.echo = echo;
            return this;
        }

        /**
         * Add provider-specific option (extensibility).
         * 
         * Example:
         * <pre>
         * AIOptions.builder()
         *     .temperature(0.7)
         *     .additionalOption("anthropic.max_tokens_to_sample", 500)
         *     .additionalOption("openai.response_format", Map.of("type", "json_object"))
         *     .build();
         * </pre>
         */
        public Builder additionalOption(String key, Object value) {
            if (this.additionalOptions == null) {
                this.additionalOptions = new java.util.HashMap<>();
            }
            this.additionalOptions.put(key, value);
            return this;
        }

        public Builder additionalOptions(Map<String, Object> additionalOptions) {
            this.additionalOptions = additionalOptions;
            return this;
        }

        public AIOptions build() {
            return new AIOptions(
                model, temperature, maxTokens, topP, topK, typicalP,
                frequencyPenalty, presencePenalty, lengthPenalty,
                stop, seed, n, logprobs, topLogprobs, echo, additionalOptions
            );
        }
    }
}

