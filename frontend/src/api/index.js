import axios from 'axios';

// Using Vite proxy - relative paths
const api = axios.create({
  baseURL: '',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Order Service APIs (proxied to localhost:8081)
export const cartApi = {
  getCart: (userId) => api.get(`/api/cart/${userId}`),
  addToCart: (userId, item) => api.post(`/api/cart/${userId}/items`, item),
  clearCart: (userId) => api.delete(`/api/cart/${userId}`),
};

export const orderApi = {
  createOrder: (userId, shippingAddress) => 
    api.post(`/api/orders`, { shippingAddress }, {
      headers: { 'X-User-Id': userId }
    }),
  getOrder: (orderId) => api.get(`/api/orders/${orderId}`),
  getUserOrders: (userId) => api.get(`/api/users/${userId}/orders`),
};

// Inventory Service APIs (proxied to localhost:8082)
export const productApi = {
  getProducts: () => api.get(`/api/products`),
  getProduct: (productId) => api.get(`/api/products/${productId}`),
  createProduct: (product) => api.post(`/api/products`, product),
};

// Recommendation Service APIs (proxied to localhost:8084)
export const recommendationApi = {
  getRecommendations: (userId) => api.get(`/api/recommendations/${userId}`),
};
