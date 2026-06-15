import { Row, Col, Card, Button, Tag, Progress, Space, Statistic } from 'antd'
import { ThunderboltOutlined, ClockCircleOutlined } from '@ant-design/icons'

const flashItems = [
  { id: 1, name: '限时秒杀商品1', price: 49.9, originalPrice: 199.9, soldPercent: 78 },
  { id: 2, name: '限时秒杀商品2', price: 99.0, originalPrice: 399.0, soldPercent: 45 },
  { id: 3, name: '限时秒杀商品3', price: 29.9, originalPrice: 99.9, soldPercent: 92 },
  { id: 4, name: '限时秒杀商品4', price: 159.0, originalPrice: 499.0, soldPercent: 30 },
]

function FlashSalePage() {
  return (
    <div>
      <Card style={{ background: 'linear-gradient(135deg, #ff4d4f, #ff7875)', marginBottom: 24, borderRadius: 12 }}>
        <Space style={{ width: '100%', justifyContent: 'center', color: '#fff' }}>
          <ThunderboltOutlined style={{ fontSize: 32 }} />
          <h1 style={{ color: '#fff', margin: 0 }}>限时秒杀</h1>
          <ClockCircleOutlined style={{ fontSize: 24 }} />
          <span style={{ fontSize: 18 }}>距结束 02:30:15</span>
        </Space>
      </Card>

      <Row gutter={[16, 16]}>
        {flashItems.map((item) => (
          <Col xs={12} sm={8} md={6} key={item.id}>
            <Card className="product-card" cover={
              <div style={{ position: 'relative', height: 180, background: 'linear-gradient(135deg, #fff1f0, #ffccc7)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 40 }}>
                <div className="flash-sale-badge">秒杀</div>
                ⚡
              </div>
            }>
              <Card.Meta
                title={item.name}
                description={
                  <Space direction="vertical" size={4} style={{ width: '100%' }}>
                    <Space>
                      <span className="price-tag">¥{item.price}</span>
                      <span style={{ textDecoration: 'line-through', color: '#999', fontSize: 12 }}>¥{item.originalPrice}</span>
                    </Space>
                    <Progress percent={item.soldPercent} size="small" strokeColor="#ff4d4f" />
                    <Button type="primary" danger block size="small">立即抢购</Button>
                  </Space>
                }
              />
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  )
}

export default FlashSalePage