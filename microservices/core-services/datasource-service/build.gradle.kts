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
    implementation(libs.spring.boot.starter.quartz)
    implementation(libs.spring.boot.starter.mail)

    implementation(libs.spring.cloud.starter)

    implementation(libs.resilience4j.spring.boot3)

    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    implementation(libs.modelmapper)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.logstash.logback.encoder)

    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    implementation("org.postgresql:postgresql:42.7.2")
    implementation("co.elastic.clients:elasticsearch-java:8.11.1")
    implementation("org.apache.kudu:kudu-client:1.17.0")
    
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("org.bouncycastle:bcprov-jdk18on:1.80")

    runtimeOnly(libs.postgresql)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Spring Cloud Contract for API Contract Testing
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-verifier:4.1.3")

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.h2)
    developmentOnly(libs.spring.boot.starter.devtools)
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.testcontainers:elasticsearch:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
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
                minimum = "0.00".toBigDecimal()
            }
        }
        rule {
            element = "CLASS"
            excludes = listOf(
                "com.inventory.datasourceservice.DatasourceServiceApplication",
                "com.inventory.datasourceservice.dto.*",
                "com.inventory.datasourceservice.entity.*",
                "com.inventory.datasourceservice.repository.*",
                "com.inventory.datasourceservice.controller.*",
                "com.inventory.datasourceservice.config.*",
                "com.inventory.datasourceservice.exception.*",
                "com.inventory.datasourceservice.service.UserContext",
                "com.inventory.datasourceservice.service.TenantContext",
                "com.inventory.datasourceservice.service.TenantContext.TenantHolder",
                "com.inventory.datasourceservice.service.NotificationService",
                "com.inventory.datasourceservice.service.NotificationService.*",
                "com.inventory.datasourceservice.service.AlertService",
                "com.inventory.datasourceservice.plugin.DataSourcePlugin.IndexInfo",
                "com.inventory.datasourceservice.plugin.PluginRegistry",
                "com.inventory.datasourceservice.plugin.elasticsearch.ElasticsearchDataSourcePlugin",
                "com.inventory.datasourceservice.plugin.kudu.KuduDataSourcePlugin",
                "com.inventory.datasourceservice.plugin.postgresql.PostgreSQLDataSourcePlugin",
                "com.inventory.datasourceservice.plugin.mysql.MySQLDataSourcePlugin",
                "com.inventory.datasourceservice.plugin.AbstractDataSourcePlugin",
                "com.inventory.datasourceservice.security.EncryptionService",
                "com.inventory.datasourceservice.service.ConnectionTestService",
                "com.inventory.datasourceservice.service.DatasourceConfigService"
            )
            limit {
                minimum = "0.45".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
