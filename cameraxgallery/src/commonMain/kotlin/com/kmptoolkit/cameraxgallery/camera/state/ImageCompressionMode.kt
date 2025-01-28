package com.kmptoolkit.cameraxgallery.camera.state

import com.kmptoolkit.core.service.image.ByteUnit
import com.kmptoolkit.core.service.image.ImageCompressor
import com.kmptoolkit.core.service.image.MaxByteCompression

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