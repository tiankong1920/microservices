import { vi, describe, it, expect, beforeEach } from 'vitest';
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import OrderCreate from '../OrderCreate';
import { ToastProvider } from '../../contexts/ToastContext';

vi.mock('../../services/api', () => ({
  orderApi: {
    createOrder: vi.fn(),
  },
  productApi: {
    getAllProducts: vi.fn(),
  },
  customerApi: {
    getAllCustomers: vi.fn(),
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

import { orderApi, productApi, customerApi } from '../../services/api';
import type { Customer, Product, ApiResponse } from '../../types';

describe('OrderCreate', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (productApi.getAllProducts as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200, message: 'Success', data: [] as Product[], timestamp: new Date().toISOString(), success: true,
    } as ApiResponse<Product[]>);
    (customerApi.getAllCustomers as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200, message: 'Success', data: [] as Customer[], timestamp: new Date().toISOString(), success: true,
    } as ApiResponse<Customer[]>);
  });

  it('应该显示新建订单标题', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <OrderCreate />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('创建订单')).toBeInTheDocument();
    });
  });

  it('应该加载产品和客户数据', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <OrderCreate />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(productApi.getAllProducts).toHaveBeenCalled();
      expect(customerApi.getAllCustomers).toHaveBeenCalled();
    });
  });
});
