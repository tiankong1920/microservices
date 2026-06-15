import api from './api'
import type { DatasourceConfig, DatasourceType, DatasourceStatus } from '../types'

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: string
}

export const datasourceApi = {
  list: (page: number = 0, size: number = 10, sortBy: string = 'createdAt', sortDir: string = 'desc') =>
    api.get<ApiResponse<PageResponse<DatasourceConfig>>>(`/datasources?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`),

  search: (params: {
    name?: string
    type?: DatasourceType
    status?: DatasourceStatus
    page?: number
    size?: number
  }) => api.get<ApiResponse<PageResponse<DatasourceConfig>>>('/datasources/search', { params }),

  get: (id: number) =>
    api.get<ApiResponse<DatasourceConfig>>(`/datasources/${id}`),

  create: (data: Partial<DatasourceConfig>) =>
    api.post<ApiResponse<DatasourceConfig>>('/datasources', data),

  update: (id: number, data: Partial<DatasourceConfig>) =>
    api.put<ApiResponse<DatasourceConfig>>(`/datasources/${id}`, data),

  delete: (id: number) =>
    api.delete<ApiResponse<void>>(`/datasources/${id}`),

  getByType: (type: DatasourceType) =>
    api.get<ApiResponse<DatasourceConfig[]>>(`/datasources/type/${type}`),

  getConfigSchema: (type: DatasourceType) =>
    api.get<ApiResponse<any>>(`/datasources/config-schema/${type}`),
}

export const connectionTestApi = {
  test: (datasourceId: number) =>
    api.post<ApiResponse<any>>(`/connection-test/${datasourceId}`),

  batchTest: (datasourceIds: number[]) =>
    api.post<ApiResponse<any[]>>('/connection-test/batch', datasourceIds),

  getHistory: (datasourceId: number, params: {
    page?: number
    size?: number
    result?: string
    testType?: string
    startTime?: string
    endTime?: string
  }) => api.get<ApiResponse<PageResponse<any>>>(`/connection-test/${datasourceId}/history`, { params }),

  getStatistics: (datasourceId: number, since?: string) =>
    api.get<ApiResponse<any>>(`/connection-test/${datasourceId}/statistics`, { params: { since } }),
}

export const dashboardApi = {
  getStats: () =>
    api.get<ApiResponse<any>>('/dashboard/stats'),

  getHealthOverview: () =>
    api.get<ApiResponse<any[]>>('/dashboard/health-overview'),
}
