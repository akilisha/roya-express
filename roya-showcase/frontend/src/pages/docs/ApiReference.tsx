import { Link } from 'wouter';

export function ApiReference() {
  const sections = [
    {
      title: 'Roya',
      href: '/docs/api/roya',
      description: 'Top-level function to create an Express application'
    },
    {
      title: 'Application',
      href: '/docs/api/application',
      description: 'The app object conventionally denotes the Express application'
    },
    {
      title: 'Request',
      href: '/docs/api/request',
      description: 'The req object represents the HTTP request'
    },
    {
      title: 'Response',
      href: '/docs/api/response',
      description: 'The res object represents the HTTP response'
    },
    {
      title: 'Router',
      href: '/docs/api/router',
      description: 'A router object is an isolated instance of middleware and routes'
    },
    {
      title: 'AI',
      href: '/docs/api/ai',
      description: 'First-class AI/LLM integration - the primary basis of differentiation'
    },
    {
      title: 'RoyaPlugin',
      href: '/docs/api/roya-plugin',
      description: 'Plugin interface for extending Roya functionality'
    }
  ];
  
  return (
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs" class="text-roya-primary dark:text-roya-primary hover:underline mb-4 inline-block transition-colors">
          ← Documentation
        </Link>
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-text dark:text-roya-textDark tracking-tight">API Reference</h1>
        <p class="text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          Complete API documentation for Roya Framework. If you know Express.js, this will look familiar.
        </p>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        {sections.map(section => (
          <Link
            href={section.href}
            class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-6 hover:shadow-xl transition-all duration-200 border border-roya-border dark:border-roya-borderDark text-left group"
          >
            <h2 class={`text-2xl font-bold mb-2 ${section.title === 'AI' ? 'text-roya-primary dark:text-roya-primary' : 'text-roya-text dark:text-roya-textDark group-hover:text-roya-primary dark:group-hover:text-roya-primary transition-colors'}`}>
              {section.title}
              {section.title === 'AI' && <span class="ml-2 text-sm bg-roya-primary/20 dark:bg-roya-primary/30 px-2 py-1 rounded">Primary Differentiator</span>}
            </h2>
            <p class="text-roya-textMuted dark:text-roya-textMutedDark">{section.description}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}

