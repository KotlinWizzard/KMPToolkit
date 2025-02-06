package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.camera.state

import io.github.kotlinwizzard.kmptoolkit.core.service.image.ByteUnit
import io.github.kotlinwizzard.kmptoolkit.core.service.image.ImageCompressor
import io.github.kotlinwizzard.kmptoolkit.core.service.image.MaxByteCompression

sealed class ImageCompressionMode {

    abstract fun compress(bytes: ByteArray): ByteArray

    data object None : ImageCompressionMode() {
        override fun compress(bytes: ByteArray): ByteArray {
            return bytes
        }
    }

    data class Compress(
        private val maxBytes: Float = 1F,
        private val unit: ByteUnit = ByteUnit.MB
    ) : ImageCompressionMode() {
        override fun compress(bytes: ByteArray): ByteArray {
            return ImageCompressor.compressImage(bytes, MaxByteCompression(maxBytes, unit))
        }
    }


}