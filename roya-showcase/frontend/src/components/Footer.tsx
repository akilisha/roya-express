export function Footer() {
  return (
    <footer class="bg-gray-900 text-gray-300">
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
              <li><a href="/docs" class="hover:text-white">Documentation</a></li>
              <li><a href="/tutorials" class="hover:text-white">Tutorials</a></li>
              <li><a href="/examples" class="hover:text-white">Examples</a></li>
            </ul>
          </div>
          
          <div>
            <h4 class="text-white font-semibold mb-4">Community</h4>
            <ul class="space-y-2 text-sm">
              <li><a href="https://github.com/your-org/roya" class="hover:text-white">GitHub</a></li>
              <li><a href="#" class="hover:text-white">Discord</a></li>
              <li><a href="#" class="hover:text-white">Twitter</a></li>
            </ul>
          </div>
          
          <div>
            <h4 class="text-white font-semibold mb-4">Resources</h4>
            <ul class="space-y-2 text-sm">
              <li><a href="/playground" class="hover:text-white">Playground</a></li>
              <li><a href="#" class="hover:text-white">Blog</a></li>
              <li><a href="#" class="hover:text-white">Changelog</a></li>
            </ul>
          </div>
        </div>
        
        <div class="mt-8 pt-8 border-t border-gray-800 text-center text-sm">
          <p>&copy; 2025 Roya Framework. Apache 2.0 License.</p>
        </div>
      </div>
    </footer>
  );
}

