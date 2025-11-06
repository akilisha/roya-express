import { Route, Router } from 'wouter';
import { Home } from './pages/Home';
import { Tutorials } from './pages/Tutorials';
import { Examples } from './pages/Examples';
import { Playground } from './pages/Playground';
import { Docs } from './pages/Docs';
import { Architecture } from './pages/Architecture';
import { Header } from './components/Header';
import { Footer } from './components/Footer';

export function App() {
  return (
    <div class="min-h-screen flex flex-col bg-gray-50">
      <Header />
      <main class="flex-1">
        <Router>
          <Route path="/" component={Home} />
          <Route path="/tutorials" component={Tutorials} />
          <Route path="/tutorials/:id" component={Tutorials} />
          <Route path="/examples" component={Examples} />
          <Route path="/playground" component={Playground} />
          <Route path="/docs" component={Docs} />
          <Route path="/docs/*" component={Docs} />
          <Route path="/architecture" component={Architecture} />
        </Router>
      </main>
      <Footer />
    </div>
  );
}

