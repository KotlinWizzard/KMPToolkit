import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val libraryGroup: String by project
val libraryVersion: String by project
val libraryJvm: String by project
val basePackage = "$libraryGroup.navigation"
val defaultJvmTarget: JvmTarget = JvmTarget.fromTarget(libraryJvm)
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.serialization) apply true
}

group = libraryGroup
version = libraryVersion

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(defaultJvmTarget)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(libs.kotlinX.dateTime)
                implementation(libs.serialization.core)
                implementation(libs.serialization.json)
                implementation(libs.kotlinx.io.core)
                implementation(libs.coroutines.core)
                implementation(libs.stdlib)
                api(libs.voyager.navigator)
                api(libs.voyager.screenmodel)
                api(libs.voyager.bottomSheetNavigator)
                api(libs.voyager.tabNavigator)
                api(libs.voyager.transitions)
                api(libs.koin.core)
                api(libs.koin.compose)
            }
        }
        val commonTest by getting {
            dependencies {
                //implementation(libs.kotlin.test)
            }
        }

        val androidMain by getting {
            dependencies {
                api(libs.koin.android)
            }
        }
    }
}

android {
    namespace = basePackage
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(group.toString(), "navigation", version.toString())

    pom {
        name = "KMPToolkit Navigation"
        description = "Voyager navigation extensions library for Kotlin Multiplatform."
        inceptionYear = "2025"
        url = "https://github.com/KotlinWizzard/KMPToolkit"
        licenses {
            license {
                name = "Apache License 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "KotlinWizzard"
                name = "KotlinWizzard"
                url = "https://github.com/KotlinWizzard"
            }
        }
        scm {
            url = "https://github.com/KotlinWizzard/KMPToolkit"
            connection = "scm:git:git://github.com/KotlinWizzard/KMPToolkit.git"
            developerConnection = "scm:git:ssh://git@github.com:KotlinWizzard/KMPToolkit.git"
        }
    }
}
