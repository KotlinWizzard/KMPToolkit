package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.processing

import io.github.kotlinwizzard.kmptoolkit.core.service.media.MediaCacheService

sealed class ImageInput {
    data class Bytes(val bytes: ByteArray) : ImageInput() {
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
        fun readBytes() = MediaCacheService.readCachedFileOrNull(path)
    }
}