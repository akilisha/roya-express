import { headerClasses } from '../utils/theme';

interface PageHeaderProps {
  title: string;
  variant?: 'default' | 'primary' | 'gradient';
  subtitle?: string;
  subtitleMuted?: boolean;
  className?: string;
}

/**
 * Consistent page header component for all pages.
 * Ensures uniform styling across the site with proper dark mode support.
 */
export function PageHeader({ 
  title, 
  variant = 'default',
  subtitle,
  subtitleMuted = false,
  className = ''
}: PageHeaderProps) {
  const h1Class = variant === 'gradient' 
    ? headerClasses.h1.gradient
    : variant === 'primary'
    ? headerClasses.h1.primary
    : headerClasses.h1.default;

  const subtitleClass = subtitleMuted
    ? 'text-lg md:text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed'
    : 'text-lg md:text-xl text-roya-text dark:text-roya-textDark leading-relaxed';

  return (
    <div class={`mb-10 ${className}`}>
      <h1 class={`${h1Class} mb-4`}>
        {title}
      </h1>
      {subtitle && (
        <p class={subtitleClass}>
          {subtitle}
        </p>
      )}
    </div>
  );
}

/**
 * Section header component for consistent h2 styling
 */
export function SectionHeader({ 
  title, 
  variant = 'default',
  className = ''
}: { title: string; variant?: 'default' | 'primary'; className?: string }) {
  const h2Class = variant === 'primary'
    ? headerClasses.h2.primary
    : headerClasses.h2.default;

  return (
    <h2 class={`${h2Class} mb-6 ${className}`}>
      {title}
    </h2>
  );
}

/**
 * Subsection header component for consistent h3 styling
 */
export function SubsectionHeader({ 
  title, 
  variant = 'default',
  className = ''
}: { title: string; variant?: 'default' | 'primary'; className?: string }) {
  const h3Class = variant === 'primary'
    ? headerClasses.h3.primary
    : headerClasses.h3.default;

  return (
    <h3 class={`${h3Class} mb-4 mt-6 ${className}`}>
      {title}
    </h3>
  );
}








