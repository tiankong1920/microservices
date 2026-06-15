/* global process */
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    target: 'esnext',
    minify: 'esbuild',
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks(id) {
          // 拆分 React 核心
          if (id.includes('node_modules/react') || id.includes('node_modules/react-dom') || id.includes('node_modules/react-router-dom')) {
            return 'vendor-react';
          }
          // 拆分MUI相关库
          if (id.includes('node_modules/@mui') || id.includes('node_modules/@emotion')) {
            return 'vendor-mui';
          }
          // 拆分ECharts - 动态导入的图表库
          if (id.includes('node_modules/echarts')) {
            return 'vendor-echarts';
          }
          // 拆分图表库 recharts
          if (id.includes('node_modules/recharts') || id.includes('node_modules/d3-') || id.includes('node_modules/reselect')) {
            return 'vendor-charts';
          }
          // 拆分日期处理
          if (id.includes('node_modules/dayjs') || id.includes('node_modules/@mui/x-date-pickers')) {
            return 'vendor-date';
          }
        },
      },
    },
    chunkSizeWarningLimit: 1000,
    reportCompressedSize: true,
  },
  esbuild: {
    drop: process.env.NODE_ENV === 'production' ? ['console', 'debugger'] : [],
  },
  optimizeDeps: {
    include: ['react', 'react-dom', 'react-router-dom'],
  },
})
