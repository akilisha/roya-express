package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.builder.AIWorkflowBuilder;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.WebhookTrigger;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.visitor.LoggingVisitor;

import java.util.Map;

/**
 * Medical Bill Negotiation AI Agent - Starter Example
 * 
 * This demonstrates how to use roya-plugins:ai to build a medical bill
 * negotiation agent using RAG to overcome domain knowledge gaps.
 * 
 * Key Strategy:
 * - Use RAG (Retrieval-Augmented Generation) to build a knowledge base
 * - Leverage Vision API for document processing
 * - Use nested workflows for parallel analysis
 * - Generate negotiation letters using LLM with RAG context
 */
public class MedicalBillNegotiationAgent {
    
    public static void main(String[] args) {
        var app = Roya.create();
        
        // Setup plugins
        app.plugin(new com.akilisha.oss.roya.plugins.ai.AIPlugin());
        
        // Get AI service
        AI ai = app.services().get(AI.class);
        
        // Step 1: Index knowledge base (run once at startup)
        indexKnowledgeBase(ai);
        
        // Step 2: Create agent workflow
        Workflow agentWorkflow = createAgentWorkflow(ai);
        
        // Step 3: Create executor
        WorkflowExecutor executor = new WorkflowExecutor(agentWorkflow)
            .addVisitor(new LoggingVisitor());
        
        // Step 4: Register API endpoint
        app.post("/api/bills/analyze", (req, res, next) -> {
            String billText = req.body(String.class);
            
            executor.executeFrom("analyze", Map.of(
                "billText", billText,
                "userId", req.get("userId", String.class).orElse("anonymous")
            )).thenAccept(result -> {
                if (result.isSuccess()) {
                    res.json(Map.of(
                        "status", "success",
                        "analysis", result.context().get("analysis"),
                        "estimatedSavings", result.context().get("estimatedSavings"),
                        "negotiationLetter", result.context().get("letter")
                    ));
                } else {
                    res.status(500).json(Map.of(
                        "error", result.error().getMessage()
                    ));
                }
            });
        });
        
        app.listen(3000, () -> {
            System.out.println("Medical Bill Negotiation Agent running on http://localhost:3000");
        });
    }
    
    /**
     * Index knowledge base documents into vector store.
     * This replaces the need for domain expertise by building a searchable knowledge base.
     */
    private static void indexKnowledgeBase(AI ai) {
        System.out.println("Indexing medical billing knowledge base...");
        
        // Create collections
        ai.vectors().createCollection("medical-billing-kb");
        ai.vectors().createCollection("cost-comparison-db");
        ai.vectors().createCollection("billing-error-patterns");
        ai.vectors().createCollection("negotiation-strategies");
        
        // Index documents from directory (if they exist)
        // In production, these would be PDFs, markdown files, etc.
        try {
            java.nio.file.Path kbPath = java.nio.file.Paths.get("knowledge-base/medical-billing/");
            if (java.nio.file.Files.exists(kbPath)) {
                ai.vectors().indexPath(
                    "medical-billing-kb",
                    kbPath,
                    com.akilisha.oss.roya.plugins.ai.AI.ChunkingOptions.medium()
                );
                System.out.println("✓ Indexed knowledge base documents");
            }
        } catch (Exception e) {
            System.out.println("⚠ Knowledge base directory not found - using RAG with empty knowledge base");
        }
        
        // Index example error patterns (in production, these would come from real data)
        ai.vectors().index("billing-error-patterns", java.util.List.of(
            new com.akilisha.oss.roya.plugins.ai.AI.VectorDoc(
                "duplicate-charges",
                "Common duplicate charge patterns: Same procedure charged multiple times, same date of service with different codes, duplicate facility fees.",
                Map.of("type", "error-pattern", "category", "duplicates")
            ),
            new com.akilisha.oss.roya.plugins.ai.AI.VectorDoc(
                "upcoding",
                "Upcoding occurs when a provider bills for a more expensive procedure than was performed. Example: Billing for level 5 office visit when level 3 was performed.",
                Map.of("type", "error-pattern", "category", "upcoding")
            )
        ));
        
        System.out.println("✓ Knowledge base indexing complete");
    }
    
    /**
     * Create the main agent workflow.
     * This workflow processes a medical bill through multiple analysis steps.
     */
    private static Workflow createAgentWorkflow(AI ai) {
        return AIWorkflowBuilder.create(ai, "medical-bill-negotiation")
            // Trigger: Webhook endpoint
            .trigger("analyze", new WebhookTrigger("/api/bills/analyze"))
            
            // Step 1: Extract structured data from bill text
            .extract("parseBill", MedicalBill.class, builder -> builder
                .systemPrompt("""
                    You are a medical billing expert. Extract structured data from this medical bill text.
                    Extract: patient name, dates, procedures (with codes), charges, insurance information, totals.
                    """)
                .inputKey("billText")
                .outputKey("billData")
            )
            
            // Step 2: Parallel analysis using nested workflows
            .nested("parallelAnalysis", java.util.List.of(
                createCostComparisonWorkflow(ai),
                createErrorDetectionWorkflow(ai),
                createNegotiationOpportunityWorkflow(ai)
            ), new com.akilisha.oss.roya.workflow.nested.MergeAllAggregator(), 
               com.akilisha.oss.roya.workflow.nested.NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT)
            
            // Step 3: Aggregate analysis results
            .extract("aggregateAnalysis", BillAnalysis.class, builder -> builder
                .systemPrompt("""
                    You are a medical billing expert. Analyze all the parallel analysis results 
                    and create a comprehensive analysis with estimated savings.
                    """)
                .inputKey("parallelAnalysis")
                .outputKey("analysis")
            )
            
            // Step 4: Generate negotiation letter using RAG
            .rag("getNegotiationTemplate", builder -> builder
                .inputKey("analysis")
                .outputKey("negotiationTemplate")
                .options(com.akilisha.oss.roya.plugins.ai.RAGOptions.builder()
                    .topK(3)
                    .collection("negotiation-strategies")
                    .build())
            )
            
            // Step 5: Generate negotiation letter
            .llm("generateLetter", builder -> builder
                .systemPrompt("""
                    You are a medical bill negotiation expert. Generate a professional, 
                    persuasive negotiation letter based on the analysis and negotiation template.
                    The letter should be fact-based, professional, and include specific dollar amounts.
                    """)
                .inputKey("negotiationTemplate")
                .outputKey("letter")
                .options(com.akilisha.oss.roya.plugins.ai.AIOptions.builder()
                    .temperature(0.3)  // Lower temperature for consistency
                    .build())
            )
            
            // Edges: Define workflow flow
            .edge("analyze", "parseBill")
            .edge("parseBill", "parallelAnalysis")
            .edge("parallelAnalysis", "aggregateAnalysis")
            .edge("aggregateAnalysis", "getNegotiationTemplate")
            .edge("getNegotiationTemplate", "generateLetter")
            
            .build();
    }
    
    /**
     * Cost comparison workflow: Compare bill costs to typical costs using RAG.
     */
    private static Workflow createCostComparisonWorkflow(AI ai) {
        return AIWorkflowBuilder.create(ai, "cost-comparison")
            .trigger("start", input -> java.util.concurrent.CompletableFuture.completedFuture(
                com.akilisha.oss.roya.workflow.core.NodeOutput.success(input.data())
            ))
            
            // RAG: Look up typical costs
            .rag("lookupTypicalCosts", builder -> builder
                .inputKey("billData")
                .outputKey("typicalCosts")
                .options(com.akilisha.oss.roya.plugins.ai.RAGOptions.builder()
                    .topK(5)
                    .collection("cost-comparison-db")
                    .build())
            )
            
            // Analyze cost differences
            .llm("analyzeCosts", builder -> builder
                .systemPrompt("""
                    Compare the procedure costs on this bill to typical costs.
                    Identify procedures that are overpriced (>20% above typical) and calculate potential savings.
                    """)
                .inputKey("typicalCosts")
                .outputKey("costAnalysis")
            )
            
            .edge("start", "lookupTypicalCosts")
            .edge("lookupTypicalCosts", "analyzeCosts")
            .build();
    }
    
    /**
     * Error detection workflow: Find billing errors using RAG knowledge base.
     */
    private static Workflow createErrorDetectionWorkflow(AI ai) {
        return AIWorkflowBuilder.create(ai, "error-detection")
            .trigger("start", input -> java.util.concurrent.CompletableFuture.completedFuture(
                com.akilisha.oss.roya.workflow.core.NodeOutput.success(input.data())
            ))
            
            // RAG: Look up common error patterns
            .rag("lookupErrorPatterns", builder -> builder
                .inputKey("billData")
                .outputKey("errorPatterns")
                .options(com.akilisha.oss.roya.plugins.ai.RAGOptions.builder()
                    .topK(10)
                    .collection("billing-error-patterns")
                    .build())
            )
            
            // Detect errors
            .llm("detectErrors", builder -> builder
                .systemPrompt("""
                    Analyze this medical bill for common billing errors:
                    - Duplicate charges
                    - Upcoding (charging for more expensive procedure)
                    - Unbundling (charging separately for bundled procedures)
                    - Incorrect dates
                    - Insurance calculation errors
                    
                    Use the error patterns provided to guide your analysis.
                    """)
                .inputKey("errorPatterns")
                .outputKey("detectedErrors")
            )
            
            .edge("start", "lookupErrorPatterns")
            .edge("lookupErrorPatterns", "detectErrors")
            .build();
    }
    
    /**
     * Negotiation opportunity workflow: Identify negotiation strategies.
     */
    private static Workflow createNegotiationOpportunityWorkflow(AI ai) {
        return AIWorkflowBuilder.create(ai, "negotiation-opportunity")
            .trigger("start", input -> java.util.concurrent.CompletableFuture.completedFuture(
                com.akilisha.oss.roya.workflow.core.NodeOutput.success(input.data())
            ))
            
            // RAG: Look up negotiation strategies
            .rag("lookupStrategies", builder -> builder
                .inputKey("billData")
                .outputKey("negotiationStrategies")
                .options(com.akilisha.oss.roya.plugins.ai.RAGOptions.builder()
                    .topK(5)
                    .collection("negotiation-strategies")
                    .build())
            )
            
            // Analyze opportunities
            .llm("analyzeOpportunities", builder -> builder
                .systemPrompt("""
                    Analyze this bill for negotiation opportunities:
                    - Payment plan options
                    - Financial assistance programs
                    - Cash payment discounts
                    - Timing strategies
                    """)
                .inputKey("negotiationStrategies")
                .outputKey("opportunities")
            )
            
            .edge("start", "lookupStrategies")
            .edge("lookupStrategies", "analyzeOpportunities")
            .build();
    }
    
    // Data structures
    
    record MedicalBill(
        String patientName,
        String accountNumber,
        java.time.LocalDate serviceDate,
        String facilityName,
        java.util.List<Procedure> procedures,
        InsuranceInfo insurance,
        Money totalCharges,
        Money amountDue
    ) {}
    
    record Procedure(
        String code,
        String description,
        java.time.LocalDate date,
        Money charge
    ) {}
    
    record InsuranceInfo(
        String providerName,
        String policyNumber,
        Money deductibleUsed,
        Money coinsurance
    ) {}
    
    record Money(
        java.math.BigDecimal amount,
        String currency
    ) {}
    
    record BillAnalysis(
        java.util.List<String> errors,
        Money potentialSavings,
        String severity,
        java.util.Map<String, Object> costComparison,
        java.util.List<String> negotiationOpportunities
    ) {}
}
