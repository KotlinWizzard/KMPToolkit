package io.github.kotlinwizzard.kmptoolkit.image.processing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import java.io.ByteArrayOutputStream

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
        val source = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            ?: throw IllegalArgumentException("Could not decode image")
        val geometry = calculateResizeGeometry(source.width, source.height, width, height, mode, allowUpscaling)
        if (geometry.outputWidth == source.width && geometry.outputHeight == source.height &&
            geometry.drawWidth == source.width && geometry.drawHeight == source.height
        ) return imageData

        val output = Bitmap.createBitmap(geometry.outputWidth, geometry.outputHeight, Bitmap.Config.ARGB_8888)
        Canvas(output).drawBitmap(
            source,
            null,
            Rect(
                geometry.offsetX,
                geometry.offsetY,
                geometry.offsetX + geometry.drawWidth,
                geometry.offsetY + geometry.drawHeight,
            ),
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
        )
        return ByteArrayOutputStream().use { stream ->
            output.compress(Bitmap.CompressFormat.JPEG, (jpegQuality * 100).toInt(), stream)
            stream.toByteArray()
        }.also {
            if (output !== source) output.recycle()
            source.recycle()
        }
    }
}
