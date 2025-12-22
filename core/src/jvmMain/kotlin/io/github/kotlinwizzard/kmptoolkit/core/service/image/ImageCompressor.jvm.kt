package io.github.kotlinwizzard.kmptoolkit.core.service.image
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.plugins.jpeg.JPEGImageWriteParam

actual fun ImageCompressor.compressImage(
    content: ByteArray,
    compressionRatio: Float
): ByteArray {
    val quality = compressionRatio.coerceIn(0f, 1f)

    // Originalbild einlesen
    val originalImage: BufferedImage = ByteArrayInputStream(content).use { inputStream ->
        ImageIO.read(inputStream)
    } ?: return content // wenn kein Bild erkannt wird

    // In RGB-Bild ohne Alpha konvertieren
    val rgbImage = if (originalImage.type == BufferedImage.TYPE_INT_RGB) {
        originalImage
    } else {
        val converted = BufferedImage(
            originalImage.width,
            originalImage.height,
            BufferedImage.TYPE_INT_RGB
        )
        val g: Graphics2D = converted.createGraphics()
        try {
            // Hintergrundfarbe wählen, falls Original Transparenz hat
            g.color = Color.WHITE
            g.fillRect(0, 0, converted.width, converted.height)
            g.drawImage(originalImage, 0, 0, null)
        } finally {
            g.dispose()
        }
        converted
    }

    val outputStream = ByteArrayOutputStream()

    // JPEG-Writer holen
    val writer: ImageWriter = ImageIO.getImageWritersByFormatName("jpeg").asSequence().firstOrNull()
        ?: return content // kein Writer verfügbar

    val writeParam: ImageWriteParam = (writer.defaultWriteParam as JPEGImageWriteParam).apply {
        compressionMode = JPEGImageWriteParam.MODE_EXPLICIT
        compressionQuality = quality
    }

    try {
        ImageIO.createImageOutputStream(outputStream).use { ios ->
            writer.output = ios
            writer.write(null, IIOImage(rgbImage, null, null), writeParam)
        }
    } finally {
        writer.dispose()
    }

    return outputStream.toByteArray()
}
