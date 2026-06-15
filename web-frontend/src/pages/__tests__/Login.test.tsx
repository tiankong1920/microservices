import { vi, describe, it, expect, beforeEach } from 'vitest';
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import Login from '../Login';
import { ToastProvider } from '../../contexts/ToastContext';
import { AuthProvider } from '../../contexts/AuthContext';

const mockLogin = vi.fn();
const mockNavigate = vi.fn();

vi.mock('../../contexts/AuthContext', () => ({
  useAuth: () => ({
    login: mockLogin,
    user: null,
    isAuthenticated: false,
  }),
  AuthProvider: ({ children }: { children: React.ReactNode }) => children,
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Login', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockLogin.mockResolvedValue(undefined);
  });

  it('应该显示登录页面标题', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <AuthProvider>
            <Login />
          </AuthProvider>
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('进销存管理系统')).toBeInTheDocument();
    });
  });

  it('应该显示用户名和密码输入框', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <AuthProvider>
            <Login />
          </AuthProvider>
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByLabelText(/用户名/)).toBeInTheDocument();
      expect(screen.getByLabelText(/密码/)).toBeInTheDocument();
    });
  });

  it('应该显示登录按钮', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <AuthProvider>
            <Login />
          </AuthProvider>
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      const button = screen.getByRole('button', { name: /登录/ });
      expect(button).toBeInTheDocument();
    });
  });

  it('应该在输入框中接受用户输入', async () => {
    const user = userEvent.setup();
    render(
      <BrowserRouter>
        <ToastProvider>
          <AuthProvider>
            <Login />
          </AuthProvider>
        </ToastProvider>
      </BrowserRouter>
    );

    const usernameInput = await screen.findByLabelText(/用户名/);
    await user.type(usernameInput, 'admin');

    expect(usernameInput).toHaveValue('admin');
  });

  it('应该在提交时调用登录函数', async () => {
    const user = userEvent.setup();
    render(
      <BrowserRouter>
        <ToastProvider>
          <AuthProvider>
            <Login />
          </AuthProvider>
        </ToastProvider>
      </BrowserRouter>
    );

    const usernameInput = await screen.findByLabelText(/用户名/);
    const passwordInput = screen.getByLabelText(/密码/);
    await user.type(usernameInput, 'admin');
    await user.type(passwordInput, 'password123');

    const submitButton = screen.getByRole('button', { name: /登录/ });
    await user.click(submitButton);

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('admin', 'password123');
    });
  });
});
