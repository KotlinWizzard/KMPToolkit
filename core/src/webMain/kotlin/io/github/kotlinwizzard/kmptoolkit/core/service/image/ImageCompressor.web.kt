package io.github.kotlinwizzard.kmptoolkit.core.service.image

import org.jetbrains.skia.ImageInfo
import kotlin.js.ExperimentalWasmJsInterop

actual fun ImageCompressor.compressImage(
    content: ByteArray,
    compressionRatio: Float
): ByteArray {
   return content
}
