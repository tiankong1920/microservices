# 权限矩阵文档

## 一、角色权限对照表

### 1.1 用户管理权限

| 权限 | ADMIN | MANAGER | OPERATOR | USER | AUDITOR |
|------|:-----:|:-------:|:--------:|:----:|:-------:|
| USER_CREATE | ✅ | ❌ | ❌ | ❌ | ❌ |
| USER_READ | ✅ | ✅ | ❌ | ❌ | ✅ |
| USER_UPDATE | ✅ | ❌ | ❌ | ❌ | ❌ |
| USER_DELETE | ✅ | ❌ | ❌ | ❌ | ❌ |
| USER_LIST | ✅ | ✅ | ❌ | ❌ | ✅ |

### 1.2 角色管理权限

| 权限 | ADMIN | MANAGER | OPERATOR | USER | AUDITOR |
|------|:-----:|:-------:|:--------:|:----:|:-------:|
| ROLE_CREATE | ✅ | ❌ | ❌ | ❌ | ❌ |
| ROLE_READ | ✅ | ❌ | ❌ | ❌ | ✅ |
| ROLE_UPDATE | ✅ | ❌ | ❌ | ❌ | ❌ |
| ROLE_DELETE | ✅ | ❌ | ❌ | ❌ | ❌ |
| ROLE_LIST | ✅ | ❌ | ❌ | ❌ | ✅ |

### 1.3 权限管理权限

| 权限 | ADMIN | MANAGER | OPERATOR | USER | AUDITOR |
|------|:-----:|:-------:|:--------:|:----:|:-------:|
| PERMISSION_CREATE | ✅ | ❌ | ❌ | ❌ | ❌ |
| PERMISSION_READ | ✅ | ❌ | ❌ | ❌ | ✅ |
| PERMISSION_UPDATE | ✅ | ❌ | ❌ | ❌ | ❌ |
| PERMISSION_DELETE | ✅ | ❌ | ❌ | ❌ | ❌ |
| PERMISSION_LIST | ✅ | ❌ | ❌ | ❌ | ✅ |

### 1.4 库存管理权限

| 权限 | ADMIN | MANAGER | OPERATOR | USER | AUDITOR |
|------|:-----:|:-------:|:--------:|:----:|:-------:|
| INVENTORY_CREATE | ✅ | ✅ | ❌ | ❌ | ❌ |
| INVENTORY_READ | ✅ | ✅ | ✅ | ✅ | ❌ |
| INVENTORY_UPDATE | ✅ | ✅ | ✅ | ❌ | ❌ |
| INVENTORY_DELETE | ✅ | ✅ | ❌ | ❌ | ❌ |
| INVENTORY_LIST | ✅ | ✅ | ✅ | ✅ | ❌ |

### 1.5 订单管理权限

| 权限 | ADMIN | MANAGER | OPERATOR | USER | AUDITOR |
|------|:-----:|:-------:|:--------:|:----:|:-------:|
| ORDER_CREATE | ✅ | ✅ | ✅ | ❌ | ❌ |
| ORDER_READ | ✅ | ✅ | ✅ | ✅ | ❌ |
| ORDER_UPDATE | ✅ | ✅ | ✅ | ❌ | ❌ |
| ORDER_DELETE | ✅ | ✅ | ❌ | ❌ | ❌ |
| ORDER_LIST | ✅ | ✅ | ✅ | ✅ | ❌ |
| ORDER_APPROVE | ✅ | ✅ | ❌ | ❌ | ❌ |

### 1.6 产品管理权限

| 权限 | ADMIN | MANAGER | OPERATOR | USER | AUDITOR |
|------|:-----:|:-------:|:--------:|:----:|:-------:|
| PRODUCT_CREATE | ✅ | ✅ | ❌ | ❌ | ❌ |
| PRODUCT_READ | ✅ | ✅ | ✅ | ✅ | ❌ |
| PRODUCT_UPDATE | ✅ | ✅ | ❌ | ❌ | ❌ |
| PRODUCT_DELETE | ✅ | ✅ | ❌ | ❌ | ❌ |
| PRODUCT_LIST | ✅ | ✅ | ✅ | ✅ | ❌ |

### 1.7 销售管理权限

| 权限 | ADMIN | MANAGER | OPERATOR | USER | AUDITOR |
|------|:-----:|:-------:|:--------:|:----:|:-------:|
| SALES_CREATE | ✅ | ✅ | ✅ | ❌ | ❌ |
| SALES_READ | ✅ | ✅ | ✅ | ✅ | ❌ |
| SALES_UPDATE | ✅ | ✅ | ✅ | ❌ | ❌ |
| SALES_DELETE | ✅ | ✅ | ❌ | ❌ | ❌ |
| SALES_LIST | ✅ | ✅ | ✅ | ✅ | ❌ |

### 1.8 客户管理权限

| 权限 | ADMIN | MANAGER | OPERATOR | USER | AUDITOR |
|------|:-----:|:-------:|:--------:|:----:|:-------:|
| CUSTOMER_CREATE | ✅ | ✅ | ❌ | ❌ | ❌ |
| CUSTOMER_READ | ✅ | ✅ | ✅ | ✅ | ❌ |
| CUSTOMER_UPDATE | ✅ | ✅ | ❌ | ❌ | ❌ |
| CUSTOMER_DELETE | ✅ | ✅ | ❌ | ❌ | ❌ |
| CUSTOMER_LIST | ✅ | ✅ | ✅ | ✅ | ❌ |

### 1.9 供应商管理权限

| 权限 | ADMIN | MANAGER | OPERATOR | USER | AUDITOR |
|------|:-----:|:-------:|:--------:|:----:|:-------:|
| SUPPLIER_CREATE | ✅ | ✅ | ❌ | ❌ | ❌ |
| SUPPLIER_READ | ✅ | ✅ | ✅ | ✅ | ❌ |
| SUPPLIER_UPDATE | ✅ | ✅ | ❌ | ❌ | ❌ |
| SUPPLIER_DELETE | ✅ | ✅ | ❌ | ❌ | ❌ |
| SUPPLIER_LIST | ✅ | ✅ | ✅ | ✅ | ❌ |

### 1.10 报表权限

| 权限 | ADMIN | MANAGER | OPERATOR | USER | AUDITOR |
|------|:-----:|:-------:|:--------:|:----:|:-------:|
| REPORT_VIEW | ✅ | ✅ | ✅ | ✅ | ❌ |
| REPORT_EXPORT | ✅ | ✅ | ❌ | ❌ | ❌ |
| REPORT_GENERATE | ✅ | ✅ | ❌ | ❌ | ❌ |

### 1.11 系统管理权限

| 权限 | ADMIN | MANAGER | OPERATOR | USER | AUDITOR |
|------|:-----:|:-------:|:--------:|:----:|:-------:|
| SYSTEM_CONFIG | ✅ | ❌ | ❌ | ❌ | ❌ |
| SYSTEM_MONITOR | ✅ | ❌ | ❌ | ❌ | ✅ |
| SYSTEM_BACKUP | ✅ | ❌ | ❌ | ❌ | ❌ |
| SYSTEM_AUDIT | ✅ | ❌ | ❌ | ❌ | ✅ |

## 二、资源-操作-权限映射

### 2.1 权限命名规范

权限名称遵循 `{资源}_{操作}` 格式：

```
{RESOURCE}_{ACTION}

例如：
- USER_CREATE    → 创建用户
- INVENTORY_READ → 查看库存
- ORDER_DELETE   → 删除订单
```

### 2.2 操作类型定义

| 操作 | 说明 | HTTP方法映射 |
|------|------|-------------|
| CREATE | 创建资源 | POST |
| READ | 查看单个资源 | GET |
| UPDATE | 更新资源 | PUT/PATCH |
| DELETE | 删除资源 | DELETE |
| LIST | 列表查询 | GET |
| APPROVE | 审批操作 | POST |
| EXPORT | 导出数据 | GET |
| MANAGE | 管理操作 | 多种 |

## 三、权限检查流程

```
1. 用户请求访问资源
2. 系统获取用户认证信息
3. 加载用户角色和权限
4. 检查资源访问权限
   - URL级别：SecurityFilterChain
   - 方法级别：@PreAuthorize/@RequirePermission
5. 允许/拒绝访问
6. 记录审计日志
```

## 四、动态权限管理

### 4.1 权限缓存

权限信息使用Redis缓存，缓存键格式：
- 用户权限：`userPermissions:{username}`
- 用户角色：`userPermissions:{username}_roles`

### 4.2 权限刷新

权限变更后自动清除相关缓存：
- 用户角色变更：清除该用户缓存
- 角色权限变更：清除所有拥有该角色的用户缓存

## 五、权限使用示例

### 5.1 Controller层

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @RequirePermission("USER_CREATE")
    @PostMapping
    public User createUser(@RequestBody UserDto dto) { ... }

    @RequirePermission("USER_READ")
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) { ... }

    @RequirePermission("USER_LIST")
    @GetMapping
    public Page<User> listUsers(Pageable pageable) { ... }
}
```

### 5.2 Service层

```java
@Service
public class OrderService {

    @PreAuthorize("hasPermission('order', 'approve')")
    public void approveOrder(Long orderId) { ... }

    @PreAuthorize("hasRole('ADMIN') or hasPermission('order', 'delete')")
    public void deleteOrder(Long orderId) { ... }
}
```

---

**版本**: 1.0.0  
**最后更新**: 2026-02-14  
**维护责任人**: 安全团队
