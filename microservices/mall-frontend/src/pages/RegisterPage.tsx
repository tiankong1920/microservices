import { useState } from 'react'
import { Form, Input, Button, Card, message, Progress } from 'antd'
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../services/authApi'

interface RegisterFormValues {
  username: string
  email: string
  password: string
  confirmPassword: string
}

export default function RegisterPage() {
  const [loading, setLoading] = useState(false)
  const [passwordStrength, setPasswordStrength] = useState(0)
  const navigate = useNavigate()

  const getPasswordStrength = (password: string): number => {
    let strength = 0
    if (password.length >= 12) strength += 25
    if (/[a-z]/.test(password)) strength += 25
    if (/[A-Z]/.test(password)) strength += 25
    if (/[0-9]/.test(password) || /[^a-zA-Z0-9]/.test(password)) strength += 25
    return strength
  }

  const getPasswordColor = (strength: number): string => {
    if (strength <= 25) return '#ff4d4f'
    if (strength <= 50) return '#faad14'
    if (strength <= 75) return '#52c41a'
    return '#73d13d'
  }

  const onFinish = async (values: RegisterFormValues) => {
    if (values.password !== values.confirmPassword) {
      message.error('Passwords do not match')
      return
    }

    setLoading(true)
    try {
      await authApi.register({
        username: values.username,
        email: values.email,
        password: values.password,
        confirmPassword: values.confirmPassword,
      })
      message.success('Registration successful! Please login.')
      navigate('/login')
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } }
      message.error(err.response?.data?.message || 'Registration failed')
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
      <Card title="用户注册" style={{ width: 400 }}>
        <Form
          name="register"
          onFinish={onFinish}
          autoComplete="off"
          layout="vertical"
        >
          <Form.Item
            name="username"
            rules={[
              { required: true, message: 'Please input username' },
              { min: 3, message: 'Username must be at least 3 characters' }
            ]}
          >
            <Input prefix={<UserOutlined />} placeholder="Username" />
          </Form.Item>

          <Form.Item
            name="email"
            rules={[
              { required: true, message: 'Please input email' },
              { type: 'email', message: 'Please input a valid email' }
            ]}
          >
            <Input prefix={<MailOutlined />} placeholder="Email" />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[
              { required: true, message: 'Please input password' },
              { min: 8, message: 'Password must be at least 8 characters' }
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="Password"
              onChange={(e) => setPasswordStrength(getPasswordStrength(e.target.value))}
            />
          </Form.Item>

          {passwordStrength > 0 && (
            <div style={{ marginBottom: 16 }}>
              <Progress
                percent={passwordStrength}
                showInfo={false}
                strokeColor={getPasswordColor(passwordStrength)}
                size="small"
              />
              <div style={{ fontSize: 12, color: getPasswordColor(passwordStrength) }}>
                {passwordStrength <= 25 && 'Weak'}
                {passwordStrength > 25 && passwordStrength <= 50 && 'Fair'}
                {passwordStrength > 50 && passwordStrength <= 75 && 'Good'}
                {passwordStrength > 75 && 'Strong'}
              </div>
            </div>
          )}

          <Form.Item
            name="confirmPassword"
            rules={[
              { required: true, message: 'Please confirm password' }
            ]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="Confirm Password" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              Register
            </Button>
          </Form.Item>

          <div style={{ textAlign: 'center' }}>
            <a onClick={() => navigate('/login')}>Already have an account? Login</a>
          </div>
        </Form>
      </Card>
    </div>
  )
}
