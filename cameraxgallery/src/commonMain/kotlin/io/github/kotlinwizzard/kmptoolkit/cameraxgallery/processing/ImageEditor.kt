package io.github.kotlinwizzard.kmptoolkit.cameraxgallery.processing

expect object ImageEditor {
    suspend fun applyBrightness(bitmapData: ByteArray, factor: Float=0F): ByteArray
    suspend fun applyGreyscale(bitmapData: ByteArray): ByteArray
    suspend fun applySepia(bitmapData: ByteArray): ByteArray
    suspend fun applyContrast(bitmapData: ByteArray, factor: Float=1F): ByteArray
    suspend fun applySaturation(bitmapData: ByteArray, factor: Float=1F): ByteArray
    suspend fun applyExposure(bitmapData: ByteArray, ev: Float=0F): ByteArray
    suspend fun applyTemperature(bitmapData: ByteArray, temperature: Float=0F): ByteArray
    suspend fun applyHue(bitmapData: ByteArray, angleDegrees: Float=0F): ByteArray
}