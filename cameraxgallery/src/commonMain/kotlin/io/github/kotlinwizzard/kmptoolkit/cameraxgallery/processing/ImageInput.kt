package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.processing

import io.github.kotlinwizzard.kmptoolkit.core.service.media.MediaCacheService

sealed class ImageInput {
    abstract fun readBytes(): ByteArray?
    data class Bytes(val bytes: ByteArray) : ImageInput() {
        override fun readBytes(): ByteArray = bytes

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Bytes

            return bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int {
            return bytes.contentHashCode()
        }
    }

    data class File(val path: String) : ImageInput() {
        override fun readBytes() = MediaCacheService.readCachedFileOrNull(path)
    }
}