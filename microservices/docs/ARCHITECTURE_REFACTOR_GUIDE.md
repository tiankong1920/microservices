# 2.1 架构重构 - 目录扁平化执行指南

## 概述
将 `microservices/project-root/` 下的所有模块上移到 `microservices/` 根目录，实现单一Gradle根项目结构。

## 前提条件
1. 确保磁盘空间充足（建议预留10GB+）
2. 建议在分支上执行，完成验证后再合入main
3. 先备份当前结构

## 执行步骤

### 步骤1: 备份当前结构（必须）
```powershell
# 在 E:\101 目录下执行
Copy-Item -Path "E:\101\microservices" -Destination "E:\101\microservices_backup" -Recurse -Force
```

### 步骤2: 验证当前项目结构
```powershell
cd E:\101\microservices

# 应该看到以下目录结构：
# - build.gradle.kts (外层)
# - settings.gradle.kts (外层)
# - gradle/libs.versions.toml (外层版本目录)
# - project-root/ (内层项目，所有模块在这里)

# 确认project-root下有所有模块
dir project-root\common
dir project-root\core-services
dir project-root\support-services
```

### 步骤3: 移动project-root内容到microservices根目录

```powershell
# 在 E:\101\microservices 目录下执行

# 移动所有模块目录
Move-Item -Path "project-root\common" -Destination ".\common" -Force
Move-Item -Path "project-root\core-services" -Destination ".\core-services" -Force
Move-Item -Path "project-root\support-services" -Destination ".\support-services" -Force
Move-Item -Path "project-root\monitoring" -Destination ".\monitoring" -Force
Move-Item -Path "project-root\monitoring-config" -Destination ".\monitoring-config" -Force
Move-Item -Path "project-root\monitoring-spring-boot-starter" -Destination ".\monitoring-spring-boot-starter" -Force
Move-Item -Path "project-root\cross-service-tests" -Destination ".\cross-service-tests" -Force
Move-Item -Path "project-root\buildSrc" -Destination ".\buildSrc" -Force
Move-Item -Path "project-root\config" -Destination ".\config" -Force
Move-Item -Path "project-root\docs" -Destination ".\docs" -Force
Move-Item -Path "project-root\scripts" -Destination ".\scripts" -Force
Move-Item -Path "project-root\gradle" -Destination ".\gradle" -Force

# 移动根目录构建文件
Move-Item -Path "project-root\build.gradle.kts" -Destination ".\build.gradle.kts" -Force
Move-Item -Path "project-root\settings.gradle.kts" -Destination ".\settings.gradle.kts" -Force

# 移动Docker相关文件
Move-Item -Path "project-root\docker-compose*.yml" -Destination ".\" -Force
Move-Item -Path "project-root\init-scripts" -Destination ".\init-scripts" -Force

# 移动隐藏文件（如果存在）
Move-Item -Path "project-root\.github" -Destination ".\.github" -Force
Move-Item -Path "project-root\.snyk" -Destination ".\.snyk" -Force
```

### 步骤4: 清理project-root目录
```powershell
# 确认project-root已清空
Remove-Item -Path "project-root" -Recurse -Force
```

### 步骤5: 更新settings.gradle.kts中的路径引用
确保 `settings.gradle.kts` 不再引用 `project-root`

### 步骤6: 验证构建
```powershell
# 清理缓存
.\gradlew clean

# 尝试构建
.\gradlew build -x test --continue

# 如果构建失败，检查错误信息并修复路径引用
```

### 步骤7: 运行测试
```powershell
.\gradlew test
```

## 预期结果
```
E:\101\microservices\                     # 唯一的Gradle根项目
├── build.gradle.kts                      # 合并后的构建文件
├── settings.gradle.kts                   # 合并后的settings
├── gradle\libs.versions.toml             # 唯一版本目录
├── common/                               # 公共模块
├── core-services/                       # 核心服务
├── support-services/                    # 支撑服务
├── monitoring/                          # 监控模块
├── cross-service-tests/                 # 测试模块
├── config/ / docs/ / scripts/           # 配置和文档
├── buildSrc/                            # 构建约定
├── docker-compose*.yml                  # Docker编排
└── .github/                            # GitHub配置
```

## 回滚步骤
如果出现问题，回滚到备份：
```powershell
# 删除当前microservices
Remove-Item -Path "E:\101\microservices" -Recurse -Force

# 恢复备份
Move-Item -Path "E:\101\microservices_backup" -Destination "E:\101\microservices"
```

## 注意事项
1. **不要在main分支直接操作** - 先在分支上验证
2. **确保CI/CD通过** - GitHub Actions可能需要更新路径
3. **Docker构建上下文** - docker-compose.yml中的build context可能需要调整
4. **IDE缓存** - 建议清除.idea/和.gradle/目录后重新导入
