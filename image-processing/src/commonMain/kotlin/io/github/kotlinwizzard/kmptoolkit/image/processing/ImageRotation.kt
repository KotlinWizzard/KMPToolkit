package io.github.kotlinwizzard.kmptoolkit.image.processing

import io.github.kotlinwizzard.kmptoolkit.core.extensions.IO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class ImageRotation(val rotation: Int) {
    DegreeNegative90(-90),
    DegreeNegative180(-180),
    DegreeNegative270(-270),
    DegreeNegative360(-360),
    Degree90(90),
    Degree180(180),
    Degree270(270),
    Degree360(360);

    companion object
}

expect suspend fun ImageRotation.Companion.rotateImage(
    byteArray: ByteArray,
    rotateBy: ImageRotation
): ByteArray


fun ImageRotation.Companion.rotateImage(
    byteArray: ByteArray,
    rotateBy: ImageRotation,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    callback: (ByteArray) -> Unit
) {
    scope.launch {
       val bytes = rotateImage(byteArray,rotateBy)
        callback(bytes)
    }
}