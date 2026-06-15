# 测试环境配置指南

## 版本
- 版本：1.0.0
- 创建日期：2025-01-18
- 最后更新：2025-01-18

## 1. 文档概述

### 1.1 目的
本指南提供了库存管理系统测试环境的完整配置步骤，包括测试环境搭建、Mock数据配置、测试数据库配置、CI/CD集成和自动化测试。通过本指南，测试人员可以快速搭建和配置测试环境，提高测试效率和质量。

### 1.2 适用范围
- 测试环境搭建
- Mock数据配置
- 测试数据库配置
- CI/CD集成
- 自动化测试
- 性能测试
- 集成测试

### 1.3 目标读者
- 测试工程师
- QA工程师
- DevOps工程师
- 开发人员

### 1.4 前置条件
- 已安装Java 21 JDK
- 已安装Maven 3.9+
- 已配置Maven仓库
- 已安装Docker和Docker Compose
- 已安装MySQL或PostgreSQL
- 已安装Redis
- 已安装测试框架（JUnit 5, Mockito, AssertJ）
- 具备基本的Linux系统管理能力

## 2. 测试环境搭建

### 2.1 测试数据库配置

#### 2.1.1 H2内存数据库配置

**配置文件**：

```yaml
# application-test.yml
spring:
  profiles:
    active: test
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  h2:
    console:
      enabled: true
    settings:
        web-allow-others: true
```

**H2控制台访问**：
```bash
# 启动应用时访问H2控制台
java -jar target/inventory-service-3.0.0.jar --spring.profiles.active=test --spring.h2.console.enabled=true

# 浏览器访问
http://localhost:8080/h2-console
```

#### 2.1.2 PostgreSQL测试数据库

**Docker Compose配置**：

```yaml
version: '3.8'

services:
  # PostgreSQL数据库
  postgresql-test:
    image: postgres:15
    container_name: inventory-postgres-test
    environment:
      POSTGRES_DB: inventory_test
      POSTGRES_USER: inventory_user
      POSTGRES_PASSWORD: test_password
      POSTGRES_PASSWORD_FILE: /run/secrets/postgres_password
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - postgres-secrets:/run/secrets:postgresql
    networks:
      - test-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U inventory_user -d inventory_test"]
      interval: 10s
      timeout: 5s
      retries: 5
  
  # 应用服务
  inventory-service-test:
    build: .
    dockerfile: Dockerfile.test
    container_name: inventory-service-test
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: test
      SPRING_APPLICATION_NAME: inventory-service
      DB_HOST: postgresql-test
      DB_PORT: 5432
      DB_NAME: inventory_test
      DB_USERNAME: inventory_user
      DB_PASSWORD: test_password
      REDIS_HOST: redis-test
      REDIS_PORT: 6379
      REDIS_PASSWORD: test_redis_password
      KAFKA_BOOTSTRAP_SERVERS: kafka-test:9092
      KAFKA_CONSUMER_GROUP: test-consumer-group
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
    depends_on:
      - postgresql-test
      - redis-test
      - kafka-test
    networks:
      - test-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

#### 2.1.3 MySQL测试数据库

**Docker Compose配置**：

```yaml
version: '3.8'

services:
  # MySQL数据库
  mysql-test:
    image: mysql:8.0
    container_name: inventory-mysql-test
    environment:
      MYSQL_ROOT_PASSWORD: test_root_password
      MYSQL_DATABASE: inventory_test
      MYSQL_USER: inventory_user
      MYSQL_PASSWORD: test_password
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - mysql-secrets:/run/secrets/mysql
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci --default-authentication-plugin=mysql_native_password
    networks:
      - test-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "inventory_user", "-ptest_password"]
      interval: 10s
      timeout: 5s
      retries: 5
  
  # 应用服务
  inventory-service-test:
    build: .
    dockerfile: Dockerfile.test
    container_name: inventory-service-test
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: test
      SPRING_APPLICATION_NAME: inventory-service
      DB_HOST: mysql-test
      DB_PORT: 3306
      DB_NAME: inventory_test
      DB_USERNAME: inventory_user
      DB_PASSWORD: test_password
      REDIS_HOST: redis-test
      REDIS_PORT: 6379
      REDIS_PASSWORD: test_redis_password
      KAFKA_BOOTSTRAP_SERVERS: kafka-test:9092
      KAFKA_CONSUMER_GROUP: test-consumer-group
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
    depends_on:
      - mysql-test
      - redis-test
      - kafka-test
    networks:
      - test-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

### 2.2 Mock数据配置

#### 2.2.1 Mock配置类

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
    private boolean enableRandomData = true;
    private boolean enableSlowResponses = false;
    private int slowResponseDelayMs = 500;
    private boolean enableErrors = false;
    private double errorRate = 0.05;
    private int totalOrders = 100;
    private int totalProducts = 1000;
    private int totalWarehouses = 10;
    private int totalUsers = 50;
    
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
    
    public int getTotalOrders() {
        return totalOrders;
    }
    
    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }
    
    public int getTotalProducts() {
        return totalProducts;
    }
    
    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }
    
    public int getTotalWarehouses() {
        return totalWarehouses;
    }
    
    public void setTotalWarehouses(int totalWarehouses) {
        this.totalWarehouses = totalWarehouses;
    }
    
    public int getTotalUsers() {
        return totalUsers;
    }
    
    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }
}
```

#### 2.2.2 Mock数据生成器

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
            return "TEST-ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } else {
            return "TEST-ORD-001";
        }
    }
    
    public String generateProductId() {
        if (mockConfig.isEnableRandomData()) {
            return "TEST-PROD-" + String.format("%04d", random.nextInt(10000));
        } else {
            return "TEST-PROD-001";
        }
    }
    
    public String generateWarehouseId() {
        if (mockConfig.isEnableRandomData()) {
            return "TEST-WH-" + String.format("%04d", random.nextInt(100));
        } else {
            return "TEST-WH-001";
        }
    }
    
    public String generateUserId() {
        if (mockConfig.isEnableRandomData()) {
            return "TEST-USER-" + String.format("%06d", random.nextInt(100000));
        } else {
            return "TEST-USER-001";
        }
    }
    
    public String generateProductName() {
        String[] products = {"Laptop", "Desktop", "Monitor", "Keyboard", "Mouse", "Printer", "Tablet"};
        return products[random.nextInt(products.length)];
    }
    
    public String generateUserName() {
        String[] firstNames = {"张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴"};
        String[] lastNames = {"伟", "芳", "娜", "秀英", "敏", "静", "强", "磊"};
        return firstNames[random.nextInt(firstNames.length)] + lastNames[random.nextInt(lastNames.length)];
    }
    
    public void simulateSlowResponse() throws InterruptedException {
        if (mockConfig.isEnableSlowResponses()) {
            Thread.sleep(mockConfig.getSlowResponseDelayMs());
        }
    }
    
    public boolean shouldSimulateError() {
        if (mockConfig.isEnableErrors()) {
            return random.nextDouble() < mockConfig.getErrorRate();
        }
        return false;
    }
}
```

#### 2.2.3 Mock数据初始化

```java
package com.inventory.test.init;

import com.inventory.test.util.MockDataGenerator;
import com.inventory.repository.OrderRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.WarehouseRepository;
import com.inventory.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("test")
public class MockDataInitializer implements CommandLineRunner {
    
    @Autowired
    private MockDataGenerator mockDataGenerator;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private WarehouseRepository warehouseRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("Initializing mock test data...");
        
        for (int i = 0; i < mockConfig.getTotalOrders(); i++) {
            String orderId = mockDataGenerator.generateOrderId();
            Order order = new Order();
            order.setId(orderId);
            order.setOrderNumber("TEST-" + String.format("%06d", i + 1));
            order.setUserId(mockDataGenerator.generateUserId());
            order.setWarehouseId(mockDataGenerator.generateWarehouseId());
            order.setStatus("PENDING");
            order.setCreatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }
        
        for (int i = 0; i < mockConfig.getTotalProducts(); i++) {
            String productId = mockDataGenerator.generateProductId();
            Product product = new Product();
            product.setId(productId);
            product.setName("Test Product " + (i + 1));
            product.setPrice(new BigDecimal(String.format("%d.00", random.nextInt(1000) + 100)));
            product.setStock(random.nextInt(100));
            product.setWarehouseId(mockDataGenerator.generateWarehouseId());
            product.setCreatedAt(LocalDateTime.now());
            productRepository.save(product);
        }
        
        for (int i = 0; i < mockConfig.getTotalWarehouses(); i++) {
            String warehouseId = mockDataGenerator.generateWarehouseId();
            Warehouse warehouse = new Warehouse();
            warehouse.setId(warehouseId);
            warehouse.setName("Test Warehouse " + (i + 1));
            warehouse.setAddress("Test Address " + (i + 1));
            warehouse.setCapacity(10000);
            warehouse.setCreatedAt(LocalDateTime.now());
            warehouseRepository.save(warehouse);
        }
        
        for (int i = 0; i < mockConfig.getTotalUsers(); i++) {
            String userId = mockDataGenerator.generateUserId();
            User user = new User();
            user.setId(userId);
            user.setUsername(mockDataGenerator.generateUserName());
            user.setEmail("test" + userId + "@example.com");
            user.setPassword("password123");
            user.setRole("USER");
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
        
        System.out.println("Mock test data initialized successfully!");
        System.out.println("Created " + mockConfig.getTotalOrders() + " orders");
        System.out.println("Created " + mockConfig.getTotalProducts() + " products");
        System.out.println("Created " + mockConfig.getTotalWarehouses() + " warehouses");
        System.out.println("Created " + mockConfig.getTotalUsers() + " users");
    }
}
```

### 2.3 CI/CD集成

#### 2.3.1 GitHub Actions配置

创建GitHub Actions工作流：

```yaml
name: Inventory Management - CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'
      
      - name: Cache Maven packages
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
      
      - name: Build with Maven
        run: mvn clean package -DskipTests
      
      - name: Run unit tests
        run: mvn test
      
      - name: Run integration tests
        run: mvn verify -DskipUnitTests
      
      - name: Generate test report
        run: mvn surefire-report:report
      
      - name: Upload test results
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: target/surefire-reports
```

#### 2.3.2 Jenkins配置

创建Jenkinsfile：

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven 3.9.6'
        jdk 'JDK 21'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        
        stage('Unit Tests') {
            steps {
                sh 'mvn test'
            }
        }
        
        stage('Integration Tests') {
            steps {
                sh 'mvn verify -DskipUnitTests'
            }
        }
        
        stage('Deploy to Test') {
            steps {
                sh 'docker-compose -f docker-compose.test.yml up -d'
                sh 'sleep 30'
                sh 'curl -f http://localhost:8080/actuator/health'
            }
        }
    }
    
    post {
        always {
            junit 'target/surefire-reports/**/*.xml'
        }
    }
}
```

### 2.4 自动化测试

#### 2.4.1 Testcontainers配置

添加Testcontainers依赖：

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.19.8</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.19.8</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <version>1.19.8</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>redis</artifactId>
    <version>1.19.8</version>
    <scope>test</scope>
</dependency>
```

集成测试示例：

```java
package com.inventory.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.containers.PostgreSQLContainer;
import org.springframework.boot.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import com.inventory.repository.OrderRepository;
import com.inventory.model.Order;

@SpringBootTest
public class IntegrationTest {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Test
    public void testDatabaseIntegration() {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))) {
            postgres.withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");
            
            postgres.start();
            
            Wait.untilHttpPortIsReady(postgres, 5432);
            
            System.out.println("PostgreSQL container started: " + postgres.getHost());
            
            Thread.sleep(5000);
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to start PostgreSQL container", e);
        }
    }
    
    @Test
    public void testOrderCreation() {
        Order order = new Order();
        order.setId("TEST-001");
        order.setOrderNumber("TEST-001");
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        
        assertNotNull(savedOrder.getId());
        assertEquals("TEST-001", savedOrder.getOrderNumber());
        assertEquals("PENDING", savedOrder.getStatus());
    }
}
```

### 2.5 性能测试

#### 2.5.1 JMeter测试计划

创建JMeter测试计划：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0,utf-8">
  <hashTree>
    <TestPlan guiclass="TestPlan" testclass="Debug" enabled="true">
      <stringProp name="TestPlan.comments" value="Performance test for Inventory Management System"/>
      <boolProp name="TestPlan.functional_mode" value="false"/>
      
      <elementProp name="TestPlan.user_define_classpath" value="true"/>
      <elementProp name="TestPlan.user_define_variables" value="true"/>
      
      <hashTree>
        <TestPlan guiclass="ThreadGroup" testclass="Debug" enabled="true">
          <stringProp name="ThreadGroup.num_threads" value="10"/>
          <stringProp name="ThreadGroup.ramp_time" value="60"/>
          <stringProp name="ThreadGroup.duration" value="300"/>
          
          <hashTree>
            <ThreadGroup guiclass="Thread" testclass="Debug" enabled="true">
              <stringProp name="Thread.loop_count" value="10"/>
              <stringProp name="Thread.ramp_time" value="60"/>
              
              <hashTree>
                <ThreadGroup guiclass="HTTPSampler" testclass="Debug" enabled="true">
                  <stringProp name="HTTPSampler.domain" value="localhost"/>
                  <stringProp name="HTTPSampler.port" value="8080"/>
                  <stringProp name="HTTPSampler.protocol" value="http"/>
                  <stringProp name="HTTPSampler.contentEncoding" value="UTF-8"/>
                  <stringProp name="HTTPSampler.path" value="/api/orders"/>
                  
                  <elementProp name="HTTPSampler.response_timeout" value="60000"/>
                  
                  <hashTree>
                    <HTTPSampler guiclass="HeaderManager" testclass="Debug" enabled="true">
                      <collectionProp name="HeaderManager.headers">
                        <elementProp name="HeaderManager.header.name" value="Content-Type"/>
                        <elementProp name="HeaderManager.header.value" value="application/json"/>
                      </collectionProp>
                    </HTTPSampler>
                  </hashTree>
                </hashTree>
              </hashTree>
            </hashTree>
          </hashTree>
        </hashTree>
      </hashTree>
    </TestPlan>
</jmeterTestPlan>
```

#### 2.5.2 性能测试脚本

创建性能测试脚本：

```bash
#!/bin/bash

# 性能测试脚本
TEST_URL="http://localhost:8080"
TEST_DURATION=300
THREAD_COUNT=10
RAMP_UP_TIME=60

echo "Starting performance test..."

# 启动JMeter测试
jmeter -n -t PerformanceTest.jmx -l -e $TEST_URL \
  -JTEST_DURATION=$TEST_DURATION \
  -JTHREAD_COUNT=$THREAD_COUNT \
  -JRAMP_UP_TIME=$RAMP_UP_TIME

echo "Performance test completed!"

# 分析测试结果
jmeter -g PerformanceTest.jmx -o report.html -e $TEST_URL
```

## 3. 测试配置文件

### 3.1 application-test.yml

```yaml
spring:
  profiles:
    active: test
  application:
    name: Inventory Management System - Test Environment
  
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  h2:
    console:
      enabled: true
      settings:
        web-allow-others: true
  
  jpa:
    hibernate:
      ddl-auto: create-drop
      show-sql: true
      properties:
        hibernate:
          format_sql: true
          use_sql_comments: true
          dialect: org.hibernate.dialect.H2Dialect
  
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: test-consumer-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
  
  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 5000
  
  mock:
    enabled: true
    enable-random-data: true
    enable-slow-responses: false
    slow-response-delay-ms: 500
    enable-errors: false
    error-rate: 0.05
    total-orders: 100
    total-products: 1000
    total-warehouses: 10
    total-users: 50
  
  logging:
    level:
      root: INFO
      com.inventory: DEBUG
      com.inventory.monitoring: DEBUG
      com.inventory.tracing: DEBUG
      com.inventory.alerting: DEBUG
      com.inventory.transport: DEBUG
    pattern:
      console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
      file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
      file:
        name: /var/log/inventory-service/application-test.log
        max-size: 100MB
        max-history: 30
        total-size-cap: 10GB
  
  management:
    endpoints:
      web:
        exposure:
          include: health,info,metrics,prometheus
      endpoint:
        health:
          show-details: always
        metrics:
          enabled: true
        prometheus:
          enabled: true
  
  opentelemetry:
    traces:
      exporter:
        jaeger:
          enabled: false
      sampler:
        type: parentbased
        ratio: 1.0
      resource:
        service-name: inventory-service-test
        service-namespace: inventory-management-test
  
  hera:
    monitoring:
      enabled: false
```

### 3.2 logback-test.xml

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
    
    <logger name="com.inventory" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </logger>
    
    <logger name="com.inventory.test" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </logger>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

## 4. 测试执行

### 4.1 单元测试执行

```bash
# 执行单元测试
mvn clean test

# 执行测试覆盖率检查
mvn jacoco:prepare-agent test
mvn jacoco:report

# 查看测试报告
open target/site/jacoco/index.html
```

### 4.2 集成测试执行

```bash
# 启动测试环境
docker-compose -f docker-compose.test.yml up -d

# 等待服务启动
sleep 30

# 执行集成测试
mvn verify -DskipUnitTests

# 停止测试环境
docker-compose -f docker-compose.test.yml down
```

### 4.3 性能测试执行

```bash
# 执行性能测试
./run-performance-test.sh

# 分析性能测试结果
jmeter -g PerformanceTest.jmx -o report.html -e $TEST_URL

# 查看性能报告
open report.html
```

## 5. 故障排查

### 5.1 常见问题

#### 5.1.1 测试数据库连接失败

**问题**：无法连接到测试数据库

**解决方案**：
```bash
# 检查数据库容器状态
docker ps | grep postgres

# 查看数据库日志
docker logs inventory-postgres-test

# 测试数据库连接
docker exec -it inventory-postgres-test psql -U inventory_user -d inventory_test -c "SELECT 1"

# 检查网络连接
ping -c 3 postgresql-test

# 重启数据库容器
docker restart inventory-postgres-test
```

#### 5.1.2 Mock数据未生成

**问题**：Mock数据初始化失败

**解决方案**：
```bash
# 查看应用日志
tail -f /var/log/inventory-service/application-test.log | grep -i "mock"

# 检查Mock配置
cat application-test.yml | grep -A 10 "mock"

# 重新启动应用
docker-compose restart inventory-service-test
```

#### 5.1.3 测试超时

**问题**：测试执行超时

**解决方案**：
```bash
# 增加测试超时时间
export TEST_TIMEOUT=60000

# 检查测试配置
cat application-test.yml | grep -A 5 "timeout"

# 使用并行测试
mvn test -T 4
```

## 6. 最佳实践

### 6.1 测试数据管理

- 使用Testcontainers进行集成测试
- 在测试后清理Mock数据
- 使用事务回滚确保数据一致性
- 定期清理测试数据库

### 6.2 测试隔离

- 每个测试用例独立运行
- 使用@Before和@After清理测试状态
- 避免测试之间的数据污染

### 6.3 性能测试

- 在非高峰时段执行性能测试
- 使用合理的并发用户数
- 设置适当的测试持续时间
- 分析性能测试结果并优化

### 6.4 CI/CD最佳实践

- 使用缓存加速构建
- 并行化测试执行
- 定期清理构建产物
- 使用条件执行避免不必要的测试

## 7. 附录

### 7.1 配置参数参考

| 参数 | 默认值 | 说明 | 可选范围 |
|------|---------|------|---------|
| SPRING_PROFILES_ACTIVE | test | Spring Profile | dev,test,prod |
| DB_TYPE | h2 | 数据库类型 | h2,postgresql,mysql |
| DB_HOST | localhost | 数据库主机 | - |
| DB_PORT | 5432 | 数据库端口 | 5432,3306 |
| DB_NAME | inventory_test | 数据库名称 | - |
| REDIS_HOST | localhost | Redis主机 | - |
| REDIS_PORT | 6379 | Redis端口 | 6379 |
| KAFKA_BOOTSTRAP_SERVERS | localhost:9092 | Kafka地址 | - |
| MOCK_ENABLED | true | Mock启用 | true,false |
| MOCK_RANDOM_DATA | true | 随机数据 | true,false |
| MOCK_SLOW_RESPONSE | false | 慢响应 | true,false |
| MOCK_ERRORS | false | 错误模拟 | true,false |
| MOCK_ERROR_RATE | 0.05 | 错误率 | 0.0-1.0 |

### 7.2 支持和资源

- Testcontainers文档：https://www.testcontainers.org/
- JMeter文档：https://jmeter.apache.org/usermanual/
- JUnit 5文档：https://junit.org/junit5/docs/current/user-guide/
- Mockito文档：https://site.mockito.org/
- AssertJ文档：https://assertj.github.io/doc/
- GitHub Actions文档：https://docs.github.com/en/actions
- Jenkins文档：https://www.jenkins.io/doc/

### 7.3 更新日志

| 版本 | 日期 | 变更内容 | 作者 |
|------|------|---------|------|
| 1.0.0 | 2025-01-18 | 初始版本 | DevOps Team |
