import { Table, Card, Tag, Space, Select, Button } from 'antd'
import { useState } from 'react'
import { Link } from 'react-router-dom'

const statusMap: Record<string, { color: string; label: string }> = {
  PENDING: { color: 'orange', label: '待支付' },
  PAID: { color: 'blue', label: '已支付' },
  SHIPPED: { color: 'cyan', label: '已发货' },
  COMPLETED: { color: 'green', label: '已完成' },
  CANCELLED: { color: 'red', label: '已取消' },
}

function OrderListPage() {
  const [status, setStatus] = useState<string | null>(null)

  const columns = [
    { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', render: (v: string) => <Link to={`/orders/${v}`}>{v}</Link> },
    { title: '金额', dataIndex: 'totalAmount', key: 'totalAmount', render: (v: number) => <span className="price-tag-small">¥{v}</span> },
    { title: '状态', dataIndex: 'orderStatus', key: 'status', render: (v: string) => {
      const s = statusMap[v] || { color: 'default', label: v }
      return <Tag color={s.color}>{s.label}</Tag>
    }},
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt' },
    { title: '操作', key: 'action', render: () => <Space><Button size="small">详情</Button><Button size="small" danger>取消</Button></Space> },
  ]

  const mockData = Array.from({ length: 5 }, (_, i) => ({
    key: i,
    orderNo: `ORD20260419${1000 + i}`,
    totalAmount: (99 + i * 50).toFixed(2),
    orderStatus: ['PENDING', 'PAID', 'SHIPPED', 'COMPLETED', 'CANCELLED'][i],
    createdAt: '2026-04-19 10:00:00',
  }))

  return (
    <Card title="我的订单" extra={
      <Select style={{ width: 120 }} placeholder="订单状态" allowClear onChange={(v) => setStatus(v)}>
        <Select.Option value="PENDING">待支付</Select.Option>
        <Select.Option value="PAID">已支付</Select.Option>
        <Select.Option value="SHIPPED">已发货</Select.Option>
        <Select.Option value="COMPLETED">已完成</Select.Option>
        <Select.Option value="CANCELLED">已取消</Select.Option>
      </Select>
    }>
      <Table columns={columns} dataSource={mockData} pagination={{ pageSize: 10 }} />
    </Card>
  )
}

export default OrderListPage