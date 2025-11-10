# Theme Migration Guide - Header Consistency

This guide helps migrate all pages to use consistent header styling with the Roya theme.

## Theme System Overview

We've created a centralized theme system:
- **`src/utils/theme.ts`** - Theme configuration and utility classes
- **`src/utils/headerStyles.ts`** - Header style utilities
- **`src/components/PageHeader.tsx`** - Reusable header components

## Color Theme

### Light Mode Colors
- Primary: `#10b981` (Green)
- Accent: `#ef4444` (Red)
- Background: `#fafafa` (Off-white)
- Surface: `#f5f5f5` (Soft gray)
- Text: `#1a1a1a` (Soft black)
- Text Muted: `#6b7280` (Muted gray)
- Border: `#e0e0e0` (Soft border)

### Dark Mode Colors
- Primary: `#10b981` (Green - same)
- Accent: `#ef4444` (Red - same)
- Background: `#000000` (Pure black)
- Surface: `#111111` (Near-black)
- Text: `#f9fafb` (Near-white)
- Text Muted: `#9ca3af` (Light gray)
- Border: `#1f1f1f` (Dark border)

## Font Theme

- **Primary Font**: Inter (loaded from Google Fonts)
- **Code Font**: Fira Code, Consolas, Monaco, monospace
- All headings use `font-sans` class for Inter font

## Header Styles

### H1 (Page Titles)
```tsx
// Default
<h1 class="text-4xl md:text-5xl font-bold tracking-tight text-roya-text dark:text-roya-textDark font-sans">

// Primary (green)
<h1 class="text-4xl md:text-5xl font-bold tracking-tight text-roya-primary dark:text-roya-primary font-sans">

// Gradient (hero sections)
<h1 class="text-4xl md:text-5xl font-bold tracking-tight bg-gradient-to-r from-roya-primary via-roya-primary to-roya-accent bg-clip-text text-transparent font-sans">
```

### H2 (Section Headers)
```tsx
// Default
<h2 class="text-3xl md:text-4xl font-bold text-roya-text dark:text-roya-textDark font-sans mb-6">

// Primary (green)
<h2 class="text-3xl md:text-4xl font-bold text-roya-primary dark:text-roya-primary font-sans mb-6">
```

### H3 (Subsection Headers)
```tsx
// Default
<h3 class="text-2xl font-bold text-roya-text dark:text-roya-textDark font-sans mb-4">

// Primary (green)
<h3 class="text-2xl font-bold text-roya-primary dark:text-roya-primary font-sans mb-4">
```

### H4 (Sub-subsection Headers)
```tsx
// Default
<h4 class="text-xl font-bold text-roya-text dark:text-roya-textDark font-sans mb-3">

// Primary (green)
<h4 class="text-xl font-bold text-roya-primary dark:text-roya-primary font-sans mb-3">
```

## Migration Patterns

### Pattern 1: Replace Old Color Classes

**Replace:**
- `text-gray-900 dark:text-white` → `text-roya-text dark:text-roya-textDark`
- `text-gray-600 dark:text-gray-300` → `text-roya-textMuted dark:text-roya-textMutedDark`
- `text-blue-600` → `text-roya-primary dark:text-roya-primary`
- `text-orange-900` → `text-roya-primary dark:text-roya-primary` (or keep for specific callouts)
- `text-purple-900` → `text-roya-primary dark:text-roya-primary` (or keep for specific callouts)

### Pattern 2: Update Header Sizes

**Replace:**
- `text-4xl font-bold` → `text-4xl md:text-5xl font-bold tracking-tight font-sans`
- `text-2xl font-semibold` → `text-2xl font-bold font-sans`
- `text-xl font-semibold` → `text-xl font-bold font-sans`

### Pattern 3: Update Code Blocks

**Replace:**
- `bg-gray-900 text-green-400` → `bg-black dark:bg-black text-roya-primary dark:text-roya-primary`
- Add: `border border-roya-borderDark`
- Add: `font-mono text-sm` to `<code>` tag

### Pattern 4: Update Section Backgrounds

**Replace:**
- `bg-white dark:bg-gray-800` → `bg-roya-bg dark:bg-roya-surfaceDark`
- `shadow-md dark:shadow-gray-900` → `shadow-soft dark:shadow-soft-dark`
- `border border-gray-200 dark:border-gray-700` → `border border-roya-border dark:border-roya-borderDark`
- `rounded-lg` → `rounded-xl`

### Pattern 5: Update Links

**Replace:**
- `text-blue-600 hover:underline` → `text-roya-primary dark:text-roya-primary hover:text-roya-primaryDark dark:hover:text-roya-primaryDark hover:underline transition-colors`

## Using PageHeader Component

Instead of manual header markup, use the reusable component:

```tsx
import { PageHeader, SectionHeader, SubsectionHeader } from '../../components/PageHeader';

// Page title
<PageHeader
  title="Getting Started"
  subtitle="Get up and running with Roya"
  subtitleMuted={true}
  variant="default" // or "primary" or "gradient"
/>

// Section header
<SectionHeader title="Installation" variant="default" />

// Subsection header
<SubsectionHeader title="Using Gradle" variant="default" />
```

## Files to Update

### High Priority (Already Updated)
- ✅ `src/pages/Home.tsx`
- ✅ `src/pages/Tutorials.tsx`
- ✅ `src/pages/docs/GettingStarted.tsx`
- ✅ `src/pages/docs/AIApi.tsx`
- ✅ `src/pages/docs/BasicRouting.tsx`

### Remaining Files
- [ ] `src/pages/docs/StaticFiles.tsx`
- [ ] `src/pages/docs/ExpressMigration.tsx`
- [ ] `src/pages/docs/SpringMigration.tsx`
- [ ] `src/pages/docs/QuarkusMigration.tsx`
- [ ] `src/pages/docs/AgentsDocs.tsx`
- [ ] `src/pages/docs/ErrorHandlingGuide.tsx`
- [ ] `src/pages/docs/DatabaseGuide.tsx`
- [ ] `src/pages/docs/UsingMiddlewareGuide.tsx`
- [ ] `src/pages/docs/RoyaApi.tsx`
- [ ] `src/pages/docs/AIQuickStart.tsx`
- [ ] `src/pages/docs/HelloWorld.tsx`
- [ ] `src/pages/docs/LLMDocs.tsx`
- [ ] `src/pages/docs/RAGDocs.tsx`
- [ ] `src/pages/docs/WorkflowsDocs.tsx`
- [ ] `src/pages/docs/MiddlewareGuide.tsx`
- [ ] `src/pages/docs/PluginsGuide.tsx`
- [ ] `src/pages/docs/Guide.tsx`
- [ ] `src/pages/docs/ApiReference.tsx`
- [ ] `src/pages/docs/MigrationGuides.tsx`
- [ ] `src/pages/docs/AIIntegration.tsx`
- [ ] `src/pages/Architecture.tsx`
- [ ] `src/pages/Docs.tsx`
- [ ] `src/pages/Examples.tsx`

## Quick Find & Replace Patterns

Use these in your editor (with regex enabled):

### 1. Fix H1 Headers
Find: `class="text-4xl.*?font-bold.*?text-(gray|blue|white|black)[^"]*"`
Replace: `class="text-4xl md:text-5xl font-bold tracking-tight text-roya-text dark:text-roya-textDark font-sans"`

### 2. Fix H2 Headers
Find: `class="text-2xl.*?font-(semibold|bold).*?text-(gray|blue|white|black)[^"]*"`
Replace: `class="text-2xl font-bold text-roya-text dark:text-roya-textDark font-sans mb-4"`

### 3. Fix H3 Headers
Find: `class="text-xl.*?font-(semibold|bold)[^"]*"`
Replace: `class="text-xl font-bold text-roya-text dark:text-roya-textDark font-sans mb-3"`

### 4. Fix Text Colors
Find: `text-gray-(\d+)`
Replace: Check context - use `text-roya-text` or `text-roya-textMuted` as appropriate

### 5. Fix Backgrounds
Find: `bg-white dark:bg-gray-800`
Replace: `bg-roya-bg dark:bg-roya-surfaceDark`

### 6. Fix Code Blocks
Find: `bg-gray-900 text-green-400`
Replace: `bg-black dark:bg-black text-roya-primary dark:text-roya-primary`

## Testing Checklist

After migration, verify:
- [ ] All headers use consistent font (Inter)
- [ ] All headers have proper dark mode colors
- [ ] No hardcoded gray/blue colors remain
- [ ] Code blocks use roya theme colors
- [ ] Links use roya-primary colors
- [ ] Sections have consistent backgrounds
- [ ] Dark mode toggles correctly

## Example: Complete Migration

**Before:**
```tsx
<div class="mb-8">
  <h1 class="text-4xl font-bold mb-4 text-gray-900 dark:text-white">Getting Started</h1>
  <p class="text-xl text-gray-600 dark:text-gray-300">
    Get up and running with Roya.
  </p>
</div>

<section class="bg-white dark:bg-gray-800 rounded-lg shadow-md p-8">
  <h2 class="text-2xl font-semibold mb-4 text-gray-900 dark:text-white">Installation</h2>
  <p class="text-gray-700 dark:text-gray-300 mb-4">
    Roya requires Java 21+.
  </p>
</section>
```

**After:**
```tsx
<PageHeader
  title="Getting Started"
  subtitle="Get up and running with Roya"
  subtitleMuted={true}
/>

<section class="bg-roya-bg dark:bg-roya-surfaceDark rounded-xl shadow-soft dark:shadow-soft-dark p-8 border border-roya-border dark:border-roya-borderDark">
  <SectionHeader title="Installation" />
  <p class="text-base text-roya-text dark:text-roya-textDark mb-4 leading-relaxed">
    Roya requires <strong class="text-roya-primary dark:text-roya-primary">Java 21+</strong>.
  </p>
</section>
```

## Notes

- Always test in both light and dark mode
- Use `font-sans` explicitly on headers to ensure Inter font
- Keep semantic meaning - primary color for important headers
- Use gradient variant only for hero sections
- Maintain accessibility - ensure sufficient contrast





