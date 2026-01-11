import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      // Auth Service (port 8085)
      '/api/auth': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      },
      // Admin User APIs (auth-service)
      '/api/admin/users': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      },
      // Admin Product APIs (inventory-service)
      '/api/admin/products': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      // Order Service (port 8081)
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
      // Inventory Service (port 8082)
      '/api/products': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/search': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      // Recommendation Service (port 8084)
      '/api/recommendations': {
        target: 'http://localhost:8084',
        changeOrigin: true,
      },
    },
  },
})
