import { vi, describe, it, expect, beforeEach } from 'vitest';
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import ProductDetail from '../ProductDetail';
import { ToastProvider } from '../../contexts/ToastContext';

vi.mock('../../services/api', () => ({
  productApi: {
    getProductById: vi.fn(),
  },
}));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useParams: () => ({ id: '1' }),
    useNavigate: () => mockNavigate,
  };
});

import { productApi } from '../../services/api';
import type { Product, ApiResponse } from '../../types';

const mockProduct: Product = {
  id: 1,
  productId: 'P001',
  productName: '商品A',
  productCode: 'CODE001',
  categoryId: 1,
  unit: '个',
  price: 100,
  status: 'ACTIVE',
  createdAt: '2024-01-01T00:00:00Z',
} as Product;

describe('ProductDetail', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (productApi.getProductById as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200,
      message: 'Success',
      data: mockProduct,
      timestamp: new Date().toISOString(),
      success: true,
    } as ApiResponse<Product>);
  });

  it('应该显示商品详情标题', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <ProductDetail />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('产品详情')).toBeInTheDocument();
    });
  });

  it('应该显示编辑按钮', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <ProductDetail />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('编辑')).toBeInTheDocument();
    });
  });
});
