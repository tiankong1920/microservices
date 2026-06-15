# 数据源管理系统实施计划

## 1. 项目概述

### 1.1 项目目标
构建企业级数据源管理系统，实现对MySQL、PostgreSQL、Elasticsearch、Kudu等多种数据存储类型的集中化、标准化管理。

### 1.2 核心功能
- 多数据存储类型支持（MySQL、PostgreSQL、Elasticsearch、Kudu）
- 插件化扩展架构
- 可视化配置界面
- 连通性检测机制
- 连接稳定性保障
- 安全机制（AES-256加密、RBAC权限控制）
- 多租户支持
- 完整的审计日志

### 1.3 技术栈
- **后端**: Java 21 + Spring Boot 3.4.4 + Spring Cloud
- **前端**: React 18 + Ant Design 5 + TypeScript
- **数据库**: MySQL 8.0+
- **缓存**: Redis 6.0+
- **部署**: Docker + Docker Compose

## 2. 系统架构

### 2.1 整体架构
```
┌─────────────────────────────────────────────────────────────────┐
│                        前端层 (React + Ant Design)                │
├─────────────────────────────────────────────────────────────────┤
│                        API网关层 (Spring Cloud Gateway)           │
├─────────────────────────────────────────────────────────────────┤
│  datasource-service │ auth-service │ monitoring-service          │
├─────────────────────────────────────────────────────────────────┤
│                        数据访问层                                  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │MySQL插件 │ │PG插件    │ │ES插件    │ │Kudu插件  │           │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
├─────────────────────────────────────────────────────────────────┤
│  MySQL 8.0+ (系统数据) │ Redis 6.0+ (缓存)                       │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 模块划分
```
datasource-service/
├── datasource-api/           # API接口定义
├── datasource-core/          # 核心业务逻辑
├── datasource-plugins/       # 数据源插件
│   ├── plugin-mysql/         # MySQL插件
│   ├── plugin-postgresql/    # PostgreSQL插件
│   ├── plugin-elasticsearch/ # Elasticsearch插件
│   └── plugin-kudu/          # Kudu插件
└── datasource-web/           # Web管理界面
```

## 3. 数据库设计

### 3.1 核心表结构

#### 数据源配置表 (datasource_config)
```sql
CREATE TABLE datasource_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    name VARCHAR(128) NOT NULL COMMENT '数据源名称',
    type VARCHAR(32) NOT NULL COMMENT '数据源类型: MYSQL/POSTGRESQL/ELASTICSEARCH/KUDU',
    version VARCHAR(32) COMMENT '版本号',
    host VARCHAR(255) NOT NULL COMMENT '主机地址',
    port INT NOT NULL COMMENT '端口',
    database_name VARCHAR(128) COMMENT '数据库名',
    username VARCHAR(128) COMMENT '用户名',
    password TEXT NOT NULL COMMENT '加密后的密码',
    extra_config JSON COMMENT '扩展配置(JSON格式)',
    status VARCHAR(16) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/INACTIVE/DELETED',
    template_id BIGINT COMMENT '模板ID',
    created_by VARCHAR(64) COMMENT '创建人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(64) COMMENT '更新人',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_type (type),
    INDEX idx_status (status)
);
```

#### 连接状态表 (connection_status)
```sql
CREATE TABLE connection_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    datasource_id BIGINT NOT NULL COMMENT '数据源ID',
    status VARCHAR(16) NOT NULL COMMENT '状态: CONNECTED/DISCONNECTED/ERROR',
    response_time INT COMMENT '响应时间(毫秒)',
    error_message TEXT COMMENT '错误信息',
    checked_at DATETIME NOT NULL COMMENT '检测时间',
    INDEX idx_datasource_id (datasource_id),
    INDEX idx_checked_at (checked_at)
);
```

#### 连接测试日志表 (connection_test_log)
```sql
CREATE TABLE connection_test_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    datasource_id BIGINT NOT NULL,
    test_type VARCHAR(16) NOT NULL COMMENT '测试类型: AUTO/MANUAL/BATCH',
    result VARCHAR(16) NOT NULL COMMENT '结果: SUCCESS/FAILURE',
    response_time INT COMMENT '响应时间(毫秒)',
    error_code VARCHAR(32) COMMENT '错误码',
    error_message TEXT COMMENT '错误详情',
    suggestions TEXT COMMENT '解决方案建议',
    tested_by VARCHAR(64) COMMENT '测试人',
    tested_at DATETIME NOT NULL COMMENT '测试时间',
    INDEX idx_datasource_id (datasource_id),
    INDEX idx_tested_at (tested_at),
    INDEX idx_result (result)
);
```

#### 配置模板表 (config_template)
```sql
CREATE TABLE config_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL COMMENT '模板名称',
    type VARCHAR(32) NOT NULL COMMENT '数据源类型',
    description TEXT COMMENT '模板描述',
    config_json JSON NOT NULL COMMENT '配置参数(JSON)',
    is_public TINYINT DEFAULT 0 COMMENT '是否公开: 0-否 1-是',
    created_by VARCHAR(64),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_type (type)
);
```

#### 审计日志表 (audit_log)
```sql
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    username VARCHAR(128),
    operation VARCHAR(32) NOT NULL COMMENT '操作类型',
    resource_type VARCHAR(32) NOT NULL COMMENT '资源类型',
    resource_id VARCHAR(64) COMMENT '资源ID',
    old_value JSON COMMENT '变更前值',
    new_value JSON COMMENT '变更后值',
    ip_address VARCHAR(64),
    user_agent VARCHAR(512),
    created_at DATETIME NOT NULL,
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);
```

#### 告警配置表 (alert_config)
```sql
CREATE TABLE alert_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    datasource_ids JSON COMMENT '关联的数据源ID列表',
    alert_level VARCHAR(16) NOT NULL COMMENT '告警级别: CRITICAL/WARNING/INFO',
    alert_channels JSON NOT NULL COMMENT '告警渠道: ["EMAIL","SMS","DINGTALK","WECHAT"]',
    receivers JSON NOT NULL COMMENT '接收人配置',
    notify_frequency VARCHAR(16) DEFAULT 'IMMEDIATE' COMMENT '通知频率: IMMEDIATE/HOURLY/DAILY',
    enabled TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant_id (tenant_id)
);
```

### 3.2 多租户数据隔离
- 所有业务表包含 `tenant_id` 字段
- 通过租户上下文自动注入查询条件
- 支持租户级别的配置定制

## 4. 任务分解

### Phase 1: 基础设施搭建 (预计工作量: 中)

#### Task 1.1: 创建服务模块结构
**描述**: 创建datasource-service服务模块，包含标准的项目结构
**验收标准**:
- [ ] 创建datasource-service目录结构
- [ ] 配置build.gradle.kts
- [ ] 创建基础Application启动类
- [ ] 配置application.yml

**依赖**: 无
**涉及文件**: 
- `core-services/datasource-service/build.gradle.kts`
- `core-services/datasource-service/src/main/java/.../Application.java`

---

#### Task 1.2: 数据库初始化脚本
**描述**: 创建数据库表结构和初始化数据
**验收标准**:
- [ ] 创建所有核心表的DDL脚本
- [ ] 创建索引和约束
- [ ] 插入初始配置数据

**依赖**: Task 1.1
**涉及文件**:
- `init-scripts/datasource-init.sql`

---

#### Task 1.3: 核心实体类开发
**描述**: 开发所有核心实体类和DTO
**验收标准**:
- [ ] 创建DatasourceConfig实体
- [ ] 创建ConnectionStatus实体
- [ ] 创建ConnectionTestLog实体
- [ ] 创建ConfigTemplate实体
- [ ] 创建AuditLog实体
- [ ] 创建AlertConfig实体
- [ ] 创建相关DTO类

**依赖**: Task 1.2
**涉及文件**:
- `core-services/datasource-service/src/main/java/.../entity/`
- `core-services/datasource-service/src/main/java/.../dto/`

---

### Phase 2: 插件化架构开发 (预计工作量: 大)

#### Task 2.1: 定义插件接口规范
**描述**: 定义数据源插件的标准接口
**验收标准**:
- [ ] 定义DataSourcePlugin接口
- [ ] 定义ConnectionTestResult接口
- [ ] 定义MetadataDiscovery接口
- [ ] 创建插件注册机制

**依赖**: Task 1.3
**涉及文件**:
- `core-services/datasource-service/src/main/java/.../plugin/`

---

#### Task 2.2: MySQL插件开发
**描述**: 开发MySQL数据源插件，支持5.7+和8.0+版本
**验收标准**:
- [ ] 实现MySQL连接逻辑
- [ ] 实现版本检测功能
- [ ] 实现连接测试功能
- [ ] 实现元数据发现功能
- [ ] 编写单元测试

**依赖**: Task 2.1
**涉及文件**:
- `core-services/datasource-service/src/main/java/.../plugin/mysql/`

---

#### Task 2.3: PostgreSQL插件开发
**描述**: 开发PostgreSQL数据源插件，支持18+版本
**验收标准**:
- [ ] 实现PostgreSQL连接逻辑
- [ ] 实现版本检测功能
- [ ] 实现连接测试功能
- [ ] 实现元数据发现功能
- [ ] 编写单元测试

**依赖**: Task 2.1
**涉及文件**:
- `core-services/datasource-service/src/main/java/.../plugin/postgresql/`

---

#### Task 2.4: Elasticsearch插件开发
**描述**: 开发Elasticsearch数据源插件，支持6.x/7.x/8.x版本
**验收标准**:
- [ ] 实现ES连接逻辑
- [ ] 实现版本检测和兼容性处理
- [ ] 实现连接测试功能
- [ ] 实现索引元数据发现
- [ ] 编写单元测试

**依赖**: Task 2.1
**涉及文件**:
- `core-services/datasource-service/src/main/java/.../plugin/elasticsearch/`

---

#### Task 2.5: Kudu插件开发
**描述**: 开发Kudu数据源插件，支持1.10+版本
**验收标准**:
- [ ] 实现Kudu连接逻辑
- [ ] 实现连接测试功能
- [ ] 实现表元数据发现
- [ ] 编写单元测试

**依赖**: Task 2.1
**涉及文件**:
- `core-services/datasource-service/src/main/java/.../plugin/kudu/`

---

### Phase 3: 核心业务功能开发 (预计工作量: 大)

#### Task 3.1: 数据源配置管理API
**描述**: 开发数据源配置的CRUD API
**验收标准**:
- [ ] 创建数据源配置接口
- [ ] 更新数据源配置接口
- [ ] 删除数据源配置接口
- [ ] 查询数据源配置接口
- [ ] 分页查询接口
- [ ] 参数验证逻辑

**依赖**: Task 1.3
**涉及文件**:
- `core-services/datasource-service/src/main/java/.../controller/`
- `core-services/datasource-service/src/main/java/.../service/`

---

#### Task 3.2: 连通性检测服务
**描述**: 实现连接测试功能
**验收标准**:
- [ ] 自动连接测试（配置保存后触发）
- [ ] 手动连接测试接口
- [ ] 批量连接测试接口（并发处理）
- [ ] 测试结果记录和查询
- [ ] 3秒超时控制
- [ ] 错误诊断和解决方案建议

**依赖**: Task 2.2, 2.3, 2.4, 2.5
**涉及文件**:
- `core-services/datasource-service/src/main/java/.../service/ConnectionTestService.java`

---

#### Task 3.3: 连接稳定性保障服务
**描述**: 实现连接监控和自动恢复机制
**验收标准**:
- [ ] 指数退避重试策略
- [ ] 超时控制配置
- [ ] 定时健康检查
- [ ] 异常自动恢复
- [ ] 连接状态监控仪表盘数据接口

**依赖**: Task 3.2
**涉及文件**:
- `core-services/datasource-service/src/main/java/.../service/ConnectionMonitorService.java`

---

#### Task 3.4: 配置模板管理
**描述**: 实现配置模板功能
**验收标准**:
- [ ] 模板创建接口
- [ ] 模板修改接口
- [ ] 模板复制接口
- [ ] 模板删除接口
- [ ] 模板共享功能
- [ ] 基于模板创建数据源

**依赖**: Task 3.1
**涉及文件**:
- `core-services/datasource-service/src/main/java/.../service/TemplateService.java`

---

#### Task 3.5: 配置导入导出
**描述**: 实现配置的导入导出功能
**验收标准**:
- [ ] JSON格式导出
- [ ] YAML格式导出
- [ ] JSON格式导入
- [ ] YAML格式导入
- [ ] 导出文件包含元数据

**依赖**: Task 3.1
**涉及文件**:
- `core-services/datasource-service/src/main/java/.../service/ConfigExportService.java`

---

### Phase 4: 安全机制开发 (预计工作量: 中)

#### Task 4.1: 密码加密服务
**描述**: 实现AES-256加密存储
**验收标准**:
- [ ] 前端加密实现
- [ ] 后端加密验证
- [ ] 密钥管理机制
- [ ] 密钥轮换支持

**依赖**: Task 1.3
**涉及文件**:
- `core-services/datasource-service/src/main/java/.../security/`

---

#### Task 4.2: RBAC权限控制
**描述**: 实现细粒度权限控制
**验收标准**:
- [ ] 权限模型设计
- [ ] 权限注解实现
- [ ] 数据源级别权限
- [ ] 操作级别权限

**依赖**: Task 4.1
**涉及文件**:
- `core-services/datasource-service/src/main/java/.../security/`

---

#### Task 4.3: 审计日志服务
**描述**: 实现完整的操作审计
**验收标准**:
- [ ] 配置变更审计
- [ ] 连接状态变化审计
- [ ] 用户操作审计
- [ ] 日志查询接口
- [ ] 日志导出功能

**依赖**: Task 1.3
**涉及文件**:
- `core-services/datasource-service/src/main/java/.../service/AuditService.java`

---

### Phase 5: 告警通知开发 (预计工作量: 中)

#### Task 5.1: 告警规则引擎
**描述**: 实现告警规则配置和触发
**验收标准**:
- [ ] 告警规则配置接口
- [ ] 告警触发逻辑
- [ ] 告警级别管理
- [ ] 告警频率控制

**依赖**: Task 3.3
**涉及文件**:
- `core-services/datasource-service/src/main/java/.../alert/`

---

#### Task 5.2: 多渠道通知服务
**描述**: 实现邮件、短信、企业微信、钉钉通知
**验收标准**:
- [ ] 邮件通知实现
- [ ] 短信通知实现
- [ ] 企业微信通知实现
- [ ] 钉钉通知实现
- [ ] 通知模板配置

**依赖**: Task 5.1
**涉及文件**:
- `core-services/datasource-service/src/main/java/.../alert/notify/`

---

### Phase 6: 前端开发 (预计工作量: 大)

#### Task 6.1: 前端项目初始化
**描述**: 创建React前端项目
**验收标准**:
- [ ] 创建React 18项目
- [ ] 配置Ant Design 5
- [ ] 配置路由
- [ ] 配置状态管理
- [ ] 配置API请求封装

**依赖**: 无
**涉及文件**:
- `datasource-frontend/`

---

#### Task 6.2: 数据源列表页面
**描述**: 开发数据源列表展示页面
**验收标准**:
- [ ] 数据源列表展示
- [ ] 搜索和筛选功能
- [ ] 分页功能
- [ ] 状态标识显示
- [ ] 响应式布局

**依赖**: Task 6.1
**涉及文件**:
- `datasource-frontend/src/pages/DatasourceList/`

---

#### Task 6.3: 数据源配置表单
**描述**: 开发数据源配置表单页面
**验收标准**:
- [ ] MySQL配置表单
- [ ] PostgreSQL配置表单
- [ ] Elasticsearch配置表单
- [ ] Kudu配置表单
- [ ] 参数验证
- [ ] 参数说明提示

**依赖**: Task 6.2
**涉及文件**:
- `datasource-frontend/src/pages/DatasourceForm/`

---

#### Task 6.4: 连接测试界面
**描述**: 开发连接测试和结果展示界面
**验收标准**:
- [ ] 单个测试按钮
- [ ] 批量测试功能
- [ ] 测试进度显示
- [ ] 结果详情展示
- [ ] 错误解决方案展示

**依赖**: Task 6.3
**涉及文件**:
- `datasource-frontend/src/components/ConnectionTest/`

---

#### Task 6.5: 监控仪表盘
**描述**: 开发连接状态监控仪表盘
**验收标准**:
- [ ] 状态概览卡片
- [ ] 响应时间折线图
- [ ] 错误率饼图
- [ ] 实时数据刷新
- [ ] 告警信息展示

**依赖**: Task 6.2
**涉及文件**:
- `datasource-frontend/src/pages/Dashboard/`

---

#### Task 6.6: 配置模板管理界面
**描述**: 开发模板管理界面
**验收标准**:
- [ ] 模板列表页面
- [ ] 模板创建/编辑表单
- [ ] 模板复制功能
- [ ] 模板共享设置

**依赖**: Task 6.3
**涉及文件**:
- `datasource-frontend/src/pages/TemplateManage/`

---

#### Task 6.7: 审计日志界面
**描述**: 开发审计日志查询界面
**验收标准**:
- [ ] 日志列表展示
- [ ] 多条件筛选
- [ ] 日志详情查看
- [ ] 日志导出功能

**依赖**: Task 6.2
**涉及文件**:
- `datasource-frontend/src/pages/AuditLog/`

---

### Phase 7: 部署和文档 (预计工作量: 中)

#### Task 7.1: Docker配置
**描述**: 创建Docker部署配置
**验收标准**:
- [ ] 后端服务Dockerfile
- [ ] 前端服务Dockerfile
- [ ] Docker Compose配置
- [ ] 环境变量配置

**依赖**: Task 3.x, 6.x
**涉及文件**:
- `core-services/datasource-service/Dockerfile`
- `datasource-frontend/Dockerfile`
- `docker-compose.datasource.yml`

---

#### Task 7.2: API文档
**描述**: 生成Swagger API文档
**验收标准**:
- [ ] 集成Swagger/OpenAPI
- [ ] 接口注解完整
- [ ] 在线文档可访问

**依赖**: Task 3.x
**涉及文件**:
- `core-services/datasource-service/src/main/java/.../config/SwaggerConfig.java`

---

#### Task 7.3: 部署指南文档
**描述**: 编写详细的部署指南
**验收标准**:
- [ ] 环境要求说明
- [ ] 安装步骤
- [ ] 配置说明
- [ ] 故障排除指南

**依赖**: Task 7.1
**涉及文件**:
- `docs/DATASOURCE_DEPLOYMENT_GUIDE.md`

---

#### Task 7.4: 用户操作手册
**描述**: 编写用户操作手册
**验收标准**:
- [ ] 功能说明
- [ ] 操作步骤截图
- [ ] 常见问题解答

**依赖**: Task 6.x
**涉及文件**:
- `docs/DATASOURCE_USER_MANUAL.md`

---

## 5. 检查点

### Checkpoint 1: 基础设施完成
- [ ] 服务模块创建完成
- [ ] 数据库表创建完成
- [ ] 实体类开发完成
- [ ] 项目可正常启动

### Checkpoint 2: 插件架构完成
- [ ] 插件接口定义完成
- [ ] MySQL插件开发完成
- [ ] PostgreSQL插件开发完成
- [ ] Elasticsearch插件开发完成
- [ ] Kudu插件开发完成
- [ ] 所有插件单元测试通过

### Checkpoint 3: 核心功能完成
- [ ] 数据源配置管理API完成
- [ ] 连通性检测功能完成
- [ ] 连接稳定性保障完成
- [ ] 配置模板功能完成
- [ ] 导入导出功能完成

### Checkpoint 4: 安全机制完成
- [ ] 密码加密实现完成
- [ ] RBAC权限控制完成
- [ ] 审计日志完成

### Checkpoint 5: 前端完成
- [ ] 前端项目搭建完成
- [ ] 所有页面开发完成
- [ ] 响应式布局验证通过

### Checkpoint 6: 系统完成
- [ ] Docker部署配置完成
- [ ] API文档完成
- [ ] 部署指南完成
- [ ] 用户手册完成
- [ ] 端到端测试通过

## 6. 风险和缓解措施

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| Kudu客户端兼容性问题 | 高 | 提前验证Kudu Java客户端版本兼容性 |
| Elasticsearch版本差异大 | 中 | 为不同版本实现适配器模式 |
| 前端开发周期长 | 中 | 使用Ant Design Pro模板加速开发 |
| 安全机制复杂 | 高 | 参考Spring Security最佳实践 |
| 多租户数据隔离 | 高 | 使用MyBatis拦截器自动注入租户条件 |

## 7. 技术决策

### 7.1 插件化架构
- 使用Java SPI机制实现插件发现
- 每个插件独立打包，支持热加载
- 插件配置通过JSON Schema定义

### 7.2 连接池管理
- 使用HikariCP管理连接池
- 每个数据源独立连接池
- 连接池参数可配置

### 7.3 缓存策略
- Redis缓存数据源配置
- TTL设置为30分钟
- 配置变更时主动失效缓存

### 7.4 安全设计
- 前端使用CryptoJS进行AES加密
- 后端使用Java Cipher进行解密
- 密钥存储在环境变量中

## 8. 开始实施

准备好后，按照Phase顺序逐步实施。每个Phase完成后进行Checkpoint验证。
