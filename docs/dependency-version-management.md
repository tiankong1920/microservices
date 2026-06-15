# 依赖版本管理指南

本文档定义了进销存管理系统中使用的依赖版本，以解决版本冲突问题并确保依赖的一致性。

## 1. 版本冲突问题说明

在项目开发过程中，我们发现以下依赖存在版本冲突问题：

1. **Lombok版本冲突**: 项目中使用的Lombok版本与Spring Boot BOM管理的版本不一致
2. **EhCache版本冲突**: 项目中使用的EhCache版本与Spring Boot推荐的版本不一致

## 2. 依赖版本规范

### 2.1 核心框架版本

| 依赖 | 版本 | 说明 |
|------|------|------|
| Java | 22 | 运行时和编译版本 |
| Spring Boot | 3.5.7 | 核心框架版本 |
| Spring Cloud | 2025.0.0 (Northfields) | 微服务框架版本 |

### 2.2 数据库相关依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| PostgreSQL JDBC Driver | 42.7.3 | 数据库驱动 |
| Flyway | 10.15.0 | 数据库迁移工具 |
| Hibernate | 6.6.1 | ORM框架 |

### 2.3 缓存相关依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| EhCache | 3.10.8 | 本地缓存实现 |
| Redis | 3.2.0 | 分布式缓存客户端 |

### 2.4 安全相关依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| JJWT | 0.12.6 | JWT实现 |
| Spring Security | 6.3.1 | 安全框架 |

### 2.5 工具类依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| Lombok | 1.18.32 | 代码简化工具 |
| Apache Commons Lang3 | 3.14.0 | 常用工具类 |
| Apache Commons Collections4 | 4.4 | 集合工具类 |

### 2.6 文档和测试依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| SpringDoc OpenAPI | 2.6.0 | API文档生成 |
| JUnit Jupiter | 5.10.2 | 单元测试框架 |
| Mockito | 5.7.0 | Mock框架 |

## 3. 版本冲突解决方案

### 3.1 Lombok版本冲突解决

在pom.xml中明确指定Lombok版本，并确保与Spring Boot BOM兼容：

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.32</version>
    <optional>true</optional>
</dependency>
```

### 3.2 EhCache版本冲突解决

使用Spring Boot推荐的EhCache版本，并确保与Jakarta EE兼容：

```xml
<dependency>
    <groupId>org.ehcache</groupId>
    <artifactId>ehcache</artifactId>
    <classifier>jakarta</classifier>
    <version>3.10.8</version>
</dependency>

<dependency>
    <groupId>javax.cache</groupId>
    <artifactId>cache-api</artifactId>
    <version>1.1.1</version>
</dependency>
```

## 4. 依赖管理最佳实践

### 4.1 使用Spring Boot BOM
尽可能使用Spring Boot BOM管理的依赖版本，避免手动指定可能冲突的版本。

### 4.2 明确指定必要版本
对于BOM未管理或需要特定版本的依赖，应在pom.xml中明确指定版本。

### 4.3 定期更新依赖
定期检查和更新依赖版本，确保使用最新的稳定版本。

### 4.4 版本兼容性验证
在更新依赖版本后，应进行全面测试以确保版本兼容性。

## 5. 依赖冲突检测

### 5.1 使用Maven命令检测冲突
```bash
mvn dependency:tree -Dverbose
```

### 5.2 解决冲突的方法
1. 使用`<exclusion>`排除冲突的传递依赖
2. 明确指定正确的依赖版本
3. 使用Maven的依赖调解机制

## 6. 版本更新流程

### 6.1 版本更新步骤
1. 检查新版本的兼容性
2. 更新pom.xml中的版本号
3. 运行构建和测试
4. 验证功能是否正常
5. 提交更改并记录更新日志

### 6.2 版本回滚
如果新版本引入问题，应立即回滚到稳定版本并记录问题原因。

本规范自发布之日起生效，所有依赖版本管理必须遵循此规范。