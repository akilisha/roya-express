import { Link, useLocation } from 'wouter';

export function ApiReference() {
  const [location] = useLocation();
  
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
    }
  ];
  
  return (
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="mb-8">
        <Link href="/docs" class="text-blue-600 hover:underline mb-4 inline-block">
          ← Documentation
        </Link>
        <h1 class="text-4xl font-bold mb-4">API Reference</h1>
        <p class="text-xl text-gray-600">
          Complete API documentation for Roya Framework. If you know Express.js, this will look familiar.
        </p>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        {sections.map(section => (
          <Link
            href={section.href}
            class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow text-left"
          >
            <h2 class="text-2xl font-semibold mb-2 text-blue-600">{section.title}</h2>
            <p class="text-gray-600">{section.description}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}

