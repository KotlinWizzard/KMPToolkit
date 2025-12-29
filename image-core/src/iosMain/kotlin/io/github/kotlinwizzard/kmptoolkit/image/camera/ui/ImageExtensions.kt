package io.github.kotlinwizzard.kmptoolkit.image.camera.ui

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.refTo
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
fun UIImage.resizePreservingAspectRatio(maxSize: Double): UIImage {
    val originalWidth = this.size.useContents { width }
    val originalHeight = this.size.useContents { height }

    // Wenn das Bild bereits klein genug ist, gib es direkt zurück
    if (originalWidth <= maxSize && originalHeight <= maxSize) {
        return this
    }

    val widthRatio = maxSize / originalWidth
    val heightRatio = maxSize / originalHeight
    val scaleRatio = minOf(widthRatio, heightRatio)

    val newWidth = originalWidth * scaleRatio
    val newHeight = originalHeight * scaleRatio

    val targetSize = CGSizeMake(newWidth, newHeight)

    val renderer = UIGraphicsImageRenderer(size = targetSize)
    return renderer.imageWithActions { _ ->
        this.drawInRect(CGRectMake(0.0, 0.0, targetSize.useContents { width }, targetSize.useContents { height }))
    }
}



fun UIImage.toResizedByteArray(maxSize:Double=1024.0, compressionQuality: Double=1.0): ByteArray {
    val resizedImage = resizePreservingAspectRatio(maxSize)
    return resizedImage.toByteArray(compressionQuality)
}

@OptIn(ExperimentalForeignApi::class)
fun UIImage.toByteArray(compressionQuality: Double=1.0): ByteArray {
    val validCompressionQuality = compressionQuality.coerceIn(0.0, 1.0)
    val jpegData = UIImageJPEGRepresentation(this, validCompressionQuality)!!
    return ByteArray(jpegData.length.toInt()).apply {
        memcpy(this.refTo(0), jpegData.bytes, jpegData.length)
        jpegData.finalize()
    }
}


fun ByteArray.toUiImage(): UIImage? {
    val byteArray = this
    if (byteArray.isEmpty()) {
        return null
    }

    return runCatching {
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