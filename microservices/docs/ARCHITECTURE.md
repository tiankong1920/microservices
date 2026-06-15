# 库存管理系统 - 架构设计文档

> **版本**: 2.0
> **日期**: 2026-04-26
> **状态**: 完善中
> **维护者**: 架构团队

---

## 目录

1. [系统概述](#1-系统概述)
2. [架构风格与原则](#2-架构风格与原则)
3. [服务拓扑与通信](#3-服务拓扑与通信)
4. [技术栈全景](#4-技术栈全景)
5. [数据架构](#5-数据架构)
6. [安全架构](#6-安全架构)
7. [部署架构](#7-部署架构)
8. [监控与可观测性](#8-监控与可观测性)
9. [跨服务业务流程](#9-跨服务业务流程)
10. [设计决策录](#10-设计决策录)
11. [演进路线图](#11-演进路线图)

---

## 1. 系统概述

库存管理系统（Inventory Management System）是一套面向中小型企业的**微服务架构**解决方案，覆盖商品管理、订单履约、库存管控、销售管理、采购管理、客户与供应商管理等核心业务领域。

### 1.1 系统愿景

> **一站式库存管理平台**：打通商品、订单、库存、采购、销售、财务全链路，实现端到端的库存可视化和智能决策。

### 1.2 业务边界

| 领域 | 服务 | 端口 | 核心职责 | 数据归属 |
|------|------|:----:|----------|:--------:|
| **商品管理** | product-service | 8081 | 商品 CRUD、SKU/BOM、分类管理 | Schema: product |
| **订单管理** | order-service | 8082 | 订单生命周期、状态机、履约 | Schema: orders |
| **库存管理** | inventory-service | 8083 | 库存数量、出入库、预警、批次 | Schema: inventory |
| **销售管理** | sales-service | 8084 | 销售订单、退货、零售管理 | Schema: sales |
| **采购管理** | procurement-service | 8085 | 采购单、采购入库、退货 | Schema: procurement |
| **客户管理** | customer-service | 8086 | 客户信息、信用评级 | Schema: customer |
| **供应商管理** | supplier-service | 8087 | 供应商信息、评估考核 | Schema: supplier |
| **合作伙伴** | business-partner-service | 8088 | 统一伙伴模型 | Schema: biz_partner |
| **认证授权** | auth-service | 8093 | JWT 认证、OAuth2、权限 RBAC | Schema: auth |
| **管理后台** | admin-service | 8091 | 系统管理、用户管理、审计日志 | Schema: admin |
| **财务管理** | finance-service | 8092 | 应收应付、结算对账、收支管理 | Schema: finance |
| **API 网关** | gateway-service | 8080 | 路由、限流、鉴权、日志 | - |
| **配置中心** | config-service | 8888 | 动态配置管理（Nacos 兼容） | - |
| **注册中心** | registry-service | 8761 | 服务注册发现（Nacos 包装） | - |
| **报表服务** | report-service | 8094 | 统计报表、数据聚合、导出 | Schema: report |

### 1.3 非功能性目标

| 指标 | 当前目标 | 长期目标 |
|------|:--------:|:--------:|
| 可用性 | 99.9% | 99.99% |
| P95 响应时间 | < 500ms | < 200ms |
| 并发（网关） | 1000 TPS | 5000 TPS |
| 数据一致性 | 最终一致性 | 最终一致性 + Saga |
| 部署模式 | Docker Compose | Kubernetes |
| 恢复时间 (RTO) | < 30 分钟 | < 5 分钟 |
| 恢复点 (RPO) | < 5 分钟 | < 1 分钟 |

---

## 2. 架构风格与原则

### 2.1 架构风格

**领域驱动设计（DDD）** 指导下的 **微服务架构**：

- ✅ 每个服务对应一个 **限界上下文（Bounded Context）**
- ✅ 每个服务拥有 **独立 Schema**（物理数据库建议独立实例）
- ✅ 通过 API 网关统一对外暴露
- ✅ 独立部署、独立扩展、独立技术栈

### 2.2 核心架构原则

```
1️⃣  [[ 服务自治 ]] → 每个服务可独立开发、测试、部署
2️⃣  [[ 数据主权 ]] → 服务只能访问自己的数据库
3️⃣  [[ 契约优先 ]] → 服务间通过 API 契约通信
4️⃣  [[ 弹性设计 ]] → 默认熔断、限流、重试、降级
5️⃣  [[ 可观测 ]]   → 每个服务必须暴露健康检查和指标
6️⃣  [[ 无共享 ]]   → 禁止共享数据库、共享缓存、共享文件系统
```

### 2.3 服务内部分层

```
┌──────────────────────────────────────────┐
│           Controller 层                    │  ← REST API 入口
│   @RestController / @RequestMapping       │  ← 请求验证 / OpenAPI 文档
├──────────────────────────────────────────┤
│           Application 层                   │  ← 用例编排 / DTO 转换
│   @Service / 事务边界                     │  ← 不包含业务规则
├──────────────────────────────────────────┤
│           Domain 层                        │  ← 核心业务逻辑
│   实体 / 值对象 / 领域服务 / 仓储接口     │  ← 纯 Java，不依赖框架
├──────────────────────────────────────────┤
│           Infrastructure 层                │  ← 技术实现
│   JPA Repository / Feign Client / Kafka   │  ← 数据库 / 消息 / 外部服务
└──────────────────────────────────────────┘
        ↕         ↕          ↕
   Request DTO   Response DTO   Query DTO
```

### 2.4 公共模块规范

`common/` 模块提供跨服务共享代码，**严格约束**：

| 允许包含 | 禁止包含 |
|----------|----------|
| 基础异常类、统一响应格式 | ❌ 业务逻辑 |
| 通用工具类（日期、加密、安全） | ❌ 业务 Entity/Repository |
| Feign 公共配置、拦截器 | ❌ 具体服务间的 Feign 接口 |
| 常量定义、枚举 | ❌ 环境相关的配置 |
| 全局异常处理器 | ❌ 第三方 SDK 封装 |

---

## 3. 服务拓扑与通信

### 3.1 架构拓扑

```
                          ┌─────────────┐
                          │   客户端     │
                          │ (React SPA) │
                          └──────┬──────┘
                                 │ HTTPS
                          ┌──────▼──────┐
                          │  gateway    │  ← 路由 / 限流 / JWT 验证
                          │  :8080      │  ← 统一入口 / 请求日志
                          └──────┬──────┘
                                 │
            ┌────────────────────┼────────────────────┐
            │                    │                    │
    ┌───────▼────────┐  ┌───────▼────────┐  ┌───────▼────────┐
    │   Core 服务     │  │  Support 服务   │  │  基础设施       │
    │                 │  │                 │  │                 │
    │ product:8081    │  │ auth:8093       │  │ PostgreSQL:5432 │
    │ order:8082      │  │ admin:8091      │  │ Redis:6379      │
    │ inventory:8083  │  │ finance:8092    │  │ Nacos:8848      │
    │ sales:8084      │  │ config:8888     │  │ Prometheus:9090 │
    │ procurement:8085│  │ registry:8761   │  │ Grafana:3000    │
    │ customer:8086   │  │ report:8094     │  │ Kafka:9092(*)   │
    │ supplier:8087   │  │ gateway:8080    │  │                 │
    │ biz-partner:8088│  │                 │  │                 │
    └───────┬────────┘  └───────┬────────┘  └───────┬────────┘
            │                    │                    │
            └────────────────────┼────────────────────┘
                                 │
                         ┌───────▼────────┐
                         │  Logstash(*)   │
                         │  (预留)        │
                         └───────┬────────┘
                                 │
                         ┌───────▼────────┐
                         │  Elasticsearch │
                         │ (预留)         │
                         └────────────────┘
```

> ⚠️ 标记 `(*)` 的服务为**预留组件**，代码层面已预留接口，但未纳入 Docker Compose 编排。

### 3.2 通信模式

| 模式 | 协议 | 场景 | 现状 |
|:----:|:----:|------|:----:|
| **同步** | REST (OpenFeign) | 查询类、实时性要求高 | ✅ 已实现 |
| **异步** | Kafka (预留) | 事件驱动、最终一致性 | ⚠️ 代码框架已建 |
| **异步** | RabbitMQ (预留) | 任务队列、消息推送 | 🔲 待规划 |

### 3.3 同步调用规范

```yaml
# Feign 客户端配置规范
feign:
  client:
    config:
      default:
        connect-timeout: 3000      # 连接超时 3s
        read-timeout: 5000         # 读取超时 5s
        logger-level: BASIC
  circuitbreaker:
    enabled: true                  # 开启熔断（Resilience4j）

# Resilience4j 熔断配置
resilience4j:
  circuitbreaker:
    configs:
      default:
        failure-rate-threshold: 50      # 50% 失败率触发熔断
        wait-duration-in-open-state: 10s # 半开等待 10s
        sliding-window-size: 10
  retry:
    configs:
      default:
        max-attempts: 3
        wait-duration: 500ms
```

### 3.4 事件驱动设计（预留）

```java
// 库存变更事件（示例）
public class InventoryChangedEvent {
    String skuCode;
    long quantity;
    ChangeType type;        // INBOUND / OUTBOUND / ADJUST
    String orderNo;
    Instant timestamp;
}

// 事件拓扑
order-service ──创建订单──→ InventoryChangedEvent ──→ inventory-service 扣库存
                              ├──→ finance-service 生成应收
                              └──→ sales-service 记录销售
```

---

## 4. 技术栈全景

### 4.1 技术选型矩阵

| 层级 | 技术 | 版本 | 用途 | 选型理由 |
|------|------|:----:|------|----------|
| **运行时** | Java | 21 (LTS) | 基础运行时 | 虚拟线程、模式匹配、最新 LTS |
| **框架** | Spring Boot | 3.4.4 | 应用框架 | 生态成熟、社区活跃 |
| **微服务** | Spring Cloud | 2024.0.2 | 基础设施 | 与 Boot 版本配套 |
| **服务发现** | Nacos | 2.3.0 | 注册+配置 | 一体化解法，替代 Eureka+Config |
| **构建** | Gradle + Kotlin DSL | 9.4.0 | 构建系统 | 类型安全、性能优于 Maven |
| **版本目录** | Version Catalog | - | 依赖统一管理 | 类型安全的版本管理 |
| **数据库** | PostgreSQL | 16+ | 主存储 | 严格的 ACID、JSONB、MVCC |
| **缓存** | Redis | 7.4+ | 缓存/限流/会话 | 高性能、数据结构丰富 |
| **消息队列** | Kafka (预留) | - | 事件驱动 | 高吞吐、持久化 |
| **前端** | React | 19 | UI 框架 | 主流、Hooks、SSR 支持 |
| **前端构建** | Vite | 8 | 构建工具 | 极速 HMR、ESM 原生 |
| **UI 组件** | MUI | 7 | 设计系统 | 成熟、TypeScript 友好 |
| **测试** | JUnit 5 + TestContainers | - | 测试框架 | 容器化集成测试 |
| **API 文档** | SpringDoc OpenAPI | 2.6.0 | API 文档 | 自动生成、Swagger UI |
| **静态分析** | Checkstyle + PMD + SpotBugs | - | 代码质量 | 全面覆盖风格+缺陷+安全 |
| **覆盖率** | JaCoCo | 0.8.12 | 覆盖率门禁 | Gradle 原生支持 |
| **安全扫描** | OWASP Dependency Check | 9.0.9 | 依赖漏洞 | CI 集成、自动检测 |
| **监控** | Prometheus + Grafana | - | 指标可视化 | CNCF 标准、开源 |
| **容器化** | Docker + Docker Compose | 24+ | 部署编排 | 开发环境标准化 |

### 4.2 版本统一管理

所有依赖版本通过 `gradle/libs.versions.toml` **唯一管理**，禁止在 `build.gradle.kts` 中硬编码版本号。

```toml
[versions]
spring-boot = "3.4.4"
spring-cloud = "2024.0.2"
# ... 所有版本集中在这里

[libraries]
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web" }
# ... 依赖定义

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
# ... 插件定义
```

---

## 5. 数据架构

### 5.1 数据库策略

| 环境 | 数据库 | 用途 |
|:----:|:------:|:----:|
| 开发 | PostgreSQL (Docker) | 完整功能验证 |
| 单元测试 | H2 (内存) | 快速执行 |
| 集成测试 | TestContainers + PostgreSQL | 真实数据库验证 |
| 生产 | PostgreSQL (独立实例) | 高可用部署 |

### 5.2 Schema 分片规划

```
                     PostgreSQL 实例
    ┌─────────────────────┼─────────────────────┐
    │                     │                     │
 ┌──▼────┐          ┌────▼───┐           ┌─────▼────┐
 │ Core  │          │Support │           │  Shared   │
 │ Schema│          │ Schema │           │  Schema   │
 ├───────┤          ├────────┤           ├───────────┤
 │product│          │auth    │           │flyway     │
 │orders │          │admin   │           │(迁移元数据)│
 │inventory│        │finance │           │           │
 │sales  │          │report  │           │           │
 │...    │          │...     │           │           │
 └───────┘          └────────┘           └───────────┘
```

> **长期演进目标**：每个核心服务使用独立 PostgreSQL 实例，实现**物理层面**的数据隔离。

### 5.3 缓存策略

| 缓存类别 | 存储 | TTL | 淘汰策略 |
|----------|:----:|:---:|:--------:|
| 热点商品数据 | Redis | 30 min | LRU |
| 用户会话 | Redis | 与 JWT 一致 (30min) | 主动删除 |
| 限流计数 | Redis | 1 min (滑动窗口) | TTL 自动过期 |
| 查询结果 | Caffeine (本地) | 5 min | 大小限制 (1000条) |
| 配置信息 | Caffeine (本地) | 10 min | 主动刷新 |

### 5.4 数据一致性模型

```
┌─────────────────────────────────────────────────────────┐
│                    数据一致性策略                        │
├──────────────┬──────────────────┬───────────────────────┤
│  服务内部     │  跨服务（同步）    │  跨服务（异步）       │
├──────────────┼──────────────────┼───────────────────────┤
│  ACID 事务   │  TCC / Saga      │  事件驱动 + 补偿      │
│  @Transactional │  Resilience4j   │  Outbox 模式         │
│  本地锁      │  最终一致性      │  幂等消费             │
├──────────────┴──────────────────┴───────────────────────┤
│ 并发控制：悲观锁（select for update） / 乐观锁 (@Version) │
└─────────────────────────────────────────────────────────┘
```

---

## 6. 安全架构

### 6.1 认证与授权模型

```
┌──────────┐     ┌──────────────┐     ┌──────────────────┐
│  客户端   │ ──→ │ gateway:8080 │ ──→ │   auth-service   │
│ (Bearer) │     │ 验证 JWT 签名 │     │  /api/auth/**    │
└──────────┘     └──────┬───────┘     └──────────────────┘
                        │ 通过 JWT 转发
                 ┌──────▼───────┐
                 │  业务服务     │
                 │ @PreAuthorize│
                 └──────────────┘
```

### 6.2 权限模型：RBAC

```
用户 ─── 角色 ─── 权限
                  ├── 页面权限（前端路由）
                  ├── API 权限（后端端点）
                  └── 数据权限（行级过滤）
```

### 6.3 安全防护清单

| 防护项 | 实现方式 | 状态 |
|--------|----------|:----:|
| SQL 注入 | JPA 参数化查询 | ✅ 已防护 |
| XSS | 输入验证 + HTML 转义 | ✅ 已防护 |
| CSRF | 无状态 JWT，不使用 Session | ✅ 固有 |
| 限流 | Redis + 网关层 | ✅ 已实现 |
| 密码安全 | BCrypt 加密 | ✅ 已实现 |
| CORS | 网关层配置白名单 | ✅ 已实现 |
| 敏感数据脱敏 | `toString()` 脱敏 | ✅ 已实现 |
| 安全审计日志 | SecurityLogger | ✅ 已实现 |
| 账户锁定 | 失败次数超限锁定 | ✅ 已实现 |
| **MFA** | 多因素认证（预留接口） | 🔲 待实现 |
| **OAuth2** | 社交登录（预留接口） | 🔲 待实现 |

---

## 7. 部署架构

### 7.1 环境策略

| 环境 | Docker Compose 文件 | 配置来源 | 用途 |
|:----:|:-------------------|----------|------|
| dev | `docker-compose.yml` + `.dev.yml` | `.env` + 本地配置 | 本地开发 |
| test | TestContainers | `application-test.yml` | CI 集成测试 |
| staging | `docker-compose.prod.yml` | Nacos 配置 | 预发布 |
| prod | Kubernetes (未来) | Nacos 配置 | 生产环境 |

### 7.2 启动顺序

```
第一阶段：基础设施
  PostgreSQL → Redis → Nacos

第二阶段：支撑服务（可并行）
  gateway-service → auth-service → config-service → registry-service

第三阶段：核心服务（可并行）
  product-service → order-service → inventory-service → sales-service
  → procurement-service → customer-service → supplier-service

第四阶段：其他服务（可并行）
  admin-service → finance-service → report-service → monitoring
```

### 7.3 健康检查

每个服务必须暴露以下 Actuator 端点：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  health:
    redis:
      enabled: true
    db:
      enabled: true
```

---

## 8. 监控与可观测性

### 8.1 可观测性三大支柱

```
┌──────────────────────────────────────────────────────────────┐
│                      可观测性                                 │
├──────────────────┬──────────────────┬────────────────────────┤
│     METRICS      │     LOGGING      │       TRACING          │
├──────────────────┼──────────────────┼────────────────────────┤
│ Prometheus       │ Logback          │ OpenTelemetry (预留)   │
│ Grafana 仪表盘   │ ELK (预留)       │ Jaeger (预留)          │
│ JVM / HTTP / 业务│ 结构化 JSON 日志  │ 分布式链路追踪          │
│ 指标             │ 日志级别动态调整  │ Span 上下文传播         │
├──────────────────┴──────────────────┴────────────────────────┤
│                       告警                                    │
│  Prometheus AlertManager → 邮件 / 钉钉 / 企业微信             │
│  告警规则：服务宕机 > 30s / 错误率 > 5% / P95 > 1s           │
└──────────────────────────────────────────────────────────────┘
```

### 8.2 关键指标

| 指标 | 来源 | 告警阈值 |
|------|------|:--------:|
| 服务存活 | Prometheus 探活 | 30s 无响应 |
| HTTP 5xx 比例 | Actuator | > 5% 持续 5min |
| P95 响应时间 | Actuator | > 1s 持续 5min |
| JVM 堆内存 | Micrometer | > 80% |
| GC 暂停时间 | Micrometer | > 1s |
| 库存预警数 | 业务指标 | 自定义 |
| 数据库连接池 | Actuator | > 80% 使用 |

---

## 9. 跨服务业务流程

### 9.1 订单履约流程（核心链路）

```
┌──────────┐    ┌──────────┐    ┌───────────┐    ┌──────────┐
│  客户端   │    │  order   │    │ inventory │    │ finance  │
└────┬─────┘    └────┬─────┘    └─────┬─────┘    └────┬─────┘
     │ 创建订单       │                │                │
     │───────────────→│                │                │
     │                │ 检查库存       │                │
     │                │───────────────→│                │
     │                │←─ 库存充足 ────│                │
     │                │                │                │
     │                │ 扣减库存       │                │
     │                │───────────────→│                │
     │                │←─ 扣减成功 ────│                │
     │                │                │                │
     │                │ 生成应收       │                │
     │                │────────────────────────────────→│
     │                │←── 生成成功 ────────────────────│
     │                │                │                │
     │←── 下单成功 ──│                │                │
```

### 9.2 采购入库流程

```
procurement → supplier (确认供应商)
    │
    ├──→ inventory (增加库存 + 批次管理)
    │
    └──→ finance (生成应付)
```

### 9.3 库存预警流程

```
inventory-service 监控库存水位
    │
    ├── 低于安全库存 → 预警 → admin-service 通知
    │
    ├── 低于最低库存 → 预警 + 自动生成采购建议
    │
    └── 缺货 → 紧急采购申请
```

---

## 10. 设计决策录

### 10.1 决策记录格式

每个决策使用 **Y-Statements** 格式记录：

```yaml
决策: <标题>
状态: [已采纳 | 已否决 | 待定]
背景: <为什么需要做这个决策>
方案:
  - A: <方案A描述>
  - B: <方案B描述>
决策: <选中的方案>
理由: <选择的理由>
后果: <决策带来的影响>
```

### 10.2 关键决策记录

#### ADR-001: 使用 Nacos 替代 Eureka + Config Server

```yaml
决策: 服务发现与配置技术选型
状态: 已采纳
背景: 需要同时实现服务注册发现和动态配置管理
方案:
  - A: Nacos（服务发现+配置管理一体化）
  - B: Eureka + Spring Cloud Config Server（两个独立组件）
  - C: Consul + Vault
决策: Nacos
理由: 1) 运维复杂度低，一个组件替代两个
      2) Netflix Eureka 已进入维护模式
      3) 阿里云原生支持，国内生态好
      4) 自带配置管理 Web UI
后果: 技术绑定阿里云生态，但原生 Spring Cloud 支持
```

#### ADR-002: 使用 PostgreSQL 替代 MySQL

```yaml
决策: 主数据库选型
状态: 已采纳
背景: 需要支持复杂查询、JSON 存储、强事务一致性
方案:
  - A: PostgreSQL
  - B: MySQL 8.x
决策: PostgreSQL
理由: 1) 严格的 ACID 事务，不依赖存储引擎
      2) 原生 JSONB 支持，适合灵活 Schema
      3) MVCC 实现更成熟，高并发性能更好
      4) 窗口函数、CTE 等高级查询能力更强
后果: 如需使用 Oracle 特有功能需额外适配
```

#### ADR-003: 统一版本目录管理

```yaml
决策: 依赖版本管理方式
状态: 已采纳
背景: 多个 build.gradle.kts 中版本定义分散，导致版本漂移
方案:
  - A: 使用 Gradle Version Catalog (libs.versions.toml)
  - B: 使用 Kotlin object V 常量对象
  - C: 使用 extra 属性传递
决策: 方案 A + 方案 B 过渡
理由: 版本目录是 Gradle 官方推荐的现代方案，类型安全
后果: 所有 build.gradle.kts 需要迁移到 libs.xxx 引用语法
```

#### ADR-004: 每服务独立 Schema 但不独立实例

```yaml
决策: 数据库部署策略
状态: 已采纳（短期）
背景: 需要在开发便利性与物理隔离之间取得平衡
方案:
  - A: 每个服务独立 PostgreSQL 实例和 Schema
  - B: 共享一个实例，每服务独立 Schema
  - C: 共享一个实例一个 Schema（表前缀区分）
决策: 方案 B（短期），目标方案 A（长期）
理由: 方案 B 降低开发和 Docker 编排复杂度
      长期演进到方案 A 实现物理隔离
后果: 当前开发环境共享数据库实例
```

---

## 11. 演进路线图

### 阶段一：架构重构 ✅ （当前）

- [x] 目录结构扁平化（`project-root/` → 上移）
- [x] 版本管理统一（迁移到 Version Catalog）
- [x] 构建配置规范化（消除重复 `build.gradle.kts`）
- [x] 删除冗余文件（crash log、临时脚本）
- [ ] 验证构建通过

### 阶段二：基础设施加固（1-2 周）

- [ ] 每核心服务独立 PostgreSQL Docker 实例
- [ ] 添加 Kafka 事件驱动（库存变更、订单状态）
- [ ] 实现 Saga 分布式事务（订单履约链路）
- [ ] 完善集成测试（cross-service-tests）

### 阶段三：可观测性完善（2-4 周）

- [ ] 集成 OpenTelemetry + Jaeger 链路追踪
- [ ] 实现结构化日志 + ELK 日志聚合
- [ ] 完善 Grafana 仪表盘（业务 + 系统级）
- [ ] 配置告警规则（Prometheus AlertManager）

### 阶段四：云原生转型（1-3 个月）

- [ ] 编写 Kubernetes 部署清单
- [ ] 实现 HPA 自动扩缩容
- [ ] 灰度发布（网关流量路由）
- [ ] 混沌工程（Chaos Monkey 集成）

---

## 附录

### A. 术语表

| 术语 | 含义 |
|:----:|------|
| SKU | Stock Keeping Unit，库存量单位 |
| BOM | Bill of Materials，物料清单 |
| RBAC | Role-Based Access Control，基于角色的访问控制 |
| Saga | 分布式事务的补偿模式 |
| TCC | Try-Confirm-Cancel，分布式事务模式 |
| Outbox | 发件箱模式，保证事件可靠投递 |
| CQRS | Command Query Responsibility Segregation，读写分离 |
| HPA | Horizontal Pod Autoscaler，水平自动扩缩 |

### B. 相关文档索引

| 文档 | 位置 | 说明 |
|------|------|------|
| 设计文档 | `ARCHITECTURE.md` | 本文档 |
| 设计决策录 | `docs/architecture/DECISIONS.md` | ADR 完整记录 |
| 贡献指南 | `CONTRIBUTING.md` | 开发者上手 |
| API 规范 | `docs/api/API_SPECIFICATION.md` | 契约定义 |
| 部署指南 | `docs/deployment/DEPLOYMENT_GUIDE.md` | 运维操作 |
| 故障排查 | `docs/deployment/TROUBLESHOOTING.md` | 常见问题 |
| 安全策略 | `docs/security/SECURITY_POLICY.md` | 安全规范 |
| 迁移指南 | `docs/migration/MIGRATION_GUIDE.md` | 版本升级 |

### C. 文档版本历史

| 版本 | 日期 | 变更内容 | 作者 |
|:----:|:----:|----------|:----:|
| 1.0 | 2026-05-03 | 初始版本 | Sisyphus |
| 2.0 | 2026-04-26 | 架构重构、补充 ADR、演进路线图 | - |