package com.kmptoolkit.core.service.image


import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
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


val LocalCache = staticCompositionLocalOf<CacheProvider> {
    error("No cache provided")
}

@Composable
fun CacheServiceProvider(
    imageCacheBasePath: String = CacheService.Image.DEFAULT_BASE_PATH,
    videoCacheBasePath: String = CacheService.Video.DEFAULT_BASE_PATH,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalCache provides CacheProvider(
            CacheService.Image(imageCacheBasePath),
            CacheService.Video(videoCacheBasePath)
        ),
        content
    )
}

data class CacheProvider(val imageCache: CacheService.Image, val videoCache: CacheService.Video) {
    fun readCachedFileOrNull(path: String): ByteArray? {
        return imageCache.readCachedFileOrNull(path)
    }
}


sealed class CacheService(
    basePath: String,
    protected open val cacheDeletionTimeoutMillis: Long?
) {
    private val tempDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / basePath.toPath()
    val fullPath = tempDir.toString()
    abstract val defaultFileExtension: String
    abstract val filenamePrefix: String

    private fun getTempDirOrCreate(): Path {
        val system = FileSystem.SYSTEM
        if (!system.exists(tempDir)) {
            system.createDirectory(tempDir)
        }
        return tempDir
    }

    fun generateFilename(extension: String = defaultFileExtension): String =
        "${filenamePrefix}_${UUID.generate()}$extension"


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
        val cacheDeletionTimeoutMillis = cacheDeletionTimeoutMillis ?: return
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

    class Image(
        basePath: String = DEFAULT_BASE_PATH,
        override val cacheDeletionTimeoutMillis: Long? = DurationUnit.DAYS.millis(1),
    ) : CacheService(basePath, cacheDeletionTimeoutMillis) {
        override val defaultFileExtension: String
            get() = ".jpg"
        override val filenamePrefix: String
            get() = "image"

        companion object {
            const val DEFAULT_BASE_PATH = "media/image"
        }
    }

    class Video(
        basePath: String = DEFAULT_BASE_PATH,
        override val cacheDeletionTimeoutMillis: Long? = DurationUnit.DAYS.millis(1)
    ) : CacheService(basePath, cacheDeletionTimeoutMillis) {
        override val defaultFileExtension: String
            get() = ".mp4"
        override val filenamePrefix: String
            get() = "video"

        companion object {
            const val DEFAULT_BASE_PATH = "media/video"
        }
    }
}


