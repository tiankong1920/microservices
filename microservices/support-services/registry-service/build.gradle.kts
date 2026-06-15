plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("io.freefair.lombok")
}

dependencies {
    implementation(project(":common"))
    
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery")
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.logstash.logback.encoder)
    
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

tasks.test {
    useJUnitPlatform()
}
