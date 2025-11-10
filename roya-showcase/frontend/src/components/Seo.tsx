import { useEffect } from 'preact/hooks';

interface SeoProps {
  title?: string;
  description?: string;
  canonical?: string;
}

const DEFAULT_TITLE = 'Roya Framework';
const DEFAULT_DESCRIPTION =
  'Roya is an Express-inspired Java framework with first-class AI, workflow orchestration, and Helidon Níma performance.';
const DEFAULT_IMAGE = 'https://roya.dev/og-image.png';

function ensureMeta(selector: string, attributes: Record<string, string>) {
  let element = document.querySelector<HTMLMetaElement>(selector);
  if (!element) {
    element = document.createElement('meta');
    Object.entries(attributes).forEach(([key, value]) => element!.setAttribute(key, value));
    document.head.appendChild(element);
  }
  return element;
}

export function Seo({ title, description, canonical }: SeoProps) {
  useEffect(() => {
    const fullTitle = title ? `${title} | Roya Framework` : DEFAULT_TITLE;
    document.title = fullTitle;

    const desc = description || DEFAULT_DESCRIPTION;
    const canonicalUrl = canonical || window.location.href;

    const descriptionMeta = ensureMeta('meta[name="description"]', { name: 'description' });
    descriptionMeta.setAttribute('content', desc);

    const ogTitle = ensureMeta('meta[property="og:title"]', { property: 'og:title' });
    ogTitle.setAttribute('content', fullTitle);

    const ogDescription = ensureMeta('meta[property="og:description"]', { property: 'og:description' });
    ogDescription.setAttribute('content', desc);

    const ogUrl = ensureMeta('meta[property="og:url"]', { property: 'og:url' });
    ogUrl.setAttribute('content', canonicalUrl);

    const ogImage = ensureMeta('meta[property="og:image"]', { property: 'og:image' });
    ogImage.setAttribute('content', DEFAULT_IMAGE);

    const twitterTitle = ensureMeta('meta[name="twitter:title"]', { name: 'twitter:title' });
    twitterTitle.setAttribute('content', fullTitle);

    const twitterDescription = ensureMeta('meta[name="twitter:description"]', { name: 'twitter:description' });
    twitterDescription.setAttribute('content', desc);

    const twitterImage = ensureMeta('meta[name="twitter:image"]', { name: 'twitter:image' });
    twitterImage.setAttribute('content', DEFAULT_IMAGE);

    const canonicalLink =
      document.querySelector<HTMLLinkElement>('link[rel="canonical"]') ?? document.createElement('link');
    canonicalLink.setAttribute('rel', 'canonical');
    canonicalLink.setAttribute('href', canonicalUrl);
    if (!canonicalLink.parentElement) {
      document.head.appendChild(canonicalLink);
    }
  }, [title, description, canonical]);

  return null;
}

