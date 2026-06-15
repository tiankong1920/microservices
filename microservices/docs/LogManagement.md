# 日志管理文档

## 版本
- 版本：1.0.0
- 创建日期：2025-01-19
- 最后更新：2025-01-19

## 1. 文档概述

### 1.1 目的
本文档提供了库存管理系统日志管理的详细指南，包括日志收集、日志分析、日志归档、日志查询和日志告警配置。

### 1.2 适用范围
- 应用日志管理
- 访问日志管理
- 错误日志管理
- 性能日志管理
- 审计日志管理

### 1.3 目标读者
- 系统运维工程师
- DevOps工程师
- 日志分析师
- 安全工程师

## 2. 日志收集

### 2.1 应用日志配置

#### 2.1.1 Logback配置

```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/inventory-system.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/inventory-system.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/inventory-system-error.log</file>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/inventory-system-error.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>90</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>

    <logger name="com.inventory" level="DEBUG" additivity="false">
        <appender-ref ref="FILE" />
    </logger>
</configuration>
```

#### 2.1.2 JSON日志格式

```xml
<configuration>
    <appender name="JSON_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/inventory-system.json</file>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <providers>
                <timestamp>
                    <timeZone>UTC</timeZone>
                </timestamp>
                <pattern>
                    <pattern>
                        {
                          "timestamp": "%d{yyyy-MM-dd'T'HH:mm:ss.SSS}",
                          "level": "%level",
                          "thread": "%thread",
                          "logger": "%logger",
                          "message": "%message"
                        }
                    </pattern>
                </pattern>
            </providers>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/inventory-system.%d{yyyy-MM-dd}.json</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>

    <root level="INFO">
        <appender-ref ref="JSON_FILE" />
    </root>
</configuration>
```

### 2.2 Docker日志配置

#### 2.2.1 Docker Compose日志配置

```yaml
services:
  product-service:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
        compress: "true"
        labels: "service"
```

#### 2.2.2 日志卷挂载

```yaml
services:
  product-service:
    volumes:
      - app-logs:/app/logs
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

volumes:
  app-logs:
    driver: local
```

## 3. 日志分析

### 3.1 ELK Stack配置

#### 3.1.1 Elasticsearch配置

```yaml
version: '3'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch:8.0.0
    container_name: elasticsearch
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
      - "9300:9300"
    volumes:
      - es-data:/usr/share/elasticsearch/data
    networks:
      - logging-network

volumes:
  es-data:
    driver: local

networks:
  logging-network:
    driver: bridge
```

#### 3.1.2 Logstash配置

```yaml
version: '3'
services:
  logstash:
    image: docker.elastic.co/logstash:8.0.0
    container_name: logstash
    volumes:
      - ./logstash/pipeline:/usr/share/logstash/pipeline
      - app-logs:/app/logs
    ports:
      - "5044:5044"
    networks:
      - logging-network
    depends_on:
      - elasticsearch

networks:
  logging-network:
    driver: bridge
```

#### 3.1.3 Kibana配置

```yaml
version: '3'
services:
  kibana:
    image: docker.elastic.co/kibana:8.0.0
    container_name: kibana
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    networks:
      - logging-network
    depends_on:
      - elasticsearch

networks:
  logging-network:
    driver: bridge
```

### 3.2 日志查询

#### 3.2.1 Elasticsearch查询

```bash
# 查询所有日志
curl -X GET "http://localhost:9200/inventory-system-*/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "match_all": {}
  },
  "size": 10
}'

# 查询错误日志
curl -X GET "http://localhost:9200/inventory-system-*/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "term": {
      "level": "ERROR"
    }
  },
  "size": 100
}'

# 查询特定时间范围
curl -X GET "http://localhost:9200/inventory-system-*/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "range": {
      "@timestamp": {
        "gte": "2025-01-19T00:00:00",
        "lte": "2025-01-19T23:59:59"
      }
    }
  },
  "size": 1000
}'
```

#### 3.2.2 Kibana查询

```bash
# 访问Kibana
open http://localhost:5601

# 创建索引模式
curl -X POST "http://localhost:5601/api/saved_objects/index-pattern" -H 'Content-Type: application/json' -d'
{
  "attributes": {
    "title": "inventory-system-*",
    "timeFieldName": "@timestamp"
  }
}'
```

## 4. 日志归档

### 4.1 归档策略

#### 4.1.1 按时间归档

```bash
#!/bin/bash
# archive-logs-by-date.sh

LOG_DIR="/opt/inventory-system/logs"
ARCHIVE_DIR="/opt/inventory-system/logs/archive"
DATE=$(date +%Y%m%d)

# 创建归档目录
mkdir -p ${ARCHIVE_DIR}

# 归档30天前的日志
find ${LOG_DIR} -name "*.log" -mtime +30 -exec gzip {} \;
find ${LOG_DIR} -name "*.log.gz" -exec mv {} ${ARCHIVE_DIR}/ \;

# 删除90天前的归档
find ${ARCHIVE_DIR} -name "*.log.gz" -mtime +90 -delete

echo "日志归档完成！"
```

#### 4.1.2 按大小归档

```bash
#!/bin/bash
# archive-logs-by-size.sh

LOG_DIR="/opt/inventory-system/logs"
ARCHIVE_DIR="/opt/inventory-system/logs/archive"
MAX_SIZE=100M

# 归档大于100MB的日志文件
find ${LOG_DIR} -name "*.log" -size +${MAX_SIZE} -exec gzip {} \;
find ${LOG_DIR} -name "*.log.gz" -exec mv {} ${ARCHIVE_DIR}/ \;

echo "日志归档完成！"
```

### 4.2 归档存储

#### 4.2.1 本地存储

```bash
# 本地归档目录
ARCHIVE_DIR="/opt/inventory-system/logs/archive"

# 按日期组织归档
${ARCHIVE_DIR}/
├── 2025/
│   ├── 01/
│   ├── 02/
│   └── ...
└── 2024/
    ├── 12/
    └── ...
```

#### 4.2.2 云存储

```bash
#!/bin/bash
# archive-to-cloud.sh

ARCHIVE_DIR="/opt/inventory-system/logs/archive"
BUCKET="s3://inventory-logs"

# 上传归档到S3
aws s3 sync ${ARCHIVE_DIR} ${BUCKET} --delete

# 验证上传
aws s3 ls ${BUCKET}

echo "归档上传完成！"
```

## 5. 日志查询

### 5.1 命令行查询

#### 5.1.1 grep查询

```bash
# 查询错误日志
grep -i "error" logs/inventory-system.log

# 查询特定服务的日志
grep "product-service" logs/inventory-system.log

# 查询特定时间范围的日志
sed -n '/2025-01-19 10:00:00/,/2025-01-19 11:00:00/p' logs/inventory-system.log

# 查询包含特定关键词的日志
grep -i "exception\|timeout\|failed" logs/inventory-system.log
```

#### 5.1.2 awk查询

```bash
# 统计错误数量
awk '/ERROR/ {count++} END {print "Error count:", count}' logs/inventory-system.log

# 统计每个日志级别的数量
awk '{level[$1]++} END {for (l in level) print l, level[l]}' logs/inventory-system.log

# 提取特定字段
awk -F'"' '{print $2}' logs/inventory-system.json
```

### 5.2 日志分析工具

#### 5.2.1 使用jq分析JSON日志

```bash
# 统计错误日志
cat logs/inventory-system.json | jq '[.[] | select(.level == "ERROR")] | length'

# 统计每个服务的日志数量
cat logs/inventory-system.json | jq 'group_by(.service) | map({service: .[0].service, count: length})'

# 查找最慢的请求
cat logs/inventory-system.json | jq '[.[] | select(.duration > 1000)] | sort_by(.duration) | reverse | .[0:5]'
```

#### 5.2.2 使用awk分析日志

```bash
# 分析响应时间分布
awk -F'"' '{print $NF}' logs/inventory-system.json | awk '{sum+=$1; count++} END {print "Avg:", sum/count, "Count:", count}'

# 分析错误率
awk '/ERROR/ {error++} /INFO/ {info++} END {print "Error rate:", error/(error+info)*100"%"}' logs/inventory-system.log

# 分析请求频率
awk '{print $1}' logs/inventory-system.log | sort | uniq -c | sort -nr | head -10
```

## 6. 日志告警

### 6.1 告警规则

#### 6.1.1 错误率告警

```yaml
# Prometheus告警规则
groups:
  - name: log_alerts
    rules:
      - alert: high_error_rate
        expr: rate(log_errors_total[5m]) > 0.1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }} errors per second"
```

#### 6.1.2 关键错误告警

```yaml
groups:
  - name: log_alerts
    rules:
      - alert: critical_error
        expr: log_errors_total{level="CRITICAL"} > 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Critical error detected"
          description: "Critical error: {{ $labels.error_message }}"
```

### 6.2 告警通知

#### 6.2.1 邮件通知

```bash
#!/bin/bash
# log-alert-email.sh

ALERT_TYPE=$1
ALERT_MESSAGE=$2
RECIPIENT="ops-team@example.com"

# 发送告警邮件
echo "Subject: [LOG ALERT] ${ALERT_TYPE}" | sendmail -t ${RECIPIENT} << EOF
Alert Type: ${ALERT_TYPE}
Message: ${ALERT_MESSAGE}
Time: $(date)
EOF

echo "告警邮件已发送！"
```

#### 6.2.2 Webhook通知

```bash
#!/bin/bash
# log-alert-webhook.sh

ALERT_TYPE=$1
ALERT_MESSAGE=$2
WEBHOOK_URL="https://webhook.example.com/alerts"

# 发送Webhook通知
curl -X POST ${WEBHOOK_URL} -H "Content-Type: application/json" -d '
{
  "alert_type": "'"${ALERT_TYPE}"'",
  "message": "'"${ALERT_MESSAGE}"'",
  "timestamp": "'"$(date -Iseconds)"'",
  "source": "inventory-system"
}'

echo "Webhook通知已发送！"
```

## 7. 日志安全

### 7.1 访问控制

#### 7.1.1 文件权限

```bash
# 设置日志文件权限
chmod 640 /opt/inventory-system/logs/*.log
chown root:app-user /opt/inventory-system/logs/*.log

# 设置日志目录权限
chmod 750 /opt/inventory-system/logs
chown root:app-user /opt/inventory-system/logs
```

#### 7.1.2 日志加密

```bash
# 加密敏感日志
gpg --encrypt --recipient "security@example.com" logs/inventory-system-sensitive.log
```

### 7.2 日志审计

#### 7.2.1 审计日志

```xml
<configuration>
    <appender name="AUDIT_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/inventory-system-audit.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/inventory-system-audit.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>365</maxHistory>
        </rollingPolicy>
    </appender>

    <logger name="AUDIT_LOGGER" level="INFO" additivity="false">
        <appender-ref ref="AUDIT_FILE" />
    </logger>
</configuration>
```

## 8. 最佳实践

### 8.1 日志收集原则

1. **结构化日志**
   - 使用JSON格式
   - 包含时间戳、级别、服务名
   - 便于解析和分析

2. **日志级别**
   - ERROR：错误信息
   - WARN：警告信息
   - INFO：重要信息
   - DEBUG：调试信息

3. **日志轮转**
   - 按时间轮转
   - 按大小轮转
   - 保留合理的历史

### 8.2 日志分析原则

1. **定期分析**
   - 每天分析日志
   - 每周生成报告
   - 及时发现问题

2. **关键指标**
   - 错误率
   - 响应时间
   - 请求量
   - 资源使用

3. **趋势分析**
   - 观察长期趋势
   - 识别异常模式
   - 预测潜在问题

### 8.3 日志归档原则

1. **定期归档**
   - 每周归档日志
   - 保留90天历史
   - 清理过期日志

2. **备份归档**
   - 归档到多个位置
   - 使用云存储
   - 验证归档完整性

3. **归档验证**
   - 定期验证归档
   - 测试恢复流程
   - 记录归档状态

## 9. 故障排查

### 9.1 日志丢失

```bash
# 检查日志文件
ls -la /opt/inventory-system/logs/

# 检查日志大小
du -sh /opt/inventory-system/logs/

# 检查日志权限
stat /opt/inventory-system/logs/inventory-system.log
```

### 9.2 日志性能问题

```bash
# 检查日志写入性能
time tail -f /opt/inventory-system/logs/inventory-system.log

# 检查日志磁盘IO
iostat -x 1 /opt/inventory-system/logs/

# 检查日志存储空间
df -h /opt/inventory-system/logs/
```

## 10. 附录

### 10.1 相关文档

- [DeploymentProcess.md](file:///e:/101/microservices/docs/DeploymentProcess.md) - 部署流程文档
- [ContainerOrchestration.md](file:///e:/101/microservices/docs/ContainerOrchestration.md) - 容器编排文档

### 10.2 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|---------|
| 1.0.0 | 2025-01-19 | 架构师团队 | 初始版本 |

---

**文档结束**
