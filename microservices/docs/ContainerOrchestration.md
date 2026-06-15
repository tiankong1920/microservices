# 容器编排文档

## 版本
- 版本：1.0.0
- 创建日期：2025-01-19
- 最后更新：2025-01-19

## 1. 文档概述

### 1.1 目的
本文档提供了库存管理系统的容器编排详细指南，包括Docker和Docker Compose配置、服务依赖关系、网络配置、卷挂载和健康检查配置。

### 1.2 适用范围
- Docker容器化部署
- Docker Compose多服务编排
- 服务依赖管理
- 网络隔离和通信
- 数据持久化和卷管理
- 健康检查和自动重启

### 1.3 目标读者
- 系统运维工程师
- DevOps工程师
- 部署工程师
- 容器编排工程师

## 2. Docker Compose配置

### 2.1 主配置文件

#### 2.1.1 开发环境配置

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16.3
    container_name: postgres-dev
    environment:
      POSTGRES_DB: inventory
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - inventory-net-dev
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7.0
    container_name: redis-dev
    command: redis-server --appendonly yes
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - inventory-net-dev
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  registry-service:
    build: ./support-services/registry-service
    container_name: registry-service-dev
    ports:
      - "8761:8761"
    environment:
      SPRING_PROFILES_ACTIVE: dev
    networks:
      - inventory-net-dev
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "wget", "-qO-", "http://localhost:8761/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  config-service:
    build: ./support-services/config-service
    container_name: config-service-dev
    ports:
      - "8888:8888"
    environment:
      SPRING_PROFILES_ACTIVE: dev
    networks:
      - inventory-net-dev
    depends_on:
      registry-service:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "wget", "-qO-", "http://localhost:8888/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  product-service:
    build: ./core-services/product-service
    container_name: product-service-dev
    ports:
      - "8082:8082"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://registry-service:8761/eureka/
      SPRING_CONFIG_IMPORT: configserver:http://config-service:8888/
    networks:
      - inventory-net-dev
    depends_on:
      config-service:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "wget", "-qO-", "http://localhost:8082/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  postgres-data:
    driver: local
  redis-data:
    driver: local

networks:
  inventory-net-dev:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16
```

#### 2.1.2 生产环境配置

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16.3
    container_name: postgres-prod
    secrets:
      - postgres_password
    environment:
      POSTGRES_DB: inventory
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD_FILE: /run/secrets/postgres_password
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - inventory-net-prod
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 8G
        reservations:
          cpus: '1.0'
          memory: 4G
      restart_policy:
        condition: on-failure
        max_attempts: 5
        window: 120s

  redis:
    image: redis:7.0
    container_name: redis-prod
    command: redis-server --appendonly yes --requirepass ${REDIS_PASSWORD}
    secrets:
      - redis_password
    environment:
      REDIS_PASSWORD_FILE: /run/secrets/redis_password
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - inventory-net-prod
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 6G
        reservations:
          cpus: '0.5'
          memory: 4G

secrets:
  postgres_password:
    external: true
  redis_password:
    external: true

volumes:
  postgres-data:
    driver: local
  redis-data:
    driver: local

networks:
  inventory-net-prod:
    driver: bridge
    ipam:
      config:
        - subnet: 172.22.0.0/16
```

### 2.2 服务依赖关系

```
┌─────────────────────────────────────────────────────────┐
│                        客户端层                                │
└────────────────────────┬────────────────────────────────────────┘
                 │
        ┌────────▼────────────────────────────────────────┐
        │           API网关层                       │
        └────────────────┬──────────────────────────────────┘
                     │
        ┌─────────────┼──────────────────────────────────┐
        │             │                              │
┌───────┼──────┐  │  ┌────────────────────────────────┐ │
│支持服务│      │  │  │       核心服务层        │ │
└───────┼──────┘  │  └────────────────────────────────┘ │
        │             │                              │
┌───────┼──────┐  │  ┌────────────────────────────────┐ │
│数据库/缓存│      │  │  │      核心服务层        │ │
└───────┼──────┘  │  └────────────────────────────────┘ │
        │             │                              │
└─────────────┴──────────────────────────────────┘
```

## 3. 网络配置

### 3.1 网络隔离

#### 3.1.1 开发环境网络

- **inventory-net-dev**: 开发环境网络
  - 子网：172.20.0.0/16
  - 驱动：bridge
  - 包含服务：所有开发服务

#### 3.1.2 测试环境网络

- **inventory-net-test**: 测试环境网络
  - 子网：172.21.0.0/16
  - 驱动：bridge
  - 包含服务：所有测试服务

#### 3.1.3 生产环境网络

- **inventory-net-prod**: 生产环境网络
  - 子网：172.22.0.0/16
  - 驱动：bridge
  - 包含服务：所有生产服务

### 3.2 网络安全

#### 3.2.1 端口映射

| 服务 | 内部端口 | 外部端口 | 协议 | 说明 |
|------|---------|---------|------|------|
| postgres | 5432 | 5432 | TCP | 数据库访问 |
| redis | 6379 | 6379 | TCP | 缓存访问 |
| registry-service | 8761 | 8761 | HTTP | 服务注册 |
| config-service | 8888 | 8888 | HTTP | 配置中心 |
| gateway-service | 9090 | 9090 | HTTP | API网关 |
| product-service | 8082 | 8082 | HTTP | 产品服务 |

#### 3.2.2 防火墙规则

```bash
# 允许数据库访问
sudo ufw allow from 172.20.0.0/16 to any port 5432 proto tcp
sudo ufw allow from 172.21.0.0/16 to any port 5432 proto tcp
sudo ufw allow from 172.22.0.0/16 to any port 5432 proto tcp

# 允许Redis访问
sudo ufw allow from 172.20.0.0/16 to any port 6379 proto tcp
sudo ufw allow from 172.21.0.0/16 to any port 6379 proto tcp
sudo ufw allow from 172.22.0.0/16 to any port 6379 proto tcp

# 允许服务端口访问
sudo ufw allow from 172.20.0.0/16 to any port 8082 proto tcp
sudo ufw allow from 172.21.0.0/16 to any port 8082 proto tcp
sudo ufw allow from 172.22.0.0/16 to any port 8082 proto tcp
```

## 4. 卷管理

### 4.1 数据卷

#### 4.1.1 数据库卷

```yaml
volumes:
  postgres-data:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /data/postgres
```

#### 4.1.2 缓存卷

```yaml
volumes:
  redis-data:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /data/redis
```

#### 4.1.3 配置卷

```yaml
volumes:
  config-data:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /data/config
```

### 4.2 卷备份策略

```bash
# 备份数据卷
docker run --rm -v postgres-data:/data -v $(pwd)/backup:/backup postgres:16 tar czf /backup/postgres-data-$(date +%Y%m%d).tar.gz /data

# 备份缓存卷
docker run --rm -v redis-data:/data -v $(pwd)/backup:/backup redis:7.0 tar czf /backup/redis-data-$(date +%Y%m%d).tar.gz /data

# 备份配置卷
docker run --rm -v config-data:/data -v $(pwd)/backup:/backup busybox tar czf /backup/config-data-$(date +%Y%m%d).tar.gz /data
```

## 5. 健康检查

### 5.1 健康检查配置

#### 5.1.1 数据库健康检查

```yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U postgres"]
  interval: 10s
  timeout: 5s
  retries: 5
  start_period: 10s
```

#### 5.1.2 Redis健康检查

```yaml
healthcheck:
  test: ["CMD", "redis-cli", "ping"]
  interval: 10s
  timeout: 5s
  retries: 5
  start_period: 10s
```

#### 5.1.3 应用服务健康检查

```yaml
healthcheck:
  test: ["CMD-SHELL", "wget", "-qO-", "http://localhost:8082/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 30s
```

### 5.2 健康检查脚本

```bash
#!/bin/bash
# health-check-all.sh

echo "开始全面健康检查..."

# 检查所有服务
SERVICES=(
    "postgres:5432"
    "redis:6379"
    "registry-service:8761"
    "config-service:8888"
    "product-service:8082"
    "order-service:8081"
    "inventory-service:8080"
)

for service in "${SERVICES[@]}"; do
    NAME=$(echo $service | cut -d':' -f1)
    PORT=$(echo $service | cut -d':' -f2)
    
    echo "检查 $NAME (端口 $PORT)..."
    if curl -f http://localhost:${PORT}/actuator/health > /dev/null 2>&1; then
        echo "✓ $NAME: 健康"
    else
        echo "✗ $NAME: 不健康"
    fi
done

echo "健康检查完成！"
```

## 6. 资源限制

### 6.1 CPU限制

```yaml
deploy:
  resources:
    limits:
      cpus: '1.0'
    reservations:
      cpus: '0.5'
```

### 6.2 内存限制

```yaml
deploy:
  resources:
    limits:
      memory: 1G
    reservations:
      memory: 512M
```

### 6.3 磁盘限制

```yaml
deploy:
  resources:
    limits:
      storage: 10G
```

## 7. 重启策略

### 7.1 重启条件

```yaml
restart: unless-stopped
```

### 7.2 重启延迟

```yaml
restart: always
deploy:
  restart_policy:
    delay: 5s
    max_attempts: 3
    window: 60s
```

### 7.3 失败重启

```yaml
restart: on-failure
deploy:
  restart_policy:
    condition: on-failure
    max_attempts: 5
    window: 120s
```

## 8. 日志配置

### 8.1 日志驱动

```yaml
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
```

### 8.2 日志轮转

```yaml
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
    compress: "true"
```

### 8.3 日志收集

```yaml
logging:
  driver: "syslog"
  options:
    syslog-address: "tcp://logserver:514"
    tag: "inventory-system"
```

## 9. 最佳实践

### 9.1 部署最佳实践

1. **使用多阶段部署**
   - 先部署基础设施服务
   - 再部署支持服务
   - 最后部署核心服务

2. **配置健康检查**
   - 所有服务都应配置健康检查
   - 设置合理的检查间隔
   - 配置重试次数

3. **使用资源限制**
   - 为每个服务设置CPU和内存限制
   - 避免资源争用
   - 优化资源利用率

4. **使用卷持久化**
   - 数据库数据必须持久化
   - 缓存数据可以持久化
   - 配置文件应持久化

5. **使用网络隔离**
   - 不同环境使用不同网络
   - 避免网络冲突
   - 配置防火墙规则

### 9.2 运维最佳实践

1. **监控容器状态**
   - 定期检查容器运行状态
   - 监控资源使用情况
   - 查看容器日志

2. **定期清理资源**
   - 清理未使用的镜像
   - 清理未使用的卷
   - 清理未使用的网络

3. **备份重要数据**
   - 定期备份数据卷
   - 备份配置文件
   - 备份环境变量

4. **更新容器镜像**
   - 定期更新基础镜像
   - 修复安全漏洞
   - 测试更新后再部署

## 10. 故障排查

### 10.1 容器启动失败

```bash
# 检查容器日志
docker logs <container-name>

# 检查容器状态
docker ps -a | grep <container-name>

# 检查镜像是否存在
docker images | grep <image-name>

# 重新拉取镜像
docker pull <image-name>
```

### 10.2 网络连接问题

```bash
# 检查网络配置
docker network ls
docker network inspect <network-name>

# 测试网络连通性
docker run --rm --network <network-name> alpine ping <service-name>
```

### 10.3 卷挂载问题

```bash
# 检查卷状态
docker volume ls
docker volume inspect <volume-name>

# 检查卷权限
ls -la /path/to/volume

# 修复卷权限
chmod 755 /path/to/volume
```

## 11. 附录

### 11.1 相关文档

- [DeploymentProcess.md](file:///e:/101/microservices/docs/DeploymentProcess.md) - 部署流程文档
- [docker-compose.yml](file:///e:/101/microservices/docker-compose.yml) - 主配置文件
- [docker-compose.prod.yml](file:///e:/101/microservices/docker-compose.prod.yml) - 生产环境配置

### 11.2 相关脚本

- [health-check-all.sh](file:///e:/101/microservices/scripts/health-check-all.sh) - 全面健康检查脚本

---

**文档结束**
