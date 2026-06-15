import { Row, Col, Card, Button, Tag, Space, Progress, Avatar, List } from 'antd'
import { ScissorOutlined, TeamOutlined, TrophyOutlined } from '@ant-design/icons'

const bargainActivities = [
  { id: 1, name: '爆款商品砍价', originalPrice: 299.0, minPrice: 49.0, maxCount: 10, participants: 1280 },
  { id: 2, name: '新品首发砍价', originalPrice: 599.0, minPrice: 99.0, maxCount: 15, participants: 856 },
  { id: 3, name: '限时特惠砍价', originalPrice: 199.0, minPrice: 29.0, maxCount: 8, participants: 2340 },
]

const myBargains = [
  { id: 1, productName: '爆款商品砍价', currentPrice: 89.0, minPrice: 49.0, progress: 70, status: 'ongoing' },
  { id: 2, productName: '新品首发砍价', currentPrice: 99.0, minPrice: 99.0, progress: 100, status: 'success' },
]

function BargainPage() {
  return (
    <div>
      <Card style={{ background: 'linear-gradient(135deg, #722ed1, #9254de)', marginBottom: 24, borderRadius: 12 }}>
        <Space style={{ width: '100%', justifyContent: 'center', color: '#fff' }}>
          <ScissorOutlined style={{ fontSize: 32 }} />
          <h1 style={{ color: '#fff', margin: 0 }}>砍价专区</h1>
          <Tag color="gold">邀请好友帮忙砍价</Tag>
        </Space>
      </Card>

      <Card title="我的砍价" style={{ marginBottom: 24 }}>
        <List
          dataSource={myBargains}
          renderItem={(item) => (
            <List.Item
              actions={[
                item.status === 'success' 
                  ? <Button type="primary" size="small">立即购买</Button>
                  : <Button type="primary" size="small">邀请好友</Button>
              ]}
            >
              <List.Item.Meta
                avatar={<Avatar style={{ backgroundColor: '#722ed1' }} icon={<ScissorOutlined />} />}
                title={item.productName}
                description={
                  <Space direction="vertical" size={4}>
                    <Space>
                      <span style={{ color: '#ff4d4f', fontWeight: 'bold' }}>当前价: ¥{item.currentPrice}</span>
                      <span style={{ color: '#52c41a' }}>最低价: ¥{item.minPrice}</span>
                    </Space>
                    <Progress 
                      percent={item.progress} 
                      size="small" 
                      strokeColor={item.status === 'success' ? '#52c41a' : '#722ed1'}
                    />
                  </Space>
                }
              />
              {item.status === 'success' && <Tag color="success">砍价成功</Tag>}
            </List.Item>
          )}
        />
      </Card>

      <Card title="热门砍价活动">
        <Row gutter={[16, 16]}>
          {bargainActivities.map((item) => (
            <Col xs={24} sm={12} md={8} key={item.id}>
              <Card className="product-card" cover={
                <div style={{ height: 160, background: 'linear-gradient(135deg, #f9f0ff, #efdbff)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 40 }}>
                  <ScissorOutlined style={{ color: '#722ed1' }} />
                </div>
              }>
                <Card.Meta
                  title={item.name}
                  description={
                    <Space direction="vertical" size={8} style={{ width: '100%' }}>
                      <Space>
                        <span className="price-tag">¥{item.minPrice}起</span>
                        <span style={{ textDecoration: 'line-through', color: '#999', fontSize: 12 }}>¥{item.originalPrice}</span>
                      </Space>
                      <Space>
                        <TeamOutlined />
                        <span>{item.participants}人参与</span>
                      </Space>
                      <Space>
                        <span style={{ fontSize: 12, color: '#666' }}>最多{item.maxCount}次砍价</span>
                      </Space>
                      <Button type="primary" style={{ background: '#722ed1', borderColor: '#722ed1' }} block size="small">
                        发起砍价
                      </Button>
                    </Space>
                  }
                />
              </Card>
            </Col>
          ))}
        </Row>
      </Card>
    </div>
  )
}

export default BargainPage
