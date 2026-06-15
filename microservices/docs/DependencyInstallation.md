# 依赖安装文档

## 版本
- 版本：1.0.0
- 创建日期：2025-01-18
- 最后更新：2025-01-18

## 1. 文档概述

### 1.1 目的
本文档提供了库存管理系统监控与追踪系统所需的所有依赖安装步骤，包括Maven依赖、第三方库安装、版本兼容性检查和依赖冲突解决。通过本文档，您可以确保所有必需的依赖都已正确安装和配置。

### 1.2 适用范围
- Maven依赖管理
- 第三方库安装
- 版本兼容性检查
- 依赖冲突解决
- 故障排查

### 1.3 目标读者
- 开发工程师
- DevOps工程师
- 系统管理员
- 构建工程师

### 1.4 前置条件
- 已安装Java 21 JDK
- 已安装Maven 3.9+
- 已配置Maven仓库
- 具备基本的Linux系统管理能力
- 具备网络连接能力

## 2. Maven依赖配置

### 2.1 父POM配置

在项目根目录的`pom.xml`中配置父POM：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.1</version>
        <relativePath/>
    </parent>
    
    <groupId>com.inventory</groupId>
    <artifactId>inventory-management</artifactId>
    <version>3.0.0</version>
    <packaging>jar</packaging>
    
    <name>Inventory Management System</name>
    <description>Enterprise Inventory Management System with Monitoring and Tracing</description>
    
    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        
        <spring-cloud.version>2025.0.0</spring-cloud.version>
        <hera.version>2.5.0</hera.version>
        <opentelemetry.version>1.38.0</opentelemetry.version>
        <micrometer.version>1.3.2</micrometer.version>
        <prometheus.version>1.12.0</prometheus.version>
        <grafana.version>11.0.0</grafana.version>
        <jaeger.version>1.53.0</jaeger.version>
        <kafka.version>3.7.0</kafka.version>
    </properties>
    
    <dependencyManagement>
        <dependencies>
            <!-- Spring Cloud Dependencies -->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Spring Cloud Dependencies -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        
        <!-- Monitoring Dependencies -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-core</artifactId>
            <version>${micrometer.version}</version>
        </dependency>
        
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
            <version>${micrometer.version}</version>
        </dependency>
        
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus-simpleclient</artifactId>
            <version>${micrometer.version}</version>
        </dependency>
        
        <dependency>
            <groupId>com.hera.monitoring</groupId>
            <artifactId>hera-monitoring-client</artifactId>
            <version>${hera.version}</version>
        </dependency>
        
        <!-- OpenTelemetry Dependencies -->
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-api</artifactId>
            <version>${opentelemetry.version}</version>
        </dependency>
        
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-sdk</artifactId>
            <version>${opentelemetry.version}</version>
        </dependency>
        
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-exporter-prometheus</artifactId>
            <version>${opentelemetry.version}</version>
        </dependency>
        
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-exporter-jaeger</artifactId>
            <version>${opentelemetry.version}</version>
        </dependency>
        
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-spring-boot-starter</artifactId>
            <version>${opentelemetry.version}</version>
        </dependency>
        
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-instrumentation-spring-webmvc</artifactId>
            <version>${opentelemetry.version}</version>
        </dependency>
        
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-instrumentation-jdbc</artifactId>
            <version>${opentelemetry.version}</version>
        </dependency>
        
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-instrumentation-kafka-clients</artifactId>
            <version>${opentelemetry.version}</version>
        </dependency>
        
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-instrumentation-apache-httpclient</artifactId>
            <version>${opentelemetry.version}</version>
        </dependency>
        
        <!-- Database Drivers -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.33</version>
            <scope>runtime</scope>
        </dependency>
        
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.6.0</version>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Utilities -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>1.5.5.Final</versionId>
        </dependency>
        
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>33.0.0-jre</version>
        </dependency>
        
        <!-- Testing -->
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
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>${maven.compiler.source}</source>
                    <target>${maven.compiler.target}</target>
                    <encoding>${project.build.sourceEncoding}</encoding>
                </configuration>
            </plugin>
            
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0-M9</version>
            </plugin>
        </plugins>
    </build>
</project>
```

### 2.2 Common模块POM配置

在`common/pom.xml`中配置通用依赖：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.inventory</groupId>
        <artifactId>inventory-management</artifactId>
        <version>3.0.0</version>
    </parent>
    
    <artifactId>common</artifactId>
    <packaging>jar</packaging>
    
    <name>Inventory Common Module</name>
    <description>Common monitoring and tracing components</description>
    
    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Monitoring -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-core</artifactId>
        </dependency>
        
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>
        
        <!-- OpenTelemetry -->
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-api</artifactId>
        </dependency>
        
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-sdk</artifactId>
        </dependency>
        
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-exporter-prometheus</artifactId>
        </dependency>
        
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-exporter-jaeger</artifactId>
        </dependency>
        
        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-instrumentation-spring-webmvc</artifactId>
        </dependency>
        
        <!-- Utilities -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.14.0</version>
        </dependency>
        
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-compress</artifactId>
            <version>1.26.0</version>
        </dependency>
    </dependencies>
</project>
```

## 3. 第三方库安装

### 3.1 Hera监控客户端

#### 3.1.1 下载Hera客户端

```bash
# 下载Hera监控客户端JAR包
wget https://repo.hera-monitoring.com/com/hera/monitoring/hera-monitoring-client/2.5.0/hera-monitoring-client-2.5.0.jar

# 或使用Maven中央仓库
mvn dependency:get -DremoteUrls=https://repo.hera-monitoring.com \
    -Dartifact=com.hera.monitoring:hera-monitoring-client:jar:2.5.0
```

#### 3.1.2 安装Hera客户端

```bash
# 创建Hera客户端目录
sudo mkdir -p /opt/hera-monitoring

# 移动JAR包到安装目录
sudo mv hera-monitoring-client-2.5.0.jar /opt/hera-monitoring/

# 创建符号链接（可选）
sudo ln -s /opt/hera-monitoring/hera-monitoring-client-2.5.0.jar /usr/local/bin/hera-monitoring-client.jar

# 设置执行权限
sudo chmod +x /opt/hera-monitoring/hera-monitoring-client-2.5.0.jar

# 验证安装
java -jar /opt/hera-monitoring/hera-monitoring-client-2.5.0.jar --version
```

### 3.2 OpenTelemetry SDK

OpenTelemetry SDK通过Maven自动安装，无需手动安装。验证安装：

```bash
# 验证OpenTelemetry依赖
mvn dependency:tree | grep opentelemetry

# 验证OpenTelemetry版本
mvn dependency:tree -Dincludes=io.opentelemetry:opentelemetry-api
```

### 3.3 Micrometer

Micrometer通过Maven自动安装，验证安装：

```bash
# 验证Micrometer依赖
mvn dependency:tree | grep micrometer

# 验证Micrometer版本
mvn dependency:tree -Dincludes=io.micrometer:micrometer-core
```

### 3.4 Prometheus Java Client

#### 3.4.1 下载Prometheus客户端

```bash
# 下载Prometheus Simpleclient JAR包
wget https://repo1.maven.org/maven2/io/micrometer/micrometer-registry-prometheus-simpleclient/1.3.2/micrometer-registry-prometheus-simpleclient-1.3.2.jar

# 或使用Maven
mvn dependency:get -Dartifact=io.micrometer:micrometer-registry-prometheus-simpleclient:jar:1.3.2
```

#### 3.4.2 安装Prometheus客户端

```bash
# 创建Prometheus客户端目录
sudo mkdir -p /opt/prometheus-client

# 移动JAR包到安装目录
sudo mv micrometer-registry-prometheus-simpleclient-1.3.2.jar /opt/prometheus-client/

# 创建符号链接
sudo ln -s /opt/prometheus-client/micrometer-registry-prometheus-simpleclient-1.3.2.jar /usr/local/bin/prometheus-client.jar

# 设置执行权限
sudo chmod +x /opt/prometheus-client/micrometer-registry-prometheus-simpleclient-1.3.2.jar

# 验证安装
java -jar /opt/prometheus-client/micrometer-registry-prometheus-simpleclient-1.3.2.jar --version
```

### 3.5 Jaeger Client

#### 3.5.1 下载Jaeger客户端

```bash
# 下载Jaeger Client JAR包
wget https://github.com/jaegertracing/jaeger-client-java/releases/download/v1.8.0/jaeger-client-1.8.0.jar

# 或使用Maven
mvn dependency:get -Dartifact=io.jaegertracing:jaeger-client:jar:1.8.0
```

#### 3.5.2 安装Jaeger客户端

```bash
# 创建Jaeger客户端目录
sudo mkdir -p /opt/jaeger-client

# 移动JAR包到安装目录
sudo mv jaeger-client-1.8.0.jar /opt/jaeger-client/

# 创建符号链接
sudo ln -s /opt/jaeger-client/jaeger-client-1.8.0.jar /usr/local/bin/jaeger-client.jar

# 设置执行权限
sudo chmod +x /opt/jaeger-client/jaeger-client-1.8.0.jar

# 验证安装
java -jar /opt/jaeger-client/jaeger-client-1.8.0.jar --version
```

### 3.6 Grafana Plugins

#### 3.6.1 安装Grafana插件

```bash
# 使用Grafana CLI安装插件
sudo grafana-cli plugins install grafana-piechart-panel
sudo grafana-cli plugins install grafana-worldmap-panel
sudo grafana-cli plugins install grafana-clock-panel

# 或手动安装插件
cd /var/lib/grafana/plugins
sudo wget https://grafana.com/api/plugins/grafana-piechart-panel/versions/1.6.1/grafana-piechart-panel-1.6.1.zip
sudo unzip grafana-piechart-panel-1.6.1.zip
sudo rm grafana-piechart-panel-1.6.1.zip

# 重启Grafana服务
sudo systemctl restart grafana
```

## 4. 版本兼容性检查

### 4.1 Java版本兼容性

```bash
# 检查Java版本
java -version

# 验证Java版本兼容性
# OpenTelemetry 1.38.0 需要 Java 11+
# Spring Boot 3.4.2 需要 Java 21+
# 推荐：Java 21 LTS

# 检查JVM参数
java -XX:+PrintFlagsFinal -version

# 验证GC算法兼容性
# G1GC: Java 7+
# ZGC: Java 15+
# 推荐：G1GC 或 ZGC
```

### 4.2 Spring Boot版本兼容性

```bash
# 检查Spring Boot版本
mvn help:evaluate -Dexpression=project.version -DforceStdout

# 验证Spring Boot版本兼容性
# Spring Boot 3.4.2
# Spring Cloud 2024.0.0
# Spring Cloud Alibaba 2023.0.1.0

# 检查依赖冲突
mvn dependency:tree -Dverbose
```

### 4.3 监控组件版本兼容性

```bash
# 验证OpenTelemetry版本
mvn dependency:tree -Dincludes=io.opentelemetry

# 验证Micrometer版本
mvn dependency:tree -Dincludes=io.micrometer

# 验证Hera客户端版本
java -jar /opt/hera-monitoring/hera-monitoring-client-2.5.0.jar --version

# 验证Prometheus客户端版本
java -jar /opt/prometheus-client/micrometer-registry-prometheus-simpleclient-1.3.2.jar --version

# 验证Jaeger客户端版本
java -jar /opt/jaeger-client/jaeger-client-1.8.0.jar --version
```

## 5. 依赖冲突解决

### 5.1 常见依赖冲突

#### 5.1.1 SLF4J冲突

**问题**：多个日志框架冲突

**解决方案**：
```xml
<!-- 排除Spring Boot默认的日志依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

#### 5.1.2 Jackson版本冲突

**问题**：不同模块使用不同版本的Jackson

**解决方案**：
```xml
<!-- 在父POM中统一Jackson版本 -->
<properties>
    <jackson.version>2.16.0</jackson.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-core</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
            <version>${jackson.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### 5.1.3 Netty版本冲突

**问题**：Spring Boot和Kafka使用不同版本的Netty

**解决方案**：
```xml
<!-- 排除Kafka的Netty依赖 -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
    <exclusions>
        <exclusion>
            <groupId>io.netty</groupId>
            <artifactId>netty-all</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### 5.2 依赖分析工具

```bash
# 分析依赖树
mvn dependency:tree

# 分析依赖冲突
mvn dependency:tree -Dverbose

# 查找未使用的依赖
mvn dependency:analyze

# 生成依赖报告
mvn dependency:tree -DoutputFile=dependency-tree.txt
```

## 6. 故障排查

### 6.1 Maven依赖问题

#### 6.1.1 依赖下载失败

**问题**：无法下载依赖

**排查步骤**：
```bash
# 检查网络连接
ping repo.maven.org

# 检查Maven配置
cat ~/.m2/settings.xml

# 清理本地缓存
mvn dependency:purge-local-repository

# 强制更新快照
mvn clean install -U

# 使用镜像源
mvn clean install -Dmaven.repo.local=/path/to/mirror
```

#### 6.1.2 依赖解析错误

**问题**：依赖解析失败

**排查步骤**：
```bash
# 查看详细错误信息
mvn dependency:resolve -X

# 检查依赖传递性
mvn dependency:tree -Dverbose

# 排除有问题的依赖
mvn dependency:tree -Dexcludes=problematic-dependency

# 强制重新解析
mvn dependency:resolve -U
```

#### 6.1.3 编译错误

**问题**：编译失败

**排查步骤**：
```bash
# 清理编译缓存
mvn clean

# 跳过测试编译
mvn compile -DskipTests

# 查看详细编译日志
mvn compile -X

# 检查Java版本
java -version
mvn -version

# 更新Maven插件
mvn clean install -U
```

### 6.2 运行时问题

#### 6.2.1 ClassNotFoundException

**问题**：类找不到

**解决方案**：
```bash
# 检查JAR包内容
jar tf target/inventory-service-3.0.0.jar

# 检查依赖是否包含
jar tf target/inventory-service-3.0.0.jar | grep opentelemetry

# 清理并重新打包
mvn clean package -U

# 检查类路径
java -cp target/inventory-service-3.0.0.jar com.inventory.Application

# 验证Maven本地仓库
ls ~/.m2/repository/io/opentelemetry
```

#### 6.2.2 NoSuchMethodError

**问题**：方法不存在

**解决方案**：
```bash
# 检查依赖版本
mvn dependency:tree

# 清理并重新打包
mvn clean package -U

# 检查API变更
# 查看OpenTelemetry API文档
# 验证方法签名是否正确
```

#### 6.2.3 NoClassDefFoundError

**问题**：类定义错误

**解决方案**：
```bash
# 检查依赖冲突
mvn dependency:tree -Dverbose

# 排除冲突依赖
# 在pom.xml中添加exclusions

# 清理并重新打包
mvn clean package -U

# 检查JAR包完整性
jar tf target/inventory-service-3.0.0.jar
```

## 7. 最佳实践

### 7.1 依赖管理

- 使用父POM统一版本管理
- 使用dependencyManagement管理依赖版本
- 避免使用SNAPSHOT版本
- 定期更新依赖到最新稳定版本
- 使用版本属性管理版本号

### 7.2 依赖优化

- 排除不必要的传递依赖
- 使用provided scope处理容器提供的依赖
- 使用optional标记可选依赖
- 合理使用exclusions排除冲突依赖

### 7.3 构建优化

- 并行化Maven构建
- 使用Maven缓存加速构建
- 配置合理的JVM参数
- 使用增量编译

### 7.4 安全考虑

- 定期更新依赖修复安全漏洞
- 使用依赖扫描工具（如OWASP Dependency-Check）
- 验证依赖的数字签名
- 使用私有Maven仓库

## 8. 验证和测试

### 8.1 依赖验证清单

- [ ] 所有Maven依赖已正确配置
- [ ] 所有第三方库已正确安装
- [ ] 版本兼容性已验证
- [ ] 依赖冲突已解决
- [ ] 本地Maven仓库已清理

### 8.2 功能测试

```bash
# 编译项目
mvn clean compile

# 运行单元测试
mvn test

# 打包项目
mvn clean package

# 验证JAR包
java -jar target/inventory-service-3.0.0.jar --version

# 检查依赖
mvn dependency:tree

# 分析依赖
mvn dependency:analyze
```

### 8.3 集成测试

```bash
# 测试Hera监控集成
mvn test -Dtest=HeraIntegrationTest

# 测试OpenTelemetry集成
mvn test -Dtest=OpenTelemetryIntegrationTest

# 测试Prometheus集成
mvn test -Dtest=PrometheusIntegrationTest

# 测试Jaeger集成
mvn test -Dtest=JaegerIntegrationTest

# 运行所有测试
mvn test
```

## 9. 附录

### 9.1 依赖版本参考

| 依赖 | 版本 | 说明 | 许可证 |
|------|------|------|--------|
| Spring Boot | 4.0.1 | 应用框架 | Apache 2.0 |
| Spring Cloud | 2025.0.0 | 微服务框架 | Apache 2.0 |
| Hera监控客户端 | 2.5.0 | 监控客户端 | Proprietary |
| OpenTelemetry | 1.38.0 | 可观测性框架 | Apache 2.0 |
| Micrometer | 1.3.2 | 指标门面 | Apache 2.0 |
| Prometheus客户端 | 1.3.2 | Prometheus导出器 | Apache 2.0 |
| Jaeger客户端 | 1.8.0 | 分布式追踪 | Apache 2.0 |
| MySQL驱动 | 8.0.33 | MySQL连接器 | GPL-2.0 |
| PostgreSQL驱动 | 42.6.0 | PostgreSQL连接器 | BSD-2-Clause |
| Lombok | 1.18.30 | 代码生成 | MIT |
| MapStruct | 1.5.5.Final | 对象映射 | Apache 2.0 |
| Guava | 33.0.0-jre | 工具库 | Apache 2.0 |

### 9.2 支持和资源

- Maven官方文档：https://maven.apache.org/guides/
- Spring Boot文档：https://spring.io/projects/spring-boot/docs/current/reference/html/
- OpenTelemetry文档：https://opentelemetry.io/docs/
- Micrometer文档：https://micrometer.io/docs/
- Hera监控文档：https://docs.hera-monitoring.com/
- Prometheus文档：https://prometheus.io/docs/
- Jaeger文档：https://www.jaegertracing.io/docs/

### 9.3 更新日志

| 版本 | 日期 | 变更内容 | 作者 |
|------|------|---------|------|
| 1.0.0 | 2025-01-18 | 初始版本 | DevOps Team |

