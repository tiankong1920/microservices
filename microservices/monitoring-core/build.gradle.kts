plugins {
    id("java-library")
    id("io.spring.dependency-management")
    id("io.freefair.lombok")
}

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    compileOnly(libs.jakarta.annotation.api)

    compileOnly(libs.spring.boot.starter.actuator)
    compileOnly(libs.spring.boot.starter.security)
    
    implementation(libs.slf4j.api)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
}

tasks.jar {
    enabled = true
    archiveClassifier.set("")
}

tasks.test {
    useJUnitPlatform()
}
