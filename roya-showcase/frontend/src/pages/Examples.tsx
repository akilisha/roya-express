import { Link, useRoute } from 'wouter';

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
      description: 'Multi-step AI orchestration showcasing Roya Workflow + LangChain4j',
      tags: ['Workflow', 'Multi-Step', 'Orchestration'],
      highlights: [
        'Webhook trigger → Extract → RAG → Generate',
        'Shows workflow orchestration (Layer 3)',
        'Combines LangChain4j primitives (Layer 2)'
      ],
      code: `Workflow workflow = ai.workflow("customer-inquiry")
    .trigger("webhook", WebhookTrigger.builder()
        .path("/api/inquiry").method("POST").build())
    .action("extract", ExtractNode.<InquiryDetails>builder(...))
    .action("rag", RAGNode.builder(...))
    .action("generate", LLMActionNode.builder(...))
    .edge("webhook", "extract")
    .edge("extract", "rag")
    .edge("rag", "generate")
    .build();`
    },
    {
      id: 'coffee-shop',
      title: 'Coffee Shop Assistant',
      description: 'RAG-powered assistant with chat memory and tools',
      tags: ['RAG', 'Memory', 'Tools'],
      code: `// Full RAG + Memory + Tools example
ChatMemory memory = memoryProvider.getOrCreate(conversationId);
RAGResponse response = ai.ragApi().ask(question, 
    RAGOptions.builder().collection("menu-items").build());
MyService service = ai.aiService(MyService.class,
    builder -> builder.tools(new MenuTools()));`
    },
    {
      id: 'customer-support',
      title: 'Customer Support Agent',
      description: 'Complete document processing pipeline with RAG',
      tags: ['RAG', 'Document Processing'],
      code: `// Index documents
ai.vectors().indexPath("customer-support", 
    Path.of("docs/"), ChunkingOptions.medium());

// Query with RAG
RAGResponse answer = ai.ragApi().ask(question,
    RAGOptions.builder().collection("customer-support").build());`
    },
    {
      id: 'mcp-github',
      title: 'MCP GitHub Example',
      description: 'Model Context Protocol integration with GitHub tools',
      tags: ['MCP', 'External Tools'],
      code: `// Discover MCP tools
MCPClient mcpClient = MCPClient.builder()
    .server("github", "https://mcp-server.example.com")
    .build();

// Use in AI Service
MyService service = ai.aiService(MyService.class,
    builder -> builder.toolProvider(mcpToolProvider));`
    }
  ];

  // Show detail view when an example is selected
  if (exampleId) {
    const example = examples.find(e => e.id === exampleId);
    
    return (
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
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
              <pre class="text-xs text-roya-primary font-mono">
                <code>{example.code.split('\n')[0]}...</code>
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
