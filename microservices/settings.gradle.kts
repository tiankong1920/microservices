rootProject.name = "inventory-management-system"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven {
            name = "springMilestone"
            url = uri("https://repo.spring.io/milestone")
        }
        maven {
            name = "aliyun"
            url = uri("https://maven.aliyun.com/repository/public")
        }
        maven {
            name = "google"
            url = uri("https://maven.google.com")
        }
    }
    
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    }

    includeBuild("buildSrc") {
        name = "build-conventions"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
        maven {
            name = "springMilestone"
            url = uri("https://repo.spring.io/milestone")
        }
        maven {
            name = "aliyun"
            url = uri("https://maven.aliyun.com/repository/public")
        }
        maven {
            name = "google"
            url = uri("https://maven.google.com")
        }
        maven {
            name = "jitpack"
            url = uri("https://jitpack.io")
        }
    }
}

val enableSourceChecks: String by settings
val enableBuildCache: String by settings

gradle.beforeProject {
    if (this.name != "buildSrc") {
        println("Configuring ${this.name}...")
    }
}

include(
    "common",
    "core-services:product-service",
    "core-services:order-service",
    "core-services:inventory-service",
    "core-services:sales-service",
    "core-services:procurement-service",
    "core-services:customer-service",
    "core-services:supplier-service",
    "core-services:business-partner-service",
    "core-services:datasource-service",
    "core-services:template-service",
    "core-services:mall-service",
    "core-services:invoice-service",
    "support-services:admin-service",
    "support-services:auth-service",
    "support-services:finance-service",
    "support-services:gateway-service",
    "support-services:config-service",
    "support-services:registry-service",
    "support-services:report-service",
    "monitoring-core",
    "monitoring-spring-boot-starter",
    "monitoring",
    "cross-service-tests"
)


enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

class ProjectFeature(val name: String, val description: String, val enabled: Boolean = true)

val projectFeatures = listOf(
    ProjectFeature("security", "Enable security features", true),
    ProjectFeature("monitoring", "Enable monitoring and metrics", true),
    ProjectFeature("distributed", "Enable distributed tracing", false),
    ProjectFeature("seata", "Enable Seata distributed transaction", false),
    ProjectFeature("websocket", "Enable WebSocket support", false)
)

gradle.projectsLoaded {
    rootProject.extensions.extraProperties.set("projectFeatures", projectFeatures)
}
