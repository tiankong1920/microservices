# Project Documentation Index

库存管理系统完整项目文档索引。

## 📁 文档目录结构

```
docs/
├── ARCHITECTURE.md          # 系统架构文档
├── DEPLOYMENT.md            # 部署指南
├── UAT_TEST_PLAN.md         # 用户验收测试计划
├── LAUNCH_CHECKLIST.md      # 上线检查清单
├── API_GUIDE.md            # API使用指南
├── TROUBLESHOOTING.md      # 故障排查指南
└── SECURITY.md             # 安全指南
```

## 🎯 项目里程碑

| 阶段 | 状态 | 说明 |
|------|------|------|
| M1: 基础设施修复 | ✅ 完成 | PostgreSQL/Redis连接修复 |
| M2: 支持服务实现 | ✅ 完成 | Nacos/Config/Gateway |
| M3: 业务功能补全 | ✅ 完成 | 订单/采购/库存/伙伴管理 |
| M4: 测试覆盖达标 | ✅ 完成 | 单元测试/集成测试 |
| M5: 监控+安全+文档 | ✅ 完成 | Prometheus/安全扫描 |
| M6: 上线交付准备 | ✅ 完成 | 部署脚本/迁移/检查清单 |

## 🚀 快速链接

### 核心文档

| 文档 | 描述 |
|------|------|
| [README.md](../README.md) | 项目主页面 |
| [ARCHITECTURE.md](./ARCHITECTURE.md) | 系统架构详解 |
| [DEPLOYMENT.md](./DEPLOYMENT.md) | 生产部署指南 |
| [CONTRIBUTING.md](../CONTRIBUTING.md) | 贡献指南 |

### API文档

| 服务 | 端口 | OpenAPI |
|------|------|---------|
| order-service | 8082 | [openapi.yaml](../core-services/order-service/src/main/resources/openapi.yaml) |
| procurement-service | 8085 | [openapi.yaml](../core-services/procurement-service/src/main/resources/openapi.yaml) |

### 配置文件

| 文件 | 用途 |
|------|------|
| [.env.prod.template](../.env.prod.template) | 生产环境变量模板 |
| [docker-compose.prod.yml](../docker-compose.prod.yml) | 生产环境Docker配置 |
| [prometheus.yml](../monitoring/prometheus.yml) | Prometheus配置 |
| [alerting-rules.yml](../monitoring/alerting-rules.yml) | 告警规则 |

### CI/CD

| 文件 | 用途 |
|------|------|
| [.github/workflows/build.yml](../.github/workflows/build.yml) | 后端构建 |
| [.github/workflows/frontend-ci.yml](../.github/workflows/frontend-ci.yml) | 前端CI/CD |
| [.github/workflows/security-scan.yml](../.github/workflows/security-scan.yml) | 安全扫描 |

### 部署脚本

| 文件 | 用途 |
|------|------|
| [scripts/deploy-prod.sh](../scripts/deploy-prod.sh) | 生产部署脚本 |
| [scripts/migrate.sh](../scripts/migrate.sh) | 数据库迁移 |
| [scripts/migrations/](../scripts/migrations/) | SQL迁移文件 |

## 📊 项目结构

### 微服务列表

```
core-services/
├── order-service/           # 订单服务 (8082)
├── product-service/          # 商品服务 (8081)
├── inventory-service/        # 库存服务 (8083)
├── procurement-service/      # 采购服务 (8085)
├── customer-service/         # 客户管理 (8086)
├── supplier-service/         # 供应商管理 (8087)
├── business-partner-service/  # 业务伙伴 (8095)
├── sales-service/             # 销售服务 (8096)
├── mall-service/              # 商城服务 (8097)
└── datasource-service/        # 数据源服务 (8098)

support-services/
├── auth-service/             # 认证服务 (8093)
├── gateway-service/          # API网关 (8080)
├── registry-service/         # 服务注册 (8761)
├── config-service/           # 配置中心 (8888)
├── admin-service/            # 管理后台 (8091)
├── finance-service/          # 财务服务 (8092)
└── report-service/           # 报表服务 (8094)

common/                        # 公共模块
└── saga/                      # Saga分布式事务框架

mall-frontend/                 # 前端应用
├── src/
│   ├── pages/               # 页面组件
│   ├── components/          # 通用组件
│   ├── store/               # Zustand状态管理
│   ├── services/            # API服务
│   └── hooks/               # React Hooks
```

## 🔧 开发指南

### 本地开发

```bash
# 启动基础设施
docker-compose -f docker-compose.dev.yml up -d

# 启动后端
./gradlew bootRun

# 启动前端
cd mall-frontend && npm run dev
```

### 运行测试

```bash
# 单元测试
./gradlew test

# 集成测试
./gradlew integrationTest

# 前端测试
cd mall-frontend && npm test
```

### 代码检查

```bash
# 后端
./gradlew checkstyleMain spotbugsMain

# 前端
cd mall-frontend && npm run lint
```

## 📈 监控与运维

### 监控组件

| 组件 | 地址 | 说明 |
|------|------|------|
| Grafana | http://localhost:3000 | 监控仪表板 |
| Prometheus | http://localhost:9090 | 指标收集 |
| AlertManager | http://localhost:9093 | 告警管理 |

### 日志聚合

| 组件 | 地址 | 说明 |
|------|------|------|
| Kibana | http://localhost:5601 | 日志查看 |
| Elasticsearch | http://localhost:9200 | 日志存储 |

## 🔐 安全

### 安全扫描工具

| 工具 | 用途 |
|------|------|
| OWASP Dependency Check | 依赖漏洞扫描 |
| SpotBugs | Java代码缺陷检测 |
| Semgrep | 代码安全规则扫描 |
| Trivy | 容器镜像扫描 |
| TruffleHog | 密钥泄露检测 |

### 安全配置

- [security-scan.yml](../.github/workflows/security-scan.yml) - 自动安全扫描
- [Spring Security配置](../core-services/auth-service/) - 认证授权

## 📝 更新日志

### v3.0.0 (2024-01-15)
- 完成M1-M6所有阶段
- 实现Saga分布式事务
- 添加完整的监控告警
- 完善API文档
- 生产环境部署就绪

### v2.0.0 (2023-12-01)
- 微服务架构重构
- 添加订单状态机
- 实现库存预警系统

### v1.0.0 (2023-10-01)
- 单体应用版本
- 基础CRUD功能

## 📧 支持

- 问题反馈: [GitHub Issues](https://github.com/inventory-system/inventory-management/issues)
- 讨论组: [GitHub Discussions](https://github.com/inventory-system/inventory-management/discussions)
- 邮箱: support@inventory-system.com
