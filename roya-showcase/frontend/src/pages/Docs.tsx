export function Docs() {
  return (
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <h1 class="text-3xl font-bold mb-2">Documentation</h1>
      <p class="text-gray-600 mb-8">
        Comprehensive guides, API reference, and examples.
      </p>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div class="bg-white rounded-lg shadow-md p-6">
          <h3 class="text-xl font-semibold mb-2">Getting Started</h3>
          <p class="text-gray-600">Learn Roya in 15 minutes</p>
        </div>
        
        <div class="bg-white rounded-lg shadow-md p-6">
          <h3 class="text-xl font-semibold mb-2">AI Integration</h3>
          <p class="text-gray-600">LLM, RAG, Agents, Workflows</p>
        </div>
        
        <div class="bg-white rounded-lg shadow-md p-6">
          <h3 class="text-xl font-semibold mb-2">API Reference</h3>
          <p class="text-gray-600">Complete API documentation</p>
        </div>
        
        <div class="bg-white rounded-lg shadow-md p-6">
          <h3 class="text-xl font-semibold mb-2">Migration Guides</h3>
          <p class="text-gray-600">Express and Spring Boot migration</p>
        </div>
      </div>
    </div>
  );
}

