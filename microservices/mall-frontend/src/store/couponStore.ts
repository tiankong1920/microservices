import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface Coupon {
  id: number
  name: string
  type: 'DISCOUNT' | 'CASH' | 'FREE_SHIPPING'
  value: number
  minAmount: number
  maxDiscount?: number
  validFrom: string
  validUntil: string
  status: 'UNUSED' | 'USED' | 'EXPIRED'
}

interface CouponStore {
  userCoupons: Coupon[]
  availableCoupons: Coupon[]
  selectedCouponId: number | null
  setUserCoupons: (coupons: Coupon[]) => void
  setAvailableCoupons: (coupons: Coupon[]) => void
  selectCoupon: (couponId: number | null) => void
  useCoupon: (couponId: number) => void
}

export const useCouponStore = create<CouponStore>()(
  persist(
    (set) => ({
      userCoupons: [],
      availableCoupons: [],
      selectedCouponId: null,
      setUserCoupons: (userCoupons) => set({ userCoupons }),
      setAvailableCoupons: (availableCoupons) => set({ availableCoupons }),
      selectCoupon: (selectedCouponId) => set({ selectedCouponId }),
      useCoupon: (couponId) =>
        set((state) => ({
          userCoupons: state.userCoupons.map((c) =>
            c.id === couponId ? { ...c, status: 'USED' as const } : c
          ),
          selectedCouponId: state.selectedCouponId === couponId ? null : state.selectedCouponId,
        })),
    }),
    { name: 'coupon-storage', partialize: (state) => ({ selectedCouponId: state.selectedCouponId }) }
  )
)