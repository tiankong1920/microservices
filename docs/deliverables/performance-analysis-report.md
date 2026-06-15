# 进销存管理系统 — 性能分析与优化建议报告

> 版本: 1.0 | 日期: 2026-06-08 | 分析范围: 全项目性能配置扫描

---

## 1. 报告摘要

对进销存管理系统 v3.0 进行全项目性能配置扫描，覆盖 25 个 Gradle 子项目。总体评估：**性能配置良好**，HikariCP 连接池、Resilience4j 熔断限流、Tomcat 线程配置均已有合理的分层设计（dev/testing/staging/prod）。主要改进空间集中在缓存策略完善、SQL 慢查询监控、和 JVM 运行时参数调优。

| 评估项 | 评分 | 说明 |
|--------|------|------|
| 连接池配置 | ⭐⭐⭐⭐ | HikariCP 多环境分层，prod 100 连接合理 |
| 熔断限流 | ⭐⭐⭐⭐⭐ | Resilience4j 四件套完整（circuitbreaker/retry/ratelimiter/timelimiter） |
| 线程配置 | ⭐⭐⭐⭐ | Tomcat prod 500 线程，dev 200 线程 |
| 缓存策略 | ⭐⭐⭐ | Redis 基础配置存在，缺少 TTL 策略和预热机制 |
| 日志性能 | ⭐⭐⭐⭐ | 结构化 JSON 日志 + 异步 Logstash |
| JVM 调优 | ⭐⭐⭐ | Gradle 构建 JVM 已优化，运行时 JVM 参数未显式配置 |
| 压缩传输 | ⭐⭐⭐⭐ | prod 环境启用 Gzip 压缩 |
| SQL 监控 | ⭐⭐ | 缺少慢 SQL 阈值配置和执行计划日志 |

---

## 2. 连接池性能分析

### 2.1 HikariCP 配置总览

| 环境 | maximum-pool-size | minimum-idle | 数据源 | 评估 |
|------|------------------|--------------|--------|------|
| dev | 20 | 5 | application-dev.yml | ✅ 合理 |
| testing | 50 (env var) | 10 (env var) | application-testing.yml | ✅ 合理，环境变量可调 |
| staging | 80 (env var) | 15 (env var) | application-staging.yml | ✅ 合理 |
| prod | 100 | 20 | application-prod.yml | ✅ 合理 |
| postgres (共享) | 20 | 5 | application-postgres.yml | ✅ 合理 |

各独立服务也有单独配置：
- template-service: dev=10/5, prod=50/10
- product-service: 20/5
- datasource-service: 20/5
- invoice-service: 20/5

### 2.2 建议优化

1. **连接泄漏检测**：建议在 prod 环境添加 `leak-detection-threshold: 10000`（10秒），自动检测连接泄漏
2. **连接超时配置**：prod 环境建议显式设置 `connection-timeout: 30000`（默认30s），避免连接池耗尽时无限等待
3. **idle-timeout**：建议设置 `idle-timeout: 600000`（10分钟），回收空闲连接
4. **max-lifetime**：建议设置 `max-lifetime: 1800000`（30分钟），低于 PostgreSQL 默认空闲超时

---

## 3. 熔断与限流分析

### 3.1 Resilience4j 配置

**dev 环境**（application.yml）：
- CircuitBreaker: failure-rate=50%, wait=60s, window=100, min-calls=10
- Retry: max-attempts=3, wait=500ms
- RateLimiter: 100/s, timeout=500ms

**prod 环境**（application-prod.yml）：
- CircuitBreaker: failure-rate=50%, wait=10s, window=10, min-calls=20, slow-call=2s
- RateLimiter: 100/60s, timeout=5s
- Retry: max-attempts=3, wait=1s
- TimeLimiter: 5s

### 3.2 评估

✅ 四件套完整，prod 配置更保守合理  
✅ slow-call 检测（2s 阈值）有助于发现性能退化  
⚠️ 建议：为不同服务配置独立的 circuitbreaker 实例（如 inventory-service vs order-service 分开配置）

---

## 4. Tomcat 线程池分析

| 参数 | dev | prod | 说明 |
|------|-----|------|------|
| threads.max | 200 | 500 | ✅ 合理 |
| threads.min-spare | 20 | 50 | ✅ 合理 |
| connection-timeout | 默认(60s) | 60000ms | ✅ prod 显式设置 |
| max-connections | 默认(10000) | 10000 | ✅ |
| keep-alive-timeout | 默认(60s) | 30000ms | ✅ prod 更短更安全 |
| accept-count | 默认(100) | 200 | ✅ prod 增大排队容量 |
| compression | 未启用 | Gzip 启用 | ✅ prod 开启压缩 |

### 建议优化
1. **max-connections 与 pool-size 匹配**：prod 环境 max-connections=10000，但 DB 连接池只有 100。建议设置 `max-connections` 上限防止雪崩
2. **线程池命名**：为不同服务设置不同的线程前缀，方便监控区分

---

## 5. 缓存策略分析

### 5.1 现状
- Redis 连接已配置（docker-compose 中暴露 7379→6379）
- 部分服务有 `@Cacheable` 注解（inventory-service, product-service）
- 缺少全局缓存 TTL 策略

### 5.2 建议
1. **统一缓存 TTL**：在 `application.yml` 中添加 Redis CacheManager 配置
   ```yaml
   spring:
     cache:
       redis:
         time-to-live: 600000  # 10分钟默认TTL
   ```
2. **缓存预热**：高频查询数据（商品列表、供应商信息）在启动时预热
3. **缓存穿透保护**：空值缓存（短 TTL）+ 布隆过滤器
4. **缓存雪崩保护**：随机化 TTL 偏移（±10%）

---

## 6. JVM 运行时建议

当前 `gradle.properties` 仅配置了构建 JVM。运行时 JVM 参数建议通过 `JAVA_TOOL_OPTIONS` 或 Dockerfile 设置：

```
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+ParallelRefProcEnabled
-XX:+UseStringDeduplication
-Xms512m -Xmx2g (根据服务负载调整)
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/tmp/heap-dump.hprof
-XX:+ExitOnOutOfMemoryError
```

---

## 7. SQL 监控建议

当前缺少慢 SQL 监控。建议添加：

```yaml
spring:
  jpa:
    properties:
      hibernate:
        session:
          events:
            log: true
logging:
  level:
    org.hibernate.SQL: DEBUG (仅 dev 环境)
    org.hibernate.stat: DEBUG (仅 dev 环境)
    org.hibernate.type.descriptor.sql: TRACE (仅 dev 环境)
```

同时配置慢 SQL 阈值：
```yaml
spring:
  datasource:
    hikari:
      data-source-properties:
        logUnclosedConnections: true
        logSlowQueryThreshold: 1000  # 1秒以上视为慢查询
```

---

## 8. 监控告警覆盖

已有监控配置（monitoring/alerting-rules.yml）：
- HikariCP 连接池使用率 > 90% 告警 ✅
- HikariCP 连接超时告警 ✅
- JVM 内存使用率 > 85% 告警 ✅
- 服务健康检查 ✅

**建议补充**：
- P99 响应时间 > 2s 告警
- 错误率 > 1% 告警
- 慢 SQL 数量 > 10/min 告警
- GC 停顿时间 > 1s 告警

---

## 9. 总体评分与优先级

| 优化项 | 优先级 | 预估工时 | 收益 |
|--------|--------|----------|------|
| 添加缓存 TTL + 预热机制 | P1 | 2天 | 大幅降低 DB 压力 |
| 慢 SQL 监控配置 | P1 | 0.5天 | 快速发现性能瓶颈 |
| 连接泄漏检测 | P1 | 0.5天 | 防止连接池耗尽 |
| 补充监控告警规则 | P1 | 1天 | 生产环境可观测性 |
| JVM 运行时参数调优 | P2 | 1天 | 减少 GC 停顿 |
| 服务级 CircuitBreaker | P2 | 1天 | 提高隔离性 |
| 缓存穿透/雪崩保护 | P2 | 1天 | 生产稳定性 |

**总计**: P1 4人·天, P2 3人·天。建议纳入 v3.1 迭代（Q3）。

---

## 10. 结论

进销存管理系统 v3.0 的性能配置基线扎实，HikariCP 分层设计、Resilience4j 熔断限流、Tomcat 线程调优均已覆盖。主要短板在缓存策略和 SQL 监控层面。P1 优化项（缓存 TTL + 慢 SQL 监控 + 连接泄漏检测）预计 4 人·天可完成，建议在 v3.1 迭代中优先实施。
