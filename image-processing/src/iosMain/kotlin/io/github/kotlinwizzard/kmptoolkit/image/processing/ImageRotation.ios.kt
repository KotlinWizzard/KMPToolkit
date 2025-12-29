package io.github.kotlinwizzard.kmptoolkit.image.processing

import io.github.kotlinwizzard.kmptoolkit.image.camera.ui.toByteArray
import io.github.kotlinwizzard.kmptoolkit.image.camera.ui.toUiImage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGContextRotateCTM
import platform.CoreGraphics.CGContextTranslateCTM
import platform.CoreGraphics.CGImageGetBitmapInfo
import platform.CoreGraphics.CGImageGetBitsPerComponent
import platform.CoreGraphics.CGImageGetColorSpace
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIImage
import kotlin.math.PI


actual suspend fun ImageRotation.Companion.rotateImage(
    byteArray: ByteArray,
    rotateBy: ImageRotation
):ByteArray {
    val image = byteArray.toUiImage() ?: return byteArray
    val rotatedImage = rotateImage(image, rotateBy)
    return rotatedImage.toByteArray()
}


@OptIn(ExperimentalForeignApi::class)
fun rotateImage(image: UIImage, rotation: ImageRotation): UIImage {
    val degrees = rotation.rotation.toDouble()
    if (degrees % 360.0 == 0.0) return image // Kein Rotieren nötig

    val radians = degrees * (PI / 180.0)

    val originalWidth = image.size.useContents { width }
    val originalHeight = image.size.useContents { height }

    val rotatedSize = if (degrees % 180 == 0.0) {
        CGSizeMake(originalWidth, originalHeight)
    } else {
        CGSizeMake(originalHeight, originalWidth)
    }
    val rotatedWidth = rotatedSize.useContents { width }
    val rotatedHeight = rotatedSize.useContents { height }

    val bitmapInfo = CGImageGetBitmapInfo(image.CGImage)
    val colorSpace = CGImageGetColorSpace(image.CGImage)

    val ctx = CGBitmapContextCreate(
        null,
        rotatedWidth.toULong(),
        rotatedHeight.toULong(),
        CGImageGetBitsPerComponent(image.CGImage),
        0u,
        colorSpace,
        bitmapInfo
    )

    CGContextTranslateCTM(ctx, rotatedWidth / 2, rotatedHeight / 2)
    CGContextRotateCTM(ctx, radians)
    CGContextDrawImage(
        ctx,
        CGRectMake(-originalWidth / 2, -originalHeight / 2, originalWidth, originalHeight),
        image.CGImage
    )

    val rotatedCGImage = CGBitmapContextCreateImage(ctx)
    CGContextRelease(ctx)

    return rotatedCGImage?.let { rotated ->
        val newImage = UIImage(rotated)
        CGImageRelease(rotated)
        newImage
    } ?: image
}

