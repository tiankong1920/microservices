# Inventory Management System — Skills 推荐配置

> 生成时间：2026-06-13（第 2 版，大幅扩充）
> 技术栈：Java 21 + Spring Boot 3.4.4 微服务后端 + React 19 + TypeScript 5.9 前端

---

## 📦 已安装 Skills（项目相关）

本项目目前已安装以下 **21 个 community skills**（来自 3 轮批量安装 + 2 次重试）。通用 skills（TDD、debugging、git-workflow、review 等）来自 addyosmani/agent-skills、gstack、superpowers、openspec、lark-* 等源，已预装。

| Skill | 来源 | 安装量 | 状态 | 备注 |
|-------|------|--------|------|------|
| `java-spring-boot` | pluginagentmarketplace/custom-plugin-java | 11.1K | ✅ 已安装 | |
| `react-dev` | softaworks/agent-toolkit | 3.6K | ✅ 已安装 | |
| `rest-api-design` | aj-geddes/useful-ai-prompts | 1.5K | ✅ 已安装 | |
| `kafka-development` | mindrally/skills | 635 | ✅ 已安装 | |
| `spring-boot-security-jwt` | giuseppe-trisciuoglio/developer-kit | 1.4K | ✅ 已安装 | 替代不可用的 `springboot-security` |
| `unit-test-service-layer` | giuseppe-trisciuoglio/developer-kit | 1.3K | ✅ 已安装 | |
| `unit-test-parameterized` | giuseppe-trisciuoglio/developer-kit | 1.2K | ✅ 已安装 | |
| `devops-engineer` | jeffallan/claude-skills | 6.1K | ✅ 已安装 | |
| `observability-designer` | alirezarezvani/claude-skills | 549 | ✅ 已安装 | ⚠️ Gen 评估 HIGH RISK |
| `java-quality` | claude-dev-suite/claude-dev-suite | 49 | ✅ 已安装 | |
| `sonarqube` | membranedev/application-skills | 55 | ✅ 已安装 | |
| `mapstruct` | claude-dev-suite/claude-dev-suite | 54 | ✅ 已安装 | |
| `lombok` | claude-dev-suite/claude-dev-suite | 41 | ✅ 已安装 | ⚠️ Snyk 评估 MEDIUM RISK |
| `spring-cache` | claude-dev-suite/claude-dev-suite | 32 | ✅ 已安装 | |
| `docker-compose-orchestration` | manutej/luxor-claude-marketplace | 1.7K | ✅ 已安装 | ⚠️ Snyk HIGH RISK, Socket 1 alert |
| `java-architect` | jeffallan/claude-skills | 3.8K | ✅ 已安装 | |
| `spring-boot-patterns` | decebals/claude-code-java | 118 | ✅ 已安装 | |
| `rabbitmq-expert` | martinholovsky/claude-skills-generator | 412 | ✅ 已安装 | ⚠️ **Gen 评估 CRITICAL RISK**, Snyk HIGH RISK |
| `kubernetes` | mindrally/skills | 635 | ✅ 已安装 | |
| `oauth2-authentication` | manutej/luxor-claude-marketplace | 328 | ✅ 已安装 | |
| `springboot-security` | affaan-m/everything-claude-code | 5.9K | ❌ 不可用 | 大仓库不包含此 skill |
| `react19-test-patterns` | github/awesome-copilot | 763 | ✅ 已安装 | 手动下载 SKILL.md（仓库克隆超时） |

> ⚠️ **安全说明**：`rabbitmq-expert` 因 Gen 评估被标为 CRITICAL RISK，`observability-designer` 为 HIGH RISK，`docker-compose-orchestration` 和 `rabbitmq-expert` 有 HIGH RISK Snyk 扫描结果。使用这些 skill 时建议审查其内容后再执行代码。

---

## 📋 已安装 Skills 总览（按领域）

### 🔙 后端 — Java / Spring Boot

| Skill | 来源 | 用途 |
|-------|------|------|
| ✅ `java-spring-boot` | pluginagentmarketplace/custom-plugin-java | Spring Boot 后端开发最佳实践 |
| ✅ `java-architect` | jeffallan/claude-skills | Java 架构设计、微服务场景 |
| ✅ `spring-boot-patterns` | decebals/claude-code-java | Spring Boot 设计模式 |
| ⬜ `java-jpa-hibernate` | pluginagentmarketplace/custom-plugin-java | JPA / Hibernate ORM（仍可安装） |
| ⬜ `java-microservices` | pluginagentmarketplace/custom-plugin-java | Java 微服务架构（仍可安装） |

### 🔐 安全 — Spring Security / JWT

| Skill | 来源 | 用途 |
|-------|------|------|
| ✅ `spring-boot-security-jwt` | giuseppe-trisciuoglio/developer-kit | Spring Boot Security + JWT |
| ✅ `oauth2-authentication` | manutej/luxor-claude-marketplace | OAuth2 认证 |
| ⬜ `jwt-security` | mindrally/skills | JWT 安全模式（仍可安装） |

### 🧪 后端测试（JUnit / Mockito）

| Skill | 来源 | 用途 |
|-------|------|------|
| ✅ `unit-test-service-layer` | giuseppe-trisciuoglio/developer-kit | Service 层单元测试 |
| ✅ `unit-test-parameterized` | giuseppe-trisciuoglio/developer-kit | 参数化测试 |
| ⬜ `java-testing` | pluginagentmarketplace/custom-plugin-java | Java 通用测试（仍可安装） |

### 🗄️ 基础设施 — Docker / 缓存 / 容器

| Skill | 来源 | 用途 |
|-------|------|------|
| ✅ `docker-compose-orchestration` ⚠️ | manutej/luxor-claude-marketplace | Docker Compose 编排 |
| ✅ `spring-cache` | claude-dev-suite/claude-dev-suite | Spring Cache / Redis 缓存 |
| ✅ `kubernetes` | mindrally/skills | Kubernetes 部署 |

### 🚀 DevOps / CI-CD

| Skill | 来源 | 用途 |
|-------|------|------|
| ✅ `devops-engineer` | jeffallan/claude-skills | DevOps 工程实践 |
| ✅ `sonarqube` | membranedev/application-skills | SonarQube 集成 |
| ⬜ `java-quality` | claude-dev-suite/claude-dev-suite | Java 代码质量（Checkstyle/PMD/SpotBugs） |

### 🎨 前端 — React / TypeScript / 测试

| Skill | 来源 | 用途 |
|-------|------|------|
| ✅ `react-dev` | softaworks/agent-toolkit | 通用 React 开发 |
| ✅ `react19-test-patterns` | github/awesome-copilot | React 19 测试模式（手动安装） |

### 📐 API 设计

| Skill | 来源 | 用途 |
|-------|------|------|
| ✅ `rest-api-design` | aj-geddes/useful-ai-prompts | REST API 设计最佳实践 |

### 🧰 Java 工具库

| Skill | 来源 | 用途 |
|-------|------|------|
| ✅ `mapstruct` | claude-dev-suite/claude-dev-suite | MapStruct 映射指南 |
| ✅ `lombok` ⚠️ | claude-dev-suite/claude-dev-suite | Lombok 使用（Snyk MEDIUM RISK） |

### 📊 监控与可观测性

| Skill | 来源 | 用途 |
|-------|------|------|
| ✅ `observability-designer` ⚠️ | alirezarezvani/claude-skills | 可观测性设计（Gen HIGH RISK） |

### 🔄 消息队列 / 事件驱动

| Skill | 来源 | 用途 |
|-------|------|------|
| ✅ `kafka-development` | mindrally/skills | Kafka 开发 |
| ✅ `rabbitmq-expert` ⚠️⚠️ | martinholovsky/claude-skills-generator | RabbitMQ 专家（**Gen CRITICAL RISK**, Snyk HIGH RISK） |

---

## 📥 安装命令汇总

### ✅ 已安装完成（无需重复安装）

```bash
# 基础（4 个）
npx skills add pluginagentmarketplace/custom-plugin-java@java-spring-boot -g -y
npx skills add softaworks/agent-toolkit@react-dev -g -y
npx skills add aj-geddes/useful-ai-prompts@rest-api-design -g -y
npx skills add mindrally/skills@kafka-development -g -y

# 安全（2 个）
npx skills add giuseppe-trisciuoglio/developer-kit@spring-boot-security-jwt -g -y
npx skills add manutej/luxor-claude-marketplace@oauth2-authentication -g -y

# 测试（2 个）
npx skills add giuseppe-trisciuoglio/developer-kit@unit-test-service-layer -g -y
npx skills add giuseppe-trisciuoglio/developer-kit@unit-test-parameterized -g -y

# Java 架构（3 个）
npx skills add jeffallan/claude-skills@java-architect -g -y
npx skills add jeffallan/claude-skills@devops-engineer -g -y
npx skills add decebals/claude-code-java@spring-boot-patterns -g -y

# 基础设施（4 个）
npx skills add manutej/luxor-claude-marketplace@docker-compose-orchestration -g -y
npx skills add mindrally/skills@kubernetes -g -y
npx skills add claude-dev-suite/claude-dev-suite@spring-cache -g -y
npx skills add membranedev/application-skills@sonarqube -g -y

# 工具库（2 个）
npx skills add claude-dev-suite/claude-dev-suite@mapstruct -g -y
npx skills add claude-dev-suite/claude-dev-suite@lombok -g -y

# 代码质量（1 个）
npx skills add claude-dev-suite/claude-dev-suite@java-quality -g -y

# 可观测性（1 个）
npx skills add alirezarezvani/claude-skills@observability-designer -g -y

# 消息队列（1 个）
npx skills add martinholovsky/claude-skills-generator@rabbitmq-expert -g -y
```

### ⬜ 仍可安装（按需）

```bash
# JPA/Hibernate ORM
npx skills add pluginagentmarketplace/custom-plugin-java@java-jpa-hibernate -g -y

# Java 微服务架构
npx skills add pluginagentmarketplace/custom-plugin-java@java-microservices -g -y

# Java 通用测试
npx skills add pluginagentmarketplace/custom-plugin-java@java-testing -g -y
```

---

## 🔍 如再需查找更多 Skills

```bash
# 搜索更多技能
npx skills find <关键词>

# 浏览所有已安装技能
npx skills list -g

# 更新已安装技能
npx skills update

# 在线浏览技能市场
# https://skills.sh/
```
