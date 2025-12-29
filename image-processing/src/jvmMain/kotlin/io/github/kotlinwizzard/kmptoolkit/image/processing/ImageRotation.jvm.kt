package io.github.kotlinwizzard.kmptoolkit.image.processing

import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

actual suspend fun ImageRotation.Companion.rotateImage(
    byteArray: ByteArray,
    rotateBy: ImageRotation
): ByteArray {
    val normalized = normalizeDegrees(rotateBy.rotation)
    if (normalized == 0) return byteArray

    val input = ImageIO.read(ByteArrayInputStream(byteArray))
        ?: throw IllegalArgumentException("Could not decode image")

    val srcW = input.width
    val srcH = input.height

    val (dstW, dstH) = if (normalized == 90 || normalized == 270) srcH to srcW else srcW to srcH

    // JPEG hat kein Alpha, daher TYPE_INT_RGB
    val outImg = BufferedImage(dstW, dstH, BufferedImage.TYPE_INT_RGB)
    val g = outImg.createGraphics()
    configure(g)

    val at = AffineTransform()
    at.translate(dstW / 2.0, dstH / 2.0)
    at.rotate(Math.toRadians(normalized.toDouble()))
    at.translate(-srcW / 2.0, -srcH / 2.0)

    g.drawImage(input, at, null)
    g.dispose()

    val baos = ByteArrayOutputStream()
    ImageIO.write(outImg, "jpeg", baos)
    return baos.toByteArray()
}

private fun normalizeDegrees(deg: Int): Int {
    val n = ((deg % 360) + 360) % 360
    // falls je etwas anderes reinkommt, auf die nächste 90° Stufe runden:
    return when (n) {
        0, 90, 180, 270 -> n
        else -> ((n + 45) / 90) * 90 % 360
    }
}

private fun configure(g: Graphics2D) {
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
}
