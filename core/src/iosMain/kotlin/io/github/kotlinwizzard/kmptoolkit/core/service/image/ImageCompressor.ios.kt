package io.github.kotlinwizzard.kmptoolkit.core.service.image

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.get
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readValue
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGAffineTransform
import platform.CoreGraphics.CGAffineTransformIdentity
import platform.CoreGraphics.CGAffineTransformRotate
import platform.CoreGraphics.CGAffineTransformScale
import platform.CoreGraphics.CGAffineTransformTranslate
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGContextConcatCTM
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageGetBitmapInfo
import platform.CoreGraphics.CGImageGetBitsPerComponent
import platform.CoreGraphics.CGImageGetColorSpace
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSCoder
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImageConfiguration
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImageOrientation
import platform.UIKit.encodeCGAffineTransform
import platform.posix.M_PI
import platform.posix.M_PI_2

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun ImageCompressor.compressImage(
    content: ByteArray,
    compressionRatio: Float
): ByteArray {
    val nsData =
        content.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = content.size.toULong())
        }
    var image = UIImage(data = nsData)
    image = fixImageOrientation(image)
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

@OptIn(ExperimentalForeignApi::class)
private fun fixImageOrientation(image: UIImage): UIImage {
    var transform = CGAffineTransformIdentity.readValue<CGAffineTransform>()
    // Rotieren des Bildes basierend auf der imageOrientation
    val width = image.size.useContents { width }
    val height = image.size.useContents { height }
    when (image.imageOrientation) {
        UIImageOrientation.UIImageOrientationRight, UIImageOrientation.UIImageOrientationRightMirrored  -> {
            transform = CGAffineTransformTranslate(transform, 0.0, height)
            transform = CGAffineTransformRotate(transform, -M_PI_2)
        }
        UIImageOrientation.UIImageOrientationUpMirrored -> Unit
        UIImageOrientation.UIImageOrientationLeft, UIImageOrientation.UIImageOrientationLeftMirrored -> {
            transform = CGAffineTransformTranslate(transform, width, 0.0)
            transform = CGAffineTransformRotate(transform, M_PI_2)
        }
        UIImageOrientation.UIImageOrientationUp -> return image
        UIImageOrientation.UIImageOrientationDown, UIImageOrientation.UIImageOrientationDownMirrored  -> {
            transform = CGAffineTransformTranslate(transform, width, height)
            transform = CGAffineTransformRotate(transform, M_PI)
        }
        else ->  Unit
    }

    when (image.imageOrientation) {
        UIImageOrientation.UIImageOrientationRightMirrored, UIImageOrientation.UIImageOrientationLeftMirrored -> {
            transform = CGAffineTransformTranslate(transform, height, 0.0)
            transform = CGAffineTransformScale(transform, -1.0,1.0)
        }
        UIImageOrientation.UIImageOrientationUpMirrored, UIImageOrientation.UIImageOrientationDownMirrored  -> {
            transform = CGAffineTransformTranslate(transform, width, 0.0)
            transform = CGAffineTransformScale(transform, -1.0,1.0)
        }
        else ->  Unit
    }
    val ctx  = CGBitmapContextCreate(null, width.toULong(), height.toULong(),
        CGImageGetBitsPerComponent(image.CGImage), 0u,
        CGImageGetColorSpace(image.CGImage),
        CGImageGetBitmapInfo(image.CGImage))
    CGContextConcatCTM(ctx, transform)
    when (image.imageOrientation) {
        UIImageOrientation.UIImageOrientationLeft, UIImageOrientation.UIImageOrientationRight, UIImageOrientation.UIImageOrientationRightMirrored, UIImageOrientation.UIImageOrientationLeftMirrored -> {
            CGContextDrawImage(ctx, CGRectMake(0.0, 0.0,height,width), image.CGImage)
        }
        else ->   CGContextDrawImage(ctx, CGRectMake(0.0, 0.0,width,height), image.CGImage)
    }
    val cgimg = CGBitmapContextCreateImage(ctx) ?: return image
    val img = UIImage(cgimg)
    CGContextRelease(ctx)
    CGImageRelease(cgimg)
    return img
}


