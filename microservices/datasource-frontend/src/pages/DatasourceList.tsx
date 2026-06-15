import React, { useEffect, useState } from 'react'
import { Table, Button, Space, Tag, Input, Select, Modal, message, Popconfirm, Tooltip } from 'antd'
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
  DeleteOutlined,
  EditOutlined,
  ApiOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { datasourceApi, connectionTestApi } from '../services/datasourceApi'
import type { DatasourceConfig, DatasourceType, DatasourceStatus } from '../types'

const DatasourceList: React.FC = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [datasources, setDatasources] = useState<DatasourceConfig[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [searchParams, setSearchParams] = useState({
    name: '',
    type: undefined as DatasourceType | undefined,
    status: undefined as DatasourceStatus | undefined,
  })
  const [testingIds, setTestingIds] = useState<Set<number>>(new Set())

  useEffect(() => {
    fetchDatasources()
  }, [page, pageSize])

  const fetchDatasources = async () => {
    setLoading(true)
    try {
      const params = {
        ...searchParams,
        page,
        size: pageSize,
      }
      const res: any = await datasourceApi.search(params)
      setDatasources(res.data.content)
      setTotal(res.data.totalElements)
    } catch (error) {
      message.error('获取数据源列表失败')
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = () => {
    setPage(0)
    fetchDatasources()
  }

  const handleReset = () => {
    setSearchParams({ name: '', type: undefined, status: undefined })
    setPage(0)
    fetchDatasources()
  }

  const handleDelete = async (id: number) => {
    try {
      await datasourceApi.delete(id)
      message.success('删除成功')
      fetchDatasources()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleTestConnection = async (id: number) => {
    setTestingIds(prev => new Set(prev).add(id))
    try {
      const res: any = await connectionTestApi.test(id)
      if (res.data.result === 'SUCCESS') {
        message.success(`连接成功，响应时间: ${res.data.responseTime}ms`)
      } else {
        Modal.error({
          title: '连接失败',
          content: (
            <div>
              <p>错误信息: {res.data.errorMessage}</p>
              {res.data.suggestions && (
                <div>
                  <p>建议解决方案:</p>
                  <ul>
                    {res.data.suggestions.map((s: string, i: number) => (
                      <li key={i}>{s}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          ),
        })
      }
      fetchDatasources()
    } catch (error) {
      message.error('连接测试失败')
    } finally {
      setTestingIds(prev => {
        const next = new Set(prev)
        next.delete(id)
        return next
      })
    }
  }

  const getStatusTag = (status: string) => {
    const colorMap: Record<string, string> = {
      ACTIVE: 'success',
      INACTIVE: 'default',
      DELETED: 'error',
      ERROR: 'warning',
    }
    return <Tag color={colorMap[status] || 'default'}>{status}</Tag>
  }

  const getConnectionStatusTag = (record: DatasourceConfig) => {
    if (!record.connectionStatus) {
      return <Tag>未测试</Tag>
    }
    const { status, responseTime } = record.connectionStatus
    const colorMap: Record<string, string> = {
      CONNECTED: 'success',
      DISCONNECTED: 'error',
      ERROR: 'warning',
      TESTING: 'processing',
    }
    return (
      <Tag color={colorMap[status] || 'default'}>
        {status === 'CONNECTED' ? `已连接 (${responseTime}ms)` : status}
      </Tag>
    )
  }

  const columns = [
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
      width: 200,
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 120,
      render: (type: string) => {
        const colorMap: Record<string, string> = {
          MYSQL: 'blue',
          POSTGRESQL: 'cyan',
          ELASTICSEARCH: 'green',
          KUDU: 'orange',
        }
        return <Tag color={colorMap[type]}>{type}</Tag>
      },
    },
    {
      title: '地址',
      key: 'address',
      width: 200,
      render: (_: any, record: DatasourceConfig) => `${record.host}:${record.port}`,
    },
    {
      title: '数据库',
      dataIndex: 'databaseName',
      key: 'databaseName',
      width: 150,
    },
    {
      title: '连接状态',
      key: 'connectionStatus',
      width: 150,
      render: (_: any, record: DatasourceConfig) => getConnectionStatusTag(record),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => getStatusTag(status),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (time: string) => time?.replace('T', ' ').slice(0, 19),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      fixed: 'right' as const,
      render: (_: any, record: DatasourceConfig) => (
        <Space size="small">
          <Tooltip title="测试连接">
            <Button
              type="link"
              size="small"
              icon={<ApiOutlined />}
              loading={testingIds.has(record.id)}
              onClick={() => handleTestConnection(record.id)}
            />
          </Tooltip>
          <Tooltip title="编辑">
            <Button
              type="link"
              size="small"
              icon={<EditOutlined />}
              onClick={() => navigate(`/datasources/${record.id}/edit`)}
            />
          </Tooltip>
          <Popconfirm
            title="确定要删除此数据源吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Tooltip title="删除">
              <Button type="link" size="small" danger icon={<DeleteOutlined />} />
            </Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <div className="page-header">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <div className="page-title">数据源管理</div>
            <div className="page-subtitle">管理所有数据源配置</div>
          </div>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/datasources/create')}>
            新建数据源
          </Button>
        </div>
      </div>

      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Input
            placeholder="搜索名称"
            prefix={<SearchOutlined />}
            value={searchParams.name}
            onChange={e => setSearchParams({ ...searchParams, name: e.target.value })}
            onPressEnter={handleSearch}
            style={{ width: 200 }}
          />
          <Select
            placeholder="选择类型"
            allowClear
            value={searchParams.type}
            onChange={type => setSearchParams({ ...searchParams, type })}
            style={{ width: 150 }}
            options={[
              { label: 'MySQL', value: 'MYSQL' },
              { label: 'PostgreSQL', value: 'POSTGRESQL' },
              { label: 'Elasticsearch', value: 'ELASTICSEARCH' },
              { label: 'Kudu', value: 'KUDU' },
            ]}
          />
          <Select
            placeholder="选择状态"
            allowClear
            value={searchParams.status}
            onChange={status => setSearchParams({ ...searchParams, status })}
            style={{ width: 120 }}
            options={[
              { label: '活跃', value: 'ACTIVE' },
              { label: '停用', value: 'INACTIVE' },
              { label: '错误', value: 'ERROR' },
            ]}
          />
          <Button icon={<SearchOutlined />} onClick={handleSearch}>
            搜索
          </Button>
          <Button onClick={handleReset}>重置</Button>
        </Space>

        <Table
          columns={columns}
          dataSource={datasources}
          rowKey="id"
          loading={loading}
          scroll={{ x: 1300 }}
          pagination={{
            current: page + 1,
            pageSize,
            total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (p, ps) => {
              setPage(p - 1)
              setPageSize(ps)
            },
          }}
        />
      </Card>
    </div>
  )
}

export default DatasourceList
