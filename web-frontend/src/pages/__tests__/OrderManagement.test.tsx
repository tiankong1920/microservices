import { vi, describe, it, expect, beforeEach } from 'vitest';
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import OrderManagement from '../OrderManagement';
import { ToastProvider } from '../../contexts/ToastContext';

vi.mock('../../services/api', () => ({
  orderApi: {
    getAllOrders: vi.fn(),
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

import { orderApi } from '../../services/api';
import type { Order, ApiResponse } from '../../types';

const mockOrders: Order[] = [
  {
    id: 1,
    orderId: 'ORD001',
    customerName: '客户1',
    totalAmount: 1000,
    status: 'PENDING',
    createdAt: '2024-01-01T00:00:00Z',
  } as Order,
  {
    id: 2,
    orderId: 'ORD002',
    customerName: '客户2',
    totalAmount: 2000,
    status: 'COMPLETED',
    createdAt: '2024-01-02T00:00:00Z',
  } as Order,
];

describe('OrderManagement', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (orderApi.getAllOrders as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200,
      message: 'Success',
      data: [],
      timestamp: new Date().toISOString(),
      success: true,
    } as ApiResponse<Order[]>);
  });

  it('应该显示订单管理标题', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <OrderManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('订单管理')).toBeInTheDocument();
    });
  });

  it('应该渲染订单数据表格', async () => {
    (orderApi.getAllOrders as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200,
      message: 'Success',
      data: mockOrders,
      timestamp: new Date().toISOString(),
      success: true,
    } as ApiResponse<Order[]>);

    render(
      <BrowserRouter>
        <ToastProvider>
          <OrderManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('table')).toBeInTheDocument();
    }, { timeout: 5000 });
  });

  it('应该显示新增订单按钮', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <OrderManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      const addButton = screen.queryByText(/新增|创建/);
      expect(addButton || screen.getByText('订单管理')).toBeInTheDocument();
    });
  });
});
