import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Trash2, ShoppingBag, X } from 'lucide-react';
import { orderApi } from '../api';
import { useCart } from '../App';

function CheckoutModal({ isOpen, onClose, onSubmit, loading }) {
  const [formData, setFormData] = useState({
    fullName: '',
    street: '',
    city: '',
    state: '',
    zipCode: '',
    country: 'USA',
    phone: '',
  });

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">Shipping Address</h2>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Full Name</label>
            <input
              type="text"
              name="fullName"
              className="form-input"
              value={formData.fullName}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-group">
            <label className="form-label">Street Address</label>
            <input
              type="text"
              name="street"
              className="form-input"
              value={formData.street}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">City</label>
              <input
                type="text"
                name="city"
                className="form-input"
                value={formData.city}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">State</label>
              <input
                type="text"
                name="state"
                className="form-input"
                value={formData.state}
                onChange={handleChange}
                required
              />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">ZIP Code</label>
              <input
                type="text"
                name="zipCode"
                className="form-input"
                value={formData.zipCode}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Country</label>
              <input
                type="text"
                name="country"
                className="form-input"
                value={formData.country}
                onChange={handleChange}
                required
              />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">Phone</label>
            <input
              type="tel"
              name="phone"
              className="form-input"
              value={formData.phone}
              onChange={handleChange}
              required
            />
          </div>
          <button 
            type="submit" 
            className="btn btn-success" 
            style={{ width: '100%', marginTop: '1rem' }}
            disabled={loading}
          >
            {loading ? 'Processing...' : 'Place Order'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default function CartPage() {
  const [showCheckout, setShowCheckout] = useState(false);
  const [loading, setLoading] = useState(false);
  const { cart, userId, clearCart, showToast } = useCart();
  const navigate = useNavigate();

  const subtotal = cart.reduce((sum, item) => sum + (item.totalPrice || item.price * item.quantity), 0);
  const tax = subtotal * 0.08;
  const shipping = subtotal >= 50 ? 0 : 5.99;
  const total = subtotal + tax + shipping;

  const handleCheckout = async (shippingAddress) => {
    setLoading(true);
    try {
      const response = await orderApi.createOrder(userId, shippingAddress);
      await clearCart();
      showToast('Order placed successfully!', 'success');
      setShowCheckout(false);
      navigate('/orders');
    } catch (error) {
      showToast('Failed to place order', 'error');
    } finally {
      setLoading(false);
    }
  };

  if (cart.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-icon">🛒</div>
        <h2 className="empty-title">Your Cart is Empty</h2>
        <p className="empty-text">Add some products to get started!</p>
        <button className="btn btn-primary" onClick={() => navigate('/')}>
          <ShoppingBag size={18} />
          Browse Products
        </button>
      </div>
    );
  }

  return (
    <div>
      <h1 className="page-title">Shopping Cart</h1>
      <p className="page-subtitle">{cart.length} item{cart.length !== 1 ? 's' : ''} in your cart</p>

      <div className="cart-container">
        <div className="cart-items">
          {cart.map((item) => (
            <div key={item.productId} className="card cart-item">
              <div className="cart-item-image">📦</div>
              <div className="cart-item-details">
                <div className="cart-item-name">{item.productName}</div>
                <div className="cart-item-price">${item.price?.toFixed(2)} each</div>
              </div>
              <div className="cart-item-quantity">
                <span>Qty: {item.quantity}</span>
              </div>
              <div style={{ fontWeight: 600, minWidth: '80px', textAlign: 'right' }}>
                ${(item.totalPrice || item.price * item.quantity)?.toFixed(2)}
              </div>
            </div>
          ))}
        </div>

        <div className="card cart-summary">
          <h3 style={{ marginBottom: '1rem' }}>Order Summary</h3>
          <div className="summary-row">
            <span>Subtotal</span>
            <span>${subtotal.toFixed(2)}</span>
          </div>
          <div className="summary-row">
            <span>Tax (8%)</span>
            <span>${tax.toFixed(2)}</span>
          </div>
          <div className="summary-row">
            <span>Shipping</span>
            <span>{shipping === 0 ? 'FREE' : `$${shipping.toFixed(2)}`}</span>
          </div>
          <div className="summary-row summary-total">
            <span>Total</span>
            <span>${total.toFixed(2)}</span>
          </div>
          <button 
            className="btn btn-success" 
            style={{ width: '100%', marginTop: '1rem' }}
            onClick={() => setShowCheckout(true)}
          >
            Proceed to Checkout
          </button>
          <button 
            className="btn btn-secondary" 
            style={{ width: '100%', marginTop: '0.5rem' }}
            onClick={clearCart}
          >
            <Trash2 size={16} />
            Clear Cart
          </button>
        </div>
      </div>

      <CheckoutModal 
        isOpen={showCheckout}
        onClose={() => setShowCheckout(false)}
        onSubmit={handleCheckout}
        loading={loading}
      />
    </div>
  );
}
