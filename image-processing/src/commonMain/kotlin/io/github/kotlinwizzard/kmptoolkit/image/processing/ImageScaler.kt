package io.github.kotlinwizzard.kmptoolkit.image.processing

/** Defines how an image is mapped to the requested size. */
enum class ImageResizeMode {
    /** Produces the exact requested size and may change the aspect ratio. */
    Stretch,

    /** Preserves the aspect ratio and keeps the whole image inside the requested bounds. */
    Fit,

    /** Preserves the aspect ratio, fills the requested bounds and crops the excess. */
    Crop,
}

/** Cross-platform image scaling operations. The returned image is JPEG encoded. */
expect object ImageScaler {
    /**
     * Resizes [imageData] according to [mode].
     *
     * When [allowUpscaling] is false, an image that already fits completely inside the
     * requested bounds is returned unchanged. [jpegQuality] must be between 0.0 and 1.0.
     */
    suspend fun resize(
        imageData: ByteArray,
        width: Int,
        height: Int,
        mode: ImageResizeMode = ImageResizeMode.Fit,
        allowUpscaling: Boolean = false,
        jpegQuality: Double = 0.92,
    ): ByteArray
}

/** Resizes an image proportionally so it fits inside [maxWidth] and [maxHeight]. */
suspend fun ImageScaler.resizeToFit(
    imageData: ByteArray,
    maxWidth: Int,
    maxHeight: Int,
    allowUpscaling: Boolean = false,
    jpegQuality: Double = 0.92,
): ByteArray = resize(
    imageData = imageData,
    width = maxWidth,
    height = maxHeight,
    mode = ImageResizeMode.Fit,
    allowUpscaling = allowUpscaling,
    jpegQuality = jpegQuality,
)

/** Resizes an image proportionally so neither dimension exceeds [maxSize]. */
suspend fun ImageScaler.resizeToMaxSize(
    imageData: ByteArray,
    maxSize: Int,
    allowUpscaling: Boolean = false,
    jpegQuality: Double = 0.92,
): ByteArray = resizeToFit(
    imageData = imageData,
    maxWidth = maxSize,
    maxHeight = maxSize,
    allowUpscaling = allowUpscaling,
    jpegQuality = jpegQuality,
)

internal data class ImageResizeGeometry(
    val outputWidth: Int,
    val outputHeight: Int,
    val drawWidth: Int,
    val drawHeight: Int,
    val offsetX: Int,
    val offsetY: Int,
)

internal fun calculateResizeGeometry(
    sourceWidth: Int,
    sourceHeight: Int,
    targetWidth: Int,
    targetHeight: Int,
    mode: ImageResizeMode,
    allowUpscaling: Boolean,
): ImageResizeGeometry {
    require(sourceWidth > 0 && sourceHeight > 0) { "Source dimensions must be greater than zero" }
    require(targetWidth > 0 && targetHeight > 0) { "Target dimensions must be greater than zero" }

    if (!allowUpscaling && sourceWidth <= targetWidth && sourceHeight <= targetHeight) {
        return ImageResizeGeometry(sourceWidth, sourceHeight, sourceWidth, sourceHeight, 0, 0)
    }

    if (mode == ImageResizeMode.Stretch) {
        val outputWidth = if (allowUpscaling) targetWidth else minOf(targetWidth, sourceWidth)
        val outputHeight = if (allowUpscaling) targetHeight else minOf(targetHeight, sourceHeight)
        return ImageResizeGeometry(outputWidth, outputHeight, outputWidth, outputHeight, 0, 0)
    }

    val widthRatio = targetWidth.toDouble() / sourceWidth
    val heightRatio = targetHeight.toDouble() / sourceHeight
    val requestedScale = if (mode == ImageResizeMode.Fit) {
        minOf(widthRatio, heightRatio)
    } else {
        maxOf(widthRatio, heightRatio)
    }
    val scale = if (allowUpscaling) requestedScale else minOf(1.0, requestedScale)
    val drawWidth = maxOf(1, (sourceWidth * scale).toInt())
    val drawHeight = maxOf(1, (sourceHeight * scale).toInt())

    if (mode == ImageResizeMode.Fit) {
        return ImageResizeGeometry(drawWidth, drawHeight, drawWidth, drawHeight, 0, 0)
    }

    val outputWidth = minOf(targetWidth, drawWidth)
    val outputHeight = minOf(targetHeight, drawHeight)
    return ImageResizeGeometry(
        outputWidth = outputWidth,
        outputHeight = outputHeight,
        drawWidth = drawWidth,
        drawHeight = drawHeight,
        offsetX = (outputWidth - drawWidth) / 2,
        offsetY = (outputHeight - drawHeight) / 2,
    )
}

internal fun validateResizeArguments(imageData: ByteArray, jpegQuality: Double) {
    require(imageData.isNotEmpty()) { "Image data must not be empty" }
    require(jpegQuality in 0.0..1.0) { "JPEG quality must be between 0.0 and 1.0" }
}
