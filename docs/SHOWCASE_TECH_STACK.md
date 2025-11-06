# Roya Showcase - Technical Architecture

**Frontend Framework**: Preact + TypeScript + Vite  
**Why Preact**: Lightweight (3KB), fast, perfect for interactive demos

---

## Project Structure

```
roya-showcase/
├── frontend/                    # Preact web app
│   ├── src/
│   │   ├── components/         # Reusable Preact components
│   │   │   ├── CodeEditor.tsx   # Monaco Editor wrapper
│   │   │   ├── Playground.tsx  # Code playground component
│   │   │   ├── TutorialViewer.tsx # Tutorial viewer
│   │   │   ├── BenchmarkChart.tsx # Performance charts
│   │   │   └── WorkflowDesigner.tsx # Visual workflow builder
│   │   ├── pages/               # Page components
│   │   │   ├── Home.tsx
│   │   │   ├── Tutorials.tsx
│   │   │   ├── Examples.tsx
│   │   │   ├── Docs.tsx
│   │   │   └── Playground.tsx
│   │   ├── hooks/               # Preact hooks
│   │   │   ├── useCodeExecution.ts
│   │   │   ├── useWebSocket.ts
│   │   │   └── useTutorial.ts
│   │   ├── signals/             # Preact Signals (state)
│   │   │   ├── codeExecution.ts
│   │   │   └── tutorials.ts
│   │   ├── utils/               # Utilities
│   │   │   ├── api.ts           # API client
│   │   │   └── codeSnippets.ts  # Example code
│   │   └── app.tsx              # Main app component
│   ├── public/                  # Static assets
│   ├── vite.config.ts
│   └── package.json
│
├── backend/                     # Code execution service
│   ├── src/
│   │   ├── execution/           # Code execution engine
│   │   │   ├── DockerExecutor.java
│   │   │   └── CodeSandbox.java
│   │   ├── websocket/           # WebSocket handlers
│   │   └── api/                 # REST API
│   └── Dockerfile
│
├── docs/                        # Documentation site (Preact-powered)
│   ├── src/
│   │   ├── components/
│   │   ├── content/             # Markdown content
│   │   └── app.tsx
│   └── vite.config.ts
│
└── shared/                      # Shared types/utilities
    └── types.ts                 # TypeScript types shared between frontend/backend
```

---

## Key Components

### 1. Code Playground (`Playground.tsx`)

```typescript
import { signal } from '@preact/signals';
import { CodeEditor } from './CodeEditor';
import { useCodeExecution } from '../hooks/useCodeExecution';

export function Playground() {
  const code = signal(`// Write Roya code here
var app = Roya.create();
app.get("/", (req, res) => res.send("Hello!"));
app.listen(3000);`);
  
  const { execute, output, loading } = useCodeExecution();
  
  return (
    <div class="playground">
      <CodeEditor value={code} language="java" />
      <button onClick={() => execute(code.value)}>
        Run Code
      </button>
      <OutputPanel output={output} loading={loading} />
    </div>
  );
}
```

### 2. Tutorial Viewer (`TutorialViewer.tsx`)

```typescript
import { signal } from '@preact/signals';
import { Playground } from './Playground';

export function TutorialViewer({ tutorialId }: { tutorialId: string }) {
  const tutorial = signal(loadTutorial(tutorialId));
  
  return (
    <div class="tutorial-viewer">
      <TutorialContent tutorial={tutorial} />
      <Playground 
        initialCode={tutorial.value.code}
        description={tutorial.value.description}
      />
      <NextButton nextId={tutorial.value.next} />
    </div>
  );
}
```

### 3. Workflow Designer (`WorkflowDesigner.tsx`)

```typescript
import { signal } from '@preact/signals';
import { DraggableNode } from './DraggableNode';

export function WorkflowDesigner() {
  const nodes = signal([
    { id: 'webhook', type: 'trigger', x: 100, y: 100 },
    { id: 'rag', type: 'action', x: 300, y: 100 },
    { id: 'llm', type: 'action', x: 500, y: 100 }
  ]);
  
  return (
    <div class="workflow-designer">
      <Canvas>
        {nodes.value.map(node => (
          <DraggableNode 
            node={node} 
            onDrag={(x, y) => updateNodePosition(node.id, x, y)}
          />
        ))}
      </Canvas>
      <CodePreview nodes={nodes} />
    </div>
  );
}
```

### 4. Benchmark Dashboard (`BenchmarkChart.tsx`)

```typescript
import { signal } from '@preact/signals';
import { Chart } from 'chart.js';

export function BenchmarkChart() {
  const data = signal(loadBenchmarkData());
  
  useEffect(() => {
    const chart = new Chart('benchmark-canvas', {
      type: 'bar',
      data: {
        labels: ['Roya', 'Spring Boot', 'Express'],
        datasets: [{
          label: 'Requests/sec',
          data: [50000, 10000, 10000]
        }]
      }
    });
    
    return () => chart.destroy();
  }, []);
  
  return <canvas id="benchmark-canvas" />;
}
```

---

## Tech Stack Details

### Frontend Stack
- **Preact**: 3KB framework, React-compatible API
- **Preact Signals**: Reactive state management (no context needed)
- **Vite**: Lightning-fast dev server, optimized builds
- **TypeScript**: Type safety throughout
- **Tailwind CSS**: Utility-first styling
- **Monaco Editor**: VS Code editor in browser
- **Wouter**: Lightweight router (1KB)
- **Chart.js**: Performance visualizations

### Backend Stack
- **Roya Framework**: Serve the execution API
- **Docker**: Safe code execution containers
- **WebSocket**: Real-time execution updates
- **Java 21**: Backend execution engine

### Deployment
- **Frontend**: Vercel or Netlify (static hosting)
- **Backend**: Fly.io or Railway (Docker container support)
- **Execution**: Separate service with Docker-in-Docker

---

## Key Features Implementation

### Live Code Execution

1. **User types code** → Monaco Editor
2. **Clicks "Run"** → Send to backend via WebSocket
3. **Backend** → Create Docker container, compile & run
4. **Stream output** → WebSocket back to frontend
5. **Display results** → Real-time in output panel

### Tutorial System

1. **Load tutorial** → JSON/YAML format
2. **Display content** → Markdown rendering
3. **Pre-fill code** → Monaco Editor
4. **Execute on-demand** → User clicks "Run"
5. **Show solution** → Optional "Show Solution" button

### Workflow Designer

1. **Drag nodes** → Preact drag handlers
2. **Connect nodes** → SVG paths between nodes
3. **Generate code** → Convert visual graph to Roya workflow code
4. **Preview/preview** → Show generated code
5. **Export** → Download as Java file

---

## Performance Optimizations

- **Code splitting**: Lazy load tutorials/examples
- **Virtual scrolling**: For long tutorial lists
- **Memoization**: Preact memo for expensive components
- **Signal updates**: Only re-render when signals change
- **CDN**: Static assets on CDN
- **Service Worker**: Cache tutorials/content

---

## Development Workflow

1. **Local dev**: `vite dev` → Hot reload, fast iteration
2. **Build**: `vite build` → Optimized production bundle
3. **Preview**: Test production build locally
4. **Deploy**: Push to Vercel → Auto-deploy

---

## Next Steps

1. ✅ Set up Preact + Vite project
2. ✅ Create basic layout and routing
3. ✅ Implement Monaco Editor integration
4. ✅ Build code execution backend
5. ✅ Add WebSocket real-time updates
6. ✅ Create tutorial viewer component
7. ✅ Build workflow designer
8. ✅ Add performance benchmarks

**Ready to start building!** 🚀

