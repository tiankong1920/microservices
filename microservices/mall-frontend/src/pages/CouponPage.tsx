import { Row, Col, Card, Tag, Button, Space } from 'antd'
import { GiftOutlined } from '@ant-design/icons'

const coupons = [
  { id: 1, name: '满100减20', type: 'FIXED', value: 20, minSpend: 100, status: 'UNUSED', endTime: '2026-05-31' },
  { id: 2, name: '8折优惠券', type: 'PERCENTAGE', value: 20, minSpend: 200, status: 'UNUSED', endTime: '2026-06-30' },
  { id: 3, name: '食品品类券', type: 'CATEGORY', value: 15, minSpend: 50, status: 'USED', endTime: '2026-04-30' },
  { id: 4, name: '新人专享券', type: 'FIXED', value: 30, minSpend: 0, status: 'UNUSED', endTime: '2026-07-31' },
]

function CouponPage() {
  return (
    <div>
      <h2 style={{ marginBottom: 24 }}><GiftOutlined /> 优惠券中心</h2>
      <Row gutter={[16, 16]}>
        {coupons.map((c) => (
          <Col xs={24} sm={12} md={8} lg={6} key={c.id}>
            <Card style={{ borderRadius: 12, border: c.status === 'USED' ? '1px dashed #d9d9d9' : '1px solid #1677ff', opacity: c.status === 'USED' ? 0.6 : 1 }}>
              <div style={{ textAlign: 'center', padding: '16px 0' }}>
                <div style={{ fontSize: 36, fontWeight: 700, color: '#ff4d4f' }}>
                  {c.type === 'FIXED' ? `¥${c.value}` : c.type === 'PERCENTAGE' ? `${100 - c.value}%OFF` : `¥${c.value}`}
                </div>
                <div style={{ fontSize: 14, color: '#666', marginTop: 4 }}>{c.name}</div>
                <div style={{ fontSize: 12, color: '#999', marginTop: 8 }}>
                  {c.minSpend > 0 ? `满${c.minSpend}可用` : '无门槛'}
                </div>
                <Tag color={c.status === 'UNUSED' ? 'blue' : 'default'} style={{ marginTop: 8 }}>
                  {c.status === 'UNUSED' ? '可使用' : '已使用'}
                </Tag>
                <div style={{ fontSize: 12, color: '#999', marginTop: 4 }}>有效期至 {c.endTime}</div>
                {c.status === 'UNUSED' && (
                  <Button type="primary" size="small" style={{ marginTop: 8 }}>立即使用</Button>
                )}
              </div>
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  )
}

export default CouponPage