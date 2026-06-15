# 性能调优文档

## 版本
- 版本：1.0.0
- 创建日期：2025-01-19
- 最后更新：2025-01-19

## 1. 文档概述

### 1.1 目的
本文档提供了库存管理系统的性能调优详细指南，包括JVM参数调优、数据库性能优化、缓存策略优化、网络连接优化和性能监控方法。

### 1.2 适用范围
- JVM性能调优
- 数据库性能优化
- 缓存性能优化
- 网络性能优化
- 应用性能优化

### 1.3 目标读者
- 系统运维工程师
- 性能工程师
- 系统架构师
- DevOps工程师

## 2. JVM参数调优

### 2.1 G1GC配置

#### 2.1.1 基础GC参数

```bash
# G1GC基础配置
-XX:+UseG1GC                          # 使用G1垃圾回收器
-XX:MaxGCPauseMillis=200              # 最大GC停顿时间目标（毫秒）
-XX:ParallelGCThreads=4               # 并行GC线程数（CPU核心数/2）
-XX:ConcGCThreads=1                   # 并发GC线程数（并行GC线程数/4）
-XX:InitiatingHeapOccupancyPercent=45 # 触发并发GC的堆占用百分比
-XX:G1ReservePercent=10              # 保留空间百分比
-XX:G1HeapRegionSize=16m             # G1堆区域大小
```

#### 2.1.2 堆内存配置

```bash
# 堆内存配置
-Xms1g                                # 初始堆大小
-Xmx2g                                # 最大堆大小

# 根据服务类型调整
# 核心服务：2GB
# 支持服务：1GB
# 网关服务：2GB
```

#### 2.1.3 GC日志配置

```bash
# GC日志配置
-XX:+PrintGCDetails                   # 打印GC详细信息
-XX:+PrintGCDateStamps                # 打印GC时间戳
-Xloggc:/tmp/gc.log                  # GC日志文件路径
-XX:+UseGCLogFileRotation             # 启用GC日志轮转
-XX:NumberOfGCLogFiles=10             # 保留GC日志文件数量
-XX:GCLogFileSize=10M                 # 单个GC日志文件大小
```

### 2.2 堆转储配置

#### 2.2.1 堆转储参数

```bash
# 堆转储配置
-XX:+HeapDumpOnOutOfMemoryError       # 内存溢出时生成堆转储
-XX:HeapDumpPath=/tmp/heapdump.hprof # 堆转储文件路径
-XX:ErrorFile=/tmp/hs_err_pid%p.log   # 错误日志文件路径
-XX:+PrintConcurrentLocks            # 打印并发锁信息
```

#### 2.2.2 堆转储分析

```bash
# 使用jhat分析堆转储
jhat -port 7000 /tmp/heapdump.hprof

# 使用VisualVM分析堆转储
jvisualvm /tmp/heapdump.hprof

# 使用Eclipse MAT分析堆转储
MemoryAnalyzer /tmp/heapdump.hprof
```

### 2.3 性能监控

#### 2.3.1 JVM指标

```bash
# 查看JVM指标
jstat -gcutil 1s 1000

# 查看类加载统计
jstat -class 1s 1000

# 查看编译统计
jstat -compiler 1s 1000

# 查看线程统计
jstat -thread 1s 1000
```

#### 2.3.2 线程转储

```bash
# 生成线程转储
jstack <pid> > thread-dump.txt

# 生成多个线程转储
for i in {1..5}; do
    jstack <pid> >> thread-dump-$i.txt
    sleep 2
done
```

## 3. 数据库性能优化

### 3.1 PostgreSQL配置优化

#### 3.1.1 连接池配置

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 100              # 最大连接池大小
      minimum-idle: 20                  # 最小空闲连接数
      connection-timeout: 30000           # 连接超时时间（毫秒）
      idle-timeout: 600000                # 空闲超时时间（毫秒）
      max-lifetime: 1800000              # 连接最大生命周期（毫秒）
      leak-detection-threshold: 60000   # 泄露检测阈值（毫秒）
```

#### 3.1.2 查询优化

```sql
-- 创建索引
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- 创建复合索引
CREATE INDEX idx_orders_customer_status ON orders(customer_id, status);

-- 分析查询计划
EXPLAIN ANALYZE
SELECT * FROM products WHERE name = 'Product 1';

-- 更新统计信息
ANALYZE products;
ANALYZE orders;
ANALYZE inventory;
```

#### 3.1.3 慢查询优化

```sql
-- 启用慢查询日志
ALTER SYSTEM SET log_min_duration_statement = 1000;

-- 查看慢查询
SELECT query, mean_exec_time, calls, total_exec_time
FROM pg_stat_statements
ORDER BY mean_exec_time DESC
LIMIT 10;

-- 优化慢查询
-- 使用索引
-- 避免全表扫描
-- 使用LIMIT限制结果集
```

### 3.2 数据库监控

#### 3.2.1 连接监控

```sql
-- 查看连接数
SELECT count(*) FROM pg_stat_activity;

-- 查看连接状态
SELECT state, count(*)
FROM pg_stat_activity
GROUP BY state;

-- 查看长时间运行的查询
SELECT pid, now() - query_start as duration, query
FROM pg_stat_activity
WHERE state = 'active'
ORDER BY duration DESC
LIMIT 10;
```

#### 3.2.2 表统计信息

```sql
-- 查看表大小
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname::text ||'.'|| tablename::text)) as size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname::text ||'.'|| tablename::text) DESC;

-- 查看索引使用情况
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY idx_scan DESC;
```

## 4. 缓存性能优化

### 4.1 Redis配置优化

#### 4.1.1 内存配置

```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 8               # 最大活跃连接数
        max-idle: 8                # 最大空闲连接数
        min-idle: 0                # 最小空闲连接数
        max-wait: -1ms             # 最大等待时间
```

#### 4.1.2 缓存策略

```java
// 缓存配置
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))           // 缓存过期时间
            .disableCachingNullValues()           // 不缓存null值
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .computePrefixWith(cacheName -> "inventory:" + cacheName + ":")
            .build();

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .transactionAware()
            .build();
    }
}
```

#### 4.1.3 缓存预热

```java
// 缓存预热
@Component
public class CacheWarmupService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    @PostConstruct
    public void warmupCache() {
        log.info("开始缓存预热...");

        List<Product> products = productRepository.findAll();
        Cache cache = cacheManager.getCache("products");

        for (Product product : products) {
            cache.put(product.getId(), product);
        }

        log.info("缓存预热完成，共加载 {} 个产品", products.size());
    }
}
```

### 4.2 缓存监控

#### 4.2.1 缓存命中率

```java
// 缓存监控
@Component
public class CacheMetricsService {

    @Autowired
    private CacheManager cacheManager;

    public void logCacheMetrics() {
        Cache cache = cacheManager.getCache("products");
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = 
            (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache.getNativeCache();

        CacheStats stats = nativeCache.stats();

        log.info("缓存统计:");
        log.info("  命中次数: {}", stats.hitCount());
        log.info("  未命中次数: {}", stats.missCount());
        log.info("  加载次数: {}", stats.loadCount());
        log.info("  命中率: {:.2f}%", stats.hitRate() * 100);
    }
}
```

#### 4.2.2 缓存大小监控

```java
// 缓存大小监控
@Component
public class CacheSizeMonitor {

    @Autowired
    private CacheManager cacheManager;

    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void monitorCacheSize() {
        Cache cache = cacheManager.getCache("products");
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = 
            (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache.getNativeCache();

        long size = nativeCache.estimatedSize();
        long maxSize = nativeCache.policy().getMaximum();

        log.info("缓存大小: {}/{}, 使用率: {:.2f}%", 
            size, maxSize, (double) size / maxSize * 100);

        if (size > maxSize * 0.9) {
            log.warn("缓存即将满，建议增加缓存大小");
        }
    }
}
```

## 5. 网络连接优化

### 5.1 HTTP客户端优化

#### 5.1.1 连接池配置

```java
// HTTP客户端连接池
@Configuration
public class HttpClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory();

        PoolingHttpClientConnectionManager connectionManager = 
            PoolingHttpClientConnectionManager.builder()
                .setMaxConnTotal(200)              // 最大连接数
                .setDefaultMaxPerRoute(50)          // 每个路由的最大连接数
                .setValidateAfterInactivity(30, TimeUnit.SECONDS)  // 30秒后验证连接
                .build();

        factory.setConnectionManager(connectionManager);

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(5, TimeUnit.SECONDS)      // 连接超时
                .setConnectionRequestTimeout(5, TimeUnit.SECONDS)  // 连接请求超时
                .setSocketTimeout(10, TimeUnit.SECONDS)         // Socket超时
                .build();

        factory.setRequestConfig(config);

        return new RestTemplate(factory);
    }
}
```

#### 5.1.2 超时配置

```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          default:
            connectTimeout: 5000
            readTimeout: 10000
            loggerLevel: basic
```

### 5.2 负载均衡优化

#### 5.2.1 负载均衡策略

```yaml
spring:
  cloud:
    loadbalancer:
      ribbon:
        NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule  # 随机策略
        # NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RoundRobinRule  # 轮询策略
        # NFLoadBalancerRuleClassName: com.netflix.loadbalancer.WeightedResponseTimeRule  # 响应时间加权策略
```

#### 5.2.2 健康检查配置

```yaml
spring:
  cloud:
    loadbalancer:
      ribbon:
        ServerListRefreshInterval: 30000      # 30秒刷新服务列表
        OkToRetryOnAllOperations: true        # 所有操作都允许重试
        MaxAutoRetriesNextServer: 1           # 自动重试次数
```

## 6. 应用性能优化

### 6.1 异步处理

#### 6.1.1 异步方法

```java
// 异步方法
@Service
public class AsyncOrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Async("orderExecutor")
    public CompletableFuture<Order> createOrderAsync(OrderDTO orderDTO) {
        Order order = convertToEntity(orderDTO);
        Order savedOrder = orderRepository.save(order);
        return CompletableFuture.completedFuture(savedOrder);
    }

    @Bean("orderExecutor")
    public Executor orderExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("order-async-");
        executor.initialize();
        return executor;
    }
}
```

#### 6.1.2 批量处理

```java
// 批量处理
@Service
public class BatchOrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public void createOrdersBatch(List<OrderDTO> orderDTOs) {
        List<Order> orders = orderDTOs.stream()
            .map(this::convertToEntity)
            .collect(Collectors.toList());

        orderRepository.saveAll(orders);
    }
}
```

### 6.2 查询优化

#### 6.2.1 分页查询

```java
// 分页查询
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Page<Product> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
}
```

#### 6.2.2 懒加载

```java
// 懒加载
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Product getProductWithDetails(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        
        if (product != null) {
            // 延迟加载关联数据
            Hibernate.initialize(product.getCategory());
            Hibernate.initialize(product.getInventory());
        }
        
        return product;
    }
}
```

## 7. 性能监控

### 7.1 应用指标

#### 7.1.1 关键指标

```java
// 关键性能指标
@Component
public class PerformanceMetrics {

    private final MeterRegistry meterRegistry;

    public PerformanceMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordOrderCreationTime(long duration) {
        Timer.builder("order.creation.time")
            .tag("service", "order-service")
            .register(meterRegistry)
            .record(duration, TimeUnit.MILLISECONDS);
    }

    public void recordDatabaseQueryTime(long duration) {
        Timer.builder("database.query.time")
            .tag("type", "select")
            .register(meterRegistry)
            .record(duration, TimeUnit.MILLISECONDS);
    }

    public void recordCacheHit() {
        Counter.builder("cache.hits")
            .tag("cache", "products")
            .register(meterRegistry)
            .increment();
    }

    public void recordCacheMiss() {
        Counter.builder("cache.misses")
            .tag("cache", "products")
            .register(meterRegistry)
            .increment();
    }
}
```

#### 7.1.2 Prometheus配置

```yaml
# Prometheus采集配置
scrape_configs:
  - job_name: 'inventory-system'
    scrape_interval: 15s
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
          - 'localhost:8080'
          - 'localhost:8081'
          - 'localhost:8082'
```

### 7.2 性能告警

#### 7.2.1 响应时间告警

```yaml
# Prometheus告警规则
groups:
  - name: performance_alerts
    rules:
      - alert: high_response_time
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time detected"
          description: "P95 response time is {{ $value }}s"

      - alert: critical_response_time
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 2
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Critical response time detected"
          description: "P95 response time is {{ $value }}s"
```

#### 7.2.2 错误率告警

```yaml
groups:
  - name: error_rate_alerts
    rules:
      - alert: high_error_rate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }}"

      - alert: critical_error_rate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.1
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Critical error rate detected"
          description: "Error rate is {{ $value }}"
```

## 8. 性能测试

### 8.1 压力测试

#### 8.1.1 JMeter测试计划

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.5">
  <hashTree>
    <TestPlan guiclass="TestPlan" testclass="TestCase" testname="Inventory System Load Test">
      <elementProp guiclass="TestPlan.user_defined_variables" testclass="Arguments" testname="User Defined Variables">
        <collectionProp name="Variables">
          <elementProp name="BASE_URL" elementType="Argument">
            <stringProp name="Argument.name">BASE_URL</stringProp>
            <stringProp name="Argument.value">http://localhost:9090</stringProp>
            <stringProp name="Argument.metadata">=</stringProp>
          </elementProp>
          <elementProp name="THREADS" elementType="Argument">
            <stringProp name="Argument.name">THREADS</stringProp>
            <stringProp name="Argument.value">100</stringProp>
            <stringProp name="Argument.metadata">=</stringProp>
          </elementProp>
          <elementProp name="LOOPS" elementType="Argument">
            <stringProp name="Argument.name">LOOPS</stringProp>
            <stringProp name="Argument.value">1000</stringProp>
            <stringProp name="Argument.metadata">=</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>

      <ThreadGroup guiclass="ThreadGroup" testclass="ThreadGroup" testname="Thread Group">
        <stringProp name="ThreadGroup.num_threads" propname="ThreadGroup.num_threads">${THREADS}</stringProp>
        <LoopController guiclass="LoopController" testclass="LoopController" testname="Loop Controller">
          <boolProp name="LoopController.continue_forever" propname="LoopController.continue_forever">false</boolProp>
          <stringProp name="LoopController.loops" propname="LoopController.loops">${LOOPS}</stringProp>
        </LoopController>

        <hashTree>
          <HTTPSamplerProxy guiclass="HTTPSamplerProxy" testclass="HTTPSamplerProxy" testname="HTTP Request">
            <stringProp name="HTTPSampler.domain" propname="HTTPSampler.domain">${BASE_URL}</stringProp>
            <stringProp name="HTTPSampler.port" propname="HTTPSampler.port">8080</stringProp>
            <stringProp name="HTTPSampler.path" propname="HTTPSampler.path">/api/products</stringProp>
            <stringProp name="HTTPSampler.method" propname="HTTPSampler.method">GET</stringProp>
          </HTTPSamplerProxy>

          <ResultCollector guiclass="ResultCollector" testclass="ResultCollector" testname="Results">
            <boolProp name="ResultCollector.error_logging" propname="ResultCollector.error_logging">true</boolProp>
            <objProp>
              <name>save_as_xml</name>
              <value>true</value>
            </objProp>
          </ResultCollector>
        </hashTree>
      </ThreadGroup>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

#### 8.1.2 压力测试执行

```bash
# 执行JMeter测试
jmeter -n -t inventory-system-load-test.jmx -l results.jtl -e -o results.html

# 分析测试结果
jmeter -g results.jtl -o results.html

# 生成报告
jmeter -g results.jtl -o report.html -o summary.html
```

### 8.2 性能基准测试

#### 8.2.1 基准测试脚本

```bash
#!/bin/bash
# benchmark.sh

echo "开始性能基准测试..."

# 测试API响应时间
for i in {1..100}; do
    START_TIME=$(date +%s%N)
    curl -s http://localhost:9090/api/products > /dev/null
    END_TIME=$(date +%s%N)
    RESPONSE_TIME=$((END_TIME - START_TIME))
    echo "Request $i: ${RESPONSE_TIME}ms"
done

# 计算统计
AVG_TIME=$(echo "scale=0; $(for i in {1..100}; do echo \$i; done)" | awk '{sum+=$1} END {print sum/NR}')
echo "Average response time: ${AVG_TIME}ms"
```

## 9. 最佳实践

### 9.1 性能优化原则

1. **监控优先**
   - 先监控，后优化
   - 建立性能基线
   - 持续观察性能变化

2. **逐步优化**
   - 一次只优化一个方面
   - 验证优化效果
   - 记录优化过程

3. **数据驱动**
   - 使用性能数据指导优化
   - 基于实际负载优化
   - 避免过度优化

4. **测试验证**
   - 每次优化后进行测试
   - 对比优化前后的性能
   - 确保优化有效

### 9.2 常见性能问题

1. **内存泄漏**
   - 定期检查堆内存使用
   - 分析堆转储
   - 修复内存泄漏

2. **GC频繁**
   - 调整GC参数
   - 减少对象创建
   - 优化对象生命周期

3. **数据库慢查询**
   - 识别慢查询
   - 创建适当的索引
   - 优化查询语句

4. **缓存命中率低**
   - 调整缓存策略
   - 增加缓存大小
   - 优化缓存键设计

## 10. 附录

### 10.1 相关文档

- [ProductionConfigGuide.md](file:///e:/101/microservices/docs/ProductionConfigGuide.md) - 生产环境配置指南
- [DeploymentProcess.md](file:///e:/101/microservices/docs/DeploymentProcess.md) - 部署流程文档

### 10.2 相关工具

- JVisualVM - JVM性能分析工具
- Eclipse MAT - 内存分析工具
- JMeter - 压力测试工具
- Prometheus - 监控工具
- Grafana - 可视化工具

### 10.3 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|---------|
| 1.0.0 | 2025-01-19 | 架构师团队 | 初始版本 |

---

**文档结束**
