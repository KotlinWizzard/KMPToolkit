package io.github.kotlinwizzard.kmptoolkit.image.processing

expect object ImageEditor {
    /**
     * Adjusts the image brightness.
     *
     * @param bitmapData The image as a JPEG ByteArray.
     * @param factor Value range: **-1.0 to 1.0**
     *   - `0.0` = no change
     *   - `> 0.0` = increase brightness
     *   - `< 0.0` = decrease brightness
     * @return The modified image as ByteArray.
     */
    suspend fun applyBrightness(bitmapData: ByteArray, factor: Float=0F): ByteArray

    /**
     * Converts the image to greyscale (black & white).
     *
     * @param bitmapData The image as a JPEG ByteArray.
     * @return The greyscale image as ByteArray.
     */
    suspend fun applyGreyscale(bitmapData: ByteArray): ByteArray

    /**
     * Applies a sepia tone (vintage brownish effect).
     *
     * @param bitmapData The image as a JPEG ByteArray.
     * @return The sepia-toned image as ByteArray.
     */
    suspend fun applySepia(bitmapData: ByteArray): ByteArray

    /**
     * Adjusts the image contrast.
     *
     * @param bitmapData The image as a JPEG ByteArray.
     * @param factor Value range: **-1.0 to 1.0**
     *   - `0.0` = no change
     *   - `> 0.0` = increase contrast
     *   - `< 0.0` = reduce contrast
     * @return The modified image as ByteArray.
     */
    suspend fun applyContrast(bitmapData: ByteArray, factor: Float=0F): ByteArray

    /**
     * Adjusts the image saturation (color intensity).
     *
     * @param bitmapData The image as a JPEG ByteArray.
     * @param factor Value range: **-1.0 to 1.0**
     *   - `0.0` = no change
     *   - `> 0.0` = increase saturation
     *   - `< 0.0` = desaturate toward greyscale
     * @return The modified image as ByteArray.
     */
    suspend fun applySaturation(bitmapData: ByteArray, factor: Float=0F): ByteArray

    /**
     * Adjusts the exposure (light sensitivity) of the image.
     *
     * @param bitmapData The image as a JPEG ByteArray.
     * @param ev Value range: **-1.0 to 1.0** (Exposure Value)
     *   - `0.0` = no change
     *   - `> 0.0` = brighter exposure
     *   - `< 0.0` = darker exposure
     * @return The modified image as ByteArray.
     */
    suspend fun applyExposure(bitmapData: ByteArray, ev: Float=0F): ByteArray

    /**
     * Adjusts the color temperature of the image.
     *
     * @param bitmapData The image as a JPEG ByteArray.
     * @param temperature Value range: **-1.0 to 1.0**
     *   - `0.0` = no change
     *   - `> 0.0` = warmer (yellowish tone)
     *   - `< 0.0` = cooler (bluish tone)
     * @return The modified image as ByteArray.
     */
    suspend fun applyTemperature(bitmapData: ByteArray, temperature: Float=0F): ByteArray

    /**
     * Rotates the image hue (color wheel rotation).
     *
     * @param bitmapData The image as a JPEG ByteArray.
     * @param angleDegrees Value range: **-180 to 180 degrees**
     *   - `0.0` = no change
     *   - Positive values = rotate hue clockwise
     *   - Negative values = rotate hue counterclockwise
     * @return The modified image as ByteArray.
     */
    suspend fun applyHue(bitmapData: ByteArray, angleDegrees: Float=0F): ByteArray
}