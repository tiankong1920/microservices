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
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    
    implementation(libs.resilience4j.spring.boot3)
    
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    
    implementation(libs.modelmapper)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.logstash.logback.encoder)
    
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    
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
