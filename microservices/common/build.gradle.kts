plugins {
    id("java-library")
    id("io.spring.dependency-management")
    id("io.freefair.lombok")
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.aop)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.resilience4j.spring.boot3)
    implementation(libs.modelmapper)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.spring.kafka)
    implementation(libs.micrometer.core)
    implementation(libs.micrometer.registry.prometheus)
    // Enable OpenFeign declarative clients and load-balancer integration
    implementation(libs.spring.cloud.starter.openfeign)
    implementation(libs.spring.cloud.loadbalancer)

    compileOnly(libs.spring.boot.starter.tomcat)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    compileOnly(libs.jakarta.servlet.api)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.jmh.core)
}

tasks.jar {
    enabled = true
    archiveClassifier.set("")
}
