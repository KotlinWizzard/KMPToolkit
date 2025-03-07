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
include(":cameraxgallery")
include(":pagingxcaching")
include(":sample:composeApp")
