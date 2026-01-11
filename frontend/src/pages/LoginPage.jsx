import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, LogIn, Mail, Lock, UserPlus, User, Eye, EyeOff } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const [isRegister, setIsRegister] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
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
    <div className="login-page">
      <div className="login-background">
        <div className="bg-gradient"></div>
        <div className="bg-pattern"></div>
      </div>
      
      <div className="login-container">
        <div className="login-card">
          <div className="login-header">
            <div className="login-logo">
              <Box size={32} strokeWidth={1.5} />
            </div>
            <h1 className="login-title">
              {isRegister ? 'Create Account' : 'Welcome Back'}
            </h1>
            <p className="login-subtitle">
              {isRegister 
                ? 'Join Orderly to start your shopping journey' 
                : 'Sign in to continue to Orderly'}
            </p>
          </div>

          <form onSubmit={handleSubmit} className="login-form">
            {error && (
              <div className="login-error">
                <span className="error-icon">!</span>
                {error}
              </div>
            )}

            {isRegister && (
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">First Name</label>
                  <div className="input-wrapper">
                    <User size={18} className="input-icon" />
                    <input
                      type="text"
                      className="form-input"
                      placeholder="John"
                      value={formData.firstName}
                      onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                    />
                  </div>
                </div>
                <div className="form-group">
                  <label className="form-label">Last Name</label>
                  <div className="input-wrapper">
                    <User size={18} className="input-icon" />
                    <input
                      type="text"
                      className="form-input"
                      placeholder="Doe"
                      value={formData.lastName}
                      onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                    />
                  </div>
                </div>
              </div>
            )}

            <div className="form-group">
              <label className="form-label">Email Address</label>
              <div className="input-wrapper">
                <Mail size={18} className="input-icon" />
                <input
                  type="email"
                  className="form-input"
                  placeholder="you@example.com"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <div className="input-wrapper">
                <Lock size={18} className="input-icon" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  className="form-input"
                  placeholder="••••••••"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                />
                <button 
                  type="button" 
                  className="password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
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
            <span className="footer-text">
              {isRegister ? 'Already have an account?' : "Don't have an account?"}
            </span>
            <button 
              type="button" 
              className="toggle-btn" 
              onClick={toggleMode}
            >
              {isRegister ? 'Sign In' : 'Create one'}
            </button>
          </div>
        </div>

        <div className="login-features">
          <div className="feature">
            <div className="feature-icon">🛒</div>
            <div className="feature-text">
              <strong>Easy Shopping</strong>
              <span>Browse products and add to cart</span>
            </div>
          </div>
          <div className="feature">
            <div className="feature-icon">📦</div>
            <div className="feature-text">
              <strong>Order Tracking</strong>
              <span>Track your orders in real-time</span>
            </div>
          </div>
          <div className="feature">
            <div className="feature-icon">✨</div>
            <div className="feature-text">
              <strong>AI Recommendations</strong>
              <span>Personalized product suggestions</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
