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
}

group = libraryGroup
version = libraryVersion

kotlin {
    jvm()
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
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
    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                //put your multiplatform dependencies here
            }
        }
        val commonTest by getting {
            dependencies {
                //implementation(libs.kotlin.test)
            }
        }
        /*

        val desktopMain by creating {
            dependsOn(commonMain)
        }
        val desktopTest by creating {
            dependsOn(commonTest)
        }

        // Bestehende JVM und Linux SourceSets anpassen
        val jvmMain by getting {
            dependsOn(desktopMain)
        }
        val jvmTest by getting {
            dependsOn(desktopTest)
        }

        val linuxX64Main by getting {
            dependsOn(desktopMain)
        }
        val linuxX64Test by getting {
            dependsOn(desktopTest)
        }*/
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
        /*
        licenses {
            license {
                name = "XXX"
                url = "YYY"
                distribution = "ZZZ"
            }
        }*/
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
