import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { RefreshCw, ShoppingBag, Eye } from 'lucide-react';
import { orderApi } from '../api';
import { useCart } from '../App';

function StatusBadge({ status }) {
  const statusClass = `status-badge status-${status?.toLowerCase()}`;
  return <span className={statusClass}>{status}</span>;
}

function OrderCard({ order, onRefresh }) {
  const [refreshing, setRefreshing] = useState(false);

  const handleRefresh = async () => {
    setRefreshing(true);
    try {
      await onRefresh(order.id);
    } finally {
      setRefreshing(false);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="card order-card">
      <div className="order-header">
        <div>
          <div className="order-id">Order #{order.id?.slice(-8)}</div>
          <div className="order-date">{formatDate(order.createdAt)}</div>
        </div>
        <div className="flex items-center gap-2">
          <StatusBadge status={order.status} />
          {order.status === 'PENDING' && (
            <button 
              className="btn btn-sm btn-secondary" 
              onClick={handleRefresh}
              disabled={refreshing}
              title="Refresh status"
            >
              <RefreshCw size={14} className={refreshing ? 'spinning' : ''} />
            </button>
          )}
        </div>
      </div>

      <div className="order-items">
        {order.items?.map((item, index) => (
          <div key={index} className="order-item">
            <span>{item.productName} × {item.quantity}</span>
            <span>${item.total?.toFixed(2)}</span>
          </div>
        ))}
      </div>

      {order.failureReason && (
        <div style={{ 
          background: 'rgba(229, 49, 112, 0.1)', 
          padding: '0.75rem', 
          borderRadius: '6px',
          marginBottom: '1rem',
          color: 'var(--error)',
          fontSize: '0.9rem'
        }}>
          ⚠️ {order.failureReason}
        </div>
      )}

      <div className="order-footer">
        <div>
          <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            {order.shippingAddress?.city}, {order.shippingAddress?.state}
          </div>
        </div>
        <div className="order-total">
          ${order.totalAmount?.toFixed(2)}
        </div>
      </div>
    </div>
  );
}

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const { userId } = useCart();
  const navigate = useNavigate();

  useEffect(() => {
    fetchOrders();
    // Poll for updates every 5 seconds if there are pending orders
    const interval = setInterval(() => {
      if (orders.some(o => o.status === 'PENDING')) {
        fetchOrders(true);
      }
    }, 5000);
    return () => clearInterval(interval);
  }, []);

  const fetchOrders = async (silent = false) => {
    if (!silent) setLoading(true);
    try {
      const response = await orderApi.getUserOrders(userId);
      const ordersList = response.data.data || [];
      // Sort by date, newest first
      ordersList.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      setOrders(ordersList);
    } catch (error) {
      console.error('Failed to fetch orders:', error);
    } finally {
      if (!silent) setLoading(false);
    }
  };

  const refreshOrder = async (orderId) => {
    try {
      const response = await orderApi.getOrder(orderId);
      const updatedOrder = response.data.data;
      setOrders(orders.map(o => o.id === orderId ? updatedOrder : o));
    } catch (error) {
      console.error('Failed to refresh order:', error);
    }
  };

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
      </div>
    );
  }

  if (orders.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-icon">📋</div>
        <h2 className="empty-title">No Orders Yet</h2>
        <p className="empty-text">Place your first order to see it here!</p>
        <button className="btn btn-primary" onClick={() => navigate('/')}>
          <ShoppingBag size={18} />
          Browse Products
        </button>
      </div>
    );
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-2">
        <div>
          <h1 className="page-title">Your Orders</h1>
          <p className="page-subtitle">{orders.length} order{orders.length !== 1 ? 's' : ''}</p>
        </div>
        <button className="btn btn-secondary" onClick={() => fetchOrders()}>
          <RefreshCw size={16} />
          Refresh
        </button>
      </div>

      <div className="orders-list">
        {orders.map(order => (
          <OrderCard 
            key={order.id} 
            order={order} 
            onRefresh={refreshOrder}
          />
        ))}
      </div>

      <style>{`
        .spinning {
          animation: spin 1s linear infinite;
        }
        @keyframes spin {
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
}
