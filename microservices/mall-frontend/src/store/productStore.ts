import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface Product {
  id: number
  name: string
  description: string
  price: number
  originalPrice?: number
  images: string[]
  categoryId: number
  categoryName: string
  stock: number
  sales: number
  rating: number
  tags: string[]
}

interface ProductStore {
  products: Product[]
  currentProduct: Product | null
  loading: boolean
  searchKeyword: string
  categoryId: number | null
  setProducts: (products: Product[]) => void
  setCurrentProduct: (product: Product | null) => void
  setLoading: (loading: boolean) => void
  setSearchKeyword: (keyword: string) => void
  setCategoryId: (categoryId: number | null) => void
  clearFilters: () => void
}

export const useProductStore = create<ProductStore>()(
  persist(
    (set) => ({
      products: [],
      currentProduct: null,
      loading: false,
      searchKeyword: '',
      categoryId: null,
      setProducts: (products) => set({ products }),
      setCurrentProduct: (currentProduct) => set({ currentProduct }),
      setLoading: (loading) => set({ loading }),
      setSearchKeyword: (searchKeyword) => set({ searchKeyword }),
      setCategoryId: (categoryId) => set({ categoryId }),
      clearFilters: () => set({ searchKeyword: '', categoryId: null }),
    }),
    { name: 'product-storage', partialize: (state) => ({ searchKeyword: state.searchKeyword, categoryId: state.categoryId }) }
  )
)