import { signal } from '@preact/signals';
import { useEffect } from 'preact/hooks';
import { CodeEditor } from '../components/CodeEditor';
import { useCodeExecution } from '../hooks/useCodeExecution';

export function Playground() {
  const code = signal(`import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;

public class HelloRoya {
    public static void main(String[] args) {
        var app = Roya.create();
        
        // Register AI plugin
        var aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        aiPlugin.start();
        
        AI ai = app.services().get(AI.class);
        
        // Simple AI endpoint
        app.post("/ask", (req, res, next) -> {
            @SuppressWarnings("unchecked")
            var body = req.body(java.util.Map.class);
            String question = (String) body.get("question");
            
            String answer = ai.llm().ask(
                "You are a helpful assistant.",
                question
            );
            
            res.json(java.util.Map.of("answer", answer));
        });
        
        app.listen(3000);
        System.out.println("🚀 Server running on http://localhost:3000");
    }
}`);

  const { execute, output, loading, error } = useCodeExecution();

  const handleRun = () => {
    execute(code.value);
  };

  const handleShare = () => {
    const encoded = encodeURIComponent(code.value);
    const url = `${window.location.origin}/playground?code=${encoded}`;
    navigator.clipboard.writeText(url);
    alert('Shareable link copied to clipboard!');
  };

  const quickExamples = [
    {
      title: 'Hello World',
      code: `var app = Roya.create();
app.get("/", (req, res) -> res.send("Hello, Roya!"));
app.listen(3000);`
    },
    {
      title: 'AI Chat',
      code: `var app = Roya.create();
AI ai = app.services().get(AI.class);
app.post("/ask", (req, res) -> {
    String answer = ai.llm().ask("You are helpful.", req.body(String.class));
    res.json(Map.of("answer", answer));
});
app.listen(3000);`
    },
    {
      title: 'Structured Extraction',
      code: `var app = Roya.create();
AI ai = app.services().get(AI.class);
app.post("/extract", (req, res) -> {
    record Product(String name, Double price) {}
    Product product = ai.llm().extract(Product.class, req.body(String.class));
    res.json(product);
});
app.listen(3000);`
    }
  ];

  return (
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <h1 class="text-3xl font-bold mb-2">Roya Playground</h1>
        <p class="text-gray-600">
          Write Roya code and see it run instantly. Perfect for learning and testing.
        </p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Code Editor */}
        <div class="bg-white rounded-lg shadow-lg overflow-hidden">
          <div class="bg-gray-800 px-4 py-2 flex items-center justify-between">
            <span class="text-white text-sm font-medium">Java</span>
            <div class="flex gap-2">
              <button
                onClick={handleShare}
                class="text-gray-300 hover:text-white text-sm px-2 py-1 rounded"
              >
                Share
              </button>
            </div>
          </div>
          <CodeEditor value={code} language="java" height="500px" />
        </div>

        {/* Output Panel */}
        <div class="bg-white rounded-lg shadow-lg overflow-hidden">
          <div class="bg-gray-800 px-4 py-2 flex items-center justify-between">
            <span class="text-white text-sm font-medium">Output</span>
            <button
              onClick={handleRun}
              disabled={loading.value}
              class={`px-4 py-1 rounded text-sm font-medium ${
                loading.value
                  ? 'bg-gray-600 text-gray-300 cursor-not-allowed'
                  : 'bg-blue-600 text-white hover:bg-blue-700'
              }`}
            >
              {loading.value ? 'Running...' : '▶ Run Code'}
            </button>
          </div>
          <div class="p-4 bg-gray-900 text-green-400 font-mono text-sm min-h-[500px] whitespace-pre-wrap">
            {error.value ? (
              <div class="text-red-400">{error.value}</div>
            ) : output.value ? (
              output.value
            ) : (
              <div class="text-gray-500">
                Click "Run Code" to execute your Roya application...
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Quick Examples */}
      <div class="mt-8">
        <h2 class="text-xl font-semibold mb-4">Quick Examples</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          {quickExamples.map(example => (
            <button
              onClick={() => code.value = example.code}
              class="bg-white rounded-lg shadow p-4 text-left hover:shadow-md transition-shadow"
            >
              <h3 class="font-semibold mb-1">{example.title}</h3>
              <p class="text-sm text-gray-600">Click to load example</p>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
