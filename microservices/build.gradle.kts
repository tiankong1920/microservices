import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType
import org.gradle.jvm.toolchain.JavaLanguageVersion
import java.time.Instant

plugins {
    java
    `java-library`
    groovy
    id("org.springframework.boot") version "3.4.4" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("io.freefair.lombok") version "9.0.0" apply false
    id("com.github.spotbugs") version "6.0.2"
    jacoco
    id("org.owasp.dependencycheck") version "9.0.9"
    checkstyle
}

// 版本常量统一管理
object V {
    const val GROUP = "com.inventory"
    const val VERSION = "3.0.0"
    const val JAVA = 21

    object Spring {
        const val BOOT = "3.4.4"
        const val CLOUD = "2024.0.2"
        const val ALIBABA = "2023.0.1.0"
    }

    object Lib {
        const val JACKSON = "2.18.0"
        const val MAPSTRUCT = "1.6.3"
        const val SPOTBUGS = "4.9.7"
        const val COMMONS_LANG3 = "3.18.0"
        const val LOG4J = "2.25.3"
        const val NETTY = "4.1.129.Final"
        const val KAFKA = "3.9.1"
        const val LZ4 = "1.8.1"
        const val JUNIT_PLATFORM = "1.11.4"
        const val JUNIT_JUPITER = "5.11.0"
        const val MICROMETER = "1.14.0"
        const val RESILIENCE4J = "2.2.0"
        const val OPENAPI = "2.3.0"
    }

    object Plugin {
        const val JACOCO = "0.8.12"
        const val CHECKSTYLE = "13.3.0"
    }

    object Quality {
        object Jacoco {
            const val LINE_COVERAGE = 0.80
            const val BRANCH_COVERAGE = 0.70
            const val INSTRUCTION_COVERAGE = 0.75
            const val METHOD_COVERAGE = 0.80
            const val CLASS_COVERAGE = 0.90
        }
        object Checkstyle {
            const val MAX_LINE_LENGTH = 120
            const val MAX_FILE_LENGTH = 1000
            const val MAX_METHOD_LENGTH = 150
            const val MAX_PARAMETERS = 7
            const val MAX_WARNINGS = 0
        }
        object Pmd {
            const val MAX_CYCLOMATIC = 15
            const val MAX_COGNITIVE = 25
            const val MAX_NPATH = 200
            const val MAX_NCSSL_METHOD = 50
            const val MAX_NCSSL_CLASS = 500
        }
        object SpotBugs {
            const val MAX_BUGS = 0
            const val MAX_HIGH_PRIORITY = 0
            const val MAX_MEDIUM_PRIORITY = 5
        }
    }
}

// 项目基础配置
group = V.GROUP
version = V.VERSION
description = "Inventory Management System - Microservices Platform"

// Java 工具链配置（Gradle 9.x 推荐写法）
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(V.JAVA))
    }
}

// 仓库配置
repositories {
    mavenCentral()
    maven {
        name = "aliyun"
        url = uri("https://maven.aliyun.com/repository/public")
    }
    maven {
        name = "springMilestone"
        url = uri("https://repo.spring.io/milestone")
    }
}

// 安全依赖强制版本列表
val securityForces = listOf(
    "com.github.spotbugs:spotbugs:${V.Lib.SPOTBUGS}",
    "org.apache.commons:commons-lang3:${V.Lib.COMMONS_LANG3}",
    "org.apache.logging.log4j:log4j-core:${V.Lib.LOG4J}",
    "org.apache.kafka:kafka-clients:${V.Lib.KAFKA}",
    "commons-fileupload:commons-fileupload:1.6.0",
    "commons-codec:commons-codec:1.18.0",
    "org.bouncycastle:bcprov-jdk18on:1.80"
) + listOf("netty-codec-http", "netty-codec-http2", "netty-common", "netty-buffer",
    "netty-transport", "netty-handler", "netty-resolver", "netty-codec")
    .map { "io.netty:$it:${V.Lib.NETTY}" }

// 全局依赖解析策略
configurations.all {
    resolutionStrategy {
        // Gradle 9.x 中 force 方法兼容，但推荐显式声明
        securityForces.forEach { module ->
            force(module)
        }
        preferProjectModules()
    }
    // 排除冗余依赖
    exclude(group = "commons-logging", module = "commons-logging")
    exclude(group = "log4j", module = "log4j")
}

// 子项目统一配置
subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "groovy")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "pmd")
    apply(plugin = "com.github.spotbugs")
    apply(plugin = "jacoco")
    apply(plugin = "checkstyle")

    group = V.GROUP
    version = V.VERSION

    // Spring 依赖管理
    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${V.Spring.BOOT}")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${V.Spring.CLOUD}")
            mavenBom("com.alibaba.cloud:spring-cloud-alibaba-dependencies:${V.Spring.ALIBABA}")
            mavenBom("com.fasterxml.jackson:jackson-bom:${V.Lib.JACKSON}")
        }
    }

    // 子项目仓库配置
    repositories {
        mavenCentral()
        maven { name = "aliyun"; url = uri("https://maven.aliyun.com/repository/public") }
    }

    // 子项目 Java 工具链
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(V.JAVA))
        }
    }

    // 编译配置
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-parameters", "-Xlint:unchecked", "-Xlint:deprecation"))
        // Gradle 9.x 中设置编译版本兼容
        options.release.set(V.JAVA)
    }

    // Javadoc 禁用（按需开启）
    tasks.withType<Javadoc>().configureEach {
        enabled = false
    }

    // 测试任务配置
    tasks.withType<Test>().configureEach {
        useJUnitPlatform {
            excludeTags("integration", "slow", "e2e")
        }
        testLogging {
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
            showStackTraces = true
            showCauses = true
        }
        // JVM 参数配置
        jvmArgs = listOf("-XX:+EnableDynamicAgentLoading", "-Xmx1g")
        // 并行测试（Gradle 9.x 推荐合理设置并行数）
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceIn(1, 4)
        // Gradle 9.x 中测试缓存默认开启，显式声明
        outputs.cacheIf { true }
    }

    // PMD 代码质量检查
    tasks.withType<org.gradle.api.plugins.quality.Pmd>().configureEach {
        ignoreFailures = false
        ruleSetFiles = files(rootProject.file("config/pmd/pmd.xml"))
        ruleSets = emptyList()
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    // SpotBugs 静态代码分析
    tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
        ignoreFailures = false
        excludeFilter = rootProject.file("config/spotbugs/spotbugs-exclude.xml")
        reports {
            create("html") { required.set(true) }
            create("xml") { required.set(true) }
        }
    }

    // Checkstyle 代码风格检查
    tasks.withType<Checkstyle>().configureEach {
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        configDirectory = rootProject.file("config/checkstyle")
        ignoreFailures = false
        maxWarnings = V.Quality.Checkstyle.MAX_WARNINGS
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    // 子项目依赖解析策略
    configurations.all {
        resolutionStrategy {
            securityForces.forEach { force(it) }
        }
    }

    // Jacoco 覆盖率报告
    tasks.withType<JacocoReport>().configureEach {
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false) // 禁用CSV报告
        }
        // 排除无需统计覆盖率的目录
        classDirectories.setFrom(files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/config/**",
                    "**/dto/**",
                    "**/entity/**",
                    "**/Application*.class"
                )
            }
        }))
        // Gradle 9.x 中指定源目录（兼容）
        sourceDirectories.setFrom(project.files("src/main/java"))
    }

    // 检查任务依赖覆盖率报告（仅在运行测试时触发）
    // Gradle 8.x 兼容性：移除隐式依赖，改为显式运行
    // tasks.named("check") {
    //     dependsOn(tasks.named("jacocoTestReport"))
    // }

    // Jar 包 MANIFEST 配置
    tasks.withType<Jar>().configureEach {
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Built-By" to System.getProperty("user.name"),
                "Build-Time" to Instant.now().toString()
            )
        }
    }

    // 通用依赖
    dependencies {
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:${V.Lib.JUNIT_PLATFORM}")
        compileOnly("com.github.spotbugs:spotbugs:${V.Lib.SPOTBUGS}")
        annotationProcessor("org.mapstruct:mapstruct-processor:${V.Lib.MAPSTRUCT}")
        // 基础测试依赖（按需补充）
        testImplementation("org.junit.jupiter:junit-jupiter-api:${V.Lib.JUNIT_JUPITER}")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${V.Lib.JUNIT_JUPITER}")
    }
}

// 自定义聚合任务
tasks.register("buildAll") {
    group = "build"
    description = "Build all subprojects"
    dependsOn(subprojects.map { it.tasks.named("build") })
}

tasks.register("testAll") {
    group = "verification"
    description = "Run all tests"
    dependsOn(subprojects.map { it.tasks.named("test") })
}

tasks.register("cleanAll") {
    group = "build"
    description = "Clean all subprojects"
    dependsOn(subprojects.map { it.tasks.named("clean") })
    doLast {
        delete(rootProject.layout.buildDirectory)
    }
}

tasks.register("checkQuality") {
    group = "verification"
    description = "Run code quality checks"
    dependsOn(subprojects.flatMap {
        listOf(
            it.tasks.named("pmdMain"),
            it.tasks.named("spotbugsMain"),
            it.tasks.named("checkstyleMain")
        )
    })
}

tasks.register("buildWithQuality") {
    group = "build"
    description = "Build with quality checks"
    dependsOn(tasks.named("cleanAll"), tasks.named("checkQuality"), tasks.named("buildAll"))
}

tasks.register("securityVersions") {
    group = "help"
    description = "显示安全版本"
    doLast {
        println("安全强制版本:")
        println("  Netty:    ${V.Lib.NETTY}")
        println("  Kafka:    ${V.Lib.KAFKA}")
        println("  Log4j:    ${V.Lib.LOG4J}")
        println("  SpotBugs: ${V.Lib.SPOTBUGS}")
    }
}

tasks.register("projectInfo") {
    group = "help"
    description = "显示项目信息"
    doLast {
        println("${rootProject.name} v${rootProject.version}")
        println("Java ${V.JAVA} | Spring Boot ${V.Spring.BOOT} | Spring Cloud ${V.Spring.CLOUD}")
        println("子项目数: ${subprojects.size}")
    }
}

// Gradle Wrapper 配置（指定 9.x 版本）
tasks.wrapper {
    gradleVersion = "9.4.0" // 与 Gradle 9.x 兼容的版本
    distributionType = DistributionType.BIN
    distributionSha256Sum = "60ea723356d81263e8002fec0fcf9e2b0eee0c0850c7a3d7ab0a63f2ccc601f3" // Gradle 9.4.0 发行包 SHA256 校验和
}

// Jacoco 全局配置
jacoco {
    toolVersion = V.Plugin.JACOCO
}

// Jacoco 覆盖率校验任务
tasks.register("jacocoCoverageCheck") {
    group = "verification"
    description = "验证 JaCoCo 覆盖率是否达标"
    dependsOn(subprojects.map { it.tasks.named("jacocoTestReport") })
    doLast {
        var allPassed = true
        subprojects.forEach { subproject ->
            val reportFile = subproject.layout.buildDirectory
                .file("reports/jacoco/test/jacocoTestReport.xml")
                .get()
                .asFile
            if (reportFile.exists()) {
                println("\n[${subproject.name}] 覆盖率检查:")
                val content = reportFile.readText()
                val lineCoverage = extractCoverage(content, "LINE")
                val branchCoverage = extractCoverage(content, "BRANCH")
                val instructionCoverage = extractCoverage(content, "INSTRUCTION")

                val lineOk = lineCoverage >= V.Quality.Jacoco.LINE_COVERAGE
                val branchOk = branchCoverage >= V.Quality.Jacoco.BRANCH_COVERAGE
                val instrOk = instructionCoverage >= V.Quality.Jacoco.INSTRUCTION_COVERAGE

                println("  行覆盖率: ${formatPercent(lineCoverage)} ${statusIcon(lineOk)} (阈值: ${formatPercent(V.Quality.Jacoco.LINE_COVERAGE)})")
                println("  分支覆盖率: ${formatPercent(branchCoverage)} ${statusIcon(branchOk)} (阈值: ${formatPercent(V.Quality.Jacoco.BRANCH_COVERAGE)})")
                println("  指令覆盖率: ${formatPercent(instructionCoverage)} ${statusIcon(instrOk)} (阈值: ${formatPercent(V.Quality.Jacoco.INSTRUCTION_COVERAGE)})")

                if (!lineOk || !branchOk || !instrOk) {
                    allPassed = false
                }
            } else {
                println("\n[${subproject.name}] 覆盖率报告文件不存在！")
                allPassed = false
            }
        }
        if (!allPassed) {
            println("\n❌ 覆盖率检查未通过")
            throw GradleException("覆盖率未达到阈值要求") // Gradle 9.x 中抛出异常终止构建
        } else {
            println("\n✅ 覆盖率检查全部通过")
        }
    }
}

// 辅助方法：提取覆盖率数据
fun extractCoverage(xml: String, type: String): Double {
    val regex = """<counter type="$type" missed="(\d+)" covered="(\d+)"/>""".toRegex()
    val match = regex.find(xml) ?: return 1.0
    val missed = match.groupValues[1].toDouble()
    val covered = match.groupValues[2].toDouble()
    val total = missed + covered
    return if (total == 0.0) 1.0 else covered / total
}

// 辅助方法：格式化百分比
fun formatPercent(value: Double): String = "${(value * 100).toInt()}%"

// 辅助方法：状态图标
fun statusIcon(passed: Boolean): String = if (passed) "✅" else "❌"

// Checkstyle 全局配置
checkstyle {
    toolVersion = V.Plugin.CHECKSTYLE
    configFile = file("config/checkstyle/checkstyle.xml")
    configDirectory = file("config/checkstyle")
    isIgnoreFailures = false
    maxWarnings = V.Quality.Checkstyle.MAX_WARNINGS
}

// SonarQube 扫描任务（使用 sonar-scanner CLI，兼容 Gradle 9.x）
tasks.register("sonar", Exec::class) {
    group = "verification"
    description = "Run SonarQube analysis using sonar-scanner CLI"

    workingDir = rootProject.projectDir

    val scannerHome = project.findProperty("sonar.scanner.home") as? String
        ?: "e:\\101\\sonar-scanner-7.3.0.5189-windows-x64"

    val isWindows = System.getProperty("os.name").lowercase().contains("windows")
    val scannerCmd = if (isWindows) {
        File(scannerHome, "bin/sonar-scanner.bat").absolutePath
    } else {
        File(scannerHome, "bin/sonar-scanner").absolutePath
    }

    val sonarToken = providers.gradleProperty("sonar.token").getOrElse("")
    val sonarHost = providers.gradleProperty("sonar.host.url").getOrElse("http://localhost:9000")

    commandLine = buildList {
        add(scannerCmd)
        add("-Dsonar.projectKey=inventory-management-system")
        add("-Dsonar.projectName=Inventory Management System")
        add("-Dsonar.projectVersion=${V.VERSION}")
        add("-Dsonar.host.url=$sonarHost")
        add("-Dsonar.sources=common/src/main/java,core-services/product-service/src/main/java,core-services/order-service/src/main/java,core-services/inventory-service/src/main/java,core-services/sales-service/src/main/java,core-services/procurement-service/src/main/java,core-services/customer-service/src/main/java,core-services/supplier-service/src/main/java,core-services/business-partner-service/src/main/java,support-services/admin-service/src/main/java,support-services/auth-service/src/main/java,support-services/finance-service/src/main/java,support-services/gateway-service/src/main/java,support-services/config-service/src/main/java,support-services/registry-service/src/main/java,support-services/report-service/src/main/java,monitoring-core/src/main/java,monitoring-spring-boot-starter/src/main/java,monitoring/src/main/java")
        add("-Dsonar.tests=common/src/test/java,core-services/product-service/src/test/java,core-services/order-service/src/test/java,core-services/inventory-service/src/test/java,core-services/sales-service/src/test/java,core-services/procurement-service/src/test/java,core-services/customer-service/src/test/java,core-services/supplier-service/src/test/java,core-services/business-partner-service/src/test/java,support-services/admin-service/src/test/java,support-services/auth-service/src/test/java,support-services/finance-service/src/test/java,support-services/config-service/src/test/java,support-services/report-service/src/test/java,monitoring-core/src/test/java,monitoring-spring-boot-starter/src/test/java,monitoring/src/test/java,cross-service-tests/src/test/java")
        add("-Dsonar.java.binaries=common/build/classes/java/main,core-services/product-service/build/classes/java/main,core-services/order-service/build/classes/java/main,core-services/inventory-service/build/classes/java/main,core-services/sales-service/build/classes/java/main,core-services/procurement-service/build/classes/java/main,core-services/customer-service/build/classes/java/main,core-services/supplier-service/build/classes/java/main,core-services/business-partner-service/build/classes/java/main,support-services/admin-service/build/classes/java/main,support-services/auth-service/build/classes/java/main,support-services/finance-service/build/classes/java/main,support-services/gateway-service/build/classes/java/main,support-services/config-service/build/classes/java/main,support-services/registry-service/build/classes/java/main,support-services/report-service/build/classes/java/main,monitoring-core/build/classes/java/main,monitoring-spring-boot-starter/build/classes/java/main,monitoring/build/classes/java/main")
        add("-Dsonar.java.test.binaries=common/build/classes/java/test,core-services/product-service/build/classes/java/test,core-services/order-service/build/classes/java/test,core-services/inventory-service/build/classes/java/test,core-services/sales-service/build/classes/java/test,core-services/procurement-service/build/classes/java/test,core-services/customer-service/build/classes/java/test,core-services/supplier-service/build/classes/java/test,core-services/business-partner-service/build/classes/java/test,support-services/admin-service/build/classes/java/test,support-services/auth-service/build/classes/java/test,support-services/finance-service/build/classes/java/test,support-services/config-service/build/classes/java/test,support-services/report-service/build/classes/java/test,monitoring-core/build/classes/java/test,monitoring-spring-boot-starter/build/classes/java/test,monitoring/build/classes/java/test")
        add("-Dsonar.sourceEncoding=UTF-8")
        add("-Dsonar.java.source=${V.JAVA}")
        add("-Dsonar.java.target=${V.JAVA}")
        add("-Dsonar.exclusions=**/config/**,**/dto/**,**/entity/**,**/Application*.java,**/generated/**,**/build/**,**/.gradle/**")
        add("-Dsonar.test.exclusions=**/test/**,**/build/**")
        add("-Dsonar.scm.disabled=true")
        add("-Dsonar.qualitygate.wait=false")
        if (sonarToken.isNotEmpty()) {
            add("-Dsonar.token=$sonarToken")
        }
    }

    doFirst {
        if (!File(scannerCmd).exists()) {
            throw GradleException("Sonar scanner not found at: $scannerCmd. Please set sonar.scanner.home property or install sonar-scanner.")
        }
        println("Running SonarQube analysis...")
        println("Scanner: $scannerCmd")
        println("Project: inventory-management-system")
        println("Host: $sonarHost")
        if (sonarToken.isNotEmpty()) {
            println("Token: ${sonarToken.take(10)}...")
        }
    }
}

tasks.register("sonarWithCoverage") {
    group = "verification"
    description = "Run tests and SonarQube analysis with coverage"
    dependsOn("testAll", "sonar")
}

// 项目评估后配置：支持跳过测试
gradle.projectsEvaluated {
    tasks.withType<Test> {
        onlyIf { !project.hasProperty("skipTests") }
    }
}
