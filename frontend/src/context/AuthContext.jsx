import { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check for existing session
    const accessToken = localStorage.getItem('orderly_access_token');
    const savedUser = localStorage.getItem('orderly_user');
    
    if (accessToken && savedUser) {
      setUser(JSON.parse(savedUser));
      // Verify token is still valid
      verifyToken();
    }
    setLoading(false);
  }, []);

  const verifyToken = async () => {
    try {
      const response = await authApi.me();
      if (response.data.success) {
        const userData = response.data.data;
        const userObj = {
          id: userData.id,
          email: userData.email,
          name: `${userData.firstName} ${userData.lastName}`,
          firstName: userData.firstName,
          lastName: userData.lastName,
          roles: userData.roles || [],
        };
        setUser(userObj);
        localStorage.setItem('orderly_user', JSON.stringify(userObj));
      }
    } catch (error) {
      // Token invalid, clear everything
      logout();
    }
  };

  const register = async (email, password, firstName, lastName) => {
    const response = await authApi.register({ email, password, firstName, lastName });
    
    if (response.data.success) {
      const { accessToken, refreshToken, user: userData } = response.data.data;
      
      localStorage.setItem('orderly_access_token', accessToken);
      localStorage.setItem('orderly_refresh_token', refreshToken);
      
      const userObj = {
        id: userData.id,
        email: userData.email,
        name: `${userData.firstName} ${userData.lastName}`,
        firstName: userData.firstName,
        lastName: userData.lastName,
        roles: userData.roles || [],
      };
      
      setUser(userObj);
      localStorage.setItem('orderly_user', JSON.stringify(userObj));
      
      return userObj;
    }
    
    throw new Error(response.data.message || 'Registration failed');
  };

  const login = async (email, password) => {
    const response = await authApi.login({ email, password });
    
    if (response.data.success) {
      const { accessToken, refreshToken, user: userData } = response.data.data;
      
      localStorage.setItem('orderly_access_token', accessToken);
      localStorage.setItem('orderly_refresh_token', refreshToken);
      
      const userObj = {
        id: userData.id,
        email: userData.email,
        name: `${userData.firstName} ${userData.lastName}`,
        firstName: userData.firstName,
        lastName: userData.lastName,
        roles: userData.roles || [],
      };
      
      setUser(userObj);
      localStorage.setItem('orderly_user', JSON.stringify(userObj));
      
      return userObj;
    }
    
    throw new Error(response.data.message || 'Login failed');
  };

  const logout = async () => {
    try {
      const refreshToken = localStorage.getItem('orderly_refresh_token');
      if (refreshToken) {
        await authApi.logout(refreshToken);
      }
    } catch (error) {
      // Ignore logout errors
    }
    
    setUser(null);
    localStorage.removeItem('orderly_access_token');
    localStorage.removeItem('orderly_refresh_token');
    localStorage.removeItem('orderly_user');
  };

  const isAdmin = () => {
    return user?.roles?.includes('ADMIN') || false;
  };

  const value = {
    user,
    userId: user?.id || 'guest',
    isAuthenticated: !!user,
    isAdmin: isAdmin(),
    login,
    register,
    logout,
    loading,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}
