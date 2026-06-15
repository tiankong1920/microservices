# 缓存监控指标文档

## 文档概述

本文档提供企业级分布式应用缓存监控指标的完整定义，涵盖Redis缓存、本地缓存、缓存命中率、缓存大小等缓存指标的监控、采集方法、告警阈值和优化建议。

## 目录

1. [缓存监控概述](#缓存监控概述)
2. [Redis缓存监控指标](#redis缓存监控指标)
3. [本地缓存监控指标](#本地缓存监控指标)
4. [缓存命中率监控指标](#缓存命中率监控指标)
5. [缓存大小监控指标](#缓存大小监控指标)
6. [缓存失效监控指标](#缓存失效监控指标)
7. [告警阈值配置](#告警阈值配置)

## 缓存监控概述

### 缓存监控定义

缓存监控是对缓存系统运行时的性能指标进行监控，包括Redis缓存、本地缓存、缓存命中率、缓存大小等。

### 缓存监控重要性

- **性能监控**：实时监控缓存性能
- **命中率优化**：提高缓存命中率，减少数据库压力
- **容量规划**：为缓存扩容提供数据支持
- **故障预警**：提前发现缓存问题

### 缓存监控范围

```yaml
cache_monitoring_scope:
  redis_cache:
    - "连接数"
    - "内存使用率"
    - "命中率"
    - "响应时间"
    - "命令执行数"
    - "过期键数"
    - "驱逐键数"
  
  local_cache:
    - "缓存大小"
    - "命中率"
    - "驱逐数"
    - "加载时间"
    - "平均加载时间"
  
  cache_metrics:
    - "命中率"
    - "平均响应时间"
    - "P95响应时间"
    - "P99响应时间"
    - "吞吐量"
    - "错误率"
```

## Redis缓存监控指标

### Redis连接指标

#### 1. 连接数

**指标名称**：`redis.connections.count`

**指标描述**：Redis连接数

**采集方法**：

```java
@Component
public class RedisConnectionMetricsCollector {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Scheduled(fixedRate = 5000)
    public void collectConnectionCount() {
        try {
            String info = redisTemplate.execute((RedisCallback<String>) connection -> {
                return ((RedisServerCommands) connection).info("server");
            });
            
            String[] lines = info.split("\\r?\\n");
            for (String line : lines) {
                if (line.startsWith("connected_clients:")) {
                    String[] parts = line.split(":");
                    int connectionCount = Integer.parseInt(parts[1].trim());
                    
                    Metrics.gauge("redis.connections.count", connectionCount)
                            .tag("host", getHostname())
                            .tag("application", getApplicationName())
                            .register(Metrics.globalRegistry);
                }
            }
        } catch (Exception e) {
            log.error("Failed to collect Redis connection count", e);
        }
    }
}
```

**告警阈值**：

```yaml
redis_connections_count_alerts:
  warning:
    threshold: 80
    duration: "5m"
    message: "Redis连接数超过80"
  critical:
    threshold: 90
    duration: "2m"
    message: "Redis连接数超过90"
```

#### 2. 拒绝连接数

**指标名称**：`redis.connections.rejected`

**指标描述**：Redis拒绝连接数

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectRejectedConnections() {
    try {
        String info = redisTemplate.execute((RedisCallback<String>) connection -> {
            return ((RedisServerCommands) connection).info("stats");
        });
        
        String[] lines = info.split("\\r?\\n");
        for (String line : lines) {
            if (line.startsWith("rejected_connections:")) {
                String[] parts = line.split(":");
                int rejectedConnections = Integer.parseInt(parts[1].trim());
                
                Metrics.counter("redis.connections.rejected")
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .increment(rejectedConnections, metrics.globalRegistry);
            }
        }
    } catch (Exception e) {
        log.error("Failed to collect rejected connections", e);
    }
}
```

**告警阈值**：

```yaml
redis_connections_rejected_alerts:
  warning:
    threshold: 10
    duration: "5m"
    message: "Redis拒绝连接数超过10/分钟"
  critical:
    threshold: 50
    duration: "2m"
    message: "Redis拒绝连接数超过50/分钟"
```

### Redis内存指标

#### 1. 内存使用率

**指标名称**：`redis.memory.usage`

**指标描述**：Redis内存使用率

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectMemoryUsage() {
    try {
        String info = redisTemplate.execute((RedisCallback<String>) connection -> {
            return ((RedisServerCommands) connection).info("memory");
        });
        
        String[] lines = info.split("\\r?\\n");
        for (String line : lines) {
            if (line.startsWith("used_memory:")) {
                String[] parts = line.split(":");
                long usedMemory = Long.parseLong(parts[1].trim());
                
                Metrics.gauge("redis.memory.used", usedMemory)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .register(metrics.globalRegistry);
            } else if (line.startsWith("maxmemory:")) {
                String[] parts = line.split(":");
                long maxMemory = Long.parseLong(parts[1].trim());
                
                Metrics.gauge("redis.memory.max", maxMemory)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .register(metrics.globalRegistry);
                
                double memoryUsage = maxMemory > 0 ? 
                        (double) usedMemory / maxMemory * 100 : 0;
                
                Metrics.gauge("redis.memory.usage", memoryUsage)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .register(metrics.globalRegistry);
            }
        }
    } catch (Exception e) {
        log.error("Failed to collect memory usage", e);
    }
}
```

**告警阈值**：

```yaml
redis_memory_usage_alerts:
  warning:
    threshold: 70
    duration: "5m"
    message: "Redis内存使用率超过70%"
  critical:
    threshold: 85
    duration: "2m"
    message: "Redis内存使用率超过85%"
```

#### 2. 内存碎片率

**指标名称**：`redis.memory.fragmentation`

**指标描述**：Redis内存碎片率

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectMemoryFragmentation() {
    try {
        String info = redisTemplate.execute((RedisCallback<String>) connection -> {
            return ((RedisServerCommands) connection).info("memory");
        });
        
        String[] lines = info.split("\\r?\\n");
        for (String line : lines) {
            if (line.startsWith("mem_fragmentation_ratio:")) {
                String[] parts = line.split(":");
                double fragmentationRatio = Double.parseDouble(parts[1].trim());
                
                Metrics.gauge("redis.memory.fragmentation", fragmentationRatio)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .register(metrics.globalRegistry);
            }
        }
    } catch (Exception e) {
        log.error("Failed to collect memory fragmentation", e);
    }
}
```

**告警阈值**：

```yaml
redis_memory_fragmentation_alerts:
  warning:
    threshold: 1.5
    duration: "5m"
    message: "Redis内存碎片率超过1.5"
  critical:
    threshold: 2.0
    duration: "2m"
    message: "Redis内存碎片率超过2.0"
```

### Redis性能指标

#### 1. 命中率

**指标名称**：`redis.cache.hit.rate`

**指标描述**：Redis缓存命中率

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectCacheHitRate() {
    try {
        String info = redisTemplate.execute((RedisCallback<String>) connection -> {
            return ((RedisServerCommands) connection).info("stats");
        });
        
        String[] lines = info.split("\\r?\\n");
        for (String line : lines) {
            if (line.startsWith("keyspace_hits:")) {
                String[] parts = line.split(":");
                long hits = Long.parseLong(parts[1].trim());
                
                Metrics.gauge("redis.cache.hit.count", hits)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .register(metrics.globalRegistry);
            } else if (line.startsWith("keyspace_misses:")) {
                String[] parts = line.split(":");
                long misses = Long.parseLong(parts[1].trim());
                
                Metrics.gauge("redis.cache.miss.count", misses)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .register(metrics.globalRegistry);
                
                long totalRequests = hits + misses;
                double hitRate = totalRequests > 0 ? 
                        (double) hits / totalRequests * 100 : 0;
                
                Metrics.gauge("redis.cache.hit.rate", hitRate)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .register(metrics.globalRegistry);
            }
        }
    } catch (Exception e) {
        log.error("Failed to collect cache hit rate", e);
    }
}
```

**告警阈值**：

```yaml
redis_cache_hit_rate_alerts:
  warning:
    threshold: 80
    duration: "5m"
    message: "Redis缓存命中率低于80%"
  critical:
    threshold: 70
    duration: "2m"
    message: "Redis缓存命中率低于70%"
```

#### 2. 平均响应时间

**指标名称**：`redis.response.time.average`

**指标描述**：Redis平均响应时间

**采集方法**：

```java
@Component
public class RedisPerformanceMetricsCollector {

    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final AtomicLong requestCount = new AtomicLong(0);

    @Scheduled(fixedRate = 5000)
    public void collectAverageResponseTime() {
        long currentTotal = totalResponseTime.get();
        long currentCount = requestCount.get();
        
        if (currentCount > 0) {
            double averageResponseTime = (double) currentTotal / currentCount;
            
            Metrics.gauge("redis.response.time.average", averageResponseTime)
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .register(metrics.globalRegistry);
        }
        
        totalResponseTime.set(0);
        requestCount.set(0);
    }

    public void recordResponseTime(long responseTime) {
        totalResponseTime.addAndGet(responseTime);
        requestCount.incrementAndGet();
    }
}
```

**告警阈值**：

```yaml
redis_response_time_average_alerts:
  warning:
    threshold: 10
    duration: "5m"
    message: "Redis平均响应时间超过10ms"
  critical:
    threshold: 50
    duration: "2m"
    message: "Redis平均响应时间超过50ms"
```

#### 3. P95响应时间

**指标名称**：`redis.response.time.p95`

**指标描述**：Redis P95响应时间

**采集方法**：

```java
@Component
public class RedisResponseTimeCollector {

    private final List<Long> responseTimes = new CopyOnWriteArrayList<>();

    @Scheduled(fixedRate = 5000)
    public void collectP95ResponseTime() {
        if (responseTimes.isEmpty()) {
            return;
        }
        
        List<Long> sortedTimes = new ArrayList<>(responseTimes);
        Collections.sort(sortedTimes);
        
        int p95Index = (int) Math.ceil(sortedTimes.size() * 0.95) - 1;
        long p95ResponseTime = sortedTimes.get(p95Index);
        
        Metrics.gauge("redis.response.time.p95", p95ResponseTime)
                .tag("host", getHostname())
                .tag("application", getApplicationName())
                .register(metrics.globalRegistry);
        
        responseTimes.clear();
    }

    public void recordResponseTime(long responseTime) {
        responseTimes.add(responseTime);
    }
}
```

**告警阈值**：

```yaml
redis_response_time_p95_alerts:
  warning:
    threshold: 50
    duration: "5m"
    message: "Redis P95响应时间超过50ms"
  critical:
    threshold: 100
    duration: "2m"
    message: "Redis P95响应时间超过100ms"
```

### Redis命令指标

#### 1. 命令执行数

**指标名称**：`redis.commands.count`

**指标描述**：Redis命令执行数

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectCommandCount() {
    try {
        String info = redisTemplate.execute((RedisCallback<String>) connection -> {
            return ((RedisServerCommands) connection).info("stats");
        });
        
        String[] lines = info.split("\\r?\\n");
        for (String line : lines) {
            if (line.startsWith("total_commands_processed:")) {
                String[] parts = line.split(":");
                long commandCount = Long.parseLong(parts[1].trim());
                
                Metrics.counter("redis.commands.count")
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .increment(commandCount, metrics.globalRegistry);
            }
        }
    } catch (Exception e) {
        log.error("Failed to collect command count", e);
    }
}
```

**告警阈值**：

```yaml
redis_commands_count_alerts:
  warning:
    threshold: 10000
    duration: "5m"
    message: "Redis命令执行数低于10000/分钟"
  critical:
    threshold: 5000
    duration: "2m"
    message: "Redis命令执行数低于5000/分钟"
```

#### 2. 慢查询数

**指标名称**：`redis.commands.low.count`

**指标描述**：Redis慢查询数

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectSlowCommandCount() {
    try {
        String info = redisTemplate.execute((RedisCallback<String>) connection -> {
            return ((RedisServerCommands) connection).info("stats");
        });
        
        String[] lines = info.split("\\r?\\n");
        for (String line : lines) {
            if (line.startsWith("instantaneous_ops_per_sec:")) {
                String[] parts = line.split(":");
                double opsPerSec = Double.parseDouble(parts[1].trim());
                
                if (opsPerSec < 100) {
                    Metrics.gauge("redis.commands.low.count", opsPerSec)
                            .tag("host", getHostname())
                            .tag("application", getApplicationName())
                            .register(metrics.globalRegistry);
                }
            }
        }
    } catch (Exception e) {
        log.error("Failed to collect slow command count", e);
    }
}
```

**告警阈值**：

```yaml
redis_commands_slow_count_alerts:
  warning:
    threshold: 100
    duration: "5m"
    message: "Redis慢查询数超过100"
  critical:
    threshold: 500
    duration: "2m"
    message: "Redis慢查询数超过500"
```

## 本地缓存监控指标

### 本地缓存大小指标

#### 1. 缓存大小

**指标名称**：`local.cache.size`

**指标描述**：本地缓存大小

**采集方法**：

```java
@Component
public class LocalCacheMetricsCollector {

    @Autowired
    private CacheManager cacheManager;

    @Scheduled(fixedRate = 5000)
    public void collectCacheSize() {
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            
            if (cache instanceof Ehcache) {
                Ehcache ehcache = (Ehcache) cache;
                Statistics statistics = ehcache.getStatistics();
                
                long cacheSize = statistics.getSize();
                
                Metrics.gauge("local.cache.size", cacheSize)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .tag("cache", cacheName)
                        .register(metrics.globalRegistry);
            }
        }
    }
}
```

**告警阈值**：

```yaml
local_cache_size_alerts:
  warning:
    threshold: 10000
    duration: "5m"
    message: "本地缓存大小超过10000"
  critical:
    threshold: 50000
    duration: "2m"
    message: "本地缓存大小超过50000"
```

#### 2. 缓存驱逐数

**指标名称**：`local.cache.eviction.count`

**指标描述**：本地缓存驱逐数

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectEvictionCount() {
    for (String cacheName : cacheManager.getCacheNames()) {
        Cache cache = cacheManager.getCache(cacheName);
        
        if (cache instanceof Ehcache) {
            Ehcache ehcache = (Ehcache) cache;
            Statistics statistics = ehcache.getStatistics();
            
            long evictionCount = statistics.cacheEvictedCount();
            
            Metrics.counter("local.cache.eviction.count")
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .tag("cache", cacheName)
                    .increment(evictionCount, metrics.globalRegistry);
        }
    }
}
```

**告警阈值**：

```yaml
local_cache_eviction_count_alerts:
  warning:
    threshold: 100
    duration: "5m"
    message: "本地缓存驱逐数超过100/分钟"
  critical:
    threshold: 500
    duration: "2m"
    message: "本地缓存驱逐数超过500/分钟"
```

### 本地缓存性能指标

#### 1. 缓存命中率

**指标名称**：`local.cache.hit.rate`

**指标描述**：本地缓存命中率

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectLocalCacheHitRate() {
    for (String cacheName : cacheManager.getCacheNames()) {
        Cache cache = cacheManager.getCache(cacheName);
        
        if (cache instanceof Ehcache) {
            Ehcache ehcache = (Ehcache) cache;
            Statistics statistics = ehcache.getStatistics();
            
            long cacheHits = statistics.cacheHitCount();
            long cacheMisses = statistics.cacheMissCount();
            
            long totalRequests = cacheHits + cacheMisses;
            double hitRate = totalRequests > 0 ? 
                    (double) cacheHits / totalRequests * 100 : 0;
            
            Metrics.gauge("local.cache.hit.rate", hitRate)
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .tag("cache", cacheName)
                    .register(metrics.globalRegistry);
        }
    }
}
```

**告警阈值**：

```yaml
local_cache_hit_rate_alerts:
  warning:
    threshold: 80
    duration: "5m"
    message: "本地缓存命中率低于80%"
  critical:
    threshold: 70
    duration: "2m"
    message: "本地缓存命中率低于70%"
```

#### 2. 平均加载时间

**指标名称**：`local.cache.load.time.average`

**指标描述**：本地缓存平均加载时间

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectAverageLoadTime() {
    for (String cacheName : cacheManager.getCacheNames()) {
        Cache cache = cacheManager.getCache(cacheName);
        
        if (cache instanceof Ehcache) {
            Ehcache ehcache = (Ehcache) cache;
            Statistics statistics = ehcache.getStatistics();
            
            double averageLoadTime = statistics.getAverageLoadTime();
            
            Metrics.gauge("local.cache.load.time.average", averageLoadTime)
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .tag("cache", cacheName)
                    .register(metrics.globalRegistry);
        }
    }
}
```

**告警阈值**：

```yaml
local_cache_load_time_average_alerts:
  warning:
    threshold: 100
    duration: "5m"
    message: "本地缓存平均加载时间超过100ms"
  critical:
    threshold: 500
    duration: "2m"
    message: "本地缓存平均加载时间超过500ms"
```

## 缓存命中率监控指标

### Redis缓存命中率

#### 1. 整体命中率

**指标名称**：`redis.cache.hit.rate.overall`

**指标描述**：Redis整体缓存命中率

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectOverallCacheHitRate() {
    try {
        String info = redisTemplate.execute((RedisCallback<String>) connection -> {
            return ((RedisServerCommands) connection).info("stats");
        });
        
        String[] lines = info.split("\\r?\\n");
        for (String line : lines) {
            if (line.startsWith("keyspace_hits:")) {
                String[] parts = line.split(":");
                long hits = Long.parseLong(parts[1].trim());
                
                Metrics.gauge("redis.cache.hit.count.overall", hits)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .register(metrics.globalRegistry);
            } else if (line.startsWith("keyspace_misses:")) {
                String[] parts = line.split(":");
                long misses = Long.parseLong(parts[1].trim());
                
                Metrics.gauge("redis.cache.miss.count.overall", misses)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .register(metrics.globalRegistry);
                
                long totalRequests = hits + misses;
                double hitRate = totalRequests > 0 ? 
                        (double) hits / totalRequests * 100 : 0;
                
                Metrics.gauge("redis.cache.hit.rate.overall", hitRate)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .register(metrics.globalRegistry);
            }
        }
    } catch (Exception e) {
        log.error("Failed to collect overall cache hit rate", e);
    }
}
```

**告警阈值**：

```yaml
redis_cache_hit_rate_overall_alerts:
  warning:
    threshold: 80
    duration: "5m"
    message: "Redis整体缓存命中率低于80%"
  critical:
    threshold: 70
    duration: "2m"
    message: "Redis整体缓存命中率低于70%"
```

#### 2. Key级别命中率

**指标名称**：`redis.cache.hit.rate.bykey`

**指标描述**：Redis Key级别缓存命中率

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectKeyLevelCacheHitRate() {
    try {
        String info = redisTemplate.execute((RedisCallback<String>) connection -> {
            return ((RedisServerCommands) connection).info("stats");
        });
        
        String[] lines = info.split("\\r?\\n");
        for (String line : lines) {
            if (line.startsWith("keyspace_hits:")) {
                String[] parts = line.split(":");
                long hits = Long.parseLong(parts[1].trim());
                
                Metrics.gauge("redis.cache.hit.count.bykey", hits)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .register(metrics.globalRegistry);
            } else if (line.startsWith("keyspace_misses:")) {
                String[] parts = line.split(":");
                long misses = Long.parseLong(parts[1].trim());
                
                Metrics.gauge("redis.cache.miss.count.bykey", misses)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .register(metrics.globalRegistry);
                
                long totalRequests = hits + misses;
                double hitRate = totalRequests > 0 ? 
                        (double) hits / totalRequests * 100 : 0;
                
                Metrics.gauge("redis.cache.hit.rate.bykey", hitRate)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .register(metrics.globalRegistry);
            }
        }
    } catch (Exception e) {
        log.error("Failed to collect key level cache hit rate", e);
    }
}
```

**告警阈值**：

```yaml
redis_cache_hit_rate_bykey_alerts:
  warning:
    threshold: 75
    duration: "5m"
    message: "Redis Key级别缓存命中率低于75%"
  critical:
    threshold: 65
    duration: "2m"
    message: "Redis Key级别缓存命中率低于65%"
```

## 缓存大小监控指标

### Redis缓存大小指标

#### 1. Key数量

**指标名称**：`redis.keys.count`

**指标描述**：Redis Key数量

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectKeyCount() {
    try {
        Long keyCount = redisTemplate.execute((RedisCallback<Long>) connection -> {
            return ((RedisKeyCommands) connection).dbSize();
        });
        
        Metrics.gauge("redis.keys.count", keyCount)
                .tag("host", getHostname())
                .tag("application", getApplicationName())
                .register(metrics.globalRegistry);
    } catch (Exception e) {
        log.error("Failed to collect key count", e);
    }
}
```

**告警阈值**：

```yaml
redis_keys_count_alerts:
  warning:
    threshold: 100000
    duration: "5m"
    message: "Redis Key数量超过100000"
  critical:
    threshold: 200000
    duration: "2m"
    message: "Redis Key数量超过200000"
```

#### 2. 内存使用量

**指标名称**：`redis.memory.used.bytes`

**指标描述**：Redis内存使用量

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectMemoryUsed() {
    try {
        String info = redisTemplate.execute((RedisCallback<String>) connection -> {
            return ((RedisServerCommands) connection).info("memory");
        });
        
        String[] lines = info.split("\\r?\\n");
        for (String line : lines) {
            if (line.startsWith("used_memory:")) {
                String[] parts = line.split(":");
                long usedMemory = Long.parseLong(parts[1].trim());
                
                Metrics.gauge("redis.memory.used.bytes", usedMemory)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .register(metrics.globalRegistry);
            }
        }
    } catch (Exception e) {
        log.error("Failed to collect memory used", e);
    }
}
```

**告警阈值**：

```yaml
redis_memory_used_bytes_alerts:
  warning:
    threshold: 1073741824
    duration: "5m"
    message: "Redis内存使用量超过1GB"
  critical:
    threshold: 2147483648
    duration: "2m"
    message: "Redis内存使用量超过2GB"
```

## 缓存失效监控指标

### Redis缓存失效指标

#### 1. 过期Key数量

**指标名称**：`redis.keys.expired.count`

**指标描述**：Redis过期Key数量

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectExpiredKeyCount() {
    try {
        String info = redisTemplate.execute((RedisCallback<String>) connection -> {
            return ((RedisServerCommands) connection).info("stats");
        });
        
        String[] lines = info.split("\\r?\\n");
        for (String line : lines) {
            if (line.startsWith("expired_keys:")) {
                String[] parts = line.split(":");
                long expiredKeys = Long.parseLong(parts[1].trim());
                
                Metrics.gauge("redis.keys.expired.count", expiredKeys)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .register(metrics.globalRegistry);
            }
        }
    } catch (Exception e) {
        log.error("Failed to collect expired key count", e);
    }
}
```

**告警阈值**：

```yaml
redis_keys_expired_count_alerts:
  warning:
    threshold: 100
    duration: "5m"
    message: "Redis过期Key数量超过100/分钟"
  critical:
    threshold: 500
    duration: "2m"
    message: "Redis过期Key数量超过500/分钟"
```

#### 2. 驱逐Key数量

**指标名称**：`redis.keys.evicted.count`

**指标描述**：Redis驱逐Key数量

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectEvictedKeyCount() {
    try {
        String info = redisTemplate.execute((RedisCallback<String>) connection -> {
            return ((RedisServerCommands) connection).info("stats");
        });
        
        String[] lines = info.split("\\r?\\n");
        for (String line : lines) {
            if (line.startsWith("evicted_keys:")) {
                String[] parts = line.split(":");
                long evictedKeys = Long.parseLong(parts[1].trim());
                
                Metrics.gauge("redis.keys.evicted.count", evictedKeys)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .register(metrics.globalRegistry);
            }
        }
    } catch (Exception e) {
        log.error("Failed to collect evicted key count", e);
    }
}
```

**告警阈值**：

```yaml
redis_keys_evicted_count_alerts:
  warning:
    threshold: 50
    duration: "5m"
    message: "Redis驱逐Key数量超过50/分钟"
  critical:
    threshold: 200
    duration: "2m"
    message: "Redis驱逐Key数量超过200/分钟"
```

## 告警阈值配置

### 告警阈值配置文件

```yaml
cache_alerts:
  redis:
    connections:
      count:
        warning:
          threshold: 80
          duration: "5m"
          message: "Redis连接数超过80"
        critical:
          threshold: 90
          duration: "2m"
          message: "Redis连接数超过90"
      rejected:
        warning:
          threshold: 10
          duration: "5m"
          message: "Redis拒绝连接数超过10/分钟"
        critical:
          threshold: 50
          duration: "2m"
          message: "Redis拒绝连接数超过50/分钟"
    memory:
      usage:
        warning:
          threshold: 70
          duration: "5m"
          message: "Redis内存使用率超过70%"
        critical:
          threshold: 85
          duration: "2m"
          message: "Redis内存使用率超过85%"
      fragmentation:
        warning:
          threshold: 1.5
          duration: "5m"
          message: "Redis内存碎片率超过1.5"
        critical:
          threshold: 2.0
          duration: "2m"
          message: "Redis内存碎片率超过2.0"
    performance:
      hit_rate:
        warning:
          threshold: 80
          duration: "5m"
          message: "Redis缓存命中率低于80%"
        critical:
          threshold: 70
          duration: "2m"
          message: "Redis缓存命中率低于70%"
      response_time:
        average:
          warning:
            threshold: 10
            duration: "5m"
            message: "Redis平均响应时间超过10ms"
          critical:
            threshold: 50
            duration: "2m"
            message: "Redis平均响应时间超过50ms"
        p95:
          warning:
            threshold: 50
            duration: "5m"
            message: "Redis P95响应时间超过50ms"
          critical:
            threshold: 100
            duration: "2m"
            message: "Redis P95响应时间超过100ms"
    commands:
      count:
        warning:
          threshold: 10000
          duration: "5m"
          message: "Redis命令执行数低于10000/分钟"
        critical:
          threshold: 5000
          duration: "2m"
          message: "Redis命令执行数低于5000/分钟"
      slow:
        warning:
          threshold: 100
          duration: "5m"
          message: "Redis慢查询数超过100"
        critical:
          threshold: 500
          duration: "2m"
          message: "Redis慢查询数超过500"
  
  local_cache:
    size:
      warning:
        threshold: 10000
        duration: "5m"
        message: "本地缓存大小超过10000"
      critical:
        threshold: 50000
        duration: "2m"
        message: "本地缓存大小超过50000"
    eviction:
      warning:
        threshold: 100
        duration: "5m"
        message: "本地缓存驱逐数超过100/分钟"
      critical:
        threshold: 500
        duration: "2m"
        message: "本地缓存驱逐数超过500/分钟"
    performance:
      hit_rate:
        warning:
          threshold: 80
          duration: "5m"
          message: "本地缓存命中率低于80%"
        critical:
          threshold: 70
          duration: "2m"
          message: "本地缓存命中率低于70%"
      load_time:
        average:
          warning:
            threshold: 100
            duration: "5m"
            message: "本地缓存平均加载时间超过100ms"
          critical:
            threshold: 500
            duration: "2m"
            message: "本地缓存平均加载时间超过500ms"
  
  cache_hit_rate:
    redis:
      overall:
        warning:
          threshold: 80
          duration: "5m"
          message: "Redis整体缓存命中率低于80%"
        critical:
          threshold: 70
          duration: "2m"
          message: "Redis整体缓存命中率低于70%"
      bykey:
        warning:
          threshold: 75
          duration: "5m"
          message: "Redis Key级别缓存命中率低于75%"
        critical:
          threshold: 65
          duration: "2m"
          message: "Redis Key级别缓存命中率低于65%"
  
  cache_size:
    redis:
      keys:
        warning:
          threshold: 100000
          duration: "5m"
          message: "Redis Key数量超过100000"
        critical:
          threshold: 200000
          duration: "2m"
          message: "Redis Key数量超过200000"
      memory:
        warning:
          threshold: 1073741824
          duration: "5m"
          message: "Redis内存使用量超过1GB"
        critical:
          threshold: 2147483648
          duration: "2m"
          message: "Redis内存使用量超过2GB"
    redis:
      expired:
        warning:
          threshold: 100
          duration: "5m"
          message: "Redis过期Key数量超过100/分钟"
        critical:
          threshold: 500
          duration: "2m"
          message: "Redis过期Key数量超过500/分钟"
      evicted:
        warning:
          threshold: 50
          duration: "5m"
          message: "Redis驱逐Key数量超过50/分钟"
        critical:
          threshold: 200
          duration: "2m"
          message: "Redis驱逐Key数量超过200/分钟"
```

## 相关文档

- [系统级监控指标文档](SystemMetricsGuide.md)
- [应用级监控指标文档](ApplicationMetricsGuide.md)
- [业务级监控指标文档](BusinessMetricsGuide.md)
- [数据库监控指标文档](DatabaseMetricsGuide.md)

## 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|---------|
| 1.0.0 | 2026-01-19 | System | 初始版本 |