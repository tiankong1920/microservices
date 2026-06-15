# 配置管理文档

## 版本
- 版本：1.0.0
- 创建日期：2025-01-19
- 最后更新：2025-01-19

## 1. 文档概述

### 1.1 目的
本文档提供了库存管理系统配置管理的详细指南，包括配置文件结构、环境变量管理、配置版本控制、配置更新流程和配置验证方法。

### 1.2 适用范围
- 配置文件管理
- 环境变量管理
- 配置版本控制
- 配置更新和验证
- 配置中心集成

### 1.3 目标读者
- 系统运维工程师
- DevOps工程师
- 配置管理员
- 系统架构师

## 2. 配置文件结构

### 2.1 目录结构

```
microservices/
├── config/
│   ├── application.yml              # 主配置文件
│   ├── application-dev.yml         # 开发环境配置
│   ├── application-test.yml        # 测试环境配置
│   ├── application-prod.yml        # 生产环境配置
│   └── application-local.yml       # 本地环境配置
├── docker-compose.yml             # 主Docker Compose文件
├── docker-compose.dev.yml         # 开发环境Docker Compose
├── docker-compose.test.yml        # 测试环境Docker Compose
├── docker-compose.prod.yml        # 生产环境Docker Compose
└── .env                         # 环境变量文件
```

### 2.2 配置文件示例

#### 2.2.1 主配置文件

```yaml
spring:
  application:
    name: inventory-system
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:prod}
  cloud:
    eureka:
      client:
        service-url:
          defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE:http://localhost:8761/eureka/}
    config:
      uri: ${SPRING_CLOUD_CONFIG_URI:http://localhost:8888}
      fail-fast: true
      import: optional:configserver

server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /api

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    root: INFO
    com.inventory: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

#### 2.2.2 开发环境配置

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/inventory
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5

  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 2000ms

  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
    consumer:
      group-id: inventory-consumer-group
      auto-offset-reset: earliest
```

#### 2.2.3 生产环境配置

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${POSTGRES_HOST:postgres}:${POSTGRES_PORT:5432}/${POSTGRES_DB:inventory}
    username: ${POSTGRES_USER:postgres}
    password: ${POSTGRES_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 100
      minimum-idle: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  redis:
    host: ${REDIS_HOST:redis}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD}
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms

  kafka:
    bootstrap-servers: ${KAFKA_BROKERS:kafka:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all
      retries: 3
      batch-size: 16384
      buffer-memory: 33554432
      compression-type: gzip
    consumer:
      group-id: inventory-consumer-group
      auto-offset-reset: earliest
      max-poll-records: 500
      session-timeout: 30000
      heartbeat-interval: 10000
```

## 3. 环境变量管理

### 3.1 环境变量文件

#### 3.1.1 开发环境变量

```bash
# 数据库配置
POSTGRES_DB=inventory
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_HOST=localhost
POSTGRES_PORT=5432

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Kafka配置
KAFKA_BROKERS=localhost:9092
KAFKA_ZOOKEEPER=localhost:2181

# Spring配置
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
SPRING_CLOUD_CONFIG_URI=http://localhost:8888

# 监控配置
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus

# 日志配置
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_INVENTORY=DEBUG
```

#### 3.1.2 生产环境变量

```bash
# 数据库配置
POSTGRES_DB=inventory
POSTGRES_USER=postgres
POSTGRES_PASSWORD_FILE=/run/secrets/postgres_password
POSTGRES_HOST=postgres
POSTGRES_PORT=5432

# Redis配置
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD_FILE=/run/secrets/redis_password

# Kafka配置
KAFKA_BROKERS=kafka:9092
KAFKA_ZOOKEEPER=zookeeper:2181

# Spring配置
SPRING_PROFILES_ACTIVE=prod
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://registry-service:8761/eureka/
SPRING_CLOUD_CONFIG_URI=http://config-service:8888

# JWT配置
JWT_SECRET_FILE=/run/secrets/jwt_secret
JWT_EXPIRATION=86400000

# 监控配置
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus

# Hera监控配置
HERA_SERVER_URL=http://hera-server:8080
HERA_API_KEY=your-hera-api-key

# Prometheus配置
PROMETHEUS_RETENTION_TIME=30d
PROMETHEUS_RETENTION_SIZE=50GB

# Grafana配置
GF_SECURITY_ADMIN_USER=admin
GF_SECURITY_ADMIN_PASSWORD=your-grafana-password
```

### 3.2 环境变量优先级

1. **命令行参数**（最高优先级）
   ```bash
   docker run -e SPRING_PROFILES_ACTIVE=test inventory-system
   ```

2. **Docker Compose环境变量**
   ```yaml
   services:
     product-service:
       environment:
         - SPRING_PROFILES_ACTIVE=prod
   ```

3. **.env文件**
   ```bash
   docker-compose --env-file .env up
   ```

4. **配置文件中的值**
   ```yaml
   spring:
     profiles:
       active: ${SPRING_PROFILES_ACTIVE:prod}
   ```

5. **默认值**
   ```yaml
   spring:
     profiles:
       active: prod  # 如果没有设置环境变量，使用此默认值
   ```

## 4. 配置版本控制

### 4.1 Git版本控制

```bash
# 初始化Git仓库
git init
git add .
git commit -m "Initial commit"

# 创建配置分支
git checkout -b config/dev
git checkout -b config/test
git checkout -b config/prod

# 合并配置变更
git checkout main
git merge config/dev
git merge config/test
git merge config/prod
```

### 4.2 配置变更记录

```bash
# 记录配置变更
echo "$(date '+%Y-%m-%d %H:%M:%S') - Updated database password" >> config-changes.log

# 查看配置变更历史
cat config-changes.log
```

### 4.3 配置回滚

```bash
# 查看配置历史
git log --oneline --all

# 回滚到特定版本
git checkout <commit-hash>

# 比较配置差异
git diff HEAD~1 HEAD
```

## 5. 配置更新流程

### 5.1 配置更新步骤

#### 5.1.1 开发环境更新

```bash
# 步骤1：备份当前配置
cp config/application-dev.yml config/application-dev.yml.backup

# 步骤2：编辑配置文件
vi config/application-dev.yml

# 步骤3：验证配置文件
yamllint config/application-dev.yml

# 步骤4：重启服务
docker-compose -f docker-compose.dev.yml restart product-service

# 步骤5：验证配置
curl http://localhost:8082/actuator/health
```

#### 5.1.2 生产环境更新

```bash
# 步骤1：备份当前配置
cp config/application-prod.yml config/application-prod.yml.backup

# 步骤2：提交到Git
git add config/application-prod.yml
git commit -m "Update production configuration"

# 步骤3：编辑配置文件
vi config/application-prod.yml

# 步骤4：验证配置文件
yamllint config/application-prod.yml

# 步骤5：更新配置中心
curl -X POST http://config-service:8888/product-service/prod -H "Content-Type: application/yaml" -d @config/application-prod.yml

# 步骤6：滚动更新服务
./scripts/rolling-update.sh product-service

# 步骤7：验证配置
curl http://localhost:8082/actuator/health
```

### 5.2 配置热更新

```bash
# 使用Spring Cloud Config实现配置热更新
curl -X POST http://config-service:8888/actuator/refresh -H "Content-Type: application/json"

# 验证配置更新
curl http://localhost:8082/actuator/env
```

### 5.3 配置验证

```bash
# 验证YAML语法
yamllint config/application-prod.yml

# 验证环境变量
docker-compose -f docker-compose.prod.yml config

# 验证配置加载
curl http://localhost:8082/actuator/env | jq '.'
```

## 6. 配置安全

### 6.1 敏感信息管理

#### 6.1.1 使用Docker Secrets

```bash
# 创建PostgreSQL密码
echo "your-postgres-password" | docker secret create postgres_password -

# 在docker-compose.yml中使用
services:
  postgres:
    secrets:
      - postgres_password
    environment:
      POSTGRES_PASSWORD_FILE: /run/secrets/postgres_password
```

#### 6.1.2 使用环境变量

```bash
# 不在配置文件中硬编码敏感信息
spring:
  datasource:
    password: ${POSTGRES_PASSWORD}  # 使用环境变量
```

#### 6.1.3 使用配置中心

```yaml
spring:
  cloud:
    config:
      uri: http://config-service:8888
      username: ${CONFIG_USERNAME}
      password: ${CONFIG_PASSWORD}
```

### 6.2 配置加密

```bash
# 加密敏感配置
echo "sensitive-value" | openssl enc -aes-256-cbc -salt "salt" -pass "pass:password" -base64

# 解密配置
echo "encrypted-value" | openssl enc -aes-256-cbc -d -salt "salt" -pass "pass:password" -base64
```

## 7. 配置监控

### 7.1 配置变更监控

```bash
# 监控配置文件变更
inotifywait -m -e modify,create,delete config/

# 记录配置变更
echo "$(date '+%Y-%m-%d %H:%M:%S') - Config file changed: $1" >> config-monitor.log
```

### 7.2 配置验证监控

```bash
# 定期验证配置
while true; do
    yamllint config/application-prod.yml
    if [ $? -ne 0 ]; then
        echo "Configuration validation failed!" | mail -s "Config Alert" ops-team@example.com
    fi
    sleep 3600
done
```

## 8. 最佳实践

### 8.1 配置管理原则

1. **环境隔离**
   - 开发、测试、生产环境完全隔离
   - 使用不同的配置文件
   - 使用不同的数据库实例

2. **最小权限**
   - 配置文件权限设置为600
   - 只允许必要用户访问
   - 定期审计访问权限

3. **版本控制**
   - 所有配置变更都应提交到Git
   - 使用有意义的提交信息
   - 定期打标签

4. **文档化**
   - 为每个配置项添加注释
   - 维护配置文档
   - 记录配置变更原因

5. **验证机制**
   - 配置更新后必须验证
   - 使用自动化验证工具
   - 监控配置加载错误

### 8.2 配置更新原则

1. **备份优先**
   - 更新前必须备份
   - 保留多个备份版本
   - 测试恢复流程

2. **逐步更新**
   - 使用滚动更新
   - 避免服务中断
   - 每步都验证

3. **监控观察**
   - 更新后密切监控
   - 观察系统指标
   - 及时发现问题

4. **快速回滚**
   - 准备回滚方案
   - 测试回滚流程
   - 确保可以快速恢复

## 9. 故障排查

### 9.1 配置加载失败

```bash
# 检查配置文件权限
ls -la config/application-prod.yml

# 检查配置文件语法
yamllint config/application-prod.yml

# 检查环境变量
docker-compose -f docker-compose.prod.yml config

# 查看应用日志
docker logs product-service | grep -i "config\|properties"
```

### 9.2 配置不生效

```bash
# 检查配置优先级
docker exec product-service env | grep SPRING_PROFILES_ACTIVE

# 检查配置中心连接
curl http://config-service:8888/actuator/health

# 检查配置刷新
curl http://product-service:8082/actuator/refresh

# 查看应用环境
curl http://product-service:8082/actuator/env | jq '.spring.profiles.active'
```

## 10. 附录

### 10.1 相关文档

- [EnvironmentPreparation.md](file:///e:/101/microservices/docs/EnvironmentPreparation.md) - 环境准备指南
- [DeploymentProcess.md](file:///e:/101/microservices/docs/DeploymentProcess.md) - 部署流程文档
- [ProductionConfigGuide.md](file:///e:/101/microservices/docs/ProductionConfigGuide.md) - 生产环境配置指南

### 10.2 相关工具

- yamllint - YAML配置验证工具
- jq - JSON数据处理工具
- docker-compose - Docker Compose命令行工具

### 10.3 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|---------|
| 1.0.0 | 2025-01-19 | 架构师团队 | 初始版本 |

---

**文档结束**
