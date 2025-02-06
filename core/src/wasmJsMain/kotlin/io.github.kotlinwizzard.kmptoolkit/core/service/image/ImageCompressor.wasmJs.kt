package io.github.kotlinwizzard.kmptoolkit.core.service.image

actual fun ImageCompressor.compressImage(
    content: ByteArray,
    compressionRatio: Float
): ByteArray {
    return content
}