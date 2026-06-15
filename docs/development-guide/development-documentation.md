# 进销存管理系统开发文档

## 1. 文档概述

### 1.1 文档目的
本文档旨在为开发人员提供系统开发和维护的指导，包括开发规范、部署指南、故障排除等内容。

### 1.2 文档范围
本文档涵盖了进销存管理系统的以下开发相关内容：
- 开发环境搭建
- 开发规范
- 代码结构
- 数据库设计
- 部署指南
- 故障排除
- 性能优化

### 1.3 适用对象
- 系统开发人员
- 系统维护人员
- 系统架构师
- 测试人员

## 2. 开发环境搭建

### 2.1 硬件要求
- CPU：至少 4 核
- 内存：至少 8 GB
- 磁盘空间：至少 50 GB

### 2.2 软件要求

| 软件 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 开发语言 |
| Maven | 3.9+ | 构建工具 |
| PostgreSQL | 16 | 数据库 |
| Redis | 7.0+ | 缓存 |
| RabbitMQ | 3.13+ | 消息队列 |
| Git | 2.30+ | 版本控制 |
| IntelliJ IDEA | 2024+ | 开发工具 |
| Node.js | 18+ | 前端开发 |
| npm | 9+ | 前端包管理 |

### 2.3 环境搭建步骤

#### 2.3.1 后端开发环境搭建
1. 安装 JDK 17
2. 安装 Maven 3.9+
3. 安装 PostgreSQL 16
4. 安装 Redis 7.0+
5. 安装 RabbitMQ 3.13+
6. 安装 Git 2.30+
7. 安装 IntelliJ IDEA 2024+
8. 克隆代码仓库
9. 配置数据库连接
10. 运行项目

#### 2.3.2 前端开发环境搭建
1. 安装 Node.js 18+
2. 安装 npm 9+
3. 进入前端项目目录
4. 运行 `npm install` 安装依赖
5. 运行 `npm run dev` 启动开发服务器

## 3. 开发规范

### 3.1 代码规范

#### 3.1.1 Java 代码规范
- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化代码
- 使用 Spring Boot 框架
- 使用 JPA 进行数据库操作
- 代码注释率 > 30%

#### 3.1.2 前端代码规范
- 遵循 ESLint 规范
- 使用 React 框架
- 使用 TypeScript
- 组件化开发
- 代码注释率 > 30%

### 3.2 命名规范

#### 3.2.1 包命名
- 包名全部小写
- 使用反域名命名规则
- 示例：`com.inventory.inventory`

#### 3.2.2 类命名
- 使用驼峰命名法
- 首字母大写
- 示例：`DirectInboundService`

#### 3.2.3 方法命名
- 使用驼峰命名法
- 首字母小写
- 动词+名词
- 示例：`createDirectInboundOrder`

#### 3.2.4 变量命名
- 使用驼峰命名法
- 首字母小写
- 有意义的变量名
- 示例：`directInboundOrder`

### 3.3 数据库命名规范
- 表名：使用下划线分隔，全部小写
- 字段名：使用下划线分隔，全部小写
- 主键：`id` 或 `表名_id`
- 外键：`关联表名_id`

### 3.4 版本控制规范

#### 3.4.1 Git 分支管理
- `main`：主分支，用于生产环境
- `develop`：开发分支，用于集成测试
- `feature/*`：功能分支，用于开发新功能
- `bugfix/*`：bug 修复分支，用于修复生产环境 bug
- `release/*`：发布分支，用于准备发布

#### 3.4.2 Git 提交规范
- 提交信息格式：`类型: 描述`
- 类型包括：`feat`（新功能）、`fix`（bug 修复）、`docs`（文档更新）、`style`（代码风格调整）、`refactor`（代码重构）、`test`（测试）、`chore`（构建过程或辅助工具的变动）
- 示例：`feat: 实现采购订单管理功能`

## 4. 代码结构

### 4.1 后端代码结构

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── inventory/
│   │           └── inventory/
│   │               ├── controller/     # 控制器层
│   │               ├── dto/            # 数据传输对象
│   │               ├── entity/         # 实体类
│   │               ├── repository/     # 数据访问层
│   │               ├── service/        # 业务逻辑层
│   │               │   └── impl/       # 业务逻辑实现
│   │               ├── util/           # 工具类
│   │               └── InventoryApplication.java  # 应用启动类
│   └── resources/                     # 资源文件
│       ├── application.yml            # 配置文件
│       └── logback-spring.xml         # 日志配置
└── test/                              # 测试代码
```

### 4.2 前端代码结构

```
src/
├── components/        # 公共组件
├── contexts/         # 上下文
├── hooks/            # 自定义钩子
├── pages/            # 页面组件
├── services/         # API 服务
├── styles/           # 样式文件
├── utils/            # 工具函数
├── App.jsx           # 应用入口组件
├── main.jsx          # 应用入口文件
└── setupTests.js     # 测试设置
```

## 5. 数据库设计

### 5.1 数据库模型

系统采用关系型数据库（PostgreSQL），主要包括以下数据模型：
- 用户模型
- 产品模型
- 库存模型
- 订单模型
- 财务模型

### 5.2 数据库连接配置

在 `application.yml` 文件中配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/inventory_system
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### 5.3 数据库迁移

使用 Flyway 进行数据库迁移，迁移脚本位于 `src/main/resources/db/migration` 目录下。

## 6. 部署指南

### 6.1 开发环境部署

#### 6.1.1 后端部署
1. 进入项目目录
2. 运行 `mvn clean install` 构建项目
3. 运行 `java -jar target/inventory-service-3.0.0.jar` 启动服务

#### 6.1.2 前端部署
1. 进入前端项目目录
2. 运行 `npm run build` 构建项目
3. 将构建生成的 `dist` 目录部署到 Nginx 或其他 Web 服务器

### 6.2 测试环境部署

使用 Docker 容器化部署：

1. 构建 Docker 镜像
2. 编写 Docker Compose 文件
3. 运行 `docker-compose up -d` 启动服务

### 6.3 生产环境部署

使用 Kubernetes 进行部署：

1. 编写 Kubernetes 部署文件
2. 部署到 Kubernetes 集群
3. 配置负载均衡
4. 配置监控和告警

## 7. 故障排除

### 7.1 常见问题

#### 7.1.1 数据库连接失败
- 检查数据库服务是否启动
- 检查数据库连接配置是否正确
- 检查数据库用户权限

#### 7.1.2 服务启动失败
- 检查端口是否被占用
- 检查依赖服务是否正常
- 检查配置文件是否正确

#### 7.1.3 API 调用失败
- 检查请求 URL 是否正确
- 检查请求参数是否正确
- 检查用户权限
- 检查服务日志

### 7.2 日志分析

- 日志文件位置：`logs/inventory-system.log`
- 日志级别：DEBUG、INFO、WARN、ERROR
- 使用 ELK Stack 进行日志分析

### 7.3 监控告警

- 使用 Prometheus 进行系统监控
- 使用 Grafana 进行监控数据可视化
- 配置告警规则，及时发现和处理问题

## 8. 性能优化

### 8.1 代码优化
- 减少数据库查询次数
- 使用缓存
- 优化算法
- 减少网络请求

### 8.2 数据库优化
- 合理设计索引
- 分表分库
- 优化 SQL 查询
- 使用连接池

### 8.3 缓存优化
- 使用 Redis 进行缓存
- 合理设置缓存过期时间
- 实现缓存一致性
- 避免缓存雪崩、缓存击穿、缓存穿透

### 8.4 并发优化
- 使用异步处理
- 优化锁机制
- 合理设计事务范围
- 使用线程池

## 9. 安全管理

### 9.1 认证授权
- 使用 JWT 进行身份验证
- 实现基于角色的访问控制（RBAC）
- 定期刷新令牌

### 9.2 数据安全
- 敏感数据加密存储
- 数据传输使用 HTTPS
- 定期数据备份
- 数据恢复机制

### 9.3 访问控制
- 实现 API 网关的访问控制
- 限制 API 调用频率
- 防止 SQL 注入、XSS 攻击等
- 实现 CSRF 防护

## 10. 测试指南

### 10.1 测试类型
- 单元测试
- 集成测试
- 系统测试
- 性能测试
- 安全测试

### 10.2 测试框架

#### 10.2.1 后端测试
- JUnit 5
- Mockito
- Spring Boot Test

#### 10.2.2 前端测试
- Jest
- React Testing Library

### 10.3 测试覆盖率
- 单元测试覆盖率 > 80%
- 集成测试覆盖率 > 60%
- 系统测试覆盖率 > 40%

## 11. 文档管理

### 11.1 文档类型
- 需求文档
- 设计文档
- 开发文档
- 测试文档
- 用户手册

### 11.2 文档更新
- 文档与代码同步更新
- 文档版本与代码版本一致
- 使用 Markdown 格式编写文档

## 12. 附录

### 12.1 常用命令

#### 12.1.1 后端命令
- `mvn clean install`：构建项目
- `mvn test`：运行测试
- `mvn spring-boot:run`：启动服务
- `mvn jacoco:report`：生成测试覆盖率报告

#### 12.1.2 前端命令
- `npm install`：安装依赖
- `npm run dev`：启动开发服务器
- `npm run build`：构建项目
- `npm run test`：运行测试
- `npm run lint`：代码检查

### 12.2 常用工具

| 工具 | 用途 |
|------|------|
| Postman | API 测试 |
| Redis Desktop Manager | Redis 管理 |
| DBeaver | 数据库管理 |
| Prometheus | 系统监控 |
| Grafana | 监控数据可视化 |
| ELK Stack | 日志分析 |

### 12.3 联系方式

- 项目负责人：project@example.com
- 技术支持：support@example.com
- 团队协作：使用 Jira 进行任务管理，Confluence 进行文档管理

### 12.4 版本信息

| 版本 | 发布日期 | 更新内容 |
|------|----------|----------|
| 3.0.0 | 2025-12-05 | 初始版本 |
