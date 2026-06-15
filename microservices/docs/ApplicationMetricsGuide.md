# 应用级监控指标文档

## 文档概述

本文档提供企业级分布式应用应用级监控指标的完整定义，涵盖JVM、线程池、连接池、HTTP请求、缓存等应用资源的监控指标、采集方法、告警阈值和优化建议。

## 目录

1. [应用级监控概述](#应用级监控概述)
2. [JVM监控指标](#jvm监控指标)
3. [线程池监控指标](#线程池监控指标)
4. [连接池监控指标](#连接池监控指标)
5. [HTTP请求监控指标](#http请求监控指标)
6. [缓存监控指标](#缓存监控指标)
7. [消息队列监控指标](#消息队列监控指标)
8. [告警阈值配置](#告警阈值配置)

## 应用级监控概述

### 应用级监控定义

应用级监控是对应用程序运行时的资源使用情况和性能指标进行监控，包括JVM、线程池、连接池、HTTP请求等应用资源。

### 应用级监控重要性

- **应用性能监控**：实时监控应用性能指标
- **资源使用监控**：监控应用资源使用情况
- **故障定位**：快速定位应用故障和瓶颈
- **容量规划**：为应用扩容提供数据支持

### 应用级监控范围

```yaml
application_monitoring_scope:
  resources:
    - "JVM内存"
    - "JVM GC"
    - "线程池"
    - "连接池"
    - "HTTP请求"
    - "缓存"
    - "消息队列"
  
  metrics:
    - "使用率"
    - "活跃数"
    - "等待数"
    - "队列长度"
    - "响应时间"
    - "错误率"
    - "吞吐量"
```

## JVM监控指标

### JVM内存指标

#### 1. 堆内存使用率

**指标名称**：`jvm.memory.heap.usage`

**指标描述**：JVM堆内存使用率

**采集方法**：

```java
@Component
public class JvmMemoryMetricsCollector {

    private final MemoryMXBean memoryBean = 
            ManagementFactory.getMemoryMXBean();

    @Scheduled(fixedRate = 5000)
    public void collectHeapMemoryUsage() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long used = heapUsage.getUsed();
        long max = heapUsage.getMax();
        double heapUsagePercent = (double) used / max * 100;
        
        Metrics.gauge("jvm.memory.heap.usage", heapUsagePercent)
                .tag("host", getHostname())
                .tag("application", getApplicationName())
                .register(Metrics.globalRegistry);
        
        Metrics.gauge("jvm.memory.heap.used", used)
                .tag("host", getHostname())
                .tag("application", getApplicationName())
                .register(Metrics.globalRegistry);
        
        Metrics.gauge("jvm.memory.heap.max", max)
                .tag("host", getHostname())
                .tag("application", getApplicationName())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
jvm_heap_usage_alerts:
  warning:
    threshold: 70
    duration: "5m"
    message: "JVM堆内存使用率超过70%"
  
  critical:
    threshold: 85
    duration: "2m"
    message: "JVM堆内存使用率超过85%"
```

#### 2. 非堆内存使用率

**指标名称**：`jvm.memory.non-heap.usage`

**指标描述**：JVM非堆内存使用率

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectNonHeapMemoryUsage() {
    MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
    long used = nonHeapUsage.getUsed();
    long max = nonHeapUsage.getMax();
    double nonHeapUsagePercent = (double) used / max * 100;
    
    Metrics.gauge("jvm.memory.non-heap.usage", nonHeapUsagePercent)
            .tag("host", getHostname())
            .tag("application", getApplicationName())
            .register(Metrics.globalRegistry);
    
    Metrics.gauge("jvm.memory.non-heap.used", used)
            .tag("host", getHostname())
            .tag("application", getApplicationName())
            .register(Metrics.globalRegistry);
    
    Metrics.gauge("jvm.memory.non-heap.max", max)
            .tag("host", getHostname())
            .tag("application", getApplicationName())
            .register(Metrics.globalRegistry);
}
```

**告警阈值**：

```yaml
jvm_non_heap_usage_alerts:
  warning:
    threshold: 70
    duration: "5m"
    message: "JVM非堆内存使用率超过70%"
  
  critical:
    threshold: 85
    duration: "2m"
    message: "JVM非堆内存使用率超过85%"
```

### JVM GC指标

#### 1. GC次数

**指标名称**：`jvm.gc.count`

**指标描述**：JVM GC执行次数

**采集方法**：

```java
@Component
public class JvmGcMetricsCollector {

    private final List<GarbageCollectorMXBean> gcBeans = 
            ManagementFactory.getGarbageCollectorMXBeans();

    @Scheduled(fixedRate = 5000)
    public void collectGcCount() {
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            String gcName = gcBean.getName();
            long gcCount = gcBean.getCollectionCount();
            
            Metrics.gauge("jvm.gc.count", gcCount)
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .tag("gc", gcName)
                    .register(Metrics.globalRegistry);
        }
    }
}
```

**告警阈值**：

```yaml
jvm_gc_count_alerts:
  warning:
    threshold: 10
    duration: "1m"
    message: "JVM GC次数超过10次/分钟"
  
  critical:
    threshold: 30
    duration: "1m"
    message: "JVM GC次数超过30次/分钟"
```

#### 2. GC时间

**指标名称**：`jvm.gc.time`

**指标描述**：JVM GC执行时间

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectGcTime() {
    for (GarbageCollectorMXBean gcBean : gcBeans) {
        String gcName = gcBean.getName();
        long gcTime = gcBean.getCollectionTime();
        
        Metrics.gauge("jvm.gc.time", gcTime)
                .tag("host", getHostname())
                .tag("application", getApplicationName())
                .tag("gc", gcName)
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
jvm_gc_time_alerts:
  warning:
    threshold: 1000
    duration: "1m"
    message: "JVM GC时间超过1000ms/分钟"
  
  critical:
    threshold: 5000
    duration: "1m"
    message: "JVM GC时间超过5000ms/分钟"
```

#### 3. GC暂停时间

**指标名称**：`jvm.gc.pause.time`

**指标描述**：JVM GC暂停时间

**采集方法**：

```java
@Component
public class GcPauseMetricsCollector {

    private final List<GarbageCollectorMXBean> gcBeans = 
            ManagementFactory.getGarbageCollectorMXBeans();

    @Scheduled(fixedRate = 5000)
    public void collectGcPauseTime() {
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            String gcName = gcBean.getName();
            
            if (gcBean instanceof com.sun.management.GarbageCollectorMXBean) {
                com.sun.management.GarbageCollectorMXBean sunGcBean = 
                        (com.sun.management.GarbageCollectorMXBean) gcBean;
                
                GcInfo lastGcInfo = sunGcBean.getLastGcInfo();
                if (lastGcInfo != null) {
                    long pauseTime = lastGcInfo.getDuration();
                    
                    Metrics.gauge("jvm.gc.pause.time", pauseTime)
                            .tag("host", getHostname())
                            .tag("application", getApplicationName())
                            .tag("gc", gcName)
                            .register(Metrics.globalRegistry);
                }
            }
        }
    }
}
```

**告警阈值**：

```yaml
jvm_gc_pause_time_alerts:
  warning:
    threshold: 100
    duration: "5m"
    message: "JVM GC暂停时间超过100ms"
  
  critical:
    threshold: 500
    duration: "2m"
    message: "JVM GC暂停时间超过500ms"
```

## 线程池监控指标

### 线程池状态指标

#### 1. 线程池活跃线程数

**指标名称**：`thread.pool.active.count`

**指标描述**：线程池活跃线程数

**采集方法**：

```java
@Component
public class ThreadPoolMetricsCollector {

    private final Map<String, ThreadPoolExecutor> threadPools = new HashMap<>();

    @Autowired
    public void setThreadPools(
            @Qualifier("taskExecutor") ThreadPoolExecutor taskExecutor,
            @Qualifier("asyncExecutor") ThreadPoolExecutor asyncExecutor) {
        threadPools.put("taskExecutor", taskExecutor);
        threadPools.put("asyncExecutor", asyncExecutor);
    }

    @Scheduled(fixedRate = 5000)
    public void collectThreadPoolMetrics() {
        for (Map.Entry<String, ThreadPoolExecutor> entry : threadPools.entrySet()) {
            String poolName = entry.getKey();
            ThreadPoolExecutor executor = entry.getValue();
            
            int activeCount = executor.getActiveCount();
            
            Metrics.gauge("thread.pool.active.count", activeCount)
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .tag("pool", poolName)
                    .register(Metrics.globalRegistry);
        }
    }
}
```

**告警阈值**：

```yaml
thread_pool_active_count_alerts:
  warning:
    threshold: 80
    duration: "5m"
    message: "线程池活跃线程数超过80"
  
  critical:
    threshold: 150
    duration: "2m"
    message: "线程池活跃线程数超过150"
```

#### 2. 线程池队列长度

**指标名称**：`thread.pool.queue.size`

**指标描述**：线程池队列长度

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectThreadPoolQueueSize() {
    for (Map.Entry<String, ThreadPoolExecutor> entry : threadPools.entrySet()) {
        String poolName = entry.getKey();
        ThreadPoolExecutor executor = entry.getValue();
        
        int queueSize = executor.getQueue().size();
        
        Metrics.gauge("thread.pool.queue.size", queueSize)
                .tag("host", getHostname())
                .tag("application", getApplicationName())
                .tag("pool", poolName)
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
thread_pool_queue_size_alerts:
  warning:
    threshold: 100
    duration: "5m"
    message: "线程池队列长度超过100"
  
  critical:
    threshold: 500
    duration: "2m"
    message: "线程池队列长度超过500"
```

#### 3. 线程池拒绝任务数

**指标名称**：`thread.pool.rejected.count`

**指标描述**：线程池拒绝任务数

**采集方法**：

```java
@Component
public class RejectedTaskCounter {

    private final Map<String, AtomicLong> rejectedCounters = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        rejectedCounters.put("taskExecutor", new AtomicLong(0));
        rejectedCounters.put("asyncExecutor", new AtomicLong(0));
    }

    public void incrementRejected(String poolName) {
        AtomicLong counter = rejectedCounters.get(poolName);
        if (counter != null) {
            counter.incrementAndGet();
        }
    }

    @Scheduled(fixedRate = 5000)
    public void collectRejectedCount() {
        for (Map.Entry<String, AtomicLong> entry : rejectedCounters.entrySet()) {
            String poolName = entry.getKey();
            long rejectedCount = entry.getValue().getAndSet(0);
            
            Metrics.counter("thread.pool.rejected.count")
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .tag("pool", poolName)
                    .increment(rejectedCount, Metrics.globalRegistry);
        }
    }
}
```

**告警阈值**：

```yaml
thread_pool_rejected_count_alerts:
  warning:
    threshold: 10
    duration: "5m"
    message: "线程池拒绝任务数超过10/分钟"
  
  critical:
    threshold: 50
    duration: "2m"
    message: "线程池拒绝任务数超过50/分钟"
```

## 连接池监控指标

### 数据库连接池指标

#### 1. 活跃连接数

**指标名称**：`datasource.pool.active.count`

**指标描述**：数据库连接池活跃连接数

**采集方法**：

```java
@Component
public class DataSourceMetricsCollector {

    @Autowired
    private DataSource dataSource;

    @Scheduled(fixedRate = 5000)
    public void collectDataSourceMetrics() {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();
            
            int activeConnections = poolBean.getActiveConnections();
            
            Metrics.gauge("datasource.pool.active.count", activeConnections)
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
datasource_pool_active_count_alerts:
  warning:
    threshold: 40
    duration: "5m"
    message: "数据库连接池活跃连接数超过40"
  
  critical:
    threshold: 45
    duration: "2m"
    message: "数据库连接池活跃连接数超过45"
```

#### 2. 空闲连接数

**指标名称**：`datasource.pool.idle.count`

**指标描述**：数据库连接池空闲连接数

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectIdleConnections() {
    if (dataSource instanceof HikariDataSource) {
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();
        
        int idleConnections = poolBean.getIdleConnections();
        
        Metrics.gauge("datasource.pool.idle.count", idleConnections)
                .tag("host", getHostname())
                .tag("application", getApplicationName())
                .tag("datasource", hikariDataSource.getPoolName())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
datasource_pool_idle_count_alerts:
  warning:
    threshold: 5
    duration: "5m"
    message: "数据库连接池空闲连接数少于5"
  
  critical:
    threshold: 2
    duration: "2m"
    message: "数据库连接池空闲连接数少于2"
```

#### 3. 等待连接数

**指标名称**：`datasource.pool.waiting.count`

**指标描述**：数据库连接池等待连接数

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectWaitingConnections() {
    if (dataSource instanceof HikariDataSource) {
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();
        
        int threadsAwaitingConnection = poolBean.getThreadsAwaitingConnection();
        
        Metrics.gauge("datasource.pool.waiting.count", threadsAwaitingConnection)
                .tag("host", getHostname())
                .tag("application", getApplicationName())
                .tag("datasource", hikariDataSource.getPoolName())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
datasource_pool_waiting_count_alerts:
  warning:
    threshold: 10
    duration: "5m"
    message: "数据库连接池等待连接数超过10"
  
  critical:
    threshold: 20
    duration: "2m"
    message: "数据库连接池等待连接数超过20"
```

## HTTP请求监控指标

### HTTP请求性能指标

#### 1. HTTP请求响应时间

**指标名称**：`http.server.requests`

**指标描述**：HTTP请求响应时间

**采集方法**：

```java
@Component
public class HttpRequestMetricsFilter implements Filter {

    private final MeterRegistry meterRegistry;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        long startTime = System.currentTimeMillis();
        
        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            Metrics.timer("http.server.requests")
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .tag("method", request.getMethod())
                    .tag("uri", getRequestUri(request))
                    .tag("status", String.valueOf(((HttpServletResponse) response).getStatus()))
                    .record(duration, TimeUnit.MILLISECONDS, meterRegistry);
        }
    }

    private String getRequestUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.length() > 100 ? uri.substring(0, 100) : uri;
    }
}
```

**告警阈值**：

```yaml
http_request_duration_alerts:
  warning:
    threshold: 1000
    duration: "5m"
    message: "HTTP请求响应时间超过1000ms"
  
  critical:
    threshold: 3000
    duration: "2m"
    message: "HTTP请求响应时间超过3000ms"
```

#### 2. HTTP请求错误率

**指标名称**：`http.server.requests.error`

**指标描述**：HTTP请求错误率

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectHttpErrorRate() {
    Timer httpTimer = Metrics.globalRegistry.find("http.server.requests").timer();
    
    if (httpTimer != null) {
        long totalRequests = httpTimer.count();
        long errorRequests = httpTimer.getIds()
                .stream()
                .filter(id -> id.getTag("status").startsWith("5"))
                .count();
        
        double errorRate = totalRequests > 0 ? 
                (double) errorRequests / totalRequests * 100 : 0;
        
        Metrics.gauge("http.server.requests.error.rate", errorRate)
                .tag("host", getHostname())
                .tag("application", getApplicationName())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
http_error_rate_alerts:
  warning:
    threshold: 1
    duration: "5m"
    message: "HTTP请求错误率超过1%"
  
  critical:
    threshold: 5
    duration: "2m"
    message: "HTTP请求错误率超过5%"
```

#### 3. HTTP请求吞吐量

**指标名称**：`http.server.requests.throughput`

**指标描述**：HTTP请求吞吐量

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectHttpThroughput() {
    Timer httpTimer = Metrics.globalRegistry.find("http.server.requests").timer();
    
    if (httpTimer != null) {
        double throughput = httpTimer.count() / 60.0;
        
        Metrics.gauge("http.server.requests.throughput", throughput)
                .tag("host", getHostname())
                .tag("application", getApplicationName())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
http_throughput_alerts:
  warning:
    threshold: 100
    duration: "5m"
    message: "HTTP请求吞吐量低于100/分钟"
  
  critical:
    threshold: 50
    duration: "2m"
    message: "HTTP请求吞吐量低于50/分钟"
```

## 缓存监控指标

### 缓存性能指标

#### 1. 缓存命中率

**指标名称**：`cache.hit.rate`

**指标描述**：缓存命中率

**采集方法**：

```java
@Component
public class CacheMetricsCollector {

    @Autowired
    private CacheManager cacheManager;

    @Scheduled(fixedRate = 5000)
    public void collectCacheMetrics() {
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
                
                Metrics.gauge("cache.hit.rate", hitRate)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .tag("cache", cacheName)
                        .register(Metrics.globalRegistry);
                
                Metrics.gauge("cache.hit.count", cacheHits)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .tag("cache", cacheName)
                        .register(Metrics.globalRegistry);
                
                Metrics.gauge("cache.miss.count", cacheMisses)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .tag("cache", cacheName)
                        .register(Metrics.globalRegistry);
            }
        }
    }
}
```

**告警阈值**：

```yaml
cache_hit_rate_alerts:
  warning:
    threshold: 70
    duration: "5m"
    message: "缓存命中率低于70%"
  
  critical:
    threshold: 50
    duration: "2m"
    message: "缓存命中率低于50%"
```

#### 2. 缓存大小

**指标名称**：`cache.size`

**指标描述**：缓存大小

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectCacheSize() {
    for (String cacheName : cacheManager.getCacheNames()) {
        Cache cache = cacheManager.getCache(cacheName);
        
        if (cache instanceof Ehcache) {
            Ehcache ehcache = (Ehcache) cache;
            Statistics statistics = ehcache.getStatistics();
            
            long cacheSize = statistics.getSize();
            
            Metrics.gauge("cache.size", cacheSize)
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .tag("cache", cacheName)
                    .register(Metrics.globalRegistry);
        }
    }
}
```

**告警阈值**：

```yaml
cache_size_alerts:
  warning:
    threshold: 10000
    duration: "5m"
    message: "缓存大小超过10000"
  
  critical:
    threshold: 50000
    duration: "2m"
    message: "缓存大小超过50000"
```

## 消息队列监控指标

### Kafka监控指标

#### 1. Kafka消息积压

**指标名称**：`kafka.consumer.lag`

**指标描述**：Kafka消费者消息积压

**采集方法**：

```java
@Component
public class KafkaMetricsCollector {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedRate = 5000)
    public void collectKafkaMetrics() {
        AdminClient adminClient = AdminClient.create(
                Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, 
                        kafkaTemplate.getProducerFactory().getConfigurationProperties()
                                .get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)));
        
        try {
            ListConsumerGroupOffsetsResult result = 
                    adminClient.listConsumerGroupOffsets("test-group");
            
            for (TopicPartition topicPartition : result.partitionsToOffsetAndMetadata().keySet()) {
                OffsetAndMetadata offsetAndMetadata = 
                        result.partitionsToOffsetAndMetadata().get(topicPartition);
                
                long consumerOffset = offsetAndMetadata.offset();
                long endOffset = getEndOffset(adminClient, topicPartition);
                long lag = endOffset - consumerOffset;
                
                Metrics.gauge("kafka.consumer.lag", lag)
                        .tag("host", getHostname())
                        .tag("application", getApplicationName())
                        .tag("topic", topicPartition.topic())
                        .tag("partition", String.valueOf(topicPartition.partition()))
                        .register(Metrics.globalRegistry);
            }
        } catch (Exception e) {
            log.error("Failed to collect Kafka metrics", e);
        } finally {
            adminClient.close();
        }
    }

    private long getEndOffset(AdminClient adminClient, TopicPartition topicPartition) {
        ListOffsetsResult result = adminClient.listOffsets(
                Map.of(topicPartition, OffsetSpec.latest()));
        
        return result.partitionResult(topicPartition).offset();
    }
}
```

**告警阈值**：

```yaml
kafka_consumer_lag_alerts:
  warning:
    threshold: 1000
    duration: "5m"
    message: "Kafka消费者消息积压超过1000"
  
  critical:
    threshold: 5000
    duration: "2m"
    message: "Kafka消费者消息积压超过5000"
```

#### 2. Kafka消费延迟

**指标名称**：`kafka.consumer.latency`

**指标描述**：Kafka消费者延迟

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectKafkaConsumerLatency() {
    AdminClient adminClient = AdminClient.create(
            Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, 
                    kafkaTemplate.getProducerFactory().getConfigurationProperties()
                            .get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)));
    
    try {
        Map<TopicPartition, OffsetAndMetadata> offsets = 
                adminClient.listConsumerGroupOffsets("test-group")
                        .partitionsToOffsetAndMetadata();
        
        for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsets.entrySet()) {
            TopicPartition topicPartition = entry.getKey();
            OffsetAndMetadata offsetAndMetadata = entry.getValue();
            
            long consumerOffset = offsetAndMetadata.offset();
            long endOffset = getEndOffset(adminClient, topicPartition);
            long latency = endOffset - consumerOffset;
            
            Metrics.gauge("kafka.consumer.latency", latency)
                    .tag("host", getHostname())
                    .tag("application", getApplicationName())
                    .tag("topic", topicPartition.topic())
                    .tag("partition", String.valueOf(topicPartition.partition()))
                    .register(Metrics.globalRegistry);
        }
    } catch (Exception e) {
        log.error("Failed to collect Kafka consumer latency", e);
    } finally {
        adminClient.close();
    }
}
```

**告警阈值**：

```yaml
kafka_consumer_latency_alerts:
  warning:
    threshold: 100
    duration: "5m"
    message: "Kafka消费者延迟超过100"
  
  critical:
    threshold: 500
    duration: "2m"
    message: "Kafka消费者延迟超过500"
```

## 告警阈值配置

### 告警阈值配置文件

```yaml
application_alerts:
  jvm:
    memory:
      heap:
        warning:
          threshold: 70
          duration: "5m"
          message: "JVM堆内存使用率超过70%"
        critical:
          threshold: 85
          duration: "2m"
          message: "JVM堆内存使用率超过85%"
      non-heap:
        warning:
          threshold: 70
          duration: "5m"
          message: "JVM非堆内存使用率超过70%"
        critical:
          threshold: 85
          duration: "2m"
          message: "JVM非堆内存使用率超过85%"
    gc:
      count:
        warning:
          threshold: 10
          duration: "1m"
          message: "JVM GC次数超过10次/分钟"
        critical:
          threshold: 30
          duration: "1m"
          message: "JVM GC次数超过30次/分钟"
      time:
        warning:
          threshold: 1000
          duration: "1m"
          message: "JVM GC时间超过1000ms/分钟"
        critical:
          threshold: 5000
          duration: "1m"
          message: "JVM GC时间超过5000ms/分钟"
      pause:
        warning:
          threshold: 100
          duration: "5m"
          message: "JVM GC暂停时间超过100ms"
        critical:
          threshold: 500
          duration: "2m"
          message: "JVM GC暂停时间超过500ms"
  
  thread_pool:
    active:
      warning:
        threshold: 80
        duration: "5m"
        message: "线程池活跃线程数超过80"
      critical:
        threshold: 150
        duration: "2m"
        message: "线程池活跃线程数超过150"
    queue:
      warning:
        threshold: 100
        duration: "5m"
        message: "线程池队列长度超过100"
      critical:
        threshold: 500
        duration: "2m"
        message: "线程池队列长度超过500"
    rejected:
      warning:
        threshold: 10
        duration: "5m"
        message: "线程池拒绝任务数超过10/分钟"
      critical:
        threshold: 50
        duration: "2m"
        message: "线程池拒绝任务数超过50/分钟"
  
  datasource:
    active:
      warning:
        threshold: 40
        duration: "5m"
        message: "数据库连接池活跃连接数超过40"
      critical:
        threshold: 45
        duration: "2m"
        message: "数据库连接池活跃连接数超过45"
    idle:
      warning:
        threshold: 5
        duration: "5m"
        message: "数据库连接池空闲连接数少于5"
      critical:
        threshold: 2
        duration: "2m"
        message: "数据库连接池空闲连接数少于2"
    waiting:
      warning:
        threshold: 10
        duration: "5m"
        message: "数据库连接池等待连接数超过10"
      critical:
        threshold: 20
        duration: "2m"
        message: "数据库连接池等待连接数超过20"
  
  http:
    duration:
      warning:
        threshold: 1000
        duration: "5m"
        message: "HTTP请求响应时间超过1000ms"
      critical:
        threshold: 3000
        duration: "2m"
        message: "HTTP请求响应时间超过3000ms"
    error_rate:
      warning:
        threshold: 1
        duration: "5m"
        message: "HTTP请求错误率超过1%"
      critical:
        threshold: 5
        duration: "2m"
        message: "HTTP请求错误率超过5%"
    throughput:
      warning:
        threshold: 100
        duration: "5m"
        message: "HTTP请求吞吐量低于100/分钟"
      critical:
        threshold: 50
        duration: "2m"
        message: "HTTP请求吞吐量低于50/分钟"
  
  cache:
    hit_rate:
      warning:
        threshold: 70
        duration: "5m"
        message: "缓存命中率低于70%"
      critical:
        threshold: 50
        duration: "2m"
        message: "缓存命中率低于50%"
    size:
      warning:
        threshold: 10000
        duration: "5m"
        message: "缓存大小超过10000"
      critical:
        threshold: 50000
        duration: "2m"
        message: "缓存大小超过50000"
  
  kafka:
    lag:
      warning:
        threshold: 1000
        duration: "5m"
        message: "Kafka消费者消息积压超过1000"
      critical:
        threshold: 5000
        duration: "2m"
        message: "Kafka消费者消息积压超过5000"
    latency:
      warning:
        threshold: 100
        duration: "5m"
        message: "Kafka消费者延迟超过100"
      critical:
        threshold: 500
        duration: "2m"
        message: "Kafka消费者延迟超过500"
```

## 相关文档

- [系统级监控指标文档](SystemMetricsGuide.md)
- [业务级监控指标文档](BusinessMetricsGuide.md)
- [数据库监控指标文档](DatabaseMetricsGuide.md)
- [缓存监控指标文档](CacheMetricsGuide.md)

## 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|---------|
| 1.0.0 | 2026-01-19 | System | 初始版本 |