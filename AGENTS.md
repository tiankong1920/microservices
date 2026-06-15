# Inventory Management System — Agent Instructions

Mono-repo: Java 21 Gradle microservices backend + React/Vite frontend.

> **This repo is not git-initialized.** `ocr review` and other git-based tools won't work until `git init` + initial commit.

## Where to Run Commands

| Area | Working Directory | Entry Point |
|------|-------------------|-------------|
| **Backend** | `microservices/` | `gradlew` in `microservices/` |
| **Frontend** | `web-frontend/` | `package.json` |
| **Infrastructure** | `microservices/` | `docker-compose.yml` |

> **Important**: `project-root/` exists under `microservices/` but is **empty** (placeholder for future refactoring). The actual Gradle wrapper, settings, and build scripts live directly in `microservices/`. Always run backend commands from `microservices/`.

---

## Backend Commands (run from `microservices/`)

```bash
# Full build (exclude tests for speed)
./gradlew build -x test

# Full build + quality gates
./gradlew buildWithQuality

# Run all tests (unit tests only — integration/slow/e2e excluded by tag)
./gradlew test

# Run single test class
./gradlew :core-services:product-service:test --tests "*ProductServiceImplTest"

# Run single test method
./gradlew :core-services:product-service:test --tests "com.inventory.productservice.service.impl.ProductServiceImplTest.testGetProductById"

# Skip all tests via property
./gradlew build -PskipTests

# Code quality only (Checkstyle + PMD + SpotBugs)
./gradlew checkQuality

# JaCoCo coverage check (tests must have run first)
./gradlew jacocoCoverageCheck

# Build a specific service
./gradlew :core-services:product-service:build

# SonarQube analysis (requires scanner CLI at e:\101\sonar-scanner-*)
./gradlew sonar

# Tests + SonarQube
./gradlew sonarWithCoverage

# Clean everything (wrapper + Gradle caches)
./gradlew cleanAll

# View project info
./gradlew projectInfo
```

**Gradle**: 9.4.0, Kotlin DSL, 25 subprojects via `settings.gradle.kts`. JVM: `-Xmx6g -Xms2g`, parallel=true, workers.max=12, daemon idle=3600s, G1GC.

---

## Port Map

| Service | Port | Service | Port |
|---------|------|---------|------|
| gateway | 8080 | supplier | 8087 |
| product | 8081 | business-partner | 8088 |
| order | 8082 | admin | 8091 |
| inventory | 8083 | finance | 8092 |
| sales | 8084 | auth | 8093 |
| procurement | 8085 | report | 8094 |
| customer | 8086 | config | 8888 |
| registry | 8761 | Nacos | 8848 |

Infrastructure: PostgreSQL 5432, Redis 7379→6379, Nacos 8848, Kafka 9092.

---

## Frontend Commands (run in `web-frontend/`)

```bash
npm run dev          # Dev server (Vite HMR, port 5173)
npm run build        # Production build
npm run lint         # ESLint check
npm run preview      # Vite preview
npm run test         # Vitest (jsdom)
npm run test:watch   # Vitest watch mode
npm run test:coverage # Vitest + coverage (v8)
```

**Stack**: React 19, TypeScript 5.9, Vite 8 (`vite.config.js` — JS not TS), MUI 7, React Router 7, Axios, ECharts 6, Recharts.

**Config quirks**:
- `vite.config.js` (not .ts) — separate `vitest.config.ts` for test configuration
- TypeScript: `noImplicitAny: false`, `noUnusedLocals: false`, `noUnusedParameters: false` — lenient by design
- Path alias: `@/` maps to `src/`
- Production build strips `console` and `debugger` via esbuild
- Test files: `src/**/*.{test,spec}.{ts,tsx}` with jsdom environment
- Test setup: `src/test/setup.ts`, timeout=10000ms, coverage=v8
- API base URL: `VITE_API_BASE_URL` env var, defaults to `http://localhost:8080/api/v1`
- No code formatter (Prettier) — ESLint-only via flat config (`eslint.config.js`)
- ESLint: `@typescript-eslint/no-explicit-any` is **warn** (not error), `no-unused-vars` is error for TS
- **Dockerfile**: nginx-based multi-stage build, served on port 80

---

## CI/CD

- **Only frontend CI exists** (`web-frontend/.github/workflows/`): Snyk security scans (dependency + code + container) + Docker build to ghcr.io on push/PR to `main`/`develop`
- **No backend CI workflows** detected — backend builds are local-only or Jenkins-based (see `microservices/Jenkinsfile`)
- Snyk token via `SNYK_TOKEN` secret, severity threshold=medium, auto-creates GitHub issue on failure for high/critical

---

## Backend Conventions (summary — see [`microservices/AGENTS.md`](file:///E:/101/microservices/AGENTS.md) for full detail)

- **Package**: `com.inventory.<service>.<layer>` (controller/service/impl/repository/entity/dto/exception/config)
- **Naming**: Repository = `I` prefix (`IProductRepository`), Mock = `mock` prefix, Interface = no `I` prefix for service interfaces, Test methods = `test` prefix
- **Mockito**: NO wildcard imports. Explicit static imports only: `import static org.mockito.Mockito.when;`
- **Exceptions**: Extend `BaseApplicationException` with code format `AAA-XX-XXX` (SYS/VAL/BIZ/AUTH)
- **Method params**: Always `final`
- **Lombok**: `@Slf4j`, `@RequiredArgsConstructor`, `@Data`, `@Builder` — applied via `io.freefair.lombok` plugin
- **API**: `/api/<plural-resource>`, return `ResponseEntity<T>`, validate with `@Valid`
- **Quality thresholds**: Line 120 chars max, method 150 lines max, cyclomatic ≤15, line coverage ≥80%, branch coverage ≥70%
- **Forbidden**: `System.out.println`, wildcard Mockito imports, `@InjectMocks`, empty catch, star imports, tabs, `\r`
- **EditorConfig** at `microservices/.editorconfig` — LF, UTF-8, trim trailing whitespace (except .md)

**Tech Stack**: Java 21, Spring Boot 3.4.4, Spring Cloud 2024.0.2, Spring Cloud Alibaba 2023.0.1.0, Gradle 9.5.1 (Kotlin DSL), PostgreSQL, Redis, Nacos, Kafka, MapStruct 1.6.3, Lombok.

**Quality**: Checkstyle 13.3.0, PMD, SpotBugs 4.9.7, JaCoCo 0.8.12, SonarQube, OWASP Dependency-Check 9.0.9, Snyk.

**Subprojects** (25 total): `common`, 12 `core-services/*` (product, order, inventory, sales, procurement, customer, supplier, business-partner, datasource, template, mall, invoice), 8 `support-services/*` (admin, auth, finance, gateway, config, config-service-simple, registry, report), `monitoring-core`, `monitoring-spring-boot-starter`, `monitoring`, `cross-service-tests`.

---

## Infrastructure

```bash
# Start all services (PostgreSQL, Redis, Nacos, Kafka)
docker compose up -d            # from microservices/

# Individual compose files also available:
docker compose -f docker-compose.postgres.yml up -d
docker compose -f docker-compose.dev.yml up -d
```

**Env**: [`microservices/.env.example`](file:///E:/101/microservices/.env.example) → copy to `.env`.

**Key env vars**: `POSTGRES_HOST`, `POSTGRES_PASSWORD=1234`, `REDIS_HOST`, `NACOS_SERVER_ADDR`, `JWT_SECRET` (≥256 bits), `KAFKA_BOOTSTRAP_SERVERS`.

---

## Documentation

Extensive docs in [`docs/`](file:///E:/101/docs/) — API design, testing strategy, security config, code review checklists, architecture design, and more. Read on demand with `@docs/<filename>`.

---

## Root Directories & Files (not part of the application build)

| Path | What it is |
|------|------------|
| `src/` | Legacy standalone Java source (stale, not in microservices build) |
| `microservices_backup/` | Snapshot of microservices dir — do not edit |
| `microservices/project-root/` | Empty placeholder for future refactoring |
| `node_modules/`, `package.json` | AI/agent tools (`@codebuff/sdk`, `oh-my-openagent-windows-x64`) — not the application |
| `.omo/`, `.omx/`, `.qoder/`, `.trae/`, `.workbuddy/`, `.superpowers/`, `.superpowers_official/`, `.team/` | AI tool config directories — ignore unless working on AI agent configuration |
| `Sirius/`, `SiriusScan/`, `SiriusSecurity/` | Security/static analysis output — read-only artifacts |
| `scripts/` | Misc helper scripts |
| `backups/` | Old backups |
| `openspec/` | OpenSpec specification documents |
| `build_output.txt`, `compile_check.txt`, `flyway_check.txt` | Analysis output logs — read-only artifacts |
| `*.md` at root (DEPENDENCY_*, DEFECT_FIX_*, IMPROVEMENT_*, NAMING_CONVENTION_*, etc.) | Audit/review reports — read-only artifacts |

---

---

## Project Skills (OpenCode `skill` Tool)

32 project-specific skills are registered in [`.opencode/skills/`](file:///E:/101/.opencode/skills/), covering backend, frontend, testing, and infrastructure domains.

### How They Load

- OpenCode scans `config.skills.paths` at **session start** to build the `skill()` tool index
- `~\.agents\skills\` (user-global, ~75 built-in skills) is always included
- `E:\101\.opencode\skills\` (project-level, 32 skills) is included for this project
- **Skills installed mid-session won't appear until next session** — the index is immutable once built

### Key Skills for This Project

**Backend (Java/Spring):**
- `java-spring-boot` — Spring Boot project setup and conventions
- `rest-api-design` — RESTful API design patterns
- `spring-boot-security-jwt` — JWT authentication for Spring Boot
- `spring-boot-patterns` — Common Spring Boot patterns
- `kafka-development` — Apache Kafka integration
- `rabbitmq-expert` — RabbitMQ messaging
- `mapstruct` — Java bean mapping
- `lombok` — Lombok annotations
- `spring-cache` — Caching with Spring

**Quality:**
- `java-quality` — Java code quality tools
- `sonarqube` — SonarQube analysis
- `unit-test-service-layer` — Service layer testing
- `unit-test-parameterized` — Parameterized tests

**Frontend:**
- `react-dev` — React development patterns
- `react19-test-patterns` — React 19 testing patterns

**Infrastructure:**
- `kubernetes` — K8s deployment
- `docker-compose-orchestration` — Docker Compose setups
- `devops-engineer` — DevOps automation
- `oauth2-authentication` — OAuth2 flows

**Architecture:**
- `java-architect` — Java architecture design
- `observability-designer` — Observability patterns

### Reference Docs

- [`docs/skills.md`](file:///E:/101/docs/skills.md) — Full skills inventory with install commands
- [`docs/skills-workflow.md`](file:///E:/101/docs/skills-workflow.md) — Skills usage workflow guide

---

## ECC (Everything Claude Code) 集成

[ECCd `ecc-universal` (v2.0.0) npm 包已安装，作为跨平台 agent 编排增强系统。

**安装路径**: `node_modules/ecc-universal/`

**集成方式**:
| 机制 | 路径 | 说明 |
|------|------|------|
| Plugin | `.opencode/opencode.json` | `"plugin": ["ecc-universal"]` — 加载 ECC agents/commands/hooks |
| Skills | `.opencode/opencode.json` | `"skills.paths": ["node_modules/ecc-universal/skills"]` — 236 个技能 |
| Commands | `node_modules/ecc-universal/commands/` | 82 个命令文件 (plan/tdd/code-review/security/build-fix 等) |
| Agents | `node_modules/ecc-universal/.opencode/opencode.json` | 25+ subagents (planner/architect/reviewer/security/tdd/build-resolver/...) |

**注意**: Skills/agents 索引在会话启动时建立，`plugin` 和 `skills.paths` 配置在新会话中生效。当前会话中 ECC 技能不可用，重启后自动加载。

**ECCd 包结构** (`node_modules/ecc-universal/`):
- `.opencode/` — OpenCode 插件完整配置
- `skills/` — 236 个技能目录 (springboot-patterns/agentic-engineering/tdd-workflow/seo 等)
- `commands/` — 82 个命令
- `agents/` — agent 定义
- `hooks/`, `rules/`, `mcp-configs/`, `manifests/`, `schemas/` — 配套配置

**平台支持**: 同时包含 `.claude-plugin/`, `.codex/`, `.cursor/`, `.gemini/`, `.qwen/`, `.zed/` 多平台配置。

---

## Common Issues

- **Not a git repo** — No `.git` at any level. `git status/diff/log` and `ocr review` will fail. Run `git init` + commit if needed.
- **Gradle wrapper in `microservices/` root**: The `gradlew`, `gradlew.bat`, and `gradle/` wrapper live directly in `microservices/`. The `project-root/` directory is a placeholder — it exists but is empty.
- **Port conflicts**: Check 5432 (PG), 7379→6379 (Redis), 8848 (Nacos), 8080-8095 (services), 8761 (registry), 8888 (config), 9092 (Kafka).
- **Build failures**: Run with `--stacktrace`. Check `.gradle/` cache corruption. Try `./gradlew cleanAll build`.
- **SonarQube scanner**: Installed at `E:\101\sonar-scanner-7.3.0.5189-windows-x64`. Run `./gradlew sonarWithCoverage`.
- **`skipTests` property**: Tests can be skipped with `-PskipTests`.
- **`microservices/build.gradle.kts`**: The single root build script, 505 lines, lives directly in `microservices/`. No duplicate in `project-root/`.
- **Maven mirrors**: `settings.gradle.kts` includes `aliyun` and `jitpack` mirrors alongside Maven Central.
- **Nacos dependency**: Backend services require Nacos at :8848 for config/service discovery. Without it, services fail to start.
