package com.akilisha.oss.roya.examples.langchain4j.workflow_demo;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.ai.RAGOptions;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.ExtractNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.LLMActionNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.RAGNode;
import com.akilisha.oss.roya.plugins.ai.nodes.actions.VectorNode;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.WebhookTrigger;
import com.akilisha.oss.roya.workflow.core.Workflow;

import java.nio.file.Paths;
import java.util.Map;

/**
 * Customer Inquiry Processing Workflow - Comprehensive AI Workflow Demo
 *
 * <p>This workflow demonstrates a complete AI-powered customer support pipeline:
 *
 * <h3>Workflow Steps:</h3>
 * <ol>
 *   <li><b>WebhookTrigger</b> - Receives customer inquiry via HTTP POST</li>
 *   <li><b>ExtractNode</b> - Extracts structured data (intent, urgency, category)</li>
 *   <li><b>RAGNode</b> - Searches knowledge base for relevant information</li>
 *   <li><b>LLMActionNode</b> - Generates personalized response using RAG context</li>
 *   <li><b>Response</b> - Returns structured response via webhook</li>
 * </ol>
 *
 * <h3>Key Features Demonstrated:</h3>
 * <ul>
 *   <li>✅ WebhookTrigger - HTTP endpoint integration</li>
 *   <li>✅ ExtractNode - Structured data extraction</li>
 *   <li>✅ RAGNode - Knowledge base retrieval</li>
 *   <li>✅ LLMActionNode - Context-aware response generation</li>
 *   <li>✅ Workflow chaining - Multiple AI steps in sequence</li>
 *   <li>✅ Data flow - Passing data between workflow nodes</li>
 * </ul>
 *
 * <h3>Workflow Flow:</h3>
 * <pre>
 * Customer Inquiry (POST /api/inquiry)
 *     ↓
 * WebhookTrigger (receives inquiry)
 *     ↓
 * ExtractNode (extract intent, urgency, category)
 *     ↓
 * RAGNode (search knowledge base)
 *     ↓
 * LLMActionNode (generate response with context)
 *     ↓
 * Response (return structured JSON)
 * </pre>
 *
 * <h3>Example Request:</h3>
 * <pre>
 * POST http://localhost:3016/api/inquiry
 * Content-Type: application/json
 *
 * {
 *   "customerId": "12345",
 *   "inquiry": "I need to return an item I ordered last week. What's the process?",
 *   "metadata": {
 *     "source": "web",
 *     "timestamp": "2025-01-29T10:30:00Z"
 *   }
 * }
 * </pre>
 *
 * <h3>Example Response:</h3>
 * <pre>
 * {
 *   "customerId": "12345",
 *   "intent": "RETURN_REQUEST",
 *   "urgency": "MEDIUM",
 *   "category": "RETURNS",
 *   "ragContext": "You have 30 days to return items...",
 *   "response": "I can help you with your return request...",
 *   "metadata": {
 *     "workflowId": "...",
 *     "timestamp": "..."
 *   }
 * }
 * </pre>
 *
 * <h3>Prerequisites:</h3>
 * <ul>
 *   <li>Document indexed in Qdrant collection "customer-support"</li>
 *   <li>OPENAI_API_KEY configured (for LLM and extraction)</li>
 *   <li>QDRANT_URL configured (optional, defaults to localhost:6333)</li>
 * </ul>
 *
 * <h3>Setup:</h3>
 * <pre>
 * // 1. Index knowledge base document
 * curl -X POST http://localhost:3016/api/index \
 *   -H "Content-Type: application/json" \
 *   -d '{"path": "miles-of-smiles-terms-of-use.txt"}'
 *
 * // 2. Send customer inquiry
 * curl -X POST http://localhost:3016/api/inquiry \
 *   -H "Content-Type: application/json" \
 *   -d '{
 *     "customerId": "12345",
 *     "inquiry": "What is your refund policy?"
 *   }'
 * </pre>
 */
public class CustomerInquiryWorkflow {

    /**
     * Structured data extracted from customer inquiry.
     */
    public record InquiryDetails(
        String intent,      // RETURN_REQUEST, REFUND_REQUEST, SHIPPING_QUESTION, etc.
        String urgency,     // LOW, MEDIUM, HIGH, CRITICAL
        String category,    // RETURNS, REFUNDS, SHIPPING, WARRANTY, GENERAL
        String summary      // Brief summary of the inquiry
    ) {}

    public static void main(String[] args) {
        var app = Roya.create();

        // Register AI plugin
        var aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        try {
            aiPlugin.start();
        } catch (Exception e) {
            System.err.println("❌ Error: AI plugin failed to start: " + e.getMessage());
            System.exit(1);
        }

        AI ai = app.services().get(AI.class);

        // Step 1: Create knowledge base collection (if needed)
        // This step indexes the document into Qdrant for RAG retrieval
        
        // Step 2: Build the Customer Inquiry Processing Workflow
        Workflow inquiryWorkflow = ai.workflow("customer-inquiry")
            // Trigger: Webhook at /api/inquiry
            .trigger("webhook", WebhookTrigger.builder()
                .path("/api/inquiry")
                .method("POST")
                .build())
            
            // Step 1: Extract structured data from inquiry
            .action("extract", ExtractNode.<InquiryDetails>builder(ai, InquiryDetails.class)
                .systemPrompt("Extract intent, urgency, category, and summary from customer inquiry. " +
                    "Intent options: RETURN_REQUEST, REFUND_REQUEST, SHIPPING_QUESTION, WARRANTY_QUESTION, GENERAL_INQUIRY. " +
                    "Urgency options: LOW, MEDIUM, HIGH, CRITICAL. " +
                    "Category options: RETURNS, REFUNDS, SHIPPING, WARRANTY, GENERAL.")
                .inputKey("inquiry")
                .outputKey("details")
                .build())
            
            // Step 2: Search knowledge base using RAG
            .action("rag", RAGNode.builder(ai)
                .inputKey("inquiry")
                .outputKey("ragAnswer")
                .options(RAGOptions.builder()
                    .collection("customer-support")
                    .topK(3)
                    .minScore(0.6)
                    .build())
                .build())
            
            // Step 3: Generate personalized response using RAG context
            .action("generate", LLMActionNode.builder(ai)
                .systemPrompt("You are a helpful customer support agent. " +
                    "Use the retrieved context from the knowledge base to answer the customer's question. " +
                    "Be professional, concise, and helpful. " +
                    "If the context doesn't contain enough information, say so politely.")
                .inputKey("inquiry")
                .outputKey("response")
                .build())
            
            // Connect workflow steps
            .edge("webhook", "extract")
            .edge("extract", "rag")
            .edge("rag", "generate")
            
            .build();

        // Step 3: Workflow is automatically registered when built
        // The WebhookTrigger will auto-register the HTTP endpoint
        System.out.println("✓ Workflow 'customer-inquiry' built and registered");

        // Step 4: Index knowledge base document (for demo)
        app.post("/api/index", (req, res, next) -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = req.body(Map.class);
            String path = body != null && body.containsKey("path") 
                ? (String) body.get("path") 
                : "miles-of-smiles-terms-of-use.txt";
            
            try {
                // Index document into Qdrant collection
                ai.vectors().indexPath(
                    "customer-support",
                    Paths.get(path),
                    com.akilisha.oss.roya.plugins.ai.AI.ChunkingOptions.medium()
                );
                
                res.json(Map.of(
                    "status", "success",
                    "message", "Document indexed successfully",
                    "collection", "customer-support",
                    "path", path
                ));
            } catch (Exception e) {
                res.status(500).json(Map.of(
                    "status", "error",
                    "message", "Failed to index document: " + e.getMessage()
                ));
            }
        });

        // Step 5: Health check endpoint
        app.get("/", (req, res, next) -> {
            res.json(Map.of(
                "application", "Customer Inquiry Processing Workflow",
                "description", "AI-powered customer support workflow with RAG",
                "endpoints", Map.of(
                    "POST /api/inquiry", "Process customer inquiry (workflow)",
                    "POST /api/index", "Index knowledge base document",
                    "GET /", "API documentation"
                ),
                "workflow", Map.of(
                    "name", "customer-inquiry",
                    "steps", java.util.List.of(
                        "WebhookTrigger - Receive inquiry",
                        "ExtractNode - Extract structured data",
                        "RAGNode - Search knowledge base",
                        "LLMActionNode - Generate response"
                    )
                ),
                "example", Map.of(
                    "request", Map.of(
                        "customerId", "12345",
                        "inquiry", "What is your refund policy?"
                    ),
                    "response", Map.of(
                        "intent", "REFUND_REQUEST",
                        "urgency", "MEDIUM",
                        "category", "REFUNDS",
                        "ragContext", "We offer full refunds within 30 days...",
                        "response", "I can help you with your refund request..."
                    )
                )
            ));
        });

        int port = 3016;
        System.out.println("🎯 Customer Inquiry Processing Workflow");
        System.out.println("📡 Server running on http://localhost:" + port);
        System.out.println();
        System.out.println("📚 Endpoints:");
        System.out.println("   POST /api/inquiry - Process customer inquiry (workflow)");
        System.out.println("   POST /api/index   - Index knowledge base document");
        System.out.println("   GET  /            - API documentation");
        System.out.println();
        System.out.println("💡 Workflow Steps:");
        System.out.println("   1. WebhookTrigger - Receives customer inquiry");
        System.out.println("   2. ExtractNode    - Extracts structured data (intent, urgency, category)");
        System.out.println("   3. RAGNode        - Searches knowledge base for relevant context");
        System.out.println("   4. LLMActionNode  - Generates personalized response");
        System.out.println();
        System.out.println("🚀 Getting Started:");
        System.out.println("   1. Index document: POST /api/index");
        System.out.println("   2. Send inquiry: POST /api/inquiry");
        System.out.println();

        app.listen(port);
    }
}

