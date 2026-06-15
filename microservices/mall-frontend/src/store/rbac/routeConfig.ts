import { Role } from './permissions'

export interface RouteConfig {
  path: string
  name: string
  icon?: string
  component?: React.ComponentType
  children?: RouteConfig[]
  meta?: RouteMeta
}

export interface RouteMeta {
  title?: string
  icon?: string
  requiresAuth?: boolean
  requiredRoles?: Role[]
  requiredPermissions?: string[]
  hidden?: boolean
  keepAlive?: boolean
}

export const ROUTE_CONFIG: RouteConfig[] = [
  {
    path: '/',
    name: 'Home',
    meta: { title: '首页', icon: 'Home', requiresAuth: false }
  },
  {
    path: '/products',
    name: 'Products',
    meta: { title: '商品列表', icon: 'Shop', requiresAuth: false }
  },
  {
    path: '/cart',
    name: 'Cart',
    meta: { title: '购物车', icon: 'ShoppingCart', requiresAuth: true, requiredRoles: ['USER', 'ADMIN', 'MANAGER'] }
  },
  {
    path: '/orders',
    name: 'Orders',
    meta: { title: '我的订单', icon: 'FileText', requiresAuth: true, requiredRoles: ['USER', 'ADMIN', 'MANAGER'] }
  },
  {
    path: '/coupons',
    name: 'Coupons',
    meta: { title: '优惠券', icon: 'Ticket', requiresAuth: true, requiredRoles: ['USER', 'ADMIN', 'MANAGER'] }
  },
  {
    path: '/flash-sale',
    name: 'FlashSale',
    meta: { title: '限时秒杀', icon: 'Flash', requiresAuth: false }
  },
  {
    path: '/group-buy',
    name: 'GroupBuy',
    meta: { title: '拼团', icon: 'Team', requiresAuth: false }
  },
  {
    path: '/bargain',
    name: 'Bargain',
    meta: { title: '砍价', icon: 'Cut', requiresAuth: false }
  },
  {
    path: '/admin',
    name: 'Admin',
    meta: { title: '管理后台', icon: 'Setting', requiresAuth: true, requiredRoles: ['ADMIN', 'MANAGER'] },
    children: [
      {
        path: '/admin/products',
        name: 'AdminProducts',
        meta: { title: '商品管理', requiredPermissions: ['MANAGE_PRODUCTS'] }
      },
      {
        path: '/admin/orders',
        name: 'AdminOrders',
        meta: { title: '订单管理', requiredPermissions: ['MANAGE_ORDERS'] }
      },
      {
        path: '/admin/customers',
        name: 'AdminCustomers',
        meta: { title: '客户管理', requiredPermissions: ['MANAGE_CUSTOMERS'] }
      },
      {
        path: '/admin/reports',
        name: 'AdminReports',
        meta: { title: '报表中心', requiredPermissions: ['VIEW_REPORTS'] }
      }
    ]
  }
]
