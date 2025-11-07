import { Link, useLocation } from 'wouter';
import { useState } from 'preact/hooks';

export function Docs() {
  const [location] = useLocation();
  const [searchQuery, setSearchQuery] = useState('');
  
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
        { title: 'Database', href: '/docs/guide/database' },
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
  
  // Filter sections based on search query
  const filteredSections = sections.filter(section => {
    if (!searchQuery) return true;
    const query = searchQuery.toLowerCase();
    return (
      section.title.toLowerCase().includes(query) ||
      section.description.toLowerCase().includes(query) ||
      section.subsections.some(sub => sub.title.toLowerCase().includes(query))
    );
  });
  
  return (
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-10">
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-text dark:text-roya-textDark tracking-tight">Documentation</h1>
        <p class="text-lg md:text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          Everything you need to build amazing applications with Roya.
        </p>
        
        {/* Search Bar */}
        <div class="mt-8">
          <div class="relative">
            <input
              type="text"
              placeholder="Search documentation..."
              value={searchQuery}
              onInput={(e) => setSearchQuery(e.currentTarget.value)}
              class="w-full px-4 py-4 pl-12 border-2 border-roya-border dark:border-roya-borderDark rounded-xl bg-roya-bg dark:bg-roya-surfaceDark text-roya-text dark:text-roya-textDark placeholder-roya-textMuted dark:placeholder-roya-textMutedDark focus:outline-none focus:ring-2 focus:ring-roya-primary dark:focus:ring-roya-primary focus:border-roya-primary dark:focus:border-roya-primary transition-all duration-200 shadow-soft dark:shadow-soft-dark"
            />
            <svg
              class="absolute left-4 top-4 w-5 h-5 text-roya-textMuted dark:text-roya-textMutedDark"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
        </div>
      </div>
      
      {filteredSections.length === 0 ? (
        <div class="text-center py-16">
          <p class="text-roya-textMuted dark:text-roya-textMutedDark text-lg md:text-xl">
            No documentation found for "{searchQuery}"
          </p>
        </div>
      ) : (
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredSections.map(section => (
            <Link
              href={section.href}
              class="group bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark p-6 hover:shadow-lg dark:hover:shadow-glow-green transition-all duration-300 text-left border border-roya-border dark:border-roya-borderDark hover:border-roya-primary/50 dark:hover:border-roya-primary/50"
            >
              <h2 class="text-2xl font-bold mb-3 text-roya-text dark:text-roya-textDark group-hover:text-roya-primary dark:group-hover:text-roya-primary transition-colors">
                {section.title}
              </h2>
              <p class="text-base text-roya-textMuted dark:text-roya-textMutedDark mb-4 leading-relaxed">
                {section.description}
              </p>
              <ul class="space-y-2">
                {section.subsections.map(sub => (
                  <li class="text-sm text-roya-textMuted dark:text-roya-textMutedDark flex items-center gap-2">
                    <span class="text-roya-primary">→</span>
                    <span>{sub.title}</span>
                  </li>
                ))}
              </ul>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
