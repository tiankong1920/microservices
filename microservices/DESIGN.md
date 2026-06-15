# 库存管理系统 - 系统架构设计文档

> **版本**: 1.0
> **日期**: 2026-05-03
> **状态**: DRAFT
> **维护者**: Sisyphus

---

## 目录

1. [系统概述](#1-系统概述)
2. [架构风格](#2-架构风格)
3. [服务拓扑](#3-服务拓扑)
4. [技术栈](#4-技术栈)
5. [数据架构](#5-数据架构)
6. [服务间通信](#6-服务间通信)
7. [安全架构](#7-安全架构)
8. [部署架构](#8-部署架构)
9. [监控与可观测性](#9-监控与可观测性)
10. [跨服务业务流程](#10-跨服务业务流程)
11. [设计决策与权衡](#11-设计决策与权衡)
12. [待解决问题](#12-待解决问题)

---

## 1. 系统概述

库存管理系统是一套面向企业的微服务架构解决方案，覆盖商品、订单、库存、销售、采购、客户、供应商等核心业务领域。系统采用 **Spring Cloud Alibaba** 技术栈，通过 **Nacos** 实现服务发现与配置管理，**PostgreSQL** 作为主数据存储，**Redis** 提供缓存与限流能力。

### 1.1 业务边界

| 领域 | 服务 | 端口 | 核心职责 |
|------|------|------|----------|
| 商品管理 | product-service | 8081 | 商品 CRUD、SKU 管理、分类 |
| 订单管理 | order-service | 8082 | 订单生命周期、状态机 |
| 库存管理 | inventory-service | 8083 | 库存数量、出入库、预警 |
| 销售管理 | sales-service | 8084 | 销售订单、退货 |
| 采购管理 | procurement-service | 8085 | 采购单、供应商采购 |
| 客户管理 | customer-service | 8086 | 客户信息、信用 |
| 供应商管理 | supplier-service | 8087 | 供应商信息、评估 |
| 合作伙伴 | business-partner-service | 8088 | 统一合作伙伴模型 |
| 认证授权 | auth-service | 8093 | JWT 认证、权限管理 |
| 管理后台 | admin-service | 8091 | 系统管理、用户管理 |
| 财务管理 | finance-service | 8092 | 应收应付、结算 |
| API 网关 | gateway-service | 8080 | 路由、限流、鉴权 |
| 配置中心 | config-service | 8888 | 动态配置 |
| 注册中心 | registry-service | 8761 | 服务注册（Nacos 包装） |
| 报表服务 | report-service | 8094 | 统计报表、数据聚合 |

### 1.2 非功能性需求

| 指标 | 目标 |
|------|------|
| 可用性 | 99.9% |
| 响应时间（P95） | < 500ms |
| 并发支持 | 1000+ TPS（网关层） |
| 数据一致性 | 最终一致性（跨服务） |
| 部署频率 | 按需独立部署 |

---

## 2. 架构风格

### 2.1 微服务架构

系统采用 **领域驱动设计（DDD）** 指导的微服务拆分策略，每个服务：

- 拥有独立的数据库 Schema
- 通过 API 网关统一对外暴露
- 独立部署、独立扩展
- 通过事件实现跨服务数据同步

### 2.2 分层架构（服务内部）

```
┌─────────────────────────────────┐
│         Controller 层            │  ← REST API, 请求验证, OpenAPI 注解
├─────────────────────────────────┤
│         Service 层               │  ← 业务逻辑, 事务管理
│         └── impl/                │  ← 服务实现
├─────────────────────────────────┤
│         Repository 层            │  ← JPA 数据访问, I 前缀接口
├─────────────────────────────────┤
│         Entity 层                │  ← JPA 实体, 数据库映射
└─────────────────────────────────┘
     ↕ DTO 层（跨层数据传输）
```

### 2.3 公共模块

`common/` 模块提供跨服务共享代码：

- `BaseApplicationException` - 统一异常基类
- `GlobalExceptionHandler` - 全局异常处理
- 通用 DTO、枚举、工具类
- 统一响应格式

**约束**: common 模块不得包含业务逻辑，仅包含基础设施代码。

---

## 3. 服务拓扑

```
                    ┌──────────────┐
                    │   客户端      │
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │ gateway-     │  ← 路由、限流、JWT 验证
                    │ service:8080 │
                    └──────┬───────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
    ┌────▼────┐      ┌────▼────┐      ┌─────▼─────┐
    │ 核心服务 │      │ 支撑服务 │      │  监控服务  │
    │         │      │         │      │           │
    │product  │      │auth     │      │Prometheus │
    │order    │      │admin    │      │Grafana    │
    │inventory│      │finance  │      │           │
    │sales    │      │config   │      │           │
    │procurement│    │registry │      │           │
    │customer │      │report   │      │           │
    │supplier │      │         │      │           │
    │biz-partner│    │         │      │           │
    └────┬────┘      └────┬────┘      └───────────┘
         │                 │
    ┌────▼─────────────────▼────┐
    │     Nacos:8848            │  ← 服务发现 + 配置中心
    │     PostgreSQL:5432       │  ← 主数据库
    │     Redis:6379            │  ← 缓存 + 限流
    └───────────────────────────┘
```

---

## 4. 技术栈

| 层级 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 运行时 | Java | 21 | 基础运行时 |
| 框架 | Spring Boot | 3.4.4 | 应用框架 |
| 云服务 | Spring Cloud | 2024.0.2 | 微服务基础设施 |
| 阿里云 | Spring Cloud Alibaba | 2023.0.1.0 | Nacos 集成 |
| 构建 | Gradle | 9.4.0 (Kotlin DSL) | 构建系统 |
| 数据库 | PostgreSQL | 16+ | 主存储 |
| 缓存 | Redis | 7.4+ | 缓存/限流/会话 |
| 测试 | JUnit 5, Mockito | - | 单元测试 |
| 测试 | TestContainers | - | 集成测试 |
| 质量 | Checkstyle, PMD, SpotBugs | - | 代码质量 |
| 覆盖率 | JaCoCo | - | 代码覆盖率（行≥80%, 分支≥70%） |
| 监控 | Prometheus + Grafana | - | 指标收集与可视化 |
| 容器 | Docker + Docker Compose | 24+ | 容器化部署 |
| CI/CD | Jenkins | - | 持续集成 |

---

## 5. 数据架构

### 5.1 数据库策略

- **每服务独立 Schema**: 每个微服务拥有独立的 PostgreSQL Schema，禁止跨服务直接查询数据库
- **H2 测试数据库**: 单元测试使用 H2 内存数据库，集成测试使用 TestContainers + PostgreSQL
- **JPA/Hibernate**: 使用 Spring Data JPA 作为 ORM 框架

### 5.2 数据一致性

| 场景 | 策略 | 说明 |
|------|------|------|
| 服务内部 | ACID 事务 | `@Transactional` 保证 |
| 跨服务写操作 | 最终一致性 | 通过事件/消息队列 |
| 跨服务读操作 | API 调用 / 数据冗余 | 通过 Feign 客户端或本地缓存 |
| 库存扣减 | 乐观锁 | `@Version` 字段防止超卖 |

### 5.3 缓存策略

| 缓存类型 | 存储 | 过期策略 |
|----------|------|----------|
| 热点数据 | Redis | TTL 30 分钟 |
| 会话数据 | Redis | TTL 与 JWT 一致 |
| 限流计数 | Redis | 滑动窗口，1 分钟 |
| 查询结果 | 本地缓存 (Caffeine) | TTL 5 分钟 |

---

## 6. 服务间通信

### 6.1 同步通信

- **OpenFeign**: 服务间 REST 调用，通过 Feign Client 接口定义
- **负载均衡**: Spring Cloud LoadBalancer
- **超时配置**: 连接超时 3s，读取超时 5s
- **重试**: 对幂等 GET 请求启用重试（最多 2 次）

### 6.2 异步通信（预留）

- **消息队列**: 预留 RabbitMQ/Kafka 集成点，用于事件驱动场景
- **事件类型**: 订单创建、库存变更、采购入库等

### 6.3 跨服务调用示例

```
order-service → product-service:  查询商品信息
order-service → inventory-service: 检查库存
order-service → customer-service:  验证客户信用
sales-service → order-service:     创建销售订单
procurement-service → supplier-service: 查询供应商信息
```

**约束**: 禁止循环依赖。如 A→B→C→A，需引入事件解耦。

---

## 7. 安全架构

### 7.1 认证与授权

| 层级 | 机制 | 说明 |
|------|------|------|
| 网关层 | JWT 验证 | gateway-service 统一验证 Token |
| 服务层 | Spring Security | 角色/权限校验 |
| 数据层 | Schema 隔离 | 每服务独立数据库用户 |

### 7.2 JWT 设计

- **算法**: HS256
- **密钥**: 环境变量 `JWT_SECRET`（≥256 位）
- **有效期**: Access Token 30 分钟，Refresh Token 7 天
- **载荷**: userId, roles, permissions, exp

### 7.3 错误码规范

格式: `AAA-XX-XXX`

| 前缀 | 含义 | 示例 |
|------|------|------|
| SYS | 系统错误 | SYS-ERR-001 |
| VAL | 验证错误 | VAL-REQ-001 |
| BIZ | 业务错误 | BIZ-INV-001 |
| AUTH | 认证/授权 | AUTH-TKN-001 |

### 7.4 安全防护

- SQL 注入防护: JPA 参数化查询
- XSS 防护: 输入验证 + 输出编码
- CSRF: 无状态 API，不使用 Session
- 限流: Redis + Redisson 网关层限流

---

## 8. 部署架构

### 8.1 环境划分

| 环境 | 用途 | 配置文件 |
|------|------|----------|
| dev | 本地开发 | `application-dev.yml` + `.env` |
| test | 集成测试 | `application-test.yml` + `gradle-test.properties` |
| prod | 生产环境 | Nacos 动态配置 + `gradle-prod.properties` |

### 8.2 Docker Compose 编排

| 文件 | 用途 |
|------|------|
| `docker-compose.yml` | 基础开发环境 |
| `docker-compose.dev.yml` | 开发环境覆盖 |
| `docker-compose.postgres.yml` | 独立数据库配置 |
| `docker-compose.monitoring.yml` | Prometheus + Grafana |
| `docker-compose.prod.yml` | 生产环境配置 |

### 8.3 服务启动顺序

```
1. PostgreSQL + Redis (基础设施)
2. Nacos (注册/配置中心)
3. gateway-service (网关)
4. auth-service (认证)
5. 核心业务服务 (可并行)
6. 支撑服务 (可并行)
7. 监控服务 (可选)
```

### 8.4 健康检查

所有服务暴露 `/actuator/health` 端点，网关定期检查服务健康状态。

---

## 9. 监控与可观测性

### 9.1 三大支柱

| 支柱 | 工具 | 用途 |
|------|------|------|
| Metrics | Prometheus | 指标收集（QPS、延迟、错误率） |
| Tracing | (预留) | 分布式链路追踪 |
| Logging | Logback + ELK (预留) | 结构化日志 |

### 9.2 关键指标

- JVM: 内存使用、GC 次数、线程数
- HTTP: 请求量、响应时间、错误率
- 业务: 订单量、库存预警数、活跃用户数

### 9.3 告警规则（预留）

- 服务下线: 超过 30s 未上报心跳
- 错误率 > 5%: 5 分钟内 HTTP 5xx 比例
- 响应时间 P95 > 1s: 持续 5 分钟
- 磁盘使用 > 80%

---

## 10. 跨服务业务流程

### 10.1 订单履约流程

```
客户下单 → order-service 创建订单
         → inventory-service 扣减库存
         → finance-service 生成应收
         → sales-service 记录销售
         → 完成
```

### 10.2 采购入库流程

```
创建采购单 → procurement-service
           → supplier-service 确认供应商
           → inventory-service 增加库存
           → finance-service 生成应付
           → 完成
```

### 10.3 库存预警流程

```
inventory-service 监控库存水位
    → 低于阈值 → 触发预警
    → 可选: 自动生成采购建议
    → 通知 admin-service
```

---

## 11. 设计决策与权衡

### 11.1 为什么选择 Nacos 而非 Eureka + Config Server？

| 维度 | Nacos | Eureka + Config |
|------|-------|-----------------|
| 功能 | 服务发现 + 配置一体 | 需要两个组件 |
| 维护 | 单一组件 | 两个组件 |
| 社区 | 阿里云支持 | Netflix 已停更 |
| 决策 | ✅ 采用 | |

### 11.2 为什么选择 PostgreSQL 而非 MySQL？

| 维度 | PostgreSQL | MySQL |
|------|-----------|-------|
| 事务 | 更严格的 ACID | 依赖存储引擎 |
| JSON | 原生 JSONB | JSON 类型较新 |
| 并发 | MVCC 更成熟 | 行锁实现不同 |
| 决策 | ✅ 采用 | |

### 11.3 为什么使用 JaCoCo 而非 Cobertura？

- JaCoCo 是 Gradle 原生支持
- 与 SonarQube 集成更好
- 支持 Java 21

### 11.4 代码质量工具选择

| 工具 | 用途 | 阈值 |
|------|------|------|
| Checkstyle | 代码风格 | 行长度 ≤ 120 |
| PMD | 代码缺陷 | 无 Critical 以上问题 |
| SpotBugs | 字节码分析 | 无 High 以上问题 |
| JaCoCo | 覆盖率 | 行 ≥ 80%, 分支 ≥ 70% |

---

## 12. 待解决问题

| 编号 | 问题 | 优先级 | 状态 |
|------|------|--------|------|
| TBD-001 | 跨服务分布式链路追踪方案选型 | 中 | 待评估 |
| TBD-002 | 日志聚合方案（ELK vs Loki） | 中 | 待评估 |
| TBD-003 | 消息队列选型（RabbitMQ vs Kafka） | 中 | 待评估 |
| TBD-004 | 灰度发布策略 | 低 | 待规划 |
| TBD-005 | 数据库迁移工具（Flyway vs Liquibase） | 中 | 待评估 |

---

## 附录

### A. 术语表

| 术语 | 含义 |
|------|------|
| SKU | Stock Keeping Unit，库存量单位 |
| BOM | Bill of Materials，物料清单 |
| TPS | Transactions Per Second |
| P95 | 95 百分位响应时间 |
| ACID | 原子性、一致性、隔离性、持久性 |

### B. 参考文档

- [环境要求](docs/deployment/ENVIRONMENT_REQUIREMENTS.md)
- [服务启动指南](docs/deployment/SERVICE_STARTUP_GUIDE.md)
- [配置最佳实践](docs/deployment/CONFIGURATION_BEST_PRACTICES.md)
- [故障排查指南](docs/deployment/TROUBLESHOOTING_GUIDE.md)

### C. 文档变更历史

| 日期 | 版本 | 变更内容 | 作者 |
|------|------|----------|------|
| 2026-05-03 | 1.0 | 初始版本 | Sisyphus |
