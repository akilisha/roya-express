# Medical Bill Negotiation AI Agent - Implementation Plan

## Executive Summary

This document outlines a comprehensive plan for building a Medical Bill Negotiation AI Agent using roya-express framework, specifically leveraging `roya-plugins:ai` and `roya-workflow` capabilities.

**Key Strategy**: Address domain knowledge gap through **RAG (Retrieval-Augmented Generation)** - build a knowledge base of medical billing expertise, regulations, and negotiation strategies that the AI can query.

---

## Architecture Overview

```
User uploads medical bill (PDF/image)
    ↓
[Document Processing Workflow]
    ├─ Extract text (Vision API)
    ├─ Parse structured data (Extract node)
    └─ Store bill data
    ↓
[Analysis Workflow]
    ├─ Compare to typical costs (RAG)
    ├─ Check insurance application (RAG)
    ├─ Identify billing errors (RAG + LLM)
    ├─ Determine negotiation opportunities (RAG)
    └─ Calculate potential savings
    ↓
[Letter Generation Workflow]
    ├─ Generate negotiation letter (LLM with RAG context)
    ├─ Include supporting data
    └─ Format for sending
    ↓
[Tracking Workflow]
    ├─ Send letter (Email plugin)
    ├─ Schedule follow-ups (Cron triggers)
    └─ Handle responses
```

---

## Phase 1: Knowledge Base Setup (Week 1)

### 1.1 Build Medical Billing Knowledge Base

**Goal**: Create a RAG knowledge base that makes up for domain knowledge gap.

**Content to Index**:
1. **Medical billing regulations**
   - CMS (Centers for Medicare & Medicaid Services) guidelines
   - State-specific billing regulations
   - No Surprises Act documentation
   - Hospital Price Transparency data

2. **Typical costs database**
   - CMS data on average procedure costs by region
   - FairHealth database (if accessible)
   - Healthcare Bluebook pricing
   - State-specific cost databases

3. **Billing error patterns**
   - Common duplicate charges
   - Upcoding patterns
   - Unbundling errors
   - Incorrect date-of-service charges
   - Insurance coordination errors

4. **Negotiation strategies**
   - Effective negotiation scripts
   - Payment plan options
   - Financial assistance programs
   - Charity care policies

5. **Insurance knowledge**
   - Common insurance plan structures
   - Deductible/coinsurance/out-of-pocket calculations
   - Appeal processes
   - Prior authorization requirements

**Implementation using roya-plugins:ai**:

```java
// Index knowledge base documents
AI ai = req.get(AI.class);

// Index documents from directory
ai.vectors().indexPath(
    "medical-billing-kb", 
    Path.of("knowledge-base/medical-billing/"),
    ChunkingOptions.medium()  // 1200 tokens, 300 overlap
);

// Index specific databases
ai.vectors().index("cost-database", List.of(
    new VectorDoc("cms-costs-2024", cmsCostData, Map.of("type", "costs", "year", 2024)),
    new VectorDoc("fairhealth-avg-costs", fairHealthData, Map.of("type", "costs")),
    // ... more cost data
));

// Index regulations
ai.vectors().index("billing-regulations", List.of(
    new VectorDoc("no-surprises-act", noSurprisesActText, Map.of("type", "regulation")),
    new VectorDoc("cms-guidelines", cmsGuidelines, Map.of("type", "regulation")),
    // ... more regulations
));
```

### 1.2 Structure Knowledge Base Collections

```java
// Separate collections for different knowledge domains
ai.vectors().createCollection("medical-billing-kb");      // General billing knowledge
ai.vectors().createCollection("cost-comparison-db");       // Cost comparison data
ai.vectors().createCollection("billing-error-patterns");   // Error detection patterns
ai.vectors().createCollection("negotiation-strategies");   // Negotiation scripts
ai.vectors().createCollection("insurance-rules");          // Insurance calculation rules
```

---

## Phase 2: Document Processing (Week 1-2)

### 2.1 Bill Upload & Processing

**Roya endpoint**:
```java
app.post("/api/bills/upload", (req, res, next) -> {
    AI ai = req.get(AI.class);
    
    // Get uploaded file
    FileUpload billFile = req.file("bill");
    
    // Trigger workflow
    Workflow workflow = createBillProcessingWorkflow(ai);
    WorkflowExecutor executor = new WorkflowExecutor(workflow);
    
    Map<String, Object> input = Map.of(
        "billUrl", billFile.url(),
        "userId", req.get("userId"),
        "uploadedAt", Instant.now()
    );
    
    CompletableFuture<WorkflowResult> result = executor.executeFrom("upload", input);
    // ... handle result
});
```

### 2.2 Vision API for Document Extraction

**Using roya-plugins:ai Vision API**:
```java
Workflow workflow = ai.workflow("bill-processor")
    .trigger("upload", new WebhookTrigger("/api/bills/upload"))
    
    // Extract text from bill (PDF/image)
    .vision("extractBillText", builder -> builder
        .imageUrl("${billUrl}")
        .prompt("Extract all text from this medical bill. Include: patient name, dates, procedures, charges, insurance information, totals.")
        .outputKey("billText")
    )
    
    // Parse structured data
    .extract("parseBill", MedicalBill.class, builder -> builder
        .systemPrompt("You are a medical billing expert. Extract structured data from the bill text.")
        .inputKey("billText")
        .outputKey("billData")
    )
    
    .edge("upload", "extractBillText")
    .edge("extractBillText", "parseBill")
    .build();
```

### 2.3 Medical Bill Data Structure

```java
record MedicalBill(
    String patientName,
    String accountNumber,
    LocalDate serviceDate,
    String facilityName,
    String facilityAddress,
    List<Procedure> procedures,
    InsuranceInfo insurance,
    Money totalCharges,
    Money insurancePayment,
    Money patientResponsibility,
    Money amountDue
) {}

record Procedure(
    String code,              // CPT/HCPCS code
    String description,
    LocalDate date,
    Money charge,
    Money allowedAmount,
    Money patientOwed
) {}

record InsuranceInfo(
    String providerName,
    String policyNumber,
    Money deductibleUsed,
    Money coinsurance,
    Money copay,
    Money outOfPocketMax
) {}
```

---

## Phase 3: Analysis Workflow (Week 2-3)

### 3.1 Multi-Path Analysis Using Nested Workflows

**Parallel analysis paths** using roya-workflow's nested workflow capability:

```java
Workflow analysisWorkflow = ai.workflow("bill-analysis")
    .trigger("analyze", new ManualTrigger())
    
    // Parallel analysis branches
    .nested("parallelAnalysis", List.of(
        createCostComparisonWorkflow(ai),
        createInsuranceVerificationWorkflow(ai),
        createErrorDetectionWorkflow(ai),
        createNegotiationOpportunityWorkflow(ai)
    ), new MergeAllAggregator(), NestedExecutionStrategy.WAIT_FOR_ALL_BEST_EFFORT)
    
    // Aggregate results
    .extract("aggregateAnalysis", BillAnalysis.class, builder -> builder
        .systemPrompt("You are a medical billing expert. Analyze all the parallel analysis results and create a comprehensive analysis.")
        .inputKey("parallelAnalysis")
        .outputKey("analysis")
    )
    
    .edge("analyze", "parallelAnalysis")
    .edge("parallelAnalysis", "aggregateAnalysis")
    .build();
```

### 3.2 Cost Comparison Workflow (RAG-based)

```java
Workflow createCostComparisonWorkflow(AI ai) {
    return ai.workflow("cost-comparison")
        .trigger("start", input -> CompletableFuture.completedFuture(
            NodeOutput.success(input.data())
        ))
        
        // For each procedure, compare to typical costs
        .llm("compareCosts", builder -> builder
            .systemPrompt("""
                You are a medical billing expert. Compare the procedure costs 
                on this bill to typical costs in the region.
                """)
            .inputKey("procedures")
            .outputKey("costComparison")
        )
        
        // RAG: Look up typical costs
        .rag("lookupTypicalCosts", builder -> builder
            .inputKey("costComparison")
            .outputKey("typicalCosts")
            .options(RAGOptions.builder()
                .topK(5)
                .collection("cost-comparison-db")
                .build())
        )
        
        // Generate cost analysis
        .llm("generateCostAnalysis", builder -> builder
            .systemPrompt("""
                Analyze the cost comparison. Identify:
                1. Procedures that are overpriced (>20% above typical)
                2. Procedures that are reasonably priced
                3. Potential savings if negotiated
                """)
            .inputKey("typicalCosts")
            .outputKey("costAnalysis")
        )
        
        .edge("start", "compareCosts")
        .edge("compareCosts", "lookupTypicalCosts")
        .edge("lookupTypicalCosts", "generateCostAnalysis")
        .build();
}
```

### 3.3 Insurance Verification Workflow (RAG-based)

```java
Workflow createInsuranceVerificationWorkflow(AI ai) {
    return ai.workflow("insurance-verification")
        .trigger("start", input -> CompletableFuture.completedFuture(
            NodeOutput.success(input.data())
        ))
        
        // RAG: Look up insurance calculation rules
        .rag("lookupInsuranceRules", builder -> builder
            .inputKey("insuranceInfo")
            .outputKey("insuranceRules")
            .options(RAGOptions.builder()
                .topK(3)
                .collection("insurance-rules")
                .build())
        )
        
        // Verify insurance calculations
        .llm("verifyInsurance", builder -> builder
            .systemPrompt("""
                Verify that the insurance has been applied correctly:
                1. Check deductible calculations
                2. Verify coinsurance percentages
                3. Check out-of-pocket maximum
                4. Verify copay amounts
                5. Check for coordination of benefits errors
                """)
            .inputKey("insuranceRules")
            .outputKey("insuranceVerification")
        )
        
        .edge("start", "lookupInsuranceRules")
        .edge("lookupInsuranceRules", "verifyInsurance")
        .build();
}
```

### 3.4 Error Detection Workflow (RAG-based)

```java
Workflow createErrorDetectionWorkflow(AI ai) {
    return ai.workflow("error-detection")
        .trigger("start", input -> CompletableFuture.completedFuture(
            NodeOutput.success(input.data())
        ))
        
        // RAG: Look up common billing error patterns
        .rag("lookupErrorPatterns", builder -> builder
            .inputKey("billData")
            .outputKey("errorPatterns")
            .options(RAGOptions.builder()
                .topK(10)
                .collection("billing-error-patterns")
                .build())
        )
        
        // Detect errors using LLM + error patterns
        .llm("detectErrors", builder -> builder
            .systemPrompt("""
                Analyze this medical bill for common billing errors:
                1. Duplicate charges
                2. Upcoding (charging for more expensive procedure)
                3. Unbundling (charging separately for procedures that should be bundled)
                4. Incorrect date of service
                5. Charging for services not received
                6. Insurance coordination errors
                
                Use the error patterns provided to guide your analysis.
                """)
            .inputKey("errorPatterns")
            .outputKey("detectedErrors")
        )
        
        // Extract structured error list
        .extract("extractErrors", ErrorList.class, builder -> builder
            .systemPrompt("Extract the list of detected billing errors into structured format.")
            .inputKey("detectedErrors")
            .outputKey("errors")
        )
        
        .edge("start", "lookupErrorPatterns")
        .edge("lookupErrorPatterns", "detectErrors")
        .edge("detectErrors", "extractErrors")
        .build();
}

record ErrorList(
    List<BillingError> errors,
    Money potentialSavings,
    String severity  // "critical", "moderate", "minor"
) {}

record BillingError(
    String type,
    String description,
    String procedureCode,
    Money incorrectAmount,
    Money correctAmount,
    Money savings
) {}
```

### 3.5 Negotiation Opportunity Analysis

```java
Workflow createNegotiationOpportunityWorkflow(AI ai) {
    return ai.workflow("negotiation-opportunity")
        .trigger("start", input -> CompletableFuture.completedFuture(
            NodeOutput.success(input.data())
        ))
        
        // RAG: Look up negotiation strategies
        .rag("lookupStrategies", builder -> builder
            .inputKey("billData")
            .outputKey("negotiationStrategies")
            .options(RAGOptions.builder()
                .topK(5)
                .collection("negotiation-strategies")
                .build())
        )
        
        // Analyze negotiation opportunities
        .llm("analyzeOpportunities", builder -> builder
            .systemPrompt("""
                Analyze this bill for negotiation opportunities:
                1. Hospitals that typically negotiate (most do)
                2. Payment plan options
                3. Financial assistance programs
                4. Charity care eligibility
                5. Cash payment discounts
                6. Timing strategies (best times to negotiate)
                
                Use the negotiation strategies provided.
                """)
            .inputKey("negotiationStrategies")
            .outputKey("opportunities")
        )
        
        .edge("start", "lookupStrategies")
        .edge("lookupStrategies", "analyzeOpportunities")
        .build();
}
```

---

## Phase 4: Letter Generation (Week 3-4)

### 4.1 Negotiation Letter Generation Workflow

```java
Workflow createLetterGenerationWorkflow(AI ai) {
    return ai.workflow("letter-generation")
        .trigger("generate", new ManualTrigger())
    
    // RAG: Get negotiation templates and strategies
    .rag("getNegotiationTemplate", builder -> builder
        .inputKey("analysis")
        .outputKey("negotiationTemplate")
        .options(RAGOptions.builder()
            .topK(3)
            .collection("negotiation-strategies")
            .build())
    )
    
    // Generate personalized negotiation letter
    .llm("generateLetter", builder -> builder
        .systemPrompt("""
            You are a medical bill negotiation expert. Generate a professional, 
            persuasive negotiation letter based on:
            
            1. The bill analysis (errors found, cost comparisons, etc.)
            2. Negotiation strategies and templates
            3. Supporting data (typical costs, regulations, etc.)
            
            The letter should be:
            - Professional and respectful
            - Fact-based (cite specific errors, cost comparisons)
            - Clear about requested actions
            - Include specific dollar amounts
            - Reference relevant regulations if applicable
            - Offer payment plan or cash discount options
            """)
        .inputKey("negotiationTemplate")
        .outputKey("letter")
        .options(AIOptions.builder()
            .model("gpt-4")  // Use best model for letter generation
            .temperature(0.3)  // Lower temperature for consistency
            .build())
    )
    
    // Extract letter components
    .extract("extractLetter", NegotiationLetter.class, builder -> builder
        .systemPrompt("Extract the letter into structured format.")
        .inputKey("letter")
        .outputKey("letterData")
    )
    
    .edge("generate", "getNegotiationTemplate")
    .edge("getNegotiationTemplate", "generateLetter")
    .edge("generateLetter", "extractLetter")
    .build();
}

record NegotiationLetter(
    String subject,
    String body,
    List<String> keyPoints,
    Money requestedReduction,
    List<String> supportingData,
    String nextSteps
) {}
```

---

## Phase 5: Tracking & Follow-up (Week 4-5)

### 5.1 Email Sending

**Using roya-plugins:email**:
```java
Workflow createEmailWorkflow(AI ai) {
    return ai.workflow("send-negotiation-email")
        .trigger("send", new ManualTrigger())
        
        .action("sendEmail", input -> {
            EmailService email = req.get(EmailService.class);
            
            NegotiationLetter letter = input.context().get("letterData");
            MedicalBill bill = input.context().get("billData");
            
            email.send(Email.builder()
                .to(bill.facilityAddress())
                .subject(letter.subject())
                .body(letter.body())
                .attachment(bill.pdf())
                .build());
            
            return CompletableFuture.completedFuture(NodeOutput.success());
        })
        
        .edge("send", "sendEmail")
        .build();
}
```

### 5.2 Follow-up Scheduling

**Using Cron triggers from roya-workflow**:
```java
Workflow createFollowUpWorkflow(AI ai) {
    return ai.workflow("follow-up")
        .trigger("schedule", new CronJobTrigger("0 9 * * *"))  // Daily at 9 AM
        
        .action("checkPending", input -> {
            // Query database for bills awaiting response
            List<Bill> pendingBills = db.query(
                "SELECT * FROM bills WHERE status = 'letter_sent' AND response_received = false"
            );
            
            return CompletableFuture.completedFuture(
                NodeOutput.success(Map.of("pendingBills", pendingBills))
            );
        })
        
        .llm("generateFollowUp", builder -> builder
            .systemPrompt("Generate a polite follow-up email for bills that haven't received a response.")
            .inputKey("pendingBills")
            .outputKey("followUpEmail")
        )
        
        .action("sendFollowUp", input -> {
            // Send follow-up emails
            // ...
        })
        
        .edge("schedule", "checkPending")
        .edge("checkPending", "generateFollowUp")
        .edge("generateFollowUp", "sendFollowUp")
        .build();
}
```

---

## Phase 6: Complete Agent Workflow

### 6.1 Main Agent Workflow

```java
public class MedicalBillNegotiationAgent {
    
    public static Workflow createAgentWorkflow(AI ai) {
        // Sub-workflows
        Workflow billProcessing = createBillProcessingWorkflow(ai);
        Workflow analysis = createAnalysisWorkflow(ai);
        Workflow letterGeneration = createLetterGenerationWorkflow(ai);
        Workflow emailSending = createEmailWorkflow(ai);
        
        // Main workflow
        return ai.workflow("medical-bill-negotiation-agent")
            .trigger("upload", new WebhookTrigger("/api/bills/upload"))
            
            // Process bill
            .continuation("processBill", billProcessing, "upload")
            
            // Analyze bill
            .continuation("analyzeBill", analysis, "analyze")
            
            // Human approval before sending (optional)
            .approval("humanReview", builder -> builder
                .provider(new PollingApprovalProvider())
                .prompt("Review the analysis and approve sending negotiation letter?")
                .timeout(Duration.ofDays(7))
            )
            
            // Generate letter
            .continuation("generateLetter", letterGeneration, "generate")
            
            // Send email
            .continuation("sendEmail", emailSending, "send")
            
            // Track status
            .action("updateStatus", input -> {
                // Update database
                db.update("bills")
                    .set("status", "letter_sent")
                    .set("sent_at", Instant.now())
                    .where("id", input.context().get("billId"))
                    .execute();
                
                return CompletableFuture.completedFuture(NodeOutput.success());
            })
            
            // Edges
            .edge("upload", "processBill")
            .edge("processBill", "analyzeBill")
            .edge("analyzeBill", "humanReview")
            .edge("humanReview", "generateLetter")
            .edge("generateLetter", "sendEmail")
            .edge("sendEmail", "updateStatus")
            
            .build();
    }
}
```

### 6.2 Roya Application Integration

```java
public class MedicalBillNegotiationApp {
    
    public static void main(String[] args) {
        var app = Roya.create();
        
        // Setup plugins
        app.plugin(new DatabasePlugin());
        app.plugin(new AIPlugin());
        app.plugin(new EmailPlugin());
        
        // Setup middleware
        app.use(Json.json());
        app.use(Cors.cors());
        app.use(Morgan.combined());
        
        // Get AI service
        AI ai = app.services().get(AI.class);
        
        // Create and register workflow
        Workflow agentWorkflow = MedicalBillNegotiationAgent.createAgentWorkflow(ai);
        WorkflowExecutor executor = new WorkflowExecutor(agentWorkflow)
            .addVisitor(new LoggingVisitor())
            .addVisitor(new CostTracker(100.00));  // $100 budget per workflow
        
        // Register workflow executor
        app.set("billNegotiationExecutor", executor);
        
        // API endpoints
        app.post("/api/bills/upload", (req, res, next) -> {
            FileUpload file = req.file("bill");
            String userId = req.get("userId");
            
            WorkflowExecutor executor = app.get("billNegotiationExecutor");
            executor.executeFrom("upload", Map.of(
                "billUrl", file.url(),
                "userId", userId
            )).thenAccept(result -> {
                if (result.isSuccess()) {
                    res.json(Map.of(
                        "status", "processing",
                        "workflowId", result.executionId(),
                        "estimatedSavings", result.context().get("estimatedSavings")
                    ));
                } else {
                    res.status(500).json(Map.of("error", result.error()));
                }
            });
        });
        
        app.get("/api/bills/:id/analysis", (req, res, next) -> {
            String billId = req.params().get("id").orElse("");
            // Query database for analysis results
            // ...
        });
        
        app.listen(3000);
    }
}
```

---

## Addressing Domain Knowledge Gap

### Strategy 1: RAG Knowledge Base (Primary Solution)

**How it works**:
1. Index all medical billing knowledge into Qdrant vector store
2. Use RAG to retrieve relevant knowledge for each analysis step
3. LLM uses retrieved knowledge + bill data to make expert decisions

**Advantages**:
- No need to be a medical billing expert
- Knowledge base can be updated as you learn
- Can combine multiple knowledge sources
- Citations provide transparency

### Strategy 2: AI Service Interface (For Complex Logic)

**Create specialized AI services** using LangChain4j's AI Services pattern:

```java
@SystemMessage("""
    You are a medical billing expert with deep knowledge of:
    - Medical billing regulations
    - Insurance calculation rules
    - Common billing errors
    - Negotiation strategies
""")
interface MedicalBillingExpert {
    
    @UserMessage("""
        Analyze this medical bill for billing errors:
        {{billText}}
    """)
    ErrorAnalysis analyzeBillingErrors(String billText);
    
    @UserMessage("""
        Compare these procedure costs to typical costs in {{region}}:
        {{procedures}}
    """)
    CostComparison compareCosts(List<Procedure> procedures, String region);
    
    @UserMessage("""
        Verify insurance calculations for this bill:
        {{insuranceInfo}}
        Bill total: {{total}}
    """)
    InsuranceVerification verifyInsurance(InsuranceInfo insuranceInfo, Money total);
    
    @UserMessage("""
        Generate a negotiation letter based on this analysis:
        {{analysis}}
        
        Facility: {{facilityName}}
        Amount due: {{amountDue}}
    """)
    NegotiationLetter generateNegotiationLetter(
        BillAnalysis analysis, 
        String facilityName, 
        Money amountDue
    );
}

// Usage in workflow
AI ai = req.get(AI.class);
MedicalBillingExpert expert = ai.aiService(MedicalBillingExpert.class, builder -> {
    builder.tools(new MedicalBillingTools());  // Add tools if needed
});

ErrorAnalysis errors = expert.analyzeBillingErrors(billText);
```

### Strategy 3: External APIs & Data Sources

**Integrate with external data sources**:
1. **FairHealth API** - Typical cost data
2. **CMS APIs** - Medicare/Medicaid pricing
3. **Healthcare Bluebook** - Fair pricing data
4. **State health department APIs** - Regional pricing

**Implementation**:
```java
.action("fetchTypicalCosts", input -> {
    String procedureCode = input.context().get("procedureCode");
    String zipCode = input.context().get("zipCode");
    
    // Call external API
    TypicalCost cost = fairHealthApi.getTypicalCost(procedureCode, zipCode);
    
    return CompletableFuture.completedFuture(
        NodeOutput.success(Map.of("typicalCost", cost))
    );
})
```

---

## Data Sources for Knowledge Base

### Free/Public Sources:
1. **CMS.gov** - Medicare pricing, regulations
2. **No Surprises Act** - Federal billing protections
3. **State health departments** - Regional regulations
4. **Healthcare Bluebook** - Fair pricing guidelines (some free content)
5. **Patient advocacy websites** - Negotiation strategies

### Paid/Commercial Sources:
1. **FairHealth** - Comprehensive cost database (API access)
2. **TurboNegotiator** - Negotiation scripts database
3. **Medical billing courses** - Educational content
4. **Legal databases** - Billing regulations

### Community Sources:
1. **Reddit r/medicalbilling** - Real-world patterns
2. **Patient forums** - Negotiation experiences
3. **Medical billing professional networks**

---

## Implementation Roadmap

### Week 1: Foundation
- [ ] Set up Qdrant vector store
- [ ] Index initial knowledge base (CMS, regulations, basic strategies)
- [ ] Create medical bill data structures
- [ ] Build document processing workflow (Vision API)

### Week 2: Analysis Workflows
- [ ] Implement cost comparison workflow (RAG)
- [ ] Implement insurance verification workflow (RAG)
- [ ] Implement error detection workflow (RAG)
- [ ] Test analysis workflows with sample bills

### Week 3: Letter Generation
- [ ] Build negotiation letter generation workflow
- [ ] Index negotiation templates and strategies
- [ ] Create letter data structures
- [ ] Test letter generation

### Week 4: Integration & Tracking
- [ ] Integrate email sending
- [ ] Build follow-up scheduling (Cron triggers)
- [ ] Create database schema for tracking
- [ ] Build API endpoints

### Week 5: Testing & Refinement
- [ ] Test with real medical bills (anonymized)
- [ ] Refine prompts based on results
- [ ] Expand knowledge base based on gaps
- [ ] Add error handling and retry logic

### Week 6: Production Readiness
- [ ] Add cost tracking and budget limits
- [ ] Add circuit breakers for external APIs
- [ ] Add monitoring and logging
- [ ] Performance testing
- [ ] Security review

---

## Cost Considerations

### AI API Costs:
- **Vision API** (bill extraction): ~$0.01-0.03 per bill
- **LLM calls** (analysis): ~$0.10-0.50 per bill (depending on complexity)
- **RAG queries**: ~$0.01-0.02 per query (embedding + retrieval)
- **Letter generation**: ~$0.05-0.15 per letter

**Total per bill**: ~$0.20-0.70

### Infrastructure Costs:
- Qdrant: Free (self-hosted) or ~$20/month (cloud)
- PostgreSQL: Free (self-hosted) or ~$15/month (cloud)
- Storage: ~$5/month for documents

### Break-even Analysis:
- If you charge 25% of savings
- Average savings: $2,000 per bill
- Your share: $500
- Cost per bill: ~$0.70
- **Profit margin: 99.86%** (extremely profitable)

---

## Legal & Compliance Considerations

### What You're NOT Doing:
- ❌ Practicing medicine
- ❌ Providing medical advice
- ❌ Diagnosing conditions
- ❌ Interpreting medical records

### What You ARE Doing:
- ✅ Analyzing bills (public information)
- ✅ Comparing to publicly available cost data
- ✅ Identifying billing errors (factual analysis)
- ✅ Negotiating on behalf of users (legal)
- ✅ Providing information and tools

### Recommended Disclaimers:
1. "This service provides bill analysis and negotiation assistance only"
2. "Not a substitute for professional medical or legal advice"
3. "Results not guaranteed"
4. "User responsible for verifying all information"

### Privacy:
- HIPAA compliance if handling PHI
- Encrypt documents at rest and in transit
- Secure storage of user data
- User consent for data processing

---

## Next Steps

1. **Start with knowledge base** - This is the foundation
2. **Build simple workflow first** - Document processing → basic analysis
3. **Test with sample bills** - Refine prompts and workflows
4. **Iterate on analysis** - Add more sophisticated checks
5. **Add letter generation** - Once analysis is solid
6. **Build tracking system** - For production use

---

## Success Metrics

- **Accuracy**: % of bills with errors correctly identified
- **Savings**: Average $ saved per bill
- **Success rate**: % of negotiations that result in reduction
- **Processing time**: Time from upload to letter generation
- **Cost efficiency**: AI costs vs. savings achieved

---

**This plan leverages roya's AI capabilities to overcome the domain knowledge gap through RAG, making it possible to build a sophisticated medical bill negotiation agent without being a medical billing expert.**




