/**
 * Theme Provider Component
 * 
 * This component ensures consistent theme application across all pages.
 * It provides CSS variables and global styles for the Roya theme.
 */

export function ThemeProvider({ children }: { children: any }) {
  return (
    <div class="roya-theme">
      {children}
    </div>
  );
}








