package com.kmptoolkit.core.service.image


import com.kmptoolkit.core.extensions.millis
import com.kmptoolkit.core.service.file.SYSTEM
import com.kmptoolkit.core.service.uuid.UUID
import kotlinx.datetime.Clock
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import kotlin.time.DurationUnit


class ImageCacheService(basePath: String="media", private val cacheDeletionTimeoutMillis: Long? = DurationUnit.DAYS.millis(1)) {
    private val tempDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / basePath.toPath()

    private val defaultFileExtension = ".jpg"

    private fun getTempDirOrCreate(): Path {
        val system = FileSystem.SYSTEM
        if (!system.exists(tempDir)) {
            system.createDirectory(tempDir)
        }
        return tempDir
    }

    fun generateFilename(extension: String = defaultFileExtension): String =
        "image_${UUID.generate()}$extension"


    private fun getPathFromFilename(filename: String) = getTempDirOrCreate() / filename.toPath()

    fun cacheFileTemporary(
        content: ByteArray,
        maxCacheBytes: MaxByteCompression? = null,
        filename: String = generateFilename(),
    ): String {
        val tempFilePath = getPathFromFilename(filename)
        val bytes =
            if (maxCacheBytes != null) {
                ImageCompressor.compressImage(content, maxCacheBytes)
            } else {
                content
            }
        FileSystem.SYSTEM.sink(tempFilePath).buffer().use { sink ->
            sink.write(bytes)
        }
        return tempFilePath.toString()
    }

    private fun cleanupOldFiles() {
        if(cacheDeletionTimeoutMillis==null) return
        val system = FileSystem.SYSTEM
        if (!system.exists(tempDir)) return
        system.listRecursively(tempDir).toList().forEach { path ->
            system.metadata(path).takeIf { it.isRegularFile }?.let { fileMetadata ->
                val times =
                    listOfNotNull(
                        fileMetadata.createdAtMillis,
                        fileMetadata.lastAccessedAtMillis,
                        fileMetadata.lastModifiedAtMillis,
                    ).sortedDescending()
                val latestUpdate = times.firstOrNull()
                val now = Clock.System.now().toEpochMilliseconds()
                if (latestUpdate == null || (now - latestUpdate) > cacheDeletionTimeoutMillis) {
                    try {
                        system.delete(path)
                    } catch (e: Exception) {
                       e.printStackTrace()
                    }
                }
            }
        }
    }

    fun getUriIfExists(filename: String) = getPathIfExists(getPathFromFilename(filename))

    private fun getPathIfExists(path: Path): String? {
        if (FileSystem.SYSTEM.exists(path)) return path.toString()
        return null
    }

    fun readCachedFileByFilename(filename: String): ByteArray =
        readCachedFile((tempDir / filename.toPath()).toString())

    private fun readCachedFile(path: String): ByteArray =
        FileSystem.SYSTEM.source(path.toPath()).buffer().use { source ->
            source.readByteArray()
        }

    fun readCachedFileOrNull(path: String): ByteArray? {
        if (getPathIfExists(path.toPath()) == null) return null
        return FileSystem.SYSTEM.source(path.toPath()).buffer().use { source ->
            source.readByteArray()
        }
    }

    init {
        cleanupOldFiles()
    }
}
