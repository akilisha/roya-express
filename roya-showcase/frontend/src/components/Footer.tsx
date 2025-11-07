export function Footer() {
  return (
    <footer class="bg-gray-900 dark:bg-black text-gray-300 border-t border-gray-800 dark:border-gray-900">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div>
            <h3 class="text-white font-bold text-lg mb-4">Roya</h3>
            <p class="text-sm">
              Express for Java. AI-Native. 100x Faster.
            </p>
          </div>
          
          <div>
            <h4 class="text-white font-semibold mb-4">Learn</h4>
            <ul class="space-y-2 text-sm">
              <li><a href="/docs" class="hover:text-white transition-colors">Documentation</a></li>
              <li><a href="/tutorials" class="hover:text-white transition-colors">Tutorials</a></li>
              <li><a href="/examples" class="hover:text-white transition-colors">Examples</a></li>
            </ul>
          </div>
          
          <div>
            <h4 class="text-white font-semibold mb-4">Community</h4>
            <ul class="space-y-2 text-sm">
              <li><a href="https://github.com/your-org/roya" target="_blank" rel="noopener noreferrer" class="hover:text-white transition-colors">GitHub</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Discord</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Twitter</a></li>
            </ul>
          </div>
          
          <div>
            <h4 class="text-white font-semibold mb-4">Resources</h4>
            <ul class="space-y-2 text-sm">
              <li><a href="/architecture" class="hover:text-white transition-colors">Architecture</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Blog</a></li>
              <li><a href="#" class="hover:text-white transition-colors">Changelog</a></li>
            </ul>
          </div>
        </div>
        
        <div class="mt-8 pt-8 border-t border-gray-800 dark:border-gray-900 text-center text-sm">
          <p>&copy; 2025 Roya Framework. Apache 2.0 License.</p>
        </div>
      </div>
    </footer>
  );
}

