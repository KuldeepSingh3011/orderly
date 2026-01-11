import { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check localStorage for existing session
    const savedUser = localStorage.getItem('orderly_user');
    if (savedUser) {
      setUser(JSON.parse(savedUser));
    }
    setLoading(false);
  }, []);

  const login = (userData) => {
    const userObj = {
      id: userData.email.replace(/[^a-zA-Z0-9]/g, '-'),
      email: userData.email,
      name: userData.name,
      loginTime: new Date().toISOString(),
    };
    setUser(userObj);
    localStorage.setItem('orderly_user', JSON.stringify(userObj));
    return userObj;
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('orderly_user');
  };

  const value = {
    user,
    userId: user?.id || 'guest',
    isAuthenticated: !!user,
    login,
    logout,
    loading,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}
