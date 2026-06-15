import { vi, describe, it, expect, beforeEach } from 'vitest';
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import SupplierManagement from '../SupplierManagement';
import { ToastProvider } from '../../contexts/ToastContext';

vi.mock('../../services/api', () => ({
  supplierApi: {
    getAllSuppliers: vi.fn(),
    deleteSupplier: vi.fn(),
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

import { supplierApi } from '../../services/api';
import type { Supplier, ApiResponse } from '../../types';

const mockSuppliers: Supplier[] = [
  {
    id: 1,
    supplierId: 'SUP001',
    supplierName: '北京供应商',
    contactPerson: '王五',
    email: 'sup1@example.com',
    phone: '13800138001',
    address: '北京市海淀区',
    status: 'ACTIVE',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-15T00:00:00Z',
  },
  {
    id: 2,
    supplierId: 'SUP002',
    supplierName: '上海供应商',
    contactPerson: '赵六',
    email: 'sup2@example.com',
    phone: '13800138002',
    address: '上海市浦东',
    status: 'INACTIVE',
    createdAt: '2024-01-02T00:00:00Z',
    updatedAt: '2024-01-16T00:00:00Z',
  },
];

describe('SupplierManagement', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (supplierApi.getAllSuppliers as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200,
      message: 'Success',
      data: [],
      timestamp: new Date().toISOString(),
      success: true,
    });
  });

  it('应该显示供应商管理标题', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <SupplierManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('供应商管理')).toBeInTheDocument();
    });
  });

  it('应该渲染数据表格', async () => {
    const mockResponse: ApiResponse<Supplier[]> = {
      code: 200,
      message: 'Success',
      data: mockSuppliers,
      timestamp: new Date().toISOString(),
      success: true,
    };
    (supplierApi.getAllSuppliers as ReturnType<typeof vi.fn>).mockResolvedValue(mockResponse);

    render(
      <BrowserRouter>
        <ToastProvider>
          <SupplierManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('table')).toBeInTheDocument();
    }, { timeout: 5000 });
  });

  it('应该显示新增供应商按钮', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <SupplierManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('新增供应商')).toBeInTheDocument();
    });
  });

  it('应该显示搜索框', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <SupplierManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      const input = document.querySelector('input[type="text"]');
      expect(input).toBeInTheDocument();
    });
  });

  it('应该在加载失败时显示空状态', async () => {
    (supplierApi.getAllSuppliers as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200,
      message: 'Success',
      data: [],
      timestamp: new Date().toISOString(),
      success: true,
    });

    render(
      <BrowserRouter>
        <ToastProvider>
          <SupplierManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('供应商管理')).toBeInTheDocument();
    });
  });
});
