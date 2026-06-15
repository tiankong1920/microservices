# 进销存管理系统设计文档

## 1. 文档概述

### 1.1 文档目的
本文档详细描述了进销存管理系统的设计方案，包括系统架构、数据库设计、API设计等，作为系统开发和维护的指导文档。

### 1.2 文档范围
本文档涵盖了进销存管理系统的以下设计内容：
- 系统架构设计
- 技术栈选型
- 数据库设计
- API设计
- 安全设计
- 性能设计

### 1.3 术语定义
| 术语 | 解释 |
|------|------|
| 微服务 | 一种软件架构风格，将应用程序拆分为多个小型、独立的服务 |
| 网关 | 微服务架构中的入口点，负责路由、负载均衡、认证授权等 |
| Eureka | Spring Cloud 生态中的服务注册与发现组件 |
| JPA | Java Persistence API，用于对象关系映射 |
| RESTful | 一种基于HTTP的API设计风格 |

## 2. 系统架构设计

### 2.1 架构风格
采用微服务架构，将系统拆分为多个独立的服务，每个服务负责特定的业务功能。

### 2.2 系统组件

#### 2.2.1 核心服务组件

| 服务名称 | 服务描述 | 主要功能 |
|----------|----------|----------|
| admin-service | 管理服务 | 系统管理、用户管理、权限管理 |
| product-service | 产品服务 | 产品信息管理、SKU管理、BOM管理 |
| inventory-service | 库存服务 | 库存管理、库存预警、库存调拨 |
| order-service | 订单服务 | 销售订单管理、采购订单管理 |
| sales-service | 销售服务 | 销售管理、客户管理、促销管理 |
| finance-service | 财务服务 | 财务管理、成本核算、利润分析 |
| procurement-service | 采购服务 | 采购管理、供应商管理 |
| customer-service | 客户服务 | 客户信息管理、客户分析 |
| supplier-service | 供应商服务 | 供应商信息管理、供应商评估 |

#### 2.2.2 基础设施组件

| 组件名称 | 组件描述 | 技术选型 |
|----------|----------|----------|
| 服务注册中心 | 服务注册与发现 | Nacos |
| 配置中心 | 集中配置管理 | Nacos Config |
| API网关 | 服务入口、路由转发 | Spring Cloud Gateway |
| 消息队列 | 异步通信 | RabbitMQ |
| 分布式锁 | 分布式环境下的锁机制 | Redis |
| 分布式事务 | 分布式环境下的事务管理 | Seata |
| 缓存 | 数据缓存 | Redis |
| 日志中心 | 集中日志管理 | ELK Stack |
| 监控中心 | 系统监控 | Prometheus + Grafana |
| 追踪系统 | 分布式追踪 | Zipkin |

### 2.3 系统架构图

```
┌───────────────────────────────────────────────────────────────────────────┐
│                               客户端层                                      │
├─────────────────────┬─────────────────────┬─────────────────────┐         │
│     Web浏览器       │     移动应用        │     第三方系统      │         │
└─────────────────────┴─────────────────────┴─────────────────────┘         │
                                │                                          │
                                ▼                                          │
┌───────────────────────────────────────────────────────────────────────────┐
│                               API网关层                                    │
│                              Spring Cloud Gateway                        │
└───────────────────────────────────────────────────────────────────────────┘
                                │                                          │
                                ▼                                          │
┌───────────────────────────────────────────────────────────────────────────┐
│                               服务注册中心                                 │
│                               Alibaba Nacos                              │
└───────────────────────────────────────────────────────────────────────────┘
                                │                                          │
        ┌───────────────────────┴───────────────────────┐                  │
        │                                               │                  │
        ▼                                               ▼                  │
┌─────────────────┐                           ┌─────────────────┐          │
│   业务服务层    │                           │   业务服务层    │          │
├─────────────────┤                           ├─────────────────┤          │
│ admin-service   │                           │ sales-service   │          │
│ product-service │                           │ finance-service │          │
│ inventory-service│                          │ procurement-service│        │
│ order-service   │                           │ customer-service│          │
│                 │                           │ supplier-service│          │
└─────────────────┘                           └─────────────────┘          │
        │                                               │                  │
        └───────────────────────┬───────────────────────┘                  │
                                ▼                                          │
┌───────────────────────────────────────────────────────────────────────────┐
│                               数据层                                      │
├─────────────────────┬─────────────────────┬─────────────────────┐         │
│    PostgreSQL       │      Redis          │      RabbitMQ       │         │
└─────────────────────┴─────────────────────┴─────────────────────┘         │
```

### 2.4 数据流设计

#### 2.4.1 采购数据流
1. 采购服务创建采购订单
2. 采购服务调用库存服务进行库存预留
3. 库存服务更新库存数据
4. 采购服务调用财务服务处理发票和付款

#### 2.4.2 销售数据流
1. 销售服务创建销售订单
2. 销售服务调用库存服务进行库存分配
3. 库存服务更新库存数据
4. 销售服务调用财务服务处理发票和收款

#### 2.4.3 生产数据流
1. 生产服务创建生产订单
2. 生产服务调用产品服务获取BOM信息
3. 生产服务调用库存服务进行原材料出库
4. 生产服务更新生产进度
5. 生产完成后，生产服务调用库存服务进行成品入库

## 3. 技术栈选型

### 3.1 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.4.3 | 应用框架 |
| Spring Cloud | 2024.0.0 | 微服务框架 |
| Spring Data JPA | 3.4.3 | ORM框架 |
| PostgreSQL | 16 | 主数据库 |
| Redis | 7.0 | 缓存、分布式锁 |
| RabbitMQ | 3.13 | 消息队列 |
| Eureka | 3.0 | 服务注册与发现 |
| Spring Cloud Gateway | 4.1.0 | API网关 |
| Spring Cloud Config | 4.1.0 | 配置中心 |
| Lombok | 1.18.42 | 代码简化工具 |
| JWT | 0.12.6 | 身份验证 |

### 3.2 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| React | 18 | 前端框架 |
| Vite | 5.0 | 构建工具 |
| Ant Design | 5.0 | UI组件库 |
| Axios | 1.6 | HTTP客户端 |
| React Router | 6.21 | 路由管理 |
| Redux Toolkit | 2.1 | 状态管理 |
| ECharts | 5.5 | 数据可视化 |

## 4. 数据库设计

### 4.1 数据库模型概述

系统采用关系型数据库（PostgreSQL）存储核心业务数据，包括：
- 用户数据
- 产品数据
- 库存数据
- 订单数据
- 财务数据

### 4.2 核心实体关系图

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│  产品表     │       │  SKU表      │       │  BOM表      │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ product_id  │───┐   │ sku_id      │       │ bom_id      │
│ product_name│   │   │ product_id  │       │ product_id  │
│ category_id │   └───│ sku_code    │       │ component_id│
│ description │       │ sku_name    │       │ quantity    │
└─────────────┘       └─────────────┘       └─────────────┘

┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│  库存表     │       │  批次表     │       │  仓库表     │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ inventory_id│───┐   │ batch_id    │       │ warehouse_id│
│ product_id  │   │   │ batch_number│       │ warehouse_name│
│ sku_id      │   └───│ product_id  │       │ location    │
│ warehouse_id│       │ production_date│     │ description │
│ quantity    │       │ expiry_date │       └─────────────┘
└─────────────┘       └─────────────┘

┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│  订单表     │       │  订单明细表 │       │  客户表     │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ order_id    │───┐   │ order_item_id│       │ customer_id │
│ order_number│   └───│ order_id    │       │ customer_name│
│ customer_id │       │ product_id  │       │ contact_info│
│ order_date  │       │ sku_id      │       │ credit_rating│
│ status      │       │ quantity    │       └─────────────┘
└─────────────┘       └─────────────┘
```

### 4.3 核心表结构

#### 4.3.1 产品表（products）

| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| product_id | BIGSERIAL | PRIMARY KEY | 产品ID |
| product_code | VARCHAR(100) | UNIQUE NOT NULL | 产品编码 |
| product_name | VARCHAR(200) | NOT NULL | 产品名称 |
| category_id | BIGINT | FOREIGN KEY | 分类ID |
| description | TEXT | | 产品描述 |
| unit | VARCHAR(20) | NOT NULL | 计量单位 |
| created_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | 更新时间 |
| deleted | BOOLEAN | NOT NULL DEFAULT FALSE | 是否删除 |

#### 4.3.2 SKU表（product_skus）

| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| sku_id | BIGSERIAL | PRIMARY KEY | SKU ID |
| product_id | BIGINT | FOREIGN KEY NOT NULL | 产品ID |
| sku_code | VARCHAR(100) | UNIQUE NOT NULL | SKU编码 |
| sku_name | VARCHAR(200) | NOT NULL | SKU名称 |
| specification | VARCHAR(200) | | 规格 |
| color | VARCHAR(50) | | 颜色 |
| size | VARCHAR(50) | | 尺寸 |
| weight | DECIMAL(10,2) | | 重量 |
| created_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | 更新时间 |
| deleted | BOOLEAN | NOT NULL DEFAULT FALSE | 是否删除 |

#### 4.3.3 库存表（inventories）

| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| inventory_id | BIGSERIAL | PRIMARY KEY | 库存ID |
| product_id | BIGINT | FOREIGN KEY NOT NULL | 产品ID |
| sku_id | BIGINT | FOREIGN KEY | SKU ID |
| warehouse_id | BIGINT | FOREIGN KEY NOT NULL | 仓库ID |
| batch_id | BIGINT | FOREIGN KEY | 批次ID |
| quantity | INTEGER | NOT NULL DEFAULT 0 | 库存数量 |
| reserved_quantity | INTEGER | NOT NULL DEFAULT 0 | 预留数量 |
| available_quantity | INTEGER | NOT NULL DEFAULT 0 | 可用数量 |
| unit_cost | DECIMAL(15,2) | | 单位成本 |
| total_cost | DECIMAL(15,2) | | 总成本 |
| created_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | 更新时间 |
| deleted | BOOLEAN | NOT NULL DEFAULT FALSE | 是否删除 |

#### 4.3.4 订单表（orders）

| 字段名 | 数据类型 | 约束 | 描述 |
|--------|----------|------|------|
| order_id | BIGSERIAL | PRIMARY KEY | 订单ID |
| order_number | VARCHAR(50) | UNIQUE NOT NULL | 订单编号 |
| customer_id | BIGINT | FOREIGN KEY | 客户ID |
| supplier_id | BIGINT | FOREIGN KEY | 供应商ID |
| order_type | VARCHAR(20) | NOT NULL | 订单类型（销售/采购） |
| order_date | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | 订单日期 |
| status | VARCHAR(20) | NOT NULL | 订单状态 |
| total_amount | DECIMAL(15,2) | NOT NULL DEFAULT 0 | 订单总金额 |
| total_quantity | INTEGER | NOT NULL DEFAULT 0 | 订单总数量 |
| created_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL DEFAULT CURRENT_TIMESTAMP | 更新时间 |
| deleted | BOOLEAN | NOT NULL DEFAULT FALSE | 是否删除 |

## 5. API设计

### 5.1 API设计原则

- 采用 RESTful API 设计风格
- 使用 HTTP 方法表示操作类型（GET/POST/PUT/DELETE）
- 使用 JSON 作为数据交换格式
- 统一的错误处理机制
- 完整的 API 文档（使用 Swagger/OpenAPI）

### 5.2 API 版本管理

API 版本通过 URL 路径进行管理，例如：
```
/api/v1/products
/api/v1/inventory
/api/v1/orders
```

### 5.3 核心 API 设计

#### 5.3.1 产品管理 API

| API路径 | 方法 | 描述 | 请求体 | 响应体 |
|---------|------|------|--------|--------|
| /api/v1/products | GET | 获取产品列表 | - | 产品列表 |
| /api/v1/products | POST | 创建产品 | 产品信息 | 产品信息 |
| /api/v1/products/{id} | GET | 获取产品详情 | - | 产品信息 |
| /api/v1/products/{id} | PUT | 更新产品 | 产品信息 | 产品信息 |
| /api/v1/products/{id} | DELETE | 删除产品 | - | - |

#### 5.3.2 库存管理 API

| API路径 | 方法 | 描述 | 请求体 | 响应体 |
|---------|------|------|--------|--------|
| /api/v1/inventory | GET | 查询库存 | - | 库存列表 |
| /api/v1/inventory | POST | 调整库存 | 库存调整信息 | 库存信息 |
| /api/v1/inventory/{id} | GET | 获取库存详情 | - | 库存信息 |
| /api/v1/inventory/alert | GET | 获取库存预警 | - | 预警列表 |
| /api/v1/inventory/transfer | POST | 库存调拨 | 调拨信息 | 调拨结果 |

#### 5.3.3 订单管理 API

| API路径 | 方法 | 描述 | 请求体 | 响应体 |
|---------|------|------|--------|--------|
| /api/v1/orders | GET | 获取订单列表 | - | 订单列表 |
| /api/v1/orders | POST | 创建订单 | 订单信息 | 订单信息 |
| /api/v1/orders/{id} | GET | 获取订单详情 | - | 订单信息 |
| /api/v1/orders/{id} | PUT | 更新订单 | 订单信息 | 订单信息 |
| /api/v1/orders/{id}/status | PUT | 更新订单状态 | 状态信息 | 订单信息 |

#### 5.3.4 报表管理 API

| API路径 | 方法 | 描述 | 请求体 | 响应体 |
|---------|------|------|--------|--------|
| /api/v1/reports/sales | GET | 获取销售报表 | - | 销售报表数据 |
| /api/v1/reports/inventory | GET | 获取库存报表 | - | 库存报表数据 |
| /api/v1/reports/cost | GET | 获取成本报表 | - | 成本报表数据 |
| /api/v1/reports/production | GET | 获取生产报表 | - | 生产报表数据 |

## 6. 安全设计

### 6.1 认证与授权

- 采用基于 JWT 的身份验证机制
- 实现基于角色的访问控制（RBAC）
- 支持多因素认证
- 定期刷新令牌

### 6.2 数据安全

- 敏感数据加密存储（如密码、银行卡号等）
- 数据传输采用 HTTPS
- 定期数据备份
- 数据恢复机制

### 6.3 访问控制

- 实现 API 网关的访问控制
- 限制 API 调用频率
- 防止 SQL 注入、XSS 攻击等
- 实现 CSRF 防护

### 6.4 审计日志

- 记录所有敏感操作
- 记录操作时间、操作人、操作内容等
- 日志不可篡改
- 定期审计日志

## 7. 性能设计

### 7.1 缓存设计

- 采用 Redis 作为缓存层
- 缓存热点数据（如产品信息、库存数据）
- 实现缓存过期机制
- 实现缓存一致性策略

### 7.2 数据库优化

- 合理设计索引
- 分表分库策略
- 数据库连接池优化
- 定期数据库优化

### 7.3 并发设计

- 采用乐观锁和悲观锁解决并发问题
- 实现分布式锁
- 异步处理耗时操作
- 合理设计事务范围

### 7.4 负载均衡

- 实现服务级别的负载均衡
- 实现网关级别的负载均衡
- 支持动态扩缩容

## 8. 部署设计

### 8.1 部署架构

- 采用 Docker 容器化部署
- 使用 Kubernetes 进行容器编排
- 实现自动化部署流程
- 支持蓝绿部署、金丝雀部署

### 8.2 环境管理

| 环境 | 用途 | 特点 |
|------|------|------|
| 开发环境 | 开发人员调试 | 单节点部署，数据模拟 |
| 测试环境 | 测试人员测试 | 多节点部署，模拟生产环境 |
| 预发环境 | 上线前验证 | 与生产环境配置一致 |
| 生产环境 | 正式运行 | 高可用部署，严格监控 |

### 8.3 部署流程

1. 代码提交到 Git 仓库
2. CI/CD 流水线自动构建镜像
3. 自动化测试
4. 镜像推送至镜像仓库
5. Kubernetes 部署新版本
6. 健康检查与监控

## 9. 监控与维护设计

### 9.1 监控设计

- 系统监控：CPU、内存、磁盘、网络等
- 应用监控：请求量、响应时间、错误率等
- 数据库监控：查询性能、连接数、慢查询等
- 日志监控：错误日志、警告日志、审计日志等

### 9.2 告警设计

- 基于阈值的告警机制
- 多种告警渠道（邮件、短信、钉钉、微信等）
- 告警级别划分（紧急、重要、警告、信息）
- 告警自愈机制

### 9.3 维护设计

- 定期备份数据
- 定期更新系统和依赖
- 定期进行安全扫描
- 制定故障应急预案
- 建立完善的运维文档

## 10. 附录

### 10.1 参考文档

- 《Spring Cloud 微服务架构设计》
- 《RESTful API 设计最佳实践》
- 《数据库设计原理》
- 《系统架构设计模式》

### 10.2 变更记录

| 版本 | 变更日期 | 变更内容 | 变更人 |
|------|----------|----------|--------|
| 1.0 | 2025-12-05 | 初始版本 | 系统管理员 |

### 10.3 审批记录

| 审批人 | 审批日期 | 审批意见 |
|--------|----------|----------|
| 技术负责人 | 2025-12-05 | 同意 |
| 架构师 | 2025-12-05 | 同意 |
| 项目经理 | 2025-12-05 | 同意 |
