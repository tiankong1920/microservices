import { create } from 'zustand'

interface UIStore {
  sidebarCollapsed: boolean
  theme: 'light' | 'dark'
  selectedLocale: string
  toggleSidebar: () => void
  setSidebarCollapsed: (collapsed: boolean) => void
  setTheme: (theme: 'light' | 'dark') => void
  setSelectedLocale: (locale: string) => void
}

export const useUIStore = create<UIStore>((set) => ({
  sidebarCollapsed: false,
  theme: 'light',
  selectedLocale: 'zh-CN',
  toggleSidebar: () => set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed })),
  setSidebarCollapsed: (sidebarCollapsed) => set({ sidebarCollapsed }),
  setTheme: (theme) => set({ theme }),
  setSelectedLocale: (selectedLocale) => set({ selectedLocale }),
}))
