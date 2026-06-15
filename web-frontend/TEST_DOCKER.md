# Frontend Docker Test

在 Docker 容器中运行前端 Vitest 测试，确保测试环境一致。

## 快速开始

```bash
# Windows
test-docker.bat

# 详细模式
test-docker.bat --verbose

# 单个文件
test-docker.bat --file ProductManagement.test.tsx

# 覆盖率
test-docker.bat --coverage

# 重建镜像
test-docker.bat --rebuild
```

## 直接使用 Docker

```bash
# 构建测试镜像
docker build -f Dockerfile.test -t web-frontend-test:latest .

# 运行测试
docker run --rm \
  -v "${PWD}:/app" \
  -v /app/node_modules \
  -e CI=true \
  -e NODE_ENV=test \
  web-frontend-test:latest
```

## Docker Compose

```bash
docker compose -f docker-compose.test.yml up --abort-on-container-exit
```

## CI/CD 集成

参考 `.github/workflows/frontend-docker-test.yml`：
- 推送到 main/develop 自动触发
- PR 自动触发
- 工作流手动触发（workflow_dispatch）

## 镜像说明

基础镜像：`docker.m.daocloud.io/library/node:20-alpine`
（使用国内镜像，绕过 Docker Hub 代理连接问题）

npm 镜像：`https://registry.npmmirror.com`

## 测试结果对比

| 环境 | 测试文件 | 测试用例 | 耗时 |
|------|---------|---------|------|
| 本机 (Windows) | 16/16 | 49/49 | ~482s |
| Docker 容器 | 16/16 | 49/49 | ~297s |

容器比本机快 38%（Linux 上的 esbuild 性能更好）。
