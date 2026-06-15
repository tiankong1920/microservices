import React, { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import { authApi } from '../services/api';
import type { LoginRequest, User } from '../types';

interface AuthState {
  user: User | null;
  token: string | null;
  loading: boolean;
}

interface AuthContextType extends AuthState {
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  // On mount, check localStorage for existing token and verify with backend
  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('token');
      if (storedToken) {
        try {
          setToken(storedToken);
          const response = await authApi.getCurrentUser();
          if (response.data) {
            setUser(response.data);
          } else {
            throw new Error('Invalid token');
          }
        } catch {
          // Token expired or invalid — clear auth state
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          setToken(null);
          setUser(null);
        }
      }
      setLoading(false);
    };
    initAuth();
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    const loginPayload = { username, password } as LoginRequest;
    const response = await authApi.login(loginPayload);
    if (response.data) {
      const { accessToken } = response.data;
      localStorage.setItem('token', accessToken);
      setToken(accessToken);

      // Fetch full user profile after successful login
      try {
        const userResponse = await authApi.getCurrentUser();
        if (userResponse.data) {
          setUser(userResponse.data);
          localStorage.setItem('user', JSON.stringify(userResponse.data));
        }
      } catch {
        // If profile fetch fails, construct minimal user from login response
        const minimalUser: User = {
          id: 0,
          username: response.data.username,
          roles: response.data.roles,
          role: response.data.roles?.[0] ?? '',
          email: '',
          fullName: '',
          status: 'ACTIVE',
          createdAt: new Date().toISOString(),
        };
        setUser(minimalUser);
        localStorage.setItem('user', JSON.stringify(minimalUser));
      }
    }
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  }, []);

  const value: AuthContextType = {
    user,
    token,
    loading,
    isAuthenticated: !!token && !!user,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
