# customer-service

## 服务概述

**服务类型**: Core Service  
**服务描述**: 客户信息管理  
**服务端口**: 8086  
**版本**: 3.0.0

## 技术栈

- Java 21
- Spring Boot 3.5.8
- Spring Cloud 2024.0.3
- PostgreSQL 16
- Redis 7.0
- Maven 3.9+

## 快速开始

### 环境要求

- JDK 21
- Maven 3.9+
- PostgreSQL 16
- Redis 7.0

### 本地运行

```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 打包
mvn clean package
```

### Docker运行

```bash
# 构建镜像
docker build -t inventory/customer-service:3.0.0 .

# 运行容器
docker run -p 8086:8086 inventory/customer-service:3.0.0
```

## API文档

服务启动后访问: http://localhost:8086/swagger-ui.html

## 主要功能

### 核心接口

- 查询接口
- 创建接口
- 更新接口
- 删除接口

## 配置说明

### 应用配置

主要配置文件位于 `config-repo/customer-service/`

### 环境变量

- `SPRING_PROFILES_ACTIVE`: 运行环境 (dev/test/prod)
- `EUREKA_URI`: Eureka服务地址
- `CONFIG_URI`: 配置中心地址

## 依赖服务

- **registry-service**: 服务注册中心 (必需)
- **config-service**: 配置中心 (必需)
- **PostgreSQL**: 数据库 (必需)
- **Redis**: 缓存 (必需)

## 监控和健康检查

- 健康检查: http://localhost:8086/actuator/health
- 指标监控: http://localhost:8086/actuator/metrics

## 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify

# 代码覆盖率
mvn jacoco:report
```

## 故障排查

### 常见问题

1. **服务无法启动**
   - 检查Java版本是否为21
   - 检查端口8086是否被占用
   - 检查Eureka服务是否启动

2. **无法连接数据库**
   - 检查PostgreSQL是否运行
   - 验证数据库连接配置

3. **服务注册失败**
   - 确认registry-service已启动
   - 检查网络连接

## 开发指南

### 代码规范

- 遵循Google Java Style Guide
- 使用Checkstyle进行代码检查
- 使用PMD进行代码质量分析

### 提交规范

- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 重构
- test: 测试相关
- chore: 构建/工具相关

## 联系方式

- 项目负责人: [待填写]
- 技术支持: [待填写]

## 更新日志

### v3.0.0 (2025-12-15)
- 升级到Java 21
- 升级到Spring Boot 3.5.8
- 初始版本发布
