plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("io.freefair.lombok")
}

dependencies {
    implementation(project(":common"))
    
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.security)
    
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.logstash.logback.encoder)
    implementation(libs.modelmapper)
    
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
