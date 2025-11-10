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

const DEFAULT_GA_MEASUREMENT_ID = 'G-R0FBHWNZFP';
const GA_MEASUREMENT_ID = import.meta.env?.VITE_GA_MEASUREMENT_ID ?? DEFAULT_GA_MEASUREMENT_ID;

/**
 * Initialize Google Analytics
 */
export function initAnalytics() {
  if (typeof window === 'undefined') {
    return;
  }

  if (typeof window.gtag !== 'function') {
    console.warn('Google Analytics snippet not loaded yet. Ensure gtag.js is included in index.html.');
    return;
  }

  window.gtag('config', GA_MEASUREMENT_ID, {
    page_path: window.location.pathname,
  });
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

