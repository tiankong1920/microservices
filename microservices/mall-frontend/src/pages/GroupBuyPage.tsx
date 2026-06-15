import { Row, Col, Card, Button, Tag, Avatar, Space, Progress } from 'antd'
import { TeamOutlined, UserOutlined } from '@ant-design/icons'

const groupBuys = [
  { id: 1, name: '拼团商品1', groupPrice: 89, singlePrice: 129, groupSize: 3, currentMembers: 2 },
  { id: 2, name: '拼团商品2', groupPrice: 159, singlePrice: 239, groupSize: 5, currentMembers: 3 },
  { id: 3, name: '拼团商品3', groupPrice: 49, singlePrice: 79, groupSize: 2, currentMembers: 1 },
]

function GroupBuyPage() {
  return (
    <div>
      <Card style={{ background: 'linear-gradient(135deg, #fa8c16, #ffc53d)', marginBottom: 24, borderRadius: 12 }}>
        <Space style={{ width: '100%', justifyContent: 'center', color: '#fff' }}>
          <TeamOutlined style={{ fontSize: 32 }} />
          <h1 style={{ color: '#fff', margin: 0 }}>拼团专区</h1>
          <span style={{ fontSize: 16 }}>邀请好友，共享低价</span>
        </Space>
      </Card>

      <Row gutter={[16, 16]}>
        {groupBuys.map((item) => (
          <Col xs={24} sm={12} md={8} key={item.id}>
            <Card style={{ borderRadius: 12 }}>
              <div style={{ height: 160, background: 'linear-gradient(135deg, #fff7e6, #ffe7ba)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 40, borderRadius: 8, marginBottom: 16 }}>
                👥
              </div>
              <h3>{item.name}</h3>
              <Space style={{ width: '100%', justifyContent: 'space-between' }}>
                <span className="price-tag">¥{item.groupPrice}</span>
                <span style={{ textDecoration: 'line-through', color: '#999' }}>¥{item.singlePrice}</span>
              </Space>
              <div style={{ marginTop: 12 }}>
                <Space>
                  {Array.from({ length: item.groupSize }, (_, i) => (
                    <Avatar key={i} size={32} icon={<UserOutlined />} style={{ background: i < item.currentMembers ? '#1677ff' : '#d9d9d9' }} />
                  ))}
                  <span style={{ fontSize: 12, color: '#666' }}>
                    还差{item.groupSize - item.currentMembers}人成团
                  </span>
                </Space>
              </div>
              <Progress percent={Math.round((item.currentMembers / item.groupSize) * 100)} size="small" style={{ marginTop: 8 }} />
              <Space style={{ width: '100%', marginTop: 12 }}>
                <Button type="primary" danger style={{ flex: 1 }}>一键拼团</Button>
                <Button style={{ flex: 1 }}>单独购买 ¥{item.singlePrice}</Button>
              </Space>
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  )
}

export default GroupBuyPage