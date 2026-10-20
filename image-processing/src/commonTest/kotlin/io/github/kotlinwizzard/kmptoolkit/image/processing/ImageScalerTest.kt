package io.github.kotlinwizzard.kmptoolkit.image.processing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ImageScalerTest {
    @Test
    fun fitPreservesLandscapeAspectRatio() {
        val result = calculateResizeGeometry(4000, 3000, 1200, 800, ImageResizeMode.Fit, false)

        assertEquals(1066, result.outputWidth)
        assertEquals(800, result.outputHeight)
        assertEquals(0, result.offsetX)
        assertEquals(0, result.offsetY)
    }

    @Test
    fun maxSizeDoesNotUpscaleByDefault() {
        val result = calculateResizeGeometry(640, 480, 1024, 1024, ImageResizeMode.Fit, false)

        assertEquals(640, result.outputWidth)
        assertEquals(480, result.outputHeight)
    }

    @Test
    fun cropFillsTargetAndCentersImage() {
        val result = calculateResizeGeometry(1600, 900, 800, 800, ImageResizeMode.Crop, false)

        assertEquals(800, result.outputWidth)
        assertEquals(800, result.outputHeight)
        assertEquals(1422, result.drawWidth)
        assertEquals(800, result.drawHeight)
        assertEquals(-311, result.offsetX)
        assertEquals(0, result.offsetY)
    }

    @Test
    fun stretchProducesExactTargetSize() {
        val result = calculateResizeGeometry(1600, 900, 640, 480, ImageResizeMode.Stretch, false)

        assertEquals(640, result.outputWidth)
        assertEquals(480, result.outputHeight)
    }

    @Test
    fun rejectsInvalidTargetDimensions() {
        assertFailsWith<IllegalArgumentException> {
            calculateResizeGeometry(100, 100, 0, 100, ImageResizeMode.Fit, false)
        }
    }
}
