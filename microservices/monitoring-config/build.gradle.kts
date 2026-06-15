plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("io.freefair.lombok")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":monitoring-core"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)

    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.logstash.logback.encoder)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
    archiveClassifier.set("")
}

tasks.test {
    useJUnitPlatform()
}
