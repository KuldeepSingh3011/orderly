import { useState, useEffect, createContext, useContext } from 'react';
import { BrowserRouter, Routes, Route, Link, useLocation, Navigate } from 'react-router-dom';
import { ShoppingCart, Package, Box, Home, LogOut, Settings, Search } from 'lucide-react';
import { cartApi, productApi } from './api';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProductsPage from './pages/ProductsPage';
import CartPage from './pages/CartPage';
import OrdersPage from './pages/OrdersPage';
import LoginPage from './pages/LoginPage';
import AdminPage from './pages/AdminPage';
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

// Admin Route component
function AdminRoute({ children }) {
  const { isAuthenticated, isAdmin, loading } = useAuth();
  
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
  
  if (!isAdmin) {
    return <Navigate to="/" replace />;
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

// Search component
function SearchBar({ onSearch }) {
  const [query, setQuery] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);

  const handleInputChange = async (e) => {
    const value = e.target.value;
    setQuery(value);

    if (value.length >= 2) {
      try {
        const response = await productApi.getSuggestions(value);
        setSuggestions(response.data.data || []);
        setShowSuggestions(true);
      } catch (error) {
        console.error('Failed to get suggestions:', error);
      }
    } else {
      setSuggestions([]);
      setShowSuggestions(false);
    }
  };

  const handleSearch = (searchQuery) => {
    setQuery(searchQuery);
    setShowSuggestions(false);
    onSearch(searchQuery);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (query.trim()) {
      handleSearch(query);
    }
  };

  return (
    <form className="search-form" onSubmit={handleSubmit}>
      <div className="search-input-wrapper">
        <Search size={18} className="search-icon" />
        <input
          type="text"
          className="search-input"
          placeholder="Search products..."
          value={query}
          onChange={handleInputChange}
          onFocus={() => query.length >= 2 && setShowSuggestions(true)}
          onBlur={() => setTimeout(() => setShowSuggestions(false), 200)}
        />
        {showSuggestions && suggestions.length > 0 && (
          <ul className="suggestions-list">
            {suggestions.map((suggestion, index) => (
              <li 
                key={index}
                onClick={() => handleSearch(suggestion)}
              >
                {suggestion}
              </li>
            ))}
          </ul>
        )}
      </div>
    </form>
  );
}

// Header component
function Header({ cartCount, onSearch }) {
  const location = useLocation();
  const { user, logout, isAuthenticated, isAdmin } = useAuth();
  
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
        
        <SearchBar onSearch={onSearch} />
        
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
          {isAdmin && (
            <Link 
              to="/admin" 
              className={`nav-link admin-link ${location.pathname === '/admin' ? 'active' : ''}`}
            >
              <Settings size={18} />
              Admin
            </Link>
          )}
        </nav>
        <div className="user-menu">
          <div className="user-info">
            <div className="user-avatar">{getInitials(user?.name)}</div>
            <div className="user-details">
              <span className="user-name">{user?.name}</span>
              {isAdmin && <span className="admin-badge">Admin</span>}
            </div>
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
  const [searchQuery, setSearchQuery] = useState('');
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

  const handleSearch = (query) => {
    setSearchQuery(query);
  };

  const cartContextValue = {
    cart,
    userId,
    addToCart,
    clearCart,
    fetchCart,
    showToast,
    searchQuery,
    setSearchQuery,
  };

  return (
    <CartContext.Provider value={cartContextValue}>
      <div className="app">
        <Header cartCount={cart.length} onSearch={handleSearch} />
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
            <Route path="/admin" element={
              <AdminRoute><AdminPage /></AdminRoute>
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
