import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val libraryGroup: String by project
val libraryJvm: String by project
val basePackage = "$libraryGroup.sample"
val defaultJvmTarget: JvmTarget  = JvmTarget.fromTarget(libraryJvm)
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

kotlin {
    //jvm("desktop")
    androidTarget {
        compilerOptions {
            jvmTarget.set(defaultJvmTarget)
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            linkerOpts.add("-lsqlite3")
        }
    }
    /*
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }*/


    sourceSets {
        val commonMain by getting {
            dependencies {
                //put your multiplatform dependencies here
                implementation(project(":core"))
                implementation(project(":navigation"))
                implementation(project(":cameraxgallery"))
                implementation(project(":pagingxcaching"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(libs.coroutines.core)
                // Room Setup
                implementation(libs.room.runtime)
                implementation(libs.room.paging)
                implementation(libs.cache.driver)

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

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            runtimeOnly(libs.room.runtime.android)
            implementation(libs.koin.android)
        }

        /*
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }*/
    }
}

android {
    namespace = basePackage
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/composeResources")

    defaultConfig {
        applicationId = basePackage
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "io.github.kotlinwizzard.kmptoolkit.sample"
            packageVersion = "1.0.0"
        }
    }
}


room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    // room
    // Replace it with ->  ksp(libs.room.compiler) when it is stable
    // add("kspCommonMainMetadata", libs.room.compiler)
    listOf(
        "kspAndroid",
        "kspIosSimulatorArm64",
        "kspIosX64",
        "kspIosArm64",
    ).forEach {
        add(it, libs.room.compiler)
    }
}
