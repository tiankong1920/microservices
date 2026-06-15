# API设计规范指南

本文档定义了进销存管理系统的API设计规范，所有API开发都应遵循这些规范以确保一致性和可维护性。

## 1. RESTful原则

### 1.1 资源导向设计
- 使用名词复数形式表示资源集合
- 资源路径应具有层次结构，体现资源之间的关系
- 每个资源应有唯一的URI标识

### 1.2 HTTP方法映射
- `GET`: 查询资源（幂等操作）
- `POST`: 创建资源或执行非幂等操作
- `PUT`: 更新整个资源（幂等操作）
- `PATCH`: 部分更新资源（幂等操作）
- `DELETE`: 删除资源（幂等操作）

### 1.3 HTTP状态码
- `200 OK`: 成功响应GET、PUT、PATCH请求
- `201 Created`: 成功创建资源，响应中应包含新资源的URI
- `204 No Content`: 成功执行DELETE请求，响应体为空
- `400 Bad Request`: 客户端请求参数错误
- `401 Unauthorized`: 未认证或认证失败
- `403 Forbidden`: 已认证但权限不足
- `404 Not Found`: 请求的资源不存在
- `409 Conflict`: 资源冲突，如重复创建
- `500 Internal Server Error`: 服务器内部错误

## 2. URI设计规范

### 2.1 命名规范
- 使用小写字母和连字符（kebab-case）分隔单词
- 避免使用下划线或驼峰命名
- URI应简洁明了，能清晰表达资源含义

### 2.2 URI结构
```
https://api.example.com/v1/{resource}/{id}/{sub-resource}
```

示例：
```
GET    /api/v1/products
POST   /api/v1/products
GET    /api/v1/products/123
PUT    /api/v1/products/123
PATCH  /api/v1/products/123
DELETE /api/v1/products/123
GET    /api/v1/products/123/inventory
```

### 2.3 版本控制
- API版本通过URI路径控制：`/api/v1/`
- 版本号使用整数，如v1, v2
- 保持向后兼容性，避免破坏性变更

## 3. 请求规范

### 3.1 查询参数
- 使用查询参数进行资源过滤、排序和分页
- 布尔值参数使用true/false
- 日期时间参数使用ISO 8601格式：`YYYY-MM-DDTHH:mm:ssZ`

查询参数示例：
```
GET /api/v1/products?category=electronics&status=active&sort=-created_at&page=1&size=20
```

### 3.2 请求体格式
- 所有请求体使用JSON格式
- 字段命名使用小写字母和下划线分隔（snake_case）
- 时间字段使用ISO 8601格式

请求体示例：
```json
{
  "name": "iPhone 15",
  "product_code": "IP15-BLK-128G",
  "category": "electronics",
  "unit_price": 999.99,
  "created_at": "2025-11-12T10:30:00Z"
}
```

## 4. 响应规范

### 4.1 响应结构
所有API响应应遵循统一的JSON结构：

成功响应：
```json
{
  "success": true,
  "data": {
    // 具体数据
  },
  "message": "操作成功"
}
```

错误响应：
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "请求参数验证失败",
    "details": [
      {
        "field": "name",
        "message": "商品名称不能为空"
      }
    ]
  },
  "timestamp": "2025-11-12T10:30:00Z"
}
```

### 4.2 分页响应
分页查询响应应包含分页信息：

```json
{
  "success": true,
  "data": {
    "items": [
      // 数据列表
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "total": 100,
      "total_pages": 5
    }
  }
}
```

### 4.3 时间格式
- 所有时间字段使用ISO 8601格式
- 使用UTC时区
- 格式：`YYYY-MM-DDTHH:mm:ss.SSSZ`

## 5. 错误处理规范

### 5.1 错误响应格式
所有错误响应应包含以下字段：
- `success`: 始终为false
- `error`: 错误详情对象
  - `code`: 错误代码（大写字母和下划线）
  - `message`: 错误描述信息
  - `details`: 详细错误信息（可选）
- `timestamp`: 错误发生时间

### 5.2 常见错误代码
| 模块 | 错误代码 | HTTP状态码 | 描述 |
|-----|---------|-----------|------|
| 通用 | INTERNAL_ERROR | 500 | 系统内部错误 |
| 通用 | VALIDATION_ERROR | 400 | 参数验证失败 |
| 通用 | UNAUTHORIZED | 401 | 未认证 |
| 通用 | FORBIDDEN | 403 | 权限不足 |
| 通用 | NOT_FOUND | 404 | 资源不存在 |
| 通用 | CONFLICT | 409 | 资源冲突 |
| 商品 | PRODUCT_NOT_FOUND | 404 | 商品不存在 |
| 商品 | PRODUCT_CODE_DUPLICATE | 409 | 商品编码重复 |
| 库存 | INSUFFICIENT_INVENTORY | 400 | 库存不足 |
| 库存 | INVENTORY_NOT_FOUND | 404 | 库存记录不存在 |
| 订单 | ORDER_NOT_FOUND | 404 | 订单不存在 |
| 订单 | ORDER_STATUS_INVALID | 400 | 订单状态不合法 |
| 用户 | USER_NOT_FOUND | 404 | 用户不存在 |
| 用户 | USER_ALREADY_EXISTS | 409 | 用户已存在 |
| 供应商 | SUPPLIER_NOT_FOUND | 404 | 供应商不存在 |
| 系统 | DATABASE_ERROR | 500 | 数据库错误 |
| 系统 | NETWORK_ERROR | 500 | 网络错误 |
| 系统 | EXTERNAL_SERVICE_ERROR | 500 | 外部服务错误 |

### 5.3 验证错误详情
验证错误应提供详细的字段级错误信息：

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "请求参数验证失败",
    "details": [
      {
        "field": "name",
        "message": "商品名称不能为空",
        "rejected_value": null
      },
      {
        "field": "price",
        "message": "价格必须大于0",
        "rejected_value": -10
      }
    ]
  },
  "timestamp": "2025-11-12T10:30:00Z"
}
```

## 6. 安全规范

### 6.1 认证
- 使用JWT Token进行无状态认证
- 所有API请求必须包含Authorization头
- 格式：`Authorization: Bearer <token>`

### 6.2 授权
- 基于角色的访问控制（RBAC）
- 敏感操作需要特定权限
- 在API文档中明确标注每个端点的权限要求

### 6.3 数据安全
- 敏感信息不应在日志中明文记录
- 密码等敏感字段应加密存储
- API响应中不应包含敏感信息

## 7. 性能规范

### 7.1 分页
- 所有集合资源查询必须支持分页
- 默认页面大小为20条记录
- 最大页面大小限制为100条记录

### 7.2 字段过滤
- 支持通过查询参数指定返回字段
- 减少不必要的数据传输

### 7.3 缓存
- 对于不经常变化的数据，应设置适当的缓存策略
- 使用HTTP缓存头：ETag, Last-Modified, Cache-Control

## 8. API文档规范

### 8.1 文档内容
每个API端点的文档应包含：
- 功能描述
- 请求方法和路径
- 请求参数说明
- 请求示例
- 响应示例
- 错误码说明
- 权限要求

### 8.2 示例文档
```yaml
paths:
  /api/v1/products:
    get:
      summary: 获取商品列表
      description: 分页获取商品列表，支持筛选和排序
      parameters:
        - name: category
          in: query
          description: 商品分类
          schema:
            type: string
        - name: page
          in: query
          description: 页码
          schema:
            type: integer
            default: 1
      responses:
        '200':
          description: 成功返回商品列表
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProductListResponse'
        '401':
          description: 未认证
        '500':
          description: 服务器内部错误
```

## 9. 版本管理

### 9.1 向后兼容
- 新版本API应保持向后兼容
- 避免删除现有字段或端点
- 如需重大变更，应发布新版本

### 9.2 废弃机制
- 废弃的API应标记为deprecated
- 提供迁移指南和时间表
- 保留废弃API至少6个月

## 10. 监控和日志

### 10.1 请求日志
- 记录所有API请求和响应
- 包含请求时间、响应时间、状态码等信息
- 敏感信息应脱敏处理

### 10.2 性能监控
- 监控API响应时间
- 统计各端点调用频率
- 设置性能告警阈值

本规范自发布之日起生效，所有新开发的API必须遵循此规范。现有API应逐步迁移到新规范。