import { Card, Table, Tag, Space, Steps, Button, Input, Timeline, Empty } from 'antd'
import { CarOutlined, EnvironmentOutlined, ClockCircleOutlined, CheckCircleOutlined, SyncOutlined } from '@ant-design/icons'

const shipments = [
  { 
    id: 1, 
    orderNo: 'ORD20240101001', 
    logisticsCompany: '顺丰速运', 
    trackingNo: 'SF1234567890', 
    status: 'DELIVERED',
    statusText: '已签收',
    estimatedTime: '2024-01-05 14:00',
    actualTime: '2024-01-05 13:25',
  },
  { 
    id: 2, 
    orderNo: 'ORD20240102001', 
    logisticsCompany: '中通快递', 
    trackingNo: 'ZT9876543210', 
    status: 'IN_TRANSIT',
    statusText: '运输中',
    estimatedTime: '2024-01-07 18:00',
    actualTime: null,
  },
  { 
    id: 3, 
    orderNo: 'ORD20240103001', 
    logisticsCompany: '京东物流', 
    trackingNo: 'JD5678901234', 
    status: 'PENDING',
    statusText: '待发货',
    estimatedTime: null,
    actualTime: null,
  },
]

const logisticsTimeline = [
  { time: '2024-01-05 13:25', status: '已签收', location: '北京市朝阳区xxx小区', operator: '快递员张三' },
  { time: '2024-01-05 09:30', status: '派送中', location: '北京市朝阳区营业点', operator: '快递员张三' },
  { time: '2024-01-04 22:15', status: '到达北京转运中心', location: '北京市转运中心', operator: '系统' },
  { time: '2024-01-04 08:00', status: '运输中', location: '上海转运中心', operator: '系统' },
  { time: '2024-01-03 18:30', status: '已发货', location: '上海市浦东新区仓库', operator: '仓库管理员' },
]

const columns = [
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo' },
  { title: '物流公司', dataIndex: 'logisticsCompany', key: 'logisticsCompany' },
  { title: '运单号', dataIndex: 'trackingNo', key: 'trackingNo' },
  { 
    title: '状态', 
    dataIndex: 'status', 
    key: 'status',
    render: (status: string, record: { statusText: string }) => {
      const colorMap: Record<string, string> = {
        'PENDING': 'default',
        'SHIPPED': 'processing',
        'IN_TRANSIT': 'blue',
        'DELIVERED': 'success',
      }
      return <Tag color={colorMap[status]}>{record.statusText}</Tag>
    }
  },
  { title: '预计送达', dataIndex: 'estimatedTime', key: 'estimatedTime', render: (v: string | null) => v || '-' },
  { 
    title: '操作', 
    key: 'action',
    render: () => <Button type="link" size="small">查看详情</Button>
  },
]

function ShipmentPage() {
  return (
    <div>
      <Card style={{ background: 'linear-gradient(135deg, #1890ff, #40a9ff)', marginBottom: 24, borderRadius: 12 }}>
        <Space style={{ width: '100%', justifyContent: 'center', color: '#fff' }}>
          <CarOutlined style={{ fontSize: 32 }} />
          <h1 style={{ color: '#fff', margin: 0 }}>物流查询</h1>
          <Tag color="cyan">实时追踪</Tag>
        </Space>
      </Card>

      <Card style={{ marginBottom: 24 }}>
        <Space.Compact style={{ width: '100%' }}>
          <Input placeholder="请输入运单号或订单号" size="large" />
          <Button type="primary" size="large" icon={<SyncOutlined />}>查询</Button>
        </Space.Compact>
      </Card>

      <Card title="我的物流" style={{ marginBottom: 24 }}>
        <Table 
          dataSource={shipments} 
          columns={columns} 
          rowKey="id"
          pagination={{ pageSize: 5 }}
        />
      </Card>

      <Card title="物流详情 - SF1234567890">
        <Space direction="vertical" size={16} style={{ width: '100%' }}>
          <Space>
            <CarOutlined style={{ fontSize: 20, color: '#1890ff' }} />
            <span style={{ fontWeight: 'bold' }}>顺丰速运</span>
            <Tag color="success">已签收</Tag>
          </Space>
          
          <Timeline
            items={logisticsTimeline.map((item, index) => ({
              color: index === 0 ? 'green' : 'gray',
              children: (
                <div>
                  <div style={{ fontWeight: index === 0 ? 'bold' : 'normal' }}>
                    <CheckCircleOutlined style={{ marginRight: 8, color: index === 0 ? '#52c41a' : '#999' }} />
                    {item.status}
                  </div>
                  <div style={{ fontSize: 12, color: '#999', marginTop: 4 }}>
                    <ClockCircleOutlined style={{ marginRight: 4 }} />
                    {item.time}
                  </div>
                  <div style={{ fontSize: 12, color: '#666', marginTop: 4 }}>
                    <EnvironmentOutlined style={{ marginRight: 4 }} />
                    {item.location}
                  </div>
                </div>
              ),
            }))}
          />
        </Space>
      </Card>
    </div>
  )
}

export default ShipmentPage
