package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.processing

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import java.io.ByteArrayOutputStream

actual object ImageEditor {
    private val compressFormat = Bitmap.CompressFormat.JPEG
    actual suspend fun applyBrightness(
        bitmapData: ByteArray,
        factor: Float
    ): ByteArray {
        val adjustedFactor = (factor.coerceIn(-1F, 1F) * 255).coerceIn(-255F, 255F)
        val bmp = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.size)
        val colorMatrix = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, adjustedFactor,
                0f, 1f, 0f, 0f, adjustedFactor,
                0f, 0f, 1f, 0f, adjustedFactor,
                0f, 0f, 0f, 1f, 0f,
            )
        )
        return applyColorMatrix(bmp, colorMatrix)
    }

    actual suspend fun applyGreyscale(bitmapData: ByteArray): ByteArray {
        val bmp = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.size)
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)
        return applyColorMatrix(bmp, matrix)
    }

    actual suspend fun applySepia(bitmapData: ByteArray): ByteArray {
        val bmp = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.size)
        val sepiaMatrix = createSepiaMatrix(1.3F)
        return applyColorMatrix(bmp, sepiaMatrix)
    }

    private fun createSepiaMatrix(intensity: Float): ColorMatrix {
        val base = ColorMatrix(
            floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )

        val identity = ColorMatrix()
        identity.setConcat(base, identity)
        identity.postConcat(ColorMatrix().apply { setScale(intensity, intensity, intensity, 1f) })

        return identity
    }

    actual suspend fun applyContrast(
        bitmapData: ByteArray,
        factor: Float
    ): ByteArray {
        val bmp = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.size)
        val clamped = factor.coerceIn(-1f, 1f)
        val contrast = when {
            clamped <= 0f -> (clamped + 1f)
            else -> (clamped * 2) + 1f
        }

        val scale = kotlin.math.abs(contrast)
        val translate = 128f * (1f - scale)

        val baseMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )

        return applyColorMatrix(bmp, baseMatrix)
    }

    actual suspend fun applySaturation(
        bitmapData: ByteArray,
        factor: Float
    ): ByteArray {
        val clamped = factor.coerceIn(-1f, 1f)
        val saturation = when {
            clamped <= 0f -> (clamped + 1f)
            else -> (clamped * 5) + 1f
        }
        val bmp = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.size)
        val matrix = ColorMatrix()
        matrix.setSaturation(saturation)
        return applyColorMatrix(bmp, matrix)
    }

    actual suspend fun applyExposure(
        bitmapData: ByteArray,
        ev: Float
    ): ByteArray {
        val bmp = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.size)
        val clamped = ev.coerceIn(-1f, 1f).toDouble()
        val exposure = when {
            clamped <= 0f -> (clamped + 1f)
            else -> (clamped * 4) + 1f
        }.toFloat()
        val matrix = ColorMatrix(
            floatArrayOf(
                exposure, 0f, 0f, 0f, 0f,
                0f, exposure, 0f, 0f, 0f,
                0f, 0f, exposure, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        return applyColorMatrix(bmp, matrix)
    }

    actual suspend fun applyTemperature(
        bitmapData: ByteArray,
        temperature: Float
    ): ByteArray {
        val bmp = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.size)
        val clamped = temperature.coerceIn(-1f, 1f)
        val r = 1f + clamped
        val b = 1f - clamped
        val matrix = ColorMatrix(
            floatArrayOf(
                r, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, b, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        return applyColorMatrix(bmp, matrix)
    }

    actual suspend fun applyHue(
        bitmapData: ByteArray,
        angleDegrees: Float
    ): ByteArray {
        val bmp = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.size)
        val width = bmp.width
        val height = bmp.height

        val pixels = IntArray(width * height)
        bmp.getPixels(pixels, 0, width, 0, 0, width, height)

        val hsv = FloatArray(3)
        pixels.indices.chunked(5000).parallelStream().forEach { chunk ->
            val localHSV = FloatArray(3) // avoid shared mutable state
            for (i in chunk) {
                Color.colorToHSV(pixels[i], localHSV)
                localHSV[0] = ((localHSV[0] + angleDegrees.coerceIn(-180F,180F)) % 360 + 360) % 360
                pixels[i] = Color.HSVToColor(Color.alpha(pixels[i]), localHSV)
            }
        }

        val result = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)

        val output = ByteArrayOutputStream()
        result.compress(Bitmap.CompressFormat.JPEG, 100, output)
        return output.toByteArray()
    }

    private fun applyColorMatrix(bmp: Bitmap, matrix: ColorMatrix): ByteArray {
        val result = createBitmap(bmp.width, bmp.height, bmp.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(bmp, 0f, 0f, paint)

        val output = ByteArrayOutputStream()
        result.compress(compressFormat, 100, output)
        return output.toByteArray()
    }
}