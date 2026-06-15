import { vi, describe, it, expect, beforeEach } from 'vitest';
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import PaymentManagement from '../PaymentManagement';
import { ToastProvider } from '../../contexts/ToastContext';

vi.mock('../../services/api', () => ({
  paymentApi: {
    getAllPayments: vi.fn(),
  },
}));

import { paymentApi } from '../../services/api';
import type { Payment, ApiResponse } from '../../types';

describe('PaymentManagement', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (paymentApi.getAllPayments as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200, message: 'Success', data: [] as Payment[], timestamp: new Date().toISOString(), success: true,
    } as ApiResponse<Payment[]>);
  });

  it('应该显示付款管理标题', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <PaymentManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('付款单管理')).toBeInTheDocument();
    });
  });

  it('应该渲染付款数据表格', async () => {
    (paymentApi.getAllPayments as ReturnType<typeof vi.fn>).mockResolvedValue({
      code: 200, message: 'Success',
      data: [{ id: 1, paymentNo: 'PAY001', amount: 1000, status: 'PENDING' } as Payment],
      timestamp: new Date().toISOString(), success: true,
    } as ApiResponse<Payment[]>);

    render(
      <BrowserRouter>
        <ToastProvider>
          <PaymentManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('table')).toBeInTheDocument();
    }, { timeout: 5000 });
  });
});
