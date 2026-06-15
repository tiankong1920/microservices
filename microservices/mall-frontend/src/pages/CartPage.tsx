import { Table, Button, InputNumber, Card, Space, Divider, Empty, message } from 'antd'
import { DeleteOutlined, ShoppingCartOutlined } from '@ant-design/icons'
import { useCartStore } from '../store/cartStore'
import { useNavigate } from 'react-router-dom'

function CartPage() {
  const { items, removeItem, updateQuantity, toggleCheck, toggleCheckAll, getCheckedTotal } = useCartStore()
  const navigate = useNavigate()
  const checkedTotal = getCheckedTotal()

  const columns = [
    { title: '商品', dataIndex: 'productName', key: 'name' },
    { title: '单价', dataIndex: 'price', key: 'price', render: (v: number) => <span className="price-tag-small">¥{v.toFixed(2)}</span> },
    { title: '数量', key: 'quantity', render: (_: unknown, record: { id: number; quantity: number }) => (
      <InputNumber min={1} max={99} value={record.quantity} onChange={(v) => updateQuantity(record.id, v || 1)} />
    )},
    { title: '小计', key: 'subtotal', render: (_: unknown, record: { price: number; quantity: number }) => (
      <span className="price-tag-small">¥{(record.price * record.quantity).toFixed(2)}</span>
    )},
    { title: '操作', key: 'action', render: (_: unknown, record: { id: number }) => (
      <Button type="text" danger icon={<DeleteOutlined />} onClick={() => removeItem(record.id)}>删除</Button>
    )},
  ]

  return (
    <div>
      <Card title={<ShoppingCartOutlined /> + ' 购物车'}>
        {items.length === 0 ? (
          <Empty description="购物车是空的" />
        ) : (
          <>
            <Table
              rowKey="id"
              columns={columns}
              dataSource={items}
              pagination={false}
              rowSelection={{
                selectedRowKeys: items.filter((i) => i.checked).map((i) => i.id),
                onChange: (keys) => toggleCheckAll(keys.length === items.length),
              }}
            />
            <Divider />
            <Space style={{ width: '100%', justifyContent: 'space-between', display: 'flex' }}>
              <Space>
                <Button type="link" onClick={() => toggleCheckAll(true)}>全选</Button>
                <Button type="link" onClick={() => toggleCheckAll(false)}>取消全选</Button>
              </Space>
              <Space size="large">
                <span>已选 <strong>{items.filter((i) => i.checked).length}</strong> 件</span>
                <span>合计：<span className="price-tag">¥{checkedTotal.toFixed(2)}</span></span>
                <Button type="primary" size="large" onClick={() => { message.success('订单创建成功'); navigate('/orders') }}>
                  去结算
                </Button>
              </Space>
            </Space>
          </>
        )}
      </Card>
    </div>
  )
}

export default CartPage