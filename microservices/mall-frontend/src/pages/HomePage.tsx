import { Row, Col, Card, Typography, Carousel, Tag, Space } from 'antd'
import { Link } from 'react-router-dom'
import { ThunderboltOutlined, TeamOutlined, GiftOutlined, FireOutlined } from '@ant-design/icons'

const { Title, Paragraph } = Typography

const banners = [
  { title: '新品上市', desc: '精选好物，限时特惠', color: '#1677ff' },
  { title: '秒杀专区', desc: '超值秒杀，手慢无', color: '#ff4d4f' },
  { title: '拼团优惠', desc: '邀请好友，共享低价', color: '#52c41a' },
]

const quickLinks = [
  { title: '秒杀', icon: <ThunderboltOutlined />, path: '/flash-sale', color: '#ff4d4f' },
  { title: '拼团', icon: <TeamOutlined />, path: '/group-buy', color: '#fa8c16' },
  { title: '优惠券', icon: <GiftOutlined />, path: '/coupons', color: '#1677ff' },
  { title: '热销', icon: <FireOutlined />, path: '/products', color: '#eb2f96' },
]

function HomePage() {
  return (
    <div>
      <Carousel autoplay style={{ marginBottom: 24 }}>
        {banners.map((b, i) => (
          <div key={i}>
            <div style={{ background: b.color, height: 300, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', color: '#fff', borderRadius: 8 }}>
              <Title level={1} style={{ color: '#fff', marginBottom: 8 }}>{b.title}</Title>
              <Paragraph style={{ color: 'rgba(255,255,255,0.9)', fontSize: 18 }}>{b.desc}</Paragraph>
            </div>
          </div>
        ))}
      </Carousel>

      <Row gutter={[16, 16]} style={{ marginBottom: 32 }}>
        {quickLinks.map((link) => (
          <Col xs={6} sm={6} md={6} key={link.title}>
            <Link to={link.path}>
              <Card hoverable style={{ textAlign: 'center', borderRadius: 12 }}>
                <div style={{ fontSize: 32, color: link.color, marginBottom: 8 }}>{link.icon}</div>
                <div style={{ fontWeight: 600 }}>{link.title}</div>
              </Card>
            </Link>
          </Col>
        ))}
      </Row>

      <Title level={3} style={{ marginBottom: 16 }}>
        <FireOutlined style={{ color: '#ff4d4f', marginRight: 8 }} />
        热门推荐
      </Title>
      <Row gutter={[16, 16]}>
        {[1, 2, 3, 4, 5, 6, 7, 8].map((i) => (
          <Col xs={12} sm={8} md={6} key={i}>
            <Link to={`/products/${i}`}>
              <Card className="product-card" cover={
                <div style={{ height: 200, background: `linear-gradient(135deg, hsl(${i * 45}, 70%, 80%), hsl(${i * 45 + 40}, 70%, 70%))`, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 48 }}>
                  🛍️
                </div>
              }>
                <Card.Meta
                  title={<span>热门商品 {i}</span>}
                  description={
                    <Space direction="vertical" size={4}>
                      <span className="price-tag">¥{(99 + i * 50).toFixed(2)}</span>
                      <Space>
                        <Tag color="red">热销</Tag>
                        <Tag>包邮</Tag>
                      </Space>
                    </Space>
                  }
                />
              </Card>
            </Link>
          </Col>
        ))}
      </Row>
    </div>
  )
}

export default HomePage