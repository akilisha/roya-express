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
        <div class="mb-6">
          <Link href="/examples" class="text-blue-600 hover:text-blue-700 mb-4 inline-block">
            ← Back to Examples
          </Link>
          <h1 class="text-3xl font-bold mb-2">{example?.title}</h1>
          <p class="text-gray-600 mb-4">{example?.description}</p>
          
          <div class="flex flex-wrap gap-2 mb-6">
            {example?.tags.map(tag => (
              <span class="px-2 py-1 rounded text-sm bg-blue-100 text-blue-600">
                {tag}
              </span>
            ))}
          </div>

          {example?.highlights && (
            <div class="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
              <h3 class="font-semibold text-blue-900 mb-2">Highlights</h3>
              <ul class="text-sm text-blue-800 space-y-1">
                {example.highlights.map(highlight => (
                  <li class="flex items-start">
                    <span class="text-blue-500 mr-2">•</span>
                    <span>{highlight}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
        
        <div class="bg-white rounded-lg shadow-lg overflow-hidden">
          <div class="bg-gray-800 px-4 py-2">
            <span class="text-white text-sm font-medium">Java Code</span>
          </div>
          <pre class="bg-gray-900 text-green-400 p-6 overflow-x-auto"><code>{example?.code}</code></pre>
        </div>

        <div class="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
          <p class="text-sm text-blue-800">
            <strong>💡 Tip:</strong> Copy this code and run it locally with the full Roya framework to see it in action.
          </p>
        </div>
      </div>
    );
  }

  // Show list view
  return (
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <h1 class="text-3xl font-bold mb-2">Real-World Examples</h1>
        <p class="text-gray-600">
          Production-ready examples showcasing Roya's capabilities, especially the 
          <Link href="/architecture" class="text-blue-600 hover:text-blue-700 underline font-semibold">workflow orchestration superpower</Link>.
        </p>
      </div>

      {/* Highlight the Workflow Example */}
      <div class="mb-8 bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg p-6 border-2 border-blue-200">
        <div class="flex items-start">
          <span class="text-3xl mr-4">⭐</span>
          <div>
            <h3 class="text-xl font-semibold mb-2">Featured: Workflow Orchestration</h3>
            <p class="text-gray-700 mb-3">
              The <strong>Customer Inquiry Workflow</strong> example below demonstrates Roya AI's unique capability: 
              combining LangChain4j's AI primitives with Roya Workflow's orchestration to create multi-step AI pipelines.
            </p>
            <p class="text-sm text-gray-600">
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
            class={`bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow block ${idx === 0 ? 'border-2 border-blue-300' : ''}`}
          >
            <div class="flex items-start justify-between mb-4">
              <div class="flex-1">
                <div class="flex items-center mb-2">
                  {idx === 0 && <span class="bg-yellow-400 text-yellow-900 px-2 py-1 rounded text-xs font-bold mr-2">FEATURED</span>}
                  <h3 class="text-xl font-semibold">{example.title}</h3>
                </div>
                <p class="text-gray-600 mb-4">{example.description}</p>
                {example.highlights && (
                  <ul class="text-sm text-gray-600 mb-4 space-y-1">
                    {example.highlights.map(highlight => (
                      <li class="flex items-start">
                        <span class="text-green-500 mr-2">•</span>
                        <span>{highlight}</span>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </div>
            
            <div class="flex flex-wrap gap-2 mb-4">
              {example.tags.map(tag => (
                <span class={`px-2 py-1 rounded text-sm ${idx === 0 ? 'bg-blue-100 text-blue-600' : 'bg-gray-100 text-gray-600'}`}>
                  {tag}
                </span>
              ))}
            </div>
            
            <div class="bg-gray-900 rounded p-3 mb-4 overflow-x-auto">
              <pre class="text-xs text-green-400 font-mono">
                <code>{example.code.split('\n')[0]}...</code>
              </pre>
            </div>
            
            <div class="text-blue-600 hover:text-blue-700 font-semibold text-sm">
              View Demo →
            </div>
          </Link>
        ))}
      </div>
      
      <div class="mt-12 bg-blue-50 border border-blue-200 rounded-lg p-6 text-center">
        <h3 class="text-xl font-semibold text-blue-900 mb-2">
          Want to understand the architecture?
        </h3>
        <p class="text-blue-800 mb-4">
          Learn how Roya AI combines <strong>LangChain4j</strong> with <strong>Roya Workflow</strong> 
          to create something unique.
        </p>
        <Link href="/architecture" class="bg-blue-600 text-white px-6 py-2 rounded-lg font-semibold hover:bg-blue-700 transition-colors inline-block">
          View Architecture →
        </Link>
      </div>
    </div>
  );
}
