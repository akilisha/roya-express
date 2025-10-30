package com.akilisha.oss.roya.plugins.ai.providers;

import com.akilisha.oss.roya.plugins.ai.AIException;
import com.akilisha.oss.roya.plugins.ai.AIOptions;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * OpenAI provider - thin wrapper around OpenAI Java SDK.
 *
 * Delegates to OpenAI SDK for actual API calls.
 */
public class OpenAIClient implements LLMProvider {

    private final OpenAiService openAiService;
    private final Encoding encoding; // For token counting

    public OpenAIClient(String apiKey) {
        this.openAiService = new OpenAiService(apiKey);
        // Initialize token encoding for accurate counting
        var registry = Encodings.newDefaultEncodingRegistry();
        this.encoding = registry.getEncoding(EncodingType.CL100K_BASE); // GPT-3.5/GPT-4 encoding
    }

    @Override
    public String name() {
        return "openai";
    }

    @Override
    public com.akilisha.oss.roya.plugins.ai.LLMResponse complete(String systemPrompt, String userMessage, AIOptions options) {
        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userMessage));

            ChatCompletionRequest.ChatCompletionRequestBuilder requestBuilder = ChatCompletionRequest.builder()
                .model(options.model())
                .messages(messages)
                .temperature(options.temperature())
                .maxTokens(options.maxTokens())
                .topP(options.topP())
                .n(options.n());

            // Add optional parameters (only those supported by OpenAI SDK)
            if (options.frequencyPenalty() != null && options.frequencyPenalty() != 0.0) {
                requestBuilder.frequencyPenalty(options.frequencyPenalty());
            }
            if (options.presencePenalty() != null && options.presencePenalty() != 0.0) {
                requestBuilder.presencePenalty(options.presencePenalty());
            }
            if (options.stop() != null && !options.stop().isEmpty()) {
                requestBuilder.stop(options.stop());
            }
            // Note: topK, seed, logprobs, topLogprobs, echo are in AIOptions for 
            // extensibility but may not be supported by all SDKs. They can be passed
            // via additionalOptions for provider-specific features.

            ChatCompletionRequest request = requestBuilder.build();

            ChatCompletionResult result = openAiService.createChatCompletion(request);

            if (result.getChoices().isEmpty()) {
                throw new AIException("OpenAI returned no choices");
            }

            var choice = result.getChoices().get(0);
            String text = choice.getMessage().getContent();

            // Count tokens (prompt + completion)
            int promptTokens = countTokens(systemPrompt + "\n\n" + userMessage);
            int completionTokens = countTokens(text);
            int totalTokens = (int) result.getUsage().getTotalTokens(); // Cast long to int

            return new com.akilisha.oss.roya.plugins.ai.LLMResponse(
                text,
                options.model(),
                promptTokens,
                completionTokens,
                totalTokens,
                java.util.Optional.ofNullable(choice.getFinishReason())
            );
        } catch (Exception e) {
            throw new AIException("OpenAI API error: " + e.getMessage(), e);
        }
    }

    @Override
    public void stream(String systemPrompt, String userMessage, AIOptions options, Consumer<String> onToken) {
        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userMessage));

            ChatCompletionRequest.ChatCompletionRequestBuilder requestBuilder = ChatCompletionRequest.builder()
                .model(options.model())
                .messages(messages)
                .temperature(options.temperature())
                .maxTokens(options.maxTokens())
                .topP(options.topP())
                .stream(true);  // Enable streaming

            // Add optional parameters (only those supported by OpenAI SDK)
            if (options.frequencyPenalty() != null && options.frequencyPenalty() != 0.0) {
                requestBuilder.frequencyPenalty(options.frequencyPenalty());
            }
            if (options.presencePenalty() != null && options.presencePenalty() != 0.0) {
                requestBuilder.presencePenalty(options.presencePenalty());
            }
            if (options.stop() != null && !options.stop().isEmpty()) {
                requestBuilder.stop(options.stop());
            }

            ChatCompletionRequest request = requestBuilder.build();

            // Stream tokens
            openAiService.streamChatCompletion(request)
                .forEach(chunk -> {
                    var choices = chunk.getChoices();
                    if (!choices.isEmpty() && choices.get(0).getMessage().getContent() != null) {
                        String content = choices.get(0).getMessage().getContent();
                        onToken.accept(content);
                    }
                });
        } catch (Exception e) {
            throw new AIException("OpenAI streaming error: " + e.getMessage(), e);
        }
    }

    @Override
    public com.akilisha.oss.roya.plugins.ai.LLMResponse completeJson(String systemPrompt, String userMessage, AIOptions options) {
        // JSON mode: Add JSON format instruction to system prompt
        // Note: SDK may not support responseFormat directly, so we use prompt engineering
        String jsonPrompt = systemPrompt + "\n\nIMPORTANT: Respond ONLY with valid JSON. No explanation, no markdown, no code blocks, just pure JSON.";
        
        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), jsonPrompt));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userMessage));

            // Use lower temperature for structured output if not explicitly set
            Double temperature = options.temperature() != null 
                ? options.temperature() 
                : 0.3; // Default lower temp for JSON mode
            
            ChatCompletionRequest.ChatCompletionRequestBuilder requestBuilder = ChatCompletionRequest.builder()
                .model(options.model())
                .messages(messages)
                .temperature(Math.min(temperature, 0.3)) // Cap at 0.3 for structured output
                .maxTokens(options.maxTokens())
                .topP(options.topP());

            // Add optional parameters (only those supported by OpenAI SDK)
            if (options.frequencyPenalty() != null && options.frequencyPenalty() != 0.0) {
                requestBuilder.frequencyPenalty(options.frequencyPenalty());
            }
            if (options.presencePenalty() != null && options.presencePenalty() != 0.0) {
                requestBuilder.presencePenalty(options.presencePenalty());
            }
            if (options.stop() != null && !options.stop().isEmpty()) {
                requestBuilder.stop(options.stop());
            }

            ChatCompletionRequest request = requestBuilder.build();

            ChatCompletionResult result = openAiService.createChatCompletion(request);

            if (result.getChoices().isEmpty()) {
                throw new AIException("OpenAI returned no choices");
            }

            var choice = result.getChoices().get(0);
            String jsonText = choice.getMessage().getContent();

            // Count tokens
            int promptTokens = countTokens(jsonPrompt + "\n\n" + userMessage);
            int completionTokens = countTokens(jsonText);
            int totalTokens = (int) result.getUsage().getTotalTokens(); // Cast long to int

            return new com.akilisha.oss.roya.plugins.ai.LLMResponse(
                jsonText,
                options.model(),
                promptTokens,
                completionTokens,
                totalTokens,
                java.util.Optional.ofNullable(choice.getFinishReason())
            );
        } catch (Exception e) {
            throw new AIException("OpenAI JSON mode error: " + e.getMessage(), e);
        }
    }

    /**
     * Count tokens in text using jtokkit.
     */
    private int countTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return encoding.countTokens(text);
    }

    /**
     * Get OpenAI service instance for advanced features.
     */
    public OpenAiService getOpenAiService() {
        return openAiService;
    }
}

