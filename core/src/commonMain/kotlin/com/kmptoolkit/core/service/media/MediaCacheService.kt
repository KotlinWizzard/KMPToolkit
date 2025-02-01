package com.kmptoolkit.core.service.media


import com.kmptoolkit.core.extensions.millis
import com.kmptoolkit.core.service.file.SYSTEM
import com.kmptoolkit.core.service.image.ImageCompressor
import com.kmptoolkit.core.service.image.MaxByteCompression
import com.kmptoolkit.core.service.uuid.UUID
import kotlinx.datetime.Clock
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import kotlin.time.DurationUnit


sealed class MediaCacheService(
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
            system.createDirectories(tempDir)
        }
        return tempDir
    }

    fun generateFilename(extension: String = defaultFileExtension): String =
        "${filenamePrefix}_${UUID.generate()}$extension"

    fun getFullPathFromFilename(filename: String) = getPathFromFilename(filename).toString()

    protected fun getPathFromFilename(filename: String) = getTempDirOrCreate() / filename.toPath()


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


    fun readCachedFileByFilename(filename: String): ByteArray =
        readCachedFile((tempDir / filename.toPath()).toString())


    init {
        cleanupOldFiles()
    }

    class Image(
        basePath: String = DEFAULT_BASE_PATH,
        override val cacheDeletionTimeoutMillis: Long? = DurationUnit.DAYS.millis(1),
    ) : MediaCacheService(basePath, cacheDeletionTimeoutMillis) {
        override val defaultFileExtension: String
            get() = ".jpg"
        override val filenamePrefix: String
            get() = "image"

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

        companion object {
            const val DEFAULT_BASE_PATH = "media-image"
        }
    }

    class Video(
        basePath: String = DEFAULT_BASE_PATH,
        override val cacheDeletionTimeoutMillis: Long? = DurationUnit.DAYS.millis(1)
    ) : MediaCacheService(basePath, cacheDeletionTimeoutMillis) {
        override val defaultFileExtension: String
            get() = ".mp4"
        override val filenamePrefix: String
            get() = "video"

        fun cacheFileTemporary(
            content: ByteArray,
            filename: String = generateFilename(),
        ): String {
            val tempFilePath = getPathFromFilename(filename)
            val bytes = content

            FileSystem.SYSTEM.sink(tempFilePath).buffer().use { sink ->
                sink.write(bytes)
            }
            return tempFilePath.toString()
        }

        companion object {
            const val DEFAULT_BASE_PATH = "media-video"
        }
    }

    companion object {
        val SYSTEM_TEMPORARY_DIRECTORY = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.toString()

        private fun getPathIfExists(path: Path): String? {
            if (FileSystem.SYSTEM.exists(path)) return path.toString()
            return null
        }

        fun getPathIfExists(path: String): String? {
            if (FileSystem.SYSTEM.exists(path.toPath())) return path.toPath().toString()
            return null
        }

        fun doesPathExists(path: String): Boolean {
            return FileSystem.SYSTEM.exists(path.toPath())
        }

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

        private fun createDirectory(path: Path, recursive: Boolean = true):Path {
            if(FileSystem.SYSTEM.exists(path)) return path
            if (recursive) {
                FileSystem.SYSTEM.createDirectories(path)
            } else {
                FileSystem.SYSTEM.createDirectory(path)
            }
            return path
        }

        fun createDirectory(path: String, recursive: Boolean = true):String {
          return  createDirectory(path = path.toPath(), recursive = recursive).toString()
        }

        fun storeFile(
            content: ByteArray,
            path: String
        ): String {
            val tempFilePath = createDirectory(path = path.toPath(), recursive = true)
            val bytes = content
            FileSystem.SYSTEM.sink(tempFilePath).buffer().use { sink ->
                sink.write(bytes)
            }
            return tempFilePath.toString()
        }
    }
}


