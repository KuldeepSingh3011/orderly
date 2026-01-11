import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      // Proxy API requests to avoid CORS issues
      '/api/cart': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/orders': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/users': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/products': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/recommendations': {
        target: 'http://localhost:8084',
        changeOrigin: true,
      },
    },
  },
})
