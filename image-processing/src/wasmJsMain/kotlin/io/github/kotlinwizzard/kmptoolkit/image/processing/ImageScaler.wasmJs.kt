package io.github.kotlinwizzard.kmptoolkit.image.processing

import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

actual object ImageScaler {
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
        val source = decodeJpegToImageBitmap(imageData)
        val geometry = calculateResizeGeometry(source.width, source.height, width, height, mode, allowUpscaling)
        if (geometry.outputWidth == source.width && geometry.outputHeight == source.height &&
            geometry.drawWidth == source.width && geometry.drawHeight == source.height
        ) return imageData

        val canvas = document.createElement("canvas") as HTMLCanvasElement
        canvas.width = geometry.outputWidth
        canvas.height = geometry.outputHeight
        val context = canvas.getContext("2d") as CanvasRenderingContext2D
        context.drawImage(
            source,
            geometry.offsetX.toDouble(),
            geometry.offsetY.toDouble(),
            geometry.drawWidth.toDouble(),
            geometry.drawHeight.toDouble(),
        )
        return blobToByteArray(canvasToJpegBlob(canvas, jpegQuality))
    }
}
