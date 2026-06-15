# 异常处理机制完善指南

本文档详细说明了进销存管理系统异常处理机制的完善方案，包括全局异常处理、自定义异常设计、异常日志记录和异常监控等方面的实现规范。

## 1. 异常处理架构

### 1.1 异常处理层次结构
```
Exception
├── RuntimeException
│   ├── BusinessException
│   │   ├── ProductException
│   │   ├── InventoryException
│   │   ├── OrderException
│   │   └── UserException
│   ├── SystemException
│   │   ├── DatabaseException
│   │   ├── NetworkException
│   │   └── ExternalServiceException
│   └── ValidationException
└── Checked Exception
    └── ImportExportException
```

### 1.2 异常处理组件
- **全局异常处理器**: 统一处理所有未捕获的异常
- **业务异常**: 由业务规则引发的可预期异常
- **系统异常**: 由系统故障引发的不可预期异常
- **参数验证异常**: 由输入参数验证失败引发的异常

## 2. 自定义异常设计

### 2.1 基础异常类
```java
public abstract class BaseException extends RuntimeException {
    private final String errorCode;
    private final Object[] args;
    private final Instant timestamp;
    
    protected BaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = new Object[0];
        this.timestamp = Instant.now();
    }
    
    protected BaseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = new Object[0];
        this.timestamp = Instant.now();
    }
    
    protected BaseException(String errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args != null ? args.clone() : new Object[0];
        this.timestamp = Instant.now();
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public Object[] getArgs() {
        return args != null ? args.clone() : new Object[0];
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public String getLocalizedMessage(Locale locale) {
        // 实现国际化消息处理
        return getMessage();
    }
}
```

### 2.2 业务异常类
```java
public class BusinessException extends BaseException {
    public BusinessException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    
    public BusinessException(String errorCode, String message, Object... args) {
        super(errorCode, message, args);
    }
}

// 商品相关异常
public class ProductException extends BusinessException {
    public ProductException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public ProductException(String errorCode, String message, Object... args) {
        super(errorCode, message, args);
    }
}

public class ProductNotFoundException extends ProductException {
    private final Long productId;
    
    public ProductNotFoundException(Long productId) {
        super("PRODUCT_NOT_FOUND", 
              String.format("商品不存在，ID: %d", productId));
        this.productId = productId;
    }
    
    public Long getProductId() {
        return productId;
    }
}

public class DuplicateProductCodeException extends ProductException {
    private final String productCode;
    
    public DuplicateProductCodeException(String productCode) {
        super("DUPLICATE_PRODUCT_CODE", 
              String.format("商品编码已存在: %s", productCode));
        this.productCode = productCode;
    }
    
    public String getProductCode() {
        return productCode;
    }
}

// 库存相关异常
public class InventoryException extends BusinessException {
    public InventoryException(String errorCode, String message) {
        super(errorCode, message);
    }
}

public class InsufficientInventoryException extends InventoryException {
    private final Long productId;
    private final Long requestedQuantity;
    private final Long availableQuantity;
    
    public InsufficientInventoryException(Long productId, Long requestedQuantity, Long availableQuantity) {
        super("INSUFFICIENT_INVENTORY", 
              String.format("库存不足，商品ID: %d，请求数量: %d，可用数量: %d", 
                           productId, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    // Getters...
}

// 订单相关异常
public class OrderException extends BusinessException {
    public OrderException(String errorCode, String message) {
        super(errorCode, message);
    }
}

public class OrderStatusException extends OrderException {
    private final String currentStatus;
    private final String expectedStatus;
    
    public OrderStatusException(String currentStatus, String expectedStatus) {
        super("ORDER_STATUS_INVALID", 
              String.format("订单状态不合法，当前状态: %s，期望状态: %s", 
                           currentStatus, expectedStatus));
        this.currentStatus = currentStatus;
        this.expectedStatus = expectedStatus;
    }
    
    // Getters...
}
```

### 2.3 系统异常类
```java
public class SystemException extends BaseException {
    public SystemException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public SystemException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}

public class DatabaseException extends SystemException {
    public DatabaseException(String message) {
        super("DATABASE_ERROR", message);
    }
    
    public DatabaseException(String message, Throwable cause) {
        super("DATABASE_ERROR", message, cause);
    }
}

public class NetworkException extends SystemException {
    public NetworkException(String message) {
        super("NETWORK_ERROR", message);
    }
    
    public NetworkException(String message, Throwable cause) {
        super("NETWORK_ERROR", message, cause);
    }
}

public class ExternalServiceException extends SystemException {
    private final String serviceName;
    
    public ExternalServiceException(String serviceName, String message) {
        super("EXTERNAL_SERVICE_ERROR", message);
        this.serviceName = serviceName;
    }
    
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super("EXTERNAL_SERVICE_ERROR", message, cause);
        this.serviceName = serviceName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
}
```

## 3. 全局异常处理器

### 3.1 异常处理器实现
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @Autowired
    private MessageSource messageSource;
    
    // 业务异常处理
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage(), e);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code(e.getErrorCode())
                        .message(e.getMessage())
                        .timestamp(e.getTimestamp())
                        .build())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    // 参数验证异常处理
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation exception: {}", e.getMessage());
        
        List<FieldErrorDetail> details = e.getBindingResult().getFieldErrors().stream()
                .map(error -> FieldErrorDetail.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("VALIDATION_ERROR")
                        .message("请求参数验证失败")
                        .timestamp(Instant.now())
                        .details(details)
                        .build())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    // 绑定异常处理
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        log.warn("Bind exception: {}", e.getMessage());
        
        List<FieldErrorDetail> details = e.getBindingResult().getFieldErrors().stream()
                .map(error -> FieldErrorDetail.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("BIND_ERROR")
                        .message("请求参数绑定失败")
                        .timestamp(Instant.now())
                        .details(details)
                        .build())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    // 约束违反异常处理
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("Constraint violation exception: {}", e.getMessage());
        
        List<FieldErrorDetail> details = e.getConstraintViolations().stream()
                .map(violation -> FieldErrorDetail.builder()
                        .field(getFieldNameFromPath(violation.getPropertyPath()))
                        .message(violation.getMessage())
                        .rejectedValue(violation.getInvalidValue())
                        .build())
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("CONSTRAINT_VIOLATION")
                        .message("约束条件违反")
                        .timestamp(Instant.now())
                        .details(details)
                        .build())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    // HTTP消息不可读异常处理
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HTTP message not readable: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("INVALID_REQUEST_BODY")
                        .message("请求体格式不正确")
                        .timestamp(Instant.now())
                        .build())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    // 系统异常处理
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleSystemException(Exception e) {
        log.error("System exception: {}", e.getMessage(), e);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("INTERNAL_ERROR")
                        .message("系统内部错误")
                        .timestamp(Instant.now())
                        .build())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    // 错误响应对象
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private boolean success;
        private ErrorDetail error;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetail {
        private String code;
        private String message;
        private Instant timestamp;
        private List<FieldErrorDetail> details;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldErrorDetail {
        private String field;
        private String message;
        private Object rejectedValue;
    }
    
    private String getFieldNameFromPath(Path path) {
        String pathStr = path.toString();
        int lastDotIndex = pathStr.lastIndexOf('.');
        return lastDotIndex != -1 ? pathStr.substring(lastDotIndex + 1) : pathStr;
    }
}
```

### 3.2 异常处理配置
```java
@Configuration
public class ExceptionHandlingConfig {
    
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600); // 缓存1小时
        return messageSource;
    }
    
    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource());
        return validator;
    }
}
```

## 4. 错误代码规范

### 4.1 错误代码命名
- 使用大写字母和下划线分隔
- 格式：`模块_错误类型`
- 错误代码应具有唯一性和描述性

### 4.2 常见错误代码
| 模块 | 错误代码 | 描述 |
|-----|---------|------|
| 通用 | INTERNAL_ERROR | 系统内部错误 |
| 通用 | VALIDATION_ERROR | 参数验证失败 |
| 通用 | UNAUTHORIZED | 未认证 |
| 通用 | FORBIDDEN | 权限不足 |
| 通用 | NOT_FOUND | 资源不存在 |
| 商品 | PRODUCT_NOT_FOUND | 商品不存在 |
| 商品 | PRODUCT_CODE_DUPLICATE | 商品编码重复 |
| 库存 | INSUFFICIENT_INVENTORY | 库存不足 |
| 库存 | INVENTORY_NOT_FOUND | 库存记录不存在 |
| 订单 | ORDER_NOT_FOUND | 订单不存在 |
| 订单 | ORDER_STATUS_INVALID | 订单状态不合法 |
| 用户 | USER_NOT_FOUND | 用户不存在 |
| 用户 | USER_ALREADY_EXISTS | 用户已存在 |
| 供应商 | SUPPLIER_NOT_FOUND | 供应商不存在 |
| 系统 | DATABASE_ERROR | 数据库错误 |
| 系统 | NETWORK_ERROR | 网络错误 |
| 系统 | EXTERNAL_SERVICE_ERROR | 外部服务错误 |

## 5. 异常日志记录

### 4.1 结构化日志记录
```java
@Component
@Slf4j
public class ExceptionLoggingService {
    
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY_AUDIT");
    private static final Logger businessLogger = LoggerFactory.getLogger("BUSINESS_AUDIT");
    
    public void logBusinessException(BusinessException e, HttpServletRequest request) {
        businessLogger.warn("Business Exception - Code: {}, Message: {}, User: {}, IP: {}, URL: {}", 
                e.getErrorCode(), 
                e.getMessage(), 
                getCurrentUser(), 
                getClientIP(request), 
                request.getRequestURI());
    }
    
    public void logSystemException(SystemException e, HttpServletRequest request) {
        log.error("System Exception - Code: {}, Message: {}, User: {}, IP: {}, URL: {}, StackTrace: {}", 
                e.getErrorCode(), 
                e.getMessage(), 
                getCurrentUser(), 
                getClientIP(request), 
                request.getRequestURI(), 
                ExceptionUtils.getStackTrace(e));
    }
    
    public void logSecurityException(SecurityException e, HttpServletRequest request) {
        securityLogger.warn("Security Exception - Message: {}, User: {}, IP: {}, URL: {}", 
                e.getMessage(), 
                getCurrentUser(), 
                getClientIP(request), 
                request.getRequestURI());
    }
    
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "ANONYMOUS";
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

### 4.2 异常追踪ID
```java
@Component
public class ExceptionTrackingService {
    
    private final ThreadLocal<String> trackingIdHolder = new ThreadLocal<>();
    
    public String generateTrackingId() {
        String trackingId = UUID.randomUUID().toString();
        trackingIdHolder.set(trackingId);
        return trackingId;
    }
    
    public String getTrackingId() {
        return trackingIdHolder.get();
    }
    
    public void clearTrackingId() {
        trackingIdHolder.remove();
    }
    
    @EventListener
    public void handleRequestReceivedEvent(RequestReceivedEvent event) {
        generateTrackingId();
    }
}

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @Autowired
    private ExceptionTrackingService trackingService;
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        String trackingId = trackingService.getTrackingId();
        
        log.error("Exception occurred [TrackingID: {}]: {}", trackingId, e.getMessage(), e);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("INTERNAL_ERROR")
                        .message("系统内部错误 [追踪ID: " + trackingId + "]")
                        .timestamp(Instant.now())
                        .build())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
```

## 5. 异常监控和告警

### 5.1 异常统计指标
```java
@Component
public class ExceptionMetricsService {
    
    private final MeterRegistry meterRegistry;
    private final Counter businessExceptions;
    private final Counter systemExceptions;
    private final Counter validationExceptions;
    private final Timer exceptionHandlingTimer;
    
    public ExceptionMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.businessExceptions = Counter.builder("exceptions.business")
                .description("Business exceptions count")
                .register(meterRegistry);
        this.systemExceptions = Counter.builder("exceptions.system")
                .description("System exceptions count")
                .register(meterRegistry);
        this.validationExceptions = Counter.builder("exceptions.validation")
                .description("Validation exceptions count")
                .register(meterRegistry);
        this.exceptionHandlingTimer = Timer.builder("exception.handling.time")
                .description("Exception handling duration")
                .register(meterRegistry);
    }
    
    public void recordBusinessException(String errorCode) {
        businessExceptions.increment(Tag.of("error_code", errorCode));
    }
    
    public void recordSystemException(String errorCode) {
        systemExceptions.increment(Tag.of("error_code", errorCode));
    }
    
    public void recordValidationException() {
        validationExceptions.increment();
    }
    
    public Timer.Sample startHandlingTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordHandlingTime(Timer.Sample sample) {
        sample.stop(exceptionHandlingTimer);
    }
}
```

### 5.2 异常告警规则
```yaml
# alert_rules.yml
groups:
  - name: exception-alerts
    rules:
      - alert: HighBusinessExceptionRate
        expr: rate(exceptions_business_total[5m]) > 10
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High business exception rate"
          description: "Business exception rate is above 10 per minute"
          
      - alert: HighSystemExceptionRate
        expr: rate(exceptions_system_total[5m]) > 5
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High system exception rate"
          description: "System exception rate is above 5 per minute"
          
      - alert: SpecificErrorCodeAlert
        expr: exceptions_business_total{error_code="INSUFFICIENT_INVENTORY"} > 100
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "High insufficient inventory exceptions"
          description: "Insufficient inventory exceptions exceeded 100"
```

## 6. 异常处理最佳实践

### 6.1 异常处理原则
1. **快速失败**: 在最早可能的地方检测和抛出异常
2. **异常隔离**: 不同类型的异常应有不同的处理路径
3. **信息丰富**: 异常应包含足够的上下文信息用于诊断
4. **安全处理**: 敏感信息不应在异常信息中泄露

### 6.2 异常链处理
```java
@Service
public class OrderService {
    
    @Autowired
    private InventoryService inventoryService;
    
    public void processOrder(Order order) {
        try {
            // 处理订单逻辑
            inventoryService.deductInventory(order.getItems());
        } catch (InsufficientInventoryException e) {
            // 包装异常并添加业务上下文
            throw new OrderException("ORDER_PROCESSING_FAILED", 
                    "订单处理失败，库存不足", e);
        } catch (DatabaseException e) {
            // 包装系统异常
            throw new OrderException("ORDER_PROCESSING_FAILED", 
                    "订单处理失败，数据库错误", e);
        }
    }
}
```

### 6.3 异常恢复机制
```java
@Service
public class ResilientService {
    
    @Retryable(value = {NetworkException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ProductDTO getProductFromExternalService(Long productId) {
        try {
            // 调用外部服务
            return externalProductService.getProduct(productId);
        } catch (Exception e) {
            throw new ExternalServiceException("external-product-service", 
                    "获取商品信息失败", e);
        }
    }
    
    @Recover
    public ProductDTO recoverFromExternalServiceFailure(ExternalServiceException e, Long productId) {
        log.warn("Failed to get product from external service after retries, using fallback: {}", productId);
        // 使用缓存或默认数据作为降级方案
        return getFallbackProduct(productId);
    }
    
    private ProductDTO getFallbackProduct(Long productId) {
        // 从缓存获取或返回默认商品信息
        return ProductDTO.builder()
                .id(productId)
                .name("商品信息暂时不可用")
                .status("TEMP_UNAVAILABLE")
                .build();
    }
}
```

本指南为进销存管理系统的异常处理机制提供了全面的规范和实现指导，所有开发团队应严格遵循这些规范进行异常处理开发。