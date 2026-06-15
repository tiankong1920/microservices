# 进销存管理系统 - 部署指南

> 版本: 3.0.0 | 最后更新: 2026-06-05

---

## 1. 环境要求

### 1.1 硬件要求

| 环境 | CPU | 内存 | 磁盘 |
|------|-----|------|------|
| 开发环境 | 4 核 | 8 GB | 50 GB SSD |
| 测试环境 | 8 核 | 16 GB | 100 GB SSD |
| 生产环境 | 16 核 | 32 GB | 200 GB SSD（建议RAID） |

### 1.2 软件要求

| 软件 | 版本 | 用途 |
|------|------|------|
| JDK | 21 | 后端运行时 |
| Node.js | 20 LTS | 前端构建 |
| Docker | 24+ | 容器化部署 |
| Docker Compose | v2+ | 容器编排 |
| PostgreSQL | 16+ | 主数据库 |
| Redis | 7.0+ | 缓存/会话 |
| Nacos | 2.4.x | 服务发现/配置中心 |
| Gradle | 8.6 | 项目构建 |
| Nginx | 1.25+ | 前端/反向代理 |

### 1.3 网络要求

#### 端口清单

| 服务 | 端口 | 协议 | 外部暴露 | 说明 |
|------|------|------|----------|------|
| gateway-service | 8080 | HTTP | 是（生产映射为80） | API 网关入口 |
| product-service | 8081 | HTTP | 否 | 产品服务 |
| order-service | 8082 | HTTP | 否 | 订单服务 |
| inventory-service | 8083 | HTTP | 否 | 库存服务 |
| sales-service | 8084 | HTTP | 否 | 销售服务 |
| procurement-service | 8085 | HTTP | 否 | 采购服务 |
| customer-service | 8086 | HTTP | 否 | 客户服务 |
| supplier-service | 8087 | HTTP | 否 | 供应商服务 |
| business-partner-service | 8088 | HTTP | 否 | 业务伙伴服务 |
| admin-service | 8091 | HTTP | 否 | 管理服务 |
| finance-service | 8092 | HTTP | 否 | 财务服务 |
| auth-service | 8093 | HTTP | 否 | 认证服务 |
| report-service | 8094 | HTTP | 否 | 报表服务 |
| registry-service | 8761 | HTTP | 否 | 注册中心（Eureka） |
| config-service | 8888 | HTTP | 否 | 配置服务 |
| PostgreSQL | 5432 | TCP | 否 | 数据库 |
| Redis | 6379 | TCP | 否 | 缓存（开发映射7379） |
| Nacos | 8848/9848 | HTTP/gRPC | 否 | 服务发现 |
| 前端（开发） | 3000 | HTTP | 是 | Vue/React 开发服务器 |
| 前端（生产） | 80/443 | HTTP/HTTPS | 是 | Nginx 静态文件 |

---

## 2. 部署架构

### 2.1 部署拓扑

```
                          Internet
                             |
                        [Nginx:80/443]
                             |
                    [Gateway:8080]  ← 统一入口，JWT鉴权，限流
                         /       \
            ┌─────────────         ─────────────┐
            |                                    |
    ┌───────┴───────┐                    ┌───────┴───────┐
    │  核心服务层    │                    │  支撑服务层    │
    ├───────────────┤                    ├───────────────┤
    │ product:8081  │                    │ admin:8091    │
    │ order:8082    │                    │ finance:8092  │
    │ inventory:8083│                    │ auth:8093     │
    │ sales:8084    │                    │ report:8094   │
    │ procurement:8085                   │ registry:8761 │
    │ customer:8086 │                    │ config:8888   │
    │ supplier:8087 │                    └───────┬───────┘
    │ bizpartner:8088                            │
    └───────┬───────┘                            │
            |                                    |
            └──────────────┬─────────────────────┘
                           |
            ┌──────────────┴──────────────┐
            │        基础设施层             │
            ├─────────────────────────────┤
            │ PostgreSQL:5432 (主库)      │
            │ Redis:6379 (缓存/会话)      │
            │ Nacos:8848 (注册/配置)      │
            │ Nacos gRPC:9848            │
            └─────────────────────────────┘
```

### 2.2 Nacos 服务分组

| 服务 | Nacos Group |
|------|-------------|
| product-service | core-product-management |
| order-service | core-order-processing |
| inventory-service | core-inventory-management |
| sales-service | core-sales-processing |
| procurement-service | core-procurement-management |
| customer-service | core-customer-management |
| supplier-service | core-supplier-management |
| business-partner-service | core-business-partner |
| admin-service | DEFAULT_GROUP |
| auth-service | support-auth |
| finance-service | support-finance-processing |
| report-service | support-report |
| registry-service | support-service-registry |
| config-service | support-config-management |
| gateway-service | DEFAULT_GROUP |

---

## 3. 快速部署（Docker Compose）

### 3.1 开发环境

#### 步骤 1: 克隆项目并准备环境

```bash
# 克隆仓库
git clone ${GIT_REPO_URL} inventory-system
cd inventory-system

# 复制环境变量模板
cp microservices/.env.example microservices/.env
# 编辑 .env 文件，修改数据库密码等敏感信息
```

#### 步骤 2: 启动基础设施

```bash
cd microservices

# 启动基础设施服务（PostgreSQL + Redis + Nacos）
docker compose -f docker-compose.yml up -d postgres redis nacos

# 预期输出:
# [+] Running 3/3
#  ✔ Container inventory-postgres  Started
#  ✔ Container inventory-redis     Started
#  ✔ Container inventory-nacos     Started
```

#### 步骤 3: 验证基础设施

```bash
# 检查服务状态
docker compose -f docker-compose.yml ps

# 检查 PostgreSQL
docker exec inventory-postgres pg_isready -U postgres -d inventory_db
# 预期输出: /var/run/postgresql:5432 - accepting connections

# 检查 Redis
docker exec inventory-redis redis-cli ping
# 预期输出: PONG

# 检查 Nacos
curl -s http://localhost:8848/nacos/v1/console/health/readiness
# 预期输出: ok
```

#### 步骤 4: 使用脚本一键启动所有服务

```bash
# 执行启动脚本
bash scripts/start-services.sh

# 预期输出:
# Starting infrastructure services...
# Starting support services...
# Starting core services...
# Starting gateway...
# All services started. Verifying health...
```

#### 步骤 5: 验证所有服务

```bash
# 检查所有服务状态
docker compose -f docker-compose.yml ps

# 测试 API 网关
curl http://localhost:8080/actuator/health
# 预期输出: {"status":"UP"}

# 测试各核心服务
curl http://localhost:8081/actuator/health  # product-service
curl http://localhost:8082/actuator/health  # order-service
curl http://localhost:8083/actuator/health  # inventory-service
```

### 3.2 生产环境

#### 步骤 1: 准备环境变量

```bash
cd microservices

# 创建生产环境变量文件
cat > .env.prod << 'EOF'
# 应用配置
APP_NAME=inventory-management-system
APP_VERSION=3.0.0
SPRING_PROFILES_ACTIVE=prod

# 数据库（生产环境必须使用强密码）
POSTGRES_HOST=postgres
POSTGRES_PORT=5432
POSTGRES_DB=inventory_db
POSTGRES_USER=inventory
POSTGRES_PASSWORD=${YOUR_STRONG_PASSWORD}

# Redis（生产环境必须设置密码）
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=${YOUR_REDIS_PASSWORD}

# Nacos
NACOS_SERVER_ADDR=nacos:8848
NACOS_AUTH_TOKEN=${YOUR_NACOS_TOKEN}
NACOS_IDENTITY_VALUE=${YOUR_NACOS_IDENTITY}

# JWT（生产环境必须使用强密钥，至少256位）
JWT_SECRET=${YOUR_JWT_SECRET_AT_LEAST_256_BITS}

# 镜像仓库
REGISTRY=ghcr.io/
VERSION=3.0.0

# Kafka（可选）
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
EOF

# 设置文件权限
chmod 600 .env.prod
```

#### 步骤 2: 执行生产部署

```bash
# 部署到生产环境
bash scripts/deploy-prod.sh deploy

# 预期输出:
# [2026-06-05 10:00:00] Checking deployment requirements...
# [2026-06-05 10:00:01] All requirements met
# [2026-06-05 10:00:01] Creating database backup...
# [2026-06-05 10:00:05] Database backup created: ./backups/postgres_backup_20260605_100000.sql.gz
# [2026-06-05 10:00:05] Pulling latest images...
# [2026-06-05 10:01:00] Images updated
# [2026-06-05 10:01:00] Deploying services...
# [2026-06-05 10:02:00] Services deployed
# [2026-06-05 10:02:00] Performing health checks...
# [2026-06-05 10:02:30] gateway-service is healthy
# [2026-06-05 10:02:30] product-service is healthy
# ...
# [2026-06-05 10:03:00] Health checks completed
```

#### 步骤 3: 查看部署状态

```bash
bash scripts/deploy-prod.sh status

# 预期输出:
# Service health:
#   postgres: healthy
#   redis: healthy
#   nacos: healthy
#   gateway-service: healthy
#   product-service: healthy
#   order-service: healthy
#   inventory-service: healthy
#   ...
```

#### 步骤 4: 数据库备份

```bash
# 手动创建数据库备份
bash scripts/deploy-prod.sh backup
```

#### 步骤 5: 清理资源

```bash
# 清理未使用的 Docker 镜像
bash scripts/deploy-prod.sh cleanup
```

---

## 4. 手动部署

### 4.1 基础设施部署

#### 4.1.1 PostgreSQL 部署

```bash
# 使用 Docker 部署
docker run -d \
  --name inventory-postgres \
  --restart always \
  -e POSTGRES_DB=inventory_db \
  -e POSTGRES_USER=${POSTGRES_USER} \
  -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \
  -p 5432:5432 \
  -v postgres-data:/var/lib/postgresql/data \
  postgres:16-alpine

# 验证
docker exec inventory-postgres pg_isready -U ${POSTGRES_USER} -d inventory_db
```

或者使用系统级安装：

```bash
# Ubuntu/Debian
sudo apt update && sudo apt install -y postgresql-16
sudo systemctl enable postgresql
sudo systemctl start postgresql

# 创建数据库和用户
sudo -u postgres psql << 'SQL'
CREATE DATABASE inventory_db;
CREATE USER inventory WITH PASSWORD '${POSTGRES_PASSWORD}';
GRANT ALL PRIVILEGES ON DATABASE inventory_db TO inventory;
\c inventory_db
GRANT ALL ON SCHEMA public TO inventory;
SQL
```

#### 4.1.2 Redis 部署

```bash
# 使用 Docker 部署
docker run -d \
  --name inventory-redis \
  --restart always \
  -p 6379:6379 \
  redis:7-alpine \
  redis-server --appendonly yes --requirepass ${REDIS_PASSWORD}

# 验证
docker exec inventory-redis redis-cli -a ${REDIS_PASSWORD} ping
```

#### 4.1.3 Nacos 部署

```bash
# 使用 Docker 部署（单机模式）
docker run -d \
  --name inventory-nacos \
  --restart always \
  -e MODE=standalone \
  -e PREFER_HOST_MODE=hostname \
  -e NACOS_AUTH_ENABLE=true \
  -e NACOS_AUTH_TOKEN=${NACOS_AUTH_TOKEN} \
  -e JVM_XMS=512m \
  -e JVM_XMX=1g \
  -p 8848:8848 \
  -p 9848:9848 \
  nacos/nacos-server:v2.4.3

# 验证
curl -s http://localhost:8848/nacos/v1/console/health/readiness
```

### 4.2 支撑服务部署

支撑服务启动顺序：registry-service → config-service → auth-service

#### 4.2.1 注册中心

```bash
cd microservices

# 构建
./gradlew :support-services:registry-service:bootJar

# 启动
java -jar \
  -Dserver.port=8761 \
  -Dspring.profiles.active=prod \
  -Dspring.cloud.nacos.discovery.server-addr=${NACOS_HOST}:8848 \
  support-services/registry-service/build/libs/registry-service-*.jar &

# 验证
curl http://localhost:8761/actuator/health
```

#### 4.2.2 配置服务

```bash
java -jar \
  -Dserver.port=8888 \
  -Dspring.profiles.active=prod \
  -Dspring.cloud.nacos.discovery.server-addr=${NACOS_HOST}:8848 \
  support-services/config-service/build/libs/config-service-*.jar &

# 验证
curl http://localhost:8888/actuator/health
```

#### 4.2.3 认证服务

```bash
java -jar \
  -Dserver.port=8093 \
  -Dspring.profiles.active=prod \
  -Dspring.cloud.nacos.discovery.server-addr=${NACOS_HOST}:8848 \
  -Dspring.datasource.url=jdbc:postgresql://${POSTGRES_HOST}:5432/inventory_db \
  -Dspring.datasource.username=${POSTGRES_USER} \
  -Dspring.datasource.password=${POSTGRES_PASSWORD} \
  -Dspring.data.redis.host=${REDIS_HOST} \
  -Dspring.data.redis.password=${REDIS_PASSWORD} \
  -Djwt.secret=${JWT_SECRET} \
  -Xms512m -Xmx1g \
  support-services/auth-service/build/libs/auth-service-*.jar &

# 验证
curl http://localhost:8093/actuator/health
```

### 4.3 核心服务部署

核心服务可以并行启动，每个服务使用不同的端口：

```bash
# 产品服务 (8081)
java -jar -Dserver.port=8081 \
  -Dspring.profiles.active=prod \
  -Dspring.cloud.nacos.discovery.server-addr=${NACOS_HOST}:8848 \
  core-services/product-service/build/libs/product-service-*.jar &

# 订单服务 (8082)
java -jar -Dserver.port=8082 \
  -Dspring.profiles.active=prod \
  -Dspring.cloud.nacos.discovery.server-addr=${NACOS_HOST}:8848 \
  core-services/order-service/build/libs/order-service-*.jar &

# 库存服务 (8083)
java -jar -Dserver.port=8083 \
  -Dspring.profiles.active=prod \
  -Dspring.cloud.nacos.discovery.server-addr=${NACOS_HOST}:8848 \
  core-services/inventory-service/build/libs/inventory-service-*.jar &

# 销售服务 (8084)
java -jar -Dserver.port=8084 \
  -Dspring.profiles.active=prod \
  -Dspring.cloud.nacos.discovery.server-addr=${NACOS_HOST}:8848 \
  core-services/sales-service/build/libs/sales-service-*.jar &

# 采购服务 (8085)
java -jar -Dserver.port=8085 \
  -Dspring.profiles.active=prod \
  -Dspring.cloud.nacos.discovery.server-addr=${NACOS_HOST}:8848 \
  core-services/procurement-service/build/libs/procurement-service-*.jar &

# 客户服务 (8086)
java -jar -Dserver.port=8086 \
  -Dspring.profiles.active=prod \
  -Dspring.cloud.nacos.discovery.server-addr=${NACOS_HOST}:8848 \
  core-services/customer-service/build/libs/customer-service-*.jar &

# 供应商服务 (8087)
java -jar -Dserver.port=8087 \
  -Dspring.profiles.active=prod \
  -Dspring.cloud.nacos.discovery.server-addr=${NACOS_HOST}:8848 \
  core-services/supplier-service/build/libs/supplier-service-*.jar &

# 业务伙伴服务 (8088)
java -jar -Dserver.port=8088 \
  -Dspring.profiles.active=prod \
  -Dspring.cloud.nacos.discovery.server-addr=${NACOS_HOST}:8848 \
  core-services/business-partner-service/build/libs/business-partner-service-*.jar &

# 等待所有服务启动
wait
```

#### 4.3.1 API 网关部署（最后启动）

```bash
java -jar \
  -Dserver.port=8080 \
  -Dspring.profiles.active=prod \
  -Dspring.cloud.nacos.discovery.server-addr=${NACOS_HOST}:8848 \
  -Dspring.data.redis.host=${REDIS_HOST} \
  -Dspring.data.redis.password=${REDIS_PASSWORD} \
  -Xms512m -Xmx1g \
  support-services/gateway-service/build/libs/gateway-service-*.jar &

# 验证
curl http://localhost:8080/actuator/health
```

### 4.4 前端部署

#### 4.4.1 构建前端

```bash
cd web-frontend

# 安装依赖
npm ci

# 构建生产版本
npm run build

# 构建产物在 dist/ 目录
```

#### 4.4.2 Nginx 部署

```nginx
# /etc/nginx/conf.d/inventory.conf
server {
    listen 80;
    server_name ${DOMAIN_NAME};

    # 前端静态文件
    root /usr/share/nginx/html;
    index index.html;

    # SPA 路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理
    location /api/ {
        proxy_pass http://gateway-service:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
}
```

```bash
# 使用 Docker 部署前端
docker build -t inventory-frontend:${VERSION} -f web-frontend/Dockerfile web-frontend/

docker run -d \
  --name inventory-frontend \
  --restart always \
  -p 80:80 \
  inventory-frontend:${VERSION}
```

---

## 5. Kubernetes 部署

### 5.1 前置条件

- Kubernetes 集群 v1.28+
- kubectl 已配置
- Helm v3.12+
- 已部署 Ingress Controller（如 NGINX Ingress）
- 已配置 StorageClass（用于持久化存储）
- 已配置容器镜像仓库访问权限

### 5.2 部署步骤

#### 5.2.1 创建命名空间

```bash
kubectl create namespace inventory-system
```

#### 5.2.2 创建 Secrets

```bash
kubectl create secret generic inventory-db-secret \
  --namespace inventory-system \
  --from-literal=username=${POSTGRES_USER} \
  --from-literal=password=${POSTGRES_PASSWORD}

kubectl create secret generic inventory-redis-secret \
  --namespace inventory-system \
  --from-literal=password=${REDIS_PASSWORD}

kubectl create secret generic inventory-jwt-secret \
  --namespace inventory-system \
  --from-literal=secret=${JWT_SECRET}
```

#### 5.2.3 部署基础设施

```yaml
# k8s/infrastructure/postgres-statefulset.yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: postgres
  namespace: inventory-system
spec:
  serviceName: postgres
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
        - name: postgres
          image: postgres:16-alpine
          ports:
            - containerPort: 5432
          env:
            - name: POSTGRES_DB
              value: inventory_db
            - name: POSTGRES_USER
              valueFrom:
                secretKeyRef:
                  name: inventory-db-secret
                  key: username
            - name: POSTGRES_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: inventory-db-secret
                  key: password
          volumeMounts:
            - name: postgres-data
              mountPath: /var/lib/postgresql/data
          resources:
            requests:
              cpu: "1"
              memory: 4Gi
            limits:
              cpu: "2"
              memory: 8Gi
          livenessProbe:
            exec:
              command: ["pg_isready", "-U", "$(POSTGRES_USER)", "-d", "inventory_db"]
            initialDelaySeconds: 30
            periodSeconds: 10
  volumeClaimTemplates:
    - metadata:
        name: postgres-data
      spec:
        accessModes: ["ReadWriteOnce"]
        resources:
          requests:
            storage: 100Gi
```

```bash
# 部署 PostgreSQL
kubectl apply -f k8s/infrastructure/postgres-statefulset.yaml
kubectl apply -f k8s/infrastructure/postgres-service.yaml

# 部署 Redis
kubectl apply -f k8s/infrastructure/redis-deployment.yaml
kubectl apply -f k8s/infrastructure/redis-service.yaml

# 部署 Nacos
kubectl apply -f k8s/infrastructure/nacos-deployment.yaml
kubectl apply -f k8s/infrastructure/nacos-service.yaml
```

#### 5.2.4 部署微服务

```yaml
# k8s/services/product-service-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: product-service
  namespace: inventory-system
spec:
  replicas: 2
  selector:
    matchLabels:
      app: product-service
  template:
    metadata:
      labels:
        app: product-service
    spec:
      containers:
        - name: product-service
          image: ${REGISTRY}/inventory-product-service:${VERSION}
          ports:
            - containerPort: 8081
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: prod
            - name: SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR
              value: nacos:8848
            - name: SPRING_DATASOURCE_URL
              value: jdbc:postgresql://postgres:5432/inventory_db
            - name: SPRING_DATASOURCE_USERNAME
              valueFrom:
                secretKeyRef:
                  name: inventory-db-secret
                  key: username
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: inventory-db-secret
                  key: password
            - name: SPRING_DATA_REDIS_HOST
              value: redis
            - name: SPRING_DATA_REDIS_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: inventory-redis-secret
                  key: password
            - name: JAVA_OPTS
              value: "-Xms512m -Xmx1g -XX:+UseG1GC"
          resources:
            requests:
              cpu: "0.5"
              memory: 512Mi
            limits:
              cpu: "1"
              memory: 1Gi
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8081
            initialDelaySeconds: 60
            periodSeconds: 15
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8081
            initialDelaySeconds: 30
            periodSeconds: 10
```

```bash
# 批量部署所有服务
for svc in product-service order-service inventory-service sales-service \
           procurement-service customer-service supplier-service business-partner-service \
           admin-service auth-service finance-service report-service \
           gateway-service config-service registry-service; do
  kubectl apply -f k8s/services/${svc}-deployment.yaml
  kubectl apply -f k8s/services/${svc}-service.yaml
done
```

### 5.3 服务暴露与 Ingress 配置

```yaml
# k8s/ingress/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: inventory-ingress
  namespace: inventory-system
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/proxy-body-size: "10m"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  ingressClassName: nginx
  tls:
    - hosts:
        - ${DOMAIN_NAME}
      secretName: inventory-tls
  rules:
    - host: ${DOMAIN_NAME}
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: gateway-service
                port:
                  number: 8080
```

```bash
kubectl apply -f k8s/ingress/ingress.yaml

# 验证 Ingress
kubectl get ingress -n inventory-system

# 预期输出:
# NAME                 CLASS   HOSTS              ADDRESS        PORTS     AGE
# inventory-ingress    nginx   ${DOMAIN_NAME}     ${INGRESS_IP}  80, 443   30s
```

---

## 6. 环境配置说明

### 6.1 环境变量清单

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `POSTGRES_HOST` | 是 | localhost | PostgreSQL 主机地址 |
| `POSTGRES_PORT` | 是 | 5432 | PostgreSQL 端口 |
| `POSTGRES_DB` | 是 | inventory_db | 数据库名称 |
| `POSTGRES_USER` | 是 | postgres | 数据库用户名 |
| `POSTGRES_PASSWORD` | 是 | - | 数据库密码（生产必须设置） |
| `REDIS_HOST` | 是 | localhost | Redis 主机地址 |
| `REDIS_PORT` | 是 | 6379 | Redis 端口 |
| `REDIS_PASSWORD` | 否 | - | Redis 密码（生产必须设置） |
| `REDIS_DATABASE` | 否 | 0 | Redis 数据库编号 |
| `NACOS_HOST` | 是 | localhost | Nacos 主机地址 |
| `NACOS_PORT` | 是 | 8848 | Nacos HTTP 端口 |
| `NACOS_NAMESPACE` | 否 | public | Nacos 命名空间 |
| `NACOS_AUTH_TOKEN` | 生产必填 | - | Nacos 认证 Token |
| `NACOS_IDENTITY_VALUE` | 生产必填 | - | Nacos 身份标识值 |
| `JWT_SECRET` | 是 | - | JWT 签名密钥（至少256位） |
| `JWT_EXPIRATION` | 否 | 86400000 | JWT 过期时间（毫秒） |
| `JWT_REFRESH_EXPIRATION` | 否 | 604800000 | 刷新令牌过期时间 |
| `OAUTH2_CLIENT_ID` | 否 | inventory-client | OAuth2 客户端ID |
| `OAUTH2_CLIENT_SECRET` | 否 | - | OAuth2 客户端密钥 |
| `KAFKA_BOOTSTRAP_SERVERS` | 否 | localhost:9092 | Kafka 地址 |
| `SPRING_PROFILES_ACTIVE` | 是 | dev | 激活的配置文件 |
| `REGISTRY` | 否 | - | Docker 镜像仓库前缀 |
| `VERSION` | 否 | latest | 镜像版本标签 |

### 6.2 配置文件说明

| 文件 | 用途 |
|------|------|
| `application.yml` | 默认配置，包含基础设置 |
| `application-dev.yml` | 开发环境配置，开启调试日志 |
| `application-prod.yml` | 生产环境配置，性能优化 |
| `application-docker.yml` | Docker 环境配置 |
| `application-postgres.yml` | PostgreSQL 特定配置 |
| `application-testing.yml` | 测试环境配置 |
| `.env.example` | 环境变量模板 |
| `.env.prod` | 生产环境变量（不纳入版本控制） |
| `docker-compose.yml` | 开发环境 Docker Compose |
| `docker-compose.prod.yml` | 生产环境 Docker Compose |

### 6.3 Redis 数据库分配

| Redis DB | 服务 | 用途 |
|----------|------|------|
| 0 | product-service | 产品缓存 |
| 1 | customer-service | 客户缓存 |
| 2 | inventory-service | 库存缓存 |
| 3 | auth-service | 会话/令牌 |

---

## 7. 部署验证

### 7.1 健康检查端点

| 服务 | 健康检查端点 | 就绪检查 | 存活检查 |
|------|-------------|----------|----------|
| gateway-service | `http://host:8080/actuator/health` | `/actuator/health/readiness` | `/actuator/health/liveness` |
| product-service | `http://host:8081/actuator/health` | 同上 | 同上 |
| order-service | `http://host:8082/actuator/health` | 同上 | 同上 |
| inventory-service | `http://host:8083/actuator/health` | 同上 | 同上 |
| sales-service | `http://host:8084/actuator/health` | 同上 | 同上 |
| procurement-service | `http://host:8085/actuator/health` | 同上 | 同上 |
| customer-service | `http://host:8086/actuator/health` | 同上 | 同上 |
| supplier-service | `http://host:8087/actuator/health` | 同上 | 同上 |
| business-partner-service | `http://host:8088/actuator/health` | 同上 | 同上 |
| admin-service | `http://host:8091/actuator/health` | 同上 | 同上 |
| finance-service | `http://host:8092/actuator/health` | 同上 | 同上 |
| auth-service | `http://host:8093/actuator/health` | 同上 | 同上 |
| report-service | `http://host:8094/actuator/health` | 同上 | 同上 |
| registry-service | `http://host:8761/actuator/health` | 同上 | 同上 |
| config-service | `http://host:8888/actuator/health` | 同上 | 同上 |
| PostgreSQL | `pg_isready` | - | - |
| Redis | `redis-cli ping` | - | - |
| Nacos | `http://host:8848/nacos/v1/console/health/readiness` | - | - |

### 7.2 冒烟测试步骤

```bash
# 1. 验证基础设施
echo "=== 基础设施检查 ==="
docker exec inventory-postgres pg_isready -U postgres -d inventory_db
docker exec inventory-redis redis-cli ping
curl -sf http://localhost:8848/nacos/v1/console/health/readiness && echo "Nacos OK"

# 2. 验证支撑服务
echo "=== 支撑服务检查 ==="
curl -sf http://localhost:8761/actuator/health | jq .status
curl -sf http://localhost:8888/actuator/health | jq .status
curl -sf http://localhost:8093/actuator/health | jq .status

# 3. 验证核心服务
echo "=== 核心服务检查 ==="
for port in 8081 8082 8083 8084 8085 8086 8087 8088; do
  status=$(curl -sf http://localhost:${port}/actuator/health | jq -r .status)
  echo "Port ${port}: ${status}"
done

# 4. 验证 API 网关
echo "=== 网关检查 ==="
curl -sf http://localhost:8080/actuator/health | jq .

# 5. 验证认证流程
echo "=== 认证测试 ==="
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"${ADMIN_PASSWORD}"}' \
  | jq -r .accessToken)

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
  echo "认证成功，Token: ${TOKEN:0:20}..."
  
  # 使用 Token 访问受保护资源
  curl -s http://localhost:8080/api/products \
    -H "Authorization: Bearer $TOKEN" | jq . | head -5
else
  echo "认证失败"
fi

# 6. 验证 Prometheus Metrics
echo "=== Prometheus Metrics 检查 ==="
for port in 8080 8081 8082 8083 8091 8093; do
  metrics_count=$(curl -s http://localhost:${port}/actuator/prometheus | wc -l)
  echo "Port ${port}: ${metrics_count} metrics exposed"
done
```

---

## 8. 回滚机制

### 8.1 版本标记策略

```bash
# 每次部署前创建 Git Tag
VERSION="v${BUILD_NUMBER}-$(date +%Y%m%d-%H%M%S)"
git tag -a ${VERSION} -m "Release ${VERSION}"
git push origin ${VERSION}

# Docker 镜像版本标记
docker tag ${REGISTRY}/inventory-product-service:latest ${REGISTRY}/inventory-product-service:${VERSION}
```

### 8.2 回滚步骤

#### Docker Compose 环境回滚

```bash
# 使用部署脚本回滚
bash scripts/deploy-prod.sh rollback

# 或者手动回滚到指定版本
VERSION=${ROLLBACK_VERSION}
docker compose -f docker-compose.prod.yml down
docker compose -f docker-compose.prod.yml up -d

# 验证回滚后服务状态
bash scripts/deploy-prod.sh status
```

#### Kubernetes 环境回滚

```bash
# 查看部署历史
kubectl rollout history deployment/product-service -n inventory-system

# 回滚到上一个版本
kubectl rollout undo deployment/product-service -n inventory-system

# 回滚到指定版本
kubectl rollout undo deployment/product-service -n inventory-system --to-revision=3

# 批量回滚所有服务
for svc in product-service order-service inventory-service; do
  kubectl rollout undo deployment/${svc} -n inventory-system
done

# 验证回滚状态
kubectl rollout status deployment/product-service -n inventory-system
```

### 8.3 数据回滚注意事项

1. **数据库回滚前必须备份**：`pg_dump -h ${POSTGRES_HOST} -U ${POSTGRES_USER} -d inventory_db > backup_$(date +%Y%m%d_%H%M%S).sql`
2. **应用回滚后检查数据一致性**：确保回滚版本与当前数据库结构兼容
3. **避免回滚覆盖新数据**：在回滚前评估新数据是否需要保留
4. **Redis 数据不需要回滚**：缓存数据会自动重建
5. **Nacos 配置**：回滚时需要同步回滚 Nacos 中的配置变更

---

## 9. 附录

### 9.1 常见部署问题 FAQ

**Q: 容器启动后立即退出怎么办？**
```bash
# 查看容器日志
docker logs inventory-product-service

# 常见原因：端口冲突、数据库连接失败、JVM 内存不足
docker logs inventory-product-service 2>&1 | grep -E "ERROR|FATAL|Exception"
```

**Q: PostgreSQL 连接被拒绝？**
```bash
# 检查 PostgreSQL 是否就绪
docker exec inventory-postgres pg_isready -U postgres -d inventory_db

# 检查连接配置
docker exec inventory-product-service env | grep POSTGRES

# 检查网络连通性
docker exec inventory-product-service ping postgres
```

**Q: 服务注册到 Nacos 失败？**
```bash
# 检查 Nacos 状态
curl http://localhost:8848/nacos/v1/console/health/readiness

# 查看已注册服务
curl http://localhost:8848/nacos/v1/ns/service/list?pageNo=1&pageSize=100

# 检查服务日志中的注册信息
docker logs inventory-product-service | grep -i "nacos.*register"
```

**Q: API 网关返回 503？**
```bash
# 检查目标服务是否在线
curl http://localhost:8080/actuator/gateway/routes

# 检查后端服务健康状态
curl http://localhost:8081/actuator/health
```

**Q: 内存不足导致 OOM？**
```bash
# 调整 JVM 堆大小
# 在 docker-compose.prod.yml 中修改 JAVA_OPTS
JAVA_OPTS: "-Xms256m -Xmx512m -XX:+UseG1GC"

# 或在 K8s 中调整资源限制
resources:
  limits:
    memory: 1Gi
```

### 9.2 性能调优建议

#### JVM 调优
```bash
# 推荐生产环境 JVM 参数
JAVA_OPTS="
  -Xms512m -Xmx2g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:ParallelGCThreads=4
  -XX:ConcGCThreads=1
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/tmp/heapdump.hprof
  -XX:+UseStringDeduplication
  -Djava.security.egd=file:/dev/./urandom
  -Dspring.jmx.enabled=false
  -Dserver.shutdown=graceful
"
```

#### 数据库连接池
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 100   # 生产环境
      minimum-idle: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

#### Tomcat 线程池
```yaml
server:
  tomcat:
    threads:
      max: 500
      min-spare: 50
    max-connections: 10000
    accept-count: 200
```

#### Redis 连接池
```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 50
          max-idle: 20
          min-idle: 10
```

#### Docker 资源限制
```yaml
deploy:
  resources:
    limits:
      cpus: '2.0'
      memory: 2G
    reservations:
      cpus: '0.5'
      memory: 512M
```
