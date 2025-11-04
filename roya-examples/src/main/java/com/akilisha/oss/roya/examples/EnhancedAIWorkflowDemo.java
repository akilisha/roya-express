package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.ai.RAGOptions;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.ManualTrigger;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.edges.Edge;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import com.akilisha.oss.roya.workflow.visitor.LoggingVisitor;

import java.nio.file.Path;
import java.util.*;

/**
 * Enhanced AI Workflow Demo - Showcasing AI Services Capabilities
 *
 * This demo demonstrates the enhanced workflow builder API with:
 * 1. RAG nodes for retrieval-augmented generation
 * 2. Vector indexing nodes for document storage
 * 3. Custom AI Service interfaces for declarative AI operations
 * 4. Enhanced LLM nodes with RAG, tools, and memory support
 *
 * Run:
 * ./gradlew :roya-examples:run --args="EnhancedAIWorkflowDemo"
 */
public class EnhancedAIWorkflowDemo {

    public static void main(String[] args) {
        System.out.println("Starting Enhnanced AI Workflow Demo");
//    }
//
//    public static void mainX(String[] args) {
        var app = Roya.create();

        // Register AI Plugin
        var aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        try {
            aiPlugin.start();
        } catch (Exception e) {
            System.err.println("❌ Error: AI plugin failed to start: " + e.getMessage());
            System.err.println("   Make sure to set: -Dai.openai.apiKey=your-api-key");
            System.exit(1);
        }

        AI ai = app.services().get(AI.class);

        // Build enhanced workflow using new AI Services capabilities
        Workflow workflow = buildEnhancedWorkflow(ai);

        // Execute workflow
        WorkflowExecutor executor = new WorkflowExecutor(workflow)
            .addVisitor(new LoggingVisitor());

        // Start from trigger node
        WorkflowResult result = executor.executeFrom(
            "start",
            Map.of("userQuestion", "How do I configure Qdrant for RAG?")
        ).join();

        // Display results
        displayResults(result);
    }

    /**
     * Build enhanced workflow showcasing AI Services capabilities.
     */
    private static Workflow buildEnhancedWorkflow(AI ai) {
        return ai.workflow("enhanced-ai-demo")
            // Trigger: Manual trigger
            .trigger("start", ManualTrigger.create())

            // Step 1: Index knowledge base documents (if not already indexed)
            .vectors("index-kb", builder -> builder
                .collection("kb")
                .directory(Path.of("docs/"), AI.ChunkingOptions.fixed(800, 200))
            )

            // Step 2: Use RAG to answer user question
            .rag("answer-question", builder -> builder
                .inputKey("userQuestion")
                .outputKey("ragAnswer")
                .options(RAGOptions.builder()
                    .collection("kb")  // Use the same collection as indexing
                    .topK(5)
                    .minScore(0.7)
                    .build())
            )

            // Step 3: Extract structured information from RAG answer
            .extract("extract-info", QuestionInfo.class, builder -> builder
                .systemPrompt("Extract key information from the answer into structured format")
                .inputKey("ragAnswer")
                .outputKey("structuredInfo")
            )

            // Step 4: Generate embeddings for similarity search
            .embeddings("embed-answer", builder -> builder
                .inputKey("ragAnswer")
                .outputKey("answerEmbedding")
                .batch(false)
            )

            // Step 5: Use LLM with RAG for enhanced analysis
            .llm("analyze-answer", builder -> builder
                .systemPrompt("Analyze the answer and provide insights")
                .inputKey("ragAnswer")
                .outputKey("analysis")
                // Note: RAG can be enabled via .rag(contentRetriever)
                // Tools can be enabled via .tools(toolSpecs)
                // Memory can be enabled via .memory(chatMemory, "userId")
            )

            // Step 6: Use custom AI Service interface for specialized operations
            .aiService("custom-service", CustomAIService.class, builder -> builder
                .outputKey("customResult")
                .execute((service, input) -> {
                    String question = input.getString("userQuestion");
                    return service.answerQuestion(question);
                })
                .configure(serviceBuilder -> {
                    // Configure RAG, tools, memory via reflection
                    // Example: serviceBuilder.contentRetriever(retriever);
                })
            )

            // Define workflow edges
            .edge("start", "index-kb")
            .edge("index-kb", "answer-question")
            .edge("answer-question", "extract-info")
            .edge("answer-question", "embed-answer", Edge.parallel())  // Parallel execution
            .edge("answer-question", "analyze-answer", Edge.parallel())  // Parallel execution
            .edge("answer-question", "custom-service", Edge.parallel())  // Parallel execution

            .build();
    }

    /**
     * Custom AI Service interface demonstrating declarative AI operations.
     *
     * Uses LangChain4j annotations for declarative AI Services:
     * - @SystemMessage for system prompts
     * - @UserMessage for user messages
     */
    interface CustomAIService {
        // @dev.langchain4j.service.SystemMessage("You are a helpful assistant specialized in answering technical questions.")
        // @dev.langchain4j.service.UserMessage("{{question}}")
        String answerQuestion(String question);
    }

    /**
     * Structured information extracted from RAG answer.
     */
    record QuestionInfo(
        String topic,
        List<String> keyPoints,
        String summary,
        Map<String, String> metadata
    ) {}

    /**
     * Display workflow results.
     */
    private static void displayResults(WorkflowResult result) {
        System.out.println("\n📊 Enhanced AI Workflow Complete!\n");
        System.out.println("═══════════════════════════════════════════════════════════");

        if (result.isSuccess()) {
            System.out.println("✅ Workflow executed successfully");
            System.out.println("\nResults:");

            var context = result.context();

            if (context.has("ragAnswer")) {
                System.out.println("\n🔍 RAG Answer:");
                String answer = context.get("ragAnswer");
                System.out.println("  " + answer);
            }

            if (context.has("structuredInfo")) {
                System.out.println("\n📋 Structured Information:");
                QuestionInfo info = context.get("structuredInfo");
                System.out.println("  Topic: " + info.topic());
                System.out.println("  Key Points: " + info.keyPoints());
                System.out.println("  Summary: " + info.summary());
            }

            if (context.has("analysis")) {
                System.out.println("\n📈 Analysis:");
                String analysis = context.get("analysis");
                System.out.println("  " + analysis);
            }

            if (context.has("customResult")) {
                System.out.println("\n🎯 Custom AI Service Result:");
                String customResult = context.get("customResult");
                System.out.println("  " + customResult);
            }

            if (context.has("answerEmbedding")) {
                @SuppressWarnings("unchecked")
                float[] embedding = (float[]) context.get("answerEmbedding");
                System.out.println("\n🔢 Generated embedding with " + embedding.length + " dimensions");
            }
        } else {
            System.out.println("❌ Workflow failed");
            result.finalOutput().error().ifPresent(error -> {
                System.out.println("Error: " + error);
            });
        }

        System.out.println("\n═══════════════════════════════════════════════════════════");
    }
}

