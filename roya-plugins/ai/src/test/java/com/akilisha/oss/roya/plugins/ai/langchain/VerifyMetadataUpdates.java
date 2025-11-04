package com.akilisha.oss.roya.plugins.ai.langchain;

import com.akilisha.oss.roya.plugins.ai.AIOptions;
import com.akilisha.oss.roya.plugins.ai.AIResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * Simple verification script to test LangChainAdapter metadata updates.
 * 
 * Usage:
 *   1. Set OPENAI_API_KEY environment variable
 *   2. Run: ./gradlew :roya-plugins:ai:compileJava
 *   3. Run this class with JUnit or directly
 */
public class VerifyMetadataUpdates {
    
    public static void main(String[] args) {
        // Setup
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("❌ ERROR: Set OPENAI_API_KEY environment variable");
            System.err.println("   Example: export OPENAI_API_KEY=sk-...");
            System.exit(1);
        }

        System.out.println("🚀 Testing LangChainAdapter Metadata Updates\n");

        var chatModel = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName("gpt-3.5-turbo")
            .logRequests(false)
            .logResponses(false)
            .build();
        
        var streamingModel = OpenAiStreamingChatModel.builder()
            .apiKey(apiKey)
            .modelName("gpt-3.5-turbo")
            .logRequests(false)
            .logResponses(false)
            .build();

        var adapter = new LangChainAdapter(chatModel, streamingModel, null);

        try {
            // Test 1: Token Usage Extraction
            System.out.println("=== Test 1: Token Usage Extraction ===");
            AIResponse<String> response = adapter.askWithMetadata(
                "You are a helpful assistant.",
                "Say hello in one sentence.",
                AIOptions.builder().model("gpt-3.5-turbo").build()
            );
            
            System.out.println("Response: " + response.data());
            System.out.println("Prompt tokens: " + response.promptTokens());
            System.out.println("Completion tokens: " + response.completionTokens());
            System.out.println("Total tokens: " + response.totalTokens());
            System.out.println("Cost: " + response.costFormatted());
            
            // Verify tokens are not zero
            if (response.promptTokens() == 0 || response.completionTokens() == 0) {
                System.err.println("❌ FAILED: Token counts are zero!");
                System.exit(1);
            }
            if (response.cost() <= 0.0) {
                System.err.println("❌ FAILED: Cost is zero!");
                System.exit(1);
            }
            System.out.println("✅ Token usage extraction works!\n");

            // Test 2: Structured Extraction with Metadata
            System.out.println("=== Test 2: Structured Extraction with Metadata ===");
            record Person(String name, int age) {}
            
            AIResponse<Person> extracted = adapter.extractWithMetadata(
                Person.class,
                "John is 30 years old.",
                AIOptions.builder().model("gpt-3.5-turbo").build()
            );
            
            System.out.println("Extracted: " + extracted.data());
            System.out.println("Tokens: " + extracted.totalTokens());
            System.out.println("Cost: " + extracted.costFormatted());
            
            if (!extracted.data().name().equals("John") || extracted.data().age() != 30) {
                System.err.println("❌ FAILED: Extraction incorrect!");
                System.exit(1);
            }
            if (extracted.totalTokens() == 0) {
                System.err.println("❌ FAILED: Token counts are zero!");
                System.exit(1);
            }
            System.out.println("✅ Structured extraction with metadata works!\n");

            // Test 3: Streaming
            System.out.println("=== Test 3: Streaming ===");
            System.out.print("Streaming response: ");
            final StringBuilder streamedText = new StringBuilder();
            adapter.stream(
                "You are helpful.",
                "Count from 1 to 3.",
                token -> {
                    System.out.print(token);
                    streamedText.append(token);
                }
            );
            System.out.println();
            
            if (streamedText.length() == 0) {
                System.err.println("❌ FAILED: No tokens received!");
                System.exit(1);
            }
            System.out.println("✅ Streaming works!\n");

            // Test 4: Backward Compatibility
            System.out.println("=== Test 4: Backward Compatibility ===");
            String simpleResponse = adapter.ask("You are helpful.", "Say hi.");
            if (simpleResponse == null || simpleResponse.isEmpty()) {
                System.err.println("❌ FAILED: Simple ask() doesn't work!");
                System.exit(1);
            }
            System.out.println("Simple ask() response: " + simpleResponse);
            System.out.println("✅ Backward compatibility works!\n");

            System.out.println("🎉 All tests passed! Metadata updates are working correctly.");
            
        } catch (Exception e) {
            System.err.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

