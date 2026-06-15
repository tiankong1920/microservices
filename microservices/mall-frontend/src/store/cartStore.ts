import { create } from 'zustand'

interface CartItem {
  id: number
  skuId: number
  productName: string
  price: number
  quantity: number
  image: string
  checked: boolean
}

interface CartStore {
  items: CartItem[]
  totalCount: number
  addItem: (item: CartItem) => void
  removeItem: (id: number) => void
  updateQuantity: (id: number, quantity: number) => void
  toggleCheck: (id: number) => void
  toggleCheckAll: (checked: boolean) => void
  clearCart: () => void
  getCheckedTotal: () => number
}

export const useCartStore = create<CartStore>((set, get) => ({
  items: [],
  totalCount: 0,
  addItem: (item) =>
    set((state) => {
      const existing = state.items.find((i) => i.skuId === item.skuId)
      if (existing) {
        return {
          items: state.items.map((i) =>
            i.skuId === item.skuId ? { ...i, quantity: i.quantity + item.quantity } : i
          ),
          totalCount: state.totalCount + item.quantity,
        }
      }
      return { items: [...state.items, item], totalCount: state.totalCount + item.quantity }
    }),
  removeItem: (id) =>
    set((state) => {
      const item = state.items.find((i) => i.id === id)
      return {
        items: state.items.filter((i) => i.id !== id),
        totalCount: state.totalCount - (item?.quantity || 0),
      }
    }),
  updateQuantity: (id, quantity) =>
    set((state) => {
      const oldItem = state.items.find((i) => i.id === id)
      const diff = quantity - (oldItem?.quantity || 0)
      return {
        items: state.items.map((i) => (i.id === id ? { ...i, quantity } : i)),
        totalCount: state.totalCount + diff,
      }
    }),
  toggleCheck: (id) =>
    set((state) => ({
      items: state.items.map((i) => (i.id === id ? { ...i, checked: !i.checked } : i)),
    })),
  toggleCheckAll: (checked) =>
    set((state) => ({ items: state.items.map((i) => ({ ...i, checked })) })),
  clearCart: () => set({ items: [], totalCount: 0 }),
  getCheckedTotal: () => {
    const state = get()
    return state.items
      .filter((i) => i.checked)
      .reduce((sum, i) => sum + i.price * i.quantity, 0)
  },
}))