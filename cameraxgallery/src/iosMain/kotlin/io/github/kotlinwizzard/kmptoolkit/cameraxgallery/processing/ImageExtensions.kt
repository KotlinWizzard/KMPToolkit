package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.processing

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.UIKit.UIImage

fun ByteArray.toUiImage(): UIImage? {
    val byteArray = this
    if (byteArray.isEmpty()) {
        return null
    }

    return kotlin.runCatching {
        val nsData = byteArray.toNSData()
        UIImage(nsData)
    }.getOrNull()
}



@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toNSData(): NSData {
    return this.usePinned {
        NSData.dataWithBytes(
            bytes = it.addressOf(0),
            length = this.size.toULong()
        )
    }
}