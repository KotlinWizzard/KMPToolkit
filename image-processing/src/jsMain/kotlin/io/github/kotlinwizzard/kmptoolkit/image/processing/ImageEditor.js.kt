package io.github.kotlinwizzard.kmptoolkit.image.processing
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.Uint8ClampedArray
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.ImageBitmap
import kotlin.js.Promise
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

actual object ImageEditor {
    actual suspend fun applyBrightness(bitmapData: ByteArray, factor: Float): ByteArray {
        val adjustedFactor = (factor.coerceIn(-1F, 1F) * 255f).coerceIn(-255f, 255f)
        val m = floatArrayOf(
            1f, 0f, 0f, 0f, adjustedFactor,
            0f, 1f, 0f, 0f, adjustedFactor,
            0f, 0f, 1f, 0f, adjustedFactor,
            0f, 0f, 0f, 1f, 0f
        )
        return applyColorMatrix(bitmapData, m)
    }

    actual suspend fun applyGreyscale(bitmapData: ByteArray): ByteArray {
        // Android: setSaturation(0f)
        val m = saturationMatrix(0f)
        return applyColorMatrix(bitmapData, m)
    }

    actual suspend fun applySepia(bitmapData: ByteArray): ByteArray {
        // Android: base sepia, dann postConcat(scale(intensity))
        val intensity = 1.3f

        val base = floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f,     0f,     0f,     1f, 0f
        )

        // scale AFTER base (RGB only), entspricht in Wirkung deinem Android Setup
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
        val m = saturationMatrix(saturation)
        return applyColorMatrix(bitmapData, m)
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
        return edit(bitmapData) { data ->
            applyPerPixelRgba(data) { r, g, b, a ->
                val hsv = rgbToHsv(r, g, b)
                hsv[0] = ((hsv[0] + angle) % 360f + 360f) % 360f
                val rgb = hsvToRgb(hsv[0], hsv[1], hsv[2])
                floatArrayOf(rgb[0], rgb[1], rgb[2], a)
            }
        }
    }

    private suspend fun applyColorMatrix(jpegBytes: ByteArray, matrix: FloatArray): ByteArray {
        require(matrix.size == 20) { "ColorMatrix must have 20 elements (4x5)" }
        return edit(jpegBytes) { data ->
            applyPerPixelRgba(data) { r, g, b, a ->
                val nr = matrix[0] * r + matrix[1] * g + matrix[2] * b + matrix[3] * a + matrix[4]
                val ng = matrix[5] * r + matrix[6] * g + matrix[7] * b + matrix[8] * a + matrix[9]
                val nb = matrix[10] * r + matrix[11] * g + matrix[12] * b + matrix[13] * a + matrix[14]
                val na = matrix[15] * r + matrix[16] * g + matrix[17] * b + matrix[18] * a + matrix[19]
                floatArrayOf(nr, ng, nb, na)
            }
        }
    }

    private suspend fun edit(
        jpegBytes: ByteArray,
        mutate: (Uint8ClampedArray) -> Unit
    ): ByteArray {
        val canvas = (document.createElement("canvas") as HTMLCanvasElement)
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D

        val blob = jpegBytesToBlob(jpegBytes)
        val imageBitmap = window.asDynamic().createImageBitmap(blob).unsafeCast<Promise<ImageBitmap>>().await()

        canvas.width = imageBitmap.width
        canvas.height = imageBitmap.height

        ctx.drawImage(imageBitmap, 0.0, 0.0)

        val imageData = ctx.getImageData(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
        val data = imageData.data

        mutate(data)

        ctx.putImageData(imageData, 0.0, 0.0)

        val outBlob = canvasToJpegBlob(canvas)
        return blobToByteArray(outBlob)
    }




    private inline fun applyPerPixelRgba(
        data: Uint8ClampedArray,
        transform: (r: Float, g: Float, b: Float, a: Float) -> FloatArray
    ) {
        val arr = data.asDynamic()
        val len = (arr.length as Int)
        val n = len - (len % 4)

        var i = 0
        while (i + 3 < n) {
            val r = (arr[i] as Number).toInt().toFloat()
            val g = (arr[i + 1] as Number).toInt().toFloat()
            val b = (arr[i + 2] as Number).toInt().toFloat()
            val a = (arr[i + 3] as Number).toInt().toFloat()

            val out = transform(r, g, b, a)

            arr[i] = out[0].roundToInt().coerceIn(0, 255)
            arr[i + 1] = out[1].roundToInt().coerceIn(0, 255)
            arr[i + 2] = out[2].roundToInt().coerceIn(0, 255)
            arr[i + 3] = out[3].roundToInt().coerceIn(0, 255)

            i += 4
        }
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
        // Skaliert nur die RGB Zeilen (je 5 Werte), Alpha-Zeile bleibt.
        for (row in 0 until 3) {
            val base = row * 5
            m[base + 0] *= intensity
            m[base + 1] *= intensity
            m[base + 2] *= intensity
            m[base + 3] *= intensity
            m[base + 4] *= intensity
        }
    }


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