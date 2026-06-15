plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("io.freefair.lombok")
    jacoco
}

jacoco {
    toolVersion = "0.8.11"
}

dependencies {
    implementation(project(":common"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.security)

    implementation(libs.spring.cloud.starter)

    implementation(libs.modelmapper)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.logstash.logback.encoder)

    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    runtimeOnly(libs.postgresql)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.h2)

    // Spring Cloud Contract for API Contract Testing
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-verifier:4.1.3")
    developmentOnly(libs.spring.boot.starter.devtools)
}

tasks.bootJar {
    enabled = true
    archiveClassifier.set("")
}

tasks.jar {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
    jvmArgs = listOf("-XX:+EnableDynamicAgentLoading")
    finalizedBy(tasks.jacocoTestReport)
    exclude("**/TemplateIntegrationTest.class")
    exclude("**/TemplateStatisticsIntegrationTest.class")
    exclude("**/TemplatePerformanceTest.class")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.10".toBigDecimal()
            }
        }
        rule {
            element = "CLASS"
            excludes = listOf(
                "com.inventory.templateservice.TemplateServiceApplication",
                "com.inventory.templateservice.dto.*",
                "com.inventory.templateservice.config.*",
                "com.inventory.templateservice.exception.*",
                "com.inventory.templateservice.controller.*",
                "com.inventory.templateservice.entity.*",
                "com.inventory.templateservice.repository.*",
                "com.inventory.templateservice.security.*",
                "com.inventory.templateservice.service.*"
            )
            limit {
                minimum = "0.50".toBigDecimal()
            }
        }
    }
}

tasks.withType<org.gradle.api.plugins.quality.Pmd>().configureEach {
    ruleSets = emptyList()
    ruleSetFiles = files(rootProject.file("config/pmd/pmd.xml"))
    exclude("**/CustomFieldService.java", "**/TemplateImportExportService.java", "**/TemplateService.java")
}
