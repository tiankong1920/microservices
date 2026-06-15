import { useEffect } from 'react'
import { useUserStore } from './userStore'
import api from '../services/api'

export function useAuth() {
  const { user, token, isAuthenticated, login, logout } = useUserStore()

  useEffect(() => {
    if (token) {
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`
    } else {
      delete api.defaults.headers.common['Authorization']
    }
  }, [token])

  return { user, isAuthenticated, login, logout }
}

export function usePermission() {
  const { user } = useUserStore()

  const hasPermission = (permission: string): boolean => {
    return user?.roles.includes(permission) ?? false
  }

  const hasAnyPermission = (permissions: string[]): boolean => {
    return permissions.some((p) => user?.roles.includes(p))
  }

  const hasAllPermissions = (permissions: string[]): boolean => {
    return permissions.every((p) => user?.roles.includes(p))
  }

  return { hasPermission, hasAnyPermission, hasAllPermissions }
}
