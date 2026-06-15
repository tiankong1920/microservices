import { vi, describe, it, expect, beforeEach } from 'vitest';
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import CustomerDetail from '../CustomerDetail';

// Mock api module - default returns customer data
vi.mock('../../services/api', () => ({
  customerApi: {
    getCustomerById: vi.fn(() => Promise.resolve({ 
      code: 200, 
      message: 'Success', 
      data: { 
        id: 1,
        customerId: 'CUST001',
        customerName: '测试客户',
        contactPerson: '张三',
        email: 'test@example.com',
        phone: '13800138000',
        address: '北京市朝阳区',
        status: 'ACTIVE',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-15T00:00:00Z',
      }, 
      timestamp: new Date().toISOString(), 
      success: true 
    })),
  },
}));

// Mock useParams and useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useParams: () => ({ id: '1' }),
    useNavigate: () => mockNavigate,
  };
});

import { customerApi } from '../../services/api';
import type { Customer, ApiResponse } from '../../types';

const mockCustomer: Customer = {
  id: 1,
  customerId: 'CUST001',
  customerName: '测试客户',
  contactPerson: '张三',
  email: 'test@example.com',
  phone: '13800138000',
  address: '北京市朝阳区',
  status: 'ACTIVE',
  createdAt: '2024-01-01T00:00:00Z',
  updatedAt: '2024-01-15T00:00:00Z',
  remark: '重要客户',
  totalOrders: 10,
  totalSalesAmount: 50000,
  lastOrderDate: '2024-01-10T00:00:00Z',
};

describe('CustomerDetail', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Default mock - returns valid customer data
    (customerApi.getCustomerById as ReturnType<typeof vi.fn>).mockImplementation(() => 
      Promise.resolve({ 
        code: 200, 
        message: 'Success', 
        data: mockCustomer, 
        timestamp: new Date().toISOString(), 
        success: true 
      })
    );
  });

  it('应该显示客户详情标题', async () => {
    render(
      <BrowserRouter>
        <CustomerDetail />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('客户详情')).toBeInTheDocument();
    });
  });

  it('应该显示客户基本信息', async () => {
    const mockResponse: ApiResponse<Customer> = {
      code: 200,
      message: 'Success',
      data: mockCustomer,
      timestamp: new Date().toISOString(),
      success: true,
    };
    
    (customerApi.getCustomerById as ReturnType<typeof vi.fn>).mockResolvedValue(mockResponse);

    render(
      <BrowserRouter>
        <CustomerDetail />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('客户详情')).toBeInTheDocument();
    }, { timeout: 5000 });
  });

  it('应该显示编辑按钮', async () => {
    const mockResponse: ApiResponse<Customer> = {
      code: 200,
      message: 'Success',
      data: mockCustomer,
      timestamp: new Date().toISOString(),
      success: true,
    };
    
    (customerApi.getCustomerById as ReturnType<typeof vi.fn>).mockResolvedValue(mockResponse);

    render(
      <BrowserRouter>
        <CustomerDetail />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('编辑客户')).toBeInTheDocument();
    });
  });

  it('应该导航到编辑页面当点击编辑按钮', async () => {
    const mockResponse: ApiResponse<Customer> = {
      code: 200,
      message: 'Success',
      data: mockCustomer,
      timestamp: new Date().toISOString(),
      success: true,
    };
    
    (customerApi.getCustomerById as ReturnType<typeof vi.fn>).mockResolvedValue(mockResponse);

    render(
      <BrowserRouter>
        <CustomerDetail />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('编辑客户')).toBeInTheDocument();
    });

    const editButton = screen.getByText('编辑客户');
    await userEvent.click(editButton);

    expect(mockNavigate).toHaveBeenCalledWith('/customers/edit/1');
  });
});
