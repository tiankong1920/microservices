import axios, { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { toastApiRef } from '../contexts/ToastContext';
import type {
  ApiResponse,
  LoginRequest,
  LoginResponse,
  Product,
  ProductDTO,
  Inventory,
  Order,
  Customer,
  Supplier,
  SalesOrder,
  ProcurementOrder,
  Payment,
  Receipt,
  Income,
  User,
  DashboardStats,
  SalesReport,
  PurchaseReport,
  FinancialReport,
  InventoryReport,
  RetailOrder,
  RetailItem,
  SalesReturnOrder,
  SalesReturnItem,
  OtherStockOrder,
  OtherStockItem,
  SettlementAccount,
  StockTransfer,
  ProcurementReturn,
} from '../types';

// Helper: build pagination query params
const buildPaginationParams = (page?: number, size?: number): Record<string, number> => {
  const params: Record<string, number> = {};
  if (page !== undefined) params.page = page;
  if (size !== undefined) params.size = size;
  return params;
};

// Create axios instance
const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig): InternalAxiosRequestConfig => {
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor
apiClient.interceptors.response.use(
  (response: AxiosResponse): AxiosResponse['data'] => response.data,
  (error) => {
    console.error('API Error:', error.response || error);

    if (error.response) {
      switch (error.response.status) {
        case 401:
          // Unauthorized - clear auth and redirect to login
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          toastApiRef.current?.showToast('登录已过期，请重新登录', 'warning');
          window.location.href = '/login';
          break;
        case 403:
          toastApiRef.current?.showToast('没有权限访问此资源', 'warning');
          break;
        case 404:
          toastApiRef.current?.showToast('请求的资源不存在', 'error');
          break;
        case 500:
          toastApiRef.current?.showToast('服务器内部错误', 'error');
          break;
        default:
          toastApiRef.current?.showToast(error.response.data?.message || '请求失败', 'error');
      }
    } else if (error.request) {
      toastApiRef.current?.showToast('网络错误，无法连接到服务器', 'error');
    } else {
      toastApiRef.current?.showToast('请求配置错误', 'error');
    }

    return Promise.reject(error);
  }
);

// Auth API
export const authApi = {
  login: (credentials: LoginRequest): Promise<ApiResponse<LoginResponse>> =>
    apiClient.post('/admin/auth/login', credentials),
  register: (userData: Partial<User>): Promise<ApiResponse<User>> =>
    apiClient.post('/admin/auth/register', userData),
  refreshToken: (): Promise<ApiResponse<LoginResponse>> =>
    apiClient.post('/admin/auth/refresh'),
  getCurrentUser: (): Promise<ApiResponse<User>> =>
    apiClient.get('/admin/auth/me'),
};

// Product API
export const productApi = {
  getAllProducts: (page?: number, size?: number): Promise<ApiResponse<Product[]>> =>
    apiClient.get('/products', { params: buildPaginationParams(page, size) }),
  getProductById: (id: number): Promise<ApiResponse<Product>> =>
    apiClient.get(`/products/${id}`),
  createProduct: (productData: ProductDTO): Promise<ApiResponse<Product>> =>
    apiClient.post('/products', productData),
  updateProduct: (id: number, productData: ProductDTO): Promise<ApiResponse<Product>> =>
    apiClient.put(`/products/${id}`, productData),
  deleteProduct: (id: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/products/${id}`),
  getProductsByCategory: (categoryId: number): Promise<ApiResponse<Product[]>> =>
    apiClient.get(`/products/category/${categoryId}`),
  searchProducts: (keyword: string): Promise<ApiResponse<Product[]>> =>
    apiClient.get(`/products/search?keyword=${encodeURIComponent(keyword)}`),
};

// Inventory API
export const inventoryApi = {
  getAllInventory: (page?: number, size?: number): Promise<ApiResponse<Inventory[]>> =>
    apiClient.get('/inventory', { params: buildPaginationParams(page, size) }),
  getInventoryById: (id: number): Promise<ApiResponse<Inventory>> =>
    apiClient.get(`/inventory/${id}`),
  updateInventory: (id: number, inventoryData: Partial<Inventory>): Promise<ApiResponse<Inventory>> =>
    apiClient.put(`/inventory/${id}`, inventoryData),
  getInventoryByProductId: (productId: number): Promise<ApiResponse<Inventory>> =>
    apiClient.get(`/inventory/product/${productId}`),
  getInventoryByWarehouse: (warehouseId: number): Promise<ApiResponse<Inventory[]>> =>
    apiClient.get(`/inventory/warehouse/${warehouseId}`),
  adjustInventory: (inventoryId: number, adjustmentData: { quantity: number; reason: string }): Promise<ApiResponse<Inventory>> =>
    apiClient.post(`/inventory/${inventoryId}/adjust`, adjustmentData),
  transferInventory: (transferData: { fromInventoryId?: number; fromWarehouseId?: number; toWarehouseId: string; productId?: number; quantity: number; reason?: string }): Promise<ApiResponse<void>> =>
    apiClient.post('/inventory/transfer', transferData),
};

// Stock Transfer API
export const stockTransferApi = {
  getAllStockTransfers: (page?: number, size?: number): Promise<ApiResponse<StockTransfer[]>> =>
    apiClient.get('/stock-transfers', { params: buildPaginationParams(page, size) }),
  getStockTransferById: (id: number): Promise<ApiResponse<StockTransfer>> =>
    apiClient.get(`/stock-transfers/${id}`),
  createStockTransfer: (transferData: Partial<StockTransfer>): Promise<ApiResponse<StockTransfer>> =>
    apiClient.post('/stock-transfers', transferData),
  updateStockTransfer: (id: number, transferData: Partial<StockTransfer>): Promise<ApiResponse<StockTransfer>> =>
    apiClient.put(`/stock-transfers/${id}`, transferData),
  deleteStockTransfer: (id: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/stock-transfers/${id}`),
};

// Procurement Return API
export const procurementReturnApi = {
  getAllProcurementReturns: (page?: number, size?: number): Promise<ApiResponse<ProcurementReturn[]>> =>
    apiClient.get('/procurement-returns', { params: buildPaginationParams(page, size) }),
  getProcurementReturnById: (id: number): Promise<ApiResponse<ProcurementReturn>> =>
    apiClient.get(`/procurement-returns/${id}`),
  createProcurementReturn: (returnData: Partial<ProcurementReturn>): Promise<ApiResponse<ProcurementReturn>> =>
    apiClient.post('/procurement-returns', returnData),
  updateProcurementReturn: (id: number, returnData: Partial<ProcurementReturn>): Promise<ApiResponse<ProcurementReturn>> =>
    apiClient.put(`/procurement-returns/${id}`, returnData),
  deleteProcurementReturn: (id: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/procurement-returns/${id}`),
};

// Order API
export const orderApi = {
  getAllOrders: (page?: number, size?: number): Promise<ApiResponse<Order[]>> =>
    apiClient.get('/orders', { params: buildPaginationParams(page, size) }),
  getOrderById: (id: number): Promise<ApiResponse<Order>> =>
    apiClient.get(`/orders/${id}`),
  createOrder: (orderData: Partial<Order>): Promise<ApiResponse<Order>> =>
    apiClient.post('/orders', orderData),
  updateOrder: (id: number, orderData: Partial<Order>): Promise<ApiResponse<Order>> =>
    apiClient.put(`/orders/${id}`, orderData),
  updateOrderStatus: (id: number, status: string): Promise<ApiResponse<Order>> =>
    apiClient.patch(`/orders/${id}/status`, { status }),
  deleteOrder: (id: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/orders/${id}`),
  getOrdersByStatus: (status: string): Promise<ApiResponse<Order[]>> =>
    apiClient.get(`/orders/status/${status}`),
};

// Procurement API
export const procurementApi = {
  getAllProcurements: (page?: number, size?: number): Promise<ApiResponse<ProcurementOrder[]>> =>
    apiClient.get('/procurement', { params: buildPaginationParams(page, size) }),
  getProcurementById: (id: number): Promise<ApiResponse<ProcurementOrder>> =>
    apiClient.get(`/procurement/${id}`),
  createProcurement: (procurementData: Partial<ProcurementOrder>): Promise<ApiResponse<ProcurementOrder>> =>
    apiClient.post('/procurement', procurementData),
  updateProcurement: (id: number, procurementData: Partial<ProcurementOrder>): Promise<ApiResponse<ProcurementOrder>> =>
    apiClient.put(`/procurement/${id}`, procurementData),
  updateProcurementStatus: (id: number, status: string): Promise<ApiResponse<ProcurementOrder>> =>
    apiClient.patch(`/procurement/${id}/status`, { status }),
  deleteProcurement: (id: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/procurement/${id}`),
};

// Customer API
export const customerApi = {
  getAllCustomers: (page?: number, size?: number): Promise<ApiResponse<Customer[]>> =>
    apiClient.get('/customers', { params: buildPaginationParams(page, size) }),
  getCustomerById: (id: number): Promise<ApiResponse<Customer>> =>
    apiClient.get(`/customers/${id}`),
  createCustomer: (customerData: Partial<Customer>): Promise<ApiResponse<Customer>> =>
    apiClient.post('/customers', customerData),
  updateCustomer: (id: number, customerData: Partial<Customer>): Promise<ApiResponse<Customer>> =>
    apiClient.put(`/customers/${id}`, customerData),
  deleteCustomer: (id: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/customers/${id}`),
  searchCustomers: (keyword: string): Promise<ApiResponse<Customer[]>> =>
    apiClient.get(`/customers/search?keyword=${encodeURIComponent(keyword)}`),
};

// Supplier API
export const supplierApi = {
  getAllSuppliers: (page?: number, size?: number): Promise<ApiResponse<Supplier[]>> =>
    apiClient.get('/suppliers', { params: buildPaginationParams(page, size) }),
  getSupplierById: (id: number): Promise<ApiResponse<Supplier>> =>
    apiClient.get(`/suppliers/${id}`),
  createSupplier: (supplierData: Partial<Supplier>): Promise<ApiResponse<Supplier>> =>
    apiClient.post('/suppliers', supplierData),
  updateSupplier: (id: number, supplierData: Partial<Supplier>): Promise<ApiResponse<Supplier>> =>
    apiClient.put(`/suppliers/${id}`, supplierData),
  deleteSupplier: (id: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/suppliers/${id}`),
  searchSuppliers: (keyword: string): Promise<ApiResponse<Supplier[]>> =>
    apiClient.get(`/suppliers/search?keyword=${encodeURIComponent(keyword)}`),
};

// Sales API
export const salesApi = {
  getAllSales: (page?: number, size?: number): Promise<ApiResponse<SalesOrder[]>> =>
    apiClient.get('/sales', { params: buildPaginationParams(page, size) }),
  getSaleById: (id: number): Promise<ApiResponse<SalesOrder>> =>
    apiClient.get(`/sales/${id}`),
  createSale: (saleData: Partial<SalesOrder>): Promise<ApiResponse<SalesOrder>> =>
    apiClient.post('/sales', saleData),
  updateSale: (id: number, saleData: Partial<SalesOrder>): Promise<ApiResponse<SalesOrder>> =>
    apiClient.put(`/sales/${id}`, saleData),
  updateSaleStatus: (id: number, status: string): Promise<ApiResponse<SalesOrder>> =>
    apiClient.patch(`/sales/${id}/status`, { status }),
  deleteSale: (id: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/sales/${id}`),
  getSalesReport: (params: { startDate?: string; endDate?: string }): Promise<ApiResponse<SalesReport>> =>
    apiClient.get('/sales/report', { params }),
};

// Payment API
export const paymentApi = {
  getAllPayments: (page?: number, size?: number): Promise<ApiResponse<Payment[]>> =>
    apiClient.get('/finance/payments', { params: buildPaginationParams(page, size) }),
  getPaymentById: (id: number): Promise<ApiResponse<Payment>> =>
    apiClient.get(`/finance/payments/${id}`),
  createPayment: (paymentData: Partial<Payment>): Promise<ApiResponse<Payment>> =>
    apiClient.post('/finance/payments', paymentData),
  updatePayment: (id: number, paymentData: Partial<Payment>): Promise<ApiResponse<Payment>> =>
    apiClient.put(`/finance/payments/${id}`, paymentData),
  deletePayment: (id: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/finance/payments/${id}`),
  getPaymentsByStatus: (status: string): Promise<ApiResponse<Payment[]>> =>
    apiClient.get(`/finance/payments/by-status?status=${status}`),
  getPaymentsByDateRange: (startDate: string, endDate: string): Promise<ApiResponse<Payment[]>> =>
    apiClient.get('/finance/payments/by-date-range', { params: { startDate, endDate } }),
};

// Receipt API
export const receiptApi = {
  getAllReceipts: (page?: number, size?: number): Promise<ApiResponse<Receipt[]>> =>
    apiClient.get('/finance/receipts', { params: buildPaginationParams(page, size) }),
  getReceiptById: (id: number): Promise<ApiResponse<Receipt>> =>
    apiClient.get(`/finance/receipts/${id}`),
  createReceipt: (receiptData: Partial<Receipt>): Promise<ApiResponse<Receipt>> =>
    apiClient.post('/finance/receipts', receiptData),
  updateReceipt: (id: number, receiptData: Partial<Receipt>): Promise<ApiResponse<Receipt>> =>
    apiClient.put(`/finance/receipts/${id}`, receiptData),
  deleteReceipt: (id: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/finance/receipts/${id}`),
  getReceiptsByStatus: (status: string): Promise<ApiResponse<Receipt[]>> =>
    apiClient.get(`/finance/receipts/by-status?status=${status}`),
  getReceiptsByDateRange: (startDate: string, endDate: string): Promise<ApiResponse<Receipt[]>> =>
    apiClient.get('/finance/receipts/by-date-range', { params: { startDate, endDate } }),
};

// Income API
export const incomeApi = {
  getAllIncomes: (page?: number, size?: number): Promise<ApiResponse<Income[]>> =>
    apiClient.get('/finance/incomes', { params: buildPaginationParams(page, size) }),
  getIncomeById: (id: number): Promise<ApiResponse<Income>> =>
    apiClient.get(`/finance/incomes/${id}`),
  createIncome: (incomeData: Partial<Income>): Promise<ApiResponse<Income>> =>
    apiClient.post('/finance/incomes', incomeData),
  updateIncome: (id: number, incomeData: Partial<Income>): Promise<ApiResponse<Income>> =>
    apiClient.put(`/finance/incomes/${id}`, incomeData),
  deleteIncome: (id: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/finance/incomes/${id}`),
  getIncomesByStatus: (status: string): Promise<ApiResponse<Receipt[]>> =>
    apiClient.get(`/finance/incomes/by-status?status=${status}`),
  getIncomesByDateRange: (startDate: string, endDate: string): Promise<ApiResponse<Receipt[]>> =>
    apiClient.get('/finance/incomes/by-date-range', { params: { startDate, endDate } }),
};

// Settlement Account API
export const settlementAccountApi = {
  getAllSettlementAccounts: (page?: number, size?: number): Promise<ApiResponse<SettlementAccount[]>> =>
    apiClient.get('/finance/settlement-accounts', { params: buildPaginationParams(page, size) }),
  getSettlementAccountById: (id: number): Promise<ApiResponse<SettlementAccount>> =>
    apiClient.get(`/finance/settlement-accounts/${id}`),
  createSettlementAccount: (accountData: Partial<SettlementAccount>): Promise<ApiResponse<SettlementAccount>> =>
    apiClient.post('/finance/settlement-accounts', accountData),
  updateSettlementAccount: (id: number, accountData: Partial<SettlementAccount>): Promise<ApiResponse<SettlementAccount>> =>
    apiClient.put(`/finance/settlement-accounts/${id}`, accountData),
  deleteSettlementAccount: (id: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/finance/settlement-accounts/${id}`),
};

// Role API
export const roleApi = {
  getAllRoles: (page?: number, size?: number): Promise<ApiResponse<{ id: number; name: string; description?: string }[]>> =>
    apiClient.get('/admin/roles', { params: buildPaginationParams(page, size) }),
  getRoleById: (id: number): Promise<ApiResponse<{ id: number; name: string; description?: string }>> =>
    apiClient.get(`/admin/roles/${id}`),
  createRole: (roleData: { name: string; description?: string }): Promise<ApiResponse<{ id: number; name: string; description?: string }>> =>
    apiClient.post('/admin/roles', roleData),
  updateRole: (id: number, roleData: { name?: string; description?: string }): Promise<ApiResponse<{ id: number; name: string; description?: string }>> =>
    apiClient.put(`/admin/roles/${id}`, roleData),
  deleteRole: (id: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/admin/roles/${id}`),
};

// User API
export const userApi = {
  getAllUsers: (page?: number, size?: number): Promise<ApiResponse<User[]>> =>
    apiClient.get('/admin/users', { params: buildPaginationParams(page, size) }),
  getUserById: (id: number): Promise<ApiResponse<User>> =>
    apiClient.get(`/admin/users/${id}`),
  createUser: (userData: Partial<User>): Promise<ApiResponse<User>> =>
    apiClient.post('/admin/users', userData),
  updateUser: (id: number, userData: Partial<User>): Promise<ApiResponse<User>> =>
    apiClient.put(`/admin/users/${id}`, userData),
  deleteUser: (id: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/admin/users/${id}`),
};

// System Settings API
export const systemConfigApi = {
  getSettings: (): Promise<ApiResponse<Record<string, unknown>>> =>
    apiClient.get('/admin/settings'),
  updateSettings: (settings: Record<string, unknown>): Promise<ApiResponse<Record<string, unknown>>> =>
    apiClient.put('/admin/settings', settings),
};

// Dashboard API
export const dashboardApi = {
  getDashboardStats: (): Promise<ApiResponse<DashboardStats>> =>
    apiClient.get('/dashboard/stats'),
  getSalesTrend: (period: string): Promise<ApiResponse<{ date: string; value: number }[]>> =>
    apiClient.get('/dashboard/sales-trend', { params: { period } }),
  getInventoryReport: (): Promise<ApiResponse<InventoryReport>> =>
    apiClient.get('/reports/inventory'),
};

// Report API
export const reportApi = {
  getSalesReport: (params?: { startDate?: string; endDate?: string }): Promise<ApiResponse<SalesReport>> =>
    apiClient.get('/reports/sales', { params }),
  getInventoryReport: (): Promise<ApiResponse<InventoryReport>> =>
    apiClient.get('/reports/inventory'),
  getFinancialReport: (params?: { startDate?: string; endDate?: string }): Promise<ApiResponse<FinancialReport>> =>
    apiClient.get('/reports/financial', { params }),
  getPurchaseReport: (params?: { startDate?: string; endDate?: string }): Promise<ApiResponse<PurchaseReport>> =>
    apiClient.get('/reports/purchase', { params }),
};

// Retail API
export const retailApi = {
  getAllRetails: (page?: number, size?: number): Promise<ApiResponse<RetailOrder[]>> =>
    apiClient.get('/retail', { params: buildPaginationParams(page, size) }),
  getRetailById: (id: number): Promise<ApiResponse<RetailOrder>> =>
    apiClient.get(`/retail/${id}`),
  createRetail: (data: Partial<RetailOrder>): Promise<ApiResponse<RetailOrder>> =>
    apiClient.post('/retail', data),
  updateRetail: (id: number, data: Partial<RetailOrder>): Promise<ApiResponse<RetailOrder>> =>
    apiClient.put(`/retail/${id}`, data),
  deleteRetail: (id: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/retail/${id}`),
};

// Sales Return API
export const salesReturnApi = {
  getAllSalesReturns: (page?: number, size?: number): Promise<ApiResponse<SalesReturnOrder[]>> =>
    apiClient.get('/sales-return', { params: buildPaginationParams(page, size) }),
  getSalesReturnById: (id: number): Promise<ApiResponse<SalesReturnOrder>> =>
    apiClient.get(`/sales-return/${id}`),
  createSalesReturn: (data: Partial<SalesReturnOrder>): Promise<ApiResponse<SalesReturnOrder>> =>
    apiClient.post('/sales-return', data),
  updateSalesReturn: (id: number, data: Partial<SalesReturnOrder>): Promise<ApiResponse<SalesReturnOrder>> =>
    apiClient.put(`/sales-return/${id}`, data),
  deleteSalesReturn: (id: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/sales-return/${id}`),
};

// Other Stock API
export const otherStockApi = {
  getAllOtherStocks: (page?: number, size?: number): Promise<ApiResponse<OtherStockOrder[]>> =>
    apiClient.get('/other-stock', { params: buildPaginationParams(page, size) }),
  getOtherStockById: (id: number): Promise<ApiResponse<OtherStockOrder>> =>
    apiClient.get(`/other-stock/${id}`),
  createOtherStock: (data: Partial<OtherStockOrder>): Promise<ApiResponse<OtherStockOrder>> =>
    apiClient.post('/other-stock', data),
  updateOtherStock: (id: number, data: Partial<OtherStockOrder>): Promise<ApiResponse<OtherStockOrder>> =>
    apiClient.put(`/other-stock/${id}`, data),
  deleteOtherStock: (id: number): Promise<ApiResponse<void>> =>
    apiClient.delete(`/other-stock/${id}`),
};

// Default export
export default apiClient;
