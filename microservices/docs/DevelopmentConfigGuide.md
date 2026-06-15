# 开发环境配置指南

## 版本
- 版本：1.0.0
- 创建日期：2025-01-18
- 最后更新：2025-01-18

## 1. 文档概述

### 1.1 目的
本指南提供了库存管理系统开发环境的完整配置步骤，包括本地开发配置、调试配置、日志配置、测试配置和IDE配置。通过本指南，开发人员可以快速搭建和配置开发环境，提高开发效率。

### 1.2 适用范围
- 本地开发环境搭建
- IDE配置和优化
- 调试工具配置
- 日志配置和分析
- 测试环境配置
- Mock数据配置
- 性能优化配置

### 1.3 目标读者
- 后端开发工程师
- 前端开发工程师
- 全栈开发工程师
- DevOps工程师
- 测试工程师

### 1.4 前置条件
- 已安装Java 21 JDK
- 已安装Maven 3.9+
- 已安装IntelliJ IDEA或VS Code
- 已安装Git
- 已安装Docker和Docker Compose
- 已安装MySQL或PostgreSQL
- 已安装Redis
- 具备基本的Linux系统管理能力

## 2. 本地开发环境搭建

### 2.1 IDE配置

#### 2.1.1 IntelliJ IDEA配置

**安装插件**：
- Lombok Plugin（代码生成）
- Spring Boot Plugin（Spring Boot支持）
- Maven Helper（Maven集成）
- GitToolBox（Git集成）
- Docker Plugin（Docker支持）
- Prometheus Plugin（监控数据查看）

**配置设置**：

```properties
# idea.properties
# 编译器配置
compiler.make.project.auto=false
compiler.auto.compile.trigger.save=on-file-save
compiler.automake.allow.when.app.running=true

# 构建配置
maven.importing.automatically=true
maven.executable.options=-Dmaven.multiModuleProjectDirectory=/opt/inventory-management

# 代码风格
editor.format.on.save=true
editor.format.trim.lines=true
editor.remove.trailing.spaces=true
editor.optimize.imports=true

# 文件编码
file.encoding.project.default=UTF-8
file.encoding.properties.default=UTF-8

# 行号显示
editor.line.separator.show=enabled
editor.line.separator.show.startup=true
```

**运行配置**：

```properties
# Run/Debug Configurations
# JVM参数
VM Options: -Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heapdump.hprof

# 环境变量
Environment:
  JAVA_HOME=/usr/lib/jvm/java-21-openjdk
  SPRING_PROFILES_ACTIVE=dev
  SPRING_APPLICATION_NAME=inventory-service
  SERVER_PORT=8080
  MANAGEMENT_PORT=8081
  DB_HOST=localhost
  DB_PORT=3306
  DB_NAME=inventory_dev
  DB_USERNAME=inventory_user
  DB_PASSWORD=dev_password
  REDIS_HOST=localhost
  REDIS_PORT=6379
  REDIS_PASSWORD=dev_redis_password
  KAFKA_BOOTSTRAP_SERVERS=localhost:9092
  KAFKA_CONSUMER_GROUP=dev-consumer-group
  HERA_SERVER_URL=http://localhost:8080
  HERA_API_KEY=dev-api-key
  JAEGER_ENDPOINT=http://localhost:14268/api/traces
  PROMETHEUS_HOST=localhost
  PROMETHEUS_PORT=9090
  GRAFANA_HOST=localhost
  GRAFANA_PORT=3000
  LOG_LEVEL=DEBUG
  LOG_PATH=/var/log/inventory-service
  LOG_MAX_SIZE=100MB
  LOG_MAX_HISTORY=30
  JVM_XMS=2g
  JVM_XMX=4g
  JVM_NEW_RATIO=0.3
  JVM_METASPACE_SIZE=256m
  MONITORING_ENABLED=true
  METRICS_EXPORT_INTERVAL=15
  TRACING_SAMPLING_RATE=0.5
  ALERTING_ENABLED=true
  ALERTING_AGGREGATION_WINDOW=5
  ALERTING_DEDUPLICATION_ENABLED=true
  ALERTING_SUPPRESSION_ENABLED=true
```

#### 2.1.2 VS Code配置

**安装扩展**：
- Spring Boot Extension Pack
- Spring Initializr Java Support
- Maven for Java
- GitLens - Git supercharged
- Docker
- Test Runner for Java
- SonarLint
- Prometheus Monitoring

**配置文件**：

```json
// .vscode/settings.json
{
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-21",
      "path": "/usr/lib/jvm/java-21-openjdk",
      "default": true
    }
  ],
  
  "java.debug.settings": {
    "enableLaunchDebugging": "true",
    "allowBreakpoints": "true",
    "console": "integratedTerminal",
    "vmArgs": "-Xms2g -Xmx4g -XX:+UseG1GC"
  },
  
  "maven.terminal.useJavaHome": true,
  "maven.executable.preferMavenWrapper": true,
  "maven.terminal.options": "-Dmaven.multiModuleProjectDirectory=/opt/inventory-management",
  
  "spring-boot.dashboard.java.link.enabled": true,
  "spring-boot.dashboard.java.link.linking.enabled": true,
  
  "docker.docker.defaultPlatform": "linux",
  "docker.host": "unix:///var/run/docker.sock",
  
  "files.associations": {
    "*.java": "default",
    "*.xml": "default",
    "*.yml": "default",
    "*.yaml": "default",
    "*.properties": "default"
  },
  
  "files.exclude": {
    "**/.git": true,
    "**/.svn": true,
    "**/.hg": true,
    "**/CVS": true,
    "**/.DS_Store": true,
    "**/node_modules": true,
    "**/target": true,
    "**/.class": true
    "**/.log": true
  },
  
  "editor.formatOnSave": true,
  "editor.trimAutoWhitespace": true,
  "editor.insertFinalNewline": true,
  "editor.tabSize": 4,
  "editor.wordWrap": "on",
  
  "files.watcherExclude": {
    "**/.git/objects/**": true,
    "**/.git/subtree-cache/**": true,
    "**/node_modules/**": true,
    "**/target/**": true,
    "**/.m2/**": true
  },
  
  "search.exclude": {
    "**/node_modules": true,
    "**/bower_components": true,
    "**/target": true,
    "**/.m2/repository": true
  },
  
  "terminal.integrated.defaultLocation": "integrated",
  "terminal.integrated.fontSize": 14,
  "terminal.integrated.shell.linux": "bash",
  
  "extensions.ignoreRecommendations": true,
  "extensions.showRecommendationsOnlyOnIgnored": false,
  
  "telemetry.enableTelemetry": false,
  "telemetry.telemetryLevel": "off"
}
```

**launch.json配置**：

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "InventoryService",
      "request": "launch",
      "mainClass": "com.inventory.InventoryServiceApplication",
      "projectName": "inventory-management",
      "args": [
        "--spring.profiles.active=dev",
        "--server.port=8080"
      ],
      "vmArgs": [
        "-Xms2g",
        "-Xmx4g",
        "-XX:+UseG1GC",
        "-XX:MaxGCPauseMillis=200",
        "-XX:+HeapDumpOnOutOfMemoryError",
        "-XX:HeapDumpPath=/tmp/heapdump.hprof"
      ],
      "env": {
        "SPRING_PROFILES_ACTIVE": "dev",
        "SPRING_APPLICATION_NAME": "inventory-service",
        "SERVER_PORT": "8080",
        "MANAGEMENT_PORT": "8081",
        "DB_HOST": "localhost",
        "DB_PORT": "3306",
        "DB_NAME": "inventory_dev",
        "DB_USERNAME": "inventory_user",
        "DB_PASSWORD": "dev_password",
        "REDIS_HOST": "localhost",
        "REDIS_PORT": "6379",
        "REDIS_PASSWORD": "dev_redis_password",
        "KAFKA_BOOTSTRAP_SERVERS": "localhost:9092",
        "KAFKA_CONSUMER_GROUP": "dev-consumer-group",
        "HERA_SERVER_URL": "http://localhost:8080",
        "HERA_API_KEY": "dev-api-key",
        "JAEGER_ENDPOINT": "http://localhost:14268/api/traces",
        "PROMETHEUS_HOST": "localhost",
        "PROMETHEUS_PORT": "9090",
        "GRAFANA_HOST": "localhost",
        "GRAFANA_PORT": "3000",
        "LOG_LEVEL": "DEBUG",
        "LOG_PATH": "/var/log/inventory-service",
        "LOG_MAX_SIZE": "100MB",
        "LOG_MAX_HISTORY": "30",
        "JVM_XMS": "2g",
        "JVM_XMX": "4g",
        "JVM_NEW_RATIO": "0.3",
        "JVM_METASPACE_SIZE": "256m",
        "MONITORING_ENABLED": "true",
        "METRICS_EXPORT_INTERVAL": "15",
        "TRACING_SAMPLING_RATE": "0.5",
        "ALERTING_ENABLED": "true",
        "ALERTING_AGGREGATION_WINDOW": "5",
        "ALERTING_DEDUPLICATION_ENABLED": "true",
        "ALERTING_SUPPRESSION_ENABLED": "true"
      },
      "console": "integratedTerminal"
    }
  ]
}
```

### 2.2 调试配置

#### 2.2.1 远程调试配置

```properties
# application-dev.yml
spring:
  devtools:
    restart:
      enabled: true
    livereload:
      enabled: true
    remote:
      debug:
        enabled: true
        secret: my-secret
        port: 5005
```

#### 2.2.2 JVM调试参数

```properties
# JVM参数
-Xdebug
-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=*:5005
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+PrintGCApplicationStoppedTime
-XX:+PrintStringDeduplication
-XX:+PrintCompilation
-verbose:class
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/tmp/heapdump.hprof
```

#### 2.2.3 日志级别配置

```yaml
# application-dev.yml
logging:
  level:
    root: INFO
    com.inventory: DEBUG
    com.inventory.monitoring: DEBUG
    com.inventory.tracing: DEBUG
    com.inventory.alerting: DEBUG
    com.inventory.transport: DEBUG
    org.springframework: DEBUG
    org.springframework.web: DEBUG
    org.springframework.jdbc: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /var/log/inventory-service/application-dev.log
    max-size: 100MB
    max-history: 30
    total-size-cap: 10GB
```

## 3. 日志配置

### 3.1 Logback配置

创建Logback配置文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProperty scope="context" name="springAppName" source="spring.application.name"/>
    <springProperty scope="context" name="activeProfile" source="spring.profiles.active"/>
    
    <property name="LOG_PATH" value="/var/log/inventory-service"/>
    <property name="LOG_FILE" value="${LOG_PATH}/application-${activeProfile}.log"/>
    <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"/>
    <property name="LOG_CHARSET" value="UTF-8"/>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
            <charset>${LOG_CHARSET}</charset>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_FILE}</file>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
            <charset>${LOG_CHARSET}</charset>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/application-${activeProfile}-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>10GB</totalSizeCap>
        </rollingPolicy>
    </appender>
    
    <appender name="ASYNC_FILE" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="FILE"/>
        <queueSize>512</queueSize>
        <discardingThreshold>0</discardingThreshold>
    </appender>
    
    <logger name="com.inventory" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_FILE"/>
    </logger>
    
    <logger name="com.inventory.monitoring" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_FILE"/>
    </logger>
    
    <logger name="com.inventory.tracing" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_FILE"/>
    </logger>
    
    <logger name="com.inventory.alerting" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_FILE"/>
    </logger>
    
    <logger name="com.inventory.transport" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_FILE"/>
    </logger>
    
    <logger name="org.springframework" level="INFO" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_FILE"/>
    </logger>
    
    <logger name="org.springframework.web" level="INFO" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_FILE"/>
    </logger>
    
    <logger name="org.springframework.jdbc" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_FILE"/>
    </logger>
    
    <logger name="org.hibernate.SQL" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_FILE"/>
    </logger>
    
    <logger name="org.hibernate.type.descriptor.sql.BasicBinder" level="TRACE" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_FILE"/>
    </logger>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_FILE"/>
    </root>
</configuration>
```

### 3.2 结构化日志

使用JSON格式日志：

```xml
<appender name="JSON_FILE" class="ch.qos.logback.core.FileAppender">
    <file>${LOG_PATH}/application-${activeProfile}.json</file>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <providers>
            <timestamp>
                <timeZone>Asia/Shanghai</timeZone>
                <pattern>yyyy-MM-dd'T'HH:mm:ss.SSS</pattern>
            </timestamp>
            <pattern>
                <pattern>
                    {
                      "timestamp": "%d{yyyy-MM-dd'T'HH:mm:ss.SSS}",
                      "level": "%level",
                      "logger": "%logger",
                      "thread": "%thread",
                      "message": "%message",
                      "exception": "%ex{Full}"
                    }
                </pattern>
            </pattern>
        </providers>
    </encoder>
</appender>
```

## 4. 测试配置

### 4.1 单元测试配置

```xml
<!-- pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 4.2 集成测试配置

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop
      show-sql: true
  
  kafka:
    bootstrap-servers: localhost:9095
    consumer:
      group-id: test-consumer-group
      auto-offset-reset: earliest
  
  redis:
    host: localhost
    port: 6380
    database: 0
```

### 4.3 Mock配置

创建Mock配置类：

```java
package com.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
@ConfigurationProperties(prefix = "mock")
public class MockConfig {
    
    private boolean enabled = true;
    private boolean enableRandomData = false;
    private boolean enableSlowResponses = false;
    private int slowResponseDelayMs = 1000;
    private boolean enableErrors = false;
    private double errorRate = 0.1;
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isEnableRandomData() {
        return enableRandomData;
    }
    
    public void setEnableRandomData(boolean enableRandomData) {
        this.enableRandomData = enableRandomData;
    }
    
    public int getSlowResponseDelayMs() {
        return slowResponseDelayMs;
    }
    
    public void setSlowResponseDelayMs(int slowResponseDelayMs) {
        this.slowResponseDelayMs = slowResponseDelayMs;
    }
    
    public boolean isEnableErrors() {
        return enableErrors;
    }
    
    public void setEnableErrors(boolean enableErrors) {
        this.enableErrors = enableErrors;
    }
    
    public double getErrorRate() {
        return errorRate;
    }
    
    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }
}
```

Mock数据生成器：

```java
package com.inventory.test.util;

import com.inventory.config.MockConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

@Component
@Profile("test")
public class MockDataGenerator {
    
    private static final Random random = new Random();
    
    @Autowired
    private MockConfig mockConfig;
    
    public String generateOrderId() {
        if (mockConfig.isEnableRandomData()) {
            return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } else {
            return "TEST-ORD-001";
        }
    }
    
    public String generateProductId() {
        if (mockConfig.isEnableRandomData()) {
            return "PROD-" + String.format("%04d", random.nextInt(10000));
        } else {
            return "TEST-PROD-001";
        }
    }
    
    public String generateWarehouseId() {
        if (mockConfig.isEnableRandomData()) {
            return "WH-" + String.format("%04d", random.nextInt(100));
        } else {
            return "TEST-WH-001";
        }
    }
    
    public String generateUserId() {
        if (mockConfig.isEnableRandomData()) {
            return "USER-" + String.format("%06d", random.nextInt(1000000));
        } else {
            return "TEST-USER-001";
        }
    }
    
    public void simulateSlowResponse() throws InterruptedException {
        if (mockConfig.isEnableSlowResponses()) {
            Thread.sleep(mockConfig.getSlowResponseDelayMs());
        }
    }
    
    public void simulateError() {
        if (mockConfig.isEnableErrors()) {
            return random.nextDouble() < mockConfig.getErrorRate();
        }
    }
}
```

## 5. Docker开发环境

### 5.1 Docker Compose配置

创建Docker Compose文件：

```yaml
version: '3.8'

services:
  # MySQL数据库
  mysql:
    image: mysql:8.0
    container_name: inventory-mysql-dev
    environment:
      MYSQL_ROOT_PASSWORD: dev_root_password
      MYSQL_DATABASE: inventory_dev
      MYSQL_USER: inventory_user
      MYSQL_PASSWORD: dev_password
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./docker/mysql/conf.d:/etc/mysql/conf.d
    networks:
      - dev-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 3
  
  # Redis缓存
  redis:
    image: redis:7.0
    container_name: inventory-redis-dev
    command: redis-server --appendonly yes
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - dev-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 3
  
  # Kafka消息队列
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: inventory-kafka-dev
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    ports:
      - "9092:9092"
    volumes:
      - kafka-data:/var/lib/kafka/data
      - kafka-secrets:/etc/kafka/secrets
    networks:
      - dev-network
    depends_on:
      - zookeeper
  
  # Zookeeper
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: inventory-zookeeper-dev
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"
    volumes:
      - zookeeper-data:/var/lib/zookeeper/data
      - zookeeper-logs:/var/lib/zookeeper/log
    networks:
      - dev-network
  
  # Prometheus监控
  prometheus:
    image: prom/prometheus:v2.50.0
    container_name: inventory-prometheus-dev
    ports:
      - "9090:9090"
    volumes:
      - ./docker/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
    networks:
      - dev-network
  
  # Grafana可视化
  grafana:
    image: grafana/grafana:11.0.0
    container_name: inventory-grafana-dev
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
      GF_INSTALL_PLUGINS: grafana-piechart-panel
    volumes:
      - grafana-data:/var/lib/grafana
      - grafana-logs:/var/log/grafana
    networks:
      - dev-network
    depends_on:
      - prometheus
  
  # Jaeger追踪
  jaeger:
    image: jaegertracing/all-in-one:1.53
    container_name: inventory-jaeger-dev
    ports:
      - "16686:16686"
      - "14268:14268"
      - "9411:9411"
    environment:
      COLLECTOR_ZIPKIN_HOST_PORT: 9411
      SPAN_STORAGE_TYPE: elasticsearch
    networks:
      - dev-network
  
  # 应用服务
  inventory-service:
    build: .
    dockerfile: Dockerfile.dev
    container_name: inventory-service-dev
    ports:
      - "8080:8080"
      - "8081:8081"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_APPLICATION_NAME: inventory-service
      SERVER_PORT: 8080
      MANAGEMENT_PORT: 8081
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: inventory_dev
      DB_USERNAME: inventory_user
      DB_PASSWORD: dev_password
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_PASSWORD: dev_redis_password
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      KAFKA_CONSUMER_GROUP: dev-consumer-group
      HERA_SERVER_URL: http://localhost:8080
      HERA_API_KEY: dev-api-key
      JAEGER_ENDPOINT: http://jaeger:14268/api/traces
      PROMETHEUS_HOST: prometheus
      PROMETHEUS_PORT: 9090
      GRAFANA_HOST: grafana
      GRAFANA_PORT: 3000
      LOG_LEVEL: DEBUG
      LOG_PATH: /var/log/inventory-service
      LOG_MAX_SIZE: 100MB
      LOG_MAX_HISTORY: 30
      JVM_XMS: 2g
      JVM_XMX: 4g
      JVM_NEW_RATIO: 0.3
      JVM_METASPACE_SIZE: 256m
      MONITORING_ENABLED: true
      METRICS_EXPORT_INTERVAL: 15
      TRACING_SAMPLING_RATE: 0.5
      ALERTING_ENABLED: true
      ALERTING_AGGREGATION_WINDOW: 5
      ALERTING_DEDUPLICATION_ENABLED: true
      ALERTING_SUPPRESSION_ENABLED: true
    volumes:
      - ./logs:/var/log/inventory-service
      - ./data:/data/inventory-service
    networks:
      - dev-network
    depends_on:
      - mysql
      - redis
      - kafka
      - prometheus
      - jaeger
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

networks:
  dev-network:
    driver: bridge

volumes:
  mysql-data:
  redis-data:
  kafka-data:
  kafka-secrets:
  zookeeper-data:
  zookeeper-logs:
  prometheus-data:
  grafana-data:
  grafana-logs:
```

### 5.2 Dockerfile配置

创建开发环境Dockerfile：

```dockerfile
FROM openjdk:21-jdk-slim

LABEL maintainer="devops@company.com"
LABEL description="Inventory Management System - Development Environment"

# 设置工作目录
WORKDIR /app

# 复制Maven配置
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# 下载Maven依赖
RUN ./mvnw dependency:go-offline -B

# 复制应用代码
COPY target/*.jar app.jar

# 创建日志目录
RUN mkdir -p /var/log/inventory-service

# 设置环境变量
ENV SPRING_PROFILES_ACTIVE=dev
ENV SPRING_APPLICATION_NAME=inventory-service
ENV SERVER_PORT=8080
ENV MANAGEMENT_PORT=8081
ENV JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 暴露端口
EXPOSE 8080 8081

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 CMD curl -f http://localhost:8081/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 6. 性能优化配置

### 6.1 JVM性能调优

```yaml
# application-dev.yml
spring:
  jmx:
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: jmx,health,info,metrics,prometheus
  endpoint:
    jmx:
      unique-names: true
```

### 6.2 数据库连接池优化

```yaml
# application-dev.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      maximum-idle: 10
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-timeout: 30000
      connection-test-query: SELECT 1
      leak-detection-threshold: 0
      pool-name: InventoryHikariCP
```

### 6.3 Redis缓存优化

```yaml
# application-dev.yml
spring:
  redis:
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: -1ms
        min-idle: 0ms
      time-between-eviction-runs: 60000
    cache:
      type: redis
      redis:
        time-to-live: 600000
        cache-null-values: false
        use-key-prefix: true
        key-prefix: "inventory:"
```

## 7. 开发工作流

### 7.1 启动开发环境

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f inventory-service
docker-compose logs -f mysql
docker-compose logs -f redis
docker-compose logs -f kafka
```

### 7.2 常用开发命令

```bash
# 编译项目
./mvnw clean compile

# 运行测试
./mvnw test

# 打包项目
./mvnw clean package -DskipTests

# 运行应用
java -jar target/inventory-service-3.0.0.jar

# 查看日志
tail -f /var/log/inventory-service/application-dev.log

# 进入容器
docker exec -it inventory-service-dev bash

# 重启服务
docker-compose restart inventory-service

# 停止服务
docker-compose stop inventory-service

# 清理容器
docker-compose down -v

# 清理数据
docker-compose down -v --remove-orphans
```

## 8. 故障排查

### 8.1 常见问题

#### 8.1.1 端口冲突

**问题**：8080端口已被占用

**解决方案**：
```bash
# 查找占用端口的进程
sudo lsof -i :8080

# 终止占用端口的进程
sudo kill -9 <PID>

# 或修改应用配置使用其他端口
export SERVER_PORT=8082
```

#### 8.1.2 数据库连接失败

**问题**：无法连接到MySQL

**解决方案**：
```bash
# 检查MySQL容器状态
docker ps | grep mysql

# 查看MySQL日志
docker logs inventory-mysql-dev

# 测试MySQL连接
docker exec -it inventory-mysql-dev mysql -u inventory_user -pdev_password -e "SELECT 1"

# 检查网络连接
docker exec -it inventory-service-dev ping mysql

# 重启MySQL容器
docker restart inventory-mysql-dev
```

#### 8.1.3 内存溢出

**问题**：OutOfMemoryError

**解决方案**：
```bash
# 查看JVM内存使用
docker stats inventory-service-dev --no-stream

# 增加JVM堆内存
export JVM_XMX=6g

# 启用GC日志
export JVM_OPTS="$JVM_OPTS -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"

# 分析堆转储文件
jhat /tmp/heapdump.hprof
```

## 9. 最佳实践

### 9.1 开发规范

- 使用统一的代码风格
- 遵循RESTful API设计规范
- 编写单元测试
- 使用Git进行版本控制
- 定期进行代码审查
- 使用CI/CD自动化构建

### 9.2 安全考虑

- 不在代码中硬编码敏感信息
- 使用环境变量管理配置
- 定期更新依赖版本
- 启用HTTPS（生产环境）
- 实施访问控制

### 9.3 性能考虑

- 使用连接池优化数据库访问
- 使用缓存减少数据库查询
- 优化JVM参数
- 使用异步处理提高响应速度
- 定期进行性能测试

## 10. 附录

### 10.1 配置参数参考

| 参数 | 默认值 | 说明 | 可选范围 |
|------|---------|------|---------|
| SPRING_PROFILES_ACTIVE | dev | Spring Profile | dev,test,prod |
| SERVER_PORT | 8080 | 应用端口 | 1024-65535 |
| MANAGEMENT_PORT | 8081 | 管理端口 | 1024-65535 |
| DB_HOST | localhost | 数据库主机 | - |
| DB_PORT | 3306 | 数据库端口 | 3306 |
| DB_NAME | inventory_dev | 数据库名称 | - |
| REDIS_HOST | localhost | Redis主机 | - |
| REDIS_PORT | 6379 | Redis端口 | 6379 |
| KAFKA_BOOTSTRAP_SERVERS | localhost:9092 | Kafka地址 | - |
| LOG_LEVEL | DEBUG | 日志级别 | TRACE,DEBUG,INFO,WARN,ERROR |
| JVM_XMS | 2g | JVM最小堆内存 | 512m-8g |
| JVM_XMX | 4g | JVM最大堆内存 | 2g-16g |
| JVM_NEW_RATIO | 0.3 | 新生代比例 | 0.1-0.9 |
| MONITORING_ENABLED | true | 监控启用 | true,false |

### 10.2 支持和资源

- Spring Boot官方文档：https://spring.io/projects/spring-boot/docs/current/reference/html/
- Docker官方文档：https://docs.docker.com/
- Docker Compose文档：https://docs.docker.com/compose/
- Java 21文档：https://docs.oracle.com/en/java/javase/21/
- Logback文档：https://logback.qos.ch/manual/
- IntelliJ IDEA文档：https://www.jetbrains.com/idea/

### 10.3 更新日志

| 版本 | 日期 | 变更内容 | 作者 |
|------|------|---------|------|
| 1.0.0 | 2025-01-18 | 初始版本 | DevOps Team |
