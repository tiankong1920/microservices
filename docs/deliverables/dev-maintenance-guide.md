# 进销存管理系统 v3.0 — 开发维护文档

> **版本**：3.0
> **日期**：2026-06-08
> **适用对象**：开发工程师、维护工程师、技术负责人
> **密级**：内部公开

---

## 目录

1. [项目结构总览](#1-项目结构总览)
2. [开发环境搭建](#2-开发环境搭建)
3. [编码规范](#3-编码规范)
4. [构建与发布流程](#4-构建与发布流程)
5. [数据库变更管理](#5-数据库变更管理)
6. [日志规范与排错](#6-日志规范与排错)
7. [性能调优指南](#7-性能调优指南)
8. [技术债务清单](#8-技术债务清单)
9. [贡献者指南](#9-贡献者指南)
10. [版本发布检查清单](#10-版本发布检查清单)

---

## 1. 项目结构总览

### 1.1 顶层目录结构

```
inventory-system-v3/
├── microservices/                # 后端微服务（Gradle 多项目）
│   ├── build.gradle.kts          # 根构建文件
│   ├── settings.gradle.kts       # 子项目声明
│   ├── gradle/                   # Gradle Wrapper 配置
│   ├── common/                   # 公共模块
│   ├── gateway/                  # API 网关
│   ├── product-service/          # 商品服务
│   ├── order-service/            # 订单服务
│   ├── inventory-service/        # 库存服务
│   ├── sales-service/            # 销售服务
│   ├── procurement-service/      # 采购服务
│   ├── customer-service/         # 客户服务
│   ├── supplier-service/         # 供应商服务
│   ├── business-partner-service/ # 业务伙伴服务
│   ├── finance-service/          # 财务服务
│   ├── auth-service/             # 认证授权服务
│   ├── admin-service/            # 系统管理服务
│   ├── report-service/           # 报表服务
│   ├── config-service/           # 配置中心
│   ├── registry-service/         # 注册中心
│   ├── api/                      # API 契约模块
│   ├── model/                    # 数据模型模块
│   ├── client/                   # 服务间调用客户端
│   ├── infrastructure/           # 基础设施模块
│   ├── test/                     # 集成测试模块
│   └── docs/                     # 后端文档
├── web-frontend/                 # 前端项目
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   └── src/
├── docker/                       # Docker 相关
│   ├── docker-compose.yml
│   └── Dockerfiles/
├── k8s/                          # Kubernetes 部署配置
│   ├── base/
│   └── overlays/
├── scripts/                      # 运维脚本
└── docs/                         # 项目文档
```

### 1.2 25 个 Gradle 子项目职责说明

| # | 子项目 | 路径 | 端口 | 职责 |
|---|--------|------|------|------|
| 1 | **common** | `common/` | — | 公共工具类、常量、通用异常、工具方法 |
| 2 | **api** | `api/` | — | 各服务 Feign 客户端接口定义（API 契约） |
| 3 | **model** | `model/` | — | 跨服务共享的 DTO、VO、枚举等数据模型 |
| 4 | **client** | `client/` | — | 服务间调用的 Feign Client 实现 |
| 5 | **infrastructure** | `infrastructure/` | — | 基础设施抽象层（Redis/Kafka/Nacos 封装） |
| 6 | **test** | `test/` | — | 集成测试基础设施、Testcontainers 配置 |
| 7 | **gateway** | `gateway/` | 8080 | Spring Cloud Gateway，路由转发、限流、鉴权 |
| 8 | **product-service** | `product-service/` | 8081 | 商品 CRUD、分类管理、规格管理 |
| 9 | **order-service** | `order-service/` | 8082 | 订单创建/流转/状态机、订单事件发布 |
| 10 | **inventory-service** | `inventory-service/` | 8083 | 库存管理、出入库、库存预警、库存锁定 |
| 11 | **sales-service** | `sales-service/` | 8084 | 销售单管理、销售统计分析 |
| 12 | **procurement-service** | `procurement-service/` | 8085 | 采购单管理、供应商比价、采购审批 |
| 13 | **customer-service** | `customer-service/` | 8086 | 客户信息管理、客户分级、客户画像 |
| 14 | **supplier-service** | `supplier-service/` | 8087 | 供应商管理、资质审核、供应商评价 |
| 15 | **business-partner-service** | `business-partner-service/` | 8088 | 业务伙伴关系管理、合作合同 |
| 16 | **finance-service** | `finance-service/` | 8092 | 应收应付、对账、财务报表 |
| 17 | **auth-service** | `auth-service/` | 8093 | 用户认证、JWT 签发/验证、MFA、RBAC 权限 |
| 18 | **admin-service** | `admin-service/` | 8091 | 系统配置管理、审计日志查看、运营统计 |
| 19 | **report-service** | `report-service/` | 8094 | 报表生成、数据导出、定时报告 |
| 20 | **config-service** | `config-service/` | 8888 | Spring Cloud Config Server，配置版本管理 |
| 21 | **registry-service** | `registry-service/` | 8761 | Spring Cloud Registry（兼容层），服务发现 |
| 22 | **docs** | `docs/` | — | Swagger/OpenAPI 文档聚合、API 契约文档 |
| 23 | **product-api** | `api/product-api/` | — | 商品服务 API 契约（Feign Interface） |
| 24 | **order-api** | `api/order-api/` | — | 订单服务 API 契约（Feign Interface） |
| 25 | **inventory-api** | `api/inventory-api/` | — | 库存服务 API 契约（Feign Interface） |

### 1.3 单服务内部结构（以 product-service 为例）

```
product-service/
├── build.gradle.kts
└── src/
    ├── main/
    │   ├── java/com/inventory/product/
    │   │   ├── ProductApplication.java        # 启动类
    │   │   ├── controller/                     # REST Controller
    │   │   │   ├── ProductController.java
    │   │   │   └── CategoryController.java
    │   │   ├── service/                        # 业务逻辑层
    │   │   │   ├── ProductService.java
    │   │   │   ├── CategoryService.java
    │   │   │   └── impl/
    │   │   │       ├── ProductServiceImpl.java
    │   │   │       └── CategoryServiceImpl.java
    │   │   ├── repository/                     # 数据访问层
    │   │   │   ├── ProductRepository.java
    │   │   │   └── CategoryRepository.java
    │   │   ├── entity/                         # JPA 实体
    │   │   │   ├── Product.java
    │   │   │   └── Category.java
    │   │   ├── dto/                            # 数据传输对象
    │   │   │   ├── ProductRequest.java
    │   │   │   ├── ProductResponse.java
    │   │   │   └── CategoryDTO.java
    │   │   ├── mapper/                         # MapStruct 映射器
    │   │   │   └── ProductMapper.java
    │   │   ├── config/                         # 配置类
    │   │   │   └── RedisConfig.java
    │   │   ├── exception/                      # 服务特定异常
    │   │   │   └── ProductNotFoundException.java
    │   │   └── event/                          # 领域事件
    │   │       └── ProductCreatedEvent.java
    │   └── resources/
    │       ├── application.yml
    │       ├── bootstrap.yml
    │       └── db/migration/                   # Flyway 迁移脚本
    │           ├── V1__create_product_table.sql
    │           └── V2__add_product_spec.sql
    └── test/
        └── java/com/inventory/product/
            ├── controller/
            │   └── ProductControllerTest.java
            ├── service/
            │   └── ProductServiceTest.java
            └── repository/
                └── ProductRepositoryTest.java
```

### 1.4 前端项目结构

```
web-frontend/
├── package.json
├── vite.config.ts
├── tsconfig.json
├── index.html
└── src/
    ├── main.tsx                    # 入口文件
    ├── App.tsx                     # 根组件
    ├── api/                        # API 调用封装
    │   ├── client.ts               # Axios 实例配置
    │   ├── product.ts
    │   ├── order.ts
    │   └── inventory.ts
    ├── components/                 # 通用组件
    │   ├── Layout/
    │   ├── Table/
    │   └── Form/
    ├── pages/                      # 页面组件
    │   ├── Product/
    │   ├── Order/
    │   ├── Inventory/
    │   └── Dashboard/
    ├── hooks/                      # 自定义 Hooks
    ├── store/                      # 状态管理（Zustand）
    ├── utils/                      # 工具函数
    └── types/                      # TypeScript 类型定义
```

---

## 2. 开发环境搭建

### 2.1 必备工具安装

#### JDK 21

```bash
# 推荐使用 SDKMAN 安装
curl -s "https://get.sdkman.io" | bash
sdk install java 21.0.3-tem

# 验证
java -version
# openjdk version "21.0.3"

# 配置 JAVA_HOME
export JAVA_HOME=$(sdk home java current)
```

#### Gradle 9.4.0

```bash
# 项目已包含 Gradle Wrapper，无需单独安装
# 使用项目自带的 Wrapper
cd microservices/
./gradlew --version
# Gradle 9.4.0

# 如需手动安装
sdk install gradle 9.4.0
```

#### Node.js（前端开发）

```bash
# 推荐使用 nvm 安装
nvm install 20
nvm use 20

# 验证
node --version  # v20.x
npm --version   # 10.x
```

#### Docker Desktop

```bash
# 下载安装 Docker Desktop
# https://www.docker.com/products/docker-desktop/

# 验证
docker --version
docker compose version

# 配置镜像加速（可选）
# 编辑 ~/.docker/daemon.json
{
  "registry-mirrors": ["https://mirror.example.com"]
}
```

#### IDE 配置

**IntelliJ IDEA（推荐）**：

1. 安装 IntelliJ IDEA Ultimate 2024.1+
2. 插件安装：
   - Lombok Plugin
   - MapStruct Support
   - Spring Boot Helper
   - Gradle Plugin（内置）
3. 导入项目：`File → Open → microservices/`
4. 启用 Annotation Processing：`Settings → Build → Compiler → Annotation Processors → Enable`
5. 设置 Gradle JVM：`Settings → Build → Gradle → Gradle JVM → 21 (Temurin)`

**VS Code（前端开发）**：

1. 安装扩展：
   - ESLint
   - Prettier
   - TypeScript Importer
   - MUI Snippets

### 2.2 本地环境启动

#### 启动基础设施

```bash
# 启动 PostgreSQL、Redis、Kafka、Nacos
cd docker/
docker compose up -d postgres redis kafka nacos

# 等待基础设施就绪
sleep 30

# 验证
pg_isready -h localhost -p 5432                # PostgreSQL
redis-cli ping                                  # Redis
kafka-topics.sh --bootstrap-server localhost:9092 --list  # Kafka
curl http://localhost:8848/nacos/               # Nacos
```

#### 配置 Nacos

1. 访问 Nacos 控制台：`http://localhost:8848/nacos`（默认账号 nacos/nacos）
2. 创建命名空间：`dev`
3. 导入配置文件（从 `docs/nacos-config/` 目录）

#### 初始化数据库

```bash
cd microservices/

# 方式1：启动服务时 Flyway 自动迁移
# 方式2：手动执行迁移
./gradlew flywayMigrate -Penv=dev
```

#### 启动后端服务

```bash
cd microservices/

# 构建项目（跳过测试）
./gradlew build -x test

# 启动单个服务（开发模式）
./gradlew :product-service:bootRun

# 或通过 IDE 直接运行各服务的 Application 类
```

#### 启动前端

```bash
cd web-frontend/

# 安装依赖
npm install

# 启动开发服务器
npm run dev
# 访问 http://localhost:5173
```

### 2.3 环境配置说明

#### 后端配置优先级

```
命令行参数 > 环境变量 > Nacos 配置 > bootstrap.yml > application.yml > 默认值
```

#### 开发环境 profile 配置

各服务 `application-dev.yml` 示例：

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/inv_product
    username: inventory_admin
    password: ${POSTGRES_PASSWORD:1234}
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: validate
  redis:
    host: localhost
    port: 6379

logging:
  level:
    com.inventory: DEBUG
    org.springframework.web: DEBUG
```

### 2.4 开发工具推荐

| 工具 | 用途 | 安装方式 |
|------|------|---------|
| Postman / Bruno | API 调试 | 桌面安装 |
| DBeaver | 数据库管理 | 桌面安装 |
| RedisInsight | Redis 管理 | 桌面安装 |
| Kafka Tool | Kafka 管理 | 桌面安装 |
| k9s | Kubernetes 管理 | `brew install k9s` |
| Stern | 多 Pod 日志 | `brew install stern` |

---

## 3. 编码规范

### 3.1 包命名规范

```
com.inventory.{service}             # 根包
├── controller                      # REST API 控制器
├── service                         # 业务逻辑接口
│   └── impl                        # 业务逻辑实现
├── repository                      # Spring Data JPA Repository
├── entity                          # JPA 实体类
├── dto                             # 数据传输对象
│   ├── request                     # 请求 DTO
│   └── response                    # 响应 DTO
├── mapper                          # MapStruct 映射器
├── config                          # Spring 配置类
├── exception                       # 自定义异常
├── event                           # 领域事件
│   ├── publisher                   # 事件发布
│   └── listener                    # 事件监听
├── constant                        # 常量定义
└── util                            # 工具类
```

**命名规则**：

- 包名全小写，使用点分隔
- 禁止使用缩写（通用缩写除外：dto, vo, dao）
- 服务名使用完整单词：`product`、`inventory`、`procurement`

### 3.2 类命名规范

| 类型 | 命名格式 | 示例 |
|------|---------|------|
| 实体类 | 名词，PascalCase | `Product`, `SalesOrder` |
| Controller | `{Entity}Controller` | `ProductController` |
| Service 接口 | `{Entity}Service` | `ProductService` |
| Service 实现 | `{Entity}ServiceImpl` | `ProductServiceImpl` |
| Repository | `{Entity}Repository` | `ProductRepository` |
| DTO | `{Entity}{Action}Request/Response` | `ProductCreateRequest`, `ProductResponse` |
| 异常类 | `{Specific}Exception` | `ProductNotFoundException` |
| 配置类 | `{Feature}Config` | `RedisConfig` |
| 事件 | `{Entity}{Action}Event` | `OrderCreatedEvent` |
| 监听器 | `{Entity}{Action}Listener` | `OrderCreatedListener` |
| 映射器 | `{Entity}Mapper` | `ProductMapper` |

### 3.3 异常处理规范

#### 全局异常处理器

每个服务应实现统一的全局异常处理器：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("资源未找到: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .code("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        ErrorResponse error = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message("参数校验失败")
                .details(errors)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        log.warn("业务异常: code={}, message={}", ex.getCode(), ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(ex.getHttpStatus()).body(error);
    }

    /**
     * 处理未知异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception ex) {
        log.error("未预期异常", ex);
        ErrorResponse error = ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("服务器内部错误")
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

#### 统一错误响应格式

```java
@Data
@Builder
public class ErrorResponse {
    private String code;          // 错误码，如 "RESOURCE_NOT_FOUND"
    private String message;       // 用户可读的错误信息
    private List<String> details; // 详细错误列表（可选）
    private Instant timestamp;    // 错误发生时间
}
```

#### 自定义业务异常

```java
@Getter
public class BusinessException extends RuntimeException {
    private final String code;
    private final HttpStatus httpStatus;

    public BusinessException(String code, String message, HttpStatus httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
```

#### 异常处理原则

1. **不吞异常**：禁止空 catch 块，至少记录日志
2. **不暴露内部信息**：返回给客户端的错误信息不应包含堆栈跟踪或 SQL
3. **业务异常优先**：使用 `BusinessException` 抛出可预期的业务错误
4. **日志与返回分离**：日志记录详细堆栈，返回精简错误信息
5. **错误码标准化**：使用大写下划线格式的错误码，如 `INSUFFICIENT_STOCK`

### 3.4 Lombok 使用规范

#### 推荐使用的注解

| 注解 | 适用场景 | 说明 |
|------|---------|------|
| `@Getter` / `@Setter` | Entity, DTO | 替代手写 getter/setter |
| `@Builder` | DTO, Request/Response | 构建器模式 |
| `@NoArgsConstructor` | Entity, DTO | 无参构造器 |
| `@AllArgsConstructor` | Entity | 全参构造器 |
| `@Slf4j` | Service, Controller | 日志对象 |
| `@EqualsAndHashCode` | Entity | equals/hashCode（仅主键字段） |
| `@ToString` | DTO | 排除敏感字段 |

#### 禁止使用的注解

| 注解 | 原因 |
|------|------|
| `@Data` | 包含 `@ToString` 可能泄露敏感数据，且 JPA Entity 使用 `@Data` 会导致循环引用 |
| `@Value` | 不可变对象与 JPA 不兼容 |

#### Entity 使用示例

```java
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"specifications", "description"})
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 50)
    private String sku;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(length = 10)
    private String unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
```

#### DTO 使用示例

```java
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称不超过200字")
    private String name;

    @NotBlank(message = "SKU不能为空")
    @Size(max = 50, message = "SKU不超过50字")
    private String sku;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    private Long categoryId;
}
```

### 3.5 API 设计约定

#### URL 规范

```
# 基础路径
/api/v1/{resource}

# 示例
GET    /api/v1/products              # 列表查询
GET    /api/v1/products/{id}         # 详情查询
POST   /api/v1/products              # 创建
PUT    /api/v1/products/{id}         # 全量更新
PATCH  /api/v1/products/{id}         # 部分更新
DELETE /api/v1/products/{id}         # 删除

# 子资源
GET    /api/v1/products/{id}/specifications
POST   /api/v1/orders/{id}/cancel

# 动作类
POST   /api/v1/inventory/{id}/lock
POST   /api/v1/inventory/{id}/unlock
```

#### 统一响应格式

```java
@Data
@Builder
public class ApiResponse<T> {
    private int code;           // 业务码，200=成功
    private String message;     // 消息
    private T data;             // 数据
    private Instant timestamp;  // 时间戳

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}
```

#### 分页查询

```java
@Data
public class PageRequest {
    @Min(0)
    private int page = 0;

    @Min(1)
    @Max(100)
    private int size = 20;

    private String sort = "createdAt";
    private String direction = "DESC";
}

@Data
@Builder
public class PageResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
}
```

#### HTTP 状态码使用

| 状态码 | 场景 |
|--------|------|
| 200 | 成功（查询、更新） |
| 201 | 创建成功 |
| 204 | 删除成功（无返回体） |
| 400 | 参数校验失败 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 冲突（如重复创建） |
| 422 | 业务规则校验失败 |
| 500 | 服务器内部错误 |

### 3.6 MapStruct 使用规范

```java
@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductCreateRequest request);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Product product, ProductUpdateRequest request);

    List<ProductResponse> toResponseList(List<Product> products);
}
```

**规则**：

- `componentModel = "spring"` 必须指定，使 Mapper 成为 Spring Bean
- 忽略自动生成的字段（id, createdAt, updatedAt）
- 更新操作使用 `@MappingTarget` 注解
- 复杂映射提供 `@AfterMapping` 或 default 方法实现

---

## 4. 构建与发布流程

### 4.1 Gradle 构建

#### 常用构建命令

```bash
cd microservices/

# 完整构建（含测试）
./gradlew build

# 构建跳过测试（日常开发推荐）
./gradlew build -x test

# 构建指定服务
./gradlew :product-service:build

# 清理构建产物
./gradlew clean

# 清理后构建
./gradlew clean build -x test

# 查看依赖树
./gradlew :product-service:dependencies

# 查看项目结构
./gradlew projects
```

#### 根 build.gradle.kts 关键配置

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.4.4" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

allprojects {
    group = "com.inventory"
    version = "3.0.0"

    repositories {
        mavenCentral()
        maven { url = uri("https://repo.spring.io/milestone") }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    dependencies {
        implementation("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        implementation("org.mapstruct:mapstruct:1.6.3")
        annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
```

### 4.2 Docker 镜像构建

#### 单服务 Dockerfile

```dockerfile
# 多阶段构建
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew :product-service:bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/product-service/build/libs/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m"
EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
```

#### Docker Compose 构建

```bash
# 构建所有镜像
docker compose build

# 构建指定服务镜像
docker compose build product-service

# 构建并启动
docker compose up -d --build
```

### 4.3 CI/CD 流水线

#### GitHub Actions 配置

```yaml
# .github/workflows/ci.yml
name: CI Pipeline

on:
  push:
    branches: [main, develop, 'release/**']
  pull_request:
    branches: [main, develop]

env:
  JAVA_VERSION: '21'
  NODE_VERSION: '20'

jobs:
  backend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'

      - name: Cache Gradle packages
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ hashFiles('microservices/**/*.gradle.kts', 'microservices/**/gradle-wrapper.properties') }}

      - name: Build & Test
        working-directory: microservices
        run: ./gradlew build

      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: microservices/**/build/reports/tests/

  frontend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: ${{ env.NODE_VERSION }}
          cache: 'npm'
          cache-dependency-path: web-frontend/package-lock.json

      - name: Install & Test
        working-directory: web-frontend
        run: |
          npm ci
          npm run lint
          npm run build
          npm test

  build-and-push:
    needs: [backend-test, frontend-test]
    if: github.ref == 'refs/heads/main' || startsWith(github.ref, 'refs/heads/release/')
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Log in to Container Registry
        uses: docker/login-action@v3
        with:
          registry: ${{ secrets.REGISTRY_URL }}
          username: ${{ secrets.REGISTRY_USERNAME }}
          password: ${{ secrets.REGISTRY_PASSWORD }}

      - name: Build and Push Images
        run: |
          docker compose build
          docker compose push

  deploy-staging:
    needs: build-and-push
    if: github.ref == 'refs/heads/develop'
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Staging
        run: |
          kubectl set image deployment/product-service \
            product-service=${{ secrets.REGISTRY_URL }}/product-service:${{ github.sha }} \
            -n inventory-staging

  deploy-production:
    needs: build-and-push
    if: startsWith(github.ref, 'refs/heads/release/')
    runs-on: ubuntu-latest
    environment: production
    steps:
      - name: Deploy to Production
        run: |
          kubectl set image deployment/product-service \
            product-service=${{ secrets.REGISTRY_URL }}/product-service:${{ github.sha }} \
            -n inventory-system
```

#### Jenkins Pipeline（Jenkinsfile）

```groovy
pipeline {
    agent any

    environment {
        REGISTRY = credentials('docker-registry-url')
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend Build & Test') {
            steps {
                dir('microservices') {
                    sh './gradlew build'
                }
            }
            post {
                always {
                    junit 'microservices/**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Frontend Build & Test') {
            steps {
                dir('web-frontend') {
                    sh 'npm ci && npm run lint && npm run build'
                }
            }
        }

        stage('Docker Build & Push') {
            when {
                anyOf {
                    branch 'main'
                    branch 'release/*'
                }
            }
            steps {
                sh 'docker compose build'
                sh 'docker compose push'
            }
        }

        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                sh 'kubectl apply -k k8s/overlays/staging/'
            }
        }

        stage('Deploy to Production') {
            when {
                branch 'release/*'
            }
            input {
                message "确认部署到生产环境？"
            }
            steps {
                sh 'kubectl apply -k k8s/overlays/production/'
            }
        }
    }
}
```

---

## 5. 数据库变更管理

### 5.1 Flyway 迁移脚本规范

#### 脚本命名规则

```
V{版本号}__{描述}.sql       # 正式迁移（版本号不可重复）
U{版本号}__{描述}.sql       # 回滚迁移（可选）
R__{描述}.sql               # 可重复执行迁移（如视图、函数）
```

**版本号规则**：

- 格式：`V{主版本}_{次版本}_{补丁}__{描述}.sql`
- 各服务独立编号，版本号必须递增
- 示例：

```
V1__create_product_table.sql
V2__add_product_spec_column.sql
V3__create_product_audit_trigger.sql
V1_1__add_sku_unique_index.sql
```

#### 迁移脚本模板

```sql
-- V3__create_product_spec_table.sql
-- 描述: 创建商品规格表
-- 作者: zhangsan
-- 日期: 2026-06-08

-- 创建规格表
CREATE TABLE product_specifications (
    id              BIGSERIAL       PRIMARY KEY,
    product_id      BIGINT          NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    spec_name       VARCHAR(100)    NOT NULL,
    spec_value      VARCHAR(500)    NOT NULL,
    sort_order      INTEGER         DEFAULT 0,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_product_spec_product_id ON product_specifications(product_id);
CREATE INDEX idx_product_spec_name ON product_specifications(spec_name);

-- 注释
COMMENT ON TABLE product_specifications IS '商品规格表';
COMMENT ON COLUMN product_specifications.product_id IS '商品ID';
COMMENT ON COLUMN product_specifications.spec_name IS '规格名称';
COMMENT ON COLUMN product_specifications.spec_value IS '规格值';
```

#### 迁移脚本编写规范

1. **禁止修改已执行的脚本**：已应用到数据库的迁移脚本不可修改
2. **向前兼容**：新脚本必须兼容当前生产数据
3. **幂等设计**：避免重复插入数据，使用 `IF NOT EXISTS`
4. **数据迁移与 DDL 分离**：结构变更与数据迁移分开脚本
5. **禁止 DROP 操作**：生产环境不使用 DROP TABLE/COLUMN，先标记废弃
6. **添加注释**：每个脚本头部包含描述、作者、日期

### 5.2 版本控制

#### 各服务迁移脚本路径

```
{service}/src/main/resources/db/migration/
```

#### Flyway 配置

```yaml
# application.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 0
    validate-on-migrate: true
    out-of-order: false
    encoding: UTF-8
    placeholder-replacement: false
```

#### 常用 Flyway 命令

```bash
# 查看迁移状态
./gradlew flywayInfo -Penv=dev

# 执行迁移
./gradlew flywayMigrate -Penv=dev

# 验证迁移一致性
./gradlew flywayValidate -Penv=dev

# 修复迁移元数据（谨慎使用）
./gradlew flywayRepair -Penv=dev
```

### 5.3 数据库变更审批流程

1. 开发人员编写迁移脚本并提交 PR
2. DBA Review 迁移脚本（SQL 语法、索引策略、向前兼容性）
3. 在 staging 环境验证迁移
4. 合并 PR 后自动应用到目标环境

---

## 6. 日志规范与排错

### 6.1 SLF4J 结构化日志

#### 日志配置

```yaml
# logback-spring.xml 配置示例
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [traceId=%X{traceId}] - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [traceId=%X{traceId}] - %msg%n"
  file:
    name: logs/${spring.application.name}.log
    max-size: 100MB
    max-history: 30
    total-size-cap: 3GB
```

#### 结构化日志示例

```java
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("创建商品: name={}, sku={}", request.getName(), request.getSku());

        Product product = productMapper.toEntity(request);
        Product saved = productRepository.save(product);

        log.info("商品创建成功: id={}, name={}", saved.getId(), saved.getName());
        return productMapper.toResponse(saved);
    }

    public ProductResponse getProduct(Long id) {
        log.debug("查询商品: id={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("商品不存在: id={}", id);
                    return new ResourceNotFoundException("商品不存在: " + id);
                });

        return productMapper.toResponse(product);
    }
}
```

### 6.2 日志级别规范

| 级别 | 使用场景 | 示例 |
|------|---------|------|
| `ERROR` | 影响业务功能的异常，需要立即关注 | 数据库连接失败、外部服务不可用 |
| `WARN` | 可预期的异常或潜在问题，不影响主流程 | 参数校验失败、重试后成功、降级处理 |
| `INFO` | 重要业务操作记录 | 创建订单、库存变更、用户登录 |
| `DEBUG` | 调试信息，开发/测试环境使用 | SQL 参数、方法入参/出参 |
| `TRACE` | 非常详细的运行时信息 | 框架内部调用链、序列化细节 |

#### 日志级别使用原则

1. **ERROR 日志必须可操作**：每条 ERROR 日志应明确可采取的行动
2. **禁止在循环中打日志**：避免高频循环产生大量日志
3. **敏感信息脱敏**：密码、Token、手机号等必须脱敏
4. **不记录大对象**：避免将完整请求/响应体记入日志
5. **使用参数化日志**：`log.info("id={}", id)` 而非 `log.info("id=" + id)`

### 6.3 分布式追踪

#### TraceId 传递

通过 Spring Cloud Sleuth / Micrometer Tracing 自动注入：

```yaml
# application.yml
management:
  tracing:
    sampling:
      probability: 1.0
    propagation:
      type: w3c
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

#### 排错流程

```
1. 获取 TraceId（从响应头 X-Trace-Id 或日志中）
2. 在 Zipkin/Jaeger 中搜索 TraceId
3. 查看完整调用链和耗时分布
4. 定位到具体服务的具体方法
5. 查看该服务的详细日志（grep traceId）
6. 分析根因并修复
```

### 6.4 日志聚合

推荐使用 ELK（Elasticsearch + Logstash + Kibana）或 Loki + Grafana：

```yaml
# Filebeat 配置（收集 Docker 日志）
filebeat.inputs:
  - type: container
    paths:
      - /var/lib/docker/containers/*/*.log
    processors:
      - add_kubernetes_metadata:

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
  index: "inventory-logs-%{+yyyy.MM.dd}"
```

---

## 7. 性能调优指南

### 7.1 JVM 参数

#### 推荐配置（生产环境）

```bash
# 通用微服务 JVM 参数
JAVA_OPTS="
  -Xms512m                                    # 初始堆内存
  -Xmx1024m                                   # 最大堆内存
  -XX:+UseG1GC                                # 使用 G1 垃圾收集器
  -XX:MaxGCPauseMillis=200                    # 目标 GC 停顿时间
  -XX:+HeapDumpOnOutOfMemoryError             # OOM 时生成堆转储
  -XX:HeapDumpPath=/logs/heapdump.hprof       # 堆转储路径
  -XX:+PrintGCDetails                         # 打印 GC 详情
  -XX:+PrintGCDateStamps                      # GC 日志带时间戳
  -Xlog:gc*:file=/logs/gc.log:time,uptime:filecount=5,filesize=10M
  -Djava.security.egd=file:/dev/./urandom     # 加速 SecureRandom
  -Dfile.encoding=UTF-8
  -Duser.timezone=Asia/Shanghai
"
```

#### 不同服务推荐配置

| 服务类型 | 堆内存 | GC | 说明 |
|---------|--------|-----|------|
| 网关/轻量服务 | 512m - 1g | G1 | 低延迟优先 |
| 业务服务 | 1g - 2g | G1 | 均衡 |
| 报表服务 | 2g - 4g | G1 | 大数据处理 |

### 7.2 连接池调优

#### HikariCP 配置

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20       # 最大连接数 = CPU核心数 * 2 + 有效磁盘数
      minimum-idle: 5             # 最小空闲连接
      connection-timeout: 30000   # 连接超时（ms）
      idle-timeout: 600000        # 空闲超时（ms）
      max-lifetime: 1800000       # 连接最大生命周期（ms）
      connection-test-query: "SELECT 1"
      leak-detection-threshold: 60000  # 连接泄漏检测阈值（ms）
```

#### Redis Lettuce 配置

```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 20          # 最大活跃连接
          max-idle: 10            # 最大空闲连接
          min-idle: 5             # 最小空闲连接
          max-wait: 3000ms        # 获取连接最大等待时间
      timeout: 5000ms             # 命令超时时间
```

### 7.3 缓存策略

#### 缓存层次设计

```
L1: 本地缓存（Caffeine）    → 热点数据，毫秒级
L2: 分布式缓存（Redis）     → 共享数据，亚秒级
L3: 数据库（PostgreSQL）    → 持久化数据
```

#### Caffeine 本地缓存配置

```java
@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, Object> localCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .expireAfterAccess(Duration.ofMinutes(5))
                .recordStats()
                .build();
    }
}
```

#### Redis 缓存策略

```java
@Service
@Slf4j
public class ProductCacheService {

    private static final String PRODUCT_CACHE_PREFIX = "product:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ProductRepository productRepository;

    /**
     * 获取商品信息（缓存优先）
     */
    public ProductResponse getProduct(Long id) {
        String cacheKey = PRODUCT_CACHE_PREFIX + id;

        // 1. 查询 Redis 缓存
        ProductResponse cached = (ProductResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("缓存命中: key={}", cacheKey);
            return cached;
        }

        // 2. 查询数据库
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("商品不存在: " + id));
        ProductResponse response = productMapper.toResponse(product);

        // 3. 写入缓存
        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL);
        log.debug("缓存写入: key={}, ttl={}", cacheKey, CACHE_TTL);

        return response;
    }

    /**
     * 使缓存失效
     */
    public void evictCache(Long id) {
        String cacheKey = PRODUCT_CACHE_PREFIX + id;
        redisTemplate.delete(cacheKey);
        log.debug("缓存失效: key={}", cacheKey);
    }
}
```

#### 缓存一致性策略

| 策略 | 适用场景 | 说明 |
|------|---------|------|
| Cache-Aside | 通用 | 读时写缓存，写时删缓存 |
| Write-Through | 数据一致性要求高 | 写操作同步更新缓存 |
| Write-Behind | 写入频繁，一致性要求低 | 异步批量写入数据库 |

### 7.4 慢 SQL 排查

#### 开启慢查询日志

```sql
-- PostgreSQL 慢查询配置
ALTER SYSTEM SET log_min_duration_statement = 500;  -- 超过 500ms 记录
ALTER SYSTEM SET log_statement = 'all';              -- 记录所有语句（调试用）
SELECT pg_reload_conf();
```

#### 常用排查命令

```sql
-- 查看当前活跃查询
SELECT pid, now() - pg_stat_activity.query_start AS duration, query, state
FROM pg_stat_activity
WHERE state = 'active' AND now() - pg_stat_activity.query_start > interval '1 second'
ORDER BY duration DESC;

-- 查看慢查询 Top 10
SELECT query, calls, total_exec_time, mean_exec_time, max_exec_time
FROM pg_stat_statements
ORDER BY mean_exec_time DESC
LIMIT 10;

-- 查看表统计信息
SELECT relname, n_live_tup, n_dead_tup, last_vacuum, last_autovacuum, last_analyze
FROM pg_stat_user_tables
ORDER BY n_dead_tup DESC;

-- 查看索引使用情况
SELECT schemaname, relname, indexrelname, idx_scan
FROM pg_stat_user_indexes
ORDER BY idx_scan ASC;
```

#### EXPLAIN ANALYZE 分析

```sql
-- 查看执行计划
EXPLAIN ANALYZE
SELECT p.*, c.name AS category_name
FROM products p
JOIN categories c ON p.category_id = c.id
WHERE p.status = 'ACTIVE' AND p.created_at > '2026-01-01'
ORDER BY p.created_at DESC
LIMIT 20;

-- 关注指标：
-- Seq Scan → 考虑添加索引
-- Nested Loop → 检查是否缺少连接条件索引
-- Sort → 考虑添加排序列索引
-- Filter → 检查 WHERE 条件是否使用索引
```

#### 常见慢 SQL 优化手段

| 问题 | 优化方式 |
|------|---------|
| 缺少索引 | 添加 B-Tree 索引（等值查询）或 GIN 索引（全文搜索） |
| 索引未命中 | 检查数据类型匹配、函数包装、OR 条件 |
| 全表扫描 | 添加合适的索引、限制返回列 |
| N+1 查询 | 使用 JOIN FETCH 或 `@EntityGraph` |
| 大数据量分页 | 使用 Keyset 分页替代 OFFSET |
| 连接池耗尽 | 排查连接泄漏、增大连接池 |

---

## 8. 技术债务清单

### 8.1 编译失败模块（6个）

以下模块当前存在编译问题，需优先修复：

| # | 模块 | 错误类型 | 严重程度 | 状态 |
|---|------|---------|---------|------|
| 1 | `sales-service` | MapStruct 生成代码与 Lombok 冲突 | 高 | 待修复 |
| 2 | `procurement-service` | 依赖循环引用 | 高 | 待修复 |
| 3 | `business-partner-service` | 缺少 common 模块依赖声明 | 中 | 待修复 |
| 4 | `finance-service` | JPA Entity 映射错误（@ManyToOne 目标不存在） | 高 | 待修复 |
| 5 | `report-service` | 第三方库版本冲突（JasperReports） | 中 | 待修复 |
| 6 | `admin-service` | Spring Security 配置类循环依赖 | 高 | 待修复 |

#### 修复方案

**sales-service（MapStruct + Lombok 冲突）**：

```kotlin
// build.gradle.kts 确保注解处理器顺序正确
dependencies {
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
}
```

**procurement-service（循环依赖）**：

```java
// 将 ProcurementClient 中的 SupplierService 依赖改为懒加载
@Lazy
@Autowired
private SupplierService supplierService;
```

**finance-service（Entity 映射错误）**：

```java
// 修正 @ManyToOne 目标实体
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "order_id")
private Order order;  // 确保使用正确的实体类而非 DTO
```

### 8.2 测试相关债务

| # | 问题描述 | 影响 | 建议措施 |
|---|---------|------|---------|
| 1 | 构建命令默认 `./gradlew build -x test` 跳过测试 | 无法发现回归缺陷 | 逐步修复失败测试，CI 管道不允许跳过 |
| 2 | 集成测试依赖本地基础设施 | CI 环境不稳定 | 引入 Testcontainers |
| 3 | 测试覆盖率低于 30% | 代码质量无法保障 | 优先覆盖核心业务逻辑 |
| 4 | 缺少契约测试 | 服务间 API 变更无感知 | 引入 Spring Cloud Contract |
| 5 | 前端无 E2E 测试 | UI 回归无法自动检测 | 引入 Playwright |

### 8.3 架构层面待优化项

| # | 问题描述 | 优先级 | 建议措施 |
|---|---------|--------|---------|
| 1 | 服务间调用使用 Feign 同步调用，存在级联故障风险 | P1 | 引入 Resilience4j 熔断/降级 |
| 2 | 分布式事务未统一处理 | P1 | 采用 Saga 模式或 Outbox Pattern |
| 3 | 缺少 API 版本管理策略 | P2 | URL 路径版本化 `/api/v1/` |
| 4 | 日志未统一格式和 TraceId | P2 | 引入 Micrometer Tracing + Zipkin |
| 5 | 缓存策略未统一，部分服务直接查库 | P2 | 统一缓存抽象层 |
| 6 | 缺少 API 限流策略 | P1 | Gateway 层集成 Redis 限流 |
| 7 | 数据库连接池参数未按服务负载调优 | P3 | 根据监控数据逐服务调优 |
| 8 | 前端状态管理不统一 | P3 | 迁移至 Zustand 统一管理 |

---

## 9. 贡献者指南

### 9.1 分支策略

#### 分支模型

```
main            ───●──────────────────────────●───────────
                     \                        /
develop          ─────●────●────●────●───────●────────────
                        \      \    /
feature/ISSUE-123 ──────●      \  /
feature/ISSUE-456 ─────────────●
                              \
hotfix/fix-login  ─────────────●──────────────────────────
```

#### 分支命名规范

| 分支类型 | 命名格式 | 示例 |
|---------|---------|------|
| 功能分支 | `feature/{ISSUE-ID}-{简述}` | `feature/INV-123-add-product-export` |
| 修复分支 | `bugfix/{ISSUE-ID}-{简述}` | `bugfix/INV-456-fix-order-total` |
| 热修复分支 | `hotfix/{简述}` | `hotfix/fix-auth-bypass` |
| 发布分支 | `release/{版本号}` | `release/3.1.0` |

#### 分支规则

- `main` 分支：生产代码，仅通过 PR 合入，禁止直接推送
- `develop` 分支：开发集成分支，每日自动部署到 staging
- 功能分支从 `develop` 创建，完成后合回 `develop`
- 热修复分支从 `main` 创建，合回 `main` 和 `develop`

### 9.2 PR 流程

#### 提交 PR 步骤

1. **创建功能分支**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/INV-123-add-product-export
   ```

2. **开发并提交**
   ```bash
   # 编码...
   git add .
   git commit -m "feat(product): 添加商品导出功能

   - 支持 Excel/CSV 格式导出
   - 支持按分类筛选导出
   - 最大导出 10000 条记录

   Closes #123"
   ```

3. **推送并创建 PR**
   ```bash
   git push origin feature/INV-123-add-product-export
   # 在 GitHub 上创建 Pull Request
   ```

4. **PR 模板**
   ```markdown
   ## 变更说明
   <!-- 简要描述本次变更内容 -->

   ## 变更类型
   - [ ] 新功能（feature）
   - [ ] 修复（bugfix）
   - [ ] 重构（refactor）
   - [ ] 文档（docs）
   - [ ] 性能优化（perf）

   ## 影响范围
   <!-- 列出受影响的服务/模块 -->

   ## 测试说明
   <!-- 如何验证本次变更 -->

   ## 检查清单
   - [ ] 代码通过 lint 检查
   - [ ] 单元测试通过
   - [ ] 新代码有对应的测试
   - [ ] API 变更已更新文档
   - [ ] 数据库迁移脚本已添加（如有）
   ```

5. **Code Review 后合入**

#### Commit Message 规范

```
<type>(<scope>): <subject>

<body>

<footer>
```

| type | 说明 |
|------|------|
| feat | 新功能 |
| fix | 修复 Bug |
| refactor | 重构（不改变行为） |
| perf | 性能优化 |
| docs | 文档 |
| test | 测试 |
| chore | 构建/工具变更 |
| ci | CI/CD 变更 |

### 9.3 Code Review 检查清单

#### 功能正确性

- [ ] 逻辑是否正确，边界条件是否覆盖
- [ ] 异常情况是否妥善处理
- [ ] 并发场景是否考虑（线程安全、锁）
- [ ] 空指针风险是否排除

#### 代码质量

- [ ] 命名是否清晰、符合规范
- [ ] 方法长度是否合理（建议不超过 30 行）
- [ ] 类职责是否单一
- [ ] 是否有重复代码可抽取
- [ ] Lombok/MapStruct 使用是否规范
- [ ] 日志级别是否恰当

#### 安全性

- [ ] 敏感数据是否脱敏
- [ ] SQL 注入风险是否排除
- [ ] 权限校验是否完整
- [ ] 输入校验是否充分

#### 性能

- [ ] 是否存在 N+1 查询
- [ ] 缓存使用是否合理
- [ ] 大数据量查询是否分页
- [ ] 慢 SQL 风险是否排除

#### 兼容性

- [ ] API 是否向后兼容
- [ ] 数据库迁移脚本是否向前兼容
- [ ] 配置变更是否向下兼容

---

## 10. 版本发布检查清单

### 10.1 发布前检查

#### 代码质量

- [ ] 所有 PR 已合并到发布分支
- [ ] Code Review 全部通过
- [ ] 无 P0/P1 级别未解决 Bug
- [ ] 代码静态分析无新增严重问题
- [ ] SonarQube 质量门通过

#### 测试验证

- [ ] 单元测试全部通过
- [ ] 集成测试全部通过
- [ ] Staging 环境端到端测试通过
- [ ] 性能测试达标（P99 < 3s，错误率 < 0.1%）
- [ ] 安全扫描无高危漏洞

#### 数据库

- [ ] Flyway 迁移脚本在 Staging 验证通过
- [ ] 迁移脚本向前兼容
- [ ] 回滚方案已准备

#### 配置

- [ ] Nacos 生产配置已更新
- [ ] 环境变量已确认
- [ ] CORS 白名单已更新
- [ ] 新增外部依赖已声明

#### 文档

- [ ] API 文档已更新（Swagger/OpenAPI）
- [ ] 变更日志（CHANGELOG.md）已更新
- [ ] 升级指南已编写
- [ ] 已知问题列表已更新

### 10.2 发布执行

```bash
# 1. 创建发布分支
git checkout main
git checkout -b release/3.1.0

# 2. 更新版本号
# 修改各服务 build.gradle.kts 中的 version
# 修改 frontend package.json 版本号

# 3. 更新 CHANGELOG.md

# 4. 提交版本变更
git commit -m "chore: bump version to 3.1.0"

# 5. 构建并推送镜像
cd microservices/
./gradlew build -x test
docker compose build
docker compose push

# 6. 部署到生产（滚动更新）
kubectl apply -k k8s/overlays/production/

# 7. 验证
# 执行健康检查脚本
# 验证核心业务流程

# 8. 打标签
git tag -a v3.1.0 -m "Release v3.1.0"
git push origin v3.1.0

# 9. 合并回 develop
git checkout develop
git merge release/3.1.0
git push origin develop

# 10. 删除发布分支
git branch -d release/3.1.0
git push origin --delete release/3.1.0
```

### 10.3 发布后验证

- [ ] 所有服务健康检查通过
- [ ] 核心业务流程验证（创建商品 → 下单 → 出库 → 付款）
- [ ] 前端页面正常加载
- [ ] 监控指标正常（错误率、响应时间、吞吐量）
- [ ] 日志无异常错误
- [ ] 数据一致性校验
- [ ] 通知相关方发布完成

### 10.4 紧急回滚

```bash
# Kubernetes 回滚到上一版本
kubectl rollout undo deployment/<service-name> -n inventory-system

# 回滚到指定版本
kubectl rollout undo deployment/<service-name> --to-revision=2 -n inventory-system

# 数据库回滚（如有迁移）
# 执行对应的 U（Undo）迁移脚本
./gradlew flywayUndo -Penv=prod
```

### 10.5 版本号规范

遵循语义化版本（SemVer）：

```
MAJOR.MINOR.PATCH

MAJOR: 不兼容的 API 变更
MINOR: 向后兼容的功能新增
PATCH: 向后兼容的问题修复

示例:
3.0.0 → 3.0.1  (Bug 修复)
3.0.1 → 3.1.0  (新功能)
3.1.0 → 4.0.0  (破坏性变更)
```

---

## 附录

### A. Gradle 常用命令速查

```bash
./gradlew build -x test           # 构建跳过测试
./gradlew build                   # 完整构建（含测试）
./gradlew clean build             # 清理后构建
./gradlew :product-service:build  # 构建指定服务
./gradlew test                    # 全量测试
./gradlew :product-service:test   # 指定服务测试
./gradlew projects                # 查看子项目列表
./gradlew dependencies            # 查看依赖树
./gradlew flywayInfo              # 查看数据库迁移状态
./gradlew flywayMigrate           # 执行数据库迁移
./gradlew bootRun                 # 启动服务（开发模式）
```

### B. 前端常用命令速查

```bash
npm install                        # 安装依赖
npm run dev                        # 启动开发服务器
npm run build                      # 生产构建
npm run lint                       # ESLint 检查
npm run lint:fix                   # ESLint 自动修复
npm test                           # 运行测试
npm run type-check                 # TypeScript 类型检查
```

### C. 关键依赖版本

| 依赖 | 版本 | 说明 |
|------|------|------|
| Java | 21 | LTS 版本 |
| Spring Boot | 3.4.4 | 框架核心 |
| Spring Cloud | 2024.0.2 | 微服务组件 |
| Spring Cloud Alibaba | 2023.0.1.0 | 阿里巴巴组件 |
| PostgreSQL Driver | 42.7.x | 数据库驱动 |
| MapStruct | 1.6.3 | 对象映射 |
| Lombok | 最新（由 Spring Boot BOM 管理） | 代码简化 |
| Gradle | 9.4.0 | 构建工具 |
| React | 19 | 前端框架 |
| TypeScript | 5.9 | 类型系统 |
| Vite | 8 | 前端构建 |
| MUI | 7 | UI 组件库 |

### D. 文档版本历史

| 版本 | 日期 | 修改内容 | 作者 |
|------|------|---------|------|
| 3.0 | 2026-06-08 | 初始版本，基于 v3.0 微服务架构 | 开发团队 |
