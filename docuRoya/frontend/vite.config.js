import { defineConfig } from 'vite';
import preact from '@preact/preset-vite';

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [preact()],
    server: {
        port: 3000,
        proxy: {
            '/api': {
                target: 'http://localhost:3003',
                changeOrigin: true
            },
            '/metrics': {
                target: 'http://localhost:3003',
                changeOrigin: true
            }
        }
    },
    build: {
        outDir: 'dist',
        emptyOutDir: true
    }
});
