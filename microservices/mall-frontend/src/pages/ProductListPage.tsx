import { useState } from 'react'
import { Row, Col, Card, Select, Input, Pagination, Tag, Space, Tree } from 'antd'
import { Link } from 'react-router-dom'

const { Search } = Input

function ProductListPage() {
  const [category, setCategory] = useState<number | null>(null)

  const categories = [
    { title: '全部分类', key: 0, children: [
      { title: '电子产品', key: 1, children: [
        { title: '手机', key: 11 },
        { title: '电脑', key: 12 },
      ]},
      { title: '服装', key: 2, children: [
        { title: '男装', key: 21 },
        { title: '女装', key: 22 },
      ]},
      { title: '食品', key: 3 },
      { title: '家居', key: 4 },
    ]},
  ]

  return (
    <div>
      <Row gutter={24}>
        <Col xs={24} sm={6} md={5}>
          <Card title="商品分类" style={{ marginBottom: 16 }}>
            <Tree
              defaultExpandAll
              treeData={categories}
              onSelect={(keys) => setCategory(keys[0] as number || null)}
            />
          </Card>
        </Col>
        <Col xs={24} sm={18} md={19}>
          <Space style={{ marginBottom: 16, width: '100%' }} size="middle">
            <Search placeholder="搜索商品" style={{ width: 300 }} />
            <Select defaultValue="default" style={{ width: 120 }}>
              <Select.Option value="default">默认排序</Select.Option>
              <Select.Option value="price-asc">价格升序</Select.Option>
              <Select.Option value="price-desc">价格降序</Select.Option>
              <Select.Option value="sales">销量优先</Select.Option>
            </Select>
          </Space>
          <Row gutter={[16, 16]}>
            {Array.from({ length: 12 }, (_, i) => i + 1).map((i) => (
              <Col xs={12} sm={8} md={6} key={i}>
                <Link to={`/products/${i}`}>
                  <Card className="product-card" cover={
                    <div style={{ height: 180, background: `linear-gradient(135deg, hsl(${i * 30}, 60%, 85%), hsl(${i * 30 + 30}, 60%, 75%))`, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 40 }}>
                      🛍️
                    </div>
                  }>
                    <Card.Meta
                      title={`商品 ${i}`}
                      description={
                        <Space direction="vertical" size={4}>
                          <span className="price-tag">¥{(49 + i * 30).toFixed(2)}</span>
                          <Space>
                            <Tag color="blue">新品</Tag>
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
          <div style={{ textAlign: 'center', marginTop: 24 }}>
            <Pagination defaultCurrent={1} total={100} />
          </div>
        </Col>
      </Row>
    </div>
  )
}

export default ProductListPage