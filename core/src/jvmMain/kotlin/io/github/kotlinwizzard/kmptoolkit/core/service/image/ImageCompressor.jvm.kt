package io.github.kotlinwizzard.kmptoolkit.core.service.image

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import javax.imageio.ImageWriter
import javax.imageio.plugins.jpeg.JPEGImageWriteParam

actual fun ImageCompressor.compressImage(
    content: ByteArray,
    compressionRatio: Float
): ByteArray {
    val quality = compressionRatio.coerceIn(0f, 1f)

    // 2) Originalbild einlesen
    val originalImage = ByteArrayInputStream(content).use { inputStream ->
        ImageIO.read(inputStream)
    } ?: return content // wenn kein Bild erkannt wird, original zurückgeben

    // 3) OutputStream vorbereiten
    val outputStream = ByteArrayOutputStream()

    // 4) Writer holen
    val writer: ImageWriter = ImageIO.getImageWritersByFormatName("jpeg").next()
    val writeParam = writer.defaultWriteParam.apply {
        compressionMode = JPEGImageWriteParam.MODE_EXPLICIT
        compressionQuality = quality
    }

    // 5) Output **setzen**
    ImageIO.createImageOutputStream(outputStream).use { ios ->
        writer.output = ios
        writer.write(null, javax.imageio.IIOImage(originalImage, null, null), writeParam)
    }

    writer.dispose()

    // 6) Ergebnis-Bytes zurückgeben
    return outputStream.toByteArray()
}