import { Card, Row, Col, Statistic, Table, Tag, Button, Space, Avatar, Descriptions } from 'antd'
import { TeamOutlined, DollarOutlined, RiseOutlined, UserOutlined } from '@ant-design/icons'

function DistributorPage() {
  const columns = [
    { title: '分销员', dataIndex: 'name', key: 'name' },
    { title: '等级', dataIndex: 'level', key: 'level', render: (v: number) => <Tag color={v === 1 ? 'gold' : v === 2 ? 'blue' : 'green'}>L{v}</Tag> },
    { title: '佣金比例', dataIndex: 'commissionRate', key: 'rate', render: (v: number) => `${v}%` },
    { title: '累计佣金', dataIndex: 'totalCommission', key: 'total', render: (v: number) => <span className="price-tag-small">¥{v}</span> },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Tag color={v === 'ACTIVE' ? 'green' : 'red'}>{v === 'ACTIVE' ? '活跃' : '停用'}</Tag> },
  ]

  const mockSubs = [
    { key: 1, name: '分销员A', level: 2, commissionRate: 10, totalCommission: 1500, status: 'ACTIVE' },
    { key: 2, name: '分销员B', level: 3, commissionRate: 5, totalCommission: 800, status: 'ACTIVE' },
  ]

  return (
    <div>
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={12} sm={6}>
          <Card><Statistic title="我的等级" value="L1" prefix={<UserOutlined />} /></Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card><Statistic title="累计佣金" value={3280.50} prefix={<DollarOutlined />} precision={2} /></Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card><Statistic title="下级人数" value={12} prefix={<TeamOutlined />} /></Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card><Statistic title="本月业绩" value={15800} prefix={<RiseOutlined />} /></Card>
        </Col>
      </Row>

      <Card title="我的分销信息" style={{ marginBottom: 24 }}>
        <Descriptions column={2}>
          <Descriptions.Item label="分销码">DIST001</Descriptions.Item>
          <Descriptions.Item label="佣金比例">15%</Descriptions.Item>
          <Descriptions.Item label="可提现佣金">¥2,280.50</Descriptions.Item>
          <Descriptions.Item label="已提现佣金">¥1,000.00</Descriptions.Item>
        </Descriptions>
        <Space style={{ marginTop: 16 }}>
          <Button type="primary">申请提现</Button>
          <Button>分享推广链接</Button>
        </Space>
      </Card>

      <Card title="下级分销员">
        <Table columns={columns} dataSource={mockSubs} pagination={{ pageSize: 10 }} />
      </Card>
    </div>
  )
}

export default DistributorPage