# 单元测试指南

## 版本
- 版本：1.0.0
- 创建日期：2025-01-19
- 最后更新：2025-01-19

## 1. 文档概述

### 1.1 目的
本文档提供了库存管理系统单元测试的详细指南，包括测试原则、测试框架使用、Mock对象使用、测试覆盖率要求和测试最佳实践。

### 1.2 适用范围
- 单元测试策略
- 测试框架使用
- Mock对象使用
- 测试覆盖率管理
- 测试命名规范

### 1.3 目标读者
- 测试工程师
- 开发工程师
- 代码审查人员

### 1.4 前置条件
- 已配置测试环境（参考[TestingConfigGuide.md](file:///e:/101/microservices/docs/TestingConfigGuide.md)）
- 已安装测试框架（JUnit 5、Mockito、AssertJ）
- 已配置测试数据库（H2、PostgreSQL）
- 已配置Mock数据

## 2. 单元测试原则

### 2.1 AAA原则

- **A**rrange（排列）: 测试应该可以以任何顺序执行
- **A**ssert（断言）: 测试应该验证预期结果
- **A**rrange（排列）: 测试应该覆盖所有代码路径
- **A**utomated（自动化）: 测试应该可以自动执行

### 2.2 FIRST原则

- **F**ast（快速）: 测试应该快速执行
- **I**ndependent（独立）: 测试之间应该相互独立
- **R**epeatable（可重复）: 测试应该可以重复执行
- **S**elf-descriptive（自描述）: 测试应该清楚地描述测试内容

### 2.3 测试金字塔

```
         /\
        / \
       /  \
      端到端测试
     / \
    / \
     集成测试
   / \
    / \
  单元测试
```

- **单元测试**: 测试单个方法和类
- **集成测试**: 测试多个组件之间的交互
- **端到端测试**: 测试完整的业务流程

## 3. 测试框架使用

### 3.1 JUnit 5配置

#### 3.1.1 Maven依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>5.5.0</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>3.24.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

#### 3.1.2 测试配置

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestMethodOrder(MethodInstance.OrdererAnnotation.class)
public class ProductServiceTest {
    
    @Test
    @DisplayName("创建产品")
    @Order(1)
    public void testCreateProduct() {
        // 测试代码
    }
}
```

### 3.2 Mock对象使用

#### 3.2.1 Mockito基础使用

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class OrderServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private OrderService orderService;
    
    @Test
    public void testCreateOrder() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setProductId(1L);
        
        Order order = orderService.createOrder(orderDTO);
        
        assertNotNull(order);
        assertEquals(1L, order.getProductId());
    }
}
```

#### 3.2.2 MockMvc使用

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class ProductControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testGetProduct() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Product"));
    }
}
```

### 3.3 测试覆盖率要求

#### 3.3.1 覆盖率目标

- **单元测试**: 80%行覆盖率
- **集成测试**: 70%行覆盖率
- **整体目标**: 75%行覆盖率

#### 3.3.2 覆盖率工具配置

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

#### 3.3.3 覆盖率报告

```bash
# 生成覆盖率报告
mvn clean test jacoco:report

# 查看覆盖率
mvn jacoco:check

# 查看覆盖率详情
open target/site/jacoco/index.html
```

## 4. 测试命名规范

### 4.1 类命名规范

```
[ServiceName]Test
```

示例：
- `ProductServiceTest`
- `OrderServiceTest`
- `InventoryServiceTest`

### 4.2 方法命名规范

```
test[Should/When][ExpectedBehavior] + [MethodName]
```

示例：
- `testCreateProduct_ShouldCreateProductSuccessfully`
- `testGetProductById_WhenProductExists_ShouldReturnProduct`
- `testDeleteProduct_WhenProductNotFound_ShouldThrowException`

### 4.3 测试用例命名

```
test[Scenario]_[ExpectedBehavior]
```

示例：
- `testCreateOrder_WithValidData_ShouldCreateOrder`
- `testCreateOrder_WithInvalidData_ShouldThrowValidationException`

## 5. 测试最佳实践

### 5.1 测试编写原则

1. **单一职责**
   - 每个测试方法只测试一个功能
   - 测试方法名称应该清楚地描述测试内容

2. **独立性**
   - 测试之间不应该有依赖关系
   - 每个测试应该可以独立执行

3. **可读性**
   - 测试代码应该清晰易懂
   - 使用有意义的变量名和方法名

4. **可维护性**
   - 测试代码应该易于修改和维护
   - 避免重复代码

5. **快速执行**
   - 测试应该快速执行
   - 避免不必要的等待和延迟

### 5.2 Mock使用原则

1. **Mock外部依赖**
   - Mock数据库、缓存、外部服务调用
   - 只测试业务逻辑

2. **Mock配置**
   - 使用`@Mock`注解创建Mock对象
   - 使用`when().thenReturn()`配置Mock行为
   - 使用`verify()`验证Mock调用

3. **避免过度Mock**
   - 只Mock必要的依赖
   - 避免Mock过多导致测试脆弱

### 5.3 断言使用原则

1. **使用有意义的断言**
   - 使用`assertEquals()`比较值
   - 使用`assertTrue()`验证条件
   - 使用`assertNull()`验证null值

2. **提供清晰的错误信息**
   - 在断言中提供详细的错误信息
   - 使用断言消息描述预期和实际值

3. **避免过度断言**
   - 只断言关键结果
   - 避免断言实现细节

## 6. 常见测试场景

### 6.1 正常场景测试

```java
@Test
@DisplayName("创建产品 - 正常场景")
public void testCreateProduct_NormalScenario() {
    ProductDTO productDTO = new ProductDTO();
    productDTO.setName("Test Product");
    productDTO.setPrice(100.0);
    productDTO.setCategoryId(1L);
    
    Product product = productService.createProduct(productDTO);
    
    assertNotNull(product);
    assertEquals("Test Product", product.getName());
    assertEquals(100.0, product.getPrice());
    assertEquals(1L, product.getCategoryId());
}
```

### 6.2 边界场景测试

```java
@Test
@DisplayName("创建产品 - 边界场景：价格为负数")
public void testCreateProduct_BoundaryScenario_NegativePrice() {
    ProductDTO productDTO = new ProductDTO();
    productDTO.setName("Test Product");
    productDTO.setPrice(-100.0);
    
    assertThrows(BusinessException.class, () -> {
        productService.createProduct(productDTO);
    });
}
```

### 6.3 异常场景测试

```java
@Test
@DisplayName("创建产品 - 异常场景：产品名称为空")
public void testCreateProduct_ExceptionScenario_EmptyName() {
    ProductDTO productDTO = new ProductDTO();
    productDTO.setName("");
    productDTO.setPrice(100.0);
    
    assertThrows(ValidationException.class, () -> {
        productService.createProduct(productDTO);
    });
}
```

## 7. 测试数据管理

### 7.1 测试数据准备

```java
@SpringBootTest
public class ProductRepositoryTest {
    
    @Autowired
    private ProductRepository productRepository;
    
    @BeforeEach
    public void setUp() {
        productRepository.deleteAll();
    }
    
    @Test
    public void testGetProduct_WithTestData() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        productRepository.save(product);
        
        Product foundProduct = productRepository.findById(1L).orElse(null);
        
        assertNotNull(foundProduct);
        assertEquals("Test Product", foundProduct.getName());
    }
}
```

### 7.2 测试数据清理

```java
@AfterEach
public void tearDown() {
    productRepository.deleteAll();
}
```

## 8. 测试配置

### 8.1 Maven测试配置

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.0.0-M7</version>
            <configuration>
                <includes>
                    <include>**/*Test.java</include>
                </includes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 8.2 测试环境配置

```yaml
spring:
  profiles:
    active: test
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    jpa:
      hibernate:
        ddl-auto: create-drop
```

## 9. 测试执行

### 9.1 运行单元测试

```bash
# 运行所有单元测试
mvn clean test

# 运行特定测试类
mvn test -Dtest=ProductServiceTest

# 运行特定测试方法
mvn test -Dtest=ProductServiceTest#testCreateProduct_NormalScenario

# 生成测试报告
mvn surefire-report:report

# 查看测试结果
mvn surefire:report
```

### 9.2 查看测试结果

```bash
# 查看测试报告
cat target/surefire-reports/TEST-classes.xml

# 查看测试覆盖率
open target/site/jacoco/index.html
```

## 10. 最佳实践

### 10.1 测试金字塔

1. **大量单元测试**
   - 单元测试应该占测试金字塔的底部
   - 快速、独立、可重复

2. **适量集成测试**
   - 集成测试应该占测试金字塔的中部
   - 测试组件之间的交互

3. **少量端到端测试**
   - 端到端测试应该占测试金字塔的顶部
   - 测试完整的业务流程

### 10.2 测试驱动开发

1. **先写测试**
   - 在编写功能代码之前先编写测试
   - 遵循红-绿-重构模式

2. **测试驱动重构**
   - 通过测试用例驱动代码重构
   - 提高代码质量和可测试性

3. **持续集成**
   - 每次代码提交后运行测试
   - 确保新代码不破坏现有功能

### 10.3 测试质量保证

1. **代码审查**
   - 定期进行代码审查
   - 使用测试代码作为审查依据
   - 确保测试代码质量

2. **测试审查**
   - 定期审查测试用例
   - 确保测试覆盖关键场景
   - 移除过时或重复的测试

3. **持续改进**
   - 根据测试结果持续改进代码
   - 优化测试执行速度
   - 提高测试覆盖率

## 11. 故障排查

### 11.1 测试失败排查

```bash
# 查看测试日志
mvn test -X

# 查看测试失败原因
cat target/surefire-reports/TEST-classes.xml

# 运行特定测试并查看详细日志
mvn test -Dtest=ProductServiceTest#testCreateProduct_NormalScenario -X
```

### 11.2 Mock问题排查

```java
// 验证Mock配置
@Test
public void testMockConfiguration() {
    verify(productRepository, times(1)).findById(1L);
    verifyNoMoreInteractions(productRepository);
}
```

## 12. 附录

### 12.1 相关文档

- [TestingConfigGuide.md](file:///e:/101/microservices/docs/TestingConfigGuide.md) - 测试环境配置指南
- [IntegrationTestingGuide.md](file:///e:/101/microservices/docs/IntegrationTestingGuide.md) - 集成测试指南
- [PerformanceTestingGuide.md](file:///e:/101/microservices/docs/PerformanceTestingGuide.md) - 性能测试指南
- [SecurityTestingGuide.md](file:///e:/101/microservices/docs/SecurityTestingGuide.md) - 安全测试指南

### 12.2 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|---------|
| 1.0.0 | 2025-01-19 | 架构师团队 | 初始版本 |

---

**文档结束**
