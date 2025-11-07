/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: 'class', // Enable class-based dark mode
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Red/Green/White/Black palette - Dark mode is bold, Light mode is toned down
        'roya': {
          // Dark mode (bold, high contrast)
          primary: '#10b981', // Green for primary actions
          primaryDark: '#059669',
          accent: '#ef4444', // Red for accents/important
          accentDark: '#dc2626',
          bgDark: '#000000', // Pure black
          surfaceDark: '#111111', // Near-black
          textDark: '#f9fafb', // Near-white text
          textMutedDark: '#9ca3af', // Light gray for dark mode (better contrast)
          borderDark: '#1f1f1f',
          
          // Light mode (toned down, less glare)
          bg: '#fafafa', // Off-white (softer than pure white)
          surface: '#f5f5f5', // Soft gray (less bright)
          text: '#1a1a1a', // Soft black (less harsh)
          textMuted: '#6b7280', // Muted gray
          border: '#e0e0e0', // Softer border
          
          // Shared colors (work in both modes)
          primaryLight: '#34d399', // Lighter green for light mode accents
          accentLight: '#f87171', // Softer red for light mode
        },
        // Keep blue as secondary for links/info
        'roya-blue': {
          50: '#eff6ff',
          100: '#dbeafe',
          200: '#bfdbfe',
          300: '#93c5fd',
          400: '#60a5fa',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
          800: '#1e40af',
          900: '#1e3a8a',
        }
      },
      fontFamily: {
        mono: ['Fira Code', 'Consolas', 'Monaco', 'monospace'],
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
      },
      boxShadow: {
        'glow-green': '0 0 20px rgba(16, 185, 129, 0.3)',
        'glow-red': '0 0 20px rgba(239, 68, 68, 0.3)',
        'soft': '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
        'soft-dark': '0 4px 6px -1px rgba(0, 0, 0, 0.3), 0 2px 4px -1px rgba(0, 0, 0, 0.2)',
      },
      backdropBlur: {
        xs: '2px',
      }
    },
  },
  plugins: [],
}


