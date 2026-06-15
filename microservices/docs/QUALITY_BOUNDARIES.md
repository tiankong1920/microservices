# 代码质量边界配置标准

## 1. 概述

本文档定义了 Inventory Management System 项目的代码质量边界标准，包括测试覆盖率、代码复杂度、代码规范等方面的阈值配置。

---

## 2. 质量边界总览

| 工具 | 指标 | 阈值 | 说明 |
|------|------|------|------|
| JaCoCo | 行覆盖率 | ≥ 80% | 代码行执行比例 |
| JaCoCo | 分支覆盖率 | ≥ 70% | 分支条件执行比例 |
| JaCoCo | 指令覆盖率 | ≥ 75% | 字节码指令执行比例 |
| JaCoCo | 方法覆盖率 | ≥ 80% | 方法执行比例 |
| JaCoCo | 类覆盖率 | ≥ 90% | 类执行比例 |
| Checkstyle | 最大行长度 | 120 字符 | 单行代码长度限制 |
| Checkstyle | 最大文件长度 | 1000 行 | 单文件代码行数限制 |
| Checkstyle | 最大方法长度 | 150 行 | 单方法代码行数限制 |
| Checkstyle | 最大参数数量 | 7 个 | 方法参数数量限制 |
| PMD | 圈复杂度 | ≤ 15 | 方法内独立路径数量 |
| PMD | 认知复杂度 | ≤ 25 | 代码理解难度 |
| PMD | NPath 复杂度 | ≤ 200 | 可能执行路径数量 |

---

## 3. JaCoCo 覆盖率边界

### 3.1 覆盖率阈值配置

```kotlin
object Jacoco {
    const val LINE_COVERAGE = 0.80        // 行覆盖率: 80%
    const val BRANCH_COVERAGE = 0.70      // 分支覆盖率: 70%
    const val INSTRUCTION_COVERAGE = 0.75 // 指令覆盖率: 75%
    const val METHOD_COVERAGE = 0.80      // 方法覆盖率: 80%
    const val CLASS_COVERAGE = 0.90       // 类覆盖率: 90%
}
```

### 3.2 覆盖率说明

| 覆盖率类型 | 定义 | 重要性 |
|------------|------|--------|
| 行覆盖率 (LINE) | 被执行的代码行占总代码行的比例 | 高 |
| 分支覆盖率 (BRANCH) | 被执行的分支占总分支的比例 | 高 |
| 指令覆盖率 (INSTRUCTION) | 被执行的 Java 字节码指令比例 | 中 |
| 方法覆盖率 (METHOD) | 被调用的方法占总方法的比例 | 中 |
| 类覆盖率 (CLASS) | 被加载的类占总类的比例 | 低 |

### 3.3 排除配置

以下类型的代码不计入覆盖率统计：

- `**/config/**` - 配置类
- `**/dto/**` - 数据传输对象
- `**/entity/**` - 实体类
- `**/Application*.class` - 应用启动类

### 3.4 运行覆盖率检查

```bash
# 生成覆盖率报告
./gradlew jacocoTestReport

# 验证覆盖率是否达标
./gradlew jacocoCoverageCheck

# 查看报告
# build/reports/jacoco/test/html/index.html
```

---

## 4. Checkstyle 规则边界

### 4.1 尺寸边界配置

```kotlin
object Checkstyle {
    const val MAX_LINE_LENGTH = 120     // 最大行长度
    const val MAX_FILE_LENGTH = 1000    // 最大文件长度
    const val MAX_METHOD_LENGTH = 150   // 最大方法长度
    const val MAX_PARAMETERS = 7        // 最大参数数量
    const val MAX_WARNINGS = 0          // 最大警告数
}
```

### 4.2 边界说明

| 规则 | 阈值 | 说明 |
|------|------|------|
| LineLength | 120 | 单行代码不超过 120 字符（URL、import 除外） |
| FileLength | 1000 | 单文件不超过 1000 行 |
| MethodLength | 150 | 单方法不超过 150 行 |
| ParameterNumber | 7 | 方法参数不超过 7 个 |

### 4.3 运行检查

```bash
# 运行 Checkstyle 检查
./gradlew checkstyleMain checkstyleTest

# 查看报告
# build/reports/checkstyle/main.html
```

---

## 5. PMD 复杂度边界

### 5.1 复杂度阈值配置

```kotlin
object Pmd {
    const val MAX_CYCLOMATIC = 15      // 圈复杂度
    const val MAX_COGNITIVE = 25       // 认知复杂度
    const val MAX_NPATH = 200          // NPath 复杂度
    const val MAX_NCSSL_METHOD = 50    // 方法 NCSS
    const val MAX_NCSSL_CLASS = 500    // 类 NCSS
}
```

### 5.2 复杂度说明

#### 圈复杂度 (Cyclomatic Complexity)

- **定义**：方法内独立执行路径的数量
- **计算**：`V(G) = E - N + 2`（E=边数，N=节点数）
- **阈值**：≤ 15
- **建议**：
  - 1-10：简单方法，易于测试
  - 11-15：中等复杂度，需要关注
  - \> 15：高风险，建议重构

#### 认知复杂度 (Cognitive Complexity)

- **定义**：代码阅读理解的难度
- **特点**：考虑嵌套深度、逻辑运算符等
- **阈值**：≤ 25
- **建议**：通过提取方法、减少嵌套来降低

#### NPath 复杂度

- **定义**：方法可能执行路径的乘积
- **阈值**：≤ 200
- **建议**：避免过多条件分支组合

### 5.3 运行检查

```bash
# 运行 PMD 检查
./gradlew pmdMain pmdTest

# 查看报告
# build/reports/pmd/main.html
```

---

## 6. SpotBugs 边界

### 6.1 Bug 数量限制

```kotlin
object SpotBugs {
    const val MAX_BUGS = 0              // 最大 Bug 数
    const val MAX_HIGH_PRIORITY = 0     // 高优先级 Bug
    const val MAX_MEDIUM_PRIORITY = 5   // 中优先级 Bug
}
```

### 6.2 优先级说明

| 优先级 | 说明 | 阈值 |
|--------|------|------|
| High | 高危问题，可能导致安全漏洞或数据丢失 | 0 |
| Medium | 中等问题，可能导致功能异常 | 5 |
| Low | 低优先级问题，代码风格建议 | 不限制 |

### 6.3 运行检查

```bash
# 运行 SpotBugs 检查
./gradlew spotbugsMain spotbugsTest

# 查看报告
# build/reports/spotbugs/main.html
```

---

## 7. 综合质量检查

### 7.1 一键检查命令

```bash
# 运行所有质量检查
./gradlew checkQuality

# 包含质量检查的完整构建
./gradlew buildWithQuality
```

### 7.2 检查任务组成

`checkQuality` 任务包含：
- `pmdMain` / `pmdTest` - PMD 代码检查
- `spotbugsMain` / `spotbugsTest` - SpotBugs Bug 检查
- `checkstyleMain` / `checkstyleTest` - Checkstyle 规范检查

### 7.3 CI/CD 集成建议

```yaml
# GitHub Actions 示例
- name: Run Quality Checks
  run: ./gradlew checkQuality jacocoCoverageCheck
  
- name: Upload Coverage Report
  uses: codecov/codecov-action@v3
  with:
    files: ./build/reports/jacoco/test/jacocoTestReport.xml
```

---

## 8. 阈值调整指南

### 8.1 调整原则

1. **渐进式提升**：新项目从低阈值开始，逐步提高
2. **项目阶段**：开发期可适当放宽，发布前必须达标
3. **团队共识**：阈值调整需团队评审确认

### 8.2 调整流程

1. 在 `build.gradle.kts` 中修改 `V.Quality` 配置
2. 同步更新对应工具的配置文件
3. 运行验证确保配置生效
4. 更新本文档

### 8.3 推荐阈值演进

| 项目阶段 | 行覆盖率 | 分支覆盖率 | 圈复杂度 |
|----------|----------|------------|----------|
| 新项目启动 | 50% | 40% | 20 |
| 开发中期 | 70% | 60% | 18 |
| 发布准备 | 80% | 70% | 15 |
| 生产维护 | 85% | 75% | 12 |

---

## 9. 版本历史

| 版本 | 日期 | 修改内容 | 作者 |
|------|------|----------|------|
| 1.0.0 | 2026-03-20 | 初始版本 | Team |

---

## 10. 参考资料

- [JaCoCo Documentation](https://www.jacoco.org/jacoco/)
- [Checkstyle Documentation](https://checkstyle.sourceforge.io/)
- [PMD Documentation](https://pmd.github.io/)
- [SpotBugs Documentation](https://spotbugs.github.io/)
