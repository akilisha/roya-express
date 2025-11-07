/**
 * Google Analytics integration for Roya Showcase
 *
 * Tracks page views and custom events.
 * Set VITE_GA_MEASUREMENT_ID environment variable to enable.
 */

declare global {
  interface ImportMetaEnv {
    readonly VITE_GA_MEASUREMENT_ID?: string;
  }

  interface ImportMeta {
    readonly env: ImportMetaEnv;
  }

  interface Window {
    gtag?: (
      command: 'config' | 'event' | 'set' | 'js',
      targetId: string | object,
      config?: object
    ) => void;
    dataLayer?: any[];
  }
}

const GA_MEASUREMENT_ID = import.meta.env?.VITE_GA_MEASUREMENT_ID;

/**
 * Initialize Google Analytics
 */
export function initAnalytics() {
  if (!GA_MEASUREMENT_ID) {
    console.log('Google Analytics not configured (VITE_GA_MEASUREMENT_ID not set)');
    return;
  }

  // Initialize dataLayer
  const dataLayer = (window.dataLayer = window.dataLayer || []);
  window.gtag = function (...args) {
    dataLayer.push(args);
  };
  window.gtag('js', new Date());
  window.gtag('config', GA_MEASUREMENT_ID, {
    page_path: window.location.pathname,
  });

  // Load Google Analytics script
  const script = document.createElement('script');
  script.async = true;
  script.src = `https://www.googletagmanager.com/gtag/js?id=${GA_MEASUREMENT_ID}`;
  document.head.appendChild(script);

  console.log('Google Analytics initialized');
}

/**
 * Track a page view
 */
export function trackPageView(path: string) {
  if (!GA_MEASUREMENT_ID || !window.gtag) {
    return;
  }

  window.gtag('config', GA_MEASUREMENT_ID, {
    page_path: path,
  });
}

/**
 * Track a custom event
 */
export function trackEvent(
  eventName: string,
  eventParams?: {
    category?: string;
    label?: string;
    value?: number;
    [key: string]: any;
  }
) {
  if (!GA_MEASUREMENT_ID || !window.gtag) {
    return;
  }

  window.gtag('event', eventName, eventParams);
}

