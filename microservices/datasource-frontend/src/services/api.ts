import axios from 'axios'

const api = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use(
  (config) => {
    const tenantId = localStorage.getItem('tenantId') || 'default'
    const userId = localStorage.getItem('userId') || 'system'
    const username = localStorage.getItem('username') || 'system'
    
    config.headers['X-Tenant-Id'] = tenantId
    config.headers['X-User-Id'] = userId
    config.headers['X-Username'] = username
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        console.error('未授权访问')
      } else if (status === 403) {
        console.error('权限不足')
      } else if (status === 500) {
        console.error('服务器错误:', data.message)
      }
      return Promise.reject(data)
    }
    return Promise.reject(error)
  }
)

export default api
