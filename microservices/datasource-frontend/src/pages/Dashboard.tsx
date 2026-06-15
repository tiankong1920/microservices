import React, { useEffect, useState } from 'react'
import { Card, Row, Col, Statistic, Table, Tag, Progress, Spin, message } from 'antd'
import {
  DatabaseOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons'
import { Pie, Column } from '@ant-design/plots'
import { dashboardApi } from '../services/datasourceApi'
import type { DashboardStats } from '../types'

const Dashboard: React.FC = () => {
  const [loading, setLoading] = useState(true)
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [healthData, setHealthData] = useState<any[]>([])

  useEffect(() => {
    fetchData()
    const interval = setInterval(fetchData, 5000)
    return () => clearInterval(interval)
  }, [])

  const fetchData = async () => {
    try {
      const [statsRes, healthRes]: any = await Promise.all([
        dashboardApi.getStats(),
        dashboardApi.getHealthOverview(),
      ])
      setStats(statsRes.data)
      setHealthData(healthRes.data)
    } catch (error) {
      message.error('获取仪表盘数据失败')
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
        <Spin size="large" />
      </div>
    )
  }

  const statusColumns = [
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => <Tag color="blue">{type}</Tag>,
    },
    {
      title: '地址',
      key: 'address',
      render: (_: any, record: any) => `${record.host}:${record.port}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        const colorMap: Record<string, string> = {
          CONNECTED: 'success',
          DISCONNECTED: 'error',
          ERROR: 'warning',
          UNKNOWN: 'default',
        }
        const iconMap: Record<string, React.ReactNode> = {
          CONNECTED: <CheckCircleOutlined />,
          DISCONNECTED: <CloseCircleOutlined />,
          ERROR: <ExclamationCircleOutlined />,
        }
        return (
          <Tag color={colorMap[status]} icon={iconMap[status]}>
            {status}
          </Tag>
        )
      },
    },
    {
      title: '响应时间',
      dataIndex: 'responseTime',
      key: 'responseTime',
      render: (time: number) => (time ? `${time}ms` : '-'),
    },
  ]

  const typePieConfig = {
    appendPadding: 10,
    data: stats?.datasourceTypeDistribution
      ? Object.entries(stats.datasourceTypeDistribution).map(([type, count]) => ({
          type,
          value: count,
        }))
      : [],
    angleField: 'value',
    colorField: 'type',
    radius: 0.8,
    label: {
      type: 'outer',
      content: '{name} {percentage}',
    },
    interactions: [{ type: 'pie-legend-active' }, { type: 'element-active' }],
  }

  return (
    <div>
      <div className="page-header">
        <div className="page-title">仪表盘</div>
        <div className="page-subtitle">数据源连接状态监控概览</div>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="数据源总数"
              value={stats?.totalDatasources || 0}
              prefix={<DatabaseOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="已连接"
              value={stats?.connectedDatasources || 0}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="连接失败"
              value={stats?.disconnectedDatasources || 0}
              prefix={<CloseCircleOutlined />}
              valueStyle={{ color: '#ff4d4f' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="平均响应时间"
              value={stats?.averageResponseTime || 0}
              suffix="ms"
              precision={0}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card title="连接成功率">
            <Progress
              type="dashboard"
              percent={Math.round((stats?.connectionSuccessRate || 0) * 100)}
              strokeColor={{
                '0%': '#108ee9',
                '100%': '#87d068',
              }}
            />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="数据源类型分布">
            <Pie {...typePieConfig} />
          </Card>
        </Col>
      </Row>

      <Card title="健康状态概览" style={{ marginTop: 16 }}>
        <Table
          columns={statusColumns}
          dataSource={healthData}
          rowKey="id"
          pagination={false}
          size="small"
        />
      </Card>
    </div>
  )
}

export default Dashboard
