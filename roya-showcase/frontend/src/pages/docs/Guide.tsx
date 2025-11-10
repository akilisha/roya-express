import { Link } from 'wouter';

export function Guide() {
  const guides = [
    {
      title: 'Writing Middleware',
      href: '/docs/guide/writing-middleware',
      description: 'Learn how to create custom middleware functions',
      highlight: 'Build reusable request processing logic'
    },
    {
      title: 'Using Middleware',
      href: '/docs/guide/using-middleware',
      description: 'Understand how to apply middleware in your application',
      highlight: 'Middleware execution order and patterns'
    },
    {
      title: 'Error Handling',
      href: '/docs/guide/error-handling',
      description: 'Handle errors gracefully in your Roya application',
      highlight: 'Centralized error handling and recovery'
    },
    {
      title: 'Database Integration',
      href: '/docs/guide/database',
      description: 'Integrate databases with JOOQ, transactions, and migrations',
      highlight: 'Type-safe database operations with Flyway'
    },
    {
      title: 'Database Internals',
      href: '/docs/guide/database-internals',
      description: 'Dive into connection pooling, migration workflow, and secrets precedence',
      highlight: 'Operational view of the database plugin'
    },
    {
      title: 'Plugins',
      href: '/docs/guide/plugins',
      description: 'Extend Roya with plugins and understand the plugin system',
      highlight: 'Modular architecture for extending functionality'
    },
    {
      title: 'Production Hardening',
      href: '/docs/guide/production',
      description: 'Deployment checklist, observability, and scaling strategies',
      highlight: 'Everything you need before hitting production'
    }
  ];
  
  return (
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-10">
        <Link href="/docs" class="text-roya-primary dark:text-roya-primary hover:underline mb-4 inline-block transition-colors">
          ← Documentation
        </Link>
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-primary dark:text-roya-primary tracking-tight">
          Guide
        </h1>
        <p class="text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          Learn core concepts and best practices for building applications with Roya.
        </p>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {guides.map(guide => (
          <Link
            href={guide.href}
            class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-6 hover:shadow-xl transition-all duration-200 border border-roya-border dark:border-roya-borderDark text-left group"
          >
            <h2 class="text-2xl font-bold mb-2 text-roya-text dark:text-roya-textDark group-hover:text-roya-primary dark:group-hover:text-roya-primary transition-colors">
              {guide.title}
            </h2>
            <p class="text-roya-textMuted dark:text-roya-textMutedDark mb-3 leading-relaxed">
              {guide.description}
            </p>
            <p class="text-sm text-roya-primary dark:text-roya-primary font-semibold">
              {guide.highlight}
            </p>
          </Link>
        ))}
      </div>
    </div>
  );
}

