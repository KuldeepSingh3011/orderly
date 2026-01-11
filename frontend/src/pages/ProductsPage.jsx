import { useState, useEffect } from 'react';
import { ShoppingCart, Package, Sparkles, Plus, Minus } from 'lucide-react';
import { productApi, recommendationApi } from '../api';
import { useCart } from '../App';

// Product emoji based on category
const getProductEmoji = (category) => {
  const emojis = {
    'Electronics': '💻',
    'Clothing': '👕',
    'Books': '📚',
    'Home': '🏠',
    'Sports': '⚽',
    'Food': '🍕',
  };
  return emojis[category] || '📦';
};

function ProductCard({ product, onAddToCart }) {
  const [quantity, setQuantity] = useState(1);
  
  const stockClass = product.availableQuantity <= 0 
    ? 'stock-out' 
    : product.availableQuantity <= 5 
      ? 'stock-low' 
      : '';

  const maxQty = Math.min(product.availableQuantity || 10, 10);

  const handleAdd = () => {
    onAddToCart(product, quantity);
    setQuantity(1); // Reset after adding
  };

  return (
    <div className="card product-card">
      <div className="product-image">
        {getProductEmoji(product.category)}
      </div>
      <div className="product-category">{product.category}</div>
      <div className="product-name">{product.name}</div>
      <div className="product-price">${product.price?.toFixed(2)}</div>
      <div className={`product-stock ${stockClass}`}>
        {product.availableQuantity <= 0 
          ? 'Out of Stock' 
          : `${product.availableQuantity} in stock`}
      </div>
      
      {product.availableQuantity > 0 && (
        <div className="quantity-selector">
          <button 
            className="qty-btn" 
            onClick={() => setQuantity(q => Math.max(1, q - 1))}
            disabled={quantity <= 1}
          >
            <Minus size={14} />
          </button>
          <span className="qty-value">{quantity}</span>
          <button 
            className="qty-btn" 
            onClick={() => setQuantity(q => Math.min(maxQty, q + 1))}
            disabled={quantity >= maxQty}
          >
            <Plus size={14} />
          </button>
        </div>
      )}
      
      <button 
        className="btn btn-primary" 
        onClick={handleAdd}
        disabled={product.availableQuantity <= 0}
      >
        <ShoppingCart size={16} />
        Add to Cart
      </button>
    </div>
  );
}

function Recommendations({ userId }) {
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchRecommendations();
  }, [userId]);

  const fetchRecommendations = async () => {
    try {
      const response = await recommendationApi.getRecommendations(userId);
      setRecommendations(response.data.data?.recommendations || []);
    } catch (error) {
      console.error('Failed to fetch recommendations:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading || recommendations.length === 0) return null;

  return (
    <div className="recommendations">
      <h3 className="recommendations-title">
        <Sparkles size={20} />
        AI Recommendations for You
      </h3>
      <div className="recommendations-list">
        {recommendations.map((rec, index) => (
          <div key={index} className="recommendation-card">
            <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>✨</div>
            <div>{rec}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default function ProductsPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const { addToCart, userId } = useCart();

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      const response = await productApi.getProducts();
      setProducts(response.data.data || []);
    } catch (error) {
      console.error('Failed to fetch products:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
      </div>
    );
  }

  return (
    <div>
      <h1 className="page-title">Products</h1>
      <p className="page-subtitle">Browse our catalog and add items to your cart</p>

      {products.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📦</div>
          <h2 className="empty-title">No Products Yet</h2>
          <p className="empty-text">
            Create some products using the API:
            <br />
            <code>POST http://localhost:8082/api/products</code>
          </p>
        </div>
      ) : (
        <div className="product-grid">
          {products.map(product => (
            <ProductCard 
              key={product.id} 
              product={product} 
              onAddToCart={addToCart}
            />
          ))}
        </div>
      )}

      <Recommendations userId={userId} />
    </div>
  );
}
