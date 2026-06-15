import { Row, Col, Card, Button, Tag, Space, Table, Badge } from 'antd'
import { GiftOutlined, ShoppingCartOutlined, CheckCircleOutlined } from '@ant-design/icons'

const fullDiscountRules = [
  { id: 1, name: '满100减20', minAmount: 100, discount: 20, status: 'active' },
  { id: 2, name: '满200减50', minAmount: 200, discount: 50, status: 'active' },
  { id: 3, name: '满500减120', minAmount: 500, discount: 120, status: 'active' },
  { id: 4, name: '满1000减300', minAmount: 1000, discount: 300, status: 'active' },
]

const columns = [
  { title: '活动名称', dataIndex: 'name', key: 'name' },
  { title: '满减门槛', dataIndex: 'minAmount', key: 'minAmount', render: (v: number) => `¥${v}` },
  { title: '优惠金额', dataIndex: 'discount', key: 'discount', render: (v: number) => <span style={{ color: '#ff4d4f', fontWeight: 'bold' }}>-¥{v}</span> },
  { title: '状态', dataIndex: 'status', key: 'status', render: () => <Badge status="success" text="进行中" /> },
  { title: '操作', key: 'action', render: () => <Button type="link" size="small">立即使用</Button> },
]

function FullDiscountPage() {
  return (
    <div>
      <Card style={{ background: 'linear-gradient(135deg, #fa8c16, #ffa940)', marginBottom: 24, borderRadius: 12 }}>
        <Space style={{ width: '100%', justifyContent: 'center', color: '#fff' }}>
          <GiftOutlined style={{ fontSize: 32 }} />
          <h1 style={{ color: '#fff', margin: 0 }}>满减优惠</h1>
          <Tag color="gold">多买多省</Tag>
        </Space>
      </Card>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        {fullDiscountRules.slice(0, 4).map((rule) => (
          <Col xs={12} sm={6} key={rule.id}>
            <Card 
              style={{ textAlign: 'center', borderRadius: 12, border: '2px solid #fa8c16' }}
              bodyStyle={{ padding: '16px 12px' }}
            >
              <div style={{ fontSize: 24, fontWeight: 'bold', color: '#fa8c16' }}>
                满{rule.minAmount}减{rule.discount}
              </div>
              <div style={{ fontSize: 12, color: '#999', marginTop: 8 }}>
                满¥{rule.minAmount}可用
              </div>
              <Button 
                type="primary" 
                style={{ background: '#fa8c16', borderColor: '#fa8c16', marginTop: 12 }} 
                size="small"
                icon={<ShoppingCartOutlined />}
              >
                去凑单
              </Button>
            </Card>
          </Col>
        ))}
      </Row>

      <Card title="满减活动列表">
        <Table 
          dataSource={fullDiscountRules} 
          columns={columns} 
          rowKey="id"
          pagination={false}
        />
      </Card>

      <Card title="优惠计算器" style={{ marginTop: 24 }}>
        <Space direction="vertical" size={16} style={{ width: '100%' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <span>订单金额：</span>
            <span style={{ fontSize: 24, fontWeight: 'bold', color: '#fa8c16' }}>¥350.00</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <span>适用优惠：</span>
            <Tag color="orange">满200减50</Tag>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <span>优惠金额：</span>
            <span style={{ fontSize: 24, fontWeight: 'bold', color: '#ff4d4f' }}>-¥50.00</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <span>实付金额：</span>
            <span style={{ fontSize: 28, fontWeight: 'bold', color: '#52c41a' }}>¥300.00</span>
          </div>
          <Button type="primary" size="large" style={{ background: '#fa8c16', borderColor: '#fa8c16' }} block>
            <CheckCircleOutlined /> 立即结算
          </Button>
        </Space>
      </Card>
    </div>
  )
}

export default FullDiscountPage
