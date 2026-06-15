import api from './api'

export interface LoginRequest {
  username: string
  password: string
  mfaCode?: number
  mfaSessionId?: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  confirmPassword: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  username: string
  roles: string[]
  mfaRequired?: boolean
  mfaSessionId?: string
}

export interface UserInfo {
  username: string
  email: string
  enabled: boolean
  mfaEnabled: boolean
  roles: string[]
  permissions: string[]
}

export const authApi = {
  login: (data: LoginRequest) => api.post<{ data: LoginResponse }>('/auth/login', data),

  register: (data: RegisterRequest) =>
    api.post<{ message: string }>('/auth/register', data),

  logout: () => api.post('/auth/logout'),

  logoutAll: () => api.post('/auth/logout-all'),

  refreshToken: (refreshToken: string) =>
    api.post<{ data: { accessToken: string } }>('/auth/refresh', { refreshToken }),

  getCurrentUser: () => api.get<{ data: UserInfo }>('/auth/me'),

  getPermissions: () => api.get<{ data: string[] }>('/auth/permissions'),

  getRoles: () => api.get<{ data: string[] }>('/auth/roles'),

  changePassword: (data: {
    currentPassword: string
    newPassword: string
    confirmPassword: string
  }) => api.post('/auth/change-password', data),

  forgotPassword: (email: string) =>
    api.post('/auth/forgot-password', { email }),
}
