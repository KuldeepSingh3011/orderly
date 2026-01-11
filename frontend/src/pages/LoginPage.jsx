import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, LogIn, Mail, User, Lock, UserPlus } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const [isRegister, setIsRegister] = useState(false);
  const [formData, setFormData] = useState({ 
    email: '', 
    password: '',
    firstName: '',
    lastName: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login, register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isRegister) {
        if (!formData.firstName.trim() || !formData.lastName.trim()) {
          setError('Please enter your name');
          setLoading(false);
          return;
        }
        if (formData.password.length < 6) {
          setError('Password must be at least 6 characters');
          setLoading(false);
          return;
        }
        await register(formData.email, formData.password, formData.firstName, formData.lastName);
      } else {
        if (!formData.email.trim() || !formData.password.trim()) {
          setError('Please fill in all fields');
          setLoading(false);
          return;
        }
        await login(formData.email, formData.password);
      }
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Authentication failed');
    } finally {
      setLoading(false);
    }
  };

  const toggleMode = () => {
    setIsRegister(!isRegister);
    setError('');
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <div className="login-logo">
            <Box size={40} />
          </div>
          <h1 className="login-title">Welcome to Orderly</h1>
          <p className="login-subtitle">
            {isRegister ? 'Create an account to start shopping' : 'Sign in to your account'}
          </p>
        </div>

        <form onSubmit={handleSubmit}>
          {error && (
            <div className="login-error">{error}</div>
          )}

          {isRegister && (
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">
                  <User size={16} style={{ marginRight: '0.5rem' }} />
                  First Name
                </label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="John"
                  value={formData.firstName}
                  onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label className="form-label">
                  <User size={16} style={{ marginRight: '0.5rem' }} />
                  Last Name
                </label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="Doe"
                  value={formData.lastName}
                  onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                />
              </div>
            </div>
          )}

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

          <div className="form-group">
            <label className="form-label">
              <Lock size={16} style={{ marginRight: '0.5rem' }} />
              Password
            </label>
            <input
              type="password"
              className="form-input"
              placeholder="••••••••"
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
            />
          </div>

          <button type="submit" className="btn btn-primary login-btn" disabled={loading}>
            {loading ? (
              <span className="spinner-small"></span>
            ) : isRegister ? (
              <>
                <UserPlus size={18} />
                Create Account
              </>
            ) : (
              <>
                <LogIn size={18} />
                Sign In
              </>
            )}
          </button>
        </form>

        <div className="login-footer">
          <p>
            {isRegister ? 'Already have an account?' : "Don't have an account?"}
            <button 
              type="button" 
              className="link-button" 
              onClick={toggleMode}
            >
              {isRegister ? 'Sign In' : 'Register'}
            </button>
          </p>
          <p className="admin-hint">
            Admin login: admin@orderly.com / admin123
          </p>
        </div>
      </div>
    </div>
  );
}
