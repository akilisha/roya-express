/**
 * Header style utilities for consistent styling across all pages.
 * 
 * Use these utilities instead of inline classes to ensure consistency.
 */

/**
 * Standard header class strings for consistent styling
 */
export const headerStyles = {
  // Page titles (h1)
  pageTitle: {
    default: 'text-4xl md:text-5xl font-bold tracking-tight text-roya-text dark:text-roya-textDark font-sans',
    primary: 'text-4xl md:text-5xl font-bold tracking-tight text-roya-primary dark:text-roya-primary font-sans',
    gradient: 'text-4xl md:text-5xl font-bold tracking-tight bg-gradient-to-r from-roya-primary via-roya-primary to-roya-accent bg-clip-text text-transparent font-sans',
  },
  
  // Section headers (h2)
  section: {
    default: 'text-3xl md:text-4xl font-bold text-roya-text dark:text-roya-textDark font-sans mb-6',
    primary: 'text-3xl md:text-4xl font-bold text-roya-primary dark:text-roya-primary font-sans mb-6',
  },
  
  // Subsection headers (h3)
  subsection: {
    default: 'text-2xl font-bold text-roya-text dark:text-roya-textDark font-sans mb-4',
    primary: 'text-2xl font-bold text-roya-primary dark:text-roya-primary font-sans mb-4',
  },
  
  // Sub-subsection headers (h4)
  subsubsection: {
    default: 'text-xl font-bold text-roya-text dark:text-roya-textDark font-sans mb-3',
    primary: 'text-xl font-bold text-roya-primary dark:text-roya-primary font-sans mb-3',
  },
} as const;

/**
 * Body text styles for consistency
 */
export const bodyStyles = {
  default: 'text-base text-roya-text dark:text-roya-textDark leading-relaxed',
  muted: 'text-base text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed',
  large: 'text-lg text-roya-text dark:text-roya-textDark leading-relaxed',
  largeMuted: 'text-lg text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed',
  xl: 'text-xl text-roya-text dark:text-roya-textDark leading-relaxed',
  xlMuted: 'text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed',
} as const;

/**
 * Link styles for consistency
 */
export const linkStyles = {
  default: 'text-roya-primary dark:text-roya-primary hover:text-roya-primaryDark dark:hover:text-roya-primaryDark hover:underline transition-colors',
  muted: 'text-roya-textMuted dark:text-roya-textMutedDark hover:text-roya-primary dark:hover:text-roya-primary hover:underline transition-colors',
} as const;

/**
 * Code block styles
 */
export const codeStyles = {
  block: 'bg-black dark:bg-black text-roya-primary dark:text-roya-primary p-4 rounded-lg overflow-x-auto border border-roya-borderDark font-mono text-sm',
  inline: 'bg-roya-surface dark:bg-roya-surfaceDark px-2 py-1 rounded text-roya-primary dark:text-roya-primary font-mono text-sm',
} as const;








