# 进销存管理系统 v3.0 — 系统管理员手册

> **版本**：3.0
> **日期**：2026-06-08
> **适用对象**：系统管理员、运维工程师、IT主管
> **密级**：内部公开

---

## 目录

1. [系统管理员职责与权限说明](#1-系统管理员职责与权限说明)
2. [系统初始化配置清单](#2-系统初始化配置清单)
3. [服务启停管理](#3-服务启停管理)
4. [用户与权限管理](#4-用户与权限管理)
5. [数据备份与恢复](#5-数据备份与恢复)
6. [监控与告警配置](#6-监控与告警配置)
7. [安全策略管理](#7-安全策略管理)
8. [常见故障排查](#8-常见故障排查)
9. [系统升级与维护流程](#9-系统升级与维护流程)
10. [应急预案](#10-应急预案)

---

## 1. 系统管理员职责与权限说明

### 1.1 角色定义

| 角色 | 职责范围 | 系统权限 |
|------|---------|---------|
| **超级管理员** | 全系统运维管理、安全策略制定、用户权限分配 | 全部模块读写、系统配置、用户管理、审计日志 |
| **运维管理员** | 服务部署、监控告警、备份恢复、故障处理 | 服务启停、监控面板、日志查看、备份操作 |
| **安全管理员** | 安全策略执行、密钥管理、安全审计 | 安全配置、密钥轮换、审计日志、CORS策略 |
| **业务管理员** | 业务数据管理、报表审核、业务参数配置 | 业务模块读写、报表查看、参数配置 |

### 1.2 最小权限原则

- 超级管理员账户日常应使用普通权限角色，仅在必要时切换至超级管理员角色
- 安全管理员与运维管理员角色应分属不同人员，实现权限分离
- 所有管理员操作均记录审计日志，日志保留期不少于 180 天
- 管理员账户必须启用 MFA（多因素认证），密码有效期不超过 90 天

### 1.3 管理员操作规范

1. 所有生产环境变更必须通过工单系统审批
2. 紧急变更需事后 24 小时内补交变更记录
3. 禁止在生产环境直接执行 DDL 操作
4. 定期（每月）审查管理员账户及权限分配

---

## 2. 系统初始化配置清单

### 2.1 环境变量配置

以下环境变量必须在系统启动前完成配置：

#### 核心环境变量

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `POSTGRES_PASSWORD` | 是 | `1234`（开发环境） | PostgreSQL 数据库密码，生产环境必须修改 |
| `POSTGRES_HOST` | 否 | `localhost` | PostgreSQL 主机地址 |
| `POSTGRES_PORT` | 否 | `5432` | PostgreSQL 端口 |
| `POSTGRES_DB` | 否 | `inventory_system` | 默认数据库名 |
| `JWT_SECRET` | 是 | — | JWT 签名密钥，至少 256 位 |
| `JWT_EXPIRATION` | 否 | `86400000` | Token 过期时间（毫秒），默认 24 小时 |
| `CORS_ALLOWED_ORIGINS` | 是 | — | CORS 允许的来源地址，多个用逗号分隔 |
| `NACOS_SERVER_ADDR` | 否 | `localhost:8848` | Nacos 服务地址 |
| `NACOS_NAMESPACE` | 否 | `public` | Nacos 命名空间 |
| `REDIS_HOST` | 否 | `localhost` | Redis 主机地址 |
| `REDIS_PORT` | 否 | `6379` | Redis 端口 |
| `REDIS_PASSWORD` | 否 | — | Redis 密码 |
| `KAFKA_BOOTSTRAP_SERVERS` | 否 | `localhost:9092` | Kafka Broker 地址 |
| `SPRING_PROFILES_ACTIVE` | 否 | `dev` | Spring 激活配置文件（dev/staging/prod） |

#### 安全相关环境变量

| 变量名 | 必填 | 说明 |
|--------|------|------|
| `BCRYPT_ROUNDS` | 否 | BCrypt 加密轮数，默认 12，生产环境建议 14 |
| `MFA_ENABLED` | 否 | 是否启用 MFA，默认 `true` |
| `LOGIN_MAX_ATTEMPTS` | 否 | 最大登录失败次数，默认 5 |
| `LOGIN_LOCK_DURATION` | 否 | 账户锁定时长（分钟），默认 30 |
| `API_RATE_LIMIT` | 否 | API 限流阈值（次/分钟），默认 100 |

### 2.2 数据库初始化

#### PostgreSQL 初始化步骤

```bash
# 1. 创建数据库和用户
psql -U postgres -c "CREATE USER inventory_admin WITH PASSWORD '${POSTGRES_PASSWORD}';"
psql -U postgres -c "CREATE DATABASE inventory_system OWNER inventory_admin;"

# 2. 授权
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE inventory_system TO inventory_admin;"

# 3. 执行 Flyway 迁移（各服务启动时自动执行）
# 如需手动执行：
cd microservices/
./gradlew flywayMigrate -Penv=prod
```

#### 各服务数据库说明

| 服务 | 数据库 | 说明 |
|------|--------|------|
| product-service | `inv_product` | 商品主数据 |
| order-service | `inv_order` | 订单数据 |
| inventory-service | `inv_inventory` | 库存数据 |
| sales-service | `inv_sales` | 销售数据 |
| procurement-service | `inv_procurement` | 采购数据 |
| customer-service | `inv_customer` | 客户数据 |
| supplier-service | `inv_supplier` | 供应商数据 |
| business-partner-service | `inv_partner` | 业务伙伴数据 |
| finance-service | `inv_finance` | 财务数据 |
| auth-service | `inv_auth` | 认证授权数据 |
| admin-service | `inv_admin` | 管理配置数据 |
| report-service | `inv_report` | 报表数据 |

### 2.3 Nacos 配置

#### 命名空间规划

| 命名空间 ID | 名称 | 用途 |
|-------------|------|------|
| `dev` | 开发环境 | 开发调试 |
| `staging` | 预发布环境 | 集成测试 |
| `prod` | 生产环境 | 正式运行 |

#### 必须配置的 Nacos 配置项

1. **公共配置**（`shared-application.yml`）
   ```yaml
   spring:
     datasource:
       driver-class-name: org.postgresql.Driver
       hikari:
         maximum-pool-size: 20
         minimum-idle: 5
         connection-timeout: 30000
         idle-timeout: 600000
         max-lifetime: 1800000
     jpa:
       hibernate:
         ddl-auto: validate
       show-sql: false
     redis:
       timeout: 5000
       lettuce:
         pool:
           max-active: 20
           max-idle: 10
           min-idle: 5
   ```

2. **网关配置**（`gateway-service.yml`）
   ```yaml
   spring:
     cloud:
       gateway:
         routes:
           - id: product-service
             uri: lb://product-service
             predicates:
               - Path=/api/products/**
           - id: order-service
             uri: lb://order-service
             predicates:
               - Path=/api/orders/**
           - id: inventory-service
             uri: lb://inventory-service
             predicates:
               - Path=/api/inventory/**
           - id: sales-service
             uri: lb://sales-service
             predicates:
               - Path=/api/sales/**
           - id: procurement-service
             uri: lb://procurement-service
             predicates:
               - Path=/api/procurement/**
           - id: customer-service
             uri: lb://customer-service
             predicates:
               - Path=/api/customers/**
           - id: supplier-service
             uri: lb://supplier-service
             predicates:
               - Path=/api/suppliers/**
           - id: auth-service
             uri: lb://auth-service
             predicates:
               - Path=/api/auth/**
   ```

3. **安全配置**（`shared-security.yml`）
   ```yaml
   security:
     jwt:
       secret: ${JWT_SECRET}
       expiration: ${JWT_EXPIRATION:86400000}
     bcrypt:
       rounds: ${BCRYPT_ROUNDS:12}
     mfa:
       enabled: ${MFA_ENABLED:true}
     cors:
       allowed-origins: ${CORS_ALLOWED_ORIGINS}
       allowed-methods: GET,POST,PUT,DELETE,PATCH
       allowed-headers: "*"
       allow-credentials: true
   ```

### 2.4 Kafka Topic 初始化

```bash
# 创建必需的 Topic
kafka-topics.sh --create --topic order-events --partitions 3 --replication-factor 2 --bootstrap-server localhost:9092
kafka-topics.sh --create --topic inventory-events --partitions 3 --replication-factor 2 --bootstrap-server localhost:9092
kafka-topics.sh --create --topic payment-events --partitions 3 --replication-factor 2 --bootstrap-server localhost:9092
kafka-topics.sh --create --topic notification-events --partitions 3 --replication-factor 2 --bootstrap-server localhost:9092
kafka-topics.sh --create --topic audit-events --partitions 3 --replication-factor 2 --bootstrap-server localhost:9092
```

### 2.5 初始化验证清单

- [ ] PostgreSQL 各服务数据库创建成功
- [ ] Nacos 配置中心可访问（`http://<host>:8848/nacos`）
- [ ] Redis 连接正常（`redis-cli ping` 返回 `PONG`）
- [ ] Kafka Broker 可达（`kafka-broker-api-versions --bootstrap-server localhost:9092`）
- [ ] 所有环境变量已正确设置
- [ ] Flyway 迁移脚本执行成功
- [ ] CORS 白名单包含前端域名
- [ ] JWT_SECRET 已设置且满足强度要求

---

## 3. 服务启停管理

### 3.1 服务端口映射

| 服务名称 | 端口 | 依赖服务 | 启动优先级 |
|---------|------|---------|-----------|
| Nacos | 8848 | PostgreSQL | P0（最先启动） |
| config-service | 8888 | Nacos | P0 |
| registry-service | 8761 | Nacos | P1 |
| auth-service | 8093 | PostgreSQL, Redis, Nacos | P1 |
| gateway-service | 8080 | Nacos, auth-service | P2 |
| product-service | 8081 | PostgreSQL, Redis, Nacos | P2 |
| order-service | 8082 | PostgreSQL, Redis, Kafka, Nacos | P2 |
| inventory-service | 8083 | PostgreSQL, Redis, Kafka, Nacos | P2 |
| sales-service | 8084 | PostgreSQL, Redis, Kafka, Nacos | P2 |
| procurement-service | 8085 | PostgreSQL, Redis, Kafka, Nacos | P2 |
| customer-service | 8086 | PostgreSQL, Redis, Nacos | P2 |
| supplier-service | 8087 | PostgreSQL, Redis, Nacos | P2 |
| business-partner-service | 8088 | PostgreSQL, Redis, Nacos | P2 |
| admin-service | 8091 | PostgreSQL, Redis, Nacos | P2 |
| finance-service | 8092 | PostgreSQL, Redis, Kafka, Nacos | P2 |
| report-service | 8094 | PostgreSQL, Redis, Nacos | P2 |

### 3.2 启动顺序

```
阶段1（基础设施）：PostgreSQL → Redis → Kafka → Nacos
阶段2（配置注册）：config-service → registry-service
阶段3（认证授权）：auth-service
阶段4（网关路由）：gateway-service
阶段5（业务服务）：product → inventory → customer → supplier → business-partner → order → sales → procurement → finance → report
阶段6（管理服务）：admin-service
```

#### Docker Compose 一键启动

```bash
# 启动所有服务（按依赖顺序）
docker compose up -d

# 查看服务状态
docker compose ps

# 查看启动日志
docker compose logs -f <service-name>
```

#### 手动逐服务启动

```bash
# 阶段1：基础设施
docker compose up -d postgres redis kafka nacos

# 等待基础设施就绪（约 30 秒）
sleep 30

# 阶段2：配置与注册
docker compose up -d config-service registry-service
sleep 15

# 阶段3：认证
docker compose up -d auth-service
sleep 10

# 阶段4：网关
docker compose up -d gateway-service
sleep 10

# 阶段5：业务服务
docker compose up -d product-service inventory-service customer-service \
  supplier-service business-partner-service
sleep 15

docker compose up -d order-service sales-service procurement-service \
  finance-service report-service
sleep 15

# 阶段6：管理
docker compose up -d admin-service
```

### 3.3 健康检查

#### 各服务健康检查端点

| 端点 | 说明 |
|------|------|
| `GET http://<host>:<port>/actuator/health` | Spring Boot Actuator 健康状态 |
| `GET http://<host>:<port>/actuator/info` | 服务信息 |
| `GET http://<host>:<port>/actuator/ready` | 就绪探针（Kubernetes） |
| `GET http://<host>:<port>/actuator/live` | 存活探针（Kubernetes） |

#### 健康检查脚本

```bash
#!/bin/bash
# health-check.sh — 全服务健康检查

declare -A SERVICES=(
  ["gateway"]="8080"
  ["product"]="8081"
  ["order"]="8082"
  ["inventory"]="8083"
  ["sales"]="8084"
  ["procurement"]="8085"
  ["customer"]="8086"
  ["supplier"]="8087"
  ["business-partner"]="8088"
  ["admin"]="8091"
  ["finance"]="8092"
  ["auth"]="8093"
  ["report"]="8094"
)

HOST="localhost"
ALL_OK=true

for svc in "${!SERVICES[@]}"; do
  port="${SERVICES[$svc]}"
  status=$(curl -s -o /dev/null -w "%{http_code}" "http://${HOST}:${port}/actuator/health" 2>/dev/null)
  if [ "$status" = "200" ]; then
    echo "✅ ${svc} (${port}) — 正常"
  else
    echo "❌ ${svc} (${port}) — 异常 (HTTP ${status})"
    ALL_OK=false
  fi
done

if [ "$ALL_OK" = true ]; then
  echo -e "\n✅ 所有服务运行正常"
  exit 0
else
  echo -e "\n❌ 存在异常服务，请排查"
  exit 1
fi
```

### 3.4 重启策略

#### Docker Compose 重启策略

在 `docker-compose.yml` 中配置：

```yaml
services:
  product-service:
    restart: unless-stopped
    deploy:
      restart_policy:
        condition: on-failure
        delay: 5s
        max_attempts: 3
        window: 120s
```

#### Kubernetes 重启策略

```yaml
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      containers:
        - name: product-service
          livenessProbe:
            httpGet:
              path: /actuator/live
              port: 8081
            initialDelaySeconds: 60
            periodSeconds: 15
            failureThreshold: 3
          readinessProbe:
            httpGet:
              path: /actuator/ready
              port: 8081
            initialDelaySeconds: 30
            periodSeconds: 10
            failureThreshold: 3
```

#### 单服务重启

```bash
# Docker Compose
docker compose restart <service-name>

# Kubernetes
kubectl rollout restart deployment/<service-name> -n inventory-system

# 查看重启状态
kubectl rollout status deployment/<service-name> -n inventory-system
```

### 3.5 优雅停机

```bash
# Docker Compose 优雅停机（等待 30 秒）
docker compose stop -t 30 <service-name>

# Kubernetes 优雅停机（Pod terminationGracePeriodSeconds: 30）
kubectl delete pod <pod-name> -n inventory-system
```

Spring Boot 配置确保优雅停机：

```yaml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

---

## 4. 用户与权限管理

### 4.1 角色定义（RBAC）

系统采用基于角色的访问控制（RBAC），预定义角色如下：

| 角色 | 角色代码 | 说明 | 可访问模块 |
|------|---------|------|-----------|
| 超级管理员 | `ROLE_SUPER_ADMIN` | 系统最高权限 | 全部模块 |
| 系统管理员 | `ROLE_ADMIN` | 系统配置与用户管理 | admin, auth, config |
| 仓库管理员 | `ROLE_WAREHOUSE_ADMIN` | 仓库与库存管理 | inventory, product |
| 销售经理 | `ROLE_SALES_MANAGER` | 销售管理 | sales, order, customer |
| 采购经理 | `ROLE_PROCUREMENT_MANAGER` | 采购管理 | procurement, supplier |
| 财务人员 | `ROLE_FINANCE_STAFF` | 财务管理 | finance, report |
| 普通用户 | `ROLE_USER` | 基础数据查看 | product（只读）, order（只读） |
| 审计员 | `ROLE_AUDITOR` | 审计查看 | audit-log（只读）, report（只读） |

### 4.2 权限分配操作

#### 创建新用户

1. 登录管理后台（`http://<host>:8091`）
2. 进入 **系统管理 → 用户管理**
3. 点击 **新增用户**
4. 填写用户信息：
   - 用户名（唯一，3-50 字符）
   - 邮箱（唯一，用于 MFA）
   - 手机号（用于 MFA 和通知）
   - 初始密码（临时密码，首次登录强制修改）
   - 分配角色（可多选）
5. 点击 **保存**
6. 系统自动发送激活邮件至用户邮箱

#### 修改用户角色

1. 进入 **系统管理 → 用户管理**
2. 搜索目标用户
3. 点击 **编辑**
4. 在角色列表中勾选/取消角色
5. 点击 **保存**，权限即时生效

#### 禁用/启用用户

1. 进入 **系统管理 → 用户管理**
2. 搜索目标用户
3. 点击 **禁用**/**启用** 按钮
4. 确认操作
5. 禁用后该用户所有会话立即失效

### 4.3 密码策略配置

密码策略在 Nacos `shared-security.yml` 中配置：

```yaml
security:
  password-policy:
    min-length: 12              # 最小长度
    max-length: 128             # 最大长度
    require-uppercase: true     # 必须包含大写字母
    require-lowercase: true     # 必须包含小写字母
    require-digit: true         # 必须包含数字
    require-special: true       # 必须包含特殊字符
    special-chars: "!@#$%^&*()_+-=[]{}|;:',.<>?/"
    expiration-days: 90         # 密码有效期（天）
    history-count: 5            # 密码历史记录数（不允许重复最近N个）
    lockout-attempts: 5         # 锁定前允许的失败次数
    lockout-duration-minutes: 30 # 锁定时长（分钟）
```

### 4.4 MFA 配置

系统支持基于 TOTP（Time-Based One-Time Password）的多因素认证：

1. 用户登录后，进入 **个人设置 → 安全设置**
2. 点击 **启用 MFA**
3. 使用 Google Authenticator / Microsoft Authenticator 扫描二维码
4. 输入 6 位验证码完成绑定
5. 后续登录需输入用户名、密码和 MFA 验证码

管理员可强制指定角色启用 MFA：

```yaml
security:
  mfa:
    enabled: true
    forced-roles:
      - ROLE_SUPER_ADMIN
      - ROLE_ADMIN
      - ROLE_FINANCE_STAFF
```

---

## 5. 数据备份与恢复

### 5.1 PostgreSQL 备份策略

#### 自动备份（推荐 Cron 方式）

| 备份类型 | 频率 | 保留期 | 存储位置 |
|---------|------|--------|---------|
| 全量备份 | 每日凌晨 2:00 | 30 天 | `/backup/postgres/full/` |
| 增量备份 | 每 4 小时 | 7 天 | `/backup/postgres/incremental/` |
| WAL 归档 | 实时 | 7 天 | `/backup/postgres/wal/` |

#### 全量备份脚本

```bash
#!/bin/bash
# pg-backup-full.sh — PostgreSQL 全量备份

BACKUP_DIR="/backup/postgres/full"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=30

# 创建备份目录
mkdir -p ${BACKUP_DIR}

# 全量备份所有服务数据库
DATABASES=("inv_product" "inv_order" "inv_inventory" "inv_sales" \
           "inv_procurement" "inv_customer" "inv_supplier" "inv_partner" \
           "inv_finance" "inv_auth" "inv_admin" "inv_report")

for DB in "${DATABASES[@]}"; do
  echo "备份 ${DB}..."
  pg_dump -U inventory_admin -Fc "${DB}" > "${BACKUP_DIR}/${DB}_${DATE}.dump"
  
  if [ $? -eq 0 ]; then
    echo "✅ ${DB} 备份成功"
  else
    echo "❌ ${DB} 备份失败"
    # 发送告警通知
    curl -X POST "${ALERT_WEBHOOK}" -H "Content-Type: application/json" \
      -d "{\"text\": \"PostgreSQL备份失败: ${DB}\"}"
  fi
done

# 清理过期备份
find ${BACKUP_DIR} -name "*.dump" -mtime +${RETENTION_DAYS} -delete

echo "备份完成: ${DATE}"
```

#### 配置 WAL 归档（postgresql.conf）

```
wal_level = replica
archive_mode = on
archive_command = 'cp %p /backup/postgres/wal/%f'
archive_timeout = 300
```

### 5.2 Redis 持久化

#### RDB 快照配置（redis.conf）

```
save 900 1        # 15分钟内有1次修改则快照
save 300 10       # 5分钟内有10次修改则快照
save 60 10000     # 1分钟内有10000次修改则快照
rdbcompression yes
rdbchecksum yes
dbfilename dump.rdb
dir /data/redis
```

#### AOF 持久化配置（redis.conf）

```
appendonly yes
appendfilename "appendonly.aof"
appendfsync everysec
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb
```

#### Redis 备份脚本

```bash
#!/bin/bash
# redis-backup.sh — Redis 数据备份

BACKUP_DIR="/backup/redis"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p ${BACKUP_DIR}

# 触发 BGSAVE
redis-cli BGSAVE

# 等待 BGSAVE 完成
while [ "$(redis-cli LASTSAVE)" = "${LASTSAVE}" ]; do
  sleep 1
done

# 复制 RDB 文件
cp /data/redis/dump.rdb "${BACKUP_DIR}/dump_${DATE}.rdb"

# 复制 AOF 文件
cp /data/redis/appendonly.aof "${BACKUP_DIR}/appendonly_${DATE}.aof"

# 清理 7 天前的备份
find ${BACKUP_DIR} -mtime +7 -delete

echo "Redis 备份完成: ${DATE}"
```

### 5.3 数据恢复步骤

#### PostgreSQL 恢复

```bash
# 1. 停止相关服务
docker compose stop product-service order-service inventory-service

# 2. 恢复指定数据库
pg_restore -U inventory_admin -d inv_product -c \
  /backup/postgres/full/inv_product_20260608_020000.dump

# 3. 验证数据完整性
psql -U inventory_admin -d inv_product -c "SELECT count(*) FROM product;"

# 4. 重启服务
docker compose start product-service

# 5. 检查服务健康
curl http://localhost:8081/actuator/health
```

#### 时间点恢复（PITR）

```bash
# 1. 停止 PostgreSQL
docker compose stop postgres

# 2. 清空数据目录
rm -rf /data/postgres/*

# 3. 恢复基础备份
pg_restore -U inventory_admin -d inv_product \
  /backup/postgres/full/inv_product_20260608_020000.dump

# 4. 配置恢复目标时间
cat >> /data/postgres/recovery.signal <<EOF
restore_command = 'cp /backup/postgres/wal/%f %p'
recovery_target_time = '2026-06-08 14:30:00'
recovery_target_action = 'promote'
EOF

# 5. 启动 PostgreSQL
docker compose start postgres

# 6. 验证恢复结果后重启业务服务
```

#### Redis 恢复

```bash
# 1. 停止 Redis
docker compose stop redis

# 2. 替换数据文件
cp /backup/redis/dump_20260608_020000.rdb /data/redis/dump.rdb

# 3. 如使用 AOF，也需恢复
cp /backup/redis/appendonly_20260608_020000.aof /data/redis/appendonly.aof

# 4. 启动 Redis
docker compose start redis

# 5. 验证
redis-cli ping
redis-cli info keyspace
```

### 5.4 备份验证

建议每周执行一次备份恢复演练：

```bash
# 恢复到临时数据库验证
pg_restore -U inventory_admin -d inv_product_verify \
  /backup/postgres/full/inv_product_20260608_020000.dump

# 数据量校验
psql -U inventory_admin -d inv_product_verify -c \
  "SELECT schemaname, relname, n_live_tup FROM pg_stat_user_tables;"

# 清理验证库
dropdb inv_product_verify
```

---

## 6. 监控与告警配置

### 6.1 Prometheus 接入

#### Spring Boot Actuator 端点配置

各服务 `application.yml` 中开启 Prometheus 端点：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
```

#### Prometheus 配置（prometheus.yml）

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'inventory-system'
    metrics_path: '/actuator/prometheus'
    consul_sd_configs:
      - server: 'localhost:8500'
        services: []

    static_configs:
      - targets:
          - 'localhost:8080'   # gateway
          - 'localhost:8081'   # product
          - 'localhost:8082'   # order
          - 'localhost:8083'   # inventory
          - 'localhost:8084'   # sales
          - 'localhost:8085'   # procurement
          - 'localhost:8086'   # customer
          - 'localhost:8087'   # supplier
          - 'localhost:8088'   # business-partner
          - 'localhost:8091'   # admin
          - 'localhost:8092'   # finance
          - 'localhost:8093'   # auth
          - 'localhost:8094'   # report
```

### 6.2 Grafana 仪表盘

#### 推荐仪表盘

| 仪表盘名称 | Grafana ID | 监控内容 |
|------------|-----------|---------|
| Spring Boot Statistics | 12900 | JVM、HTTP 请求、线程池 |
| JVM Micrometer | 4701 | JVM 内存、GC、线程 |
| PostgreSQL | 9628 | 数据库连接、查询性能 |
| Redis | 763 | 缓存命中率、内存使用 |
| Kafka | 7589 | 消息吞吐、消费者延迟 |

#### 关键监控指标

| 指标 | 阈值 | 级别 |
|------|------|------|
| 服务可用性 | < 99.9% | P1 |
| HTTP 5xx 错误率 | > 1% | P1 |
| API 响应时间 P99 | > 3s | P2 |
| JVM 堆内存使用率 | > 85% | P2 |
| 数据库连接池使用率 | > 80% | P2 |
| Redis 内存使用率 | > 80% | P3 |
| Kafka 消费者 Lag | > 10000 | P2 |
| 磁盘使用率 | > 85% | P2 |
| CPU 使用率 | > 80%（持续 5 分钟） | P2 |

### 6.3 告警规则

#### Prometheus 告警规则（alert-rules.yml）

```yaml
groups:
  - name: inventory-system-alerts
    rules:
      - alert: ServiceDown
        expr: up{job="inventory-system"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "服务 {{ $labels.instance }} 不可用"
          description: "服务已宕机超过 1 分钟"

      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(http_server_requests_seconds_count[5m]) > 0.01
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "{{ $labels.application }} 5xx 错误率超过 1%"
          description: "当前 5xx 错误率: {{ $value | humanizePercentage }}"

      - alert: HighResponseTime
        expr: histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[5m])) > 3
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "{{ $labels.application }} P99 响应时间超过 3 秒"
          description: "当前 P99 响应时间: {{ $value }}s"

      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "{{ $labels.application }} 堆内存使用率超过 85%"

      - alert: DatabaseConnectionPoolExhaustion
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.8
        for: 3m
        labels:
          severity: warning
        annotations:
          summary: "{{ $labels.application }} 数据库连接池使用率超过 80%"

      - alert: KafkaConsumerLag
        expr: kafka_consumer_group_lag > 10000
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "{{ $labels.application }} Kafka 消费者 Lag 超过 10000"

      - alert: DiskSpaceLow
        expr: (node_filesystem_avail_bytes / node_filesystem_size_bytes) < 0.15
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "磁盘 {{ $labels.mountpoint }} 剩余空间不足 15%"
```

#### 告警通知渠道

| 渠道 | 类型 | 用途 |
|------|------|------|
| 钉钉/企微 | Webhook | 实时告警通知 |
| 邮件 | SMTP | 告警摘要（每日） |
| 短信 | SMS | P1 级别告警 |
| 电话 | 语音 | P0 级别告警（值班） |

### 6.4 日志查看

#### ELK 日志聚合

```bash
# 查询指定服务日志
# Kibana 地址: http://<host>:5601

# 查询条件示例：
# 1. 按服务名过滤
app_name: "product-service"

# 2. 按日志级别过滤
level: "ERROR"

# 3. 按时间范围过滤
@timestamp: [2026-06-08T00:00:00 TO 2026-06-08T23:59:59]

# 4. 按追踪ID过滤
trace_id: "abc123def456"
```

#### Docker 日志查看

```bash
# 查看服务日志（实时）
docker compose logs -f <service-name>

# 查看最近 100 行
docker compose logs --tail 100 <service-name>

# 查看指定时间段
docker compose logs --since "2026-06-08T10:00:00" --until "2026-06-08T12:00:00" <service-name>
```

#### Kubernetes 日志查看

```bash
# 查看 Pod 日志
kubectl logs -f deployment/<service-name> -n inventory-system

# 查看前一个崩溃的容器日志
kubectl logs --previous deployment/<service-name> -n inventory-system

# 查看多个副本日志
kubectl logs -l app=<service-name> -n inventory-system --all-containers=true
```

---

## 7. 安全策略管理

### 7.1 JWT 密钥轮换

#### 轮换策略

| 项目 | 要求 |
|------|------|
| 轮换周期 | 每 90 天 |
| 密钥长度 | 至少 256 位（HMAC-SHA256） |
| 过渡期 | 新旧密钥共存 24 小时 |
| 存储 | Nacos 加密配置 / Vault |

#### 轮换步骤

```bash
# 1. 生成新密钥
NEW_JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
echo "新密钥已生成"

# 2. 在 Nacos 中添加过渡配置
# shared-security.yml 添加：
# security:
#   jwt:
#     secret: ${JWT_SECRET}              # 旧密钥（仍可验证）
#     new-secret: ${NEW_JWT_SECRET}      # 新密钥（用于签发）

# 3. 更新环境变量
export JWT_SECRET_OLD="${JWT_SECRET}"
export JWT_SECRET="${NEW_JWT_SECRET}"

# 4. 逐步重启各服务（滚动重启）
# Kubernetes:
kubectl rollout restart deployment/auth-service -n inventory-system
sleep 30
kubectl rollout restart deployment/gateway-service -n inventory-system
sleep 30
# 依次重启业务服务...

# 5. 等待 24 小时过渡期后，移除旧密钥配置
# 6. 验证所有服务正常
```

### 7.2 CORS 白名单更新

#### 更新步骤

1. 确认需要添加的前端域名
2. 修改 Nacos 配置 `shared-security.yml`

```yaml
security:
  cors:
    allowed-origins: >-
      https://app.example.com,
      https://admin.example.com,
      https://new-domain.example.com
```

3. 通知 gateway-service 刷新配置：

```bash
# 通过 Nacos 动态刷新（如果已启用 @RefreshScope）
# 或重启 gateway-service
docker compose restart gateway-service
```

4. 验证 CORS 头：

```bash
curl -I -X OPTIONS \
  -H "Origin: https://new-domain.example.com" \
  -H "Access-Control-Request-Method: GET" \
  http://localhost:8080/api/products
# 应返回 Access-Control-Allow-Origin: https://new-domain.example.com
```

### 7.3 API 密钥管理

#### 密钥生成

```bash
# 生成 API 密钥
API_KEY=$(openssl rand -hex 32)
echo "API Key: ${API_KEY}"

# 生成密钥哈希（存储用）
API_KEY_HASH=$(echo -n "${API_KEY}" | bcrypt-cli 12)
```

#### 密钥存储

- 密钥哈希存储在 auth-service 数据库中
- 明文密钥仅在创建时展示一次，不持久化
- 建议使用 HashiCorp Vault 管理敏感密钥

#### 密钥生命周期

| 阶段 | 操作 |
|------|------|
| 创建 | 管理员生成密钥，分配给第三方系统 |
| 轮换 | 每 180 天或泄露后立即轮换 |
| 吊销 | 发现异常使用时立即吊销 |
| 审计 | 定期审查密钥使用日志 |

---

## 8. 常见故障排查

### 8.1 服务不可用

**现象**：服务健康检查返回非 200 或超时

**排查步骤**：

```bash
# 1. 检查容器/进程状态
docker compose ps <service-name>
# 或
kubectl get pods -n inventory-system -l app=<service-name>

# 2. 查看服务日志
docker compose logs --tail 200 <service-name>

# 3. 检查资源使用
docker stats <container-name>
# 或
kubectl top pod -n inventory-system

# 4. 检查 Nacos 注册状态
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=<service-name>

# 5. 端口检查
netstat -tlnp | grep <port>
```

**常见原因与处理**：

| 原因 | 处理方式 |
|------|---------|
| OOM（内存不足） | 增大容器内存限制或调整 JVM 参数 |
| 启动超时 | 检查依赖服务是否就绪 |
| 端口冲突 | 检查端口占用，修改端口配置 |
| 配置错误 | 检查 Nacos 配置和环境变量 |

### 8.2 数据库连接超时

**现象**：日志中出现 `Connection refused` 或 `Connection timeout`

**排查步骤**：

```bash
# 1. 检查 PostgreSQL 状态
docker compose ps postgres
pg_isready -h localhost -p 5432

# 2. 检查连接数
psql -U inventory_admin -c "SELECT count(*) FROM pg_stat_activity;"

# 3. 检查最大连接数
psql -U inventory_admin -c "SHOW max_connections;"

# 4. 检查连接池状态
curl http://localhost:8081/actuator/metrics/hikaricp.connections.active

# 5. 检查网络连通性
telnet <postgres-host> 5432
```

**常见原因与处理**：

| 原因 | 处理方式 |
|------|---------|
| 连接池耗尽 | 增大 `maximum-pool-size`，检查连接泄漏 |
| 最大连接数达上限 | 调整 PostgreSQL `max_connections` |
| 网络不通 | 检查防火墙、Docker 网络配置 |
| 长事务阻塞 | 查询 `pg_stat_activity` 中 `active` 状态的长事务 |

### 8.3 网关 503 错误

**现象**：通过网关访问返回 `503 Service Unavailable`

**排查步骤**：

```bash
# 1. 检查目标服务是否在 Nacos 注册
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=product-service

# 2. 检查网关路由配置
curl http://localhost:8080/actuator/gateway/routes

# 3. 直接访问目标服务（绕过网关）
curl http://localhost:8081/actuator/health

# 4. 检查网关日志
docker compose logs --tail 200 gateway-service | grep -i "503\|error\|not found"
```

**常见原因与处理**：

| 原因 | 处理方式 |
|------|---------|
| 目标服务未注册到 Nacos | 启动目标服务，检查 Nacos 连接配置 |
| 路由配置缺失 | 检查 Nacos 中 gateway-service.yml 路由配置 |
| 目标服务健康检查失败 | 重启目标服务，检查其健康状态 |
| 负载均衡无可用实例 | 检查服务实例是否被标记为不健康 |

### 8.4 Nacos 注册失败

**现象**：服务启动日志中提示注册 Nacos 失败

**排查步骤**：

```bash
# 1. 检查 Nacos 服务状态
curl http://localhost:8848/nacos/v1/ns/operator/servers

# 2. 检查网络连通性
telnet <nacos-host> 8848

# 3. 检查 Nacos 日志
docker compose logs --tail 200 nacos

# 4. 检查命名空间配置
curl http://localhost:8848/nacos/v1/console/namespaces

# 5. 检查服务配置
# 确认 spring.cloud.nacos.discovery.server-addr 和 namespace 配置正确
```

**常见原因与处理**：

| 原因 | 处理方式 |
|------|---------|
| Nacos 未启动 | 启动 Nacos 服务 |
| 命名空间不存在 | 在 Nacos 控制台创建对应命名空间 |
| 网络隔离 | 检查 Docker 网络或 Kubernetes Service 配置 |
| Nacos 磁盘满 | 清理 Nacos 数据目录或扩容磁盘 |
| 服务 IP 配置错误 | 配置 `spring.cloud.nacos.discovery.ip` 指定正确 IP |

---

## 9. 系统升级与维护流程

### 9.1 升级前准备

1. **通知相关方**：提前 3 个工作日通知用户维护时间窗口
2. **备份验证**：执行全量备份并验证备份完整性
3. **变更审批**：提交变更工单，获得技术负责人审批
4. **回滚方案**：确认回滚方案和步骤
5. **环境验证**：在 staging 环境完成升级演练

### 9.2 升级流程

#### 滚动升级（零停机）

```bash
# 1. 拉取新版本镜像
docker compose pull

# 2. 逐服务滚动升级
for svc in auth-service gateway-service product-service order-service \
  inventory-service sales-service procurement-service customer-service \
  supplier-service business-partner-service finance-service \
  admin-service report-service; do
  
  echo "升级 ${svc}..."
  docker compose up -d --no-deps ${svc}
  
  # 等待服务就绪
  RETRY=0
  MAX_RETRY=30
  until curl -s http://localhost:$(grep -A1 "${svc}" docker-compose.yml | grep -oP ':\K\d+' | head -1)/actuator/health | grep -q "UP" || [ ${RETRY} -eq ${MAX_RETRY} ]; do
    sleep 5
    RETRY=$((RETRY+1))
  done
  
  if [ ${RETRY} -eq ${MAX_RETRY} ]; then
    echo "❌ ${svc} 启动超时，执行回滚"
    docker compose up -d --no-deps ${svc} --force-recreate
    break
  fi
  
  echo "✅ ${svc} 升级完成"
done
```

#### Kubernetes 滚动升级

```bash
# 更新镜像版本
kubectl set image deployment/<service-name> \
  <service-name>=registry.example.com/inventory/<service-name>:v3.0.1 \
  -n inventory-system

# 监控升级状态
kubectl rollout status deployment/<service-name> -n inventory-system

# 如需回滚
kubectl rollout undo deployment/<service-name> -n inventory-system
```

### 9.3 数据库迁移

```bash
# 1. 检查待执行的 Flyway 迁移
cd microservices/
./gradlew flywayInfo -Penv=prod

# 2. 执行迁移（各服务启动时自动执行，也可手动执行）
./gradlew flywayMigrate -Penv=prod

# 3. 验证迁移结果
./gradlew flywayValidate -Penv=prod
```

### 9.4 升级后验证

- [ ] 所有服务健康检查通过
- [ ] Nacos 注册列表完整
- [ ] 网关路由可访问
- [ ] 前端页面正常加载
- [ ] 核心业务流程验证（创建商品 → 下单 → 出库 → 付款）
- [ ] 监控指标正常
- [ ] 日志无异常错误

### 9.5 维护窗口

| 维护类型 | 时间窗口 | 频率 |
|---------|---------|------|
| 常规升级 | 周六 02:00-06:00 | 按需 |
| 安全补丁 | 工作日 22:00-23:00 | 紧急 |
| 数据库维护 | 周日 01:00-05:00 | 每月 |
| 系统备份 | 每日 02:00-04:00 | 每日 |

---

## 10. 应急预案

### 10.1 宕机恢复

#### 全系统宕机

```
响应级别：P0
响应时间：15 分钟内
目标恢复时间：2 小时
```

**恢复步骤**：

1. **评估影响**（5 分钟）
   - 确认宕机范围（部分服务/全系统）
   - 通知相关方（技术团队、业务方、管理层）

2. **基础设施恢复**（30 分钟）
   ```bash
   # 检查基础设施
   docker compose ps
   # 或
   kubectl get nodes -n inventory-system
   
   # 按顺序恢复
   docker compose up -d postgres redis kafka nacos
   sleep 30
   docker compose up -d config-service registry-service
   sleep 15
   docker compose up -d auth-service
   sleep 10
   docker compose up -d gateway-service
   sleep 10
   docker compose up -d product-service inventory-service customer-service \
     supplier-service business-partner-service
   sleep 15
   docker compose up -d order-service sales-service procurement-service \
     finance-service report-service
   sleep 15
   docker compose up -d admin-service
   ```

3. **数据一致性验证**（30 分钟）
   - 检查数据库连接和数据完整性
   - 检查 Redis 缓存数据
   - 检查 Kafka 消费者 Lag

4. **业务验证**（30 分钟）
   - 执行核心业务流程验证
   - 检查监控指标
   - 确认日志正常

5. **恢复确认**（15 分钟）
   - 通知相关方系统恢复
   - 记录事件报告

#### 单服务宕机

```
响应级别：P1
响应时间：30 分钟内
目标恢复时间：30 分钟
```

```bash
# 1. 查看服务状态和日志
kubectl describe pod <pod-name> -n inventory-system
kubectl logs --previous <pod-name> -n inventory-system

# 2. 尝试重启
kubectl rollout restart deployment/<service-name> -n inventory-system

# 3. 如重启失败，检查最近变更
kubectl rollout history deployment/<service-name> -n inventory-system

# 4. 必要时回滚到上一版本
kubectl rollout undo deployment/<service-name> -n inventory-system
```

### 10.2 数据丢失

```
响应级别：P0
响应时间：15 分钟内
目标恢复时间：4 小时（含 PITR 恢复）
```

**恢复步骤**：

1. **立即停止写入**
   ```bash
   # 通过网关阻止写入请求
   # 在 Nacos 中配置 gateway-service.yml 添加熔断规则
   ```

2. **评估数据丢失范围**
   - 确认受影响的数据库和表
   - 确认最后有效备份时间点
   - 评估丢失数据量

3. **执行数据恢复**（参见第 5 章数据恢复步骤）
   - 全量恢复：使用最近的全量备份
   - PITR 恢复：恢复到故障发生前的时间点

4. **数据一致性校验**
   ```bash
   # 校验跨服务数据一致性
   # 1. 订单与库存一致性
   # 2. 订单与财务一致性
   # 3. 采购单与供应商一致性
   ```

5. **恢复业务**
   - 逐步开放写入权限
   - 监控系统运行状态

### 10.3 安全事件

#### 密钥泄露

```
响应级别：P0
响应时间：15 分钟内
```

**处理步骤**：

1. **立即轮换泄露的密钥**
   ```bash
   # JWT 密钥泄露
   # 参见 7.1 JWT 密钥轮换
   
   # 数据库密码泄露
   # 1. 修改 PostgreSQL 密码
   psql -U postgres -c "ALTER USER inventory_admin WITH PASSWORD 'new_password';"
   # 2. 更新 Nacos 配置
   # 3. 重启所有服务
   ```

2. **评估影响范围**
   - 检查审计日志中的异常访问
   - 分析是否发生了数据泄露

3. **通知相关方**
   - 通知安全团队和法务
   - 如涉及用户数据，按法规要求通知用户

4. **加固措施**
   - 审查所有密钥和凭证
   - 缩短密钥轮换周期
   - 加强访问控制

#### 异常访问

```
响应级别：P1
响应时间：30 分钟内
```

**处理步骤**：

1. **锁定可疑账户**
   ```bash
   # 通过管理后台或 API 禁用账户
   curl -X PATCH http://localhost:8093/api/auth/users/{userId}/disable \
     -H "Authorization: Bearer ${ADMIN_TOKEN}"
   ```

2. **撤销所有活跃会话**
   ```bash
   # 清除 Redis 中的 Token 缓存
   redis-cli KEYS "auth:token:*" | xargs redis-cli DEL
   ```

3. **分析攻击路径**
   - 检查登录日志（时间、IP、设备）
   - 检查操作日志
   - 确认是否为暴力破解

4. **加固措施**
   - 启用 IP 白名单
   - 降低限流阈值
   - 强制受影响用户修改密码

---

## 附录

### A. 常用命令速查

```bash
# 服务管理
docker compose up -d                          # 启动所有服务
docker compose down                           # 停止所有服务
docker compose restart <service>              # 重启单个服务
docker compose logs -f <service>              # 查看服务日志

# 健康检查
curl http://localhost:<port>/actuator/health  # 检查服务健康

# 数据库
pg_dump -U inventory_admin -Fc <db> > backup.dump   # 备份
pg_restore -U inventory_admin -d <db> backup.dump   # 恢复
psql -U inventory_admin -c "SELECT 1"               # 连接测试

# Redis
redis-cli ping                                # 连接测试
redis-cli info memory                         # 内存信息
redis-cli DBSIZE                              # 键数量

# Nacos
curl http://localhost:8848/nacos/v1/ns/service/list?pageNo=1&pageSize=100  # 服务列表
```

### B. 联系方式

| 角色 | 联系方式 | 响应时间 |
|------|---------|---------|
| 运维值班 | ops-oncall@example.com | 15 分钟 |
| 安全团队 | security@example.com | 30 分钟 |
| DBA | dba@example.com | 30 分钟 |
| 技术负责人 | tech-lead@example.com | 1 小时 |

### C. 文档版本历史

| 版本 | 日期 | 修改内容 | 作者 |
|------|------|---------|------|
| 3.0 | 2026-06-08 | 初始版本，基于 v3.0 微服务架构 | 系统管理团队 |
