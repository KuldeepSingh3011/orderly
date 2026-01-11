import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Package, Plus, Edit2, Trash2, DollarSign, 
  Archive, CheckCircle, XCircle, Search, Users, 
  TrendingUp, ShoppingBag, AlertCircle
} from 'lucide-react';
import { adminApi } from '../api';
import { useAuth } from '../context/AuthContext';

export default function AdminPage() {
  const [activeTab, setActiveTab] = useState('products');
  const [products, setProducts] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const { isAdmin } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAdmin) {
      navigate('/');
      return;
    }
    fetchData();
  }, [isAdmin, navigate]);

  const fetchData = async () => {
    setLoading(true);
    try {
      if (activeTab === 'products') {
        const response = await adminApi.getProducts(true);
        setProducts(response.data.data || []);
      } else if (activeTab === 'users') {
        const response = await adminApi.getUsers();
        setUsers(response.data.data || []);
      }
    } catch (error) {
      console.error('Failed to fetch data:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [activeTab]);

  const handleCreateProduct = () => {
    setEditingProduct({
      name: '',
      description: '',
      category: '',
      price: '',
      stockQuantity: '',
      sku: '',
      imageUrl: ''
    });
    setShowModal(true);
  };

  const handleEditProduct = (product) => {
    setEditingProduct({ ...product });
    setShowModal(true);
  };

  const handleSaveProduct = async () => {
    try {
      const productData = {
        ...editingProduct,
        price: parseFloat(editingProduct.price),
        stockQuantity: parseInt(editingProduct.stockQuantity, 10)
      };

      if (editingProduct.id) {
        await adminApi.updateProduct(editingProduct.id, productData);
      } else {
        await adminApi.createProduct(productData);
      }
      setShowModal(false);
      setEditingProduct(null);
      fetchData();
    } catch (error) {
      console.error('Failed to save product:', error);
      alert('Failed to save product');
    }
  };

  const handleDeleteProduct = async (productId) => {
    if (!confirm('Are you sure you want to delete this product?')) return;
    try {
      await adminApi.deleteProduct(productId);
      fetchData();
    } catch (error) {
      console.error('Failed to delete product:', error);
    }
  };

  const handleToggleActive = async (product) => {
    try {
      if (product.active) {
        await adminApi.deactivateProduct(product.id);
      } else {
        await adminApi.activateProduct(product.id);
      }
      fetchData();
    } catch (error) {
      console.error('Failed to toggle product status:', error);
    }
  };

  const handleToggleUserStatus = async (user) => {
    try {
      if (user.enabled) {
        await adminApi.disableUser(user.id);
      } else {
        await adminApi.enableUser(user.id);
      }
      fetchData();
    } catch (error) {
      console.error('Failed to toggle user status:', error);
    }
  };

  const handleQuickStockUpdate = async (productId, adjustment) => {
    try {
      await adminApi.adjustStock(productId, adjustment);
      fetchData();
    } catch (error) {
      console.error('Failed to adjust stock:', error);
    }
  };

  const filteredProducts = products.filter(p => 
    p.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    p.sku?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const stats = {
    totalProducts: products.length,
    activeProducts: products.filter(p => p.active).length,
    lowStock: products.filter(p => p.stockQuantity < 10).length,
    totalUsers: users.length
  };

  if (loading && products.length === 0) {
    return (
      <div className="loading">
        <div className="spinner"></div>
      </div>
    );
  }

  return (
    <div className="admin-page">
      <div className="admin-header">
        <h1>Admin Dashboard</h1>
        <p>Manage products, inventory, and users</p>
      </div>

      {/* Stats Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon products">
            <Package size={24} />
          </div>
          <div className="stat-info">
            <span className="stat-value">{stats.totalProducts}</span>
            <span className="stat-label">Total Products</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon active">
            <CheckCircle size={24} />
          </div>
          <div className="stat-info">
            <span className="stat-value">{stats.activeProducts}</span>
            <span className="stat-label">Active Products</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon warning">
            <AlertCircle size={24} />
          </div>
          <div className="stat-info">
            <span className="stat-value">{stats.lowStock}</span>
            <span className="stat-label">Low Stock</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon users">
            <Users size={24} />
          </div>
          <div className="stat-info">
            <span className="stat-value">{stats.totalUsers}</span>
            <span className="stat-label">Users</span>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="admin-tabs">
        <button 
          className={`tab-btn ${activeTab === 'products' ? 'active' : ''}`}
          onClick={() => setActiveTab('products')}
        >
          <Package size={18} />
          Products
        </button>
        <button 
          className={`tab-btn ${activeTab === 'users' ? 'active' : ''}`}
          onClick={() => setActiveTab('users')}
        >
          <Users size={18} />
          Users
        </button>
      </div>

      {/* Products Tab */}
      {activeTab === 'products' && (
        <div className="admin-section">
          <div className="section-header">
            <div className="search-box">
              <Search size={18} />
              <input
                type="text"
                placeholder="Search products..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <button className="btn btn-primary" onClick={handleCreateProduct}>
              <Plus size={18} />
              Add Product
            </button>
          </div>

          <div className="products-table-container">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>SKU</th>
                  <th>Category</th>
                  <th>Price</th>
                  <th>Stock</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredProducts.map(product => (
                  <tr key={product.id} className={!product.active ? 'inactive' : ''}>
                    <td>
                      <div className="product-cell">
                        {product.imageUrl && (
                          <img src={product.imageUrl} alt={product.name} className="product-thumb" />
                        )}
                        <span>{product.name}</span>
                      </div>
                    </td>
                    <td>{product.sku}</td>
                    <td>{product.category}</td>
                    <td>${product.price?.toFixed(2)}</td>
                    <td>
                      <div className="stock-controls">
                        <button 
                          className="stock-btn"
                          onClick={() => handleQuickStockUpdate(product.id, -1)}
                        >-</button>
                        <span className={product.stockQuantity < 10 ? 'low-stock' : ''}>
                          {product.stockQuantity}
                        </span>
                        <button 
                          className="stock-btn"
                          onClick={() => handleQuickStockUpdate(product.id, 1)}
                        >+</button>
                      </div>
                    </td>
                    <td>
                      <span className={`status-badge ${product.active ? 'active' : 'inactive'}`}>
                        {product.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td>
                      <div className="action-buttons">
                        <button 
                          className="action-btn edit"
                          onClick={() => handleEditProduct(product)}
                          title="Edit"
                        >
                          <Edit2 size={16} />
                        </button>
                        <button 
                          className="action-btn toggle"
                          onClick={() => handleToggleActive(product)}
                          title={product.active ? 'Deactivate' : 'Activate'}
                        >
                          {product.active ? <XCircle size={16} /> : <CheckCircle size={16} />}
                        </button>
                        <button 
                          className="action-btn delete"
                          onClick={() => handleDeleteProduct(product.id)}
                          title="Delete"
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Users Tab */}
      {activeTab === 'users' && (
        <div className="admin-section">
          <div className="users-table-container">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>User</th>
                  <th>Email</th>
                  <th>Roles</th>
                  <th>Status</th>
                  <th>Created</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map(user => (
                  <tr key={user.id} className={!user.enabled ? 'inactive' : ''}>
                    <td>{user.firstName} {user.lastName}</td>
                    <td>{user.email}</td>
                    <td>
                      <div className="roles">
                        {user.roles?.map(role => (
                          <span key={role} className={`role-badge ${role.toLowerCase()}`}>
                            {role}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td>
                      <span className={`status-badge ${user.enabled ? 'active' : 'inactive'}`}>
                        {user.enabled ? 'Active' : 'Disabled'}
                      </span>
                    </td>
                    <td>{new Date(user.createdAt).toLocaleDateString()}</td>
                    <td>
                      <button 
                        className={`btn btn-small ${user.enabled ? 'btn-secondary' : 'btn-primary'}`}
                        onClick={() => handleToggleUserStatus(user)}
                      >
                        {user.enabled ? 'Disable' : 'Enable'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Product Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h2>{editingProduct?.id ? 'Edit Product' : 'Add New Product'}</h2>
            
            <div className="form-group">
              <label>Product Name</label>
              <input
                type="text"
                className="form-input"
                value={editingProduct?.name || ''}
                onChange={e => setEditingProduct({...editingProduct, name: e.target.value})}
              />
            </div>

            <div className="form-group">
              <label>SKU</label>
              <input
                type="text"
                className="form-input"
                value={editingProduct?.sku || ''}
                onChange={e => setEditingProduct({...editingProduct, sku: e.target.value})}
              />
            </div>

            <div className="form-group">
              <label>Description</label>
              <textarea
                className="form-input"
                rows="3"
                value={editingProduct?.description || ''}
                onChange={e => setEditingProduct({...editingProduct, description: e.target.value})}
              />
            </div>

            <div className="form-group">
              <label>Category</label>
              <input
                type="text"
                className="form-input"
                value={editingProduct?.category || ''}
                onChange={e => setEditingProduct({...editingProduct, category: e.target.value})}
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Price ($)</label>
                <input
                  type="number"
                  className="form-input"
                  step="0.01"
                  value={editingProduct?.price || ''}
                  onChange={e => setEditingProduct({...editingProduct, price: e.target.value})}
                />
              </div>
              <div className="form-group">
                <label>Stock Quantity</label>
                <input
                  type="number"
                  className="form-input"
                  value={editingProduct?.stockQuantity || ''}
                  onChange={e => setEditingProduct({...editingProduct, stockQuantity: e.target.value})}
                />
              </div>
            </div>

            <div className="form-group">
              <label>Image URL</label>
              <input
                type="url"
                className="form-input"
                value={editingProduct?.imageUrl || ''}
                onChange={e => setEditingProduct({...editingProduct, imageUrl: e.target.value})}
              />
            </div>

            <div className="modal-actions">
              <button className="btn btn-secondary" onClick={() => setShowModal(false)}>
                Cancel
              </button>
              <button className="btn btn-primary" onClick={handleSaveProduct}>
                {editingProduct?.id ? 'Save Changes' : 'Create Product'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
