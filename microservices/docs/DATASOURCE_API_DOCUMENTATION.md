# 数据源管理系统 API 文档

## 概述

数据源管理系统提供RESTful API接口，用于管理多种数据存储类型的配置和连接。

### 基础信息

- **Base URL**: `http://localhost:8090/api/v1`
- **Content-Type**: `application/json`
- **认证方式**: Header认证

### 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| X-Tenant-Id | 是 | 租户ID，默认为`default` |
| X-User-Id | 是 | 用户ID |
| X-Username | 否 | 用户名 |

### 响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2024-01-01T12:00:00"
}
```

---

## 数据源配置管理

### 1. 创建数据源

**POST** `/datasources`

创建新的数据源配置。

**请求体**:
```json
{
  "name": "MySQL生产库",
  "type": "MYSQL",
  "version": "8.0",
  "host": "192.168.1.100",
  "port": 3306,
  "databaseName": "production",
  "username": "root",
  "password": "encrypted_password",
  "extraConfig": "{\"charset\": \"utf8mb4\", \"timezone\": \"Asia/Shanghai\"}"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "数据源创建成功",
  "data": {
    "id": 1,
    "name": "MySQL生产库",
    "type": "MYSQL",
    "status": "ACTIVE",
    "createdAt": "2024-01-01T12:00:00"
  }
}
```

### 2. 更新数据源

**PUT** `/datasources/{id}`

更新指定数据源的配置。

**路径参数**:
- `id`: 数据源ID

**请求体**: 同创建数据源

### 3. 删除数据源

**DELETE** `/datasources/{id}`

软删除指定数据源。

**路径参数**:
- `id`: 数据源ID

### 4. 获取数据源详情

**GET** `/datasources/{id}`

获取指定数据源的详细信息。

**路径参数**:
- `id`: 数据源ID

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "MySQL生产库",
    "type": "MYSQL",
    "version": "8.0",
    "host": "192.168.1.100",
    "port": 3306,
    "databaseName": "production",
    "username": "root",
    "status": "ACTIVE",
    "connectionStatus": {
      "status": "CONNECTED",
      "responseTime": 15,
      "checkedAt": "2024-01-01T12:00:00"
    },
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T12:00:00"
  }
}
```

### 5. 获取数据源列表

**GET** `/datasources`

分页获取数据源列表。

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认0 |
| size | int | 否 | 每页大小，默认10 |
| sortBy | string | 否 | 排序字段，默认createdAt |
| sortDir | string | 否 | 排序方向，默认desc |

### 6. 搜索数据源

**GET** `/datasources/search`

根据条件搜索数据源。

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 否 | 名称模糊搜索 |
| type | string | 否 | 数据源类型 |
| status | string | 否 | 状态 |
| page | int | 否 | 页码 |
| size | int | 否 | 每页大小 |

### 7. 获取配置模板

**GET** `/datasources/config-schema/{type}`

获取指定类型数据源的配置字段定义。

**路径参数**:
- `type`: 数据源类型 (MYSQL/POSTGRESQL/ELASTICSEARCH/KUDU)

**响应**:
```json
{
  "code": 200,
  "data": {
    "type": "MYSQL",
    "fields": [
      {
        "name": "host",
        "label": "主机地址",
        "type": "text",
        "required": true,
        "description": "MySQL服务器的主机地址"
      },
      {
        "name": "port",
        "label": "端口",
        "type": "number",
        "defaultValue": "3306",
        "required": true,
        "minValue": 1,
        "maxValue": 65535
      }
    ],
    "defaults": {
      "port": 3306,
      "charset": "utf8mb4"
    }
  }
}
```

---

## 连接测试

### 1. 测试单个连接

**POST** `/connection-test/{datasourceId}`

测试指定数据源的连接状态。

**路径参数**:
- `datasourceId`: 数据源ID

**响应**:
```json
{
  "code": 200,
  "data": {
    "datasourceId": 1,
    "datasourceName": "MySQL生产库",
    "result": "SUCCESS",
    "responseTime": 15,
    "testedAt": "2024-01-01T12:00:00"
  }
}
```

**失败响应**:
```json
{
  "code": 200,
  "data": {
    "datasourceId": 1,
    "datasourceName": "MySQL生产库",
    "result": "FAILURE",
    "errorCode": "CONNECTION_REFUSED",
    "errorMessage": "Connection refused",
    "suggestions": [
      "请检查MySQL服务是否正在运行",
      "请检查主机地址和端口是否正确",
      "请检查防火墙是否允许该端口访问"
    ],
    "testedAt": "2024-01-01T12:00:00"
  }
}
```

### 2. 批量测试连接

**POST** `/connection-test/batch`

批量测试多个数据源的连接状态。

**请求体**:
```json
[1, 2, 3, 4, 5]
```

**响应**: 返回每个数据源的测试结果数组

### 3. 获取测试历史

**GET** `/connection-test/{datasourceId}/history`

获取指定数据源的连接测试历史记录。

**查询参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| size | int | 每页大小 |
| result | string | 筛选结果 (SUCCESS/FAILURE/TIMEOUT) |
| testType | string | 筛选类型 (AUTO/MANUAL/BATCH/SCHEDULED) |
| startTime | datetime | 开始时间 |
| endTime | datetime | 结束时间 |

### 4. 获取测试统计

**GET** `/connection-test/{datasourceId}/statistics`

获取指定数据源的连接测试统计数据。

**查询参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| since | datetime | 统计起始时间 |

**响应**:
```json
{
  "code": 200,
  "data": {
    "totalTests": 100,
    "successTests": 95,
    "failedTests": 5,
    "timeoutTests": 0,
    "successRate": 0.95,
    "averageResponseTime": 18.5
  }
}
```

---

## 仪表盘

### 1. 获取统计数据

**GET** `/dashboard/stats`

获取仪表盘统计数据。

**响应**:
```json
{
  "code": 200,
  "data": {
    "totalDatasources": 10,
    "activeDatasources": 10,
    "connectedDatasources": 8,
    "disconnectedDatasources": 1,
    "errorDatasources": 1,
    "averageResponseTime": 25.5,
    "connectionSuccessRate": 0.95,
    "totalTestCount": 1000,
    "successTestCount": 950,
    "failedTestCount": 50,
    "datasourceTypeDistribution": {
      "MYSQL": 4,
      "POSTGRESQL": 3,
      "ELASTICSEARCH": 2,
      "KUDU": 1
    },
    "connectionStatusDistribution": {
      "CONNECTED": 8,
      "DISCONNECTED": 1,
      "ERROR": 1
    },
    "lastUpdated": "2024-01-01T12:00:00"
  }
}
```

### 2. 获取健康概览

**GET** `/dashboard/health-overview`

获取所有数据源的健康状态概览。

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "MySQL生产库",
      "type": "MYSQL",
      "host": "192.168.1.100",
      "port": 3306,
      "status": "CONNECTED",
      "responseTime": 15,
      "lastChecked": "2024-01-01T12:00:00"
    }
  ]
}
```

---

## 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 数据源类型

| 类型 | 说明 | 支持版本 |
|------|------|----------|
| MYSQL | MySQL数据库 | 5.7+, 8.0+ |
| POSTGRESQL | PostgreSQL数据库 | 18+ |
| ELASTICSEARCH | Elasticsearch搜索引擎 | 6.x, 7.x, 8.x |
| KUDU | Apache Kudu列式存储 | 1.10+ |

---

## 数据源状态

| 状态 | 说明 |
|------|------|
| ACTIVE | 活跃状态 |
| INACTIVE | 停用状态 |
| DELETED | 已删除 |
| ERROR | 错误状态 |

---

## 连接状态

| 状态 | 说明 |
|------|------|
| CONNECTED | 已连接 |
| DISCONNECTED | 断开连接 |
| ERROR | 连接错误 |
| TESTING | 测试中 |
