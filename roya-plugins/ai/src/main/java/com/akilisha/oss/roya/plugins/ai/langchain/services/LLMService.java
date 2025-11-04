package com.akilisha.oss.roya.plugins.ai.langchain.services;

import dev.langchain4j.model.output.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * AI Service interface for LLM operations using LangChain4j AI Services.
 * 
 * This declarative interface leverages LangChain4j's AI Services to handle
 * all the low-level plumbing (message construction, parsing, etc.) automatically.
 * 
 * Features:
 * - Automatic system message handling via @SystemMessage
 * - Type-safe structured outputs (no manual JSON parsing!)
 * - Built-in streaming support
 * - Token usage tracking
 * 
 * This interface is used with AiServices.create() to create a proxy instance.
 */
public interface LLMService {
    
    /**
     * Simple chat completion with system and user messages.
     * 
     * @param systemPrompt System prompt (instructions for the AI)
     * @param userMessage User message/query
     * @return AI response
     */
    @SystemMessage("{{systemPrompt}}")
    @UserMessage("{{userMessage}}")
    String ask(
        @V("systemPrompt") String systemPrompt,
        @V("userMessage") String userMessage
    );
    
    /**
     * Type-safe structured extraction.
     * 
     * AI Services automatically:
     * - Formats the prompt for extraction
     * - Parses JSON response into the target type
     * - Handles errors gracefully
     * 
     * @param systemPrompt System prompt (instructions for extraction)
     * @param prompt Text to extract from
     * @param extractType Target type (Java record/class)
     * @return Extracted structured data
     */
    @SystemMessage("{{systemPrompt}}\n\nExtract information from the following text into the specified format. Respond with JSON only.")
    @UserMessage("{{prompt}}")
    <T> T extract(
        @V("systemPrompt") String systemPrompt,
        @V("prompt") String prompt,
        Class<T> extractType
    );
    
    /**
     * Streaming chat completion with system and user messages.
     * 
     * Returns a TokenStream that can be configured with callbacks to handle
     * partial responses, completion, and errors.
     * 
     * @param systemPrompt System prompt (instructions for the AI)
     * @param userMessage User message/query
     * @return TokenStream for streaming responses
     */
    @SystemMessage("{{systemPrompt}}")
    @UserMessage("{{userMessage}}")
    TokenStream stream(
        @V("systemPrompt") String systemPrompt,
        @V("userMessage") String userMessage
    );
    
    /**
     * Chat completion with metadata (token usage, finish reason, sources, etc.).
     * 
     * Returns a Result wrapper that contains both the response and metadata
     * about the AI call (tokens used, finish reason, sources retrieved, etc.).
     * 
     * @param systemPrompt System prompt (instructions for the AI)
     * @param userMessage User message/query
     * @return Result containing response and metadata
     */
    @SystemMessage("{{systemPrompt}}")
    @UserMessage("{{userMessage}}")
    Result<String> askWithMetadata(
        @V("systemPrompt") String systemPrompt,
        @V("userMessage") String userMessage
    );
    
    /**
     * Structured extraction with metadata.
     * 
     * Extracts structured data and returns it wrapped in a Result to access
     * metadata like token usage, finish reason, etc.
     * 
     * @param systemPrompt System prompt (instructions for extraction)
     * @param prompt Text to extract from
     * @param extractType Target type (Java record/class)
     * @return Result containing extracted data and metadata
     */
    @SystemMessage("{{systemPrompt}}\n\nExtract information from the following text into the specified format. Respond with JSON only.")
    @UserMessage("{{prompt}}")
    <T> Result<T> extractWithMetadata(
        @V("systemPrompt") String systemPrompt,
        @V("prompt") String prompt,
        Class<T> extractType
    );
}
