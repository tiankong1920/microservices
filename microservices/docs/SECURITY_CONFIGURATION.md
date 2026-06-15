# Spring Security 企业级安全配置文档

## 一、概述

本文档详细描述了库存管理系统的 Spring Security 安全配置，包括认证、授权、会话管理、密码策略等企业级安全特性。

## 二、安全架构

### 2.1 架构图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              客户端请求                                   │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           API Gateway (网关层)                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐ │
│  │  IP Filter   │→ │ Rate Limiter │→ │ JWT Validate │→ │ Route Filter│ │
│  └──────────────┘  └──────────────┘  └──────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        Auth Service (认证服务)                           │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    Spring Security Filter Chain                  │   │
│  │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────────────┐ │   │
│  │  │ CORS   │→│Session │→│ Authn  │→│ Authz  │→│ Exception      │ │   │
│  │  │ Filter │ │ Mgmt   │ │ Filter │ │ Filter │ │ Handler        │ │   │
│  │  └────────┘ └────────┘ └────────┘ └────────┘ └────────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                    │                                    │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                         核心服务层                                 │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │  │
│  │  │ JWT Service │  │ MFA Service │  │ Permission Service      │  │  │
│  │  └─────────────┘  └─────────────┘  └─────────────────────────┘  │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │  │
│  │  │Audit Service│  │Lockout Svc  │  │ Blacklist Service       │  │  │
│  │  └─────────────┘  └─────────────┘  └─────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2.2 核心组件

| 组件 | 说明 | 文件路径 |
|------|------|----------|
| UnifiedSecurityConfig | 统一安全配置 | `config/UnifiedSecurityConfig.java` |
| PasswordPolicyValidator | 密码策略验证 | `security/PasswordPolicyValidator.java` |
| JwtBlacklistService | JWT黑名单服务 | `service/JwtBlacklistService.java` |
| AccountLockoutService | 账户锁定服务 | `service/AccountLockoutService.java` |
| DynamicPermissionService | 动态权限服务 | `service/DynamicPermissionService.java` |
| SecurityAuditService | 安全审计服务 | `service/SecurityAuditService.java` |

## 三、认证配置

### 3.1 登录认证流程

```
1. 用户提交用户名/密码
2. 系统验证凭据
3. 检查账户状态（锁定、禁用、过期）
4. 检查MFA是否启用
5. 生成JWT令牌
6. 记录审计日志
7. 返回认证结果
```

### 3.2 密码策略

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 最小长度 | 8 | 密码最少字符数 |
| 最大长度 | 128 | 密码最多字符数 |
| 大写字母 | 必须 | 至少包含一个大写字母 |
| 小写字母 | 必须 | 至少包含一个小写字母 |
| 数字 | 必须 | 至少包含一个数字 |
| 特殊字符 | 必须 | 至少包含一个特殊字符 |
| BCrypt强度 | 12 | 加密算法工作因子 |
| 密码历史 | 5 | 禁止重复使用最近5次密码 |

### 3.3 账户锁定策略

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 最大失败次数 | 5 | 连续失败登录次数阈值 |
| 锁定时长 | 30分钟 | 账户锁定持续时间 |
| 尝试窗口 | 15分钟 | 失败次数统计时间窗口 |

### 3.4 JWT令牌配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 访问令牌有效期 | 1小时 | Access Token 有效期 |
| 刷新令牌有效期 | 7天 | Refresh Token 有效期 |
| 记住我有效期 | 30天 | Remember-me 令牌有效期 |
| 签名算法 | RS256 | 非对称加密算法 |

## 四、授权配置

### 4.1 RBAC权限模型

```
User (用户) ←→ UserRole ←→ Role (角色) ←→ RolePermission ←→ Permission (权限)
```

### 4.2 预定义角色

| 角色 | 说明 | 权限范围 |
|------|------|----------|
| ADMIN | 系统管理员 | 全部权限 |
| MANAGER | 业务经理 | 业务操作+报表+审批 |
| OPERATOR | 操作员 | 日常业务操作 |
| USER | 普通用户 | 只读访问 |
| AUDITOR | 审计员 | 审计日志查看 |

### 4.3 方法级权限控制

使用注解进行方法级权限控制：

```java
// 要求用户拥有指定权限
@RequirePermission("USER_CREATE")
public User createUser(UserDto userDto) { ... }

// 要求用户拥有任一权限
@RequirePermission(value = {"ORDER_READ", "ORDER_LIST"}, mode = RequireMode.ANY)
public List<Order> getOrders() { ... }

// 要求用户拥有指定角色
@RequireRole("ADMIN")
public void deleteUser(Long userId) { ... }

// 使用Spring Security注解
@PreAuthorize("hasRole('ADMIN') or hasPermission('user', 'delete')")
public void deleteUser(Long userId) { ... }
```

### 4.4 URL级权限控制

| 路径模式 | 权限要求 | 说明 |
|----------|----------|------|
| `/auth/login` | 公开 | 登录接口 |
| `/auth/register` | 公开 | 注册接口 |
| `/api/admin/**` | ROLE_ADMIN | 管理接口 |
| `/api/manager/**` | ROLE_ADMIN, ROLE_MANAGER | 经理接口 |
| `/api/operator/**` | ROLE_ADMIN, ROLE_MANAGER, ROLE_OPERATOR | 操作员接口 |
| `/api/user/**` | 已认证 | 用户接口 |
| `/actuator/health` | 公开 | 健康检查 |

## 五、会话管理

### 5.1 会话配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| 会话策略 | IF_REQUIRED | 按需创建会话 |
| 最大并发会话 | 3 | 单用户最大会话数 |
| 会话超时 | 30分钟 | 会话过期时间 |
| 会话固定防护 | migrateSession | 登录后迁移会话ID |

### 5.2 会话事件处理

- 登录成功：创建会话，记录审计日志
- 登录失败：记录失败次数，可能触发锁定
- 注销：清除会话，使JWT失效，清除Cookie

## 六、安全防护

### 6.1 HTTP安全头

| 头部 | 值 | 说明 |
|------|-----|------|
| Content-Security-Policy | default-src 'self' | 内容安全策略 |
| X-XSS-Protection | 1; mode=block | XSS防护 |
| X-Content-Type-Options | nosniff | MIME类型嗅探防护 |
| Strict-Transport-Security | max-age=31536000 | HSTS强制HTTPS |
| X-Frame-Options | DENY | 点击劫持防护 |

### 6.2 CORS配置

```yaml
security:
  cors:
    allowed-origins: http://localhost:3000,http://localhost:8080
    allowed-methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
    allowed-headers: "*"
    allow-credentials: true
    max-age: 3600
```

### 6.3 限流策略

| 端点 | 限制 | 说明 |
|------|------|------|
| 登录接口 | 10次/分钟/IP | 防止暴力破解 |
| API接口 | 100次/分钟/用户 | 防止滥用 |
| 敏感操作 | 5次/分钟/用户 | 关键操作保护 |

## 七、审计日志

### 7.1 审计事件类型

| 事件类型 | 说明 |
|----------|------|
| LOGIN_SUCCESS | 登录成功 |
| LOGIN_FAILURE | 登录失败 |
| LOGOUT | 注销 |
| PASSWORD_CHANGE | 密码修改 |
| ACCOUNT_LOCKOUT | 账户锁定 |
| ACCOUNT_UNLOCK | 账户解锁 |
| PERMISSION_CHANGE | 权限变更 |
| MFA_ENABLE | MFA启用 |
| MFA_DISABLE | MFA禁用 |

### 7.2 审计日志字段

| 字段 | 说明 |
|------|------|
| timestamp | 事件时间戳 |
| eventType | 事件类型 |
| username | 用户名 |
| ipAddress | IP地址 |
| userAgent | 用户代理 |
| description | 事件描述 |
| success | 操作是否成功 |
| sessionId | 会话ID |

## 八、配置参数

### 8.1 application.yml 配置

```yaml
security:
  # 密码策略
  password:
    min-length: 8
    max-length: 128
    require-uppercase: true
    require-lowercase: true
    require-digit: true
    require-special-char: true
    special-chars: "!@#$%^&*()_+-=[]{};':\"\\|,.<>/?"
    history-count: 5

  # 账户锁定
  lockout:
    max-attempts: 5
    lock-duration-minutes: 30
    attempts-window-minutes: 15

  # CORS配置
  cors:
    allowed-origins: http://localhost:3000,http://localhost:8080
    allowed-methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
    allowed-headers: "*"
    allow-credentials: true
    max-age: 3600

jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: 3600000
  refresh-expiration: 604800000
```

## 九、最佳实践

### 9.1 安全开发规范

1. **永远不要在代码中硬编码密钥**
2. **使用环境变量存储敏感配置**
3. **所有API端点默认拒绝访问，显式开放**
4. **敏感操作记录审计日志**
5. **使用参数化查询防止SQL注入**
6. **对用户输入进行验证和清理**

### 9.2 密钥管理

- 使用环境变量或密钥管理服务
- 定期轮换密钥
- 不同环境使用不同密钥
- 密钥泄露后立即更换

### 9.3 安全检查清单

- [ ] 所有端点都有适当的权限控制
- [ ] 敏感数据传输使用HTTPS
- [ ] 密码使用BCrypt加密存储
- [ ] JWT令牌有合理的过期时间
- [ ] 审计日志完整记录关键操作
- [ ] 错误信息不泄露敏感信息
- [ ] 输入验证完整有效

---

**版本**: 1.0.0  
**最后更新**: 2026-02-14  
**维护责任人**: 安全团队
