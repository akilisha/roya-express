import { useEffect } from 'preact/hooks';
import { useLocation } from 'wouter';
import { trackPageView } from '../utils/analytics';

/**
 * Component that tracks page views when routes change
 */
export function AnalyticsTracker() {
  const [location] = useLocation();

  useEffect(() => {
    // Track page view when location changes
    trackPageView(location);
  }, [location]);

  return null; // This component doesn't render anything
}

