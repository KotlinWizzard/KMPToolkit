plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinMultiplatform) apply  false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
}

val secretsFile = rootDir.resolve("secrets.gradle.properties")
if (secretsFile.exists()) {
    secretsFile.reader().use {
        java.util.Properties().apply { load(it) }.forEach { key, value ->
          gradle.rootProject.extra.set(key.toString(), value)
        }
    }
}


allprojects {

}