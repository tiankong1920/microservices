plugins {
    id("java-library")
    id("io.spring.dependency-management")
    id("io.freefair.lombok")
}

dependencies {
    testImplementation(project(":common"))
    testImplementation(project(":core-services:sales-service"))
    testImplementation(project(":core-services:order-service"))
    testImplementation(project(":core-services:inventory-service"))
    testImplementation(project(":support-services:finance-service"))
    testImplementation(project(":core-services:product-service"))
    testImplementation(project(":core-services:customer-service"))

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.data.jpa)
    testImplementation(libs.spring.boot.starter.web)
    testImplementation(libs.spring.boot.starter.webflux)

    testImplementation(libs.reactor.test)

    testImplementation(libs.janino)
    testImplementation(libs.logstash.logback.encoder)

    testImplementation(libs.seata.spring.boot.starter) {
        exclude(group = "com.alibaba", module = "druid")
    }

    testCompileOnly(libs.lombok)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.rabbitmq)

    testRuntimeOnly(libs.h2)
}

tasks.jar {
    enabled = true
    archiveClassifier.set("")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.jacocoTestReport {
    mustRunAfter(tasks.processResources)
}

afterEvaluate {
    val bootJarProjects = listOf(
        ":core-services:sales-service",
        ":core-services:order-service",
        ":core-services:inventory-service",
        ":core-services:product-service",
        ":core-services:customer-service",
        ":support-services:finance-service",
    )
    bootJarProjects.forEach { projectPath ->
        val bootJarTask = project(projectPath).tasks.findByName("bootJar")
        if (bootJarTask != null) {
            tasks.named("compileTestJava").configure {
                dependsOn(bootJarTask)
            }
            tasks.named("checkstyleTest").configure {
                dependsOn(bootJarTask)
            }
        }
    }
}
