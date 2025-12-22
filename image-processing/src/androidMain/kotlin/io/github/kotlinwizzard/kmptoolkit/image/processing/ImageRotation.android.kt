package io.github.kotlinwizzard.kmptoolkit.image.processing

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.ui.graphics.asAndroidBitmap
import io.github.kotlinwizzard.kmptoolkit.image.core.toByteArray
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

@OptIn(ExperimentalResourceApi::class)
actual fun ImageRotation.Companion.rotateImage(
    byteArray: ByteArray,
    rotateBy: ImageRotation
) :ByteArray{
    val bitmap = byteArray.decodeToImageBitmap().asAndroidBitmap()
   return bitmap.rotate(rotateBy.rotation.toFloat()).toByteArray()
}


fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}