import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface Order {
  id: number
  orderNumber: string
  status: 'PENDING' | 'PAID' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED' | 'REFUNDING' | 'REFUNDED'
  totalAmount: number
  itemCount: number
  createdAt: string
  updatedAt: string
  shippingAddress?: string
  paymentMethod?: string
}

interface OrderStore {
  orders: Order[]
  currentOrder: Order | null
  loading: boolean
  totalPages: number
  currentPage: number
  setOrders: (orders: Order[]) => void
  addOrder: (order: Order) => void
  updateOrder: (orderId: number, updates: Partial<Order>) => void
  setCurrentOrder: (order: Order | null) => void
  setLoading: (loading: boolean) => void
  setPagination: (totalPages: number, currentPage: number) => void
}

export const useOrderStore = create<OrderStore>()(
  persist(
    (set) => ({
      orders: [],
      currentOrder: null,
      loading: false,
      totalPages: 0,
      currentPage: 0,
      setOrders: (orders) => set({ orders }),
      addOrder: (order) => set((state) => ({ orders: [order, ...state.orders] })),
      updateOrder: (orderId, updates) =>
        set((state) => ({
          orders: state.orders.map((o) => (o.id === orderId ? { ...o, ...updates } : o)),
          currentOrder: state.currentOrder?.id === orderId ? { ...state.currentOrder, ...updates } : state.currentOrder,
        })),
      setCurrentOrder: (currentOrder) => set({ currentOrder }),
      setLoading: (loading) => set({ loading }),
      setPagination: (totalPages, currentPage) => set({ totalPages, currentPage }),
    }),
    { name: 'order-storage' }
  )
)