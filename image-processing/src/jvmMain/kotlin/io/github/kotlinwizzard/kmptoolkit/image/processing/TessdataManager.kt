package io.github.kotlinwizzard.kmptoolkit.image.processing

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

internal object TessdataManager {


    fun ensureExtracted(clazz: Class<*>, language: String): Path {
        val langs = language.split('+').map { it.trim() }.filter { it.isNotEmpty() }
        require(langs.isNotEmpty()) { "No language specified" }

        val resourceNames = buildList {
            langs.forEach { add("tessdata/$it.traineddata") }
        }

        val versionHash = hashOfResources(clazz, resourceNames)

        val root = Files.createTempDirectory("kmptoolkit-tessdata").toAbsolutePath()
            .resolve(versionHash)
        val tessdataDir = root.resolve("tessdata").createDirectories()

        resourceNames.forEach { res ->
            val fileName = res.substringAfterLast('/')
            val target = tessdataDir.resolve(fileName)
            if (!target.exists()) {
                extractResource(clazz, "/$res", target)
            }
        }

        return tessdataDir
    }

    private fun extractResource(clazz: Class<*>, resourcePath: String, target: Path) {
        val input = clazz.getResourceAsStream(resourcePath)
            ?: throw IllegalStateException("Missing resource: $resourcePath")

        input.use { ins ->
            Files.copy(ins, target, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun hashOfResources(clazz: Class<*>, resources: List<String>): String {
        val md = MessageDigest.getInstance("SHA-256")
        for (res in resources) {
            val bytes = clazz.getResourceAsStream("/$res")?.use { it.readBytes() }
                ?: throw IllegalStateException("Missing resource: /$res")
            md.update(bytes)
        }
        val digest = md.digest()
        return digest.joinToString("") { b -> "%02x".format(b) }.take(16)
    }
}