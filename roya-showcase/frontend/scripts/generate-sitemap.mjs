import { mkdirSync, writeFileSync } from 'node:fs';
import { resolve } from 'node:path';

const BASE_URL = process.env.SITEMAP_BASE_URL ?? 'https://roya.dev';

const routes = [
  '/',
  '/tutorials',
  '/examples',
  '/architecture',
  '/docs',
  '/docs/getting-started',
  '/docs/getting-started/hello-world',
  '/docs/getting-started/basic-routing',
  '/docs/getting-started/static-files',
  '/docs/getting-started/examples',
  '/docs/api',
  '/docs/api/roya',
  '/docs/api/application',
  '/docs/api/request',
  '/docs/api/response',
  '/docs/api/router',
  '/docs/api/ai',
  '/docs/api/roya-plugin',
  '/docs/ai',
  '/docs/ai/quick-start',
  '/docs/ai/llm',
  '/docs/ai/rag',
  '/docs/ai/agents',
  '/docs/ai/workflows',
  '/docs/guide',
  '/docs/guide/writing-middleware',
  '/docs/guide/using-middleware',
  '/docs/guide/error-handling',
  '/docs/guide/database',
  '/docs/guide/database-internals',
  '/docs/guide/plugins',
  '/docs/guide/production',
  '/docs/migration',
  '/docs/migration/express',
  '/docs/migration/spring',
  '/docs/migration/quarkus'
];

const lastmod = new Date().toISOString();

const urlEntries = routes
  .map(
    (route) => `  <url>
    <loc>${new URL(route.replace(/^\//, ''), BASE_URL + '/').href.replace(/\/+$/, '')}</loc>
    <lastmod>${lastmod}</lastmod>
    <changefreq>weekly</changefreq>
    <priority>${route === '/' ? '1.0' : '0.7'}</priority>
  </url>`
  )
  .join('\n');

const xml = `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
${urlEntries}
</urlset>
`;

const publicDir = resolve(process.cwd(), 'public');
mkdirSync(publicDir, { recursive: true });

const sitemapPath = resolve(publicDir, 'sitemap.xml');
writeFileSync(sitemapPath, xml.trim() + '\n', 'utf8');

console.info(`Sitemap generated with ${routes.length} routes at ${sitemapPath}`);


