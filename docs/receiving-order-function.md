# 入库单功能说明

## 功能概述

入库单功能是进销存管理系统的重要组成部分，用于管理采购订单的收货和入库流程。该功能允许用户创建进货订单、确认收货、更新库存，并跟踪进货订单的状态。

## 核心功能

### 1. 创建进货订单

用户可以根据采购订单创建进货订单，系统会自动从采购订单中提取相关信息。

**API端点**: `POST /api/v1/receiving-orders/from-purchase-order/{purchaseOrderId}`

### 2. 确认收货

用户可以确认收货，系统会更新库存并更改进货订单状态。

**API端点**: `POST /api/v1/receiving-orders/receive`

**请求参数**:
- `receivingOrderId`: 进货订单ID
- `items`: 收货项列表
  - `itemId`: 进货订单项ID
  - `receivedQuantity`: 实际收货数量
  - `rejectedQuantity`: 拒收数量
  - `rejectReason`: 拒收原因
  - `notes`: 备注

### 3. 更新进货订单

用户可以更新进货订单的基本信息。

**API端点**: `PUT /api/v1/receiving-orders/{id}`

### 4. 查询进货订单

系统提供多种方式查询进货订单：
- 根据ID查询
- 根据进货订单号查询
- 根据供应商ID查询
- 根据状态查询
- 根据采购订单ID查询

## 状态管理

进货订单有以下状态：

1. **PENDING**: 待处理
2. **CONFIRMED**: 已确认
3. **PROCESSING**: 处理中
4. **PARTIALLY_RECEIVED**: 部分收货
5. **RECEIVED**: 已收货
6. **COMPLETED**: 已完成
7. **CANCELLED**: 已取消

收货状态：

1. **NOT_RECEIVED**: 未收货
2. **PARTIALLY_RECEIVED**: 部分收货
3. **FULLY_RECEIVED**: 全部收货

## 业务流程

1. 根据采购订单创建进货订单
2. 确认进货订单
3. 收货时更新库存
4. 根据收货情况更新进货订单状态

## 异常处理

系统会对以下异常情况进行处理：

1. 进货订单不存在
2. 进货订单状态不允许收货
3. 收货数量超过订购数量
4. 库存更新失败

## 事件发布

系统会在以下关键节点发布事件：

1. 进货订单创建完成
2. 收货完成

## 技术实现

### 主要类说明

1. **ReceivingOrder**: 进货订单实体类
2. **ReceivingOrderItem**: 进货订单项实体类
3. **ReceivingOrderDto**: 进货订单数据传输对象
4. **ReceivingRequest**: 收货请求数据传输对象
5. **ReceivingOrderService**: 进货订单服务接口
6. **ReceivingOrderServiceImpl**: 进货订单服务实现类
7. **ReceivingOrderController**: 进货订单控制器
8. **InventoryServiceClient**: 库存服务Feign客户端
9. **OrderServiceClient**: 订单服务Feign客户端

### 关键方法

1. `createReceivingOrderByPurchaseOrderId`: 根据采购订单ID创建进货订单
2. `receiveGoods`: 确认收货并更新库存
3. `updateReceivingOrder`: 更新进货订单
4. `getReceivingOrderById`: 根据ID获取进货订单

## 测试

系统包含以下测试：

1. **ReceivingOrderControllerTest**: 控制器层测试
2. **ReceivingOrderServiceImplTest**: 服务层测试

## 部署要求

1. Java 21+
2. Spring Boot 3.x
3. PostgreSQL数据库
4. Redis缓存
5. Eureka服务注册中心
6. Config配置中心
