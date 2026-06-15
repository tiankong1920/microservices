# 部署流程文档

## 版本
- 版本：1.0.0
- 创建日期：2025-01-19
- 最后更新：2025-01-19

## 1. 文档概述

### 1.1 目的
本文档提供了库存管理系统的详细部署流程，包括部署前检查、环境准备、服务部署、部署验证和回滚流程。通过本指南，运维工程师可以快速、安全地部署系统到各个环境。

### 1.2 适用范围
- 开发环境部署
- 测试环境部署
- 预发布环境部署
- 生产环境部署
- 服务升级部署
- 配置更新部署

### 1.3 目标读者
- 系统运维工程师
- DevOps工程师
- 部署工程师
- 系统管理员

### 1.4 前置条件
- 已完成环境准备（参考[EnvironmentPreparation.md](file:///e:/101/microservices/docs/EnvironmentPreparation.md)）
- 已完成依赖安装（参考[DependencyInstallation.md](file:///e:/101/microservices/docs/DependencyInstallation.md)）
- 已配置生产环境（参考[ProductionConfigGuide.md](file:///e:/101/microservices/docs/ProductionConfigGuide.md)）
- 已准备部署脚本和配置文件
- 已完成备份（参考[BackupRecoveryGuide.md](file:///e:/101/microservices/docs/BackupRecoveryGuide.md)）

## 2. 部署前检查清单

### 2.1 系统资源检查

#### 2.1.1 硬件资源检查

- [ ] CPU：至少8核，推荐16核
- [ ] 内存：至少32GB，推荐64GB
- [ ] 磁盘：至少500GB SSD，推荐1TB SSD
- [ ] 网络：至少1Gbps带宽
- [ ] 备份存储：至少2TB可用空间

#### 2.1.2 软件环境检查

- [ ] Docker 24.0+已安装
- [ ] Docker Compose 2.20+已安装
- [ ] Java 21 JDK已安装
- [ ] Maven 3.9+已配置
- [ ] Git客户端已安装
- [ ] 文本编辑器已安装（VS Code、IntelliJ IDEA等）

#### 2.1.3 网络配置检查

- [ ] 防火墙规则已配置
- [ ] 端口已开放（80、443、8761、8888、9090等）
- [ ] DNS解析正常
- [ ] 网络连通性已验证
- [ ] 负载均衡器已配置

#### 2.1.4 安全配置检查

- [ ] SSL/TLS证书已准备
- [ ] 密钥已生成（Docker Secrets）
- [ ] 访问控制已配置
- [ ] 审计日志已启用
- [ ] 安全扫描已完成

### 2.2 配置文件检查

#### 2.2.1 Docker Compose配置检查

```bash
# 检查Docker Compose配置文件
docker-compose -f docker-compose.yml config
docker-compose -f docker-compose.prod.yml config
docker-compose -f docker-compose.dev.yml config
```

#### 2.2.2 环境变量检查

```bash
# 检查环境变量文件
cat .env.production
cat .env.development
cat .env.testing
```

#### 2.2.3 配置文件验证

```bash
# 验证YAML配置文件
yamllint docker-compose.yml
yamllint docker-compose.prod.yml
yamllint config/application-prod.yml
```

### 2.3 依赖服务检查

#### 2.3.1 数据库检查

```bash
# 检查PostgreSQL连接
docker exec postgres-1 psql -U postgres -d inventory -c "SELECT version();"

# 检查数据库大小
docker exec postgres-1 psql -U postgres -d inventory -c "SELECT pg_size_pretty(pg_database_size('inventory'));"

# 检查数据库连接数
docker exec postgres-1 psql -U postgres -d inventory -c "SELECT count(*) FROM pg_stat_activity;"
```

#### 2.3.2 Redis检查

```bash
# 检查Redis连接
docker exec redis-1 redis-cli ping

# 检查Redis内存使用
docker exec redis-1 redis-cli INFO memory

# 检查Redis键数量
docker exec redis-1 redis-cli DBSIZE
```

#### 2.3.3 Kafka检查

```bash
# 检查Kafka连接
docker exec kafka-1 kafka-topics.sh --list

# 检查Kafka主题
docker exec kafka-1 kafka-topics.sh --describe --topic metrics
docker exec kafka-1 kafka-topics.sh --describe --topic alerts
docker exec kafka-1 kafka-topics.sh --describe --topic traces
```

#### 2.3.4 注册中心检查

```bash
# 检查Eureka注册中心
curl http://localhost:8761/actuator/health

# 检查已注册服务
curl http://localhost:8761/eureka/apps
```

#### 2.3.5 配置中心检查

```bash
# 检查配置中心
curl http://localhost:8888/actuator/health

# 检查配置文件
curl http://localhost:8888/product-service/prod
curl http://localhost:8888/order-service/prod
```

## 3. 环境准备

### 3.1 创建部署目录

```bash
# 创建部署目录
mkdir -p /opt/inventory-system
cd /opt/inventory-system

# 创建子目录
mkdir -p logs backups config certificates scripts
```

### 3.2 复制配置文件

```bash
# 复制Docker Compose文件
cp docker-compose.yml /opt/inventory-system/
cp docker-compose.prod.yml /opt/inventory-system/
cp docker-compose.dev.yml /opt/inventory-system/

# 复制配置文件
cp -r config/ /opt/inventory-system/config/

# 复制环境变量文件
cp .env.production /opt/inventory-system/.env
```

### 3.3 准备证书文件

```bash
# 创建证书目录
mkdir -p /opt/inventory-system/certificates/ssl

# 复制SSL证书
cp /path/to/server.crt /opt/inventory-system/certificates/ssl/
cp /path/to/server.key /opt/inventory-system/certificates/ssl/
cp /path/to/ca.crt /opt/inventory-system/certificates/ssl/

# 设置文件权限
chmod 600 /opt/inventory-system/certificates/ssl/server.key
chmod 644 /opt/inventory-system/certificates/ssl/server.crt
chmod 644 /opt/inventory-system/certificates/ssl/ca.crt
```

### 3.4 创建Docker Secrets

```bash
# 创建PostgreSQL密码
echo "your-postgres-password" | docker secret create postgres_password -

# 创建Redis密码
echo "your-redis-password" | docker secret create redis_password -

# 创建JWT密钥
echo "your-jwt-secret" | docker secret create jwt_secret -

# 创建配置服务密码
echo "your-config-password" | docker secret create config_service_password -

# 验证secrets
docker secret ls
```

### 3.5 配置环境变量

```bash
# 创建环境变量文件
cat > /opt/inventory-system/.env << 'EOF'
# 数据库配置
POSTGRES_DB=inventory
POSTGRES_USER=postgres
POSTGRES_PASSWORD_FILE=/run/secrets/postgres_password

# Redis配置
REDIS_PASSWORD_FILE=/run/secrets/redis_password

# JWT配置
JWT_SECRET_FILE=/run/secrets/jwt_secret

# 配置服务密码
CONFIG_SERVICE_PASSWORD_FILE=/run/secrets/config_service_password

# JWK配置
JWK_SET_URI=https://your-auth-server/.well-known/jwks.json

# Grafana配置
GF_SECURITY_ADMIN_USER=admin
GF_SECURITY_ADMIN_PASSWORD=your-grafana-password

# Hera监控配置
HERA_SERVER_URL=http://hera-server:8080
HERA_API_KEY=your-hera-api-key

# Kafka配置
KAFKA_BROKERS=kafka:9092
KAFKA_ZOOKEEPER=zookeeper:2181

# 监控配置
PROMETHEUS_RETENTION_TIME=30d
PROMETHEUS_RETENTION_SIZE=50GB
EOF
```

## 4. 服务部署

### 4.1 部署顺序

#### 4.1.1 基础设施服务（第一优先级）

```bash
# 步骤1：启动数据库和缓存
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d postgres redis

# 等待基础设施服务就绪
echo "等待基础设施服务就绪..."
sleep 30

# 验证服务状态
docker ps | grep postgres
docker ps | grep redis
```

#### 4.1.2 支持服务（第二优先级）

```bash
# 步骤2：启动注册中心和配置中心
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d registry-service config-service

# 等待支持服务就绪
echo "等待支持服务就绪..."
sleep 20

# 验证服务状态
docker ps | grep registry-service
docker ps | grep config-service

# 检查服务健康
curl http://localhost:8761/actuator/health
curl http://localhost:8888/actuator/health
```

#### 4.1.3 监控服务（第三优先级）

```bash
# 步骤3：启动监控服务
docker-compose -f monitoring/docker-compose.yml up -d

# 等待监控服务就绪
echo "等待监控服务就绪..."
sleep 20

# 验证服务状态
docker ps | grep prometheus
docker ps | grep grafana
docker ps | grep alertmanager
docker ps | grep jaeger

# 检查服务健康
curl http://localhost:9090/-/healthy
curl http://localhost:3000/api/health
curl http://localhost:9093/-/healthy
curl http://localhost:16686/api/health
```

#### 4.1.4 核心服务（第四优先级）

```bash
# 步骤4：启动核心服务
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d \
  product-service \
  order-service \
  inventory-service \
  sales-service \
  procurement-service \
  customer-service \
  supplier-service \
  business-partner-service

# 等待核心服务就绪
echo "等待核心服务就绪..."
sleep 30

# 验证服务状态
docker ps | grep product-service
docker ps | grep order-service
docker ps | grep inventory-service

# 检查服务健康
curl http://localhost:8082/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8080/actuator/health
```

#### 4.1.5 网关服务（第五优先级）

```bash
# 步骤5：启动API网关
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d gateway-service

# 等待网关服务就绪
echo "等待网关服务就绪..."
sleep 10

# 验证服务状态
docker ps | grep gateway-service

# 检查服务健康
curl http://localhost:9090/actuator/health
```

#### 4.1.6 管理服务（最后启动）

```bash
# 步骤6：启动管理服务
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d admin-service finance-service

# 等待管理服务就绪
echo "等待管理服务就绪..."
sleep 10

# 验证服务状态
docker ps | grep admin-service
docker ps | grep finance-service

# 检查服务健康
curl http://localhost:9091/actuator/health
curl http://localhost:9092/actuator/health
```

### 4.2 部署脚本

#### 4.2.1 自动部署脚本

```bash
#!/bin/bash
# deploy.sh

set -e

echo "=========================================="
echo "  库存管理系统部署脚本"
echo "=========================================="

# 配置变量
ENVIRONMENT=${1:-prod}
COMPOSE_FILE="docker-compose.${ENVIRONMENT}.yml"
BACKUP_DIR="/opt/inventory-system/backups"
LOG_DIR="/opt/inventory-system/logs"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# 创建备份目录
mkdir -p ${BACKUP_DIR}/${TIMESTAMP}
mkdir -p ${LOG_DIR}

# 备份当前配置
echo "备份当前配置..."
cp -r config/ ${BACKUP_DIR}/${TIMESTAMP}/config/

# 备份当前环境变量
echo "备份环境变量..."
cp .env.${ENVIRONMENT} ${BACKUP_DIR}/${TIMESTAMP}/.env.${ENVIRONMENT}

# 停止现有服务
echo "停止现有服务..."
docker-compose -f ${COMPOSE_FILE} down

# 拉取最新镜像
echo "拉取最新镜像..."
docker-compose -f ${COMPOSE_FILE} pull

# 启动新服务
echo "启动新服务..."
docker-compose -f ${COMPOSE_FILE} up -d

# 等待服务启动
echo "等待服务启动..."
sleep 60

# 验证服务状态
echo "验证服务状态..."
docker-compose -f ${COMPOSE_FILE} ps

# 检查服务健康
echo "检查服务健康..."
./scripts/health-check.sh

echo "=========================================="
echo "  部署完成！"
echo "=========================================="
```

#### 4.2.2 滚动更新脚本

```bash
#!/bin/bash
# rolling-update.sh

set -e

SERVICE_NAME=${1:-product-service}
NEW_VERSION=${2:-latest}
COMPOSE_FILE="docker-compose.prod.yml"
MAX_REPLICAS=3

echo "开始滚动更新 ${SERVICE_NAME} 到版本 ${NEW_VERSION}..."

# 获取当前副本数
CURRENT_REPLICAS=$(docker-compose -f ${COMPOSE_FILE} ps -q ${SERVICE_NAME} | wc -l)

echo "当前副本数: ${CURRENT_REPLICAS}"

# 逐个更新实例
for i in $(seq 1 ${CURRENT_REPLICAS}); do
    echo "更新实例 ${i}/${CURRENT_REPLICAS}..."
    
    # 停止一个实例
    docker-compose -f ${COMPOSE_FILE} stop ${SERVICE_NAME}_${i}
    
    # 更新镜像
    docker-compose -f ${COMPOSE_FILE} up -d --no-deps --scale ${SERVICE_NAME}=${i} ${SERVICE_NAME}
    
    # 等待实例就绪
    sleep 30
    
    # 健康检查
    if curl -f http://localhost:8082/actuator/health; then
        echo "实例 ${i} 更新成功"
    else
        echo "实例 ${i} 更新失败，回滚"
        docker-compose -f ${COMPOSE_FILE} up -d --scale ${SERVICE_NAME}=${CURRENT_REPLICAS} ${SERVICE_NAME}
        exit 1
    fi
done

echo "滚动更新完成！"
```

#### 4.2.3 蓝绿部署脚本

```bash
#!/bin/bash
# blue-green-deploy.sh

set -e

SERVICE_NAME=${1:-product-service}
NEW_VERSION=${2:-latest}
BLUE_PORT=8082
GREEN_PORT=8083

echo "开始蓝绿部署 ${SERVICE_NAME}..."

# 检查当前环境
CURRENT_ENV=$(docker ps | grep ${SERVICE_NAME} | grep -o "blue\|green" || echo "none")

if [ "${CURRENT_ENV}" = "blue" ]; then
    echo "当前环境: blue，部署到 green"
    TARGET_ENV="green"
elif [ "${CURRENT_ENV}" = "green" ]; then
    echo "当前环境: green，部署到 blue"
    TARGET_ENV="blue"
else
    echo "当前环境: none，部署到 blue"
    TARGET_ENV="blue"
fi

# 部署到目标环境
echo "部署到 ${TARGET_ENV} 环境..."
docker-compose -f docker-compose.${TARGET_ENV}.yml up -d ${SERVICE_NAME}

# 等待服务就绪
echo "等待服务就绪..."
sleep 60

# 健康检查
if curl -f http://localhost:${GREEN_PORT}/actuator/health; then
    echo "Green环境健康，切换流量到 green"
    # 切换负载均衡器流量
    # ...
elif curl -f http://localhost:${BLUE_PORT}/actuator/health; then
    echo "Blue环境健康，切换流量到 blue"
    # 切换负载均衡器流量
    # ...
else
    echo "部署失败，保持当前环境"
fi

echo "蓝绿部署完成！"
```

## 5. 部署验证

### 5.1 服务健康检查

#### 5.1.1 基础服务健康检查

```bash
#!/bin/bash
# health-check.sh

echo "开始健康检查..."

# 检查PostgreSQL
echo "检查PostgreSQL..."
if curl -f http://localhost:5432/actuator/health; then
    echo "✓ PostgreSQL: 健康"
else
    echo "✗ PostgreSQL: 不健康"
fi

# 检查Redis
echo "检查Redis..."
if curl -f http://localhost:6379/actuator/health; then
    echo "✓ Redis: 健康"
else
    echo "✗ Redis: 不健康"
fi

# 检查Kafka
echo "检查Kafka..."
if docker exec kafka-1 kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1; then
    echo "✓ Kafka: 健康"
else
    echo "✗ Kafka: 不健康"
fi

# 检查Eureka
echo "检查Eureka..."
if curl -f http://localhost:8761/actuator/health; then
    echo "✓ Eureka: 健康"
else
    echo "✗ Eureka: 不健康"
fi

# 检查Config Server
echo "检查Config Server..."
if curl -f http://localhost:8888/actuator/health; then
    echo "✓ Config Server: 健康"
else
    echo "✗ Config Server: 不健康"
fi

echo "基础服务健康检查完成！"
```

#### 5.1.2 应用服务健康检查

```bash
#!/bin/bash
# app-health-check.sh

echo "开始应用服务健康检查..."

SERVICES=(
    "product-service:8082"
    "order-service:8081"
    "inventory-service:8080"
    "sales-service:8086"
    "procurement-service:8085"
    "customer-service:8083"
    "supplier-service:8084"
    "business-partner-service:8087"
    "gateway-service:9090"
    "admin-service:9091"
    "finance-service:9092"
)

for service in "${SERVICES[@]}"; do
    NAME=$(echo $service | cut -d':' -f1)
    PORT=$(echo $service | cut -d':' -f2)
    
    echo "检查 ${NAME}..."
    if curl -f http://localhost:${PORT}/actuator/health; then
        echo "✓ ${NAME}: 健康"
    else
        echo "✗ ${NAME}: 不健康"
    fi
done

echo "应用服务健康检查完成！"
```

#### 5.1.3 监控服务健康检查

```bash
#!/bin/bash
# monitoring-health-check.sh

echo "开始监控服务健康检查..."

# 检查Prometheus
echo "检查Prometheus..."
if curl -f http://localhost:9090/-/healthy; then
    echo "✓ Prometheus: 健康"
else
    echo "✗ Prometheus: 不健康"
fi

# 检查Grafana
echo "检查Grafana..."
if curl -f http://localhost:3000/api/health; then
    echo "✓ Grafana: 健康"
else
    echo "✗ Grafana: 不健康"
fi

# 检查Alertmanager
echo "检查Alertmanager..."
if curl -f http://localhost:9093/-/healthy; then
    echo "✓ Alertmanager: 健康"
else
    echo "✗ Alertmanager: 不健康"
fi

# 检查Jaeger
echo "检查Jaeger..."
if curl -f http://localhost:16686/api/health; then
    echo "✓ Jaeger: 健康"
else
    echo "✗ Jaeger: 不健康"
fi

echo "监控服务健康检查完成！"
```

### 5.2 功能验证

#### 5.2.1 API端点验证

```bash
#!/bin/bash
# api-validation.sh

echo "开始API端点验证..."

# 测试产品服务API
echo "测试产品服务API..."
curl -f http://localhost:9090/api/products
curl -f http://localhost:9090/api/products/1

# 测试订单服务API
echo "测试订单服务API..."
curl -f http://localhost:9090/api/orders
curl -f http://localhost:9090/api/orders/1

# 测试库存服务API
echo "测试库存服务API..."
curl -f http://localhost:9090/api/inventory
curl -f http://localhost:9090/api/inventory/1

# 测试客户服务API
echo "测试客户服务API..."
curl -f http://localhost:9090/api/customers
curl -f http://localhost:9090/api/customers/1

echo "API端点验证完成！"
```

#### 5.2.2 服务间通信验证

```bash
#!/bin/bash
# service-communication-validation.sh

echo "开始服务间通信验证..."

# 测试订单服务调用产品服务
echo "测试订单服务调用产品服务..."
curl -f http://localhost:8081/api/orders/1/products

# 测试订单服务调用库存服务
echo "测试订单服务调用库存服务..."
curl -f http://localhost:8081/api/orders/1/inventory

# 测试订单服务调用客户服务
echo "测试订单服务调用客户服务..."
curl -f http://localhost:8081/api/orders/1/customer

echo "服务间通信验证完成！"
```

#### 5.2.3 数据持久化验证

```bash
#!/bin/bash
# data-persistence-validation.sh

echo "开始数据持久化验证..."

# 测试数据库写入
echo "测试数据库写入..."
docker exec postgres-1 psql -U postgres -d inventory -c "INSERT INTO test_table (name) VALUES ('test');"

# 测试数据库读取
echo "测试数据库读取..."
docker exec postgres-1 psql -U postgres -d inventory -c "SELECT * FROM test_table;"

# 测试Redis写入
echo "测试Redis写入..."
docker exec redis-1 redis-cli SET test_key "test_value"

# 测试Redis读取
echo "测试Redis读取..."
docker exec redis-1 redis-cli GET test_key

# 测试Kafka写入
echo "测试Kafka写入..."
docker exec kafka-1 kafka-console-producer --broker-list localhost:9092 --topic test --message "test_message"

# 测试Kafka读取
echo "测试Kafka读取..."
docker exec kafka-1 kafka-console-consumer --bootstrap-server localhost:9092 --topic test --from-beginning --max-messages 1

echo "数据持久化验证完成！"
```

### 5.3 性能验证

#### 5.3.1 响应时间验证

```bash
#!/bin/bash
# response-time-validation.sh

echo "开始响应时间验证..."

# 测试API响应时间
for i in {1..10}; do
    START_TIME=$(date +%s%N)
    curl -s http://localhost:9090/api/products > /dev/null
    END_TIME=$(date +%s%N)
    RESPONSE_TIME=$((END_TIME - START_TIME))
    echo "请求 $i: ${RESPONSE_TIME}ms"
done

# 计算平均响应时间
AVG_TIME=$(echo "scale=0; $(for i in {1..10}; do echo \$i; done)" | awk '{sum+=$1} END {print sum/NR}')
echo "平均响应时间: ${AVG_TIME}ms"

# 验证响应时间阈值
if [ ${AVG_TIME} -lt 1000 ]; then
    echo "✓ 响应时间正常"
else
    echo "✗ 响应时间过长"
fi

echo "响应时间验证完成！"
```

#### 5.3.2 吞吐量验证

```bash
#!/bin/bash
# throughput-validation.sh

echo "开始吞吐量验证..."

# 并发测试API
for i in {1..100}; do
    curl -s http://localhost:9090/api/products > /dev/null &
done

wait

# 统计成功请求数
SUCCESS_COUNT=$(grep -c "200 OK" /tmp/throughput-test.log 2>/dev/null || echo "100")
echo "成功请求数: ${SUCCESS_COUNT}"

# 计算吞吐量
THROUGHPUT=$(echo "scale=0; ${SUCCESS_COUNT}/10" | bc)
echo "吞吐量: ${THROUGHPUT} 请求/秒"

# 验证吞吐量阈值
if [ ${THROUGHPUT} -gt 50 ]; then
    echo "✓ 吞吐量正常"
else
    echo "✗ 吞吐量过低"
fi

echo "吞吐量验证完成！"
```

#### 5.3.3 资源使用验证

```bash
#!/bin/bash
# resource-usage-validation.sh

echo "开始资源使用验证..."

# 检查CPU使用率
CPU_USAGE=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1)
echo "CPU使用率: ${CPU_USAGE}%"

# 检查内存使用率
MEMORY_USAGE=$(free | grep Mem | awk '{printf("%.1f\n", $3/$2 * 100)}')
echo "内存使用率: ${MEMORY_USAGE}%"

# 检查磁盘使用率
DISK_USAGE=$(df -h / | awk 'NR==2 {print $5}' | cut -d'%' -f1)
echo "磁盘使用率: ${DISK_USAGE}%"

# 验证资源使用阈值
if [ ${CPU_USAGE} -lt 80 ] && [ ${MEMORY_USAGE} -lt 85 ] && [ ${DISK_USAGE} -lt 85 ]; then
    echo "✓ 资源使用正常"
else
    echo "✗ 资源使用过高"
fi

echo "资源使用验证完成！"
```

## 6. 回滚流程

### 6.1 回滚触发条件

- 部署失败且无法恢复
- 服务健康检查失败超过阈值
- 功能验证失败
- 性能指标严重下降
- 数据损坏或丢失
- 安全漏洞发现

### 6.2 回滚策略

#### 6.2.1 配置回滚

```bash
#!/bin/bash
# config-rollback.sh

set -e

BACKUP_DIR="/opt/inventory-system/backups"
TIMESTAMP=${1:-latest}

echo "开始配置回滚到 ${TIMESTAMP}..."

# 停止当前服务
echo "停止当前服务..."
docker-compose -f docker-compose.prod.yml down

# 恢复配置文件
echo "恢复配置文件..."
cp -r ${BACKUP_DIR}/${TIMESTAMP}/config/* config/

# 恢复环境变量
echo "恢复环境变量..."
cp ${BACKUP_DIR}/${TIMESTAMP}/.env.prod .env.prod

# 重启服务
echo "重启服务..."
docker-compose -f docker-compose.prod.yml up -d

# 等待服务启动
echo "等待服务启动..."
sleep 60

# 验证服务状态
echo "验证服务状态..."
docker-compose -f docker-compose.prod.yml ps

echo "配置回滚完成！"
```

#### 6.2.2 数据库回滚

```bash
#!/bin/bash
# database-rollback.sh

set -e

BACKUP_FILE=${1:-/opt/inventory-system/backups/database/inventory_full_20250119_020000.sql}

echo "开始数据库回滚..."

# 停止应用服务
echo "停止应用服务..."
docker-compose -f docker-compose.prod.yml stop product-service order-service inventory-service

# 恢复数据库
echo "恢复数据库..."
docker exec -i postgres-1 psql -U postgres -d inventory < ${BACKUP_FILE}

# 重启应用服务
echo "重启应用服务..."
docker-compose -f docker-compose.prod.yml start product-service order-service inventory-service

# 等待服务启动
echo "等待服务启动..."
sleep 30

# 验证数据库
echo "验证数据库..."
docker exec postgres-1 psql -U postgres -d inventory -c "SELECT COUNT(*) FROM products;"

echo "数据库回滚完成！"
```

#### 6.2.3 完整系统回滚

```bash
#!/bin/bash
# full-system-rollback.sh

set -e

BACKUP_DIR="/opt/inventory-system/backups"
TIMESTAMP=${1:-latest}

echo "开始完整系统回滚到 ${TIMESTAMP}..."

# 停止所有服务
echo "停止所有服务..."
docker-compose -f docker-compose.prod.yml down

# 停止监控服务
echo "停止监控服务..."
docker-compose -f monitoring/docker-compose.yml down

# 恢复所有配置
echo "恢复所有配置..."
cp -r ${BACKUP_DIR}/${TIMESTAMP}/* config/
cp ${BACKUP_DIR}/${TIMESTAMP}/.env.prod .env.prod

# 恢复数据库
echo "恢复数据库..."
docker-compose -f docker-compose.prod.yml up -d postgres
docker exec -i postgres-1 psql -U postgres -d inventory < ${BACKUP_DIR}/${TIMESTAMP}/inventory_full_*.sql

# 启动所有服务
echo "启动所有服务..."
docker-compose -f docker-compose.prod.yml up -d

# 启动监控服务
echo "启动监控服务..."
docker-compose -f monitoring/docker-compose.yml up -d

# 等待服务启动
echo "等待服务启动..."
sleep 60

# 验证所有服务
echo "验证所有服务..."
docker-compose -f docker-compose.prod.yml ps
docker-compose -f monitoring/docker-compose.yml ps

echo "完整系统回滚完成！"
```

### 6.3 回滚验证

#### 6.3.1 配置验证

```bash
#!/bin/bash
# config-validation-after-rollback.sh

echo "验证回滚后的配置..."

# 验证配置文件
echo "验证配置文件..."
yamllint docker-compose.prod.yml
yamllint config/application-prod.yml

# 验证环境变量
echo "验证环境变量..."
cat .env.prod

# 验证secrets
echo "验证secrets..."
docker secret ls

echo "配置验证完成！"
```

#### 6.3.2 功能验证

```bash
#!/bin/bash
# functionality-validation-after-rollback.sh

echo "验证回滚后的功能..."

# 运行健康检查
echo "运行健康检查..."
./scripts/health-check.sh

# 运行API验证
echo "运行API验证..."
./scripts/api-validation.sh

# 运行服务间通信验证
echo "运行服务间通信验证..."
./scripts/service-communication-validation.sh

echo "功能验证完成！"
```

## 7. 部署最佳实践

### 7.1 部署前准备

1. **完整备份**
   - 部署前必须完成完整备份
   - 验证备份完整性
   - 测试备份恢复流程

2. **环境隔离**
   - 使用独立的部署环境
   - 避免开发、测试、生产环境混淆
   - 使用不同的数据库实例

3. **依赖检查**
   - 验证所有依赖服务可用
   - 检查网络连通性
   - 确认资源充足

4. **配置验证**
   - 验证配置文件语法正确
   - 检查环境变量完整
   - 测试配置加载

### 7.2 部署过程

1. **分步部署**
   - 按照依赖关系分步部署
   - 每步部署后验证服务状态
   - 记录部署日志

2. **健康检查**
   - 每个服务启动后进行健康检查
   - 等待服务完全就绪
   - 验证服务功能正常

3. **监控告警**
   - 部署过程中启用监控
   - 配置部署告警
   - 及时发现和处理问题

4. **回滚准备**
   - 准备回滚脚本
   - 测试回滚流程
   - 确保可以快速回滚

### 7.3 部署后验证

1. **全面测试**
   - 执行功能测试
   - 执行性能测试
   - 执行安全测试
   - 验证业务流程

2. **监控观察**
   - 观察系统指标
   - 检查错误日志
   - 验证告警配置
   - 监控资源使用

3. **文档更新**
   - 更新部署文档
   - 记录部署变更
   - 更新配置版本
   - 通知相关人员

## 8. 故障排查

### 8.1 常见部署问题

#### 8.1.1 服务启动失败

**问题**：服务容器启动后立即退出

**排查步骤**：
1. 检查容器日志
   ```bash
   docker logs <container-name>
   ```

2. 检查配置文件
   ```bash
   docker-compose -f docker-compose.prod.yml config
   ```

3. 检查环境变量
   ```bash
   docker-compose -f docker-compose.prod.yml config
   ```

4. 检查资源限制
   ```bash
   docker stats <container-name>
   ```

**解决方案**：
- 修正配置错误
- 增加资源限制
- 检查依赖服务状态

#### 8.1.2 服务健康检查失败

**问题**：服务启动但健康检查失败

**排查步骤**：
1. 检查应用日志
   ```bash
   docker logs <container-name> | grep -i error
   ```

2. 检查数据库连接
   ```bash
   docker exec <container-name> curl http://postgres:5432/actuator/health
   ```

3. 检查Redis连接
   ```bash
   docker exec <container-name> curl http://redis:6379/actuator/health
   ```

4. 检查配置中心连接
   ```bash
   curl http://localhost:8888/actuator/health
   ```

**解决方案**：
- 修复连接配置
- 重启依赖服务
- 更新服务注册信息

#### 8.1.3 性能问题

**问题**：部署后系统性能下降

**排查步骤**：
1. 检查系统资源
   ```bash
   top
   free -h
   df -h
   ```

2. 检查应用日志
   ```bash
   docker logs <container-name> | grep -i "slow\|timeout"
   ```

3. 检查数据库性能
   ```bash
   docker exec postgres-1 psql -U postgres -d inventory -c "SELECT * FROM pg_stat_statements ORDER BY mean_exec_time DESC LIMIT 10;"
   ```

4. 检查缓存命中率
   ```bash
   docker exec redis-1 redis-cli INFO stats
   ```

**解决方案**：
- 优化数据库查询
- 调整缓存策略
- 增加系统资源
- 优化应用配置

### 8.2 紧急联系方式

| 角色 | 姓名 | 电话 | 邮箱 |
|------|------|------|------|
| 运维负责人 | - | ops-team@example.com |
| DevOps负责人 | - | devops-team@example.com |
| 数据库管理员 | - | dba-team@example.com |
| 安全负责人 | - | security-team@example.com |

## 9. 附录

### 9.1 相关文档

- [EnvironmentPreparation.md](file:///e:/101/microservices/docs/EnvironmentPreparation.md) - 环境准备指南
- [DependencyInstallation.md](file:///e:/101/microservices/docs/DependencyInstallation.md) - 依赖安装指南
- [ProductionConfigGuide.md](file:///e:/101/microservices/docs/ProductionConfigGuide.md) - 生产环境配置指南
- [BackupRecoveryGuide.md](file:///e:/101/microservices/docs/BackupRecoveryGuide.md) - 备份与恢复指南
- [OperationsManual.md](file:///e:/101/microservices/docs/OperationsManual.md) - 运维手册

### 9.2 相关脚本

- [deploy.sh](file:///e:/101/microservices/scripts/deploy.sh) - 自动部署脚本
- [rolling-update.sh](file:///e:/101/microservices/scripts/rolling-update.sh) - 滚动更新脚本
- [blue-green-deploy.sh](file:///e:/101/microservices/scripts/blue-green-deploy.sh) - 蓝绿部署脚本
- [health-check.sh](file:///e:/101/microservices/scripts/health-check.sh) - 健康检查脚本
- [api-validation.sh](file:///e:/101/microservices/scripts/api-validation.sh) - API验证脚本

### 9.3 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|---------|
| 1.0.0 | 2025-01-19 | 架构师团队 | 初始版本 |

---

**文档结束**
