# 服务扩缩容文档

## 版本
- 版本：1.0.0
- 创建日期：2025-01-19
- 最后更新：2025-01-19

## 1. 文档概述

### 1.1 目的
本文档提供了库存管理系统服务扩缩容的详细指南，包括水平扩容、垂直扩容、负载均衡配置和资源限制调整。

### 1.2 适用范围
- 水平扩容（增加服务实例）
- 垂直扩容（增加服务资源）
- 负载均衡配置
- 资源限制调整
- 扩缩容验证

### 1.3 目标读者
- 系统运维工程师
- DevOps工程师
- 系统架构师

## 2. 水平扩容

### 2.1 Docker Compose扩容

```bash
# 扩容product-service到3个实例
docker-compose -f docker-compose.prod.yml up -d --scale product-service=3

# 扩容order-service到2个实例
docker-compose -f docker-compose.prod.yml up -d --scale order-service=2

# 扩容inventory-service到2个实例
docker-compose -f docker-compose.prod.yml up -d --scale inventory-service=2
```

### 2.2 Kubernetes扩容

```yaml
# 扩容deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: product-service
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: product-service
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

### 2.3 滚动扩容脚本

```bash
#!/bin/bash
# rolling-scale.sh

SERVICE_NAME=${1:-product-service}
NEW_REPLICAS=${2:-3}
COMPOSE_FILE="docker-compose.prod.yml"

echo "开始滚动扩容 ${SERVICE_NAME} 到 ${NEW_REPLICAS} 个实例..."

# 获取当前副本数
CURRENT_REPLICAS=$(docker-compose -f ${COMPOSE_FILE} ps -q ${SERVICE_NAME} | wc -l)

echo "当前副本数: ${CURRENT_REPLICAS}"

# 逐个增加实例
for i in $(seq $((CURRENT_REPLICAS + 1)) ${NEW_REPLICAS}); do
    echo "启动实例 ${i}/${NEW_REPLICAS}..."
    
    docker-compose -f ${COMPOSE_FILE} up -d --no-deps --scale ${SERVICE_NAME}=${i} ${SERVICE_NAME}
    
    sleep 30
    
    if curl -f http://localhost:8082/actuator/health; then
        echo "实例 ${i} 启动成功"
    else
        echo "实例 ${i} 启动失败"
        exit 1
    fi
done

echo "滚动扩容完成！"
```

## 3. 垂直扩容

### 3.1 增加CPU资源

```yaml
# docker-compose.prod.yml
services:
  product-service:
    deploy:
      resources:
        limits:
          cpus: '2.0'
        reservations:
          cpus: '1.0'
```

### 3.2 增加内存资源

```yaml
services:
  product-service:
    deploy:
      resources:
        limits:
          memory: 2G
        reservations:
          memory: 1G
```

### 3.3 增加存储资源

```yaml
services:
  postgres:
    volumes:
      - postgres-data:/var/lib/postgresql/data
    deploy:
      resources:
        limits:
          storage: 100G
```

## 4. 负载均衡配置

### 4.1 Nginx负载均衡

```nginx
upstream product-service {
    least_conn;
    server product-service-1:8082 weight=1;
    server product-service-2:8082 weight=1;
    server product-service-3:8082 weight=1;
}

server {
    listen 80;
    server_name api.example.com;

    location /api/products {
        proxy_pass http://product-service;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 4.2 API网关负载均衡

```yaml
# gateway-service配置
spring:
  cloud:
    gateway:
      routes:
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
          filters:
            - name: Retry
              args:
                retries: 3
                statuses: 503,504
                methods: GET,POST
                backoff:
                  firstBackoff: 10ms
                  maxBackoff: 50ms
                  factor: 2
```

### 4.3 健康检查配置

```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
      routes:
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
```

## 5. 资源限制调整

### 5.1 CPU限制

```bash
# 查看当前CPU限制
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}"

# 调整CPU限制
docker update --cpus="2.0" product-service-1
```

### 5.2 内存限制

```bash
# 查看当前内存限制
docker stats --no-stream --format "table {{.Name}}\t{{.MemUsage}}"

# 调整内存限制
docker update --memory="2g" product-service-1
```

### 5.3 磁盘限制

```bash
# 查看当前磁盘使用
df -h

# 清理磁盘空间
docker system prune -a
docker volume prune -f
```

## 6. 扩缩容验证

### 6.1 服务健康检查

```bash
#!/bin/bash
# verify-scaling.sh

echo "验证扩缩容结果..."

# 检查所有实例
docker-compose -f docker-compose.prod.yml ps product-service

# 检查负载均衡
curl http://localhost:9090/api/products

# 检查服务注册
curl http://localhost:8761/eureka/apps/product-service

echo "扩缩容验证完成！"
```

### 6.2 性能验证

```bash
# 测试响应时间
ab -n 1000 -c 10 http://localhost:9090/api/products

# 测试吞吐量
ab -n 10000 -c 100 http://localhost:9090/api/products

# 测试并发能力
ab -n 5000 -c 50 http://localhost:9090/api/products
```

## 7. 最佳实践

### 7.1 扩容原则

1. **逐步扩容**
   - 使用滚动扩容
   - 避免服务中断
   - 每次扩容后验证

2. **监控资源**
   - 扩容前监控资源使用
   - 扩容后观察资源变化
   - 及时调整资源限制

3. **负载均衡**
   - 配置合理的负载均衡策略
   - 启用健康检查
   - 配置故障转移

4. **自动扩容**
   - 配置自动扩容策略
   - 设置扩容阈值
   - 配置自动缩容

### 7.2 缩容原则

1. **逐步缩容**
   - 使用滚动缩容
   - 避免服务中断
   - 每次缩容后验证

2. **资源释放**
   - 缩容后释放资源
   - 清理未使用的资源
   - 优化资源利用率

3. **监控影响**
   - 缩容后监控系统性能
   - 观察用户体验
   - 及时调整策略

## 8. 故障排查

### 8.1 扩容失败

```bash
# 检查容器日志
docker logs product-service-1
docker logs product-service-2
docker logs product-service-3

# 检查资源限制
docker stats product-service-1
docker stats product-service-2
docker stats product-service-3

# 检查网络连接
docker network inspect inventory-net-prod
```

### 8.2 负载均衡问题

```bash
# 检查Nginx配置
nginx -t

# 检查后端服务器状态
curl http://localhost:8082/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8082/actuator/health

# 检查API网关状态
curl http://localhost:9090/actuator/health
```

## 9. 附录

### 9.1 相关文档

- [DeploymentProcess.md](file:///e:/101/microservices/docs/DeploymentProcess.md) - 部署流程文档
- [ContainerOrchestration.md](file:///e:/101/microservices/docs/ContainerOrchestration.md) - 容器编排文档

### 9.2 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|---------|
| 1.0.0 | 2025-01-19 | 架构师团队 | 初始版本 |

---

**文档结束**
