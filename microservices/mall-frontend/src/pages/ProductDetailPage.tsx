import { Row, Col, Card, Button, InputNumber, Tabs, Tag, Space, Descriptions, Image } from 'antd'
import { ShoppingCartOutlined, HeartOutlined, ShareAltOutlined } from '@ant-design/icons'
import { useCartStore } from '../store/cartStore'
import { useNavigate } from 'react-router-dom'

function ProductDetailPage() {
  const addItem = useCartStore((s) => s.addItem)
  const navigate = useNavigate()

  const handleAddToCart = () => {
    addItem({
      id: Date.now(),
      skuId: 1,
      productName: '精选商品',
      price: 199.00,
      quantity: 1,
      image: '',
      checked: true,
    })
  }

  return (
    <div>
      <Row gutter={32}>
        <Col xs={24} md={10}>
          <Card style={{ borderRadius: 12 }}>
            <div style={{ height: 400, background: 'linear-gradient(135deg, #e6f7ff, #bae7ff)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 80, borderRadius: 8 }}>
              🛍️
            </div>
          </Card>
        </Col>
        <Col xs={24} md={14}>
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <h2>精选商品 - 高品质产品</h2>
            <div>
              <Tag color="red">限时特惠</Tag>
              <Tag color="blue">包邮</Tag>
              <Tag>7天无理由退换</Tag>
            </div>
            <div>
              <span style={{ fontSize: 28, color: '#ff4d4f', fontWeight: 700 }}>¥199.00</span>
              <span style={{ marginLeft: 12, textDecoration: 'line-through', color: '#999' }}>¥399.00</span>
            </div>
            <Descriptions column={2} size="small">
              <Descriptions.Item label="品牌">品牌名</Descriptions.Item>
              <Descriptions.Item label="分类">电子产品</Descriptions.Item>
              <Descriptions.Item label="库存">999件</Descriptions.Item>
              <Descriptions.Item label="销量">1.2万</Descriptions.Item>
            </Descriptions>
            <Space>
              <span>数量：</span>
              <InputNumber min={1} max={99} defaultValue={1} />
            </Space>
            <Space size="large">
              <Button type="primary" size="large" icon={<ShoppingCartOutlined />} onClick={handleAddToCart}>
                加入购物车
              </Button>
              <Button size="large" danger onClick={() => { handleAddToCart(); navigate('/cart') }}>
                立即购买
              </Button>
              <Button icon={<HeartOutlined />}>收藏</Button>
              <Button icon={<ShareAltOutlined />}>分享</Button>
            </Space>
          </Space>
        </Col>
      </Row>

      <Card style={{ marginTop: 24 }}>
        <Tabs items={[
          { key: 'detail', label: '商品详情', children: (
            <div style={{ padding: 24, lineHeight: 2 }}>
              <h3>商品详情</h3>
              <p>这是一款高品质的商品，采用优质材料制作，工艺精湛。</p>
              <p>产品参数：</p>
              <Descriptions column={2} bordered size="small">
                <Descriptions.Item label="材质">优质材料</Descriptions.Item>
                <Descriptions.Item label="产地">中国</Descriptions.Item>
                <Descriptions.Item label="重量">0.5kg</Descriptions.Item>
                <Descriptions.Item label="尺寸">30x20x10cm</Descriptions.Item>
              </Descriptions>
            </div>
          )},
          { key: 'review', label: '用户评价', children: (
            <div style={{ padding: 24 }}>
              <p>暂无评价</p>
            </div>
          )},
        ]} />
      </Card>
    </div>
  )
}

export default ProductDetailPage