import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

val libraryGroup: String by project
val libraryJvm: String by project
val basePackage = "$libraryGroup.sample"
val defaultJvmTarget: JvmTarget  = JvmTarget.fromTarget(libraryJvm)
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {

    jvm()


    js {
        nodejs()
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                //put your multiplatform dependencies here
                implementation(project(":core"))
                implementation(project(":navigation"))
               implementation(project(":gallery"))
                implementation(project(":paging"))
                implementation(project(":image-processing"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(libs.coroutines.core)


                //DI
               implementation(libs.koin.core)
               implementation(libs.koin.compose)
                //IMAGE Loading
                implementation(libs.sketch.compose)
                implementation(libs.sketch.svg)
                implementation(libs.sketch.animated)
                implementation(libs.sketch.http)
                implementation(libs.sketch.ktor)
            }
        }
    }
}

