import { signal } from '@preact/signals';

// Dark mode state - persisted to localStorage
const getInitialDarkMode = () => {
  if (typeof window !== 'undefined') {
    const saved = localStorage.getItem('darkMode');
    if (saved !== null) {
      return saved === 'true';
    }
    // Check system preference
    return window.matchMedia('(prefers-color-scheme: dark)').matches;
  }
  return false;
};

export const isDarkMode = signal(getInitialDarkMode());

// Apply dark mode class to document
if (typeof document !== 'undefined') {
  if (isDarkMode.value) {
    document.documentElement.classList.add('dark');
  } else {
    document.documentElement.classList.remove('dark');
  }
  
  // Watch for changes
  isDarkMode.subscribe((dark) => {
    if (dark) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('darkMode', 'true');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('darkMode', 'false');
    }
  });
}

export function toggleDarkMode() {
  isDarkMode.value = !isDarkMode.value;
}

