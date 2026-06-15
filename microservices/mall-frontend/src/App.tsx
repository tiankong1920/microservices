import { Routes, Route, Navigate } from 'react-router-dom'
import { Layout } from 'antd'
import MainHeader from './components/MainHeader'
import HomePage from './pages/HomePage'
import ProductListPage from './pages/ProductListPage'
import ProductDetailPage from './pages/ProductDetailPage'
import CartPage from './pages/CartPage'
import OrderListPage from './pages/OrderListPage'
import OrderDetailPage from './pages/OrderDetailPage'
import CouponPage from './pages/CouponPage'
import FlashSalePage from './pages/FlashSalePage'
import GroupBuyPage from './pages/GroupBuyPage'
import BargainPage from './pages/BargainPage'
import FullDiscountPage from './pages/FullDiscountPage'
import RefundPage from './pages/RefundPage'
import ShipmentPage from './pages/ShipmentPage'
import DistributorPage from './pages/DistributorPage'

const { Content, Footer } = Layout

function App() {
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <MainHeader />
      <Content style={{ padding: '24px', maxWidth: 1400, margin: '0 auto', width: '100%' }}>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/products" element={<ProductListPage />} />
          <Route path="/products/:id" element={<ProductDetailPage />} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/orders" element={<OrderListPage />} />
          <Route path="/orders/:id" element={<OrderDetailPage />} />
          <Route path="/coupons" element={<CouponPage />} />
          <Route path="/flash-sale" element={<FlashSalePage />} />
          <Route path="/group-buy" element={<GroupBuyPage />} />
          <Route path="/bargain" element={<BargainPage />} />
          <Route path="/full-discount" element={<FullDiscountPage />} />
          <Route path="/refund" element={<RefundPage />} />
          <Route path="/shipment" element={<ShipmentPage />} />
          <Route path="/distributor" element={<DistributorPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Content>
      <Footer style={{ textAlign: 'center' }}>
        企业商城系统 ©{new Date().getFullYear()}
      </Footer>
    </Layout>
  )
}

export default App
