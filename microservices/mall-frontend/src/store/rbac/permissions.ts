export type Role = 'ADMIN' | 'USER' | 'MANAGER' | 'GUEST'

export type Permission =
  | 'VIEW_PRODUCTS'
  | 'MANAGE_PRODUCTS'
  | 'VIEW_ORDERS'
  | 'MANAGE_ORDERS'
  | 'VIEW_CUSTOMERS'
  | 'MANAGE_CUSTOMERS'
  | 'VIEW_COUPONS'
  | 'MANAGE_COUPONS'
  | 'VIEW_REPORTS'
  | 'MANAGE_REPORTS'
  | 'ADMIN_SETTINGS'

export const ROLE_PERMISSIONS: Record<Role, Permission[]> = {
  ADMIN: [
    'VIEW_PRODUCTS', 'MANAGE_PRODUCTS',
    'VIEW_ORDERS', 'MANAGE_ORDERS',
    'VIEW_CUSTOMERS', 'MANAGE_CUSTOMERS',
    'VIEW_COUPONS', 'MANAGE_COUPONS',
    'VIEW_REPORTS', 'MANAGE_REPORTS',
    'ADMIN_SETTINGS'
  ],
  MANAGER: [
    'VIEW_PRODUCTS', 'MANAGE_PRODUCTS',
    'VIEW_ORDERS', 'MANAGE_ORDERS',
    'VIEW_CUSTOMERS',
    'VIEW_COUPONS', 'MANAGE_COUPONS',
    'VIEW_REPORTS'
  ],
  USER: [
    'VIEW_PRODUCTS',
    'VIEW_ORDERS', 'MANAGE_ORDERS',
    'VIEW_COUPONS'
  ],
  GUEST: [
    'VIEW_PRODUCTS'
  ]
}

export function hasPermission(userRoles: Role[], permission: Permission): boolean {
  return userRoles.some(role => ROLE_PERMISSIONS[role]?.includes(permission))
}

export function hasAnyPermission(userRoles: Role[], permissions: Permission[]): boolean {
  return permissions.some(p => hasPermission(userRoles, p))
}

export function hasAllPermissions(userRoles: Role[], permissions: Permission[]): boolean {
  return permissions.every(p => hasPermission(userRoles, p))
}
