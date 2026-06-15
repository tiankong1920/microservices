import { vi, describe, it, expect, beforeEach } from 'vitest';
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import ProductManagement from '../ProductManagement';
import { ToastProvider } from '../../contexts/ToastContext';

vi.mock('../../services/api', () => ({
  productApi: {
    getAllProducts: vi.fn(),
    deleteProduct: vi.fn(),
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

import { productApi } from '../../services/api';
import type { Product, ApiResponse } from '../../types';

const mockProducts: Product[] = [
  {
    id: 1,
    productId: 'P001',
    productName: '商品A',
    productCode: 'CODE001',
    categoryId: 1,
    unit: '个',
    price: 100,
    status: 'ACTIVE',
    createdAt: '2024-01-01T00:00:00Z',
  } as Product,
  {
    id: 2,
    productId: 'P002',
    productName: '商品B',
    productCode: 'CODE002',
    categoryId: 1,
    unit: '件',
    price: 200,
    status: 'INACTIVE',
    createdAt: '2024-01-02T00:00:00Z',
  } as Product,
];

describe('ProductManagement', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (productApi.getAllProducts as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200,
      message: 'Success',
      data: [],
      timestamp: new Date().toISOString(),
      success: true,
    } as ApiResponse<Product[]>);
  });

  it('应该显示商品管理标题', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <ProductManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('产品管理')).toBeInTheDocument();
    });
  });

  it('应该渲染商品数据表格', async () => {
    (productApi.getAllProducts as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200,
      message: 'Success',
      data: mockProducts,
      timestamp: new Date().toISOString(),
      success: true,
    } as ApiResponse<Product[]>);

    render(
      <BrowserRouter>
        <ToastProvider>
          <ProductManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('table')).toBeInTheDocument();
    }, { timeout: 5000 });
  });

  it('应该显示新增商品按钮', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <ProductManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('产品管理')).toBeInTheDocument();
    });
  });
});
