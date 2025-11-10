import { Link } from 'wouter';
import { useState } from 'preact/hooks';
import { Seo } from '../components/Seo';

const docsIndex = [
  {
    title: 'Documentation Overview',
    description: 'Landing page for Roya documentation and guides',
    href: '/docs',
    category: 'Overview'
  },
  // Getting Started
  { title: 'Hello World', description: 'Build your first Roya application', href: '/docs/getting-started/hello-world', category: 'Getting Started' },
  { title: 'Basic Routing', description: 'Define routes the Express way in Java', href: '/docs/getting-started/basic-routing', category: 'Getting Started' },
  { title: 'Static Files', description: 'Serve assets and static content', href: '/docs/getting-started/static-files', category: 'Getting Started' },
  { title: 'Getting Started Examples', description: 'Real-world starter examples for Roya', href: '/docs/getting-started/examples', category: 'Getting Started' },
  // API Reference
  { title: 'Roya API', description: 'Create and configure Roya applications', href: '/docs/api/roya', category: 'API Reference' },
  { title: 'Application Interface', description: 'Express-compatible application methods', href: '/docs/api/application', category: 'API Reference' },
  { title: 'Request Interface', description: 'HTTP request helpers and accessors', href: '/docs/api/request', category: 'API Reference' },
  { title: 'Response Interface', description: 'Send responses, headers, and JSON', href: '/docs/api/response', category: 'API Reference' },
  { title: 'Router Interface', description: 'Nested routers and route mounting', href: '/docs/api/router', category: 'API Reference' },
  { title: 'AI Interface', description: 'First-class AI/LLM integration in Roya', href: '/docs/api/ai', category: 'API Reference' },
  { title: 'RoyaPlugin Interface', description: 'Extend Roya with plugins, services, and lifecycle hooks', href: '/docs/api/roya-plugin', category: 'API Reference' },
  { title: 'Aggregated Javadoc', description: 'Generated API reference hosted inside the docs site', href: '/javadoc/index.html', category: 'API Reference' },
  // Guide
  { title: 'Guide: Writing Middleware', description: 'Create custom middleware in Roya', href: '/docs/guide/writing-middleware', category: 'Guide' },
  { title: 'Guide: Using Middleware', description: 'Compose middleware stacks effectively', href: '/docs/guide/using-middleware', category: 'Guide' },
  { title: 'Guide: Error Handling', description: 'Handling exceptions and error middleware', href: '/docs/guide/error-handling', category: 'Guide' },
  { title: 'Guide: Database Integration', description: 'Work with the database plugin and JOOQ', href: '/docs/guide/database', category: 'Guide' },
  { title: 'Guide: Database Internals', description: 'Understand pooling, migrations, and secrets precedence', href: '/docs/guide/database-internals', category: 'Guide' },
  { title: 'Guide: Plugins', description: 'Understand plugin architecture and usage', href: '/docs/guide/plugins', category: 'Guide' },
  { title: 'Guide: Production Hardening', description: 'Deployment checklist, observability, and scaling guidance', href: '/docs/guide/production', category: 'Guide' },
  // AI Integration
  { title: 'AI Quick Start', description: 'Add AI to your Roya app in minutes', href: '/docs/ai/quick-start', category: 'AI Integration' },
  { title: 'AI LLM', description: 'Ask, extract, and stream with LLMs', href: '/docs/ai/llm', category: 'AI Integration' },
  { title: 'AI RAG', description: 'Retrieval-Augmented Generation APIs', href: '/docs/ai/rag', category: 'AI Integration' },
  { title: 'AI Agents', description: 'Tool-enabled agents and LangGraph integration', href: '/docs/ai/agents', category: 'AI Integration' },
  { title: 'AI Workflows', description: 'Multi-step AI workflow orchestration', href: '/docs/ai/workflows', category: 'AI Integration' },
  // Migration Guides
  { title: 'Migrate from Express.js', description: 'Bring Express apps to Roya', href: '/docs/migration/express', category: 'Migration Guides' },
  { title: 'Migrate from Spring Boot', description: 'Move Spring workloads onto Roya', href: '/docs/migration/spring', category: 'Migration Guides' },
  { title: 'Migrate from Quarkus', description: 'Adopt Roya from Quarkus projects', href: '/docs/migration/quarkus', category: 'Migration Guides' }
];

const escapeRegExp = (value: string) => value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

export function Docs() {
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
        { title: 'Database Integration', href: '/docs/guide/database' },
        { title: 'Database Internals', href: '/docs/guide/database-internals' },
        { title: 'Plugins', href: '/docs/guide/plugins' },
        { title: 'Production Hardening', href: '/docs/guide/production' }
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
        { title: 'Router', href: '/docs/api/router' },
        { title: 'RoyaPlugin', href: '/docs/api/roya-plugin' },
        { title: 'Full Javadoc', href: '/javadoc/index.html' }
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
  
  const query = searchQuery.trim().toLowerCase();

  // Filter sections based on search query
  const filteredSections = sections.filter(section => {
    if (!query) return true;
    return (
      section.title.toLowerCase().includes(query) ||
      section.description.toLowerCase().includes(query) ||
      section.subsections.some(sub => sub.title.toLowerCase().includes(query))
    );
  });
  
  const searchResults = query
    ? docsIndex.filter(item =>
        item.title.toLowerCase().includes(query) ||
        item.description.toLowerCase().includes(query) ||
        item.category.toLowerCase().includes(query)
      ).slice(0, 10)
    : [];
  
  const highlight = (text: string) => {
    if (!query) return text;
    const regex = new RegExp(`(${escapeRegExp(query)})`, 'gi');
    const parts = text.split(regex);
    return (
      <>
        {parts.map((part, idx) => (
          idx % 2 === 1 ? (
            <span
              key={`${part}-${idx}`}
              class="bg-roya-primary/20 dark:bg-roya-primary/30 text-roya-primary dark:text-roya-primary px-1 rounded"
            >
              {part}
            </span>
          ) : (
            <span key={idx}>{part}</span>
          )
        ))}
      </>
    );
  };
  
  return (
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <Seo
        title="Documentation"
        description="Explore Roya's getting started guides, API reference, AI integration walkthroughs, and migration guides."
      />
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
        
        {query && (
          <div class="mt-6 bg-roya-bg dark:bg-roya-surfaceDark border border-roya-border dark:border-roya-borderDark rounded-xl shadow-soft dark:shadow-soft-dark">
            {searchResults.length === 0 ? (
              <div class="p-6 text-roya-textMuted dark:text-roya-textMutedDark">
                No documentation matches "{searchQuery}"
              </div>
            ) : (
              <ul class="divide-y divide-roya-border dark:divide-roya-borderDark">
                {searchResults.map(result => (
                  <li key={result.href}>
                    <Link
                      href={result.href}
                      class="block p-4 hover:bg-roya-surface dark:hover:bg-roya-surfaceDark transition-colors duration-150"
                    >
                      <p class="text-sm uppercase tracking-wide text-roya-textMuted dark:text-roya-textMutedDark mb-1">
                        {result.category}
                      </p>
                      <p class="text-lg font-semibold text-roya-text dark:text-roya-textDark">
                        {highlight(result.title)}
                      </p>
                      <p class="text-sm text-roya-textMuted dark:text-roya-textMutedDark mt-1">
                        {highlight(result.description)}
                      </p>
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}
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
