# 错误处理规范指南

本文档定义了进销存管理系统的错误处理规范，所有开发人员都应遵循这些规范以确保系统的一致性和可靠性。

## 1. 异常处理原则

### 1.1 统一异常处理
- 使用@ControllerAdvice或@RestControllerAdvice实现全局异常处理
- 避免在业务代码中直接处理HTTP响应
- 异常处理应与业务逻辑分离

### 1.2 异常分类
- **业务异常**: 由业务规则引发的异常，如库存不足、价格错误等
- **系统异常**: 由系统故障引发的异常，如数据库连接失败、网络超时等
- **参数异常**: 由客户端输入参数错误引发的异常，如字段验证失败等

### 1.3 异常日志记录
- 所有异常都应记录日志，包括异常堆栈信息
- 业务异常记录为WARN级别
- 系统异常记录为ERROR级别
- 敏感信息不应在日志中明文记录

## 2. 自定义异常设计

### 2.1 异常类层次结构
```
Exception
├── RuntimeException
│   ├── BusinessException
│   │   ├── InventoryException
│   │   ├── OrderException
│   │   ├── ProductException
│   │   └── UserException
│   └── SystemException
│       ├── DatabaseException
│       ├── NetworkException
│       └── ExternalServiceException
└── Checked Exception
    └── ImportExportException
```

### 2.2 BusinessException（业务异常）
```java
public class BusinessException extends RuntimeException {
    private final String errorCode;
    private final Object[] args;
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = new Object[0];
    }
    
    public BusinessException(String errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args != null ? args.clone() : new Object[0];
    }
    
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = new Object[0];
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public Object[] getArgs() {
        return args != null ? args.clone() : new Object[0];
    }
}
```

### 2.3 具体业务异常示例
```java
public class InsufficientInventoryException extends BusinessException {
    private final String productId;
    private final Long requestedQuantity;
    private final Long availableQuantity;
    
    public InsufficientInventoryException(String productId, Long requestedQuantity, Long availableQuantity) {
        super("INSUFFICIENT_INVENTORY", 
              String.format("商品%s库存不足，请求数量：%d，可用数量：%d", productId, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    // Getters...
}
```

## 3. 全局异常处理器

### 3.1 异常处理器结构
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage(), e);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code(e.getErrorCode())
                        .message(e.getMessage())
                        .timestamp(Instant.now())
                        .build())
                .build();
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
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
}
```

### 3.2 错误响应对象
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private boolean success;
    private ErrorDetail error;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail {
    private String code;
    private String message;
    private Instant timestamp;
    private List<FieldErrorDetail> details;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldErrorDetail {
    private String field;
    private String message;
    private Object rejectedValue;
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

## 5. 参数验证规范

### 5.1 验证注解使用
- 使用Bean Validation(JSR-380)注解进行参数验证
- 在DTO类中定义验证规则
- 使用分组验证处理不同场景的验证需求

### 5.2 验证示例
```java
@Data
public class ProductCreateRequest {
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称长度不能超过100个字符")
    private String name;
    
    @NotBlank(message = "商品编码不能为空")
    @Size(max = 50, message = "商品编码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "商品编码只能包含字母、数字、下划线和横线")
    private String productCode;
    
    @NotNull(message = "采购价格不能为空")
    @DecimalMin(value = "0.00", message = "采购价格必须大于等于0")
    private BigDecimal purchasePrice;
    
    @NotNull(message = "销售价格不能为空")
    @DecimalMin(value = "0.00", message = "销售价格必须大于等于0")
    private BigDecimal sellingPrice;
    
    @Min(value = 0, message = "保质期天数必须大于等于0")
    private Integer shelfLifeDays;
}
```

### 5.3 分组验证
```java
public interface CreateValidationGroup {}
public interface UpdateValidationGroup {}

@Data
public class ProductRequest {
    @Null(groups = CreateValidationGroup.class, message = "创建时ID必须为空")
    @NotNull(groups = UpdateValidationGroup.class, message = "更新时ID不能为空")
    private Long id;
    
    @NotBlank(groups = {CreateValidationGroup.class, UpdateValidationGroup.class}, 
              message = "商品名称不能为空")
    private String name;
}

// Controller中使用
@PostMapping
public ResponseEntity<ProductResponse> createProduct(
        @Validated(CreateValidationGroup.class) @RequestBody ProductRequest request) {
    // ...
}
```

## 6. 日志记录规范

### 6.1 日志级别使用
- **ERROR**: 系统异常、数据不一致等严重问题
- **WARN**: 业务异常、可预期的错误情况
- **INFO**: 重要业务流程、系统启动等信息
- **DEBUG**: 详细调试信息，仅在开发和测试环境启用

### 6.2 异常日志记录示例
```java
@Service
@Slf4j
public class ProductService {
    
    public Product findById(Long id) {
        try {
            return productRepository.findById(id)
                    .orElseThrow(() -> new ProductNotFoundException(id));
        } catch (DataAccessException e) {
            log.error("查询商品失败，ID: {}", id, e);
            throw new DatabaseException("查询商品失败", e);
        }
    }
    
    public Product create(ProductCreateRequest request) {
        try {
            // 检查商品编码是否重复
            if (productRepository.existsByProductCode(request.getProductCode())) {
                log.warn("商品编码已存在: {}", request.getProductCode());
                throw new DuplicateProductCodeException(request.getProductCode());
            }
            
            Product product = new Product();
            // 设置属性...
            return productRepository.save(product);
        } catch (DataAccessException e) {
            log.error("创建商品失败: {}", request.getProductCode(), e);
            throw new DatabaseException("创建商品失败", e);
        }
    }
}
```

## 7. 客户端错误处理

### 7.1 前端错误处理
- 统一封装API调用，处理通用错误
- 根据错误代码显示用户友好的错误信息
- 对于业务异常，应提供明确的操作建议

### 7.2 错误信息国际化
- 错误信息应支持多语言
- 根据客户端语言环境返回相应语言的错误信息
- 错误代码保持不变，便于系统间识别

## 8. 监控和告警

### 8.1 异常监控
- 统计各类异常的发生频率
- 设置异常阈值告警
- 定期分析异常趋势

### 8.2 错误率监控
- 监控API错误率
- 区分不同类型的错误率
- 设置错误率告警阈值

### 8.3 异常统计指标
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

本规范自发布之日起生效，所有新开发的代码必须遵循此规范。现有代码应逐步迁移到新规范。