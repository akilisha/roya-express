import { signal } from '@preact/signals';
import { useRoute } from 'wouter';
import { CodeEditor } from '../components/CodeEditor';

export function Tutorials() {
  const [, params] = useRoute('/tutorials/:id');
  const tutorialId = params?.id;

  const tutorials = [
    { 
      id: '01', 
      title: 'Basic LLM', 
      description: 'Simple ask/response with system prompts',
      code: `AI ai = app.services().get(AI.class);
String response = ai.llm().ask(
    "You are a helpful assistant.",
    "What is Roya?"
);`
    },
    { 
      id: '02', 
      title: 'Model Parameters', 
      description: 'Temperature, max tokens, topP, etc.',
      code: `AI ai = app.services().get(AI.class);
AIOptions options = AIOptions.builder()
    .temperature(0.7)
    .maxTokens(500)
    .build();
String response = ai.llm().ask("...", "...", options);`
    },
    { 
      id: '03', 
      title: 'Image Generation', 
      description: 'DALL-E integration via LangChain4j',
      code: `AI ai = app.services().get(AI.class);
String imageUrl = ai.vision().generateImage(
    "A futuristic robot assistant"
);`
    },
    { 
      id: '04', 
      title: 'Prompt Templates', 
      description: 'Native LangChain4j Prompt/PromptTemplate',
      code: `Prompt prompt = Prompt.from("Hello {{name}}!");
Prompt result = prompt.apply(Map.of("name", "World"));`
    },
    { 
      id: '05', 
      title: 'Streaming', 
      description: 'Server-Sent Events with token-by-token streaming',
      code: `ai.llm().stream("...", "...", token -> {
    // Handle each token as it arrives
    System.out.print(token);
});`
    },
    { 
      id: '06', 
      title: 'Memory', 
      description: 'ChatMemory and ChatMemoryProvider',
      code: `ChatMemory memory = memoryProvider.getOrCreate(conversationId);
String response = ai.llm().ask(memory, "...", "...");`
    },
    { 
      id: '07', 
      title: 'Few-Shot Learning', 
      description: 'In-context learning with examples',
      code: `String examples = "Example 1: ...\\nExample 2: ...";
String prompt = examples + "\\n\\n" + userInput;
String response = ai.llm().ask("...", prompt);`
    },
    { 
      id: '08', 
      title: 'AI Services', 
      description: 'Declarative AI Service interfaces',
      code: `interface MyService {
    @SystemMessage("You are helpful")
    String answer(String question);
}
MyService service = ai.aiService(MyService.class);`
    },
    { 
      id: '09', 
      title: 'Persistent Memory', 
      description: 'Database-backed ChatMemory',
      code: `PersistentChatMemoryProvider provider = 
    new PersistentChatMemoryProvider(database);
ChatMemory memory = provider.getOrCreate(userId);`
    },
    { 
      id: '10', 
      title: 'Tools', 
      description: 'Function calling with @Tool annotations',
      code: `MyService service = ai.aiService(MyService.class, 
    builder -> builder.tools(new CalculatorTools()));`
    },
    { 
      id: '11', 
      title: 'Dynamic Tools', 
      description: 'Runtime tool selection',
      code: `List<ToolSpecification> tools = discoverTools();
MyService service = ai.aiService(MyService.class,
    builder -> builder.tools(tools));`
    },
    { 
      id: '12', 
      title: 'RAG with Documents', 
      description: 'Document indexing and RAG queries',
      code: `ai.vectors().indexPath("kb", Path.of("docs/"), 
    ChunkingOptions.medium());
RAGResponse response = ai.ragApi().ask("Question?",
    RAGOptions.builder().collection("kb").build());`
    }
  ];

  if (tutorialId) {
    const tutorial = tutorials.find(t => t.id === tutorialId);
    const code = signal(tutorial?.code || '');
    
    return (
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div class="mb-6">
          <a href="/tutorials" class="text-blue-600 hover:text-blue-700 mb-4 inline-block">
            ← Back to Tutorials
          </a>
          <h1 class="text-3xl font-bold mb-2">Tutorial {tutorialId}: {tutorial?.title}</h1>
          <p class="text-gray-600 mb-4">{tutorial?.description}</p>
        </div>
        
        <div class="bg-white rounded-lg shadow-lg overflow-hidden">
          <div class="bg-gray-800 px-4 py-2">
            <span class="text-white text-sm font-medium">Java</span>
          </div>
          <CodeEditor value={code} language="java" height="400px" />
        </div>
        
        <div class="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h3 class="font-semibold text-blue-900 mb-2">💡 Try It</h3>
          <p class="text-blue-800 text-sm">
            Copy this code to the <a href="/playground" class="underline font-semibold">Playground</a> to run it yourself!
          </p>
        </div>
      </div>
    );
  }

  return (
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <h1 class="text-3xl font-bold mb-2">Roya Tutorials</h1>
        <p class="text-gray-600">
          Showing feature parity with LangChain4j. All 12 tutorials working in Roya.
        </p>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {tutorials.map(tutorial => (
          <a
            href={`/tutorials/${tutorial.id}`}
            class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow block"
          >
            <div class="text-sm text-blue-600 mb-2 font-semibold">Tutorial {tutorial.id}</div>
            <h3 class="text-xl font-semibold mb-2">{tutorial.title}</h3>
            <p class="text-gray-600 mb-4">{tutorial.description}</p>
            <div class="text-sm text-gray-500 bg-gray-50 p-2 rounded font-mono text-xs overflow-hidden">
              {tutorial.code.split('\n')[0]}...
            </div>
          </a>
        ))}
      </div>
    </div>
  );
}
