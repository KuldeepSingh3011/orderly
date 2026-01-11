import { useState, useEffect, createContext, useContext } from 'react';
import { BrowserRouter, Routes, Route, Link, useLocation } from 'react-router-dom';
import { ShoppingCart, Package, Box, Sparkles, Home } from 'lucide-react';
import { cartApi } from './api';
import ProductsPage from './pages/ProductsPage';
import CartPage from './pages/CartPage';
import OrdersPage from './pages/OrdersPage';
import './index.css';

// Context for cart state
const CartContext = createContext();

export const useCart = () => useContext(CartContext);

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
      </div>
    </header>
  );
}

function App() {
  const [cart, setCart] = useState([]);
  const [toast, setToast] = useState(null);
  const userId = 'demo-user'; // In production, this would come from auth

  // Fetch cart on load
  useEffect(() => {
    fetchCart();
  }, []);

  const fetchCart = async () => {
    try {
      const response = await cartApi.getCart(userId);
      setCart(response.data.data?.items || []);
    } catch (error) {
      console.error('Failed to fetch cart:', error);
    }
  };

  const addToCart = async (product) => {
    try {
      await cartApi.addToCart(userId, {
        productId: product.id,
        productName: product.name,
        quantity: 1,
        price: product.price,
      });
      await fetchCart();
      showToast('Added to cart!', 'success');
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
      <BrowserRouter>
        <div className="app">
          <Header cartCount={cart.length} />
          <main className="main-content">
            <Routes>
              <Route path="/" element={<ProductsPage />} />
              <Route path="/cart" element={<CartPage />} />
              <Route path="/orders" element={<OrdersPage />} />
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
      </BrowserRouter>
    </CartContext.Provider>
  );
}

export default App;
