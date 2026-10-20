package io.github.kotlinwizzard.kmptoolkit.image.processing

import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

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
        val source = ImageIO.read(ByteArrayInputStream(imageData))
            ?: throw IllegalArgumentException("Could not decode image")
        val geometry = calculateResizeGeometry(source.width, source.height, width, height, mode, allowUpscaling)
        if (geometry.outputWidth == source.width && geometry.outputHeight == source.height &&
            geometry.drawWidth == source.width && geometry.drawHeight == source.height
        ) return imageData

        val output = BufferedImage(geometry.outputWidth, geometry.outputHeight, BufferedImage.TYPE_INT_RGB)
        output.createGraphics().use { graphics ->
            graphics.color = Color.BLACK
            graphics.fillRect(0, 0, output.width, output.height)
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            graphics.drawImage(
                source,
                geometry.offsetX,
                geometry.offsetY,
                geometry.offsetX + geometry.drawWidth,
                geometry.offsetY + geometry.drawHeight,
                0,
                0,
                source.width,
                source.height,
                null,
            )
        }
        return encodeJpeg(output, jpegQuality)
    }
}

private inline fun <T : java.awt.Graphics, R> T.use(block: (T) -> R): R = try {
    block(this)
} finally {
    dispose()
}

private fun encodeJpeg(image: BufferedImage, quality: Double): ByteArray {
    val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
    return ByteArrayOutputStream().use { output ->
        ImageIO.createImageOutputStream(output).use { imageOutput ->
            writer.output = imageOutput
            val parameters = writer.defaultWriteParam.apply {
                compressionMode = ImageWriteParam.MODE_EXPLICIT
                compressionQuality = quality.toFloat()
            }
            writer.write(null, IIOImage(image, null, null), parameters)
        }
        writer.dispose()
        output.toByteArray()
    }
}
