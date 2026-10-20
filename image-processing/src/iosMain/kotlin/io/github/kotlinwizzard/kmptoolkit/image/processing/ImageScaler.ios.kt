package io.github.kotlinwizzard.kmptoolkit.image.processing

import io.github.kotlinwizzard.kmptoolkit.image.camera.ui.toByteArray
import io.github.kotlinwizzard.kmptoolkit.image.camera.ui.toUiImage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext

actual object ImageScaler {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun resize(
        imageData: ByteArray,
        width: Int,
        height: Int,
        mode: ImageResizeMode,
        allowUpscaling: Boolean,
        jpegQuality: Double,
    ): ByteArray {
        validateResizeArguments(imageData, jpegQuality)
        require(width > 0 && height > 0) { "Target dimensions must be greater than zero" }
        val source = imageData.toUiImage() ?: throw IllegalArgumentException("Could not decode image")
        val sourceWidth = source.size.useContents { width }.times(source.scale).toInt()
        val sourceHeight = source.size.useContents { height }.times(source.scale).toInt()
        val geometry = calculateResizeGeometry(sourceWidth, sourceHeight, width, height, mode, allowUpscaling)
        if (geometry.outputWidth == sourceWidth && geometry.outputHeight == sourceHeight &&
            geometry.drawWidth == sourceWidth && geometry.drawHeight == sourceHeight
        ) return imageData

        val outputSize = CGSizeMake(geometry.outputWidth.toDouble(), geometry.outputHeight.toDouble())
        UIGraphicsBeginImageContextWithOptions(outputSize, true, 1.0)
        val output = try {
            source.drawInRect(
                CGRectMake(
                    geometry.offsetX.toDouble(),
                    geometry.offsetY.toDouble(),
                    geometry.drawWidth.toDouble(),
                    geometry.drawHeight.toDouble(),
                ),
            )
            UIGraphicsGetImageFromCurrentImageContext()
                ?: throw IllegalStateException("Could not create resized image")
        } finally {
            UIGraphicsEndImageContext()
        }
        return output.toByteArray(jpegQuality)
    }
}
