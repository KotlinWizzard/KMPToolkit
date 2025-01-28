package com.kmptoolkit.core.service.image

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun ImageCompressor.compressImage(
    content: ByteArray,
    compressionRatio: Float
): ByteArray {
    val nsData =
        content.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = content.size.toULong())
        }
    val image = UIImage(data = nsData)
    if (image == null) {
        return content
    }

    val compressedData = UIImageJPEGRepresentation(image, compressionRatio.toDouble())
    return compressedData?.let {
        val bytes = it.bytes?.reinterpret<ByteVar>()
        val length = it.length.toInt()
        ByteArray(length).apply {
            if (bytes != null) {
                for (i in 0 until length) {
                    this[i] = bytes[i]
                }
            }
        }
    } ?: content
}