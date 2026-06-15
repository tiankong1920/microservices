# 开发指南 (Development Guide)

## 快速开始

### 环境要求

| 工具 | 版本 | 说明 |
|------|------|------|
| Java | 21+ | LTS版本 |
| Gradle | 8.14.4 | 通过Wrapper自动下载 |
| Docker | 24+ | 容器化开发 |
| Docker Compose | 2.x | 服务编排 |
| Git | 2.x | 版本控制 |

### 初始化项目

```bash
# 克隆项目
git clone https://github.com/your-org/inventory-management-system.git
cd inventory-management-system

# 配置环境变量
cp .env.example .env
# 编辑 .env 文件，填入实际配置

# 启动开发环境
docker-compose -f docker-compose.dev.yml up -d

# 构建项目
./gradlew build

# 运行测试
./gradlew test
```

## 开发工作流

### 分支策略

```
main (生产分支)
  └── develop (开发分支)
        ├── feature/* (功能分支)
        ├── bugfix/* (修复分支)
        └── release/* (发布分支)
```

### 提交规范

使用 Conventional Commits 规范：

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**类型说明：**
- `feat`: 新功能
- `fix`: Bug修复
- `docs`: 文档更新
- `style`: 代码格式
- `refactor`: 重构
- `test`: 测试相关
- `build`: 构建相关
- `ci`: CI/CD配置
- `chore`: 其他修改

**示例：**
```bash
feat(product): add product search functionality
fix(order): resolve order status update issue
docs(readme): update installation instructions
```

### 开发流程

1. **创建功能分支**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/your-feature
   ```

2. **开发与测试**
   ```bash
   # 编写代码
   # 运行测试
   ./gradlew test
   
   # 代码格式化
   ./gradlew spotlessApply
   
   # 代码检查
   ./gradlew check
   ```

3. **提交代码**
   ```bash
   git add .
   git commit -m "feat(module): your changes"
   ```

4. **推送与创建PR**
   ```bash
   git push origin feature/your-feature
   # 在GitHub上创建Pull Request
   ```

## 常用命令

### Gradle命令

```bash
# 构建项目
./gradlew build

# 清理构建
./gradlew clean

# 运行测试
./gradlew test

# 运行特定模块测试
./gradlew :core-services:product-service:test

# 代码格式化
./gradlew spotlessApply

# 代码检查
./gradlew checkstyleMain pmdMain spotbugsMain

# 生成测试覆盖率报告
./gradlew jacocoTestReport

# 查看依赖树
./gradlew dependencies

# 检查依赖更新
./gradlew dependencyUpdates

# 运行应用
./gradlew :core-services:product-service:bootRun
```

### Docker命令

```bash
# 启动开发环境
docker-compose -f docker-compose.dev.yml up -d

# 查看日志
docker-compose -f docker-compose.dev.yml logs -f

# 停止环境
docker-compose -f docker-compose.dev.yml down

# 重建服务
docker-compose -f docker-compose.dev.yml up -d --build
```

## 代码规范

### Java代码规范

- 遵循 Google Java Style Guide
- 最大行长度：120字符
- 使用4空格缩进
- 类和方法必须有Javadoc注释

### 代码质量工具

| 工具 | 配置文件 | 说明 |
|------|----------|------|
| Checkstyle | checkstyle.xml | 代码风格检查 |
| PMD | pmd.xml | 静态代码分析 |
| SpotBugs | spotbugs-exclude.xml | Bug检测 |
| Spotless | build.gradle.kts | 代码格式化 |

### 测试规范

- 单元测试覆盖率：≥80%
- 测试类命名：`*Test.java`
- 集成测试命名：`*IT.java`
- 使用JUnit 5 + Mockito

## 调试指南

### 本地调试

1. **IDE配置**
   - 导入项目为Gradle项目
   - 配置Java 21 SDK
   - 启用注解处理器

2. **远程调试**
   ```bash
   # 启动应用时添加调试参数
   ./gradlew bootRun -Dagentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
   ```

3. **日志调试**
   ```yaml
   # application-dev.yml
   logging:
     level:
       root: INFO
       com.inventory: DEBUG
   ```

### 常见问题

1. **构建失败**
   ```bash
   # 清理缓存重新构建
   ./gradlew clean build --no-daemon --no-build-cache
   ```

2. **依赖下载失败**
   ```bash
   # 刷新依赖
   ./gradlew build --refresh-dependencies
   ```

3. **端口冲突**
   ```bash
   # 检查端口占用
   netstat -tulpn | grep :8080
   
   # 终止进程
   kill -9 <PID>
   ```

## 性能优化

### 构建优化

- 启用并行构建：`org.gradle.parallel=true`
- 启用构建缓存：`org.gradle.caching=true`
- 启用配置缓存：`org.gradle.configuration-cache=true`
- 增加内存：`org.gradle.jvmargs=-Xmx6g`

### 开发效率

- 使用Gradle Daemon
- 配置IDE自动格式化
- 使用Live Reload
- 配置本地缓存

## 相关链接

- [Spring Boot文档](https://docs.spring.io/spring-boot/docs/4.0.2/reference/html/)
- [Spring Cloud文档](https://docs.spring.io/spring-cloud/docs/2025.1.0/reference/html/)
- [Gradle用户指南](https://docs.gradle.org/8.14.4/userguide/userguide.html)
