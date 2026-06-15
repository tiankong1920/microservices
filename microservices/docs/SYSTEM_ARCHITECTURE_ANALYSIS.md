# 库存管理系统 - 系统性架构分析

## 一、项目整体架构

```
inventory-management-system (根项目)
├── 构建系统: Gradle 9.4.0 + Kotlin DSL
├── Java版本: JDK 21
├── Spring Boot: 3.4.3
├── Spring Cloud: 2024.0.1
└── Spring Cloud Alibaba: 2023.0.1.0
```

## 二、模块层次结构

```
┌─────────────────────────────────────────────────────────────────┐
│                        根项目 (Root Project)                      │
│  插件: java, java-library, groovy, spotbugs, jacoco, owasp      │
│  全局配置: 代码质量检查、依赖版本强制、测试覆盖率                   │
└─────────────────────────────────────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
┌───────────────┐    ┌───────────────────┐    ┌────────────────┐
│    common     │    │  monitoring-core  │    │   monitoring   │
│  (公共模块)    │    │   (监控核心)       │    │   (监控服务)    │
│  java-library │    │   java-library    │    │ Spring Boot    │
└───────────────┘    └───────────────────┘    └────────────────┘
        │                       │                       │
        │                       └───────────────────────┘
        │
        ├──────────────────────────────────────────────────────┐
        ▼                                                      ▼
┌───────────────────────────┐              ┌───────────────────────────┐
│      core-services        │              │     support-services      │
│       (核心业务服务)        │              │       (支撑服务)           │
├───────────────────────────┤              ├───────────────────────────┤
│ • product-service         │              │ • gateway-service         │
│ • order-service           │              │ • auth-service            │
│ • inventory-service       │              │ • admin-service           │
│ • sales-service           │              │ • finance-service         │
│ • procurement-service     │              │ • config-service          │
│ • customer-service        │              │ • registry-service        │
│ • supplier-service        │              │ • report-service          │
│ • business-partner-service│              └───────────────────────────┘
└───────────────────────────┘
```

## 三、模块依赖联动关系

```
                    ┌─────────────────┐
                    │     common      │
                    │  (公共基础设施)  │
                    └────────┬────────┘
                             │
         ┌───────────────────┼───────────────────┐
         │                   │                   │
         ▼                   ▼                   ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│ product-service │ │  order-service  │ │ inventory-svc   │
│   (商品服务)     │ │   (订单服务)     │ │  (库存服务)      │
└────────┬────────┘ └────────┬────────┘ └────────┬────────┘
         │                   │                   │
         │    Feign调用      │                   │
         └───────────────────┼───────────────────┘
                             │
         ┌───────────────────┼───────────────────┐
         ▼                   ▼                   ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│ sales-service   │ │procurement-svc  │ │customer-service │
│   (销售服务)     │ │  (采购服务)      │ │  (客户服务)      │
└─────────────────┘ └─────────────────┘ └─────────────────┘
         │                   │
         ▼                   ▼
┌─────────────────┐ ┌─────────────────┐
│supplier-service │ │business-partner │
│  (供应商服务)    │ │    -service     │
└─────────────────┘ └─────────────────┘
```

## 四、技术栈依赖矩阵

| 层级 | 技术 | 用途 |
|------|------|------|
| **Web层** | Spring Boot Starter Web | REST API |
| **数据层** | Spring Data JPA + PostgreSQL | 持久化 |
| **缓存层** | Spring Data Redis | 缓存 |
| **服务发现** | Nacos / Eureka | 服务注册发现 |
| **配置中心** | Nacos / Spring Cloud Config | 配置管理 |
| **网关** | Spring Cloud Gateway | API网关 |
| **服务调用** | OpenFeign | 服务间通信 |
| **熔断降级** | Resilience4j | 容错处理 |
| **监控** | Micrometer + Prometheus | 指标收集 |
| **日志** | Logback + Logstash | 日志收集 |
| **安全** | JJWT + Spring Security | 认证授权 |
| **文档** | SpringDoc OpenAPI | API文档 |

## 五、构建任务联动

```
┌─────────────────────────────────────────────────────────────────┐
│                      Gradle 任务依赖图                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  cleanAll ─────────────────────────────────────────────────────►│
│      │                                                          │
│      ▼                                                          │
│  checkQuality ──► pmdMain ──► spotbugsMain ──► jacocoTestReport │
│      │                                                          │
│      ▼                                                          │
│  buildAll ──► subprojects.build ──► bootJar/jar                 │
│      │                                                          │
│      ▼                                                          │
│  testAll ──► subprojects.test ──► test.exec                     │
│      │                                                          │
│      ▼                                                          │
│  generateQualityReport ──► 汇总所有质量报告                       │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 主要构建任务说明

| 任务名称 | 描述 | 依赖 |
|---------|------|------|
| `buildAll` | 构建所有模块 | subprojects.build |
| `testAll` | 运行所有测试 | subprojects.test |
| `cleanAll` | 清理所有模块 | subprojects.clean |
| `checkQuality` | 代码质量检查 | pmdMain, spotbugsMain |
| `buildWithQuality` | 带质量检查的构建 | cleanAll, checkQuality, buildAll |
| `generateQualityReport` | 生成质量报告 | jacocoTestReport, pmdMain, spotbugsMain |

## 六、Docker服务编排联动

```yaml
基础设施层:
  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
  │  PostgreSQL │   │    Redis    │   │    Nacos    │
  │   :5432     │   │   :6379     │   │   :8848     │
  └──────┬──────┘   └──────┬──────┘   └──────┬──────┘
         │                 │                 │
         └─────────────────┼─────────────────┘
                           │
核心服务层:
  ┌──────────────────────────────────────────────────┐
  │ product │ order │ inventory │ sales │ procurement│
  │ customer│ supplier │ business-partner            │
  └──────────────────────────────────────────────────┘
                           │
支撑服务层:
  ┌──────────────────────────────────────────────────┐
  │ gateway(:8080) │ auth │ admin │ finance │ report │
  │ registry(:8761)│ config(:8888)                    │
  └──────────────────────────────────────────────────┘
```

### 服务端口映射

| 服务 | 端口 | 说明 |
|------|------|------|
| PostgreSQL | 5432 | 数据库 |
| Redis | 6379 | 缓存 |
| Nacos | 8848, 9848 | 服务发现/配置中心 |
| Gateway | 8080 | API网关 |
| Registry | 8761 | Eureka服务注册 |
| Config | 8888 | 配置服务 |

## 七、关键配置联动点

### 7.1 版本统一管理

所有依赖版本集中在 `gradle/libs.versions.toml` 文件中管理：

```toml
[versions]
java = "21"
spring-boot = "3.4.2"
spring-cloud = "2024.0.0"
spring-cloud-alibaba = "2023.0.1.0"
```

### 7.2 公共配置继承

子项目通过 `subprojects {}` 块继承根项目配置：

```kotlin
subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "io.spring.dependency-management")
    // ... 公共配置
}
```

### 7.3 依赖强制版本

通过 `resolutionStrategy.force()` 确保安全版本：

```kotlin
configurations.all {
    resolutionStrategy {
        force("com.github.spotbugs:spotbugs:4.9.7")
        force("org.apache.commons:commons-lang3:3.18.0")
        force("org.apache.logging.log4j:log4j-core:2.25.3")
    }
}
```

### 7.4 质量检查集成

| 工具 | 配置文件 | 用途 |
|------|---------|------|
| PMD | config/pmd/pmd.xml | 静态代码分析 |
| SpotBugs | config/spotbugs/spotbugs-exclude.xml | 字节码分析 |
| JaCoCo | 内置配置 | 测试覆盖率 |
| OWASP | config/owasp/suppressions.xml | 依赖漏洞检查 |

## 八、服务间通信模式

```
┌─────────────────────────────────────────────────────────────┐
│                     API Gateway (:8080)                      │
│                    Spring Cloud Gateway                      │
└─────────────────────────┬───────────────────────────────────┘
                          │
          ┌───────────────┼───────────────┐
          ▼               ▼               ▼
    ┌──────────┐    ┌──────────┐    ┌──────────┐
    │ Product  │    │  Order   │    │Inventory │
    │ Service  │    │ Service  │    │ Service  │
    └────┬─────┘    └────┬─────┘    └────┬─────┘
         │               │               │
         │   OpenFeign   │               │
         └───────────────┼───────────────┘
                         │
              Nacos 服务发现/配置中心
```

### 通信机制

1. **同步通信**: OpenFeign 用于服务间REST调用
2. **异步通信**: Spring Kafka 用于事件驱动
3. **服务发现**: Nacos 提供服务注册与发现
4. **配置管理**: Nacos Config 提供动态配置

## 九、架构设计原则

本系统架构遵循以下微服务最佳实践：

| 原则 | 实现方式 |
|------|---------|
| **单一职责** | 每个服务专注特定业务领域 |
| **依赖分层** | common → core-services → support-services |
| **基础设施解耦** | 通过 Nacos 实现服务发现和配置管理 |
| **可观测性** | 完整的监控、日志、追踪体系 |
| **安全加固** | 强制依赖版本、代码质量检查 |
| **高可用** | Resilience4j 熔断降级、Redis 缓存 |

## 十、模块详细说明

### 10.1 公共模块 (common)

提供所有服务共享的基础设施：

- 通用工具类
- 统一响应封装
- 异常处理
- Redis配置
- AOP切面
- 验证框架

### 10.2 监控模块

| 模块 | 类型 | 说明 |
|------|------|------|
| monitoring-core | java-library | 监控核心接口和抽象 |
| monitoring | Spring Boot | 监控服务实现 |
| monitoring-spring-boot-starter | starter | Spring Boot 自动配置 |

### 10.3 核心业务服务 (core-services)

| 服务 | 职责 | 主要实体 |
|------|------|---------|
| product-service | 商品管理 | Product, Category, Brand |
| order-service | 订单处理 | Order, OrderItem |
| inventory-service | 库存管理 | Inventory, StockMovement |
| sales-service | 销售管理 | Sale, SalesOrder |
| procurement-service | 采购管理 | Purchase, PurchaseOrder |
| customer-service | 客户管理 | Customer, Address |
| supplier-service | 供应商管理 | Supplier, Contact |
| business-partner-service | 业务伙伴 | Partner, Agreement |

### 10.4 支撑服务 (support-services)

| 服务 | 职责 |
|------|------|
| gateway-service | API网关、路由、限流 |
| auth-service | 认证授权、JWT令牌 |
| admin-service | 系统管理、用户权限 |
| finance-service | 财务管理、账单 |
| config-service | 配置中心 |
| registry-service | 服务注册中心 |
| report-service | 报表生成 |

---

## 十一、项目完成状态报告

### 11.1 模块实现状态

| 模块类型 | 模块名称 | 实现状态 | 说明 |
|---------|---------|---------|------|
| **公共模块** | common | ✅ 完成 | ApiResponse、异常处理、工具类完整 |
| **监控模块** | monitoring-core | ✅ 完成 | BusinessException、ErrorCode完整 |
| **监控模块** | monitoring | ✅ 完成 | 全局异常处理完整 |

### 11.2 核心业务服务状态

| 服务 | 实现状态 | 主要组件 |
|------|---------|---------|
| product-service | ✅ 完成 | Controller, Service, Repository, DTO, Entity |
| order-service | ✅ 完成 | Controller, Service, Repository, Feign Clients |
| inventory-service | ✅ 完成 | Controller, Service, Repository, Redis分布式锁 |
| sales-service | ✅ 完成 | Controller, Service, Repository, Feign Clients |
| procurement-service | ✅ 完成 | Controller, Service, Repository, Feign Clients |
| customer-service | ✅ 完成 | Controller, Service, Repository, 测试完整 |
| supplier-service | ✅ 完成 | Controller, Service, Repository, 测试完整 |
| business-partner-service | ✅ 完成 | Controller, Service, Repository |

### 11.3 支撑服务状态

| 服务 | 实现状态 | 主要组件 |
|------|---------|---------|
| gateway-service | ✅ 完成 | 过滤器、路由、限流、JWT工具 |
| auth-service | ✅ 完成 | OAuth2、MFA、JWT黑名单、权限管理 |
| admin-service | ✅ 完成 | 用户管理、角色权限、密码策略 |
| finance-service | ✅ 完成 | 账户管理、凭证、收支管理 |
| config-service | ✅ 完成 | 配置服务应用 |
| registry-service | ✅ 完成 | Eureka服务注册 |
| report-service | ✅ 完成 | 报表生成、多数据源聚合 |

### 11.4 服务间通信验证

| 通信方式 | 状态 | 说明 |
|---------|------|------|
| OpenFeign | ✅ 完成 | order-service → product-service, inventory-service |
| OpenFeign | ✅ 完成 | sales-service → product-service, inventory-service, customer-service |
| OpenFeign | ✅ 完成 | procurement-service → inventory-service, supplier-service |
| OpenFeign | ✅ 完成 | report-service → finance-service |

### 11.5 构建验证结果

```
BUILD SUCCESSFUL in 2m 8s
224 actionable tasks: 1 executed, 223 up-to-date
```

### 11.6 Docker配置更新

- ✅ 更新 product-service Dockerfile 使用 Gradle 构建
- ✅ 更新 gateway-service Dockerfile 使用 Gradle 构建
- ✅ 使用多阶段构建优化镜像大小
- ✅ 配置非root用户运行提高安全性

---

*文档生成时间: 2026-03-20*
*项目版本: 3.0.0*
*最后更新: 2026-03-20 构建验证通过*
