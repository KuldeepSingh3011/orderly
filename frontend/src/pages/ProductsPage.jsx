import { useState, useEffect } from 'react';
import { ShoppingCart, Package, Sparkles, Plus, Minus, X } from 'lucide-react';
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

  const maxQty = Math.min(product.availableQuantity || product.stockQuantity || 10, 10);

  const handleAdd = () => {
    onAddToCart(product, quantity);
    setQuantity(1); // Reset after adding
  };

  const available = product.availableQuantity ?? product.stockQuantity ?? 0;

  return (
    <div className="card product-card">
      <div className="product-image">
        {product.imageUrl ? (
          <img src={product.imageUrl} alt={product.name} />
        ) : (
          getProductEmoji(product.category)
        )}
      </div>
      <div className="product-category">{product.category}</div>
      <div className="product-name">{product.name}</div>
      <div className="product-price">${product.price?.toFixed(2)}</div>
      <div className={`product-stock ${stockClass}`}>
        {available <= 0 
          ? 'Out of Stock' 
          : `${available} in stock`}
      </div>
      
      {available > 0 && (
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
        disabled={available <= 0}
      >
        <ShoppingCart size={16} />
        Add to Cart
      </button>
    </div>
  );
}

function RecommendationCard({ product, onAddToCart }) {
  const handleAdd = () => {
    onAddToCart(product, 1);
  };

  const available = product.availableQuantity ?? product.stockQuantity ?? 0;

  return (
    <div className="recommendation-card clickable" onClick={handleAdd}>
      <div className="rec-product-image">
        {product.imageUrl ? (
          <img src={product.imageUrl} alt={product.name} />
        ) : (
          getProductEmoji(product.category)
        )}
      </div>
      <div className="rec-product-info">
        <div className="rec-product-category">{product.category}</div>
        <div className="rec-product-name">{product.name}</div>
        <div className="rec-product-price">${product.price?.toFixed(2)}</div>
      </div>
      <button 
        className="rec-add-btn"
        disabled={available <= 0}
        title="Add to Cart"
      >
        <Plus size={16} />
      </button>
    </div>
  );
}

function Recommendations({ userId, onAddToCart }) {
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchRecommendations();
  }, [userId]);

  const fetchRecommendations = async () => {
    try {
      const response = await recommendationApi.getRecommendations(userId);
      // Now recommendations are product objects, not strings
      const recs = response.data.data?.recommendations || [];
      setRecommendations(recs);
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
        Recommended for You
      </h3>
      <p className="recommendations-subtitle">Click on a product to add it to your cart</p>
      <div className="recommendations-grid">
        {recommendations.map((product) => (
          <RecommendationCard 
            key={product.id} 
            product={product} 
            onAddToCart={onAddToCart}
          />
        ))}
      </div>
    </div>
  );
}

export default function ProductsPage() {
  const [products, setProducts] = useState([]);
  const [searchResults, setSearchResults] = useState(null);
  const [loading, setLoading] = useState(true);
  const [searching, setSearching] = useState(false);
  const { addToCart, userId, searchQuery, setSearchQuery } = useCart();

  useEffect(() => {
    fetchProducts();
  }, []);

  useEffect(() => {
    if (searchQuery && searchQuery.trim()) {
      performSearch(searchQuery);
    } else {
      setSearchResults(null);
    }
  }, [searchQuery]);

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

  const performSearch = async (query) => {
    setSearching(true);
    try {
      const response = await productApi.search(query);
      setSearchResults(response.data.data || []);
    } catch (error) {
      console.error('Search failed:', error);
      // Fall back to client-side filtering
      const filtered = products.filter(p => 
        p.name?.toLowerCase().includes(query.toLowerCase()) ||
        p.description?.toLowerCase().includes(query.toLowerCase())
      );
      setSearchResults(filtered);
    } finally {
      setSearching(false);
    }
  };

  const clearSearch = () => {
    setSearchQuery('');
    setSearchResults(null);
  };

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
      </div>
    );
  }

  const displayProducts = searchResults !== null ? searchResults : products;
  const isSearching = searchResults !== null;

  return (
    <div>
      <h1 className="page-title">
        {isSearching ? `Search Results for "${searchQuery}"` : 'Products'}
      </h1>
      <p className="page-subtitle">
        {isSearching ? (
          <span className="search-info">
            Found {displayProducts.length} product{displayProducts.length !== 1 ? 's' : ''}
            <button className="clear-search-btn" onClick={clearSearch}>
              <X size={14} />
              Clear Search
            </button>
          </span>
        ) : (
          'Browse our catalog and add items to your cart'
        )}
      </p>

      {searching && (
        <div className="searching-overlay">
          <div className="spinner"></div>
          <p>Searching...</p>
        </div>
      )}

      {displayProducts.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">{isSearching ? '🔍' : '📦'}</div>
          <h2 className="empty-title">
            {isSearching ? 'No Results Found' : 'No Products Yet'}
          </h2>
          <p className="empty-text">
            {isSearching ? (
              <>
                No products match your search.
                <button className="link-button" onClick={clearSearch}>View all products</button>
              </>
            ) : (
              <>
                Create some products using the API:
                <br />
                <code>POST http://localhost:8082/api/products</code>
              </>
            )}
          </p>
        </div>
      ) : (
        <div className="product-grid">
          {displayProducts.map(product => (
            <ProductCard 
              key={product.id} 
              product={product} 
              onAddToCart={addToCart}
            />
          ))}
        </div>
      )}

      {!isSearching && <Recommendations userId={userId} onAddToCart={addToCart} />}
    </div>
  );
}
