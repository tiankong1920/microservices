# inventory-system Java代码规范规则

## 1. 命名约定

### 1.1 类名
- 使用UpperCamelCase风格
- 类名应清晰表达类的职责
- 抽象类以Abstract或Base开头
- 异常类以Exception结尾
- 测试类以Test结尾

```java
// 正确示例
public class UserServiceImpl implements UserService {
    // ...
}

public class OrderProcessingException extends RuntimeException {
    // ...
}

public class ProductControllerTest {
    // ...
}

// 错误示例
public class user_service_impl {  // 不符合命名规范
    // ...
}
```

### 1.2 方法名和变量名
- 使用lowerCamelCase风格
- 方法名应表达其功能，动词开头
- 布尔类型方法使用is或has前缀
- 集合类型变量使用复数形式

```java
// 正确示例
private List<Product> products;
private boolean isActive;

public User findUserById(Long id) {
    // ...
}

public boolean isProductAvailable(String productId) {
    // ...
}

// 错误示例
private List<Product> Products;  // 首字母不应大写
private boolean active_;

public User Find_User_By_Id(Long id) {  // 不符合命名规范
    // ...
}
```

### 1.3 常量名
- 全部大写，单词间用下划线分隔
- 常量应使用static final修饰
- 常量名应能清晰表达其含义

```java
// 正确示例
public static final int MAX_RETRY_COUNT = 3;
public static final String DEFAULT_ENCODING = "UTF-8";

// 错误示例
public static final int maxRetryCount = 3;  // 不应使用驼峰命名
public static final String defaultEncoding = "UTF-8";  // 不应使用驼峰命名
```

### 1.4 包名
- 全部小写，使用有意义的英文单词
- 包名应反映模块功能和分层结构
- 使用反向域名作为根包名

```java
// 正确示例
package com.inventory.service;
package com.inventory.controller;
package com.inventory.repository;

// 错误示例
package com.inventory.Service;  // 不应包含大写字母
package com.inventory.controller_;  // 不应包含特殊字符
```

## 2. 代码格式

### 2.1 缩进
- 使用4个空格进行缩进（而非制表符）
- IDE应配置为将制表符转换为4个空格

```java
// 正确示例
public class UserService {
    public void createUser(User user) {
        if (user != null) {
            // 4个空格缩进
            userRepository.save(user);
        }
    }
}

// 错误示例（使用制表符缩进）
public class UserService {
	public void createUser(User user) {  // 使用制表符而不是空格
		if (user != null) {
			userRepository.save(user);
		}
	}
}
```

### 2.2 大括号
- 采用K&R风格（左大括号放在行尾，右大括号单独一行）
- 即使单行语句也应使用大括号

```java
// 正确示例
public void processOrder(Order order) {
    if (order != null) {
        validateOrder(order);
        saveOrder(order);
    } else {
        throw new IllegalArgumentException("Order cannot be null");
    }
}

// 错误示例
public void processOrder(Order order)
{
    if (order != null)
        validateOrder(order);
}

// 另一种错误示例
public void processOrder(Order order) {
    if (order != null) validateOrder(order);  // 单行语句未使用大括号
}
```

### 2.3 空格
- 操作符两侧添加空格
- 关键字后添加空格
- 逗号后添加空格

```java
// 正确示例
int result = a + b;
if (condition) {
    // ...
}
List<String> items = Arrays.asList("a", "b", "c");

// 错误示例
int result=a+b;  // 操作符两侧缺少空格
if(condition){   // 关键字后缺少空格
    // ...
}
List<String> items = Arrays.asList("a","b","c");  // 逗号后缺少空格
```

### 2.4 换行
- 每行不超过120个字符
- 当表达式过长时应合理换行

```java
// 正确示例
String longString = "This is a very long string that exceeds the 120 character limit "
    + "and needs to be broken into multiple lines for better readability.";

// 错误示例（超过120字符）
String longString = "This is a very long string that exceeds the 120 character limit and needs to be broken into multiple lines for better readability and should have been split earlier.";

```

### 2.5 导入语句
- 按字母顺序排序
- 不得使用通配符导入
- 移除未使用的导入

```java
// 正确示例
import com.inventory.model.Product;
import com.inventory.model.User;
import com.inventory.repository.ProductRepository;
import com.inventory.service.UserService;
import java.util.List;
import java.util.Optional;

// 错误示例
import com.inventory.model.*;  // 使用了通配符导入
import java.util.*;           // 使用了通配符导入
import com.inventory.service.UserService;
import com.inventory.model.User;
import com.inventory.model.Product;  // 未按字母顺序排序
// import java.time.LocalDateTime;  // 未使用的导入
```

## 3. 注释要求

### 3.1 文件头注释
- 包含版权信息、作者、创建日期和文件描述
- 使用统一的注释模板

```java
/**
 * Copyright (c) 2025 Inventory System. All rights reserved.
 *
 * @author Zhang Wei
 * @since 2025-11-12
 * @version 1.0
 *
 * Product service implementation for managing product related operations.
 */
public class ProductServiceImpl implements ProductService {
    // ...
}
```

### 3.2 JavaDoc
- 所有公共类、接口、方法必须有完整的JavaDoc注释
- 包含参数说明、返回值说明和可能抛出的异常
- 注释应简洁明了，准确描述功能

```java
/**
 * Finds a user by their unique identifier.
 *
 * @param userId the unique identifier of the user to find, must not be null
 * @return the user with the specified ID, or null if no user found
 * @throws IllegalArgumentException if userId is null
 * @throws DataAccessException if there is a database access error
 */
public User findUserById(Long userId) {
    // ...
}

/**
 * Product service interface for managing product related operations.
 */
public interface ProductService {
    // ...
}
```

### 3.3 行内注释
- 用于解释复杂逻辑或重要决策点
- 避免冗余注释
- 注释应与代码保持同步

```java
// 正确示例
public void processOrder(Order order) {
    // Validate inventory before processing order
    if (!inventoryService.checkAvailability(order.getItems())) {
        throw new InsufficientInventoryException("Not enough items in stock");
    }
    
    // Apply discount based on customer tier
    BigDecimal discount = customerService.getDiscountRate(order.getCustomerId());
    order.setTotalAmount(order.calculateTotal().multiply(BigDecimal.ONE.subtract(discount)));
}

// 错误示例
public void processOrder(Order order) {
    if (!inventoryService.checkAvailability(order.getItems())) {  // 检查库存是否充足
        throw new InsufficientInventoryException("Not enough items in stock");
    }
    
    BigDecimal discount = customerService.getDiscountRate(order.getCustomerId());  // 获取折扣率
    order.setTotalAmount(order.calculateTotal().multiply(BigDecimal.ONE.subtract(discount)));  // 设置总金额
}
```

## 4. 异常处理规范

### 4.1 空catch块
- 不得捕获异常后不做任何处理（空catch块）
- 如确实需要忽略异常，应添加注释说明原因

```java
// 正确示例
try {
    fileService.readFile("config.properties");
} catch (FileNotFoundException e) {
    // 使用默认配置，因为配置文件是可选的
    logger.info("Configuration file not found, using default settings");
}

// 错误示例
try {
    fileService.readFile("config.properties");
} catch (FileNotFoundException e) {
    // 空的catch块，忽略异常
}
```

### 4.2 具体异常类型捕获
- 不得使用通用的Exception类型捕获，应捕获具体异常类型
- 按照从具体到一般的顺序捕获异常

```java
// 正确示例
public User findUserById(Long userId) {
    try {
        return userRepository.findById(userId);
    } catch (DataAccessException e) {
        logger.error("Database error while finding user with id: " + userId, e);
        throw new UserServiceException("Failed to retrieve user", e);
    } catch (IllegalArgumentException e) {
        logger.warn("Invalid user id provided: " + userId, e);
        throw e;
    }
}

// 错误示例
public User findUserById(Long userId) {
    try {
        return userRepository.findById(userId);
    } catch (Exception e) {  // 捕获了通用异常
        logger.error("Error occurred", e);
        throw new RuntimeException(e);
    }
}
```

### 4.3 自定义异常
- 自定义异常应继承RuntimeException或Checked Exception
- 提供有意义的错误信息
- 包含错误码以便于分类处理

```java
// 正确示例
public class InsufficientInventoryException extends BusinessException {
    public InsufficientInventoryException(String message) {
        super("INSUFFICIENT_INVENTORY", message);
    }
}

public class UserServiceException extends RuntimeException {
    private final String errorCode;
    
    public UserServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public UserServiceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

// 错误示例
public class InsufficientInventoryException extends Exception {
    public InsufficientInventoryException() {  // 缺少有意义的错误信息
        super();
    }
}
```

### 4.4 日志记录
- 日志记录应在适当的层级进行
- 避免重复记录同一异常
- 使用合适的日志级别

```java
// 正确示例
@Service
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    public void processOrder(Order order) {
        try {
            validateOrder(order);
            paymentService.processPayment(order);
            inventoryService.updateInventory(order.getItems());
            orderRepository.save(order);
            logger.info("Order {} processed successfully", order.getId());
        } catch (PaymentProcessingException e) {
            logger.error("Payment failed for order {}", order.getId(), e);
            // 不在controller中重复记录日志
            throw new OrderProcessingException("Payment failed", e);
        } catch (InsufficientInventoryException e) {
            logger.warn("Insufficient inventory for order {}", order.getId());
            throw e;
        }
    }
}

// 错误示例
@Controller
public class OrderController {
    
    @PostMapping("/orders")
    public ResponseEntity<String> createOrder(@RequestBody Order order) {
        try {
            orderService.processOrder(order);
            return ResponseEntity.ok("Order created successfully");
        } catch (OrderProcessingException e) {
            // 不必要的重复日志记录，Service层已经记录过了
            logger.error("Order processing failed", e);
            return ResponseEntity.badRequest().body("Order processing failed");
        }
    }
}
```

## 5. 单元测试标准

### 5.1 测试覆盖率
- 核心业务逻辑应达到80%以上分支覆盖率
- 使用工具如JaCoCo监控测试覆盖率
- 对边界条件和异常情况编写测试

```java
// 正确示例 - 测试不同的分支路径
@Test
public void testCalculateDiscount_shouldReturnTenPercent_whenCustomerIsGold() {
    // Given
    Customer customer = new Customer(CustomerType.GOLD);
    Order order = new Order(customer, BigDecimal.valueOf(100));
    
    // When
    BigDecimal discount = discountService.calculateDiscount(order);
    
    // Then
    assertThat(discount).isEqualByComparingTo(BigDecimal.valueOf(10));  // 10% discount
}

@Test
public void testCalculateDiscount_shouldReturnZero_whenCustomerIsRegular() {
    // Given
    Customer customer = new Customer(CustomerType.REGULAR);
    Order order = new Order(customer, BigDecimal.valueOf(100));
    
    // When
    BigDecimal discount = discountService.calculateDiscount(order);
    
    // Then
    assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
}

@Test
public void testCalculateDiscount_shouldReturnFifteenPercent_whenOrderAmountExceedsThreshold() {
    // Given
    Customer customer = new Customer(CustomerType.SILVER);
    Order order = new Order(customer, BigDecimal.valueOf(1000));  // 大于阈值的订单
    
    // When
    BigDecimal discount = discountService.calculateDiscount(order);
    
    // Then
    assertThat(discount).isEqualByComparingTo(BigDecimal.valueOf(15));  // 15% discount
}
```

### 5.2 命名规范
- 测试方法使用下划线分隔的描述性命名
- 命名应清晰表达测试条件和期望结果

```java
// 正确示例
@Test
public void test_findUserById_shouldReturnUser_whenValidIdProvided() {
    // ...
}

@Test
public void test_findUserById_shouldThrowException_whenNullIdProvided() {
    // ...
}

@Test
public void test_calculateTotal_shouldReturnZero_whenOrderHasNoItems() {
    // ...
}

// 错误示例
@Test
public void test1() {  // 命名不明确
    // ...
}

@Test
public void findUser() {  // 缺少条件和期望结果描述
    // ...
}
```

### 5.3 断言使用
- 优先使用AssertJ或Hamcrest提供的断言库
- 提高断言可读性
- 使用流式的断言风格

```java
// 正确示例 - 使用AssertJ
@Test
public void test_getProductById_shouldReturnCorrectProduct() {
    // Given
    Long productId = 1L;
    Product expectedProduct = new Product(productId, "Test Product", BigDecimal.valueOf(10.0));
    when(productRepository.findById(productId)).thenReturn(Optional.of(expectedProduct));
    
    // When
    Product actualProduct = productService.getProductById(productId);
    
    // Then
    assertThat(actualProduct)
        .isNotNull()
        .extracting(Product::getId, Product::getName, Product::getPrice)
        .containsExactly(productId, "Test Product", BigDecimal.valueOf(10.0));
}

// 错误示例 - 使用基本断言
@Test
public void test_getProductById_shouldReturnCorrectProduct() {
    // Given
    Long productId = 1L;
    Product expectedProduct = new Product(productId, "Test Product", BigDecimal.valueOf(10.0));
    when(productRepository.findById(productId)).thenReturn(Optional.of(expectedProduct));
    
    // When
    Product actualProduct = productService.getProductById(productId);
    
    // Then
    assertNotNull(actualProduct);
    assertEquals(productId, actualProduct.getId());
    assertEquals("Test Product", actualProduct.getName());
    assertEquals(BigDecimal.valueOf(10.0), actualProduct.getPrice());
}
```

### 5.4 测试独立性
- 每个测试方法应独立运行
- 不依赖其他测试的执行结果
- 使用@BeforeEach和@AfterEach进行测试环境准备和清理

```java
// 正确示例
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private PaymentService paymentService;
    
    @InjectMocks
    private OrderService orderService;
    
    private Order testOrder;
    
    @BeforeEach
    void setUp() {
        testOrder = new Order(1L, new ArrayList<>(), BigDecimal.valueOf(100));
    }
    
    @Test
    void test_processOrder_shouldSaveOrder_whenPaymentSuccessful() {
        // Given
        when(paymentService.processPayment(testOrder)).thenReturn(true);
        
        // When
        orderService.processOrder(testOrder);
        
        // Then
        verify(orderRepository).save(testOrder);
    }
    
    @Test
    void test_processOrder_shouldNotSaveOrder_whenPaymentFails() {
        // Given
        when(paymentService.processPayment(testOrder)).thenReturn(false);
        
        // When/Then
        assertThatThrownBy(() -> orderService.processOrder(testOrder))
            .isInstanceOf(OrderProcessingException.class);
        verify(orderRepository, never()).save(any());
    }
}

// 错误示例 - 测试之间存在依赖
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @InjectMocks
    private OrderService orderService;
    
    private Order testOrder;
    
    @Test
    void test_createOrder() {  // 第一个测试创建订单
        testOrder = new Order(1L, new ArrayList<>(), BigDecimal.valueOf(100));
        when(orderRepository.save(any())).thenReturn(testOrder);
        
        Order result = orderService.createOrder(new ArrayList<>());
        assertThat(result).isNotNull();
        // 错误：依赖这个测试来设置testOrder变量
    }
    
    @Test
    void test_cancelOrder() {  // 第二个测试依赖第一个测试的结果
        // 错误：依赖testOrder变量，如果第一个测试未运行则为null
        orderService.cancelOrder(testOrder.getId());
        verify(orderRepository).delete(testOrder);
    }
}
```

这套Java代码规范规则与项目中已集成的Checkstyle、PMD、SpotBugs等代码质量工具保持一致，有助于提高代码质量和团队协作效率。开发团队应严格遵守这些规范，并通过自动化工具进行检查和验证。