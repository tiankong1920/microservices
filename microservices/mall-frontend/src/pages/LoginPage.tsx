import { useState } from 'react'
import { Form, Input, Button, Card, message, Checkbox } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { useNavigate, useLocation } from 'react-router-dom'
import { authApi } from '../services/authApi'
import { useUserStore } from '../store'

interface LoginFormValues {
  username: string
  password: string
  remember?: boolean
}

export default function LoginPage() {
  const [loading, setLoading] = useState(false)
  const [mfaRequired, setMfaRequired] = useState(false)
  const [mfaSessionId, setMfaSessionId] = useState('')
  const navigate = useNavigate()
  const location = useLocation()
  const { login } = useUserStore()

  const from = (location.state as { from?: Location })?.from?.pathname || '/'

  const onFinish = async (values: LoginFormValues) => {
    setLoading(true)
    try {
      const response = await authApi.login({
        username: values.username,
        password: values.password,
      })

      const data = response.data.data

      if (data.mfaRequired) {
        setMfaRequired(true)
        setMfaSessionId(data.mfaSessionId || '')
        message.info('Please enter your MFA code')
        return
      }

      login(
        {
          id: 0,
          username: data.username,
          email: '',
          roles: data.roles,
        },
        data.accessToken
      )

      message.success('Login successful')
      navigate(from, { replace: true })
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } }
      message.error(err.response?.data?.message || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  const onMfaFinish = async (values: { mfaCode: string }) => {
    setLoading(true)
    try {
      const response = await authApi.login({
        username: '',
        password: '',
        mfaCode: parseInt(values.mfaCode),
        mfaSessionId: mfaSessionId,
      })

      const data = response.data.data

      login(
        {
          id: 0,
          username: data.username,
          email: '',
          roles: data.roles,
        },
        data.accessToken
      )

      message.success('Login successful')
      navigate(from, { replace: true })
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } }
      message.error(err.response?.data?.message || 'MFA verification failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '80vh'
    }}>
      <Card title="用户登录" style={{ width: 400 }}>
        {!mfaRequired ? (
          <Form
            name="login"
            onFinish={onFinish}
            autoComplete="off"
            layout="vertical"
          >
            <Form.Item
              name="username"
              rules={[{ required: true, message: 'Please input your username' }]}
            >
              <Input prefix={<UserOutlined />} placeholder="Username" />
            </Form.Item>

            <Form.Item
              name="password"
              rules={[{ required: true, message: 'Please input your password' }]}
            >
              <Input.Password prefix={<LockOutlined />} placeholder="Password" />
            </Form.Item>

            <Form.Item name="remember" valuePropName="checked">
              <Checkbox>Remember me</Checkbox>
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading} block>
                Log in
              </Button>
            </Form.Item>

            <div style={{ textAlign: 'center' }}>
              <a onClick={() => navigate('/register')}>Don't have an account? Register</a>
            </div>
          </Form>
        ) : (
          <Form
            name="mfa"
            onFinish={onMfaFinish}
            autoComplete="off"
            layout="vertical"
          >
            <Form.Item
              name="mfaCode"
              rules={[
                { required: true, message: 'Please input MFA code' },
                { len: 6, message: 'MFA code must be 6 digits' }
              ]}
            >
              <Input.OTP length={6} placeholder="Enter 6-digit code" />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading} block>
                Verify
              </Button>
            </Form.Item>

            <div style={{ textAlign: 'center' }}>
              <a onClick={() => setMfaRequired(false)}>Back to login</a>
            </div>
          </Form>
        )}
      </Card>
    </div>
  )
}
