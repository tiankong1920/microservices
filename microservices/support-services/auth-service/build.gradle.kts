plugins {
    id("java-library")
    id("io.spring.dependency-management")
    id("io.freefair.lombok")
}

dependencies {
    implementation(project(":common"))
    
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.aop)
    implementation(libs.spring.boot.starter.cache)
    
    implementation(libs.spring.cloud.starter)
    
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.spring.security.oauth2.authorization.server)
    implementation("com.warrenstrange:googleauth:1.5.0")
    
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.h2)
    
    compileOnly(libs.lombok)
    
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    runtimeOnly(libs.spring.boot.starter.devtools)
}

tasks.jar {
    enabled = true
    archiveClassifier.set("")
}
