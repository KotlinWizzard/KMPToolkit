package com.kmptoolkit.core.service.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

actual fun ImageCompressor.compressImage(
    content: ByteArray,
    compressionRatio: Float
): ByteArray {
    val quality = (compressionRatio * 100).toInt().coerceIn(0, 100)
    val bitmap = BitmapFactory.decodeByteArray(content, 0, content.size)
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    return outputStream.toByteArray()
}