# 错误码映射表与异常迁移对照表

## 1. 概述

本文档用于记录项目中异常处理机制改革的关键信息，包括：
- 错误码与异常类型的映射关系
- 原有异常类到BusinessException的迁移对照
- 错误码使用规范和最佳实践

## 2. 错误码格式规范

### 2.1 格式定义

错误码采用三段式结构：
```
[模块前缀]-[错误类型]-[序号]
```

### 2.2 各段说明

| 段名 | 格式要求 | 说明 | 示例 |
|------|----------|------|------|
| 模块前缀 | 大写字母，不超过4个字符 | 表示错误所属的服务模块 | INV（库存）、ORD（订单）、PROD（产品） |
| 错误类型 | 2位数字 | 表示错误的类型 | 01（参数错误）、02（业务逻辑错误）、03（资源不存在） |
| 序号 | 3位数字 | 表示同类型错误的序号 | 001、002、003 |

### 2.3 错误类型枚举

| 错误类型 | 含义 |
|----------|------|
| 01 | 系统错误 |
| 02 | 参数错误 |
| 03 | 资源不存在 |
| 04 | 业务逻辑错误 |
| 05 | 认证授权错误 |
| 06 | 第三方服务错误 |

## 3. 错误码映射表

### 3.1 通用错误码

| 错误码 | 异常场景描述 | 错误描述模板 | 严重程度 | 处理建议 |
|--------|--------------|--------------|----------|----------|
| COM-02-001 | 参数无效 | 参数 {paramName} 无效，值为 {paramValue} | 低 | 检查请求参数格式是否符合要求 |
| COM-02-002 | 参数缺失 | 缺少必填参数 {paramName} | 低 | 检查请求是否包含所有必填参数 |
| COM-03-001 | 资源不存在 | 资源 {resourceType} 不存在，ID: {resourceId} | 中等 | 检查请求的资源ID是否正确 |
| COM-04-001 | 操作失败 | 操作 {operation} 失败 | 中等 | 检查操作条件是否满足 |
| COM-01-001 | 系统错误 | 系统内部错误 | 高 | 联系系统管理员，查看系统日志 |
| COM-05-001 | 令牌过期 | 认证令牌已过期 | 中等 | 重新登录获取新令牌 |
| COM-05-002 | 令牌无效 | 认证令牌无效 | 中等 | 检查令牌格式是否正确 |
| COM-05-003 | 权限不足 | 没有执行此操作的权限 | 中等 | 检查用户是否有相应权限 |

### 3.2 库存服务错误码

| 错误码 | 异常场景描述 | 错误描述模板 | 严重程度 | 处理建议 |
|--------|--------------|--------------|----------|----------|
| INV-03-001 | 库存不存在 | 库存不存在，产品ID: {productId} | 中等 | 检查产品ID是否正确 |
| INV-02-001 | 库存不足 | 库存不足，产品ID: {productId}，请求数量: {requestQuantity}，可用数量: {availableQuantity} | 中等 | 减少请求数量或补充库存 |
| INV-02-002 | 库存锁定失败 | 库存锁定失败，产品ID: {productId} | 中等 | 稍后重试或检查库存状态 |
| INV-02-003 | 库存解锁失败 | 库存解锁失败，产品ID: {productId} | 中等 | 手动检查库存状态并修复 |
| INV-02-004 | 库存调整失败 | 库存调整失败，产品ID: {productId}，调整数量: {adjustQuantity} | 中等 | 检查调整数量是否合理 |
| INV-03-002 | 批次不存在 | 批次不存在，批次ID: {batchId} | 中等 | 检查批次ID是否正确 |
| INV-02-005 | 批次已过期 | 批次已过期，批次ID: {batchId} | 中等 | 禁用过期批次或清理过期数据 |
| INV-02-006 | 批次库存不足 | 批次库存不足，批次ID: {batchId}，请求数量: {requestQuantity}，可用数量: {availableQuantity} | 中等 | 减少请求数量或选择其他批次 |
| INV-03-003 | 仓库不存在 | 仓库不存在，仓库ID: {warehouseId} | 中等 | 检查仓库ID是否正确 |
| INV-02-007 | 仓库已满 | 仓库已满，仓库ID: {warehouseId} | 中等 | 扩展仓库容量或清理库存 |

### 3.3 订单服务错误码

| 错误码 | 异常场景描述 | 错误描述模板 | 严重程度 | 处理建议 |
|--------|--------------|--------------|----------|----------|
| ORD-03-001 | 订单不存在 | 订单不存在，订单ID: {orderId} | 中等 | 检查订单ID是否正确 |
| ORD-02-001 | 订单状态错误 | 订单状态错误，订单ID: {orderId}，当前状态: {currentStatus}，目标状态: {targetStatus} | 中等 | 检查订单状态流转是否符合业务规则 |
| ORD-02-002 | 订单创建失败 | 订单创建失败，产品ID: {productId} | 中等 | 检查订单数据是否完整 |
| ORD-02-003 | 订单更新失败 | 订单更新失败，订单ID: {orderId} | 中等 | 检查订单是否存在且状态允许更新 |
| ORD-02-004 | 订单删除失败 | 订单删除失败，订单ID: {orderId} | 中等 | 检查订单是否存在且状态允许删除 |
| ORD-02-005 | 订单取消失败 | 订单取消失败，订单ID: {orderId} | 中等 | 检查订单是否存在且状态允许取消 |
| ORD-02-006 | 订单支付失败 | 订单支付失败，订单ID: {orderId} | 中等 | 检查支付信息是否正确 |
| ORD-03-002 | 订单项不存在 | 订单项不存在，订单项ID: {orderItemId} | 中等 | 检查订单项ID是否正确 |
| ORD-01-001 | 订单项无效 | 订单项无效，产品ID: {productId}，数量: {quantity} | 低 | 检查订单项数据是否符合要求 |
| ORD-02-007 | 订单流程错误 | 订单流程错误，订单ID: {orderId} | 高 | 检查订单流程配置是否正确 |
| ORD-02-008 | 订单超时 | 订单超时，订单ID: {orderId} | 中等 | 清理超时订单或延长超时时间 |

### 3.4 产品服务错误码

| 错误码 | 异常场景描述 | 错误描述模板 | 严重程度 | 处理建议 |
|--------|--------------|--------------|----------|----------|
| PROD-03-001 | 产品不存在 | 产品不存在，产品ID: {productId} | 中等 | 检查产品ID是否正确 |
| PROD-02-001 | 产品创建失败 | 产品创建失败，产品名称: {productName} | 中等 | 检查产品数据是否完整 |
| PROD-02-002 | 产品更新失败 | 产品更新失败，产品ID: {productId} | 中等 | 检查产品是否存在且状态允许更新 |
| PROD-02-003 | 产品删除失败 | 产品删除失败，产品ID: {productId} | 中等 | 检查产品是否存在且状态允许删除 |
| PROD-02-004 | 产品状态无效 | 产品状态无效，产品ID: {productId}，状态: {status} | 中等 | 检查产品状态是否符合业务规则 |
| PROD-02-005 | 产品重复 | 产品重复，产品名称: {productName} | 中等 | 检查产品是否已存在 |
| PROD-03-002 | 产品分类不存在 | 产品分类不存在，分类ID: {categoryId} | 中等 | 检查分类ID是否正确 |
| PROD-01-001 | 产品分类无效 | 产品分类无效，分类ID: {categoryId} | 低 | 检查分类数据是否符合要求 |
| PROD-03-003 | 产品属性不存在 | 产品属性不存在，属性ID: {attrId} | 中等 | 检查属性ID是否正确 |
| PROD-01-002 | 产品属性无效 | 产品属性无效，属性名称: {attrName} | 低 | 检查属性数据是否符合要求 |
| PROD-03-004 | BOM不存在 | BOM不存在，BOM ID: {bomId} | 中等 | 检查BOM ID是否正确 |
| PROD-01-003 | BOM无效 | BOM无效，BOM ID: {bomId} | 低 | 检查BOM数据是否符合要求 |

### 3.5 销售服务错误码

| 错误码 | 异常场景描述 | 错误描述模板 | 严重程度 | 处理建议 |
|--------|--------------|--------------|----------|----------|
| SAL-03-001 | 销售订单不存在 | 销售订单不存在，订单ID: {orderId} | 中等 | 检查订单ID是否正确 |
| SAL-04-001 | 销售订单创建失败 | 销售订单创建失败，客户ID: {customerId} | 中等 | 检查销售订单数据是否完整 |
| SAL-04-002 | 销售订单更新失败 | 销售订单更新失败，订单ID: {orderId} | 中等 | 检查销售订单是否存在且状态允许更新 |
| SAL-04-003 | 销售订单删除失败 | 销售订单删除失败，订单ID: {orderId} | 中等 | 检查销售订单是否存在且状态允许删除 |
| SAL-04-004 | 销售订单状态错误 | 销售订单状态错误，订单ID: {orderId}，当前状态: {currentStatus}，目标状态: {targetStatus} | 中等 | 检查销售订单状态流转是否符合业务规则 |
| SAL-03-002 | 销售订单项不存在 | 销售订单项不存在，订单项ID: {orderItemId} | 中等 | 检查订单项ID是否正确 |
| SAL-02-001 | 销售订单项无效 | 销售订单项无效，产品ID: {productId}，数量: {quantity} | 低 | 检查订单项数据是否符合要求 |
| SAL-03-003 | 客户不存在 | 客户不存在，客户ID: {customerId} | 中等 | 检查客户ID是否正确 |
| SAL-02-002 | 客户无效 | 客户无效，客户名称: {customerName} | 低 | 检查客户数据是否符合要求 |

## 4. 异常迁移对照表

### 4.1 库存服务异常迁移

| 原异常类名 | 全限定类名 | 出现文件路径 | 替换后的错误码 | 上下文信息提取方式 | 处理逻辑变更说明 |
|------------|------------|--------------|----------------|--------------------|------------------|
| InventoryNotFoundException | com.inventory.inventoryservice.exception.InventoryNotFoundException | 多处 | INV-03-001 | 从异常消息中提取产品ID | 统一使用BusinessException，添加上下文信息 |
| BatchNotFoundException | com.inventory.inventoryservice.exception.BatchNotFoundException | 多处 | INV-03-002 | 从异常消息中提取批次ID | 统一使用BusinessException，添加上下文信息 |
| WarehouseNotFoundException | com.inventory.inventoryservice.exception.WarehouseNotFoundException | 多处 | INV-03-003 | 从异常消息中提取仓库ID | 统一使用BusinessException，添加上下文信息 |

### 4.2 订单服务异常迁移

| 原异常类名 | 全限定类名 | 出现文件路径 | 替换后的错误码 | 上下文信息提取方式 | 处理逻辑变更说明 |
|------------|------------|--------------|----------------|--------------------|------------------|
| OrderNotFoundException | com.inventory.orderservice.exception.OrderNotFoundException | 多处 | ORD-03-001 | 从异常消息中提取订单ID | 统一使用BusinessException，添加上下文信息 |

### 4.3 产品服务异常迁移

| 原异常类名 | 全限定类名 | 出现文件路径 | 替换后的错误码 | 上下文信息提取方式 | 处理逻辑变更说明 |
|------------|------------|--------------|----------------|--------------------|------------------|
| ProductNotFoundException | com.inventory.productservice.exception.ProductNotFoundException | 多处 | PROD-03-001 | 从异常消息中提取产品ID | 统一使用BusinessException，添加上下文信息 |

### 4.4 销售服务异常迁移

| 原异常类名 | 全限定类名 | 出现文件路径 | 替换后的错误码 | 上下文信息提取方式 | 处理逻辑变更说明 |
|------------|------------|--------------|----------------|--------------------|------------------|
| SalesOrderNotFoundException | com.inventory.salesservice.exception.SalesOrderNotFoundException | 多处 | SAL-03-001 | 从异常消息中提取销售订单ID | 统一使用BusinessException，添加上下文信息 |
| ProductNotFoundException | com.inventory.salesservice.exception.ProductNotFoundException | 多处 | PROD-03-001 | 从异常消息中提取产品ID | 统一使用BusinessException，添加上下文信息 |
| CustomerNotFoundException | com.inventory.salesservice.exception.CustomerNotFoundException | 多处 | SAL-03-003 | 从异常消息中提取客户ID | 统一使用BusinessException，添加上下文信息 |

### 4.5 采购服务异常迁移

| 原异常类名 | 全限定类名 | 出现文件路径 | 替换后的错误码 | 上下文信息提取方式 | 处理逻辑变更说明 |
|------------|------------|--------------|----------------|--------------------|------------------|
| ProcurementOrderNotFoundException | com.inventory.procurementservice.exception.ProcurementOrderNotFoundException | 多处 | PRO-03-001 | 从异常消息中提取采购订单ID | 统一使用BusinessException，添加上下文信息 |

### 4.6 客户服务异常迁移

| 原异常类名 | 全限定类名 | 出现文件路径 | 替换后的错误码 | 上下文信息提取方式 | 处理逻辑变更说明 |
|------------|------------|--------------|----------------|--------------------|------------------|
| CustomerNotFoundException | com.inventory.customerservice.exception.CustomerNotFoundException | 多处 | CUST-03-001 | 从异常消息中提取客户ID | 统一使用BusinessException，添加上下文信息 |

### 4.7 供应商服务异常迁移

| 原异常类名 | 全限定类名 | 出现文件路径 | 替换后的错误码 | 上下文信息提取方式 | 处理逻辑变更说明 |
|------------|------------|--------------|----------------|--------------------|------------------|
| SupplierNotFoundException | com.inventory.supplierservice.exception.SupplierNotFoundException | 多处 | SUPP-03-001 | 从异常消息中提取供应商ID | 统一使用BusinessException，添加上下文信息 |

### 4.8 业务伙伴服务异常迁移

| 原异常类名 | 全限定类名 | 出现文件路径 | 替换后的错误码 | 上下文信息提取方式 | 处理逻辑变更说明 |
|------------|------------|--------------|----------------|--------------------|------------------|
| BusinessPartnerException | com.inventory.businesspartnerservice.exception.BusinessPartnerException | 多处 | BP-02-001 | 从异常消息中提取业务伙伴ID | 统一使用BusinessException，添加上下文信息 |

## 5. 错误码使用最佳实践

### 5.1 错误码创建原则

1. **唯一性**：每个错误场景必须对应唯一的错误码
2. **可读性**：错误码应易于理解和记忆
3. **可扩展性**：预留足够的序号空间，便于未来扩展
4. **一致性**：相同业务场景使用相同的错误码

### 5.2 异常抛出规范

1. **明确错误信息**：错误信息应包含足够的上下文，便于定位问题
2. **添加错误码**：所有BusinessException必须包含错误码
3. **添加上下文**：根据需要添加相关上下文信息
4. **使用错误码常量**：使用预定义的错误码常量，避免硬编码

### 5.3 示例代码

```java
// 错误示例
throw new BusinessException("产品不存在");

// 正确示例
Map<String, Object> context = new HashMap<>();
context.put("productId", productId);
throw new BusinessException("产品不存在，ID: " + productId, ProductErrorCodeConstants.PRODUCT_NOT_FOUND, context);

// 更简洁的示例
throw new BusinessException("产品不存在，ID: " + productId, ProductErrorCodeConstants.PRODUCT_NOT_FOUND)
        .putContext("productId", productId);
```

## 6. 文档更新记录

| 更新时间 | 更新内容 | 更新人 | 版本号 |
|----------|----------|--------|--------|
| 2026-01-13 | 初始创建 | System | 1.0 |
