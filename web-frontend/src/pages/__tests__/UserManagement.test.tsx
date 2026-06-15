import { vi, describe, it, expect, beforeEach } from 'vitest';
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import UserManagement from '../UserManagement';
import { ToastProvider } from '../../contexts/ToastContext';

vi.mock('../../services/api', () => ({
  userApi: {
    getAllUsers: vi.fn(),
  },
  roleApi: {
    getAllRoles: vi.fn(),
  },
}));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

import { userApi, roleApi } from '../../services/api';
import type { User, ApiResponse } from '../../types';

const mockUsers: User[] = [
  {
    id: 1,
    username: 'admin',
    email: 'admin@example.com',
    fullName: '管理员',
    role: 'ADMIN',
    status: 'ACTIVE',
    createdAt: '2024-01-01T00:00:00Z',
  } as User,
  {
    id: 2,
    username: 'user1',
    email: 'user1@example.com',
    fullName: '用户1',
    role: 'USER',
    status: 'ACTIVE',
    createdAt: '2024-01-02T00:00:00Z',
  } as User,
];

describe('UserManagement', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (userApi.getAllUsers as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200,
      message: 'Success',
      data: [],
      timestamp: new Date().toISOString(),
      success: true,
    } as ApiResponse<User[]>);
    (roleApi.getAllRoles as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200,
      message: 'Success',
      data: [],
      timestamp: new Date().toISOString(),
      success: true,
    });
  });

  it('应该显示用户管理标题', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <UserManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('用户管理')).toBeInTheDocument();
    }, { timeout: 5000 });
  });

  it('应该渲染用户数据表格', async () => {
    (userApi.getAllUsers as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200,
      message: 'Success',
      data: mockUsers,
      timestamp: new Date().toISOString(),
      success: true,
    } as ApiResponse<User[]>);

    render(
      <BrowserRouter>
        <ToastProvider>
          <UserManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('table')).toBeInTheDocument();
    }, { timeout: 5000 });
  });
});
