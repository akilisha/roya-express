import { Link, useLocation } from 'wouter';

export function Docs() {
  const [location] = useLocation();
  
  const sections = [
    {
      title: 'Getting Started',
      href: '/docs/getting-started',
      description: 'Install Roya and build your first app in minutes',
      subsections: [
        { title: 'Hello World', href: '/docs/getting-started/hello-world' },
        { title: 'Basic Routing', href: '/docs/getting-started/basic-routing' },
        { title: 'Static Files', href: '/docs/getting-started/static-files' },
        { title: 'Examples', href: '/docs/getting-started/examples' }
      ]
    },
    {
      title: 'Guide',
      href: '/docs/guide',
      description: 'Learn core concepts and best practices',
      subsections: [
        { title: 'Routing', href: '/docs/guide/routing' },
        { title: 'Writing Middleware', href: '/docs/guide/writing-middleware' },
        { title: 'Using Middleware', href: '/docs/guide/using-middleware' },
        { title: 'Error Handling', href: '/docs/guide/error-handling' },
        { title: 'Plugins', href: '/docs/guide/plugins' }
      ]
    },
    {
      title: 'API Reference',
      href: '/docs/api',
      description: 'Complete API documentation',
      subsections: [
        { title: 'Roya', href: '/docs/api/roya' },
        { title: 'Application', href: '/docs/api/application' },
        { title: 'Request', href: '/docs/api/request' },
        { title: 'Response', href: '/docs/api/response' },
        { title: 'Router', href: '/docs/api/router' }
      ]
    },
    {
      title: 'AI Integration',
      href: '/docs/ai',
      description: 'LLM, RAG, Agents, and Workflows',
      subsections: [
        { title: 'Quick Start', href: '/docs/ai/quick-start' },
        { title: 'LLM', href: '/docs/ai/llm' },
        { title: 'RAG', href: '/docs/ai/rag' },
        { title: 'Agents', href: '/docs/ai/agents' },
        { title: 'Workflows', href: '/docs/ai/workflows' }
      ]
    },
    {
      title: 'Migration Guides',
      href: '/docs/migration',
      description: 'Migrate from Express.js, Spring Boot, or Quarkus',
      subsections: [
        { title: 'From Express.js', href: '/docs/migration/express' },
        { title: 'From Spring Boot', href: '/docs/migration/spring' },
        { title: 'From Quarkus', href: '/docs/migration/quarkus' }
      ]
    }
  ];
  
  return (
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <h1 class="text-4xl font-bold mb-4">Documentation</h1>
        <p class="text-xl text-gray-600">
          Everything you need to build amazing applications with Roya.
        </p>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {sections.map(section => (
          <Link
            href={section.href}
            class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow text-left"
          >
            <h2 class="text-2xl font-semibold mb-2 text-blue-600">{section.title}</h2>
            <p class="text-gray-600 mb-4">{section.description}</p>
            <ul class="space-y-1">
              {section.subsections.map(sub => (
                <li class="text-sm text-gray-500">
                  → {sub.title}
                </li>
              ))}
            </ul>
          </Link>
        ))}
      </div>
    </div>
  );
}
