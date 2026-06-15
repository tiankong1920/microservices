import { vi, describe, it, expect, beforeEach } from 'vitest';
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import StockTransferManagement from '../StockTransferManagement';
import { ToastProvider } from '../../contexts/ToastContext';

vi.mock('../../services/api', () => ({
  stockTransferApi: {
    getAllStockTransfers: vi.fn(),
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

import { stockTransferApi } from '../../services/api';
import type { StockTransfer, ApiResponse } from '../../types';

describe('StockTransferManagement', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (stockTransferApi.getAllStockTransfers as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200, message: 'Success', data: [] as StockTransfer[], timestamp: new Date().toISOString(), success: true,
    } as ApiResponse<StockTransfer[]>);
  });

  it('应该显示库存调拨管理标题', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <StockTransferManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('库存调拨管理')).toBeInTheDocument();
    });
  });

  it('应该渲染调拨单数据表格', async () => {
    (stockTransferApi.getAllStockTransfers as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200, message: 'Success',
      data: [{ id: 1, transferNumber: 'TF001', sourceWarehouseName: '仓库1', targetWarehouseName: '仓库2', status: 'PENDING' } as StockTransfer],
      timestamp: new Date().toISOString(), success: true,
    } as ApiResponse<StockTransfer[]>);

    render(
      <BrowserRouter>
        <ToastProvider>
          <StockTransferManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('table')).toBeInTheDocument();
    }, { timeout: 5000 });
  });
});
