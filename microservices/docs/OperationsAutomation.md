# 运维自动化文档

## 版本
- 版本：1.0.0
- 创建日期：2025-01-19
- 最后更新：2025-01-19

## 1. 文档概述

### 1.1 目的
本文档提供了库存管理系统运维自动化的详细指南，包括自动化部署脚本、自动化监控脚本、自动化备份脚本、CI/CD集成和自动化告警处理。

### 1.2 适用范围
- 自动化部署
- 自动化监控
- 自动化备份
- 自动化告警处理
- 自动化日志分析
- 自动化性能优化

### 1.3 目标读者
- 系统运维工程师
- DevOps工程师
- 自动化工程师
- 系统架构师

## 2. 自动化部署

### 2.1 部署自动化脚本

#### 2.1.1 完整部署脚本

```bash
#!/bin/bash
# auto-deploy.sh

set -e

ENVIRONMENT=${1:-prod}
COMPOSE_FILE="docker-compose.${ENVIRONMENT}.yml"
BACKUP_DIR="/opt/inventory-system/backups"
LOG_DIR="/opt/inventory-system/logs"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "=========================================="
echo "  自动化部署脚本"
echo "=========================================="

# 创建备份目录
mkdir -p ${BACKUP_DIR}/${TIMESTAMP}
mkdir -p ${LOG_DIR}

# 备份当前配置
echo "备份当前配置..."
cp -r config/ ${BACKUP_DIR}/${TIMESTAMP}/config/
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

# 记录部署结果
if [ $? -eq 0 ]; then
    echo "=========================================="
    echo "  部署成功！"
    echo "=========================================="
    exit 0
else
    echo "=========================================="
    echo "  部署失败！"
    echo "=========================================="
    exit 1
fi
```

#### 2.1.2 滚动更新脚本

```bash
#!/bin/bash
# auto-rolling-update.sh

set -e

SERVICE_NAME=${1:-product-service}
NEW_VERSION=${2:-latest}
COMPOSE_FILE="docker-compose.prod.yml"
MAX_REPLICAS=3

echo "开始自动化滚动更新 ${SERVICE_NAME} 到版本 ${NEW_VERSION}..."

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

#### 2.1.3 蓝绿部署脚本

```bash
#!/bin/bash
# auto-blue-green-deploy.sh

set -e

SERVICE_NAME=${1:-product-service}
NEW_VERSION=${2:-latest}
BLUE_PORT=8082
GREEN_PORT=8083

echo "开始自动化蓝绿部署 ${SERVICE_NAME}..."

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

## 3. 自动化监控

### 3.1 监控脚本

#### 3.1.1 健康检查自动化

```bash
#!/bin/bash
# auto-health-check.sh

set -e

SERVICES=(
    "product-service:8082"
    "order-service:8081"
    "inventory-service:8080"
    "gateway-service:9090"
)

ALERT_THRESHOLD=3

echo "开始自动化健康检查..."

FAILED_SERVICES=()

for service in "${SERVICES[@]}"; do
    NAME=$(echo $service | cut -d':' -f1)
    PORT=$(echo $service | cut -d':' -f2)
    
    echo "检查 ${NAME}..."
    if ! curl -f http://localhost:${PORT}/actuator/health > /dev/null 2>&1; then
        echo "✗ ${NAME}: 不健康"
        FAILED_SERVICES+=(${NAME})
    else
        echo "✓ ${NAME}: 健康"
    fi
done

# 检查失败服务数量
if [ ${#FAILED_SERVICES[@]} -gt ${ALERT_THRESHOLD} ]; then
    echo "警告: ${#FAILED_SERVICES[@]} 个服务不健康"
    
    # 发送告警
    ./scripts/send-alert.sh "Health Check Failed" "${FAILED_SERVICES[*]}"
    
    # 尝试自动重启
    for service in "${FAILED_SERVICES[@]}"; do
        echo "尝试重启 ${service}..."
        docker-compose -f docker-compose.prod.yml restart ${service}
        sleep 30
    done
else
    echo "所有服务健康"
fi

echo "健康检查完成！"
```

#### 3.1.2 性能监控自动化

```bash
#!/bin/bash
# auto-performance-monitor.sh

set -e

echo "开始自动化性能监控..."

# 监控CPU使用率
CPU_USAGE=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1)
echo "CPU使用率: ${CPU_USAGE}%"

if [ ${CPU_USAGE} -gt 80 ]; then
    echo "警告: CPU使用率过高"
    ./scripts/send-alert.sh "High CPU Usage" "CPU usage: ${CPU_USAGE}%"
fi

# 监控内存使用率
MEMORY_USAGE=$(free | grep Mem | awk '{printf("%.1f\n", $3/$2 * 100)}')
echo "内存使用率: ${MEMORY_USAGE}%"

if [ ${MEMORY_USAGE} -gt 85 ]; then
    echo "警告: 内存使用率过高"
    ./scripts/send-alert.sh "High Memory Usage" "Memory usage: ${MEMORY_USAGE}%"
fi

# 监控磁盘使用率
DISK_USAGE=$(df -h / | awk 'NR==2 {print $5}' | cut -d'%' -f1)
echo "磁盘使用率: ${DISK_USAGE}%"

if [ ${DISK_USAGE} -gt 85 ]; then
    echo "警告: 磁盘使用率过高"
    ./scripts/send-alert.sh "High Disk Usage" "Disk usage: ${DISK_USAGE}%"
fi

echo "性能监控完成！"
```

#### 3.1.3 日志监控自动化

```bash
#!/bin/bash
# auto-log-monitor.sh

set -e

LOG_DIR="/opt/inventory-system/logs"
ERROR_THRESHOLD=10

echo "开始自动化日志监控..."

# 检查错误日志数量
ERROR_COUNT=$(find ${LOG_DIR} -name "*.log" -exec grep -l "ERROR" {} \; | wc -l)
echo "错误日志数量: ${ERROR_COUNT}"

if [ ${ERROR_COUNT} -gt ${ERROR_THRESHOLD} ]; then
    echo "警告: 错误日志过多"
    ./scripts/send-alert.sh "High Error Count" "Error count: ${ERROR_COUNT}"
fi

# 检查最近的错误
RECENT_ERRORS=$(find ${LOG_DIR} -name "*.log" -mmin -60 -exec grep -h "ERROR" {} \; | head -10)
echo "最近60分钟的错误:"
echo "${RECENT_ERRORS}"

echo "日志监控完成！"
```

## 4. 自动化备份

### 4.1 备份自动化脚本

#### 4.1.1 数据库备份自动化

```bash
#!/bin/bash
# auto-backup-database.sh

set -e

BACKUP_DIR="/opt/inventory-system/backups/database"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=30

echo "开始自动化数据库备份..."

# 创建备份目录
mkdir -p ${BACKUP_DIR}

# 数据库全量备份
echo "执行数据库全量备份..."
docker exec postgres-1 pg_dump -U postgres -d inventory > ${BACKUP_DIR}/inventory_full_${DATE}.sql

# 压缩备份文件
echo "压缩备份文件..."
gzip ${BACKUP_DIR}/inventory_full_${DATE}.sql

# 删除过期备份
echo "删除 ${RETENTION_DAYS} 天前的备份..."
find ${BACKUP_DIR} -name "inventory_full_*.sql.gz" -mtime +${RETENTION_DAYS} -delete

# 上传到云存储（可选）
# aws s3 cp ${BACKUP_DIR}/inventory_full_${DATE}.sql.gz s3://backup-bucket/inventory/

# 验证备份
echo "验证备份..."
if [ -f ${BACKUP_DIR}/inventory_full_${DATE}.sql.gz ]; then
    echo "备份成功: ${BACKUP_DIR}/inventory_full_${DATE}.sql.gz"
    SIZE=$(du -h ${BACKUP_DIR}/inventory_full_${DATE}.sql.gz | cut -f1)
    echo "备份大小: ${SIZE}"
else
    echo "备份失败"
    exit 1
fi

echo "数据库备份完成！"
```

#### 4.1.2 配置备份自动化

```bash
#!/bin/bash
# auto-backup-config.sh

set -e

BACKUP_DIR="/opt/inventory-system/backups/config"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=90

echo "开始自动化配置备份..."

# 创建备份目录
mkdir -p ${BACKUP_DIR}

# 备份配置文件
echo "备份配置文件..."
tar -czf ${BACKUP_DIR}/config_${DATE}.tar.gz \
  config/ \
  docker-compose*.yml \
  .env*

# 删除过期备份
echo "删除 ${RETENTION_DAYS} 天前的备份..."
find ${BACKUP_DIR} -name "config_*.tar.gz" -mtime +${RETENTION_DAYS} -delete

# 验证备份
echo "验证备份..."
if [ -f ${BACKUP_DIR}/config_${DATE}.tar.gz ]; then
    echo "备份成功: ${BACKUP_DIR}/config_${DATE}.tar.gz"
    SIZE=$(du -h ${BACKUP_DIR}/config_${DATE}.tar.gz | cut -f1)
    echo "备份大小: ${SIZE}"
else
    echo "备份失败"
    exit 1
fi

echo "配置备份完成！"
```

#### 4.1.3 日志备份自动化

```bash
#!/bin/bash
# auto-backup-logs.sh

set -e

LOG_DIR="/opt/inventory-system/logs"
ARCHIVE_DIR="/opt/inventory-system/logs/archive"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=90

echo "开始自动化日志备份..."

# 创建归档目录
mkdir -p ${ARCHIVE_DIR}

# 归档日志文件
echo "归档日志文件..."
find ${LOG_DIR} -name "*.log" -mtime +1 -exec gzip {} \;
find ${LOG_DIR} -name "*.log.gz" -exec mv {} ${ARCHIVE_DIR}/ \;

# 删除过期归档
echo "删除 ${RETENTION_DAYS} 天前的归档..."
find ${ARCHIVE_DIR} -name "*.log.gz" -mtime +${RETENTION_DAYS} -delete

# 验证归档
ARCHIVE_COUNT=$(find ${ARCHIVE_DIR} -name "*.log.gz" | wc -l)
echo "归档文件数量: ${ARCHIVE_COUNT}"

echo "日志备份完成！"
```

## 5. CI/CD集成

### 5.1 GitHub Actions配置

#### 5.1.1 工作流配置

```yaml
name: Inventory System CI/CD

on:
  push:
    branches:
      - main
      - develop
  pull_request:
    branches:
      - main

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Build with Maven
        run: mvn clean package -DskipTests

      - name: Build Docker images
        run: |
          docker build -t inventory-system/product-service:${{ github.sha }} ./core-services/product-service
          docker build -t inventory-system/order-service:${{ github.sha }} ./core-services/order-service

      - name: Login to Docker Hub
        run: echo "${{ secrets.DOCKER_PASSWORD }}" | docker login -u "${{ secrets.DOCKER_USERNAME }}" --password-stdin

      - name: Push Docker images
        run: |
          docker push inventory-system/product-service:${{ github.sha }}
          docker push inventory-system/order-service:${{ github.sha }}

      - name: Deploy to production
        run: |
          ssh user@production-server "cd /opt/inventory-system && docker-compose -f docker-compose.prod.yml pull && docker-compose -f docker-compose.prod.yml up -d"
```

#### 5.1.2 自动化测试配置

```yaml
name: Automated Tests

on:
  push:
    branches:
      - main

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Run unit tests
        run: mvn test

      - name: Run integration tests
        run: mvn verify

      - name: Generate test report
        run: mvn surefire-report:report

      - name: Upload test results
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: target/surefire-reports
```

### 5.2 Jenkins配置

#### 5.2.1 Jenkinsfile配置

```groovy
pipeline {
    agent any
    
    stages {
        stage('Build') {
            steps {
                checkout scm
                sh 'mvn clean package -DskipTests'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
                sh 'mvn verify'
            }
        }
        
        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                sh '''
                    cd /opt/inventory-system
                    docker-compose -f docker-compose.prod.yml pull
                    docker-compose -f docker-compose.prod.yml up -d
                '''
            }
        }
    }
}
```

#### 5.2.2 自动化部署配置

```groovy
pipeline {
    agent any
    
    stages {
        stage('Deploy to Staging') {
            steps {
                sh '''
                    cd /opt/inventory-system/staging
                    docker-compose -f docker-compose.staging.yml pull
                    docker-compose -f docker-compose.staging.yml up -d
                '''
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
                input {
                    message 'Deploy to production?',
                    ok: 'Yes, deploy'
                }
            }
            steps {
                sh '''
                    cd /opt/inventory-system/production
                    docker-compose -f docker-compose.prod.yml pull
                    docker-compose -f docker-compose.prod.yml up -d
                '''
            }
        }
    }
}
```

## 6. 自动化告警处理

### 6.1 告警处理脚本

#### 6.1.1 告警发送脚本

```bash
#!/bin/bash
# send-alert.sh

ALERT_TYPE=${1:-INFO}
ALERT_MESSAGE=${2:-Alert message}
RECIPIENT=${3:-ops-team@example.com}
WEBHOOK_URL=${4:-https://webhook.example.com/alerts}

echo "发送告警: ${ALERT_TYPE}"

# 发送邮件
echo "Subject: [ALERT] ${ALERT_TYPE}" | sendmail -t ${RECIPIENT} << EOF
Alert Type: ${ALERT_TYPE}
Message: ${ALERT_MESSAGE}
Time: $(date)
EOF

# 发送Webhook
curl -X POST ${WEBHOOK_URL} -H "Content-Type: application/json" -d '
{
  "alert_type": "'"${ALERT_TYPE}"'",
  "message": "'"${ALERT_MESSAGE}"'",
  "timestamp": "'"$(date -Iseconds)"'",
  "source": "inventory-system"
}'

echo "告警已发送！"
```

#### 6.1.2 告警聚合脚本

```bash
#!/bin/bash
# aggregate-alerts.sh

ALERT_FILE="/tmp/alerts.log"
AGGREGATION_WINDOW=300
THRESHOLD=5

echo "开始告警聚合..."

# 聚合告警
ALERT_COUNT=$(grep -c "ALERT" ${ALERT_FILE})

if [ ${ALERT_COUNT} -ge ${THRESHOLD} ]; then
    echo "告警数量超过阈值: ${ALERT_COUNT}"
    
    # 聚合告警信息
    ALERT_SUMMARY=$(grep "ALERT" ${ALERT_FILE} | tail -${THRESHOLD})
    
    # 发送聚合告警
    ./scripts/send-alert.sh "Aggregated Alerts" "${ALERT_SUMMARY}"
    
    # 清空告警文件
    > ${ALERT_FILE}
fi

echo "告警聚合完成！"
```

#### 6.1.3 告警抑制脚本

```bash
#!/bin/bash
# suppress-alerts.sh

ALERT_FILE="/tmp/alerts.log"
SUPPRESSION_FILE="/tmp/suppression-rules.txt"
SUPPRESSION_WINDOW=600

echo "开始告警抑制..."

# 检查抑制规则
if [ -f ${SUPPRESSION_FILE} ]; then
    while IFS= read -r rule; do
        ALERT_TYPE=$(echo $rule | cut -d':' -f1)
        SUPPRESSION_TIME=$(echo $rule | cut -d':' -f2)
        
        # 检查是否在抑制窗口内
        LAST_ALERT_TIME=$(grep "${ALERT_TYPE}" ${ALERT_FILE} | tail -1 | awk '{print $1}')
        CURRENT_TIME=$(date +%s)
        
        if [ $((CURRENT_TIME - LAST_ALERT_TIME)) -lt ${SUPPRESSION_TIME} ]; then
            echo "抑制告警: ${ALERT_TYPE}"
            continue
        fi
    done < ${SUPPRESSION_FILE}
fi

echo "告警抑制完成！"
```

## 7. 定时任务

### 7.1 Cron配置

```bash
# 编辑crontab
crontab -e

# 每天凌晨2点执行数据库备份
0 2 * * * /opt/inventory-system/scripts/auto-backup-database.sh

# 每天凌晨3点执行配置备份
0 3 * * * /opt/inventory-system/scripts/auto-backup-config.sh

# 每天凌晨4点执行日志备份
0 4 * * * /opt/inventory-system/scripts/auto-backup-logs.sh

# 每小时执行健康检查
0 * * * * /opt/inventory-system/scripts/auto-health-check.sh

# 每天凌晨1点执行性能监控
0 1 * * * /opt/inventory-system/scripts/auto-performance-monitor.sh

# 每周日凌晨2点执行清理任务
0 2 * * 0 /opt/inventory-system/scripts/cleanup.sh
```

### 7.2 Systemd定时器

```ini
# /etc/systemd/system/inventory-backup.service
[Unit]
Description=Inventory System Backup Service
After=network.target

[Service]
Type=oneshot
User=root
ExecStart=/opt/inventory-system/scripts/auto-backup-database.sh
WorkingDirectory=/opt/inventory-system/scripts

[Timer]
OnCalendar=daily
Persistent=true

[Install]
WantedBy=multi-user.target
```

### 7.3 自动化清理脚本

```bash
#!/bin/bash
# auto-cleanup.sh

set -e

# 清理Docker镜像
echo "清理Docker镜像..."
docker system prune -a -f --filter "until=24h"

# 清理未使用的卷
echo "清理未使用的卷..."
docker volume prune -f

# 清理构建缓存
echo "清理构建缓存..."
mvn clean

# 清理临时文件
echo "清理临时文件..."
find /tmp -name "*.tmp" -mtime +7 -delete

echo "清理完成！"
```

## 8. 最佳实践

### 8.1 自动化原则

1. **幂等性**
   - 脚本可以多次执行而不产生副作用
   - 检查前置条件
   - 避免重复操作

2. **错误处理**
   - 所有脚本都应有错误处理
   - 提供有意义的错误信息
   - 设置适当的退出码

3. **日志记录**
   - 记录所有操作
   - 使用结构化日志
   - 便于问题排查

4. **验证机制**
   - 每个操作后验证结果
   - 提供详细的验证输出
   - 失败时回滚

5. **通知机制**
   - 关键操作发送通知
   - 提供操作状态更新
   - 及时发现问题

### 8.2 自动化测试

1. **单元测试**
   - 每次提交前运行
   - 覆盖核心业务逻辑
   - 保持高测试覆盖率

2. **集成测试**
   - 测试服务间通信
   - 测试数据库集成
   - 测试缓存集成

3. **端到端测试**
   - 测试完整业务流程
   - 验证用户体验
   - 检查性能指标

4. **性能测试**
   - 负载测试
   - 压力测试
   - 稳定性测试

### 8.3 自动化部署

1. **蓝绿部署**
   - 实现零停机部署
   - 快速回滚能力
   - 自动化流量切换

2. **滚动更新**
   - 逐步更新服务
   - 避免服务中断
   - 验证每个实例

3. **金丝雀发布**
   - 小规模测试新版本
   - 逐步扩大流量
   - 监控关键指标

## 9. 故障排查

### 9.1 自动化脚本问题

#### 9.1.1 脚本执行失败

```bash
# 检查脚本权限
ls -la /opt/inventory-system/scripts/

# 检查脚本语法
bash -n /opt/inventory-system/scripts/auto-deploy.sh

# 检查依赖
which docker
which docker-compose
which jq
```

#### 9.1.2 定时任务问题

```bash
# 查看cron任务
crontab -l

# 查看systemd服务
systemctl list-timers

# 查看定时任务日志
journalctl -u root -n inventory-backup
```

#### 9.1.3 CI/CD问题

```bash
# 检查GitHub Actions日志
# 查看Jenkins构建日志

# 检查部署状态
docker ps -a
docker-compose -f docker-compose.prod.yml ps
```

## 10. 附录

### 10.1 相关文档

- [DeploymentProcess.md](file:///e:/101/microservices/docs/DeploymentProcess.md) - 部署流程文档
- [ContainerOrchestration.md](file:///e:/101/microservices/docs/ContainerOrchestration.md) - 容器编排文档
- [ServiceScaling.md](file:///e:/101/microservices/docs/ServiceScaling.md) - 服务扩缩容文档
- [ConfigurationManagement.md](file:///e:/101/microservices/docs/ConfigurationManagement.md) - 配置管理文档
- [LogManagement.md](file:///e:/101/microservices/docs/LogManagement.md) - 日志管理文档
- [PerformanceTuning.md](file:///e:/101/microservices/docs/PerformanceTuning.md) - 性能调优文档
- [DisasterRecovery.md](file:///e:/101/microservices/docs/DisasterRecovery.md) - 故障恢复文档

### 10.2 相关脚本

- [auto-deploy.sh](file:///e:/101/microservices/scripts/auto-deploy.sh) - 自动化部署脚本
- [auto-rolling-update.sh](file:///e:/101/microservices/scripts/auto-rolling-update.sh) - 滚动更新脚本
- [auto-blue-green-deploy.sh](file:///e:/101/microservices/scripts/auto-blue-green-deploy.sh) - 蓝绿部署脚本
- [auto-health-check.sh](file:///e:/101/microservices/scripts/auto-health-check.sh) - 自动化健康检查脚本
- [auto-backup-database.sh](file:///e:/101/microservices/scripts/auto-backup-database.sh) - 自动化数据库备份脚本

### 10.3 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|---------|
| 1.0.0 | 2025-01-19 | 架构师团队 | 初始版本 |

---

**文档结束**
