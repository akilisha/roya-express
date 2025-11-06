import { Link } from 'wouter';

export function AgentsDocs() {
  return (
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs/ai" class="text-blue-600 hover:underline mb-4 inline-block">
          ← AI Integration
        </Link>
        <h1 class="text-4xl font-bold mb-4">AI Agents</h1>
        <p class="text-xl text-gray-600">
          Build AI agents that can use tools, access external APIs, and make decisions autonomously.
        </p>
      </div>
      
      <div class="space-y-8">
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">What are AI Agents?</h2>
          <p class="text-gray-700 mb-4">
            AI Agents are LLMs that can:
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700">
            <li>Use tools (functions) to interact with external systems</li>
            <li>Make decisions based on tool outputs</li>
            <li>Maintain conversation context</li>
            <li>Chain multiple tool calls together</li>
          </ul>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">AI Services (Declarative Agents)</h2>
          <p class="text-gray-700 mb-4">
            The easiest way to create agents is using AI Services - declarative interfaces:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.MemoryId;

interface Assistant {
    @SystemMessage("You are a helpful assistant with access to tools.")
    String chat(@MemoryId String conversationId, 
                @UserMessage String userMessage);
}

// Create agent with tools
Assistant assistant = ai.aiService(Assistant.class, builder -> 
    builder.tools(new CalculatorTools(), new WeatherTools())
);

// Use the agent
String response = assistant.chat("conv-123", "What's 42 * 7?");`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Defining Tools</h2>
          <p class="text-gray-700 mb-4">
            Tools are Java methods annotated with <code class="bg-gray-100 px-2 py-1 rounded">@Tool</code>:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import dev.langchain4j.agent.tool.Tool;

public class CalculatorTools {
    @Tool("Multiplies two numbers")
    public double multiply(double a, double b) {
        return a * b;
    }
    
    @Tool("Adds two numbers")
    public double add(double a, double b) {
        return a + b;
    }
}

public class WeatherTools {
    @Tool("Gets the current weather for a location")
    public String getWeather(String location) {
        // Call weather API
        return "Sunny, 72°F";
    }
}`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">MCP (Model Context Protocol)</h2>
          <p class="text-gray-700 mb-4">
            Integrate external tools via MCP:
          </p>
          
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`import com.akilisha.oss.roya.plugins.ai.mcp.MCPClient;

// Discover MCP tools
MCPClient mcpClient = MCPClient.builder()
    .server("github", "https://mcp-server.example.com")
    .build();

// Use in AI Service
Assistant assistant = ai.aiService(Assistant.class, builder ->
    builder.toolProvider(mcpClient.getToolProvider())
);`}</code></pre>
        </section>
        
        <section class="bg-white rounded-lg shadow-md p-8">
          <h2 class="text-2xl font-semibold mb-4">Complete Example</h2>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto"><code>{`interface CustomerSupportAgent {
    @SystemMessage("You are a customer support agent. " +
                   "Use tools to help customers.")
    String help(@MemoryId String userId, 
                @UserMessage String question);
}

app.post("/support", (req, res, next) -> {
    Map<String, Object> body = req.body(Map.class);
    String userId = (String) body.get("userId");
    String question = (String) body.get("question");
    
    CustomerSupportAgent agent = ai.aiService(
        CustomerSupportAgent.class,
        builder -> builder
            .tools(new OrderTools(), new AccountTools())
            .chatMemory(memoryProvider.getOrCreate(userId))
    );
    
    String response = agent.help(userId, question);
    res.json(Map.of("response", response));
});`}</code></pre>
        </section>
        
        <div class="flex justify-between pt-8">
          <Link href="/docs/ai/rag" class="text-blue-600 hover:underline">
            ← RAG
          </Link>
          <Link href="/docs/ai/workflows" class="text-blue-600 hover:underline">
            Workflows →
          </Link>
        </div>
      </div>
    </div>
  );
}

