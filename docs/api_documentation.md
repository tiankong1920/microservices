# 进销存管理系统 API 文档

## 1. 文档概述

### 1.1 文档目的
本文档提供了进销存管理系统所有API的详细说明，包括API的功能、请求参数、响应格式、示例等，旨在帮助开发人员和集成方理解和使用系统API。

### 1.2 文档范围
本文档涵盖了进销存管理系统的所有微服务API：
- 产品服务
- 库存服务
- 订单服务
- 采购服务
- 销售服务
- 客户服务
- 供应商服务

### 1.3 适用对象
- 前端开发人员
- 后端开发人员
- 系统集成人员
- 测试人员
- API调用方

## 2. 通用信息

### 2.1 API版本
- 产品服务：v1
- 库存服务：v1
- 订单服务：v1
- 采购服务：v1
- 销售服务：v1
- 客户服务：v1
- 供应商服务：v1

### 2.2 认证授权
所有API均需要JWT认证，认证信息通过HTTP头中的`Authorization`字段传递，格式为：`Bearer {token}`。

### 2.3 错误处理
API返回的错误信息格式统一为：
```json
{
  "timestamp": "2025-12-08T10:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "错误信息描述",
  "path": "/api/v1/products/1"
}
```

### 2.4 响应格式
所有API成功响应的格式统一为：
```json
{
  "timestamp": "2025-12-08T10:00:00.000+00:00",
  "status": 200,
  "data": { ... }
}
```

## 3. 产品服务 API

### 3.1 基础路径
`/api/products`

### 3.2 API列表

| API路径 | 请求方法 | 功能描述 |
|---------|----------|----------|
| `/` | GET | 获取所有产品 |
| `/` | POST | 创建新产品 |
| `/{productId}` | GET | 根据ID获取产品 |
| `/{productId}` | PUT | 更新产品 |
| `/{productId}` | DELETE | 删除产品 |
| `/code/{productCode}` | GET | 根据产品代码获取产品 |
| `/category/{categoryId}` | GET | 根据分类获取产品 |
| `/search` | GET | 根据名称搜索产品 |

### 3.3 API详情

#### 3.3.1 获取所有产品
- **路径**: `/api/products`
- **方法**: `GET`
- **功能**: 获取系统中所有产品信息
- **响应示例**: 
  ```json
  {
    "timestamp": "2025-12-08T10:00:00.000+00:00",
    "status": 200,
    "data": [
      {
        "id": 1,
        "productCode": "P001",
        "productName": "产品名称",
        "category": "电子产品",
        "price": 199.99,
        "description": "产品描述"
      }
    ]
  }
  ```

#### 3.3.2 创建新产品
- **路径**: `/api/products`
- **方法**: `POST`
- **功能**: 创建一个新的产品
- **请求体**: 
  ```json
  {
    "productCode": "P001",
    "productName": "产品名称",
    "category": "电子产品",
    "price": 199.99,
    "description": "产品描述"
  }
  ```
- **响应示例**: 
  ```json
  {
    "timestamp": "2025-12-08T10:00:00.000+00:00",
    "status": 201,
    "data": {
      "id": 1,
      "productCode": "P001",
      "productName": "产品名称",
      "category": "电子产品",
      "price": 199.99,
      "description": "产品描述"
    }
  }
  ```

#### 3.3.3 根据ID获取产品
- **路径**: `/api/products/{productId}`
- **方法**: `GET`
- **功能**: 根据产品ID获取产品信息
- **响应示例**: 
  ```json
  {
    "timestamp": "2025-12-08T10:00:00.000+00:00",
    "status": 200,
    "data": {
      "id": 1,
      "productCode": "P001",
      "productName": "产品名称",
      "category": "电子产品",
      "price": 199.99,
      "description": "产品描述"
    }
  }
  ```

## 4. 库存服务 API

### 4.1 基础路径
`/api/v1/inventory`

### 4.2 API列表

| API路径 | 请求方法 | 功能描述 |
|---------|----------|----------|
| `/` | GET | 获取所有库存记录 |
| `/` | POST | 创建库存记录 |
| `/{inventoryId}` | GET | 根据ID获取库存记录 |
| `/{inventoryId}` | PUT | 更新库存记录 |
| `/{inventoryId}` | DELETE | 删除库存记录 |
| `/product/{productId}` | GET | 根据产品ID获取库存记录 |
| `/warehouse/{warehouseId}` | GET | 根据仓库ID获取库存记录 |
| `/product/{productId}/warehouse/{warehouseId}` | GET | 根据产品ID和仓库ID获取库存记录 |
| `/available` | GET | 获取可用库存 |
| `/alert` | GET | 获取库存告警 |
| `/{inventoryId}/reserve` | POST | 预留库存 |
| `/{inventoryId}/release` | POST | 释放预留库存 |
| `/{inventoryId}/adjust` | POST | 调整库存数量 |

### 4.3 API详情

#### 4.3.1 获取所有库存记录
- **路径**: `/api/v1/inventory`
- **方法**: `GET`
- **功能**: 获取系统中所有库存记录
- **响应示例**: 
  ```json
  {
    "timestamp": "2025-12-08T10:00:00.000+00:00",
    "status": 200,
    "data": [
      {
        "id": 1,
        "productId": 1,
        "warehouseId": 1,
        "quantity": 100,
        "reservedQuantity": 10,
        "availableQuantity": 90,
        "unitCost": 100.00,
        "lastUpdated": "2025-12-08T10:00:00.000+00:00"
      }
    ]
  }
  ```

#### 4.3.2 调整库存数量
- **路径**: `/api/v1/inventory/{inventoryId}/adjust`
- **方法**: `POST`
- **功能**: 调整指定库存记录的数量
- **参数**: 
  - `quantity`: 调整数量（正数增加，负数减少）
- **响应示例**: 
  ```json
  {
    "timestamp": "2025-12-08T10:00:00.000+00:00",
    "status": 200,
    "data": {
      "id": 1,
      "productId": 1,
      "warehouseId": 1,
      "quantity": 120,
      "reservedQuantity": 10,
      "availableQuantity": 110,
      "unitCost": 100.00,
      "lastUpdated": "2025-12-08T10:00:00.000+00:00"
    }
  }
  ```

## 5. 订单服务 API

### 5.1 基础路径
`/api/v1/orders`

### 5.2 API列表

| API路径 | 请求方法 | 功能描述 |
|---------|----------|----------|
| `/` | GET | 获取所有订单 |
| `/` | POST | 创建新订单 |
| `/{orderId}` | GET | 根据ID获取订单 |
| `/{orderId}` | PUT | 更新订单 |
| `/{orderId}` | DELETE | 删除订单 |
| `/order-number/{orderNumber}` | GET | 根据订单号获取订单 |
| `/{orderId}/status` | PUT | 更新订单状态 |
| `/type/{orderType}` | GET | 根据订单类型获取订单 |
| `/status/{status}` | GET | 根据状态获取订单 |
| `/customer/{customerId}` | GET | 根据客户ID获取订单 |
| `/supplier/{supplierId}` | GET | 根据供应商ID获取订单 |

### 5.3 API详情

#### 5.3.1 创建新订单
- **路径**: `/api/v1/orders`
- **方法**: `POST`
- **功能**: 创建一个新的订单
- **请求体**: 
  ```json
  {
    "orderNumber": "ORD-20251208-001",
    "orderType": "SALE",
    "customerId": 1,
    "supplierId": null,
    "orderDate": "2025-12-08T10:00:00.000+00:00",
    "totalAmount": 199.99,
    "status": "PENDING",
    "items": [
      {
        "productId": 1,
        "quantity": 1,
        "unitPrice": 199.99,
        "subtotal": 199.99
      }
    ]
  }
  ```
- **响应示例**: 
  ```json
  {
    "timestamp": "2025-12-08T10:00:00.000+00:00",
    "status": 201,
    "data": {
      "id": 1,
      "orderNumber": "ORD-20251208-001",
      "orderType": "SALE",
      "customerId": 1,
      "supplierId": null,
      "orderDate": "2025-12-08T10:00:00.000+00:00",
      "totalAmount": 199.99,
      "status": "PENDING",
      "items": [
        {
          "id": 1,
          "productId": 1,
          "quantity": 1,
          "unitPrice": 199.99,
          "subtotal": 199.99
        }
      ]
    }
  }
  ```

#### 5.3.2 更新订单状态
- **路径**: `/api/v1/orders/{orderId}/status`
- **方法**: `PUT`
- **功能**: 更新指定订单的状态
- **参数**: 
  - `status`: 新状态（PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED）
- **响应示例**: 
  ```json
  {
    "timestamp": "2025-12-08T10:00:00.000+00:00",
    "status": 200,
    "data": {
      "id": 1,
      "orderNumber": "ORD-20251208-001",
      "orderType": "SALE",
      "customerId": 1,
      "supplierId": null,
      "orderDate": "2025-12-08T10:00:00.000+00:00",
      "totalAmount": 199.99,
      "status": "SHIPPED",
      "items": [
        {
          "id": 1,
          "productId": 1,
          "quantity": 1,
          "unitPrice": 199.99,
          "subtotal": 199.99
        }
      ]
    }
  }
  ```

## 6. 采购服务 API

### 6.1 基础路径
`/api/v1/procurement`

### 6.2 API列表

| API路径 | 请求方法 | 功能描述 |
|---------|----------|----------|
| `/orders` | GET | 获取所有采购订单 |
| `/orders` | POST | 创建采购订单 |
| `/orders/{procurementOrderId}` | GET | 根据ID获取采购订单 |
| `/orders/{procurementOrderId}` | PUT | 更新采购订单 |
| `/orders/{procurementOrderId}` | DELETE | 删除采购订单 |
| `/orders/order-number/{orderNumber}` | GET | 根据订单号获取采购订单 |
| `/orders/{procurementOrderId}/status` | PUT | 更新采购订单状态 |
| `/orders/supplier/{supplierId}` | GET | 根据供应商ID获取采购订单 |
| `/orders/status/{status}` | GET | 根据状态获取采购订单 |
| `/items/{procurementOrderItemId}/receive` | POST | 记录采购订单项的收货数量 |

### 6.3 API详情

#### 6.3.1 创建采购订单
- **路径**: `/api/v1/procurement/orders`
- **方法**: `POST`
- **功能**: 创建一个新的采购订单
- **请求体**: 
  ```json
  {
    "orderNumber": "PROC-20251208-001",
    "supplierId": 1,
    "orderDate": "2025-12-08T10:00:00.000+00:00",
    "expectedDeliveryDate": "2025-12-15T10:00:00.000+00:00",
    "totalAmount": 1999.99,
    "status": "PENDING",
    "items": [
      {
        "productId": 1,
        "quantity": 10,
        "unitPrice": 199.99,
        "subtotal": 1999.99
      }
    ]
  }
  ```
- **响应示例**: 
  ```json
  {
    "timestamp": "2025-12-08T10:00:00.000+00:00",
    "status": 201,
    "data": {
      "id": 1,
      "orderNumber": "PROC-20251208-001",
      "supplierId": 1,
      "orderDate": "2025-12-08T10:00:00.000+00:00",
      "expectedDeliveryDate": "2025-12-15T10:00:00.000+00:00",
      "totalAmount": 1999.99,
      "status": "PENDING",
      "items": [
        {
          "id": 1,
          "productId": 1,
          "quantity": 10,
          "unitPrice": 199.99,
          "subtotal": 1999.99,
          "receivedQuantity": 0
        }
      ]
    }
  }
  ```

## 7. 销售服务 API

### 7.1 基础路径
`/api/sales-orders`

### 7.2 API列表

| API路径 | 请求方法 | 功能描述 |
|---------|----------|----------|
| `/` | GET | 获取所有销售订单 |
| `/` | POST | 创建销售订单 |
| `/{salesOrderId}` | GET | 根据ID获取销售订单 |
| `/{salesOrderId}` | PUT | 更新销售订单 |
| `/{salesOrderId}` | DELETE | 删除销售订单 |
| `/order-number/{orderNumber}` | GET | 根据订单号获取销售订单 |
| `/customer/{customerId}` | GET | 根据客户ID获取销售订单 |
| `/status/{status}` | GET | 根据状态获取销售订单 |
| `/{salesOrderId}/items` | GET | 获取销售订单的所有商品 |
| `/{salesOrderId}/items` | POST | 向销售订单添加商品 |
| `/{salesOrderId}/items/{salesOrderItemId}` | PUT | 更新销售订单的商品 |
| `/{salesOrderId}/items/{salesOrderItemId}` | DELETE | 从销售订单删除商品 |
| `/{salesOrderId}/status` | PATCH | 更新销售订单状态 |

### 7.3 API详情

#### 7.3.1 创建销售订单
- **路径**: `/api/sales-orders`
- **方法**: `POST`
- **功能**: 创建一个新的销售订单
- **请求体**: 
  ```json
  {
    "orderNumber": "SALE-20251208-001",
    "customerId": 1,
    "orderDate": "2025-12-08T10:00:00.000+00:00",
    "deliveryDate": "2025-12-10T10:00:00.000+00:00",
    "totalAmount": 399.98,
    "status": "PENDING",
    "salesOrderItems": [
      {
        "productId": 1,
        "quantity": 2,
        "unitPrice": 199.99,
        "subtotal": 399.98
      }
    ]
  }
  ```
- **响应示例**: 
  ```json
  {
    "timestamp": "2025-12-08T10:00:00.000+00:00",
    "status": 201,
    "data": {
      "id": 1,
      "orderNumber": "SALE-20251208-001",
      "customerId": 1,
      "orderDate": "2025-12-08T10:00:00.000+00:00",
      "deliveryDate": "2025-12-10T10:00:00.000+00:00",
      "totalAmount": 399.98,
      "status": "PENDING",
      "salesOrderItems": [
        {
          "id": 1,
          "productId": 1,
          "quantity": 2,
          "unitPrice": 199.99,
          "subtotal": 399.98
        }
      ]
    }
  }
  ```

## 8. 客户服务 API

### 8.1 基础路径
`/api/customers`

### 8.2 API列表

| API路径 | 请求方法 | 功能描述 |
|---------|----------|----------|
| `/` | GET | 获取所有客户 |
| `/` | POST | 创建客户 |
| `/{customerId}` | GET | 根据ID获取客户 |
| `/{customerId}` | PUT | 更新客户 |
| `/{customerId}` | DELETE | 删除客户 |
| `/code/{customerCode}` | GET | 根据客户代码获取客户 |
| `/name/{customerName}` | GET | 根据名称获取客户 |
| `/status/{status}` | GET | 根据状态获取客户 |
| `/credit-rating/{creditRating}` | GET | 根据信用评级获取客户 |
| `/credit-limit/gte` | GET | 根据信用额度获取客户 |
| `/{customerId}/activate` | PATCH | 激活客户 |
| `/{customerId}/deactivate` | PATCH | 停用客户 |

### 8.3 API详情

#### 8.3.1 创建客户
- **路径**: `/api/customers`
- **方法**: `POST`
- **功能**: 创建一个新的客户
- **请求体**: 
  ```json
  {
    "customerCode": "CUST-001",
    "customerName": "客户名称",
    "contactPerson": "联系人",
    "phone": "13800138000",
    "email": "customer@example.com",
    "address": "客户地址",
    "status": "ACTIVE",
    "creditRating": "A",
    "creditLimit": 100000.00
  }
  ```
- **响应示例**: 
  ```json
  {
    "timestamp": "2025-12-08T10:00:00.000+00:00",
    "status": 201,
    "data": {
      "id": 1,
      "customerCode": "CUST-001",
      "customerName": "客户名称",
      "contactPerson": "联系人",
      "phone": "13800138000",
      "email": "customer@example.com",
      "address": "客户地址",
      "status": "ACTIVE",
      "creditRating": "A",
      "creditLimit": 100000.00
    }
  }
  ```

## 9. 供应商服务 API

### 9.1 基础路径
`/api/suppliers`

### 9.2 API列表

| API路径 | 请求方法 | 功能描述 |
|---------|----------|----------|
| `/` | GET | 获取所有供应商 |
| `/` | POST | 创建供应商 |
| `/{supplierId}` | GET | 根据ID获取供应商 |
| `/{supplierId}` | PUT | 更新供应商 |
| `/{supplierId}` | DELETE | 删除供应商 |
| `/code/{supplierCode}` | GET | 根据供应商代码获取供应商 |
| `/name/{supplierName}` | GET | 根据名称获取供应商 |
| `/status/{status}` | GET | 根据状态获取供应商 |
| `/credit-rating/{creditRating}` | GET | 根据信用评级获取供应商 |
| `/{supplierId}/activate` | PATCH | 激活供应商 |
| `/{supplierId}/deactivate` | PATCH | 停用供应商 |

### 9.3 API详情

#### 9.3.1 创建供应商
- **路径**: `/api/suppliers`
- **方法**: `POST`
- **功能**: 创建一个新的供应商
- **请求体**: 
  ```json
  {
    "supplierCode": "SUPP-001",
    "supplierName": "供应商名称",
    "contactPerson": "联系人",
    "phone": "13900139000",
    "email": "supplier@example.com",
    "address": "供应商地址",
    "status": "ACTIVE",
    "creditRating": "A"
  }
  ```
- **响应示例**: 
  ```json
  {
    "timestamp": "2025-12-08T10:00:00.000+00:00",
    "status": 201,
    "data": {
      "id": 1,
      "supplierCode": "SUPP-001",
      "supplierName": "供应商名称",
      "contactPerson": "联系人",
      "phone": "13900139000",
      "email": "supplier@example.com",
      "address": "供应商地址",
      "status": "ACTIVE",
      "creditRating": "A"
    }
  }
  ```

## 10. 状态码说明

| 状态码 | 描述 |
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

## 11. 数据类型说明

| 数据类型 | 描述 |
|----------|------|
| string | 字符串类型 |
| integer | 整数类型 |
| long | 长整数类型 |
| float | 浮点数类型 |
| double | 双精度浮点数类型 |
| boolean | 布尔类型（true/false） |
| date | 日期类型（YYYY-MM-DD） |
| datetime | 日期时间类型（YYYY-MM-DD HH:MM:SS） |
| array | 数组类型 |
| object | 对象类型 |

## 12. 附录

### 12.1 常用请求示例

#### 使用 curl 创建产品
```bash
curl -X POST "http://localhost:8080/api/products" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"productCode": "P001", "productName": "产品名称", "category": "电子产品", "price": 199.99}'
```

#### 使用 curl 获取库存记录
```bash
curl -X GET "http://localhost:8081/api/v1/inventory/product/1" \
  -H "Authorization: Bearer {token}"
```

### 12.2 错误码参考

| 错误码 | 描述 |
|--------|------|
| INVALID_REQUEST | 请求参数无效 |
| RESOURCE_NOT_FOUND | 资源不存在 |
| UNAUTHORIZED | 未授权 |
| FORBIDDEN | 禁止访问 |
| INTERNAL_SERVER_ERROR | 服务器内部错误 |
| SERVICE_UNAVAILABLE | 服务不可用 |

### 12.3 联系方式

- API 支持：api-support@example.com
- 技术文档：docs.example.com/inventory-api
- 问题反馈：issues.example.com/inventory-api

## 13. 版本历史

| 版本 | 发布日期 | 更新内容 |
|------|----------|----------|
| 3.0.0 | 2025-12-08 | 初始版本，包含所有微服务API文档 |