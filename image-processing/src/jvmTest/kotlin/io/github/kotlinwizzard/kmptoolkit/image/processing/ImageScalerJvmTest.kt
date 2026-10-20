package io.github.kotlinwizzard.kmptoolkit.image.processing

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageScalerJvmTest {
    @Test
    fun resizesEncodedImageToFit() = runBlocking {
        val source = BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB)
        val sourceBytes = ByteArrayOutputStream().use { output ->
            ImageIO.write(source, "jpeg", output)
            output.toByteArray()
        }

        val resizedBytes = ImageScaler.resizeToFit(sourceBytes, maxWidth = 100, maxHeight = 100)
        val resized = ImageIO.read(ByteArrayInputStream(resizedBytes))

        assertEquals(100, resized.width)
        assertEquals(75, resized.height)
    }

    @Test
    fun cropsEncodedImageToExactSize() = runBlocking {
        val source = BufferedImage(400, 200, BufferedImage.TYPE_INT_RGB)
        val sourceBytes = ByteArrayOutputStream().use { output ->
            ImageIO.write(source, "jpeg", output)
            output.toByteArray()
        }

        val resizedBytes = ImageScaler.resize(
            imageData = sourceBytes,
            width = 100,
            height = 100,
            mode = ImageResizeMode.Crop,
        )
        val resized = ImageIO.read(ByteArrayInputStream(resizedBytes))

        assertEquals(100, resized.width)
        assertEquals(100, resized.height)
    }
}
