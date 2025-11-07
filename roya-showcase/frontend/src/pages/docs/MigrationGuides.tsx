import { Link, useLocation } from 'wouter';

export function MigrationGuides() {
  const [location] = useLocation();
  
  const guides = [
    {
      title: 'From Express.js',
      href: '/docs/migration/express',
      description: 'Express.js developers - transfer your skills to Java with the lowest barrier ever',
      highlight: 'If you know Express, you already know Roya'
    },
    {
      title: 'From Spring Boot',
      href: '/docs/migration/spring',
      description: 'Spring Boot developers looking for a power pack - find it in Roya',
      highlight: 'Express-style simplicity meets Java performance'
    },
    {
      title: 'From Quarkus',
      href: '/docs/migration/quarkus',
      description: 'Quarkus developers seeking modern Java web framework',
      highlight: 'Virtual threads, FFM, and Express-compatible API'
    }
  ];
  
  return (
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-10">
        <Link href="/docs" class="text-roya-primary dark:text-roya-primary hover:underline mb-4 inline-block transition-colors">
          ← Documentation
        </Link>
        <h1 class="text-4xl md:text-5xl font-bold mb-4 text-roya-primary dark:text-roya-primary tracking-tight">
          Migration Guides
        </h1>
        <p class="text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed">
          Migrate to Roya from Express.js, Spring Boot, or Quarkus. We've made it easy.
        </p>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        {guides.map(guide => (
          <Link
            href={guide.href}
            class="bg-roya-surface dark:bg-roya-surfaceDark rounded-xl shadow-lg p-6 hover:shadow-xl transition-all duration-200 border border-roya-border dark:border-roya-borderDark text-left group"
          >
            <h2 class="text-2xl font-bold mb-2 text-roya-text dark:text-roya-textDark group-hover:text-roya-primary dark:group-hover:text-roya-primary transition-colors">{guide.title}</h2>
            <p class="text-roya-textMuted dark:text-roya-textMutedDark mb-3 leading-relaxed">{guide.description}</p>
            <p class="text-sm text-roya-primary dark:text-roya-primary font-semibold">{guide.highlight}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}

