import { Card, Descriptions, Steps, Tag, Button, Space, Timeline } from 'antd'
import { useParams } from 'react-router-dom'

function OrderDetailPage() {
  const { id } = useParams()

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card title={`订单详情 - ${id}`}>
        <Steps current={2} items={[
          { title: '提交订单' },
          { title: '支付成功' },
          { title: '已发货' },
          { title: '已完成' },
        ]} style={{ marginBottom: 24 }} />

        <Descriptions bordered column={2}>
          <Descriptions.Item label="订单号">ORD202604191000</Descriptions.Item>
          <Descriptions.Item label="订单状态"><Tag color="cyan">已发货</Tag></Descriptions.Item>
          <Descriptions.Item label="商品总额">¥299.00</Descriptions.Item>
          <Descriptions.Item label="实付金额"><span className="price-tag">¥199.00</span></Descriptions.Item>
          <Descriptions.Item label="收货人">张三</Descriptions.Item>
          <Descriptions.Item label="联系电话">138****8888</Descriptions.Item>
          <Descriptions.Item label="收货地址" span={2}>北京市朝阳区xxx</Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title="物流信息">
        <Timeline items={[
          { children: '2026-04-19 14:00 快递已到达北京分拨中心' },
          { children: '2026-04-19 10:00 快递已从上海发出' },
          { children: '2026-04-19 08:00 商家已发货' },
        ]} />
      </Card>

      <Card title="操作">
        <Space>
          <Button type="primary">确认收货</Button>
          <Button>申请退款</Button>
        </Space>
      </Card>
    </Space>
  )
}

export default OrderDetailPage