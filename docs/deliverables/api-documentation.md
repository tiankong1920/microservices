# API接口文档

> **进销存管理系统** | 版本 3.0.0 | 最后更新: 2025-06-05

---

## 1. 概述

### 1.1 基础URL

| 环境 | URL |
|------|-----|
| 开发环境 | `http://localhost:{service-port}` |
| 生产环境 | `https://api.{service-name}.inventory-system.com` |

各服务端口：

| 服务名称 | 端口 | 基础路径 |
|----------|------|----------|
| API网关 | 8080 | `/api/v1/**` |
| 认证服务 (auth-service) | 8093 | `/api/v1/auth` |
| 产品服务 (product-service) | 8081 | `/api/v1/products` |
| 库存服务 (inventory-service) | 8083 | `/api/v1/inventory` |
| 订单服务 (order-service) | 8082 | `/api/v1/orders` |
| 采购服务 (procurement-service) | 8085 | `/api/v1/procurement` |
| 销售服务 (sales-service) | 8084 | `/api/v1/sales` |
| 客户服务 (customer-service) | 8086 | `/api/v1/customers` |
| 供应商服务 (supplier-service) | 8087 | `/api/v1/suppliers` |
| 财务服务 (finance-service) | 8090 | `/api/v1/finance` |
| 报表服务 (report-service) | 8092 | `/api/v1/reports` |
| 模板服务 (template-service) | 8088 | `/api/v1/templates` |
| 发票服务 (invoice-service) | 8091 | `/api/v1/invoice` |
| 数据源服务 (datasource-service) | 8089 | `/api/v1/datasources` |
| 管理服务 (admin-service) | 8094 | `/api/v1/auth` |

### 1.2 认证方式

系统采用 **JWT (JSON Web Token)** 认证方式，认证信息通过HTTP头传递：

```
Authorization: Bearer <access_token>
```

- **Access Token** 有效期：3600秒（1小时）
- **Refresh Token** 用于刷新Access Token

### 1.3 通用请求头

| 请求头 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `Authorization` | string | 是 | JWT认证令牌，格式: `Bearer <token>` |
| `Content-Type` | string | 是 | 请求体内容类型，通常为 `application/json` |
| `Accept` | string | 否 | 期望的响应格式，默认为 `application/json` |
| `X-Request-ID` | string | 否 | 请求追踪ID |

### 1.4 通用响应格式

**成功响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": "2025-06-05T10:00:00.000+00:00"
}
```

**错误响应：**
```json
{
  "code": 400,
  "message": "错误信息描述",
  "data": null,
  "timestamp": "2025-06-05T10:00:00.000+00:00"
}
```

### 1.5 通用错误码

| 错误码 | HTTP状态码 | 说明 |
|--------|-----------|------|
| INVALID_REQUEST | 400 | 请求参数无效 |
| RESOURCE_NOT_FOUND | 404 | 资源不存在 |
| UNAUTHORIZED | 401 | 未授权/Token无效 |
| FORBIDDEN | 403 | 禁止访问 |
| INVALID_CREDENTIALS | 401 | 用户名或密码错误 |
| ACCOUNT_LOCKED | 403 | 账户已锁定 |
| ACCOUNT_DISABLED | 403 | 账户已禁用 |
| INVALID_MFA_CODE | 400 | MFA验证码无效 |
| TOKEN_REVOKED | 401 | Token已撤销 |
| NOT_AUTHENTICATED | 401 | 未认证 |
| PASSWORD_POLICY_VIOLATION | 400 | 密码策略违规 |
| INTERNAL_SERVER_ERROR | 500 | 服务器内部错误 |
| SERVICE_UNAVAILABLE | 503 | 服务不可用 |

### 1.6 HTTP状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 201 | 资源创建成功 |
| 204 | 请求成功，无返回内容 |
| 400 | 请求参数错误 |
| 401 | 未授权，缺少或无效的认证信息 |
| 403 | 禁止访问，没有权限执行该操作 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 503 | 服务不可用 |

---

## 2. 认证服务 API (auth-service, port 8093)

### 2.1 用户登录

- **Method**: POST
- **Path**: /api/v1/auth/login
- **Description**: 用户登录认证，支持MFA多因素认证
- **Authentication**: 无需认证

**Request Body**:
```json
{
  "username": "string (必填)",
  "password": "string (必填)",
  "mfaCode": "integer (可选, MFA启用时必填)"
}
```

**Response (200) - 登录成功**:
```json
{
  "code": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "username": "admin",
    "roles": ["ROLE_ADMIN", "ROLE_USER"]
  }
}
```

**Response (206) - 需要MFA验证**:
```json
{
  "code": 206,
  "message": "MFA verification required",
  "data": {
    "mfaRequired": true,
    "mfaSessionId": "uuid-string"
  }
}
```

**Error Codes**:
| 错误码 | 说明 |
|--------|------|
| INVALID_CREDENTIALS | 用户名或密码错误 |
| ACCOUNT_LOCKED | 账户已锁定 |
| ACCOUNT_DISABLED | 账户已禁用 |
| INVALID_MFA_CODE | MFA验证码无效 |
| AUTH_ERROR | 认证失败 |

### 2.2 Token刷新

- **Method**: POST
- **Path**: /api/v1/auth/refresh
- **Description**: 使用Refresh Token刷新Access Token
- **Authentication**: 无需认证

**Request Body**:
```json
{
  "refreshToken": "string (必填)"
}
```

**Response (200)**:
```json
{
  "code": 200,
  "message": "Token refreshed",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer"
  }
}
```

**Error Codes**:
| 错误码 | 说明 |
|--------|------|
| TOKEN_REVOKED | Token已撤销 |
| INVALID_REFRESH_TOKEN | 无效的Refresh Token |

### 2.3 用户登出

- **Method**: POST
- **Path**: /api/v1/auth/logout
- **Description**: 当前用户登出，将当前Token加入黑名单
- **Authentication**: Bearer Token (required)

**Response (200)**:
```json
{
  "code": 200,
  "message": "Logout successful",
  "data": null
}
```

### 2.4 登出所有设备

- **Method**: POST
- **Path**: /api/v1/auth/logout-all
- **Description**: 登出所有设备，将所有用户Token加入黑名单
- **Authentication**: Bearer Token (required)

**Response (200)**:
```json
{
  "code": 200,
  "message": "All sessions terminated",
  "data": null
}
```

### 2.5 修改密码

- **Method**: POST
- **Path**: /api/v1/auth/change-password
- **Description**: 修改当前登录用户的密码
- **Authentication**: Bearer Token (required)

**Request Body**:
```json
{
  "currentPassword": "string (必填)",
  "newPassword": "string (必填, 8-128字符)",
  "confirmPassword": "string (必填)"
}
```

**Response (200)**:
```json
{
  "code": 200,
  "message": "Password changed successfully",
  "data": null
}
```

**Error Codes**:
| 错误码 | 说明 |
|--------|------|
| INVALID_CURRENT_PASSWORD | 当前密码不正确 |
| PASSWORD_MISMATCH | 两次输入的密码不一致 |
| PASSWORD_POLICY_VIOLATION | 密码不满足策略要求 |
| NOT_AUTHENTICATED | 未认证 |

### 2.6 忘记密码

- **Method**: POST
- **Path**: /api/v1/auth/forgot-password
- **Description**: 发起忘记密码请求，系统将发送重置链接到邮箱
- **Authentication**: 无需认证

**Request Body**:
```json
{
  "email": "string (必填)"
}
```

**Response (200)**:
```json
{
  "code": 200,
  "message": "If the email exists, a password reset link has been sent",
  "data": null
}
```

### 2.7 获取当前用户信息

- **Method**: GET
- **Path**: /api/v1/auth/me
- **Description**: 获取当前登录用户的详细信息
- **Authentication**: Bearer Token (required)

**Response (200)**:
```json
{
  "code": 200,
  "data": {
    "username": "admin",
    "email": "admin@example.com",
    "enabled": true,
    "mfaEnabled": true,
    "roles": ["ROLE_ADMIN"],
    "permissions": ["PRODUCT:CREATE", "PRODUCT:READ"]
  }
}
```

### 2.8 获取当前用户权限

- **Method**: GET
- **Path**: /api/v1/auth/permissions
- **Description**: 获取当前用户的权限集合
- **Authentication**: Bearer Token (required)

**Response (200)**:
```json
{
  "code": 200,
  "data": ["PRODUCT:CREATE", "PRODUCT:READ", "ORDER:CREATE"]
}
```

### 2.9 获取当前用户角色

- **Method**: GET
- **Path**: /api/v1/auth/roles
- **Description**: 获取当前用户的角色集合
- **Authentication**: Bearer Token (required)

**Response (200)**:
```json
{
  "code": 200,
  "data": ["ROLE_ADMIN", "ROLE_USER"]
}
```

### 2.10 MFA 初始化

- **Method**: POST
- **Path**: /api/v1/mfa/init
- **Description**: 初始化MFA多因素认证
- **Authentication**: Bearer Token (required)

**Response (200)**:
```json
{
  "secret": "JBSWY3DPEHPK3PXP",
  "qrCodeUrl": "otpauth://totp/...",
  "recoveryCode": "ABCD-EFGH-IJKL"
}
```

### 2.11 MFA 启用

- **Method**: POST
- **Path**: /api/v1/mfa/enable
- **Description**: 启用MFA认证
- **Authentication**: Bearer Token (required)

**Query Parameters**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | integer | 是 | MFA验证码 |

**Response (200)**:
```json
{
  "message": "MFA enabled successfully"
}
```

### 2.12 MFA 禁用

- **Method**: POST
- **Path**: /api/v1/mfa/disable
- **Description**: 禁用MFA认证
- **Authentication**: Bearer Token (required)

**Response (200)**:
```json
{
  "message": "MFA disabled successfully"
}
```

### 2.13 MFA 使用恢复码禁用

- **Method**: POST
- **Path**: /api/v1/mfa/disable/recovery
- **Description**: 使用恢复码禁用MFA
- **Authentication**: Bearer Token (required)

**Query Parameters**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| recoveryCode | string | 是 | MFA恢复码 |

### 2.14 重新生成MFA恢复码

- **Method**: POST
- **Path**: /api/v1/mfa/recovery/generate
- **Description**: 重新生成MFA恢复码
- **Authentication**: Bearer Token (required)

**Response (200)**:
```json
{
  "recoveryCode": "WXYZ-1234-5678"
}
```

### 2.15 获取MFA状态

- **Method**: GET
- **Path**: /api/v1/mfa/status
- **Description**: 获取当前用户的MFA状态
- **Authentication**: Bearer Token (required)

**Response (200)**:
```json
{
  "mfaEnabled": true,
  "hasRecoveryCode": true
}
```

---

## 3. 产品服务 API (product-service)

**基础路径**: `/api/v1/products`

### 3.1 获取所有产品

- **Method**: GET
- **Path**: /api/v1/products
- **Description**: 获取系统中所有产品列表
- **Authentication**: Bearer Token (required)

**Response (200)**:
```json
[
  {
    "id": 1,
    "productCode": "P001",
    "sku": "SKU-001",
    "name": "产品名称",
    "brand": "品牌",
    "price": 199.99,
    "costPrice": 150.00,
    "stockQuantity": 100,
    "status": "ACTIVE",
    "weight": 1.5,
    "isActive": true,
    "createdAt": "2025-06-05T10:00:00",
    "updatedAt": "2025-06-05T10:00:00"
  }
]
```

### 3.2 分页获取产品

- **Method**: GET
- **Path**: /api/v1/products/paged
- **Description**: 分页获取产品列表，支持排序
- **Authentication**: Bearer Token (required)

**Query Parameters**:
| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | integer | 0 | 页码（从0开始） |
| size | integer | 20 | 每页数量 |
| sort | string | id | 排序字段（支持: id, productCode, sku, name, brand, price, costPrice, stockQuantity, status, weight, isActive, createdAt, updatedAt） |

**Response (200)**:
```json
{
  "content": [ ... ],
  "totalElements": 100,
  "totalPages": 5,
  "number": 0,
  "size": 20
}
```

### 3.3 根据ID获取产品

- **Method**: GET
- **Path**: /api/v1/products/{id}
- **Description**: 根据唯一标识符获取单个产品
- **Authentication**: Bearer Token (required)

**Path Parameters**:
| 参数 | 类型 | 说明 |
|------|------|------|
| id | integer | 产品ID |

**Response (200)**:
```json
{
  "id": 1,
  "productCode": "P001",
  "sku": "SKU-001",
  "name": "产品名称",
  "brand": "品牌",
  "price": 199.99,
  "costPrice": 150.00,
  "stockQuantity": 100,
  "status": "ACTIVE",
  "weight": 1.5,
  "isActive": true
}
```

### 3.4 创建产品

- **Method**: POST
- **Path**: /api/v1/products
- **Description**: 创建新产品
- **Authentication**: Bearer Token (required)

**Request Body**:
```json
{
  "productCode": "string (必填)",
  "sku": "string",
  "name": "string (必填)",
  "brand": "string",
  "price": "number (必填)",
  "costPrice": "number",
  "stockQuantity": "integer",
  "status": "string",
  "weight": "number",
  "isActive": "boolean"
}
```

**Response (201)**:
```json
{
  "id": 1,
  "productCode": "P001",
  "sku": "SKU-001",
  "name": "产品名称",
  ...
}
```

### 3.5 更新产品

- **Method**: PUT
- **Path**: /api/v1/products/{id}
- **Description**: 更新现有产品信息
- **Authentication**: Bearer Token (required)

**Path Parameters**: `id` - 产品ID

**Request Body**: 同创建产品

**Response (200)**: 同创建产品

### 3.6 删除产品

- **Method**: DELETE
- **Path**: /api/v1/products/{id}
- **Description**: 根据ID删除产品
- **Authentication**: Bearer Token (required)

**Response (204)**: 无内容

### 3.7 根据SKU获取产品

- **Method**: GET
- **Path**: /api/v1/products/sku/{sku}
- **Description**: 根据SKU编码获取产品
- **Authentication**: Bearer Token (required)

**Path Parameters**: `sku` - 产品SKU编码

**Response (200)**: 同获取产品

### 3.8 获取产品SKU列表

- **Method**: GET
- **Path**: /api/v1/products/{productId}/skus
- **Description**: 获取指定产品的所有SKU
- **Authentication**: Bearer Token (required)

**Path Parameters**: `productId` - 产品ID

### 3.9 根据SKU编码获取产品SKU

- **Method**: GET
- **Path**: /api/v1/products/skus/code/{skuCode}
- **Description**: 根据SKU编码获取产品SKU详情
- **Authentication**: Bearer Token (required)

### 3.10 创建产品SKU

- **Method**: POST
- **Path**: /api/v1/products/{productId}/skus
- **Description**: 为指定产品创建SKU
- **Authentication**: Bearer Token (required)

### 3.11 更新产品SKU

- **Method**: PUT
- **Path**: /api/v1/products/skus/{skuId}
- **Description**: 更新现有产品SKU
- **Authentication**: Bearer Token (required)

### 3.12 删除产品SKU

- **Method**: DELETE
- **Path**: /api/v1/products/skus/{skuId}
- **Description**: 删除产品SKU
- **Authentication**: Bearer Token (required)

### 3.13 商品分类管理

**基础路径**: `/api/v1/categories`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取分类详情 | GET | /api/v1/categories/{id} | 根据ID获取分类 |
| 获取分类树 | GET | /api/v1/categories/tree | 获取树形分类结构 |
| 获取子分类 | GET | /api/v1/categories/{parentId}/children | 获取子分类列表 |
| 创建分类 | POST | /api/v1/categories | 创建商品分类 |
| 更新分类 | PUT | /api/v1/categories/{id} | 更新商品分类 |
| 删除分类 | DELETE | /api/v1/categories/{id} | 删除商品分类 |
| 更新分类状态 | PUT | /api/v1/categories/{id}/status | 更新分类状态 |
| 移动分类 | PUT | /api/v1/categories/{id}/move | 移动分类到新父级 |
| 排序分类 | PUT | /api/v1/categories/sort | 批量排序分类 |

---

## 4. 库存服务 API (inventory-service)

**基础路径**: `/api/v1/inventory`

### 4.1 库存管理

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取所有库存 | GET | /api/v1/inventory | 获取所有库存记录 |
| 根据ID获取库存 | GET | /api/v1/inventory/{id} | 根据ID获取库存记录 |
| 根据产品ID获取库存 | GET | /api/v1/inventory/product/{productId} | 获取产品所有库存 |
| 根据仓库ID获取库存 | GET | /api/v1/inventory/warehouse/{warehouseId} | 获取仓库所有库存 |
| 创建库存记录 | POST | /api/v1/inventory | 创建新库存记录 |
| 更新库存记录 | PUT | /api/v1/inventory/{id} | 更新库存记录 |
| 删除库存记录 | DELETE | /api/v1/inventory/{id} | 删除库存记录 |
| 预留库存 | POST | /api/v1/inventory/reserve | 预留指定数量库存 |
| 释放预留库存 | POST | /api/v1/inventory/release | 释放预留库存 |
| 调整库存数量 | POST | /api/v1/inventory/adjust | 调整库存（正增负减） |
| 获取所有仓库 | GET | /api/v1/inventory/warehouses | 获取仓库列表 |
| 获取所有批次 | GET | /api/v1/inventory/batches | 获取批次列表 |
| 获取即将过期批次 | GET | /api/v1/inventory/batches/expiring | 获取指定日期前过期批次 |

### 4.2 预留库存

- **Method**: POST
- **Path**: /api/v1/inventory/reserve
- **Description**: 为仓库中的产品预留指定数量库存
- **Authentication**: Bearer Token (required)

**Query Parameters**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productId | integer | 是 | 产品ID |
| warehouseId | integer | 是 | 仓库ID |
| quantity | integer | 是 | 预留数量 |

**Response (200)**:
```json
90
```
(返回剩余可用数量)

### 4.3 释放预留库存

- **Method**: POST
- **Path**: /api/v1/inventory/release
- **Description**: 释放仓库中产品先前预留的库存
- **Authentication**: Bearer Token (required)

**Query Parameters**: 同预留库存

### 4.4 调整库存数量

- **Method**: POST
- **Path**: /api/v1/inventory/adjust
- **Description**: 调整仓库中产品的库存数量（正值增加，负值减少）
- **Authentication**: Bearer Token (required)

**Query Parameters**: 同预留库存

### 4.5 库存告警

**基础路径**: `/api/v1/inventory/alerts`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 创建/更新告警阈值 | POST | /api/v1/inventory/alerts/thresholds | 设置库存告警阈值 |
| 获取所有告警阈值 | GET | /api/v1/inventory/alerts/thresholds | 获取告警阈值列表 |
| 检查库存并告警 | POST | /api/v1/inventory/alerts/check/{inventoryId} | 检查指定库存 |
| 检查所有库存 | POST | /api/v1/inventory/alerts/check-all | 批量检查所有库存 |
| 获取未确认告警 | GET | /api/v1/inventory/alerts/unacknowledged | 获取未确认告警 |
| 确认告警 | PATCH | /api/v1/inventory/alerts/{alertId}/acknowledge | 确认指定告警 |

### 4.6 库存调拨

**基础路径**: `/api/v1/stock-transfers`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取所有调拨单 | GET | /api/v1/stock-transfers | 获取所有库存调拨单 |
| 根据ID获取 | GET | /api/v1/stock-transfers/{id} | 根据ID获取调拨单 |
| 根据调拨号获取 | GET | /api/v1/stock-transfers/number/{transferNumber} | 根据调拨单号获取 |
| 根据源仓库获取 | GET | /api/v1/stock-transfers/source-warehouse/{warehouseId} | 根据源仓库查询 |
| 根据目标仓库获取 | GET | /api/v1/stock-transfers/target-warehouse/{warehouseId} | 根据目标仓库查询 |
| 根据状态获取 | GET | /api/v1/stock-transfers/status/{status} | 根据状态查询 |
| 创建调拨单 | POST | /api/v1/stock-transfers | 创建库存调拨单 |
| 更新调拨单 | PUT | /api/v1/stock-transfers/{id} | 更新库存调拨单 |
| 删除调拨单 | DELETE | /api/v1/stock-transfers/{id} | 删除库存调拨单 |
| 更新调拨状态 | PATCH | /api/v1/stock-transfers/{id}/status | 更新调拨单状态 |
| 获取调拨总数 | GET | /api/v1/stock-transfers/total/{warehouseId} | 获取仓库调拨总数量 |

### 4.7 其他出入库

**基础路径**: `/api/v1/other-stock`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取所有入库单 | GET | /api/v1/other-stock/in | 获取其他入库单 |
| 获取所有出库单 | GET | /api/v1/other-stock/out | 获取其他出库单 |
| 根据ID获取入库单 | GET | /api/v1/other-stock/in/{id} | 获取入库单详情 |
| 根据ID获取出库单 | GET | /api/v1/other-stock/out/{id} | 获取出库单详情 |
| 创建入库单 | POST | /api/v1/other-stock/in | 创建其他入库单 |
| 创建出库单 | POST | /api/v1/other-stock/out | 创建其他出库单 |
| 删除入库单 | DELETE | /api/v1/other-stock/in/{id} | 删除入库单 |
| 删除出库单 | DELETE | /api/v1/other-stock/out/{id} | 删除出库单 |

---

## 5. 订单服务 API (order-service)

**基础路径**: `/api/v1/orders`

### 5.1 订单管理

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取所有订单 | GET | /api/v1/orders | 获取所有订单列表 |
| 根据ID获取订单 | GET | /api/v1/orders/{id} | 根据ID获取订单 |
| 根据订单号获取 | GET | /api/v1/orders/number/{orderNumber} | 根据订单号获取 |
| 根据客户名称查找 | GET | /api/v1/orders/customer/{customerName} | 根据客户名搜索 |
| 根据状态查找 | GET | /api/v1/orders/status/{status} | 根据状态搜索 |
| 获取有效状态转换 | GET | /api/v1/orders/{id}/transitions | 获取订单有效状态转换 |
| 创建订单 | POST | /api/v1/orders | 创建新订单 |
| 更新订单 | PUT | /api/v1/orders/{id} | 更新订单信息 |
| 更新订单状态 | PATCH | /api/v1/orders/{id}/status | 更新订单状态 |
| 取消订单 | POST | /api/v1/orders/{id}/cancel | 取消订单并恢复库存 |
| 向订单添加商品 | POST | /api/v1/orders/{id}/items | 添加商品到订单 |
| 从订单移除商品 | DELETE | /api/v1/orders/{orderId}/items/{itemId} | 移除订单商品 |
| 删除订单 | DELETE | /api/v1/orders/{id} | 删除订单 |

### 5.2 创建订单

- **Method**: POST
- **Path**: /api/v1/orders
- **Description**: 创建新订单，触发分布式事务Saga
- **Authentication**: Bearer Token (required)

**Request Body**:
```json
{
  "customerName": "string (必填)",
  "customerEmail": "string",
  "customerPhone": "string",
  "shippingAddress": "string",
  "billingAddress": "string",
  "paymentMethod": "CREDIT_CARD|DEBIT_CARD|PAYPAL|BANK_TRANSFER|CASH",
  "notes": "string",
  "items": [
    {
      "productId": "integer (必填)",
      "productName": "string",
      "quantity": "integer (必填, min:1)",
      "unitPrice": "number",
      "discount": "number"
    }
  ]
}
```

**Response (201)**:
```json
{
  "code": 201,
  "message": "Order created successfully",
  "data": {
    "id": 1,
    "orderNumber": "ORD-2025-00001",
    "customerName": "张三",
    "customerEmail": "zhangsan@example.com",
    "status": "PENDING",
    "paymentMethod": "CREDIT_CARD",
    "paymentStatus": "PENDING",
    "subtotal": 199.99,
    "taxAmount": 19.99,
    "shippingCost": 10.00,
    "discountAmount": 0,
    "totalAmount": 229.98,
    "itemCount": 1,
    "items": [...],
    "createdAt": "2025-06-05T10:00:00",
    "updatedAt": "2025-06-05T10:00:00"
  }
}
```

### 5.3 订单状态说明

**订单状态枚举**: `PENDING`, `PROCESSING`, `COMPLETED`, `CANCELLED`, `REFUNDING`, `REFUNDED`

**有效状态转换**:
- PENDING → PROCESSING, CANCELLED
- PROCESSING → SHIPPED, CANCELLED
- SHIPPED → DELIVERED
- DELIVERED → REFUNDING
- REFUNDING → REFUNDED, COMPLETED

### 5.4 Saga事务查询

- **Method**: GET
- **Path**: /api/orders/saga/status/{sagaId}
- **Description**: 获取分布式事务Saga状态（来自OpenAPI规范）
- **Authentication**: Bearer Token (required)

**Response (200)**:
```json
{
  "sagaId": "string",
  "status": "RUNNING|COMPLETED|COMPENSATING|COMPENSATED|FAILED",
  "executedSteps": ["step1", "step2"],
  "failedStep": "string",
  "errorMessage": "string"
}
```

---

## 6. 采购服务 API (procurement-service)

**基础路径**: `/api/v1/procurement`

### 6.1 采购订单管理

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取所有采购订单 | GET | /api/v1/procurement | 获取所有采购订单 |
| 根据ID获取 | GET | /api/v1/procurement/{id} | 根据ID获取采购订单 |
| 根据订单号获取 | GET | /api/v1/procurement/number/{orderNumber} | 根据订单号获取 |
| 根据供应商获取 | GET | /api/v1/procurement/supplier/{supplierId} | 根据供应商查询 |
| 根据状态获取 | GET | /api/v1/procurement/status/{status} | 根据状态查询 |
| 根据仓库获取 | GET | /api/v1/procurement/warehouse/{warehouseId} | 根据仓库查询 |
| 获取有效状态转换 | GET | /api/v1/procurement/{id}/transitions | 获取订单状态转换 |
| 获取可用状态列表 | GET | /api/v1/procurement/statuses | 获取所有状态 |
| 创建采购订单 | POST | /api/v1/procurement | 创建采购订单 |
| 更新采购订单 | PUT | /api/v1/procurement/{id} | 更新采购订单 |
| 删除采购订单 | DELETE | /api/v1/procurement/{id} | 删除采购订单 |
| 更新订单状态 | PATCH | /api/v1/procurement/{id}/status | 更新采购订单状态 |
| 确认订单(步骤1) | POST | /api/v1/procurement/{id}/confirm | 确认采购订单 |
| 开始处理(步骤2) | POST | /api/v1/procurement/{id}/process | 开始处理采购订单 |
| 发货(步骤3) | POST | /api/v1/procurement/{id}/ship | 发货采购订单 |
| 收货(步骤4) | POST | /api/v1/procurement/{id}/deliver | 收货采购订单 |
| 取消订单 | POST | /api/v1/procurement/{id}/cancel | 取消采购订单 |
| 供应商采购总额 | GET | /api/v1/procurement/total/supplier/{supplierId} | 获取供应商采购总金额 |
| 仓库采购总额 | GET | /api/v1/procurement/total/warehouse/{warehouseId} | 获取仓库采购总金额 |

### 6.2 采购订单状态

**状态枚举**: `PENDING`, `CONFIRMED`, `IN_PROGRESS`, `SHIPPED`, `DELIVERED`, `CANCELLED`, `RETURNED`

**状态转换流程**:
PENDING → CONFIRMED → IN_PROGRESS → SHIPPED → DELIVERED
任意状态 (PENDING/CONFIRMED/IN_PROGRESS) → CANCELLED

### 6.3 采购退货

**基础路径**: `/api/v1/procurement-returns`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取所有退货订单 | GET | /api/v1/procurement-returns | 获取所有采购退货 |
| 根据ID获取 | GET | /api/v1/procurement-returns/{id} | 根据ID获取 |
| 根据退货单号获取 | GET | /api/v1/procurement-returns/number/{returnNumber} | 根据退货单号获取 |
| 根据供应商获取 | GET | /api/v1/procurement-returns/supplier/{supplierId} | 根据供应商查询 |
| 根据仓库获取 | GET | /api/v1/procurement-returns/warehouse/{warehouseId} | 根据仓库查询 |
| 根据状态获取 | GET | /api/v1/procurement-returns/status/{status} | 根据状态查询 |
| 根据原订单获取 | GET | /api/v1/procurement-returns/original-order/{originalOrderId} | 根据原订单查询 |
| 创建退货订单 | POST | /api/v1/procurement-returns | 创建采购退货 |
| 更新退货订单 | PUT | /api/v1/procurement-returns/{id} | 更新退货订单 |
| 删除退货订单 | DELETE | /api/v1/procurement-returns/{id} | 删除退货订单 |
| 更新退货状态 | PATCH | /api/v1/procurement-returns/{id}/status | 更新退货状态 |
| 供应商退货总额 | GET | /api/v1/procurement-returns/total/{supplierId} | 获取供应商退货总金额 |

---

## 7. 销售服务 API (sales-service)

**基础路径**: `/api/v1/sales`

### 7.1 销售订单管理

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取所有销售订单 | GET | /api/v1/sales | 获取所有销售订单 |
| 根据ID获取 | GET | /api/v1/sales/{id} | 根据ID获取 |
| 根据订单号获取 | GET | /api/v1/sales/number/{orderNumber} | 根据订单号获取 |
| 根据客户获取 | GET | /api/v1/sales/customer/{customerId} | 根据客户查询 |
| 根据状态获取 | GET | /api/v1/sales/status/{status} | 根据状态查询 |
| 根据日期范围获取 | GET | /api/v1/sales/date-range | 根据日期范围查询 |
| 创建销售订单 | POST | /api/v1/sales | 创建销售订单 |
| 更新销售订单 | PUT | /api/v1/sales/{id} | 更新销售订单 |
| 删除销售订单 | DELETE | /api/v1/sales/{id} | 删除销售订单 |
| 更新订单状态 | PATCH | /api/v1/sales/{id}/status | 更新销售订单状态 |
| 客户销售总额 | GET | /api/v1/sales/total/{customerId} | 获取客户销售总金额 |

### 7.2 销售退货

**基础路径**: `/api/v1/sales-returns`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取所有退货订单 | GET | /api/v1/sales-returns | 获取所有销售退货 |
| 根据ID获取 | GET | /api/v1/sales-returns/{id} | 根据ID获取 |
| 根据退货单号获取 | GET | /api/v1/sales-returns/number/{returnNumber} | 根据退货单号获取 |
| 根据客户获取 | GET | /api/v1/sales-returns/customer/{customerId} | 根据客户查询 |
| 根据仓库获取 | GET | /api/v1/sales-returns/warehouse/{warehouseId} | 根据仓库查询 |
| 根据状态获取 | GET | /api/v1/sales-returns/status/{status} | 根据状态查询 |
| 根据原订单获取 | GET | /api/v1/sales-returns/original-order/{originalOrderId} | 根据原订单查询 |
| 创建退货订单 | POST | /api/v1/sales-returns | 创建销售退货 |
| 更新退货订单 | PUT | /api/v1/sales-returns/{id} | 更新退货订单 |
| 删除退货订单 | DELETE | /api/v1/sales-returns/{id} | 删除退货订单 |
| 更新退货状态 | PATCH | /api/v1/sales-returns/{id}/status | 更新退货状态 |
| 客户退货总额 | GET | /api/v1/sales-returns/total/{customerId} | 获取客户退货总金额 |

### 7.3 零售订单

**基础路径**: `/api/v1/retail`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取所有零售订单 | GET | /api/v1/retail | 获取所有零售订单 |
| 根据ID获取 | GET | /api/v1/retail/{id} | 根据ID获取 |
| 根据零售单号获取 | GET | /api/v1/retail/number/{retailNumber} | 根据零售单号获取 |
| 根据客户获取 | GET | /api/v1/retail/customer/{customerId} | 根据客户查询 |
| 根据仓库获取 | GET | /api/v1/retail/warehouse/{warehouseId} | 根据仓库查询 |
| 根据支付状态获取 | GET | /api/v1/retail/payment-status/{paymentStatus} | 根据支付状态查询 |
| 根据日期范围获取 | GET | /api/v1/retail/date-range | 根据日期范围查询 |
| 创建零售订单 | POST | /api/v1/retail | 创建零售订单 |
| 更新零售订单 | PUT | /api/v1/retail/{id} | 更新零售订单 |
| 删除零售订单 | DELETE | /api/v1/retail/{id} | 删除零售订单 |
| 更新支付状态 | PATCH | /api/v1/retail/{id}/payment-status | 更新支付状态 |
| 客户零售总额 | GET | /api/v1/retail/total/{customerId} | 获取客户零售总金额 |

---

## 8. 客户服务 API (customer-service)

**基础路径**: `/api/v1/customers`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 分页获取所有客户 | GET | /api/v1/customers | 分页获取客户列表 |
| 获取所有客户 | GET | /api/v1/customers/all | 获取全部客户（不分页） |
| 根据ID获取 | GET | /api/v1/customers/{id} | 根据ID获取客户 |
| 根据邮箱获取 | GET | /api/v1/customers/email/{email} | 根据邮箱获取客户 |
| 获取活跃客户 | GET | /api/v1/customers/active | 分页获取活跃客户 |
| 搜索客户 | GET | /api/v1/customers/search | 关键词搜索客户 |
| 按名称搜索 | GET | /api/v1/customers/search/name | 按名称搜索客户 |
| 创建客户 | POST | /api/v1/customers | 创建新客户 |
| 更新客户 | PUT | /api/v1/customers/{id} | 更新客户信息 |
| 删除客户 | DELETE | /api/v1/customers/{id} | 删除客户 |
| 激活客户 | PATCH | /api/v1/customers/{id}/activate | 激活客户 |
| 停用客户 | PATCH | /api/v1/customers/{id}/deactivate | 停用客户 |

**搜索客户 Query Parameters**:
| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| keyword | string | - | 搜索关键词 |
| page | integer | 0 | 页码 |
| size | integer | 20 | 每页数量 |

---

## 9. 供应商服务 API (supplier-service)

**基础路径**: `/api/v1/suppliers`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 分页获取所有供应商 | GET | /api/v1/suppliers | 分页获取供应商列表 |
| 获取所有供应商 | GET | /api/v1/suppliers/all | 获取全部供应商（不分页） |
| 根据ID获取 | GET | /api/v1/suppliers/{id} | 根据ID获取供应商 |
| 获取活跃供应商 | GET | /api/v1/suppliers/active | 分页获取活跃供应商 |
| 按类别获取 | GET | /api/v1/suppliers/category/{category} | 根据产品类别获取 |
| 搜索供应商 | GET | /api/v1/suppliers/search | 关键词搜索供应商 |
| 按名称搜索 | GET | /api/v1/suppliers/search/name | 按名称搜索供应商 |
| 创建供应商 | POST | /api/v1/suppliers | 创建新供应商 |
| 更新供应商 | PUT | /api/v1/suppliers/{id} | 更新供应商信息 |
| 删除供应商 | DELETE | /api/v1/suppliers/{id} | 删除供应商 |
| 激活供应商 | PATCH | /api/v1/suppliers/{id}/activate | 激活供应商 |
| 停用供应商 | PATCH | /api/v1/suppliers/{id}/deactivate | 停用供应商 |

---

## 10. 财务服务 API (finance-service)

**基础路径**: `/api/v1/finance`

### 10.1 收入管理

**基础路径**: `/api/v1/finance/accounts`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取所有收入 | GET | /api/v1/finance/accounts/incomes | 获取所有收入记录 |
| 根据ID获取收入 | GET | /api/v1/finance/accounts/incomes/{id} | 根据ID获取收入 |
| 根据收入编号获取 | GET | /api/v1/finance/accounts/incomes/number/{incomeNumber} | 根据收入编号获取 |
| 根据日期范围获取 | GET | /api/v1/finance/accounts/incomes/date-range | 按日期范围查询 |
| 根据收入类型获取 | GET | /api/v1/finance/accounts/incomes/type/{incomeType} | 按收入类型查询 |
| 根据结算账户获取 | GET | /api/v1/finance/accounts/incomes/account/{settlementAccountId} | 按结算账户查询 |
| 创建收入 | POST | /api/v1/finance/accounts/incomes | 创建收入记录 |
| 更新收入 | PUT | /api/v1/finance/accounts/incomes/{id} | 更新收入记录 |
| 删除收入 | DELETE | /api/v1/finance/accounts/incomes/{id} | 删除收入记录 |

### 10.2 支出管理

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取所有支出 | GET | /api/v1/finance/accounts/expenses | 获取所有支出记录 |
| 根据ID获取支出 | GET | /api/v1/finance/accounts/expenses/{id} | 根据ID获取支出 |
| 根据支出编号获取 | GET | /api/v1/finance/accounts/expenses/number/{expenseNumber} | 根据支出编号获取 |
| 根据日期范围获取 | GET | /api/v1/finance/accounts/expenses/date-range | 按日期范围查询 |
| 根据支出类型获取 | GET | /api/v1/finance/accounts/expenses/type/{expenseType} | 按支出类型查询 |
| 根据结算账户获取 | GET | /api/v1/finance/accounts/expenses/account/{settlementAccountId} | 按结算账户查询 |
| 创建支出 | POST | /api/v1/finance/accounts/expenses | 创建支出记录 |
| 更新支出 | PUT | /api/v1/finance/accounts/expenses/{id} | 更新支出记录 |
| 删除支出 | DELETE | /api/v1/finance/accounts/expenses/{id} | 删除支出记录 |

### 10.3 结算账户管理

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取所有结算账户 | GET | /api/v1/finance/accounts/settlement-accounts | 获取结算账户列表 |
| 根据ID获取 | GET | /api/v1/finance/accounts/settlement-accounts/{id} | 根据ID获取 |
| 根据账户编号获取 | GET | /api/v1/finance/accounts/settlement-accounts/number/{accountNumber} | 根据账户编号获取 |
| 根据账户类型获取 | GET | /api/v1/finance/accounts/settlement-accounts/type/{accountType} | 按类型获取 |
| 创建结算账户 | POST | /api/v1/finance/accounts/settlement-accounts | 创建结算账户 |
| 更新结算账户 | PUT | /api/v1/finance/accounts/settlement-accounts/{id} | 更新结算账户 |
| 删除结算账户 | DELETE | /api/v1/finance/accounts/settlement-accounts/{id} | 删除结算账户 |
| 调整账户余额 | PATCH | /api/v1/finance/accounts/settlement-accounts/{id}/balance | 调整账户余额 |

### 10.4 财务凭证管理

**基础路径**: `/api/v1/finance/vouchers`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取所有凭证 | GET | /api/v1/finance/vouchers | 获取所有财务凭证 |
| 根据ID获取凭证 | GET | /api/v1/finance/vouchers/{id} | 根据ID获取凭证 |
| 创建凭证 | POST | /api/v1/finance/vouchers | 创建财务凭证 |
| 更新凭证 | PUT | /api/v1/finance/vouchers/{id} | 更新财务凭证 |
| 删除凭证 | DELETE | /api/v1/finance/vouchers/{id} | 删除财务凭证 |

### 10.5 其他财务Controller（待补充详情）

以下Controller存在但未读取详细代码，标注为待补充：

- **BudgetController** (`/api/v1/finance/budgets`) - 预算管理 (待补充)
- **FinancialReportController** (`/api/v1/finance/reports`) - 财务报表 (待补充)
- **ReceiptController** (`/api/v1/finance/receipts`) - 收款管理 (待补充)
- **PaymentController** (`/api/v1/finance/payments`) - 付款管理 (待补充)
- **ExpenseController** (`/api/v1/finance/expenses`) - 费用管理 (待补充)
- **IncomeController** (`/api/v1/finance/incomes`) - 收入管理（独立控制器） (待补充)
- **TaxController** (`/api/v1/finance/taxes`) - 税务管理 (待补充)
- **InventoryIntegrationController** (`/api/v1/finance/inventory-integration`) - 库存集成 (待补充)
- **SettlementAccountController** (`/api/v1/finance/settlement-accounts`) - 结算账户管理（独立控制器） (待补充)

---

## 11. 报表服务 API (report-service)

**基础路径**: `/api/v1/reports`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 生成报表 | POST | /api/v1/reports | 生成新报表 |
| 查询报表列表 | GET | /api/v1/reports | 按条件查询报表 |
| 根据ID获取报表 | GET | /api/v1/reports/{id} | 获取报表详情 |
| 获取报表内容 | GET | /api/v1/reports/{id}/content | 获取报表内容 |
| 导出报表 | GET | /api/v1/reports/{id}/export | 导出报表（PDF等） |
| 删除报表 | DELETE | /api/v1/reports/{id} | 删除报表 |
| 获取报表统计 | GET | /api/v1/reports/statistics | 获取报表统计数据 |

**查询报表 Query Parameters**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reportName | string | 否 | 报表名称 |
| reportType | string | 否 | 报表类型 |
| status | string | 否 | 报表状态 |
| startDate | datetime | 否 | 开始日期 |
| endDate | datetime | 否 | 结束日期 |
| page | integer | 否 | 页码 (默认0) |
| size | integer | 否 | 每页大小 (默认10) |

**导出报表 Query Parameters**:
| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| format | string | PDF | 导出格式 (PDF, EXCEL, CSV) |

---

## 12. 模板服务 API (template-service)

### 12.1 模板管理

**基础路径**: `/api/v1/templates`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 搜索模板 | GET | /api/v1/templates | 按条件搜索模板 |
| 根据ID获取 | GET | /api/v1/templates/{id} | 获取模板详情 |
| 根据编码获取 | GET | /api/v1/templates/code/{code} | 根据模板编码获取 |
| 按领域获取 | GET | /api/v1/templates/domain/{domain} | 按业务领域获取 |
| 按状态获取 | GET | /api/v1/templates/status/{status} | 按状态获取 |
| 创建模板 | POST | /api/v1/templates | 创建新模板 |
| 更新模板 | PUT | /api/v1/templates/{id} | 更新模板 |
| 删除模板 | DELETE | /api/v1/templates/{id} | 删除模板 |
| 发布模板 | POST | /api/v1/templates/{id}/publish | 发布模板 |
| 废弃模板 | POST | /api/v1/templates/{id}/deprecate | 废弃模板 |
| 归档模板 | POST | /api/v1/templates/{id}/archive | 归档模板 |
| 添加字段 | POST | /api/v1/templates/{templateId}/fields | 添加模板字段 |
| 更新字段 | PUT | /api/v1/templates/{templateId}/fields/{fieldId} | 更新字段 |
| 删除字段 | DELETE | /api/v1/templates/{templateId}/fields/{fieldId} | 删除字段 |
| 验证数据 | POST | /api/v1/templates/{templateId}/validate | 验证数据是否符合模板 |
| 获取必填字段 | GET | /api/v1/templates/{templateCode}/required-fields | 获取必填字段 |
| 获取版本历史 | GET | /api/v1/templates/{id}/versions | 获取版本历史 |
| 获取版本详情 | GET | /api/v1/templates/{id}/versions/{versionId} | 获取指定版本 |
| 回滚版本 | POST | /api/v1/templates/{id}/rollback/{versionId} | 回滚到指定版本 |
| 对比版本 | GET | /api/v1/templates/{id}/versions/compare | 对比两个版本 |
| 版本统计 | GET | /api/v1/templates/{id}/versions/statistics | 版本统计数据 |

### 12.2 批量操作

**基础路径**: `/api/v1/templates/batch`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 批量创建 | POST | /api/v1/templates/batch/create | 批量创建模板 |
| 批量更新 | PUT | /api/v1/templates/batch/update | 批量更新模板 |
| 批量删除 | DELETE | /api/v1/templates/batch/delete | 批量删除模板 |
| 批量发布 | POST | /api/v1/templates/batch/publish | 批量发布模板 |
| 批量废弃 | POST | /api/v1/templates/batch/deprecate | 批量废弃模板 |
| 批量归档 | POST | /api/v1/templates/batch/archive | 批量归档模板 |
| 批量复制 | POST | /api/v1/templates/batch/copy | 批量复制模板 |

### 12.3 导入导出

**基础路径**: `/api/v1/templates/import-export`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 导入JSON | POST | /api/v1/templates/import-export/import/json | 从JSON导入模板 |
| 导入Excel | POST | /api/v1/templates/import-export/import/excel | 从Excel导入模板 |
| 验证导入文件 | POST | /api/v1/templates/import-export/validate | 验证导入文件 |
| 导出JSON | GET | /api/v1/templates/import-export/export/json | 导出为JSON |
| 按领域导出JSON | GET | /api/v1/templates/import-export/export/json/domain/{domain} | 按领域导出JSON |
| 导出Excel | GET | /api/v1/templates/import-export/export/excel | 导出为Excel |
| 按领域导出Excel | GET | /api/v1/templates/import-export/export/excel/domain/{domain} | 按领域导出Excel |
| 导出单个JSON | GET | /api/v1/templates/import-export/export/{id}/json | 导出单个模板JSON |
| 导出单个Excel | GET | /api/v1/templates/import-export/export/{id}/excel | 导出单个模板Excel |
| 获取导入模板 | GET | /api/v1/templates/import-export/template/{fileType} | 获取导入模板示例 |

### 12.4 自定义字段

**基础路径**: `/api`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 创建自定义字段 | POST | /api/templates/{templateId}/custom-fields | 创建自定义字段 |
| 更新自定义字段 | PUT | /api/custom-fields/{id} | 更新自定义字段 |
| 删除自定义字段 | DELETE | /api/custom-fields/{id} | 删除自定义字段 |
| 激活自定义字段 | POST | /api/custom-fields/{id}/activate | 激活自定义字段 |
| 停用自定义字段 | POST | /api/custom-fields/{id}/deactivate | 停用自定义字段 |
| 获取字段详情 | GET | /api/custom-fields/{id} | 获取自定义字段 |
| 获取模板字段 | GET | /api/templates/{templateId}/custom-fields | 获取模板自定义字段 |
| 获取激活字段 | GET | /api/templates/{templateId}/custom-fields/active | 获取激活的字段 |
| 验证字段值 | POST | /api/custom-fields/{id}/validate | 验证字段值 |
| 创建校验规则 | POST | /api/validation-rules | 创建校验规则 |
| 更新校验规则 | PUT | /api/validation-rules/{id} | 更新校验规则 |
| 删除校验规则 | DELETE | /api/validation-rules/{id} | 删除校验规则 |
| 获取校验规则详情 | GET | /api/validation-rules/{id} | 获取校验规则 |
| 根据编码获取规则 | GET | /api/validation-rules/code/{code} | 根据编码获取规则 |
| 获取所有校验规则 | GET | /api/validation-rules | 获取所有规则 |
| 获取内置校验规则 | GET | /api/validation-rules/builtin | 获取内置规则 |
| 按字段类型获取规则 | GET | /api/validation-rules/field-type/{fieldType} | 按字段类型获取规则 |
| 验证正则表达式 | POST | /api/validation-rules/validate-regex | 验证正则表达式 |

### 12.5 审计日志

**基础路径**: `/api/v1/audit-logs`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取模板审计日志 | GET | /api/v1/audit-logs/template/{templateId} | 获取模板审计日志 |
| 分页获取审计日志 | GET | /api/v1/audit-logs/template/{templateId}/paged | 分页获取审计日志 |
| 获取租户审计日志 | GET | /api/v1/audit-logs/tenant/{tenantId} | 按租户获取 |
| 按时间范围获取 | GET | /api/v1/audit-logs/template/{templateId}/time-range | 按时间范围获取 |
| 搜索审计日志 | GET | /api/v1/audit-logs/search | 搜索审计日志 |
| 获取审计统计 | GET | /api/v1/audit-logs/template/{templateId}/statistics | 审计统计数据 |
| 获取最近操作 | GET | /api/v1/audit-logs/operator/{operator}/recent | 最近操作记录 |

### 12.6 模板统计

**基础路径**: `/api/v1/templates/statistics`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取概览统计 | GET | /api/v1/templates/statistics/overview | 模板概览统计 |
| 按状态统计 | GET | /api/v1/templates/statistics/by-status | 按状态统计 |
| 按领域统计 | GET | /api/v1/templates/statistics/by-domain | 按领域统计 |
| 按租户统计 | GET | /api/v1/templates/statistics/by-tenant/{tenantId} | 按租户统计 |
| 获取趋势统计 | GET | /api/v1/templates/statistics/trend | 模板创建趋势 |
| 获取仪表盘数据 | GET | /api/v1/templates/statistics/dashboard | 仪表盘数据 |

### 12.7 其他模板Controller

- **MonitoringController** - 监控端点 (待补充)
- **PermissionController** - 权限控制端点（config目录） (待补充)

---

## 13. 发票服务 API (invoice-service)

### 13.1 开票商品管理

**基础路径**: `/api/v1/invoice/products`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取所有商品 | GET | /api/v1/invoice/products | 获取所有商品信息 |
| 根据ID获取 | GET | /api/v1/invoice/products/{id} | 根据ID获取商品 |
| 获取启用商品 | GET | /api/v1/invoice/products/active | 获取启用状态商品 |
| 创建商品 | POST | /api/v1/invoice/products | 创建商品信息 |
| 更新商品 | PUT | /api/v1/invoice/products/{id} | 更新商品信息 |
| 删除商品 | DELETE | /api/v1/invoice/products/{id} | 逻辑删除商品 |
| 搜索商品 | GET | /api/v1/invoice/products/search | 搜索商品 |
| 高频商品 | GET | /api/v1/invoice/products/frequent | 获取高频使用商品 |
| 推荐商品 | GET | /api/v1/invoice/products/recommend | 根据客户推荐商品 |

### 13.2 金额计算

**基础路径**: `/api/v1/invoice/calculation`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 计算单个商品金额 | POST | /api/v1/invoice/calculation/item | 计算单个商品条目 |
| 计算整张发票金额 | POST | /api/v1/invoice/calculation/invoice | 计算发票金额 |
| 校验金额计算 | POST | /api/v1/invoice/calculation/validate | 校验计算结果 |

### 13.3 其他发票Controller（待补充详情）

- **CustomerInfoController** (`/api/v1/invoice/customers`) - 客户信息管理 (待补充)
- **UnitInfoController** (`/api/v1/invoice/units`) - 单位信息管理 (待补充)
- **IntelligentSearchController** (`/api/v1/invoice/intelligent-search`) - 智能搜索 (待补充)
- **PinyinSearchController** (`/api/v1/invoice/pinyin-search`) - 拼音搜索 (待补充)
- **OperationLogController** (`/api/v1/invoice/operation-logs`) - 操作日志 (待补充)

---

## 14. 数据源服务 API (datasource-service)

### 14.1 数据源配置管理

**基础路径**: `/api/v1/datasources`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取数据源列表 | GET | /api/v1/datasources | 分页获取数据源列表 |
| 获取数据源详情 | GET | /api/v1/datasources/{id} | 根据ID获取数据源 |
| 搜索数据源 | GET | /api/v1/datasources/search | 条件搜索数据源 |
| 按类型获取 | GET | /api/v1/datasources/type/{type} | 按类型获取数据源 |
| 获取配置模板 | GET | /api/v1/datasources/config-schema/{type} | 获取配置字段定义 |
| 创建数据源 | POST | /api/v1/datasources | 创建新数据源 |
| 更新数据源 | PUT | /api/v1/datasources/{id} | 更新数据源配置 |
| 删除数据源 | DELETE | /api/v1/datasources/{id} | 删除数据源 |

### 14.2 连接测试

**基础路径**: `/api/v1/connection-test`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 测试单个连接 | POST | /api/v1/connection-test/{datasourceId} | 测试数据源连接 |
| 批量测试连接 | POST | /api/v1/connection-test/batch | 批量测试多个连接 |
| 获取测试历史 | GET | /api/v1/connection-test/{datasourceId}/history | 获取测试历史 |
| 获取测试统计 | GET | /api/v1/connection-test/{datasourceId}/statistics | 获取测试统计 |

### 14.3 仪表盘

**基础路径**: `/api/v1/dashboard`

| 端点 | Method | 路径 | 说明 |
|------|--------|------|------|
| 获取仪表盘统计 | GET | /api/v1/dashboard/stats | 数据源监控仪表盘统计 |
| 获取健康概览 | GET | /api/v1/dashboard/health-overview | 所有数据源健康概览 |

---

## 15. 管理服务 API (admin-service)

### 15.1 管理端登录

- **Method**: POST
- **Path**: /api/v1/auth/login
- **Description**: 管理端用户登录认证（admin-service中的独立认证端点）
- **Authentication**: 无需认证

**Request Body**:
```json
{
  "username": "string (必填)",
  "password": "string (必填)"
}
```

**Response (200)**:
```json
{
  "code": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "type": "Bearer"
  }
}
```

> **注意**: admin-service中的AuthController提供独立的认证端点，区别于auth-service的完整认证服务。其他管理端API端点需要通过admin-service的具体Controller进行探索（待补充）。

---

## 16. 通用错误码汇总

| 错误码 | HTTP状态码 | 来源服务 | 说明 |
|--------|-----------|----------|------|
| INVALID_REQUEST | 400 | 所有服务 | 请求参数无效 |
| RESOURCE_NOT_FOUND | 404 | 所有服务 | 资源不存在 |
| UNAUTHORIZED | 401 | 所有服务 | 未授权/Token无效 |
| FORBIDDEN | 403 | 所有服务 | 禁止访问 |
| INVALID_CREDENTIALS | 401 | auth-service | 用户名或密码错误 |
| ACCOUNT_LOCKED | 403 | auth-service | 账户已锁定 |
| ACCOUNT_DISABLED | 403 | auth-service | 账户已禁用 |
| INVALID_MFA_CODE | 400 | auth-service | MFA验证码无效 |
| TOKEN_REVOKED | 401 | auth-service | Token已撤销 |
| NOT_AUTHENTICATED | 401 | auth-service | 未认证 |
| PASSWORD_MISMATCH | 400 | auth-service | 两次密码不一致 |
| PASSWORD_POLICY_VIOLATION | 400 | auth-service | 密码策略违规 |
| INVALID_CURRENT_PASSWORD | 400 | auth-service | 当前密码不正确 |
| INVALID_REFRESH_TOKEN | 401 | auth-service | 无效的Refresh Token |
| AUTH_ERROR | 500 | auth-service | 认证服务器错误 |
| INTERNAL_SERVER_ERROR | 500 | 所有服务 | 服务器内部错误 |
| SERVICE_UNAVAILABLE | 503 | 所有服务 | 服务不可用 |

---

## 17. 附录

### 17.1 认证流程说明

```
1. 客户端 POST /api/v1/auth/login { username, password }
2. 若用户启用了MFA:
   - 返回 206 Partial Content, 包含 mfaSessionId
   - 客户端再次 POST /api/v1/auth/login { username, password, mfaCode }
3. 认证成功, 返回 accessToken 和 refreshToken
4. 客户端在后续请求中携带: Authorization: Bearer <accessToken>
5. Access Token 过期后, 使用 POST /api/v1/auth/refresh 刷新
6. 登出时调用 POST /api/v1/auth/logout
```

### 17.2 分页参数规范

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | integer | 0 | 页码（从0开始） |
| size | integer | 20 | 每页数量 |
| sort | string | id | 排序字段 |
| sortDir | string | asc | 排序方向 (asc/desc) |

### 17.3 日期时间格式

- **日期**: `YYYY-MM-DD` (例: `2025-06-05`)
- **日期时间**: `YYYY-MM-DDTHH:mm:ss` (例: `2025-06-05T10:00:00`)
- **带时区**: `YYYY-MM-DDTHH:mm:ss.SSS+00:00` (例: `2025-06-05T10:00:00.000+00:00`)

### 17.4 速率限制说明

| 限制类型 | 限制值 | 说明 |
|----------|--------|------|
| 登录尝试 | 5次/15分钟 | 超过限制账户将被锁定 |
| API请求 | 100次/分钟/IP | 全局API请求限制 |
| Token刷新 | 10次/小时 | Refresh Token刷新频率限制 |

### 17.5 业务伙伴服务 (business-partner-service)

- **BusinessPartnerController** - 业务伙伴统一管理 (待补充)
  - 该服务可能整合了客户和供应商的统一视图

### 17.6 商城服务 (mall-service)

以下商城服务Controller存在但未详细读取，标注为待补充：

- **OrderController** - 商城订单 (待补充)
- **ShoppingCartController** - 购物车 (待补充)
- **PaymentController** - 支付 (待补充)
- **ShipmentController** - 物流 (待补充)
- **RefundController** - 退款 (待补充)
- **CouponController** - 优惠券 (待补充)
- **GroupBuyController** - 团购 (待补充)
- **FullDiscountController** - 满减 (待补充)
- **FlashSaleController** - 秒杀 (待补充)
- **BargainController** - 砍价 (待补充)
- **DistributorController** - 分销 (待补充)

### 17.7 网关服务

- **FallbackController** - 服务降级处理
  - 提供各服务的熔断降级响应

### 17.8 配置服务

- **ConfigManagementController** - 配置管理 (待补充)

### 17.9 服务注册服务

- **ServiceRegistryController** - 服务注册与发现管理 (待补充)

---

> **文档版本**: 3.0.0 | **生成日期**: 2025-06-05 | **生成方式**: 基于Controller源码和OpenAPI规范自动生成
>
> 标记为"待补充"的端点需要进一步阅读对应Controller源码或与开发团队确认获取详细信息。
