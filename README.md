# 库存管理系统 (Inventory Management System)

基于微服务架构的企业库存管理平台，覆盖商品、订单、库存、销售、采购、客户、供应商等核心业务领域。系统采用前后端分离架构，后端基于 Spring Cloud Alibaba 微服务栈，前端基于 React 19 + Vite 构建。

## 架构概览

```
┌──────────────────────────────────────────────────────────────┐
│                      Web Frontend (React)                     │
│                  Port: 5173 (dev) / 80 (prod)                 │
└──────────────────────────┬───────────────────────────────────┘
                           │ HTTP/REST
┌──────────────────────────▼───────────────────────────────────┐
│                    gateway-service :8080                      │
│              路由 · 限流 · JWT 鉴权 · 请求转发                 │
└──────┬────────────────────┬───────────────────┬──────────────┘
       │                    │                   │
  ┌────▼────┐          ┌────▼────┐         ┌────▼────┐
  │ 核心服务  │          │ 支撑服务  │         │ 基础设施  │
  │          │          │          │         │          │
  │product  │          │auth      │         │ Nacos    │
  │order    │          │admin     │         │ :8848    │
  │inventory│          │finance   │         │ PG :5432 │
  │sales    │          │config    │         │ Redis    │
  │procurement│        │registry  │         │ :6379    │
  │customer │          │report    │         │          │
  │supplier │          │          │         │          │
  │biz-partner│       │          │         │          │
  └─────────┘          └──────────┘         └──────────┘
```

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.4.4 |
| 微服务 | Spring Cloud / Alibaba | 2024.0.2 / 2023.0.1.0 |
| 运行时 | Java | 21 |
| 构建工具 | Gradle (Kotlin DSL) | 9.4.0 |
| 数据库 | PostgreSQL | 16+ |
| 缓存 | Redis | 7+ |
| 注册/配置中心 | Nacos | 2.3.0 |
| 前端框架 | React | 19 |
| 前端构建 | Vite | 8 |
| UI 组件库 | MUI (Material UI) | 7 |
| 测试 | JUnit 5, Mockito, Vitest | - |

## 快速开始

### 环境要求

- Java 21+
- Node.js 20+
- Docker & Docker Compose (可选，用于基础设施)

### 后端启动

```bash
# 1. 启动基础设施 (PostgreSQL + Redis + Nacos)
cd microservices
docker compose up -d

# 2. 构建后端
./gradlew build -x test

# 3. 启动微服务 (以 product-service 为例)
./gradlew :core-services:product-service:bootRun
```

### 前端启动

```bash
# 1. 安装依赖
cd web-frontend
npm install

# 2. 启动开发服务器
npm run dev
```

### 常用命令

```bash
# 后端 - 构建并运行测试
cd microservices
./gradlew build

# 后端 - 运行质量检查
./gradlew checkQuality

# 后端 - 带质量门禁的构建
./gradlew buildWithQuality

# 前端 - 构建生产版本
cd web-frontend
npm run build

# 前端 - 运行测试
npm run test

# 前端 - 代码检查
npm run lint
```

## 项目结构

```
E:\101
├── microservices/              # 后端微服务
│   ├── common/                 # 共享代码 (DTO, 异常, 工具类)
│   ├── core-services/          # 核心业务服务
│   │   ├── product-service/    # 商品服务
│   │   ├── order-service/      # 订单服务
│   │   ├── inventory-service/  # 库存服务
│   │   ├── sales-service/      # 销售服务
│   │   ├── procurement-service/ # 采购服务
│   │   ├── customer-service/   # 客户服务
│   │   ├── supplier-service/   # 供应商服务
│   │   └── business-partner-service/ # 合作伙伴服务
│   ├── support-services/       # 支撑服务
│   │   ├── auth-service/       # 认证授权
│   │   ├── admin-service/      # 管理后台
│   │   ├── finance-service/    # 财务管理
│   │   ├── gateway-service/    # API 网关
│   │   ├── config-service/     # 配置中心
│   │   ├── registry-service/   # 注册中心
│   │   └── report-service/     # 报表服务
│   ├── project-root/           # 映射的服务模块
│   ├── build.gradle.kts        # 根构建脚本
│   └── DESIGN.md               # 架构设计文档
├── web-frontend/               # 前端应用
│   └── src/
│       ├── components/         # 公共组件
│       ├── pages/              # 页面组件
│       ├── services/           # API 服务层
│       ├── contexts/           # React Context
│       └── types/              # TypeScript 类型
└── docs/                       # 项目文档
```

## 服务端口映射

| 服务 | 端口 | 服务 | 端口 |
|------|------|------|------|
| gateway | 8080 | supplier | 8087 |
| product | 8081 | business-partner | 8088 |
| order | 8082 | admin | 8091 |
| inventory | 8083 | finance | 8092 |
| sales | 8084 | auth | 8093 |
| procurement | 8085 | report | 8094 |
| customer | 8086 | config | 8888 |
| registry | 8761 | Nacos | 8848 |

## 相关文档

- [架构设计文档](microservices/DESIGN.md)
- [后端开发指南](microservices/AGENTS.md)
- [前端开发指南](web-frontend/README.md)
- [Docker 部署配置](microservices/docker-compose.yml)
