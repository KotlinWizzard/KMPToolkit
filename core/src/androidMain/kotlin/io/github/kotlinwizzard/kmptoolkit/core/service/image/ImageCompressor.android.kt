package io.github.kotlinwizzard.kmptoolkit.core.service.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import org.jetbrains.compose.resources.decodeToImageBitmap
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

actual fun ImageCompressor.compressImage(
    content: ByteArray,
    compressionRatio: Float
): ByteArray {
    val quality = (compressionRatio * 100).toInt().coerceIn(0, 100)
    val bitmap = BitmapFactory.decodeByteArray(content, 0, content.size)
    val rotation = getImageOrientation(content)
    val outputStream = ByteArrayOutputStream()
    val rotatedBitmap = getBitmapRotatedByDegree(bitmap, rotation)
    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    return outputStream.toByteArray()
}

private fun getImageOrientation(byteArray: ByteArray): Int {
    val tempFile = ByteArrayInputStream(byteArray)
    val exif = ExifInterface(tempFile)
    return runCatching {
        when (exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }.getOrNull() ?: 0
}

private fun getBitmapRotatedByDegree(bitmap: Bitmap, rotationDegree: Int): Bitmap {
    val matrix = Matrix()
    matrix.preRotate(rotationDegree.toFloat())

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}