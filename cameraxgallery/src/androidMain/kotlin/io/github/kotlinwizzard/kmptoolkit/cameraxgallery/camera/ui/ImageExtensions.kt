package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.camera.ui

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

internal fun ImageProxy.toByteArray(): ByteArray {
    val rotationDegrees = imageInfo.rotationDegrees
    val bitmap = toBitmap()
    val rotatedData =
        if (rotationDegrees != 0) {
            bitmap.rotate(rotationDegrees)
        } else {
            bitmap.toByteArray()
        }
    close()

    return rotatedData
}

internal fun Bitmap.toByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    return stream.toByteArray()
}

internal fun Bitmap.rotate(degrees: Int): ByteArray {
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    val rotatedBitmap = Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
    return rotatedBitmap.toByteArray()
}