/**
 * Theme configuration and utility classes for consistent styling across the site.
 * 
 * This ensures headers, fonts, and colors are applied uniformly.
 */

export const theme = {
  // Header styles
  headers: {
    h1: {
      base: 'text-4xl md:text-5xl font-bold tracking-tight',
      light: 'text-roya-text',
      dark: 'dark:text-roya-textDark',
      primary: 'text-roya-primary dark:text-roya-primary',
      gradient: 'bg-gradient-to-r from-roya-primary via-roya-primary to-roya-accent bg-clip-text text-transparent'
    },
    h2: {
      base: 'text-3xl md:text-4xl font-bold',
      light: 'text-roya-text dark:text-roya-textDark',
      dark: 'dark:text-roya-textDark',
      primary: 'text-roya-primary dark:text-roya-primary',
    },
    h3: {
      base: 'text-2xl font-bold',
      light: 'text-roya-text dark:text-roya-textDark',
      dark: 'dark:text-roya-textDark',
      primary: 'text-roya-primary dark:text-roya-primary',
    },
    h4: {
      base: 'text-xl font-semibold',
      light: 'text-roya-text dark:text-roya-textDark',
      dark: 'dark:text-roya-textDark',
      primary: 'text-roya-primary dark:text-roya-primary',
    },
  },
  
  // Typography
  typography: {
    body: 'text-base text-roya-text dark:text-roya-textDark leading-relaxed',
    bodyMuted: 'text-base text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed',
    bodyLarge: 'text-lg text-roya-text dark:text-roya-textDark leading-relaxed',
    bodyLargeMuted: 'text-lg text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed',
    bodyXL: 'text-xl text-roya-text dark:text-roya-textDark leading-relaxed',
    bodyXLMuted: 'text-xl text-roya-textMuted dark:text-roya-textMutedDark leading-relaxed',
    code: 'font-mono text-sm',
  },
  
  // Spacing
  spacing: {
    section: 'mb-10',
    subsection: 'mb-6',
    item: 'mb-4',
  },
} as const;

/**
 * Helper functions to generate header class strings
 */
export const headerClasses = {
  h1: {
    default: `${theme.headers.h1.base} ${theme.headers.h1.light}`,
    primary: `${theme.headers.h1.base} ${theme.headers.h1.primary}`,
    gradient: `${theme.headers.h1.base} ${theme.headers.h1.gradient}`,
  },
  h2: {
    default: `${theme.headers.h2.base} ${theme.headers.h2.light}`,
    primary: `${theme.headers.h2.base} ${theme.headers.h2.primary}`,
  },
  h3: {
    default: `${theme.headers.h3.base} ${theme.headers.h3.light}`,
    primary: `${theme.headers.h3.base} ${theme.headers.h3.primary}`,
  },
  h4: {
    default: `${theme.headers.h4.base} ${theme.headers.h4.light}`,
    primary: `${theme.headers.h4.base} ${theme.headers.h4.primary}`,
  },
} as const;

