import { vi, describe, it, expect, beforeEach } from 'vitest';
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import SalesManagement from '../SalesManagement';
import { ToastProvider } from '../../contexts/ToastContext';

vi.mock('../../services/api', () => ({
  salesApi: {
    getAllSales: vi.fn(),
  },
}));

import { salesApi } from '../../services/api';
import type { SalesOrder, ApiResponse } from '../../types';

describe('SalesManagement', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (salesApi.getAllSales as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200,
      message: 'Success',
      data: [],
      timestamp: new Date().toISOString(),
      success: true,
    } as ApiResponse<SalesOrder[]>);
  });

  it('应该显示销售管理标题', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <SalesManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('销售管理')).toBeInTheDocument();
    });
  });

  it('应该渲染销售数据表格', async () => {
    (salesApi.getAllSales as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200,
      message: 'Success',
      data: [
        { id: 1, salesOrderId: 'SO001', customerName: '客户A', totalAmount: 1000, status: 'PENDING' } as SalesOrder,
      ],
      timestamp: new Date().toISOString(),
      success: true,
    } as ApiResponse<SalesOrder[]>);

    render(
      <BrowserRouter>
        <ToastProvider>
          <SalesManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('table')).toBeInTheDocument();
    }, { timeout: 5000 });
  });
});
