# 故障恢复文档

## 版本
- 版本：1.0.0
- 创建日期：2025-01-19
- 最后更新：2025-01-19

## 1. 文档概述

### 1.1 目的
本文档提供了库存管理系统故障恢复的详细指南，包括故障诊断、服务恢复、数据恢复、灾难恢复演练和恢复验证方法。

### 1.2 适用范围
- 服务故障恢复
- 数据库故障恢复
- 缓存故障恢复
- 网络故障恢复
- 灾难恢复演练

### 1.3 目标读者
- 系统运维工程师
- DevOps工程师
- 系统管理员
- 数据库管理员

### 1.4 前置条件
- 已完成备份（参考[BackupRecoveryGuide.md](file:///e:/101/microservices/docs/BackupRecoveryGuide.md)）
- 已配置监控告警
- 已准备恢复脚本
- 已测试恢复流程

## 2. 故障诊断

### 2.1 服务故障诊断

#### 2.1.1 检查容器状态

```bash
# 检查所有容器状态
docker ps -a

# 检查特定服务状态
docker ps | grep product-service
docker ps | grep order-service
docker ps | grep inventory-service

# 查看容器详情
docker inspect product-service-1
```

#### 2.1.2 检查服务日志

```bash
# 查看容器日志
docker logs product-service-1 --tail 100

# 查看最近错误
docker logs product-service-1 --since 1h | grep -i error

# 查看特定时间范围的日志
docker logs product-service-1 --since 2025-01-19T00:00:00 --until 2025-01-19T01:00:00
```

#### 2.1.3 检查资源使用

```bash
# 检查容器资源使用
docker stats product-service-1

# 检查系统资源
top -bn1 | head -20
free -h
df -h
```

### 2.2 数据库故障诊断

#### 2.2.1 检查数据库连接

```bash
# 检查PostgreSQL连接
docker exec postgres-1 psql -U postgres -d inventory -c "SELECT version();"

# 检查连接数
docker exec postgres-1 psql -U postgres -d inventory -c "SELECT count(*) FROM pg_stat_activity;"

# 检查慢查询
docker exec postgres-1 psql -U postgres -d inventory -c "SELECT * FROM pg_stat_statements ORDER BY mean_exec_time DESC LIMIT 10;"
```

#### 2.2.2 检查数据库锁

```bash
# 检查数据库锁
docker exec postgres-1 psql -U postgres -d inventory -c "SELECT * FROM pg_locks;"

# 检查长时间运行的查询
docker exec postgres-1 psql -U postgres -d inventory -c "SELECT pid, now() - query_start as duration, query FROM pg_stat_activity WHERE state = 'active' ORDER BY duration DESC LIMIT 10;"
```

### 2.3 缓存故障诊断

#### 2.3.1 检查Redis连接

```bash
# 检查Redis连接
docker exec redis-1 redis-cli ping

# 检查Redis信息
docker exec redis-1 redis-cli INFO

# 检查Redis键数量
docker exec redis-1 redis-cli DBSIZE
```

#### 2.3.2 检查Redis内存

```bash
# 检查Redis内存使用
docker exec redis-1 redis-cli INFO memory

# 检查Redis键空间使用
docker exec redis-1 redis-cli INFO keyspace
```

## 3. 服务恢复

### 3.1 服务重启

#### 3.1.1 单服务重启

```bash
# 重启单个服务
docker-compose -f docker-compose.prod.yml restart product-service

# 等待服务启动
sleep 30

# 验证服务状态
docker ps | grep product-service
curl http://localhost:8082/actuator/health
```

#### 3.1.2 多服务重启

```bash
# 重启多个服务
docker-compose -f docker-compose.prod.yml restart \
  product-service \
  order-service \
  inventory-service

# 等待服务启动
sleep 30

# 验证服务状态
docker-compose -f docker-compose.prod.yml ps
```

#### 3.1.3 滚动重启

```bash
#!/bin/bash
# rolling-restart.sh

SERVICE_NAME=${1:-product-service}
COMPOSE_FILE="docker-compose.prod.yml"

echo "开始滚动重启 ${SERVICE_NAME}..."

# 获取当前副本数
CURRENT_REPLICAS=$(docker-compose -f ${COMPOSE_FILE} ps -q ${SERVICE_NAME} | wc -l)

# 逐个重启实例
for i in $(seq 1 ${CURRENT_REPLICAS}); do
    echo "重启实例 ${i}/${CURRENT_REPLICAS}..."
    
    docker-compose -f ${COMPOSE_FILE} restart ${SERVICE_NAME}_${i}
    
    sleep 30
    
    if curl -f http://localhost:8082/actuator/health; then
        echo "实例 ${i} 重启成功"
    else
        echo "实例 ${i} 重启失败"
        exit 1
    fi
done

echo "滚动重启完成！"
```

### 3.2 服务重建

#### 3.2.1 重建容器

```bash
# 停止并删除容器
docker-compose -f docker-compose.prod.yml rm -f product-service

# 重新创建容器
docker-compose -f docker-compose.prod.yml up -d product-service

# 验证服务状态
docker ps | grep product-service
curl http://localhost:8082/actuator/health
```

#### 3.2.2 重建镜像

```bash
# 重新构建镜像
docker-compose -f docker-compose.prod.yml build product-service

# 启动新镜像
docker-compose -f docker-compose.prod.yml up -d product-service

# 验证服务状态
docker ps | grep product-service
curl http://localhost:8082/actuator/health
```

## 4. 数据恢复

### 4.1 数据库恢复

#### 4.1.1 PostgreSQL恢复

```bash
#!/bin/bash
# restore-database.sh

BACKUP_FILE=${1:-/opt/inventory-system/backups/database/inventory_full_20250119_020000.sql}

echo "开始数据库恢复..."

# 停止应用服务
echo "停止应用服务..."
docker-compose -f docker-compose.prod.yml stop product-service order-service inventory-service

# 恢复数据库
echo "恢复数据库..."
docker exec -i postgres-1 psql -U postgres -d inventory < ${BACKUP_FILE}

# 验证数据库
echo "验证数据库..."
docker exec postgres-1 psql -U postgres -d inventory -c "SELECT COUNT(*) FROM products;"
docker exec postgres-1 psql -U postgres -d inventory -c "SELECT COUNT(*) FROM orders;"

# 重启应用服务
echo "重启应用服务..."
docker-compose -f docker-compose.prod.yml start product-service order-service inventory-service

# 等待服务启动
echo "等待服务启动..."
sleep 30

# 验证服务状态
echo "验证服务状态..."
docker-compose -f docker-compose.prod.yml ps
curl http://localhost:8082/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8080/actuator/health

echo "数据库恢复完成！"
```

#### 4.1.2 时间点恢复（PITR）

```bash
# PostgreSQL时间点恢复
docker exec postgres-1 psql -U postgres -d inventory -c "SELECT pg_create_restore_point('before_restore');"

# 执行可能导致数据丢失的操作

# 恢复到时间点
docker exec postgres-1 psql -U postgres -d inventory -c "SELECT pg_restore_point('before_restore');"
```

### 4.2 缓存恢复

#### 4.2.1 Redis数据恢复

```bash
# 从AOF文件恢复
docker exec redis-1 redis-cli SHUTDOWN
docker cp redis-data/appendonly.aof redis-data/appendonly.aof.backup
docker-compose -f docker-compose.prod.yml restart redis

# 从RDB文件恢复
docker exec redis-1 redis-cli SHUTDOWN
docker cp redis-data/dump.rdb redis-data/dump.rdb.backup
docker-compose -f docker-compose.prod.yml restart redis

# 验证Redis
docker exec redis-1 redis-cli ping
docker exec redis-1 redis-cli DBSIZE
```

#### 4.2.2 缓存预热

```bash
# 缓存预热脚本
curl -X POST http://localhost:8082/actuator/cache/warmup

# 验证缓存
curl http://localhost:8082/actuator/cache/stats
```

## 5. 灾难恢复演练

### 5.1 演练计划

#### 5.1.1 演练类型

1. **服务故障演练**
   - 模拟服务宕机
   - 测试自动恢复
   - 验证故障转移

2. **数据库故障演练**
   - 模拟数据库故障
   - 测试数据库恢复
   - 验证数据完整性

3. **网络故障演练**
   - 模拟网络中断
   - 测试服务降级
   - 验证缓存可用性

4. **灾难恢复演练**
   - 模拟完整系统故障
   - 测试灾难恢复流程
   - 验证RTO/RPO

### 5.2 演练执行

#### 5.2.1 服务故障演练

```bash
#!/bin/bash
# service-failure-drill.sh

SERVICE_NAME=${1:-product-service}

echo "开始服务故障演练..."

# 停止服务
echo "停止服务 ${SERVICE_NAME}..."
docker-compose -f docker-compose.prod.yml stop ${SERVICE_NAME}

# 等待告警触发
echo "等待告警触发..."
sleep 60

# 重启服务
echo "重启服务 ${SERVICE_NAME}..."
docker-compose -f docker-compose.prod.yml start ${SERVICE_NAME}

# 验证服务恢复
echo "验证服务恢复..."
sleep 30
curl http://localhost:8082/actuator/health

echo "服务故障演练完成！"
```

#### 5.2.2 数据库故障演练

```bash
#!/bin/bash
# database-failure-drill.sh

echo "开始数据库故障演练..."

# 停止数据库
echo "停止数据库..."
docker-compose -f docker-compose.prod.yml stop postgres

# 等待告警触发
echo "等待告警触发..."
sleep 60

# 重启数据库
echo "重启数据库..."
docker-compose -f docker-compose.prod.yml start postgres

# 验证数据库恢复
echo "验证数据库恢复..."
sleep 30
docker exec postgres-1 psql -U postgres -d inventory -c "SELECT version();"

echo "数据库故障演练完成！"
```

#### 5.2.3 灾难恢复演练

```bash
#!/bin/bash
# disaster-recovery-drill.sh

echo "开始灾难恢复演练..."

# 停止所有服务
echo "停止所有服务..."
docker-compose -f docker-compose.prod.yml down
docker-compose -f monitoring/docker-compose.yml down

# 恢复数据库
echo "恢复数据库..."
BACKUP_FILE=/opt/inventory-system/backups/database/inventory_full_20250119_020000.sql
docker-compose -f docker-compose.prod.yml up -d postgres
docker exec -i postgres-1 psql -U postgres -d inventory < ${BACKUP_FILE}

# 启动所有服务
echo "启动所有服务..."
docker-compose -f docker-compose.prod.yml up -d
docker-compose -f monitoring/docker-compose.yml up -d

# 验证所有服务
echo "验证所有服务..."
sleep 60
docker-compose -f docker-compose.prod.yml ps
docker-compose -f monitoring/docker-compose.yml ps

# 验证功能
echo "验证功能..."
curl http://localhost:9090/api/products
curl http://localhost:9090/api/orders

echo "灾难恢复演练完成！"
```

## 6. 恢复验证

### 6.1 服务验证

#### 6.1.1 健康检查

```bash
#!/bin/bash
# verify-service-health.sh

echo "验证服务健康..."

# 检查所有服务
SERVICES=(
    "product-service:8082"
    "order-service:8081"
    "inventory-service:8080"
    "gateway-service:9090"
)

for service in "${SERVICES[@]}"; do
    NAME=$(echo $service | cut -d':' -f1)
    PORT=$(echo $service | cut -d':' -f2)
    
    echo "检查 ${NAME}..."
    if curl -f http://localhost:${PORT}/actuator/health > /dev/null 2>&1; then
        echo "✓ ${NAME}: 健康"
    else
        echo "✗ ${NAME}: 不健康"
    fi
done

echo "服务健康检查完成！"
```

#### 6.1.2 功能验证

```bash
#!/bin/bash
# verify-functionality.sh

echo "验证功能..."

# 测试产品服务
echo "测试产品服务..."
curl -f http://localhost:9090/api/products
curl -f http://localhost:9090/api/products/1

# 测试订单服务
echo "测试订单服务..."
curl -f http://localhost:9090/api/orders
curl -f http://localhost:9090/api/orders/1

# 测试库存服务
echo "测试库存服务..."
curl -f http://localhost:9090/api/inventory
curl -f http://localhost:9090/api/inventory/1

echo "功能验证完成！"
```

### 6.2 数据验证

#### 6.2.1 数据完整性验证

```bash
#!/bin/bash
# verify-data-integrity.sh

echo "验证数据完整性..."

# 验证产品数据
PRODUCT_COUNT=$(docker exec postgres-1 psql -U postgres -d inventory -t -c "SELECT COUNT(*) FROM products;")
echo "产品数量: ${PRODUCT_COUNT}"

# 验证订单数据
ORDER_COUNT=$(docker exec postgres-1 psql -U postgres -d inventory -t -c "SELECT COUNT(*) FROM orders;")
echo "订单数量: ${ORDER_COUNT}"

# 验证库存数据
INVENTORY_COUNT=$(docker exec postgres-1 psql -U postgres -d inventory -t -c "SELECT COUNT(*) FROM inventory;")
echo "库存数量: ${INVENTORY_COUNT}"

# 验证外键约束
docker exec postgres-1 psql -U postgres -d inventory -c "SELECT COUNT(*) FROM orders WHERE customer_id IN (SELECT id FROM customers);"

echo "数据完整性验证完成！"
```

#### 6.2.2 数据一致性验证

```bash
#!/bin/bash
# verify-data-consistency.sh

echo "验证数据一致性..."

# 验证订单和库存一致性
docker exec postgres-1 psql -U postgres -d inventory -c "
SELECT 
    o.id as order_id,
    o.status as order_status,
    i.quantity as inventory_quantity,
    CASE 
        WHEN i.quantity IS NULL THEN 'missing'
        WHEN i.quantity < o.quantity THEN 'insufficient'
        ELSE 'consistent'
    END as consistency_status
FROM orders o
LEFT JOIN inventory i ON o.product_id = i.product_id
WHERE o.created_at >= NOW() - INTERVAL '1 day'
LIMIT 10;
"

echo "数据一致性验证完成！"
```

## 7. 最佳实践

### 7.1 故障预防

1. **定期备份**
   - 每天自动备份
   - 保留多个备份版本
   - 验证备份完整性

2. **监控告警**
   - 配置全面的监控
   - 设置合理的告警阈值
   - 及时发现和处理问题

3. **高可用架构**
   - 使用多副本部署
   - 配置负载均衡
   - 实现故障转移

4. **文档化**
   - 记录所有故障
   - 更新恢复流程
   - 维护故障知识库

### 7.2 故障响应

1. **快速响应**
   - 设置响应时间目标
   - 建立应急响应流程
   - 配置24/7值班

2. **分级处理**
   - 根据故障级别分配资源
   - 优先处理关键故障
   - 协调跨团队响应

3. **透明沟通**
   - 及时通知相关人员
   - 定期更新故障状态
   - 提供预计恢复时间

### 7.3 恢复原则

1. **数据优先**
   - 确保数据完整性
   - 验证数据一致性
   - 优先恢复关键数据

2. **服务优先**
   - 快速恢复核心服务
   - 逐步恢复辅助服务
   - 验证服务功能

3. **验证优先**
   - 全面验证恢复结果
   - 执行功能测试
   - 观察系统稳定性

## 8. 紧急联系方式

> ⚠️ **隐私保护提醒**: 以下联系方式中的个人信息（姓名、电话）均为模板占位符，实际部署前必须替换为真实联系人信息，并确保符合公司隐私保护政策。

### 8.1 联系方式模板（部署前必须填写）

> 📝 **填写说明**: 请联系项目经理获取真实联系人信息后，按以下格式填写

| 角色 | 姓名 | 电话 | 邮箱 | 微信/钉钉 |
|------|------|------|------|-----------|
| 运维负责人 | [姓名] | [电话] | [邮箱] | [账号] |
| DevOps负责人 | [姓名] | [电话] | [邮箱] | [账号] |
| 数据库管理员 | [姓名] | [电话] | [邮箱] | [账号] |
| 安全负责人 | [姓名] | [电话] | [邮箱] | [账号] |
| 应急联系人 | [姓名] | [电话] | [邮箱] | [账号] |
| 备用联系人 | [姓名] | [电话] | [邮箱] | [账号] |

### 8.2 电话号码脱敏规则 (DOC-001)

> 🔒 **合规要求**: 根据数据安全法规，文档中的电话号码必须脱敏处理

| 原始格式 | 脱敏后格式 | 示例 |
|----------|------------|------|
| 手机号 (11位) | 前3后4，中间用*替代 | 138****5678 |
| 固话 (带区号) | 区号-***-后4位 | 010-****-1234 |
| 400/800电话 | 保持完整但加密存储 | 400-***-xxxx |

> ⚠️ **注意**: 实际紧急联系名单请勿在文档中明文存储真实电话号码，建议使用加密存储或通过内部通讯工具查询

### 8.3 24小时值班安排表

> ⚠️ **注意**: 以下值班安排为模板占位，实际值班安排请根据团队情况填写

| 班次 | 日期 | 值班人 | 联系电话 | 邮箱 | 备注 |
|------|------|--------|----------|------|------|
| 白班 | 周一 | [姓名] | [电话] | [邮箱] | 09:00-18:00 |
| 白班 | 周二 | [姓名] | [电话] | [邮箱] | 09:00-18:00 |
| 白班 | 周三 | [姓名] | [电话] | [邮箱] | 09:00-18:00 |
| 白班 | 周四 | [姓名] | [电话] | [邮箱] | 09:00-18:00 |
| 白班 | 周五 | [姓名] | [电话] | [邮箱] | 09:00-18:00 |
| 白班 | 周六 | [姓名] | [电话] | [邮箱] | 09:00-18:00 |
| 白班 | 周日 | [姓名] | [电话] | [邮箱] | 09:00-18:00 |
| 夜班 | 周一 | [姓名] | [电话] | [邮箱] | 18:00-09:00 |
| 夜班 | 周二 | [姓名] | [电话] | [邮箱] | 18:00-09:00 |
| 夜班 | 周三 | [姓名] | [电话] | [邮箱] | 18:00-09:00 |
| 夜班 | 周四 | [姓名] | [电话] | [邮箱] | 18:00-09:00 |
| 夜班 | 周五 | [姓名] | [电话] | [邮箱] | 18:00-09:00 |
| 周末 | 周六 | [姓名] | [电话] | [邮箱] | 全天 |
| 周末 | 周日 | [姓名] | [电话] | [邮箱] | 全天 |

### 8.4 值班职责

1. **监控职责**
   - 监控系统告警和异常指标
   - 及时响应生产环境告警
   - 记录和处理突发事件

2. **响应职责**
   - P0/P1级别问题需在15分钟内响应
   - P2级别问题需在30分钟内响应
   - P3/P4级别问题需在2小时内响应

3. **交接职责**
   - 详细记录当班事件
   - 确保信息传递给下一班
   - 重要事项需口头确认

### 8.5 紧急联系方式（示例）

| 角色 | 姓名 | 电话 | 邮箱 |
|------|------|------|------|
| 运维负责人 | [姓名] | [电话] | ops-team@example.com |
| DevOps负责人 | [姓名] | [电话] | devops-team@example.com |
| 数据库管理员 | [姓名] | [电话] | dba-team@example.com |
| 安全负责人 | [姓名] | [电话] | security-team@example.com |
| 应急联系人 | [姓名] | [电话] | emergency@example.com |

> ⚠️ **注意**: 表格中的"待定"和"-"均为占位符，部署前请替换为真实联系人信息

## 9. 附录

### 9.1 相关文档

- [BackupRecoveryGuide.md](file:///e:/101/microservices/docs/BackupRecoveryGuide.md) - 备份与恢复指南
- [DeploymentProcess.md](file:///e:/101/microservices/docs/DeploymentProcess.md) - 部署流程文档
- [OperationsManual.md](file:///e:/101/microservices/docs/OperationsManual.md) - 运维手册

### 9.2 相关脚本

- [restore-database.sh](file:///e:/101/microservices/scripts/restore-database.sh) - 数据库恢复脚本
- [rolling-restart.sh](file:///e:/101/microservices/scripts/rolling-restart.sh) - 滚动重启脚本
- [verify-service-health.sh](file:///e:/101/microservices/scripts/verify-service-health.sh) - 服务健康验证脚本
- [verify-functionality.sh](file:///e:/101/microservices/scripts/verify-functionality.sh) - 功能验证脚本

### 9.3 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|---------|
| 1.0.0 | 2025-01-19 | 架构师团队 | 初始版本 |

---

**文档结束**
