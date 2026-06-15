# 微服务开发规范

## 1. 概述

本文档定义了进销存管理系统微服务架构的开发规范和标准，旨在确保团队开发的一致性、可维护性和可扩展性。

## 2. 微服务设计原则

### 2.1 单一职责原则
每个微服务应专注于一个明确的业务领域，具有清晰的边界和职责。

### 2.2 高内聚低耦合
服务内部功能高度相关，服务间通过明确定义的接口进行通信。

### 2.3 数据去中心化
每个微服务管理自己的数据存储，避免共享数据库。

### 2.4 容错性设计
微服务应具备故障隔离和优雅降级能力。

## 3. 服务拆分规范

### 3.1 业务领域划分
根据业务功能将系统拆分为以下核心服务：

1. **用户服务** (User Service)
   - 用户认证、授权
   - 用户信息管理
   - 角色权限管理

2. **产品服务** (Product Service)
   - 产品信息管理
   - 产品分类管理
   - 产品库存查询

3. **采购服务** (Purchase Service)
   - 采购订单管理
   - 供应商管理
   - 采购流程处理

4. **销售服务** (Sales Service)
   - 销售订单管理
   - 客户管理
   - 销售流程处理

5. **库存服务** (Inventory Service)
   - 库存管理
   - 入库出库操作
   - 库存盘点

6. **财务服务** (Finance Service)
   - 财务结算
   - 收付款管理
   - 财务报表

### 3.2 服务粒度控制
- 避免过度拆分导致服务间频繁通信
- 避免过大服务导致职责不清
- 单个服务代码行数控制在50,000行以内

## 4. API设计规范

### 4.1 RESTful API设计原则

#### 4.1.1 资源命名
- 使用名词复数形式表示资源集合
- 使用连字符分隔多词资源名
- 资源名使用小写字母

```http
GET /api/users
GET /api/products
GET /api/purchase-orders
```

#### 4.1.2 HTTP动词使用
- GET: 查询资源
- POST: 创建资源
- PUT: 更新整个资源
- PATCH: 部分更新资源
- DELETE: 删除资源

#### 4.1.3 状态码规范
```http
200 OK - 请求成功
201 Created - 资源创建成功
204 No Content - 请求成功但无返回内容
400 Bad Request - 请求参数错误
401 Unauthorized - 未授权
403 Forbidden - 禁止访问
404 Not Found - 资源不存在
409 Conflict - 资源冲突
500 Internal Server Error - 服务器内部错误
```

### 4.2 API版本管理
```http
GET /api/v1/users
GET /api/v2/users
```

### 4.3 请求响应格式

#### 4.3.1 请求格式
```json
{
  "name": "张三",
  "email": "zhangsan@example.com",
  "department": "销售部"
}
```

#### 4.3.2 响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "张三",
    "email": "zhangsan@example.com",
    "department": "销售部",
    "createTime": "2023-01-01T12:00:00Z"
  },
  "timestamp": "2023-01-01T12:00:00Z"
}
```

## 5. 代码开发规范

### 5.1 包结构规范

```
com.inventory.{service-name}
├── config/          # 配置类
├── controller/      # 控制器层
├── dto/            # 数据传输对象
├── entity/         # 实体类
├── repository/     # 数据访问层
├── service/        # 业务逻辑层
├── exception/      # 异常处理
├── util/           # 工具类
└── Application.java # 启动类
```

### 5.2 命名规范

#### 5.2.1 类命名
- 控制器类以 Controller 结尾
- 服务类以 Service 结尾
- 数据访问类以 Repository 结尾
- 实体类使用业务名词

```java
// 控制器类
public class UserController { }

// 服务类
public class UserService { }

// 数据访问类
public interface UserRepository { }

// 实体类
public class User { }
```

#### 5.2.2 方法命名
- 查询方法以 find/get/query 开头
- 创建方法以 create/save/add 开头
- 更新方法以 update/modify/edit 开头
- 删除方法以 delete/remove 开头

```java
public User findUserById(Long id);
public User saveUser(User user);
public void updateUser(User user);
public void deleteUserById(Long id);
```

### 5.3 异常处理规范

#### 5.3.1 自定义异常类
```java
public class BusinessException extends RuntimeException {
    private final int code;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
    
    // getter方法
}
```

#### 5.3.2 全局异常处理器
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }
}
```

### 5.4 日志规范

#### 5.4.1 日志级别使用
```java
@Slf4j
@Service
public class UserService {
    
    public User findUserById(Long id) {
        log.debug("查询用户，ID: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            
        log.info("用户查询成功，用户ID: {}", user.getId());
        return user;
    }
    
    public User saveUser(User user) {
        log.info("保存用户，用户名: {}", user.getUsername());
        
        try {
            User savedUser = userRepository.save(user);
            log.info("用户保存成功，用户ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            log.error("用户保存失败，用户名: {}", user.getUsername(), e);
            throw new BusinessException(ErrorCode.USER_SAVE_FAILED);
        }
    }
}
```

## 6. 数据库设计规范

### 6.1 表命名规范
- 表名使用小写字母和下划线
- 表名使用复数形式
- 表名前缀表示业务领域

```sql
-- 用户表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 产品表
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 6.2 字段命名规范
- 字段名使用小写字母和下划线
- 避免使用数据库关键字
- 字段名具有明确含义

### 6.3 索引规范
- 主键自动创建索引
- 外键字段创建索引
- 经常查询的字段创建索引
- 复合索引字段顺序遵循最左匹配原则

## 7. 安全规范

### 7.1 认证授权
- 使用 JWT 进行无状态认证
- 实现 RBAC 基于角色的访问控制
- 敏感操作需要二次验证

### 7.2 数据安全
- 敏感数据加密存储
- 数据传输使用 HTTPS
- SQL 注入防护

### 7.3 输入验证
```java
@Data
public class CreateUserRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    private String username;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, message = "密码长度不能少于8位")
    private String password;
}
```

## 8. 测试规范

### 8.1 单元测试
- 每个业务方法都应有对应的单元测试
- 测试覆盖率要求达到80%以上
- 使用 JUnit 5 和 Mockito

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void shouldFindUserById() {
        // Given
        Long userId = 1L;
        User expectedUser = new User();
        expectedUser.setId(userId);
        expectedUser.setUsername("testuser");
        
        when(userRepository.findById(userId))
            .thenReturn(Optional.of(expectedUser));
        
        // When
        User actualUser = userService.findUserById(userId);
        
        // Then
        assertThat(actualUser).isEqualTo(expectedUser);
        verify(userRepository).findById(userId);
    }
}
```

### 8.2 集成测试
- 测试服务间调用
- 测试数据库操作
- 使用 @SpringBootTest 注解

### 8.3 接口测试
- 使用 Postman 或 Swagger 进行接口测试
- 编写自动化接口测试脚本
- 测试各种边界条件和异常情况

## 9. 部署规范

### 9.1 Docker化部署
每个微服务应提供 Dockerfile：

```dockerfile
FROM openjdk:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### 9.2 Kubernetes部署
提供 Kubernetes 部署配置：

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: user-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
```

### 9.3 环境配置
- 使用配置中心管理环境配置
- 通过环境变量传递敏感信息
- 支持蓝绿部署和灰度发布

## 10. 监控规范

### 10.1 健康检查
- 实现 `/actuator/health` 健康检查端点
- 自定义健康检查指标
- 集成外部监控系统

### 10.2 性能监控
- 集成 Micrometer 和 Prometheus
- 监控 JVM 性能指标
- 监控业务指标

### 10.3 日志收集
- 统一日志格式
- 集中日志收集
- 日志分析和告警

## 11. 文档规范

### 11.1 API文档
- 使用 Swagger 生成 API 文档
- 提供详细的接口说明
- 包含请求示例和响应示例

### 11.2 技术文档
- 每个微服务提供 README.md
- 包含服务架构说明
- 包含部署和运维指南

### 11.3 业务文档
- 提供业务流程图
- 说明业务规则和约束
- 包含数据字典

## 12. 代码审查规范

### 12.1 审查清单
- 代码是否符合规范
- 是否有充分的测试
- 是否有安全漏洞
- 性能是否合理

### 12.2 审查流程
1. 开发人员提交代码
2. 至少一名同事进行代码审查
3. 修复审查发现的问题
4. 合并到主分支

## 13. 持续集成/持续部署(CI/CD)

### 13.1 构建流程
- 自动化代码构建
- 自动化测试执行
- 自动化部署到测试环境

### 13.2 部署流程
- 支持一键部署
- 支持回滚操作
- 部署过程可视化

## 14. 总结

本规范为微服务开发提供了全面的指导原则和标准，团队成员应严格遵守这些规范，确保系统的高质量和可维护性。随着项目的发展，这些规范也应不断更新和完善。