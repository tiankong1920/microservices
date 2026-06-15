import api from './api'

export const productApi = {
  getCategories: () => api.get('/categories/tree'),
  getCategoryById: (id: number) => api.get(`/categories/${id}`),
  getProducts: (params?: Record<string, unknown>) => api.get('/products', { params }),
  getProductById: (id: number) => api.get(`/products/${id}`),
}

export const cartApi = {
  getCart: (userId: number) => api.get(`/cart/user/${userId}`),
  addToCart: (data: { userId: number; skuId: number; quantity: number }) =>
    api.post('/cart/add', data),
  updateQuantity: (id: number, quantity: number) =>
    api.put(`/cart/${id}/quantity`, { quantity }),
  removeFromCart: (id: number) => api.delete(`/cart/${id}`),
  checkCartItem: (id: number, checked: boolean) =>
    api.put(`/cart/${id}/check`, { checked }),
}

export const orderApi = {
  createFromCart: (data: { userId: number; cartItemIds: number[]; couponId?: number; notes?: string }) =>
    api.post('/orders/from-cart', data),
  createDirect: (data: { userId: number; skuId: number; quantity: number; couponId?: number }) =>
    api.post('/orders/direct', data),
  getOrder: (id: number) => api.get(`/orders/${id}`),
  getUserOrders: (userId: number, page = 0, size = 20) =>
    api.get(`/orders/user/${userId}`, { params: { page, size } }),
  cancelOrder: (id: number, reason: string) =>
    api.put(`/orders/${id}/cancel`, { reason }),
  confirmReceipt: (id: number) => api.put(`/orders/${id}/confirm`),
}

export const couponApi = {
  getTemplates: (page = 0, size = 20) => api.get('/coupons/templates', { params: { page, size } }),
  getUserCoupons: (userId: number, status?: string) =>
    api.get(`/coupons/user/${userId}`, { params: { status } }),
  validateCoupon: (data: { couponId: number; userId: number; orderAmount: number }) =>
    api.post('/coupons/validate', data),
}

export const refundApi = {
  createRefund: (data: { orderId: number; userId: number; refundType: string; refundAmount: number; reason: string }) =>
    api.post('/refunds', data),
  getUserRefunds: (userId: number, page = 0, size = 20) =>
    api.get(`/refunds/user/${userId}`, { params: { page, size } }),
}

export const distributorApi = {
  register: (data: { userId: number; parentId?: number }) =>
    api.post('/distributors/register', data),
  getDistributor: (id: number) => api.get(`/distributors/${id}`),
  getSubDistributors: (id: number) => api.get(`/distributors/${id}/sub`),
  getChain: (userId: number) => api.get(`/distributors/chain/${userId}`),
}