pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "KMPToolkit"
include(":core")
include(":navigation")
include(":camera")
include(":gallery")
include(":image-processing")
include(":image-core")
include(":pagingxcaching")
include(":paging")
include(":sample:composeApp")
include(":sample-web:composeApp")
