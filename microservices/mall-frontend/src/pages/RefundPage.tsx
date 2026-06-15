import { Card, Table, Tag, Button, Space, Select, Form, Input, Modal, message } from 'antd'
import { useState } from 'react'

const statusMap: Record<string, { color: string; label: string }> = {
  PENDING: { color: 'orange', label: '待审核' },
  APPROVED: { color: 'blue', label: '已通过' },
  REJECTED: { color: 'red', label: '已拒绝' },
  PROCESSING: { color: 'cyan', label: '退款中' },
  COMPLETED: { color: 'green', label: '已完成' },
  FAILED: { color: 'red', label: '退款失败' },
}

function RefundPage() {
  const [modalOpen, setModalOpen] = useState(false)

  const columns = [
    { title: '退款单号', dataIndex: 'id', key: 'id' },
    { title: '订单号', dataIndex: 'orderId', key: 'orderId' },
    { title: '退款类型', dataIndex: 'refundType', key: 'refundType', render: (v: string) => v === 'REFUND_ONLY' ? '仅退款' : '退货退款' },
    { title: '退款金额', dataIndex: 'refundAmount', key: 'refundAmount', render: (v: number) => <span className="price-tag-small">¥{v}</span> },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => {
      const s = statusMap[v] || { color: 'default', label: v }
      return <Tag color={s.color}>{s.label}</Tag>
    }},
    { title: '操作', key: 'action', render: () => <Button size="small">查看详情</Button> },
  ]

  const mockData = [
    { key: 1, id: 'RF001', orderId: 'ORD001', refundType: 'REFUND_ONLY', refundAmount: 99.00, status: 'COMPLETED' },
    { key: 2, id: 'RF002', orderId: 'ORD002', refundType: 'RETURN_REFUND', refundAmount: 199.00, status: 'PENDING' },
  ]

  return (
    <div>
      <Card title="售后退款" extra={<Button type="primary" onClick={() => setModalOpen(true)}>申请退款</Button>}>
        <Table columns={columns} dataSource={mockData} pagination={{ pageSize: 10 }} />
      </Card>

      <Modal title="申请退款" open={modalOpen} onCancel={() => setModalOpen(false)} onOk={() => { message.success('退款申请已提交'); setModalOpen(false) }}>
        <Form layout="vertical">
          <Form.Item label="退款类型" required>
            <Select placeholder="请选择退款类型">
              <Select.Option value="REFUND_ONLY">仅退款</Select.Option>
              <Select.Option value="RETURN_REFUND">退货退款</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item label="退款金额" required>
            <Input type="number" placeholder="请输入退款金额" />
          </Form.Item>
          <Form.Item label="退款原因" required>
            <Input.TextArea rows={3} placeholder="请输入退款原因" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default RefundPage