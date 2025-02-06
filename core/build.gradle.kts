import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val libraryGroup: String by project
val libraryVersion: String by project
val libraryJvm: String by project
val basePackage = "$libraryGroup.core"
val defaultJvmTarget: JvmTarget  = JvmTarget.fromTarget(libraryJvm)
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

    /*
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }*/

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.androidxKmp.lifecycle.viewmodelCompose)
                implementation(libs.androidxKmp.lifecycle.viewmodel)
                implementation(libs.androidxKmp.lifecycle.runtimeCompose)
                implementation(libs.androidxKmp.core.bundle)
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
                implementation(libs.okio)
                implementation(libs.uuid)
            }
        }
        val commonTest by getting {
            dependencies {
                //implementation(libs.kotlin.test)
            }
        }

        val jvmMain by getting {
            dependencies{
                implementation(compose.desktop.currentOs)
                implementation(compose.desktop.common)
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

    coordinates(group.toString(), "core", version.toString())

    pom {
        name = "KMPToolkit Core"
        description = "A core library for Kotlin Multiplatform."
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


    /*
    repositories {
        maven {
            name = "Sonatype"
            url = if (version.toString().endsWith("SNAPSHOT")) {
                uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            } else {
                uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            }
            credentials {
                username = env.SONATYPE_USERNAME.value
                password = env.SONATYPE_PASSWORD.value
            }
        }
    }*/
}
