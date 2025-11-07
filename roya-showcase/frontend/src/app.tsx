import { Route, Router } from 'wouter';
import { Home } from './pages/Home';
import { Tutorials } from './pages/Tutorials';
import { Examples } from './pages/Examples';
import { Docs } from './pages/Docs';
import { Architecture } from './pages/Architecture';
import { Header } from './components/Header';
import { Footer } from './components/Footer';
import { AnalyticsTracker } from './components/AnalyticsTracker';

// Getting Started
import { GettingStarted } from './pages/docs/GettingStarted';
import { HelloWorld } from './pages/docs/HelloWorld';
import { BasicRouting } from './pages/docs/BasicRouting';
import { StaticFiles } from './pages/docs/StaticFiles';
import { GettingStartedExamples } from './pages/docs/GettingStartedExamples';

// API Reference
import { ApiReference } from './pages/docs/ApiReference';
import { RoyaApi } from './pages/docs/RoyaApi';
import { ApplicationApi } from './pages/docs/ApplicationApi';
import { RequestApi } from './pages/docs/RequestApi';
import { ResponseApi } from './pages/docs/ResponseApi';
import { RouterApi } from './pages/docs/RouterApi';
import { AIApi } from './pages/docs/AIApi';
import { RoyaPluginApi } from './pages/docs/RoyaPluginApi';

// AI Integration
import { AIIntegration } from './pages/docs/AIIntegration';
import { AIQuickStart } from './pages/docs/AIQuickStart';
import { LLMDocs } from './pages/docs/LLMDocs';
import { RAGDocs } from './pages/docs/RAGDocs';
import { AgentsDocs } from './pages/docs/AgentsDocs';
import { WorkflowsDocs } from './pages/docs/WorkflowsDocs';

// Guide
import { Guide } from './pages/docs/Guide';
import { MiddlewareGuide } from './pages/docs/MiddlewareGuide';
import { PluginsGuide } from './pages/docs/PluginsGuide';
import { DatabaseGuide } from './pages/docs/DatabaseGuide';
import { ErrorHandlingGuide } from './pages/docs/ErrorHandlingGuide';
import { UsingMiddlewareGuide } from './pages/docs/UsingMiddlewareGuide';

// Migration Guides
import { MigrationGuides } from './pages/docs/MigrationGuides';
import { ExpressMigration } from './pages/docs/ExpressMigration';
import { SpringMigration } from './pages/docs/SpringMigration';
import { QuarkusMigration } from './pages/docs/QuarkusMigration';

export function App() {
  return (
    <div class="min-h-screen flex flex-col bg-roya-surface dark:bg-roya-bgDark transition-colors duration-300">
      <Header />
      <main class="flex-1">
        <Router>
          <AnalyticsTracker />
          <Route path="/" component={Home} />
          <Route path="/tutorials" component={Tutorials} />
          <Route path="/tutorials/:id" component={Tutorials} />
          <Route path="/examples" component={Examples} />
          <Route path="/examples/:id" component={Examples} />
          <Route path="/architecture" component={Architecture} />
          
          {/* Documentation */}
          <Route path="/docs" component={Docs} />
          
          {/* Getting Started */}
          <Route path="/docs/getting-started" component={GettingStarted} />
          <Route path="/docs/getting-started/hello-world" component={HelloWorld} />
          <Route path="/docs/getting-started/basic-routing" component={BasicRouting} />
          <Route path="/docs/getting-started/static-files" component={StaticFiles} />
          <Route path="/docs/getting-started/examples" component={GettingStartedExamples} />
          
          {/* API Reference */}
          <Route path="/docs/api" component={ApiReference} />
          <Route path="/docs/api/roya" component={RoyaApi} />
          <Route path="/docs/api/application" component={ApplicationApi} />
          <Route path="/docs/api/request" component={RequestApi} />
          <Route path="/docs/api/response" component={ResponseApi} />
          <Route path="/docs/api/router" component={RouterApi} />
          <Route path="/docs/api/ai" component={AIApi} />
          <Route path="/docs/api/roya-plugin" component={RoyaPluginApi} />
          
          {/* AI Integration */}
          <Route path="/docs/ai" component={AIIntegration} />
          <Route path="/docs/ai/quick-start" component={AIQuickStart} />
          <Route path="/docs/ai/llm" component={LLMDocs} />
          <Route path="/docs/ai/rag" component={RAGDocs} />
          <Route path="/docs/ai/agents" component={AgentsDocs} />
          <Route path="/docs/ai/workflows" component={WorkflowsDocs} />
          
          {/* Guide */}
          <Route path="/docs/guide" component={Guide} />
          <Route path="/docs/guide/writing-middleware" component={MiddlewareGuide} />
          <Route path="/docs/guide/using-middleware" component={UsingMiddlewareGuide} />
          <Route path="/docs/guide/error-handling" component={ErrorHandlingGuide} />
          <Route path="/docs/guide/database" component={DatabaseGuide} />
          <Route path="/docs/guide/plugins" component={PluginsGuide} />
          
          {/* Migration Guides */}
          <Route path="/docs/migration" component={MigrationGuides} />
          <Route path="/docs/migration/express" component={ExpressMigration} />
          <Route path="/docs/migration/spring" component={SpringMigration} />
          <Route path="/docs/migration/quarkus" component={QuarkusMigration} />
        </Router>
      </main>
      <Footer />
    </div>
  );
}

