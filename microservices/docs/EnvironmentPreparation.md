# 环境准备文档

## 版本
- 版本：1.0.0
- 创建日期：2025-01-18
- 最后更新：2025-01-18

## 1. 文档概述

### 1.1 目的
本文档提供了部署库存管理系统监控与追踪系统所需的环境准备步骤，包括系统要求、依赖安装、环境变量配置、网络配置、端口配置和权限配置。通过本文档，您可以确保环境满足系统运行的所有前置条件。

### 1.2 适用范围
- 开发环境准备
- 测试环境准备
- 生产环境准备
- 容器化环境准备
- 云平台环境准备

### 1.3 目标读者
- 系统管理员
- DevOps工程师
- 运维工程师
- 开发人员

## 2. 系统要求

### 2.1 硬件要求

#### 2.1.1 开发环境
| 组件 | 最低配置 | 推荐配置 | 说明 |
|--------|---------|---------|------|
| CPU | 4核 | 8核 | 支持多线程编译和运行 |
| 内存 | 8GB | 16GB | JVM堆内存和系统开销 |
| 磁盘 | 50GB SSD | 100GB SSD | 应用和日志存储 |
| 网络 | 100Mbps | 1Gbps | 内网通信和外部API访问 |

#### 2.1.2 测试环境
| 组件 | 最低配置 | 推荐配置 | 说明 |
|--------|---------|---------|------|
| CPU | 8核 | 16核 | 支持并发测试 |
| 内存 | 16GB | 32GB | 多个测试实例并行运行 |
| 磁盘 | 100GB SSD | 200GB SSD | 测试数据和日志存储 |
| 网络 | 1Gbps | 10Gbps | 高并发测试网络 |

#### 2.1.3 生产环境
| 组件 | 最低配置 | 推荐配置 | 说明 |
|--------|---------|---------|------|
| CPU | 16核 | 32核 | 高并发处理能力 |
| 内存 | 32GB | 64GB | 大堆内存和缓存 |
| 磁盘 | 500GB SSD | 1TB SSD | 生产数据和日志存储 |
| 网络 | 10Gbps | 40Gbps | 高吞吐量网络 |

### 2.2 软件要求

#### 2.2.1 操作系统
- **推荐操作系统**：Linux（CentOS 7+、Ubuntu 18.04+、Debian 10+）
- **最低内核版本**：3.10+
- **必需软件包**：
  - `curl`（HTTP请求）
  - `wget`（文件下载）
  - `tar`（压缩文件解压）
  - `unzip`（ZIP文件解压）
  - `vim`或`nano`（文本编辑）

#### 2.2.2 Java环境
- **Java版本**：JDK 17或JDK 21
- **推荐版本**：JDK 21 LTS
- **配置要求**：
  - `JAVA_HOME`环境变量已设置
  - `java -version`命令可用
  - JVM参数支持（G1GC、ZGC等）

#### 2.2.3 数据库
- **MySQL**：8.0+（推荐8.0.36+）
- **PostgreSQL**：14+（推荐15+）
- **Redis**：7.0+（推荐7.2+）
- **MongoDB**：6.0+（推荐7.0+）

#### 2.2.4 消息队列
- **Kafka**：3.5+（推荐3.7+）
- **RabbitMQ**：3.12+（推荐3.13+）

#### 2.2.5 监控系统
- **Prometheus**：2.45+（推荐2.50+）
- **Grafana**：10.0+（推荐11.0+）
- **Alertmanager**：0.25+（推荐0.27+）
- **Jaeger**：1.50+（推荐1.53+）
- **Loki**：2.9+（推荐3.0+）

### 2.3 网络要求

#### 2.3.1 端口配置
| 服务 | 端口 | 协议 | 说明 |
|------|------|------|------|
| 应用服务 | 8080 | HTTP | 应用主端口 |
| 管理端口 | 8081 | HTTP | Actuator管理端口 |
| Prometheus | 9090 | HTTP | Prometheus指标端点 |
| Grafana | 3000 | HTTP | Grafana Web界面 |
| Alertmanager | 9093 | HTTP | Alertmanager Web界面 |
| Jaeger | 16686 | HTTP | Jaeger UI端口 |
| Jaeger Collector | 14268 | HTTP | Jaeger Collector端口 |
| Loki | 3100 | HTTP | Loki HTTP端口 |
| Kafka Broker | 9092 | TCP | Kafka内部通信 |
| Kafka External | 9094 | TCP | Kafka外部访问 |
| MySQL | 3306 | TCP | MySQL数据库端口 |
| PostgreSQL | 5432 | TCP | PostgreSQL数据库端口 |
| Redis | 6379 | TCP | Redis缓存端口 |

#### 2.3.2 防火墙配置
```bash
# 开放必需端口
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=8081/tcp
sudo firewall-cmd --permanent --add-port=9090/tcp
sudo firewall-cmd --permanent --add-port=3000/tcp
sudo firewall-cmd --permanent --add-port=9093/tcp
sudo firewall-cmd --permanent --add-port=16686/tcp
sudo firewall-cmd --permanent --add-port=14268/tcp
sudo firewall-cmd --permanent --add-port=3100/tcp
sudo firewall-cmd --permanent --add-port=9092/tcp
sudo firewall-cmd --permanent --add-port=9094/tcp
sudo firewall-cmd --permanent --add-port=3306/tcp
sudo firewall-cmd --permanent --add-port=5432/tcp
sudo firewall-cmd --permanent --add-port=6379/tcp

# 重载防火墙规则
sudo firewall-cmd --reload

# 验证端口开放
sudo firewall-cmd --list-ports
```

#### 2.3.3 网络延迟要求
- **应用到Prometheus**：< 10ms
- **应用到Kafka**：< 5ms
- **应用到数据库**：< 1ms
- **应用到Redis**：< 1ms

## 3. 依赖安装

### 3.1 基础工具安装

#### 3.1.1 CentOS/RHEL
```bash
# 更新系统包
sudo yum update -y

# 安装基础工具
sudo yum install -y curl wget tar unzip vim git

# 安装Java 21
sudo yum install -y java-21-openjdk-devel

# 验证Java安装
java -version

# 设置JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk' | sudo tee -a /etc/profile.d/java.sh
source /etc/profile.d/java.sh

# 安装Maven
sudo yum install -y maven

# 验证Maven安装
mvn -version
```

#### 3.1.2 Ubuntu/Debian
```bash
# 更新系统包
sudo apt update -y
sudo apt upgrade -y

# 安装基础工具
sudo apt install -y curl wget tar unzip vim git

# 安装Java 21
sudo apt install -y openjdk-21-jdk

# 验证Java安装
java -version

# 设置JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk' | sudo tee -a /etc/profile.d/java.sh
source /etc/profile.d/java.sh

# 安装Maven
sudo apt install -y maven

# 验证Maven安装
mvn -version
```

### 3.2 Docker安装（可选）

#### 3.2.1 安装Docker
```bash
# CentOS/RHEL
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install -y docker-ce docker-ce-cli containerd.io

# Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# 启动Docker服务
sudo systemctl start docker
sudo systemctl enable docker

# 验证Docker安装
docker --version
```

#### 3.2.2 安装Docker Compose
```bash
# 下载Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.24.5/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# 添加执行权限
sudo chmod +x /usr/local/bin/docker-compose

# 验证安装
docker-compose --version
```

### 3.3 监控系统安装

#### 3.3.1 Prometheus安装
```bash
# 下载Prometheus
wget https://github.com/prometheus/prometheus/releases/download/v2.50.0/prometheus-2.50.0.linux-amd64.tar.gz

# 解压Prometheus
tar xvfz prometheus-2.50.0.linux-amd64.tar.gz

# 移动到安装目录
sudo mv prometheus-2.50.0.linux-amd64 /opt/prometheus

# 创建Prometheus用户
sudo useradd --no-create-home --shell /bin/false prometheus

# 创建数据目录
sudo mkdir -p /data/prometheus
sudo chown prometheus:prometheus /data/prometheus

# 创建配置文件
sudo vim /opt/prometheus/prometheus.yml
```

Prometheus配置示例：
```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    cluster: 'inventory-cluster'
    environment: 'production'

scrape_configs:
  - job_name: 'inventory-services'
    static_configs:
      - targets: 
          - 'localhost:8080/actuator/prometheus'
          - 'localhost:8081/actuator/prometheus'
          - 'localhost:8082/actuator/prometheus'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
    scrape_timeout: 10s

alerting:
  alertmanagers:
    - static_configs:
        - targets:
            - 'localhost:9093'
```

#### 3.3.2 Grafana安装
```bash
# 下载Grafana
wget https://dl.grafana.com/oss/release/grafana-11.0.0/grafana-11.0.0.linux-amd64.tar.gz

# 解压Grafana
tar xvfz grafana-11.0.0.linux-amd64.tar.gz

# 移动到安装目录
sudo mv grafana-11.0.0 /opt/grafana

# 创建Grafana用户
sudo useradd --no-create-home --shell /bin/false grafana

# 创建数据目录
sudo mkdir -p /data/grafana
sudo chown grafana:grafana /data/grafana

# 配置Grafana服务
sudo vim /etc/systemd/system/grafana.service
```

Grafana服务配置：
```ini
[Unit]
Description=Grafana
After=network.target

[Service]
User=grafana
Group=grafana
Type=notify
ExecStart=/opt/grafana/bin/grafana-server \
  --config=/etc/grafana/grafana.ini \
  --homepath=/data/grafana \
  cfg:default.paths.logs=/var/log/grafana
Restart=on-failure
RestartSec=10s

[Install]
WantedBy=multi-user.target
```

#### 3.3.3 Jaeger安装
```bash
# 下载Jaeger
wget https://github.com/jaegertracing/jaeger/releases/download/v1.53.0/jaeger-1.53.0-linux-amd64.tar.gz

# 解压Jaeger
tar xvfz jaeger-1.53.0-linux-amd64.tar.gz

# 移动到安装目录
sudo mv jaeger-1.53.0-linux-amd64 /opt/jaeger

# 创建Jaeger用户
sudo useradd --no-create-home --shell /bin/false jaeger

# 创建数据目录
sudo mkdir -p /data/jaeger
sudo chown jaeger:jaeger /data/jaeger

# 配置Jaeger Collector
sudo vim /opt/jaeger/jaeger-collector-config.yaml
```

Jaeger配置示例：
```yaml
collector:
  zipkin:
    host-port: 9411
  otlp:
    http:
      endpoint: 0.0.0.0:4318
  metrics:
    prometheus:
      host-port: 14269
      route: /metrics
  tracing:
    max-tag-value-length: 256
```

## 4. 环境变量配置

### 4.1 应用环境变量

创建应用环境变量文件：

```bash
# /etc/profile.d/inventory-service.sh
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH=$JAVA_HOME/bin:$PATH
export SPRING_PROFILES_ACTIVE=prod
export SPRING_APPLICATION_NAME=inventory-service
export SERVER_PORT=8080
export MANAGEMENT_PORT=8081

# Hera监控配置
export HERA_SERVER_URL=http://localhost:8080
export HERA_API_KEY=your-api-key-here
export HERA_MONITORING_ENABLED=true

# OpenTelemetry配置
export JAEGER_ENDPOINT=http://localhost:14268/api/traces
export JAEGER_AUTH_TOKEN=your-auth-token-here
export OTEL_SAMPLING_RATIO=0.1
export OTEL_EXPORTER_JAEGER_ENDPOINT=http://localhost:14268/api/traces

# Prometheus配置
export PROMETHEUS_HOST=localhost
export PROMETHEUS_PORT=9090

# Grafana配置
export GRAFANA_HOST=localhost
export GRAFANA_PORT=3000

# 数据库配置
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=inventory_db
export DB_USERNAME=inventory_user
export DB_PASSWORD=your-password-here

# Redis配置
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=your-redis-password-here

# Kafka配置
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export KAFKA_CONSUMER_GROUP=inventory-consumer-group
export KAFKA_PRODUCER_ACKS=all

# 日志配置
export LOG_LEVEL=INFO
export LOG_PATH=/var/log/inventory-service
export LOG_MAX_SIZE=100MB
export LOG_MAX_HISTORY=30

# JVM配置
export JVM_XMS=2g
export JVM_XMX=4g
export JVM_NEW_RATIO=0.3
export JVM_METASPACE_SIZE=256m

# 监控配置
export MONITORING_ENABLED=true
export METRICS_EXPORT_INTERVAL=15
export TRACING_SAMPLING_RATE=0.1
export ALERTING_ENABLED=true
export ALERTING_AGGREGATION_WINDOW=5
export ALERTING_DEDUPLICATION_ENABLED=true
```

### 4.2 系统环境变量

创建系统环境变量：

```bash
# /etc/environment
JAVA_HOME=/usr/lib/jvm/java-21-openjdk
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/bin
LANG=en_US.UTF-8
LC_ALL=en_US.UTF-8
TZ=Asia/Shanghai
```

### 4.3 Docker环境变量

创建Docker Compose环境变量文件：

```yaml
# docker-compose.yml
version: '3.8'

services:
  inventory-service:
    image: inventory-service:3.0.0
    container_name: inventory-service
    ports:
      - "8080:8080"
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_APPLICATION_NAME=inventory-service
      - JAVA_OPTS=-Xms2g -Xmx4g -XX:+UseG1GC
      - HERA_SERVER_URL=http://hera-server:8080
      - HERA_API_KEY=${HERA_API_KEY}
      - JAEGER_ENDPOINT=http://jaeger:14268/api/traces
      - JAEGER_AUTH_TOKEN=${JAEGER_AUTH_TOKEN}
      - OTEL_SAMPLING_RATIO=0.1
      - DB_HOST=mysql
      - DB_PORT=3306
      - DB_NAME=inventory_db
      - DB_USERNAME=inventory_user
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    volumes:
      - ./logs:/var/log/inventory-service
      - ./data:/data/inventory-service
    networks:
      - monitoring-network
    depends_on:
      - mysql
      - redis
      - kafka
      - prometheus
      - jaeger

  prometheus:
    image: prom/prometheus:v2.50.0
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/usr/share/prometheus/console_libraries'
      - '--web.console.templates=/usr/share/prometheus/consoles'
      - '--storage.tsdb.retention.time=200h'
      - '--web.enable-lifecycle'
    networks:
      - monitoring-network

  grafana:
    image: grafana/grafana:11.0.0
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_ADMIN_PASSWORD}
      - GF_INSTALL_PLUGINS=grafana-piechart-panel
    volumes:
      - grafana-data:/var/lib/grafana
      - grafana-logs:/var/log/grafana
    networks:
      - monitoring-network
    depends_on:
      - prometheus

  jaeger:
    image: jaegertracing/all-in-one:1.53
    container_name: jaeger
    ports:
      - "16686:16686"
      - "14268:14268"
      - "9411:9411"
      - "14269:14269"
    environment:
      - COLLECTOR_ZIPKIN_HOST_PORT=9411
      - SPAN_STORAGE_TYPE=elasticsearch
    networks:
      - monitoring-network

networks:
  monitoring-network:
    driver: bridge
```

## 5. 权限配置

### 5.1 文件系统权限

```bash
# 创建应用目录
sudo mkdir -p /opt/inventory-service
sudo mkdir -p /var/log/inventory-service
sudo mkdir -p /data/inventory-service

# 设置目录所有者
sudo chown -R appuser:appgroup /opt/inventory-service
sudo chown -R appuser:appgroup /var/log/inventory-service
sudo chown -R appuser:appgroup /data/inventory-service

# 设置目录权限
sudo chmod -R 755 /opt/inventory-service
sudo chmod -R 775 /var/log/inventory-service
sudo chmod -R 775 /data/inventory-service

# 设置日志目录权限
sudo chmod 777 /var/log/inventory-service
```

### 5.2 服务权限

```bash
# 创建应用用户
sudo useradd -m -s /bin/bash appuser
sudo usermod -aG appuser docker

# 设置sudo权限（如需要）
sudo visudo
# 添加以下行：
# appuser ALL=(ALL) NOPASSWD: /bin/systemctl restart inventory-service
# appuser ALL=(ALL) NOPASSWD: /bin/systemctl restart prometheus
# appuser ALL=(ALL) NOPASSWD: /bin/systemctl restart grafana
```

### 5.3 网络权限

```bash
# 配置SELinux（如启用）
sudo setsebool -P httpd_can_network_connect on
sudo setsebool -P httpd_can_sendmail on

# 配置AppArmor（如使用）
sudo aa-complain /usr/bin/java
sudo aa-logprof /usr/bin/java

# 验证网络访问
sudo -u appuser curl http://localhost:9090/metrics
sudo -u appuser curl http://localhost:3000
```

## 6. 验证检查

### 6.1 环境验证清单

- [ ] Java版本正确（JDK 21）
- [ ] Maven版本正确（3.9+）
- [ ] 所有必需端口已开放
- [ ] 防火墙规则已配置
- [ ] 环境变量已设置
- [ ] 文件权限已配置
- [ ] 网络连接正常
- [ ] DNS解析正常
- [ ] 时区配置正确

### 6.2 依赖验证

```bash
# 验证Java安装
java -version

# 验证Maven安装
mvn -version

# 验证网络连接
ping -c 3 8.8.8.8

# 验证DNS解析
nslookup hera-server.example.com

# 验证端口开放
netstat -tuln | grep -E ':(8080|9090|3000|16686|14268)'

# 验证防火墙状态
sudo firewall-cmd --state

# 验证SELinux状态
getenforce

# 验证磁盘空间
df -h

# 验证内存
free -h

# 验证CPU
lscpu
```

### 6.3 服务启动验证

```bash
# 启动Prometheus
sudo systemctl start prometheus
sudo systemctl enable prometheus
sudo systemctl status prometheus

# 启动Grafana
sudo systemctl start grafana
sudo systemctl enable grafana
sudo systemctl status grafana

# 启动Jaeger
sudo systemctl start jaeger
sudo systemctl enable jaeger
sudo systemctl status jaeger

# 验证服务健康状态
curl http://localhost:9090/-/healthy
curl http://localhost:3000/api/health
curl http://localhost:16686/api/health
```

## 7. 故障排查

### 7.1 常见问题

#### 7.1.1 Java版本问题
**问题**：Java版本不兼容

**解决方案**：
```bash
# 检查Java版本
java -version

# 更新Java版本（如需要）
sudo yum install -y java-21-openjdk-devel

# 设置正确的JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
```

#### 7.1.2 端口冲突
**问题**：端口已被占用

**解决方案**：
```bash
# 查找占用端口的进程
sudo lsof -i :8080
sudo lsof -i :9090

# 终止占用端口的进程
sudo kill -9 <PID>

# 或修改应用配置使用其他端口
export SERVER_PORT=8082
export MANAGEMENT_PORT=8083
```

#### 7.1.3 权限问题
**问题**：文件或目录权限不足

**解决方案**：
```bash
# 检查文件权限
ls -la /var/log/inventory-service

# 修改权限
sudo chmod 775 /var/log/inventory-service

# 修改所有者
sudo chown -R appuser:appgroup /var/log/inventory-service
```

#### 7.1.4 内存不足
**问题**：系统内存不足

**解决方案**：
```bash
# 检查内存使用
free -h

# 增加交换空间
sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

# 调整JVM参数
export JVM_XMS=1g
export JVM_XMX=2g
export JVM_NEW_RATIO=0.2
```

### 7.2 日志检查

```bash
# 查看应用日志
tail -f /var/log/inventory-service/application.log

# 查看Prometheus日志
tail -f /var/log/prometheus/prometheus.log

# 查看Grafana日志
tail -f /var/log/grafana/grafana.log

# 查看Jaeger日志
tail -f /var/log/jaeger/all-in-one.log

# 查看系统日志
sudo journalctl -u prometheus -f
sudo journalctl -u grafana -f
sudo journalctl -u jaeger -f
```

## 8. 安全配置

### 8.1 防火墙配置

```bash
# 配置防火墙规则
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --permanent --add-port=9092/tcp
sudo firewall-cmd --permanent --add-port=9094/tcp
sudo firewall-cmd --reload

# 验证防火墙规则
sudo firewall-cmd --list-all

# 配置IP白名单（如需要）
sudo firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="192.168.1.0/24" accept'
sudo firewall-cmd --reload
```

### 8.2 SELinux配置

```bash
# 配置SELinux上下文
sudo seman fcontext -a -t httpd_sys_content_t "/var/log/inventory-service(/.*)?"
sudo restorecon -R -v /var/log/inventory-service

# 配置SELinux布尔值
sudo setsebool -P httpd_can_network_connect on
sudo setsebool -P httpd_can_sendmail on

# 验证SELinux状态
getenforce
sestatus
```

### 8.3 SSH配置

```bash
# 配置SSH访问
sudo vim /etc/ssh/sshd_config

# 添加以下配置：
Port 22
PermitRootLogin no
PasswordAuthentication yes
PubkeyAuthentication yes
MaxAuthTries 3
ClientAliveInterval 300
ClientAliveCountMax 2

# 重启SSH服务
sudo systemctl restart sshd
```

## 9. 附录

### 9.1 配置参数参考
| 参数 | 默认值 | 说明 | 可选范围 |
|------|---------|------|---------|
| JAVA_HOME | /usr/lib/jvm/java-21-openjdk | Java安装目录 | - |
| SPRING_PROFILES_ACTIVE | prod | Spring Profile | dev,test,prod |
| SERVER_PORT | 8080 | 应用端口 | 1024-65535 |
| MANAGEMENT_PORT | 8081 | 管理端口 | 1024-65535 |
| HERA_SERVER_URL | http://localhost:8080 | Hera服务器URL | - |
| HERA_API_KEY | - | Hera API密钥 | - |
| JAEGER_ENDPOINT | http://localhost:14268/api/traces | Jaeger端点 | - |
| OTEL_SAMPLING_RATIO | 0.1 | 采样率 | 0.0-1.0 |
| DB_HOST | localhost | 数据库主机 | - |
| DB_PORT | 3306 | 数据库端口 | - |
| REDIS_HOST | localhost | Redis主机 | - |
| REDIS_PORT | 6379 | Redis端口 | - |
| KAFKA_BOOTSTRAP_SERVERS | localhost:9092 | Kafka地址 | - |

### 9.2 支持和资源
- Java官方文档：https://docs.oracle.com/en/java/
- Spring Boot文档：https://spring.io/projects/spring-boot/docs/current/reference/html/
- Prometheus文档：https://prometheus.io/docs/
- Grafana文档：https://grafana.com/docs/
- Jaeger文档：https://www.jaegertracing.io/docs/
- Docker文档：https://docs.docker.com/
- Linux系统管理：https://access.redhat.com/documentation/

### 9.3 更新日志
| 版本 | 日期 | 变更内容 | 作者 |
|------|------|---------|------|
| 1.0.0 | 2025-01-18 | 初始版本 | DevOps Team |
