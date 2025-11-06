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
      <div class="mb-8">
        <Link href="/docs" class="text-blue-600 hover:underline mb-4 inline-block">
          ← Documentation
        </Link>
        <h1 class="text-4xl font-bold mb-4">Migration Guides</h1>
        <p class="text-xl text-gray-600">
          Migrate to Roya from Express.js, Spring Boot, or Quarkus. We've made it easy.
        </p>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        {guides.map(guide => (
          <Link
            href={guide.href}
            class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow text-left"
          >
            <h2 class="text-2xl font-semibold mb-2 text-blue-600">{guide.title}</h2>
            <p class="text-gray-600 mb-3">{guide.description}</p>
            <p class="text-sm text-blue-600 font-semibold">{guide.highlight}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}

