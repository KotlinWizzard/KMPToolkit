import org.jetbrains.kotlin.config.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply  false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
}

ext {
    var jvmTarget = JvmTarget.JVM_1_8
}
