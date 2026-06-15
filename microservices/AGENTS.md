# 库存管理系统 - AI 代理开发指南

本文档为 AI 代理提供编码规范和构建命令，适用于 Java 21/Gradle 微服务项目。

## 构建命令

### 本地开发

```bash
# 清理并构建整个项目
./gradlew clean build

# 构建但不运行测试
./gradlew build -x test

# 运行所有测试
./gradlew test

# 运行单个测试类
./gradlew :core-services:product-service:test --tests "com.inventory.productservice.service.impl.ProductServiceImplTest"

# 运行单个测试方法
./gradlew :core-services:product-service:test --tests "com.inventory.productservice.service.impl.ProductServiceImplTest.testGetProductById"

# 按标签运行测试（排除集成测试）
./gradlew test -x integration -x slow -x e2e

# 仅运行代码质量检查
./gradlew checkQuality

# 带质量检查的构建
./gradlew buildWithQuality

# 检查 JaCoCo 覆盖率
./gradlew jacocoCoverageCheck

# 查看项目信息
./gradlew projectInfo
```

### 按模块命令

```bash
# 构建特定服务
./gradlew :core-services:product-service:build
./gradlew :core-services:order-service:build
./gradlew :support-services:auth-service:build

# 测试特定服务
./gradlew :core-services:product-service:test
./gradlew :support-services:admin-service:test

# 在特定模块上运行 checkstyle
./gradlew :core-services:product-service:checkstyleMain
```

## 代码规范

### 格式化

- **行长度**: 最多 120 个字符
- **缩进**: Java 4 空格，YAML/XML/JSON 2 空格
- **行尾符**: LF (Unix 风格)
- **字符编码**: UTF-8
- **末尾换行**: 必须保留
- **尾部空格**: 必须删除

### 命名规范

| 元素 | 规范 | 示例 |
|------|------|------|
| 包名 | 小写，点分隔 | `com.inventory.productservice` |
| 类名 | PascalCase | `ProductServiceImpl`, `ProductDTO` |
| 接口名 | PascalCase（核心模块不加 I 前缀） | `ProductService`, `JpaRepository` |
| Repository 接口 | I 前缀 | `IProductRepository`, `IOrderRepository` |
| 方法名 | camelCase | `getProductById`, `findBySku` |
| 变量名 | camelCase | `productId`, `orderDTO` |
| 常量名 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE` |
| 测试方法名 | `test` 前缀 | `testGetProductById`, `testCreateProduct` |
| Mock 变量名 | `mock` 前缀 | `mockProductRepository`, `mockModelMapper` |

### Java 规范

#### 类结构
```java
// 1. 包声明
package com.inventory.productservice.service.impl;

// 2. 导入语句（分组：java, org, com, static）
import java.util.List;
import org.springframework.stereotype.Service;
import com.inventory.productservice.dto.ProductDTO;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

// 3. 类注解
@Service

// 4. 类声明（使用 Lombok）
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProductServiceImpl implements ProductService {
    // 字段
    private final ProductRepository productRepository;
    
    // 方法
}
```

#### 注解规范
- **服务类**: `@Slf4j`, `@Service`, `@RequiredArgsConstructor`, `@Transactional`
- **控制器**: `@RestController`, `@RequestMapping`, `@RequiredArgsConstructor`, `@Tag` (OpenAPI)
- **实体类**: `@Entity`, `@Table`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`
- **DTO 类**: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- **Repository**: `@Repository`
- **测试类**: `@ExtendWith(MockitoExtension.class)`, `@SuppressWarnings("null")`
- **Mock 字段**: `@Mock`（禁止 `@InjectMocks`，使用构造器手动注入）

#### 方法模式
```java
@Override
public ProductDTO getProductById(Long id) {
    log.info("Getting product by id: {}", id);
    final Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    return modelMapper.map(product, ProductDTO.class);
}
```

#### 参数和变量
- 方法参数必须使用 `final` 修饰：`void updateProduct(final Long id, final ProductDTO dto)`
- 局部变量在不重新赋值时也应使用 `final`

### Mockito 规范

**必须使用显式静态导入（禁止通配符导入）：**
```java
// 正确
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

// 禁止
import static org.mockito.Mockito.*;
```

**Mock 命名规则:** 必须使用 `mock` 前缀
```java
@Mock
private ProductRepository mockProductRepository;
```

**Stub 创建：**
```java
when(mockProductRepository.findById(anyLong())).thenReturn(Optional.of(product));
verify(mockProductRepository, times(1)).findById(1L);
```

### 异常处理

所有业务异常必须继承 `BaseApplicationException`：
```java
public class ProductNotFoundException extends BaseApplicationException {
    public ProductNotFoundException(String message) {
        super("PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND, message);
    }
    
    public ProductNotFoundException(String message, Map<String, Object> context) {
        super("PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND, message, context);
    }
}
```

**错误码格式**: `AAA-XX-XXX`
- `SYS`: 系统错误
- `VAL`: 验证错误
- `BIZ`: 业务逻辑错误
- `AUTH`: 认证授权错误

### 代码质量阈值

| 指标 | 限制 |
|------|------|
| 最大行长度 | 120 |
| 最大方法长度 | 150 |
| 最大参数数量 | 7 |
| 最大圈复杂度 | 15 |
| 最大认知复杂度 | 25 |
| 最小行覆盖率 | 80% |
| 最小分支覆盖率 | 70% |

### 禁止的模式

- **禁止** 在生产代码中使用 `System.out.println` 或 `System.err.print`
- **禁止** 使用 Mockito 的通配符静态导入 (`import static org.mockito.Mockito.*`)
- **禁止** 使用 `@InjectMocks`（使用构造器注入配合 `@InjectMocks`）
- **禁止** 使用 `as any` 类型压制
- **禁止** 空 catch 块
- **禁止** star imports（`static org.mockito.Mockito.*` 除外）
- **禁止** Tab 字符（使用空格）
- **禁止** 回车符 (`\r`)

### 包结构

```
src/
├── main/
│   ├── java/
│   │   └── com/inventory/<service>/
│   │       ├── controller/      # REST 控制器
│   │       ├── service/         # 服务接口
│   │       │   └── impl/        # 服务实现
│   │       ├── repository/      # JPA 仓库
│   │       ├── entity/          # JPA 实体
│   │       ├── dto/             # 数据传输对象
│   │       ├── exception/       # 自定义异常
│   │       └── config/          # 配置类
│   └── resources/
│       └── application.yml       # 服务配置
└── test/
    ├── java/
    │   └── com/inventory/<service>/
    │       ├── controller/      # 控制器测试
    │       └── service/         # 服务单元测试
    │           └── impl/
    └── resources/
        └── application-test.yml  # 测试配置
```

### API 设计

- 基础路径: `/api/<资源复数>` (例如: `/api/products`)
- 请求体验证使用 `@Valid`
- 所有控制器方法返回 `ResponseEntity<T>`
- 使用 OpenAPI 注解: `@Operation`, `@Tag`
- HTTP 状态码: `200 OK`, `201 CREATED`, `204 NO_CONTENT`, `400 BAD_REQUEST`, `404 NOT_FOUND`

### 日志规范

- 使用 SLF4J 和 `@Slf4j`
- 方法入口日志: `log.info("Method starting with param: {}", value)`
- 方法出口日志: `log.info("Method completed, result: {}", result)`
- 错误日志: `log.error("Error occurred: {}", message, exception)`

### 测试命名规范

测试方法遵循模式: `test<方法名>When<条件>`

```java
@Test
void testGetProductById() { ... }

@Test
void testGetProductByIdNotFound() { ... }

@Test
void testCreateProductSuccess() { ... }

@Test
void testCreateProductWithInvalidData() { ... }
```

### 技术栈

- **Java**: 21
- **框架**: Spring Boot 3.4.4
- **云服务**: Spring Cloud 2024.0.2, Spring Cloud Alibaba 2023.0.1.0
- **构建工具**: Gradle 9.5.1 (Kotlin DSL)
- **数据库**: PostgreSQL, H2 (测试)
- **缓存**: Redis
- **测试**: JUnit 5, Mockito, TestContainers
- **质量工具**: Checkstyle, PMD, SpotBugs, JaCoCo

### 多模块结构

```
inventory-management-system/
├── common/                          # 共享代码
├── core-services/
│   ├── product-service/
│   ├── order-service/
│   ├── inventory-service/
│   ├── sales-service/
│   ├── procurement-service/
│   ├── customer-service/
│   ├── supplier-service/
│   ├── business-partner-service/
│   ├── mall-service/
│   ├── datasource-service/
│   ├── template-service/
│   └── invoice-service/
├── support-services/
│   ├── auth-service/
│   ├── admin-service/
│   ├── finance-service/
│   ├── gateway-service/
│   ├── config-service/
│   ├── config-service-simple/
│   ├── registry-service/
│   └── report-service/
├── monitoring-core/
├── monitoring-spring-boot-starter/
├── monitoring/
└── cross-service-tests/
```
