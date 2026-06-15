import React, { useEffect, useState } from 'react'
import { Card, Form, Input, Select, InputNumber, Button, Space, message, Spin, Divider, Steps } from 'antd'
import { SaveOutlined, ArrowLeftOutlined, ApiOutlined } from '@ant-design/icons'
import { useNavigate, useParams } from 'react-router-dom'
import { datasourceApi, connectionTestApi } from '../services/datasourceApi'
import type { DatasourceConfig, DatasourceType, ConfigField } from '../types'

const DatasourceForm: React.FC = () => {
  const navigate = useNavigate()
  const { id } = useParams()
  const isEdit = !!id
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [testing, setTesting] = useState(false)
  const [currentStep, setCurrentStep] = useState(0)
  const [selectedType, setSelectedType] = useState<DatasourceType | null>(null)
  const [configFields, setConfigFields] = useState<ConfigField[]>([])

  useEffect(() => {
    if (isEdit) {
      fetchDatasource()
    }
  }, [id])

  useEffect(() => {
    if (selectedType) {
      fetchConfigSchema(selectedType)
    }
  }, [selectedType])

  const fetchDatasource = async () => {
    setLoading(true)
    try {
      const res: any = await datasourceApi.get(Number(id))
      const data = res.data
      form.setFieldsValue(data)
      setSelectedType(data.type)
    } catch (error) {
      message.error('获取数据源信息失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchConfigSchema = async (type: DatasourceType) => {
    try {
      const res: any = await datasourceApi.getConfigSchema(type)
      setConfigFields(res.data.fields || [])
      
      const defaults = res.data.defaults || {}
      if (!isEdit) {
        Object.entries(defaults).forEach(([key, value]) => {
          if (!form.getFieldValue(key)) {
            form.setFieldValue(key, value)
          }
        })
      }
    } catch (error) {
      message.error('获取配置模板失败')
    }
  }

  const handleTypeChange = (type: DatasourceType) => {
    setSelectedType(type)
    form.setFieldsValue({
      type,
      port: undefined,
      databaseName: undefined,
      username: undefined,
      password: undefined,
    })
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      setSubmitting(true)
      
      if (isEdit) {
        await datasourceApi.update(Number(id), values)
        message.success('更新成功')
      } else {
        await datasourceApi.create(values)
        message.success('创建成功')
      }
      
      navigate('/datasources')
    } catch (error: any) {
      if (error.errorFields) {
        message.error('请检查表单填写是否正确')
      } else {
        message.error(error.message || '操作失败')
      }
    } finally {
      setSubmitting(false)
    }
  }

  const handleTestConnection = async () => {
    try {
      const values = await form.validateFields()
      setTesting(true)
      
      const tempConfig = {
        ...values,
        id: id ? Number(id) : 0,
        name: values.name || 'temp',
      }
      
      const res: any = await connectionTestApi.test(tempConfig.id || 0)
      
      if (res.data.result === 'SUCCESS') {
        message.success(`连接成功，响应时间: ${res.data.responseTime}ms`)
      } else {
        message.error(`连接失败: ${res.data.errorMessage}`)
      }
    } catch (error: any) {
      if (!error.errorFields) {
        message.error('连接测试失败')
      }
    } finally {
      setTesting(false)
    }
  }

  const renderFormField = (field: ConfigField) => {
    switch (field.type) {
      case 'text':
        return (
          <Input
            placeholder={field.placeholder}
            maxLength={field.maxLength}
          />
        )
      case 'password':
        return (
          <Input.Password
            placeholder={field.placeholder}
            maxLength={field.maxLength}
          />
        )
      case 'number':
        return (
          <InputNumber
            placeholder={field.placeholder}
            min={field.minValue}
            max={field.maxValue}
            style={{ width: '100%' }}
          />
        )
      case 'select':
        return (
          <Select
            placeholder={`请选择${field.label}`}
            options={field.options?.map(opt => ({ label: opt, value: opt }))}
          />
        )
      case 'boolean':
        return (
          <Select
            options={[
              { label: '是', value: true },
              { label: '否', value: false },
            ]}
          />
        )
      default:
        return <Input placeholder={field.placeholder} />
    }
  }

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
        <Spin size="large" />
      </div>
    )
  }

  return (
    <div>
      <div className="page-header">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <div className="page-title">{isEdit ? '编辑数据源' : '新建数据源'}</div>
            <div className="page-subtitle">
              {isEdit ? '修改数据源配置信息' : '创建新的数据源配置'}
            </div>
          </div>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/datasources')}>
            返回列表
          </Button>
        </div>
      </div>

      <Card>
        <Steps
          current={currentStep}
          onChange={setCurrentStep}
          items={[
            { title: '基本信息', description: '选择数据源类型' },
            { title: '连接配置', description: '填写连接参数' },
            { title: '完成', description: '保存配置' },
          ]}
          style={{ marginBottom: 24 }}
        />

        <Form
          form={form}
          layout="vertical"
          initialValues={{ status: 'ACTIVE' }}
        >
          {currentStep === 0 && (
            <>
              <Form.Item
                name="name"
                label="数据源名称"
                rules={[{ required: true, message: '请输入数据源名称' }]}
              >
                <Input placeholder="请输入数据源名称" maxLength={128} />
              </Form.Item>

              <Form.Item
                name="type"
                label="数据源类型"
                rules={[{ required: true, message: '请选择数据源类型' }]}
              >
                <Select
                  placeholder="请选择数据源类型"
                  onChange={handleTypeChange}
                  disabled={isEdit}
                  options={[
                    { label: 'MySQL', value: 'MYSQL' },
                    { label: 'PostgreSQL', value: 'POSTGRESQL' },
                    { label: 'Elasticsearch', value: 'ELASTICSEARCH' },
                    { label: 'Kudu', value: 'KUDU' },
                  ]}
                />
              </Form.Item>

              <Form.Item name="version" label="版本号">
                <Input placeholder="自动检测或手动输入版本号" />
              </Form.Item>
            </>
          )}

          {currentStep === 1 && selectedType && (
            <>
              <Divider>基本连接信息</Divider>
              
              <Form.Item
                name="host"
                label="主机地址"
                rules={[{ required: true, message: '请输入主机地址' }]}
              >
                <Input placeholder="例如: localhost 或 192.168.1.100" />
              </Form.Item>

              <Form.Item
                name="port"
                label="端口"
                rules={[{ required: true, message: '请输入端口' }]}
              >
                <InputNumber min={1} max={65535} style={{ width: '100%' }} />
              </Form.Item>

              <Form.Item name="databaseName" label="数据库名">
                <Input placeholder="请输入数据库名" />
              </Form.Item>

              <Form.Item name="username" label="用户名">
                <Input placeholder="请输入用户名" />
              </Form.Item>

              <Form.Item name="password" label="密码">
                <Input.Password placeholder="请输入密码" />
              </Form.Item>

              <Divider>高级配置</Divider>

              {configFields
                .filter(f => !['host', 'port', 'databaseName', 'username', 'password'].includes(f.name))
                .map(field => (
                  <Form.Item
                    key={field.name}
                    name={['extraConfig', field.name]}
                    label={field.label}
                    rules={[{ required: field.required, message: `请输入${field.label}` }]}
                    extra={field.description}
                  >
                    {renderFormField(field)}
                  </Form.Item>
                ))}
            </>
          )}

          {currentStep === 2 && (
            <div style={{ textAlign: 'center', padding: '40px 0' }}>
              <p style={{ fontSize: 16, color: '#666' }}>
                配置已完成，请点击保存按钮提交
              </p>
              <Space>
                <Button
                  icon={<ApiOutlined />}
                  onClick={handleTestConnection}
                  loading={testing}
                >
                  测试连接
                </Button>
                <Button
                  type="primary"
                  icon={<SaveOutlined />}
                  onClick={handleSubmit}
                  loading={submitting}
                >
                  保存配置
                </Button>
              </Space>
            </div>
          )}

          {currentStep < 2 && (
            <Form.Item style={{ marginTop: 24 }}>
              <Space>
                {currentStep > 0 && (
                  <Button onClick={() => setCurrentStep(currentStep - 1)}>
                    上一步
                  </Button>
                )}
                <Button
                  type="primary"
                  onClick={() => setCurrentStep(currentStep + 1)}
                >
                  下一步
                </Button>
              </Space>
            </Form.Item>
          )}
        </Form>
      </Card>
    </div>
  )
}

export default DatasourceForm
