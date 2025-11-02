package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.api.Edge;
import com.akilisha.oss.roya.api.Workflow;
import com.akilisha.oss.roya.api.WorkflowInput;
import com.akilisha.oss.roya.api.WorkflowResult;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;

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
        
        // Build and execute workflow
        Workflow workflow = buildReceiptWorkflow(ai);
        WorkflowResult result = executeReceiptProcessing(workflow);
        
        // Display results
        displayResults(result);
    }
    
    /**
     * Build a simplified receipt processing workflow.
     */
    private static Workflow buildReceiptWorkflow(AI ai) {
        return ai.workflow("receipt-processor")
            // Step 1: Extract structured data from receipt text
            .node("extract-details", node -> node.extract(ReceiptDetails.class)
                .systemPrompt("Extract receipt information into structured format")
                .input("${receiptText}")
                .output("receiptDetails")
            )
            
            // Step 2: Analyze spending patterns
            .node("analyze-spending", node -> node.llm()
                .systemPrompt("""
                    Analyze the spending pattern from this receipt:
                    - Identify spending category (electronics, groceries, clothing, etc.)
                    - Assess if the purchase is essential or discretionary
                    - Provide a brief financial health assessment
                    - Suggest potential savings opportunities
                    """)
                .input("${receiptDetails}")
                .output("analysisReport")
            )
            
            // Define workflow edges
            .edge("extract-details", "analyze-spending", Edge.always())
            
            .build();
    }
    
    /**
     * Execute the workflow with mock receipt data.
     */
    private static WorkflowResult executeReceiptProcessing(Workflow workflow) {
        System.out.println("\n🚀 Executing Receipt Processing Workflow...\n");
        
        // Mock receipt data
        String receiptText = """
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
        
        // Execute workflow
        return workflow.run(WorkflowInput.of(
            "receiptText", receiptText
        ));
    }
    
    /**
     * Display workflow results.
     */
    private static void displayResults(WorkflowResult result) {
        System.out.println("\n📊 Receipt Processing Complete!\n");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        if (result.success()) {
            System.out.println("✅ Workflow executed successfully");
            System.out.println("\nResults:");
            
            if (result.has("receiptDetails")) {
                System.out.println("\n📋 Extracted Receipt Details:");
                ReceiptDetails details = result.get("receiptDetails");
                System.out.println("  Vendor: " + details.vendor());
                System.out.println("  Date: " + details.date());
                System.out.println("  Total: $" + details.total());
                System.out.println("  Items: " + details.items().size());
                for (ReceiptItem item : details.items()) {
                    System.out.println("    - " + item.name() + " x" + item.quantity() + " = $" + item.price());
                }
            }
            
            if (result.has("analysisReport")) {
                System.out.println("\n📈 Spending Analysis:");
                String report = (String) result.get("analysisReport");
                System.out.println(report);
            }
        } else {
            System.out.println("❌ Workflow failed: " + result.error().getMessage());
            result.error().printStackTrace();
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
