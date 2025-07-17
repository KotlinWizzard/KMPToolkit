package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.processing

actual object ImageEditor {
    actual suspend fun applyBrightness(
        bitmapData: ByteArray,
        factor: Float
    ): ByteArray {
        TODO("Not yet implemented")
    }

    actual suspend fun applyGreyscale(bitmapData: ByteArray): ByteArray {
        TODO("Not yet implemented")
    }

    actual suspend fun applySepia(bitmapData: ByteArray): ByteArray {
        TODO("Not yet implemented")
    }

    actual suspend fun applyContrast(
        bitmapData: ByteArray,
        factor: Float
    ): ByteArray {
        TODO("Not yet implemented")
    }

    actual suspend fun applySaturation(
        bitmapData: ByteArray,
        factor: Float
    ): ByteArray {
        TODO("Not yet implemented")
    }

    actual suspend fun applyExposure(
        bitmapData: ByteArray,
        ev: Float
    ): ByteArray {
        TODO("Not yet implemented")
    }

    actual suspend fun applyTemperature(
        bitmapData: ByteArray,
        temperature: Float
    ): ByteArray {
        TODO("Not yet implemented")
    }

    actual suspend fun applyHue(
        bitmapData: ByteArray,
        angleDegrees: Float
    ): ByteArray {
        TODO("Not yet implemented")
    }
}