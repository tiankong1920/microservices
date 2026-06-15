# Gradle 9.4.0 升级方案

## 一、升级概述

### 当前版本
- Gradle: 9.3.1 → 9.4.0
- Java: 21
- Spring Boot: 3.4.2

### 升级目标
将 Gradle 从 9.3.1 升级到最新的 9.4.0 版本，并修复所有兼容性问题。

## 二、已完成的配置更新

### 1. gradle-wrapper.properties
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.4.0-bin.zip
```

### 2. 插件版本更新

| 插件 | 旧版本 | 新版本 | 说明 |
|------|--------|--------|------|
| io.freefair.lombok | 8.6 | 9.0.0 | Gradle 9.x 兼容 |
| com.github.spotbugs | 6.0.18 | 6.2.2 | Gradle 9.x 兼容 |
| com.diffplug.spotless | 6.25.0 | 7.0.2 | Gradle 9.x 兼容 |

### 3. Maven 仓库语法更新

**旧语法 (已弃用):**
```kotlin
maven { url = uri("https://maven.aliyun.com/repository/public") }
```

**新语法:**
```kotlin
maven {
    name = "aliyun"
    url = uri("https://maven.aliyun.com/repository/public")
}
```

## 三、Gradle 9.x 主要变更

### 1. 废弃的 API

| 废弃 API | 替代方案 |
|----------|----------|
| `ProjectConvention` | 类型安全 API |
| `url = uri(...)` 无 name | 添加 name 属性 |
| `<<` 操作符 | 使用 `doLast { }` |
| `compile` 配置 | 使用 `implementation` |

### 2. 新特性

- **改进的配置缓存**: 更好的性能和稳定性
- **增强的类型安全**: Kotlin DSL 改进
- **JDK 25 支持**: 支持最新 JDK 版本
- **依赖锁定改进**: 更可靠的依赖管理

## 四、预期错误及修复方案

### 错误 1: 插件版本不兼容

**错误信息:**
```
Plugin 'io.freefair.lombok' version '8.6' is not compatible with Gradle 9.x
```

**修复方案:**
```kotlin
// build.gradle.kts
id("io.freefair.lombok") version "9.0.0" apply false
```

### 错误 2: Maven 仓库语法警告

**错误信息:**
```
The syntax 'maven { url = uri(...) }' is deprecated
```

**修复方案:**
```kotlin
maven {
    name = "aliyun"
    url = uri("https://maven.aliyun.com/repository/public")
}
```

### 错误 3: Spotless 插件兼容性

**错误信息:**
```
Spotless plugin version 6.x is not compatible with Gradle 9.x
```

**修复方案:**
```kotlin
// libs.versions.toml
spotless = "7.0.2"
```

### 错误 4: SpotBugs 插件兼容性

**错误信息:**
```
SpotBugs plugin version 6.0.x has compatibility issues with Gradle 9.x
```

**修复方案:**
```kotlin
// libs.versions.toml
spotbugs = { id = "com.github.spotbugs", version = "6.2.2" }
```

### 错误 5: Kotlin DSL 类型推断问题

**错误信息:**
```
Type inference failed for 'configure' function
```

**修复方案:**
```kotlin
// 使用显式类型
configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.0")
    }
}
```

### 错误 6: Test 任务 JVM 参数

**错误信息:**
```
Property 'jvmArgs' has changed behavior in Gradle 9.x
```

**修复方案:**
```kotlin
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs = listOf("-XX:+EnableDynamicAgentLoading")
}
```

## 五、逐步修复步骤

### 步骤 1: 更新 Wrapper
```bash
./gradlew wrapper --gradle-version=9.4.0
```

### 步骤 2: 清理缓存
```bash
./gradlew clean
rm -rf .gradle
rm -rf build
rm -rf */build
```

### 步骤 3: 更新依赖
```bash
./gradlew dependencies --refresh-dependencies
```

### 步骤 4: 构建验证
```bash
./gradlew build -x test
```

### 步骤 5: 运行测试
```bash
./gradlew test
```

## 六、验证清单

- [ ] Gradle 版本更新到 9.4.0
- [ ] 所有插件版本兼容
- [ ] Maven 仓库语法更新
- [ ] 构建成功 (`./gradlew build`)
- [ ] 测试通过 (`./gradlew test`)
- [ ] 无弃用警告
- [ ] 配置缓存工作正常

## 七、回滚方案

如果升级失败，可以回滚到之前版本：

```properties
# gradle-wrapper.properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.2.1-bin.zip
```

```kotlin
// build.gradle.kts
id("io.freefair.lombok") version "8.6" apply false
```

## 八、参考链接

- [Gradle 9.0 Release Notes](https://docs.gradle.org/9.0.0/release-notes.html)
- [Gradle 9.3.1 Release Notes](https://docs.gradle.org/9.3.1/release-notes.html)
- [Gradle 9.4.0 Release Notes](https://docs.gradle.org/9.4.0/release-notes.html)
- [Gradle Upgrade Guide](https://docs.gradle.org/current/userguide/upgrading_version_9.html)
- [Kotlin DSL Migration Guide](https://docs.gradle.org/current/userguide/migrating_from_groovy_to_kotlin_dsl.html)
