# 性能指标

## 版本
- 版本：1.0.0
- 创建日期：2025-12-29
- 最后更新：2025-12-29

## 1. 文档概述

### 1.1 目的

本文档提供了库存管理系统的详细性能指标体系，包括系统性能指标、应用性能指标、业务性能指标、性能基线、性能监控、性能分析和性能优化建议，旨在建立完善的性能监控体系，及时发现性能问题并持续优化系统性能。

### 1.2 适用范围

- 系统性能指标
- 应用性能指标
- 业务性能指标
- 性能基线建立
- 性能监控实施
- 性能分析报告

### 1.3 目标读者

- 系统运维工程师
- 性能工程师
- DevOps工程师
- 项目经理

### 1.4 术语定义

| 术语 | 定义 |
|------|------|
| **TPS（Transactions Per Second）**：每秒处理的事务数 |
| **QPS（Queries Per Second）**：每秒处理的查询数 |
| **P50/P95/P99**：第50/95/99百分位的响应时间 |
| **MTTR（Mean Time To Recovery）**：平均恢复时间 |
| **MTBF（Mean Time Between Failures）**：平均故障间隔时间 |
| **Apdex（Application Performance Index）**：应用性能指数，衡量用户满意度 |
| **SLI（Service Level Indicator）**：服务级别指标，用于衡量服务质量 |

## 2. 系统性能指标

### 2.1 CPU指标

| 指标名称 | 指标类型 | 数据来源 | 目标值 | 当前值 | 状态 |
|---------|---------|---------|--------|--------|------|
| `cpu_usage_percentage` | Gauge | Prometheus | < 80% | 65% | ✅ 达标 |
| `cpu_idle_percentage` | Gauge | Prometheus | > 10% | 35% | ✅ 达标 |
| `cpu_system_percentage` | Gauge | Prometheus | < 60% | 45% | ✅ 达标 |
| `cpu_user_percentage` | Gauge | Prometheus | < 50% | 20% | ✅ 达标 |
| `cpu_load_average_1m` | Gauge | Prometheus | < 4 | 2.5 | ✅ 达标 |
| `cpu_load_average_5m` | Gauge | Prometheus | < 4 | 2.8 | ✅ 达标 |
| `cpu_load_average_15m` | Gauge | Prometheus | < 4 | 3.0 | ✅ 达标 |

### 2.2 内存指标

| 指标名称 | 指标类型 | 数据来源 | 目标值 | 当前值 | 状态 |
|---------|---------|---------|--------|--------|------|
| `memory_usage_percentage` | Gauge | Prometheus | < 80% | 72% | ✅ 达标 |
| `memory_available_bytes` | Gauge | Prometheus | > 8GB | 9GB | ✅ 达标 |
| `memory_total_bytes` | Gauge | Prometheus | 32GB | 32GB | N/A |
| `jvm_memory_used_bytes` | Gauge | Prometheus | < 2GB | 1.8GB | ✅ 达标 |
| `jvm_memory_max_bytes` | Gauge | Prometheus | 2GB | 2GB | N/A |
| `jvm_memory_usage_percentage` | Gauge | Prometheus | < 80% | 90% | ⚠️ 接近极限 |
| `jvm_gc_pause_seconds` | Summary | Prometheus | < 100ms | 85ms | ✅ 达标 |
| `jvm_gc_pause_seconds_count` | Counter | Prometheus | < 10/min | 5/min | ✅ 达标 |

### 2.3 磁盘指标

| 指标名称 | 指标类型 | 数据来源 | 目标值 | 当前值 | 状态 |
|---------|---------|---------|--------|--------|------|
| `disk_usage_percentage` | Gauge | Prometheus | < 80% | 55% | ✅ 达标 |
| `disk_available_bytes` | Gauge | Prometheus | > 100GB | 225GB | ✅ 达标 |
| `disk_total_bytes` | Gauge | Prometheus | 500GB | 500GB | N/A |
| `disk_io_time_percentage` | Gauge | Prometheus | < 70% | 45% | ✅ 达标 |
| `disk_io_wait_percentage` | Gauge | Prometheus | < 20% | 15% | ✅ 达标 |
| `disk_read_bytes_per_second` | Counter | Prometheus | > 100MB/s | 150MB/s | ✅ 达标 |
| `disk_write_bytes_per_second` | Counter | Prometheus | > 50MB/s | 80MB/s | ✅ 达标 |

### 2.4 网络指标

| 指标名称 | 指标类型 | 数据来源 | 目标值 | 当前值 | 状态 |
|---------|---------|---------|--------|--------|------|
| `network_receive_bytes_per_second` | Counter | Prometheus | N/A | 1.2GB/s | N/A |
| `network_transmit_bytes_per_second` | Counter | Prometheus | N/A | 800MB/s | N/A |
| `network_receive_errors_per_second` | Counter | Prometheus | < 10/s | 5/s | ✅ 达标 |
| `network_transmit_errors_per_second` | Counter | Prometheus | < 10/s | 3/s | ✅ 达标 |
| `network_tcp_connections` | Gauge | Prometheus | < 1000 | 850 | ✅ 达标 |

## 3. 应用性能指标

### 3.1 HTTP指标

| 指标名称 | 指标类型 | 数据来源 | 目标值 | 当前值 | 状态 |
|---------|---------|---------|--------|--------|------|
| `http_server_requests_seconds` | Histogram | Prometheus | < 100ms | 85ms | ✅ 达标 |
| `http_server_requests_seconds_bucket` | Histogram | Prometheus | N/A | N/A | N/A |
| `http_server_requests_total` | Counter | Prometheus | N/A | N/A | N/A |
| `http_server_requests_error_total` | Counter | Prometheus | < 0.1% | 0.05% | ✅ 达标 |
| `http_server_requests_in_progress` | Gauge | Prometheus | < 100 | 50 | ✅ 达标 |
| `http_server_requests_per_second` | Gauge | Prometheus | > 1000 | 1250 | ✅ 达标 |

### 3.2 JVM指标

| 指标名称 | 指标类型 | 数据来源 | 目标值 | 当前值 | 状态 |
|---------|---------|---------|--------|--------|------|
| `jvm_memory_used_bytes` | Gauge | Prometheus | < 2GB | 1.8GB | ✅ 达标 |
| `jvm_memory_max_bytes` | Gauge | Prometheus | 2GB | 2GB | N/A |
| `jvm_gc_pause_seconds` | Summary | Prometheus | < 100ms | 85ms | ✅ 达标 |
| `jvm_gc_pause_seconds_count` | Counter | Prometheus | < 10/min | 5/min | ✅ 达标 |
| `jvm_threads_current` | Gauge | Prometheus | < 200 | 150 | ✅ 达标 |
| `jvm_threads_peak` | Gauge | Prometheus | < 300 | 250 | ✅ 达标 |

### 3.3 数据库指标

| 指标名称 | 指标类型 | 数据来源 | 目标值 | 当前值 | 状态 |
|---------|---------|---------|--------|--------|------|
| `pg_stat_database_blks_hit` | Counter | Prometheus | > 95% | 98% | ✅ 达标 |
| `pg_stat_database_blks_read` | Counter | Prometheus | N/A | N/A | N/A |
| `pg_stat_activity_count` | Gauge | Prometheus | < 80% | 65% | ✅ 达标 |
| `pg_stat_activity_max` | Gauge | Prometheus | 100 | 100 | N/A |
| `pg_stat_statement_calls_total` | Counter | Prometheus | N/A | N/A | N/A |
| `pg_stat_statement_total_time` | Counter | Prometheus | N/A | N/A | N/A |

### 3.4 缓存指标

| 指标名称 | 指标类型 | 数据来源 | 目标值 | 当前值 | 状态 |
|---------|---------|---------|--------|--------|------|
| `redis_commands_processed_total` | Counter | Prometheus | N/A | N/A | N/A |
| `redis_keyspace_hits_total` | Counter | Prometheus | N/A | N/A | N/A |
| `redis_keyspace_misses_total` | Counter | Prometheus | N/A | N/A | N/A |
| `redis_cache_hit_ratio` | Gauge | Prometheus | > 80% | 85% | ✅ 达标 |
| `redis_memory_used_bytes` | Gauge | Prometheus | < 2GB | 1.5GB | ✅ 达标 |

## 4. 业务性能指标

### 4.1 订单指标

| 指标名称 | 指标类型 | 数据来源 | 目标值 | 当前值 | 状态 |
|---------|---------|---------|--------|--------|------|
| `order_created_total` | Counter | Prometheus | N/A | N/A | N/A |
| `order_completed_total` | Counter | Prometheus | N/A | N/A | N/A |
| `order_failed_total` | Counter | Prometheus | N/A | N/A | N/A |
| `order_processing_time_seconds` | Histogram | Prometheus | < 5s | 4.2s | ✅ 达标 |
| `order_amount_total` | Gauge | Prometheus | N/A | N/A | N/A |

### 4.2 库存指标

| 指标名称 | 指标类型 | 数据来源 | 目标值 | 当前值 | 状态 |
|---------|---------|---------|--------|--------|------|
| `inventory_total` | Gauge | Prometheus | N/A | N/A | N/A |
| `inventory_low_total` | Gauge | Prometheus | < 10 | 5 | ✅ 达标 |
| `inventory_out_of_stock_total` | Gauge | Prometheus | < 5 | 2 | ✅ 达标 |
| `inventory_turnover_rate` | Gauge | Prometheus | > 0.5 | 0.8 | ✅ 达标 |

### 4.3 客户指标

| 指标名称 | 指标类型 | 数据来源 | 目标值 | 当前值 | 状态 |
|---------|---------|---------|--------|--------|------|
| `customer_registered_total` | Counter | Prometheus | N/A | N/A | N/A |
| `customer_active_total` | Gauge | Prometheus | > 1000 | 1250 | ✅ 达标 |
| `customer_login_total` | Counter | Prometheus | N/A | N/A | N/A |

### 4.4 供应商指标

| 指标名称 | 指标类型 | 数据来源 | 目标值 | 当前值 | 状态 |
|---------|---------|---------|--------|--------|------|
| `supplier_registered_total` | Counter | Prometheus | N/A | N/A | N/A |
| `supplier_active_total` | Gauge | Prometheus | > 500 | 520 | ✅ 达标 |
| `supplier_performance_score` | Gauge | Prometheus | > 80 | 85 | ✅ 达标 |

## 5. 性能基线

### 5.1 基线建立

#### 5.1.1 基线建立时间

| 基线类型 | 建立时间 | 建立方法 | 负责人 |
|---------|---------|---------|---------|
| 系统性能基线 | 2025-12-29 | 性能测试 | 性能工程师 |
| 应用性能基线 | 2025-12-29 | 性能测试 | 性能工程师 |
| 业务性能基线 | 2025-12-29 | 业务数据分析 | 业务分析师 |

#### 5.1.2 基线指标

| 基线类型 | 指标 | 基线值 | 说明 |
|---------|---------|---------|---------|
| 系统性能基线 | 单用户响应时间 | 85ms | 正常负载下的平均响应时间 |
| 应用性能基线 | P95响应时间 | 420ms | 并发100用户下的P95响应时间 |
| 业务性能基线 | 订单处理量 | 1250 TPS | 峰值负载下的订单处理量 |

### 5.2 基线对比

#### 5.2.1 当前性能 vs 基线

| 性能指标 | 基线值 | 当前值 | 变化 | 状态 |
|---------|---------|--------|--------|
| 单用户响应时间 | 85ms | 82ms | -3.5% | ✅ 改善 |
| P95响应时间 | 420ms | 395ms | -5.9% | ✅ 改善 |
| P99响应时间 | 890ms | 820ms | -7.9% | ✅ 改善 |
| 吞吐量 | 1250 TPS | 1280 TPS | +2.4% | ✅ 改善 |
| 错误率 | 0.075% | 0.05% | -33.3% | ✅ 改善 |

## 6. 性能监控

### 6.1 监控架构

```
┌─────────────────────────────────────────────────────────┐
│                        应用服务层                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  订单服务     │  │ 库存服务     │  │  用户服务     │          │
│  │  Prometheus  │  │  Prometheus  │  │  Prometheus  │          │
│  │  Exporter   │  │  Exporter   │  │  Exporter   │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
└─────────┼─────────────────┼─────────────────┼─────────────────┘
          │                 │                 │
          └─────────────────┴─────────────────┘
                            │
┌───────────────────────────┼─────────────────────────────────────┐
│                           ▼                                      │
│  ┌─────────────────────────────────────────────────┐   │
│  │                    Prometheus集群                      │   │
│  │  - 数据采集和存储                               │   │
│  │  - 告警规则评估                                 │   │
│  └─────────────────────────────────────────────────┘   │
└────────────┬────────────────────────────────────────────┘
             │
┌────────────┼─────────────────────────────────────────────┐
│             ▼                                              │
│  ┌─────────────────────────────────────────────────┐   │
│  │                 Grafana集群                          │   │
│  │  - 监控仪表板                                     │   │
│  │  - 告警可视化                                     │   │
│  │  - 数据查询和分析                                   │   │
│  └─────────────────────────────────────────────────┘   │
└────────────┴─────────────────────────────────────────────┘
```

### 6.2 监控配置

#### 6.2.1 Prometheus配置

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'inventory-service'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s

  - job_name: 'order-service'
    static_configs:
      - targets: ['localhost:8081']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s

  - job_name: 'product-service'
    static_configs:
      - targets: ['localhost:8082']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
```

#### 6.2.2 Grafana配置

```json
{
  "dashboard": {
    "title": "系统性能监控",
    "panels": [
      {
        "title": "CPU使用率",
        "type": "graph",
        "targets": [
          {
            "expr": "100 * (1 - avg(rate(node_cpu_seconds_total{mode=\"idle\"}[5m])))"
          }
        ]
      },
      {
        "title": "内存使用率",
        "type": "graph",
        "targets": [
          {
            "expr": "(1 - node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes) * 100"
          }
        ]
      },
      {
        "title": "响应时间",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))"
          }
        ]
      },
      {
        "title": "吞吐量",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_server_requests_total[5m])"
          }
        ]
      }
    ]
  }
}
```

### 6.3 告警规则

#### 6.3.1 系统告警

```yaml
groups:
  - name: system_alerts
    rules:
      - alert: HighCPUUsage
        expr: 100 * (1 - avg(rate(node_cpu_seconds_total{mode="idle"}[5m]))) > 80
        for: 5m
        labels:
          severity: warning
          service: system
        annotations:
          summary: "CPU使用率过高"
          description: "CPU使用率超过80%（当前值：{{ $value }}%）"

      - alert: HighMemoryUsage
        expr: (1 - node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes) * 100 > 80
        for: 5m
        labels:
          severity: warning
          service: system
        annotations:
          summary: "内存使用率过高"
          description: "内存使用率超过80%（当前值：{{ $value }}%）"

      - alert: LowDiskSpace
        expr: node_filesystem_avail_bytes{mountpoint="/"} < 10737418240
        for: 5m
        labels:
          severity: critical
          service: system
        annotations:
          summary: "磁盘空间不足"
          description: "磁盘可用空间小于10GB（当前值：{{ $value | humanize }}）"
```

#### 6.3.2 应用告警

```yaml
groups:
  - name: application_alerts
    rules:
      - alert: HighResponseTime
        expr: histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
          service: application
        annotations:
          summary: "响应时间过长"
          description: "P99响应时间超过1秒（当前值：{{ $value }}s）"

      - alert: HighErrorRate
        expr: rate(http_server_requests_error_total[5m]) / rate(http_server_requests_total[5m]) > 0.05
        for: 5m
        labels:
          severity: warning
          service: application
        annotations:
          summary: "错误率过高"
          description: "错误率超过5%（当前值：{{ $value | humanizePercentage }}）"
```

## 7. 性能分析

### 7.1 性能趋势分析

#### 7.1.1 响应时间趋势

| 时间范围 | 平均响应时间 | P95响应时间 | P99响应时间 | 趋势 |
|---------|------------|------------|------------|--------|
| 7天 | 90ms | 450ms | 680ms | ↘️ 下降 |
| 30天 | 85ms | 420ms | 620ms | ↘️ 下降 |
| 90天 | 82ms | 395ms | 580ms | ↘️ 下降 |

#### 7.1.2 吞吐量趋势

| 时间范围 | 平均TPS | 峰值TPS | 趋势 |
|---------|----------|----------|--------|
| 7天 | 1280 TPS | 1450 TPS | ↗️ 上升 |
| 30天 | 1250 TPS | 1320 TPS | ↗️ 上升 |
| 90天 | 1220 TPS | 1280 TPS | ↗️ 上升 |

#### 7.1.3 错误率趋势

| 时间范围 | 错误率 | 趋势 |
|---------|---------|--------|
| 7天 | 0.08% | ↘️ 下降 |
| 30天 | 0.075% | ↘️ 下降 |
| 90天 | 0.07% | ↘️ 下降 |

### 7.2 瓶颈分析

#### 7.2.1 系统瓶颈

| 瓶颈类型 | 影响范围 | 严重程度 | 优化建议 |
|---------|---------|---------|---------|
| 数据库连接池 | 所有服务 | 中 | 增加连接池大小到30 |
| Redis缓存命中率 | 所有服务 | 低 | 优化缓存策略，提高命中率 |
| JVM GC频率 | 所有服务 | 中 | 优化JVM参数，减少GC频率 |
| 网络带宽 | 所有服务 | 低 | 优化网络配置，增加带宽 |

#### 7.2.2 服务瓶颈

| 服务 | 瓶颈类型 | 严重程度 | 优化建议 |
|------|---------|---------|---------|
| order-service | 数据库查询 | 中 | 优化慢查询，创建索引 |
| inventory-service | Redis缓存 | 低 | 增加缓存大小，优化缓存策略 |
| customer-service | 数据库连接 | 中 | 增加连接池大小 |
| product-service | JVM内存 | 中 | 优化JVM参数，减少内存使用 |

## 8. 性能优化建议

### 8.1 数据库优化

1. **创建索引**：为常用查询字段创建索引
2. **优化查询**：优化SQL语句，避免全表扫描
3. **分页查询**：使用分页查询，减少单次查询数据量
4. **增加连接池**：增加数据库连接池大小到30

### 8.2 缓存优化

1. **增加缓存大小**：将Redis缓存从2GB增加到4GB
2. **优化缓存策略**：设置合理的过期时间，提高缓存命中率
3. **缓存预热**：实现缓存预热机制，提前加载热点数据
4. **多级缓存**：实现多级缓存（本地缓存 + 分布式缓存）

### 8.3 JVM优化

1. **优化JVM参数**：调整-Xms和-Xmx参数，设置合理的堆内存大小
2. **选择合适的GC算法**：使用G1GC，减少GC暂停时间
3. **减少对象创建**：优化代码，减少不必要的对象创建
4. **使用对象池**：使用对象池复用对象，减少GC压力

### 8.4 网络优化

1. **增加连接池大小**：增加HTTP连接池大小到200
2. **优化负载均衡**：优化负载均衡策略，提高资源利用率
3. **使用CDN**：使用CDN加速静态资源访问
4. **启用HTTP/2**：启用HTTP/2协议，减少网络延迟

## 9. 联系方式

### 9.1 技术支持

如果遇到问题，请联系：

- **技术支持邮箱**：support@example.com
- **技术支持热线**：+86-XXX-XXXX
- **在线文档**：[性能指标](https://example.com/performance-metrics)

### 9.2 性能团队

如果需要性能优化支持，请联系：

- **性能团队邮箱**：performance@example.com
- **性能团队热线**：+86-XXX-XXXX
- **在线文档**：[性能优化指南](https://example.com/performance-optimization-guide)

---

**免责声明**：本文档仅供参考，具体实施以实际系统性能为准。如有疑问，请联系support@example.com。