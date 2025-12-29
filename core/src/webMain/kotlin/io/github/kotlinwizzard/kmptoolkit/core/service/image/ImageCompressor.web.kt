package io.github.kotlinwizzard.kmptoolkit.core.service.image

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

actual fun ImageCompressor.compressImage(
    content: ByteArray,
    compressionRatio: Float
): ByteArray {

    val image = Image.makeFromEncoded(content)
    val data = image.encodeToData(
        EncodedImageFormat.JPEG,
        (compressionRatio*100).toInt().coerceIn(0,100)
    ) ?: return content
    return data.bytes
}
