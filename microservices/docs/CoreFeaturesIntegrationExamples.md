# 核心功能集成示例文档

## 版本
- 版本：1.0.0
- 创建日期：2025-01-19
- 最后更新：2025-01-19

## 1. 文档概述

### 1.1 目的
本文档提供了库存管理系统核心功能的完整集成示例，展示如何将监控、告警、追踪、缓存、健康检查等功能集成到实际业务场景中。通过这些示例，开发人员可以快速理解和使用系统的核心功能。

### 1.2 适用范围
- 监控系统集成
- 告警系统集成
- 追踪系统集成
- 缓存系统集成
- 健康检查集成
- 服务间调用集成
- 异常处理集成

### 1.3 目标读者
- 后端开发工程师
- 全栈开发工程师
- 系统架构师
- DevOps工程师

## 2. 监控系统集成示例

### 2.1 订单处理监控

```java
@Service
@RequiredArgsConstructor
public class OrderMonitoringService {

    private final MeterRegistry meterRegistry;
    private final OrderService orderService;

    public Order createOrder(OrderDTO orderDTO) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            Order order = orderService.createOrder(orderDTO);
            
            Counter.builder("orders.created")
                .tag("status", "success")
                .register(meterRegistry)
                .increment();
            
            return order;
        } catch (Exception e) {
            Counter.builder("orders.created")
                .tag("status", "failed")
                .register(meterRegistry)
                .increment();
            
            throw e;
        } finally {
            sample.stop(Timer.builder("order.creation.time")
                .tag("service", "order-service")
                .register(meterRegistry));
        }
    }
}
```

### 2.2 库存监控

```java
@Service
@RequiredArgsConstructor
public class InventoryMonitoringService {

    private final MeterRegistry meterRegistry;
    private final InventoryService inventoryService;

    public void updateInventory(Long productId, Integer quantity) {
        Gauge.builder("inventory.level")
            .tag("product.id", productId.toString())
            .register(meterRegistry, inventoryService, 
                service -> service.getInventoryLevel(productId));
        
        inventoryService.updateInventory(productId, quantity);
        
        DistributionSummary.builder("inventory.change")
            .tag("product.id", productId.toString())
            .register(meterRegistry)
            .record(quantity);
    }
}
```

## 3. 告警系统集成示例

### 3.1 订单告警

```java
@Service
@RequiredArgsConstructor
public class OrderAlertService {

    private final AlertRuleEngine alertRuleEngine;
    private final NotificationManager notificationManager;

    public void checkOrderAlerts() {
        AlertContext context = AlertContext.builder()
            .service("order-service")
            .instance("order-service-1")
            .timestamp(LocalDateTime.now())
            .build();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("pending.orders", orderService.getPendingOrderCount());
        metrics.put("failed.orders", orderService.getFailedOrderCount());
        metrics.put("processing.time", orderService.getAverageProcessingTime());

        List<Alert> alerts = alertRuleEngine.evaluateRules(context, metrics);

        alerts.forEach(alert -> {
            NotificationChannel channel = NotificationChannel.builder()
                .type(NotificationChannelType.DINGTALK)
                .config(Map.of("webhook", "https://oapi.dingtalk.com/robot/send"))
                .build();

            notificationManager.sendNotification(alert, channel);
        });
    }
}
```

### 3.2 库存告警

```java
@Service
@RequiredArgsConstructor
public class InventoryAlertService {

    private final AlertRuleEngine alertRuleEngine;

    public void checkInventoryAlerts() {
        AlertContext context = AlertContext.builder()
            .service("inventory-service")
            .instance("inventory-service-1")
            .timestamp(LocalDateTime.now())
            .build();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("low.stock.products", inventoryService.getLowStockProducts());
        metrics.put("out.of.stock.products", inventoryService.getOutOfStockProducts());
        metrics.put("inventory.turnover", inventoryService.getInventoryTurnover());

        List<Alert> alerts = alertRuleEngine.evaluateRules(context, metrics);

        alerts.forEach(alert -> {
            if (alert.getLevel() == AlertLevel.CRITICAL) {
                sendCriticalAlert(alert);
            } else if (alert.getLevel() == AlertLevel.WARNING) {
                sendWarningAlert(alert);
            }
        });
    }
}
```

## 4. 追踪系统集成示例

### 4.1 订单处理追踪

```java
@Service
@RequiredArgsConstructor
public class OrderTracingService {

    private final Tracer tracer;

    public Order processOrder(OrderDTO orderDTO) {
        Span parentSpan = tracer.spanBuilder("process-order")
            .setSpanKind(SpanKind.SERVER)
            .startSpan();

        try {
            parentSpan.setAttribute("order.id", orderDTO.getId());
            parentSpan.setAttribute("customer.id", orderDTO.getCustomerId());

            validateOrder(parentSpan, orderDTO);
            checkInventory(parentSpan, orderDTO);
            createOrder(parentSpan, orderDTO);
            sendConfirmation(parentSpan, orderDTO);

            return order;
        } catch (Exception e) {
            parentSpan.recordException(e);
            throw e;
        } finally {
            parentSpan.end();
        }
    }

    private void validateOrder(Span parentSpan, OrderDTO orderDTO) {
        Span span = tracer.spanBuilder("validate-order")
            .setParent(parentSpan)
            .startSpan();

        try {
            span.setAttribute("validation.result", "success");
        } catch (ValidationException e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    private void checkInventory(Span parentSpan, OrderDTO orderDTO) {
        Span span = tracer.spanBuilder("check-inventory")
            .setParent(parentSpan)
            .setSpanKind(SpanKind.CLIENT)
            .startSpan();

        try {
            inventoryClient.checkAvailability(orderDTO.getItems());
            span.setAttribute("inventory.status", "available");
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
```

### 4.2 跨服务追踪

```java
@Service
@RequiredArgsConstructor
public class CrossServiceTracingService {

    private final Tracer tracer;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final CustomerClient customerClient;

    public OrderDTO createOrder(OrderDTO orderDTO) {
        Span parentSpan = tracer.spanBuilder("create-order")
            .setSpanKind(SpanKind.SERVER)
            .startSpan();

        try {
            Span productSpan = tracer.spanBuilder("get-product")
                .setParent(parentSpan)
                .setSpanKind(SpanKind.CLIENT)
                .startSpan();

            ProductDTO product = productClient.getProductById(orderDTO.getProductId());
            productSpan.setAttribute("product.id", product.getId());
            productSpan.end();

            Span inventorySpan = tracer.spanBuilder("check-inventory")
                .setParent(parentSpan)
                .setSpanKind(SpanKind.CLIENT)
                .startSpan();

            InventoryDTO inventory = inventoryClient.checkAvailability(orderDTO.getProductId());
            inventorySpan.setAttribute("inventory.available", inventory.getAvailable());
            inventorySpan.end();

            Span customerSpan = tracer.spanBuilder("get-customer")
                .setParent(parentSpan)
                .setSpanKind(SpanKind.CLIENT)
                .startSpan();

            CustomerDTO customer = customerClient.getCustomerById(orderDTO.getCustomerId());
            customerSpan.setAttribute("customer.id", customer.getId());
            customerSpan.end();

            return orderDTO;
        } catch (Exception e) {
            parentSpan.recordException(e);
            throw e;
        } finally {
            parentSpan.end();
        }
    }
}
```

## 5. 缓存系统集成示例

### 5.1 产品缓存

```java
@Service
@RequiredArgsConstructor
public class ProductCacheService {

    private final ProductRepository productRepository;

    @Cacheable(value = "products", key = "#id")
    public Product getProduct(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                ErrorCodeConstants.PRODUCT_NOT_FOUND,
                "Product not found with id: " + id
            ));
    }

    @CachePut(value = "products", key = "#product.id")
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @CacheEvict(value = "products", allEntries = true)
    public void clearAllProducts() {
        log.info("Clearing all products from cache");
    }
}
```

### 5.2 分布式锁

```java
@Service
@RequiredArgsConstructor
public class DistributedLockService {

    private final StringRedisTemplate redisTemplate;

    public boolean acquireLock(String key, String value, Duration ttl) {
        Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(key, value, ttl);

        if (Boolean.TRUE.equals(acquired)) {
            log.info("Lock acquired: {}", key);
        } else {
            log.warn("Lock already held: {}", key);
        }

        return Boolean.TRUE.equals(acquired);
    }

    public void releaseLock(String key, String value) {
        Boolean released = redisTemplate.opsForValue()
            .setIfAbsent(key, value, Duration.ZERO);

        if (Boolean.TRUE.equals(released)) {
            log.info("Lock released: {}", key);
        } else {
            log.warn("Lock not held or already released: {}", key);
        }
    }

    public <T> T executeWithLock(String key, Duration ttl, Supplier<T> supplier) {
        String value = UUID.randomUUID().toString();

        if (acquireLock(key, value, ttl)) {
            try {
                return supplier.get();
            } finally {
                releaseLock(key, value);
            }
        } else {
            throw new BusinessException(
                ErrorCodeConstants.LOCK_ACQUISITION_FAILED,
                "Failed to acquire lock: " + key
            );
        }
    }
}
```

## 6. 健康检查集成示例

### 6.1 综合健康检查

```java
@Component
@RequiredArgsConstructor
public class CompositeHealthIndicator implements HealthIndicator {

    private final DatabaseHealthIndicator databaseHealthIndicator;
    private final RedisHealthIndicator redisHealthIndicator;
    private final KafkaHealthIndicator kafkaHealthIndicator;

    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();

        Health dbHealth = databaseHealthIndicator.health();
        if (dbHealth.getStatus() == Status.UP) {
            builder.up()
                  .withDetail("database", "UP")
                  .withDetails(dbHealth.getDetails());
        } else {
            builder.down()
                  .withDetail("database", "DOWN")
                  .withDetails(dbHealth.getDetails());
        }

        Health redisHealth = redisHealthIndicator.health();
        if (redisHealth.getStatus() == Status.UP) {
            builder.withDetail("redis", "UP")
                  .withDetails(redisHealth.getDetails());
        } else {
            builder.withDetail("redis", "DOWN")
                  .withDetails(redisHealth.getDetails());
        }

        Health kafkaHealth = kafkaHealthIndicator.health();
        if (kafkaHealth.getStatus() == Status.UP) {
            builder.withDetail("kafka", "UP")
                  .withDetails(kafkaHealth.getDetails());
        } else {
            builder.withDetail("kafka", "DOWN")
                  .withDetails(kafkaHealth.getDetails());
        }

        return builder.build();
    }
}
```

### 6.2 业务健康检查

```java
@Component
@RequiredArgsConstructor
public class BusinessHealthIndicator implements HealthIndicator {

    private final OrderService orderService;
    private final InventoryService inventoryService;

    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();

        long pendingOrders = orderService.getPendingOrderCount();
        if (pendingOrders > 1000) {
            builder.down()
                  .withDetail("pending.orders", pendingOrders)
                  .withDetail("status", "Too many pending orders");
        } else {
            builder.up()
                  .withDetail("pending.orders", pendingOrders)
                  .withDetail("status", "Normal");
        }

        long lowStockProducts = inventoryService.getLowStockProductsCount();
        if (lowStockProducts > 50) {
            builder.withDetail("low.stock.products", lowStockProducts)
                  .withDetail("status", "Many products with low stock");
        } else {
            builder.withDetail("low.stock.products", lowStockProducts)
                  .withDetail("status", "Normal");
        }

        return builder.build();
    }
}
```

## 7. 服务间调用集成示例

### 7.1 订单服务集成

```java
@Service
@RequiredArgsConstructor
public class OrderIntegrationService {

    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final CustomerClient customerClient;

    public OrderDTO createOrder(OrderDTO orderDTO) {
        ProductDTO product = productClient.getProductById(orderDTO.getProductId());
        
        if (product == null) {
            throw new BusinessException(
                ErrorCodeConstants.PRODUCT_NOT_FOUND,
                "Product not found"
            );
        }

        InventoryDTO inventory = inventoryClient.checkAvailability(orderDTO.getProductId());
        
        if (!inventory.getAvailable()) {
            throw new BusinessException(
                ErrorCodeConstants.INVENTORY_INSUFFICIENT,
                "Insufficient inventory"
            );
        }

        CustomerDTO customer = customerClient.getCustomerById(orderDTO.getCustomerId());
        
        if (customer == null) {
            throw new BusinessException(
                ErrorCodeConstants.CUSTOMER_NOT_FOUND,
                "Customer not found"
            );
        }

        return orderDTO;
    }
}
```

### 7.2 熔断和重试

```java
@FeignClient(
    name = "product-service",
    fallbackFactory = ProductClientFallback.class,
    configuration = ProductClientConfig.class
)
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductDTO getProductById(@PathVariable("id") Long id);
}

@Configuration
public class ProductClientConfig {

    @Bean
    public Request.Options requestOptions(OptionsFactory optionsFactory) {
        return new Request.Options(
            optionsFactory,
            Duration.ofSeconds(5),
            Duration.ofSeconds(10),
            true,
            Duration.ofSeconds(30),
            Duration.ofSeconds(5),
            3
        );
    }
}
```

## 8. 异常处理集成示例

### 8.1 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e) {
        log.error("Business exception: {}", e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(ValidationException e) {
        log.error("Validation exception: {}", e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(
                ErrorCodeConstants.VALIDATION_ERROR,
                e.getMessage()
            ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        log.error("Unexpected exception: {}", e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(
                ErrorCodeConstants.INTERNAL_ERROR,
                "Internal server error"
            ));
    }
}
```

### 8.2 业务异常处理

```java
@Service
@RequiredArgsConstructor
public class OrderExceptionService {

    private final OrderRepository orderRepository;

    public Order createOrder(OrderDTO orderDTO) {
        try {
            validateOrder(orderDTO);
            checkInventory(orderDTO);
            return orderRepository.save(convertToEntity(orderDTO));
        } catch (ValidationException e) {
            throw new BusinessException(
                ErrorCodeConstants.VALIDATION_ERROR,
                "Order validation failed: " + e.getMessage(),
                e
            );
        } catch (InventoryException e) {
            throw new BusinessException(
                ErrorCodeConstants.INVENTORY_ERROR,
                "Inventory check failed: " + e.getMessage(),
                e
            );
        }
    }

    private void validateOrder(OrderDTO orderDTO) {
        if (orderDTO.getCustomerId() == null) {
            throw new ValidationException("Customer ID is required");
        }

        if (orderDTO.getItems() == null || orderDTO.getItems().isEmpty()) {
            throw new ValidationException("Order items are required");
        }

        if (orderDTO.getTotalAmount() <= 0) {
            throw new ValidationException("Order amount must be positive");
        }
    }

    private void checkInventory(OrderDTO orderDTO) {
        for (OrderItemDTO item : orderDTO.getItems()) {
            InventoryDTO inventory = inventoryClient.checkAvailability(item.getProductId());
            
            if (!inventory.getAvailable() || inventory.getQuantity() < item.getQuantity()) {
                throw new InventoryException(
                    "Insufficient inventory for product: " + item.getProductId()
                );
            }
        }
    }
}
```

## 9. 完整集成示例

### 9.1 订单创建完整流程

```java
@Service
@RequiredArgsConstructor
public class OrderCreationService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final CustomerClient customerClient;
    private final MeterRegistry meterRegistry;
    private final Tracer tracer;
    private final AlertRuleEngine alertRuleEngine;
    private final NotificationManager notificationManager;

    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        Span parentSpan = tracer.spanBuilder("create-order")
            .setSpanKind(SpanKind.SERVER)
            .startSpan();

        try {
            parentSpan.setAttribute("order.id", orderDTO.getId());
            parentSpan.setAttribute("customer.id", orderDTO.getCustomerId());

            Timer.Sample sample = Timer.start(meterRegistry);

            validateOrder(parentSpan, orderDTO);
            checkInventory(parentSpan, orderDTO);
            checkCustomer(parentSpan, orderDTO);
            reserveInventory(parentSpan, orderDTO);
            saveOrder(parentSpan, orderDTO);
            sendConfirmation(parentSpan, orderDTO);
            checkAlerts(parentSpan, orderDTO);

            sample.stop(Timer.builder("order.creation.time")
                .tag("service", "order-service")
                .register(meterRegistry));

            Counter.builder("orders.created")
                .tag("status", "success")
                .register(meterRegistry)
                .increment();

            return orderDTO;
        } catch (BusinessException e) {
            parentSpan.recordException(e);

            Counter.builder("orders.created")
                .tag("status", "failed")
                .tag("error.type", "business")
                .register(meterRegistry)
                .increment();

            throw e;
        } catch (Exception e) {
            parentSpan.recordException(e);

            Counter.builder("orders.created")
                .tag("status", "failed")
                .tag("error.type", "system")
                .register(meterRegistry)
                .increment();

            throw new BusinessException(
                ErrorCodeConstants.INTERNAL_ERROR,
                "Failed to create order",
                e
            );
        } finally {
            parentSpan.end();
        }
    }

    private void validateOrder(Span parentSpan, OrderDTO orderDTO) {
        Span span = tracer.spanBuilder("validate-order")
            .setParent(parentSpan)
            .startSpan();

        try {
            if (orderDTO.getCustomerId() == null) {
                throw new BusinessException(
                    ErrorCodeConstants.VALIDATION_ERROR,
                    "Customer ID is required"
                );
            }

            if (orderDTO.getItems() == null || orderDTO.getItems().isEmpty()) {
                throw new BusinessException(
                    ErrorCodeConstants.VALIDATION_ERROR,
                    "Order items are required"
                );
            }

            span.setAttribute("validation.result", "success");
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    private void checkInventory(Span parentSpan, OrderDTO orderDTO) {
        Span span = tracer.spanBuilder("check-inventory")
            .setParent(parentSpan)
            .setSpanKind(SpanKind.CLIENT)
            .startSpan();

        try {
            for (OrderItemDTO item : orderDTO.getItems()) {
                InventoryDTO inventory = inventoryClient.checkAvailability(item.getProductId());
                
                if (!inventory.getAvailable() || inventory.getQuantity() < item.getQuantity()) {
                    throw new BusinessException(
                        ErrorCodeConstants.INVENTORY_INSUFFICIENT,
                        "Insufficient inventory for product: " + item.getProductId()
                    );
                }
            }

            span.setAttribute("inventory.status", "available");
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    private void checkCustomer(Span parentSpan, OrderDTO orderDTO) {
        Span span = tracer.spanBuilder("check-customer")
            .setParent(parentSpan)
            .setSpanKind(SpanKind.CLIENT)
            .startSpan();

        try {
            CustomerDTO customer = customerClient.getCustomerById(orderDTO.getCustomerId());
            
            if (customer == null) {
                throw new BusinessException(
                    ErrorCodeConstants.CUSTOMER_NOT_FOUND,
                    "Customer not found"
                );
            }

            span.setAttribute("customer.id", customer.getId());
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    private void reserveInventory(Span parentSpan, OrderDTO orderDTO) {
        Span span = tracer.spanBuilder("reserve-inventory")
            .setParent(parentSpan)
            .setSpanKind(SpanKind.CLIENT)
            .startSpan();

        try {
            for (OrderItemDTO item : orderDTO.getItems()) {
                inventoryClient.reserve(item.getProductId(), item.getQuantity());
            }

            span.setAttribute("reservation.status", "success");
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    private void saveOrder(Span parentSpan, OrderDTO orderDTO) {
        Span span = tracer.spanBuilder("save-order")
            .setParent(parentSpan)
            .startSpan();

        try {
            Order order = convertToEntity(orderDTO);
            orderRepository.save(order);

            span.setAttribute("order.id", order.getId());
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    private void sendConfirmation(Span parentSpan, OrderDTO orderDTO) {
        Span span = tracer.spanBuilder("send-confirmation")
            .setParent(parentSpan)
            .startSpan();

        try {
            customerClient.sendOrderConfirmation(orderDTO);
            span.setAttribute("confirmation.status", "sent");
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    private void checkAlerts(Span parentSpan, OrderDTO orderDTO) {
        AlertContext context = AlertContext.builder()
            .service("order-service")
            .instance("order-service-1")
            .timestamp(LocalDateTime.now())
            .build();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("order.amount", orderDTO.getTotalAmount());
        metrics.put("order.items", orderDTO.getItems().size());
        metrics.put("pending.orders", orderRepository.countByStatus(OrderStatus.PENDING));

        List<Alert> alerts = alertRuleEngine.evaluateRules(context, metrics);

        alerts.forEach(alert -> {
            NotificationChannel channel = NotificationChannel.builder()
                .type(NotificationChannelType.EMAIL)
                .config(Map.of(
                    "to", "ops-team@example.com",
                    "subject", "[ALERT] Order Alert"
                ))
                .build();

            notificationManager.sendNotification(alert, channel);
        });
    }
}
```

### 9.2 产品查询完整流程

```java
@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final MeterRegistry meterRegistry;
    private final Tracer tracer;

    @Cacheable(value = "products", key = "#id")
    public ProductDTO getProduct(Long id) {
        Span span = tracer.spanBuilder("get-product")
            .setSpanKind(SpanKind.SERVER)
            .startSpan();

        try {
            Timer.Sample sample = Timer.start(meterRegistry);

            Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                    ErrorCodeConstants.PRODUCT_NOT_FOUND,
                    "Product not found with id: " + id
                ));

            sample.stop(Timer.builder("product.query.time")
                .tag("service", "product-service")
                .register(meterRegistry));

            Counter.builder("products.queries")
                .tag("status", "success")
                .register(meterRegistry)
                .increment();

            span.setAttribute("product.id", product.getId());
            span.setAttribute("product.name", product.getName());

            return convertToDTO(product);
        } catch (BusinessException e) {
            span.recordException(e);

            Counter.builder("products.queries")
                .tag("status", "not-found")
                .register(meterRegistry)
                .increment();

            throw e;
        } catch (Exception e) {
            span.recordException(e);

            Counter.builder("products.queries")
                .tag("status", "error")
                .register(meterRegistry)
                .increment();

            throw new BusinessException(
                ErrorCodeConstants.INTERNAL_ERROR,
                "Failed to query product",
                e
            );
        } finally {
            span.end();
        }
    }

    @CacheEvict(value = "products", key = "#id")
    public void updateProduct(ProductDTO productDTO) {
        Span span = tracer.spanBuilder("update-product")
            .setSpanKind(SpanKind.SERVER)
            .startSpan();

        try {
            Timer.Sample sample = Timer.start(meterRegistry);

            Product product = convertToEntity(productDTO);
            productRepository.save(product);

            sample.stop(Timer.builder("product.update.time")
                .tag("service", "product-service")
                .register(meterRegistry));

            Counter.builder("products.updated")
                .tag("status", "success")
                .register(meterRegistry)
                .increment();

            span.setAttribute("product.id", product.getId());
        } catch (Exception e) {
            span.recordException(e);

            Counter.builder("products.updated")
                .tag("status", "error")
                .register(meterRegistry)
                .increment();

            throw new BusinessException(
                ErrorCodeConstants.INTERNAL_ERROR,
                "Failed to update product",
                e
            );
        } finally {
            span.end();
        }
    }
}
```

## 10. 最佳实践

### 10.1 监控最佳实践

1. **指标命名规范**
   - 使用小写字母和点号分隔
   - 包含服务名称和指标类型
   - 示例：`orders.created`, `products.queries`

2. **标签使用规范**
   - 使用有意义的标签键值对
   - 避免高基数标签
   - 示例：`status:success`, `service:order-service`

3. **指标采集时机**
   - 在关键业务操作前后采集
   - 记录成功和失败状态
   - 使用Timer记录操作耗时

### 10.2 告警最佳实践

1. **告警级别定义**
   - INFO：信息性告警
   - WARNING：警告性告警
   - ERROR：错误性告警
   - CRITICAL：严重告警

2. **告警规则设计**
   - 使用静态阈值和动态基线结合
   - 设置合理的告警间隔
   - 配置告警抑制和去重

3. **通知渠道选择**
   - CRITICAL：使用多渠道通知
   - WARNING：使用邮件和IM通知
   - INFO：使用邮件通知

### 10.3 追踪最佳实践

1. **Span命名规范**
   - 使用动词-名词格式
   - 描述操作类型
   - 示例：`create-order`, `get-product`

2. **Trace上下文传播**
   - 在服务间调用时传播TraceContext
   - 使用Feign拦截器自动传播
   - 确保Span父子关系正确

3. **Span属性添加**
   - 添加业务关键属性
   - 添加错误信息
   - 避免添加敏感信息

### 10.4 缓存最佳实践

1. **缓存键设计**
   - 使用唯一标识符
   - 避免缓存键冲突
   - 示例：`products:{id}`

2. **缓存过期策略**
   - 根据数据更新频率设置TTL
   - 使用合理的缓存时间
   - 避免缓存雪崩

3. **缓存更新策略**
   - 使用@CachePut更新缓存
   - 使用@CacheEvict清除缓存
   - 考虑缓存一致性

### 10.5 异常处理最佳实践

1. **异常分类**
   - 业务异常：BusinessException
   - 系统异常：RuntimeException
   - 验证异常：ValidationException

2. **错误码管理**
   - 使用统一的错误码
   - 提供清晰的错误信息
   - 记录详细的错误上下文

3. **异常日志记录**
   - 记录完整的异常堆栈
   - 添加业务上下文信息
   - 使用合适的日志级别

## 11. 故障排查

### 11.1 监控问题排查

1. **指标缺失**
   - 检查MeterRegistry配置
   - 确认指标注册代码
   - 验证Prometheus抓取配置

2. **指标异常**
   - 检查业务逻辑
   - 验证标签使用
   - 确认指标计算逻辑

### 11.2 告警问题排查

1. **告警未触发**
   - 检查告警规则配置
   - 确认指标数据
   - 验证告警条件

2. **告警误报**
   - 调整告警阈值
   - 优化告警规则
   - 配置告警抑制

### 11.3 追踪问题排查

1. **Trace数据缺失**
   - 检查Tracer配置
   - 确认Span创建代码
   - 验证TraceContext传播

2. **Span数据不完整**
   - 检查Span属性添加
   - 确认Span嵌套关系
   - 验证Span结束逻辑

### 11.4 缓存问题排查

1. **缓存未命中**
   - 检查缓存键配置
   - 确认缓存注解使用
   - 验证缓存TTL设置

2. **缓存不一致**
   - 检查缓存更新逻辑
   - 确认缓存清除时机
   - 验证缓存一致性策略

## 12. 附录

### 12.1 相关文档

- [MetricsCollectionExample.java](file:///e:/101/microservices/common/src/main/java/com/inventory/monitoring/examples/MetricsCollectionExample.java)
- [AlertRuleExample.java](file:///e:/101/microservices/common/src/main/java/com/inventory/monitoring/examples/AlertRuleExample.java)
- [DistributedTracingExample.java](file:///e:/101/microservices/common/src/main/java/com/inventory/monitoring/examples/DistributedTracingExample.java)
- [AlertNotificationExample.java](file:///e:/101/microservices/common/src/main/java/com/inventory/monitoring/examples/AlertNotificationExample.java)
- [MonitoringDataTransportExample.java](file:///e:/101/microservices/common/src/main/java/com/inventory/monitoring/examples/MonitoringDataTransportExample.java)
- [HealthCheckExample.java](file:///e:/101/microservices/common/src/main/java/com/inventory/monitoring/examples/HealthCheckExample.java)
- [CacheUsageExample.java](file:///e:/101/microservices/common/src/main/java/com/inventory/monitoring/examples/CacheUsageExample.java)
- [GatewayConfigurationExample.java](file:///e:/101/microservices/support-services/gateway-service/src/main/java/com/inventory/gatewayservice/examples/GatewayConfigurationExample.java)
- [FeignClientExample.java](file:///e:/101/microservices/common/src/main/java/com/inventory/common/examples/FeignClientExample.java)
- [ExceptionHandlingExample.java](file:///e:/101/microservices/common/src/main/java/com/inventory/common/examples/ExceptionHandlingExample.java)

### 12.2 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|---------|
| 1.0.0 | 2025-01-19 | 架构师团队 | 初始版本 |

---

**文档结束**
