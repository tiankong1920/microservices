import React from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useUserStore } from '../store'
import { hasPermission, hasAnyPermission, Role, Permission } from './permissions'
import type { RouteMeta } from './routeConfig'

interface AuthGuardProps {
  children: React.ReactNode
  requiredRoles?: Role[]
  requiredPermissions?: Permission[]
  fallbackPath?: string
}

export function AuthGuard({
  children,
  requiredRoles,
  requiredPermissions,
  fallbackPath = '/login'
}: AuthGuardProps) {
  const location = useLocation()
  const { user, isAuthenticated } = useUserStore()

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (requiredRoles && requiredRoles.length > 0) {
    const userRoles = (user?.roles || []) as Role[]
    const hasRequiredRole = requiredRoles.some(role => userRoles.includes(role))

    if (!hasRequiredRole) {
      return <Navigate to="/403" state={{ from: location }} replace />
    }
  }

  if (requiredPermissions && requiredPermissions.length > 0) {
    const userRoles = (user?.roles || []) as Role[]
    if (!hasAnyPermission(userRoles, requiredPermissions)) {
      return <Navigate to="/403" state={{ from: location }} replace />
    }
  }

  return <>{children}</>
}

export function useRouteGuard() {
  const { user, isAuthenticated } = useUserStore()

  const canAccess = (meta?: RouteMeta): boolean => {
    if (!meta) return true

    if (meta.requiresAuth && !isAuthenticated) {
      return false
    }

    if (meta.requiredRoles && meta.requiredRoles.length > 0) {
      const userRoles = (user?.roles || []) as Role[]
      if (!meta.requiredRoles.some(role => userRoles.includes(role))) {
        return false
      }
    }

    if (meta.requiredPermissions && meta.requiredPermissions.length > 0) {
      const userRoles = (user?.roles || []) as Role[]
      if (!hasAnyPermission(userRoles, meta.requiredPermissions as Permission[])) {
        return false
      }
    }

    return true
  }

  return { canAccess }
}
