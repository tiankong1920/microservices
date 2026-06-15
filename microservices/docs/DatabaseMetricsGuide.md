# 数据库监控指标文档

## 文档概述

本文档提供企业级分布式应用数据库监控指标的完整定义，涵盖连接数、查询性能、锁等待、缓存命中率等数据库指标的监控、采集方法、告警阈值和优化建议。

## 目录

1. [数据库监控概述](#数据库监控概述)
2. [连接池监控指标](#连接池监控指标)
3. [查询性能监控指标](#查询性能监控指标)
4. [锁等待监控指标](#锁等待监控指标)
5. [缓存命中率监控指标](#缓存命中率监控指标)
6. [表空间监控指标](#表空间监控指标)
7. [告警阈值配置](#告警阈值配置)

## 数据库监控概述

### 数据库监控定义

数据库监控是对数据库运行时的性能指标进行监控，包括连接数、查询性能、锁等待、缓存命中率等。

### 数据库监控重要性

- **性能监控**：实时监控数据库性能
- **瓶颈定位**：快速定位数据库性能瓶颈
- **容量规划**：为数据库扩容提供数据支持
- **故障预警**：提前发现数据库问题

### 数据库监控范围

```yaml
database_monitoring_scope:
  connection_pool:
    - "活跃连接数"
    - "空闲连接数"
    - "等待连接数"
    - "连接使用率"
  
  query_performance:
    - "查询响应时间"
    - "慢查询数"
    - "查询吞吐量"
    - "查询错误率"
  
  lock_waits:
    - "锁等待时间"
    - "锁等待次数"
    - "死锁次数"
    - "锁超时次数"
  
  cache_hit_rate:
    - "缓存命中率"
    - "缓存大小"
    - "缓存失效次数"
  
  table_space:
    - "表空间使用率"
    - "表空间增长趋势"
    - "表空间碎片率"
```

## 连接池监控指标

### 连接数指标

#### 1. 活跃连接数

**指标名称**：`database.connections.active`

**指标描述**：数据库活跃连接数

**采集方法**：

```java
@Component
public class DatabaseConnectionMetricsCollector {

    @Autowired
    private DataSource dataSource;

    @Scheduled(fixedRate = 5000)
    public void collectActiveConnections() {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();
            
            int activeConnections = poolBean.getActiveConnections();
            
            Metrics.gauge("database.connections.active", activeConnections)
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .tag("datasource", hikariDataSource.getPoolName())
                    .register(Metrics.globalRegistry);
        }
    }
}
```

**告警阈值**：

```yaml
database_connections_active_alerts:
  warning:
    threshold: 40
    duration: "5m"
    message: "数据库活跃连接数超过40"
  critical:
    threshold: 45
    duration: "2m"
    message: "数据库活跃连接数超过45"
```

#### 2. 空闲连接数

**指标名称**：`database.connections.idle`

**指标描述**：数据库空闲连接数

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectIdleConnections() {
    if (dataSource instanceof HikariDataSource) {
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();
        
        int idleConnections = poolBean.getIdleConnections();
        
        Metrics.gauge("database.connections.idle", idleConnections)
                .tag("host", getHostname())
                .tag("application", getApplicationName())
                .tag("datasource", hikariDataSource.getPoolName())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
database_connections_idle_alerts:
  warning:
    threshold: 5
    duration: "5m"
    message: "数据库空闲连接数少于5"
  critical:
    threshold: 2
    duration: "2m"
    message: "数据库空闲连接数少于2"
```

#### 3. 等待连接数

**指标名称**：`database.connections.waiting`

**指标描述**：数据库等待连接数

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectWaitingConnections() {
    if (dataSource instanceof HikariDataSource) {
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();
        
        int threadsAwaitingConnection = poolBean.getThreadsAwaitingConnection();
        
        Metrics.gauge("database.connections.waiting", threadsAwaitingConnection)
                .tag("host", getHostname())
                .tag("application", getApplicationName())
                .tag("datasource", hikariDataSource.getPoolName())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
database_connections_waiting_alerts:
  warning:
    threshold: 10
    duration: "5m"
    message: "数据库等待连接数超过10"
  critical:
    threshold: 20
    duration: "2m"
    message: "数据库等待连接数超过20"
```

#### 4. 连接使用率

**指标名称**：`database.connections.usage`

**指标描述**：数据库连接使用率

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectConnectionUsage() {
    if (dataSource instanceof HikariDataSource) {
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();
        
        int activeConnections = poolBean.getActiveConnections();
        int totalConnections = poolBean.getTotalConnections();
        double connectionUsage = (double) activeConnections / totalConnections * 100;
        
        Metrics.gauge("database.connections.usage", connectionUsage)
                .tag("host", getHostname())
                .tag("application", getApplicationName())
                .tag("datasource", hikariDataSource.getPoolName())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
database_connections_usage_alerts:
  warning:
    threshold: 80
    duration: "5m"
    message: "数据库连接使用率超过80%"
  critical:
    threshold: 90
    duration: "2m"
    message: "数据库连接使用率超过90%"
```

## 查询性能监控指标

### 查询响应时间指标

#### 1. 平均查询响应时间

**指标名称**：`database.query.latency.average`

**指标描述**：平均查询响应时间

**采集方法**：

```java
@Component
public class DatabaseQueryMetricsCollector {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Scheduled(fixedRate = 5000)
    public void collectAverageQueryLatency() {
        try {
            String sql = "SELECT mean_exec_time FROM pg_stat_statements WHERE calls > 0";
            Double averageLatency = jdbcTemplate.queryForObject(sql, Double.class);
            
            if (averageLatency != null) {
                Metrics.gauge("database.query.latency.average", averageLatency)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .register(Metrics.globalRegistry);
            }
        } catch (Exception e) {
            log.error("Failed to collect average query latency", e);
        }
    }
}
```

**告警阈值**：

```yaml
database_query_latency_average_alerts:
  warning:
    threshold: 100
    duration: "5m"
    message: "平均查询响应时间超过100ms"
  critical:
    threshold: 500
    duration: "2m"
    message: "平均查询响应时间超过500ms"
```

#### 2. P95查询响应时间

**指标名称**：`database.query.latency.p95`

**指标描述**：P95查询响应时间

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectP95QueryLatency() {
    try {
        String sql = "SELECT percentile_disc(0.95) WITHIN GROUP (mean_exec_time) "
                   + "FROM pg_stat_statements WHERE calls > 0";
        Double p95Latency = jdbcTemplate.queryForObject(sql, Double.class);
        
        if (p95Latency != null) {
            Metrics.gauge("database.query.latency.p95", p95Latency)
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .register(Metrics.globalRegistry);
        }
    } catch (Exception e) {
        log.error("Failed to collect P95 query latency", e);
    }
}
```

**告警阈值**：

```yaml
database_query_latency_p95_alerts:
  warning:
    threshold: 500
    duration: "5m"
    message: "P95查询响应时间超过500ms"
  critical:
    threshold: 1000
    duration: "2m"
    message: "P95查询响应时间超过1000ms"
```

### 慢查询指标

#### 1. 慢查询数

**指标名称**：`database.query.slow.count`

**指标描述**：慢查询数

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectSlowQueryCount() {
    try {
        String sql = "SELECT COUNT(*) FROM pg_stat_statements "
                   + "WHERE mean_exec_time > 1000 AND calls > 0";
        Integer slowQueryCount = jdbcTemplate.queryForObject(sql, Integer.class);
        
        if (slowQueryCount != null) {
            Metrics.gauge("database.query.slow.count", slowQueryCount)
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .register(Metrics.globalRegistry);
        }
    } catch (Exception e) {
        log.error("Failed to collect slow query count", e);
    }
}
```

**告警阈值**：

```yaml
database_query_slow_count_alerts:
  warning:
    threshold: 10
    duration: "5m"
    message: "慢查询数超过10"
  critical:
    threshold: 50
    duration: "2m"
    message: "慢查询数超过50"
```

#### 2. 慢查询日志

**指标名称**：`database.query.slow.log`

**指标描述**：慢查询日志

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectSlowQueryLog() {
    try {
        String sql = "SELECT queryid, calls, total_time, mean_time, rows "
                   + "FROM pg_stat_statements "
                   + "WHERE mean_exec_time > 1000 AND calls > 0 "
                   + "ORDER BY total_time DESC LIMIT 10";
        
        List<Map<String, Object>> slowQueries = jdbcTemplate.queryForList(sql);
        
        for (Map<String, Object> query : slowQueries) {
            String queryId = (String) query.get("queryid");
            Long calls = (Long) query.get("calls");
            Double totalTime = (Double) query.get("total_time");
            Double meanTime = (Double) query.get("mean_time");
            Long rows = (Long) query.get("rows");
            
            Metrics.gauge("database.query.slow.log.time", meanTime)
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .tag("queryid", queryId)
                    .register(Metrics.globalRegistry);
            
            Metrics.gauge("database.query.slow.log.calls", calls)
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .tag("queryid", queryId)
                    .register(Metrics.globalRegistry);
        }
    } catch (Exception e) {
        log.error("Failed to collect slow query log", e);
    }
}
```

**告警阈值**：

```yaml
database_query_slow_log_alerts:
  warning:
    threshold: 1000
    duration: "5m"
    message: "慢查询平均时间超过1000ms"
  critical:
    threshold: 5000
    duration: "2m"
    message: "慢查询平均时间超过5000ms"
```

### 查询吞吐量指标

#### 1. 查询吞吐量

**指标名称**：`database.query.throughput`

**指标描述**：查询吞吐量（QPS）

**采集方法**：

```java
@Component
public class DatabaseThroughputMetricsCollector {

    private final AtomicLong queryCount = new AtomicLong(0);
    private final AtomicLong lastTimestamp = new AtomicLong(System.currentTimeMillis());

    @Scheduled(fixedRate = 5000)
    public void collectQueryThroughput() {
        long currentTimestamp = System.currentTimeMillis();
        long timeDiff = currentTimestamp - lastTimestamp.get();
        
        if (timeDiff > 0) {
            long currentCount = queryCount.get();
            long previousCount = queryCount.getAndSet(0);
            long queryCountDiff = currentCount - previousCount;
            
            double throughput = (double) queryCountDiff / (timeDiff / 1000.0);
            
            Metrics.gauge("database.query.throughput", throughput)
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .register(Metrics.globalRegistry);
            
            lastTimestamp.set(currentTimestamp);
        }
    }

    public void incrementQueryCount() {
        queryCount.incrementAndGet();
    }
}
```

**告警阈值**：

```yaml
database_query_throughput_alerts:
  warning:
    threshold: 100
    duration: "5m"
    message: "查询吞吐量低于100 QPS"
  critical:
    threshold: 50
    duration: "2m"
    message: "查询吞吐量低于50 QPS"
```

## 锁等待监控指标

### 锁等待时间指标

#### 1. 平均锁等待时间

**指标名称**：`database.lock.wait.average`

**指标描述**：平均锁等待时间

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectAverageLockWait() {
    try {
        String sql = "SELECT AVG(lockwait) FROM pg_stat_activity "
                   + "WHERE lockwait IS NOT NULL";
        Double averageLockWait = jdbcTemplate.queryForObject(sql, Double.class);
        
        if (averageLockWait != null) {
            Metrics.gauge("database.lock.wait.average", averageLockWait)
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .register(Metrics.globalRegistry);
        }
    } catch (Exception e) {
        log.error("Failed to collect average lock wait", e);
    }
}
```

**告警阈值**：

```yaml
database_lock_wait_average_alerts:
  warning:
    threshold: 100
    duration: "5m"
    message: "平均锁等待时间超过100ms"
  critical:
    threshold: 500
    duration: "2m"
    message: "平均锁等待时间超过500ms"
```

#### 2. 锁等待次数

**指标名称**：`database.lock.wait.count`

**指标描述**：锁等待次数

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectLockWaitCount() {
    try {
        String sql = "SELECT COUNT(*) FROM pg_stat_activity "
                   + "WHERE lockwait IS NOT NULL";
        Integer lockWaitCount = jdbcTemplate.queryForObject(sql, Integer.class);
        
        if (lockWaitCount != null) {
            Metrics.gauge("database.lock.wait.count", lockWaitCount)
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .register(Metrics.globalRegistry);
        }
    } catch (Exception e) {
        log.error("Failed to collect lock wait count", e);
    }
}
```

**告警阈值**：

```yaml
database_lock_wait_count_alerts:
  warning:
    threshold: 10
    duration: "5m"
    message: "锁等待次数超过10"
  critical:
    threshold: 50
    duration: "2m"
    message: "锁等待次数超过50"
```

### 死锁指标

#### 1. 死锁次数

**指标名称**：`database.lock.deadlock.count`

**指标描述**：死锁次数

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectDeadlockCount() {
    try {
        String sql = "SELECT COUNT(*) FROM pg_stat_database_deadlocks";
        Integer deadlockCount = jdbcTemplate.queryForObject(sql, Integer.class);
        
        if (deadlockCount != null) {
            Metrics.gauge("database.lock.deadlock.count", deadlockCount)
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .register(Metrics.globalRegistry);
        }
    } catch (Exception e) {
        log.error("Failed to collect deadlock count", e);
    }
}
```

**告警阈值**：

```yaml
database_lock_deadlock_count_alerts:
  warning:
    threshold: 1
    duration: "5m"
    message: "检测到死锁"
  critical:
    threshold: 5
    duration: "2m"
    message: "死锁次数超过5"
```

## 缓存命中率监控指标

### 缓存命中率指标

#### 1. 缓存命中率

**指标名称**：`database.cache.hit.rate`

**指标描述**：数据库缓存命中率

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectCacheHitRate() {
    try {
        String sql = "SELECT SUM(heap_blks_hit) AS hits, "
                   + "SUM(heap_blks_read) AS reads "
                   + "FROM pg_statio_user_tables";
        
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        Long hits = ((Number) result.get("hits")).longValue();
        Long reads = ((Number) result.get("reads")).longValue();
        
        if (reads > 0) {
            double hitRate = (double) hits / (hits + reads) * 100;
            
            Metrics.gauge("database.cache.hit.rate", hitRate)
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .register(Metrics.globalRegistry);
        }
    } catch (Exception e) {
        log.error("Failed to collect cache hit rate", e);
    }
}
```

**告警阈值**：

```yaml
database_cache_hit_rate_alerts:
  warning:
    threshold: 80
    duration: "5m"
    message: "数据库缓存命中率低于80%"
  critical:
    threshold: 70
    duration: "2m"
    message: "数据库缓存命中率低于70%"
```

#### 2. 缓存大小

**指标名称**：`database.cache.size`

**指标描述**：数据库缓存大小

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectCacheSize() {
    try {
        String sql = "SELECT SUM(heap_blks_hit) AS hits, "
                   + "SUM(heap_blks_read) AS reads, "
                   + "SUM(blks_read) * 8192 AS cache_size "
                   + "FROM pg_statio_user_tables";
        
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        Long cacheSize = ((Number) result.get("cache_size")).longValue();
        
        Metrics.gauge("database.cache.size", cacheSize)
                .tag("host", getHostname())
                .tag("application", getApplicationName())
                .register(Metrics.globalRegistry);
    } catch (Exception e) {
        log.error("Failed to collect cache size", e);
    }
}
```

**告警阈值**：

```yaml
database_cache_size_alerts:
  warning:
    threshold: 1073741824
    duration: "5m"
    message: "数据库缓存大小超过1GB"
  critical:
    threshold: 2147483648
    duration: "2m"
    message: "数据库缓存大小超过2GB"
```

## 表空间监控指标

### 表空间使用率指标

#### 1. 表空间使用率

**指标名称**：`database.tablespace.usage`

**指标描述**：表空间使用率

**采集方法**：

```java
@Scheduled(fixedRate = 60000)
public void collectTablespaceUsage() {
    try {
        String sql = "SELECT pg_database.datname AS database_name, "
                   + "pg_size_pretty(pg_database_size(pg_database.datname)) AS size, "
                   + "pg_tablespace.spcname AS tablespace_name "
                   + "FROM pg_database "
                   + "JOIN pg_tablespace ON pg_database.dattablespace = pg_tablespace.oid "
                   + "WHERE pg_database.datistemplate = false";
        
        List<Map<String, Object>> tablespaces = jdbcTemplate.queryForList(sql);
        
        for (Map<String, Object> tablespace : tablespaces) {
            String databaseName = (String) tablespace.get("database_name");
            String tablespaceName = (String) tablespace.get("tablespace_name");
            String size = (String) tablespace.get("size");
            
            Metrics.gauge("database.tablespace.usage", parseSizeToBytes(size))
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .tag("database", databaseName)
                    .tag("tablespace", tablespaceName)
                    .register(Metrics.globalRegistry);
        }
    } catch (Exception e) {
        log.error("Failed to collect tablespace usage", e);
    }
}

private long parseSizeToBytes(String size) {
    try {
        size = size.trim().toUpperCase();
        long multiplier = 1;
        
        if (size.endsWith("KB")) {
            multiplier = 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("MB")) {
            multiplier = 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("GB")) {
            multiplier = 1024 * 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("TB")) {
            multiplier = 1024L * 1024 * 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        }
        
        return Long.parseLong(size) * multiplier;
    } catch (Exception e) {
        return 0;
    }
}
```

**告警阈值**：

```yaml
database_tablespace_usage_alerts:
  warning:
    threshold: 8589934592
    duration: "5m"
    message: "表空间使用率超过80%"
  critical:
    threshold: 9663676416
    duration: "2m"
    message: "表空间使用率超过90%"
```

#### 2. 表空间增长趋势

**指标名称**：`database.tablespace.growth`

**指标描述**：表空间增长趋势

**采集方法**：

```java
@Component
public class TablespaceGrowthMetricsCollector {

    private final Map<String, Long> previousTablespaceSizes = new ConcurrentHashMap<>();

    @Scheduled(fixedRate = 60000)
    public void collectTablespaceGrowth() {
        try {
            String sql = "SELECT pg_database.datname AS database_name, "
                       + "pg_database_size(pg_database.datname) AS size "
                       + "FROM pg_database "
                       + "WHERE pg_database.datistemplate = false";
            
            List<Map<String, Object>> databases = jdbcTemplate.queryForList(sql);
            
            for (Map<String, Object> database : databases) {
                String databaseName = (String) database.get("database_name");
                Long size = ((Number) database.get("size")).longValue();
                Long previousSize = previousTablespaceSizes.get(databaseName);
                
                if (previousSize != null && size > previousSize) {
                    long growth = size - previousSize;
                    double growthRate = (double) growth / previousSize * 100;
                    
                    Metrics.gauge("database.tablespace.growth", growthRate)
                            .tag("host", getHostname())
                            .tag("application", getApplicationName())
                            .tag("database", databaseName)
                            .register(Metrics.globalRegistry);
                }
                
                previousTablespaceSizes.put(databaseName, size);
            }
        } catch (Exception e) {
            log.error("Failed to collect tablespace growth", e);
        }
    }
}
```

**告警阈值**：

```yaml
database_tablespace_growth_alerts:
  warning:
    threshold: 10
    duration: "1d"
    message: "表空间增长率超过10%"
  critical:
    threshold: 20
    duration: "12h"
    message: "表空间增长率超过20%"
```

## 告警阈值配置

### 告警阈值配置文件

```yaml
database_alerts:
  connection_pool:
    active:
      warning:
        threshold: 40
        duration: "5m"
        message: "数据库活跃连接数超过40"
      critical:
        threshold: 45
        duration: "2m"
        message: "数据库活跃连接数超过45"
    idle:
      warning:
        threshold: 5
        duration: "5m"
        message: "数据库空闲连接数少于5"
      critical:
        threshold: 2
        duration: "2m"
        message: "数据库空闲连接数少于2"
    waiting:
      warning:
        threshold: 10
        duration: "5m"
        message: "数据库等待连接数超过10"
      critical:
        threshold: 20
        duration: "2m"
        message: "数据库等待连接数超过20"
    usage:
      warning:
        threshold: 80
        duration: "5m"
        message: "数据库连接使用率超过80%"
      critical:
        threshold: 90
        duration: "2m"
        message: "数据库连接使用率超过90%"
  
  query_performance:
    latency_average:
      warning:
        threshold: 100
        duration: "5m"
        message: "平均查询响应时间超过100ms"
      critical:
        threshold: 500
        duration: "2m"
        message: "平均查询响应时间超过500ms"
    latency_p95:
      warning:
        threshold: 500
        duration: "5m"
        message: "P95查询响应时间超过500ms"
      critical:
        threshold: 1000
        duration: "2m"
        message: "P95查询响应时间超过1000ms"
    slow_count:
      warning:
        threshold: 10
        duration: "5m"
        message: "慢查询数超过10"
      critical:
        threshold: 50
        duration: "2m"
        message: "慢查询数超过50"
    throughput:
      warning:
        threshold: 100
        duration: "5m"
        message: "查询吞吐量低于100 QPS"
      critical:
        threshold: 50
        duration: "2m"
        message: "查询吞吐量低于50 QPS"
  
  lock_waits:
    wait_average:
      warning:
        threshold: 100
        duration: "5m"
        message: "平均锁等待时间超过100ms"
      critical:
        threshold: 500
        duration: "2m"
        message: "平均锁等待时间超过500ms"
    wait_count:
      warning:
        threshold: 10
        duration: "5m"
        message: "锁等待次数超过10"
      critical:
        threshold: 50
        duration: "2m"
        message: "锁等待次数超过50"
    deadlock_count:
      warning:
        threshold: 1
        duration: "5m"
        message: "检测到死锁"
      critical:
        threshold: 5
        duration: "2m"
        message: "死锁次数超过5"
  
  cache_hit_rate:
    hit_rate:
      warning:
        threshold: 80
        duration: "5m"
        message: "数据库缓存命中率低于80%"
      critical:
        threshold: 70
        duration: "2m"
        message: "数据库缓存命中率低于70%"
    size:
      warning:
        threshold: 1073741824
        duration: "5m"
        message: "数据库缓存大小超过1GB"
      critical:
        threshold: 2147483648
        duration: "2m"
        message: "数据库缓存大小超过2GB"
  
  table_space:
    usage:
      warning:
        threshold: 8589934592
        duration: "5m"
        message: "表空间使用率超过80%"
      critical:
        threshold: 9663676416
        duration: "2m"
        message: "表空间使用率超过90%"
    growth:
      warning:
        threshold: 10
        duration: "1d"
        message: "表空间增长率超过10%"
      critical:
        threshold: 20
        duration: "12h"
        message: "表空间增长率超过20%"
```

## 相关文档

- [系统级监控指标文档](SystemMetricsGuide.md)
- [应用级监控指标文档](ApplicationMetricsGuide.md)
- [业务级监控指标文档](BusinessMetricsGuide.md)
- [缓存监控指标文档](CacheMetricsGuide.md)

## 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|---------|
| 1.0.0 | 2026-01-19 | System | 初始版本 |