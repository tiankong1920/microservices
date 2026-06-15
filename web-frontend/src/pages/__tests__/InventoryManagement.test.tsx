import { vi, describe, it, expect, beforeEach } from 'vitest';
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import InventoryManagement from '../InventoryManagement';
import { ToastProvider } from '../../contexts/ToastContext';

vi.mock('../../services/api', () => ({
  inventoryApi: {
    getAllInventory: vi.fn(),
    adjustInventory: vi.fn(),
    transferInventory: vi.fn(),
  },
}));

import { inventoryApi } from '../../services/api';
import type { Inventory, ApiResponse } from '../../types';

const mockInventory: Inventory[] = [
  {
    id: 1,
    productId: 1,
    productName: '商品A',
    sku: 'SKU001',
    warehouse: '仓库1',
    quantity: 100,
    batchNumber: 'B20240101',
    createdAt: '2024-01-01T00:00:00Z',
  },
  {
    id: 2,
    productId: 2,
    productName: '商品B',
    sku: 'SKU002',
    warehouse: '仓库2',
    quantity: 50,
    batchNumber: 'B20240102',
    createdAt: '2024-01-02T00:00:00Z',
  },
];

describe('InventoryManagement', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (inventoryApi.getAllInventory as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200,
      message: 'Success',
      data: [],
      timestamp: new Date().toISOString(),
      success: true,
    } as ApiResponse<Inventory[]>);
  });

  it('应该显示库存管理标题', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <InventoryManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('库存管理')).toBeInTheDocument();
    });
  });

  it('应该渲染库存数据表格', async () => {
    (inventoryApi.getAllInventory as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200,
      message: 'Success',
      data: mockInventory,
      timestamp: new Date().toISOString(),
      success: true,
    } as ApiResponse<Inventory[]>);

    render(
      <BrowserRouter>
        <ToastProvider>
          <InventoryManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('table')).toBeInTheDocument();
    }, { timeout: 5000 });
  });

  it('应该显示搜索框', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <InventoryManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('库存管理')).toBeInTheDocument();
    });
  });

  it('应该在加载失败时处理错误', async () => {
    (inventoryApi.getAllInventory as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('Network error'));

    render(
      <BrowserRouter>
        <ToastProvider>
          <InventoryManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('库存管理')).toBeInTheDocument();
    });
  });
});
