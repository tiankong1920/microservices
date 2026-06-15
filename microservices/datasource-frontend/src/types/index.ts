export type DatasourceType = 'MYSQL' | 'POSTGRESQL' | 'ELASTICSEARCH' | 'KUDU'
export type DatasourceStatus = 'ACTIVE' | 'INACTIVE' | 'DELETED' | 'ERROR'

export interface ConnectionStatus {
  status: string
  responseTime?: number
  errorMessage?: string
  checkedAt?: string
}

export interface DatasourceConfig {
  id: number
  name: string
  type: DatasourceType
  version?: string
  host: string
  port: number
  databaseName?: string
  username?: string
  password?: string
  extraConfig?: string
  status: DatasourceStatus
  templateId?: number
  createdBy?: string
  createdAt?: string
  updatedBy?: string
  updatedAt?: string
  connectionStatus?: ConnectionStatus
}

export interface ConfigTemplate {
  id: number
  name: string
  type: DatasourceType
  description?: string
  configJson: string
  isPublic?: boolean
  createdBy?: string
  createdAt?: string
  updatedAt?: string
}

export interface AlertConfig {
  id: number
  name: string
  datasourceIds?: number[]
  alertLevel: 'CRITICAL' | 'WARNING' | 'INFO'
  alertChannels: string[]
  receivers: {
    emails?: string[]
    phones?: string[]
    dingtalkWebhooks?: string[]
    wechatWebhooks?: string[]
  }
  notifyFrequency?: 'IMMEDIATE' | 'HOURLY' | 'DAILY'
  enabled?: boolean
  createdAt?: string
  updatedAt?: string
}

export interface AuditLog {
  id: number
  userId: string
  username?: string
  operation: string
  resourceType: string
  resourceId?: string
  oldValue?: string
  newValue?: string
  ipAddress?: string
  userAgent?: string
  createdAt: string
}

export interface DashboardStats {
  totalDatasources: number
  activeDatasources: number
  connectedDatasources: number
  disconnectedDatasources: number
  errorDatasources: number
  averageResponseTime: number
  connectionSuccessRate: number
  totalTestCount: number
  successTestCount: number
  failedTestCount: number
  datasourceTypeDistribution: Record<string, number>
  connectionStatusDistribution: Record<string, number>
  lastUpdated: string
}

export interface ConfigField {
  name: string
  label: string
  type: string
  defaultValue?: string
  required?: boolean
  description?: string
  placeholder?: string
  validationRegex?: string
  options?: string[]
  minLength?: number
  maxLength?: number
  minValue?: number
  maxValue?: number
}

export interface ConfigSchema {
  type: DatasourceType
  fields: ConfigField[]
  defaults: Record<string, any>
}
