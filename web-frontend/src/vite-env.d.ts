/// <reference types="vite/client" />

// Declare all JSX modules for TypeScript

declare module '*.jsx' {
  import { ComponentType } from 'react';
  const component: ComponentType<any>;
  export default component;
}

// Page module declarations
declare module './pages/Dashboard' {
  import { ComponentType } from 'react';
  const Dashboard: ComponentType<any>;
  export default Dashboard;
}

declare module './pages/ProductManagement' {
  import { ComponentType } from 'react';
  const ProductManagement: ComponentType<any>;
  export default ProductManagement;
}

declare module './pages/ProductDetail' {
  import { ComponentType } from 'react';
  const ProductDetail: ComponentType<any>;
  export default ProductDetail;
}

declare module './pages/ProductEdit' {
  import { ComponentType } from 'react';
  const ProductEdit: ComponentType<any>;
  export default ProductEdit;
}

declare module './pages/InventoryManagement' {
  import { ComponentType } from 'react';
  const InventoryManagement: ComponentType<any>;
  export default InventoryManagement;
}

declare module './pages/OrderManagement' {
  import { ComponentType } from 'react';
  const OrderManagement: ComponentType<any>;
  export default OrderManagement;
}

declare module './pages/OrderDetail' {
  import { ComponentType } from 'react';
  const OrderDetail: ComponentType<any>;
  export default OrderDetail;
}

declare module './pages/OrderCreate' {
  import { ComponentType } from 'react';
  const OrderCreate: ComponentType<any>;
  export default OrderCreate;
}

declare module './pages/ProcurementManagement' {
  import { ComponentType } from 'react';
  const ProcurementManagement: ComponentType<any>;
  export default ProcurementManagement;
}

declare module './pages/ProcurementDetail' {
  import { ComponentType } from 'react';
  const ProcurementDetail: ComponentType<any>;
  export default ProcurementDetail;
}

declare module './pages/SalesManagement' {
  import { ComponentType } from 'react';
  const SalesManagement: ComponentType<any>;
  export default SalesManagement;
}

declare module './pages/SalesDetail' {
  import { ComponentType } from 'react';
  const SalesDetail: ComponentType<any>;
  export default SalesDetail;
}

declare module './pages/ReceiptManagement' {
  import { ComponentType } from 'react';
  const ReceiptManagement: ComponentType<any>;
  export default ReceiptManagement;
}

declare module './pages/ReceiptDetail' {
  import { ComponentType } from 'react';
  const ReceiptDetail: ComponentType<any>;
  export default ReceiptDetail;
}

declare module './pages/PaymentManagement' {
  import { ComponentType } from 'react';
  const PaymentManagement: ComponentType<any>;
  export default PaymentManagement;
}

declare module './pages/PaymentDetail' {
  import { ComponentType } from 'react';
  const PaymentDetail: ComponentType<any>;
  export default PaymentDetail;
}

declare module './pages/IncomeManagement' {
  import { ComponentType } from 'react';
  const IncomeManagement: ComponentType<any>;
  export default IncomeManagement;
}

declare module './pages/IncomeDetail' {
  import { ComponentType } from 'react';
  const IncomeDetail: ComponentType<any>;
  export default IncomeDetail;
}

declare module './pages/SettlementAccountManagement' {
  import { ComponentType } from 'react';
  const SettlementAccountManagement: ComponentType<any>;
  export default SettlementAccountManagement;
}

declare module './pages/SettlementAccountDetail' {
  import { ComponentType } from 'react';
  const SettlementAccountDetail: ComponentType<any>;
  export default SettlementAccountDetail;
}

declare module './pages/CustomerManagement' {
  import { ComponentType } from 'react';
  const CustomerManagement: ComponentType<any>;
  export default CustomerManagement;
}

declare module './pages/CustomerDetail' {
  import { ComponentType } from 'react';
  const CustomerDetail: ComponentType<any>;
  export default CustomerDetail;
}

declare module './pages/CustomerEdit' {
  import { ComponentType } from 'react';
  const CustomerEdit: ComponentType<any>;
  export default CustomerEdit;
}

declare module './pages/SupplierManagement' {
  import { ComponentType } from 'react';
  const SupplierManagement: ComponentType<any>;
  export default SupplierManagement;
}

declare module './pages/SupplierDetail' {
  import { ComponentType } from 'react';
  const SupplierDetail: ComponentType<any>;
  export default SupplierDetail;
}

declare module './pages/SupplierEdit' {
  import { ComponentType } from 'react';
  const SupplierEdit: ComponentType<any>;
  export default SupplierEdit;
}

declare module './pages/SalesReport' {
  import { ComponentType } from 'react';
  const SalesReport: ComponentType<any>;
  export default SalesReport;
}

declare module './pages/PurchaseReport' {
  import { ComponentType } from 'react';
  const PurchaseReport: ComponentType<any>;
  export default PurchaseReport;
}

declare module './pages/InventoryReport' {
  import { ComponentType } from 'react';
  const InventoryReport: ComponentType<any>;
  export default InventoryReport;
}

declare module './pages/FinancialReport' {
  import { ComponentType } from 'react';
  const FinancialReport: ComponentType<any>;
  export default FinancialReport;
}

declare module './pages/SystemManagement' {
  import { ComponentType } from 'react';
  const SystemManagement: ComponentType<any>;
  export default SystemManagement;
}

declare module './pages/UserManagement' {
  import { ComponentType } from 'react';
  const UserManagement: ComponentType<any>;
  export default UserManagement;
}

declare module './pages/Login' {
  import { ComponentType } from 'react';
  const Login: ComponentType<any>;
  export default Login;
}

declare module './pages/StockTransferManagement' {
  import { ComponentType } from 'react';
  const StockTransferManagement: ComponentType<any>;
  export default StockTransferManagement;
}

declare module './pages/StockTransferDetail' {
  import { ComponentType } from 'react';
  const StockTransferDetail: ComponentType<any>;
  export default StockTransferDetail;
}

declare module './pages/ProcurementReturnManagement' {
  import { ComponentType } from 'react';
  const ProcurementReturnManagement: ComponentType<any>;
  export default ProcurementReturnManagement;
}

declare module './pages/ProcurementReturnDetail' {
  import { ComponentType } from 'react';
  const ProcurementReturnDetail: ComponentType<any>;
  export default ProcurementReturnDetail;
}
