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
    val quality = (compressionRatio * 100).toInt().coerceIn(0, 100)
    val inputStream = ByteArrayInputStream(content)
    val originalImage = ImageIO.read(inputStream)
    val outputStream = ByteArrayOutputStream()
    val writer: ImageWriter = ImageIO.getImageWritersByFormatName("jpeg").next()
    val writeParam = JPEGImageWriteParam(null)
    writeParam.compressionMode = JPEGImageWriteParam.MODE_EXPLICIT
    writeParam.compressionQuality = quality / 100.0f

    val imageOut = writer.output
    writer.output = imageOut

    writer.write(null, javax.imageio.IIOImage(originalImage, null, null), writeParam)

    return outputStream.toByteArray()
}