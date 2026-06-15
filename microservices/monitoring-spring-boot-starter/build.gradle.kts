plugins {
    id("java-library")
    id("io.spring.dependency-management")
    id("io.freefair.lombok")
}

dependencies {
    api(project(":monitoring-core"))
    
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.web)
    
    implementation(libs.micrometer.core)
    implementation(libs.micrometer.registry.prometheus)
    
    implementation(libs.logstash.logback.encoder)
    implementation(libs.elasticsearch.java)
    implementation(libs.jackson.databind)
    
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
}

tasks.jar {
    enabled = true
    archiveClassifier.set("")
}

tasks.test {
    useJUnitPlatform()
}
