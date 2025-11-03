package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.edges.Edge;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import com.akilisha.oss.roya.workflow.visitor.LoggingVisitor;

import java.util.List;
import java.util.Map;

/**
 * AI Workflow Showcase: Receipt Processing Demo
 *
 * Demonstrates a real-world AI workflow:
 * 1. Extract structured data from receipts
 * 2. Generate embeddings for similarity search
 * 3. Analyze spending patterns
 * 4. Save analysis results
 */
public class AIShowcaseDemo {
    
    public static void main(String[] args) {
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
        
        // Get AI service from a mock request context
        // In a real app, this would be inside a route handler
        AI ai = app.services().get(AI.class);
        
        // Build workflow using new AI workflow builder
        Workflow workflow = buildReceiptWorkflow(ai);
        
        // Execute workflow
        WorkflowExecutor executor = new WorkflowExecutor(workflow)
            .addVisitor(new LoggingVisitor());
        
        // Start from extract-details node (first node in workflow)
        WorkflowResult result = executor.executeFrom(
            "extract-details",  // Trigger node
            Map.of("receiptText", getMockReceiptText())
        ).join();
        
        // Display results
        displayResults(result);
    }
    
    /**
     * Build comprehensive receipt processing workflow using new roya-workflow abstractions.
     * 
     * This workflow demonstrates:
     * - Type-safe structured extraction (ExtractNode)
     * - LLM analysis (LLMActionNode)
     * - Batch embeddings (EmbeddingNode)
     * - Conditional routing (Edge.when)
     * - Parallel execution (Edge.parallel)
     * 
     * Future nodes (not yet implemented but would fit here):
     * - Vision node for OCR
     * - Audio node for voice notes
     * - Vector search node
     * - RAG node
     * - MCP node for geolocation
     * - Cache/Database/Storage/Email nodes
     */
    private static Workflow buildReceiptWorkflow(AI ai) {
        return ai.workflow("receipt-processor")
            // Step 1: Extract structured data from receipt text
            .extract("extract-details", ReceiptDetails.class, builder -> builder
                .systemPrompt("Extract receipt information into structured format. Include vendor, date, items, totals, payment method, and receipt ID.")
                .inputKey("receiptText")
                .outputKey("receiptDetails")
            )
            
            // Step 2: Generate embeddings for all items (for future vector search)
            .embeddings("embed-items", builder -> builder
                .inputKey("receiptDetails.items")
                .outputKey("itemEmbeddings")
                .batch(true)  // Batch process all items
            )
            
            // Step 3: Analyze spending patterns (uses extracted details)
            .llm("analyze-spending", builder -> builder
                .systemPrompt("""
                    Analyze the spending pattern from this receipt:
                    - Identify spending category (electronics, groceries, clothing, etc.)
                    - Assess if the purchase is essential or discretionary
                    - Provide a brief financial health assessment
                    - Suggest potential savings opportunities
                    - Format as a structured report
                    """)
                .inputKey("receiptDetails")
                .outputKey("analysisReport")
            )
            
            // Step 4: Generate summary (parallel with analysis)
            .llm("generate-summary", builder -> builder
                .systemPrompt("Generate a one-sentence summary of this receipt purchase.")
                .inputKey("receiptDetails")
                .outputKey("summary")
            )
            
            // Define workflow edges
            .edge("extract-details", "embed-items")
            .edge("extract-details", "analyze-spending", Edge.parallel())  // Parallel execution
            .edge("extract-details", "generate-summary", Edge.parallel())  // Parallel execution
            
            // Both analysis and summary can use embeddings results (if available)
            .edge("embed-items", "analyze-spending")
            .edge("embed-items", "generate-summary")
            
            .build();
    }
    
    /**
     * Get mock receipt data.
     */
    private static String getMockReceiptText() {
        return """
            AMAZON.COM
            12345-MY-ORDER
            ============================================
            MacBook Pro 14"
            Qty: 1          $1,999.00
            --------------------------------------------
            AirPods Pro
            Qty: 1          $249.00
            --------------------------------------------
            SUBTOTAL:                  $2,248.00
            TAX:                       $180.24
            --------------------------------------------
            TOTAL:                     $2,428.24
            
            Payment: Visa ending in 1234
            Date: 2024-11-01 14:32
            Receipt ID: AMZ-2024-001
            """;
    }
    
    /**
     * Display workflow results.
     */
    private static void displayResults(WorkflowResult result) {
        System.out.println("\n📊 Receipt Processing Complete!\n");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        if (result.isSuccess()) {
            System.out.println("✅ Workflow executed successfully");
            System.out.println("\nResults:");
            
            var context = result.context();
            if (context.has("receiptDetails")) {
                System.out.println("\n📋 Extracted Receipt Details:");
                ReceiptDetails details = context.get("receiptDetails");
                System.out.println("  Vendor: " + details.vendor());
                System.out.println("  Date: " + details.date());
                System.out.println("  Total: $" + details.total());
                System.out.println("  Items: " + details.items().size());
                for (ReceiptItem item : details.items()) {
                    System.out.println("    - " + item.name() + " x" + item.quantity() + " = $" + item.price());
                }
            }
            
            if (context.has("analysisReport")) {
                System.out.println("\n📈 Spending Analysis:");
                String report = context.get("analysisReport");
                System.out.println(report);
            }
            
            if (context.has("summary")) {
                System.out.println("\n📝 Summary:");
                String summary = context.get("summary");
                System.out.println("  " + summary);
            }
            
            if (context.has("itemEmbeddings")) {
                @SuppressWarnings("unchecked")
                List<float[]> embeddings = (List<float[]>) context.get("itemEmbeddings");
                System.out.println("\n🔢 Generated " + embeddings.size() + " item embeddings (for vector search)");
            }
        } else {
            System.out.println("❌ Workflow failed");
            result.finalOutput().error().ifPresent(error -> {
                System.out.println("Error: " + error);
            });
        }
        
        System.out.println("\n═══════════════════════════════════════════════════════════");
    }
    
    /**
     * Receipt details record for structured extraction.
     */
    record ReceiptDetails(
        String vendor,
        String date,
        List<ReceiptItem> items,
        Double subtotal,
        Double tax,
        Double total,
        String paymentMethod,
        String receiptId
    ) {}
    
    record ReceiptItem(
        String name,
        Integer quantity,
        Double price
    ) {}
}
