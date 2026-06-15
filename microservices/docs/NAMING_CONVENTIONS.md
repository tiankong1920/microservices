# 项目命名规范标准

## 1. 概述

本文档定义了 Inventory Management System 项目的统一命名规范，确保代码与数据库命名风格的一致性、可读性和可维护性。

---

## 2. 数据库对象命名规范

### 2.1 基本规则

所有数据库表名、字段名、索引名、视图名、存储过程名等数据库相关对象必须严格采用**蛇形命名法（snake_case）**。

### 2.2 命名规则

| 规则 | 说明 |
|------|------|
| 字符集 | 仅使用小写英文字母（a-z）和下划线（_） |
| 分隔符 | 单词之间通过单个下划线分隔 |
| 禁止项 | 禁止使用大写字母、数字开头或特殊字符 |

### 2.3 命名示例

#### 表名
```
✅ 正确示例：
user_info
order_detail
product_category
inventory_stock
payment_transaction

❌ 错误示例：
UserInfo
user-info
userInfo
1_user_info
```

#### 字段名
```
✅ 正确示例：
user_name
created_at
is_active
total_amount
order_status

❌ 错误示例：
userName
createdAt
IsActive
```

#### 索引名
```
✅ 正确示例：
idx_user_name
idx_order_date
idx_product_sku
pk_user_id          -- 主键索引
fk_order_user       -- 外键索引
uq_user_email       -- 唯一索引

❌ 错误示例：
idxUserName
IDX_user_name
index_user_name
```

#### 视图名
```
✅ 正确示例：
v_user_order_summary
v_product_inventory
v_monthly_sales_report

❌ 错误示例：
vUserOrderSummary
V_User_Order_Summary
```

#### 存储过程名
```
✅ 正确示例：
sp_calculate_order_total
sp_update_inventory_stock
sp_generate_monthly_report

❌ 错误示例：
spCalculateOrderTotal
SP_calculate_order_total
```

### 2.4 数据库对象前缀规范

| 对象类型 | 前缀 | 示例 |
|----------|------|------|
| 表 | 无 | `user_info`, `order_detail` |
| 视图 | `v_` | `v_user_summary` |
| 索引 | `idx_` | `idx_user_name` |
| 主键 | `pk_` | `pk_user_id` |
| 外键 | `fk_` | `fk_order_user` |
| 唯一约束 | `uq_` | `uq_user_email` |
| 存储过程 | `sp_` | `sp_calculate_total` |
| 函数 | `fn_` | `fn_format_date` |
| 触发器 | `tr_` | `tr_update_timestamp` |
| 序列 | `seq_` | `seq_order_id` |

---

## 3. 代码元素命名规范

### 3.1 小驼峰命名法（camelCase）

适用于：变量名、函数名、方法名、参数名。

#### 命名规则

| 规则 | 说明 |
|------|------|
| 首字母 | 小写 |
| 后续单词 | 每个单词首字母大写 |
| 禁止项 | 禁止使用下划线或其他特殊字符 |

#### 示例

```java
// ✅ 正确示例
String userName;
int totalAmount;
boolean isActive;

public String getUserName() { }
public void calculateTotalAmount() { }
public void updateOrderStatus(Long orderId, String status) { }

// ❌ 错误示例
String user_name;
int TotalAmount;
boolean is_active;
public String get_user_name() { }
```

### 3.2 大驼峰命名法（PascalCase）

适用于：类名、枚举名、注解名。

#### 命名规则

| 规则 | 说明 |
|------|------|
| 所有单词 | 每个单词首字母均大写 |
| 禁止项 | 禁止使用下划线或其他特殊字符 |

#### 示例

```java
// ✅ 正确示例
public class UserInfo { }
public enum OrderStatus { }
public @interface AuditLog { }

// ❌ 错误示例
public class user_info { }
public enum order_status { }
```

### 3.3 接口命名规范

适用于：所有接口（Repository、Service 等）。

#### 命名规则

| 规则 | 说明 |
|------|------|
| 前缀 | 使用 `I` 前缀 |
| 格式 | I + PascalCase |

#### 示例

```java
// ✅ 正确示例
public interface IProductRepository extends JpaRepository<Product, Long> { }
public interface IProductService { }
public interface IOrderRepository { }

// ❌ 错误示例
public interface ProductRepository { }  // 缺少 I 前缀
public interface ProductService { }     // 缺少 I 前缀
```

### 3.4 常量命名

常量使用**全大写蛇形命名法（SCREAMING_SNAKE_CASE）**。

```java
// ✅ 正确示例
public static final int MAX_RETRY_COUNT = 3;
public static final String DEFAULT_CHARSET = "UTF-8";
public static final long CACHE_EXPIRE_SECONDS = 3600;

// ❌ 错误示例
public static final int maxRetryCount = 3;
public static final String defaultCharset = "UTF-8";
```

### 3.4 包命名

包名使用**全小写**，单词之间用点分隔。

```java
// ✅ 正确示例
package com.inventory.user.service;
package com.inventory.order.repository;
package com.inventory.common.util;

// ❌ 错误示例
package com.inventory.UserService;
package com.inventory.user_service;
```

### 3.5 Java 特定命名规范

#### 类成员命名

| 类型 | 规范 | 示例 |
|------|------|------|
| 实体类 | 名词，无后缀 | `User`, `Order`, `Product` |
| 服务接口 | I + 名词 + Service | `IUserService`, `IOrderService` |
| 仓库接口 | I + 名词 + Repository | `IUserRepository`, `IOrderRepository` |
| 控制器类 | 名词 + Controller | `UserController`, `OrderController` |
| DTO 类 | 名词 + DTO/Request/Response | `UserDTO`, `CreateOrderRequest` |
| 异常类 | 名词 + Exception | `UserNotFoundException` |
| 工具类 | 名词 + Utils/Helper | `DateUtils`, `StringHelper` |

#### 布尔类型命名

布尔变量/方法应以 `is`、`has`、`can`、`should`、`will` 等开头：

```java
// ✅ 正确示例
private boolean isActive;
private boolean hasPermission;
public boolean canDelete() { }
public boolean shouldRetry() { }

// ❌ 错误示例
private boolean active;
private boolean permission;
public boolean delete() { }  // 返回布尔值但命名不明确
```

#### 集合类型命名

集合类型应使用复数形式：

```java
// ✅ 正确示例
List<User> users;
Set<String> orderIds;
Map<Long, Order> orderMap;

// ❌ 错误示例
List<User> userList;
Set<String> orderIdSet;
Map<Long, Order> orderMapping;
```

---

## 4. 配置文件命名规范

### 4.1 YAML/Properties 文件

```
✅ 正确示例：
application.yml
application-dev.yml
application-prod.yml
application-redis.yml
bootstrap.yml

❌ 错误示例：
Application.yml
application_dev.yml
application.dev.yml
```

### 4.2 XML 文件

```
✅ 正确示例：
spring-context.xml
mybatis-config.xml
logback-spring.xml

❌ 错误示例：
SpringContext.xml
spring_context.xml
```

---

## 5. 代码检查工具配置

### 5.1 Checkstyle 配置

项目使用 Checkstyle 进行命名规范检查，配置文件位于 `config/checkstyle/checkstyle.xml`。

检查规则包括：
- `TypeName`：类/接口/枚举命名检查（PascalCase）
- `MethodName`：方法命名检查（camelCase）
- `ParameterName`：参数命名检查（camelCase）
- `LocalVariableName`：局部变量命名检查（camelCase）
- `MemberName`：成员变量命名检查（camelCase）
- `ConstantName`：常量命名检查（SCREAMING_SNAKE_CASE）
- `PackageName`：包命名检查（全小写）

### 5.2 PMD 配置

PMD 配置文件位于 `config/pmd/pmd.xml`，包含命名规范相关规则：
- `LongVariable`：变量名长度检查
- `ShortVariable`：变量名长度检查
- `ShortMethodName`：方法名长度检查

### 5.3 运行检查

```bash
# 运行 Checkstyle 检查
./gradlew checkstyleMain checkstyleTest

# 运行 PMD 检查
./gradlew pmdMain pmdTest

# 运行所有质量检查
./gradlew checkQuality
```

---

## 6. 例外处理机制

### 6.1 第三方库/框架例外

对于第三方库、框架强制要求的命名方式，可作为例外处理，但需满足以下条件：

1. **文档说明**：在代码注释中明确说明例外原因
2. **最小范围**：例外仅限于必要的最小范围
3. **团队共识**：例外需经过团队评审确认

```java
// 示例：Spring Boot 要求的特殊命名
// @ConfigurationProperties 需要匹配配置文件中的属性名
@ConfigurationProperties(prefix = "app.datasource")  // 配置文件使用 kebab-case
public class DataSourceProperties {
    // 例外说明：Spring Boot 自动绑定要求
}
```

### 6.2 历史遗留系统例外

历史遗留系统迁移时，可制定阶段性整改计划：

| 阶段 | 内容 | 时间 |
|------|------|------|
| 第一阶段 | 新开发部分严格遵守规范 | 立即执行 |
| 第二阶段 | 高频修改模块重构 | 1-3 个月 |
| 第三阶段 | 低频修改模块重构 | 3-6 个月 |
| 第四阶段 | 全面审计和清理 | 6-12 个月 |

---

## 7. 代码审查检查清单

在代码审查流程中加入以下命名规范检查环节：

### 7.1 数据库对象检查

- [ ] 表名是否使用 snake_case
- [ ] 字段名是否使用 snake_case
- [ ] 索引名是否使用正确前缀
- [ ] 外键命名是否规范

### 7.2 Java 代码检查

- [ ] 类名是否使用 PascalCase
- [ ] 方法名是否使用 camelCase
- [ ] 变量名是否使用 camelCase
- [ ] 常量名是否使用 SCREAMING_SNAKE_CASE
- [ ] 包名是否全小写
- [ ] 布尔变量命名是否语义明确
- [ ] 集合变量是否使用复数形式

### 7.3 配置文件检查

- [ ] 配置文件命名是否规范
- [ ] 配置属性命名是否使用 kebab-case

---

## 8. 常见问题与解决方案

### 8.1 接口命名问题

**问题**: 接口未使用 `I` 前缀（如 `ProductService`）

**规范**: 接口必须使用 `I` 前缀

```java
// ✅ 正确示例
public interface IProductService { }
public interface IProductRepository { }

// ❌ 错误示例
public interface ProductService { }  // 缺少 I 前缀
public interface ProductRepository { }
```

**处理建议**: 所有接口必须添加 I 前缀，新增接口必须遵守规范

### 8.2 拼写错误检查

常见拼写错误及正确写法：

| 错误拼写 | 正确拼写 | 说明 |
|----------|----------|------|
| Recieve | Receive | 接收 |
| Aquire | Acquire | 获取 |
| Seperate | Separate | 分离 |
| Definately | Definitely | 确定 |
| Occured | Occurred | 发生 |

### 8.3 短变量名处理

**问题**: 变量名过短（如 `id`）

**规范**: 对于常见的短变量名，业界普遍接受以下例外：

```java
// ✅ 可接受的短变量名
Long id;           // 实体ID
int i, j, k;       // 循环变量
String x, y;       // 坐标变量
T e;               // 泛型元素
```

**处理建议**: PMD 的 ShortVariable 规则可配置最小长度，建议设置为 2 字符

### 8.4 业务缩写类名

**问题**: 类名过短（如 `BOM`）

**规范**: 对于行业标准缩写，可作为例外处理：

```java
// ✅ 可接受的缩写类名
public class BOM { }        // Bill of Materials - 物料清单
public class SKU { }        // Stock Keeping Unit - 库存单位
public class DTO { }        // Data Transfer Object
public class API { }        // Application Programming Interface
```

**处理建议**: 缩写必须为业界公认的标准术语，并在类注释中说明全称
| Adress | Address | 地址 |
| Contoller | Controller | 控制器 |
| Repositry | Repository | 仓库 |
| Entitiy | Entity | 实体 |
| Excpetion | Exception | 异常 |
| processer | processor | 处理器 |
| mananger | manager | 管理器 |

### 8.5 保留字冲突处理

避免使用 Java 保留字和常用类名作为变量名：

```java
// ❌ 错误示例
String string = "value";
int int = 10;
List list = new ArrayList();
Class class = obj.getClass();

// ✅ 正确示例
String stringValue = "value";
int intValue = 10;
List<String> stringList = new ArrayList<>();
Class<?> objectClass = obj.getClass();
```

### 8.6 命名长度规范

| 标识符类型 | 最小长度 | 最大长度 | 建议 |
|------------|----------|----------|------|
| 变量名 | 2 字符 | 20 字符 | 循环变量 `i` 可例外 |
| 方法名 | 2 字符 | 25 字符 | 动词开头 |
| 类名 | 2 字符 | 30 字符 | 名词 |
| 常量名 | 2 字符 | 40 字符 | 全大写 |
| 包名 | 1 字符 | - | 全小写 |

**例外情况**:
- 循环变量可使用单字符（如 `i`, `j`, `k`）
- 泛型参数可使用单字符（如 `T`, `E`, `K`, `V`）
- 坐标变量可使用单字符（如 `x`, `y`, `z`）

---

## 9. 版本历史

| 版本 | 日期 | 修改内容 | 作者 |
|------|------|----------|------|
| 1.3.0 | 2026-03-21 | 更新接口命名规范：接口必须使用 I 前缀 | Team |
| 1.2.0 | 2026-03-21 | 完成接口 I 前缀修复，更新常见问题章节 | Team |
| 1.1.0 | 2026-03-20 | 新增常见问题与解决方案章节 | Team |
| 1.0.0 | 2026-03-20 | 初始版本 | Team |

---

## 10. 参考资料

- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Alibaba Java Coding Guidelines](https://github.com/alibaba/p3c)
- [Spring Framework Code Style](https://github.com/spring-projects/spring-framework/wiki/Code-Style)
