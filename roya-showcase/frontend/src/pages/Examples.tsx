import { Link, useRoute } from 'wouter';
import { Seo } from '../components/Seo';

interface Example {
  id: string;
  title: string;
  description: string;
  tags: string[];
  code: string;
  highlights?: string[];
}

export function Examples() {
  const [, params] = useRoute('/examples/:id');
  const exampleId = params?.id;

  const examples: Example[] = [
    {
      id: 'workflow',
      title: 'Customer Inquiry Workflow',
      description: 'Multi-step AI orchestration that ingests customer emails, retrieves answers with RAG, and drafts personalized responses.',
      tags: ['Workflow', 'Multi-Step', 'Orchestration'],
      highlights: [
        'Webhook trigger → Extraction → RAG → LLM response',
        'Combines Roya Workflow nodes with LangChain4j AI Services',
        'Demonstrates document indexing + contextual response generation'
      ],
      code: `Workflow workflow = ai.workflow("customer-inquiry")
    .trigger("webhook", WebhookTrigger.builder()
        .path("/api/inquiry")
        .method("POST")
        .signingKey(env("WEBHOOK_SECRET"))
        .build())
    .extract("extract", CustomerInquiry.class, builder -> builder
        .systemPrompt("Extract contact, topic, sentiment, priority")
        .inputKey("body")
        .outputKey("inquiry"))
    .rag("knowledge", builder -> builder
        .collection("support-kb")
        .question("Given {{extract.inquiry}}, retrieve relevant policies")
        .minScore(0.65)
        .outputKey("context"))
    .llm("draft", builder -> builder
        .systemPrompt("You are a friendly support agent")
        .userPrompt("Inquiry: {{extract.inquiry}}\nContext: {{knowledge.context}}")
        .outputKey("response"))
    .edge("webhook", "extract")
    .edge("extract", "knowledge")
    .edge("knowledge", "draft")
    .build();

app.post("/api/inquiry", (req, res, next) -> {
    workflow.execute(Map.of("body", req.body()));
    res.status(202).json(Map.of("status", "queued"));
});`
    },
    {
      id: 'coffee-shop',
      title: 'Coffee Shop Assistant',
      description: 'Conversational assistant that remembers guests, recommends drinks, and browses menu knowledge with RAG + tools.',
      tags: ['RAG', 'Memory', 'Tools'],
      highlights: [
        'LangChain4j AI Service with chat memory + function tools',
        'Menu documents indexed in Qdrant for contextual answers',
        'Dynamic specials fetched from an HTTP API tool'
      ],
      code: `interface BaristaAssistant {
    @SystemMessage("You are a helpful barista")
    @UserMessage("{{question}}")
    String chat(@V("question") String question);
}

record DrinkSuggestion(String name, String reason, List<String> pairings) {}

class MenuTools {
    @Tool("List today specials")
    List<String> specials() { return http.get("https://menu/api/specials"); }
}

ChatMemory memory = memoryProvider.getOrCreate(conversationId);
BaristaAssistant assistant = ai.aiService(BaristaAssistant.class, builder -> builder
    .memory(memory)
    .tools(new MenuTools())
    .retrieval(builder1 -> builder1
        .collection("coffee-menu")
        .documentScoreThreshold(0.6)));

String answer = assistant.chat("What should I drink if I love chocolate?");`
    },
    {
      id: 'customer-support',
      title: 'Customer Support Agent',
      description: 'Document ingestion + persistent memory agent that answers policy questions and keeps conversation history.',
      tags: ['RAG', 'Persistent Memory', 'Database'],
      highlights: [
        'Persistent ChatMemory stored via Database plugin',
        'On-demand document ingestion with chunking presets',
        'REST API exposing /ask and /chat endpoints'
      ],
      code: `PersistentChatMemoryProvider memoryProvider = new PersistentChatMemoryProvider(database);

ai.vectors().indexPath("customer-support",
    Path.of("docs/policies"),
    AI.ChunkingOptions.markdown());

CustomerSupportAgentService agent = ai.aiService(CustomerSupportAgentService.class, cfg -> {
    cfg.memory(memoryProvider.getOrCreate("support"));
    cfg.retrieval(retrieval -> retrieval.collection("customer-support"));
});

app.post("/chat/:id", (req, res, next) -> {
    String conversationId = req.params().get("id").orElseThrow();
    String question = (String) req.get("body").get("question");
    ChatMemory memory = memoryProvider.getOrCreate(conversationId);
    String reply = agent.chatWithMemory(memory, question);
    res.json(Map.of("reply", reply));
});`
    },
    {
      id: 'mcp-github',
      title: 'MCP GitHub Analyst',
      description: 'Model Context Protocol client that summarizes Git history and opens issues with LangChain tools.',
      tags: ['MCP', 'External Tools', 'Agents'],
      highlights: [
        'Connects to GitHub MCP server via STDIO transport',
        'Converts MCP tool definitions into LangChain tools automatically',
        'Streams summaries and file diffs back to the user'
      ],
      code: `MCPClient client = MCPClient.builder()
    .server("github", "docker run --rm github-mcp")
    .cacheDuration(Duration.ofMinutes(10))
    .build();

List<ToolSpecification> tools = client.discoverTools("github");

interface GitAnalyst {
    @SystemMessage("Summarize repository activity")
    String summarizeCommits(String repo, String branch);
}

GitAnalyst analyst = ai.aiService(GitAnalyst.class, builder -> builder
    .toolSpecifications(tools));

String summary = analyst.summarizeCommits("akilisha/roya-express", "main");`
    },
    {
      id: 'travel-agent',
      title: 'Adaptive Travel Agent',
      description: 'Agentic trip planner that interviews the traveler, checks weather, hunts flight deals, and guides booking with human approvals.',
      tags: ['Agents', 'MCP', 'Human-in-the-loop'],
      highlights: [
        'Conversation-driven requirements capture',
        'Weather + flight deal lookups via MCP and REST tools',
        'Workflow branches for traveler approval and booking'
      ],
      code: `interface TravelConcierge {
    @SystemMessage("You are a proactive travel planner")
    String planTrip(String travelerProfile);
}

class TravelTools {
    @Tool("Check weather for a destination")
    WeatherReport weather(String city, LocalDate start, LocalDate end) { ... }

    @Tool("Search flight bundles")
    List<FlightBundle> flights(String origin, String destination, LocalDate start, LocalDate end) { ... }
}

Workflow travelWorkflow = ai.workflow("adaptive-travel")
    .trigger("start", ChatTrigger.builder()
        .path("/travel-agent")
        .welcomeMessage("Tell me about the trip you're dreaming of!")
        .build())
    .llm("intake", builder -> builder
        .systemPrompt("Interview the traveler and summarize constraints")
        .inputKey("chat.message")
        .outputKey("profile"))
    .agent("research", builder -> builder
        .systemPrompt("Given {{intake.profile}}, expand options")
        .tools(new TravelTools())
        .outputKey("bundles"))
    .approval("confirm", approval -> approval
        .prompt("Traveler, do any of these itineraries look good?\n{{research.bundles}}"))
    .llm("booking", builder -> builder
        .systemPrompt("Summarize the confirmed plan and collect booking info")
        .inputKey("confirm.response")
        .outputKey("nextSteps"))
    .edge("start", "intake")
    .edge("intake", "research")
    .edge("research", "confirm")
    .edge("confirm", "booking")
    .build();`
    },
    {
      id: 'smart-marketplace',
      title: 'Uberlist Marketplace Assistant',
      description: 'Conversational marketplace that tailors search results, runs comparisons across sources, and routes buyers to sellers.',
      tags: ['Marketplace', 'RAG', 'Agents'],
      highlights: [
        'Conversational search with dynamic filters and faceted results',
        'RAG-powered item summaries + cross-store availability checks',
        'Guided negotiation and seller hand-off via workflow nodes'
      ],
      code: `Workflow marketplace = ai.workflow("smart-marketplace")
    .trigger("chat", ChatTrigger.builder()
        .path("/uberlist")
        .welcomeMessage("Hi! What are you hunting for today?")
        .build())
    .llm("understand", builder -> builder
        .systemPrompt("Extract product intent, budget, condition preferences")
        .inputKey("chat.message")
        .outputKey("criteria"))
    .rag("inventory", builder -> builder
        .collection("classifieds")
        .question("Find items matching {{understand.criteria}}")
        .outputKey("listings"))
    .agent("augment", builder -> builder
        .systemPrompt("For each listing, fetch competitor pricing and nearby store availability")
        .tools(new MarketplaceTools())
        .outputKey("enriched"))
    .llm("present", builder -> builder
        .systemPrompt("Act as a personal shopper. Present top matches and next steps")
        .userPrompt("Criteria: {{understand.criteria}}\nListings: {{augment.enriched}}")
        .outputKey("summary"))
    .approval("handoff", approval -> approval
        .prompt("Ready to contact a seller? I'll share your info when you confirm."))
    .edge("chat", "understand")
    .edge("understand", "inventory")
    .edge("inventory", "augment")
    .edge("augment", "present")
    .edge("present", "handoff")
    .build();`
    },
    {
      id: 'sre-copilot',
      title: 'SRE Incident Copilot',
      description: 'Operations assistant that triages alerts, surfaces runbooks, asks for human approval, and orchestrates remediation tasks.',
      tags: ['DevOps', 'Monitoring', 'Human Approval'],
      highlights: [
        'Ingests alert payloads and enriches them with metrics + logs',
        'RAG against runbooks, past incidents, and Slack transcripts',
        'Guided mitigation with approval gates and postmortem draft'
      ],
      code: `Workflow incident = ai.workflow("incident-copilot")
    .trigger("alert", WebhookTrigger.builder()
        .path("/alerts")
        .method("POST")
        .build())
    .extract("classify", IncidentContext.class, builder -> builder
        .systemPrompt("Parse alert JSON and summarize the incident context")
        .inputKey("body")
        .outputKey("context"))
    .rag("runbook", builder -> builder
        .collection("sre-runbooks")
        .question("Given {{classify.context}}, retrieve mitigation steps")
        .outputKey("steps"))
    .llm("plan", builder -> builder
        .systemPrompt("Create a step-by-step remediation plan with owners")
        .userPrompt("Incident: {{classify.context}}\nRunbook: {{runbook.steps}}")
        .outputKey("plan"))
    .approval("execute", approval -> approval
        .prompt("Execute this plan?\n{{plan.plan}}"))
    .agent("automate", builder -> builder
        .systemPrompt("Run automation for approved steps and collect outputs")
        .tools(new DevOpsTools())
        .inputKey("execute.response")
        .outputKey("automation"))
    .llm("postmortem", builder -> builder
        .systemPrompt("Draft an incident summary for the postmortem doc")
        .userPrompt("Context: {{classify.context}}\nPlan: {{plan.plan}}\nExecution: {{automate.automation}}")
        .outputKey("summary"))
    .edge("alert", "classify")
    .edge("classify", "runbook")
    .edge("runbook", "plan")
    .edge("plan", "execute")
    .edge("execute", "automate")
    .edge("automate", "postmortem")
    .build();`
    }
  ];

  // Show detail view when an example is selected
  if (exampleId) {
    const example = examples.find(e => e.id === exampleId);
    
    return (
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <Seo
          title={example ? `${example.title} Example` : 'Example'}
          description={example?.description ?? 'Detailed Roya example'}
        />
        <div class="mb-8">
          <Link href="/examples" class="text-roya-primary dark:text-roya-primary hover:text-roya-primaryDark dark:hover:text-roya-primaryDark mb-6 inline-flex items-center gap-2 font-medium transition-colors">
            <span>←</span>
            <span>Back to Examples</span>
          </Link>
          <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-text dark:text-roya-textDark tracking-tight">
            {example?.title}
          </h1>
          <p class="text-lg text-roya-textMuted dark:text-roya-textMutedDark mb-6 leading-relaxed">
            {example?.description}
          </p>
          
          <div class="flex flex-wrap gap-2 mb-6">
            {example?.tags.map(tag => (
              <span class="px-3 py-1 rounded-full text-sm font-medium bg-roya-primary/10 text-roya-primary dark:bg-roya-primary/20 dark:text-roya-primary border border-roya-primary/20 dark:border-roya-primary/30">
                {tag}
              </span>
            ))}
          </div>

          {example?.highlights && (
            <div class="bg-gradient-to-br from-roya-primary/10 to-roya-accent/10 dark:from-roya-primary/20 dark:to-roya-accent/20 border-2 border-roya-primary/30 dark:border-roya-primary/50 rounded-xl p-6 mb-8 shadow-soft dark:shadow-soft-dark">
              <h3 class="font-bold text-lg text-roya-text dark:text-roya-textDark mb-3">Highlights</h3>
              <ul class="text-sm text-roya-textMuted dark:text-roya-textMutedDark space-y-2">
                {example.highlights.map(highlight => (
                  <li class="flex items-start">
                    <span class="text-roya-primary mr-3 font-bold">•</span>
                    <span class="leading-relaxed">{highlight}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
        
        <div class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-lg dark:shadow-soft-dark overflow-hidden border border-roya-border dark:border-roya-borderDark">
          <div class="bg-black dark:bg-black px-4 py-3 border-b border-roya-borderDark">
            <span class="text-roya-primary text-sm font-bold uppercase tracking-wide">Java Code</span>
          </div>
          <pre class="bg-black dark:bg-black text-roya-primary p-6 overflow-x-auto font-mono text-sm leading-relaxed"><code>{example?.code}</code></pre>
        </div>

        <div class="mt-8 bg-gradient-to-r from-roya-primary/10 to-roya-accent/10 dark:from-roya-primary/20 dark:to-roya-accent/20 border-2 border-roya-primary/30 dark:border-roya-primary/50 rounded-xl p-6 shadow-soft dark:shadow-soft-dark">
          <p class="text-sm text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
            <strong class="text-roya-text dark:text-roya-textDark">💡 Tip:</strong> Copy this code and run it locally with the full Roya framework to see it in action.
          </p>
        </div>
      </div>
    );
  }

  // Show list view
  return (
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <Seo
        title="Examples"
        description="Explore Roya examples for workflow orchestration, RAG, MCP integrations, and production-ready AI agents."
      />
      <div class="mb-10">
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-text dark:text-roya-textDark tracking-tight">
          Real-World Examples
        </h1>
        <p class="text-lg md:text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          Production-ready examples showcasing Roya's capabilities, especially the{' '}
          <Link href="/architecture" class="text-roya-primary dark:text-roya-primary font-semibold underline decoration-2 underline-offset-2 hover:text-roya-primaryDark dark:hover:text-roya-primary transition-colors">
            workflow orchestration superpower
          </Link>.
        </p>
      </div>

      {/* Highlight the Workflow Example */}
      <div class="mb-10 bg-gradient-to-br from-roya-primary/10 via-roya-primary/5 to-roya-accent/10 dark:from-roya-primary/20 dark:via-roya-primary/10 dark:to-roya-accent/20 rounded-2xl p-8 border-2 border-roya-primary/30 dark:border-roya-primary/50 shadow-soft dark:shadow-soft-dark backdrop-blur-sm">
        <div class="flex items-start gap-4">
          <div class="text-4xl">⭐</div>
          <div class="flex-1">
            <h3 class="text-2xl font-bold mb-3 text-roya-text dark:text-roya-textDark">Featured: Workflow Orchestration</h3>
            <p class="text-base text-roya-text dark:text-roya-textDark mb-3 leading-relaxed">
              The <strong class="text-roya-primary dark:text-roya-primary">Customer Inquiry Workflow</strong> example below demonstrates Roya AI's unique capability: 
              combining LangChain4j's AI primitives with Roya Workflow's orchestration to create multi-step AI pipelines.
            </p>
            <p class="text-sm text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
              This is what makes Roya AI more than just a LangChain4j wrapper—it's a <strong>workflow orchestrator</strong> 
              similar to n8n AI agents, but with Java's type-safety and performance.
            </p>
          </div>
        </div>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        {examples.map((example, idx) => (
          <Link
            href={`/examples/${example.id}`}
            class={`group bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark p-6 hover:shadow-lg dark:hover:shadow-glow-green transition-all duration-300 block border border-roya-border dark:border-roya-borderDark ${idx === 0 ? 'border-2 border-roya-primary dark:border-roya-primary ring-2 ring-roya-primary/20 dark:ring-roya-primary/30' : 'hover:border-roya-primary/50 dark:hover:border-roya-primary/50'}`}
          >
            <div class="flex items-start justify-between mb-4">
              <div class="flex-1">
                <div class="flex items-center gap-2 mb-3">
                  {idx === 0 && (
                    <span class="bg-roya-accent text-white px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wide shadow-sm">
                      Featured
                    </span>
                  )}
                  <h3 class="text-xl font-bold text-roya-text dark:text-roya-textDark group-hover:text-roya-primary dark:group-hover:text-roya-primary transition-colors">
                    {example.title}
                  </h3>
                </div>
                <p class="text-base text-roya-textMuted dark:text-roya-textMutedDark mb-4 leading-relaxed">
                  {example.description}
                </p>
                {example.highlights && (
                  <ul class="text-sm text-roya-textMuted dark:text-roya-textMutedDark mb-4 space-y-2">
                    {example.highlights.map(highlight => (
                      <li class="flex items-start">
                        <span class="text-roya-primary mr-2 font-bold">•</span>
                        <span class="leading-relaxed">{highlight}</span>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </div>
            
            <div class="flex flex-wrap gap-2 mb-4">
              {example.tags.map(tag => (
                <span class={`px-3 py-1 rounded-full text-xs font-medium ${idx === 0 ? 'bg-roya-primary/10 text-roya-primary dark:bg-roya-primary/20 dark:text-roya-primary' : 'bg-roya-surface dark:bg-roya-surfaceDark text-roya-textMuted dark:text-roya-textMutedDark border border-roya-border dark:border-roya-borderDark'}`}>
                  {tag}
                </span>
              ))}
            </div>
            
            <div class="bg-black dark:bg-black rounded-lg p-4 mb-4 overflow-x-auto border border-roya-borderDark">
              <pre class="text-xs text-roya-primary font-mono leading-snug">
                <code>{example.code.split('\n').slice(0, 3).join('\n')}...</code>
              </pre>
            </div>
            
            <div class="flex items-center text-roya-primary dark:text-roya-primary font-semibold text-sm group-hover:gap-2 transition-all">
              <span>View Demo</span>
              <span class="group-hover:translate-x-1 transition-transform">→</span>
            </div>
          </Link>
        ))}
      </div>
      
      <div class="mt-12 bg-gradient-to-r from-roya-primary/10 to-roya-accent/10 dark:from-roya-primary/20 dark:to-roya-accent/20 border-2 border-roya-primary/30 dark:border-roya-primary/50 rounded-2xl p-8 text-center shadow-soft dark:shadow-soft-dark">
        <h3 class="text-2xl font-bold text-roya-text dark:text-roya-textDark mb-3">
          Want to understand the architecture?
        </h3>
        <p class="text-base text-roya-textMuted dark:text-roya-textMutedDark mb-6 max-w-2xl mx-auto leading-relaxed">
          Learn how Roya AI combines <strong class="text-roya-primary dark:text-roya-primary">LangChain4j</strong> with <strong class="text-roya-primary dark:text-roya-primary">Roya Workflow</strong> 
          to create something unique.
        </p>
        <Link href="/architecture" class="inline-flex items-center gap-2 bg-roya-primary hover:bg-roya-primaryDark text-white px-8 py-3 rounded-xl font-bold shadow-lg hover:shadow-glow-green transition-all duration-300">
          <span>View Architecture</span>
          <span>→</span>
        </Link>
      </div>
    </div>
  );
}
