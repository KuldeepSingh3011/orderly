import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, LogIn, Mail, User } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const [formData, setFormData] = useState({ name: '', email: '' });
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    setError('');

    if (!formData.name.trim() || !formData.email.trim()) {
      setError('Please fill in all fields');
      return;
    }

    if (!formData.email.includes('@')) {
      setError('Please enter a valid email');
      return;
    }

    login(formData);
    navigate('/');
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <div className="login-logo">
            <Box size={40} />
          </div>
          <h1 className="login-title">Welcome to Orderly</h1>
          <p className="login-subtitle">Sign in to start shopping</p>
        </div>

        <form onSubmit={handleSubmit}>
          {error && (
            <div className="login-error">{error}</div>
          )}

          <div className="form-group">
            <label className="form-label">
              <User size={16} style={{ marginRight: '0.5rem' }} />
              Your Name
            </label>
            <input
              type="text"
              className="form-input"
              placeholder="John Doe"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">
              <Mail size={16} style={{ marginRight: '0.5rem' }} />
              Email Address
            </label>
            <input
              type="email"
              className="form-input"
              placeholder="john@example.com"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            />
          </div>

          <button type="submit" className="btn btn-primary login-btn">
            <LogIn size={18} />
            Sign In
          </button>
        </form>

        <div className="login-footer">
          <p>This is a demo app. No password required.</p>
        </div>
      </div>
    </div>
  );
}
