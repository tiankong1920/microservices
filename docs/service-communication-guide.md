# 服务间通信机制指南

本文档定义了进销存管理系统中微服务间通信的机制和最佳实践，包括同步通信、异步通信和通信安全性等方面。

## 1. 通信机制概述

### 1.1 通信方式分类
- **同步通信**: 请求-响应模式，客户端等待服务端响应
- **异步通信**: 消息发布-订阅模式，客户端不等待直接响应
- **流式通信**: 实时数据流传输，适用于大数据量场景

### 1.2 通信协议选择
- **HTTP/REST**: 适用于简单的请求-响应场景
- **gRPC**: 适用于高性能、强类型的服务间调用
- **消息队列**: 适用于解耦和异步处理场景
- **WebSocket**: 适用于实时双向通信场景

## 2. 同步通信机制

### 2.1 RESTful API调用
使用Spring Cloud OpenFeign实现声明式REST客户端：

```java
@FeignClient(name = "product-service", configuration = FeignConfig.class)
public interface ProductServiceClient {
    
    @GetMapping("/api/products/{id}")
    ProductDTO getProductById(@PathVariable("id") Long id);
    
    @PostMapping("/api/products")
    ProductDTO createProduct(@RequestBody ProductCreateRequest request);
    
    @PutMapping("/api/products/{id}")
    ProductDTO updateProduct(@PathVariable("id") Long id, @RequestBody ProductUpdateRequest request);
    
    @DeleteMapping("/api/products/{id}")
    void deleteProduct(@PathVariable("id") Long id);
}
```

### 2.2 负载均衡
使用Spring Cloud LoadBalancer实现客户端负载均衡：

```yaml
# application.yml
product-service:
  ribbon:
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RoundRobinRule
```

### 2.3 熔断器模式
使用Resilience4j实现服务熔断和降级：

```java
@Component
public class ProductServiceClientFallback implements ProductServiceClient {
    
    @Override
    public ProductDTO getProductById(Long id) {
        // 返回默认产品信息或抛出自定义异常
        return ProductDTO.builder()
                .id(id)
                .name("默认商品")
                .status("UNAVAILABLE")
                .build();
    }
    
    // 其他方法的降级实现...
}

@FeignClient(name = "product-service", fallback = ProductServiceClientFallback.class)
public interface ProductServiceClient {
    // 接口定义...
}
```

### 2.4 超时和重试配置
```yaml
# application.yml
feign:
  client:
    config:
      product-service:
        connectTimeout: 5000
        readTimeout: 10000
  retryer:
    max-attempts: 3

resilience4j:
  retry:
    instances:
      product-service:
        max-attempts: 3
        wait-duration: 1000
        enable-exponential-backoff: true
```

## 3. 异步通信机制

### 3.1 消息队列选择
- **RabbitMQ**: 适用于复杂路由和可靠消息传递
- **Apache Kafka**: 适用于高吞吐量和流处理场景

### 3.2 RabbitMQ集成
```java
@Configuration
@EnableRabbit
public class RabbitMQConfig {
    
    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable("order.created.queue").build();
    }
    
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange("order.exchange");
    }
    
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue())
                .to(orderExchange())
                .with("order.created.#");
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }
}

// 消息发布
@Service
public class OrderEventPublisher {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend("order.exchange", "order.created", event);
    }
}

// 消息消费
@Component
public class InventoryEventListener {
    
    @RabbitListener(queues = "order.created.queue")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // 处理库存扣减逻辑
        try {
            inventoryService.deductInventory(event.getOrderItems());
        } catch (InsufficientInventoryException e) {
            // 发布库存不足事件
            eventPublisher.publishInventoryInsufficientEvent(
                InventoryInsufficientEvent.builder()
                    .orderId(event.getOrderId())
                    .productId(e.getProductId())
                    .requestedQuantity(e.getRequestedQuantity())
                    .availableQuantity(e.getAvailableQuantity())
                    .build()
            );
        }
    }
}
```

### 3.3 Kafka集成
```java
@Configuration
@EnableKafka
public class KafkaConfig {
    
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "inventory-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}

// 消息发布
@Service
public class OrderEventPublisher {
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        kafkaTemplate.send("order-created-topic", event.getOrderId().toString(), event);
    }
}

// 消息消费
@Component
public class InventoryEventListener {
    
    @KafkaListener(topics = "order-created-topic", groupId = "inventory-group")
    public void handleOrderCreated(ConsumerRecord<String, OrderCreatedEvent> record) {
        OrderCreatedEvent event = record.value();
        // 处理库存扣减逻辑
        inventoryService.deductInventory(event.getOrderItems());
    }
}
```

## 4. 通信安全性

### 4.1 服务间认证
使用JWT Token进行服务间认证：

```java
@Configuration
public class FeignConfig {
    
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String token = jwtTokenProvider.generateServiceToken();
            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }
}

@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String secret;
    
    public String generateServiceToken() {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000); // 1小时过期
        
        return Jwts.builder()
                .setSubject("service-account")
                .claim("service", "inventory-system")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
}
```

### 4.2 HTTPS通信
配置HTTPS确保通信安全：

```yaml
# application.yml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: password
    key-store-type: PKCS12
    key-alias: tomcat
```

### 4.3 请求签名
对重要请求进行签名验证：

```java
@Component
public class RequestSignatureInterceptor implements RequestInterceptor {
    
    @Value("${service.signature.secret}")
    private String secret;
    
    @Override
    public void apply(RequestTemplate template) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = generateSignature(template, timestamp);
        
        template.header("X-Timestamp", timestamp);
        template.header("X-Signature", signature);
    }
    
    private String generateSignature(RequestTemplate template, String timestamp) {
        String data = template.method() + template.url() + timestamp;
        return HmacUtils.hmacSha256Hex(secret, data);
    }
}
```

## 5. 通信监控和追踪

### 5.1 分布式链路追踪
集成Spring Cloud Sleuth和Zipkin：

```yaml
# application.yml
spring:
  sleuth:
    web:
      client:
        enabled: true
    sampler:
      probability: 1.0
  zipkin:
    base-url: http://localhost:9411
```

### 5.2 指标收集
使用Micrometer收集通信指标：

```java
@Component
public class CommunicationMetrics {
    
    private final Counter successfulCalls;
    private final Counter failedCalls;
    private final Timer responseTime;
    
    public CommunicationMetrics(MeterRegistry meterRegistry) {
        successfulCalls = Counter.builder("service.communication.calls")
                .tag("status", "success")
                .register(meterRegistry);
                
        failedCalls = Counter.builder("service.communication.calls")
                .tag("status", "failure")
                .register(meterRegistry);
                
        responseTime = Timer.builder("service.communication.response.time")
                .register(meterRegistry);
    }
    
    public void recordSuccessfulCall() {
        successfulCalls.increment();
    }
    
    public void recordFailedCall() {
        failedCalls.increment();
    }
    
    public Timer.Sample startTimer() {
        return Timer.start();
    }
    
    public void recordResponseTime(Timer.Sample sample) {
        sample.stop(responseTime);
    }
}
```

## 6. 通信优化策略

### 6.1 连接池配置
优化HTTP客户端连接池：

```yaml
# application.yml
feign:
  httpclient:
    enabled: true
    max-connections: 200
    max-connections-per-route: 50
```

### 6.2 缓存策略
对频繁调用的结果进行缓存：

```java
@Service
public class ProductServiceClientWrapper {
    
    @Autowired
    private ProductServiceClient productServiceClient;
    
    @Cacheable(value = "products", key = "#id")
    public ProductDTO getProductById(Long id) {
        return productServiceClient.getProductById(id);
    }
    
    @CacheEvict(value = "products", key = "#id")
    public void evictProductCache(Long id) {
        // 缓存会在下次调用getProductById时自动刷新
    }
}
```

### 6.3 批量处理
合并多个小请求为批量请求：

```java
@FeignClient(name = "product-service")
public interface ProductServiceClient {
    
    @PostMapping("/api/products/batch")
    List<ProductDTO> getProductsByIds(@RequestBody List<Long> ids);
}

@Service
public class BatchProductService {
    
    @Autowired
    private ProductServiceClient productServiceClient;
    
    public Map<Long, ProductDTO> getProductsMap(List<Long> ids) {
        List<ProductDTO> products = productServiceClient.getProductsByIds(ids);
        return products.stream()
                .collect(Collectors.toMap(ProductDTO::getId, Function.identity()));
    }
}
```

## 7. 故障处理和恢复

### 7.1 降级策略
实现服务降级处理：

```java
@Component
public class ProductServiceFallback implements ProductServiceClient {
    
    @Override
    public ProductDTO getProductById(Long id) {
        // 返回缓存数据或默认值
        return ProductDTO.builder()
                .id(id)
                .name("商品服务暂时不可用")
                .status("TEMP_UNAVAILABLE")
                .build();
    }
    
    @Override
    public List<ProductDTO> getProductsByIds(List<Long> ids) {
        return ids.stream()
                .map(id -> ProductDTO.builder()
                        .id(id)
                        .name("商品服务暂时不可用")
                        .status("TEMP_UNAVAILABLE")
                        .build())
                .collect(Collectors.toList());
    }
}
```

### 7.2 限流策略
使用Resilience4j实现限流：

```yaml
# application.yml
resilience4j:
  ratelimiter:
    instances:
      product-service:
        limit-for-period: 10
        limit-refresh-period: 1s
        timeout-duration: 0
        register-health-indicator: true
        event-consumer-buffer-size: 100
```

```java
@Service
public class ProductServiceClientWrapper {
    
    @Autowired
    private ProductServiceClient productServiceClient;
    
    @RateLimiter(name = "product-service")
    public ProductDTO getProductById(Long id) {
        return productServiceClient.getProductById(id);
    }
}
```

本指南为进销存管理系统的服务间通信提供了全面的规范和实现指导，所有开发团队应严格遵循这些规范进行服务通信开发。