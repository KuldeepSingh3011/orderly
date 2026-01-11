import axios from 'axios';

// Create axios instance with interceptors for auth
const api = axios.create({
  baseURL: '',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('orderly_access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for token refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        const refreshToken = localStorage.getItem('orderly_refresh_token');
        if (refreshToken) {
          const response = await axios.post('/api/auth/refresh', { refreshToken });
          const { accessToken, refreshToken: newRefreshToken } = response.data.data;
          
          localStorage.setItem('orderly_access_token', accessToken);
          localStorage.setItem('orderly_refresh_token', newRefreshToken);
          
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        // Refresh failed, clear tokens and redirect to login
        localStorage.removeItem('orderly_access_token');
        localStorage.removeItem('orderly_refresh_token');
        localStorage.removeItem('orderly_user');
        window.location.href = '/login';
      }
    }
    
    return Promise.reject(error);
  }
);

// Auth Service APIs (port 8085)
export const authApi = {
  register: (data) => api.post('/api/auth/register', data),
  login: (data) => api.post('/api/auth/login', data),
  logout: (refreshToken) => api.post('/api/auth/logout', { refreshToken }),
  refresh: (refreshToken) => api.post('/api/auth/refresh', { refreshToken }),
  me: () => api.get('/api/auth/me'),
};

// Order Service APIs (port 8081)
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

// Inventory Service APIs (port 8082)
export const productApi = {
  getProducts: () => api.get('/api/products'),
  getProduct: (productId) => api.get(`/api/products/${productId}`),
  search: (query, filters = {}) => {
    const params = new URLSearchParams({ q: query, ...filters });
    return api.get(`/api/search?${params}`);
  },
  getSuggestions: (query) => api.get(`/api/search/suggestions?q=${query}`),
};

// Admin APIs
export const adminApi = {
  // Product management
  getProducts: (includeInactive = false) => 
    api.get(`/api/admin/products?includeInactive=${includeInactive}`),
  createProduct: (product) => api.post('/api/admin/products', product),
  updateProduct: (productId, product) => api.put(`/api/admin/products/${productId}`, product),
  updatePrice: (productId, price) => 
    api.put(`/api/admin/products/${productId}/price?price=${price}`),
  updateStock: (productId, quantity) => 
    api.put(`/api/admin/products/${productId}/stock?quantity=${quantity}`),
  adjustStock: (productId, adjustment) => 
    api.put(`/api/admin/products/${productId}/stock/adjust?adjustment=${adjustment}`),
  deleteProduct: (productId) => api.delete(`/api/admin/products/${productId}`),
  activateProduct: (productId) => api.put(`/api/admin/products/${productId}/activate`),
  deactivateProduct: (productId) => api.put(`/api/admin/products/${productId}/deactivate`),
  
  // User management
  getUsers: () => api.get('/api/admin/users'),
  createAdmin: (data) => api.post('/api/admin/users/create-admin', data),
  disableUser: (userId) => api.put(`/api/admin/users/${userId}/disable`),
  enableUser: (userId) => api.put(`/api/admin/users/${userId}/enable`),
};

// Recommendation Service APIs (port 8084)
export const recommendationApi = {
  getRecommendations: (userId) => api.get(`/api/recommendations/${userId}`),
};

export default api;
