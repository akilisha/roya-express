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

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(options.model())
                .messages(messages)
                .temperature(options.temperature())
                .maxTokens(options.maxTokens())
                .topP(options.topP())
                .n(options.n())
                .build();

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

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(options.model())
                .messages(messages)
                .temperature(options.temperature())
                .maxTokens(options.maxTokens())
                .topP(options.topP())
                .stream(true)  // Enable streaming
                .build();

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

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(options.model())
                .messages(messages)
                .temperature(options.temperature() != null ? Math.min(options.temperature(), 0.3) : 0.3) // Lower temp for structured output
                .maxTokens(options.maxTokens())
                .topP(options.topP())
                .build();

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

