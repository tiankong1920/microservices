# API文档

## 版本
- 版本：3.0.0
- 创建日期：2026-01-30
- 最后更新：2026-01-30

## 1. 文档概述

### 1.1 目的

本文档提供了库存管理系统的详细API接口说明，包括API概述、认证方式、请求格式、响应格式、各服务API接口、API错误码和API使用示例，旨在帮助开发者快速集成和使用系统API。

### 1.2 适用范围

- API接口调用
- 认证和授权
- 请求和响应格式
- 错误处理
- API使用示例

### 1.3 目标读者

- 前端开发人员
- 第三方系统集成人员
- 移动应用开发人员
- 测试工程师

### 1.4 术语定义

| 术语 | 定义 |
|------|------|
| **RESTful API**：基于REST架构风格的API，使用HTTP方法（GET、POST、PUT、DELETE）操作资源 |
| **JWT（JSON Web Token）**：用于身份验证的令牌，包含用户身份信息和权限 |
| **HTTP状态码**：HTTP协议定义的状态码，用于表示请求的处理结果 |
| **请求头（Request Header）**：HTTP请求的头部信息，包含认证、内容类型等 |
| **响应头（Response Header）**：HTTP响应的头部信息，包含内容类型、缓存控制等 |
| **分页（Pagination）**：将大量数据分成多个页面返回，减少单次返回的数据量 |

## 2. API概述

### 2.1 基础信息

| 项目 | 说明 |
|------|------|
| **API基础URL**：http://localhost:9090/api |
| **API版本**：v1 |
| **认证方式**：JWT Bearer Token |
| **请求格式**：JSON |
| **响应格式**：JSON |
| **字符编码**：UTF-8 |

### 2.2 通用请求头

| 请求头 | 说明 | 示例 |
|--------|------|------|
| `Content-Type` | 请求内容类型 | `application/json` |
| `Authorization` | 认证令牌 | `Bearer <token>` |
| `Accept` | 响应内容类型 | `application/json` |
| `User-Agent` | 客户端标识 | `InventorySystem/1.0.0` |

### 2.3 通用响应头

| 响应头 | 说明 | 示例 |
|--------|------|------|
| `Content-Type` | 响应内容类型 | `application/json` |
| `Content-Length` | 响应内容长度 | `1024` |
| `X-Request-Id` | 请求ID | `abc123def456` |
| `X-Rate-Limit-Remaining` | 剩余请求次数 | `100` |
| `X-Rate-Limit-Reset` | 请求次数重置时间 | `1609459200` |

### 2.4 通用响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 业务数据
  },
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

### 2.5 分页参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | Integer | 否 | 页码，从1开始，默认为1 |
| `size` | Integer | 否 | 每页大小，默认为10，最大为100 |
| `sort` | String | 否 | 排序字段，默认为id |
| `order` | String | 否 | 排序方向，asc或desc，默认为asc |

### 2.6 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      // 数据列表
    ],
    "pageable": {
      "page": 1,
      "size": 10,
      "total": 100,
      "totalPages": 10
    }
  },
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

## 3. 认证API

### 3.1 用户登录

#### 3.1.1 登录接口

**接口地址**：`POST /api/v1/auth/login`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `username` | String | 是 | 用户名 |
| `password` | String | 是 | 密码 |

**请求示例**：

```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应示例（登录成功）**：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "roles": ["ADMIN"]
    }
  },
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

**响应示例（需要MFA验证）**：

```json
{
  "code": 208,
  "message": "MFA verification required",
  "data": {
    "mfaRequired": true,
    "mfaSessionId": "session-uuid-here"
  },
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

**登录后携带MFA码再次请求**：

```json
{
  "username": "admin",
  "password": "admin123",
  "mfaCode": "123456"
}
```

### 3.2 令牌刷新

#### 3.2.1 刷新令牌接口

**接口地址**：`POST /api/v1/auth/refresh`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `refreshToken` | String | 是 | 刷新令牌 |

**请求示例**：

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "令牌刷新成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "roles": ["ADMIN"]
    }
  },
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

### 3.3 用户登出

#### 3.3.1 登出接口

**接口地址**：`POST /api/v1/auth/logout`

**请求头**：

| 请求头 | 说明 |
|--------|------|
| `Authorization` | `Bearer <token>` |

**响应示例**：

```json
{
  "code": 200,
  "message": "登出成功",
  "data": null,
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

### 3.4 MFA多因素认证

#### 3.4.1 初始化MFA

**接口地址**：`POST /api/v1/mfa/init`

**认证**：需要JWT认证

**响应示例**：

```json
{
  "code": 200,
  "message": null,
  "data": {
    "secret": "JBSWY3DPEHPK3PXP",
    "qrCodeUrl": "otpauth://totp/InventorySystem:admin?secret=JBSWY3DPEHPK3PXP&issuer=InventorySystem&algorithm=SHA1&digits=6&period=30",
    "recoveryCode": "ABCD-1234-EFGH-5678"
  },
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

#### 3.4.2 启用MFA

**接口地址**：`POST /api/v1/mfa/enable`

**认证**：需要JWT认证

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `code` | String | 是 | 来自 authenticator app 的6位验证码 |

**响应示例**：

```json
{
  "code": 200,
  "message": "MFA enabled successfully",
  "data": null,
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

#### 3.4.3 禁用MFA

**接口地址**：`POST /api/v1/mfa/disable`

**认证**：需要JWT认证

**响应示例**：

```json
{
  "code": 200,
  "message": "MFA disabled successfully",
  "data": null,
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

#### 3.4.4 使用恢复码禁用MFA

**接口地址**：`POST /api/v1/mfa/disable/recovery`

**认证**：需要JWT认证

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `recoveryCode` | String | 是 | 恢复码 (格式: XXXX-XXXX-XXXX-XXXX) |

**响应示例**：

```json
{
  "code": 200,
  "message": "MFA disabled successfully",
  "data": null,
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

## 4. 订单服务API

### 4.1 创建订单

#### 4.1.1 创建订单接口

**接口地址**：`POST /api/v1/orders`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `customerId` | Long | 是 | 客户ID |
| `orderDate` | String | 是 | 订单日期，格式：yyyy-MM-dd |
| `items` | Array | 是 | 订单明细 |
| `items[].productId` | Long | 是 | 产品ID |
| `items[].quantity` | Integer | 是 | 数量 |
| `items[].price` | BigDecimal | 是 | 单价 |
| `remark` | String | 否 | 备注 |

**请求示例**：

```json
{
  "customerId": 1,
  "orderDate": "2025-12-29",
  "items": [
    {
      "productId": 1,
      "quantity": 10,
      "price": 100.00
    },
    {
      "productId": 2,
      "quantity": 5,
      "price": 50.00
    }
  ],
  "remark": "紧急订单"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "订单创建成功",
  "data": {
    "orderId": 1,
    "orderNo": "SO202512290001",
    "status": "PENDING",
    "totalAmount": 1250.00
  },
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

### 4.2 查询订单

#### 4.2.1 查询订单列表接口

**接口地址**：`GET /api/v1/orders`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `customerId` | Long | 否 | 客户ID |
| `status` | String | 否 | 订单状态 |
| `startDate` | String | 否 | 开始日期，格式：yyyy-MM-dd |
| `endDate` | String | 否 | 结束日期，格式：yyyy-MM-dd |
| `page` | Integer | 否 | 页码，默认为1 |
| `size` | Integer | 否 | 每页大小，默认为10 |

**请求示例**：

```
GET /api/v1/orders?customerId=1&status=PENDING&page=1&size=10
```

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "content": [
      {
        "orderId": 1,
        "orderNo": "SO202512290001",
        "customerId": 1,
        "customerName": "客户A",
        "status": "PENDING",
        "totalAmount": 1250.00,
        "orderDate": "2025-12-29",
        "createTime": "2025-12-29 10:00:00"
      }
    ],
    "pageable": {
      "page": 1,
      "size": 10,
      "total": 100,
      "totalPages": 10
    }
  },
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

#### 4.2.2 查询订单详情接口

**接口地址**：`GET /api/v1/orders/{orderId}`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `orderId` | Long | 是 | 订单ID |

**请求示例**：

```
GET /api/v1/orders/1
```

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "orderId": 1,
    "orderNo": "SO202512290001",
    "customerId": 1,
    "customerName": "客户A",
    "status": "PENDING",
    "totalAmount": 1250.00,
    "orderDate": "2025-12-29",
    "createTime": "2025-12-29 10:00:00",
    "items": [
      {
        "itemId": 1,
        "productId": 1,
        "productName": "产品A",
        "quantity": 10,
        "price": 100.00,
        "amount": 1000.00
      },
      {
        "itemId": 2,
        "productId": 2,
        "productName": "产品B",
        "quantity": 5,
        "price": 50.00,
        "amount": 250.00
      }
    ]
  },
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

### 4.3 更新订单

#### 4.3.1 更新订单接口

**接口地址**：`PUT /api/v1/orders/{orderId}`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `orderId` | Long | 是 | 订单ID |

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `status` | String | 否 | 订单状态 |
| `remark` | String | 否 | 备注 |

**请求示例**：

```json
{
  "status": "CONFIRMED",
  "remark": "已确认"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "订单更新成功",
  "data": {
    "orderId": 1,
    "status": "CONFIRMED",
    "updateTime": "2025-12-29 11:00:00"
  },
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

### 4.4 取消订单

#### 4.4.1 取消订单接口

**接口地址**：`DELETE /api/v1/orders/{orderId}`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `orderId` | Long | 是 | 订单ID |

**请求示例**：

```
DELETE /api/v1/orders/1
```

**响应示例**：

```json
{
  "code": 200,
  "message": "订单取消成功",
  "data": {
    "orderId": 1,
    "status": "CANCELLED",
    "cancelTime": "2025-12-29 12:00:00"
  },
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

## 5. 库存服务API

### 5.1 库存查询

#### 5.1.1 查询库存列表接口

**接口地址**：`GET /api/v1/inventory`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `productId` | Long | 否 | 产品ID |
| `warehouseId` | Long | 否 | 仓库ID |
| `status` | String | 否 | 库存状态 |
| `page` | Integer | 否 | 页码，默认为1 |
| `size` | Integer | 否 | 每页大小，默认为10 |

**请求示例**：

```
GET /api/v1/inventory?productId=1&page=1&size=10
```

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "content": [
      {
        "inventoryId": 1,
        "productId": 1,
        "productName": "产品A",
        "warehouseId": 1,
        "warehouseName": "仓库A",
        "quantity": 100,
        "availableQuantity": 80,
        "reservedQuantity": 20,
        "status": "NORMAL"
      }
    ],
    "pageable": {
      "page": 1,
      "size": 10,
      "total": 50,
      "totalPages": 5
    }
  },
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

### 5.2 库存调整

#### 5.2.1 调整库存接口

**接口地址**：`POST /api/v1/inventory/adjust`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `productId` | Long | 是 | 产品ID |
| `adjustmentType` | String | 是 | 调整类型：IN（入库）、OUT（出库） |
| `quantity` | Integer | 是 | 调整数量 |
| `reason` | String | 是 | 调整原因 |

**请求示例**：

```json
{
  "productId": 1,
  "adjustmentType": "IN",
  "quantity": 10,
  "reason": "盘点调整"
}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "库存调整成功",
  "data": {
    "inventoryId": 1,
    "productId": 1,
    "productName": "产品A",
    "adjustmentType": "IN",
    "adjustmentQuantity": 10,
    "beforeQuantity": 100,
    "afterQuantity": 110,
    "adjustmentTime": "2025-12-29 13:00:00"
  },
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

## 6. 产品服务API

### 6.1 产品列表

#### 6.1.1 查询产品列表接口

**接口地址**：`GET /api/v1/products`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `categoryId` | Long | 否 | 产品分类ID |
| `name` | String | 否 | 产品名称（模糊查询） |
| `status` | String | 否 | 产品状态 |
| `page` | Integer | 否 | 页码，默认为1 |
| `size` | Integer | 否 | 每页大小，默认为10 |

**请求示例**：

```
GET /api/v1/products?name=产品&page=1&size=10
```

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "content": [
      {
        "productId": 1,
        "productCode": "P001",
        "productName": "产品A",
        "categoryId": 1,
        "categoryName": "分类A",
        "costPrice": 80.00,
        "salePrice": 100.00,
        "safetyStock": 20,
        "status": "ACTIVE",
        "createTime": "2025-12-29 08:00:00"
      }
    ],
    "pageable": {
      "page": 1,
      "size": 10,
      "total": 200,
      "totalPages": 20
    }
  },
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

### 6.2 产品详情

#### 6.2.1 查询产品详情接口

**接口地址**：`GET /api/v1/products/{productId}`

**路径参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `productId` | Long | 是 | 产品ID |

**请求示例**：

```
GET /api/v1/products/1
```

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "productId": 1,
    "productCode": "P001",
    "productName": "产品A",
    "categoryId": 1,
    "categoryName": "分类A",
    "description": "产品描述",
    "costPrice": 80.00,
    "salePrice": 100.00,
    "safetyStock": 20,
    "unit": "个",
    "status": "ACTIVE",
    "supplierId": 1,
    "supplierName": "供应商A",
    "createTime": "2025-12-29 08:00:00",
    "updateTime": "2025-12-29 09:00:00"
  },
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

## 7. 客户服务API

### 7.1 客户信息

#### 7.1.1 查询客户列表接口

**接口地址**：`GET /api/v1/customers`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | String | 否 | 客户名称（模糊查询） |
| `status` | String | 否 | 客户状态 |
| `page` | Integer | 否 | 页码，默认为1 |
| `size` | Integer | 否 | 每页大小，默认为10 |

**请求示例**：

```
GET /api/v1/customers?name=客户&page=1&size=10
```

**响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "content": [
      {
        "customerId": 1,
        "customerCode": "C001",
        "customerName": "客户A",
        "contactPerson": "张三",
        "contactPhone": "13800138000",
        "contactEmail": "customer@example.com",
        "address": "北京市朝阳区",
        "creditLimit": 100000.00,
        "status": "ACTIVE",
        "createTime": "2025-12-29 08:00:00"
      }
    ],
    "pageable": {
      "page": 1,
      "size": 10,
      "total": 50,
      "totalPages": 5
    }
  },
  "timestamp": 1609459200000,
  "requestId": "abc123def456"
}
```

## 8. API错误码

### 8.1 通用错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|---------|
| 200 | 成功 | N/A |
| 400 | 请求参数错误 | 检查请求参数格式和必填项 |
| 401 | 未授权 | 检查认证令牌是否有效 |
| 403 | 无权限 | 检查用户是否有操作权限 |
| 404 | 资源不存在 | 检查请求的资源ID是否正确 |
| 500 | 服务器内部错误 | 联系技术支持 |

### 8.2 业务错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|---------|
| 1001 | 用户名或密码错误 | 检查用户名和密码是否正确 |
| 1002 | 令牌已过期 | 重新登录获取新令牌 |
| 1003 | 令牌无效 | 检查令牌格式是否正确 |
| 2001 | 库存不足 | 检查库存数量是否充足 |
| 2002 | 产品不存在 | 检查产品ID是否正确 |
| 2003 | 客户不存在 | 检查客户ID是否正确 |
| 2004 | 供应商不存在 | 检查供应商ID是否正确 |
| 3001 | 订单状态不允许 | 检查订单状态是否允许该操作 |
| 3002 | 订单已取消 | 订单已取消，无法继续操作 |

## 9. API使用示例

### 9.1 使用cURL调用API

#### 9.1.1 登录示例

```bash
curl -X POST http://localhost:9090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

#### 9.1.2 创建订单示例

```bash
curl -X POST http://localhost:9090/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "customerId": 1,
    "orderDate": "2025-12-29",
    "items": [
      {
        "productId": 1,
        "quantity": 10,
        "price": 100.00
      }
    ]
  }'
```

### 9.2 使用JavaScript调用API

#### 9.2.1 登录示例

```javascript
fetch('http://localhost:9090/api/v1/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'admin',
    password: 'admin123'
  })
})
  .then(response => response.json())
  .then(data => {
    console.log('登录成功：', data);
    localStorage.setItem('token', data.data.token);
  })
  .catch(error => {
    console.error('登录失败：', error);
  });
```

#### 9.2.2 创建订单示例

```javascript
const token = localStorage.getItem('token');

fetch('http://localhost:9090/api/v1/orders', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    customerId: 1,
    orderDate: '2025-12-29',
    items: [
      {
        productId: 1,
        quantity: 10,
        price: 100.00
      }
    ]
  })
})
  .then(response => response.json())
  .then(data => {
    console.log('订单创建成功：', data);
  })
  .catch(error => {
    console.error('订单创建失败：', error);
  });
```

## 10. 联系方式

### 10.1 技术支持

如果遇到问题，请联系：

- **技术支持邮箱**：support@example.com
- **技术支持热线**：+86-XXX-XXXX
- **在线文档**：[API文档](https://example.com/api-documentation)

### 10.2 开发者支持

如果需要开发支持，请联系：

- **开发者邮箱**：developer@example.com
- **开发者热线**：+86-XXX-XXXX
- **开发者论坛**：[开发者论坛](https://forum.example.com)

---

**免责声明**：本文档仅供参考，具体API接口以实际系统为准。如有疑问，请联系support@example.com。