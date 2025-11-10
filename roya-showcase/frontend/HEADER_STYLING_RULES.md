# Header Styling Rules - Quick Reference

## Standard Header Classes

### H1 (Page Titles)
```tsx
// Default
class="text-4xl md:text-5xl font-bold tracking-tight text-roya-text dark:text-roya-textDark font-sans"

// Primary (green accent)
class="text-4xl md:text-5xl font-bold tracking-tight text-roya-primary dark:text-roya-primary font-sans"

// Gradient (hero only)
class="text-4xl md:text-5xl font-bold tracking-tight bg-gradient-to-r from-roya-primary via-roya-primary to-roya-accent bg-clip-text text-transparent font-sans"
```

### H2 (Section Headers)
```tsx
// Default
class="text-3xl md:text-4xl font-bold text-roya-text dark:text-roya-textDark font-sans mb-6"

// Primary
class="text-3xl md:text-4xl font-bold text-roya-primary dark:text-roya-primary font-sans mb-6"
```

### H3 (Subsection Headers)
```tsx
// Default
class="text-2xl font-bold text-roya-text dark:text-roya-textDark font-sans mb-4"

// Primary
class="text-2xl font-bold text-roya-primary dark:text-roya-primary font-sans mb-4"
```

### H4 (Sub-subsection Headers)
```tsx
// Default
class="text-xl font-bold text-roya-text dark:text-roya-textDark font-sans mb-3"

// Primary
class="text-xl font-bold text-roya-primary dark:text-roya-primary font-sans mb-3"
```

## Color Replacements

| Old Class | New Class |
|-----------|-----------|
| `text-gray-900 dark:text-white` | `text-roya-text dark:text-roya-textDark` |
| `text-gray-600 dark:text-gray-300` | `text-roya-textMuted dark:text-roya-textMutedDark` |
| `text-gray-700` | `text-roya-text dark:text-roya-textDark` |
| `text-blue-600` | `text-roya-primary dark:text-roya-primary` |
| `bg-white dark:bg-gray-800` | `bg-roya-bg dark:bg-roya-surfaceDark` |
| `bg-gray-900 text-green-400` | `bg-black dark:bg-black text-roya-primary dark:text-roya-primary` |

## Always Include

1. **Font**: Always add `font-sans` to headers
2. **Dark Mode**: Always include dark mode variant
3. **Spacing**: Use consistent mb-* spacing (mb-6 for h2, mb-4 for h3, mb-3 for h4)

## Never Use

- ❌ Hardcoded gray colors (`text-gray-*`, `bg-gray-*`)
- ❌ Hardcoded blue colors (`text-blue-*`)  
- ❌ Headers without `font-sans`
- ❌ Headers without dark mode variants
- ❌ Inconsistent font sizes





