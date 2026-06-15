# 缓存机制指南

本文档定义了进销存管理系统的缓存机制，包括多级缓存策略、缓存配置、缓存更新和缓存监控等方面的实现规范。

## 1. 缓存策略设计

### 1.1 多级缓存架构
```
应用层缓存 (Caffeine本地缓存)
     ↓
分布式缓存 (Redis)
     ↓
数据库 (PostgreSQL)
```

### 1.2 缓存层级说明
- **一级缓存**: 应用内本地缓存，访问速度最快，容量有限
- **二级缓存**: 分布式缓存，跨应用共享，容量大，访问速度较快
- **三级缓存**: 数据库，持久化存储，访问速度相对较慢

### 1.3 缓存数据分类
- **热点数据**: 频繁访问的数据，如商品信息、用户信息
- **临时数据**: 短期有效的数据，如会话信息、验证码
- **计算结果**: 复杂计算的结果，如报表数据、统计信息

## 2. 缓存配置

### 2.1 Redis配置
```yaml
# application.yml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 2
        max-wait: 2000ms
      cluster:
        refresh:
          adaptive: true
          period: 30s
```

### 2.2 Caffeine本地缓存配置
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }
    
    Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .weakKeys()
                .recordStats();
    }
    
    @Bean("productCache")
    public Cache<String, Object> productCache() {
        return Caffeine.newBuilder()
                .initialCapacity(50)
                .maximumSize(500)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
    
    @Bean("inventoryCache")
    public Cache<String, Object> inventoryCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
}
```

### 2.3 RedisTemplate配置
```java
@Configuration
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LazyCollectionResolver.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(mapper);
        
        template.setValueSerializer(serializer);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        
        return template;
    }
    
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
```

## 3. 缓存实现

### 3.1 缓存服务接口
```java
public interface CacheService {
    
    // 通用缓存操作
    <T> void put(String key, T value);
    <T> void put(String key, T value, long timeout, TimeUnit unit);
    <T> T get(String key, Class<T> type);
    boolean hasKey(String key);
    void evict(String key);
    void clear();
    
    // 商品缓存操作
    void putProduct(ProductDTO product);
    ProductDTO getProduct(Long productId);
    List<ProductDTO> getAllProducts();
    void updateProductCache(ProductDTO product);
    void evictProduct(Long productId);
    void evictProductCache(Long productId);
    void evictAllProducts();
    void evictAllProductsCache();
    
    // 库存缓存操作
    void putInventory(InventoryDTO inventory);
    InventoryDTO getInventory(Long productId, Long warehouseId);
    void evictInventory(Long productId, Long warehouseId);
    
    // 用户缓存操作
    void putUser(UserDTO user);
    UserDTO getUser(Long userId);
    void evictUser(Long userId);
    
    // 批量操作
    void putProducts(List<ProductDTO> products);
    List<ProductDTO> getProducts(List<Long> productIds);
}
```

### 3.2 缓存服务实现
```java
@Service
@Slf4j
public class CacheServiceImpl implements CacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    @Qualifier("productCache")
    private Cache<String, Object> productCache;
    
    @Autowired
    @Qualifier("inventoryCache")
    private Cache<String, Object> inventoryCache;
    
    // 通用缓存操作
    @Override
    public <T> void put(String key, T value) {
        try {
            // 先放入本地缓存
            if (isLocalCacheKey(key)) {
                putToLocalCache(key, value);
            }
            // 再放入Redis缓存
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Failed to put cache for key: {}", key, e);
        }
    }
    
    @Override
    public <T> void put(String key, T value, long timeout, TimeUnit unit) {
        try {
            // 先放入本地缓存
            if (isLocalCacheKey(key)) {
                putToLocalCache(key, value);
            }
            // 再放入Redis缓存
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("Failed to put cache for key: {}", key, e);
        }
    }
    
    @Override
    public <T> T get(String key, Class<T> type) {
        try {
            // 先从本地缓存获取
            if (isLocalCacheKey(key)) {
                T localValue = getFromLocalCache(key, type);
                if (localValue != null) {
                    return localValue;
                }
            }
            
            // 从Redis缓存获取
            Object redisValue = redisTemplate.opsForValue().get(key);
            if (redisValue != null) {
                // 放入本地缓存
                if (isLocalCacheKey(key)) {
                    putToLocalCache(key, redisValue);
                }
                return type.cast(redisValue);
            }
            
            return null;
        } catch (Exception e) {
            log.error("Failed to get cache for key: {}", key, e);
            return null;
        }
    }
    
    @Override
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Failed to check cache key: {}", key, e);
            return false;
        }
    }
    
    @Override
    public void evict(String key) {
        try {
            // 清除本地缓存
            if (isLocalCacheKey(key)) {
                evictFromLocalCache(key);
            }
            // 清除Redis缓存
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Failed to evict cache for key: {}", key, e);
        }
    }
    
    @Override
    public void clear() {
        try {
            // 清除所有本地缓存
            productCache.invalidateAll();
            inventoryCache.invalidateAll();
            // 清除所有Redis缓存
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("Failed to clear all cache", e);
        }
    }
    
    // 商品缓存操作
    @Override
    public void putProduct(ProductDTO product) {
        String key = buildProductKey(product.getId());
        put(key, product, 30, TimeUnit.MINUTES);
    }
    
    @Override
    public ProductDTO getProduct(Long productId) {
        String key = buildProductKey(productId);
        return get(key, ProductDTO.class);
    }
    
    @Override
    public void evictProduct(Long productId) {
        String key = buildProductKey(productId);
        evict(key);
    }
    
    @Override
    public List<ProductDTO> getAllProducts() {
        try {
            // 从Redis获取所有商品缓存
            Set<String> keys = redisTemplate.keys("product:*");
            if (keys != null && !keys.isEmpty()) {
                List<Object> values = redisTemplate.opsForValue().multiGet(keys);
                if (values != null) {
                    return values.stream()
                            .filter(Objects::nonNull)
                            .map(value -> (ProductDTO) value)
                            .collect(Collectors.toList());
                }
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Failed to get all products from cache", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public void updateProductCache(ProductDTO product) {
        putProduct(product);
    }
    
    @Override
    public void evictProductCache(Long productId) {
        evictProduct(productId);
    }
    
    @Override
    public void evictAllProducts() {
        try {
            // 清除本地商品缓存
            productCache.invalidateAll();
            // 清除Redis中所有商品相关缓存
            Set<String> keys = redisTemplate.keys("product:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("Failed to evict all products cache", e);
        }
    }
    
    @Override
    public void evictAllProductsCache() {
        evictAllProducts();
    }
    
    // 库存缓存操作
    @Override
    public void putInventory(InventoryDTO inventory) {
        String key = buildInventoryKey(inventory.getProductId(), inventory.getWarehouseId());
        put(key, inventory, 15, TimeUnit.MINUTES);
    }
    
    @Override
    public InventoryDTO getInventory(Long productId, Long warehouseId) {
        String key = buildInventoryKey(productId, warehouseId);
        return get(key, InventoryDTO.class);
    }
    
    @Override
    public void evictInventory(Long productId, Long warehouseId) {
        String key = buildInventoryKey(productId, warehouseId);
        evict(key);
    }
    
    // 批量操作
    @Override
    public void putProducts(List<ProductDTO> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        
        try {
            // 批量放入Redis
            Map<String, Object> batchData = new HashMap<>();
            for (ProductDTO product : products) {
                String key = buildProductKey(product.getId());
                batchData.put(key, product);
                // 同时放入本地缓存
                putToLocalCache(key, product);
            }
            redisTemplate.opsForValue().multiSet(batchData);
            
            // 设置过期时间
            for (ProductDTO product : products) {
                String key = buildProductKey(product.getId());
                redisTemplate.expire(key, 30, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.error("Failed to put products cache in batch", e);
        }
    }
    
    @Override
    public List<ProductDTO> getProducts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            List<ProductDTO> result = new ArrayList<>();
            List<String> keys = productIds.stream()
                    .map(this::buildProductKey)
                    .collect(Collectors.toList());
            
            // 批量获取Redis缓存
            List<Object> values = redisTemplate.opsForValue().multiGet(keys);
            if (values != null) {
                for (int i = 0; i < values.size(); i++) {
                    Object value = values.get(i);
                    if (value instanceof ProductDTO) {
                        ProductDTO product = (ProductDTO) value;
                        result.add(product);
                        // 放入本地缓存
                        putToLocalCache(keys.get(i), product);
                    }
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("Failed to get products cache in batch", e);
            return new ArrayList<>();
        }
    }
    
    // 本地缓存操作
    private void putToLocalCache(String key, Object value) {
        if (key.startsWith("product:")) {
            productCache.put(key, value);
        } else if (key.startsWith("inventory:")) {
            inventoryCache.put(key, value);
        }
    }
    
    private <T> T getFromLocalCache(String key, Class<T> type) {
        if (key.startsWith("product:")) {
            Object value = productCache.getIfPresent(key);
            return value != null ? type.cast(value) : null;
        } else if (key.startsWith("inventory:")) {
            Object value = inventoryCache.getIfPresent(key);
            return value != null ? type.cast(value) : null;
        }
        return null;
    }
    
    private void evictFromLocalCache(String key) {
        if (key.startsWith("product:")) {
            productCache.invalidate(key);
        } else if (key.startsWith("inventory:")) {
            inventoryCache.invalidate(key);
        }
    }
    
    private boolean isLocalCacheKey(String key) {
        return key.startsWith("product:") || key.startsWith("inventory:");
    }
    
    private String buildProductKey(Long productId) {
        return "product:" + productId;
    }
    
    private String buildInventoryKey(Long productId, Long warehouseId) {
        return "inventory:" + productId + ":" + warehouseId;
    }
}
```

### 3.3 缓存注解使用
```java
@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CacheService cacheService;
    
    @Cacheable(value = "products", key = "#id")
    @Override
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product from database: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return convertToDTO(product);
    }
    
    @CachePut(value = "products", key = "#result.id")
    @Override
    public ProductDTO createProduct(ProductCreateRequest request) {
        // 创建商品逻辑
        Product product = new Product();
        // 设置属性...
        Product savedProduct = productRepository.save(product);
        
        // 放入缓存
        ProductDTO productDTO = convertToDTO(savedProduct);
        cacheService.putProduct(productDTO);
        
        return productDTO;
    }
    
    @CachePut(value = "products", key = "#id")
    @Override
    public ProductDTO updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        // 更新属性...
        Product updatedProduct = productRepository.save(product);
        
        // 更新缓存
        ProductDTO productDTO = convertToDTO(updatedProduct);
        cacheService.putProduct(productDTO);
        
        return productDTO;
    }
    
    @CacheEvict(value = "products", key = "#id")
    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        product.setDeletedAt(Instant.now());
        productRepository.save(product);
        
        // 清除缓存
        cacheService.evictProduct(id);
    }
    
    @CacheEvict(value = "products", allEntries = true)
    @Override
    public void batchUpdateProducts(List<ProductUpdateRequest> requests) {
        // 批量更新逻辑
        for (ProductUpdateRequest request : requests) {
            updateProduct(request.getId(), request);
        }
        
        // 清除所有商品缓存
        cacheService.evictAllProducts();
    }
}
```

## 4. 缓存更新策略

### 4.1 Cache-Aside模式
```java
@Service
public class CacheAsideService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CacheService cacheService;
    
    public ProductDTO getProduct(Long productId) {
        // 1. 先从缓存获取
        ProductDTO product = cacheService.getProduct(productId);
        if (product != null) {
            return product;
        }
        
        // 2. 缓存未命中，从数据库获取
        Product entity = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        product = convertToDTO(entity);
        
        // 3. 放入缓存
        cacheService.putProduct(product);
        
        return product;
    }
    
    public ProductDTO updateProduct(Long productId, ProductUpdateRequest request) {
        // 1. 更新数据库
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        // 更新属性...
        Product updatedProduct = productRepository.save(product);
        
        // 2. 更新缓存
        ProductDTO productDTO = convertToDTO(updatedProduct);
        cacheService.putProduct(productDTO);
        
        return productDTO;
    }
    
    public void deleteProduct(Long productId) {
        // 1. 删除数据库记录
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        productRepository.delete(product);
        
        // 2. 清除缓存
        cacheService.evictProduct(productId);
    }
}
```

### 4.2 Write-Through模式
```java
@Service
public class WriteThroughService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CacheService cacheService;
    
    public ProductDTO createProduct(ProductCreateRequest request) {
        // 1. 创建数据库记录
        Product product = new Product();
        // 设置属性...
        Product savedProduct = productRepository.save(product);
        
        // 2. 同时写入缓存（Write-Through）
        ProductDTO productDTO = convertToDTO(savedProduct);
        cacheService.putProduct(productDTO);
        
        return productDTO;
    }
}
```

### 4.3 缓存失效策略
```java
@Component
public class CacheInvalidationService {
    
    @Autowired
    private CacheService cacheService;
    
    @EventListener
    public void handleProductUpdatedEvent(ProductUpdatedEvent event) {
        // 商品更新事件触发缓存失效
        cacheService.evictProduct(event.getProductId());
    }
    
    @EventListener
    public void handleInventoryChangedEvent(InventoryChangedEvent event) {
        // 库存变更事件触发缓存失效
        cacheService.evictInventory(event.getProductId(), event.getWarehouseId());
    }
    
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanExpiredCache() {
        // 清理过期缓存
        log.info("Cleaning expired cache entries");
    }
}
```

## 5. 缓存监控和统计

### 5.1 缓存统计指标
```java
@Component
public class CacheMetricsService {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Autowired
    @Qualifier("productCache")
    private Cache<String, Object> productCache;
    
    private final Counter cacheHits;
    private final Counter cacheMisses;
    private final Timer cacheLoadTimer;
    
    public CacheMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.cacheHits = Counter.builder("cache.hits")
                .tag("cache", "product")
                .register(meterRegistry);
        this.cacheMisses = Counter.builder("cache.misses")
                .tag("cache", "product")
                .register(meterRegistry);
        this.cacheLoadTimer = Timer.builder("cache.load.time")
                .tag("cache", "product")
                .register(meterRegistry);
    }
    
    public void recordCacheHit() {
        cacheHits.increment();
    }
    
    public void recordCacheMiss() {
        cacheMisses.increment();
    }
    
    public Timer.Sample startLoadTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordLoadTime(Timer.Sample sample) {
        sample.stop(cacheLoadTimer);
    }
    
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void reportCacheStats() {
        CacheStats stats = productCache.stats();
        log.info("Product cache stats - Hit rate: {}, Miss rate: {}, Load count: {}", 
                stats.hitRate(), stats.missRate(), stats.loadCount());
        
        // 上报到监控系统
        Gauge.builder("cache.hit.rate")
                .tag("cache", "product")
                .register(meterRegistry, stats, CacheStats::hitRate);
    }
}
```

### 5.2 缓存健康检查
```java
@Component
public class CacheHealthIndicator implements HealthIndicator {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public Health health() {
        try {
            // 检查Redis连接
            String redisStatus = redisTemplate.getConnectionFactory().getConnection().ping();
            if (!"PONG".equals(redisStatus)) {
                return Health.down()
                        .withDetail("redis", "Redis ping failed")
                        .build();
            }
            
            // 检查缓存读写
            String testKey = "health_check_" + System.currentTimeMillis();
            String testValue = "test_value";
            redisTemplate.opsForValue().set(testKey, testValue, 10, TimeUnit.SECONDS);
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
            
            if (!testValue.equals(retrievedValue)) {
                return Health.down()
                        .withDetail("redis", "Redis read/write test failed")
                        .build();
            }
            
            // 清理测试数据
            redisTemplate.delete(testKey);
            
            return Health.up()
                    .withDetail("redis", "Redis connection OK")
                    .withDetail("test", "Cache read/write test passed")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
```

## 6. 缓存最佳实践

### 6.1 缓存键设计
```java
public class CacheKeyGenerator {
    
    public static String buildProductKey(Long productId) {
        return "product:" + productId;
    }
    
    public static String buildInventoryKey(Long productId, Long warehouseId) {
        return "inventory:" + productId + ":" + warehouseId;
    }
    
    public static String buildUserKey(Long userId) {
        return "user:" + userId;
    }
    
    public static String buildUserPermissionKey(Long userId) {
        return "user:" + userId + ":permissions";
    }
    
    public static String buildReportKey(String reportType, LocalDate date) {
        return "report:" + reportType + ":" + date.toString();
    }
}
```

### 6.2 缓存穿透防护
```java
@Service
public class CachePenetrationProtectionService {
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private ProductRepository productRepository;
    
    public ProductDTO getProductSafely(Long productId) {
        // 使用布隆过滤器检查商品是否存在
        if (!mightContainProduct(productId)) {
            return null; // 商品肯定不存在，直接返回
        }
        
        // 正常缓存查询流程
        ProductDTO product = cacheService.getProduct(productId);
        if (product != null) {
            return product;
        }
        
        // 数据库查询
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            product = convertToDTO(productOpt.get());
            cacheService.putProduct(product);
            return product;
        } else {
            // 将空结果缓存一小段时间，防止缓存穿透
            cacheService.put("product:empty:" + productId, "EMPTY", 5, TimeUnit.MINUTES);
            return null;
        }
    }
    
    private boolean mightContainProduct(Long productId) {
        // 布隆过滤器实现
        // 这里简化处理，实际应使用Redis的布隆过滤器模块
        return true;
    }
}
```

### 6.3 缓存雪崩防护
```java
@Service
public class CacheAvalancheProtectionService {
    
    private final Random random = new Random();
    
    public ProductDTO getProductWithExpirationRandomization(Long productId) {
        // 获取缓存
        ProductDTO product = cacheService.getProduct(productId);
        if (product != null) {
            return product;
        }
        
        // 数据库查询
        Product entity = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        product = convertToDTO(entity);
        
        // 随机过期时间，防止缓存雪崩
        int randomExpire = 1800 + random.nextInt(1800); // 30-60分钟随机过期
        cacheService.put("product:" + productId, product, randomExpire, TimeUnit.SECONDS);
        
        return product;
    }
}
```

本指南为进销存管理系统的缓存机制提供了全面的规范和实现指导，所有开发团队应严格遵循这些规范进行缓存开发和配置。