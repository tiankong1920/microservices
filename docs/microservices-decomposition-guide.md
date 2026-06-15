# 微服务拆分指导手册

本文档提供了进销存管理系统从业务单体应用向微服务架构拆分的详细指导，包括服务边界划分、数据管理策略和服务间通信机制。

## 1. 服务拆分原则

### 1.1 业务边界划分
- 每个微服务负责一个明确的业务领域
- 服务间通过API进行通信，避免功能重叠
- 遵循单一职责原则，确保服务职责清晰

### 1.2 数据所有权
- 每个微服务拥有独立的数据库模式
- 通过API访问其他服务的数据
- 使用事件驱动实现服务间数据同步

### 1.3 独立部署
- 每个微服务可以独立开发、测试、部署和扩展
- 服务间通过网络调用进行通信
- 支持不同技术栈的选择

## 2. 业务服务识别

### 2.1 核心业务服务

#### 商品服务 (Product Service)
**职责范围**:
- 商品信息管理（增删改查）
- 商品分类管理
- 商品规格和属性管理
- 商品批次和保质期管理

**核心实体**:
- Product（商品）
- ProductCategory（商品分类）
- ProductSpecification（商品规格）

#### 库存服务 (Inventory Service)
**职责范围**:
- 库存数量管理
- 多仓库管理
- 库存预警和安全库存
- 库存调拨和盘点

**核心实体**:
- Inventory（库存）
- Warehouse（仓库）
- StockMovement（库存变动）

#### 采购服务 (Purchase Service)
**职责范围**:
- 供应商管理
- 采购订单管理
- 采购入库管理
- 供应商对账

**核心实体**:
- Supplier（供应商）
- PurchaseOrder（采购订单）
- PurchaseOrderItem（采购订单明细）

#### 销售服务 (Sales Service)
**职责范围**:
- 客户管理
- 销售订单管理
- 销售出库管理
- 客户对账

**核心实体**:
- Customer（客户）
- SalesOrder（销售订单）
- SalesOrderItem（销售订单明细）

#### 财务服务 (Finance Service)
**职责范围**:
- 应收应付管理
- 收支统计
- 财务报表生成
- 发票管理

**核心实体**:
- Receivable（应收账款）
- Payable（应付账款）
- Payment（收付款）
- Invoice（发票）

#### 报表服务 (Report Service)
**职责范围**:
- 销售报表生成
- 库存周转报表
- 客户行为分析
- 供应商分析

**核心实体**:
- SalesReport（销售报表）
- InventoryTurnoverReport（库存周转报表）
- CustomerBehaviorReport（客户行为报表）

#### 用户服务 (User Service)
**职责范围**:
- 用户管理
- 角色权限管理
- 认证授权
- 系统配置

**核心实体**:
- User（用户）
- Role（角色）
- Permission（权限）
- SystemConfig（系统配置）

### 2.2 基础设施服务

#### 注册中心服务 (Registry Service)
**职责范围**:
- 服务注册与发现
- 服务健康检查
- 负载均衡支持

#### 配置中心服务 (Config Service)
**职责范围**:
- 统一配置管理
- 环境特定配置
- 配置动态更新

#### API网关服务 (Gateway Service)
**职责范围**:
- 请求路由
- 负载均衡
- 认证鉴权
- 限流熔断

#### 通知服务 (Notification Service)
**职责范围**:
- 邮件通知
- 短信通知
- 系统消息
- 微信通知

## 3. 数据管理策略

### 3.1 数据库拆分
每个微服务使用独立的数据库模式，确保数据隔离：

```
product_service_db/
├── products
├── product_categories
└── product_specifications

inventory_service_db/
├── inventories
├── warehouses
└── stock_movements

purchase_service_db/
├── suppliers
├── purchase_orders
└── purchase_order_items

sales_service_db/
├── customers
├── sales_orders
└── sales_order_items

finance_service_db/
├── receivables
├── payables
└── payments

report_service_db/
├── sales_reports
├── inventory_turnover_reports
└── customer_behavior_reports

user_service_db/
├── users
├── roles
└── permissions
```

### 3.2 数据一致性
使用事件驱动架构保证最终一致性：

1. **领域事件发布**: 当业务状态发生变化时，服务发布领域事件
2. **事件订阅处理**: 其他服务订阅感兴趣的事件并更新本地数据
3. **补偿机制**: 对于关键业务，实现补偿事务处理异常情况

### 3.3 共享数据处理
对于需要共享的数据：

1. **API访问**: 通过服务API获取其他服务的数据
2. **数据复制**: 对于频繁访问的只读数据，可以复制到本地数据库
3. **缓存策略**: 使用分布式缓存提高数据访问性能

## 4. 服务间通信

### 4.1 同步通信
使用RESTful API进行同步通信：

```java
// Feign Client示例
@FeignClient(name = "inventory-service")
public interface InventoryServiceClient {
    
    @GetMapping("/api/inventories/products/{productId}")
    Inventory getInventoryByProductId(@PathVariable("productId") Long productId);
    
    @PostMapping("/api/inventories/reservations")
    Boolean reserveInventory(@RequestBody InventoryReservationRequest request);
}
```

### 4.2 异步通信
使用消息队列实现异步通信：

```java
// 消息发布示例
@Component
public class OrderEventPublisher {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend("order.created", event);
    }
}

// 消息订阅示例
@Component
public class InventoryEventListener {
    
    @RabbitListener(queues = "order.created")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // 处理库存扣减逻辑
    }
}
```

### 4.3 通信协议
- **HTTP/REST**: 用于同步请求响应场景
- **AMQP/RabbitMQ**: 用于异步消息传递
- **Apache Kafka**: 用于高吞吐量事件流处理

## 5. 服务拆分实施计划

### 5.1 第一阶段：核心服务拆分
1. **用户服务**: 独立拆分用户管理功能
2. **商品服务**: 独立拆分商品管理功能
3. **库存服务**: 独立拆分库存管理功能

### 5.2 第二阶段：业务服务拆分
1. **采购服务**: 拆分采购相关功能
2. **销售服务**: 拆分销售相关功能
3. **财务服务**: 拆分财务管理功能

### 5.3 第三阶段：支撑服务完善
1. **报表服务**: 独立拆分报表生成功能
2. **通知服务**: 独立拆分通知功能
3. **配置中心**: 完善配置管理功能

## 6. 服务拆分检查清单

### 6.1 服务设计检查
- [ ] 明确服务职责边界
- [ ] 定义服务API接口
- [ ] 设计数据模型
- [ ] 确定服务依赖关系

### 6.2 技术实现检查
- [ ] 创建独立的Maven模块
- [ ] 配置Spring Boot应用
- [ ] 实现服务注册发现
- [ ] 配置服务间通信

### 6.3 数据迁移检查
- [ ] 设计数据拆分方案
- [ ] 实现数据迁移脚本
- [ ] 验证数据一致性
- [ ] 制定回滚方案

### 6.4 测试验证检查
- [ ] 编写单元测试
- [ ] 编写集成测试
- [ ] 进行性能测试
- [ ] 验证服务间通信

## 7. 最佳实践

### 7.1 服务粒度控制
- 避免过度拆分导致服务间通信复杂化
- 避免拆分不足导致服务职责不清
- 根据团队规模和业务复杂度调整服务粒度

### 7.2 错误处理
- 实现服务降级和熔断机制
- 设计合理的超时和重试策略
- 提供详细的错误信息和日志记录

### 7.3 监控和运维
- 实现分布式链路追踪
- 建立统一的日志收集和分析
- 设置服务健康检查和告警机制

## 8. 库存预警服务扩展

### 8.1 功能扩展
在库存服务中，库存预警功能可以进一步扩展为独立的预警服务：

#### 库存预警服务 (Inventory Alert Service)
**职责范围**:
- 库存预警规则管理
- 库存预警检查和通知
- 预警历史记录和统计
- 多种预警方式支持（邮件、短信、微信等）

**核心实体**:
- AlertRule（预警规则）
- AlertNotification（预警通知）
- AlertHistory（预警历史）

### 8.2 数据模型扩展
```
inventory_alert_service_db/
├── alert_rules
├── alert_notifications
└── alert_histories
```

### 8.3 服务间通信扩展
库存预警服务需要与其他服务进行通信：

1. **与库存服务通信**: 获取库存数据进行预警检查
2. **与通知服务通信**: 发送预警通知
3. **与用户服务通信**: 获取用户信息用于通知发送

### 8.4 实施建议
1. 在库存服务中实现基础的库存预警功能
2. 随着业务发展，将库存预警功能独立为专门的服务
3. 支持更灵活的预警规则配置
4. 提供多种预警通知方式

本指导手册为进销存管理系统的微服务拆分提供了全面的规范和实施指南，所有开发团队应严格遵循这些规范进行服务拆分工作。