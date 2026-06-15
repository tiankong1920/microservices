plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("io.freefair.lombok")
}

dependencies {
    implementation(project(":common"))

    implementation(libs.spring.boot.starter.data.redis.reactive)
    implementation(libs.spring.boot.starter.security)

    implementation(libs.spring.cloud.starter.gateway)
    implementation(libs.resilience4j.spring.boot3)
    implementation(libs.springdoc.openapi.starter.webflux.ui)

    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    
    compileOnly(libs.lombok)
    
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.junit.jupiter)
    developmentOnly(libs.spring.boot.starter.devtools)
}

tasks.bootJar {
    enabled = true
    archiveClassifier.set("")
}

tasks.jar {
    enabled = false
}
