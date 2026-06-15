# 数据源管理系统部署指南

## 1. 环境要求

### 1.1 硬件要求

| 环境 | CPU | 内存 | 磁盘 |
|------|-----|------|------|
| 开发环境 | 2核 | 4GB | 20GB |
| 测试环境 | 4核 | 8GB | 50GB |
| 生产环境 | 8核 | 16GB | 100GB |

### 1.2 软件要求

| 软件 | 版本要求 |
|------|----------|
| Java JDK | 21+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| Node.js | 18+ (前端构建) |
| Docker | 24.0+ |
| Docker Compose | 2.20+ |

---

## 2. 快速部署 (Docker Compose)

### 2.1 准备配置文件

创建 `.env` 文件：

```env
# 数据库配置
DB_HOST=mysql
DB_PORT=3306
DB_NAME=datasource_management
DB_USERNAME=root
DB_PASSWORD=your_secure_password

# Redis配置
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=

# 邮件配置
MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=your_email@example.com
MAIL_PASSWORD=your_email_password

# 加密密钥 (32字节)
ENCRYPTION_SECRET_KEY=your-32-byte-secret-key-here
```

### 2.2 启动服务

```bash
# 进入项目目录
cd project-root

# 构建并启动所有服务
docker-compose -f docker-compose.datasource.yml up -d

# 查看服务状态
docker-compose -f docker-compose.datasource.yml ps

# 查看日志
docker-compose -f docker-compose.datasource.yml logs -f datasource-service
```

### 2.3 访问服务

- **后端API**: http://localhost:8090/api/v1
- **Swagger文档**: http://localhost:8090/api/v1/swagger-ui.html
- **健康检查**: http://localhost:8090/api/v1/actuator/health

---

## 3. 手动部署

### 3.1 数据库初始化

```bash
# 连接MySQL
mysql -u root -p

# 执行初始化脚本
source init-scripts/datasource-init.sql
```

### 3.2 后端部署

```bash
# 构建项目
./gradlew :core-services:datasource-service:build

# 运行服务
java -jar core-services/datasource-service/build/libs/datasource-service-*.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:mysql://localhost:3306/datasource_management \
  --spring.datasource.username=root \
  --spring.datasource.password=your_password
```

### 3.3 前端部署

```bash
# 进入前端目录
cd datasource-frontend

# 安装依赖
npm install

# 构建生产版本
npm run build

# 部署到Nginx
cp -r dist/* /var/www/html/
```

### 3.4 Nginx配置

```nginx
server {
    listen 80;
    server_name datasource.example.com;

    location / {
        root /var/www/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8090;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## 4. Kubernetes部署

### 4.1 创建ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: datasource-service-config
data:
  application.yml: |
    spring:
      datasource:
        url: jdbc:mysql://mysql-service:3306/datasource_management
        username: root
      data:
        redis:
          host: redis-service
          port: 6379
```

### 4.2 创建Secret

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: datasource-service-secret
type: Opaque
stringData:
  DB_PASSWORD: your_password
  REDIS_PASSWORD: ""
  ENCRYPTION_SECRET_KEY: your-32-byte-secret-key
```

### 4.3 创建Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: datasource-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: datasource-service
  template:
    metadata:
      labels:
        app: datasource-service
    spec:
      containers:
      - name: datasource-service
        image: datasource-service:latest
        ports:
        - containerPort: 8090
        envFrom:
        - configMapRef:
            name: datasource-service-config
        - secretRef:
            name: datasource-service-secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /api/v1/actuator/health/liveness
            port: 8090
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/v1/actuator/health/readiness
            port: 8090
          initialDelaySeconds: 30
          periodSeconds: 5
```

### 4.4 创建Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: datasource-service
spec:
  selector:
    app: datasource-service
  ports:
  - port: 80
    targetPort: 8090
  type: ClusterIP
```

---

## 5. 配置说明

### 5.1 应用配置

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| server.port | 服务端口 | 8090 |
| spring.datasource.url | 数据库连接URL | - |
| spring.datasource.username | 数据库用户名 | - |
| spring.datasource.password | 数据库密码 | - |
| spring.data.redis.host | Redis主机 | localhost |
| spring.data.redis.port | Redis端口 | 6379 |
| app.encryption.secret-key | 加密密钥 | - |
| app.datasource.connection.timeout | 连接超时(ms) | 3000 |
| app.datasource.connection.max-retries | 最大重试次数 | 5 |

### 5.2 连接池配置

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000
      max-lifetime: 1800000
      connection-timeout: 30000
```

### 5.3 缓存配置

```yaml
spring:
  data:
    redis:
      timeout: 10000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

---

## 6. 监控配置

### 6.1 Prometheus配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### 6.2 健康检查

```bash
# 检查服务健康状态
curl http://localhost:8090/api/v1/actuator/health

# 检查数据库连接
curl http://localhost:8090/api/v1/actuator/health/db

# 检查Redis连接
curl http://localhost:8090/api/v1/actuator/health/redis
```

---

## 7. 故障排除

### 7.1 常见问题

#### 数据库连接失败

```bash
# 检查MySQL服务状态
docker-compose -f docker-compose.datasource.yml logs mysql

# 检查网络连接
docker exec -it datasource-service ping mysql
```

#### Redis连接失败

```bash
# 检查Redis服务状态
docker-compose -f docker-compose.datasource.yml logs redis

# 测试Redis连接
docker exec -it datasource-redis redis-cli ping
```

#### 服务启动失败

```bash
# 查看详细日志
docker-compose -f docker-compose.datasource.yml logs -f datasource-service

# 检查端口占用
netstat -tlnp | grep 8090
```

### 7.2 日志查看

```bash
# 查看实时日志
docker-compose -f docker-compose.datasource.yml logs -f datasource-service

# 查看最近100行日志
docker-compose -f docker-compose.datasource.yml logs --tail=100 datasource-service
```

---

## 8. 性能优化

### 8.1 JVM参数

```bash
java -jar datasource-service.jar \
  -Xms512m \
  -Xmx1g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/logs/heapdump.hprof
```

### 8.2 数据库优化

```sql
-- 添加索引
CREATE INDEX idx_datasource_config_tenant_status ON datasource_config(tenant_id, status);

-- 定期清理历史数据
DELETE FROM connection_test_log WHERE tested_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
DELETE FROM audit_log WHERE created_at < DATE_SUB(NOW(), INTERVAL 180 DAY);
```

---

## 9. 安全配置

### 9.1 HTTPS配置

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

### 9.2 密钥管理

- 加密密钥应存储在安全的密钥管理系统中
- 定期轮换加密密钥
- 不要在代码或配置文件中硬编码密钥

---

## 10. 备份与恢复

### 10.1 数据库备份

```bash
# 备份数据库
docker exec datasource-mysql mysqldump -u root -p datasource_management > backup.sql

# 恢复数据库
docker exec -i datasource-mysql mysql -u root -p datasource_management < backup.sql
```

### 10.2 配置备份

```bash
# 备份配置
tar -czvf config-backup.tar.gz .env docker-compose.datasource.yml
```

---

## 11. 升级指南

### 11.1 版本升级

```bash
# 拉取最新代码
git pull

# 重新构建
./gradlew clean build

# 停止旧服务
docker-compose -f docker-compose.datasource.yml down

# 启动新服务
docker-compose -f docker-compose.datasource.yml up -d
```

### 11.2 数据库迁移

```bash
# 执行迁移脚本
mysql -u root -p datasource_management < migration/v2.0.0.sql
```
