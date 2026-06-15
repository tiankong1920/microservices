// Type definitions for Inventory Management System

// API Response Types
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: string;
  success: boolean;
}

// Product Types
export interface Product {
  productId: number;
  productName: string;
  productCode: string;
  description?: string;
  categoryId: number;
  categoryName?: string;
  price?: number;
  cost?: number;
  stock?: number;
  stockQuantity?: number;
  minStockLevel?: number;
  maxStockLevel?: number;
  unit: string;
  status?: 'ACTIVE' | 'INACTIVE' | 'DISCONTINUED';
  deleted?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductDTO {
  id?: number;
  name: string;
  description?: string;
  sku: string;
  category: string;
  price: number;
  cost: number;
  stockQuantity?: number;
  minStockLevel?: number;
  maxStockLevel?: number;
  unit: string;
  status?: string;
}

// Inventory Types
export interface Inventory {
  id: number;
  productId: number;
  productName: string;
  sku?: string;
  warehouseId: number;
  warehouseName: string;
  warehouse?: string;
  quantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  batchNumber?: string;
  unit?: string;
  status?: string;
  lastUpdated: string;
}

export interface Warehouse {
  id: number;
  name: string;
  location: string;
  capacity: number;
  currentUsage: number;
  status: 'ACTIVE' | 'INACTIVE';
}

// Order Types
export interface Order {
  id: number;
  orderNo: string;
  customerId: number;
  customerName: string;
  orderDate: string;
  deliveryDate?: string;
  status: 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
  totalAmount: number;
  paymentMethod?: string;
  items: OrderItem[];
  createdAt: string;
}

export interface OrderItem {
  id: number;
  orderId: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

// Customer Types
export interface Customer {
  id: number;
  customerId: string;
  customerName: string;
  contactPerson: string;
  email: string;
  phone: string;
  address?: string;
  creditLimit?: number;
  currentCredit?: number;
  totalOrders?: number;
  totalSalesAmount?: number;
  lastOrderDate?: string;
  remark?: string;
  status: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  updatedAt: string;
}

// Customer Form Data (for create/update)
export interface CustomerFormData {
  customerName: string;
  contactPerson: string;
  phone: string;
  email: string;
  address: string;
  status: 'ACTIVE' | 'INACTIVE';
  remark: string;
}

// Supplier Types
export interface Supplier {
  id: number;
  supplierId: string;
  supplierName: string;
  contactPerson: string;
  email: string;
  phone: string;
  address?: string;
  paymentTerms?: string;
  remark?: string;
  totalProcurements?: number;
  totalProcurementAmount?: number;
  lastProcurementDate?: string;
  status: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  updatedAt: string;
}

// Supplier Form Data (for create/update)
export interface SupplierFormData {
  supplierName: string;
  contactPerson: string;
  phone: string;
  email: string;
  address: string;
  status: 'ACTIVE' | 'INACTIVE';
  remark: string;
}

// Sales Types
export interface SalesOrder {
  id: number;
  orderNo: string;
  saleNumber?: string;
  customerId: number;
  customerName: string;
  orderDate: string;
  saleDate?: string;
  totalAmount: number;
  discountAmount: number;
  netAmount: number;
  status: string;
  salesperson?: string;
  items: SalesOrderItem[];
}

export interface SalesOrderItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  discountPercent: number;
  totalPrice: number;
}

// Procurement Types
export interface ProcurementOrder {
  id: number;
  orderNo: string;
  supplierId: number;
  supplierName: string;
  orderDate: string;
  expectedDeliveryDate?: string;
  totalAmount: number;
  status: string;
  items: ProcurementOrderItem[];
  procurementItems?: ProcurementOrderItem[];
}

export interface ProcurementOrderItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

// Finance Types
export interface Payment {
  id: number;
  paymentNo: string;
  paymentNumber?: string;
  orderId?: number;
  customerId?: number;
  customerName?: string;
  supplierId?: number;
  supplierName?: string;
  relatedOrderNumber?: string;
  amount: number;
  paymentAmount?: number;
  actualPaymentAmount?: number;
  differenceAmount?: number;
  paymentMethod: string;
  settlementAccountName?: string;
  paymentDate: string;
  status: string;
  reference?: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Receipt {
  id: number;
  receiptNo: string;
  receiptNumber?: string;
  paymentId?: number;
  customerId?: number;
  customerName?: string;
  relatedOrderNumber?: string;
  paymentMethod?: string;
  settlementAccountName?: string;
  amount: number;
  receiptAmount?: number;
  actualReceiptAmount?: number;
  differenceAmount?: number;
  receiptDate: string;
  status: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

// Income Types
export interface Income {
  id: number;
  incomeNo: string;
  incomeNumber: string;
  customerId?: number;
  customerName?: string;
  amount: number;
  incomeAmount: number;
  incomeDate: string;
  incomeType: string;
  incomeSource: string;
  incomeStatus: string;
  settlementAccountName: string;
  relatedOrderNumber?: string;
  description?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

// User Types
export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: string;
  roles?: string[];
  password?: string;
  status: 'ACTIVE' | 'INACTIVE';
  lastLogin?: string;
  createdAt: string;
}

export interface LoginRequest {
  username: string;
  password: string;
  mfaCode?: number;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  username: string;
  roles: string[];
  mfaRequired: boolean;
  mfaSessionId?: string;
}

// Report Types
export interface SalesReport {
  period: string;
  totalSales: number;
  totalOrders: number;
  averageOrderValue: number;
  topProducts: TopProduct[];
}

export interface TopProduct {
  productId: number;
  productName: string;
  quantitySold: number;
  totalRevenue: number;
}

export interface InventoryReport {
  totalProducts: number;
  totalValue: number;
  lowStockItems: LowStockItem[];
  categoryBreakdown: CategoryBreakdown[];
}

export interface LowStockItem {
  productId: number;
  productName: string;
  currentStock: number;
  minStockLevel: number;
}

export interface CategoryBreakdown {
  category: string;
  productCount: number;
  totalValue: number;
}

// Purchase Report
export interface PurchaseReport {
  period: string;
  totalPurchases: number;
  totalOrders: number;
  totalAmount?: number;
  averageOrderValue: number;
  topSuppliers: TopSupplier[];
}

export interface TopSupplier {
  supplierId: number;
  supplierName: string;
  orderCount: number;
  totalAmount: number;
}

// Financial Report
export interface FinancialReport {
  period: string;
  totalIncome: number;
  totalExpense: number;
  netProfit: number;
  incomeBreakdown: BreakdownItem[];
  expenseBreakdown: BreakdownItem[];
}

export interface BreakdownItem {
  category: string;
  amount: number;
  percentage: number;
}

// Dashboard Types
export interface DashboardStats {
  totalSales: number;
  totalOrders: number;
  totalCustomers: number;
  totalProducts: number;
  lowStockCount: number;
  pendingOrdersCount: number;
  recentOrders: Order[];
  salesTrend: TrendData[];
}

export interface TrendData {
  date: string;
  value: number;
}

// API Error Type
export interface ApiError {
  code: number;
  message: string;
  errorCode?: string;
  details?: Record<string, string[]>;
}

// Pagination Types
export interface PageRequest {
  page: number;
  size: number;
  sort?: string;
  direction?: 'ASC' | 'DESC';
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// Form Error Types
export interface FormErrors {
  [key: string]: string | undefined;
  general?: string;
}

// Retail Order Types
export interface RetailOrder {
  id: number;
  retailNumber: string;
  customerName: string;
  retailDate: string;
  totalAmount: number;
  paymentStatus: string;
  status: string;
  paymentMethod?: string;
  subtotal?: number;
  tax?: number;
  discount?: number;
  notes?: string;
  items?: RetailItem[];
}

export interface RetailItem {
  productName: string;
  productSku: string;
  quantity: number;
  unit: string;
  unitPrice: number;
  discount: number;
  subtotal: number;
}

// Sales Return Types
export interface SalesReturnOrder {
  id: number;
  returnNumber: string;
  customerName: string;
  returnDate: string;
  status: string;
  subtotal: number;
  tax: number;
  discount: number;
  totalAmount: number;
  reason?: string;
  notes?: string;
  items?: SalesReturnItem[];
}

export interface SalesReturnItem {
  productName: string;
  productSku: string;
  quantity: number;
  unit: string;
  unitPrice: number;
  discount: number;
  subtotal: number;
  reason?: string;
}

// Other Stock Types
export interface OtherStockOrder {
  id: number;
  orderNumber: string;
  orderType: string;
  orderDate: string;
  warehouseName: string;
  operatorName: string;
  status: string;
  totalAmount: number;
  notes?: string;
  items?: OtherStockItem[];
}

export interface OtherStockItem {
  productName: string;
  productSku: string;
  quantity: number;
  unit: string;
  unitPrice: number;
  totalAmount: number;
}

// Settlement Account Types
export interface SettlementAccount {
  id: number;
  accountId: string;
  accountName: string;
  accountType: string;
  bankName?: string;
  accountNumber?: string;
  currency: string;
  balance: number;
  status: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  updatedAt: string;
  remark?: string;
}

// Stock Transfer Types
export interface StockTransfer {
  id: number;
  transferNumber: string;
  sourceWarehouseId: number;
  sourceWarehouseName: string;
  targetWarehouseId: number;
  targetWarehouseName: string;
  transferDate: string;
  totalQuantity: number;
  status: string;
  items?: StockTransferItem[];
  createdAt?: string;
}

export interface StockTransferItem {
  productId: number;
  productName: string;
  productSku: string;
  quantity: number;
  unit: string;
}

// Procurement Return Types
export interface ProcurementReturn {
  id: number;
  returnNumber: string;
  supplierId: number;
  supplierName: string;
  returnDate: string;
  totalAmount: number;
  status: string;
  items?: ProcurementReturnItem[];
  subtotal?: number;
  tax?: number;
  reason?: string;
  notes?: string;
}

export interface ProcurementReturnItem {
  productId: number;
  productName: string;
  productSku: string;
  quantity: number;
  unit: string;
  unitPrice: number;
  discount?: number;
  subtotal: number;
  reason?: string;
}
