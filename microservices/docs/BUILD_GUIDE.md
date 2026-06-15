# 项目编译使用指南

## 版本
- 版本：3.0.0
- 创建日期：2026-01-30
- 最后更新：2026-01-30

## 1. 文档概述

### 1.1 目的

本指南提供了库存管理系统的详细编译和构建指南，包括环境要求、编译流程、构建命令、常见问题和优化建议，旨在帮助开发人员快速掌握项目的编译和构建方法，确保项目能够顺利编译和部署。

### 1.2 适用范围

- 项目编译和构建
- 开发环境配置
- 构建工具使用
- 编译优化
- 构建故障排查

### 1.3 目标读者

- 开发人员
- 系统管理员
- DevOps工程师
- 测试人员

## 2. 编译环境要求

### 2.1 系统要求

| 要求 | 最低配置 | 推荐配置 |
|------|---------|---------|
| 操作系统 | Windows 10+ / Linux 4.x+ / macOS 12+ | Windows 11 / Linux 5.x+ / macOS 13+ |
| CPU | 2核 | 4核或以上 |
| 内存 | 8GB | 16GB或以上 |
| 磁盘 | 50GB | 100GB或以上 |
| 网络 | 100Mbps | 1Gbps |

### 2.2 工具要求

| 工具 | 版本 | 用途 |
|------|------|------|
| Java | 21 LTS | 项目编译和运行 |
| Gradle | 9.4.0 | 项目构建和依赖管理 |
| Git | 2.30.x+ | 版本控制 |
| Docker | 20.10.x+ | 容器化部署 |
| Docker Compose | 2.10.x+ | 多容器部署 |

### 2.3 环境变量配置

| 环境变量 | 说明 | 示例值 |
|----------|------|--------|
| JAVA_HOME | Java安装路径 | C:\Program Files\Eclipse Adoptium\jdk-21.0.5.10-hotspot |
| PATH | 系统路径 | %PATH%;%JAVA_HOME%\bin |
| DOCKER_HOST | Docker主机地址 | tcp://localhost:2375 |

## 3. 编译流程

### 3.1 完整编译流程

```
1. 克隆代码 → 2. 配置环境 → 3. 下载依赖 → 4. 编译源代码 → 5. 运行单元测试 → 6. 打包项目 → 7. 运行集成测试 → 8. 生成报告
```

### 3.2 详细步骤

#### 3.2.1 克隆代码

```bash
# 克隆代码仓库
git clone https://github.com/your-username/inventory-management-system.git

# 进入项目目录
cd inventory-management-system
```

#### 3.2.2 配置环境

```bash
# 检查Java版本
java -version

# 检查Gradle版本
./gradlew --version

# 检查Git版本
git --version

# 检查Docker版本
docker --version
```

#### 3.2.3 下载依赖

```bash
# 下载项目依赖
./gradlew dependencies

# 或使用以下命令，同时验证依赖
./gradlew dependencies --configuration compileClasspath
```

#### 3.2.4 编译源代码

```bash
# 编译整个项目
./gradlew compileJava

# 编译特定模块
./gradlew :core-services:product-service:compileJava

# 跳过测试编译
./gradlew compileJava -x test

# 跳过代码质量检查编译
./gradlew compileJava -x checkstyleMain -x pmdMain -x spotbugsMain
```

#### 3.2.5 运行单元测试

```bash
# 运行所有单元测试
./gradlew test

# 运行特定模块的测试
./gradlew :core-services:product-service:test

# 运行特定测试类
./gradlew test --tests ProductServiceTest

# 运行特定测试方法
./gradlew test --tests ProductServiceTest.testGetProductById
```

#### 3.2.6 打包项目

```bash
# 打包整个项目
./gradlew build

# 打包特定模块
./gradlew :core-services:product-service:build

# 跳过测试打包
./gradlew build -x test

# 跳过代码质量检查打包
./gradlew build -x test -x checkstyleMain -x pmdMain -x spotbugsMain
```

#### 3.2.7 运行集成测试

```bash
# 运行集成测试
./gradlew integrationTest

# 运行特定模块的集成测试
./gradlew :core-services:product-service:integrationTest

# 仅运行集成测试（跳过单元测试）
./gradlew integrationTest -x test
```

#### 3.2.8 生成报告

```bash
# 生成项目报告
./gradlew site

# 生成代码覆盖率报告
./gradlew jacocoTestReport

# 生成依赖报告
./gradlew dependencies
```

## 4. 构建命令

### 4.1 基本构建命令

| 命令 | 功能 |
|------|------|
| `./gradlew compileJava` | 编译项目源代码 |
| `./gradlew test` | 运行单元测试 |
| `./gradlew build` | 打包项目 |
| `./gradlew integrationTest` | 运行集成测试 |
| `./gradlew publishToMavenLocal` | 安装到本地仓库 |
| `./gradlew clean` | 清理构建产物 |
| `./gradlew site` | 生成项目报告 |

### 4.2 高级构建命令

#### 4.2.1 并行构建

```bash
# 启用并行构建（每个CPU核心一个线程）
mvn compile -T 1C

# 启用并行构建（指定线程数）
./gradlew compileJava --parallel --max-workers=4
```

#### 4.2.2 增量构建

```bash
# 启用增量构建（Gradle默认支持增量构建）
./gradlew compileJava --parallel
```

#### 4.2.3 多环境构建

```bash
# 开发环境构建
./gradlew build -Dspring.profiles.active=dev

# 测试环境构建
./gradlew build -Dspring.profiles.active=test

# 生产环境构建
./gradlew build -Dspring.profiles.active=prod
```

#### 4.2.4 构建缓存

```bash
# 启用构建缓存（Gradle默认支持构建缓存）
./gradlew build --build-cache

# 清理构建缓存
./gradlew clean --build-cache
```

### 4.3 模块构建命令

#### 4.3.1 构建核心服务

```bash
# 构建所有核心服务
./gradlew :core-services:build

# 构建特定核心服务
./gradlew :core-services:product-service:build :core-services:order-service:build
```

#### 4.3.2 构建支持服务

```bash
# 构建所有支持服务
./gradlew :support-services:build

# 构建特定支持服务
./gradlew :support-services:auth-service:build :support-services:gateway-service:build
```

## 5. 编译优化

### 5.1 Gradle配置优化

#### 5.1.1 配置文件优化

编辑 `gradle.properties` 文件，添加以下配置：

```properties
# Gradle配置优化
org.gradle.jvmargs=-Xms1024m -Xmx2048m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:+UseG1GC -XX:+UseStringDeduplication
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
org.gradle.daemon=true

# 编译配置
sourceCompatibility=21
targetCompatibility=21
```

#### 5.1.2 JVM参数优化

```bash
# 设置Gradle JVM参数
export GRADLE_OPTS="-Xms1024m -Xmx2048m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:+UseG1GC -XX:+UseStringDeduplication"

# 或在命令中指定
./gradlew compileJava -Dorg.gradle.jvmargs="-Xms1024m -Xmx2048m"
```

### 5.2 编译流程优化

#### 5.2.1 跳过不必要的任务

```bash
# 跳过代码质量检查
mvn compile -Dcheckstyle.skip -Dpmd.skip -Dspotbugs.skip

# 跳过测试
mvn compile -DskipTests

# 跳过文档生成
mvn compile -Dmaven.javadoc.skip=true

# 跳过资源处理
mvn compile -Dmaven.resources.skip=true
```

#### 5.2.2 使用Maven Wrapper

```bash
# 使用Maven Wrapper编译项目
./gradlew compile

# Windows系统
gradlew.bat compile
```

### 5.3 依赖管理优化

#### 5.3.1 依赖版本锁定

```kotlin
// 在libs.versions.toml中锁定版本
[versions]
spring-boot = "4.0.1"
spring-cloud = "2025.0.0"

[libraries]
spring-boot-dependencies = { module = "org.springframework.boot:spring-boot-dependencies", version.ref = "spring-boot" }
```

#### 5.3.2 依赖排除

```kotlin
// 排除冲突依赖
implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client") {
    exclude(group = "org.apache.httpcomponents", module = "httpclient")
}
```

## 6. 常见编译问题

### 6.1 依赖冲突

#### 6.1.1 问题现象

```
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':compileJava'.
> Could not resolve dependencies for project com.inventory:auth-service:jar:3.0.0.
   > Dependency convergence error for com.google.guava:guava:33.3.0-jre paths to dependency are:
```

#### 6.1.2 解决方案

```bash
# 查看依赖树，定位冲突
mvn dependency:tree -Dverbose

# 排除冲突依赖
./gradlew dependencies --configuration compileClasspath

# 在build.gradle.kts中排除冲突
```

### 6.2 编译错误

#### 6.2.1 问题现象

```
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':compileJava'.
> Compilation failed
  /src/main/java/com/inventory/authservice/service/impl/UserDetailsServiceImpl.java:[45,30] error: cannot find symbol
```

#### 6.2.2 解决方案

1. 检查代码中的语法错误
2. 检查依赖是否正确引入
3. 检查类路径是否配置正确
4. 检查Java版本是否兼容

### 6.3 测试失败

#### 6.3.1 问题现象

```
FAILED
Tests run: 10, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 2.345 s
```

#### 6.3.2 解决方案

```bash
# 运行特定测试，查看详细错误
./gradlew test --tests FailedTestClass

# 查看测试报告
open build/reports/tests/test/classes/FailedTestClass.html

# 跳过失败的测试
./gradlew test --tests !FailedTestClass
```

### 6.4 资源不足

#### 6.4.1 问题现象

```
FAILURE: Build failed with an exception.
* What went wrong:
Java heap space
Out of memory error
```

#### 6.4.2 解决方案

```bash
# 增加Gradle堆内存
export GRADLE_OPTS="-Xms1024m -Xmx4096m"

# 或在命令中指定
./gradlew compileJava -Dorg.gradle.jvmargs="-Xms1024m -Xmx4096m"
```

## 7. 构建报告

### 7.1 代码质量报告

#### 7.1.1 生成代码质量报告

```bash
# 生成代码质量报告
./gradlew site

# 查看报告
open build/docs/site/index.html
```

#### 7.1.2 报告内容

| 报告类型 | 描述 |
|----------|------|
| Checkstyle | 代码风格检查报告 |
| PMD | 静态代码分析报告 |
| SpotBugs | 缺陷检测报告 |
| JaCoCo | 代码覆盖率报告 |
| Test | 单元测试报告 |
| IntegrationTest | 集成测试报告 |

### 7.2 依赖报告

#### 7.2.1 生成依赖报告

```bash
# 生成依赖报告
./gradlew dependencies

#### 7.2.2 依赖分析

```bash
# 分析依赖
./gradlew dependencies --configuration compileClasspath

# 分析未使用的依赖
./gradlew dependencies --configuration compileClasspath

# 分析冲突依赖
./gradlew dependencies
```

### 7.3 构建时间报告

#### 7.3.1 生成构建时间报告

```bash
# 生成构建时间报告
./gradlew clean compileJava --scan

# 使用构建时间分析
./gradlew clean compileJava --profile
```

## 8. 持续集成

### 8.1 GitHub Actions配置

在 `.github/workflows/ci.yml` 文件中配置：

```yaml
name: CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        cache: gradle
    - name: Build with Gradle
      run: ./gradlew build
    - name: Run tests
      run: ./gradlew test
    - name: Code quality check
      run: ./gradlew checkstyleMain pmdMain spotbugsMain
```

### 8.2 GitLab CI配置

在 `.gitlab-ci.yml` 文件中配置：

```yaml
image: eclipse-temurin:21

stages:
  - build
  - test
  - quality

variables:
  GRADLE_OPTS: "-Dorg.gradle.daemon=false"

cache:
  paths:
    - .gradle/
    - build/

build:
  stage: build
  script:
    - ./gradlew compileJava -x test
  artifacts:
    paths:
      - build/

test:
  stage: test
  script:
    - ./gradlew test
  artifacts:
    reports:
      junit:
        - build/test-results/test/*.xml

quality:
  stage: quality
  script:
    - ./gradlew checkstyleMain pmdMain spotbugsMain
```

## 9. 最佳实践

### 9.1 开发环境最佳实践

1. **使用统一的开发环境**：所有开发人员使用相同版本的Java、Gradle和IDE
2. **定期更新依赖**：定期更新项目依赖，修复安全漏洞
3. **使用Gradle Wrapper**：确保所有开发人员使用相同版本的Gradle
4. **遵循代码风格**：使用Checkstyle、PMD等工具确保代码风格一致
5. **编写单元测试**：为核心功能编写单元测试，确保代码质量

### 9.2 构建最佳实践

1. **使用模块化构建**：按模块构建，提高构建效率
2. **启用并行构建**：使用 `--parallel` 参数启用并行构建
3. **使用增量构建**：Gradle默认支持增量构建
4. **跳过不必要的检查**：开发环境中跳过代码质量检查，提高构建速度
5. **使用构建缓存**：启用Gradle构建缓存，减少构建时间

### 9.3 依赖管理最佳实践

1. **使用BOM管理版本**：使用Bill of Materials管理依赖版本
2. **锁定依赖版本**：在libs.versions.toml中锁定依赖版本
3. **排除冲突依赖**：及时排除冲突依赖
4. **定期检查依赖**：使用 `./gradlew dependencies` 定期检查依赖
5. **使用可靠的镜像**：使用可靠的Gradle镜像，提高依赖下载速度

## 10. 联系方式

### 10.1 技术支持

- **技术支持邮箱**：support@example.com
- **技术支持热线**：+86-XXX-XXXX
- **在线文档**：[BUILD_GUIDE](https://example.com/build-guide)

### 10.2 反馈建议

- **反馈邮箱**：feedback@example.com
- **反馈热线**：+86-XXX-XXXX
- **在线反馈**：[反馈表单](https://example.com/feedback-form)

---

**免责声明**：本指南仅供参考，具体实施以实际项目配置为准。如有疑问，请联系support@example.com。
