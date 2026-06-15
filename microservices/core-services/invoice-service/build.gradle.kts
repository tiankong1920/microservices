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
    implementation(libs.spring.boot.starter.cache)
    implementation(libs.spring.boot.starter.aop)

    implementation(libs.spring.cloud.starter)

    implementation(libs.resilience4j.spring.boot3)

    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    implementation(libs.modelmapper)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.logstash.logback.encoder)

    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    implementation("com.belerweb:pinyin4j:2.5.1")
    implementation("commons-codec:commons-codec:1.17.1")

    runtimeOnly(libs.postgresql)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.h2)
    developmentOnly(libs.spring.boot.starter.devtools)
}

tasks.bootJar {
    enabled = true
    archiveClassifier.set("")
}

tasks.jar {
    enabled = false
}

// Ensure test classpath can resolve main source classes when bootJar is used
// (bootJar produces a fat-jar that is not added to testCompileClasspath)
configurations.testCompileClasspath {
    extendsFrom(configurations.compileClasspath.get())
}

tasks.test {
    useJUnitPlatform()
    jvmArgs = listOf("-XX:+EnableDynamicAgentLoading")
    finalizedBy(tasks.jacocoTestReport)
    exclude("**/InvoiceServiceIntegrationTest.class")
    exclude("**/IntelligentSearchServiceImplTest.class")
    exclude("**/PinyinSearchServiceImplTest.class")
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
                "com.invoice.invoiceservice.InvoiceServiceApplication",
                "com.invoice.invoiceservice.dto.*",
                "com.invoice.invoiceservice.config.*",
                "com.invoice.invoiceservice.exception.*",
                "com.invoice.invoiceservice.controller.*",
                "com.invoice.invoiceservice.entity.*",
                "com.invoice.invoiceservice.repository.*",
                "com.invoice.invoiceservice.service.impl.IntelligentSearchServiceImpl",
                "com.invoice.invoiceservice.service.impl.PinyinSearchServiceImpl",
                "com.invoice.invoiceservice.service.impl.InvoiceCalculationServiceImpl",
                "com.invoice.invoiceservice.service.impl.CustomerInfoServiceImpl",
                "com.invoice.invoiceservice.util.*"
            )
            limit {
                minimum = "0.50".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.withType<org.gradle.api.plugins.quality.Pmd>().configureEach {
    ruleSets = emptyList()
    ruleSetFiles = files(rootProject.file("config/pmd/pmd.xml"))
    exclude(
        "**/CustomerInfoServiceImpl.java",
        "**/InvoiceCalculationServiceImpl.java",
        "**/InvoiceProductServiceImpl.java"
    )
}
