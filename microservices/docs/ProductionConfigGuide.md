# 生产环境配置指南

## 版本
- 版本：1.0.0
- 创建日期：2025-01-19
- 最后更新：2025-01-19

## 1. 文档概述

### 1.1 目的
本指南提供了库存管理系统生产环境的完整配置步骤，包括部署前准备、环境部署、配置优化、监控告警、安全配置、性能优化、备份恢复和运维操作。通过本指南，运维工程师可以快速搭建和配置生产环境，确保系统稳定运行、高效管理和快速故障恢复。

### 1.2 适用范围
- 生产环境部署和配置
- 系统性能优化
- 监控和告警配置
- 安全配置和管理
- 备份和恢复策略
- 运维操作和故障排查

### 1.3 目标读者
- 系统运维工程师
- DevOps工程师
- 系统管理员
- 数据库管理员
- 安全工程师

### 1.4 前置条件
- 已安装Docker 24.0+和Docker Compose 2.20+
- 已安装Java 21 JDK
- 已配置Maven 3.9+环境
- 已准备生产服务器（至少8核CPU、32GB内存、500GB SSD）
- 已配置网络和防火墙规则
- 已准备域名和SSL证书
- 已准备监控和告警系统（Prometheus、Grafana、Alertmanager）
- 已准备分布式追踪系统（Jaeger）
- 已配置Hera监控系统
- 具备基本的Linux系统管理能力

### 1.5 术语定义

| 术语 | 定义 |
|------|------|
| **RTO（Recovery Time Objective）**：恢复时间目标，从故障发生到系统恢复的最大可接受时间 |
| **RPO（Recovery Point Objective）**：恢复点目标，可接受的最大数据丢失量 |
| **SLA（Service Level Agreement）**：服务级别协议，定义服务提供商和客户之间的服务质量和性能指标 |
| **HA（High Availability）**：高可用性，系统在长时间内保持可用的能力 |
| **G1GC（Garbage First Garbage Collector）**：Java的垃圾回收器，旨在提供可预测的停顿时间和高吞吐量 |
| **HikariCP**：高性能的JDBC连接池，提供快速、可靠和轻量级的数据库连接管理 |
| **Prometheus**：开源的监控和告警工具，用于采集和存储时间序列数据 |
| **Grafana**：开源的数据可视化工具，用于创建监控仪表板 |
| **Jaeger**：开源的分布式追踪系统，用于监控和诊断微服务架构中的事务 |
| **Alertmanager**：Prometheus的告警管理组件，负责处理和路由告警 |

## 2. 生产环境架构概述

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        客户端层                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Web浏览器   │  │  移动应用     │  │  第三方系统   │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
└─────────┼─────────────────┼─────────────────┼──────────────────┘
          │                 │                 │
          └─────────────────┴─────────────────┘
                            │
┌───────────────────────────┼─────────────────────────────────────┐
│                           ▼                                      │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    负载均衡层                           │   │
│  │              (Nginx/HAProxy: 443/80)                    │   │
│  └─────────────────────────────────────────────────────────┘   │
└────────────┬────────────────────────────────────────────────────┘
             │
┌────────────┼────────────────────────────────────────────────────┐
│             ▼                                              │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    API网关层                           │   │
│  │              (gateway-service: 9090)                   │   │
│  └─────────────────────────────────────────────────────────┘   │
└────────────┬────────────────────────────────────────────────────┘
             │
┌────────────┼────────────────────────────────────────────────────┐
│             ▼                                              │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    服务层                              │   │
│  ├─────────────────────────────────────────────────────────┤   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│  │  │ 订单服务     │  │ 库存服务     │  │ 产品服务     │    │   │
│  │  │:8081        │  │:8080        │  │:8082        │    │   │
│  │  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘    │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│  │  │ 客户服务     │  │ 供应商服务   │  │ 采购服务     │    │   │
│  │  │:8083        │  │:8084        │  │:8085        │    │   │
│  │  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘    │   │
│  │  ┌──────────────┐  ┌──────────────┐                   │   │
│  │  │ 销售服务     │  │ 业务伙伴服务  │                   │   │
│  │  │:8086        │  │:8087        │                   │   │
│  │  └──────┬───────┘  └──────┬───────┘                   │   │
│  └─────────┼─────────────────┼───────────────────────────────┘   │
└────────────┼─────────────────┼───────────────────────────────────┘
             │                 │
┌────────────┼─────────────────┼───────────────────────────────────┐
│             ▼                 ▼                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    支持服务层                           │   │
│  ├─────────────────────────────────────────────────────────┤   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│  │  │ 注册中心     │  │ 配置中心     │  │ 管理服务     │    │   │
│  │  │:8761        │  │:8888        │  │:9091        │    │   │
│  │  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘    │   │
│  │  ┌──────────────┐                                        │   │
│  │  │ 财务服务     │                                        │   │
│  │  │:9092        │                                        │   │
│  │  └──────┬───────┘                                        │   │
│  └─────────┼─────────────────────────────────────────────────┘   │
└────────────┼───────────────────────────────────────────────────────┘
             │
┌────────────┼───────────────────────────────────────────────────────┐
│             ▼                                                      │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    数据层                              │   │
│  ├─────────────────────────────────────────────────────────┤   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│  │  │ PostgreSQL   │  │    Redis     │  │    Kafka     │    │   │
│  │  │:5432        │  │:6379        │  │:9092        │    │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘    │   │
│  └─────────────────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────────────────┘
             │
┌────────────┼───────────────────────────────────────────────────────┐
│             ▼                                                      │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                  监控和告警层                           │   │
│  ├─────────────────────────────────────────────────────────┤   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │   │
│  │  │  Prometheus  │  │   Grafana    │  │Alertmanager  │    │   │
│  │  │:9090        │  │:3000        │  │:9093        │    │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘    │   │
│  │  ┌──────────────┐  ┌──────────────┐                     │   │
│  │  │    Jaeger    │  │     Hera     │                     │   │
│  │  │:16686       │  │:8080        │                     │   │
│  │  └──────────────┘  └──────────────┘                     │   │
│  └─────────────────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────────────────┘
```

### 2.2 服务组件说明

#### 2.2.1 核心服务

| 服务名称 | 端口 | 副本数 | 说明 |
|---------|------|--------|------|
| product-service | 8082 | 2 | 产品管理服务 |
| order-service | 8081 | 2 | 订单管理服务 |
| inventory-service | 8080 | 2 | 库存管理服务 |
| sales-service | 8086 | 2 | 销售管理服务 |
| procurement-service | 8085 | 2 | 采购管理服务 |
| customer-service | 8083 | 2 | 客户管理服务 |
| supplier-service | 8084 | 2 | 供应商管理服务 |
| business-partner-service | 8087 | 2 | 业务伙伴管理服务 |

#### 2.2.2 支持服务

| 服务名称 | 端口 | 副本数 | 说明 |
|---------|------|--------|------|
| registry-service | 8761 | 1 | 服务注册中心（Eureka） |
| config-service | 8888 | 1 | 配置中心 |
| gateway-service | 9090 | 2 | API网关 |
| admin-service | 9091 | 1 | 管理服务 |
| finance-service | 9092 | 2 | 财务服务 |

#### 2.2.3 基础设施服务

| 服务名称 | 端口 | 副本数 | 说明 |
|---------|------|--------|------|
| PostgreSQL | 5432 | 1 | 主数据库 |
| Redis | 6379 | 1 | 缓存和会话存储 |
| Kafka | 9092 | 1 | 消息队列 |

#### 2.2.4 监控服务

| 服务名称 | 端口 | 副本数 | 说明 |
|---------|------|--------|------|
| Prometheus | 9090 | 1 | 指标采集和存储 |
| Grafana | 3000 | 1 | 数据可视化 |
| Alertmanager | 9093 | 1 | 告警管理 |
| Jaeger | 16686 | 1 | 分布式追踪 |
| Hera | 8080 | 1 | 监控系统 |

### 2.3 网络拓扑

#### 2.3.1 网络分段

- **外部网络**：互联网访问，通过负载均衡器暴露服务
- **DMZ网络**：包含负载均衡器和API网关
- **应用网络**：包含所有微服务
- **数据网络**：包含数据库、缓存和消息队列
- **监控网络**：包含监控和告警服务

#### 2.3.2 端口映射

| 服务 | 外部端口 | 内部端口 | 协议 | 说明 |
|------|---------|---------|------|------|
| 负载均衡器 | 443 | 443 | HTTPS | 安全访问 |
| 负载均衡器 | 80 | 80 | HTTP | 重定向到HTTPS |
| API网关 | 9090 | 9090 | HTTP | API访问 |
| Grafana | 3000 | 3000 | HTTP | 监控仪表板 |
| Jaeger UI | 16686 | 16686 | HTTP | 追踪界面 |
| PostgreSQL | 5432 | 5432 | TCP | 数据库访问 |
| Redis | 6379 | 6379 | TCP | 缓存访问 |

### 2.4 数据流向

#### 2.4.1 请求流向

```
客户端 → 负载均衡器 → API网关 → 核心服务 → 数据层
```

#### 2.4.2 监控数据流向

```
应用服务 → Prometheus → Alertmanager → 通知渠道
应用服务 → Jaeger → 分布式追踪
应用服务 → Grafana → 可视化
```

#### 2.4.3 日志数据流向

```
应用服务 → 日志收集器 → 日志存储 → 日志分析
```

## 3. 部署前准备

### 3.1 系统要求

#### 3.1.1 硬件要求

| 组件 | 最低配置 | 推荐配置 | 说明 |
|------|---------|---------|------|
| 应用服务器 | 8核CPU、32GB内存、500GB SSD | 16核CPU、64GB内存、1TB SSD | 运行所有微服务 |
| 数据库服务器 | 8核CPU、32GB内存、1TB SSD | 16核CPU、64GB内存、2TB SSD | PostgreSQL主库 |
| 缓存服务器 | 4核CPU、16GB内存、500GB SSD | 8核CPU、32GB内存、1TB SSD | Redis |
| 监控服务器 | 4核CPU、16GB内存、500GB SSD | 8核CPU、32GB内存、1TB SSD | Prometheus、Grafana等 |
| 负载均衡器 | 4核CPU、8GB内存、100GB SSD | 8核CPU、16GB内存、200GB SSD | Nginx/HAProxy |

#### 3.1.2 软件要求

| 软件 | 版本要求 | 说明 |
|------|---------|------|
| 操作系统 | Ubuntu 22.04 LTS / CentOS 8+ | Linux服务器 |
| Docker | 24.0+ | 容器运行时 |
| Docker Compose | 2.20+ | 容器编排 |
| Java JDK | 21 | 应用运行环境 |
| Maven | 3.9+ | 构建工具 |
| Nginx | 1.24+ | 负载均衡器 |
| PostgreSQL | 15+ | 数据库 |
| Redis | 7.0+ | 缓存 |
| Kafka | 3.5+ | 消息队列 |

### 3.2 网络配置要求

#### 3.2.1 网络规划

| 网络段 | 用途 | 说明 |
|--------|------|------|
| 10.0.0.0/24 | 外部网络 | 互联网访问 |
| 10.0.1.0/24 | DMZ网络 | 负载均衡器和API网关 |
| 10.0.2.0/24 | 应用网络 | 微服务 |
| 10.0.3.0/24 | 数据网络 | 数据库、缓存、消息队列 |
| 10.0.4.0/24 | 监控网络 | 监控和告警服务 |

#### 3.2.2 防火墙规则

**入站规则**：

```bash
# 允许SSH访问
sudo ufw allow 22/tcp

# 允许HTTP和HTTPS访问
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# 允许API网关访问
sudo ufw allow from 10.0.1.0/24 to any port 9090

# 允许监控服务访问
sudo ufw allow from 10.0.4.0/24 to any port 9090
sudo ufw allow from 10.0.4.0/24 to any port 3000

# 允许数据库访问
sudo ufw allow from 10.0.2.0/24 to any port 5432
sudo ufw allow from 10.0.2.0/24 to any port 6379

# 默认拒绝其他入站连接
sudo ufw default deny incoming
```

**出站规则**：

```bash
# 允许所有出站连接
sudo ufw default allow outgoing
```

#### 3.2.3 DNS配置

```bash
# 配置DNS服务器
sudo nano /etc/systemd/resolved.conf

[Resolve]
DNS=8.8.8.8 8.8.4.4
FallbackDNS=1.1.1.1 1.0.0.1

# 重启DNS服务
sudo systemctl restart systemd-resolved
```

### 3.3 安全配置要求

#### 3.3.1 SSL/TLS证书

**生成自签名证书（仅用于测试）**：

```bash
# 生成私钥
openssl genrsa -out server.key 2048

# 生成证书签名请求
openssl req -new -key server.key -out server.csr

# 生成自签名证书
openssl x509 -req -days 365 -in server.csr -signkey server.key -out server.crt
```

**使用Let's Encrypt证书（推荐用于生产）**：

```bash
# 安装Certbot
sudo apt-get install certbot

# 获取证书
sudo certbot certonly --standalone -d yourdomain.com

# 证书位置
# /etc/letsencrypt/live/yourdomain.com/fullchain.pem
# /etc/letsencrypt/live/yourdomain.com/privkey.pem
```

#### 3.3.2 密钥管理

**使用Docker Secrets**：

```bash
# 创建PostgreSQL密码
echo "your-postgres-password" | docker secret create postgres_password -

# 创建Redis密码
echo "your-redis-password" | docker secret create redis_password -

# 创建JWT密钥
echo "your-jwt-secret" | docker secret create jwt_secret -

# 查看secrets
docker secret ls
```

#### 3.3.3 访问控制

**配置RBAC（基于角色的访问控制）**：

```yaml
# application-prod.yml
spring:
  security:
    user:
      name: admin
      password: ${ADMIN_PASSWORD}
    roles:
      - ADMIN
      - OPERATOR
      - VIEWER
```

### 3.4 环境变量清单

创建`.env.production`文件：

```bash
# 数据库配置
POSTGRES_DB=inventory
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your-postgres-password

# Redis配置
REDIS_PASSWORD=your-redis-password

# JWT配置
JWT_SECRET=your-jwt-secret
JWT_EXPIRATION=86400000

# 配置服务密码
CONFIG_SERVICE_PASSWORD=your-config-password

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
```

### 3.5 密钥和证书准备

#### 3.5.1 证书文件结构

```
certificates/
├── ssl/
│   ├── server.crt
│   ├── server.key
│   └── ca.crt
└── secrets/
    ├── postgres_password.txt
    ├── redis_password.txt
    └── jwt_secret.txt
```

#### 3.5.2 权限设置

```bash
# 设置证书文件权限
chmod 600 ssl/server.key
chmod 644 ssl/server.crt
chmod 644 ssl/ca.crt

# 设置密钥文件权限
chmod 600 secrets/*.txt

# 设置目录权限
chmod 700 ssl
chmod 700 secrets
```

## 4. 生产环境部署

### 4.1 Docker Compose部署步骤

#### 4.1.1 准备部署目录

```bash
# 创建部署目录
mkdir -p /opt/inventory-system
cd /opt/inventory-system

# 创建子目录
mkdir -p logs backups config certificates/ssl certificates/secrets
```

#### 4.1.2 复制配置文件

```bash
# 复制Docker Compose文件
cp docker-compose.yml /opt/inventory-system/
cp docker-compose.prod.yml /opt/inventory-system/

# 复制配置文件
cp config/application-prod.yml /opt/inventory-system/config/

# 复制证书文件
cp -r certificates/* /opt/inventory-system/certificates/

# 复制环境变量文件
cp .env.production /opt/inventory-system/.env
```

#### 4.1.3 构建镜像

```bash
# 构建所有服务镜像
docker-compose -f docker-compose.yml -f docker-compose.prod.yml build

# 或者单独构建特定服务
docker-compose -f docker-compose.yml -f docker-compose.prod.yml build product-service
```

#### 4.1.4 启动服务

```bash
# 启动所有服务
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# 查看服务状态
docker-compose -f docker-compose.yml -f docker-compose.prod.yml ps

# 查看服务日志
docker-compose -f docker-compose.yml -f docker-compose.prod.yml logs -f
```

### 4.2 服务启动顺序

#### 4.2.1 启动顺序

1. **基础设施服务**（第一优先级）
   - PostgreSQL
   - Redis
   - Kafka

2. **支持服务**（第二优先级）
   - registry-service（Eureka）
   - config-service
   - monitoring services（Prometheus、Grafana、Alertmanager、Jaeger）

3. **核心服务**（第三优先级）
   - product-service
   - order-service
   - inventory-service
   - sales-service
   - procurement-service
   - customer-service
   - supplier-service
   - business-partner-service

4. **网关服务**（第四优先级）
   - gateway-service

5. **管理服务**（最后启动）
   - admin-service
   - finance-service

#### 4.2.2 分步启动脚本

```bash
#!/bin/bash
# deploy.sh

echo "开始部署生产环境..."

# 步骤1：启动基础设施服务
echo "步骤1：启动基础设施服务..."
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d postgres redis kafka

# 等待基础设施服务就绪
echo "等待基础设施服务就绪..."
sleep 30

# 步骤2：启动支持服务
echo "步骤2：启动支持服务..."
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d registry-service config-service

# 等待支持服务就绪
echo "等待支持服务就绪..."
sleep 20

# 步骤3：启动监控服务
echo "步骤3：启动监控服务..."
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d prometheus grafana alertmanager jaeger

# 等待监控服务就绪
echo "等待监控服务就绪..."
sleep 20

# 步骤4：启动核心服务
echo "步骤4：启动核心服务..."
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d \
  product-service order-service inventory-service sales-service \
  procurement-service customer-service supplier-service business-partner-service

# 等待核心服务就绪
echo "等待核心服务就绪..."
sleep 30

# 步骤5：启动网关服务
echo "步骤5：启动网关服务..."
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d gateway-service

# 等待网关服务就绪
echo "等待网关服务就绪..."
sleep 10

# 步骤6：启动管理服务
echo "步骤6：启动管理服务..."
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d admin-service finance-service

echo "部署完成！"
echo "检查服务状态..."
docker-compose -f docker-compose.yml -f docker-compose.prod.yml ps
```

### 4.3 健康检查配置

#### 4.3.1 健康检查端点

所有服务都提供健康检查端点：

```bash
# 检查所有服务健康状态
curl http://localhost:9090/actuator/health

# 检查特定服务健康状态
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

#### 4.3.2 健康检查脚本

```bash
#!/bin/bash
# health-check.sh

services=(
  "registry-service:8761"
  "config-service:8888"
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

echo "开始健康检查..."

for service in "${services[@]}"; do
  name=$(echo $service | cut -d':' -f1)
  port=$(echo $service | cut -d':' -f2)
  
  response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health)
  
  if [ $response -eq 200 ]; then
    echo "✓ $name: 健康"
  else
    echo "✗ $name: 不健康 (HTTP $response)"
  fi
done

echo "健康检查完成！"
```

### 4.4 部署验证

#### 4.4.1 服务验证清单

- [ ] 所有服务容器运行正常
- [ ] 所有服务健康检查通过
- [ ] 服务注册到Eureka
- [ ] 数据库连接正常
- [ ] Redis连接正常
- [ ] Kafka连接正常
- [ ] Prometheus采集指标正常
- [ ] Grafana仪表板显示正常
- [ ] Alertmanager告警配置正常
- [ ] Jaeger追踪数据正常
- [ ] API网关路由正常
- [ ] 负载均衡器工作正常

#### 4.4.2 功能验证

```bash
# 测试API网关
curl http://localhost:9090/api/products

# 测试产品服务
curl http://localhost:8082/api/products

# 测试订单服务
curl http://localhost:8081/api/orders

# 测试库存服务
curl http://localhost:8080/api/inventory

# 测试监控端点
curl http://localhost:9090/actuator/metrics
curl http://localhost:9090/actuator/prometheus
```

#### 4.4.3 性能验证

```bash
# 使用Apache Bench进行压力测试
ab -n 1000 -c 10 http://localhost:9090/api/products

# 使用JMeter进行性能测试
# 创建JMeter测试计划并执行
```

## 5. 生产环境配置

### 5.1 数据库配置（PostgreSQL调优）

#### 5.1.1 PostgreSQL配置文件

编辑`postgresql.conf`：

```ini
# 连接设置
max_connections = 200
shared_buffers = 4GB
effective_cache_size = 12GB
maintenance_work_mem = 1GB
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100
random_page_cost = 1.1
effective_io_concurrency = 200
work_mem = 2621kB
min_wal_size = 1GB
max_wal_size = 4GB

# 查询优化
shared_preload_libraries = 'pg_stat_statements'
pg_stat_statements.max = 10000
pg_stat_statements.track = all

# 日志设置
logging_collector = on
log_directory = 'pg_log'
log_filename = 'postgresql-%Y-%m-%d_%H%M%S.log'
log_rotation_age = 1d
log_rotation_size = 100MB
log_min_duration_statement = 1000

# 性能优化
synchronous_commit = off
commit_delay = 0
wal_compression = on
```

#### 5.1.2 HikariCP连接池配置

```yaml
# application-prod.yml
spring:
  datasource:
    hikari:
      # 最大连接池大小
      maximum-pool-size: 100
      
      # 最小空闲连接数
      minimum-idle: 20
      
      # 连接超时时间（毫秒）
      connection-timeout: 30000
      
      # 验证超时时间（毫秒）
      validation-timeout: 5000
      
      # 空闲超时时间（毫秒）
      idle-timeout: 600000
      
      # 连接最大生命周期（毫秒）
      max-lifetime: 1800000
      
      # 连接测试查询
      connection-test-query: SELECT 1
      
      # 泄露检测阈值（毫秒）
      leak-detection-threshold: 60000
```

#### 5.1.3 数据库索引优化

```sql
-- 创建常用查询的索引
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_inventory_product ON inventory(product_id);
CREATE INDEX idx_inventory_warehouse ON inventory(warehouse_id);

-- 创建复合索引
CREATE INDEX idx_orders_customer_status ON orders(customer_id, status);
CREATE INDEX idx_inventory_product_warehouse ON inventory(product_id, warehouse_id);

-- 分析表统计信息
ANALYZE products;
ANALYZE orders;
ANALYZE inventory;
```

### 5.2 Redis配置（内存和持久化）

#### 5.2.1 Redis配置文件

编辑`redis.conf`：

```ini
# 内存配置
maxmemory 4gb
maxmemory-policy allkeys-lru

# 持久化配置
save 900 1
save 300 10
save 60 10000

# AOF配置
appendonly yes
appendfilename "appendonly.aof"
appendfsync everysec
no-appendfsync-on-rewrite no
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb

# 网络配置
bind 0.0.0.0
port 6379
timeout 300
tcp-keepalive 60

# 日志配置
loglevel notice
logfile ""

# 慢查询配置
slowlog-log-slower-than 10000
slowlog-max-len 128

# 客户端配置
maxclients 10000

# 安全配置
requirepass ${REDIS_PASSWORD}
```

#### 5.2.2 Redis缓存策略

```java
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .disableCachingNullValues()
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .withInitialCacheConfigurations(getCacheConfigurations())
            .transactionAware()
            .build();
    }

    private Map<String, RedisCacheConfiguration> getCacheConfigurations() {
        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        
        // 产品缓存 - 1小时
        configMap.put("products", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1)));
        
        // 库存缓存 - 5分钟
        configMap.put("inventory", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5)));
        
        // 订单缓存 - 30分钟
        configMap.put("orders", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30)));
        
        // 客户缓存 - 2小时
        configMap.put("customers", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(2)));
        
        return configMap;
    }
}
```

### 5.3 JVM参数调优

#### 5.3.1 G1GC配置

```yaml
# docker-compose.prod.yml
environment:
  - JAVA_OPTS=-XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:ParallelGCThreads=4 \
    -XX:ConcGCThreads=1 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/tmp/heapdump.hprof \
    -XX:+PrintGCDetails \
    -XX:+PrintGCDateStamps \
    -Xloggc:/tmp/gc.log \
    -XX:+UseGCLogFileRotation \
    -XX:NumberOfGCLogFiles=10 \
    -XX:GCLogFileSize=10M
```

#### 5.3.2 堆内存配置

根据服务类型调整堆内存大小：

| 服务类型 | 堆内存大小 | 说明 |
|---------|-----------|------|
| 核心服务 | 1GB | product-service、order-service等 |
| 支持服务 | 512MB | admin-service、finance-service等 |
| 网关服务 | 1GB | gateway-service |
| 注册中心 | 512MB | registry-service |
| 配置中心 | 512MB | config-service |

#### 5.3.3 JVM参数说明

```bash
# G1GC参数
-XX:+UseG1GC                          # 使用G1垃圾回收器
-XX:MaxGCPauseMillis=200              # 最大GC停顿时间目标（毫秒）
-XX:ParallelGCThreads=4               # 并行GC线程数
-XX:ConcGCThreads=1                   # 并发GC线程数

# 堆内存参数
-Xms512m                              # 初始堆大小
-Xmx1g                                # 最大堆大小

# 错误处理参数
-XX:+HeapDumpOnOutOfMemoryError       # 内存溢出时生成堆转储
-XX:HeapDumpPath=/tmp/heapdump.hprof  # 堆转储文件路径

# GC日志参数
-XX:+PrintGCDetails                   # 打印GC详细信息
-XX:+PrintGCDateStamps                # 打印GC时间戳
-Xloggc:/tmp/gc.log                   # GC日志文件路径
-XX:+UseGCLogFileRotation             # 启用GC日志轮转
-XX:NumberOfGCLogFiles=10             # 保留GC日志文件数量
-XX:GCLogFileSize=10M                # 单个GC日志文件大小
```

### 5.4 Tomcat线程池配置

```yaml
# application-prod.yml
server:
  tomcat:
    # 线程池配置
    threads:
      max: 500                        # 最大线程数
      min-spare: 50                   # 最小空闲线程数
    
    # 连接配置
    max-connections: 10000            # 最大连接数
    accept-count: 200                 # 等待队列长度
    connection-timeout: 60000          # 连接超时时间（毫秒）
    keep-alive-timeout: 30000         # 保持连接超时时间（毫秒）
    
    # 其他配置
    max-http-form-post-size: 10MB     # 最大POST请求大小
    max-swallow-size: 2MB             # 最大吞咽大小
```

### 5.5 Kafka配置

#### 5.5.1 Kafka生产者配置

```yaml
# application-prod.yml
spring:
  kafka:
    producer:
      # 服务器地址
      bootstrap-servers: kafka:9092
      
      # 键序列化器
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      
      # 值序列化器
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      
      # 确认模式
      acks: all
      
      # 重试次数
      retries: 3
      
      # 批量大小
      batch-size: 16384
      
      # 缓冲区大小
      buffer-memory: 33554432
      
      # 压缩类型
      compression-type: gzip
      
      # 发送超时时间
      properties:
        request.timeout.ms: 30000
        delivery.timeout.ms: 120000
```

#### 5.5.2 Kafka消费者配置

```yaml
spring:
  kafka:
    consumer:
      # 服务器地址
      bootstrap-servers: kafka:9092
      
      # 键反序列化器
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      
      # 值反序列化器
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      
      # 消费者组ID
      group-id: inventory-consumer-group
      
      # 自动提交偏移量
      enable-auto-commit: false
      
      # 自动偏移量重置策略
      auto-offset-reset: earliest
      
      # 最大轮询记录数
      max-poll-records: 500
      
      # 会话超时时间
      session.timeout.ms: 30000
      
      # 心跳间隔
      heartbeat.interval.ms: 10000
      
      # 最大轮询间隔
      max.poll.interval.ms: 300000
```

## 6. 监控和告警配置

### 6.1 Prometheus配置

#### 6.1.1 Prometheus配置文件

编辑`prometheus/prometheus.yml`：

```yaml
# 全局配置
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    cluster: 'inventory-prod'
    environment: 'production'

# 告警规则文件
rule_files:
  - '/etc/prometheus/rules/*.yml'

# 抓取配置
scrape_configs:
  # Prometheus自身监控
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  # API网关监控
  - job_name: 'gateway-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['gateway-service:9090']
        labels:
          service: 'gateway'

  # 核心服务监控
  - job_name: 'core-services'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['product-service:8082', 'order-service:8081', 'inventory-service:8080']
        labels:
          service: 'core'
      - targets: ['sales-service:8086', 'procurement-service:8085']
        labels:
          service: 'core'
      - targets: ['customer-service:8083', 'supplier-service:8084']
        labels:
          service: 'core'
      - targets: ['business-partner-service:8087']
        labels:
          service: 'core'

  # 支持服务监控
  - job_name: 'support-services'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['admin-service:9091', 'finance-service:9092']
        labels:
          service: 'support'

  # PostgreSQL监控
  - job_name: 'postgres'
    static_configs:
      - targets: ['postgres-exporter:9187']

  # Redis监控
  - job_name: 'redis'
    static_configs:
      - targets: ['redis-exporter:9121']

  # Kafka监控
  - job_name: 'kafka'
    static_configs:
      - targets: ['kafka-exporter:9308']

  # Node Exporter监控
  - job_name: 'node'
    static_configs:
      - targets: ['node-exporter:9100']

# 告警管理器配置
alerting:
  alertmanagers:
    - static_configs:
        - targets: ['alertmanager:9093']
```

#### 6.1.2 Prometheus告警规则

创建`prometheus/rules/alerts.yml`：

```yaml
groups:
  - name: service_alerts
    interval: 30s
    rules:
      # 服务可用性告警
      - alert: ServiceDown
        expr: up{job=~".*service"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "服务 {{ $labels.instance }} 宕机"
          description: "服务 {{ $labels.job }} 在 {{ $labels.instance }} 已经宕机超过1分钟"

      # 高错误率告警
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "服务 {{ $labels.instance }} 错误率过高"
          description: "服务 {{ $labels.job }} 在 {{ $labels.instance }} 的错误率超过5%"

      # 高响应时间告警
      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "服务 {{ $labels.instance }} 响应时间过长"
          description: "服务 {{ $labels.job }} 在 {{ $labels.instance }} 的P95响应时间超过1秒"

  - name: jvm_alerts
    interval: 30s
    rules:
      # JVM堆内存使用率告警
      - alert: HighHeapMemoryUsage
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "JVM堆内存使用率过高"
          description: "实例 {{ $labels.instance }} 的堆内存使用率超过85%"

      # GC时间过长告警
      - alert: LongGcPause
        expr: rate(jvm_gc_pause_seconds_sum[5m]) > 0.1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "GC停顿时间过长"
          description: "实例 {{ $labels.instance }} 的GC停顿时间超过100ms"

  - name: database_alerts
    interval: 30s
    rules:
      # 数据库连接池耗尽告警
      - alert: DatabasePoolExhausted
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "数据库连接池即将耗尽"
          description: "数据库连接池使用率超过90%"

      # 数据库慢查询告警
      - alert: SlowDatabaseQuery
        expr: rate(hikaricp_connections_active[5m]) > 0.1
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "数据库查询缓慢"
          description: "数据库查询响应时间超过阈值"

  - name: system_alerts
    interval: 30s
    rules:
      # CPU使用率告警
      - alert: HighCPUUsage
        expr: 100 - (avg by(instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100) > 80
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "CPU使用率过高"
          description: "实例 {{ $labels.instance }} 的CPU使用率超过80%"

      # 内存使用率告警
      - alert: HighMemoryUsage
        expr: (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100 > 85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "内存使用率过高"
          description: "实例 {{ $labels.instance }} 的内存使用率超过85%"

      # 磁盘使用率告警
      - alert: HighDiskUsage
        expr: (1 - (node_filesystem_avail_bytes / node_filesystem_size_bytes)) * 100 > 85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "磁盘使用率过高"
          description: "实例 {{ $labels.instance }} 的磁盘使用率超过85%"
```

#### 6.1.3 Prometheus存储配置

```yaml
# prometheus/prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

# 存储配置
storage:
  tsdb:
    path: /prometheus
    retention.time: 30d
    retention.size: 50GB
```

### 6.2 Grafana配置

#### 6.2.1 数据源配置

创建`grafana/provisioning/datasources/prometheus.yml`：

```yaml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
    jsonData:
      timeInterval: 15s
      queryTimeout: 60s
      httpMethod: POST
```

#### 6.2.2 仪表板配置

创建`grafana/provisioning/dashboards/dashboard.yml`：

```yaml
apiVersion: 1

providers:
  - name: 'Inventory System'
    orgId: 1
    folder: ''
    type: file
    disableDeletion: false
    updateIntervalSeconds: 10
    allowUiUpdates: true
    options:
      path: /var/lib/grafana/dashboards
```

#### 6.2.3 核心仪表板

**系统概览仪表板**：
- 服务可用性
- 请求速率
- 错误率
- 响应时间
- JVM指标
- 系统资源使用率

**服务详情仪表板**：
- 服务健康状态
- 请求流量
- 错误分析
- 性能指标
- JVM内存和GC
- 线程池状态

**数据库仪表板**：
- 连接池状态
- 查询性能
- 慢查询分析
- 事务统计
- 锁等待

### 6.3 Alertmanager配置

#### 6.3.1 Alertmanager配置文件

编辑`prometheus/alertmanager.yml`：

```yaml
# 全局配置
global:
  resolve_timeout: 5m
  smtp_smarthost: 'smtp.example.com:587'
  smtp_from: 'alertmanager@example.com'
  smtp_auth_username: 'alertmanager@example.com'
  smtp_auth_password: 'your-password'

# 路由配置
route:
  group_by: ['alertname', 'cluster', 'service']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 12h
  receiver: 'default'
  
  routes:
    # 关键告警路由
    - match:
        severity: critical
      receiver: 'critical-alerts'
      continue: false
    
    # 警告告警路由
    - match:
        severity: warning
      receiver: 'warning-alerts'
      continue: false
    
    # 数据库告警路由
    - match_re:
        alertname: 'Database.*'
      receiver: 'database-team'
    
    # JVM告警路由
    - match_re:
        alertname: 'JVM.*'
      receiver: 'development-team'

# 接收器配置
receivers:
  - name: 'default'
    email_configs:
      - to: 'team@example.com'
        headers:
          Subject: '[Alert] {{ .GroupLabels.alertname }}'

  - name: 'critical-alerts'
    email_configs:
      - to: 'oncall@example.com'
        headers:
          Subject: '[CRITICAL] {{ .GroupLabels.alertname }}'
    slack_configs:
      - api_url: 'https://hooks.slack.com/services/xxx'
        channel: '#critical-alerts'
        title: '[CRITICAL] {{ .GroupLabels.alertname }}'
        text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'
    webhook_configs:
      - url: 'http://pagerduty-webhook-url'

  - name: 'warning-alerts'
    email_configs:
      - to: 'team@example.com'
        headers:
          Subject: '[WARNING] {{ .GroupLabels.alertname }}'
    slack_configs:
      - api_url: 'https://hooks.slack.com/services/xxx'
        channel: '#warning-alerts'
        title: '[WARNING] {{ .GroupLabels.alertname }}'
        text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'

  - name: 'database-team'
    email_configs:
      - to: 'database-team@example.com'
        headers:
          Subject: '[DB Alert] {{ .GroupLabels.alertname }}'

  - name: 'development-team'
    email_configs:
      - to: 'dev-team@example.com'
        headers:
          Subject: '[Dev Alert] {{ .GroupLabels.alertname }}'

# 抑制规则
inhibit_rules:
  # 如果服务宕机，抑制该服务的其他告警
  - source_match:
      alertname: 'ServiceDown'
    target_match_re:
      alertname: '.*'
    equal: ['service', 'instance']

  # 如果数据库连接池耗尽，抑制数据库慢查询告警
  - source_match:
      alertname: 'DatabasePoolExhausted'
    target_match:
      alertname: 'SlowDatabaseQuery'
    equal: ['instance']
```

#### 6.3.2 告警模板

创建`prometheus/templates/default.tmpl`：

```go
{{ define "slack.default.title" }}
[{{ .Status | toUpper }}{{ if eq .Status "firing" }}:{{ .Alerts.Firing | len }}{{ end }}] {{ .GroupLabels.alertname }}
{{ end }}

{{ define "slack.default.text" }}
{{ range .Alerts }}
{{ if .Labels.severity }}**Severity:** {{ .Labels.severity }}{{ end }}
**Description:** {{ .Annotations.description }}
**Instance:** {{ .Labels.instance }}
**Service:** {{ .Labels.job }}
{{ end }}
{{ end }}
```

### 6.4 Jaeger配置

#### 6.4.1 Jaeger环境变量

```yaml
# docker-compose.yml
environment:
  - COLLECTOR_ZIPKIN_HOST_PORT=:9411
  - COLLECTOR_OTLP_ENABLED=true
  - SPAN_STORAGE_TYPE=elasticsearch
  - ES_SERVER_URLS=http://elasticsearch:9200
  - ES_USERNAME=elastic
  - ES_PASSWORD=changeme
```

#### 6.4.2 OpenTelemetry采样率配置

```yaml
# application-prod.yml
management:
  tracing:
    sampling:
      probability: 0.1  # 10%采样率
```

#### 6.4.3 追踪导出器配置

```yaml
# application-prod.yml
management:
  otlp:
    tracing:
      endpoint: http://jaeger-collector:4317
      headers:
        Authorization: Bearer ${JAEGER_AUTH_TOKEN}
```

### 6.5 Hera监控配置

#### 6.5.1 Hera客户端配置

```yaml
# application-prod.yml
hera:
  enabled: true
  server-url: ${HERA_SERVER_URL:http://hera-server:8080}
  api-key: ${HERA_API_KEY}
  
  # 指标上报配置
  metrics:
    enabled: true
    reporting-interval: 30s
    batch-size: 1000
    
  # 告警配置
  alerting:
    enabled: true
    webhook-url: ${HERA_WEBHOOK_URL}
    
  # 追踪配置
  tracing:
    enabled: true
    sample-rate: 0.1
```

#### 6.5.2 Hera监控指标

```java
@Configuration
public class HeraMetricsConfig {

    @Bean
    public HeraMeterRegistry heraMeterRegistry() {
        return HeraMeterRegistry.builder()
            .serverUrl("http://hera-server:8080")
            .apiKey("${HERA_API_KEY}")
            .build();
    }
}
```

## 7. 安全配置

### 7.1 密钥管理

#### 7.1.1 Docker Secrets

```bash
# 创建PostgreSQL密码
echo "your-postgres-password" | docker secret create postgres_password -

# 创建Redis密码
echo "your-redis-password" | docker secret create redis_password -

# 创建JWT密钥
echo "your-jwt-secret" | docker secret create jwt_secret -

# 创建配置服务密码
echo "your-config-password" | docker secret create config_service_password -

# 查看secrets
docker secret ls

# 删除secret
docker secret rm postgres_password
```

#### 7.1.2 使用Docker Secrets

```yaml
# docker-compose.prod.yml
services:
  postgres:
    secrets:
      - postgres_password
    environment:
      - POSTGRES_PASSWORD_FILE=/run/secrets/postgres_password

  redis:
    secrets:
      - redis_password
    command: redis-server --requirepass $(cat /run/secrets/redis_password)

secrets:
  postgres_password:
    external: true
  redis_password:
    external: true
  jwt_secret:
    external: true
```

### 7.2 SSL/TLS配置

#### 7.2.1 Nginx SSL配置

```nginx
# nginx.conf
server {
    listen 443 ssl http2;
    server_name yourdomain.com;

    # SSL证书配置
    ssl_certificate /etc/nginx/ssl/server.crt;
    ssl_certificate_key /etc/nginx/ssl/server.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # SSL会话配置
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    # HSTS配置
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # 代理到API网关
    location / {
        proxy_pass http://gateway-service:9090;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# HTTP重定向到HTTPS
server {
    listen 80;
    server_name yourdomain.com;
    return 301 https://$server_name$request_uri;
}
```

#### 7.2.2 Spring Boot SSL配置

```yaml
# application-prod.yml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: tomcat
    trust-store: classpath:truststore.p12
    trust-store-password: ${TRUSTSTORE_PASSWORD}
    trust-store-type: PKCS12
```

### 7.3 网络安全

#### 7.3.1 Docker网络隔离

```yaml
# docker-compose.prod.yml
networks:
  inventory-net-prod:
    driver: bridge
    internal: false
    ipam:
      config:
        - subnet: 10.0.2.0/24

  monitoring-network:
    driver: bridge
    internal: false
    ipam:
      config:
        - subnet: 10.0.4.0/24
```

#### 7.3.2 防火墙规则

```bash
# 允许SSH访问
sudo ufw allow 22/tcp

# 允许HTTP和HTTPS访问
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# 允许监控服务访问
sudo ufw allow from 10.0.4.0/24 to any port 9090
sudo ufw allow from 10.0.4.0/24 to any port 3000

# 允许数据库访问
sudo ufw allow from 10.0.2.0/24 to any port 5432
sudo ufw allow from 10.0.2.0/24 to any port 6379

# 默认拒绝其他入站连接
sudo ufw default deny incoming

# 启用防火墙
sudo ufw enable
```

### 7.4 访问控制

#### 7.4.1 Spring Security配置

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/metrics").hasRole("VIEWER")
                .requestMatchers("/actuator/prometheus").hasRole("VIEWER")
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .requestMatchers("/api/**").authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
}
```

#### 7.4.2 RBAC配置

```yaml
# application-prod.yml
spring:
  security:
    user:
      name: admin
      password: ${ADMIN_PASSWORD}
      roles: ADMIN
```

### 7.5 数据加密

#### 7.5.1 数据库加密

```sql
-- 启用PostgreSQL加密
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 加密敏感数据
SELECT pgp_sym_encrypt('sensitive-data', 'encryption-key');
```

#### 7.5.2 Redis加密

```bash
# 启用Redis TLS
redis-server --tls-port 6379 --port 0 --tls-cert-file /path/to/redis.crt --tls-key-file /path/to/redis.key --tls-ca-cert-file /path/to/ca.crt
```

## 8. 性能优化

### 8.1 JVM参数优化

#### 8.1.1 G1GC优化参数

```bash
# 生产环境推荐配置
-XX:+UseG1GC                          # 使用G1垃圾回收器
-XX:MaxGCPauseMillis=200              # 最大GC停顿时间目标
-XX:ParallelGCThreads=4               # 并行GC线程数（CPU核心数/2）
-XX:ConcGCThreads=1                   # 并发GC线程数（并行GC线程数/4）
-XX:InitiatingHeapOccupancyPercent=45 # 触发并发GC的堆占用百分比
-XX:G1ReservePercent=10              # 保留空间百分比
-XX:G1HeapRegionSize=16m              # G1堆区域大小

# 堆内存配置
-Xms1g                                # 初始堆大小
-Xmx2g                                # 最大堆大小

# 错误处理和日志
-XX:+HeapDumpOnOutOfMemoryError       # 内存溢出时生成堆转储
-XX:HeapDumpPath=/tmp/heapdump.hprof  # 堆转储文件路径
-XX:+PrintGCDetails                   # 打印GC详细信息
-XX:+PrintGCDateStamps                # 打印GC时间戳
-Xloggc:/tmp/gc.log                   # GC日志文件路径
-XX:+UseGCLogFileRotation             # 启用GC日志轮转
-XX:NumberOfGCLogFiles=10             # 保留GC日志文件数量
-XX:GCLogFileSize=10M                 # 单个GC日志文件大小
```

#### 8.1.2 不同服务的JVM配置

| 服务类型 | 堆内存 | GC参数 | 说明 |
|---------|--------|--------|------|
| 核心服务 | 2GB | G1GC, MaxGCPauseMillis=200ms | 高并发、低延迟 |
| 支持服务 | 1GB | G1GC, MaxGCPauseMillis=200ms | 中等负载 |
| 网关服务 | 2GB | G1GC, MaxGCPauseMillis=100ms | 极低延迟要求 |
| 注册中心 | 512MB | G1GC, MaxGCPauseMillis=200ms | 低负载 |
| 配置中心 | 512MB | G1GC, MaxGCPauseMillis=200ms | 低负载 |

### 8.2 数据库查询优化

#### 8.2.1 慢查询分析

```sql
-- 启用慢查询日志
ALTER SYSTEM SET log_min_duration_statement = 1000;
SELECT pg_reload_conf();

-- 查看慢查询
SELECT query, mean_exec_time, calls, total_exec_time
FROM pg_stat_statements
ORDER BY mean_exec_time DESC
LIMIT 10;
```

#### 8.2.2 查询优化建议

```sql
-- 使用EXPLAIN分析查询计划
EXPLAIN ANALYZE
SELECT p.name, i.quantity
FROM products p
JOIN inventory i ON p.id = i.product_id
WHERE p.category_id = 1;

-- 创建适当的索引
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_inventory_product ON inventory(product_id);

-- 使用复合索引
CREATE INDEX idx_orders_customer_status ON orders(customer_id, status);

-- 定期更新统计信息
ANALYZE products;
ANALYZE orders;
ANALYZE inventory;
```

#### 8.2.3 连接池优化

```yaml
# application-prod.yml
spring:
  datasource:
    hikari:
      # 根据实际负载调整
      maximum-pool-size: 100          # 最大连接数
      minimum-idle: 20               # 最小空闲连接数
      connection-timeout: 30000       # 连接超时时间（毫秒）
      idle-timeout: 600000            # 空闲超时时间（毫秒）
      max-lifetime: 1800000          # 连接最大生命周期（毫秒）
      leak-detection-threshold: 60000 # 泄露检测阈值（毫秒）
```

### 8.3 缓存策略优化

#### 8.3.1 Redis缓存配置

```java
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 产品缓存 - 1小时
        cacheConfigurations.put("products", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .disableCachingNullValues()
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer())));
        
        // 库存缓存 - 5分钟（高频更新）
        cacheConfigurations.put("inventory", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .disableCachingNullValues()
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer())));
        
        // 订单缓存 - 30分钟
        cacheConfigurations.put("orders", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .disableCachingNullValues()
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer())));
        
        // 客户缓存 - 2小时
        cacheConfigurations.put("customers", RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(2))
            .disableCachingNullValues()
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer())));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                    .fromSerializer(new GenericJackson2JsonRedisSerializer())))
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();
    }
}
```

#### 8.3.2 缓存预热

```java
@Component
public class CacheWarmupService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    @PostConstruct
    public void warmupCache() {
        log.info("开始缓存预热...");
        
        // 预热产品缓存
        List<Product> products = productRepository.findAll();
        Cache productsCache = cacheManager.getCache("products");
        if (productsCache != null) {
            products.forEach(product -> 
                productsCache.put(product.getId(), product));
        }
        
        log.info("缓存预热完成，共加载 {} 个产品", products.size());
    }
}
```

### 8.4 网络连接优化

#### 8.4.1 Tomcat连接器优化

```yaml
# application-prod.yml
server:
  tomcat:
    threads:
      max: 500                        # 最大线程数
      min-spare: 50                   # 最小空闲线程数
    max-connections: 10000            # 最大连接数
    accept-count: 200                  # 等待队列长度
    connection-timeout: 60000         # 连接超时时间（毫秒）
    keep-alive-timeout: 30000         # 保持连接超时时间（毫秒）
    max-http-form-post-size: 10MB     # 最大POST请求大小
```

#### 8.4.2 HTTP客户端优化

```java
@Configuration
public class HttpClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory();
        
        // 连接池配置
        PoolingHttpClientConnectionManager connectionManager = 
            new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(50);
        
        // 请求配置
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(5000)
            .setConnectionRequestTimeout(5000)
            .setSocketTimeout(30000)
            .build();
        
        // 构建HttpClient
        CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .build();
        
        factory.setHttpClient(httpClient);
        return new RestTemplate(factory);
    }
}
```

### 8.5 资源限制配置

#### 8.5.1 Docker资源限制

```yaml
# docker-compose.prod.yml
services:
  product-service:
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: '1G'
        reservations:
          cpus: '0.5'
          memory: '512M'

  order-service:
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: '1G'
        reservations:
          cpus: '0.5'
          memory: '512M'

  postgres:
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: '8G'
        reservations:
          cpus: '1.0'
          memory: '4G'

  redis:
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: '6G'
        reservations:
          cpus: '0.5'
          memory: '4G'
```

#### 8.5.2 系统资源监控

```bash
# 监控CPU使用率
top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%* id.*/\1/" | awk '{print 100 - $1}'

# 监控内存使用率
free | grep Mem | awk '{print ($3/$2) * 100.0}'

# 监控磁盘使用率
df -h | awk '$NF=="/"{print $5}'

# 监控网络流量
iftop -i eth0 -t -s 10
```

## 9. 备份和恢复

### 9.1 备份策略

#### 9.1.1 RTO和RPO定义

| 服务类型 | RTO | RPO | 说明 |
|---------|-----|-----|------|
| 核心服务 | 1小时 | 15分钟 | 产品、订单、库存等 |
| 支持服务 | 4小时 | 1小时 | 管理、财务等 |
| 数据库 | 30分钟 | 5分钟 | PostgreSQL |
| 缓存 | 1小时 | 30分钟 | Redis |
| 配置 | 10分钟 | 0 | 配置文件 |

#### 9.1.2 备份计划

**数据库备份**：

```bash
#!/bin/bash
# backup-database.sh

BACKUP_DIR="/opt/backups/database"
DATE=$(date +%Y%m%d_%H%M%S)

# 创建备份目录
mkdir -p $BACKUP_DIR

# 全量备份
docker exec postgres pg_dump -U postgres -d inventory > $BACKUP_DIR/inventory_full_$DATE.sql

# 压缩备份
gzip $BACKUP_DIR/inventory_full_$DATE.sql

# 删除30天前的备份
find $BACKUP_DIR -name "inventory_full_*.sql.gz" -mtime +30 -delete

# 上传到异地存储
# aws s3 cp $BACKUP_DIR/inventory_full_$DATE.sql.gz s3://backup-bucket/inventory/
```

**配置文件备份**：

```bash
#!/bin/bash
# backup-config.sh

BACKUP_DIR="/opt/backups/config"
DATE=$(date +%Y%m%d_%H%M%S)

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份配置文件
tar -czf $BACKUP_DIR/config_$DATE.tar.gz \
  /opt/inventory-system/config \
  /opt/inventory-system/docker-compose.yml \
  /opt/inventory-system/docker-compose.prod.yml \
  /opt/inventory-system/.env

# 删除90天前的备份
find $BACKUP_DIR -name "config_*.tar.gz" -mtime +90 -delete
```

**日志备份**：

```bash
#!/bin/bash
# backup-logs.sh

BACKUP_DIR="/opt/backups/logs"
DATE=$(date +%Y%m%d)

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份日志文件
tar -czf $BACKUP_DIR/logs_$DATE.tar.gz /opt/inventory-system/logs/

# 删除90天前的备份
find $BACKUP_DIR -name "logs_*.tar.gz" -mtime +90 -delete
```

#### 9.1.3 定时备份配置

```bash
# 编辑crontab
crontab -e

# 每日凌晨2点执行数据库全量备份
0 2 * * * /opt/scripts/backup-database.sh

# 每日凌晨3点执行配置文件备份
0 3 * * * /opt/scripts/backup-config.sh

# 每周日凌晨4点执行日志备份
0 4 * * 0 /opt/scripts/backup-logs.sh

# 每小时执行数据库增量备份
0 * * * * docker exec postgres pg_dump -U postgres -d inventory --schema-only > /opt/backups/database/incremental_$(date +\%Y\%m\%d_\%H).sql
```

### 9.2 数据库备份

#### 9.2.1 PostgreSQL备份

```bash
# 全量备份
docker exec postgres pg_dump -U postgres -d inventory > backup-full.sql

# 仅备份结构
docker exec postgres pg_dump -U postgres -d inventory --schema-only > backup-schema.sql

# 仅备份数据
docker exec postgres pg_dump -U postgres -d inventory --data-only > backup-data.sql

# 备份特定表
docker exec postgres pg_dump -U postgres -d inventory -t products -t orders > backup-tables.sql

# 使用pg_dumpall备份所有数据库
docker exec postgres pg_dumpall -U postgres > backup-all.sql
```

#### 9.2.2 PostgreSQL恢复

```bash
# 恢复完整备份
docker exec -i postgres psql -U postgres -d inventory < backup-full.sql

# 恢复特定表
docker exec -i postgres psql -U postgres -d inventory < backup-tables.sql

# 使用pg_restore恢复自定义格式备份
docker exec -i postgres pg_restore -U postgres -d inventory < backup.custom
```

### 9.3 配置备份

#### 9.3.1 配置文件备份

```bash
# 备份所有配置文件
tar -czf config-backup.tar.gz \
  /opt/inventory-system/config/*.yml \
  /opt/inventory-system/docker-compose*.yml \
  /opt/inventory-system/.env

# 备份特定配置
cp /opt/inventory-system/config/application-prod.yml /opt/backups/config/application-prod.yml.backup
```

#### 9.3.2 配置恢复

```bash
# 恢复配置文件
tar -xzf config-backup.tar.gz -C /opt/inventory-system/

# 恢复特定配置
cp /opt/backups/config/application-prod.yml.backup /opt/inventory-system/config/application-prod.yml
```

### 9.4 恢复流程

#### 9.4.1 数据库恢复流程

```bash
#!/bin/bash
# restore-database.sh

BACKUP_FILE=$1

if [ -z "$BACKUP_FILE" ]; then
    echo "请指定备份文件"
    exit 1
fi

# 停止所有服务
docker-compose -f docker-compose.yml -f docker-compose.prod.yml down

# 启动数据库
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d postgres

# 等待数据库就绪
sleep 30

# 恢复数据库
docker exec -i postgres psql -U postgres -d inventory < $BACKUP_FILE

# 重启所有服务
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

echo "数据库恢复完成！"
```

#### 9.4.2 完整系统恢复流程

```bash
#!/bin/bash
# restore-system.sh

echo "开始系统恢复..."

# 步骤1：恢复配置文件
echo "步骤1：恢复配置文件..."
tar -xzf /opt/backups/config/config_*.tar.gz -C /opt/inventory-system/

# 步骤2：恢复数据库
echo "步骤2：恢复数据库..."
/opt/scripts/restore-database.sh /opt/backups/database/inventory_full_*.sql.gz

# 步骤3：重启所有服务
echo "步骤3：重启所有服务..."
docker-compose -f docker-compose.yml -f docker-compose.prod.yml restart

# 步骤4：验证服务状态
echo "步骤4：验证服务状态..."
/opt/scripts/health-check.sh

echo "系统恢复完成！"
```

### 9.5 灾难恢复演练

#### 9.5.1 演练计划

**演练频率**：每季度一次

**演练内容**：
1. 模拟数据库故障
2. 执行数据库恢复
3. 验证数据完整性
4. 验证服务可用性
5. 记录恢复时间

#### 9.5.2 演练脚本

```bash
#!/bin/bash
# disaster-recovery-drill.sh

echo "开始灾难恢复演练..."

# 记录开始时间
START_TIME=$(date +%s)

# 步骤1：模拟数据库故障
echo "步骤1：模拟数据库故障..."
docker-compose -f docker-compose.yml -f docker-compose.prod.yml stop postgres

# 步骤2：验证服务不可用
echo "步骤2：验证服务不可用..."
curl -f http://localhost:8082/actuator/health || echo "服务不可用（预期）"

# 步骤3：恢复数据库
echo "步骤3：恢复数据库..."
docker-compose -f docker-compose.yml -f docker-compose.prod.yml start postgres
sleep 30

# 步骤4：验证数据完整性
echo "步骤4：验证数据完整性..."
docker exec postgres psql -U postgres -d inventory -c "SELECT COUNT(*) FROM products;"

# 步骤5：验证服务可用性
echo "步骤5：验证服务可用性..."
curl -f http://localhost:8082/actuator/health && echo "服务可用"

# 记录结束时间
END_TIME=$(date +%s)
RECOVERY_TIME=$((END_TIME - START_TIME))

echo "灾难恢复演练完成！"
echo "恢复时间：${RECOVERY_TIME}秒"

# 记录演练结果
echo "$(date): 灾难恢复演练完成，恢复时间：${RECOVERY_TIME}秒" >> /opt/backups/drill-log.txt
```

## 10. 运维操作

### 10.1 服务管理命令

#### 10.1.1 启动和停止服务

```bash
# 启动所有服务
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# 停止所有服务
docker-compose -f docker-compose.yml -f docker-compose.prod.yml down

# 重启所有服务
docker-compose -f docker-compose.yml -f docker-compose.prod.yml restart

# 启动特定服务
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d product-service

# 停止特定服务
docker-compose -f docker-compose.yml -f docker-compose.prod.yml stop product-service

# 重启特定服务
docker-compose -f docker-compose.yml -f docker-compose.prod.yml restart product-service
```

#### 10.1.2 查看服务状态

```bash
# 查看所有服务状态
docker-compose -f docker-compose.yml -f docker-compose.prod.yml ps

# 查看服务日志
docker-compose -f docker-compose.yml -f docker-compose.prod.yml logs -f

# 查看特定服务日志
docker-compose -f docker-compose.yml -f docker-compose.prod.yml logs -f product-service

# 查看服务资源使用情况
docker stats
```

#### 10.1.3 服务扩缩容

```bash
# 扩容服务到3个实例
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --scale product-service=3

# 缩容服务到1个实例
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --scale product-service=1
```

### 10.2 日志管理

#### 10.2.1 查看日志

```bash
# 查看实时日志
docker-compose -f docker-compose.yml -f docker-compose.prod.yml logs -f

# 查看最近100行日志
docker-compose -f docker-compose.yml -f docker-compose.prod.yml logs --tail=100

# 查看特定服务的日志
docker logs -f product-service

# 查看特定时间段的日志
docker logs --since 2025-01-19T00:00:00 --until 2025-01-19T23:59:59 product-service
```

#### 10.2.2 日志分析

```bash
# 统计错误日志数量
docker-compose -f docker-compose.yml -f docker-compose.prod.yml logs | grep -i error | wc -l

# 查找慢查询
docker logs postgres | grep "duration:" | awk '$NF > 1000'

# 分析访问日志
docker logs gateway-service | grep "GET /api/products" | awk '{print $1}' | sort | uniq -c | sort -nr
```

#### 10.2.3 日志归档

```bash
#!/bin/bash
# archive-logs.sh

LOG_DIR="/opt/inventory-system/logs"
ARCHIVE_DIR="/opt/backups/logs"
DATE=$(date +%Y%m%d)

# 创建归档目录
mkdir -p $ARCHIVE_DIR

# 归档日志文件
find $LOG_DIR -name "*.log" -mtime +7 -exec gzip {} \;

# 移动归档文件
find $LOG_DIR -name "*.gz" -exec mv {} $ARCHIVE_DIR/ \;

# 删除90天前的归档文件
find $ARCHIVE_DIR -name "*.gz" -mtime +90 -delete
```

### 10.3 配置更新流程

#### 10.3.1 配置更新步骤

```bash
#!/bin/bash
# update-config.sh

CONFIG_FILE=$1

if [ -z "$CONFIG_FILE" ]; then
    echo "请指定配置文件"
    exit 1
fi

# 步骤1：备份当前配置
echo "步骤1：备份当前配置..."
cp /opt/inventory-system/config/$CONFIG_FILE /opt/backups/config/${CONFIG_FILE}.backup.$(date +%Y%m%d_%H%M%S)

# 步骤2：更新配置文件
echo "步骤2：更新配置文件..."
cp $CONFIG_FILE /opt/inventory-system/config/

# 步骤3：重启相关服务
echo "步骤3：重启相关服务..."
docker-compose -f docker-compose.yml -f docker-compose.prod.yml restart

# 步骤4：验证配置
echo "步骤4：验证配置..."
/opt/scripts/health-check.sh

echo "配置更新完成！"
```

#### 10.3.2 滚动更新

```bash
#!/bin/bash
# rolling-update.sh

SERVICE=$1
NEW_IMAGE=$2

if [ -z "$SERVICE" ] || [ -z "$NEW_IMAGE" ]; then
    echo "请指定服务和新镜像"
    exit 1
fi

echo "开始滚动更新 $SERVICE..."

# 获取当前副本数
CURRENT_REPLICAS=$(docker-compose -f docker-compose.yml -f docker-compose.prod.yml ps -q $SERVICE | wc -l)

# 逐个更新实例
for i in $(seq 1 $CURRENT_REPLICAS); do
    echo "更新实例 $i/$CURRENT_REPLICAS..."
    
    # 更新一个实例
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --no-deps --scale $SERVICE=$i $SERVICE
    
    # 等待实例就绪
    sleep 30
    
    # 健康检查
    if curl -f http://localhost:8082/actuator/health; then
        echo "实例 $i 更新成功"
    else
        echo "实例 $i 更新失败，回滚..."
        docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --no-deps --scale $SERVICE=$CURRENT_REPLICAS $SERVICE
        exit 1
    fi
done

echo "滚动更新完成！"
```

### 10.4 版本升级

#### 10.4.1 升级前准备

```bash
#!/bin/bash
# pre-upgrade-check.sh

echo "升级前检查..."

# 检查服务状态
echo "检查服务状态..."
/opt/scripts/health-check.sh

# 检查磁盘空间
echo "检查磁盘空间..."
df -h

# 检查内存使用
echo "检查内存使用..."
free -h

# 创建备份
echo "创建备份..."
/opt/scripts/backup-database.sh
/opt/scripts/backup-config.sh

echo "升级前检查完成！"
```

#### 10.4.2 升级流程

```bash
#!/bin/bash
# upgrade.sh

NEW_VERSION=$1

if [ -z "$NEW_VERSION" ]; then
    echo "请指定新版本"
    exit 1
fi

echo "开始升级到版本 $NEW_VERSION..."

# 步骤1：升级前检查
echo "步骤1：升级前检查..."
/opt/scripts/pre-upgrade-check.sh

# 步骤2：拉取新镜像
echo "步骤2：拉取新镜像..."
docker pull inventory-system/product-service:$NEW_VERSION
docker pull inventory-system/order-service:$NEW_VERSION
docker pull inventory-system/inventory-service:$NEW_VERSION

# 步骤3：更新docker-compose文件
echo "步骤3：更新docker-compose文件..."
sed -i "s/:latest/:$NEW_VERSION/g" docker-compose.prod.yml

# 步骤4：滚动更新服务
echo "步骤4：滚动更新服务..."
/opt/scripts/rolling-update.sh product-service inventory-system/product-service:$NEW_VERSION
/opt/scripts/rolling-update.sh order-service inventory-system/order-service:$NEW_VERSION
/opt/scripts/rolling-update.sh inventory-service inventory-system/inventory-service:$NEW_VERSION

# 步骤5：验证升级
echo "步骤5：验证升级..."
/opt/scripts/health-check.sh

echo "升级完成！"
```

### 10.5 故障排查步骤

#### 10.5.1 服务无法启动

```bash
# 查看服务日志
docker-compose -f docker-compose.yml -f docker-compose.prod.yml logs $SERVICE

# 检查容器状态
docker ps -a | grep $SERVICE

# 检查资源使用
docker stats

# 检查端口占用
netstat -tulpn | grep $PORT

# 检查配置文件
cat /opt/inventory-system/config/application-prod.yml
```

#### 10.5.2 性能问题诊断

```bash
# 查看CPU使用率
top -bn1 | grep "Cpu(s)"

# 查看内存使用
free -h

# 查看磁盘IO
iostat -x 1 5

# 查看网络流量
iftop -i eth0

# 查看数据库连接
docker exec postgres psql -U postgres -d inventory -c "SELECT count(*) FROM pg_stat_activity;"

# 查看慢查询
docker logs postgres | grep "duration:"
```

#### 10.5.3 数据库问题诊断

```bash
# 检查数据库连接
docker exec postgres psql -U postgres -d inventory -c "SELECT version();"

# 检查数据库大小
docker exec postgres psql -U postgres -d inventory -c "SELECT pg_size_pretty(pg_database_size('inventory'));"

# 检查表大小
docker exec postgres psql -U postgres -d inventory -c "SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) FROM pg_tables WHERE schemaname = 'public' ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;"

# 检查索引使用情况
docker exec postgres psql -U postgres -d inventory -c "SELECT schemaname, tablename, indexname, idx_scan FROM pg_stat_user_indexes ORDER BY idx_scan;"

# 检查锁等待
docker exec postgres psql -U postgres -d inventory -c "SELECT * FROM pg_stat_activity WHERE wait_event_type = 'Lock';"
```

## 11. 最佳实践

### 11.1 部署最佳实践

#### 11.1.1 蓝绿部署

```bash
#!/bin/bash
# blue-green-deploy.sh

ENVIRONMENT=$1
VERSION=$2

if [ "$ENVIRONMENT" = "blue" ]; then
    TARGET="green"
else
    TARGET="blue"
fi

echo "部署到 $TARGET 环境..."

# 构建新镜像
docker build -t inventory-system:$VERSION .

# 启动新环境
docker-compose -f docker-compose.$TARGET.yml up -d

# 健康检查
sleep 30
if curl -f http://localhost:9090/actuator/health; then
    echo "部署成功，切换流量到 $TARGET"
    # 切换负载均衡器流量
    # ...
else
    echo "部署失败，回滚"
    docker-compose -f docker-compose.$TARGET.yml down
    exit 1
fi
```

#### 11.1.2 金丝雀发布

```bash
#!/bin/bash
# canary-deploy.sh

VERSION=$1
CANARY_PERCENTAGE=10

echo "金丝雀发布版本 $VERSION，流量比例 $CANARY_PERCENTAGE%"

# 启动金丝雀实例
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --scale product-service=2

# 配置负载均衡器路由10%流量到新版本
# ...

# 监控金丝雀实例
sleep 300
if curl -f http://localhost:8082/actuator/health; then
    echo "金丝雀发布成功，逐步扩大流量"
    # 逐步扩大流量比例
    # ...
else
    echo "金丝雀发布失败，回滚"
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --scale product-service=1
    exit 1
fi
```

### 11.2 监控最佳实践

#### 11.2.1 监控指标

**核心指标**：
- 服务可用性（Uptime）
- 请求速率（Request Rate）
- 错误率（Error Rate）
- 响应时间（Response Time）

**JVM指标**：
- 堆内存使用率
- GC频率和停顿时间
- 线程数
- 类加载数

**系统指标**：
- CPU使用率
- 内存使用率
- 磁盘使用率
- 网络流量

**业务指标**：
- 订单量
- 库存周转率
- 客户活跃度
- 销售额

#### 11.2.2 告警规则

**关键告警**：
- 服务宕机（立即通知）
- 数据库连接池耗尽（立即通知）
- 系统资源耗尽（立即通知）

**警告告警**：
- 高错误率（5分钟内）
- 高响应时间（5分钟内）
- 高CPU/内存使用率（10分钟内）

**信息告警**：
- 配置变更
- 版本升级
- 定期备份完成

### 11.3 安全最佳实践

#### 11.3.1 密钥管理

- 使用Docker Secrets管理敏感信息
- 定期轮换密钥和证书
- 使用强密码策略
- 启用多因素认证

#### 11.3.2 网络安全

- 使用SSL/TLS加密所有通信
- 配置防火墙规则限制访问
- 使用VPN访问生产环境
- 定期审计网络访问日志

#### 11.3.3 访问控制

- 实施最小权限原则
- 定期审计用户权限
- 启用审计日志
- 及时撤销离职员工权限

### 11.4 性能优化最佳实践

#### 11.4.1 数据库优化

- 创建适当的索引
- 定期更新统计信息
- 优化慢查询
- 使用连接池

#### 11.4.2 缓存优化

- 使用多级缓存
- 设置合理的过期时间
- 缓存预热
- 缓存穿透保护

#### 11.4.3 应用优化

- 使用异步处理
- 批量操作
- 连接池优化
- JVM参数调优

## 12. 故障排除

### 12.1 常见问题和解决方案

#### 12.1.1 服务启动失败

**问题**：服务容器启动后立即退出

**解决方案**：
```bash
# 查看容器日志
docker logs $CONTAINER_ID

# 检查配置文件
cat /opt/inventory-system/config/application-prod.yml

# 检查环境变量
docker exec $CONTAINER_ID env

# 检查端口占用
netstat -tulpn | grep $PORT
```

#### 12.1.2 数据库连接失败

**问题**：应用无法连接到数据库

**解决方案**：
```bash
# 检查数据库状态
docker ps | grep postgres

# 检查数据库连接
docker exec postgres psql -U postgres -d inventory -c "SELECT version();"

# 检查连接池配置
cat /opt/inventory-system/config/application-prod.yml | grep -A 10 hikari

# 检查网络连接
docker exec $SERVICE_CONTAINER ping postgres
```

#### 12.1.3 内存溢出

**问题**：服务因内存不足而崩溃

**解决方案**：
```bash
# 查看堆转储文件
ls -lh /tmp/heapdump.hprof

# 分析堆转储
jhat -port 7000 /tmp/heapdump.hprof

# 增加堆内存大小
# 修改docker-compose.prod.yml中的JAVA_OPTS

# 优化内存使用
# 检查是否有内存泄漏
```

#### 12.1.4 GC停顿时间过长

**问题**：应用响应时间因GC停顿而变慢

**解决方案**：
```bash
# 查看GC日志
cat /tmp/gc.log

# 调整GC参数
# 修改JAVA_OPTS中的GC参数

# 增加堆内存
# 减少对象创建
```

### 12.2 性能问题诊断

#### 12.2.1 高CPU使用率

**诊断步骤**：
```bash
# 查看CPU使用率
top -bn1 | grep "Cpu(s)"

# 查看进程CPU使用
top -p $PID

# 生成线程转储
jstack $PID > thread-dump.txt

# 分析线程转储
# 查找死锁、长时间运行的线程
```

#### 12.2.2 高内存使用率

**诊断步骤**：
```bash
# 查看内存使用
free -h

# 查看进程内存使用
ps aux | grep $PROCESS_NAME

# 生成堆转储
jmap -dump:format=b,file=heapdump.hprof $PID

# 分析堆转储
jhat -port 7000 heapdump.hprof
```

#### 12.2.3 高响应时间

**诊断步骤**：
```bash
# 查看应用日志
docker logs $SERVICE_CONTAINER | grep "duration:"

# 查看数据库慢查询
docker logs postgres | grep "duration:"

# 查看网络延迟
ping $TARGET_HOST

# 使用分布式追踪
# 访问Jaeger UI分析请求链路
```

### 12.3 服务故障恢复

#### 12.3.1 自动重启

```yaml
# docker-compose.prod.yml
services:
  product-service:
    restart: unless-stopped
    deploy:
      restart_policy:
        condition: on-failure
        max_attempts: 3
        window: 60s
```

#### 12.3.2 手动恢复

```bash
#!/bin/bash
# manual-recovery.sh

SERVICE=$1

echo "手动恢复服务 $SERVICE..."

# 停止服务
docker-compose -f docker-compose.yml -f docker-compose.prod.yml stop $SERVICE

# 删除容器
docker-compose -f docker-compose.yml -f docker-compose.prod.yml rm -f $SERVICE

# 启动服务
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d $SERVICE

# 验证服务
sleep 30
if curl -f http://localhost:8082/actuator/health; then
    echo "服务恢复成功"
else
    echo "服务恢复失败"
    exit 1
fi
```

### 12.4 紧急联系方式

| 角色 | 姓名 | 电话 | 邮箱 |
|------|------|------|------|
| 运维负责人 | - | - | ops@example.com |
| 开发负责人 | - | - | dev@example.com |
| DBA | - | - | dba@example.com |
| 安全负责人 | - | - | security@example.com |

## 附录

### A. 参考文档

- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [Docker官方文档](https://docs.docker.com/)
- [PostgreSQL官方文档](https://www.postgresql.org/docs/)
- [Redis官方文档](https://redis.io/documentation)
- [Prometheus官方文档](https://prometheus.io/docs/)
- [Grafana官方文档](https://grafana.com/docs/)
- [Jaeger官方文档](https://www.jaegertracing.io/docs/)

### B. 相关工具

- **监控工具**：Prometheus、Grafana、Jaeger
- **日志工具**：ELK Stack、Loki
- **部署工具**：Docker、Docker Compose、Kubernetes
- **CI/CD工具**：GitHub Actions、Jenkins、GitLab CI
- **性能测试工具**：JMeter、Gatling、Locust

### C. 常用命令速查

```bash
# Docker命令
docker ps                          # 查看运行中的容器
docker logs <container>             # 查看容器日志
docker exec -it <container> bash    # 进入容器
docker stats                        # 查看容器资源使用

# Docker Compose命令
docker-compose up -d               # 启动服务
docker-compose down                # 停止服务
docker-compose logs -f             # 查看日志
docker-compose restart             # 重启服务

# PostgreSQL命令
docker exec -it postgres psql -U postgres -d inventory  # 连接数据库
docker exec postgres pg_dump -U postgres -d inventory > backup.sql  # 备份数据库
docker exec -i postgres psql -U postgres -d inventory < backup.sql  # 恢复数据库

# Redis命令
docker exec -it redis redis-cli    # 连接Redis
docker exec redis redis-cli FLUSHALL  # 清空所有数据

# 系统监控命令
top                               # 查看系统资源使用
free -h                           # 查看内存使用
df -h                             # 查看磁盘使用
netstat -tulpn                    # 查看网络连接
```

### D. 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|---------|
| 1.0.0 | 2025-01-19 | 架构师团队 | 初始版本 |

---

**文档结束**
