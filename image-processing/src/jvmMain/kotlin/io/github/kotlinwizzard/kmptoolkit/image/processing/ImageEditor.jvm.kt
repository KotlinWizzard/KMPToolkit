package io.github.kotlinwizzard.kmptoolkit.image.processing

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

actual object ImageEditor {

    actual suspend fun applyBrightness(bitmapData: ByteArray, factor: Float): ByteArray {
        val adjustedFactor = (factor.coerceIn(-1f, 1f) * 255f).coerceIn(-255f, 255f)
        val m = floatArrayOf(
            1f, 0f, 0f, 0f, adjustedFactor,
            0f, 1f, 0f, 0f, adjustedFactor,
            0f, 0f, 1f, 0f, adjustedFactor,
            0f, 0f, 0f, 1f, 0f
        )
        return applyColorMatrix(bitmapData, m)
    }

    actual suspend fun applyGreyscale(bitmapData: ByteArray): ByteArray {
        return applyColorMatrix(bitmapData, saturationMatrix(0f))
    }

    actual suspend fun applySepia(bitmapData: ByteArray): ByteArray {
        val intensity = 1.3f

        val base = floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f,     0f,     0f,     1f, 0f
        )

        val m = base.copyOf()
        scaleRgbRowsInPlace(m, intensity)
        return applyColorMatrix(bitmapData, m)
    }

    actual suspend fun applyContrast(bitmapData: ByteArray, factor: Float): ByteArray {
        val clamped = factor.coerceIn(-1f, 1f)
        val contrast = if (clamped <= 0f) (clamped + 1f) else (clamped * 2f) + 1f

        val scale = abs(contrast)
        val translate = 128f * (1f - scale)

        val m = floatArrayOf(
            scale, 0f,   0f,   0f, translate,
            0f,   scale, 0f,   0f, translate,
            0f,   0f,   scale, 0f, translate,
            0f,   0f,   0f,   1f, 0f
        )
        return applyColorMatrix(bitmapData, m)
    }

    actual suspend fun applySaturation(bitmapData: ByteArray, factor: Float): ByteArray {
        val clamped = factor.coerceIn(-1f, 1f)
        val saturation = if (clamped <= 0f) (clamped + 1f) else (clamped * 5f) + 1f
        return applyColorMatrix(bitmapData, saturationMatrix(saturation))
    }

    actual suspend fun applyExposure(bitmapData: ByteArray, ev: Float): ByteArray {
        val clamped = ev.coerceIn(-1f, 1f).toDouble()
        val exposure = if (clamped <= 0.0) (clamped + 1.0) else (clamped * 4.0) + 1.0
        val e = exposure.toFloat()

        val m = floatArrayOf(
            e,  0f, 0f, 0f, 0f,
            0f, e,  0f, 0f, 0f,
            0f, 0f, e,  0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
        return applyColorMatrix(bitmapData, m)
    }

    actual suspend fun applyTemperature(bitmapData: ByteArray, temperature: Float): ByteArray {
        val clamped = temperature.coerceIn(-1f, 1f)
        val r = 1f + clamped
        val b = 1f - clamped

        val m = floatArrayOf(
            r,  0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, b,  0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
        return applyColorMatrix(bitmapData, m)
    }

    actual suspend fun applyHue(bitmapData: ByteArray, angleDegrees: Float): ByteArray {
        val angle = angleDegrees.coerceIn(-180f, 180f)
        val img = decodeToArgb(bitmapData)

        val w = img.width
        val h = img.height
        val out = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)

        for (y in 0 until h) {
            for (x in 0 until w) {
                val argb = img.getRGB(x, y)
                val a = (argb ushr 24) and 0xFF
                val r = (argb ushr 16) and 0xFF
                val g = (argb ushr 8) and 0xFF
                val b = (argb) and 0xFF

                val hsv = rgbToHsv(r.toFloat(), g.toFloat(), b.toFloat())
                hsv[0] = ((hsv[0] + angle) % 360f + 360f) % 360f
                val rgb = hsvToRgb(hsv[0], hsv[1], hsv[2])

                val nr = rgb[0].roundToInt().coerceIn(0, 255)
                val ng = rgb[1].roundToInt().coerceIn(0, 255)
                val nb = rgb[2].roundToInt().coerceIn(0, 255)

                out.setRGB(x, y, (a shl 24) or (nr shl 16) or (ng shl 8) or nb)
            }
        }

        return encodeJpeg(out)
    }

    private fun applyColorMatrix(jpegBytes: ByteArray, matrix: FloatArray): ByteArray {
        require(matrix.size == 20) { "ColorMatrix must have 20 elements (4x5)" }
        val src = decodeToArgb(jpegBytes)
        val w = src.width
        val h = src.height
        val out = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)

        for (y in 0 until h) {
            for (x in 0 until w) {
                val argb = src.getRGB(x, y)
                val a = ((argb ushr 24) and 0xFF).toFloat()
                val r = ((argb ushr 16) and 0xFF).toFloat()
                val g = ((argb ushr 8) and 0xFF).toFloat()
                val b = ((argb) and 0xFF).toFloat()

                val nr = matrix[0] * r + matrix[1] * g + matrix[2] * b + matrix[3] * a + matrix[4]
                val ng = matrix[5] * r + matrix[6] * g + matrix[7] * b + matrix[8] * a + matrix[9]
                val nb = matrix[10] * r + matrix[11] * g + matrix[12] * b + matrix[13] * a + matrix[14]
                val na = matrix[15] * r + matrix[16] * g + matrix[17] * b + matrix[18] * a + matrix[19]

                val ir = nr.roundToInt().coerceIn(0, 255)
                val ig = ng.roundToInt().coerceIn(0, 255)
                val ib = nb.roundToInt().coerceIn(0, 255)
                val ia = na.roundToInt().coerceIn(0, 255)

                out.setRGB(x, y, (ia shl 24) or (ir shl 16) or (ig shl 8) or ib)
            }
        }

        return encodeJpeg(out)
    }

    private fun decodeToArgb(jpegBytes: ByteArray): BufferedImage {
        val input = ImageIO.read(ByteArrayInputStream(jpegBytes))
            ?: throw IllegalArgumentException("Could not decode image")

        // In ARGB kopieren, damit getRGB/setRGB konsistent ist
        val argb = BufferedImage(input.width, input.height, BufferedImage.TYPE_INT_ARGB)
        val g = argb.createGraphics()
        g.drawImage(input, 0, 0, null)
        g.dispose()
        return argb
    }

    private fun encodeJpeg(imgArgb: BufferedImage): ByteArray {
        // JPEG hat kein Alpha, daher nach RGB konvertieren (wie bei Android compress JPEG)
        val rgb = BufferedImage(imgArgb.width, imgArgb.height, BufferedImage.TYPE_INT_RGB)
        val g = rgb.createGraphics()
        g.drawImage(imgArgb, 0, 0, null)
        g.dispose()

        val baos = ByteArrayOutputStream()
        ImageIO.write(rgb, "jpeg", baos)
        return baos.toByteArray()
    }

    private fun saturationMatrix(s: Float): FloatArray {
        val lumR = 0.213f
        val lumG = 0.715f
        val lumB = 0.072f

        val inv = 1f - s
        val r = lumR * inv
        val g = lumG * inv
        val b = lumB * inv

        return floatArrayOf(
            r + s, g,     b,     0f, 0f,
            r,     g + s, b,     0f, 0f,
            r,     g,     b + s, 0f, 0f,
            0f,    0f,    0f,    1f, 0f
        )
    }

    private fun scaleRgbRowsInPlace(m: FloatArray, intensity: Float) {
        for (row in 0 until 3) {
            val base = row * 5
            m[base + 0] *= intensity
            m[base + 1] *= intensity
            m[base + 2] *= intensity
            m[base + 3] *= intensity
            m[base + 4] *= intensity
        }
    }

    // HSV helpers (Hue in Grad 0..360, S/V 0..1)
    private fun rgbToHsv(r255: Float, g255: Float, b255: Float): FloatArray {
        val r = r255 / 255f
        val g = g255 / 255f
        val b = b255 / 255f

        val maxC = max(r, max(g, b))
        val minC = min(r, min(g, b))
        val delta = maxC - minC

        val h = when {
            delta == 0f -> 0f
            maxC == r -> 60f * (((g - b) / delta) % 6f)
            maxC == g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }.let { if (it < 0f) it + 360f else it }

        val s = if (maxC == 0f) 0f else delta / maxC
        val v = maxC

        return floatArrayOf(h, s, v)
    }

    private fun hsvToRgb(h: Float, s: Float, v: Float): FloatArray {
        val c = v * s
        val x = c * (1f - abs(((h / 60f) % 2f) - 1f))
        val m = v - c

        val (rp, gp, bp) = when {
            h < 60f -> floatArrayOf(c, x, 0f)
            h < 120f -> floatArrayOf(x, c, 0f)
            h < 180f -> floatArrayOf(0f, c, x)
            h < 240f -> floatArrayOf(0f, x, c)
            h < 300f -> floatArrayOf(x, 0f, c)
            else -> floatArrayOf(c, 0f, x)
        }

        val r = (rp + m) * 255f
        val g = (gp + m) * 255f
        val b = (bp + m) * 255f
        return floatArrayOf(r, g, b)
    }
}
