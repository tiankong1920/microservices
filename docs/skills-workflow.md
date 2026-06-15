# Skills Workflow

> 如何有效使用已安装的 Agent Skills 进行日常开发
> 适用：Inventory Management System（Java 21 + Spring Boot 微服务 + React 19 前端）

---

## 工作流概览

```
任务 → 加载 Skill → 遵循 Skill 指南 → 实现 → 验证
```

Agent Skills 提供**领域上下文**和**最佳实践**，不是替代品。每个 skill 都是"专家指导"，在你开始编码前提供知识。

---

## 核心原则

1. **先加载，再动手** - 任何与 skill 相关的任务，先用 `skill()` 工具加载
2. **1% 法则** - 只要觉得有 1% 的可能某个 skill 适用，就加载它
3. **skill 是 MAP，不是代码** - skill 提供方向，具体实现由你决定
4. **安全优先** - 高风险 skill（rabbitmq-expert 等）使用时先审查内容

---

## 技能选择速查表

### 按任务类型

| 任务场景 | 优先加载的 Skill | 来源 |
|---------|-----------------|------|
| **新建 Spring Boot 微服务** | `java-spring-boot` | pluginagentmarketplace |
| **设计 REST API** | `rest-api-design` | aj-geddes |
| **添加 JWT/Spring Security** | `spring-boot-security-jwt` | giuseppe-trisciuoglio |
| **编写 Service 层测试** | `unit-test-service-layer` | giuseppe-trisciuoglio |
| **编写参数化测试** | `unit-test-parameterized` | giuseppe-trisciuoglio |
| **Kafka 集成开发** | `kafka-development` | mindrally |
| **Docker Compose 编排** | `docker-compose-orchestration` | manutej |
| **Spring Cache / Redis** | `spring-cache` | claude-dev-suite |
| **React 前端开发** | `react-dev` | softaworks |
| **MapStruct 实体映射** | `mapstruct` | claude-dev-suite |
| **Lombok 使用** | `lombok` | claude-dev-suite |
| **DevOps / CI-CD 实践** | `devops-engineer` | jeffallan |
| **RabbitMQ 集成** | `rabbitmq-expert` | martinholovsky |
| **Kubernetes 部署** | `kubernetes` | mindrally |
| **OAuth2 认证** | `oauth2-authentication` | manutej |
| **可观测性设计** | `observability-designer` | alirezarezvani |
| **SonarQube 代码分析** | `sonarqube` | membranedev |
| **Java 代码质量** | `java-quality` | claude-dev-suite |
| **Java 架构设计** | `java-architect` | jeffallan |
| **Spring Boot 设计模式** | `spring-boot-patterns` | decebals |

### 按技术栈

```
后端开发
  ├── microservice 创建 → java-spring-boot
  ├── REST API 设计  → rest-api-design
  ├── 安全认证       → spring-boot-security-jwt, oauth2-authentication
  ├── 数据映射       → mapstruct
  ├── 工具库        → lombok
  ├── 缓存          → spring-cache
  ├── 消息队列       → kafka-development, rabbitmq-expert
  └── 测试          → unit-test-service-layer, unit-test-parameterized

前端开发
  └── React 开发    → react-dev

基础设施
  ├── 容器编排       → docker-compose-orchestration
  ├── 容器编排       → kubernetes
  └── 可观测性       → observability-designer

质量控制
  ├── 代码质量       → java-quality
  ├── 静态分析       → sonarqube
  └── 架构审查       → java-architect
```

---

## 使用方式

### 1. 加载 Skill

在实现之前，通过 `skill()` 工具加载对应 skill：

```
调用: name="java-spring-boot"
```

Skill 加载后，你会获得该领域的**最佳实践**、**模式**和**注意事项**。

### 2. 多 Skill 串联

复杂任务可能需要多个 skill。按顺序加载：

```
task: 添加 JWT 认证到 product-service

1. skill(name="spring-boot-security-jwt")  → JWT 配置指南
2. skill(name="java-spring-boot")          → Spring Boot 项目结构
3. skill(name="unit-test-service-layer")   → 编写测试
```

### 3. 与通用 Skills 配合

通用 skills（来自 addyosmani/agent-skills）提供**流程**支持：

```
1. skill(name="spec-driven-development")    → 写 spec
2. skill(name="planning-and-task-breakdown") → 拆任务
3. skill(name="incremental-implementation")  → 增量实现
4. skill(name="code-review-and-quality")     → 代码审查

→ 各步骤中加载对应领域 skill
```

---

## 最佳实践

### DO

- **实现前加载** skill，获取领域上下文
- **跨领域任务**加载多个 skill，按依赖顺序
- **不熟悉的库**优先找对应 skill
- **高风险 skill**（有 ⚠️ 标记）审查后再执行

### DON'T

- ❌ 不要在有 skill 可用时自己猜测 API
- ❌ 不要一次性加载所有 skill（上下文浪费）
- ❌ 不要完全照搬 skill 内容，适配项目实际代码风格
- ❌ 不要跳过 skill 直接问"如何做 X"

### 示例

```
✅ 好做法：

User: "给 product-service 添加 Redis 缓存"
Agent: 
  1. skill(name="spring-cache")       → 获取 Spring Cache 最佳实践
  2. skill(name="java-spring-boot")   → 获取项目结构参考
  3. 实现缓存逻辑
  4. skill(name="unit-test-service-layer") → 编写缓存测试

❌ 不好做法：

User: "给 product-service 添加 Redis 缓存"
Agent: 凭印象写 RedisTemplate 配置（可能过时或有遗漏）
```

---

## 安全说明

以下 skill 有安全风险标记，使用时需注意：

| Skill | 风险等级 | 说明 |
|-------|---------|------|
| `rabbitmq-expert` | 🔴 CRITICAL | Gen 评估风险，Snyk HIGH RISK |
| `observability-designer` | 🟠 HIGH | Gen 评估高风险 |
| `docker-compose-orchestration` | 🟠 HIGH | Snyk HIGH RISK，Socket 1 alert |
| `lombok` | 🟡 MEDIUM | Snyk 中等风险 |

使用这些 skill 时，**先审查其 SKILL.md 内容**，确认没有可疑指令后再执行。

---

## 维护

```bash
# 查看所有已安装 skill
npx skills list -g

# 更新已安装 skill
npx skills update

# 搜索新 skill
npx skills find <关键词>

# 安装新 skill
npx skills add <owner/repo@skill> -g -y

# 在线浏览市场
# https://skills.sh/
```

---

## 参考

- 完整安装清单：[docs/skills.md](file:///E:/101/docs/skills.md)
- 项目根 AGENTS.md：[AGENTS.md](file:///E:/101/AGENTS.md)
