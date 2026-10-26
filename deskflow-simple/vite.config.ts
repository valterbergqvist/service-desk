import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Every request that starts with /api is forwarded to the Spring Boot backend.
// This way the browser only talks to port 5173 and we need no CORS setup.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: { '/api': 'http://127.0.0.1:8080' },
  },
});
