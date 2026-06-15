import { Role, Permission } from './permissions'

declare global {
  interface Window {
    __USER_ROLES__?: Role[]
    __USER_PERMISSIONS__?: Permission[]
  }
}

const ROLES_KEY = 'user-roles'
const PERMISSIONS_KEY = 'user-permissions'

export function getStoredRoles(): Role[] {
  try {
    const stored = localStorage.getItem(ROLES_KEY)
    return stored ? JSON.parse(stored) : []
  } catch {
    return window.__USER_ROLES__ || []
  }
}

export function getStoredPermissions(): Permission[] {
  try {
    const stored = localStorage.getItem(PERMISSIONS_KEY)
    return stored ? JSON.parse(stored) : []
  } catch {
    return window.__USER_PERMISSIONS__ || []
  }
}

export function setStoredRoles(roles: Role[]): void {
  localStorage.setItem(ROLES_KEY, JSON.stringify(roles))
}

export function setStoredPermissions(permissions: Permission[]): void {
  localStorage.setItem(PERMISSIONS_KEY, JSON.stringify(permissions))
}

export function clearStoredAuth(): void {
  localStorage.removeItem(ROLES_KEY)
  localStorage.removeItem(PERMISSIONS_KEY)
}
