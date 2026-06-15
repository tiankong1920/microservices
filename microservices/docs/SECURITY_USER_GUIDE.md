# 安全功能使用指南

## 一、用户认证

### 1.1 登录

**请求**
```http
POST /auth/login
Content-Type: application/json

{
    "username": "your_username",
    "password": "YourSecure@123"
}
```

**成功响应**
```json
{
    "success": true,
    "message": "Login successful",
    "data": {
        "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
        "refreshToken": "eyJhbGciOiJSUzI1NiJ9...",
        "tokenType": "Bearer",
        "expiresIn": 3600,
        "username": "your_username",
        "roles": ["USER", "OPERATOR"]
    }
}
```

**失败响应**
```json
{
    "success": false,
    "message": "Invalid username or password. 3 attempts remaining.",
    "errorCode": "INVALID_CREDENTIALS"
}
```

### 1.2 使用令牌访问API

```http
GET /api/inventory
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
```

### 1.3 刷新令牌

```http
POST /auth/refresh
Content-Type: application/json

{
    "refreshToken": "eyJhbGciOiJSUzI1NiJ9..."
}
```

### 1.4 注销

```http
POST /auth/logout
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9...
```

## 二、密码管理

### 2.1 密码要求

密码必须满足以下条件：
- 至少8个字符
- 包含大写字母 (A-Z)
- 包含小写字母 (a-z)
- 包含数字 (0-9)
- 包含特殊字符 (!@#$%^&*等)
- 不能包含连续字符 (如abc, 123)
- 不能包含重复字符 (如aaa, 111)
- 不能包含常见弱密码 (如password, admin)

### 2.2 修改密码

```http
POST /auth/change-password
Authorization: Bearer {token}
Content-Type: application/json

{
    "currentPassword": "Current@123",
    "newPassword": "NewSecure@456",
    "confirmPassword": "NewSecure@456"
}
```

### 2.3 忘记密码

1. 请求重置链接：
```http
POST /auth/forgot-password
Content-Type: application/json

{
    "email": "user@example.com"
}
```

2. 使用重置令牌：
```http
POST /auth/reset-password
Content-Type: application/json

{
    "token": "reset-token-from-email",
    "newPassword": "NewSecure@789"
}
```

## 三、多因素认证 (MFA)

### 3.1 启用MFA

1. 获取MFA设置信息：
```http
POST /auth/mfa/setup
Authorization: Bearer {token}
```

响应：
```json
{
    "success": true,
    "data": {
        "secret": "JBSWY3DPEHPK3PXP",
        "qrCodeUrl": "otpauth://totp/Inventory:user@example.com?secret=JBSWY3DPEHPK3PXP&issuer=Inventory",
        "recoveryCode": "ABCD-EFGH-IJKL-MNOP"
    }
}
```

2. 验证并启用：
```http
POST /auth/mfa/verify
Authorization: Bearer {token}
Content-Type: application/json

{
    "code": "123456"
}
```

### 3.2 MFA登录

1. 正常登录后，如果MFA启用，会收到：
```json
{
    "success": true,
    "data": {
        "mfaRequired": true,
        "mfaSessionId": "session-uuid"
    }
}
```

2. 提交MFA验证码：
```http
POST /auth/mfa/validate
Content-Type: application/json

{
    "mfaSessionId": "session-uuid",
    "code": "123456"
}
```

### 3.3 禁用MFA

```http
DELETE /auth/mfa
Authorization: Bearer {token}
Content-Type: application/json

{
    "password": "YourPassword@123"
}
```

## 四、账户安全

### 4.1 账户锁定

连续5次登录失败后，账户将被锁定30分钟。

**解锁方式：**
1. 等待30分钟自动解锁
2. 联系管理员手动解锁

### 4.2 查看登录历史

```http
GET /auth/security/history
Authorization: Bearer {token}
```

### 4.3 活跃会话管理

```http
GET /auth/sessions
Authorization: Bearer {token}
```

撤销其他会话：
```http
DELETE /auth/sessions/{sessionId}
Authorization: Bearer {token}
```

## 五、管理员操作

### 5.1 用户管理

**创建用户**
```http
POST /api/admin/users
Authorization: Bearer {admin-token}
Content-Type: application/json

{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "SecurePass@123",
    "roles": ["USER", "OPERATOR"]
}
```

**解锁用户**
```http
POST /api/admin/users/{userId}/unlock
Authorization: Bearer {admin-token}
```

**重置用户密码**
```http
POST /api/admin/users/{userId}/reset-password
Authorization: Bearer {admin-token}
Content-Type: application/json

{
    "newPassword": "NewSecure@123"
}
```

### 5.2 角色管理

**分配角色**
```http
POST /api/admin/users/{userId}/roles
Authorization: Bearer {admin-token}
Content-Type: application/json

{
    "roles": ["MANAGER"]
}
```

**移除角色**
```http
DELETE /api/admin/users/{userId}/roles/{roleName}
Authorization: Bearer {admin-token}
```

### 5.3 审计日志查询

```http
GET /api/admin/audit-logs?page=0&size=20&eventType=LOGIN_FAILURE
Authorization: Bearer {admin-token}
```

## 六、常见问题

### 6.1 登录失败

| 错误代码 | 原因 | 解决方案 |
|----------|------|----------|
| INVALID_CREDENTIALS | 用户名或密码错误 | 检查输入，注意大小写 |
| ACCOUNT_LOCKED | 账户被锁定 | 等待解锁或联系管理员 |
| ACCOUNT_DISABLED | 账户被禁用 | 联系管理员 |
| MFA_REQUIRED | 需要MFA验证 | 使用认证器应用获取验证码 |

### 6.2 令牌过期

访问令牌过期后，使用刷新令牌获取新令牌：
```http
POST /auth/refresh
Content-Type: application/json

{
    "refreshToken": "{refresh-token}"
}
```

### 6.3 权限不足

如果收到403错误，表示当前用户没有所需权限。请联系管理员申请相应权限。

## 七、安全建议

### 7.1 密码安全
- 使用强密码，避免使用个人信息
- 定期更换密码
- 不要在多个网站使用相同密码
- 使用密码管理器

### 7.2 账户安全
- 启用多因素认证
- 定期检查登录历史
- 发现异常立即修改密码
- 不要共享账户

### 7.3 令牌安全
- 不要分享访问令牌
- 令牌存储在安全位置
- 使用HTTPS传输
- 及时注销不活跃会话

---

**版本**: 1.0.0  
**最后更新**: 2026-02-14  
**维护责任人**: 安全团队
