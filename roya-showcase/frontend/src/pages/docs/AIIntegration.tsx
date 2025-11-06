import { Link, useLocation } from 'wouter';

export function AIIntegration() {
  const [location] = useLocation();
  
  const sections = [
    {
      title: 'Quick Start',
      href: '/docs/ai/quick-start',
      description: 'Get started with AI in Roya in 5 minutes'
    },
    {
      title: 'LLM',
      href: '/docs/ai/llm',
      description: 'Large Language Models - OpenAI, Anthropic, local models'
    },
    {
      title: 'RAG',
      href: '/docs/ai/rag',
      description: 'Retrieval-Augmented Generation with vector stores'
    },
    {
      title: 'Agents',
      href: '/docs/ai/agents',
      description: 'AI Agents with tools and memory'
    },
    {
      title: 'Workflows',
      href: '/docs/ai/workflows',
      description: 'Multi-step AI orchestration with Roya Workflow'
    }
  ];
  
  return (
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs" class="text-blue-600 hover:underline mb-4 inline-block">
          ← Documentation
        </Link>
        <h1 class="text-4xl font-bold mb-4">AI Integration</h1>
        <p class="text-xl text-gray-600">
          Roya AI brings LangChain4j's powerful AI primitives to Java with a clean, Express-inspired API. Build AI-powered applications with LLMs, RAG, Agents, and Workflows.
        </p>
      </div>
      
      <div class="mb-8 bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg p-6 border-2 border-blue-200">
        <h2 class="text-2xl font-semibold mb-3">Why Roya AI?</h2>
        <ul class="space-y-2 text-gray-700">
          <li>✅ <strong>Native LangChain4j Integration</strong> - Not a wrapper, but a framework built on LangChain4j</li>
          <li>✅ <strong>Workflow Orchestration</strong> - Multi-step AI pipelines (like n8n AI agents, but in Java)</li>
          <li>✅ <strong>Express-Style API</strong> - Familiar patterns if you know Express.js</li>
          <li>✅ <strong>Type-Safe</strong> - Java's type system ensures correctness</li>
          <li>✅ <strong>Production-Ready</strong> - Built for scale with virtual threads</li>
        </ul>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        {sections.map(section => (
          <Link
            href={section.href}
            class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow text-left"
          >
            <h2 class="text-2xl font-semibold mb-2 text-blue-600">{section.title}</h2>
            <p class="text-gray-600">{section.description}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}

