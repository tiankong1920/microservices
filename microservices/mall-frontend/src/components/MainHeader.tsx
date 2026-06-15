import { Link } from 'react-router-dom'
import { Layout, Menu, Input, Badge, Button } from 'antd'
import { ShoppingCartOutlined, HomeOutlined, AppstoreOutlined, ThunderboltOutlined, TeamOutlined, GiftOutlined, CustomerServiceOutlined, UserOutlined } from '@ant-design/icons'
import { useCartStore } from '../store/cartStore'

const { Header } = Layout
const { Search } = Input

function MainHeader() {
  const cartCount = useCartStore((s) => s.totalCount)

  const menuItems = [
    { key: '/', label: <Link to="/">首页</Link>, icon: <HomeOutlined /> },
    { key: '/products', label: <Link to="/products">商品</Link>, icon: <AppstoreOutlined /> },
    { key: '/flash-sale', label: <Link to="/flash-sale">秒杀</Link>, icon: <ThunderboltOutlined /> },
    { key: '/group-buy', label: <Link to="/group-buy">拼团</Link>, icon: <TeamOutlined /> },
    { key: '/coupons', label: <Link to="/coupons">优惠券</Link>, icon: <GiftOutlined /> },
    { key: '/refund', label: <Link to="/refund">售后</Link>, icon: <CustomerServiceOutlined /> },
  ]

  return (
    <Header style={{ display: 'flex', alignItems: 'center', background: '#fff', boxShadow: '0 2px 8px rgba(0,0,0,0.06)', padding: '0 24px', position: 'sticky', top: 0, zIndex: 100 }}>
      <div style={{ fontSize: 20, fontWeight: 700, color: '#1677ff', marginRight: 40, whiteSpace: 'nowrap' }}>
        🛒 企业商城
      </div>
      <Menu mode="horizontal" items={menuItems} style={{ flex: 1, border: 'none' }} />
      <Search placeholder="搜索商品" style={{ width: 240, marginRight: 16 }} />
      <Link to="/cart">
        <Badge count={cartCount} size="small">
          <Button type="text" icon={<ShoppingCartOutlined style={{ fontSize: 20 }} />} />
        </Badge>
      </Link>
      <Button type="text" icon={<UserOutlined style={{ fontSize: 18 }} />} style={{ marginLeft: 8 }}>
        登录
      </Button>
    </Header>
  )
}

export default MainHeader