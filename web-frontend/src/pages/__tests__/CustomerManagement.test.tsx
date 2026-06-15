import { vi, describe, it, expect, beforeEach } from 'vitest';
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import CustomerManagement from '../CustomerManagement';
import { ToastProvider } from '../../contexts/ToastContext';

// Mock api module - the component imports customerApi from services/api
vi.mock('../../services/api', () => ({
  customerApi: {
    getAllCustomers: vi.fn(() => Promise.resolve({ 
      code: 200, 
      message: 'Success', 
      data: [], 
      timestamp: new Date().toISOString(), 
      success: true 
    })),
    deleteCustomer: vi.fn(() => Promise.resolve({ 
      code: 200, 
      message: 'Success', 
      data: null, 
      timestamp: new Date().toISOString(), 
      success: true 
    })),
  },
}));

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Import after mocking
import { customerApi } from '../../services/api';
import type { Customer, ApiResponse } from '../../types';

const mockCustomers: Customer[] = [
  {
    id: 1,
    customerId: 'CUST001',
    customerName: '测试客户1',
    contactPerson: '张三',
    email: 'test1@example.com',
    phone: '13800138001',
    address: '北京市朝阳区',
    status: 'ACTIVE',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-15T00:00:00Z',
  },
  {
    id: 2,
    customerId: 'CUST002',
    customerName: '测试客户2',
    contactPerson: '李四',
    email: 'test2@example.com',
    phone: '13800138002',
    address: '上海市浦东新区',
    status: 'INACTIVE',
    createdAt: '2024-01-02T00:00:00Z',
    updatedAt: '2024-01-16T00:00:00Z',
  },
];

describe('CustomerManagement', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Reset to default mock implementation
    (customerApi.getAllCustomers as ReturnType<typeof vi.fn>).mockImplementation(() => 
      Promise.resolve({ 
        code: 200, 
        message: 'Success', 
        data: [], 
        timestamp: new Date().toISOString(), 
        success: true 
      })
    );
  });

  it('应该显示客户列表标题', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <CustomerManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('客户管理')).toBeInTheDocument();
    });
  });

  it('应该正确显示客户列表', async () => {
    // Set up mock BEFORE render
    const mockResponse: ApiResponse<Customer[]> = {
      code: 200,
      message: 'Success',
      data: mockCustomers,
      timestamp: new Date().toISOString(),
      success: true,
    };
    
    (customerApi.getAllCustomers as ReturnType<typeof vi.fn>).mockResolvedValue(mockResponse);

    render(
      <BrowserRouter>
        <ToastProvider>
          <CustomerManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    // Wait for data to load - look for table content
    await waitFor(() => {
      // The component should have loaded data now
      const table = screen.getByRole('table');
      expect(table).toBeInTheDocument();
    }, { timeout: 5000 });
  });

  it('应该导航到新增页面当点击新增按钮', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <CustomerManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('新增客户')).toBeInTheDocument();
    });

    const addButton = screen.getByText('新增客户');
    await userEvent.click(addButton);

    expect(mockNavigate).toHaveBeenCalledWith('/customers/edit/new');
  });

  it('应该显示搜索框', async () => {
    render(
      <BrowserRouter>
        <ToastProvider>
          <CustomerManagement />
        </ToastProvider>
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByPlaceholderText('搜索客户名称、联系人、电话或邮箱...')).toBeInTheDocument();
    });
  });
});
