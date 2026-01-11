import { useState, useEffect, createContext, useContext } from 'react';
import { BrowserRouter, Routes, Route, Link, useLocation, Navigate } from 'react-router-dom';
import { ShoppingCart, Package, Box, Home, LogOut } from 'lucide-react';
import { cartApi } from './api';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProductsPage from './pages/ProductsPage';
import CartPage from './pages/CartPage';
import OrdersPage from './pages/OrdersPage';
import LoginPage from './pages/LoginPage';
import './index.css';

// Context for cart state
const CartContext = createContext();

export const useCart = () => useContext(CartContext);

// Protected Route component
function ProtectedRoute({ children }) {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
      </div>
    );
  }
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  return children;
}

// Toast notification component
function Toast({ message, type, onClose }) {
  useEffect(() => {
    const timer = setTimeout(onClose, 3000);
    return () => clearTimeout(timer);
  }, [onClose]);

  return (
    <div className={`toast toast-${type}`}>
      {type === 'success' ? '✓' : '✕'} {message}
    </div>
  );
}

// Header component
function Header({ cartCount }) {
  const location = useLocation();
  const { user, logout, isAuthenticated } = useAuth();
  
  if (!isAuthenticated) return null;
  
  const getInitials = (name) => {
    return name?.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) || '?';
  };
  
  return (
    <header className="header">
      <div className="header-content">
        <Link to="/" className="logo">
          <Box size={28} />
          Orderly
        </Link>
        <nav className="nav">
          <Link 
            to="/" 
            className={`nav-link ${location.pathname === '/' ? 'active' : ''}`}
          >
            <Home size={18} />
            Products
          </Link>
          <Link 
            to="/cart" 
            className={`nav-link ${location.pathname === '/cart' ? 'active' : ''}`}
          >
            <ShoppingCart size={18} />
            Cart
            {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
          </Link>
          <Link 
            to="/orders" 
            className={`nav-link ${location.pathname === '/orders' ? 'active' : ''}`}
          >
            <Package size={18} />
            Orders
          </Link>
        </nav>
        <div className="user-menu">
          <div className="user-info">
            <div className="user-avatar">{getInitials(user?.name)}</div>
            <span>{user?.name}</span>
          </div>
          <button className="btn btn-secondary logout-btn" onClick={logout}>
            <LogOut size={16} />
            Logout
          </button>
        </div>
      </div>
    </header>
  );
}

function AppContent() {
  const [cart, setCart] = useState([]);
  const [toast, setToast] = useState(null);
  const { user, isAuthenticated } = useAuth();
  const userId = user?.id || 'guest';

  // Fetch cart on load and when user changes
  useEffect(() => {
    if (isAuthenticated) {
      fetchCart();
    } else {
      setCart([]);
    }
  }, [isAuthenticated, userId]);

  const fetchCart = async () => {
    try {
      const response = await cartApi.getCart(userId);
      setCart(response.data.data?.items || []);
    } catch (error) {
      console.error('Failed to fetch cart:', error);
    }
  };

  const addToCart = async (product, quantity = 1) => {
    try {
      await cartApi.addToCart(userId, {
        productId: product.id,
        productName: product.name,
        quantity: quantity,
        price: product.price,
      });
      await fetchCart();
      showToast(`Added ${quantity} item${quantity > 1 ? 's' : ''} to cart!`, 'success');
    } catch (error) {
      showToast('Failed to add to cart', 'error');
    }
  };

  const clearCart = async () => {
    try {
      await cartApi.clearCart(userId);
      setCart([]);
    } catch (error) {
      console.error('Failed to clear cart:', error);
    }
  };

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
  };

  const cartContextValue = {
    cart,
    userId,
    addToCart,
    clearCart,
    fetchCart,
    showToast,
  };

  return (
    <CartContext.Provider value={cartContextValue}>
      <div className="app">
        <Header cartCount={cart.length} />
        <main className="main-content">
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/" element={
              <ProtectedRoute><ProductsPage /></ProtectedRoute>
            } />
            <Route path="/cart" element={
              <ProtectedRoute><CartPage /></ProtectedRoute>
            } />
            <Route path="/orders" element={
              <ProtectedRoute><OrdersPage /></ProtectedRoute>
            } />
          </Routes>
        </main>
        {toast && (
          <Toast 
            message={toast.message} 
            type={toast.type} 
            onClose={() => setToast(null)} 
          />
        )}
      </div>
    </CartContext.Provider>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
