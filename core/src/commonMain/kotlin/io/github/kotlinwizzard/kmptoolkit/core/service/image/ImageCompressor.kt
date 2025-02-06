package io.github.kotlinwizzard.kmptoolkit.core.service.image


object ImageCompressor {
    fun compressImage(
        content: ByteArray,
        maxBytesCompression: MaxByteCompression = MaxByteCompression(1F, ByteUnit.MB),
    ): ByteArray {
        val unit = maxBytesCompression.unit
        var currentBytes = content
        var contentByteUnit = unit.getUnitFromByteArray(currentBytes)
        val minCompressionRatio = 0.1F
        val maxIterations = 10
        var iteration = 0
        while (contentByteUnit > maxBytesCompression.value && iteration < maxIterations) {
            iteration++
            val sizeRatio = contentByteUnit / maxBytesCompression.value
            val compressionRatio = (1 / sizeRatio).coerceIn(minCompressionRatio, 0.9F)
            currentBytes =
                compressImage(content = currentBytes, compressionRatio = compressionRatio)
            contentByteUnit = unit.getUnitFromByteArray(currentBytes)
            if (compressionRatio <= minCompressionRatio) {
                break
            }
        }

        return currentBytes
    }
}

data class MaxByteCompression(
    val value: Float,
    val unit: ByteUnit,
)

sealed class ByteUnit(
    private val numberInBytes: Int,
) {
    fun getUnitFromByteArray(byteArray: ByteArray) = getUnitFromSize(byteArray.size)

    fun getUnitFromSize(size: Int): Float = size / numberInBytes.toFloat()

    data object Byte : ByteUnit(1)

    data object KB : ByteUnit(1024)

    data object MB : ByteUnit(1024 * 1024)

    data object GB : ByteUnit(1024 * 1024 * 1024)
}

expect fun ImageCompressor.compressImage(
    content: ByteArray,
    compressionRatio: Float,
): ByteArray