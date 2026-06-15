# 配置变更记录

**文档编号**: INIT-CFG-2026-001  
**项目名称**: inventory-management-system  
**文档版本**: 1.0.0  
**编制日期**: 2026-04-16  
**编制人**: 系统管理员  

---

## 目录

1. [配置变更概述](#1-配置变更概述)
2. [Docker配置变更](#2-docker配置变更)
3. [Logback配置变更](#3-logback配置变更)
4. [Gradle配置变更](#4-gradle配置变更)
5. [数据库配置变更](#5-数据库配置变更)
6. [服务配置变更](#6-服务配置变更)
7. [变更统计汇总](#7-变更统计汇总)

---

## 1. 配置变更概述

| 统计项 | 数值 |
|--------|------|
| 配置变更总数 | 12 |
| 涉及文件数 | 15 |
| 变更类型-新增 | 3 |
| 变更类型-修改 | 9 |
| 变更类型-删除 | 0 |

---

## 2. Docker配置变更

### CFG-001: PostgreSQL镜像配置

**配置标识**:
| 项目 | 内容 |
|------|------|
| 配置ID | CFG-001 |
| 配置文件 | docker-compose.yml |
| 配置路径 | services.postgres.image |
| 变更时间 | 2026-04-15 10:45:00 |

**变更详情**:

```diff
- image: postgres:18.0.3
+ image: docker.1ms.run/postgres:18-alpine
```

**变更依据**:
| 类型 | 说明 |
|------|------|
| 技术依据 | postgres:18.0.3版本不存在 |
| 业务原因 | 需要使用国内镜像加速下载 |
| 决策文档 | ERR-001错误修复记录 |

**变更管理**:
| 角色 | 人员 | 时间 |
|------|------|------|
| 变更执行人 | 系统管理员 | 2026-04-15 10:45:00 |
| 审核人 | 技术负责人 | 2026-04-15 11:00:00 |

**风险控制**:
| 项目 | 内容 |
|------|------|
| 回滚方案 | 将image改回官方源 |
| 潜在风险 | 镜像源不可用时需切换备用源 |
| 影响范围 | 所有依赖PostgreSQL的服务 |

---

### CFG-002: Redis镜像配置

**配置标识**:
| 项目 | 内容 |
|------|------|
| 配置ID | CFG-002 |
| 配置文件 | docker-compose.yml |
| 配置路径 | services.redis.image |

**变更详情**:

```diff
- image: redis:7-alpine
+ image: docker.1ms.run/redis:7-alpine
```

**变更依据**: 使用国内镜像加速

---

### CFG-003: Nacos镜像配置

**配置标识**:
| 项目 | 内容 |
|------|------|
| 配置ID | CFG-003 |
| 配置文件 | docker-compose.yml |
| 配置路径 | services.nacos.image |

**变更详情**:

```diff
- image: nacos/nacos-server:v2.4.3
+ image: docker.1ms.run/nacos/nacos-server:v2.4.3
```

**变更依据**: 使用国内镜像加速

---

### CFG-004: 应用基础镜像配置

**配置标识**:
| 项目 | 内容 |
|------|------|
| 配置ID | CFG-004 |
| 配置文件 | 各服务Dockerfile |
| 配置路径 | FROM指令 |

**变更详情**:

```diff
- FROM eclipse-temurin:21-jre-alpine
+ FROM docker.1ms.run/eclipse-temurin:21-jre-alpine
```

**涉及文件**:
- core-services/product-service/Dockerfile
- core-services/order-service/Dockerfile
- core-services/inventory-service/Dockerfile
- core-services/business-partner-service/Dockerfile
- core-services/procurement-service/Dockerfile

---

## 3. Logback配置变更

### CFG-005: 移除LOGSTASH Appender

**配置标识**:
| 项目 | 内容 |
|------|------|
| 配置ID | CFG-005 |
| 配置文件 | */src/main/resources/logback-spring.xml |
| 变更时间 | 2026-04-15 14:30:00 |

**变更详情**:

变更前:
```xml
<appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>localhost:5000</destination>
    <encoder class="net.logstash.logback.LogstashEncoder"/>
</appender>

<root level="INFO">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="LOGSTASH"/>
</root>
```

变更后:
```xml
<root level="INFO">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="FILE"/>
</root>
```

**变更依据**:
| 类型 | 说明 |
|------|------|
| 技术依据 | Logstash服务未配置，导致启动失败 |
| 业务原因 | 简化日志配置，使用标准输出 |

**涉及文件**:
- common/src/main/resources/logback-spring.xml
- core-services/product-service/src/main/resources/logback-spring.xml
- core-services/order-service/src/main/resources/logback-spring.xml
- core-services/inventory-service/src/main/resources/logback-spring.xml
- core-services/business-partner-service/src/main/resources/logback-spring.xml
- core-services/procurement-service/src/main/resources/logback-spring.xml
- support-services/auth-service/src/main/resources/logback-spring.xml
- support-services/gateway-service/src/main/resources/logback-spring.xml

---

### CFG-006: 删除Logback YAML配置

**配置标识**:
| 项目 | 内容 |
|------|------|
| 配置ID | CFG-006 |
| 配置文件 | logback-spring.yml |
| 变更类型 | 删除 |

**变更依据**: YAML格式配置与XML冲突，统一使用XML格式

---

## 4. Gradle配置变更

### CFG-007: 镜像源配置

**配置标识**:
| 项目 | 内容 |
|------|------|
| 配置ID | CFG-007 |
| 配置文件 | settings.gradle.kts |
| 配置路径 | dependencyResolutionManagement.repositories |

**变更详情**:

```kotlin
repositories {
    mavenCentral()
    maven {
        name = "springMilestone"
        url = uri("https://repo.spring.io/milestone")
    }
    maven {
        name = "aliyun"
        url = uri("https://maven.aliyun.com/repository/public")
    }
    maven {
        name = "google"
        url = uri("https://maven.google.com")
    }
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }
}
```

**变更依据**: 配置国内镜像源加速依赖下载

---

### CFG-008: 依赖验证配置

**配置标识**:
| 项目 | 内容 |
|------|------|
| 配置ID | CFG-008 |
| 配置文件 | gradle/verification-metadata.xml |

**变更详情**:

```xml
<configuration>
    <verify-metadata>true</verify-metadata>
    <verify-signatures>false</verify-signatures>
    <verification-mode>lenient</verification-mode>
</configuration>
```

**变更依据**: 启用依赖校验和验证，确保依赖完整性

---

## 5. 数据库配置变更

### CFG-009: 数据库初始化脚本

**配置标识**:
| 项目 | 内容 |
|------|------|
| 配置ID | CFG-009 |
| 配置文件 | init-scripts/02-create-tables.sql |
| 变更类型 | 新增 |

**变更详情**:

新增完整的数据库表结构，包含41张表：

| 表类别 | 表数量 | 主要表名 |
|--------|--------|----------|
| 核心业务表 | 15 | products, orders, inventory, warehouses |
| 合作伙伴表 | 8 | customers, suppliers, business_partners |
| 采购相关表 | 6 | purchase_orders, purchase_order_items |
| 库存调整表 | 6 | stock_transfer_orders, other_stock_in_orders |
| 系统配置表 | 6 | sys_users, sys_roles, sys_permissions |

**变更依据**: 项目业务需求，建立完整的数据模型

---

## 6. 服务配置变更

### CFG-010: InventoryService应用扫描配置

**配置标识**:
| 项目 | 内容 |
|------|------|
| 配置ID | CFG-010 |
| 配置文件 | InventoryServiceApplication.java |
| 变更时间 | 2026-04-16 08:30:00 |

**变更详情**:

```diff
- @SpringBootApplication
+ @SpringBootApplication(scanBasePackages = "com.inventory")
```

**变更依据**: 扫描common模块的组件

---

### CFG-011: Kafka消息服务配置

**配置标识**:
| 项目 | 内容 |
|------|------|
| 配置ID | CFG-011 |
| 配置文件 | KafkaMessageService.java, NoOpKafkaMessageService.java |
| 变更类型 | 新增 |

**变更详情**:

新增文件:
- `common/src/main/java/com/inventory/common/core/kafka/IKafkaMessageService.java`
- 修改 `KafkaMessageService.java` 添加 `@Primary` 注解
- 修改 `NoOpKafkaMessageService.java` 作为默认实现

**变更依据**: 解决Kafka未配置时的依赖注入问题

---

### CFG-012: 实体类属性命名规范化

**配置标识**:
| 项目 | 内容 |
|------|------|
| 配置ID | CFG-012 |
| 配置文件 | Warehouse.java |
| 变更时间 | 2026-04-16 09:15:00 |

**变更详情**:

```diff
- private Boolean active;
- private Boolean primary;
+ private Boolean isActive;
+ private Boolean isPrimary;
```

**变更依据**: 遵循JPA属性命名规范，解决Repository方法映射问题

---

## 7. 变更统计汇总

### 按配置类型统计

| 配置类型 | 变更数量 | 占比 |
|----------|----------|------|
| Docker配置 | 4 | 33% |
| Logback配置 | 2 | 17% |
| Gradle配置 | 2 | 17% |
| 数据库配置 | 1 | 8% |
| 服务配置 | 3 | 25% |

### 按变更类型统计

| 变更类型 | 数量 | 占比 |
|----------|------|------|
| 新增 | 3 | 25% |
| 修改 | 9 | 75% |
| 删除 | 0 | 0% |

### 按影响范围统计

| 影响范围 | 变更数量 |
|----------|----------|
| 全局配置 | 4 |
| 单服务配置 | 5 |
| 基础设施 | 3 |

---

## 8. 配置回滚指南

### Docker镜像回滚

```bash
# 将docker-compose.yml中的镜像改回官方源
sed -i 's/docker.1ms.run\///g' docker-compose.yml
docker-compose down
docker-compose up -d
```

### Logback配置回滚

```bash
# 从备份恢复logback配置
cp /backups/20260416_200044/logback-spring.xml \
   core-services/inventory-service/src/main/resources/
```

### Gradle配置回滚

```bash
# 恢复settings.gradle.kts
git checkout HEAD -- settings.gradle.kts
```

---

**文档状态**: 已完成  
**最后更新**: 2026-04-16 20:20:00  
**下次审核**: 每次配置变更后更新
