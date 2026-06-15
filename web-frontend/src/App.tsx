import React, { Suspense, ReactNode, ComponentType, useContext } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, Outlet } from 'react-router-dom';
import './App.css';
import Layout from './components/Layout';
import { AuthContext } from './contexts/AuthContext';

// Type for lazy loaded components
// eslint-disable-next-line @typescript-eslint/no-explicit-any
type LazyComponent = ComponentType<any>;

// Lazy load page components for code splitting
const Dashboard = React.lazy(() => import('./pages/Dashboard')) as unknown as LazyComponent;
const ProductManagement = React.lazy(() => import('./pages/ProductManagement')) as unknown as LazyComponent;
const ProductDetail = React.lazy(() => import('./pages/ProductDetail')) as unknown as LazyComponent;
const ProductEdit = React.lazy(() => import('./pages/ProductEdit')) as unknown as LazyComponent;
const InventoryManagement = React.lazy(() => import('./pages/InventoryManagement')) as unknown as LazyComponent;
const OrderManagement = React.lazy(() => import('./pages/OrderManagement')) as unknown as LazyComponent;
const OrderDetail = React.lazy(() => import('./pages/OrderDetail')) as unknown as LazyComponent;
const OrderCreate = React.lazy(() => import('./pages/OrderCreate')) as unknown as LazyComponent;
const ProcurementManagement = React.lazy(() => import('./pages/ProcurementManagement')) as unknown as LazyComponent;
const ProcurementDetail = React.lazy(() => import('./pages/ProcurementDetail')) as unknown as LazyComponent;
const SalesManagement = React.lazy(() => import('./pages/SalesManagement')) as unknown as LazyComponent;
const SalesDetail = React.lazy(() => import('./pages/SalesDetail')) as unknown as LazyComponent;
const ReceiptManagement = React.lazy(() => import('./pages/ReceiptManagement')) as unknown as LazyComponent;
const ReceiptDetail = React.lazy(() => import('./pages/ReceiptDetail')) as unknown as LazyComponent;
const PaymentManagement = React.lazy(() => import('./pages/PaymentManagement')) as unknown as LazyComponent;
const PaymentDetail = React.lazy(() => import('./pages/PaymentDetail')) as unknown as LazyComponent;
const IncomeManagement = React.lazy(() => import('./pages/IncomeManagement')) as unknown as LazyComponent;
const IncomeDetail = React.lazy(() => import('./pages/IncomeDetail')) as unknown as LazyComponent;
const SettlementAccountManagement = React.lazy(() => import('./pages/SettlementAccountManagement')) as unknown as LazyComponent;
const SettlementAccountDetail = React.lazy(() => import('./pages/SettlementAccountDetail')) as unknown as LazyComponent;
const CustomerManagement = React.lazy(() => import('./pages/CustomerManagement')) as unknown as LazyComponent;
const CustomerDetail = React.lazy(() => import('./pages/CustomerDetail')) as unknown as LazyComponent;
const CustomerEdit = React.lazy(() => import('./pages/CustomerEdit')) as unknown as LazyComponent;
const SupplierManagement = React.lazy(() => import('./pages/SupplierManagement')) as unknown as LazyComponent;
const SupplierDetail = React.lazy(() => import('./pages/SupplierDetail')) as unknown as LazyComponent;
const SupplierEdit = React.lazy(() => import('./pages/SupplierEdit')) as unknown as LazyComponent;
const RetailManagement = React.lazy(() => import('./pages/RetailManagement')) as unknown as LazyComponent;
const RetailDetail = React.lazy(() => import('./pages/RetailDetail')) as unknown as LazyComponent;
const SalesReturnManagement = React.lazy(() => import('./pages/SalesReturnManagement')) as unknown as LazyComponent;
const SalesReturnDetail = React.lazy(() => import('./pages/SalesReturnDetail')) as unknown as LazyComponent;
const OtherStockManagement = React.lazy(() => import('./pages/OtherStockManagement')) as unknown as LazyComponent;
const OtherStockDetail = React.lazy(() => import('./pages/OtherStockDetail')) as unknown as LazyComponent;
const StockTransferManagement = React.lazy(() => import('./pages/StockTransferManagement')) as unknown as LazyComponent;
const StockTransferDetail = React.lazy(() => import('./pages/StockTransferDetail')) as unknown as LazyComponent;
const ProcurementReturnManagement = React.lazy(() => import('./pages/ProcurementReturnManagement')) as unknown as LazyComponent;
const ProcurementReturnDetail = React.lazy(() => import('./pages/ProcurementReturnDetail')) as unknown as LazyComponent;
const SalesReport = React.lazy(() => import('./pages/SalesReport')) as unknown as LazyComponent;
const PurchaseReport = React.lazy(() => import('./pages/PurchaseReport')) as unknown as LazyComponent;
const InventoryReport = React.lazy(() => import('./pages/InventoryReport')) as unknown as LazyComponent;
const FinancialReport = React.lazy(() => import('./pages/FinancialReport')) as unknown as LazyComponent;
const SystemManagement = React.lazy(() => import('./pages/SystemManagement')) as unknown as LazyComponent;
const UserManagement = React.lazy(() => import('./pages/UserManagement')) as unknown as LazyComponent;
const Login = React.lazy(() => import('./pages/Login')) as unknown as LazyComponent;

// Simple loading fallback
const LoadingFallback: React.FC = () => <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>加载中...</div>;

// Route guard component
interface ProtectedRouteProps {
  children: ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const authCtx = useContext(AuthContext);
  if (!authCtx) {
    return <Navigate to="/login" replace />;
  }
  if (authCtx.loading) {
    return <LoadingFallback />;
  }
  return authCtx.isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
};

// Login route component
interface LoginRouteProps {
  children: ReactNode;
}

const LoginRoute: React.FC<LoginRouteProps> = ({ children }) => {
  const authCtx = useContext(AuthContext);
  if (!authCtx) {
    return <>{children}</>;
  }
  if (authCtx.loading) {
    return <LoadingFallback />;
  }
  return authCtx.isAuthenticated ? <Navigate to="/" replace /> : <>{children}</>;
};

// Layout wrapper with outlet
const LayoutWrapper: React.FC = () => {
  return (
    <Layout>
      <Outlet />
    </Layout>
  );
};

// Loading component
const Loading: React.FC = () => (
  <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
    <div className="loading-spinner"></div>
  </div>
);

const App: React.FC = () => {
  return (
    <Router>
      <Suspense fallback={<Loading />}>
        <Routes>
          {/* Login page (no Layout required) */}
          <Route path="/login" element={<LoginRoute><Login /></LoginRoute>} />
          
          {/* Protected routes (require Layout and login) */}
          <Route element={<ProtectedRoute><LayoutWrapper /></ProtectedRoute>}>
            <Route path="/" element={<Dashboard />} />
            <Route path="/products" element={<ProductManagement />} />
            <Route path="/products/:id" element={<ProductDetail />} />
            <Route path="/products/edit/:id" element={<ProductEdit />} />
            <Route path="/inventory" element={<InventoryManagement />} />
            <Route path="/orders" element={<OrderManagement />} />
            <Route path="/orders/:id" element={<OrderDetail />} />
            <Route path="/orders/create" element={<OrderCreate />} />
            <Route path="/procurement" element={<ProcurementManagement />} />
            <Route path="/procurement/:id" element={<ProcurementDetail />} />
            <Route path="/sales" element={<SalesManagement />} />
            <Route path="/sales/:id" element={<SalesDetail />} />
            <Route path="/receipts" element={<ReceiptManagement />} />
            <Route path="/receipts/:id" element={<ReceiptDetail />} />
            <Route path="/payments" element={<PaymentManagement />} />
            <Route path="/payments/:id" element={<PaymentDetail />} />
            <Route path="/incomes" element={<IncomeManagement />} />
            <Route path="/incomes/:id" element={<IncomeDetail />} />
            <Route path="/settlement-accounts" element={<SettlementAccountManagement />} />
            <Route path="/settlement-accounts/:id" element={<SettlementAccountDetail />} />
            <Route path="/customers" element={<CustomerManagement />} />
            <Route path="/customers/:id" element={<CustomerDetail />} />
            <Route path="/customers/edit/:id" element={<CustomerEdit />} />
            <Route path="/suppliers" element={<SupplierManagement />} />
            <Route path="/suppliers/:id" element={<SupplierDetail />} />
            <Route path="/suppliers/edit/:id" element={<SupplierEdit />} />
            <Route path="/retail" element={<RetailManagement />} />
            <Route path="/retail/:id" element={<RetailDetail />} />
            <Route path="/sales-return" element={<SalesReturnManagement />} />
            <Route path="/sales-return/:id" element={<SalesReturnDetail />} />
            <Route path="/other-stock" element={<OtherStockManagement />} />
            <Route path="/other-stock/:id" element={<OtherStockDetail />} />
            <Route path="/stock-transfers" element={<StockTransferManagement />} />
            <Route path="/stock-transfers/:id" element={<StockTransferDetail />} />
            <Route path="/procurement-returns" element={<ProcurementReturnManagement />} />
            <Route path="/procurement-returns/:id" element={<ProcurementReturnDetail />} />
            {/* Report routes */}
            <Route path="/reports/sales" element={<SalesReport />} />
            <Route path="/reports/purchase" element={<PurchaseReport />} />
            <Route path="/reports/inventory" element={<InventoryReport />} />
            <Route path="/reports/financial" element={<FinancialReport />} />
            
            {/* System management routes */}
            <Route path="/system" element={<SystemManagement />} />
            <Route path="/system/users" element={<UserManagement />} />
          </Route>
          
          {/* 404 page */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Suspense>
    </Router>
  );
};

export default App;
