import { useRoute, Link } from 'wouter';

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
    
    return (
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div class="mb-8">
          <Link href="/tutorials" class="text-roya-primary dark:text-roya-primary hover:text-roya-primaryDark dark:hover:text-roya-primaryDark mb-6 inline-flex items-center gap-2 font-medium transition-colors">
            <span>←</span>
            <span>Back to Tutorials</span>
          </Link>
          <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-text dark:text-roya-textDark tracking-tight font-sans">
            Tutorial {tutorialId}: {tutorial?.title}
          </h1>
          <p class="text-lg text-roya-textMuted dark:text-roya-textMutedDark mb-6 leading-relaxed">
            {tutorial?.description}
          </p>
        </div>
        
        <div class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-lg dark:shadow-soft-dark overflow-hidden border border-roya-border dark:border-roya-borderDark">
          <div class="bg-black dark:bg-black px-4 py-3 border-b border-roya-borderDark">
            <span class="text-roya-primary text-sm font-bold uppercase tracking-wide">Java</span>
          </div>
          <pre class="bg-black dark:bg-black text-roya-primary p-6 overflow-x-auto font-mono text-sm leading-relaxed"><code>{tutorial?.code}</code></pre>
        </div>
      </div>
    );
  }

  return (
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-10">
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-text dark:text-roya-textDark tracking-tight font-sans">
          Roya Tutorials
        </h1>
        <p class="text-lg md:text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          Showing feature parity with LangChain4j. All 12 tutorials working in Roya.
        </p>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {tutorials.map(tutorial => (
          <a
            href={`/tutorials/${tutorial.id}`}
            class="group bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark p-6 hover:shadow-lg dark:hover:shadow-glow-green transition-all duration-300 block border border-roya-border dark:border-roya-borderDark hover:border-roya-primary/50 dark:hover:border-roya-primary/50"
          >
            <div class="text-sm text-roya-primary dark:text-roya-primary mb-3 font-bold uppercase tracking-wide">
              Tutorial {tutorial.id}
            </div>
            <h3 class="text-xl font-bold mb-3 text-roya-text dark:text-roya-textDark group-hover:text-roya-primary dark:group-hover:text-roya-primary transition-colors">
              {tutorial.title}
            </h3>
            <p class="text-base text-roya-textMuted dark:text-roya-textMutedDark mb-4 leading-relaxed">
              {tutorial.description}
            </p>
            <div class="text-xs text-roya-textMuted dark:text-roya-textMutedDark bg-black dark:bg-black p-3 rounded-lg font-mono border border-roya-borderDark overflow-hidden">
              {tutorial.code.split('\n')[0]}...
            </div>
          </a>
        ))}
      </div>
    </div>
  );
}
