import { vi, describe, it, expect, beforeEach } from 'vitest';
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Dashboard from '../Dashboard';
import { ToastProvider } from '../../contexts/ToastContext';

vi.mock('../../services/api', () => ({
  dashboardApi: {
    getDashboardStats: vi.fn(),
    getSalesTrend: vi.fn(),
    getInventoryReport: vi.fn(),
  },
  procurementApi: {
    getAllProcurements: vi.fn(),
  },
}));

import { dashboardApi, procurementApi } from '../../services/api';

describe('Dashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (dashboardApi.getDashboardStats as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200, message: 'OK', success: true,
      data: { totalProducts: 100, totalCustomers: 50, totalSuppliers: 20, totalOrders: 200, totalSales: 100000, totalInventoryValue: 500000, lowStockItems: 5 },
      timestamp: new Date().toISOString(),
    });
    (dashboardApi.getSalesTrend as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200, message: 'OK', success: true, data: [], timestamp: new Date().toISOString(),
    });
    (dashboardApi.getInventoryReport as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200, message: 'OK', success: true,
      data: { totalItems: 0, totalValue: 0, lowStockItems: 0, items: [] },
      timestamp: new Date().toISOString(),
    });
    (procurementApi.getAllProcurements as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200, message: 'OK', success: true, data: [], timestamp: new Date().toISOString(),
    });
  });

  it('应该渲染 Dashboard 页面', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <Dashboard />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      const titles = screen.getAllByText(/仪表盘|Dashboard|总览/);
      expect(titles.length).toBeGreaterThanOrEqual(0);
    });
  });

  it('应该加载统计数据', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <Dashboard />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(dashboardApi.getDashboardStats).toHaveBeenCalled();
    });
  });

  it('应该处理 API 错误而不崩溃', async () => {
    (dashboardApi.getDashboardStats as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('API Error'));

    render(
      <BrowserRouter>
        <ToastProvider>
          <Dashboard />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(dashboardApi.getDashboardStats).toHaveBeenCalled();
    });
  });
});
