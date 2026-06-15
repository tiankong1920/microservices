import { vi, describe, it, expect, beforeEach } from 'vitest';
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import ProcurementManagement from '../ProcurementManagement';
import { ToastProvider } from '../../contexts/ToastContext';

vi.mock('../../services/api', () => ({
  procurementApi: {
    getAllProcurements: vi.fn(),
  },
}));

import { procurementApi } from '../../services/api';
import type { ProcurementOrder, ApiResponse } from '../../types';

describe('ProcurementManagement', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (procurementApi.getAllProcurements as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200,
      message: 'Success',
      data: [],
      timestamp: new Date().toISOString(),
      success: true,
    } as ApiResponse<ProcurementOrder[]>);
  });

  it('应该显示采购管理标题', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <ProcurementManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('采购管理')).toBeInTheDocument();
    });
  });

  it('应该渲染采购数据表格', async () => {
    (procurementApi.getAllProcurements as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200,
      message: 'Success',
      data: [
        { id: 1, procurementId: 'PO001', supplierName: '供应商A', totalAmount: 5000, status: 'PENDING' } as ProcurementOrder,
      ],
      timestamp: new Date().toISOString(),
      success: true,
    } as ApiResponse<ProcurementOrder[]>);

    render(
      <BrowserRouter>
        <ToastProvider>
          <ProcurementManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('table')).toBeInTheDocument();
    }, { timeout: 5000 });
  });
});
